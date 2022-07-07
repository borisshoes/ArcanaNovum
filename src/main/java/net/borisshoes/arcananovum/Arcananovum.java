package net.borisshoes.arcananovum;

import net.borisshoes.arcananovum.callbacks.*;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.Utils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.IntStream;

import static net.borisshoes.arcananovum.cardinalcomponents.LoginCallbackComponentInitializer.LOGIN_CALLBACK_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.MagicBlocksComponentInitializer.MAGIC_BLOCK_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class Arcananovum implements ModInitializer {
   
   private static final Logger logger = LogManager.getLogger("Arcana Novum");
   public static final ArrayList<TickTimerCallback> TIMER_CALLBACKS = new ArrayList<>();
   public static final boolean devMode = true;
   
   @Override
   public void onInitialize(){
      ServerTickEvents.END_WORLD_TICK.register(WorldTickCallback::onWorldTick);
      ServerTickEvents.END_SERVER_TICK.register(TickCallback::onTick);
      UseItemCallback.EVENT.register(ItemUseCallback::useItem);
      UseBlockCallback.EVENT.register(BlockUseCallback::useBlock);
      PlayerBlockBreakEvents.BEFORE.register(BlockBreakCallback::breakBlock);
      ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(EntityKilledCallback::killedEntity);
      AttackEntityCallback.EVENT.register(EntityAttackCallback::attackEntity);
      ServerPlayConnectionEvents.JOIN.register(PlayerJoinCallback::onPlayerJoin);
      CommandRegistrationCallback.EVENT.register(CommandRegisterCallback::registerCommands);
   
      logger.info("Initializing Arcana Novum");
   }
   
   public static boolean addTickTimerCallback(TickTimerCallback callback){
      return TIMER_CALLBACKS.add(callback);
   }
   
   public static boolean addLoginCallback(LoginCallback callback){
      return LOGIN_CALLBACK_LIST.get(callback.getWorld()).addCallback(callback);
   }
   
   public static void log(String msg){
      if(devMode){
         System.out.println(msg);
      }
   }
}
