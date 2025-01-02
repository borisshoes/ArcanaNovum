package net.borisshoes.arcananovum.cardinalcomponents;

import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class PlayerComponentInitializer implements EntityComponentInitializer {
   public static final ComponentKey<IArcanaProfileComponent> PLAYER_DATA =
         ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("arcananovum", "profile"), IArcanaProfileComponent.class);
   
   @Override
   public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry){
      registry.registerForPlayers(PLAYER_DATA, ArcanaProfileComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
   }
}