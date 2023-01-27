package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ConditionalsAchievement extends ArcanaAchievement{
   private String[] conditions;
   private boolean[] conditionFlags;
   
   public ConditionalsAchievement(String name, String id, ItemStack displayItem, MagicItem magicItem, int xpReward, int pointsReward, String[] description, String[] conditions){
      super(name, id, 2, displayItem, magicItem, xpReward, pointsReward, description);
      this.conditions = conditions;
      conditionFlags = new boolean[conditions.length];
      setAcquired(false);
   }
   
   protected boolean setCondition(int condInd, boolean set){
      boolean had = isAcquired();
      this.conditionFlags[condInd] = set;
      boolean allConds = true;
      for(int i = 0; i < conditionFlags.length; i++){
         if(!conditionFlags[i]) {
            allConds = false;
            break;
         }
      }
      setAcquired(allConds);
      return isAcquired() && !had;
   }
   
   public String[] getConditions(){
      return conditions;
   }
   
   @Override
   public NbtCompound toNbt(){
      NbtCompound nbt = new NbtCompound();
      nbt.putBoolean("acquired",isAcquired());
      nbt.putString("name",name);
      nbt.putInt("type",type);
      NbtCompound condsTag = new NbtCompound();
      for(int i = 0; i < conditions.length; i++){
         condsTag.putBoolean(conditions[i],conditionFlags[i]);
      }
      nbt.put("conditions",condsTag);
      return nbt;
   }
   
   @Override
   public ConditionalsAchievement fromNbt(String id, NbtCompound nbt){
      ConditionalsAchievement ach = (ConditionalsAchievement) ArcanaAchievements.registry.get(id);
      ach.setAcquired(nbt.getBoolean("acquired"));
      NbtCompound condsTag = nbt.getCompound("conditions");
      String[] conds = new String[condsTag.getKeys().size()];
      boolean[] condFlags = new boolean[conds.length];
      int i = 0;
      for(String key : condsTag.getKeys()){
         conds[i] = key;
         condFlags[i] = condsTag.getBoolean(key);
         i++;
      }
      ach.conditions = conds;
      ach.conditionFlags = condFlags;
      return ach;
   }
   
   @Override
   public MutableText[] getStatusDisplay(ServerPlayerEntity player){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      ConditionalsAchievement achievement = (ConditionalsAchievement) profile.getAchievement(getMagicItem().getId(), id);
      
      if(achievement != null && achievement.isAcquired()) {
         return new MutableText[]{Text.literal("Achieved!").formatted(Formatting.AQUA)};
      }
      
      List<MutableText> missing = new ArrayList<>();
      missing.add(Text.literal("Missing:").formatted(Formatting.DARK_AQUA));
      
      if(achievement == null){
         for(int i = 0; i < conditions.length; i++){
            missing.add(Text.literal(""+conditions[i]).formatted(Formatting.AQUA));
         }
      }else{
         for(int i = 0; i < achievement.conditions.length; i++){
            if(achievement.conditionFlags[i]) continue;
            missing.add(Text.literal(""+conditions[i]).formatted(Formatting.AQUA));
         }
      }
      
      return missing.toArray(new MutableText[0]);
   }
   
   @Override
   public ConditionalsAchievement makeNew(){
      return new ConditionalsAchievement(name, id, getDisplayItem(), getMagicItem(), xpReward, pointsReward, getDescription(), conditions);
   }
}
