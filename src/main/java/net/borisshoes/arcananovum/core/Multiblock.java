package net.borisshoes.arcananovum.core;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.StairShape;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class Multiblock {
   private final int[][][] statePattern;
   private final List<Pair<BlockState,Predicate<BlockState>>> predicates;
   
   private static final HashMap<ServerPlayerEntity,List<HolderAttachment>> MULTIBLOCK_DISPLAYS = new HashMap<>();
   
   private Multiblock(int[][][] statePattern, List<Pair<BlockState,Predicate<BlockState>>> predicates){
      this.statePattern = statePattern;
      this.predicates = predicates;
   }
   
   public HashMap<Item,Integer> getMaterialList(){
      HashMap<Item,Integer> mats = new HashMap<>();
      
      int width = statePattern.length;
      int height = statePattern[0].length;
      int length = statePattern[0][0].length;
      
      for(int x=0;x<width;x++){
         for(int y = 0; y < height; y++){
            for(int z = 0; z < length; z++){
               int pattern = statePattern[x][y][z];
               if(pattern == -1) continue;
               Pair<BlockState,Predicate<BlockState>> pair = predicates.get(pattern);
               Item item = pair.getLeft().getBlock().asItem();
               if(item == Items.AIR) continue;
               if(mats.containsKey(item)){
                  mats.put(item,mats.get(item)+1);
               }else{
                  mats.put(item,1);
               }
            }
         }
      }
      
      return mats;
   }
   
   public void displayStructure(MultiblockCheck checkParams, ServerPlayerEntity player){
      if(checkParams == null) return;
      List<MultiblockCheckResult> incorrect = getIncorrect(checkParams);
      
      List<HolderAttachment> playerAttachments;
      if(MULTIBLOCK_DISPLAYS.containsKey(player)){
         playerAttachments = MULTIBLOCK_DISPLAYS.get(player);
         for(HolderAttachment attachment : playerAttachments){
            attachment.holder().destroy();
         }
         playerAttachments.clear();
      }else{
         playerAttachments = new ArrayList<>();
      }
      
      for(MultiblockCheckResult result : incorrect){
         BlockPos blockPos = result.pos();
         BlockState blockState = result.displayState();
         
         BlockDisplayElement element = createEmptyElement();
         ElementHolder holder = createHolder(blockPos,checkParams.coreState(),checkParams.corePos(),result.predicate());
         HolderAttachment attachment = ChunkAttachment.ofTicking(holder, checkParams.world(), blockPos);
         
         if(blockState.isAir()){
            element.setBlockState(Blocks.GLASS.getDefaultState());
         }else{
            element.setBlockState(blockState);
         }
         
         if(result.foundState().isAir()){
            element.setGlowColorOverride(0xFF55FF);
         }else{
            element.setGlowColorOverride(0xFF0000);
         }
         element.setGlowing(true);
         holder.addElement(element);
         
         playerAttachments.add(attachment);
         
         for(ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()){
            if(serverPlayer != player){
               holder.stopWatching(serverPlayer);
               attachment.stopWatching(serverPlayer);
            }
         }
      }
      MULTIBLOCK_DISPLAYS.put(player,playerAttachments);
   }
   
   public boolean matches(MultiblockCheck checkParams){
      if(checkParams == null) return false;
      return getIncorrect(checkParams).isEmpty();
   }
   
   public List<MultiblockCheckResult> getIncorrect(MultiblockCheck checkParams){
      if(checkParams == null) return new ArrayList<>();
      List<MultiblockCheckResult> incorrect = new ArrayList<>();
      int numRotations = calculateRotations(checkParams);
      int[][][] rotatedPattern = calculateRotated(numRotations);
      int width = rotatedPattern.length;
      int height = rotatedPattern[0].length;
      int length = rotatedPattern[0][0].length;
      BlockPos cornerOffset = checkParams.cornerOffset();
      BlockPos rotatedOffset = calculateRotOffset(numRotations,cornerOffset);
      
      BlockPos corner = checkParams.corePos().add(rotatedOffset);
      for(int x=0;x<width;x++){
         for(int y=0;y<height;y++){
            for(int z=0;z<length;z++){
               int pattern = rotatedPattern[x][y][z];
               if(pattern == -1) continue;
               
               BlockPos pos = corner.add(x,y,z);
               BlockState state = checkParams.world.getBlockState(pos);
               Pair<BlockState,Predicate<BlockState>> pair = predicates.get(pattern);
               BlockState rotatedRawState = pair.getLeft();
               for(int i = 0; i < numRotations; i++){
                  rotatedRawState = rotatedRawState.rotate(BlockRotation.COUNTERCLOCKWISE_90);
               }
               
               Predicate<BlockState> predicate = pair.getRight();
               Predicate<BlockState> rotatedPred = bs -> {
                  for(int i = 0; i < numRotations; i++){
                     bs = bs.rotate(BlockRotation.CLOCKWISE_90);
                  }
                  return predicate.test(bs);
               };
               
               if(!rotatedPred.test(state)){
                  incorrect.add(new MultiblockCheckResult(checkParams.world(),rotatedRawState,state,rotatedPred,new BlockPos(pos)));
               }
            }
         }
      }
      
      return incorrect;
   }
   
   private int calculateRotations(MultiblockCheck checkParams){
      Direction direction = checkParams.direction();
      if(direction == null) return 0;
      BlockPos offset = checkParams.cornerOffset();
      BlockState storedCore = predicates.get(statePattern[-offset.getX()][-offset.getY()][-offset.getZ()]).getLeft();
      Direction storedDir = storedCore.get(Properties.HORIZONTAL_FACING);
      int numRotations = 0;
      Direction testDir = storedDir;
      while(direction.getHorizontalQuarterTurns() != testDir.getHorizontalQuarterTurns()){
         testDir = testDir.rotateYCounterclockwise();
         numRotations++;
      }
      return numRotations;
   }
   
   private int[][][] calculateRotated(int numRotations){
      if(numRotations == 0) return statePattern;
      
      // Rotate Clockwise
      int[][][] rotatedPattern = statePattern;
      for(int i = 0; i < numRotations; i++){
         final int M = rotatedPattern.length;
         final int Y = rotatedPattern[0].length;
         final int N = rotatedPattern[0][0].length;
         int[][][] ret = new int[N][Y][M];
         
         for(int y = 0; y < Y; y++){
            for (int r = 0; r < M; r++){
               for (int c = 0; c < N; c++){
                  ret[c][y][M-1-r] = rotatedPattern[r][y][c];
               }
            }
         }
         rotatedPattern = ret;
      }
      return rotatedPattern;
   }
   
   private BlockPos calculateRotOffset(int numRotations, BlockPos pos){
      if(numRotations == 0) return pos;
      BlockPos newPos = pos.multiply(-1);
      int M = statePattern.length;
      int N = statePattern[0][0].length;
      for(int i = 0; i < numRotations; i++){
         newPos = new BlockPos(newPos.getZ(),newPos.getY(),M-1-newPos.getX());
         int T = M; M = N; N = T;
      }
      return newPos.multiply(-1);
   }
   
   public static Multiblock loadFromFile(String id){
      return loadFromFile(MOD_ID,id);
   }
   
   public static Multiblock loadFromFile(String namespace, String id){
      try{
         Optional<Optional<Path>> pathOptional = FabricLoader.getInstance().getModContainer(namespace).map(container -> container.findPath("data/"+namespace+"/multiblocks/"+id+".nbt"));
         if(pathOptional.isEmpty() || pathOptional.get().isEmpty()){
            return null;
         }
         Path path = pathOptional.get().get();
         InputStream in = Files.newInputStream(path);
         NbtCompound compound = NbtIo.readCompressed(in,NbtSizeTracker.ofUnlimitedBytes());
         if(compound == null) return null;
         
         NbtList size = compound.getList("size", NbtElement.INT_TYPE);
         int sizeX = size.getInt(0);
         int sizeY = size.getInt(1);
         int sizeZ = size.getInt(2);
         
         int[][][] pattern = new int[sizeX][sizeY][sizeZ];
         for(int i=0;i<sizeX;i++){for(int j=0;j<sizeY;j++){for(int k=0;k<sizeZ;k++){pattern[i][j][k] = -1;}}} // Set all elements to -1 because 0 is used by the palette
         
         NbtList blocks = compound.getList("blocks",NbtElement.COMPOUND_TYPE);
         for(NbtElement b : blocks){
            NbtCompound block = (NbtCompound) b;
            NbtList pos = block.getList("pos", NbtElement.INT_TYPE);
            pattern[pos.getInt(0)][pos.getInt(1)][pos.getInt(2)] = block.getInt("state");
         }
         
         // Build predicates for checking block states
         NbtList palette = compound.getList("palette",NbtElement.COMPOUND_TYPE);
         List<Pair<BlockState,Predicate<BlockState>>> preds = new ArrayList<>();
         for(NbtElement e : palette){
            // Get the actual block
            NbtCompound blockTag = (NbtCompound) e;
            String blockName = blockTag.getString("Name");
            BlockState rawState = NbtHelper.toBlockState(Registries.BLOCK,blockTag); // Save raw state for display
            Predicate<BlockState> pred;
            Identifier identifier = Identifier.of(blockName);
            Optional<RegistryEntry.Reference<Block>> optional = Registries.BLOCK.getOptional(RegistryKey.of(RegistryKeys.BLOCK, identifier));
            if(optional.isEmpty()){ // If block isn't found, let any block work
               pred = blockState -> true;
               preds.add(new Pair<>(rawState,pred));
               continue;
            }
            
            // Block found, build predicate
            Block block = (Block)((RegistryEntry<?>)optional.get()).value();
            HashMap<Property<? extends Comparable<?>>,Comparable<?>> blockProperties = new HashMap<>();
            if(blockTag.contains("Properties", NbtElement.COMPOUND_TYPE)){
               NbtCompound properties = blockTag.getCompound("Properties");
               StateManager<Block, BlockState> stateManager = block.getStateManager();
               for (String key : properties.getKeys()){
                  Property<?> p = stateManager.getProperty(key);
                  if(p == null) continue;
                  blockProperties.put(p,rawState.get(p)); // Add all properties to the map with all values that need to be checked
               }
            }
            
            pred = state -> {
               if(!state.isOf(rawState.getBlock())) return false; // Check block type
               boolean stairExempt = false;
               if(rawState.getBlock() instanceof StairsBlock && blockProperties.containsKey(StairsBlock.FACING) && blockProperties.containsKey(StairsBlock.SHAPE)){
                  Direction desiredDirection = rawState.get(StairsBlock.FACING);
                  StairShape desiredShape = rawState.get(StairsBlock.SHAPE);
                  Direction stateDirection = state.get(StairsBlock.FACING);
                  StairShape stateShape = state.get(StairsBlock.SHAPE);
                  
                  if((desiredShape.equals(StairShape.INNER_LEFT) && stateShape.equals(StairShape.INNER_RIGHT)) || (desiredShape.equals(StairShape.OUTER_LEFT) && stateShape.equals(StairShape.OUTER_RIGHT))){
                     stairExempt = switch(desiredDirection){
                        case NORTH -> stateDirection.equals(Direction.WEST);
                        case SOUTH -> stateDirection.equals(Direction.EAST);
                        case EAST -> stateDirection.equals(Direction.NORTH);
                        case WEST -> stateDirection.equals(Direction.SOUTH);
                        default -> false;
                     };
                  }else if((desiredShape.equals(StairShape.INNER_RIGHT) && stateShape.equals(StairShape.INNER_LEFT)) || (desiredShape.equals(StairShape.OUTER_RIGHT) && stateShape.equals(StairShape.OUTER_LEFT))){
                     stairExempt = switch(desiredDirection){
                        case WEST -> stateDirection.equals(Direction.NORTH);
                        case EAST -> stateDirection.equals(Direction.SOUTH);
                        case NORTH -> stateDirection.equals(Direction.EAST);
                        case SOUTH -> stateDirection.equals(Direction.WEST);
                        default -> false;
                     };
                  }
               }
               
               for(Map.Entry<Property<? extends Comparable<?>>, Comparable<?>> entry : blockProperties.entrySet()){
                  //System.out.println("Testing "+entry.getKey()+": Found "+state.get(entry.getKey())+" expecting: "+entry.getValue()+" | Matches:"+state.get(entry.getKey()).equals(entry.getValue()));
                  if((entry.getKey().equals(StairsBlock.FACING) || entry.getKey().equals(StairsBlock.SHAPE)) && stairExempt) continue;
                  if(!state.get(entry.getKey()).equals(entry.getValue())) return false; // Check all mapped properties
               }
               return true;
            };
            preds.add(new Pair<>(rawState,pred)); // Add predicate
         }
         
         return new Multiblock(pattern,preds);
      }catch(Exception e){
         e.printStackTrace();
      }
      return null;
   }
   
   public ElementHolder createHolder(BlockPos pos, BlockState cs, BlockPos cp, Predicate<BlockState> p){
      return new ElementHolder(){
         int lifeTime = 600;
         final BlockState coreState = cs;
         final BlockPos corePos = cp;
         final Predicate<BlockState> pred = p;
         
         @Override
         public Vec3d getPos(){
            return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
         }
         
         @Override
         protected void onTick(){
            super.onTick();
            
            if(lifeTime-- <= 0){
               destroy(); // Time expired, remove
               return;
            }
            
            if(getAttachment() != null && getAttachment().getWorld() != null){
               ServerWorld world = getAttachment().getWorld();
               if(!world.getBlockState(corePos).isOf(coreState.getBlock())){
                  destroy(); // Core block destroyed, remove
                  return;
               }
               if(pred.test(world.getBlockState(pos))){
                  destroy(); // Block now satisfies predicate, remove
                  return;
               }
               for(VirtualElement element : getElements()){
                  if(element instanceof BlockDisplayElement elem){
                     if(world.getBlockState(pos).isAir()){
                        elem.setGlowColorOverride(0xFF55FF);
                     }else{
                        elem.setGlowColorOverride(0xFF0000);
                     }
                  }
               }
            }
         }
      };
   }
   
   public BlockDisplayElement createEmptyElement(){
      BlockDisplayElement element = new BlockDisplayElement();
      element.setScale(new Vector3f(0.5F, 0.5F, 0.5F));
      element.setOffset(new Vec3d(0.25F, 0.25F, 0.25F));
      return element;
   }
   
   public record MultiblockCheck(ServerWorld world, BlockPos corePos, BlockState coreState, BlockPos cornerOffset, @Nullable Direction direction){
      public MultiblockCheck {
         Objects.requireNonNull(world);
         Objects.requireNonNull(corePos);
         Objects.requireNonNull(coreState);
         Objects.requireNonNull(cornerOffset);
      }
   }
   
   public record MultiblockCheckResult(ServerWorld world, BlockState displayState, BlockState foundState, Predicate<BlockState> predicate, BlockPos pos){
      public MultiblockCheckResult {
         Objects.requireNonNull(world);
         Objects.requireNonNull(displayState);
         Objects.requireNonNull(foundState);
         Objects.requireNonNull(predicate);
         Objects.requireNonNull(pos);
      }
   }
}
