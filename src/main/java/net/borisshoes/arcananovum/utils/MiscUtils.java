package net.borisshoes.arcananovum.utils;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class MiscUtils {
   
   public static void outlineGUI(SimpleGui gui, int color, Text borderText){
      outlineGUI(gui,color,borderText,null);
   }
   
   public static void outlineGUI(SimpleGui gui, int color, Text borderText, List<Text> lore){
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         GuiElementBuilder menuItem;
         boolean top = i/9 == 0;
         boolean bottom = i/9 == (gui.getSize()/9 - 1);
         boolean left = i%9 == 0;
         boolean right = i%9 == 8;
         
         if(top){
            if(left){
               menuItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP_LEFT,color));
            }else if(right){
               menuItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP_RIGHT,color));
            }else{
               menuItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,color));
            }
         }else if(bottom){
            if(left){
               menuItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_BOTTOM_LEFT,color));
            }else if(right){
               menuItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_BOTTOM_RIGHT,color));
            }else{
               menuItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_BOTTOM,color));
            }
         }else if(left){
            menuItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_LEFT,color));
         }else if(right){
            menuItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_RIGHT,color));
         }else{
            menuItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,color));
         }
         
         if(borderText.getString().isEmpty()){
            menuItem.hideTooltip();
         }else{
            menuItem.setName(borderText).hideDefaultTooltip();
            if(lore != null && !lore.isEmpty()){
               for(Text text : lore){
                  menuItem.addLoreLine(text);
               }
            }
         }
         
         gui.setSlot(i,menuItem);
      }
   }
   
   public static UUID getUUID(String str){
      try{
         return UUID.fromString(str);
      }catch(Exception e){
         return UUID.fromString("00000000-0000-4000-8000-000000000000");
      }
   }
   
   public static <T> T getWeightedOption(List<Pair<T,Integer>> options){
      return getWeightedOption(options, new Random().nextLong());
   }
   
   public static <T> T getWeightedOption(List<Pair<T,Integer>> options, long seed){
      ArrayList<T> weightedList = new ArrayList<>();
      for(Pair<T, Integer> option : options){
         for(int i = 0; i < option.getRight(); i++){
            weightedList.add(option.getLeft());
         }
      }
      Random random = new Random(seed);
      return weightedList.get(random.nextInt(weightedList.size()));
   }
   
   public static <T> List<Pair<T,Integer>> randomlySpace(List<T> items, int size, long seed){
      Random random = new Random(seed);
      
      List<Integer> remaining = new ArrayList<>();
      List<Pair<T,Integer>> randomized = new ArrayList<>();
      
      for(int i = 0; i < size; i++){
         remaining.add(i);
      }
      
      int i = 0;
      while(i < items.size() && !remaining.isEmpty()){
         int index = random.nextInt(remaining.size());
         randomized.add(new Pair<>(items.get(i),remaining.get(index)));
         remaining.remove(remaining.get(index));
         i++;
      }
      
      return randomized;
   }
   
   public static <T> List<T> listToPage(List<T> items, int page, int pageSize){
      if(page <= 0){
         return items;
      }else if(pageSize*(page-1) >= items.size()){
         return new ArrayList<>();
      }else{
         return items.subList(pageSize*(page-1), Math.min(items.size(), pageSize*page));
      }
   }
   
   public static Vec3d randomSpherePoint(Vec3d center, double range){
      Random random = new Random();
      double x = random.nextGaussian();
      double y = random.nextGaussian();
      double z = random.nextGaussian();
      
      double mag = Math.sqrt(x*x + y*y + z*z);
      x /= mag; y /= mag; z /= mag;
      
      double r = range*Math.cbrt(random.nextDouble());
      
      return new Vec3d(x*r,y*r,z*r).add(center);
   }
   
   public static Vec3d randomSpherePoint(Vec3d center, double maxRange, double minRange){
      Random random = new Random();
      double x = random.nextGaussian();
      double y = random.nextGaussian();
      double z = random.nextGaussian();
      
      double mag = Math.sqrt(x*x + y*y + z*z);
      x /= mag; y /= mag; z /= mag;
      
      double r = maxRange*Math.cbrt(random.nextDouble(minRange / maxRange,1));
      
      return new Vec3d(x*r,y*r,z*r).add(center);
   }
   
   public static ItemStack removeLore(ItemStack stack){
      ItemStack copy = stack.copy();
      copy.remove(DataComponentTypes.LORE);
      return copy;
   }
   
   public static RegistryEntry<Enchantment> getEnchantment(RegistryKey<Enchantment> key){
      if(ArcanaNovum.SERVER == null){
         ArcanaNovum.log(2,"Attempted to access Enchantment "+key.toString()+" before DRM is available");
         return null;
      }
      Optional<RegistryEntry.Reference<Enchantment>> opt = ArcanaNovum.SERVER.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(key);
      return opt.orElse(null);
   }
   
   public static RegistryEntry<Enchantment> getEnchantment(DynamicRegistryManager drm, RegistryKey<Enchantment> key){
      Optional<RegistryEntry.Reference<Enchantment>> opt = drm.getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(key);
      return opt.orElse(null);
   }
   
   public static ItemEnchantmentsComponent makeEnchantComponent(EnchantmentLevelEntry... entries){
      ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
      
      for(EnchantmentLevelEntry entry : entries){
         builder.add(entry.enchantment,entry.level);
      }
      
      return builder.build();
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
      ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(itemStack);
      int count = 0;
      for(RegistryEntry<Enchantment> entry : comp.getEnchantments()){
         int lvl = comp.getLevel(entry);
         count += (int)(calcEssenceValue(entry,lvl)/2.0);
      }
      return count;
   }
   
   public static int calcEssenceValue(RegistryEntry<Enchantment> enchant, int lvl){
      int essence = (int) (0.25 * lvl * enchant.value().getMaxPower(1));
      if(enchant.isIn(EnchantmentTags.CURSE)){
         essence = 0;
      }else if(enchant.isIn(EnchantmentTags.TREASURE)){
         essence *= 2;
      }
      return essence;
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
   
   public static ItemEntity getLargestItemEntity(List<ItemEntity> list){
      ItemEntity largest = null;
      double largestNumber = 0;
      for(ItemEntity itemEntity : list){
         ItemStack itemStack = itemEntity.getStack();
         if(itemStack.getCount() > largestNumber){
            largestNumber = itemStack.getCount();
            largest = itemEntity;
         }
      }
      return largest;
   }
   
   
   public static void removeMaxAbsorption(LivingEntity entity, Identifier id, float amount) {
      AttributeContainer attributeContainer = entity.getAttributes();
      EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(EntityAttributes.MAX_ABSORPTION);
      if (entityAttributeInstance == null) return;
      EntityAttributeModifier existing = entityAttributeInstance.getModifier(id);
      if(existing != null){
         double current = existing.value();
         double newAmount = current-amount;
         entityAttributeInstance.removeModifier(id);
         if(newAmount > 0.01){
            EntityAttributeModifier modifier = new EntityAttributeModifier(id, newAmount, EntityAttributeModifier.Operation.ADD_VALUE);
            entityAttributeInstance.addPersistentModifier(modifier);
         }
      }
   }
   
   public static void addMaxAbsorption(LivingEntity entity, Identifier id, double amount) {
      AttributeContainer attributeContainer = entity.getAttributes();
      EntityAttributeModifier modifier = new EntityAttributeModifier(id, amount, EntityAttributeModifier.Operation.ADD_VALUE);
      EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(EntityAttributes.MAX_ABSORPTION);
      if (entityAttributeInstance == null) return;
      EntityAttributeModifier existing = entityAttributeInstance.getModifier(id);
      if(existing != null){
         double current = existing.value();
         entityAttributeInstance.removeModifier(id);
         modifier = new EntityAttributeModifier(id, amount+current, EntityAttributeModifier.Operation.ADD_VALUE);
      }
      entityAttributeInstance.addPersistentModifier(modifier);
   }
}
