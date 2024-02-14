package net.borisshoes.arcananovum.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;

public class MiscUtils {
   
   public static ItemStack addLoreLine(ItemStack stack, Text loreLine){
      NbtCompound display = stack.getOrCreateSubNbt("display");
      NbtList loreList = display.getList("Lore", NbtElement.STRING_TYPE);
      loreList.add(NbtString.of(Text.Serialization.toJsonString(Text.empty().append(loreLine))));
      display.put("Lore",loreList);
      stack.setSubNbt("display",display);
      return stack;
   }
   
   public static void giveStacks(PlayerEntity player, ItemStack... stacks){
      returnItems(new SimpleInventory(stacks),player);
   }
   
   public static void returnItems(Inventory inv, PlayerEntity player){
      if(inv == null) return;
      for(int i=0; i<inv.size();i++){
         ItemStack stack = inv.getStack(i).copy();
         if(!stack.isEmpty()){
            inv.setStack(0,ItemStack.EMPTY);
            
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
      if(player.isCreative()) return true;
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
   
   public static boolean inCone(Vec3d center, Vec3d direction, double range, double closeWidth, double farWidth, Vec3d targetPos){
      final double angle = 2*Math.atan2((.5*(farWidth-closeWidth)),range);
      final double ha = angle/2;
      final double ri = closeWidth / (2*Math.sin(ha)); // Cone characteristics from given parameters
      final double ro = farWidth / (2*Math.sin(ha));
      // Delicious trigonometry and linear algebra at its finest
      Vec3d origin = center.add(direction.multiply(-ri*Math.cos(ha)));
      Vec3d u = center.subtract(origin).normalize();           // Linear algebra black magic stuff which
      Vec3d uvr = targetPos.subtract(origin).normalize();      // finds the angle between cone axis and target
      double targetAngle = Math.acos(uvr.dotProduct(u));
      double dist = targetPos.distanceTo(origin);
      double scalProj = targetPos.subtract(center).dotProduct(direction.normalize()); // Scalar projection to see if target is in front of player
      boolean inAngle = targetAngle <= ha;
      boolean inRadius = dist <= ro;
      boolean inFront = scalProj > 0;
      
      return inAngle && inRadius && inFront;
   }
   
   public static double distToLine(Vec3d pos, Vec3d start, Vec3d end){
      final Vec3d line = end.subtract(start);
      final Vec3d distStart = pos.subtract(start);
      final Vec3d distEnd = pos.subtract(end);
      
      if(distStart.dotProduct(line) <= 0) return distStart.length(); // Start is closest
      if(distEnd.dotProduct(line) >= 0) return distEnd.length(); // End is closest
      return (line.crossProduct(distStart)).length() / line.length(); // Infinite line case
   }
   
   public static <T extends Entity> T getClosestEntity(List<T> list, Vec3d pos){
      T closest = null;
      double smallestDist = Double.MAX_VALUE;
      for(T t : list){
         if(t.getPos().distanceTo(pos) < smallestDist){
            closest = t;
            smallestDist = t.getPos().distanceTo(pos);
         }
      }
      return closest;
   }
}
