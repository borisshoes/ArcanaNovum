package net.borisshoes.arcananovum.blocks.astralgateway;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

public class AstralGatewayBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, ContainerListener, PolymerObject, ArcanaBlockEntity, Portal {
   
   private static final String STATE_TIME_TAG = "stateTime";
   private static final String STARDUST_TAG = "stardust";
   private static final String FRAME_TAG = "frame";
   private static final String SYNCED_TAG = "synced";
   private static final String DIALLER_TAG = "dialler";
   private static final String WARMUP_DURATION_TAG = "warmupDuration";
   private static final String COOLDOWN_DURATION_TAG = "cooldownDuration";
   private static final String FIND_COOLDOWN_TAG = "findCooldown";
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private GatewayFrame frame;
   private int stateTime;
   private long stardust;
   private AstralGatewayBlockEntity syncedGateway;
   private boolean dialler = false;
   private boolean updating = false;
   private Map<BlockPos, Integer> blockDistances;
   private SimpleContainer inventory = new SimpleContainer(getContainerSize());
   private boolean astralStargate;
   private int recyclerLvl;
   private boolean forceRectangular;
   private boolean forceConvex;
   private boolean tryFind = false;
   private int openingStardust;
   private int stardustPerMinute;
   private double stardustAccumulator;
   private int warmupDuration;
   private int cooldownDuration;
   private int findCooldown;
   private ElementHolder hologram;
   private HolderAttachment attachment;
   private int interactCooldown = 0;
   private static final int LOCK_DURATION = 100;
   private static final int STARDUST_PER_MINUTE = 64;
   private static final double[] RECYCLER_RATE = new double[]{1.0,0.9,0.8,0.7,0.5,0.25,0};
   
   public AstralGatewayBlockEntity(BlockPos blockPos, BlockState blockState){
      super(ArcanaRegistry.ASTRAL_GATEWAY_BLOCK_ENTITY, blockPos, blockState);
   }
   
