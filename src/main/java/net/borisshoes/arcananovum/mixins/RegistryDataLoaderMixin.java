package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.world.structures.FabricStructurePoolImpl;
import net.borisshoes.arcananovum.world.structures.FabricStructurePoolRegistry;
import net.borisshoes.arcananovum.world.structures.StructurePoolAddCallback;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.ConcurrentHolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(RegistryLoadTask.class)
public abstract class RegistryDataLoaderMixin<T> {
   
   @Shadow @Final protected RegistryDataLoader.RegistryData<T> data;
   @Shadow @Final private WritableRegistry<T> registry;
   @Shadow @Final protected ConcurrentHolderGetter<T> concurrentRegistrationGetter;
   
   @Inject(method = "freezeRegistry", at = @At("HEAD"))
   private void arcananovum_beforeFreeze(Map<ResourceKey<?>, Exception> loadingErrors, CallbackInfoReturnable<Boolean> cir){
      // Capture the processor list getter before TEMPLATE_POOL is processed.
      // In WORLDGEN_REGISTRIES, PROCESSOR_LIST is ordered before TEMPLATE_POOL,
      // so this runs first and sets the static field for use in the callback below.
      if(this.data.key().equals(Registries.PROCESSOR_LIST)){
         @SuppressWarnings("unchecked")
         HolderGetter<StructureProcessorList> getter = (HolderGetter<StructureProcessorList>)(Object)this.concurrentRegistrationGetter;
         FabricStructurePoolRegistry.registryEntryLookup = getter;
      }
      
      if(this.data.key().equals(Registries.TEMPLATE_POOL)){
         for(T registryEntry : this.registry.stream().toList()){
            if(!(registryEntry instanceof StructureTemplatePool pool)){
               continue;
            }
            Identifier id = this.registry.getKey(registryEntry);
            StructurePoolAddCallback.EVENT.invoker().onAdd(new FabricStructurePoolImpl(pool, id));
         }
      }
   }
}