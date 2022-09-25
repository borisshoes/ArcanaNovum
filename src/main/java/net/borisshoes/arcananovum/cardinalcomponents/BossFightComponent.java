package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.bosses.BossFights;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;

import static net.borisshoes.arcananovum.Arcananovum.log;

public class BossFightComponent implements IBossFightComponent{
   public Pair<BossFights,NbtCompound> bossFight;
   
   @Override
   public void readFromNbt(NbtCompound tag){
      try{
         String id = tag.getString("id");
         if(id.isEmpty()){
            bossFight = null;
         }else{
            bossFight = new Pair<>(BossFights.fromLabel(id),tag.getCompound("data"));
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeToNbt(NbtCompound tag){
      try{
         if(bossFight == null){
            tag.putString("id","");
         }else{
            tag.putString("id",bossFight.getLeft().label);
            tag.put("data",bossFight.getRight());
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
