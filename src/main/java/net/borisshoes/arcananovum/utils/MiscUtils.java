package net.borisshoes.arcananovum.utils;

import com.google.common.collect.HashMultimap;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.mixins.LivingEntityAccessor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;

public class MiscUtils {
   
   public static boolean removeItemEntities(ServerWorld serverWorld, Box area, Predicate<ItemStack> predicate, int count){
      List<ItemEntity> entities = serverWorld.getEntitiesByClass(ItemEntity.class, area, entity -> predicate.test(entity.getStack()));
      int foundCount = 0;
      for(ItemEntity entity : entities){
         foundCount += entity.getStack().getCount();
         if(foundCount >= count) break;
      }
      if(foundCount < count) return false;
      for(ItemEntity entity : entities){
         ItemStack stack = entity.getStack();
         int stackCount = stack.getCount();
         int toRemove = Math.min(count, stackCount);
         if(toRemove >= stackCount){
            entity.discard();
         }else{
            stack.setCount(stackCount - toRemove);
         }
         count -= toRemove;
         if(count <= 0) break;
      }
      return true;
   }
   
   public static String convertToBase64(String binaryString) {
      int byteLength = (binaryString.length() + 7) / 8;
      byte[] byteArray = new byte[byteLength];
      
      for (int i = 0; i < binaryString.length(); i++) {
         if (binaryString.charAt(i) == '1') {
            byteArray[i / 8] |= (byte) (1 << (7 - (i % 8)));
         }
      }
      
      return Base64.getEncoder().encodeToString(byteArray);
   }
   
   public static Vec3d rotatePoint(Vec3d point, Vec3d direction, float roll){
      float pitch = (float) -Math.toDegrees(Math.asin(direction.y));
      float yaw = (float) -Math.toDegrees(Math.atan2(direction.x, direction.z));
      Quaternionf rotQuat1 = new Quaternionf().fromAxisAngleDeg(new Vector3f(0,1,0),-yaw-90);
      float sideAxisAngle = -(yaw+90) * ((float) Math.PI / 180);
      Vector3f sideAxis = new Vector3f((float) Math.sin(sideAxisAngle), 0, (float) Math.cos(sideAxisAngle));
      Quaternionf rotQuat2 = new Quaternionf().fromAxisAngleDeg(sideAxis,-pitch);
      Quaternionf rotQuat3 = new Quaternionf().fromAxisAngleDeg(direction.toVector3f(),roll);
      Quaternionf rotQuat = rotQuat3.mul(rotQuat2.mul(rotQuat1));
      return new Vec3d(rotQuat.transform(point.toVector3f()));
   }
   
   public static Vec3d rotatePoint(Vec3d point, float yaw, float pitch, float roll){
      Quaternionf rotQuat1 = new Quaternionf().fromAxisAngleDeg(new Vector3f(0,1,0),-yaw-90);
      float sideAxisAngle = -(yaw+90) * ((float) Math.PI / 180);
      Vector3f sideAxis = new Vector3f((float) Math.sin(sideAxisAngle), 0, (float) Math.cos(sideAxisAngle));
      Quaternionf rotQuat2 = new Quaternionf().fromAxisAngleDeg(sideAxis,-pitch);
      Quaternionf rotQuat3 = new Quaternionf().fromAxisAngleDeg(Vec3d.fromPolar(pitch,yaw).toVector3f(),roll);
      Quaternionf rotQuat = rotQuat3.mul(rotQuat2.mul(rotQuat1));
      return new Vec3d(rotQuat.transform(point.toVector3f()));
   }
   
