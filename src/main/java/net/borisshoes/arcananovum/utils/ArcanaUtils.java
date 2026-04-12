package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

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
   
   /**
    * Note that this does not give a mutable item stack for items in containers!
    */
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
            for(ItemStackTemplate template : containerItems.nonEmptyItems()){
               ItemStack stack = template.create();
               ArcanaItem aItem = ArcanaItemUtils.identifyItem(stack);
               if(aItem != null && aItem.getId().equals(arcanaItem.getId())){
                  stacks.add(stack);
               }
            }
         }
      }
      return stacks;
   }
   
   /**
    * Note that this does not give a mutable item stack for items in containers!
    */
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
            for(ItemStackTemplate template : containerItems.nonEmptyItems()){
               ItemStack stack = template.create();
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
   
   public static int calcEssenceFromEnchants(ItemStack itemStack){
      ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
      int count = 0;
      for(Holder<Enchantment> entry : comp.keySet()){
         int lvl = comp.getLevel(entry);
         count += calcEssenceValue(entry, lvl);
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
