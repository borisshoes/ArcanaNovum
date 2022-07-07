package net.borisshoes.arcananovum;

import net.borisshoes.arcananovum.callbacks.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static net.borisshoes.arcananovum.cardinalcomponents.LoginCallbackComponentInitializer.LOGIN_CALLBACK_LIST;

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
