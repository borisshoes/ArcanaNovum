package net.borisshoes.arcananovum.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ArcanaUtils {
   
   public static final Codec<StackWithSlot> BIG_STACK_CODEC = RecordCodecBuilder.create(
         instance -> instance.group(Codecs.NON_NEGATIVE_INT.fieldOf("Slot").orElse(0).forGetter(StackWithSlot::slot), ItemStack.MAP_CODEC.forGetter(StackWithSlot::stack))
               .apply(instance, StackWithSlot::new)
   );
   
   public static void readBigInventory(ReadView view, DefaultedList<ItemStack> stacks){
      for (StackWithSlot stackWithSlot : view.getTypedListView("Items", BIG_STACK_CODEC)) {
         if (stackWithSlot.isValidSlot(stacks.size())) {
            stacks.set(stackWithSlot.slot(), stackWithSlot.stack());
         }
      }
   }
   
   public static void writeBigInventory(WriteView view, DefaultedList<ItemStack> stacks, boolean setIfEmpty) {
      WriteView.ListAppender<StackWithSlot> listAppender = view.getListAppender("Items", BIG_STACK_CODEC);
      
      for (int i = 0; i < stacks.size(); i++) {
         ItemStack itemStack = stacks.get(i);
         if (!itemStack.isEmpty()) {
            listAppender.add(new StackWithSlot(i, itemStack));
         }
      }
      
      if (listAppender.isEmpty() && !setIfEmpty) {
         view.remove("Items");
      }
   }
   
   public static MutableText getFormattedDimName(RegistryKey<World> worldKey){
      if(worldKey.getValue().toString().equals(ServerWorld.OVERWORLD.getValue().toString())){
         return Text.literal("Overworld").formatted(Formatting.GREEN);
      }else if(worldKey.getValue().toString().equals(ServerWorld.NETHER.getValue().toString())){
         return Text.literal("The Nether").formatted(Formatting.RED);
      }else if(worldKey.getValue().toString().equals(ServerWorld.END.getValue().toString())){
         return Text.literal("The End").formatted(Formatting.DARK_PURPLE);
      }else{
         return Text.literal(worldKey.getValue().toString()).formatted(Formatting.YELLOW);
      }
   }
   
   public static void blockWithShield(LivingEntity entity, float damage){
      if(entity.isBlocking()){
         // TODO Make this better and properly damage shield components
         //SoundUtils.playSound(entity.getWorld(),entity.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS,1f,1f);
         
         // Activate Shield of Fortitude
         ItemStack activeItem = entity.getActiveItem();
         if(ArcanaItemUtils.identifyItem(activeItem) instanceof ShieldOfFortitude shield){
            shield.shieldBlock(entity, activeItem, damage);
         }
      }
   }
   
   public static List<ItemStack> getArcanaItems(PlayerEntity player, ArcanaItem arcanaItem){
      List<ItemStack> stacks = new ArrayList<>();
      PlayerInventory inv = player.getInventory();
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
         
         ArcanaItem arcItem = ArcanaItemUtils.identifyItem(item);
         if(arcItem != null && arcItem.getId().equals(arcanaItem.getId())){
            stacks.add(item);
         }
         if(arcItem instanceof ArcanistsBelt){
            ContainerComponent containerItems = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
            for(ItemStack stack : containerItems.iterateNonEmpty()){
               ArcanaItem aItem = ArcanaItemUtils.identifyItem(stack);
               if(aItem != null && aItem.getId().equals(arcanaItem.getId())){
                  stacks.add(stack);
               }
            }
         }
      }
      return stacks;
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
}
