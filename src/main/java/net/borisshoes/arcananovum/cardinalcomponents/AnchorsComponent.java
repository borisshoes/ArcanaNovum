package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.borislib.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class AnchorsComponent implements IAnchorsComponent{
   
   public final List<BlockPos> anchors = new ArrayList<>();
   
   @Override
   public List<BlockPos> getAnchors(){
      return anchors;
   }
   
   @Override
   public boolean addAnchor(BlockPos anchor){
      if(anchors.contains(anchor)) return false;
      return anchors.add(anchor);
   }
   
   @Override
   public boolean removeAnchor(BlockPos anchor){
      if(!anchors.contains(anchor)) return false;
      return anchors.remove(anchor);
   }
   
   @Override
   public void readData(ValueInput readView){
      try{
         anchors.clear();
         readView.read("Anchors", CodecUtils.BLOCKPOS_LIST).ifPresent(anchors::addAll);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeData(ValueOutput writeView){
      try{
         writeView.store("Anchors",CodecUtils.BLOCKPOS_LIST,anchors);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
