package net.borisshoes.arcananovum.cardinalcomponents;

import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class PlayerComponentInitializer implements EntityComponentInitializer {
   public static final ComponentKey<IArcanaProfileComponent> PLAYER_DATA =
         ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of(MOD_ID, "profile"), IArcanaProfileComponent.class);
   
   @Override
   public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry){
      registry.registerForPlayers(PLAYER_DATA, ArcanaProfileComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
   }
}