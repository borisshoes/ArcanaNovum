package net.borisshoes.arcananovum.world.structures;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.ListPoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FabricStructurePoolRegistry {
   
   // Code used under MIT License from
   // https://github.com/fzzyhmstrs/structurized-reborn/tree/master
   
   private static final Multimap<String, Quintuple<String, String, RegistryKey<StructureProcessorList>, String, Integer>> structuresInfo = LinkedHashMultimap.create();
   private static final Map<String, String> structuresKeyRef = new HashMap<>();
   private static final Multimap<String, Pair<String, RegistryEntry<PlacedFeature>>> featureStructures = LinkedHashMultimap.create();
   private static final Multimap<String, ListPoolElement> listStructures = LinkedHashMultimap.create();
   public static RegistryEntryLookup<StructureProcessorList> registryEntryLookup;
   
   static {
      StructurePoolAddCallback.EVENT.register(FabricStructurePoolRegistry::processRegistry);
   }
   
   public static void registerSimple(Identifier poolId, Identifier structureId, int weight) {
      register(poolId, structureId, weight, StructureProcessorLists.EMPTY, StructurePool.Projection.RIGID, StructurePoolElementType.LEGACY_SINGLE_POOL_ELEMENT);
   }
   
   public static void register(Identifier poolId, Identifier structureId, int weight, RegistryKey<StructureProcessorList> processor) {
      register(poolId, structureId, weight, processor, StructurePool.Projection.RIGID, StructurePoolElementType.LEGACY_SINGLE_POOL_ELEMENT);
   }
   
   public static void register(Identifier poolId, Identifier structureId, int weight, RegistryKey<StructureProcessorList> processor, StructurePool.Projection projection) {
      register(poolId, structureId, weight, processor, projection, StructurePoolElementType.LEGACY_SINGLE_POOL_ELEMENT);
   }
   
   public static void register(Identifier poolId, Identifier structureId, int weight, RegistryKey<StructureProcessorList> processor, StructurePool.Projection projection, StructurePoolElementType<?> type) {
      String poolType = Objects.requireNonNull(Registries.STRUCTURE_POOL_ELEMENT.getId(type)).toString();
      String projectionId = projection.getId();
      structuresInfo.put(poolId.toString(), new Quintuple<>(structureId.toString(), poolType, processor, projectionId, weight));
      structuresKeyRef.put(structureId.toString(), poolId.toString());
   }
   
   public static void registerFeature(Identifier poolId, Identifier structureId, int weight, StructurePool.Projection projection, RegistryEntry<PlacedFeature> entry) {
      register(poolId, structureId, weight, StructureProcessorLists.EMPTY, projection, StructurePoolElementType.FEATURE_POOL_ELEMENT);
      featureStructures.put(poolId.toString(), new Pair<>(structureId.toString(), entry));
   }
   
   public static void registerList(Identifier poolId, int weight, ListPoolElement listPoolElement) {
      register(poolId, Identifier.ofVanilla("air"), weight, StructureProcessorLists.EMPTY, StructurePool.Projection.RIGID, StructurePoolElementType.LIST_POOL_ELEMENT);
      listStructures.put(poolId.toString(), listPoolElement);
   }
   
   public static @Nullable Triple<String, String, String> getPoolStructureElementInfo(String id) {
      String poolId = structuresKeyRef.get(id);
      for (Quintuple<String, String, RegistryKey<StructureProcessorList>, String, Integer> quint : structuresInfo.get(poolId)) {
         if (quint.a.equals(id)) {
            return Triple.of(quint.b, quint.c.getValue().toString(), quint.d);
         }
      }
      return null;
   }
   
   public static void processRegistry(FabricStructurePool structurePool) {
      String poolId = structurePool.getId().toString();
      for (String key : structuresInfo.keys()) {
         if (Objects.equals(key, poolId)) {
            structuresInfo.get(key).forEach(value -> addToPool(structurePool, value, key, registryEntryLookup));
         }
      }
   }
   
   private static void addToPool(FabricStructurePool structurePool, Quintuple<String, String, RegistryKey<StructureProcessorList>, String, Integer> quint, String key, RegistryEntryLookup<StructureProcessorList> registryEntryLookup) {
      List<StructurePoolElement> spe = new LinkedList<>();
      StructurePoolElementType<?> type = Registries.STRUCTURE_POOL_ELEMENT.get(Identifier.of(quint.b));
      if (type == StructurePoolElementType.SINGLE_POOL_ELEMENT) {
         RegistryEntry<StructureProcessorList> entry = registryEntryLookup.getOrThrow(quint.c);
         spe.add(StructurePoolElement.ofProcessedSingle(quint.a, entry).apply(StructurePool.Projection.getById(quint.d)));
      } else if (type == StructurePoolElementType.LEGACY_SINGLE_POOL_ELEMENT) {
         RegistryEntry<StructureProcessorList> entry = registryEntryLookup.getOrThrow(quint.c);
         spe.add(StructurePoolElement.ofProcessedLegacySingle(quint.a, entry).apply(StructurePool.Projection.getById(quint.d)));
      } else if (type == StructurePoolElementType.LIST_POOL_ELEMENT) {
         spe.addAll(listStructures.get(key));
      } else if (type == StructurePoolElementType.FEATURE_POOL_ELEMENT) {
         List<StructurePoolElement> finalSpe = new LinkedList<>();
         featureStructures.get(key).forEach(
               value -> {
                  if (value.getLeft().equals(quint.a)) {
                     finalSpe.add(StructurePoolElement.ofFeature(value.getRight()).apply(StructurePool.Projection.getById(quint.d)));
                  }
               }
         );
         spe.addAll(finalSpe);
      } else {
         spe.add(StructurePoolElement.ofEmpty().apply(StructurePool.Projection.RIGID));
      }
      spe.forEach(value -> structurePool.addStructurePoolElement(value, quint.e));
   }
   
   private record Quintuple<A, B, C, D, E>(A a, B b, C c, D d, E e) {}
}