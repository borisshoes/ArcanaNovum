package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class PlayerComponentInitializer implements EntityComponentInitializer {
   public static final ComponentKey<IArcanaProfileComponent> PLAYER_DATA =
         ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("arcananovum", "profile"), IArcanaProfileComponent.class);
   
   
   @Override
   public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
      registry.registerForPlayers(PLAYER_DATA, playerEntity -> new ArcanaProfileComponent(playerEntity), RespawnCopyStrategy.ALWAYS_COPY.ALWAYS_COPY);
   }
}