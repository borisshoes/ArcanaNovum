package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class TimedAchievement extends ArcanaAchievement{
   
   private int goal;
   private int progress;
   private int timeFrame; // In ticks
   private boolean active;
   private NbtCompound data;
   
   public TimedAchievement(String name, String id, ItemStack displayItem, ArcanaItem arcanaItem, int xpReward, int pointsReward, String[] description, int goal, int timeFrame){
      super(name, id, 1, displayItem, arcanaItem, xpReward, pointsReward, description);
      this.progress = 0;
      this.goal = goal;
      this.timeFrame = timeFrame;
      this.active = false;
      this.data = new NbtCompound();
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
   
   public void setData(NbtCompound data){
      this.data = data;
   }
   
   public NbtCompound getData(){
      return data;
   }
   
   // Bypasses timer restriction
   private boolean setProgress(int progress){
      boolean had = isAcquired();
      this.progress = MathHelper.clamp(progress,0,goal);
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
         this.progress = MathHelper.clamp(this.progress+progress,0,goal);
         setAcquired(this.progress >= goal);
      }
      return isAcquired() && !had;
   }
   
   protected void reset(){
      if(!isAcquired()){
         this.progress = 0;
         this.active = false;
         this.data = new NbtCompound();
      }
   }
   
   public int getGoal(){
      return goal;
   }
   
   @Override
   public NbtCompound toNbt(){
      NbtCompound nbt = new NbtCompound();
      nbt.putBoolean("acquired",isAcquired());
      nbt.putString("name",name);
      nbt.putInt("type",type);
      nbt.putInt("progress", progress);
      nbt.putInt("goal", goal);
      nbt.putBoolean("active",active);
      nbt.putInt("timeFrame", timeFrame);
      nbt.put("data",data);
      return nbt;
   }
   
   @Override
   public TimedAchievement fromNbt(String id, NbtCompound nbt){
      TimedAchievement ach = (TimedAchievement) ArcanaAchievements.registry.get(id).makeNew();
      ach.setProgress(nbt.getInt("progress", 0));
      ach.setActive(nbt.getBoolean("active", false));
      ach.setAcquired(nbt.getBoolean("acquired", false));
      ach.setData(nbt.getCompoundOrEmpty("data"));
      return ach;
   }
   
   @Override
   public MutableText[] getStatusDisplay(ServerPlayerEntity player){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      TimedAchievement achievement = (TimedAchievement) profile.getAchievement(getArcanaItem().getId(), id);
      if(achievement == null) return null;
   
      if(achievement.isAcquired()){
         return new MutableText[]{Text.literal("")
               .append(Text.literal("Achieved!").formatted(Formatting.AQUA))};
      }else{
         return null;
      }
   }
   
   @Override
   public TimedAchievement makeNew(){
      return new TimedAchievement(name, id, getDisplayItem(), getArcanaItem(), xpReward, pointsReward, getDescription(), goal, timeFrame);
   }
}
