package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec3;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnhancedStatUtils {
   public static final String ENHANCED_STAT_TAG = "stardust_enhanced";
   private static final ParticleOptions STARDUST_PARTICLE = new DustParticleOptions(0xf7ed57, 0.61f);
   
   public static boolean isItemEnhanceable(ItemStack stack){
      return (stack.is(ItemTags.ARMOR_ENCHANTABLE)) ||
            (stack.is(ItemTags.WEAPON_ENCHANTABLE) || stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)) ||
            (stack.has(DataComponents.TOOL)) ||
            (stack.has(DataComponents.MAX_DAMAGE));
   }
   
   public static double generatePercentile(int stars, RandomSource random){
      double r = random.nextDouble();
      double uncapped = 0.2 * (stars + 1) * r * r + 0.1 * stars;
      return Math.min(1, uncapped);
   }
   
   public static double getPercentile(ItemStack stack){
      if(ArcanaItem.hasProperty(stack, ENHANCED_STAT_TAG)){
         return ArcanaItem.getDoubleProperty(stack, ENHANCED_STAT_TAG);
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
         
         Tool newComp = new Tool(newRules, comp.defaultMiningSpeed(), comp.damagePerBlock(), comp.canDestroyBlocksInCreative());
         stack.set(DataComponents.TOOL, newComp);
      }
      if(stack.has(DataComponents.MAX_DAMAGE)){
         int maxDamage = stack.getItem().getDefaultInstance().getMaxDamage();
         stack.set(DataComponents.MAX_DAMAGE, maxDamage);
      }
      
      
      ItemAttributeModifiers newComponent = new ItemAttributeModifiers(attributeList);
      stack.set(DataComponents.ATTRIBUTE_MODIFIERS, newComponent);
      
      if(removeTag){
         ArcanaItem.removeProperty(stack, ENHANCED_STAT_TAG);
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
         
         double maxArmor = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.INFUSION_MAX_ARMOR);
         double maxToughness = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.INFUSION_MAX_ARMOR_TOUGHNESS);
         double maxKbRes = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.INFUSION_MAX_KNOCKBACK_RESISTANCE);
         double maxHP = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.INFUSION_MAX_HEALTH_BOOST);
         double newArmor = maxArmor * percentile;
         double newToughness = maxToughness * percentile;
         double newKbRes = maxKbRes * percentile;
         double maxHpBoost = percentile >= 0.95 ? (maxHP / 0.05) * (percentile - 0.95) : 0;
         String stat_tag = ENHANCED_STAT_TAG + "_" + equipComp.slot().getName();
         
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.ARMOR, new AttributeModifier(ArcanaRegistry.arcanaId(stat_tag), newArmor, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(equipComp.slot())));
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(ArcanaRegistry.arcanaId(stat_tag), newToughness, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(equipComp.slot())));
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(ArcanaRegistry.arcanaId(stat_tag), newKbRes, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(equipComp.slot())));
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.MAX_HEALTH, new AttributeModifier(ArcanaRegistry.arcanaId(stat_tag), maxHpBoost, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(equipComp.slot())));
         enhanced = true;
      }
      
      if(stack.is(ItemTags.WEAPON_ENCHANTABLE) || stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)){
         double maxAttackSpeed = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.INFUSION_MAX_ATTACK_SPEED);
         double maxAttackDamage = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.INFUSION_MAX_ATTACK_DAMAGE);
         double newAttackSpeed = percentile >= 0.5 ? maxAttackSpeed * (2 * percentile - 1) : 0;
         double newAttackDamage = maxAttackDamage * percentile;
         
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.ATTACK_DAMAGE, new AttributeModifier(ArcanaRegistry.arcanaId(ENHANCED_STAT_TAG), newAttackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND));
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.ATTACK_SPEED, new AttributeModifier(ArcanaRegistry.arcanaId(ENHANCED_STAT_TAG), newAttackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND));
         enhanced = true;
      }
      
      if(stack.has(DataComponents.TOOL)){
         double maxMiningSpeed = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.INFUSION_MAX_MINING_SPEED);
         double speedBuff = 1 + (maxMiningSpeed * percentile * percentile);
         Tool comp = stack.getItem().getDefaultInstance().get(DataComponents.TOOL);
         
         List<Tool.Rule> newRules = new ArrayList<>();
         for(Tool.Rule rule : comp.rules()){
            if(rule.speed().isPresent()){
               float newSpeed = Math.min(Float.MAX_VALUE, (float) ((rule.speed().get() + 4 * percentile) * speedBuff));
               newRules.add(new Tool.Rule(rule.blocks(), Optional.of(newSpeed), rule.correctForDrops()));
            }else{
               newRules.add(rule);
            }
         }
         
         Tool newComp = new Tool(newRules, comp.defaultMiningSpeed(), comp.damagePerBlock(), comp.canDestroyBlocksInCreative());
         stack.set(DataComponents.TOOL, newComp);
         enhanced = true;
      }
      if(stack.has(DataComponents.MAX_DAMAGE)){
         double maxDurability = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.INFUSION_MAX_DURABILITY);
         double durabilityBuff = 1 + (maxDurability * percentile * percentile);
         int maxDamage = stack.getItem().getDefaultInstance().getMaxDamage();
         stack.set(DataComponents.MAX_DAMAGE, (int) (maxDamage * durabilityBuff));
         enhanced = true;
      }
      
      
      if(enhanced){
         ItemAttributeModifiers newComponent = new ItemAttributeModifiers(attributeList);
         stack.set(DataComponents.ATTRIBUTE_MODIFIERS, newComponent);
         
         ArcanaItem.putProperty(stack, ENHANCED_STAT_TAG, percentile);
         
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
                  .append(Component.literal(df.format(percentile * 100) + "%").withStyle(ChatFormatting.GOLD)));
            
            lines.add(line);
            styledLines.add(line);
            
            stack.set(DataComponents.LORE, new ItemLore(lines, styledLines));
         }else{
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
            arcanaItem.buildItemLore(stack, BorisLib.SERVER);
         }
      }
   }
   
   public static boolean isEnhanced(ItemStack stack){
      if(stack.isEmpty()) return false;
      return ArcanaItem.hasProperty(stack, ENHANCED_STAT_TAG);
   }
   
   public static double combineStats(double p1, double p2){
      double max = Math.max(p1, p2);
      double min = Math.min(p1, p2);
      double magic = 0.07;
      double increased = max + min * ((2 * magic) / (max * max + magic));
      return Math.min(1, increased);
   }
   
   public static void glowInfusedGear(LivingEntity entity, double chance){
      if(!(entity.level() instanceof ServerLevel world)) return;
      ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
      ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
      ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
      ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
      ItemStack body = entity.getItemBySlot(EquipmentSlot.BODY);
      ItemStack mainhand = entity.getMainHandItem();
      ItemStack offhand = entity.getOffhandItem();
      if(helmet.isEmpty() && chest.isEmpty() && legs.isEmpty() && boots.isEmpty() && body.isEmpty() && mainhand.isEmpty() && offhand.isEmpty())
         return;
      
      double width = entity.getBbWidth() / 3;
      double leyway = entity.getBbHeight() / 12.0;
      Vec3 pos = entity.position().add(0, leyway, 0);
      double section = (entity.getEyePosition().y - entity.position().y - leyway) / 4.0;
      ArrayList<Vec3> positions = new ArrayList<>();
      if(entity.random.nextFloat() < chance && EnhancedStatUtils.isEnhanced(helmet)){
         positions.add(new Vec3(pos.x, pos.y + (section * 4), pos.z));
      }
      if(entity.random.nextFloat() < chance && (EnhancedStatUtils.isEnhanced(chest) || EnhancedStatUtils.isEnhanced(body))){
         positions.add(new Vec3(pos.x, pos.y + (section * 3), pos.z));
      }
      if(entity.random.nextFloat() < chance && EnhancedStatUtils.isEnhanced(legs)){
         positions.add(new Vec3(pos.x, pos.y + (section * 2), pos.z));
      }
      if(entity.random.nextFloat() < chance && EnhancedStatUtils.isEnhanced(boots)){
         positions.add(new Vec3(pos.x, pos.y + (section * 1), pos.z));
      }
      if(entity.random.nextFloat() < chance && EnhancedStatUtils.isEnhanced(mainhand)){
         Vec3 newPos = new Vec3(pos.x, pos.y + (section * 2.5), pos.z);
         Vec3 look = entity.getForward().multiply(1, 0, 1).normalize().scale(width * 1.5);
         Vec3 handPos = newPos.add(-look.z, 0, look.x).add(entity.getForward().multiply(1, 0, 1).normalize().scale(width * 3));
         positions.add(handPos);
      }
      if(entity.random.nextFloat() < chance && EnhancedStatUtils.isEnhanced(offhand)){
         Vec3 newPos = new Vec3(pos.x, pos.y + (section * 2.5), pos.z);
         Vec3 look = entity.getForward().multiply(1, 0, 1).normalize().scale(width * 1.5);
         Vec3 handPos = newPos.add(look.z, 0, -look.x).add(entity.getForward().multiply(1, 0, 1).normalize().scale(width * 3));
         positions.add(handPos);
      }
      
      for(ServerPlayer player : world.getPlayers(p -> p.distanceTo(entity) < 25)){
         if(player.equals(entity)) continue;
         for(Vec3 poses : positions){
            world.sendParticles(STARDUST_PARTICLE, poses.x, poses.y, poses.z, 1, width, leyway, width, 0.02);
         }
      }
   }
}
