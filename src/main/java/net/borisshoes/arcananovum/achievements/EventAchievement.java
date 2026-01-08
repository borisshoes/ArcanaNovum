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

public class EventAchievement extends ArcanaAchievement{
   
   public EventAchievement(String name, String id, ItemStack displayItem, ArcanaItem arcanaItem, int xpReward, int pointsReward, String[] description){
      super(name, id, 0, displayItem, arcanaItem, xpReward, pointsReward, description);
   }
   
   @Override
   public CompoundTag toNbt(){
      CompoundTag nbt = new CompoundTag();
      nbt.putBoolean("acquired",isAcquired());
      nbt.putString("name",name);
      nbt.putInt("type",type);
      return nbt;
   }
   
   @Override
   public EventAchievement fromNbt(String id, CompoundTag nbt){
      EventAchievement ach = (EventAchievement) ArcanaAchievements.registry.get(id).makeNew();
      ach.setAcquired(nbt.getBooleanOr("acquired", false));
      return ach;
   }
   
   @Override
   public MutableComponent[] getStatusDisplay(ServerPlayer player){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      EventAchievement achievement = (EventAchievement) profile.getAchievement(getArcanaItem().getId(), id);
      if(achievement == null) return null;
      
      return new MutableComponent[]{Component.literal("")
            .append(Component.literal("Achieved!").withStyle(ChatFormatting.AQUA))};
   }
   
   @Override
   public EventAchievement makeNew(){
      return new EventAchievement(name, id, getDisplayItem(), getArcanaItem(), xpReward, pointsReward, getDescription());
   }
}
