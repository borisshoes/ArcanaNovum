package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class TimedAchievement extends ArcanaAchievement{
   
   private int goal;
   private int progress;
   private int timeFrame; // In ticks
   private boolean active;
   private CompoundTag data;
   
   public TimedAchievement(String id, ItemStack displayItem, ArcanaItem arcanaItem, int xpReward, int pointsReward, int goal, int timeFrame){
      super(id, 1, displayItem, arcanaItem, xpReward, pointsReward);
      this.progress = 0;
      this.goal = goal;
      this.timeFrame = timeFrame;
      this.active = false;
      this.data = new CompoundTag();
      setAcquired(false);
   }
   
   public int getTimeFrame(){
      return timeFrame;
   }
   
   protected int getProgress(){
      return progress;
   }
   
   public void setActive(boolean active){
      this.active = active;
   }
   
   public boolean isActive(){
      return active;
   }
   
   public void setData(CompoundTag data){
      this.data = data;
   }
   
   public CompoundTag getData(){
      return data;
   }
   
   // Bypasses timer restriction
   private boolean setProgress(int progress){
      boolean had = isAcquired();
      this.progress = Mth.clamp(progress,0,goal);
      setAcquired(this.progress >= goal);
      return isAcquired() && !had;
   }
   
   protected boolean addProgress(int progress){
      boolean had = isAcquired();
      if(!had && this.progress == 0 && !active){
         // Start the timer
         active = true;
         BorisLib.addTickTimerCallback(new GenericTimer(timeFrame, this::reset));
      }
      
      if(active){
         this.progress = Mth.clamp(this.progress+progress,0,goal);
         setAcquired(this.progress >= goal);
      }
      return isAcquired() && !had;
   }
   
   protected void reset(){
      if(!isAcquired()){
         this.progress = 0;
         this.active = false;
         this.data = new CompoundTag();
      }
   }
   
   public int getGoal(){
      return goal;
   }
   
   @Override
   public CompoundTag toNbt(){
      CompoundTag nbt = new CompoundTag();
      nbt.putBoolean("acquired",isAcquired());
      nbt.putString("id",id);
      nbt.putInt("type",type);
      nbt.putInt("progress", progress);
      nbt.putInt("goal", goal);
      nbt.putBoolean("active",active);
      nbt.putInt("timeFrame", timeFrame);
      nbt.put("data",data);
      return nbt;
   }
   
   @Override
   public TimedAchievement fromNbt(String id, CompoundTag nbt){
      TimedAchievement ach = (TimedAchievement) ArcanaAchievements.registry.get(id).makeNew();
      ach.setProgress(nbt.getIntOr("progress", 0));
      ach.setActive(nbt.getBooleanOr("active", false));
      ach.setAcquired(nbt.getBooleanOr("acquired", false));
      ach.setData(nbt.getCompoundOrEmpty("data"));
      return ach;
   }
   
   @Override
   public MutableComponent[] getStatusDisplay(ServerPlayer player){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      TimedAchievement achievement = (TimedAchievement) profile.getAchievement(getArcanaItem().getId(), id);
      if(achievement == null) return null;
   
      if(achievement.isAcquired()){
         return new MutableComponent[]{Component.literal("")
               .append(Component.literal("Achieved!").withStyle(ChatFormatting.AQUA))};
      }else{
         return null;
      }
   }
   
   @Override
   public TimedAchievement makeNew(){
      return new TimedAchievement(id, getDisplayItem(), getArcanaItem(), xpReward, pointsReward, goal, timeFrame);
   }
}
