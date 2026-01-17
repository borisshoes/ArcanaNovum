package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;

public class ProgressAchievement extends ArcanaAchievement{
   
   private int goal;
   private int progress;
   
   public ProgressAchievement(String id, ItemStack displayItem, ArcanaItem arcanaItem, int xpReward, int pointsReward, int goal){
      super(id, 1, displayItem, arcanaItem, xpReward, pointsReward);
      this.progress = 0;
      this.goal = goal;
      setAcquired(false);
   }
   
   protected int getProgress(){
      return progress;
   }
   
   protected boolean setProgress(int progress){
      boolean had = isAcquired();
      this.progress = Mth.clamp(progress,0,goal);
      setAcquired(this.progress >= goal);
      return isAcquired() && !had;
   }
   
   protected int getGoal(){
      return goal;
   }
   
   protected void setGoal(int goal){
      this.goal = goal;
   }
   
   @Override
   public CompoundTag toNbt(){
      CompoundTag nbt = new CompoundTag();
      nbt.putBoolean("acquired",isAcquired());
      nbt.putString("id",id);
      nbt.putInt("type",type);
      nbt.putInt("progress", progress);
      nbt.putInt("goal", goal);
      return nbt;
   }
   
   @Override
   public ProgressAchievement fromNbt(String id, CompoundTag nbt){
      ProgressAchievement ach = (ProgressAchievement) ArcanaAchievements.registry.get(id).makeNew();
      ach.setProgress(nbt.getIntOr("progress", 0));
      ach.setAcquired(nbt.getBooleanOr("acquired", false));
      return ach;
   }
   
   @Override
   public MutableComponent[] getStatusDisplay(ServerPlayer player){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      ProgressAchievement achievement = (ProgressAchievement) profile.getAchievement(getArcanaItem().getId(), id);
      
      MutableComponent[] text = new MutableComponent[achievement != null && achievement.isAcquired() ? 2 : 1];
      DecimalFormat df = new DecimalFormat("##0.00");
      double percent = 100.0 * (achievement == null ? 0 : achievement.getProgress()) / (double) goal;
      text[0] = Component.literal("")
            .append(Component.literal("Progress: ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("" + (achievement == null ? 0 : LevelUtils.readableInt(achievement.getProgress()))).withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" / ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(LevelUtils.readableInt(goal)).withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(df.format(percent)+"%").withStyle(ChatFormatting.BLUE));
      
      if(achievement != null && achievement.isAcquired()){
         text[1] = Component.literal("Achieved!").withStyle(ChatFormatting.AQUA);
      }
      return text;
   }
   
   @Override
   public ProgressAchievement makeNew(){
      return new ProgressAchievement(id, getDisplayItem(), getArcanaItem(), xpReward, pointsReward, goal);
   }
}
