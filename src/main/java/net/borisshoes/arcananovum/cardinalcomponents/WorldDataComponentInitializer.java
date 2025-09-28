package net.borisshoes.arcananovum.cardinalcomponents;

import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class WorldDataComponentInitializer implements WorldComponentInitializer {
   public static final ComponentKey<IBossFightComponent> BOSS_FIGHT = ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of(MOD_ID, "bossfight"), IBossFightComponent.class);
   public static final ComponentKey<IAnchorsComponent> ACTIVE_ANCHORS = ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of(MOD_ID, "active_anchors"), IAnchorsComponent.class);
   
   @Override
   public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry){
      registry.register(BOSS_FIGHT, BossFightComponent.class, world -> new BossFightComponent());
      registry.register(ACTIVE_ANCHORS, AnchorsComponent.class, world -> new AnchorsComponent());
   }
}
