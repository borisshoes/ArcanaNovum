package net.borisshoes.arcananovum.mixins;

import com.mojang.serialization.Decoder;
import net.borisshoes.arcananovum.world.structures.FabricStructurePoolImpl;
import net.borisshoes.arcananovum.world.structures.FabricStructurePoolRegistry;
import net.borisshoes.arcananovum.world.structures.StructurePoolAddCallback;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
   @Inject(method = "loadContentsFromManager(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V", at = @At("TAIL"))
   private static <E> void load(ResourceManager resourceManager, RegistryOps.RegistryInfoLookup infoGetter, WritableRegistry<E> registry, Decoder<E> elementDecoder, Map<ResourceKey<?>, Exception> errors, CallbackInfo ci){
      if(registry.key().equals(Registries.TEMPLATE_POOL)){
         for (E registryEntry : registry.stream().toList()){
            if(!(registryEntry instanceof StructureTemplatePool pool)){
               continue;
            }
            Identifier id = registry.getKey(registryEntry);
            if(FabricStructurePoolRegistry.registryEntryLookup == null){
               Optional<RegistryOps.RegistryInfo<StructureProcessorList>> optionalRegistryInfo = infoGetter.lookup(Registries.PROCESSOR_LIST);
               optionalRegistryInfo.ifPresent(info -> {
                  FabricStructurePoolRegistry.registryEntryLookup = info.getter();
               });
            }
            StructurePoolAddCallback.EVENT.invoker().onAdd(new FabricStructurePoolImpl(pool, id));
         }
      }
   }
}