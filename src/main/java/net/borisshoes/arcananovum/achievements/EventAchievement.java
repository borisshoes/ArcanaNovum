package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class EventAchievement extends ArcanaAchievement{
   
   public EventAchievement(String name, String id, ItemStack displayItem, ArcanaItem arcanaItem, int xpReward, int pointsReward, String[] description){
      super(name, id, 0, displayItem, arcanaItem, xpReward, pointsReward, description);
   }
   
   @Override
   public NbtCompound toNbt(){
      NbtCompound nbt = new NbtCompound();
      nbt.putBoolean("acquired",isAcquired());
      nbt.putString("name",name);
      nbt.putInt("type",type);
      return nbt;
   }
   
   @Override
   public EventAchievement fromNbt(String id, NbtCompound nbt){
      EventAchievement ach = (EventAchievement) ArcanaAchievements.registry.get(id).makeNew();
      ach.setAcquired(nbt.getBoolean("acquired"));
      return ach;
   }
   
   @Override
   public MutableText[] getStatusDisplay(ServerPlayerEntity player){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      EventAchievement achievement = (EventAchievement) profile.getAchievement(getArcanaItem().getId(), id);
      if(achievement == null) return null;
      
      return new MutableText[]{Text.literal("")
            .append(Text.literal("Achieved!").formatted(Formatting.AQUA))};
   }
   
   @Override
   public EventAchievement makeNew(){
      return new EventAchievement(name, id, getDisplayItem(), getArcanaItem(), xpReward, pointsReward, getDescription());
   }
}
