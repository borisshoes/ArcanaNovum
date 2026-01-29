package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
