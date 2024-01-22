package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
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
import java.util.HashMap;
import java.util.Map;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.LOGIN_CALLBACK_LIST;

public class PlayerConnectionCallback {
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
      HashMap<String,ArrayList<ArcanaAugment>> linkedAugments = new HashMap<>();
      HashMap<String,Integer> highestValue = new HashMap<>();
      for(Map.Entry<ArcanaAugment, String> entry : ArcanaAugments.linkedAugments.entrySet()){
         String linkedId = entry.getValue();
         ArcanaAugment augment = entry.getKey();
         
         if(linkedAugments.containsKey(linkedId)){
            linkedAugments.get(linkedId).add(augment);
            int lvl = profile.getAugmentLevel(augment.id);
            if(lvl > highestValue.get(linkedId)){
               highestValue.put(linkedId,lvl);
            }
         }else{
            ArrayList<ArcanaAugment> augs = new ArrayList<>();
            augs.add(augment);
            highestValue.put(linkedId,profile.getAugmentLevel(augment.id));
            linkedAugments.put(linkedId,augs);
         }
      }
      
      for(Map.Entry<String,ArrayList<ArcanaAugment>> entry : linkedAugments.entrySet()){
         ArrayList<ArcanaAugment> augs = entry.getValue();
         for(ArcanaAugment aug : augs){
            profile.setAugmentLevel(aug.id,highestValue.get(entry.getKey()));
         }
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
   
   public static void onPlayerLeave(ServerPlayNetworkHandler handler, MinecraftServer server){
      ServerPlayerEntity player = handler.player;
      if(player.getMaxHealth() > 20 && player.getHealth() > 20){
         ArcanaNovum.addLoginCallback(new MaxHealthLoginCallback(server,player.getUuidAsString(),player.getHealth()));
      }
   }
}
