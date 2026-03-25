package net.borisshoes.arcananovum.datastorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.borisshoes.borislib.datastorage.StorableData;
import net.borisshoes.borislib.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class InterdictionZones implements StorableData {
   
   private static final int CELL_SIZE = 16; // Same as chunk size for efficiency
   
   public static final DataKey<InterdictionZones> KEY = DataRegistry.register(DataKey.ofWorld(ArcanaRegistry.arcanaId("interdiction_zones"), InterdictionZones::new));
   
   private final ResourceKey<Level> worldKey;
   private final List<InterdictionZone> zones = new ArrayList<>();
   private final Map<Long, List<InterdictionZone>> spatialIndex = new HashMap<>();
   
   public InterdictionZones(ResourceKey<Level> worldKey){
      this.worldKey = worldKey;
   }
   
   @Override
   public void read(ValueInput view){
      this.zones.clear();
      this.spatialIndex.clear();
      view.listOrEmpty("zones", InterdictionZone.CODEC).forEach(zone -> {
         this.zones.add(zone);
         this.indexZone(zone);
      });
   }
   
   @Override
   public void writeNbt(CompoundTag tag){
      ListTag zonesList = new ListTag();
      for(InterdictionZone zone : zones){
         InterdictionZone.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, BorisLib.SERVER.registryAccess()), zone).result().ifPresent(zonesList::add);
      }
      tag.put("zones", zonesList);
   }
   
   public ResourceKey<Level> getWorldKey(){
      return worldKey;
   }
   
   /**
    * Converts a BlockPos to a cell key for the spatial index
    */
   private static long getCellKey(int x, int y, int z){
      int cellX = Math.floorDiv(x, CELL_SIZE);
      int cellY = Math.floorDiv(y, CELL_SIZE);
      int cellZ = Math.floorDiv(z, CELL_SIZE);
      // Pack into a long: 21 bits each for x, y, z (supports +-1M blocks)
      return ((long)(cellX & 0x1FFFFF) << 42) | ((long)(cellY & 0x1FFFFF) << 21) | (cellZ & 0x1FFFFF);
   }
   
   private static long getCellKey(BlockPos pos){
      return getCellKey(pos.getX(), pos.getY(), pos.getZ());
   }
   
   /**
    * Gets all cell keys that an AABB overlaps
    */
   private static List<Long> getCellsForAABB(AABB box){
      List<Long> cells = new ArrayList<>();
      int minCellX = Math.floorDiv((int) Math.floor(box.minX), CELL_SIZE);
      int minCellY = Math.floorDiv((int) Math.floor(box.minY), CELL_SIZE);
      int minCellZ = Math.floorDiv((int) Math.floor(box.minZ), CELL_SIZE);
      int maxCellX = Math.floorDiv((int) Math.floor(box.maxX), CELL_SIZE);
      int maxCellY = Math.floorDiv((int) Math.floor(box.maxY), CELL_SIZE);
      int maxCellZ = Math.floorDiv((int) Math.floor(box.maxZ), CELL_SIZE);
      
      for(int x = minCellX; x <= maxCellX; x++){
         for(int y = minCellY; y <= maxCellY; y++){
            for(int z = minCellZ; z <= maxCellZ; z++){
               long key = ((long)(x & 0x1FFFFF) << 42) | ((long)(y & 0x1FFFFF) << 21) | (z & 0x1FFFFF);
               cells.add(key);
            }
         }
      }
      return cells;
   }
   
   /**
    * Adds a zone to the spatial index
    */
   private void indexZone(InterdictionZone zone){
      for(Long cellKey : getCellsForAABB(zone.box)){
         spatialIndex.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(zone);
      }
   }
   
   /**
    * Removes a zone from the spatial index
    */
   private void unindexZone(InterdictionZone zone){
      for(Long cellKey : getCellsForAABB(zone.box)){
         List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
         if(cellZones != null){
            cellZones.remove(zone);
            if(cellZones.isEmpty()){
               spatialIndex.remove(cellKey);
            }
         }
      }
   }
   
   /**
    * Adds or refreshes an interdiction zone
    */
   public void addOrRefreshZone(AABB box, BlockPos source, int keepAlive, boolean redirect){
      // Check if zone from this source already exists
      for(InterdictionZone zone : zones){
         if(zone.sourcePos.equals(source)){
            zone.refreshKeepAlive(keepAlive);
            return;
         }
      }
      // Add new zone
      InterdictionZone zone = new InterdictionZone(box, source, keepAlive, redirect);
      zones.add(zone);
      indexZone(zone);
   }
   
   /**
    * Gets all zones containing the given BlockPos - O(1) cell lookup + zone checks
    */
   public List<InterdictionZone> getZonesContaining(BlockPos pos){
      List<InterdictionZone> result = new ArrayList<>();
      long cellKey = getCellKey(pos);
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(zone.zoneContains(pos)){
               result.add(zone);
            }
         }
      }
      return result;
   }
   
   /**
    * Gets all zones containing the given Vec3 - O(1) cell lookup + zone checks
    */
   public List<InterdictionZone> getZonesContaining(Vec3 pos){
      List<InterdictionZone> result = new ArrayList<>();
      long cellKey = getCellKey((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(zone.zoneContains(pos)){
               result.add(zone);
            }
         }
      }
      return result;
   }
   
   /**
    * Checks if any zone contains the given BlockPos
    */
   public boolean isInAnyZone(BlockPos pos){
      long cellKey = getCellKey(pos);
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(zone.zoneContains(pos)){
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * Checks if any zone contains the given Vec3
    */
   public boolean isInAnyZone(Vec3 pos){
      long cellKey = getCellKey((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(zone.zoneContains(pos)){
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * Checks if any blocking zone (redirect=false) contains the given BlockPos
    */
   public boolean isInAnyBlockingZone(BlockPos pos){
      long cellKey = getCellKey(pos);
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(!zone.isRedirect() && zone.zoneContains(pos)){
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * Checks if any blocking zone (redirect=false) contains the given Vec3
    */
   public boolean isInAnyBlockingZone(Vec3 pos){
      long cellKey = getCellKey((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(!zone.isRedirect() && zone.zoneContains(pos)){
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * Checks if any redirect zone (redirect=true) contains the given BlockPos
    */
   public boolean isInAnyRedirectZone(BlockPos pos){
      long cellKey = getCellKey(pos);
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(zone.isRedirect() && zone.zoneContains(pos)){
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * Checks if any redirect zone (redirect=true) contains the given Vec3
    */
   public boolean isInAnyRedirectZone(Vec3 pos){
      long cellKey = getCellKey((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(zone.isRedirect() && zone.zoneContains(pos)){
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * Gets all blocking zones (redirect=false) containing the given BlockPos
    */
   public List<InterdictionZone> getBlockingZonesContaining(BlockPos pos){
      List<InterdictionZone> result = new ArrayList<>();
      long cellKey = getCellKey(pos);
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(!zone.isRedirect() && zone.zoneContains(pos)){
               result.add(zone);
            }
         }
      }
      return result;
   }
   
   /**
    * Gets all blocking zones (redirect=false) containing the given Vec3
    */
   public List<InterdictionZone> getBlockingZonesContaining(Vec3 pos){
      List<InterdictionZone> result = new ArrayList<>();
      long cellKey = getCellKey((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(!zone.isRedirect() && zone.zoneContains(pos)){
               result.add(zone);
            }
         }
      }
      return result;
   }
   
   /**
    * Gets all redirect zones (redirect=true) containing the given BlockPos
    */
   public List<InterdictionZone> getRedirectZonesContaining(BlockPos pos){
      List<InterdictionZone> result = new ArrayList<>();
      long cellKey = getCellKey(pos);
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(zone.isRedirect() && zone.zoneContains(pos)){
               result.add(zone);
            }
         }
      }
      return result;
   }
   
   /**
    * Gets all redirect zones (redirect=true) containing the given Vec3
    */
   public List<InterdictionZone> getRedirectZonesContaining(Vec3 pos){
      List<InterdictionZone> result = new ArrayList<>();
      long cellKey = getCellKey((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
      List<InterdictionZone> cellZones = spatialIndex.get(cellKey);
      if(cellZones != null){
         for(InterdictionZone zone : cellZones){
            if(zone.isRedirect() && zone.zoneContains(pos)){
               result.add(zone);
            }
         }
      }
      return result;
   }
   
   /**
    * Ticks all zones and removes expired ones
    */
   public void tick(){
      Iterator<InterdictionZone> iterator = zones.iterator();
      while(iterator.hasNext()){
         InterdictionZone zone = iterator.next();
         if(zone.tick()){
            unindexZone(zone);
            iterator.remove();
         }
      }
   }
   
   /**
    * Removes a zone by its source position
    */
   public void removeZone(BlockPos source){
      Iterator<InterdictionZone> iterator = zones.iterator();
      while(iterator.hasNext()){
         InterdictionZone zone = iterator.next();
         if(zone.sourcePos.equals(source)){
            unindexZone(zone);
            iterator.remove();
            return;
         }
      }
   }
   
   public int getZoneCount(){
      return zones.size();
   }
   
   public boolean isEmpty(){
      return zones.isEmpty();
   }
   
   public static class InterdictionZone {
      
      public static final Codec<InterdictionZone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtils.AABB_CODEC.fieldOf("box").forGetter(zone -> zone.box),
            BlockPos.CODEC.fieldOf("sourcePos").forGetter(zone -> zone.sourcePos),
            Codec.INT.fieldOf("keepAlive").forGetter(zone -> zone.keepAlive),
            Codec.BOOL.optionalFieldOf("redirect", false).forGetter(zone -> zone.redirect)
      ).apply(instance, InterdictionZone::new));
      
      private final AABB box;
      private final BlockPos sourcePos;
      private int keepAlive;
      private final boolean redirect;
      
      public InterdictionZone(AABB box, BlockPos sourcePos, int keepAlive, boolean redirect){
         this.box = box;
         this.sourcePos = sourcePos;
         this.keepAlive = keepAlive;
         this.redirect = redirect;
      }
      
      public boolean isRedirect(){
         return redirect;
      }
      
      public AABB getBox(){
         return box;
      }
      
      public BlockPos getSourcePos(){
         return sourcePos;
      }
      
      public boolean zoneContains(BlockPos pos){
         return box.contains(pos.getCenter());
      }
      
      public boolean zoneContains(Vec3 pos){
         return box.contains(pos);
      }
      
      public void refreshKeepAlive(int time){
         this.keepAlive = time;
      }
      
      public boolean tick(){
         this.keepAlive--;
         return keepAlive < 0;
      }
      
      public boolean isExpired(){
         return this.keepAlive < 0;
      }
   }
}
