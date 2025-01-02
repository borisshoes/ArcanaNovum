package net.borisshoes.arcananovum.cardinalcomponents;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

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
   public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup){
      try{
         anchors.clear();
         NbtList anchorsTag = tag.getList("Anchors", NbtElement.COMPOUND_TYPE);
         for (NbtElement e : anchorsTag){
            NbtCompound anchorTag = (NbtCompound) e;
            anchors.add(new BlockPos(anchorTag.getInt("x"),anchorTag.getInt("y"),anchorTag.getInt("z")));
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup){
      try{
         NbtList anchorsTag = new NbtList();
         for(BlockPos anchor : anchors){
            NbtCompound anchorTag = new NbtCompound();
            anchorTag.putInt("x",anchor.getX());
            anchorTag.putInt("y",anchor.getY());
            anchorTag.putInt("z",anchor.getZ());
            anchorsTag.add(anchorTag);
         }
         tag.put("Anchors",anchorsTag);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
