package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.Arrays;

public class AchievementLoginCallback extends LoginCallback{
   
   private ArrayList<ArcanaAchievement> achievements;
   
   public AchievementLoginCallback(){
      this.id = "achievement_login_callback";
   }
   
   public AchievementLoginCallback(MinecraftServer server, String player, ArcanaAchievement... achievements){
      this.id = "achievement_login_callback";
      this.world = server.getWorld(ServerWorld.OVERWORLD);
      this.playerUUID = player;
      this.achievements = new ArrayList<>(Arrays.stream(achievements).toList());
   }
   
   @Override
   public boolean combineCallbacks(LoginCallback callback){
      if(callback instanceof AchievementLoginCallback newCallback){
         this.achievements.addAll(newCallback.achievements.stream().filter(a -> !this.achievements.contains(a)).toList());
         return true;
      }
      return false;
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         ArcanaNovum.addTickTimerCallback(new GenericTimer(100, ()->{
            for(ArcanaAchievement achievement : achievements){
               ArcanaAchievements.grant(player,achievement.id);
            }
         }));
      }
   }
   
   @Override
   public void setData(NbtCompound data, RegistryWrapper.WrapperLookup registryLookup){
      this.data = data;
      achievements = new ArrayList<>();
      if(data.contains("achievements")){
         NbtList achTag = data.getListOrEmpty("achievements");
         for(NbtElement e : achTag){
            NbtString nbtS = (NbtString) e;
            ArcanaAchievement ach = ArcanaAchievements.registry.get(nbtS.asString().orElse(""));
            if(ach != null) achievements.add(ach);
         }
      }
   }
   
   @Override
   public NbtCompound getData(RegistryWrapper.WrapperLookup registryLookup){
      NbtCompound data = new NbtCompound();
      NbtList achTag = new NbtList();
      for(ArcanaAchievement achievement : achievements){
         achTag.add(NbtString.of(achievement.id));
      }
      data.put("achievements",achTag);
      this.data = data;
      return this.data;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new AchievementLoginCallback();
   }
}