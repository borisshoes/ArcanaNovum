package net.borisshoes.arcananovum.utils;

import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

import java.util.Map;
import java.util.UUID;

public class EnhancedStatUtils {
   
   public static boolean isItemEnhanceable(ItemStack stack){
      return stack.isIn(ItemTags.TRIMMABLE_ARMOR) || stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.AXES);
   }
   
   public static double generatePercentile(int stardustCount, int influence){
      double random = Math.min(1,Math.random()+0.125*influence);
      double n = 0.046875 * stardustCount;
      return random*random*random - n*random*random + n*random;
   }
   
   public static void enhanceItem(ItemStack stack, double percentile){
      if(stack.isIn(ItemTags.TRIMMABLE_ARMOR) && stack.getItem() instanceof ArmorItem armorItem){
         double armor = armorItem.getProtection();
         double toughness = armorItem.getToughness();
         double kbRes = armorItem.getMaterial().getKnockbackResistance();
         // linear boost
         double newArmor = armor + 0.5*armor*percentile; // up to 50% extra
         double newToughness = Math.min(6.0, toughness + 4.0*percentile);
         double newKbRes = Math.min(0.25, kbRes + 0.25*percentile);
         // boost max hp for top 5%, up to 2.5 hearts per piece for a total of 1 extra health bar max
         double maxHpBoost = percentile >= 0.95 ? percentile*5 : 0;
         stack.addAttributeModifier(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(UUID.randomUUID(), "Arcana Novum Enhanced Armor", newArmor, EntityAttributeModifier.Operation.ADDITION),armorItem.getSlotType());
         stack.addAttributeModifier(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, new EntityAttributeModifier(UUID.randomUUID(), "Arcana Novum Enhanced Armor Toughness", newToughness, EntityAttributeModifier.Operation.ADDITION),armorItem.getSlotType());
         stack.addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, new EntityAttributeModifier(UUID.randomUUID(), "Arcana Novum Enhanced Knockback Resistance", newKbRes, EntityAttributeModifier.Operation.ADDITION),armorItem.getSlotType());
         stack.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier(UUID.randomUUID(), "Arcana Novum Enhanced Max Health", maxHpBoost, EntityAttributeModifier.Operation.ADDITION),armorItem.getSlotType());
         stack.getOrCreateNbt().putDouble("ArcanaStats",percentile);
      }else if(stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.AXES)){
         Multimap<EntityAttribute, EntityAttributeModifier> attributes = stack.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND);
         double attackSpeed = 0;
         double attackDamage = 1;
         for(Map.Entry<EntityAttribute, EntityAttributeModifier> entry : attributes.entries()){
            if(entry.getKey() == EntityAttributes.GENERIC_ATTACK_DAMAGE){
               attackDamage = entry.getValue().getValue();
            }else if(entry.getKey() == EntityAttributes.GENERIC_ATTACK_SPEED){
               attackSpeed = entry.getValue().getValue();
            }
         }
         // linear boost, up to 50% in each category
         double newAttackSpeed = attackSpeed + 0.5*Math.abs(attackSpeed)*percentile; // Attack speed is usually negative for weapons
         double newAttackDamage = attackDamage + 0.5*attackDamage*percentile - 1;
         stack.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(UUID.randomUUID(), "Arcana Novum Enhanced Damage", newAttackDamage, EntityAttributeModifier.Operation.ADDITION),EquipmentSlot.MAINHAND);
         stack.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.randomUUID(), "Arcana Novum Enhanced Attack Speed", newAttackSpeed, EntityAttributeModifier.Operation.ADDITION),EquipmentSlot.MAINHAND);
         stack.getOrCreateNbt().putDouble("ArcanaStats",percentile);
      }
   }
   
   public static boolean isEnhanced(ItemStack stack){
      return stack.hasNbt() && stack.getNbt().contains("ArcanaStats");
   }
   
   public static double combineStats(double p1, double p2){
      double max = Math.max(p1,p2);
      double min = Math.min(p1,p2);
      double magic = 0.03;
      double increased = max + min*((2*magic) / (max*max+magic));
      return Math.min(1,increased);
   }
}
