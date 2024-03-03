package net.borisshoes.arcananovum;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.voicechat.Voicechat;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.areaeffects.AreaEffectTracker;
import net.borisshoes.arcananovum.callbacks.*;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.MagicBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.utils.ConfigUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.UserCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.ACTIVE_ANCHORS;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.LOGIN_CALLBACK_LIST;

public class ArcanaNovum implements ModInitializer, ClientModInitializer {
   
   private static final Logger logger = LogManager.getLogger("Arcana Novum");
   public static final ArrayList<TickTimerCallback> SERVER_TIMER_CALLBACKS = new ArrayList<>();
   public static final ArrayList<Pair<ServerWorld,TickTimerCallback>> WORLD_TIMER_CALLBACKS = new ArrayList<>();
   public static final HashMap<ServerPlayerEntity, WatchedGui> OPEN_GUIS = new HashMap<>();
   public static final HashMap<ServerWorld,ArrayList<ChunkPos>> ANCHOR_CHUNKS = new HashMap<>();
   public static final ArrayList<Pair<BlockEntity,MagicBlockEntity>> ACTIVE_MAGIC_BLOCKS = new ArrayList<>();
   public static final HashMap<String,List<UUID>> PLAYER_ACHIEVEMENT_TRACKER = new HashMap<>();
   public static final HashMap<UUID,Integer> PLAYER_XP_TRACKER = new HashMap<>();
   public static MinecraftServer SERVER = null;
   public static final boolean devMode = false;
   private static final String CONFIG_NAME = "ArcanaNovum.properties";
   public static final String MOD_ID = "arcananovum";
   public static ConfigUtils config;
   public static int DEBUG_VALUE = 0;
   
   @Override
   public void onInitialize(){
      ServerTickEvents.END_WORLD_TICK.register(WorldTickCallback::onWorldTick);
      ServerTickEvents.END_SERVER_TICK.register(TickCallback::onTick);
      //UseItemCallback.EVENT.register(ItemUseCallback::useItem);
      UseEntityCallback.EVENT.register(EntityUseCallback::useEntity);
      //UseBlockCallback.EVENT.register(BlockUseCallback::useBlock);
      AttackBlockCallback.EVENT.register(BlockAttackCallback::attackBlock);
      PlayerBlockBreakEvents.BEFORE.register(BlockBreakCallback::breakBlock);
      ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(EntityKilledCallback::killedEntity);
      AttackEntityCallback.EVENT.register(EntityAttackCallback::attackEntity);
      ServerPlayConnectionEvents.JOIN.register(PlayerConnectionCallback::onPlayerJoin);
      ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionCallback::onPlayerLeave);
      CommandRegistrationCallback.EVENT.register(CommandRegisterCallback::registerCommands);
      ServerEntityEvents.ENTITY_LOAD.register(EntityLoadCallbacks::loadEntity);
      ServerEntityEvents.ENTITY_UNLOAD.register(EntityLoadCallbacks::unloadEntity);
      ServerPlayerEvents.AFTER_RESPAWN.register(PlayerDeathCallback::afterRespawn);
      ServerLifecycleEvents.SERVER_STARTING.register(ServerStartingCallback::serverStarting);
      ServerLifecycleEvents.SERVER_STARTED.register(ServerStartedCallback::serverStarted);
      
      ArcanaRegistry.initialize();

      logger.info("Arcana Surges Through The World!");
      
      config = new ConfigUtils(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME).toFile(), logger, Arrays.asList(new ConfigUtils.IConfigValue[] {
            new ConfigUtils.BooleanConfigValue("doConcentrationDamage", true,
                  new ConfigUtils.Command("Do Concentration Damage is %s", "Do Concentration Damage is now %s")),
            new ConfigUtils.BooleanConfigValue("announceAchievements", true,
                  new ConfigUtils.Command("Announce Achievements is %s", "Announce Achievements is now %s")),
            new ConfigUtils.IntegerConfigValue("ingredientReduction", 1, new ConfigUtils.IntegerConfigValue.IntLimits(1,64),
                  new ConfigUtils.Command("Recipe ingredient counts are divided by %s", "Recipe ingredient count will now be divided by %s")),
      }));
   }
   
   @Override
   public void onInitializeClient(){
      logger.info("Arcana Surges Through Your Client!");
   }
   
   public static boolean addTickTimerCallback(TickTimerCallback callback){
      return SERVER_TIMER_CALLBACKS.add(callback);
   }
   
   public static boolean addTickTimerCallback(ServerWorld world, TickTimerCallback callback){
      return WORLD_TIMER_CALLBACKS.add(new Pair<>(world,callback));
   }
   
   public static boolean addLoginCallback(LoginCallback callback){
      return LOGIN_CALLBACK_LIST.get(callback.getWorld()).addCallback(callback);
   }
   
   public static boolean addActiveAnchor(ServerWorld world, BlockPos pos){
      return ACTIVE_ANCHORS.get(world).addAnchor(pos);
   }
   
   public static boolean removeActiveAnchor(ServerWorld targetWorld, BlockPos pos){
      return ACTIVE_ANCHORS.get(targetWorld).removeAnchor(pos);
   }
   
   public static void devPrint(String msg){
      if(devMode){
         System.out.println(msg);
      }
   }
   
   /**
    * Uses built in logger to log a message
    * @param level 0 - Info | 1 - Warn | 2 - Error | 3 - Fatal | Else - Debug
    * @param msg  The {@code String} to be printed.
    */
   public static void log(int level, String msg){
      switch(level){
         case 0 -> logger.info(msg);
         case 1 -> logger.warn(msg);
         case 2 -> logger.error(msg);
         case 3 -> logger.fatal(msg);
         default -> logger.debug(msg);
      }
   }
}
