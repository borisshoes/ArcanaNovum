package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.bosses.BossFights;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BossFightComponent implements IBossFightComponent {
   public Tuple<BossFights, CompoundTag> bossFight;
   
   @Override
   public void readData(ValueInput readView){
      try{
         String id = readView.getStringOr("id", "");
         if(id.isEmpty()){
            bossFight = null;
         }else{
            bossFight = new Tuple<>(BossFights.fromLabel(id), readView.read("data", CompoundTag.CODEC).orElse(new CompoundTag()));
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeData(ValueOutput writeView){
      try{
         if(bossFight == null){
            writeView.putString("id", "");
         }else{
            writeView.putString("id", bossFight.getA().label);
            writeView.store("data", CompoundTag.CODEC, bossFight.getB());
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public boolean setBossFight(BossFights boss, CompoundTag data){
      if(bossFight == null || bossFight.getA() == boss){
         bossFight = new Tuple<>(boss, data);
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
   public Tuple<BossFights, CompoundTag> getBossFight(){
      return bossFight;
   }
}
