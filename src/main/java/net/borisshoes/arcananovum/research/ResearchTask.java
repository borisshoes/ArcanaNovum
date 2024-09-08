package net.borisshoes.arcananovum.research;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;


public abstract class ResearchTask {
   
   public final String id;
   public final Type type;
   private final Text name;
   private final Text[] description;
   private final ItemStack displayItem;
   protected final RegistryKey<ResearchTask>[] prerequisites;
   
   protected ResearchTask(String id, Type type, Text name, Text[] description, ItemStack displayItem, RegistryKey<ResearchTask>... prerequisites){
      this.id = id;
      this.type = type;
      this.name = name;
      this.description = description;
      this.displayItem = displayItem;
      this.prerequisites = prerequisites;
   }
   
   protected ResearchTask(String id, Type type, Text name, Text[] description, ItemStack displayItem){
      this.id = id;
      this.type = type;
      this.name = name;
      this.description = description;
      this.displayItem = displayItem;
      this.prerequisites = new RegistryKey[0];
   }
   
   public String getTranslationKey(){
      return "research."+MOD_ID+".name."+this.id;
   }
   
   public enum Type{
      STATISTIC,
      OBTAIN_ITEM,
      ADVANCEMENT,
      ARCANA_ITEM_UNLOCK,
      CUSTOM_EVENT
   }
   
   public boolean satisfiedPreReqs(ServerPlayerEntity player){
      for(RegistryKey<ResearchTask> prerequisite : this.prerequisites){
         Optional<ResearchTask> opt = ResearchTasks.RESEARCH_TASKS.getOrEmpty(prerequisite);
         if(opt.isPresent() && !opt.get().isAcquired(player)){
            return false;
         }
      }
      return true;
   }
   
   public boolean satisfiedPrePreReqs(ServerPlayerEntity player){
      for(RegistryKey<ResearchTask> prerequisite : this.prerequisites){
         Optional<ResearchTask> opt = ResearchTasks.RESEARCH_TASKS.getOrEmpty(prerequisite);
         if(opt.isPresent() && !opt.get().satisfiedPreReqs(player)){
            return false;
         }
      }
      return true;
   }
   
   public List<ResearchTask> getPreReqs(){
      List<ResearchTask> prereqs = new ArrayList<>();
      for(RegistryKey<ResearchTask> prerequisite : this.prerequisites){
         ResearchTasks.RESEARCH_TASKS.getOrEmpty(prerequisite).ifPresent(prereqs::add);
      }
      return prereqs;
   }
   
   public abstract boolean isAcquired(ServerPlayerEntity player);
   
   public Text getName(){
      return name.copy();
   }
   
   public String getId(){
      return id;
   }
   
   public Text[] getDescription(){
      Text[] copy = new Text[description.length];
      for(int i = 0; i < description.length; i++){
         copy[i] = description[i].copy();
      }
      return copy;
   }
   
   public ItemStack getDisplayItem(){
      return displayItem.copy();
   }
   
   @Override
   public boolean equals(Object obj){
      return obj instanceof ResearchTask task && task.getId().equals(this.id);
   }
   
   @Override
   public String toString(){
      return type.name() + " - " + this.id;
   }
}
