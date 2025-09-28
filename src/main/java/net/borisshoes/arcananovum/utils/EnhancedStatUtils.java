package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnhancedStatUtils {
   public static String ENHANCED_STAT_TAG = "stardust_enhanced";
   
   public static boolean isItemEnhanceable(ItemStack stack){
      return (stack.isIn(ItemTags.ARMOR_ENCHANTABLE)) ||
             (stack.isIn(ItemTags.WEAPON_ENCHANTABLE) || stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.AXES)) ||
             (stack.contains(DataComponentTypes.TOOL)) ||
             (stack.contains(DataComponentTypes.MAX_DAMAGE));
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
      
      AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
      List<AttributeModifiersComponent.Entry> attributeList = new ArrayList<>();
      
      // Strip enhanced stats
      for(AttributeModifiersComponent.Entry entry : modifiers.modifiers()){
         RegistryEntry<EntityAttribute> attribute = entry.attribute();
         EntityAttributeModifier modifier = entry.modifier();
         AttributeModifierSlot slot = entry.slot();
         
         if(!modifier.id().toString().contains(ENHANCED_STAT_TAG)){
            attributeList.add(entry);
         }
      }
      
      if(stack.contains(DataComponentTypes.TOOL)){
         ToolComponent comp = stack.getItem().getDefaultStack().get(DataComponentTypes.TOOL);
         
         List<ToolComponent.Rule> newRules = new ArrayList<>();
         for(ToolComponent.Rule rule : comp.rules()){
            if(rule.speed().isPresent()){
               float newSpeed = rule.speed().get();
               newRules.add(new ToolComponent.Rule(rule.blocks(), Optional.of(newSpeed), rule.correctForDrops()));
            }else{
               newRules.add(rule);
            }
         }
         
         ToolComponent newComp = new ToolComponent(newRules,comp.defaultMiningSpeed(),comp.damagePerBlock(),comp.canDestroyBlocksInCreative());
         stack.set(DataComponentTypes.TOOL,newComp);
      }
      if(stack.contains(DataComponentTypes.MAX_DAMAGE)){
         int maxDamage = stack.getItem().getDefaultStack().getMaxDamage();
         stack.set(DataComponentTypes.MAX_DAMAGE,maxDamage);
      }
      
      
      AttributeModifiersComponent newComponent = new AttributeModifiersComponent(attributeList);
      stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS,newComponent);
      
      if(removeTag){
         ArcanaItem.removeProperty(stack,ENHANCED_STAT_TAG);
      }
      
      if(!ArcanaItemUtils.isArcane(stack)){
         LoreComponent lore = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
         List<Text> lines = new ArrayList<>();
         List<Text> styledLines = new ArrayList<>();
         
         for(Text line : lore.lines()){
            if(!line.getString().contains("Stardust Infusion: ")){
               lines.add(line);
            }
         }
         
         for(Text line : lore.styledLines()){
            if(!line.getString().contains("Stardust Infusion: ")){
               styledLines.add(line);
            }
         }
         
         stack.set(DataComponentTypes.LORE, new LoreComponent(lines, styledLines));
      }else{
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
         arcanaItem.buildItemLore(stack,ArcanaNovum.SERVER);
      }
   }
   
   public static void enhanceItem(ItemStack stack, double percentile){
      AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
      List<AttributeModifiersComponent.Entry> attributeList = new ArrayList<>();
      
      // Strip old enhanced stats
      for(AttributeModifiersComponent.Entry entry : modifiers.modifiers()){
         RegistryEntry<EntityAttribute> attribute = entry.attribute();
         EntityAttributeModifier modifier = entry.modifier();
         AttributeModifierSlot slot = entry.slot();
         
         if(!modifier.id().toString().contains(ENHANCED_STAT_TAG)){
            attributeList.add(entry);
         }
      }
      
      boolean enhanced = false;
      if(stack.isIn(ItemTags.ARMOR_ENCHANTABLE) && stack.contains(DataComponentTypes.EQUIPPABLE)){
         EquippableComponent equipComp = stack.get(DataComponentTypes.EQUIPPABLE);
         
         double newArmor = 5 * percentile;
         double newToughness = 5 * percentile;
         double newKbRes = 0.15 * percentile;
         double maxHpBoost = percentile >= 0.95 ? percentile*5 : 0;
         String stat_tag = ENHANCED_STAT_TAG +"_"+ equipComp.slot().getName();
         
         attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.ARMOR,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,stat_tag),newArmor,EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.forEquipmentSlot(equipComp.slot())));
         attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.ARMOR_TOUGHNESS,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,stat_tag),newToughness,EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.forEquipmentSlot(equipComp.slot())));
         attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.KNOCKBACK_RESISTANCE,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,stat_tag),newKbRes,EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.forEquipmentSlot(equipComp.slot())));
         attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.MAX_HEALTH,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,stat_tag),maxHpBoost,EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.forEquipmentSlot(equipComp.slot())));
         enhanced = true;
      }
      
      if(stack.isIn(ItemTags.WEAPON_ENCHANTABLE) || stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.AXES)){
         double newAttackSpeed = percentile >= 0.5 ? 0.5*(2*percentile-1) : 0;
         double newAttackDamage = 5 * percentile;
         
         attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.ATTACK_DAMAGE,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,ENHANCED_STAT_TAG),newAttackDamage,EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.MAINHAND));
         attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.ATTACK_SPEED,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,ENHANCED_STAT_TAG),newAttackSpeed,EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.MAINHAND));
         enhanced = true;
      }
      
      if(stack.contains(DataComponentTypes.TOOL)){
         double speedBuff = 1 + (1.25 * percentile*percentile);
         ToolComponent comp = stack.getItem().getDefaultStack().get(DataComponentTypes.TOOL);
         
         List<ToolComponent.Rule> newRules = new ArrayList<>();
         for(ToolComponent.Rule rule : comp.rules()){
            if(rule.speed().isPresent()){
               float newSpeed = Math.min(Float.MAX_VALUE,(float)((rule.speed().get() + 4*percentile) * speedBuff));
               newRules.add(new ToolComponent.Rule(rule.blocks(), Optional.of(newSpeed), rule.correctForDrops()));
            }else{
               newRules.add(rule);
            }
         }
         
         ToolComponent newComp = new ToolComponent(newRules,comp.defaultMiningSpeed(),comp.damagePerBlock(),comp.canDestroyBlocksInCreative());
         stack.set(DataComponentTypes.TOOL,newComp);
         enhanced = true;
      }
      if(stack.contains(DataComponentTypes.MAX_DAMAGE)){
         double durabilityBuff = 1 + (0.5 * percentile*percentile);
         int maxDamage = stack.getItem().getDefaultStack().getMaxDamage();
         stack.set(DataComponentTypes.MAX_DAMAGE,(int)(maxDamage*durabilityBuff));
         enhanced = true;
      }
      
      
      if(enhanced){
         AttributeModifiersComponent newComponent = new AttributeModifiersComponent(attributeList);
         stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS,newComponent);
         
         ArcanaItem.putProperty(stack,ENHANCED_STAT_TAG,percentile);
         
         if(!ArcanaItemUtils.isArcane(stack)){
            LoreComponent lore = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
            List<Text> lines = new ArrayList<>();
            List<Text> styledLines = new ArrayList<>();
            
            for(Text line : lore.lines()){
               if(!line.getString().contains("Stardust Infusion: ")){
                  lines.add(line);
               }
            }
            
            for(Text line : lore.styledLines()){
               if(!line.getString().contains("Stardust Infusion: ")){
                  styledLines.add(line);
               }
            }
            
            DecimalFormat df = new DecimalFormat("#0.00");
            df.setRoundingMode(RoundingMode.DOWN);
            Text line = TextUtils.removeItalics(Text.literal("")
                  .append(Text.literal("Stardust Infusion: ").formatted(Formatting.YELLOW))
                  .append(Text.literal(df.format(percentile*100)+"%").formatted(Formatting.GOLD)));
            
            lines.add(line);
            styledLines.add(line);
            
            stack.set(DataComponentTypes.LORE, new LoreComponent(lines, styledLines));
         }else{
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
            arcanaItem.buildItemLore(stack,ArcanaNovum.SERVER);
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
