package net.borisshoes.arcananovum.mixins;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CauldronInteraction.Dispatcher.class)
public interface CauldronInteractionDispatcherAccessor {
   
   @Invoker("put")
   void callPut(final Item item, final CauldronInteraction interaction);
   
   @Invoker("put")
   void callPut(final TagKey<Item> tag, final CauldronInteraction interaction);
}
