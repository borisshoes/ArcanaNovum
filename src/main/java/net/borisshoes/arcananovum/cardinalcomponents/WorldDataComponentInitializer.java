package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.util.Identifier;

public class WorldDataComponentInitializer implements WorldComponentInitializer {
   public static final ComponentKey<ILoginCallbackComponent> LOGIN_CALLBACK_LIST = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("arcananovum", "login_callbacks"), ILoginCallbackComponent.class);
   public static final ComponentKey<IBossFightComponent> BOSS_FIGHT = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("arcananovum", "bossfight"), IBossFightComponent.class);
   public static final ComponentKey<IAnchorsComponent> ACTIVE_ANCHORS = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("arcananovum", "active_anchors"), IAnchorsComponent.class);
   
   @Override
   public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry){
      registry.register(LOGIN_CALLBACK_LIST, LoginCallbackComponent.class, world -> new LoginCallbackComponent());
      registry.register(BOSS_FIGHT, BossFightComponent.class, world -> new BossFightComponent());
      registry.register(ACTIVE_ANCHORS, AnchorsComponent.class, world -> new AnchorsComponent());
   }
}
