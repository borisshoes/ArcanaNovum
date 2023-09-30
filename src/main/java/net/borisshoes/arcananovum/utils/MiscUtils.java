package net.borisshoes.arcananovum.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class MiscUtils {
   public static void returnItems(Inventory inv, PlayerEntity player){
      if(inv == null) return;
      for(int i=0; i<inv.size();i++){
         ItemStack stack = inv.getStack(i);
         if(!stack.isEmpty()){
            
            ItemEntity itemEntity;
            boolean bl = player.getInventory().insertStack(stack);
            if (!bl || !stack.isEmpty()) {
               itemEntity = player.dropItem(stack, false);
               if (itemEntity == null) continue;
               itemEntity.resetPickupDelay();
               itemEntity.setOwner(player.getUuid());
               continue;
            }
            stack.setCount(1);
            itemEntity = player.dropItem(stack, false);
            if (itemEntity != null) {
               itemEntity.setDespawnImmediately();
            }
            player.currentScreenHandler.sendContentUpdates();
         }
      }
   }
   
   public static boolean removeItems(PlayerEntity player, Item item, int count){
      int remaining = count;
      PlayerInventory inv = player.getInventory();
      int[] slots = new int[inv.size()];
      for(int i = 0; i < inv.size() && remaining > 0; i++){
         ItemStack stack = inv.getStack(i);
         int stackCount = stack.getCount();
         if(stack.isOf(item)){
            if(remaining < stackCount){
               slots[i] = remaining;
               remaining = 0;
            }else{
               slots[i] = stackCount;
               remaining -= stackCount;
            }
         }
      }
      if(remaining > 0)return false;
      
      for(int i = 0; i < slots.length; i++){
         if(slots[i] <= 0) continue;
         inv.removeStack(i,slots[i]);
      }
      return true;
   }
   
   public static int calcEssenceFromEnchants(ItemStack itemStack){
      Map<Enchantment,Integer> enchants = EnchantmentHelper.get(itemStack);
      int count = 0;
      for(Map.Entry<Enchantment, Integer> entry : enchants.entrySet()){
         Enchantment enchant = entry.getKey();
         int lvl = entry.getValue();
         count += (int)(calcEssenceValue(enchant,lvl)/2.0);
      }
      return count;
   }
   
   public static int calcEssenceValue(Enchantment enchant, int lvl){
      int rarityMod = switch(enchant.getRarity()){
         case COMMON -> 1;
         case UNCOMMON -> 2;
         case RARE -> 5;
         case VERY_RARE -> 10;
      };
      if(enchant.isCursed()){
         rarityMod = 0;
      }else if(enchant.isTreasure()){
         rarityMod *= 2;
      }
      return lvl*rarityMod;
   }
}
