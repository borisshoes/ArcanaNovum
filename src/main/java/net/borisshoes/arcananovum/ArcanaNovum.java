package net.borisshoes.arcananovum;

import net.borisshoes.arcananovum.callbacks.*;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.events.ArcanaEvent;
import net.borisshoes.arcananovum.utils.ConfigUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.ACTIVE_ANCHORS;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.LOGIN_CALLBACK_LIST;

public class ArcanaNovum implements ModInitializer, ClientModInitializer {
   
   private static final Logger LOGGER = LogManager.getLogger("Arcana Novum");
   public static final ArrayList<TickTimerCallback> SERVER_TIMER_CALLBACKS = new ArrayList<>();
   public static final ArrayList<Pair<ServerWorld,TickTimerCallback>> WORLD_TIMER_CALLBACKS = new ArrayList<>();
   public static final HashMap<ServerWorld,ArrayList<ChunkPos>> ANCHOR_CHUNKS = new HashMap<>();
   public static final HashMap<Pair<BlockEntity, ArcanaBlockEntity>,Integer> ACTIVE_ARCANA_BLOCKS = new HashMap<>();
   public static final HashMap<String,List<UUID>> PLAYER_ACHIEVEMENT_TRACKER = new HashMap<>();
   public static final HashMap<UUID,Integer> PLAYER_XP_TRACKER = new HashMap<>();
   public static final HashMap<ServerPlayerEntity, Pair<Vec3d,Vec3d>> PLAYER_MOVEMENT_TRACKER = new HashMap<>();
   public static final List<ArcanaEvent> RECENT_EVENTS = new ArrayList<>();
   public static final List<UUID> TOTEM_KILL_LIST = new ArrayList<>();
   public static MinecraftServer SERVER = null;
   public static final boolean DEV_MODE = false;
   private static final String CONFIG_NAME = "ArcanaNovum.properties";
   public static final String MOD_ID = "arcananovum";
   public static final String BLANK_UUID = "00000000-0000-4000-8000-000000000000";
   public static ConfigUtils CONFIG;
   public static int DEBUG_VALUE = 0;
   
   @Override
   public void onInitialize(){
      CONFIG = new ConfigUtils(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME).toFile(), LOGGER, ArcanaRegistry.CONFIG_SETTINGS.stream().map(ArcanaConfig.ConfigSetting::makeConfigValue).collect(Collectors.toList()));
      ArcanaRegistry.initialize();
      
      ServerTickEvents.END_WORLD_TICK.register(WorldTickCallback::onWorldTick);
      ServerTickEvents.END_SERVER_TICK.register(TickCallback::onTick);
      UseEntityCallback.EVENT.register(EntityUseCallback::useEntity);
      AttackBlockCallback.EVENT.register(BlockAttackCallback::attackBlock);
      PlayerBlockBreakEvents.BEFORE.register(BlockBreakCallback::breakBlock);
      AttackEntityCallback.EVENT.register(EntityAttackCallback::attackEntity);
      ServerPlayConnectionEvents.JOIN.register(PlayerConnectionCallback::onPlayerJoin);
      ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionCallback::onPlayerLeave);
      CommandRegistrationCallback.EVENT.register(CommandRegisterCallback::registerCommands);
      ServerEntityEvents.ENTITY_LOAD.register(EntityLoadCallbacks::loadEntity);
      ServerEntityEvents.ENTITY_UNLOAD.register(EntityLoadCallbacks::unloadEntity);
      ServerPlayerEvents.AFTER_RESPAWN.register(PlayerDeathCallback::afterRespawn);
      ServerPlayerEvents.COPY_FROM.register(PlayerDeathCallback::onPlayerCopy);
      ServerLifecycleEvents.SERVER_STARTING.register(ServerStartingCallback::serverStarting);
      ServerLifecycleEvents.SERVER_STARTED.register(ServerStartedCallback::serverStarted);
      
      LOGGER.info("Arcana Surges Through The World!");
   }
   
   @Override
   public void onInitializeClient(){
      LOGGER.info("Arcana Surges Through Your Client!");
   }
   
   public static <T extends ArcanaEvent> List<T> getEventsOfType(Class<T> eventType){
      List<T> filteredEvents = new ArrayList<>();
      for (ArcanaEvent event : RECENT_EVENTS) {
         if (eventType.isInstance(event)) {
            filteredEvents.add(eventType.cast(event));
         }
      }
      return filteredEvents;
   }
   
   public static void addArcanaEvent(ArcanaEvent event){
      RECENT_EVENTS.add(event);
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
   
   public static boolean addActiveBlock(Pair<BlockEntity,ArcanaBlockEntity> pair){
      List<Pair<BlockEntity,ArcanaBlockEntity>> existing = ACTIVE_ARCANA_BLOCKS.keySet().stream().filter(p -> p.getRight().getUuid().equals(pair.getRight().getUuid())).toList();
      existing.forEach(ACTIVE_ARCANA_BLOCKS::remove);
      ACTIVE_ARCANA_BLOCKS.put(pair,30);
      return existing.isEmpty();
   }
   
   public static IArcanaProfileComponent data(PlayerEntity player){
      if(player == null){
         return null;
      }
      try{
         return PLAYER_DATA.get(player);
      }catch(Exception e){
         log(3,"Failed to get Arcane Profile for "+player.getNameForScoreboard() + " ("+player.getUuidAsString()+")");
         log(3,e.toString());
      }
      return null;
   }
   
   public static void devPrint(String msg){
      if(DEV_MODE){
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
         case 0 -> LOGGER.info(msg);
         case 1 -> LOGGER.warn(msg);
         case 2 -> LOGGER.error(msg);
         case 3 -> LOGGER.fatal(msg);
         default -> LOGGER.debug(msg);
      }
   }
}
