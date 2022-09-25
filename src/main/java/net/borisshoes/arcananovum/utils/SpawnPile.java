package net.borisshoes.arcananovum.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
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
      BlockPos blockPos = new BlockPos(this.x, (double)(this.getY(world, maxY) - 1), this.z);
      BlockState blockState = world.getBlockState(blockPos);
      Material material = blockState.getMaterial();
      return blockPos.getY() < maxY && !material.isLiquid() && material != Material.FIRE;
   }
   
   public void setPileLocation(Random random, double minX, double minZ, double maxX, double maxZ) {
      this.x = MathHelper.nextDouble(random, minX, maxX);
      this.z = MathHelper.nextDouble(random, minZ, maxZ);
   }
   
   public static ArrayList<BlockPos> makeSpawnLocations(int num, int range, ServerWorld endWorld){
      ArrayList<BlockPos> positions = new ArrayList<>();
      for(int i = 0; i < num; i++){
         SpawnPile pile;
         do{
            int x = (int) (Math.random() * range * 2 - range);
            int z = (int) (Math.random() * range * 2 - range);
            pile = new SpawnPile(x, z);
         }while(!pile.isSafe(endWorld,128));
         positions.add(new BlockPos(pile.x,pile.getY(endWorld,128),pile.z));
      }
      return positions;
   }
}