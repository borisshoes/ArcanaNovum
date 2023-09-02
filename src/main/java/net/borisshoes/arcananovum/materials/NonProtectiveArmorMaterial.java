package net.borisshoes.arcananovum.materials;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class NonProtectiveArmorMaterial implements ArmorMaterial {
   @Override
   public int getDurability(ArmorItem.Type type){
      return switch(type){
         case HELMET -> 13;
         case CHESTPLATE -> 15;
         case LEGGINGS -> 16;
         case BOOTS -> 11;
      };
   }
   
   @Override
   public int getProtection(ArmorItem.Type type){
      return 0;
   }
   
   @Override
   public int getEnchantability() {
      return 0;
   }
   
   @Override
   public SoundEvent getEquipSound() {
      return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
   }
   
   @Override
   public Ingredient getRepairIngredient() {
      return Ingredient.ofItems(Items.LEATHER);
   }
   
   @Override
   public String getName() {
      // Must be all lowercase
      return "nonprotective";
   }
   
   @Override
   public float getToughness() {
      return 0.0F;
   }
   
   @Override
   public float getKnockbackResistance() {
      return 0.0F;
   }
}