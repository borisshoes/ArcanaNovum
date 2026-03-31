package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionalsAchievement extends ArcanaAchievement {
   private final HashMap<String, Boolean> conditions;
   
   public ConditionalsAchievement(String id, ItemStack displayItem, ArcanaItem arcanaItem, int xpReward, int pointsReward, String[] conditions){
      super(id, 2, displayItem, arcanaItem, xpReward, pointsReward);
      this.conditions = new HashMap<>();
      for(String cond : conditions){
         this.conditions.put(cond, false);
      }
      setAcquired(false);
   }
   
   protected boolean setCondition(String cond, boolean set){
      boolean had = isAcquired();
      if(conditions.containsKey(cond)){
         conditions.put(cond, set);
      }
      
      setAcquired(conditions.values().stream().allMatch(b -> b));
      return isAcquired() && !had;
   }
   
   public HashMap<String, Boolean> getConditions(){
      return conditions;
   }
   
   @Override
   public CompoundTag toNbt(){
      CompoundTag nbt = new CompoundTag();
      nbt.putBoolean("acquired", isAcquired());
      nbt.putString("id", id);
      nbt.putInt("type", type);
      CompoundTag condsTag = new CompoundTag();
      for(Map.Entry<String, Boolean> entry : conditions.entrySet()){
         condsTag.putBoolean(entry.getKey(), entry.getValue());
      }
      nbt.put("conditions", condsTag);
      return nbt;
   }
   
   @Override
   public ConditionalsAchievement fromNbt(String id, CompoundTag nbt){
      ConditionalsAchievement ach = (ConditionalsAchievement) ArcanaAchievements.ARCANA_ACHIEVEMENTS.get(id).makeNew();
      ach.conditions.clear();
      ach.setAcquired(nbt.getBooleanOr("acquired", false));
      CompoundTag condsTag = nbt.getCompoundOrEmpty("conditions");
      for(String key : condsTag.keySet()){
         ach.conditions.put(key, condsTag.getBooleanOr(key, false));
      }
      return ach;
   }
   
   @Override
   public MutableComponent[] getStatusDisplay(ServerPlayer player){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      ConditionalsAchievement achievement = (ConditionalsAchievement) profile.getAchievement(this);
      
      if(achievement != null && achievement.isAcquired()){
         return new MutableComponent[]{Component.literal("Achieved!").withStyle(ChatFormatting.AQUA)};
      }
      
      List<MutableComponent> missing = new ArrayList<>();
      missing.add(Component.literal("Missing:").withStyle(ChatFormatting.DARK_AQUA));
      
      if(achievement == null){
         for(String cond : conditions.keySet()){
            missing.add(Component.literal(cond).withStyle(ChatFormatting.AQUA));
         }
      }else{
         for(String cond : conditions.keySet()){
            if(achievement.getConditions().get(cond)) continue;
            missing.add(Component.literal(cond).withStyle(ChatFormatting.AQUA));
         }
      }
      
      return missing.toArray(new MutableComponent[0]);
   }
   
   @Override
   public ConditionalsAchievement makeNew(){
      ConditionalsAchievement ach = new ConditionalsAchievement(id, getDisplayItem(), getArcanaItem(), xpReward, pointsReward, conditions.keySet().toArray(new String[0]));
      ach.setHidden(hidden);
      return ach;
   }
}
