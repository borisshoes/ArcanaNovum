package net.borisshoes.arcananovum.datagen;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class EnchantmentGenerator extends FabricDynamicRegistryProvider {
   public EnchantmentGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture){
      super(output, registriesFuture);
   }
   
   @Override
   protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries){
      RegistryWrapper<Item> itemLookup = registries.getOrThrow(RegistryKeys.ITEM);
      RegistryWrapper<Enchantment> enchantmentLookup = registries.getOrThrow(RegistryKeys.ENCHANTMENT);
      
      register(entries, ArcanaRegistry.FATE_ANCHOR, Enchantment.builder(
            Enchantment.definition(
                  itemLookup.getOrThrow(ArcanaRegistry.FATE_ANCHOR_ENCHANTABLE),
                  1,
                  1,
                  Enchantment.constantCost(50),
                  Enchantment.constantCost(100),
                  30,
                  AttributeModifierSlot.ANY
            )).exclusiveSet(enchantmentLookup.getOrThrow(ArcanaRegistry.FATE_ANCHOR_EXCLUSIVE_SET))
      );
   }
   
   private static void register(Entries entries, RegistryKey<Enchantment> key, Enchantment.Builder builder, ResourceCondition... conditions){
      entries.add(key, builder.build(key.getValue()), conditions);
   }
   
   @Override
   public String getName(){
      return "ArcanaNovum - Enchantment Generator";
   }
}
