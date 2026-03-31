package net.borisshoes.arcananovum.world.structures;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FabricStructurePoolRegistry {
   
   // Code used under MIT License from
   // https://github.com/fzzyhmstrs/structurized-reborn/tree/master
   
   private static final Multimap<String, Quintuple<String, String, ResourceKey<StructureProcessorList>, String, Integer>> structuresInfo = LinkedHashMultimap.create();
   private static final Map<String, String> structuresKeyRef = new HashMap<>();
   private static final Multimap<String, Tuple<String, Holder<PlacedFeature>>> featureStructures = LinkedHashMultimap.create();
   private static final Multimap<String, ListPoolElement> listStructures = LinkedHashMultimap.create();
   public static HolderGetter<StructureProcessorList> registryEntryLookup;
   
   static{
      StructurePoolAddCallback.EVENT.register(FabricStructurePoolRegistry::processRegistry);
   }
   
   public static void registerSimple(Identifier poolId, Identifier structureId, int weight){
      register(poolId, structureId, weight, ProcessorLists.EMPTY, StructureTemplatePool.Projection.RIGID, StructurePoolElementType.LEGACY);
   }
   
   public static void register(Identifier poolId, Identifier structureId, int weight, ResourceKey<StructureProcessorList> processor){
      register(poolId, structureId, weight, processor, StructureTemplatePool.Projection.RIGID, StructurePoolElementType.LEGACY);
   }
   
   public static void register(Identifier poolId, Identifier structureId, int weight, ResourceKey<StructureProcessorList> processor, StructureTemplatePool.Projection projection){
      register(poolId, structureId, weight, processor, projection, StructurePoolElementType.LEGACY);
   }
   
   public static void register(Identifier poolId, Identifier structureId, int weight, ResourceKey<StructureProcessorList> processor, StructureTemplatePool.Projection projection, StructurePoolElementType<?> type){
      String poolType = Objects.requireNonNull(BuiltInRegistries.STRUCTURE_POOL_ELEMENT.getKey(type)).toString();
      String projectionId = projection.getName();
      structuresInfo.put(poolId.toString(), new Quintuple<>(structureId.toString(), poolType, processor, projectionId, weight));
      structuresKeyRef.put(structureId.toString(), poolId.toString());
   }
   
   public static void registerFeature(Identifier poolId, Identifier structureId, int weight, StructureTemplatePool.Projection projection, Holder<PlacedFeature> entry){
      register(poolId, structureId, weight, ProcessorLists.EMPTY, projection, StructurePoolElementType.FEATURE);
      featureStructures.put(poolId.toString(), new Tuple<>(structureId.toString(), entry));
   }
   
   public static void registerList(Identifier poolId, int weight, ListPoolElement listPoolElement){
      register(poolId, Identifier.withDefaultNamespace("air"), weight, ProcessorLists.EMPTY, StructureTemplatePool.Projection.RIGID, StructurePoolElementType.LIST);
      listStructures.put(poolId.toString(), listPoolElement);
   }
   
   public static @Nullable Triple<String, String, String> getPoolStructureElementInfo(String id){
      String poolId = structuresKeyRef.get(id);
      for(Quintuple<String, String, ResourceKey<StructureProcessorList>, String, Integer> quint : structuresInfo.get(poolId)){
         if(quint.a.equals(id)){
            return Triple.of(quint.b, quint.c.identifier().toString(), quint.d);
         }
      }
      return null;
   }
   
   public static void processRegistry(FabricStructurePool structurePool){
      String poolId = structurePool.getId().toString();
      for(String key : structuresInfo.keys()){
         if(Objects.equals(key, poolId)){
            structuresInfo.get(key).forEach(value -> addToPool(structurePool, value, key, registryEntryLookup));
         }
      }
   }
   
   private static void addToPool(FabricStructurePool structurePool, Quintuple<String, String, ResourceKey<StructureProcessorList>, String, Integer> quint, String key, HolderGetter<StructureProcessorList> registryEntryLookup){
      List<StructurePoolElement> spe = new LinkedList<>();
      StructurePoolElementType<?> type = BuiltInRegistries.STRUCTURE_POOL_ELEMENT.getValue(Identifier.parse(quint.b));
      if(type == StructurePoolElementType.SINGLE){
         Holder<StructureProcessorList> entry = registryEntryLookup.getOrThrow(quint.c);
         spe.add(StructurePoolElement.single(quint.a, entry).apply(StructureTemplatePool.Projection.byName(quint.d)));
      }else if(type == StructurePoolElementType.LEGACY){
         Holder<StructureProcessorList> entry = registryEntryLookup.getOrThrow(quint.c);
         spe.add(StructurePoolElement.legacy(quint.a, entry).apply(StructureTemplatePool.Projection.byName(quint.d)));
      }else if(type == StructurePoolElementType.LIST){
         spe.addAll(listStructures.get(key));
      }else if(type == StructurePoolElementType.FEATURE){
         List<StructurePoolElement> finalSpe = new LinkedList<>();
         featureStructures.get(key).forEach(
               value -> {
                  if(value.getA().equals(quint.a)){
                     finalSpe.add(StructurePoolElement.feature(value.getB()).apply(StructureTemplatePool.Projection.byName(quint.d)));
                  }
               }
         );
         spe.addAll(finalSpe);
      }else{
         spe.add(StructurePoolElement.empty().apply(StructureTemplatePool.Projection.RIGID));
      }
      spe.forEach(value -> structurePool.addStructurePoolElement(value, quint.e));
   }
   
   private record Quintuple<A, B, C, D, E>(A a, B b, C c, D d, E e) {
   }
}