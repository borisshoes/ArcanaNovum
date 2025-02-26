package net.borisshoes.arcananovum.datagen;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class EnchantmentTagGenerator extends FabricTagProvider<Enchantment> {
   public EnchantmentTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture){
      super(output, RegistryKeys.ENCHANTMENT, registriesFuture);
   }
   
   @Override
   protected void configure(RegistryWrapper.WrapperLookup lookup){
      getOrCreateTagBuilder(ArcanaRegistry.FATE_ANCHOR_EXCLUSIVE_SET)
            .addOptional(ArcanaRegistry.FATE_ANCHOR)
            .add(Enchantments.VANISHING_CURSE)
            .add(Enchantments.BINDING_CURSE)
      ;
   }
   
   @Override
   public String getName(){
      return "Arcana Novum - Enchantment Tag Generator";
   }
}
