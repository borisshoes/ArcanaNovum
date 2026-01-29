package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ManualAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ItineranteurBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   
   private static final Map<ResourceKey<Level>, Map<Long, Set<ItineranteurZone>>> ACTIVE_ZONES = new HashMap<>();
   private static final float[] BOOST_LEVELS = new float[]{0.5f, 1.0f, 1.5f, 2.5f};
   private static final String BLOCKS_TAG = "itineranteur_blocks";
   public static final String CRAFTER_TAG = "itineranteur_crafter";
   public static final String FED_TAG = "itineranteur_fed";
   private static final int KEEP_ALIVE = 21;
   
   private final Set<BlockPos> blocks = new HashSet<>();
   private final Set<GridHolder> displays = new HashSet<>();
   private TreeMap<ArcanaAugment, Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private int maxBlocks, maxRange;
   private float speedBoost;
   private boolean keepFed;
   private ServerPlayer editor;
   private BlockPos selectedPos;
   private BlockPos highlightedPos;
   private ItineranteurZone currentZone;
   private static final int REFRESH_DUR = 2;
   
   public ItineranteurBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.ITINERANTEUR_BLOCK_ENTITY, pos, state);
   }
   
   public void initialize(TreeMap<ArcanaAugment, Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
      this.maxRange = 16 * (1 + ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.GUIDING_LIGHT));
      this.maxBlocks = 64 * (2 + ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.THOROUGHFARE));
      this.speedBoost = BOOST_LEVELS[ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.PAVED_WARMTH)];
      this.keepFed = ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.ROAD_SNACKS) > 0;
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof ItineranteurBlockEntity itineranteur){
         itineranteur.tick();
      }
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(serverWorld.random.nextFloat() < 0.05f){
         ParticleOptions particleType;
         if(getBlockState().getValue(Itineranteur.ItineranteurBlock.TYPE) == Itineranteur.LanternType.GREEN ||
               getBlockState().getValue(Itineranteur.ItineranteurBlock.TYPE) == Itineranteur.LanternType.BLUE){
            particleType = ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS;
         }else{
            particleType = ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER;
         }
         serverWorld.sendParticles(particleType, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.00);
      }
      
      if(serverWorld.getServer().getTickCount() % REFRESH_DUR == 0){
         if(this.editor != null && (this.editor.distanceToSqr(this.getBlockPos().getCenter()) > (maxRange * maxRange * 2) || this.editor.isDeadOrDying() || !this.editor.level().dimension().identifier().equals(serverWorld.dimension().identifier()))){
            setEditor(null);
         }
         if(editor != null){
            Vec3 vec3d = editor.getEyePosition(0);
            Vec3 vec3d2 = editor.getViewVector(0);
            double maxDistance = 10;
            Vec3 vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
            BlockHitResult result = serverWorld.clip(new ClipContext(vec3d, vec3d3, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, editor));
            if(result.getType() == BlockHitResult.Type.MISS || !isInRange(result.getBlockPos())){
               this.highlightedPos = null;
            }else{
               this.highlightedPos = result.getBlockPos();
            }
            
            if(this.selectedPos != null){
               createDisplayForBlockSet(serverWorld, Set.of(this.selectedPos), this.blocks.contains(this.selectedPos) ? Blocks.RED_CONCRETE : (this.selectedPos.equals(getBlockPos()) ? Blocks.MAGENTA_CONCRETE : Blocks.WHITE_CONCRETE), 0.07f);
            }
            int dispCount = blocks.size();
            if(this.highlightedPos != null){
               if(selectedPos == null){
                  createDisplayForBlockSet(serverWorld, Set.of(this.highlightedPos), this.blocks.contains(this.highlightedPos) ? Blocks.RED_CONCRETE : (this.highlightedPos.equals(getBlockPos()) ? Blocks.MAGENTA_CONCRETE : Blocks.WHITE_CONCRETE), 0.07f);
               }else{
                  Set<BlockPos> otherSet = calculateEncompassedBlocks(this.highlightedPos, this.selectedPos);
                  if(this.blocks.contains(this.selectedPos)){
                     dispCount -= otherSet.size();
                  }else{
                     dispCount += otherSet.size();
                  }
                  if(dispCount <= maxBlocks){
                     createDisplayForBlockSet(serverWorld, otherSet, this.blocks.contains(this.selectedPos) ? Blocks.RED_CONCRETE : Blocks.GOLD_BLOCK, 0.06f);
                  }
               }
            }
            createDisplayForBlockSet(serverWorld, this.blocks, Blocks.YELLOW_CONCRETE, 0.05f);
            editor.sendSystemMessage(Component.literal("Blocks: " + dispCount + "/" + maxBlocks).withStyle(dispCount > maxBlocks ? ChatFormatting.RED : ChatFormatting.GOLD), true);
         }
      }
      
      if(serverWorld.getServer().getTickCount() % 20 == 0){
         if(!this.blocks.isEmpty()){
            if(currentZone != null){
               // Refresh existing zone
               currentZone.refresh();
            }else{
               // Register new zone
               currentZone = new ItineranteurZone(this.blocks, serverWorld.dimension(), this.speedBoost, this.keepFed, this);
               registerZone(currentZone);
            }
            ArcanaNovum.addActiveBlock(new Tuple<>(this, this));
         }else if(currentZone != null){ // No blocks, unregister zone
            unregisterZone(currentZone);
            currentZone = null;
         }
      }
      
      for(GridHolder block : displays){
         block.tick();
      }
      displays.removeIf(GridHolder::wasDestroyed);
   }
   
   public Set<BlockPos> calculateEncompassedBlocks(BlockPos pos1, BlockPos pos2){
      // Calculate blocks encompassed between highlighted and selected pos
      Set<BlockPos> encompassedBlocks = new HashSet<>();
      if(pos1 == null || pos2 == null) return encompassedBlocks;
      int minX = Math.min(pos1.getX(), pos2.getX());
      int maxX = Math.max(pos1.getX(), pos2.getX());
      int minY = Math.min(pos1.getY(), pos2.getY());
      int maxY = Math.max(pos1.getY(), pos2.getY());
      int minZ = Math.min(pos1.getZ(), pos2.getZ());
      int maxZ = Math.max(pos1.getZ(), pos2.getZ());
      
      for(int x = minX; x <= maxX; x++){
         for(int y = minY; y <= maxY; y++){
            for(int z = minZ; z <= maxZ; z++){
               encompassedBlocks.add(new BlockPos(x, y, z));
            }
         }
      }
      return encompassedBlocks;
   }
   
   public boolean isInRange(BlockPos pos){
      return pos.distChessboard(getBlockPos()) <= maxRange;
   }
   
   public void setSelectedPos(BlockPos selectedPos){
      if(selectedPos == null || !isInRange(selectedPos)){
         this.selectedPos = null;
         return;
      }
      if(this.selectedPos == null){
         this.selectedPos = selectedPos;
      }else{
         boolean remove = this.blocks.contains(this.selectedPos);
         if(remove){
            this.blocks.removeAll(calculateEncompassedBlocks(this.selectedPos, selectedPos));
            setChanged();
            updateZoneRegistration();
         }else{
            Set<BlockPos> added = calculateEncompassedBlocks(this.selectedPos, selectedPos);
            if(added.size() + blocks.size() <= maxBlocks){
               this.blocks.addAll(added);
               setChanged();
               updateZoneRegistration();
            }
         }
         this.selectedPos = null;
      }
   }
   
   private void updateZoneRegistration(){
      if(!(this.level instanceof ServerLevel serverLevel)) return;
      
      // Unregister old zone if exists
      if(currentZone != null){
         unregisterZone(currentZone);
         currentZone = null;
      }
      
      // Register new zone if we have blocks
      if(!this.blocks.isEmpty()){
         currentZone = new ItineranteurZone(this.blocks, serverLevel.dimension(), this.speedBoost, this.keepFed, this);
         registerZone(currentZone);
      }
   }
   
   @Override
   public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState){
      super.preRemoveSideEffects(blockPos, blockState);
      this.editor = null;
      this.displays.forEach(ElementHolder::destroy);
   }
   
   public ServerPlayer getEditor(){
      return editor;
   }
   
   public void setEditor(ServerPlayer editor){
      if(this.editor != null && editor == null){
         ArcanaNovum.ITINERANTEUR_USERS.remove(this.editor);
         this.highlightedPos = null;
         this.selectedPos = null;
      }else{
         ArcanaNovum.ITINERANTEUR_USERS.put(editor, this);
      }
      this.editor = editor;
   }
   
   public TreeMap<ArcanaAugment, Integer> getAugments(){
      return augments;
   }
   
   public String getCrafterId(){
      return crafterId;
   }
   
   public String getUuid(){
      return uuid;
   }
   
   public int getOrigin(){
      return origin;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.ITINERANTEUR;
   }
   
   /**
    * Computes the minimum number of lines to outline all blocks as continuous solids.
    * Lines are merged when collinear to reduce the total count.
    * Only generates edges on the actual boundary of the volume.
    * Rule: An edge is drawn if both adjacent blocks (that would share that edge) are NOT in the set.
    *
    * @return A list of line segments as Edge
    */
   private List<Edge> getOutlineLines(Set<BlockPos> blockSet){
      if(blockSet.isEmpty()) return new ArrayList<>();
      
      Set<Edge> boundaryEdges = new HashSet<>();
      
      for(BlockPos pos : blockSet){
         int x = pos.getX();
         int y = pos.getY();
         int z = pos.getZ();
         
         // For each of the 12 edges of this block, check if the two adjacent blocks
         // (that would share this edge) are both NOT in the set
         
         // Top face edges (y + 1)
         // Top-North edge: adjacent blocks are above and north
         if(!blockSet.contains(pos.above()) && !blockSet.contains(pos.north())){
            boundaryEdges.add(new Edge(x, y + 1, z, x + 1, y + 1, z).normalizeForHashing());
         }
         // Top-South edge: adjacent blocks are above and south
         if(!blockSet.contains(pos.above()) && !blockSet.contains(pos.south())){
            boundaryEdges.add(new Edge(x, y + 1, z + 1, x + 1, y + 1, z + 1).normalizeForHashing());
         }
         // Top-West edge: adjacent blocks are above and west
         if(!blockSet.contains(pos.above()) && !blockSet.contains(pos.west())){
            boundaryEdges.add(new Edge(x, y + 1, z, x, y + 1, z + 1).normalizeForHashing());
         }
         // Top-East edge: adjacent blocks are above and east
         if(!blockSet.contains(pos.above()) && !blockSet.contains(pos.east())){
            boundaryEdges.add(new Edge(x + 1, y + 1, z, x + 1, y + 1, z + 1).normalizeForHashing());
         }
         
         // Bottom face edges (y)
         // Bottom-North edge: adjacent blocks are below and north
         if(!blockSet.contains(pos.below()) && !blockSet.contains(pos.north())){
            boundaryEdges.add(new Edge(x, y, z, x + 1, y, z).normalizeForHashing());
         }
         // Bottom-South edge: adjacent blocks are below and south
         if(!blockSet.contains(pos.below()) && !blockSet.contains(pos.south())){
            boundaryEdges.add(new Edge(x, y, z + 1, x + 1, y, z + 1).normalizeForHashing());
         }
         // Bottom-West edge: adjacent blocks are below and west
         if(!blockSet.contains(pos.below()) && !blockSet.contains(pos.west())){
            boundaryEdges.add(new Edge(x, y, z, x, y, z + 1).normalizeForHashing());
         }
         // Bottom-East edge: adjacent blocks are below and east
         if(!blockSet.contains(pos.below()) && !blockSet.contains(pos.east())){
            boundaryEdges.add(new Edge(x + 1, y, z, x + 1, y, z + 1).normalizeForHashing());
         }
         
         // Vertical edges
         // North-West vertical: adjacent blocks are north and west
         if(!blockSet.contains(pos.north()) && !blockSet.contains(pos.west())){
            boundaryEdges.add(new Edge(x, y, z, x, y + 1, z).normalizeForHashing());
         }
         // North-East vertical: adjacent blocks are north and east
         if(!blockSet.contains(pos.north()) && !blockSet.contains(pos.east())){
            boundaryEdges.add(new Edge(x + 1, y, z, x + 1, y + 1, z).normalizeForHashing());
         }
         // South-West vertical: adjacent blocks are south and west
         if(!blockSet.contains(pos.south()) && !blockSet.contains(pos.west())){
            boundaryEdges.add(new Edge(x, y, z + 1, x, y + 1, z + 1).normalizeForHashing());
         }
         // South-East vertical: adjacent blocks are south and east
         if(!blockSet.contains(pos.south()) && !blockSet.contains(pos.east())){
            boundaryEdges.add(new Edge(x + 1, y, z + 1, x + 1, y + 1, z + 1).normalizeForHashing());
         }
      }
      
      // Separate edges by their axis direction
      List<Edge> xEdges = new ArrayList<>();
      List<Edge> yEdges = new ArrayList<>();
      List<Edge> zEdges = new ArrayList<>();
      
      for(Edge edge : boundaryEdges){
         if(edge.isAlongX()){
            xEdges.add(edge);
         }else if(edge.isAlongY()){
            yEdges.add(edge);
         }else if(edge.isAlongZ()){
            zEdges.add(edge);
         }
      }
      
      // Merge collinear edges
      List<Edge> result = new ArrayList<>();
      result.addAll(mergeCollinearEdges(xEdges, Direction.Axis.X));
      result.addAll(mergeCollinearEdges(yEdges, Direction.Axis.Y));
      result.addAll(mergeCollinearEdges(zEdges, Direction.Axis.Z));
      
      return result;
   }
   
   private List<Edge> mergeCollinearEdges(List<Edge> edges, Direction.Axis axis){
      List<Edge> merged = new ArrayList<>();
      
      // Group edges by their fixed coordinates
      Map<Long, List<Edge>> grouped = new HashMap<>();
      for(Edge edge : edges){
         long key = switch(axis){
            case X -> Double.doubleToLongBits(edge.y1) ^ (Double.doubleToLongBits(edge.z1) * 31);
            case Y -> Double.doubleToLongBits(edge.x1) ^ (Double.doubleToLongBits(edge.z1) * 31);
            case Z -> Double.doubleToLongBits(edge.x1) ^ (Double.doubleToLongBits(edge.y1) * 31);
         };
         grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(edge);
      }
      
      // For each group, sort by the varying coordinate and merge adjacent segments
      for(List<Edge> group : grouped.values()){
         group.sort(Comparator.comparingDouble(e -> switch(axis){
            case X -> Math.min(e.x1, e.x2);
            case Y -> Math.min(e.y1, e.y2);
            case Z -> Math.min(e.z1, e.z2);
         }));
         
         // Merge adjacent edges
         Edge current = null;
         for(Edge edge : group){
            if(current == null){
               current = edge.normalized(axis);
            }else{
               Edge normalized = edge.normalized(axis);
               double currentEnd = switch(axis){
                  case X -> current.x2;
                  case Y -> current.y2;
                  case Z -> current.z2;
               };
               double normalizedStart = switch(axis){
                  case X -> normalized.x1;
                  case Y -> normalized.y1;
                  case Z -> normalized.z1;
               };
               
               if(Math.abs(currentEnd - normalizedStart) < 0.001){
                  // Extend current edge
                  current = new Edge(current.x1, current.y1, current.z1, normalized.x2, normalized.y2, normalized.z2);
               }else{
                  // Gap found, output current and start new
                  merged.add(current);
                  current = normalized;
               }
            }
         }
         if(current != null){
            merged.add(current);
         }
      }
      
      return merged;
   }
   
   private record Edge(double x1, double y1, double z1, double x2, double y2, double z2) {
      boolean isAlongX(){
         return Math.abs(y1 - y2) < 0.001 && Math.abs(z1 - z2) < 0.001; // Y and Z constant
      }
      
      boolean isAlongY(){
         return Math.abs(x1 - x2) < 0.001 && Math.abs(z1 - z2) < 0.001; // X and Z constant
      }
      
      boolean isAlongZ(){
         return Math.abs(x1 - x2) < 0.001 && Math.abs(y1 - y2) < 0.001; // X and Y constant
      }
      
      // Normalize edge so that the "start" point has the smaller varying coordinate
      private Edge normalized(Direction.Axis axis){
         boolean needsSwap = switch(axis){
            case X -> x1 > x2;
            case Y -> y1 > y2;
            case Z -> z1 > z2;
         };
         if(needsSwap){
            return new Edge(x2, y2, z2, x1, y1, z1);
         }
         return this;
      }
      
      // Normalize for consistent hashing - always put the "smaller" point first
      private Edge normalizeForHashing(){
         if(x1 < x2) return this;
         if(x1 > x2) return new Edge(x2, y2, z2, x1, y1, z1);
         if(y1 < y2) return this;
         if(y1 > y2) return new Edge(x2, y2, z2, x1, y1, z1);
         if(z1 < z2) return this;
         if(z1 > z2) return new Edge(x2, y2, z2, x1, y1, z1);
         return this;
      }
      
      private Tuple<Vec3, Vec3> toTuple(){
         return new Tuple<>(new Vec3(x1, y1, z1), new Vec3(x2, y2, z2));
      }
      
      private Vec3 middlePos(){
         return new Vec3((x1 + x2) / 2.0, (y1 + y2) / 2.0, (z1 + z2) / 2.0);
      }
      
      private Vec3 start(){
         return new Vec3(x1, y1, z1);
      }
      
      private Vec3 end(){
         return new Vec3(x2, y2, z2);
      }
      
      private Vec3 getDirection(){
         return end().subtract(start());
      }
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      this.blocks.clear();
      this.blocks.addAll(view.read(BLOCKS_TAG, BlockPos.CODEC.listOf()).orElse(new ArrayList<>()));
      this.maxRange = 16 * (1 + ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.GUIDING_LIGHT));
      this.maxBlocks = 64 * (2 + ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.THOROUGHFARE));
      this.speedBoost = BOOST_LEVELS[ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.PAVED_WARMTH)];
      this.keepFed = ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.ROAD_SNACKS) > 0;
      updateZoneRegistration();
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC, this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG, this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG, this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME, this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG, this.origin);
      view.store(BLOCKS_TAG, BlockPos.CODEC.listOf(), new ArrayList<>(blocks));
   }
   
   private void createDisplayForBlockSet(ServerLevel serverLevel, Set<BlockPos> set, Block type, float thickness){
      for(Edge edge : getOutlineLines(set)){
         GridHolder tempHolder = new GridHolder(edge, type);
         Optional<GridHolder> existing = displays.stream().filter(h -> h.equals(tempHolder)).findFirst();
         if(existing.isPresent()){
            existing.get().refresh();
         }else{
            GridHolder holder = new GridHolder(edge, this, type, thickness);
            ManualAttachment attachment = new ManualAttachment(holder, serverLevel, holder::getPos);
            holder.setAttachment(attachment);
            displays.add(holder);
         }
      }
   }
   
   private static BlockDisplayElement createGridElement(Edge edge, Block block, float thickness){
      BlockDisplayElement element = new BlockDisplayElement();
      element.setBlockState(block.defaultBlockState());
      element.setBrightness(Brightness.FULL_BRIGHT);
      
      Vec3 direction = edge.getDirection();
      float length = (float) direction.length() + thickness;
      element.setScale(new Vector3f(length, thickness, thickness));
      
      float halfThickness = thickness / 2.0f;
      float halfLength = length / 2.0f;
      
      Vec3 normalizedDir = direction.normalize();
      Vec3 defaultAxis = new Vec3(1, 0, 0);
      
      Quaternionf rotation = new Quaternionf().rotationTo(defaultAxis.toVector3f(), normalizedDir.toVector3f());
      element.setLeftRotation(rotation);
      Vec3 localOffset = new Vec3(-halfLength, -halfThickness, -halfThickness);
      Vector3f rotatedOffset = new Vector3f((float) localOffset.x, (float) localOffset.y, (float) localOffset.z);
      rotation.transform(rotatedOffset);
      element.setOffset(new Vec3(rotatedOffset.x, rotatedOffset.y, rotatedOffset.z));
      return element;
   }
   
   private static class GridHolder extends ElementHolder {
      int lifeTime = REFRESH_DUR;
      boolean destroyed;
      Vec3 pos;
      Edge edge;
      Block block;
      BlockDisplayElement element;
      ItineranteurBlockEntity itineranteur;
      
      // Lightweight constructor for equality checking only
      public GridHolder(Edge edge, Block block){
         super();
         this.edge = edge;
         this.block = block;
      }
      
      public GridHolder(Edge edge, ItineranteurBlockEntity itineranteur, Block block, float thickness){
         super();
         this.block = block;
         this.edge = edge;
         this.pos = edge.middlePos();
         this.element = createGridElement(edge, block, thickness);
         this.itineranteur = itineranteur;
         addElement(this.element);
      }
      
      public void refresh(){
         lifeTime = REFRESH_DUR + 1;
      }
      
      public Edge getEdge(){
         return this.edge;
      }
      
      public Block getBlock(){
         return this.block;
      }
      
      public boolean wasDestroyed(){
         return destroyed;
      }
      
      @Override
      public int hashCode(){
         return Objects.hash(edge.normalizeForHashing(), block);
      }
      
      @Override
      public boolean equals(Object obj){
         if(this == obj) return true;
         if(!(obj instanceof GridHolder other)) return false;
         return this.edge.normalizeForHashing().equals(other.edge.normalizeForHashing()) && this.block.equals(other.block);
      }
      
      @Override
      public Vec3 getPos(){
         return pos;
      }
      
      @Override
      protected void onTick(){
         super.onTick();
         
         if(itineranteur.getEditor() == null){
            destroyed = true;
            destroy();
         }else if(lifeTime-- <= 0){
            destroyed = true;
            destroy(); // Time expired, remove
         }else if(getAttachment() == null || getAttachment().getWorld() == null){
            destroyed = true;
            destroy();
         }
         
         if(!wasDestroyed()){
            if(itineranteur.getEditor() != null){
               startWatching(itineranteur.getEditor());
            }
         }
      }
   }
   
   public static class ItineranteurZone {
      private final Set<BlockPos> blocks;
      private final Set<Long> chunks;
      private final ResourceKey<Level> dimension;
      private final float speedBoost;
      private final boolean keepFed;
      private final ItineranteurBlockEntity blockEntity;
      private int keepAlive;
      
      private ItineranteurZone(Set<BlockPos> blocks, ResourceKey<Level> dimension, float speedBoost, boolean keepFed, ItineranteurBlockEntity blockEntity){
         this.blocks = new HashSet<>(blocks);
         this.dimension = dimension;
         this.speedBoost = speedBoost;
         this.keepFed = keepFed;
         this.blockEntity = blockEntity;
         this.keepAlive = KEEP_ALIVE;
         this.chunks = new HashSet<>();
         for(BlockPos pos : blocks){
            chunks.add(getChunkKey(pos));
         }
      }
      
      public boolean containsBlock(BlockPos pos){
         return blocks.contains(pos);
      }
      
      public Set<Long> getChunks(){
         return chunks;
      }
      
      public void decrementKeepAlive(){
         keepAlive--;
      }
      
      public boolean checkExpired(){
         if(keepAlive <= 0){
            // Clear the block entity's reference so it can re-register
            if(blockEntity.currentZone == this){
               blockEntity.currentZone = null;
            }
            return true;
         }
         return false;
      }
      
      public void refresh(){
         keepAlive = KEEP_ALIVE;
      }
      
      public float getSpeedBoost(){
         return speedBoost;
      }
      
      public boolean keepsPlayerFed(){
         return keepFed;
      }
      
      public BlockPos getSource(){
         return blockEntity.getBlockPos();
      }
      
      public ItineranteurBlockEntity getBlockEntity(){
         return blockEntity;
      }
      
      @Override
      public int hashCode(){
         return blockEntity.getBlockPos().hashCode();
      }
      
      @Override
      public boolean equals(Object obj){
         if(this == obj) return true;
         if(!(obj instanceof ItineranteurZone other)) return false;
         return this.getSource().equals(other.getSource()) && this.dimension.equals(other.dimension);
      }
   }
   
   public static void tickPlayer(ServerPlayer player){
      ServerLevel serverLevel = player.level();
      BlockPos pos = player.getOnPos();
      
      float speed = 0;
      boolean feed = false;
      ItineranteurBlockEntity blockEntity = null;
      Map<Long, Set<ItineranteurZone>> dimensionZones = ACTIVE_ZONES.get(serverLevel.dimension());
      if(dimensionZones == null){
         ArcanaPlayerData data = ArcanaNovum.data(player);
         data.removeMiscData(CRAFTER_TAG);
         data.removeMiscData(FED_TAG);
         MinecraftUtils.attributeEffect(player, Attributes.MOVEMENT_SPEED, speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Identifier.fromNamespaceAndPath(MOD_ID,ArcanaRegistry.ITINERANTEUR.getId()),true);
         return;
      }
      
      long chunkKey = getChunkKey(pos);
      Set<ItineranteurZone> chunkZones = dimensionZones.get(chunkKey);
      if(chunkZones == null){
         ArcanaPlayerData data = ArcanaNovum.data(player);
         data.removeMiscData(CRAFTER_TAG);
         data.removeMiscData(FED_TAG);
         MinecraftUtils.attributeEffect(player, Attributes.MOVEMENT_SPEED, speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Identifier.fromNamespaceAndPath(MOD_ID,ArcanaRegistry.ITINERANTEUR.getId()),true);
         return;
      }
      
      for(ItineranteurZone zone : chunkZones){
         if(zone.containsBlock(pos)){
            blockEntity = zone.getBlockEntity();
            speed = Math.max(speed, zone.getSpeedBoost());
            feed = feed || zone.keepsPlayerFed();
         }
      }
      
      if(blockEntity != null){
         ArcanaPlayerData data = ArcanaNovum.data(player);
         String crafterId = blockEntity.getCrafterId();
         if(crafterId != null && !crafterId.isEmpty()){
            data.addMiscData(CRAFTER_TAG, StringTag.valueOf(crafterId));
         }else{
            data.removeMiscData(CRAFTER_TAG);
         }
         data.addMiscData(FED_TAG, ByteTag.valueOf(feed));
         MinecraftUtils.attributeEffect(player, Attributes.MOVEMENT_SPEED, speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Identifier.fromNamespaceAndPath(MOD_ID,ArcanaRegistry.ITINERANTEUR.getId()),false);
      }else{
         ArcanaPlayerData data = ArcanaNovum.data(player);
         data.removeMiscData(CRAFTER_TAG);
         data.removeMiscData(FED_TAG);
         MinecraftUtils.attributeEffect(player, Attributes.MOVEMENT_SPEED, speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Identifier.fromNamespaceAndPath(MOD_ID,ArcanaRegistry.ITINERANTEUR.getId()),true);
      }
   }
   
   public static @Nullable ItineranteurZone getZoneAtPlayer(ServerPlayer player, boolean withFeed){
      return getZoneAtPos(player.level(), player.getOnPos(), withFeed);
   }
   
   public static @Nullable ItineranteurZone getZoneAtPos(ServerLevel level, BlockPos pos, boolean withFeed){
      Map<Long, Set<ItineranteurZone>> dimensionZones = ACTIVE_ZONES.get(level.dimension());
      if(dimensionZones == null) return null;
      
      long chunkKey = getChunkKey(pos);
      Set<ItineranteurZone> chunkZones = dimensionZones.get(chunkKey);
      if(chunkZones == null) return null;
      
      for(ItineranteurZone zone : chunkZones){
         if(!zone.keepsPlayerFed() && withFeed) continue;
         if(zone.containsBlock(pos)){
            return zone;
         }
      }
      return null;
   }
   
   public static boolean isPlayerInZone(ServerPlayer player, boolean withFeed){
      return getZoneAtPlayer(player, withFeed) != null;
   }
   
   private static long getChunkKey(BlockPos pos){
      return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
   }
   
   private static void registerZone(ItineranteurZone zone){
      Map<Long, Set<ItineranteurZone>> dimensionZones = ACTIVE_ZONES.computeIfAbsent(zone.dimension, k -> new HashMap<>());
      for(long chunkKey : zone.getChunks()){
         dimensionZones.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(zone);
      }
   }
   
   private static void unregisterZone(ItineranteurZone zone){
      Map<Long, Set<ItineranteurZone>> dimensionZones = ACTIVE_ZONES.get(zone.dimension);
      if(dimensionZones == null) return;
      
      for(long chunkKey : zone.getChunks()){
         Set<ItineranteurZone> chunkZones = dimensionZones.get(chunkKey);
         if(chunkZones != null){
            chunkZones.remove(zone);
            if(chunkZones.isEmpty()){
               dimensionZones.remove(chunkKey);
            }
         }
      }
      
      if(dimensionZones.isEmpty()){
         ACTIVE_ZONES.remove(zone.dimension);
      }
   }
   
   public static void tickZones(){
      // Collect all unique zones first
      Set<ItineranteurZone> allZones = new HashSet<>();
      for(Map<Long, Set<ItineranteurZone>> dimensionZones : ACTIVE_ZONES.values()){
         for(Set<ItineranteurZone> chunkZones : dimensionZones.values()){
            allZones.addAll(chunkZones);
         }
      }
      
      // Decrement keepalive for each unique zone once
      for(ItineranteurZone zone : allZones){
         zone.decrementKeepAlive();
      }
      
      // Find expired zones
      Set<ItineranteurZone> expiredZones = new HashSet<>();
      for(ItineranteurZone zone : allZones){
         if(zone.checkExpired()){
            expiredZones.add(zone);
         }
      }
      
      // Remove expired zones from all chunk sets
      for(ItineranteurZone expired : expiredZones){
         unregisterZone(expired);
      }
   }
}
