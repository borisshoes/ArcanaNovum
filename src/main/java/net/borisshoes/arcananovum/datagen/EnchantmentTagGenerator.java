package net.borisshoes.arcananovum.datagen;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.concurrent.CompletableFuture;

public class EnchantmentTagGenerator extends FabricTagProvider<Enchantment> {
   public EnchantmentTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture){
      super(output, Registries.ENCHANTMENT, registriesFuture);
   }
   
   @Override
   protected void addTags(HolderLookup.Provider lookup){
      builder(ArcanaRegistry.FATE_ANCHOR_EXCLUSIVE_SET)
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
