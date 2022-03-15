package net.borisshoes.arcananovum.cardinalcomponents;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class MagicBlockComponent implements IMagicBlockComponent{
   public final List<MagicBlock> blocks = new ArrayList<>();
   
   
   @Override
   public List<MagicBlock> getBlocks(){
      return blocks;
   }
   
   @Override
   public boolean addBlock(MagicBlock block){
      if (blocks.contains(block)) return false;
      return blocks.add(block);
   }
   
   @Override
   public boolean removeBlock(MagicBlock block){
      if (!blocks.contains(block)) return false;
      return blocks.remove(block);
   }
   
   @Override
   public void readFromNbt(NbtCompound tag){
      try{
         blocks.clear();
         NbtList blocksTag = tag.getList("MagicBlocks", NbtType.COMPOUND);
         for (NbtElement e : blocksTag) {
            NbtCompound blockTag = (NbtCompound) e;
            NbtList pos = blockTag.getList("pos",NbtType.INT);
            blocks.add(new MagicBlock(pos.getInt(0),pos.getInt(1),pos.getInt(2),blockTag.getCompound("data")));
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeToNbt(NbtCompound tag){
      try{
         NbtList blocksTag = new NbtList();
         for(MagicBlock block : blocks){
            NbtCompound blockTag = new NbtCompound();
            NbtList pos = new NbtList();
            pos.add(0, NbtInt.of(block.getPos().getX()));
            pos.add(1, NbtInt.of(block.getPos().getY()));
            pos.add(2, NbtInt.of(block.getPos().getZ()));
            blockTag.put("pos",pos);
            blockTag.put("data",block.getData());
            blocksTag.add(blockTag);
         }
         tag.put("MagicBlocks",blocksTag);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
