package net.borisshoes.arcananovum.cardinalcomponents;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class MagicBlock {
   private NbtCompound data;
   private BlockPos pos;
   private boolean guiOpen = false;
   
   public MagicBlock(BlockPos pos){
      this.pos = pos;
      this.data = new NbtCompound();
   }
   
   public MagicBlock(int x, int y, int z){
      this.pos = new BlockPos(x,y,z);
      this.data = new NbtCompound();
   }
   
   public MagicBlock(BlockPos pos, NbtCompound nbt){
      this.pos = pos;
      this.data = nbt;
   }
   
   public MagicBlock(int x, int y, int z, NbtCompound nbt){
      this.pos = new BlockPos(x,y,z);
      this.data = nbt;
   }
   
   public BlockPos getPos(){
      return pos;
   }
   
   public NbtCompound getData(){
      return data;
   }
   
   public void setData(NbtCompound data){
      this.data = data;
   }
   
   public void setPos(BlockPos pos){
      this.pos = pos;
   }
   
   public void setPos(int x, int y, int z){
      this.pos = new BlockPos(x,y,z);
   }
   
   public boolean isGuiOpen(){
      return guiOpen;
   }
   
   public void setGuiOpen(boolean guiOpen){
      this.guiOpen = guiOpen;
   }
}
