package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;

public class ProgressAchievement extends ArcanaAchievement{
   
   private int goal;
   private int progress;
   
   public ProgressAchievement(String name, String id, ItemStack displayItem, ArcanaItem arcanaItem, int xpReward, int pointsReward, String[] description, int goal){
      super(name, id, 1, displayItem, arcanaItem, xpReward, pointsReward, description);
      this.progress = 0;
      this.goal = goal;
      setAcquired(false);
   }
   
   protected int getProgress(){
      return progress;
   }
   
   protected boolean setProgress(int progress){
      boolean had = isAcquired();
      this.progress = MathHelper.clamp(progress,0,goal);
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
   public NbtCompound toNbt(){
      NbtCompound nbt = new NbtCompound();
      nbt.putBoolean("acquired",isAcquired());
      nbt.putString("name",name);
      nbt.putInt("type",type);
      nbt.putInt("progress", progress);
      nbt.putInt("goal", goal);
      return nbt;
   }
   
   @Override
   public ProgressAchievement fromNbt(String id, NbtCompound nbt){
      ProgressAchievement ach = (ProgressAchievement) ArcanaAchievements.registry.get(id).makeNew();
      ach.setProgress(nbt.getInt("progress"));
      ach.setAcquired(nbt.getBoolean("acquired"));
      return ach;
   }
   
   @Override
   public MutableText[] getStatusDisplay(ServerPlayerEntity player){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      ProgressAchievement achievement = (ProgressAchievement) profile.getAchievement(getArcanaItem().getId(), id);
      
      MutableText[] text = new MutableText[achievement != null && achievement.isAcquired() ? 2 : 1];
      DecimalFormat df = new DecimalFormat("##0.00");
      double percent = 100.0 * (achievement == null ? 0 : achievement.getProgress()) / (double) goal;
      text[0] = Text.literal("")
            .append(Text.literal("Progress: ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("" + (achievement == null ? 0 : achievement.getProgress())).formatted(Formatting.AQUA))
            .append(Text.literal(" / ").formatted(Formatting.AQUA))
            .append(Text.literal("" + goal).formatted(Formatting.AQUA))
            .append(Text.literal(" | ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(df.format(percent)+"%").formatted(Formatting.BLUE));
      
      if(achievement != null && achievement.isAcquired()){
         text[1] = Text.literal("Achieved!").formatted(Formatting.AQUA);
      }
      return text;
   }
   
   @Override
   public ProgressAchievement makeNew(){
      return new ProgressAchievement(name, id, getDisplayItem(), getArcanaItem(), xpReward, pointsReward, getDescription(), goal);
   }
}
