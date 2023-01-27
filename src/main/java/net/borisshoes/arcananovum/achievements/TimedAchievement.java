package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TimedAchievement extends ArcanaAchievement{
   
   private int goal;
   private int progress;
   private int timeFrame; // In ticks
   private boolean active;
   private NbtCompound data;
   
   public TimedAchievement(String name, String id, ItemStack displayItem, MagicItem magicItem, int xpReward, int pointsReward, String[] description, int goal, int timeFrame){
      super(name, id, 1, displayItem, magicItem, xpReward, pointsReward, description);
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
         Arcananovum.addTickTimerCallback(new GenericTimer(timeFrame, new TimerTask() {
            @Override
            public void run(){
               reset();
            }
         }));
      }
      
      this.progress = MathHelper.clamp(this.progress+progress,0,goal);
      setAcquired(this.progress >= goal);
      return isAcquired() && !had;
   }
   
   protected void reset(){
      if(!isAcquired()){
         this.progress = 0;
         this.active = false;
         this.data = new NbtCompound();
      }
   }
   
   protected int getGoal(){
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
      TimedAchievement ach = (TimedAchievement) ArcanaAchievements.registry.get(id);
      ach.setProgress(nbt.getInt("progress"));
      ach.setActive(nbt.getBoolean("active"));
      ach.setAcquired(nbt.getBoolean("acquired"));
      ach.setData(nbt.getCompound("data"));
      return ach;
   }
   
   @Override
   public MutableText[] getStatusDisplay(ServerPlayerEntity player){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      TimedAchievement achievement = (TimedAchievement) profile.getAchievement(getMagicItem().getId(), id);
      if(achievement == null) return null;
   
      return new MutableText[]{Text.literal("")
            .append(Text.literal("Achieved!").formatted(Formatting.AQUA))};
   }
   
   @Override
   public TimedAchievement makeNew(){
      return new TimedAchievement(name, id, getDisplayItem(), getMagicItem(), xpReward, pointsReward, getDescription(), goal, timeFrame);
   }
}
