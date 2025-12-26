package net.borisshoes.arcananovum.world.structures;

import com.mojang.datafixers.util.Pair;
import net.borisshoes.arcananovum.mixins.StructureTemplatePoolAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.ArrayList;
import java.util.List;

public class FabricStructurePoolImpl implements FabricStructurePool {
   private final StructureTemplatePool pool;
   private final Identifier id;
   
   public FabricStructurePoolImpl(StructureTemplatePool pool, Identifier id){
      this.pool = pool;
      this.id = id;
   }
   
   @Override
   public void addStructurePoolElement(StructurePoolElement element){
      addStructurePoolElement(element, 1);
   }
   
   @Override
   public void addStructurePoolElement(StructurePoolElement element, int weight){
      StructureTemplatePoolAccessor pool = (StructureTemplatePoolAccessor) getUnderlyingPool();
      
      if(pool.getRawTemplates() instanceof ArrayList){
         pool.getRawTemplates().add(new Pair<>(element, weight));
      }else{
         List<Pair<StructurePoolElement, Integer>> list = new ArrayList<>(pool.getRawTemplates());
         list.add(new Pair<>(element, weight));
         pool.setRawTemplates(list);
      }
      
      for (int i = 0; i < weight; i++){
         pool.getTemplates().add(element);
      }
   }
   
   @Override
   public StructureTemplatePool getUnderlyingPool(){
      return pool;
   }
   
   @Override
   public Identifier getId(){
      return id;
   }
}