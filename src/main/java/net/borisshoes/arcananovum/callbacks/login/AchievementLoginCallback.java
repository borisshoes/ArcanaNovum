package net.borisshoes.arcananovum.callbacks.login;

import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.borisshoes.borislib.timers.GenericTimer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.ArrayList;
import java.util.Arrays;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AchievementLoginCallback extends LoginCallback {
   
   private ArrayList<ArcanaAchievement> achievements;
   
   public AchievementLoginCallback(){
      super(Identifier.fromNamespaceAndPath(MOD_ID,"achievement_login_callback"));
   }
   
   public AchievementLoginCallback(MinecraftServer server, String player, ArcanaAchievement... achievements){
      this();
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
   public void onLogin(ServerGamePacketListenerImpl netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayer player = netHandler.player;
      if(player.getStringUUID().equals(playerUUID)){
         BorisLib.addTickTimerCallback(new GenericTimer(100, ()->{
            for(ArcanaAchievement achievement : achievements){
               ArcanaAchievements.grant(player,achievement.id);
            }
         }));
      }
   }
   
   @Override
   public void setData(CompoundTag data){
      this.data = data;
      achievements = new ArrayList<>();
      if(data.contains("achievements")){
         ListTag achTag = data.getListOrEmpty("achievements");
         for(Tag e : achTag){
            StringTag nbtS = (StringTag) e;
            ArcanaAchievement ach = ArcanaAchievements.registry.get(nbtS.asString().orElse(""));
            if(ach != null) achievements.add(ach);
         }
      }
   }
   
   @Override
   public CompoundTag getData(){
      CompoundTag data = new CompoundTag();
      ListTag achTag = new ListTag();
      for(ArcanaAchievement achievement : achievements){
         achTag.add(StringTag.valueOf(achievement.id));
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