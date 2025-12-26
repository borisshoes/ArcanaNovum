package net.borisshoes.arcananovum.datagen;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.concurrent.CompletableFuture;

public class EnchantmentGenerator extends FabricDynamicRegistryProvider {
   public EnchantmentGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture){
      super(output, registriesFuture);
   }
   
   @Override
   protected void configure(HolderLookup.Provider registries, Entries entries){
      HolderLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);
      HolderLookup<Enchantment> enchantmentLookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
      
      register(entries, ArcanaRegistry.FATE_ANCHOR, Enchantment.enchantment(
            Enchantment.definition(
                  itemLookup.getOrThrow(ArcanaRegistry.FATE_ANCHOR_ENCHANTABLE),
                  1,
                  1,
                  Enchantment.constantCost(50),
                  Enchantment.constantCost(100),
                  30,
                  EquipmentSlotGroup.ANY
            )).exclusiveWith(enchantmentLookup.getOrThrow(ArcanaRegistry.FATE_ANCHOR_EXCLUSIVE_SET))
      );
   }
   
   private static void register(Entries entries, ResourceKey<Enchantment> key, Enchantment.Builder builder, ResourceCondition... conditions){
      entries.add(key, builder.build(key.identifier()), conditions);
   }
   
   @Override
   public String getName(){
      return "Arcana Novum - Enchantment Generator";
   }
}
