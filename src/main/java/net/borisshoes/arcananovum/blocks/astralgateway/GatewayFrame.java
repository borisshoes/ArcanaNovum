package net.borisshoes.arcananovum.blocks.astralgateway;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class GatewayFrame {
   private final List<BlockPos> blocks = new ArrayList<>();
   private final Set<BlockPos> blockSet = new HashSet<>();
   private final BlockPos core;
   private final Block type;
   private final Direction.Axis axis;
   private final List<Vec3i> moveVecs = new ArrayList<>();
   private final List<Vec3i> fillVecs = new ArrayList<>();
   private final Set<BlockPos> invalid = new HashSet<>();
   private final Set<BlockPos> hasAir = new HashSet<>();
   private final Set<BlockPos> endingMoves = new HashSet<>();
   private final Set<BlockPos> enclosed = new HashSet<>();
   private final boolean forceRectangular, forceConvex;
   private final int maxSize;
   private final Deque<Set<BlockPos>> invalidHistory = new ArrayDeque<>();
   private final Map<BlockPos, BlockState> blockStateCache = new HashMap<>();
   private final Map<BlockPos, Boolean> fullBlockCache = new HashMap<>();
   private int size = 1;
   private boolean finished = false;
   private boolean valid = false;
   
   public GatewayFrame(BlockPos core, Block type, Direction.Axis axis, int maxSize, boolean forceRectangular, boolean forceConvex){
      this.core = core;
      this.type = type;
      this.axis = axis;
      this.maxSize = maxSize;
      this.forceRectangular = forceRectangular;
      this.forceConvex = forceConvex;
      this.blocks.add(core);
      this.blockSet.add(core);
      this.invalid.add(core);
      if(axis == Direction.Axis.X){
         moveVecs.add(new Vec3i(0, 0, 1));
         moveVecs.add(new Vec3i(0, 0, -1));
         moveVecs.add(new Vec3i(0, 1, 1));
         moveVecs.add(new Vec3i(0, 1, -1));
         moveVecs.add(new Vec3i(0, -1, 1));
         moveVecs.add(new Vec3i(0, -1, -1));
         moveVecs.add(new Vec3i(0, 1, 0));
         moveVecs.add(new Vec3i(0, -1, 0));
         fillVecs.add(new Vec3i(0, 1, 0));
         fillVecs.add(new Vec3i(0, -1, 0));
         fillVecs.add(new Vec3i(0, 0, 1));
         fillVecs.add(new Vec3i(0, 0, -1));
      }else if(axis == Direction.Axis.Y){
         moveVecs.add(new Vec3i(0, 0, 1));
         moveVecs.add(new Vec3i(0, 0, -1));
         moveVecs.add(new Vec3i(1, 0, 1));
         moveVecs.add(new Vec3i(1, 0, -1));
         moveVecs.add(new Vec3i(-1, 0, 1));
         moveVecs.add(new Vec3i(-1, 0, -1));
         moveVecs.add(new Vec3i(1, 0, 0));
         moveVecs.add(new Vec3i(-1, 0, 0));
         fillVecs.add(new Vec3i(1, 0, 0));
         fillVecs.add(new Vec3i(-1, 0, 0));
         fillVecs.add(new Vec3i(0, 0, 1));
         fillVecs.add(new Vec3i(0, 0, -1));
      }else{
         moveVecs.add(new Vec3i(0, 1, 0));
         moveVecs.add(new Vec3i(0, -1, 0));
         moveVecs.add(new Vec3i(1, 1, 0));
         moveVecs.add(new Vec3i(1, -1, 0));
         moveVecs.add(new Vec3i(-1, 1, 0));
         moveVecs.add(new Vec3i(-1, -1, 0));
         moveVecs.add(new Vec3i(1, 0, 0));
         moveVecs.add(new Vec3i(-1, 0, 0));
         fillVecs.add(new Vec3i(1, 0, 0));
         fillVecs.add(new Vec3i(-1, 0, 0));
         fillVecs.add(new Vec3i(0, 1, 0));
         fillVecs.add(new Vec3i(0, -1, 0));
      }
      for(Vec3i moveVec : moveVecs){
         endingMoves.add(core.offset(moveVec));
      }
   }
   
   private GatewayFrame(BlockPos core, Block type, Direction.Axis axis, int maxSize, boolean forceRectangular, boolean forceConvex,
                        List<BlockPos> blocks, Set<BlockPos> enclosed, int size, boolean finished, boolean valid){
      this(core, type, axis, maxSize, forceRectangular, forceConvex);
      this.blocks.clear();
      this.blocks.addAll(blocks);
      this.blockSet.clear();
      this.blockSet.addAll(blocks);
      this.enclosed.clear();
      this.enclosed.addAll(enclosed);
      this.size = size;
      this.finished = finished;
      this.valid = valid;
   }
   
   private BlockState getCachedBlockState(ServerLevel level, BlockPos pos){
      return blockStateCache.computeIfAbsent(pos, level::getBlockState);
   }
   
   private boolean isFullBlock(ServerLevel level, BlockPos pos){
      return fullBlockCache.computeIfAbsent(pos, p -> getCachedBlockState(level, p).isCollisionShapeFullBlock(level, p));
   }
   
   // Assume the move is legal - tracks changes for undoMove()
   private void makeMove(BlockPos move, ServerLevel level){
      Set<BlockPos> addedInvalid = new HashSet<>();
      
      for(Vec3i moveVec : moveVecs){
         BlockPos newMove = move.offset(moveVec);
         if(invalid.contains(newMove)) continue;
         for(Vec3i vec : moveVecs){
            BlockPos check = newMove.offset(vec);
            if(check.equals(core)) continue;
            if(blockSet.contains(check)){
               int air = 0;
               for(Vec3i fillVec : fillVecs){
                  if(air > 2) break;
                  if(getCachedBlockState(level, check.offset(fillVec)).isAir()){
                     air++;
                  }
               }
               if(air > 0){
                  hasAir.add(check);
               }
               if(air != 2){
                  if(!invalid.contains(newMove)){
                     invalid.add(newMove);
                     addedInvalid.add(newMove);
                  }
                  break;
               }
            }
            if(!hasAir.contains(newMove) && getCachedBlockState(level, check).isAir()){
               hasAir.add(newMove);
            }
         }
         if(!hasAir.contains(newMove) && !invalid.contains(newMove)){
            invalid.add(newMove);
            addedInvalid.add(newMove);
         }
      }
      blocks.add(move);
      blockSet.add(move);
      invalid.add(move);
      addedInvalid.add(move);
      
      invalidHistory.push(addedInvalid);
      
      size++;
      if(size >= 4 && endingMoves.contains(move)){
         this.finished = true;
      }
   }
   
   /**
    * Undoes the last move made, restoring the frame to its previous state.
    */
   private void undoMove(){
      if(blocks.size() <= 1) return; // Can't undo the core block
      
      BlockPos removed = blocks.removeLast();
      blockSet.remove(removed);
      size--;
      finished = false;
      valid = false;
      
      // Restore invalid set
      if(!invalidHistory.isEmpty()){
         Set<BlockPos> addedInvalid = invalidHistory.pop();
         invalid.removeAll(addedInvalid);
      }
   }
   
   private List<BlockPos> validMoves(ServerLevel level){
      List<BlockPos> moves = new ArrayList<>();
      if(size > maxSize){
         return moves;
      }
      BlockPos head = blocks.getLast();
      for(Vec3i moveVec : moveVecs){
         BlockPos move = head.offset(moveVec);
         if(invalid.contains(move)) continue;
         BlockState state = getCachedBlockState(level, move);
         if(state.getBlock() != this.type || !isFullBlock(level, move)){
            invalid.add(move);
            continue;
         }
         // Early air adjacency check - a valid frame block must have at least one adjacent air block
         if(!hasAir.contains(move)){
            boolean foundAir = false;
            for(Vec3i fillVec : fillVecs){
               if(getCachedBlockState(level, move.offset(fillVec)).isAir()){
                  foundAir = true;
                  hasAir.add(move);
                  break;
               }
            }
            if(!foundAir){
               invalid.add(move);
               continue;
            }
         }
         moves.add(move);
      }
      // Prioritize moves that are closer to ending moves (completing the loop)
      // Precompute distances to avoid repeated calculations during sort
      if(size >= 3 && moves.size() > 1){
         Map<BlockPos, Integer> distances = new HashMap<>(moves.size());
         for(BlockPos move : moves){
            distances.put(move, minDistanceToEnding(move));
         }
         moves.sort(Comparator.comparingInt(distances::get));
      }
      return moves;
   }
   
   public void validateEnding(ServerLevel level){
      if(forceRectangular && !isRectangular()){
         this.valid = false;
         return;
      }
      
      calculateEnclosedBlocks(level);
      if(this.enclosed.isEmpty()){
         this.valid = false;
         return;
      }
      
      if(forceConvex && !isConvex()){
         this.valid = false;
         return;
      }
      
      for(BlockPos block : getBlocks()){
         if(!level.getBlockState(block).is(type)){
            this.valid = false;
            return;
         }
      }
      for(BlockPos blockPos : this.enclosed){
         BlockState state = level.getBlockState(blockPos);
         if(state.isAir() || state.is(ArcanaRegistry.ASTRAL_GATEWAY_PORTAL_BLOCK)) continue;
         this.valid = false;
         return;
      }
      
      this.valid = true;
   }
   
   private boolean isRectangular(){
      if(blocks.size() < 4) return false;
      
      // Get the two coordinates that matter based on axis (perpendicular plane)
      int minA = Integer.MAX_VALUE, maxA = Integer.MIN_VALUE;
      int minB = Integer.MAX_VALUE, maxB = Integer.MIN_VALUE;
      
      for(BlockPos block : blocks){
         int a, b;
         if(axis == Direction.Axis.X){
            a = block.getY();
            b = block.getZ();
         }else if(axis == Direction.Axis.Y){
            a = block.getX();
            b = block.getZ();
         }else{
            a = block.getX();
            b = block.getY();
         }
         minA = Math.min(minA, a);
         maxA = Math.max(maxA, a);
         minB = Math.min(minB, b);
         maxB = Math.max(maxB, b);
      }
      
      // All blocks must lie on the edges of the bounding rectangle
      for(BlockPos block : blocks){
         int a, b;
         if(axis == Direction.Axis.X){
            a = block.getY();
            b = block.getZ();
         }else if(axis == Direction.Axis.Y){
            a = block.getX();
            b = block.getZ();
         }else{
            a = block.getX();
            b = block.getY();
         }
         
         // Block must be on one of the four edges
         boolean onEdge = (a == minA || a == maxA || b == minB || b == maxB);
         if(!onEdge){
            return false;
         }
      }
      
      return true;
   }
   
   private void calculateEnclosedBlocks(ServerLevel level){
      if(blocks.size() < 4) return;
      
      Set<BlockPos> blockSet = new HashSet<>(blocks);
      int maxBlocks = (maxSize * maxSize) / 4 + 1;
      
      for(Vec3i fillVec : fillVecs){
         BlockPos candidate = core.offset(fillVec);
         if(!blockSet.contains(candidate)){
            if(boundedFloodFill(level, candidate, blockSet, maxBlocks)){
               return;
            }
         }
      }
   }
   
   private boolean boundedFloodFill(ServerLevel level, BlockPos start, Set<BlockPos> boundary, int maxBlocks){
      // Check if start position is valid (air or portal)
      BlockState startState = level.getBlockState(start);
      if(!startState.isAir() && !startState.is(ArcanaRegistry.ASTRAL_GATEWAY_PORTAL_BLOCK)) return false;
      
      Set<BlockPos> filled = new HashSet<>();
      Set<BlockPos> visited = new HashSet<>();
      Queue<BlockPos> queue = new LinkedList<>();
      queue.add(start);
      visited.add(start);
      
      while(!queue.isEmpty()){
         BlockPos current = queue.poll();
         filled.add(current);
         
         if(filled.size() > maxBlocks){
            return false; // Escaped - not a bounded interior
         }
         
         for(Vec3i moveVec : fillVecs){
            BlockPos neighbor = current.offset(moveVec);
            if(visited.contains(neighbor)) continue;
            visited.add(neighbor);
            
            if(boundary.contains(neighbor)) continue; // Hit frame boundary
            BlockState neighborState = level.getBlockState(neighbor);
            if(!neighborState.isAir() && !neighborState.is(ArcanaRegistry.ASTRAL_GATEWAY_PORTAL_BLOCK))
               return false; // Enclosure not empty
            queue.add(neighbor);
         }
      }
      
      this.enclosed.clear();
      this.enclosed.addAll(filled);
      return true;
   }
   
   public boolean isFinished(){
      return finished;
   }
   
   private int getRemainingBudget(){
      return maxSize - size + 1;
   }
   
   /**
    * Returns the minimum distance from the given position to any valid ending position.
    */
   private int getMinDistanceToEnding(){
      if(blocks.isEmpty()) return Integer.MAX_VALUE;
      return minDistanceToEnding(blocks.getLast());
   }
   
   /**
    * Checks if any ending position is still potentially reachable.
    * Returns false if all ending positions are invalid or have wrong block type.
    */
   private boolean hasReachableEnding(ServerLevel level){
      for(BlockPos end : endingMoves){
         if(invalid.contains(end)) continue;
         BlockState state = getCachedBlockState(level, end);
         if(state.getBlock() == this.type && isFullBlock(level, end)){
            return true;
         }
      }
      return false;
   }
   
   private int minDistanceToEnding(BlockPos pos){
      int minDist = Integer.MAX_VALUE;
      for(BlockPos endingMove : endingMoves){
         if(invalid.contains(endingMove)) continue;
         int dist = Math.abs(pos.getX() - endingMove.getX()) +
               Math.abs(pos.getY() - endingMove.getY()) +
               Math.abs(pos.getZ() - endingMove.getZ());
         minDist = Math.min(minDist, dist);
      }
      return minDist;
   }
   
   public boolean finishedAndValid(){
      return finished && valid;
   }
   
   public List<BlockPos> getBlocks(){
      return blocks;
   }
   
   public Set<BlockPos> getEnclosed(){
      return enclosed;
   }
   
   /**
    * Returns a map of enclosed BlockPos to their manhattan distance from the nearest frame block.
    * Uses BFS from all frame blocks simultaneously to compute distances.
    */
   public Map<BlockPos, Integer> getEnclosedDistances(){
      Map<BlockPos, Integer> distances = new HashMap<>();
      if(enclosed.isEmpty() || blocks.isEmpty()) return distances;
      
      Set<BlockPos> enclosedSet = new HashSet<>(enclosed);
      Queue<BlockPos> queue = new LinkedList<>();
      Set<BlockPos> visited = new HashSet<>();
      
      // Initialize with all frame blocks at distance 0
      for(BlockPos frameBlock : blocks){
         queue.add(frameBlock);
         visited.add(frameBlock);
      }
      
      int currentDistance = 0;
      int nodesAtCurrentDistance = queue.size();
      int nodesProcessed = 0;
      
      while(!queue.isEmpty()){
         BlockPos current = queue.poll();
         nodesProcessed++;
         
         // Check if this is an enclosed block
         if(enclosedSet.contains(current)){
            distances.put(current, currentDistance);
         }
         
         // Expand to neighbors using fillVecs (cardinal directions in the portal plane)
         for(Vec3i fillVec : fillVecs){
            BlockPos neighbor = current.offset(fillVec);
            if(visited.contains(neighbor)) continue;
            if(!enclosedSet.contains(neighbor) && !blocks.contains(neighbor)) continue;
            
            visited.add(neighbor);
            queue.add(neighbor);
         }
         
         // Move to next distance level
         if(nodesProcessed >= nodesAtCurrentDistance){
            currentDistance++;
            nodesAtCurrentDistance = queue.size();
            nodesProcessed = 0;
         }
      }
      
      return distances;
   }
   
   public BlockPos getCore(){
      return core;
   }
   
   public Block getType(){
      return type;
   }
   
   private void addInvalid(BlockPos pos){
      invalid.add(pos);
   }
   
   public GatewayFrame copy(){
      GatewayFrame newFrame = new GatewayFrame(core, type, axis, maxSize, forceRectangular, forceConvex);
      newFrame.blocks.clear();
      newFrame.blocks.addAll(this.blocks);
      newFrame.blockSet.clear();
      newFrame.blockSet.addAll(this.blockSet);
      newFrame.invalid.clear();
      newFrame.invalid.addAll(this.invalid);
      newFrame.hasAir.clear();
      newFrame.hasAir.addAll(this.hasAir);
      newFrame.enclosed.clear();
      newFrame.enclosed.addAll(this.enclosed);
      newFrame.blockStateCache.putAll(this.blockStateCache);
      newFrame.fullBlockCache.putAll(this.fullBlockCache);
      newFrame.size = size;
      newFrame.valid = valid;
      newFrame.finished = finished;
      return newFrame;
   }
   
   /**
    * Returns an ordered list of Vec3 points representing the boundary between the frame blocks and the enclosed blocks.
    * The points trace the inner edge of the frame in the same order as the blocks list (clockwise or counterclockwise).
    * Each point is at a corner where the frame meets the enclosed area.
    */
   public List<Vec3> getOrderedBoundaryPoints(){
      List<Vec3> boundaryPoints = new ArrayList<>();
      if(blocks.size() < 4 || enclosed.isEmpty()) return boundaryPoints;
      
      Set<BlockPos> enclosedSet = new HashSet<>(enclosed);
      Set<BlockPos> blockSet = new HashSet<>(blocks);
      
      for(int i = 0; i < blocks.size(); i++){
         BlockPos current = blocks.get(i);
         BlockPos prev = blocks.get((i - 1 + blocks.size()) % blocks.size());
         BlockPos next = blocks.get((i + 1) % blocks.size());
         
         Vec3i toPrev = new Vec3i(prev.getX() - current.getX(), prev.getY() - current.getY(), prev.getZ() - current.getZ());
         Vec3i toNext = new Vec3i(next.getX() - current.getX(), next.getY() - current.getY(), next.getZ() - current.getZ());
         
         toPrev = normalizeToUnit(toPrev);
         toNext = normalizeToUnit(toNext);
         
         List<Vec3> blockCorners = getBlockCornersTowardEnclosed(current, toPrev, toNext, enclosedSet, blockSet);
         boundaryPoints.addAll(blockCorners);
      }
      
      return boundaryPoints;
   }
   
   private Vec3i normalizeToUnit(Vec3i vec){
      int x = Integer.compare(vec.getX(), 0);
      int y = Integer.compare(vec.getY(), 0);
      int z = Integer.compare(vec.getZ(), 0);
      return new Vec3i(x, y, z);
   }
   
   private List<Vec3> getBlockCornersTowardEnclosed(BlockPos block, Vec3i toPrev, Vec3i toNext, Set<BlockPos> enclosedSet, Set<BlockPos> blockSet){
      List<Vec3> corners = new ArrayList<>();
      
      // This is perpendicular to the path direction and toward the enclosed blocks
      Vec3i inward = findInwardDirection(block, enclosedSet, blockSet);
      if(inward == null) return corners;
      
      double baseX = block.getX() + 0.5;
      double baseY = block.getY() + 0.5;
      double baseZ = block.getZ() + 0.5;
      
      Vec3 corner1 = new Vec3(
            baseX + (toPrev.getX() + inward.getX()) * 0.5,
            baseY + (toPrev.getY() + inward.getY()) * 0.5,
            baseZ + (toPrev.getZ() + inward.getZ()) * 0.5
      );
      
      // Check if we need to add an extra corner (for L-shaped turns)
      boolean isStraight = (toPrev.getX() + toNext.getX() == 0 &&
            toPrev.getY() + toNext.getY() == 0 &&
            toPrev.getZ() + toNext.getZ() == 0);
      
      if(isStraight){
         corners.add(corner1);
      }else{
         // Check if this is an inner corner (convex toward enclosed) or outer corner (concave toward enclosed)
         Vec3i turnDir = new Vec3i(toPrev.getX() + toNext.getX(), toPrev.getY() + toNext.getY(), toPrev.getZ() + toNext.getZ());
         turnDir = normalizeToUnit(turnDir);
         
         // Determine if turn is toward or away from enclosed area
         BlockPos checkPos = block.offset(turnDir);
         boolean turnTowardEnclosed = enclosedSet.contains(checkPos);
         
         if(turnTowardEnclosed){
            Vec3 innerCorner = new Vec3(
                  baseX + inward.getX() * 0.5 + turnDir.getX() * 0.5,
                  baseY + inward.getY() * 0.5 + turnDir.getY() * 0.5,
                  baseZ + inward.getZ() * 0.5 + turnDir.getZ() * 0.5
            );
            corners.add(innerCorner);
         }else{
            corners.add(corner1);
            Vec3 outerCorner = new Vec3(
                  baseX + inward.getX() * 0.5,
                  baseY + inward.getY() * 0.5,
                  baseZ + inward.getZ() * 0.5
            );
            corners.add(outerCorner);
         }
      }
      
      return corners;
   }
   
   private Vec3i findInwardDirection(BlockPos block, Set<BlockPos> enclosedSet, Set<BlockPos> blockSet){
      for(Vec3i fillVec : fillVecs){
         BlockPos neighbor = block.offset(fillVec);
         if(enclosedSet.contains(neighbor)){
            return fillVec;
         }
      }
      return null;
   }
   
   
   public boolean isConvex(){
      List<Vec3> points = getOrderedBoundaryPoints();
      if(points.size() < 3) return false;
      
      int n = points.size();
      int sign = 0;
      
      for(int i = 0; i < n; i++){
         Vec3 p0 = points.get(i);
         Vec3 p1 = points.get((i + 1) % n);
         Vec3 p2 = points.get((i + 2) % n);
         Vec3 edge1 = p1.subtract(p0);
         Vec3 edge2 = p2.subtract(p1);
         Vec3 cross = edge1.cross(edge2);
         
         double crossComponent;
         if(axis == Direction.Axis.X){
            crossComponent = cross.x;
         }else if(axis == Direction.Axis.Y){
            crossComponent = cross.y;
         }else{
            crossComponent = cross.z;
         }
         
         if(Math.abs(crossComponent) < 1e-9) continue;
         
         int currentSign = crossComponent > 0 ? 1 : -1;
         if(sign == 0){
            sign = currentSign;
         }else if(sign != currentSign){
            return false;
         }
      }
      return true;
   }
   
   public Direction.Axis getAxis(){
      return axis;
   }
   
   /**
    * Searches for a valid gateway frame starting from the given positions.
    * Uses backtracking DFS with heuristic-guided move ordering for efficiency.
    * Searches for smaller frames first (eighth, quarter, half, then full size) to find solutions faster.
    *
    * @param level            The server level to search in
    * @param startPositions   List of potential frame starting positions
    * @param excludePos       Position to exclude from the frame (typically the gateway block)
    * @param maxFrameSize     Maximum number of blocks in the frame
    * @param forceRectangular Whether to require rectangular frames
    * @param forceConvex      Whether to require convex frames
    * @param maxIterations    Maximum search iterations before giving up
    * @return A valid GatewayFrame, or null if none found
    */
   public static GatewayFrame search(ServerLevel level, List<BlockPos> startPositions, BlockPos excludePos,
                                     int maxFrameSize, boolean forceRectangular, boolean forceConvex, int maxIterations){
      int[] iterations = {0};
      Set<Long> visitedStates = new HashSet<>();
      
      // Search with progressively larger frame size limits: eighth, quarter, half, then full
      int[] sizeLimits = {
            Math.max(4, maxFrameSize / 8),
            Math.max(4, maxFrameSize / 4),
            Math.max(4, maxFrameSize / 2),
            maxFrameSize
      };
      
      for(int sizeLimit : sizeLimits){
         for(BlockPos frameCore : startPositions){
            BlockState coreState = level.getBlockState(frameCore);
            if(!coreState.isCollisionShapeFullBlock(level, frameCore)) continue;
            Block frameType = coreState.getBlock();
            
            for(Direction.Axis axis : Direction.Axis.values()){
               GatewayFrame frame = new GatewayFrame(frameCore, frameType, axis, sizeLimit, forceRectangular, forceConvex);
               frame.addInvalid(excludePos);
               visitedStates.clear();
               
               GatewayFrame result = searchDFS(level, frame, iterations, maxIterations, visitedStates);
               if(result != null){
                  return result;
               }
            }
         }
      }
      return null;
   }
   
   /**
    * Computes a hash for the current frame state based on the block set and head position.
    * Used to detect repeated states during search.
    */
   private long computeStateHash(){
      long hash = 0;
      for(BlockPos pos : blockSet){
         // Combine position components into a hash
         hash ^= ((long) pos.getX() * 73856093L) ^ ((long) pos.getY() * 19349663L) ^ ((long) pos.getZ() * 83492791L);
      }
      // Include head position to distinguish different paths to same block set
      BlockPos head = blocks.getLast();
      hash = hash * 31 + head.hashCode();
      return hash;
   }
   
   /**
    * Recursive backtracking DFS search for a valid frame.
    * Uses a single mutable frame object with undo capability instead of copying.
    */
   private static GatewayFrame searchDFS(ServerLevel level, GatewayFrame frame, int[] iterations, int maxIterations, Set<Long> visitedStates){
      if(iterations[0]++ > maxIterations) return null;
      
      // Check for repeated state using transposition table
      long stateHash = frame.computeStateHash();
      if(visitedStates.contains(stateHash)){
         return null; // Already explored this configuration from this head
      }
      visitedStates.add(stateHash);
      
      // Check if we've completed a loop
      if(frame.isFinished()){
         frame.validateEnding(level);
         if(frame.finishedAndValid()){
            return frame.copy(); // Return a copy of the valid frame
         }
         return null;
      }
      
      // Early termination: if no ending positions are reachable, prune
      if(!frame.hasReachableEnding(level)) return null;
      
      // Early termination: if we can't possibly reach an ending with remaining budget, prune
      int minDistToEnd = frame.getMinDistanceToEnding();
      if(minDistToEnd > frame.getRemainingBudget()){
         return null;
      }
      
      // Get and sort moves by distance to ending (greedy heuristic)
      List<BlockPos> moves = frame.validMoves(level);
      if(moves.isEmpty()) return null;
      
      for(BlockPos move : moves){
         frame.makeMove(move, level);
         
         GatewayFrame result = searchDFS(level, frame, iterations, maxIterations, visitedStates);
         if(result != null){
            return result;
         }
         
         frame.undoMove();
      }
      
      return null;
   }
   
   @Override
   public boolean equals(Object obj){
      if(super.equals(obj)) return true;
      if(!(obj instanceof GatewayFrame frame)) return false;
      if(frame.axis != this.axis) return false;
      if(frame.type != this.type) return false;
      if(!frame.core.equals(this.core)) return false;
      if(this.blocks.size() != frame.blocks.size()) return false;
      for(BlockPos block : this.blocks){
         if(!frame.blocks.contains(block)) return false;
      }
      return true;
   }
   
   /**
    * Serializes this GatewayFrame to a CompoundTag.
    */
   public static CompoundTag toTag(GatewayFrame frame){
      CompoundTag tag = new CompoundTag();
      
      // Core position
      tag.putIntArray("core", new int[]{frame.core.getX(), frame.core.getY(), frame.core.getZ()});
      
      // Block type
      tag.putString("type", BuiltInRegistries.BLOCK.getKey(frame.type).toString());
      
      // Axis
      tag.putString("axis", frame.axis.getName());
      
      // Size limits
      tag.putInt("maxSize", frame.maxSize);
      tag.putInt("size", frame.size);
      
      // Flags
      tag.putBoolean("forceRectangular", frame.forceRectangular);
      tag.putBoolean("forceConvex", frame.forceConvex);
      tag.putBoolean("finished", frame.finished);
      tag.putBoolean("valid", frame.valid);
      
      // Blocks list (ordered)
      ListTag blocksList = new ListTag();
      for(BlockPos pos : frame.blocks){
         CompoundTag posTag = new CompoundTag();
         posTag.putIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
         blocksList.add(posTag);
      }
      tag.put("blocks", blocksList);
      
      // Enclosed set
      ListTag enclosedList = new ListTag();
      for(BlockPos pos : frame.enclosed){
         CompoundTag posTag = new CompoundTag();
         posTag.putIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
         enclosedList.add(posTag);
      }
      tag.put("enclosed", enclosedList);
      
      return tag;
   }
   
   /**
    * Deserializes a GatewayFrame from a CompoundTag.
    *
    * @return The deserialized GatewayFrame, or null if the tag is invalid.
    */
   public static GatewayFrame fromTag(CompoundTag tag){
      if(tag == null || tag.isEmpty()) return null;
      
      try{
         // Core position
         int[] coreArray = tag.getIntArray("core").orElse(new int[0]);
         if(coreArray.length != 3) return null;
         BlockPos core = new BlockPos(coreArray[0], coreArray[1], coreArray[2]);
         
         // Block type
         String typeId = tag.getStringOr("type", "");
         Block type = BuiltInRegistries.BLOCK.getValue(Identifier.parse(typeId));
         if(type == Blocks.AIR) return null;
         
         // Axis
         String axisName = tag.getStringOr("axis", "");
         Direction.Axis axis = Direction.Axis.byName(axisName);
         if(axis == null) return null;
         
         // Size limits
         int maxSize = tag.getIntOr("maxSize", 64);
         int size = tag.getIntOr("size", 1);
         
         // Flags
         boolean forceRectangular = tag.getBooleanOr("forceRectangular", false);
         boolean forceConvex = tag.getBooleanOr("forceConvex", false);
         boolean finished = tag.getBooleanOr("finished", false);
         boolean valid = tag.getBooleanOr("valid", false);
         
         // Blocks list (ordered)
         List<BlockPos> blocks = new ArrayList<>();
         ListTag blocksList = tag.getListOrEmpty("blocks");
         for(int i = 0; i < blocksList.size(); i++){
            CompoundTag posTag = blocksList.getCompoundOrEmpty(i);
            int[] posArray = posTag.getIntArray("pos").orElse(new int[0]);
            if(posArray.length == 3){
               blocks.add(new BlockPos(posArray[0], posArray[1], posArray[2]));
            }
         }
         if(blocks.isEmpty()) return null;
         
         // Enclosed set
         Set<BlockPos> enclosed = new HashSet<>();
         ListTag enclosedList = tag.getListOrEmpty("enclosed");
         for(int i = 0; i < enclosedList.size(); i++){
            CompoundTag posTag = enclosedList.getCompoundOrEmpty(i);
            int[] posArray = posTag.getIntArray("pos").orElse(new int[0]);
            if(posArray.length == 3){
               enclosed.add(new BlockPos(posArray[0], posArray[1], posArray[2]));
            }
         }
         
         return new GatewayFrame(core, type, axis, maxSize, forceRectangular, forceConvex, blocks, enclosed, size, finished, valid);
      }catch(Exception e){
         return null;
      }
   }
}