   @Override
   public void initialize(TreeMap<ArcanaAugment, Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
      
      forceRectangular = ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.ASTRAL_STABILIZERS) <= 0;
      forceConvex = ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.ASTRAL_STABILIZERS) <= 1;
      astralStargate = ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.ASTRAL_STARGATE) > 0;
      recyclerLvl = ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.STARLIGHT_RECYCLERS);
      
      int stardustPerMinute = (int) Math.round(STARDUST_PER_MINUTE * RECYCLER_RATE[recyclerLvl]);
      this.stardustPerMinute = stardustPerMinute;
      this.stardustAccumulator = 0.0;
      openingStardust = (int) Math.ceil(stardustPerMinute * (LOCK_DURATION / 1200.0)) + stardustPerMinute;
      inventory.addListener(this);
      tryFind = true;
   }
   
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof AstralGatewayBlockEntity gateway){
         gateway.tick();
      }
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(serverWorld.getServer().getTickCount() % 10 == 0){
         if(validWaystone(this.inventory.getItem(0))){
            Vec3 pos = getHologramPos();
            Player player = serverWorld.getNearestPlayer(pos.x(),pos.y(),pos.z(), 32, entity -> true);
            if(player != null && hologram == null){
               hologram = getNewHologram(serverWorld);
               if(hologram != null) attachment = ChunkAttachment.ofTicking(this.hologram,serverWorld,pos);
            }else if(player == null && hologram != null){
               hologram.destroy();
               hologram = null;
            }
         }
      }
      
      GatewayState state = getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE);
      if(serverWorld.getServer().getTickCount() % 20 == 0 && state != GatewayState.CLOSED){
         ArcanaNovum.addActiveBlock(new Tuple<>(this,this));
      }
      
      if(serverWorld.getServer().getTickCount() % 200 == 0 && state != GatewayState.CLOSED){
         serverWorld.getChunkSource().addTicketWithRadius(TicketType.PORTAL, new ChunkPos(this.getBlockPos()), 2);
         if(syncedGateway != null && syncedGateway.getLevel() instanceof ServerLevel otherLevel){
            otherLevel.getChunkSource().addTicketWithRadius(TicketType.PORTAL, new ChunkPos(syncedGateway.getBlockPos()), 2);
         }
      }
      
      if(state == GatewayState.CLOSED){
         if(this.frame == null){
            if(findCooldown == 0 && tryFind){
               validateOrFindGateway(serverWorld);
               tryFind = false;
               findCooldown = 100;
            }else if(findCooldown > 0){
               findCooldown--;
            }
         }else{
            if(serverWorld.getServer().getTickCount() % 200 == 0){
               boolean check = validateCurrentGateway(serverWorld);
               if(!check){
                  this.frame = null;
               }
            }
         }
      }else{
         boolean check = validateCurrentGateway(serverWorld);
         if(!check){
            frameBreak();
            return;
         }
         if(state == GatewayState.WARMUP){
            if(stateTime == 0){
               this.blockDistances = this.frame.getEnclosedDistances();
               int maxDepth = blockDistances.values().stream().max(Integer::compareTo).orElse(1);
               int ticksPerDepth = 10;
               int totalPortalTicks = ticksPerDepth*maxDepth;
               int totalStarTicks = warmupDuration - totalPortalTicks;
               BlockPos pos = getBlockPos();
               for(Map.Entry<BlockPos, Integer> entry : this.blockDistances.entrySet()){
                  int delay = totalStarTicks + ticksPerDepth*entry.getValue() + (int)(Math.random()*ticksPerDepth);
                  int initialKeepAlive = LOCK_DURATION + (warmupDuration - delay) + 1;
                  BorisLib.addTickTimerCallback(serverWorld,new GenericTimer(delay,() -> {
                     serverWorld.setBlock(entry.getKey(), ArcanaRegistry.ASTRAL_GATEWAY_PORTAL_BLOCK.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                     BlockEntity be = serverWorld.getBlockEntity(entry.getKey());
                     if(be instanceof AstralGatewayPortalBlockEntity portalEntity){
                        portalEntity.initialize(pos, initialKeepAlive);
                     }
                  }));
               }
               
               ArcanaEffectUtils.astralGatewayWarmup(this, serverWorld, this.frame.getOrderedBoundaryPoints().stream().distinct().toList(), totalStarTicks, warmupDuration, 0);
            }
            if(stateTime >= warmupDuration) setState(GatewayState.LOCKED_OPEN);
         }else if(state == GatewayState.LOCKED_OPEN){
            if(stateTime >= LOCK_DURATION){
               setState(GatewayState.OPEN);
            }
         }else if(state == GatewayState.COOLDOWN){
            if(stateTime == 0) SoundUtils.playSound(this.level,getBlockPos(), SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS,1f,0.1f);
            if(stateTime == cooldownDuration/2) SoundUtils.playSound(this.level,getBlockPos(), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS,0.4f,1.5f);
            if(stateTime >= cooldownDuration) setState(GatewayState.CLOSED);
         }else{ // Open
            if(this.blockDistances == null){
               this.blockDistances = this.frame.getEnclosedDistances();
            }
            int maxDepth = blockDistances.values().stream().max(Integer::compareTo).orElse(1);
            int ticksPerLayer = cooldownDuration / maxDepth;
            for(Map.Entry<BlockPos, Integer> entry : this.blockDistances.entrySet()){
               BlockState portalState = serverWorld.getBlockState(entry.getKey());
               if(!portalState.is(ArcanaRegistry.ASTRAL_GATEWAY_PORTAL_BLOCK)){
                  frameBreak();
                  return;
               }else{
                  BlockEntity be = serverWorld.getBlockEntity(entry.getKey());
                  if(be instanceof AstralGatewayPortalBlockEntity portalEntity){
                     portalEntity.refreshKeepAlive(ticksPerLayer * (maxDepth - entry.getValue() + 1) + (int)(Math.random()*ticksPerLayer));
                  }
               }
            }
         }
         
         if(state == GatewayState.LOCKED_OPEN || state == GatewayState.OPEN){
            // Smooth stardust consumption using fractional accumulator
            stardustAccumulator += stardustPerMinute / 1200.0; // 1200 ticks per minute
            if(stardustAccumulator >= 1.0){
               int toConsume = (int) stardustAccumulator;
               this.stardust -= Math.min(this.stardust, toConsume);
               stardustAccumulator -= toConsume;
               evaluateForOpenOrClose();
            }
         }
      }
      stateTime++;
   }
   
   private GatewayFrame findFrame(ServerLevel serverWorld){
      List<BlockPos> startPosList = new ArrayList<>();
      BlockPos thisPos = this.getBlockPos();
      for(Direction value : Direction.values()){
         if(value == Direction.DOWN){
            startPosList.add(thisPos.offset(value.getUnitVec3i()));
         }else if(value == Direction.UP){
            continue;
         }else{
            startPosList.add(thisPos.offset(value.getUnitVec3i()));
            startPosList.add(thisPos.offset(value.getUnitVec3i()).offset(Direction.UP.getUnitVec3i()));
            startPosList.add(thisPos.offset(value.getUnitVec3i()).offset(Direction.DOWN.getUnitVec3i()));
         }
      }
      return GatewayFrame.search(serverWorld, startPosList, thisPos, 64, forceRectangular, forceConvex, 100000);
   }
   
   private boolean validateCurrentGateway(ServerLevel serverWorld){
      if(frame == null) return false;
      if(!frame.finishedAndValid()) return false;
      
      Block type = frame.getType();
      for(BlockPos block : frame.getBlocks()){
         if(!serverWorld.getBlockState(block).is(type)) return false;
      }
      for(BlockPos blockPos : frame.getEnclosed()){
         BlockState state = serverWorld.getBlockState(blockPos);
         if(state.isAir() || state.is(ArcanaRegistry.ASTRAL_GATEWAY_PORTAL_BLOCK)) continue;
         return false;
      }
      return true;
   }
   
   private boolean hasEnoughClearance(Vec3 pos, double width, double height, Level level){
      AABB entityBox = new AABB(
            pos.x - width / 2, pos.y, pos.z - width / 2,
            pos.x + width / 2, pos.y + height, pos.z + width / 2
      );
      return level.noCollision(entityBox);
   }
   
   @Override
   public @Nullable TeleportTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos blockPos){
      if(!(entity.level() instanceof ServerLevel sourceLevel)){
         return null;
      }
      
      if(syncedGateway == null || syncedGateway == this || syncedGateway.frame == null){
         return null;
      }
      
      if(!(syncedGateway.level instanceof ServerLevel destLevel)){
         return null;
      }
      
      Set<BlockPos> sourceEnclosed = this.frame.getEnclosed();
      Set<BlockPos> destEnclosed = syncedGateway.frame.getEnclosed();
      if(sourceEnclosed.isEmpty() || destEnclosed.isEmpty()){
         return null;
      }
      
      Direction sourceDir = this.getBlockState().getValue(AstralGateway.AstralGatewayBlock.HORIZONTAL_FACING);
      Direction destDir = syncedGateway.getBlockState().getValue(AstralGateway.AstralGatewayBlock.HORIZONTAL_FACING);
      Direction.Axis sourceAxis = this.frame.getAxis();
      Direction.Axis destAxis = this.syncedGateway.frame.getAxis();
      boolean thisY = sourceAxis == Direction.Axis.Y;
      boolean thatY = destAxis == Direction.Axis.Y;
      
      float finalYaw = entity.getYRot();
      if(thatY){ // no rotation
         finalYaw = 0;
      }else if(thisY){ // Align directly to portal
         finalYaw = destDir.toYRot()-finalYaw;
      }else{ // Rotate player
         float sourceYaw = sourceDir.toYRot();
         float destYaw = destDir.toYRot();
         float deltaYaw = sourceYaw-destYaw;
         finalYaw = deltaYaw;
      }
      
      int minSourceX = Integer.MAX_VALUE, minSourceY = Integer.MAX_VALUE, minSourceZ = Integer.MAX_VALUE;
      for(BlockPos pos : sourceEnclosed){
         minSourceX = Math.min(minSourceX, pos.getX());
         minSourceY = Math.min(minSourceY, pos.getY());
         minSourceZ = Math.min(minSourceZ, pos.getZ());
      }
      BlockPos minSourcePos = new BlockPos(minSourceX, minSourceY, minSourceZ);
      Vec3i offset = blockPos.subtract(minSourcePos);
      
      // Extract the 2D offset components based on source portal axis
      double sourceU, sourceV; // U and V are the two axes perpendicular to the portal's normal (0-1)
      Tuple<Integer,Integer> sourceBounds = getPortalBounds(sourceEnclosed, sourceAxis);
      if(sourceAxis == Direction.Axis.X){
         sourceU = (double) offset.getZ() / sourceBounds.getA();
         sourceV = (double) offset.getY() / sourceBounds.getB();
      }else if(sourceAxis == Direction.Axis.Y){
         sourceU = (double) offset.getX() / sourceBounds.getA();
         sourceV = (double) offset.getZ() / sourceBounds.getB();
      }else{ // Z axis
         sourceU = (double) offset.getX() / sourceBounds.getA();
         sourceV = (double) offset.getY() / sourceBounds.getB();
      }
      
      int minDestX = Integer.MAX_VALUE, minDestY = Integer.MAX_VALUE, minDestZ = Integer.MAX_VALUE;
      for(BlockPos pos : destEnclosed){
         minDestX = Math.min(minDestX, pos.getX());
         minDestY = Math.min(minDestY, pos.getY());
         minDestZ = Math.min(minDestZ, pos.getZ());
      }
      BlockPos minDestPos = new BlockPos(minDestX, minDestY, minDestZ);
      
      
      // Scale to destination bounds
      Tuple<Integer,Integer> destBounds = getPortalBounds(destEnclosed, destAxis);
      double destU = sourceU * destBounds.getA();
      double destV = sourceV * destBounds.getB();
      
      // Convert back to world coordinates based on destination axis
      Vec3 destOffset;
      if(destAxis == Direction.Axis.X){
         destOffset = new Vec3(0, destV, destU);
      }else if(destAxis == Direction.Axis.Y){
         destOffset = new Vec3(destU, 0, destV);
      }else{ // Z axis
         destOffset = new Vec3(destU, destV, 0);
      }
      
      Vec3 idealPos = new Vec3(minDestPos.getX(),minDestPos.getY(),minDestPos.getZ()).add(destOffset);
      BlockPos idealBlockPos = BlockPos.containing(idealPos);
      
      // Snap to nearest enclosed block
      BlockPos snappedPos = destEnclosed.contains(idealBlockPos) ? idealBlockPos : destEnclosed.stream()
            .min(Comparator.comparingDouble(pos -> pos.distSqr(idealBlockPos)))
            .orElse(idealBlockPos);
      
      // Lower to the lowest continuous enclosed block on the Y axis
      while(destEnclosed.contains(snappedPos.below())){
         snappedPos = snappedPos.below();
      }
      
      if(entity instanceof HappyGhast happyGhast){
         for(Entity passenger : happyGhast.getPassengers()){
            if(passenger instanceof ServerPlayer player){
               ArcanaAchievements.grant(player,ArcanaAchievements.CARRIER_HAS_ARRIVED);
            }
         }
      }else if(entity instanceof ServerPlayer player || (entity instanceof TamableAnimal tamableAnimal && tamableAnimal.getOwner() != null)){
         int portalCount = 0;
         for(BlockPos pos : BlockPos.withinManhattan(snappedPos, 16, 16, 16)){
            if(destLevel.getBlockEntity(pos) instanceof AstralGatewayBlockEntity agbe && agbe.getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE) != GatewayState.CLOSED){
               portalCount++;
            }
         }
         if(portalCount >= 3){
            Player nearest = destLevel.getNearestPlayer(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 25, (e) -> true);
            if(nearest instanceof ServerPlayer serverPlayer){
               ArcanaAchievements.grant(serverPlayer,ArcanaAchievements.ON_YOUR_LEFT);
            }
         }
      }
      
      return new TeleportTransition(
            destLevel,
            Vec3.atBottomCenterOf(snappedPos),
            Vec3.ZERO,
            finalYaw,
            0,
            Relative.union(Relative.DELTA, Relative.ROTATION),
            TeleportTransition.PLAY_PORTAL_SOUND
      );
   }
   
   /**
    * Gets the integer length of a portal's U and V axes in its local 2D space.
    * Returns Tuple(uLength, vLength) where U and V are the two axes perpendicular to the portal normal.
    */
   private Tuple<Integer, Integer> getPortalBounds(Set<BlockPos> enclosed, Direction.Axis axis){
      if(enclosed.isEmpty()) return new Tuple<>(0, 0);
      
      int minU = Integer.MAX_VALUE, maxU = Integer.MIN_VALUE;
      int minV = Integer.MAX_VALUE, maxV = Integer.MIN_VALUE;
      
      for(BlockPos pos : enclosed){
         int u, v;
         if(axis == Direction.Axis.X){
            u = pos.getZ();
            v = pos.getY();
         }else if(axis == Direction.Axis.Y){
            u = pos.getX();
            v = pos.getZ();
         }else{ // Z axis
            u = pos.getX();
            v = pos.getY();
         }
         minU = Math.min(minU, u);
         maxU = Math.max(maxU, u);
         minV = Math.min(minV, v);
         maxV = Math.max(maxV, v);
      }
      
      int lengthU = maxU - minU + 1;
      int lengthV = maxV - minV + 1;
      
      return new Tuple<>(lengthU, lengthV);
   }
   
   private AstralGatewayBlockEntity dialGateway(ServerLevel level){
      ItemStack currentStone = this.inventory.getItem(0);
      if(currentStone.isEmpty()) return null;
      if(!validWaystone(currentStone)) return null;
      Waystone.WaystoneTarget target = Waystone.getTarget(currentStone);
      if(target == null) return null;
      BlockPos pos = BlockPos.containing(target.position());
      ResourceKey<Level> world = target.world();
      MinecraftServer server = level.getServer();
      ServerLevel targetLevel = server.getLevel(world);
      if(targetLevel == null) return null;
      //targetLevel.getChunkSource().addTicketWithRadius(TicketType.PORTAL, new ChunkPos(pos), 2);
      BlockEntity blockEntity = targetLevel.getBlockEntity(pos);
      if(!(blockEntity instanceof AstralGatewayBlockEntity otherGateway)) return null;
      if(blockEntity.getBlockState().getValue(AstralGateway.AstralGatewayBlock.MODE) == GatewayMode.SEND_ONLY) return null;
      if(blockEntity.getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE) != GatewayState.CLOSED) return null;
      return otherGateway;
   }
   
   private GatewayFrame validateOrFindGateway(ServerLevel level){
      boolean validated = false;
      if(this.frame != null && this.frame.finishedAndValid()){
         validated = validateCurrentGateway(level);
      }
      if(!validated){
         this.frame = findFrame(level);
         if(this.frame == null) return null;
      }
      return this.frame;
   }
   
   public void tryFind(){
      this.tryFind = true;
   }
   
   private int calculateWarmup(){
      // 20 ticks 'warmup'
      // 5 ticks per star node
      // 10 ticks per portal level
      int calc = 20;
      calc += 5*this.frame.getOrderedBoundaryPoints().stream().distinct().toList().size();
      calc += 10*this.frame.getEnclosedDistances().values().stream().max(Integer::compareTo).orElse(1);
      return calc;
   }
   
   private int calculateCooldown(){
      // 5 ticks per portal level
      return 5*this.frame.getEnclosedDistances().values().stream().max(Integer::compareTo).orElse(1);
   }
   
   public boolean tryActivateGateway(ServerLevel level){
      if(getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE) != GatewayState.CLOSED) return false;
      if(getBlockState().getValue(AstralGateway.AstralGatewayBlock.MODE) == GatewayMode.RECEIVE_ONLY) return false;
      if(this.stardust < openingStardust) return false;
      validateOrFindGateway(level);
      if(this.frame == null) return false;
      AstralGatewayBlockEntity otherGateway = dialGateway(level);
      if(otherGateway == null || otherGateway == this) return false;
      if(otherGateway.stardust < otherGateway.openingStardust) return false;
      ServerLevel otherLevel = (ServerLevel) otherGateway.getLevel();
      GatewayFrame otherFrame = otherGateway.validateOrFindGateway(otherLevel);
      if(otherFrame == null) return false;
      if(otherFrame.getEnclosed().stream().anyMatch(block -> this.frame.getEnclosed().contains(block))) return false;
      this.dialler = true;
      otherGateway.dialler = false;
      this.syncedGateway = otherGateway;
      otherGateway.syncedGateway = this;
      int maxWarmup = Math.max(this.calculateWarmup(),otherGateway.calculateWarmup());
      int maxCooldown = Math.max(this.calculateCooldown(),otherGateway.calculateCooldown());
      this.warmupDuration = maxWarmup;
      otherGateway.warmupDuration = maxWarmup;
      this.cooldownDuration = maxCooldown;
      otherGateway.cooldownDuration = maxCooldown;
      level.getChunkSource().addTicketWithRadius(TicketType.PORTAL, new ChunkPos(this.getBlockPos()), 2);
      otherLevel.getChunkSource().addTicketWithRadius(TicketType.PORTAL, new ChunkPos(otherGateway.getBlockPos()), 2);
      this.setState(GatewayState.WARMUP);
      otherGateway.setState(GatewayState.WARMUP);
      if(this.frame.getType().defaultBlockState().is(BlockTags.BEACON_BASE_BLOCKS)){
         Player player = level.getNearestPlayer(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 25, (e) -> true);
         if(player instanceof ServerPlayer serverPlayer){
            ArcanaAchievements.grant(serverPlayer,ArcanaAchievements.FANCIER_STARGATE);
         }
      }
      if(this.frame.getType().defaultBlockState().is(Blocks.NETHERITE_BLOCK) && this.frame.getBlocks().size() >= 64){
         Player player = level.getNearestPlayer(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 25, (e) -> true);
         if(player instanceof ServerPlayer serverPlayer){
            ArcanaAchievements.grant(serverPlayer,ArcanaAchievements.MODERATELY_INSANE);
         }
      }
      if(!this.level.dimension().identifier().equals(Level.NETHER.identifier()) && otherGateway.level.dimension().identifier().equals(Level.NETHER.identifier())){
         Player player = level.getNearestPlayer(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 25, (e) -> true);
         if(player instanceof ServerPlayer serverPlayer){
            ArcanaAchievements.grant(serverPlayer,ArcanaAchievements.DIALING_HELL);
         }
      }
      
      return true;
   }
   
   public void evaluateForOpenOrClose(){
      boolean closed = getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE) == GatewayState.CLOSED;
      boolean open = getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE) == GatewayState.OPEN;
      if(open || closed){
         boolean hasOpeningStardust = this.stardust >= openingStardust;
         boolean hasAnyStardust = this.stardust > 0 || getStardustPerMinute() == 0;
         boolean hasRedstone = this.level.hasNeighborSignal(getBlockPos());
         boolean validWaystone = validWaystone(this.inventory.getItem(0));
         boolean lostConnection = (this.syncedGateway == null || this.syncedGateway.getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE) != GatewayState.OPEN);
         boolean cannotMaintainDial = dialler && (lostConnection || !validWaystone || !hasRedstone || !hasAnyStardust);
         
         if(open && (cannotMaintainDial || lostConnection || !hasAnyStardust)){
            setState(GatewayState.COOLDOWN);
            if(this.syncedGateway != null) this.syncedGateway.setState(GatewayState.COOLDOWN);
         }else if(closed && hasOpeningStardust && validWaystone && hasRedstone && this.level instanceof ServerLevel serverLevel){
            tryActivateGateway(serverLevel);
         }
      }
   }
   
   private void setState(GatewayState state){
      if(this.level.getBlockState(getBlockPos()).is(((ArcanaBlock)ArcanaRegistry.ASTRAL_GATEWAY).getBlock())){
         if(this.getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE) == GatewayState.CLOSED && state != GatewayState.WARMUP){
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, worldPosition, GameEvent.Context.of(getBlockState()));
         }else if(this.getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE) != GatewayState.CLOSED && state == GatewayState.CLOSED){
            level.gameEvent(GameEvent.BLOCK_DEACTIVATE, worldPosition, GameEvent.Context.of(getBlockState()));
         }
         this.level.setBlock(getBlockPos(),this.getBlockState().setValue(AstralGateway.AstralGatewayBlock.STATE,state),Block.UPDATE_ALL);
      }
      this.stateTime = 0;
      if(state == GatewayState.CLOSED){
         this.syncedGateway = null;
         this.dialler = false;
      }
   }
   
   public boolean interact(ServerPlayer player, ItemStack stack){
      player.getCooldowns().addCooldown(player.getMainHandItem(), 1);
      player.getCooldowns().addCooldown(player.getOffhandItem(), 1);
      // TODO
      return false;
   }
   
   private Vec3 getHologramPos(){
      return this.getBlockPos().getCenter();
   }
   
   private ElementHolder getNewHologram(ServerLevel world){
      ItemStack stone = this.inventory.getItem(0);
      if(!validWaystone(stone)) return null;
      ItemDisplayElement icon = new ItemDisplayElement(stone);
      icon.setInterpolationDuration(3);
      InteractionElement click = new InteractionElement(new VirtualElement.InteractionHandler(){
         public void click(ServerPlayer player, ItemStack stack){
            if(interactCooldown == 0){
               AstralGatewayBlockEntity.this.interact(player,stack);
               interactCooldown = 5;
            }
         }
         
         @Override
         public void interact(ServerPlayer player, InteractionHand hand){
            click(player,player.getItemInHand(hand));
         }
         
         @Override
         public void interactAt(ServerPlayer player, InteractionHand hand, Vec3 pos){
            click(player,player.getItemInHand(hand));
         }
      });
      click.setSize(0.65f,0.65f);
      
      ElementHolder holder = new ElementHolder() {
         ServerLevel serverLevel = world;
         private final ItemDisplayElement iconElem = icon;
         private final InteractionElement clickElem = click;
         private int tickCount = 0;
         private float currentYaw = 0f;
         
         @Override
         protected void onTick(){
            super.onTick();
            
            if(!validWaystone(AstralGatewayBlockEntity.this.inventory.getItem(0))){
               AstralGatewayBlockEntity.this.hologram = null;
               destroy();
               return;
            }
            
            tickCount++;
            if(interactCooldown > 0) interactCooldown--;
            
            float f = Mth.TWO_PI / 80f;
            float yOffset;
            
            if(AstralGatewayBlockEntity.this.getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE) != GatewayState.CLOSED){
               currentYaw -= 5.5f * Mth.DEG_TO_RAD;
               yOffset = 0.625f + 0.85f * f * Mth.cos(f * tickCount) + 0.1f;
            }else{
               currentYaw += 0.5f * Mth.DEG_TO_RAD;
               yOffset = 0.625f + 0.6f * f * Mth.cos(f * tickCount);
            }
            if(currentYaw > Mth.TWO_PI) currentYaw -= Mth.TWO_PI;
            if(currentYaw < 0) currentYaw += Mth.TWO_PI;
            
            Matrix4f matrix = new Matrix4f();
            matrix.translate(0, yOffset, 0);
            matrix.rotateY(currentYaw);
            matrix.scale(0.5f);
            iconElem.setTransformation(matrix);
            iconElem.startInterpolation();
         }
      };
      
      click.setOffset(new Vec3(0,0.375,0));
      icon.setOffset(Vec3.ZERO);
      
      // Set initial transformation before adding to prevent interpolation from default position
      Matrix4f initialMatrix = new Matrix4f();
      initialMatrix.translate(0, 0.625f, 0);
      initialMatrix.scale(0.5f);
      icon.setTransformation(initialMatrix);
      
      holder.addElement(icon);
      holder.addElement(click);
      return holder;
   }
   
   @Override
   public void containerChanged(Container container){
      if(updating) return;
      updating = true;
      ItemStack stardustStack = container.getItem(1);
      if(stardustStack.is(ArcanaRegistry.STARDUST)){
         this.stardust += stardustStack.getCount();
      }
      ItemStack waystone = container.getItem(0);
      if(this.level.getBlockState(getBlockPos()).is(((ArcanaBlock)ArcanaRegistry.ASTRAL_GATEWAY).getBlock()))
         this.level.setBlock(getBlockPos(),getBlockState().setValue(AstralGateway.AstralGatewayBlock.HAS_EYE,!waystone.isEmpty()),Block.UPDATE_ALL);
      if(!stardustStack.isEmpty()) this.inventory.setItem(1,ItemStack.EMPTY);
      
      // Update hologram item if it exists
      if(this.hologram != null){
         for(var element : this.hologram.getElements()){
            if(element instanceof ItemDisplayElement itemDisplay){
               if(validWaystone(waystone)){
                  itemDisplay.setItem(waystone);
               }
               break;
            }
         }
      }
      
      evaluateForOpenOrClose();
      updating = false;
   }
   
   public void frameBreak(){
      if(this.syncedGateway != null){
         if(this.syncedGateway.frame != null){
            for(BlockPos blockPos : this.syncedGateway.frame.getEnclosed()){
               BlockState bs = this.syncedGateway.level.getBlockState(blockPos);
               if(bs.is(ArcanaRegistry.ASTRAL_GATEWAY_PORTAL_BLOCK)){
                  this.syncedGateway.level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
               }
            }
         }
         this.syncedGateway.setState(GatewayState.CLOSED);
         this.syncedGateway.dialler = false;
         this.syncedGateway.syncedGateway = null;
      }
      this.syncedGateway = null;
      SoundUtils.playSound(this.level,getBlockPos(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.BLOCKS,2,2f);
      BorisLib.addTickTimerCallback((ServerLevel) this.level,new GenericTimer(20,() ->{
         SoundUtils.playSound(this.level,getBlockPos(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS,2,0.5f);
      }));
      if(this.frame != null){
         for(BlockPos blockPos : this.frame.getEnclosed()){
            BlockState bs = this.level.getBlockState(blockPos);
            if(bs.is(ArcanaRegistry.ASTRAL_GATEWAY_PORTAL_BLOCK)){
               this.level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }
         }
      }
      this.setState(GatewayState.CLOSED);
      this.dialler = false;
      this.frame = null;
   }
   
   boolean validWaystone(ItemStack stack){
      if(!stack.is(ArcanaRegistry.WAYSTONE.getItem())) return false;
      Waystone.WaystoneTarget target = Waystone.getTarget(stack);
      if(target == null){
         return false;
      }
      if(!astralStargate && !target.world().identifier().equals(this.level.dimension().identifier())){
         return false;
      }
      boolean isForGateway = Waystone.isForGateway(stack);
      return isForGateway && !BlockPos.containing(target.position()).equals(getBlockPos());
   }
   
   public GatewayFrame getFrame(){
      return frame;
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
      return ArcanaRegistry.ASTRAL_GATEWAY;
   }
   
   public long getStardust(){
      return stardust;
   }
   
   public int getStardustPerMinute(){
      return this.stardustPerMinute;
   }
   
   public int getOpeningStardust(){
      return openingStardust;
   }
   
   public boolean isAstralStargate(){
      return astralStargate;
   }
   
   @Override
   public int[] getSlotsForFace(Direction direction){
      if(direction == Direction.UP || direction == Direction.DOWN){
         return new int[]{0};
      }else{
         return new int[]{1};
      }
   }
   
   @Override
   public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction){
      if(direction == null){
         if(i == 0 && this.inventory.getItem(0).isEmpty()) return validWaystone(itemStack);
         if(i == 1) return itemStack.is(ArcanaRegistry.STARDUST);
      }
      if(i == 0 && direction == Direction.UP) return validWaystone(itemStack);
      if(i == 1 && direction != Direction.UP && direction != Direction.DOWN) return itemStack.is(ArcanaRegistry.STARDUST);
      return false;
   }
   
   @Override
   public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction){
      return i == 0 && direction == Direction.DOWN && itemStack.is(ArcanaRegistry.WAYSTONE.getItem());
   }
   
   @Override
   public int getContainerSize(){
      return 23;
   }
   
   public Container getInventory(){
      return this.inventory;
   }
   
   @Override
   public void setItem(int i, ItemStack itemStack){
      this.getInventory().setItem(i,itemStack);
   }
   
   @Override
   public ItemStack getItem(int i){
      return this.getInventory().getItem(i);
   }
   
   @Override
   public ItemStack removeItem(int i, int j){
      return this.getInventory().removeItem(i, j);
   }
   
   @Override
   public ItemStack removeItemNoUpdate(int i){
      return this.getInventory().removeItemNoUpdate(i);
   }
   
   @Override
   protected Component getDefaultName(){
      return Component.literal("Astral Gateway");
   }
   
   @Override
   protected NonNullList<ItemStack> getItems(){
      return this.inventory.getItems();
   }
   
   @Override
   protected void setItems(NonNullList<ItemStack> list){
      for(int i = 0; i < list.size(); i++){
         this.inventory.setItem(i,list.get(i));
      }
   }
   
   @Override
   protected AbstractContainerMenu createMenu(int i, Inventory inventory){
      return null;
   }
   
   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState oldState){
      if(!(this.level instanceof ServerLevel serverWorld)) return;
      if(!this.inventory.getItem(0).isEmpty()){
         Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), this.inventory.getItem(0).copyAndClear());
      }
      if(oldState.getValue(AstralGateway.AstralGatewayBlock.STATE) != GatewayState.CLOSED || this.syncedGateway != null){
         frameBreak();
      }
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.stateTime = view.getIntOr(STATE_TIME_TAG, 0);
      this.stardust = view.getLongOr(STARDUST_TAG, 0L);
      this.warmupDuration = view.getIntOr(WARMUP_DURATION_TAG, 0);
      this.cooldownDuration = view.getIntOr(COOLDOWN_DURATION_TAG, 0);
      this.findCooldown = view.getIntOr(FIND_COOLDOWN_TAG, 0);
      this.dialler = view.getBooleanOr(DIALLER_TAG, false);
      this.tryFind = view.getBooleanOr("tryFind",false);
      this.augments = new TreeMap<>();
      inventory = new SimpleContainer(getContainerSize());
      inventory.addListener(this);
      view.read(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      if (!this.tryLoadLootTable(view)) {
         ContainerHelper.loadAllItems(view, this.inventory.getItems());
      }
      view.read(FRAME_TAG, CompoundTag.CODEC).ifPresent((data) -> {
         this.frame = GatewayFrame.fromTag(data);
      });
      view.read(SYNCED_TAG, CompoundTag.CODEC).ifPresent((data) -> {
         MinecraftServer server = BorisLib.SERVER;
         ServerLevel otherLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(data.getStringOr("dim",""))));
         if(otherLevel == null) return;
         int[] posArray = data.getIntArray("pos").orElse(new int[0]);
         if(posArray.length == 3){
            BlockPos pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
            BlockEntity be = otherLevel.getBlockEntity(pos);
            if(be instanceof AstralGatewayBlockEntity gateway){
               this.syncedGateway = gateway;
            }
         }
      });
      
      forceRectangular = ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.ASTRAL_STABILIZERS) <= 0;
      forceConvex = ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.ASTRAL_STABILIZERS) <= 1;
      astralStargate = ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.ASTRAL_STARGATE) > 0;
      recyclerLvl = ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.STARLIGHT_RECYCLERS);
      
      int stardustPerMinute = (int) Math.round(STARDUST_PER_MINUTE * RECYCLER_RATE[recyclerLvl]);
      this.stardustPerMinute = stardustPerMinute;
      this.stardustAccumulator = view.getDoubleOr("stardustAccumulator", 0.0);
      openingStardust = (int) Math.ceil(stardustPerMinute * (LOCK_DURATION / 1200.0)) + stardustPerMinute;
      inventory.addListener(this);
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG,this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG,this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME,this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG,this.origin);
      view.putInt(STATE_TIME_TAG,this.stateTime);
      view.putLong(STARDUST_TAG,this.stardust);
      view.putDouble("stardustAccumulator", this.stardustAccumulator);
      view.putBoolean(DIALLER_TAG, this.dialler);
      view.putInt(WARMUP_DURATION_TAG, this.warmupDuration);
      view.putInt(COOLDOWN_DURATION_TAG, this.cooldownDuration);
      view.putInt(FIND_COOLDOWN_TAG, this.findCooldown);
      view.putBoolean("tryFind",tryFind || this.frame != null);
      if(this.frame != null) view.storeNullable(FRAME_TAG, CompoundTag.CODEC, GatewayFrame.toTag(this.frame));
      if(this.syncedGateway != null){
         CompoundTag synced = new CompoundTag();
         synced.putIntArray("pos", new int[]{syncedGateway.getBlockPos().getX(), syncedGateway.getBlockPos().getY(), syncedGateway.getBlockPos().getZ()});
         synced.putString("dim",syncedGateway.level.dimension().identifier().toString());
         view.storeNullable(SYNCED_TAG,CompoundTag.CODEC,synced);
      }
      if (!this.trySaveLootTable(view)) {
         ContainerHelper.saveAllItems(view, this.inventory.getItems());
      }
   }
   
   public void readStardustAndStones(int stardust, ListTag stoneList, HolderLookup.Provider registryLookup){
      inventory = new SimpleContainer(getContainerSize());
      inventory.addListener(this);
      this.stardust = stardust;
      for(Tag tag : stoneList){
         if(!(tag instanceof CompoundTag comp)) continue;
         byte slot = comp.getByteOr("slot",(byte) 0);
         ItemStack stack = ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE,registryLookup),comp).result().orElse(ItemStack.EMPTY);
         if(Waystone.getTarget(stack) != null){
            this.inventory.setItem(slot,stack);
         }
      }
      setChanged();
   }
   
   public ListTag saveStones(HolderLookup.Provider registryLookup){
      if(this.inventory != null){
         ListTag stoneList = new ListTag();
         for(int i = 2; i < inventory.getContainerSize(); i++){
            ItemStack stone = inventory.getItem(i);
            if(stone.isEmpty()) continue;
            CompoundTag tag = (CompoundTag) ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,registryLookup),stone).getOrThrow();
            tag.putByte("slot",(byte)i);
            stoneList.add(tag);
         }
         return stoneList;
      }else{
         return new ListTag();
      }
   }
}
