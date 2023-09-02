package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.Map;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.LOGIN_CALLBACK_LIST;

public class PlayerJoinCallback {
   public static void onPlayerJoin(ServerPlayNetworkHandler netHandler, PacketSender sender, MinecraftServer server){
      ServerPlayerEntity player = netHandler.player;
      //log(player.getEntityName()+" has joined the game");
      
      ArrayList<LoginCallback> toBeRemoved = new ArrayList<>();
      for(LoginCallback callback : LOGIN_CALLBACK_LIST.get(server.getWorld(ServerWorld.OVERWORLD)).getCallbacks()){
         if(callback.getPlayer().equals(player.getUuidAsString())){
            //log("Running login callback for "+player.getEntityName()+". ID: "+callback.getId());
            callback.onLogin(netHandler,server);
            toBeRemoved.add(callback);
         }
      }
      for(LoginCallback callback :toBeRemoved){
         LOGIN_CALLBACK_LIST.get(server.getWorld(ServerWorld.OVERWORLD)).removeCallback(callback);
      }
   
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      if(profile.getLevel() == 0){ // Profile needs initialization
         profile.setLevel(1);
      }
      profile.setLevel(LevelUtils.levelFromXp(profile.getXP())); // update level from xp just in case leveling changed
   
   
      // Linked Augments
      int o1 = profile.getAugmentLevel(ArcanaAugments.OVERFLOWING_BOTTOMLESS.id);
      int r1 = profile.getAugmentLevel(ArcanaAugments.RUNIC_BOTTOMLESS.id);
      if(o1 > r1){
         profile.setAugmentLevel(ArcanaAugments.RUNIC_BOTTOMLESS.id,o1);
      }else if(r1 > o1){
         profile.setAugmentLevel(ArcanaAugments.OVERFLOWING_BOTTOMLESS.id,r1);
      }
   
      int o2 = profile.getAugmentLevel(ArcanaAugments.ABUNDANT_AMMO.id);
      int r2 = profile.getAugmentLevel(ArcanaAugments.QUIVER_DUPLICATION.id);
      if(o2 > r2){
         profile.setAugmentLevel(ArcanaAugments.QUIVER_DUPLICATION.id,o2);
      }else if(r2 > o2){
         profile.setAugmentLevel(ArcanaAugments.ABUNDANT_AMMO.id,r2);
      }
      
      int o3 = profile.getAugmentLevel(ArcanaAugments.HARNESS_RECYCLER.id);
      int r3 = profile.getAugmentLevel(ArcanaAugments.SHULKER_RECYCLER.id);
      if(o3 > r3){
         profile.setAugmentLevel(ArcanaAugments.SHULKER_RECYCLER.id,o3);
      }else if(r3 > o3){
         profile.setAugmentLevel(ArcanaAugments.HARNESS_RECYCLER.id,r3);
      }
   
      for(Map.Entry<ArcanaAugment, Integer> entry : profile.getAugments().entrySet()){
         ArcanaAugment baseAug = ArcanaAugments.registry.get(entry.getKey().id);
         if(baseAug != null){
            if(entry.getValue() > baseAug.getTiers().length){
               profile.setAugmentLevel(entry.getKey().id,baseAug.getTiers().length);
            }
         }
      }
      
      if(profile.getMiscData("quiverCD") == null){
         profile.addMiscData("quiverCD", NbtInt.of(0));
      }
      if(profile.getMiscData("runicInvId") == null){
         profile.addMiscData("runicInvId", NbtString.of(""));
      }
      if(profile.getMiscData("arrowInvId") == null){
         profile.addMiscData("arrowInvId",NbtString.of(""));
      }
      if(profile.getMiscData("runicInvSlot") == null){
         profile.addMiscData("runicInvSlot",NbtInt.of(0));
      }
      if(profile.getMiscData("arrowInvSlot") == null){
         profile.addMiscData("arrowInvSlot",NbtInt.of(0));
      }
   }
}
