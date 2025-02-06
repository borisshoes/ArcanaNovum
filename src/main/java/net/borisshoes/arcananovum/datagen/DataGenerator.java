package net.borisshoes.arcananovum.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGenerator implements DataGeneratorEntrypoint {
   
   @Override
   public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator){
      FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
      pack.addProvider(EnchantmentGenerator::new);
      pack.addProvider(ItemTagGenerator::new);
      pack.addProvider(EnchantmentTagGenerator::new);
   }
}
