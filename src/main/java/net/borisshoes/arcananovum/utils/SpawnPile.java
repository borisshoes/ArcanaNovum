package net.borisshoes.arcananovum.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;

import java.util.ArrayList;


public class SpawnPile {
   double x;
   double z;
   
   public SpawnPile(double x, double z) {
      this.x=x;
      this.z=z;
   }
   
   double getDistance(SpawnPile other) {
      double d = this.x - other.x;
      double e = this.z - other.z;
      return Math.sqrt(d * d + e * e);
   }
   
   void normalize() {
      double d = this.absolute();
      this.x /= d;
      this.z /= d;
   }
   
   double absolute() {
      return Math.sqrt(this.x * this.x + this.z * this.z);
   }
   
   public void subtract(SpawnPile other) {
      this.x -= other.x;
      this.z -= other.z;
   }
   
   public boolean clamp(double minX, double minZ, double maxX, double maxZ) {
      boolean bl = false;
      if (this.x < minX) {
         this.x = minX;
         bl = true;
      } else if (this.x > maxX) {
         this.x = maxX;
         bl = true;
      }
      if (this.z < minZ) {
         this.z = minZ;
         bl = true;
      } else if (this.z > maxZ) {
         this.z = maxZ;
         bl = true;
      }
      return bl;
   }
   
   public static int getSurfaceY(BlockView blockView, int maxY, int x, int z){
      BlockPos.Mutable mutable = new BlockPos.Mutable(x, (double)(maxY + 1), z);
      boolean bl = blockView.getBlockState(mutable).isAir();
      mutable.move(Direction.DOWN);
      boolean bl2 = blockView.getBlockState(mutable).isAir();
      while (mutable.getY() > blockView.getBottomY()) {
         mutable.move(Direction.DOWN);
         boolean bl3 = blockView.getBlockState(mutable).isAir();
         if (!bl3 && bl2 && bl) {
            return mutable.getY() + 1;
         }
         bl = bl2;
         bl2 = bl3;
      }
      return maxY + 1;
   }
   
   public int getY(BlockView blockView, int maxY) {
      BlockPos.Mutable mutable = new BlockPos.Mutable(this.x, (double)(maxY + 1), this.z);
      boolean bl = blockView.getBlockState(mutable).isAir();
      mutable.move(Direction.DOWN);
      boolean bl2 = blockView.getBlockState(mutable).isAir();
      while (mutable.getY() > blockView.getBottomY()) {
         mutable.move(Direction.DOWN);
         boolean bl3 = blockView.getBlockState(mutable).isAir();
         if (!bl3 && bl2 && bl) {
            return mutable.getY() + 1;
         }
         bl = bl2;
         bl2 = bl3;
      }
      return maxY + 1;
   }
   
   public boolean isSafe(BlockView world, int maxY) {
      BlockPos blockPos = BlockPos.ofFloored(this.x, (double)(this.getY(world, maxY) - 1), this.z);
      BlockState blockState = world.getBlockState(blockPos);
      FluidState fluidState = world.getFluidState(blockPos);
      boolean invalid = blockState.isOf(Blocks.WITHER_ROSE) || blockState.isOf(Blocks.SWEET_BERRY_BUSH) || blockState.isOf(Blocks.CACTUS) || blockState.isOf(Blocks.POWDER_SNOW) || blockState.isIn(BlockTags.PREVENT_MOB_SPAWNING_INSIDE) || LandPathNodeMaker.inflictsFireDamage(blockState);
      return blockPos.getY() < maxY && fluidState.isEmpty() && !invalid;
   }
   
   public void setPileLocation(Random random, double minX, double minZ, double maxX, double maxZ) {
      this.x = MathHelper.nextDouble(random, minX, maxX);
      this.z = MathHelper.nextDouble(random, minZ, maxZ);
   }
   
   public static ArrayList<BlockPos> makeSpawnLocations(int num, int range, ServerWorld world){
      return makeSpawnLocations(num,range,world,new BlockPos(0,0,0));
   }

   public static ArrayList<BlockPos> makeSpawnLocations(int num, int range, ServerWorld world, BlockPos center){
      return makeSpawnLocations(num,range,128,world,center);
   }
   
   public static ArrayList<BlockPos> makeSpawnLocations(int num, int range, int maxY, ServerWorld world, BlockPos center){
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
         positions.add(BlockPos.ofFloored(pile.x,pile.getY(world,maxY),pile.z));
      }
      return positions;
   }
}