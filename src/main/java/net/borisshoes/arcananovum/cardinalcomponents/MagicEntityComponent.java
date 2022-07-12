package net.borisshoes.arcananovum.cardinalcomponents;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.List;

public class MagicEntityComponent implements IMagicEntityComponent{
   public final List<MagicEntity> entities = new ArrayList<>();
   
   
   @Override
   public List<MagicEntity> getEntities(){
      return entities;
   }
   
   @Override
   public boolean addEntity(MagicEntity entity){
      if (entities.contains(entity)) return false;
      return entities.add(entity);
   }
   
   @Override
   public boolean removeEntity(MagicEntity entity){
      if (!entities.contains(entity)) return false;
      return entities.remove(entity);
   }
   
   @Override
   public void readFromNbt(NbtCompound tag){
      try{
         entities.clear();
         NbtList entitiesTag = tag.getList("MagicEntities", NbtType.COMPOUND);
         for (NbtElement e : entitiesTag) {
            NbtCompound entityTag = (NbtCompound) e;
            NbtList pos = entityTag.getList("pos",NbtType.DOUBLE);
            entities.add(new MagicEntity(pos.getDouble(0),pos.getDouble(1),pos.getDouble(2),entityTag.getCompound("data")));
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeToNbt(NbtCompound tag){
      try{
         NbtList entitiesTag = new NbtList();
         for(MagicEntity entity : entities){
            NbtCompound entityTag = new NbtCompound();
            NbtList pos = new NbtList();
            pos.add(0, NbtDouble.of(entity.getPos().getX()));
            pos.add(1, NbtDouble.of(entity.getPos().getY()));
            pos.add(2, NbtDouble.of(entity.getPos().getZ()));
            entityTag.put("pos",pos);
            entityTag.put("data",entity.getData());
            entitiesTag.add(entityTag);
         }
         tag.put("MagicEntities",entitiesTag);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}