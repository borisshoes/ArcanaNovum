package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.util.Identifier;

public class MagicBlocksComponentInitializer implements WorldComponentInitializer {
   public static final ComponentKey<IMagicBlockComponent> MAGIC_BLOCK_LIST = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("arcananovum", "magicblocks"), IMagicBlockComponent.class);
   
   @Override
   public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
      registry.register(MAGIC_BLOCK_LIST, MagicBlockComponent.class, world -> new MagicBlockComponent());
   }
}
