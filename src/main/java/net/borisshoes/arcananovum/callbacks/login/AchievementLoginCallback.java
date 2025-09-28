package net.borisshoes.arcananovum.callbacks.login;

import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.borisshoes.borislib.timers.GenericTimer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AchievementLoginCallback extends LoginCallback {
   
   private ArrayList<ArcanaAchievement> achievements;
   
   public AchievementLoginCallback(){
      super(Identifier.of(MOD_ID,"achievement_login_callback"));
   }
   
   public AchievementLoginCallback(MinecraftServer server, String player, ArcanaAchievement... achievements){
      this();
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
   public boolean canCombine(LoginCallback loginCallback){
      return true;
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         BorisLib.addTickTimerCallback(new GenericTimer(100, ()->{
            for(ArcanaAchievement achievement : achievements){
               ArcanaAchievements.grant(player,achievement.id);
            }
         }));
      }
   }
   
   @Override
   public void setData(NbtCompound data){
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
   public NbtCompound getData(){
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