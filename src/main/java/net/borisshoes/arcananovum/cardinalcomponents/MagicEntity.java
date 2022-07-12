package net.borisshoes.arcananovum.cardinalcomponents;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

public class MagicEntity {
   private NbtCompound data;
   private Vec3d pos;
   
   public MagicEntity(Vec3d pos, NbtCompound nbt){
      this.pos = pos;
      this.data = nbt;
   }
   
   public MagicEntity(double x, double y, double z, NbtCompound nbt){
      this.pos = new Vec3d(x,y,z);
      this.data = nbt;
   }
   
   public Vec3d getPos(){
      return pos;
   }
   
   public NbtCompound getData(){
      return data;
   }
   
   public void setData(NbtCompound data){
      this.data = data;
   }
   
   public void setPos(Vec3d pos){
      this.pos = pos;
   }
   
   public void setPos(double x, double y, double z){
      this.pos = new Vec3d(x,y,z);
   }
}
