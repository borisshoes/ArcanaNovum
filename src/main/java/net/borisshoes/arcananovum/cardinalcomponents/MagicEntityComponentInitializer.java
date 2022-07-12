package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.util.Identifier;

public class MagicEntityComponentInitializer  implements WorldComponentInitializer {
   public static final ComponentKey<IMagicEntityComponent> MAGIC_ENTITY_LIST = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("arcananovum", "magicentities"), IMagicEntityComponent.class);
   
   @Override
   public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
      registry.register(MAGIC_ENTITY_LIST, MagicEntityComponent.class, world -> new MagicEntityComponent());
   }
}