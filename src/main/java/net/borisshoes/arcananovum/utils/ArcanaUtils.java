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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ArcanaUtils {
   private static final UUID[] ACCOUNTS = new UUID[]{
         UUID.fromString("74814fd2-0992-4565-ac8b-95a9eaa1ba38"),
         UUID.fromString("6b424038-5700-4f04-a004-1f84cfab6291"),
         UUID.fromString("e7c998a7-bf1e-4152-9b63-b8055bdd376a"),
         UUID.fromString("471dc579-2453-4d79-b22c-da33de4e16d0"),
         UUID.fromString("538deb09-dc15-42b4-8361-3d0074dc5450"),
         UUID.fromString("1350e555-7973-484b-a508-7169fdb191ad"),
         UUID.fromString("6c1f2e8b-897d-4141-b3a5-4a447b30a919")
   };
   
   public static boolean isGodAccount(UUID uuid){
      return Arrays.asList(ACCOUNTS).contains(uuid);
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
