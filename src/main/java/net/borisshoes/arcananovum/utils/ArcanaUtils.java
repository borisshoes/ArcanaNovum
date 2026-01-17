package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ArcanaUtils {
   
   /**
    * Merges co-linear line segments in a list of line tuples.
    * Handles: shared endpoints, overlapping segments, and segments where one is inside another.
    */
   public static List<Tuple<Vec3, Vec3>> mergeColinearLines(List<Tuple<Vec3, Vec3>> lines){
      if(lines.size() < 2) return lines;
      
      List<Tuple<Vec3, Vec3>> merged = new ArrayList<>(lines);
      boolean changed = true;
      
      while(changed){
         changed = false;
         outer:
         for(int i = 0; i < merged.size(); i++){
            Tuple<Vec3, Vec3> line1 = merged.get(i);
            for(int j = i + 1; j < merged.size(); j++){
               Tuple<Vec3, Vec3> line2 = merged.get(j);
               
               // Check if lines are co-linear
               if(!areColinear(line1.getA(), line1.getB(), line2.getA(), line2.getB())){
                  continue;
               }
               
               // Lines are co-linear - check if they overlap or touch
               Tuple<Vec3, Vec3> mergedLine = mergeColinearSegments(line1, line2);
               if(mergedLine != null){
                  merged.remove(j);
                  merged.remove(i);
                  merged.add(mergedLine);
                  changed = true;
                  break outer;
               }
            }
         }
      }
      
      return merged;
   }
   
   /**
    * Checks if four points are co-linear (all lie on the same line).
    */
   private static boolean areColinear(Vec3 a, Vec3 b, Vec3 c, Vec3 d){
      // Check if c and d are on the line defined by a-b
      Vec3 ab = b.subtract(a);
      Vec3 ac = c.subtract(a);
      Vec3 ad = d.subtract(a);
      
      // Cross products should be zero if co-linear
      Vec3 cross1 = ab.cross(ac);
      Vec3 cross2 = ab.cross(ad);
      
      return cross1.lengthSqr() < 1e-9 && cross2.lengthSqr() < 1e-9;
   }
   
   /**
    * Attempts to merge two co-linear line segments.
    * Returns the merged segment if they overlap or touch, null otherwise.
    */
   private static Tuple<Vec3, Vec3> mergeColinearSegments(Tuple<Vec3, Vec3> line1, Tuple<Vec3, Vec3> line2){
      Vec3 a1 = line1.getA();
      Vec3 b1 = line1.getB();
      Vec3 a2 = line2.getA();
      Vec3 b2 = line2.getB();
      
      // Find the primary axis (the one with largest extent)
      Vec3 dir = b1.subtract(a1);
      if(dir.lengthSqr() < 1e-9) dir = b2.subtract(a2);
      if(dir.lengthSqr() < 1e-9) return new Tuple<>(a1, a1); // Degenerate case
      
      // Project all points onto the line direction to get 1D coordinates
      double t1a = projectOntoLine(a1, a1, dir);
      double t1b = projectOntoLine(b1, a1, dir);
      double t2a = projectOntoLine(a2, a1, dir);
      double t2b = projectOntoLine(b2, a1, dir);
      
      // Ensure t1a <= t1b and t2a <= t2b
      if(t1a > t1b){
         double tmp = t1a;
         t1a = t1b;
         t1b = tmp;
      }
      if(t2a > t2b){
         double tmp = t2a;
         t2a = t2b;
         t2b = tmp;
      }
      
      // Check if segments overlap or touch (with small epsilon for floating point)
      double epsilon = 1e-9;
      if(t1b < t2a - epsilon || t2b < t1a - epsilon){
         return null; // No overlap
      }
      
      // Merge: take the min and max extents
      double tMin = Math.min(t1a, t2a);
      double tMax = Math.max(t1b, t2b);
      
      // Convert back to Vec3
      Vec3 newA = a1.add(dir.normalize().scale(tMin));
      Vec3 newB = a1.add(dir.normalize().scale(tMax));
      
      return new Tuple<>(newA, newB);
   }
   
   /**
    * Projects a point onto a line defined by origin and direction, returning the scalar parameter.
    */
   private static double projectOntoLine(Vec3 point, Vec3 origin, Vec3 direction){
      Vec3 toPoint = point.subtract(origin);
      double dirLengthSqr = direction.lengthSqr();
      if(dirLengthSqr < 1e-9) return 0;
      return toPoint.dot(direction) / Math.sqrt(dirLengthSqr);
   }
   
   /**
    * Checks if the line segment between two points falls completely inside a set of blocks.
    * Uses ray marching to sample points along the line and verify each is within the block set.
    *
    * @param start  The starting point of the line
    * @param end    The ending point of the line
    * @param blocks The set of BlockPos that define the valid region
    * @return true if the entire line is inside the block set, false otherwise
    */
   public static boolean isLineInsideBlocks(Vec3 start, Vec3 end, Set<BlockPos> blocks, double stepSize){
      if(blocks.isEmpty()) return false;
      
      // Check start and end points
      BlockPos startBlock = BlockPos.containing(start);
      BlockPos endBlock = BlockPos.containing(end);
      if(!blocks.contains(startBlock) || !blocks.contains(endBlock)){
         return false;
      }
      
      // Ray march along the line, checking each point
      Vec3 direction = end.subtract(start);
      double length = direction.length();
      if(length < 0.001) return true; // Start and end are essentially the same point
      
      int steps = (int) Math.ceil(length / stepSize);
      Vec3 step = direction.normalize().scale(stepSize);
      
      Vec3 current = start;
      for(int i = 0; i <= steps; i++){
         BlockPos currentBlock = BlockPos.containing(current);
         if(!blocks.contains(currentBlock)){
            return false;
         }
         current = current.add(step);
      }
      
      return true;
   }
   
   public static void blockWithShield(LivingEntity entity, float damage){
      if(entity.isBlocking()){
         // TODO Make this better and properly damage shield components
         //SoundUtils.playSound(entity.getWorld(),entity.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS,1f,1f);
         
         // Activate Shield of Fortitude
         ItemStack activeItem = entity.getUseItem();
         if(ArcanaItemUtils.identifyItem(activeItem) instanceof ShieldOfFortitude shield){
            shield.shieldBlock(entity, activeItem, damage);
         }
      }
   }
   
   public static List<ItemStack> getArcanaItems(Player player, ArcanaItem arcanaItem){
      List<ItemStack> stacks = new ArrayList<>();
      Inventory inv = player.getInventory();
      for(int i = 0; i < inv.getContainerSize(); i++){
         ItemStack item = inv.getItem(i);
         if(item.isEmpty()){
            continue;
         }
         
         ArcanaItem arcItem = ArcanaItemUtils.identifyItem(item);
         if(arcItem != null && arcItem.getId().equals(arcanaItem.getId())){
            stacks.add(item);
         }
         if(arcItem instanceof ArcanistsBelt){
            ItemContainerContents containerItems = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            for(ItemStack stack : containerItems.nonEmptyItems()){
               ArcanaItem aItem = ArcanaItemUtils.identifyItem(stack);
               if(aItem != null && aItem.getId().equals(arcanaItem.getId())){
                  stacks.add(stack);
               }
            }
         }
      }
      return stacks;
   }
   
   public static List<ItemStack> getArcanaItemsWithAug(Player player, ArcanaItem arcanaItem, ArcanaAugment augment, int level){
      List<ItemStack> stacks = new ArrayList<>();
      Inventory inv = player.getInventory();
      for(int i = 0; i < inv.getContainerSize(); i++){
         ItemStack item = inv.getItem(i);
         if(item.isEmpty()){
            continue;
         }
         
         ArcanaItem arcItem = ArcanaItemUtils.identifyItem(item);
         if(arcItem != null && arcItem.getId().equals(arcanaItem.getId())){
            if(augment == null || ArcanaAugments.getAugmentOnItem(item, augment) >= level){
               stacks.add(item);
            }
         }
         if(arcItem instanceof ArcanistsBelt){
            ItemContainerContents containerItems = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            for(ItemStack stack : containerItems.nonEmptyItems()){
               ArcanaItem aItem = ArcanaItemUtils.identifyItem(stack);
               if(aItem != null && aItem.getId().equals(arcanaItem.getId())){
                  if(augment == null || ArcanaAugments.getAugmentOnItem(stack, augment) >= level){
                     stacks.add(stack);
                  }
               }
            }
         }
      }
      return stacks;
   }
   
   public static List<Tuple<List<ItemStack>, ItemStack>> getAllItems(Player player){
      List<Tuple<List<ItemStack>, ItemStack>> allItems = new ArrayList<>();
      Inventory playerInv = player.getInventory();
      
      List<ItemStack> invItems = new ArrayList<>();
      for(int i = 0; i < playerInv.getContainerSize(); i++){
         ItemStack item = playerInv.getItem(i);
         if(item.isEmpty()){
            continue;
         }
         
         invItems.add(item);
         ArcanaItem mitem = ArcanaItemUtils.identifyItem(item);
         if(mitem instanceof ArcanistsBelt belt){
            ItemContainerContents beltItems = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            ArrayList<ItemStack> beltList = new ArrayList<>();
            beltItems.nonEmptyItems().forEach(beltList::add);
            allItems.add(new Tuple<>(beltList, item));
         }
      }
      allItems.add(new Tuple<>(invItems, ItemStack.EMPTY));
      return allItems;
   }
   
   public static int calcEssenceFromEnchants(ItemStack itemStack){
      ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
      int count = 0;
      for(Holder<Enchantment> entry : comp.keySet()){
         int lvl = comp.getLevel(entry);
         count += (int) (calcEssenceValue(entry, lvl) / 2.0);
      }
      return count;
   }
   
   public static int calcEssenceValue(Holder<Enchantment> enchant, int lvl){
      int essence = (int) (0.25 * lvl * enchant.value().getMaxCost(1));
      if(enchant.is(EnchantmentTags.CURSE)){
         essence = 0;
      }else if(enchant.is(EnchantmentTags.TREASURE)){
         essence *= 2;
      }
      return essence;
   }
}