   public static List<ItemStack> getArcanaItemsWithAug(PlayerEntity player, ArcanaItem arcanaItem, ArcanaAugment augment, int level){
      List<ItemStack> stacks = new ArrayList<>();
      PlayerInventory inv = player.getInventory();
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
         
         ArcanaItem arcItem = ArcanaItemUtils.identifyItem(item);
         if(arcItem != null && arcItem.getId().equals(arcanaItem.getId())){
            if(augment == null || ArcanaAugments.getAugmentOnItem(item,augment) >= level){
               stacks.add(item);
            }
         }
         if(arcItem instanceof ArcanistsBelt){
            ContainerComponent containerItems = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
            for(ItemStack stack : containerItems.iterateNonEmpty()){
               ArcanaItem aItem = ArcanaItemUtils.identifyItem(stack);
               if(aItem != null && aItem.getId().equals(arcanaItem.getId())){
                  if(augment == null || ArcanaAugments.getAugmentOnItem(stack,augment) >= level){
                     stacks.add(stack);
                  }
               }
            }
         }
      }
      return stacks;
   }
   
   public static List<ItemStack> getMatchingItemsFromContainerComp(ItemStack container, Item item){
      ContainerComponent containerItems = container.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
      ArrayList<ItemStack> items = new ArrayList<>();
      for(ItemStack stack : containerItems.iterateNonEmpty()){
         if(stack.isOf(item)){
            items.add(stack);
         }
      }
      return items;
   }
   
   // THIS METHOD IS UNTESTED
   public static void inventoryAttributeEffect(LivingEntity livingEntity, RegistryEntry<EntityAttribute> attribute, double value, EntityAttributeModifier.Operation operation, Identifier identifier, boolean remove){
      boolean hasMod = livingEntity.getAttributes().hasModifierForAttribute(attribute,identifier);
      if(hasMod && remove){ // Remove the modifier
         HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> map = HashMultimap.create();
         map.put(attribute, new EntityAttributeModifier(identifier, value, operation));
         livingEntity.getAttributes().removeModifiers(map);
      }else if(!hasMod && !remove){ // Add the modifier
         HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> map = HashMultimap.create();
         map.put(attribute, new EntityAttributeModifier(identifier, value, operation));
         livingEntity.getAttributes().addTemporaryModifiers(map);
      }
   }
   
   public static Pair<ContainerComponent,ItemStack> tryAddStackToContainerComp(ContainerComponent container, int size, ItemStack stack){
      List<ItemStack> beltList = new ArrayList<>(container.stream().toList());
      
      // Fill up existing slots first
      for(ItemStack existingStack : beltList){
         int curCount = stack.getCount();
         if(stack.isEmpty()) break;
         boolean canCombine = !existingStack.isEmpty()
               && ItemStack.areItemsAndComponentsEqual(existingStack, stack)
               && existingStack.isStackable()
               && existingStack.getCount() < existingStack.getMaxCount();
         if(!canCombine) continue;
         int toAdd = Math.min(existingStack.getMaxCount() - existingStack.getCount(),curCount);
         existingStack.increment(toAdd);
         stack.setCount(curCount - toAdd);
      }
      
      int nonEmpty = (int) beltList.stream().filter(s -> !s.isEmpty()).count();
      
      if(!stack.isEmpty() && nonEmpty < size){
         if(nonEmpty == beltList.size()){ // No middle empty slots, append new slot to end
            beltList.add(stack.copyAndEmpty());
         }else{
            for(int i = 0; i < nonEmpty; i++){ // Find middle empty slot to fill
               if(beltList.get(i).isEmpty()){
                  beltList.set(i, stack.copyAndEmpty());
                  break;
               }
            }
         }
      }
      return new Pair<>(ContainerComponent.fromStacks(beltList),stack);
   }
   
   public static void blockWithShield(LivingEntity entity, float damage){
      if(entity.isBlocking()){
         ((LivingEntityAccessor) entity).invokeDamageShield(damage);
         SoundUtils.playSound(entity.getWorld(),entity.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS,1f,1f);
         
         // Activate Shield of Fortitude
         ItemStack activeItem = entity.getActiveItem();
         if(ArcanaItemUtils.identifyItem(activeItem) instanceof ShieldOfFortitude shield){
            shield.shieldBlock(entity, activeItem, damage);
         }
      }
   }
   
   public static LasercastResult lasercast(World world, Vec3d startPos, Vec3d direction, double distance, boolean blockedByShields, Entity entity){
      Vec3d rayEnd = startPos.add(direction.multiply(distance));
      BlockHitResult raycast = world.raycast(new RaycastContext(startPos,rayEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));
      EntityHitResult entityHit;
      List<Entity> hits = new ArrayList<>();
      Box box = new Box(startPos,raycast.getPos());
      box = box.expand(2);
      // Primary hitscan check
      do{
         entityHit = ProjectileUtil.raycast(entity,startPos,raycast.getPos(),box, e -> e instanceof LivingEntity && !e.isSpectator() && !hits.contains(e),distance*2);
         if(entityHit != null && entityHit.getType() == HitResult.Type.ENTITY){
            hits.add(entityHit.getEntity());
         }
      }while(entityHit != null && entityHit.getType() == HitResult.Type.ENTITY);
      
      // Secondary hitscan check to add lenience
      List<Entity> hits2 = world.getOtherEntities(entity, box, (e)-> e instanceof LivingEntity && !e.isSpectator() && !hits.contains(e) && inRange(e,startPos,raycast.getPos()));
      hits.addAll(hits2);
      hits.sort(Comparator.comparingDouble(e->e.distanceTo(entity)));
      
      if(!blockedByShields){
         return new LasercastResult(startPos, raycast.getPos(), direction, hits);
      }
      
      List<Entity> hits3 = new ArrayList<>();
      Vec3d endPoint = raycast.getPos();
      for(Entity hit : hits){
         boolean blocked = false;
         if(hit instanceof ServerPlayerEntity hitPlayer && hitPlayer.isBlocking()){
            double dp = hitPlayer.getRotationVecClient().normalize().dotProduct(direction.normalize());
            blocked = dp < -0.6;
            if(blocked){
               SoundUtils.playSound(world,hitPlayer.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS,1f,1f);
               endPoint = startPos.add(direction.normalize().multiply(direction.normalize().dotProduct(hitPlayer.getPos().subtract(startPos)))).subtract(direction.normalize());
            }
         }
         hits3.add(hit);
         if(blocked){
            break;
         }
      }
      
      return new LasercastResult(startPos,endPoint,direction,hits3);
   }
   
   public record LasercastResult(Vec3d startPos, Vec3d endPos, Vec3d direction, List<Entity> sortedHits){}
   
   public static boolean inRange(Entity e, Vec3d start, Vec3d end){
      double range = .25;
      Box entityBox = e.getBoundingBox().expand(e.getTargetingMargin());
      double len = end.subtract(start).length();
      Vec3d trace = end.subtract(start).normalize().multiply(range);
      int i = 0;
      Vec3d t2 = trace.multiply(i);
      while(t2.length() < len){
         Vec3d t3 = start.add(t2);
         Box hitBox = new Box(t3.x-range,t3.y-range,t3.z-range,t3.x+range,t3.y+range,t3.z+range);
         if(entityBox.intersects(hitBox)){
            return true;
         }
         t2 = trace.multiply(i);
         i++;
      }
      return false;
   }
   
   
   public static List<Pair<List<ItemStack>,ItemStack>> getAllItems(PlayerEntity player){
      List<Pair<List<ItemStack>,ItemStack>> allItems = new ArrayList<>();
      PlayerInventory playerInv = player.getInventory();
      
      List<ItemStack> invItems = new ArrayList<>();
      for(int i=0; i<playerInv.size();i++){
         ItemStack item = playerInv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
         
         invItems.add(item);
         ArcanaItem mitem = ArcanaItemUtils.identifyItem(item);
         if(mitem instanceof ArcanistsBelt belt){
            ContainerComponent beltItems = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
            ArrayList<ItemStack> beltList = new ArrayList<>();
            beltItems.iterateNonEmpty().forEach(beltList::add);
            allItems.add(new Pair<>(beltList,item));
         }
      }
      allItems.add(new Pair<>(invItems,ItemStack.EMPTY));
      return allItems;
   }
   
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
         return UUID.fromString(ArcanaNovum.BLANK_UUID);
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
      
      double r = range* Math.cbrt(random.nextDouble());
      
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
            if(!bl || !stack.isEmpty()){
               itemEntity = player.dropItem(stack, false);
               if(itemEntity == null) continue;
               itemEntity.resetPickupDelay();
               itemEntity.setOwner(player.getUuid());
               continue;
            }
            stack.setCount(1);
            itemEntity = player.dropItem(stack, false);
            if(itemEntity != null){
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
   
   
   public static void removeMaxAbsorption(LivingEntity entity, Identifier id, float amount){
      AttributeContainer attributeContainer = entity.getAttributes();
      EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(EntityAttributes.MAX_ABSORPTION);
      if(entityAttributeInstance == null) return;
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
   
   public static void addMaxAbsorption(LivingEntity entity, Identifier id, double amount){
      AttributeContainer attributeContainer = entity.getAttributes();
      EntityAttributeModifier modifier = new EntityAttributeModifier(id, amount, EntityAttributeModifier.Operation.ADD_VALUE);
      EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(EntityAttributes.MAX_ABSORPTION);
      if(entityAttributeInstance == null) return;
      EntityAttributeModifier existing = entityAttributeInstance.getModifier(id);
      if(existing != null){
         double current = existing.value();
         entityAttributeInstance.removeModifier(id);
         modifier = new EntityAttributeModifier(id, amount+current, EntityAttributeModifier.Operation.ADD_VALUE);
      }
      entityAttributeInstance.addPersistentModifier(modifier);
   }
}
