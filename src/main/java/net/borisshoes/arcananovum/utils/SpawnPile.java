package net.borisshoes.arcananovum.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import java.util.ArrayList;


public class SpawnPile {
   double x;
   double z;
   
   public SpawnPile(double x, double z){
      this.x=x;
      this.z=z;
   }
   
   double getDistance(SpawnPile other){
      double d = this.x - other.x;
      double e = this.z - other.z;
      return Math.sqrt(d * d + e * e);
   }
   
   void normalize(){
      double d = this.absolute();
      this.x /= d;
      this.z /= d;
   }
   
   double absolute(){
      return Math.sqrt(this.x * this.x + this.z * this.z);
   }
   
   public void subtract(SpawnPile other){
      this.x -= other.x;
      this.z -= other.z;
   }
   
   public boolean clamp(double minX, double minZ, double maxX, double maxZ){
      boolean bl = false;
      if(this.x < minX){
         this.x = minX;
         bl = true;
      } else if(this.x > maxX){
         this.x = maxX;
         bl = true;
      }
      if(this.z < minZ){
         this.z = minZ;
         bl = true;
      } else if(this.z > maxZ){
         this.z = maxZ;
         bl = true;
      }
      return bl;
   }
   
   public static int getSurfaceY(BlockGetter blockView, int maxY, int x, int z){
      BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, (double)(maxY + 1), z);
      boolean bl = blockView.getBlockState(mutable).isAir();
      mutable.move(Direction.DOWN);
      boolean bl2 = blockView.getBlockState(mutable).isAir();
      while(mutable.getY() > blockView.getMinY()){
         mutable.move(Direction.DOWN);
         boolean bl3 = blockView.getBlockState(mutable).isAir();
         if(!bl3 && bl2 && bl){
            return mutable.getY() + 1;
         }
         bl = bl2;
         bl2 = bl3;
      }
      return maxY + 1;
   }
   
   private static BlockPos getEntitySpawnPos(Level world, EntityType<?> entityType, int x, int z, boolean ignoreRestrictions){
      //int i = world.getTopY(SpawnRestriction.getHeightmapType(entityType), x, z);
      int i = world.getMaxY();
      BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, i, z);
      if(world.dimensionType().hasCeiling()){
         do{
            mutable.move(Direction.DOWN);
         }while(!world.getBlockState(mutable).isAir());
      }
      do{
         mutable.move(Direction.DOWN);
      }while(world.getBlockState(mutable).isAir() && mutable.getY() > world.getMinY());
      mutable.move(Direction.UP);
      
      if(ignoreRestrictions){
         return mutable.immutable();
      }else{
         return SpawnPlacements.getPlacementType(entityType).adjustSpawnPosition(world, mutable.immutable());
      }
      
      
   }
   
   public static ArrayList<BlockPos> makeSpawnLocations(int num, int range, ServerLevel world, EntityType<?> entityType, BlockPos center){
      ArrayList<BlockPos> positions = new ArrayList<>();
      for(int i = 0; i < num; i++){
         BlockPos entitySpawnPos;
         int tries = 0;
         do{
            int x = center.getX() + (int) (Math.random() * range * 2 - range);
            int z = center.getZ() + (int) (Math.random() * range * 2 - range);
            entitySpawnPos = getEntitySpawnPos(world, entityType, x, z, true);
            tries++;
         }while(Math.abs(entitySpawnPos.getY()-center.getY()) > range && tries < 10000);
         positions.add(entitySpawnPos);
      }
      return positions;
   }
   
   public int getY(BlockGetter blockView, int maxY){
      BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(this.x, (double)(maxY + 1), this.z);
      boolean bl = blockView.getBlockState(mutable).isAir();
      mutable.move(Direction.DOWN);
      boolean bl2 = blockView.getBlockState(mutable).isAir();
      while (mutable.getY() > blockView.getMinY()){
         mutable.move(Direction.DOWN);
         boolean bl3 = blockView.getBlockState(mutable).isAir();
         if(!bl3 && bl2 && bl){
            return mutable.getY() + 1;
         }
         bl = bl2;
         bl2 = bl3;
      }
      return maxY + 1;
   }
   
   public boolean isSafe(BlockGetter world, int maxY){
      BlockPos blockPos = BlockPos.containing(this.x, (double)(this.getY(world, maxY) - 1), this.z);
      BlockState blockState = world.getBlockState(blockPos);
      FluidState fluidState = world.getFluidState(blockPos);
      boolean invalid = blockState.is(Blocks.WITHER_ROSE) || blockState.is(Blocks.SWEET_BERRY_BUSH) || blockState.is(Blocks.CACTUS) || blockState.is(Blocks.POWDER_SNOW) || blockState.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE) || WalkNodeEvaluator.isBurningBlock(blockState);
      return blockPos.getY() < maxY && fluidState.isEmpty() && !invalid;
   }
   
   public void setPileLocation(RandomSource random, double minX, double minZ, double maxX, double maxZ){
      this.x = Mth.nextDouble(random, minX, maxX);
      this.z = Mth.nextDouble(random, minZ, maxZ);
   }
   
   public static ArrayList<BlockPos> makeSpawnLocations(int num, int range, ServerLevel world){
      return makeSpawnLocations(num,range,world,new BlockPos(0,0,0));
   }

   public static ArrayList<BlockPos> makeSpawnLocations(int num, int range, ServerLevel world, BlockPos center){
      return makeSpawnLocations(num,range,128,world,center);
   }
   
   public static ArrayList<BlockPos> makeSpawnLocations(int num, int range, int maxY, ServerLevel world, BlockPos center){
      ArrayList<BlockPos> positions = new ArrayList<>();
      for(int i = 0; i < num; i++){
         SpawnPile pile;
         int tries = 0;
         do{
            int x = center.getX() + (int) (Math.random() * range * 2 - range);
            int z = center.getZ() + (int) (Math.random() * range * 2 - range);
            pile = new SpawnPile(x, z);
            tries++;
         }while(!pile.isSafe(world,maxY) && tries < 10000);
         positions.add(BlockPos.containing(pile.x,pile.getY(world,maxY),pile.z));
      }
      return positions;
   }
}