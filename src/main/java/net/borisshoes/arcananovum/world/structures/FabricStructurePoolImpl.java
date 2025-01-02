package net.borisshoes.arcananovum.world.structures;

import net.borisshoes.arcananovum.mixins.StructurePoolAccessor;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class FabricStructurePoolImpl implements FabricStructurePool {
   private final StructurePool pool;
   private final Identifier id;
   
   public FabricStructurePoolImpl(StructurePool pool, Identifier id){
      this.pool = pool;
      this.id = id;
   }
   
   @Override
   public void addStructurePoolElement(StructurePoolElement element){
      addStructurePoolElement(element, 1);
   }
   
   @Override
   public void addStructurePoolElement(StructurePoolElement element, int weight){
      StructurePoolAccessor pool = (StructurePoolAccessor) getUnderlyingPool();
      
      if(pool.getElementWeights() instanceof ArrayList){
         pool.getElementWeights().add(new Pair<>(element, weight));
      }else{
         List<Pair<StructurePoolElement, Integer>> list = new ArrayList<>(pool.getElementWeights());
         list.add(new Pair<>(element, weight));
         pool.setElementWeights(list);
      }
      
      for (int i = 0; i < weight; i++){
         pool.getElements().add(element);
      }
   }
   
   @Override
   public StructurePool getUnderlyingPool(){
      return pool;
   }
   
   @Override
   public Identifier getId(){
      return id;
   }
}