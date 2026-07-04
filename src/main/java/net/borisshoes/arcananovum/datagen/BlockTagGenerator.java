package net.borisshoes.arcananovum.datagen;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.references.BlockIds;
import net.minecraft.references.BlockItemIds;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends FabricTagsProvider.BlockTagsProvider {
   public BlockTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture){
      super(output, registriesFuture);
   }
   
   @Override
   protected void addTags(HolderLookup.Provider lookup){
      builder(ArcanaRegistry.CEPTYUS_VEIN_MINEABLE)
            .add(BlockItemIds.COAL_ORE)
            .add(BlockItemIds.DEEPSLATE_COAL_ORE)
            .add(BlockItemIds.IRON_ORE)
            .add(BlockItemIds.DEEPSLATE_IRON_ORE)
            .add(BlockItemIds.COPPER_ORE)
            .add(BlockItemIds.DEEPSLATE_COPPER_ORE)
            .add(BlockItemIds.GOLD_ORE)
            .add(BlockItemIds.DEEPSLATE_GOLD_ORE)
            .add(BlockItemIds.REDSTONE_ORE)
            .add(BlockItemIds.DEEPSLATE_REDSTONE_ORE)
            .add(BlockItemIds.EMERALD_ORE)
            .add(BlockItemIds.DEEPSLATE_EMERALD_ORE)
            .add(BlockItemIds.LAPIS_ORE)
            .add(BlockItemIds.DEEPSLATE_LAPIS_ORE)
            .add(BlockItemIds.DIAMOND_ORE)
            .add(BlockItemIds.DEEPSLATE_DIAMOND_ORE)
            .add(BlockItemIds.NETHER_GOLD_ORE)
            .add(BlockItemIds.NETHER_QUARTZ_ORE)
            .add(BlockItemIds.ANCIENT_DEBRIS)
            .add(BlockItemIds.RAW_COPPER_BLOCK)
            .add(BlockItemIds.RAW_GOLD_BLOCK)
            .add(BlockItemIds.RAW_IRON_BLOCK)
            .add(BlockItemIds.AMETHYST_CLUSTER)
            .add(BlockItemIds.GLOWSTONE)
      ;
   }
   
   @Override
   public String getName(){
      return "Arcana Novum - Block Tag Generator";
   }
}
