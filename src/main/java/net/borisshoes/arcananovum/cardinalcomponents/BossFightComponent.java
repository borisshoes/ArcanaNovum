package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.bosses.BossFights;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Pair;

public class BossFightComponent implements IBossFightComponent{
   public Pair<BossFights,NbtCompound> bossFight;
   
   @Override
   public void readData(ReadView readView){
      try{
         String id = readView.getString("id", "");
         if(id.isEmpty()){
            bossFight = null;
         }else{
            bossFight = new Pair<>(BossFights.fromLabel(id), readView.read("data",NbtCompound.CODEC).orElse(new NbtCompound()));
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeData(WriteView writeView){
      try{
         if(bossFight == null){
            writeView.putString("id","");
         }else{
            writeView.putString("id",bossFight.getLeft().label);
            writeView.put("data",NbtCompound.CODEC,bossFight.getRight());
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public boolean setBossFight(BossFights boss, NbtCompound data){
      if(bossFight == null || bossFight.getLeft() == boss){
         bossFight = new Pair<>(boss,data);
         return true;
      }else{
         return false;
      }
   }
   
   @Override
   public boolean removeBossFight(){
      if(bossFight == null){
         return false;
      }else{
         bossFight = null;
         return true;
      }
   }
   
   @Override
   public Pair<BossFights, NbtCompound> getBossFight(){
      return bossFight;
   }
}
