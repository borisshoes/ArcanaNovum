package net.borisshoes.arcananovum.cardinalcomponents;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

public class MagicEntity {
   private NbtCompound data;
   private String uuid;
   
   public MagicEntity(String uuid, NbtCompound nbt){
      this.uuid = uuid;
      this.data = nbt;
   }
   
   public NbtCompound getData(){
      return data;
   }
   
   public void setData(NbtCompound data){
      this.data = data;
   }
   
   public String getUuid(){
      return uuid;
   }
   
   public void setUuid(String uuid){
      this.uuid = uuid;
   }
}
