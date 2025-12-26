package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.equipment.Equippable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnhancedStatUtils {
   public static String ENHANCED_STAT_TAG = "stardust_enhanced";
   
   public static boolean isItemEnhanceable(ItemStack stack){
      return (stack.is(ItemTags.ARMOR_ENCHANTABLE)) ||
             (stack.is(ItemTags.WEAPON_ENCHANTABLE) || stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)) ||
             (stack.has(DataComponents.TOOL)) ||
             (stack.has(DataComponents.MAX_DAMAGE));
   }
   
   public static double generatePercentile(int stars){
      double random = Math.random();
      double uncapped = 0.2*(stars+1) * random*random + 0.1*stars;
      return Math.min(1,uncapped);
   }
   
   public static double getPercentile(ItemStack stack){
      if(ArcanaItem.hasProperty(stack,ENHANCED_STAT_TAG)){
         return ArcanaItem.getDoubleProperty(stack,ENHANCED_STAT_TAG);
      }else{
         return 0;
      }
   }
   
   public static void stripEnhancements(ItemStack stack, boolean removeTag){
      if(!isEnhanced(stack)) return;
      
      ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
      List<ItemAttributeModifiers.Entry> attributeList = new ArrayList<>();
      
      // Strip enhanced stats
      for(ItemAttributeModifiers.Entry entry : modifiers.modifiers()){
         Holder<Attribute> attribute = entry.attribute();
         AttributeModifier modifier = entry.modifier();
         EquipmentSlotGroup slot = entry.slot();
         
         if(!modifier.id().toString().contains(ENHANCED_STAT_TAG)){
            attributeList.add(entry);
         }
      }
      
      if(stack.has(DataComponents.TOOL)){
         Tool comp = stack.getItem().getDefaultInstance().get(DataComponents.TOOL);
         
         List<Tool.Rule> newRules = new ArrayList<>();
         for(Tool.Rule rule : comp.rules()){
            if(rule.speed().isPresent()){
               float newSpeed = rule.speed().get();
               newRules.add(new Tool.Rule(rule.blocks(), Optional.of(newSpeed), rule.correctForDrops()));
            }else{
               newRules.add(rule);
            }
         }
         
         Tool newComp = new Tool(newRules,comp.defaultMiningSpeed(),comp.damagePerBlock(),comp.canDestroyBlocksInCreative());
         stack.set(DataComponents.TOOL,newComp);
      }
      if(stack.has(DataComponents.MAX_DAMAGE)){
         int maxDamage = stack.getItem().getDefaultInstance().getMaxDamage();
         stack.set(DataComponents.MAX_DAMAGE,maxDamage);
      }
      
      
      ItemAttributeModifiers newComponent = new ItemAttributeModifiers(attributeList);
      stack.set(DataComponents.ATTRIBUTE_MODIFIERS,newComponent);
      
      if(removeTag){
         ArcanaItem.removeProperty(stack,ENHANCED_STAT_TAG);
      }
      
      if(!ArcanaItemUtils.isArcane(stack)){
         ItemLore lore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
         List<Component> lines = new ArrayList<>();
         List<Component> styledLines = new ArrayList<>();
         
         for(Component line : lore.lines()){
            if(!line.getString().contains("Stardust Infusion: ")){
               lines.add(line);
            }
         }
         
         for(Component line : lore.styledLines()){
            if(!line.getString().contains("Stardust Infusion: ")){
               styledLines.add(line);
            }
         }
         
         stack.set(DataComponents.LORE, new ItemLore(lines, styledLines));
      }else{
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
         arcanaItem.buildItemLore(stack, BorisLib.SERVER);
      }
   }
   
   public static void enhanceItem(ItemStack stack, double percentile){
      ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
      List<ItemAttributeModifiers.Entry> attributeList = new ArrayList<>();
      
      // Strip old enhanced stats
      for(ItemAttributeModifiers.Entry entry : modifiers.modifiers()){
         Holder<Attribute> attribute = entry.attribute();
         AttributeModifier modifier = entry.modifier();
         EquipmentSlotGroup slot = entry.slot();
         
         if(!modifier.id().toString().contains(ENHANCED_STAT_TAG)){
            attributeList.add(entry);
         }
      }
      
      boolean enhanced = false;
      if(stack.is(ItemTags.ARMOR_ENCHANTABLE) && stack.has(DataComponents.EQUIPPABLE)){
         Equippable equipComp = stack.get(DataComponents.EQUIPPABLE);
         
         double newArmor = 5 * percentile;
         double newToughness = 5 * percentile;
         double newKbRes = 0.15 * percentile;
         double maxHpBoost = percentile >= 0.95 ? percentile*5 : 0;
         String stat_tag = ENHANCED_STAT_TAG +"_"+ equipComp.slot().getName();
         
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.ARMOR,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,stat_tag),newArmor, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(equipComp.slot())));
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.ARMOR_TOUGHNESS,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,stat_tag),newToughness, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(equipComp.slot())));
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.KNOCKBACK_RESISTANCE,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,stat_tag),newKbRes, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(equipComp.slot())));
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.MAX_HEALTH,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,stat_tag),maxHpBoost, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(equipComp.slot())));
         enhanced = true;
      }
      
      if(stack.is(ItemTags.WEAPON_ENCHANTABLE) || stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)){
         double newAttackSpeed = percentile >= 0.5 ? 0.5*(2*percentile-1) : 0;
         double newAttackDamage = 5 * percentile;
         
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.ATTACK_DAMAGE,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,ENHANCED_STAT_TAG),newAttackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND));
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.ATTACK_SPEED,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,ENHANCED_STAT_TAG),newAttackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND));
         enhanced = true;
      }
      
      if(stack.has(DataComponents.TOOL)){
         double speedBuff = 1 + (1.25 * percentile*percentile);
         Tool comp = stack.getItem().getDefaultInstance().get(DataComponents.TOOL);
         
         List<Tool.Rule> newRules = new ArrayList<>();
         for(Tool.Rule rule : comp.rules()){
            if(rule.speed().isPresent()){
               float newSpeed = Math.min(Float.MAX_VALUE,(float)((rule.speed().get() + 4*percentile) * speedBuff));
               newRules.add(new Tool.Rule(rule.blocks(), Optional.of(newSpeed), rule.correctForDrops()));
            }else{
               newRules.add(rule);
            }
         }
         
         Tool newComp = new Tool(newRules,comp.defaultMiningSpeed(),comp.damagePerBlock(),comp.canDestroyBlocksInCreative());
         stack.set(DataComponents.TOOL,newComp);
         enhanced = true;
      }
      if(stack.has(DataComponents.MAX_DAMAGE)){
         double durabilityBuff = 1 + (0.5 * percentile*percentile);
         int maxDamage = stack.getItem().getDefaultInstance().getMaxDamage();
         stack.set(DataComponents.MAX_DAMAGE,(int)(maxDamage*durabilityBuff));
         enhanced = true;
      }
      
      
      if(enhanced){
         ItemAttributeModifiers newComponent = new ItemAttributeModifiers(attributeList);
         stack.set(DataComponents.ATTRIBUTE_MODIFIERS,newComponent);
         
         ArcanaItem.putProperty(stack,ENHANCED_STAT_TAG,percentile);
         
         if(!ArcanaItemUtils.isArcane(stack)){
            ItemLore lore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
            List<Component> lines = new ArrayList<>();
            List<Component> styledLines = new ArrayList<>();
            
            for(Component line : lore.lines()){
               if(!line.getString().contains("Stardust Infusion: ")){
                  lines.add(line);
               }
            }
            
            for(Component line : lore.styledLines()){
               if(!line.getString().contains("Stardust Infusion: ")){
                  styledLines.add(line);
               }
            }
            
            DecimalFormat df = new DecimalFormat("#0.00");
            df.setRoundingMode(RoundingMode.DOWN);
            Component line = TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Stardust Infusion: ").withStyle(ChatFormatting.YELLOW))
                  .append(Component.literal(df.format(percentile*100)+"%").withStyle(ChatFormatting.GOLD)));
            
            lines.add(line);
            styledLines.add(line);
            
            stack.set(DataComponents.LORE, new ItemLore(lines, styledLines));
         }else{
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
            arcanaItem.buildItemLore(stack,BorisLib.SERVER);
         }
      }
   }
   
   public static boolean isEnhanced(ItemStack stack){
      return ArcanaItem.hasProperty(stack,ENHANCED_STAT_TAG);
   }
   
   public static double combineStats(double p1, double p2){
      double max = Math.max(p1,p2);
      double min = Math.min(p1,p2);
      double magic = 0.07;
      double increased = max + min*((2*magic) / (max*max+magic));
      return Math.min(1,increased);
   }
}
