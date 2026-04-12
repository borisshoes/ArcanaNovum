package net.borisshoes.arcananovum.datagen;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends FabricTagsProvider.BlockTagsProvider {
   public BlockTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture){
      super(output, registriesFuture);
   }
   
   @Override
   protected void addTags(HolderLookup.Provider lookup){
      valueLookupBuilder(ArcanaRegistry.CEPTYUS_VEIN_MINEABLE)
            .add(Blocks.COAL_ORE)
            .add(Blocks.DEEPSLATE_COAL_ORE)
            .add(Blocks.IRON_ORE)
            .add(Blocks.DEEPSLATE_IRON_ORE)
            .add(Blocks.COPPER_ORE)
            .add(Blocks.DEEPSLATE_COPPER_ORE)
            .add(Blocks.GOLD_ORE)
            .add(Blocks.DEEPSLATE_GOLD_ORE)
            .add(Blocks.REDSTONE_ORE)
            .add(Blocks.DEEPSLATE_REDSTONE_ORE)
            .add(Blocks.EMERALD_ORE)
            .add(Blocks.DEEPSLATE_EMERALD_ORE)
            .add(Blocks.LAPIS_ORE)
            .add(Blocks.DEEPSLATE_LAPIS_ORE)
            .add(Blocks.DIAMOND_ORE)
            .add(Blocks.DEEPSLATE_DIAMOND_ORE)
            .add(Blocks.NETHER_GOLD_ORE)
            .add(Blocks.NETHER_QUARTZ_ORE)
            .add(Blocks.ANCIENT_DEBRIS)
            .add(Blocks.RAW_COPPER_BLOCK)
            .add(Blocks.RAW_GOLD_BLOCK)
            .add(Blocks.RAW_IRON_BLOCK)
            .add(Blocks.AMETHYST_CLUSTER)
            .add(Blocks.GLOWSTONE)
      ;
   }
   
   @Override
   public String getName(){
      return "Arcana Novum - Block Tag Generator";
   }
}
