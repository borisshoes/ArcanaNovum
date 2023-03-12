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

import java.util.*;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ConditionalsAchievement extends ArcanaAchievement{
   private HashMap<String,Boolean> conditions;
   
   public ConditionalsAchievement(String name, String id, ItemStack displayItem, MagicItem magicItem, int xpReward, int pointsReward, String[] description, String[] conditions){
      super(name, id, 2, displayItem, magicItem, xpReward, pointsReward, description);
      this.conditions = new HashMap<>();
      for(String cond : conditions){
         this.conditions.put(cond,false);
      }
      setAcquired(false);
   }
   
   protected boolean setCondition(String cond, boolean set){
      boolean had = isAcquired();
      if(conditions.containsKey(cond)){
         conditions.put(cond,set);
      }
      
      setAcquired(conditions.values().stream().allMatch(b -> b));
      return isAcquired() && !had;
   }
   
   public HashMap<String, Boolean> getConditions(){
      return conditions;
   }
   
   @Override
   public NbtCompound toNbt(){
      NbtCompound nbt = new NbtCompound();
      nbt.putBoolean("acquired",isAcquired());
      nbt.putString("name",name);
      nbt.putInt("type",type);
      NbtCompound condsTag = new NbtCompound();
      for(Map.Entry<String, Boolean> entry : conditions.entrySet()){
         condsTag.putBoolean(entry.getKey(),entry.getValue());
      }
      nbt.put("conditions",condsTag);
      return nbt;
   }
   
   @Override
   public ConditionalsAchievement fromNbt(String id, NbtCompound nbt){
      conditions.clear();
      ConditionalsAchievement ach = (ConditionalsAchievement) ArcanaAchievements.registry.get(id).makeNew();
      ach.setAcquired(nbt.getBoolean("acquired"));
      NbtCompound condsTag = nbt.getCompound("conditions");
      for(String key : condsTag.getKeys()){
         conditions.put(key,condsTag.getBoolean(key));
      }
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
         for(String cond : conditions.keySet()){
            missing.add(Text.literal(cond).formatted(Formatting.AQUA));
         }
      }else{
         for(String cond : conditions.keySet()){
            if(achievement.getConditions().get(cond)) continue;
            missing.add(Text.literal(cond).formatted(Formatting.AQUA));
         }
      }
      
      return missing.toArray(new MutableText[0]);
   }
   
   @Override
   public ConditionalsAchievement makeNew(){
      return new ConditionalsAchievement(name, id, getDisplayItem(), getMagicItem(), xpReward, pointsReward, getDescription(), conditions.keySet().toArray(new String[0]));
   }
}
