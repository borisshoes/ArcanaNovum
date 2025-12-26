package net.borisshoes.arcananovum.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;


public abstract class ResearchTask {
   
   public final String id;
   public final Type type;
   private final Component name;
   private final Component[] description;
   private final ItemStack displayItem;
   protected final ResourceKey<ResearchTask>[] prerequisites;
   
   protected ResearchTask(String id, Type type, Component name, Component[] description, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      this.id = id;
      this.type = type;
      this.name = name;
      this.description = description;
      this.displayItem = displayItem;
      this.prerequisites = prerequisites;
   }
   
   protected ResearchTask(String id, Type type, Component name, Component[] description, ItemStack displayItem){
      this.id = id;
      this.type = type;
      this.name = name;
      this.description = description;
      this.displayItem = displayItem;
      this.prerequisites = new ResourceKey[0];
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
   
   public boolean satisfiedPreReqs(ServerPlayer player){
      for(ResourceKey<ResearchTask> prerequisite : this.prerequisites){
         Optional<ResearchTask> opt = ResearchTasks.RESEARCH_TASKS.getOptional(prerequisite);
         if(opt.isPresent() && !opt.get().isAcquired(player)){
            return false;
         }
      }
      return true;
   }
   
   public boolean satisfiedPrePreReqs(ServerPlayer player){
      for(ResourceKey<ResearchTask> prerequisite : this.prerequisites){
         Optional<ResearchTask> opt = ResearchTasks.RESEARCH_TASKS.getOptional(prerequisite);
         if(opt.isPresent() && !opt.get().satisfiedPreReqs(player)){
            return false;
         }
      }
      return true;
   }
   
   public List<ResearchTask> getPreReqs(){
      List<ResearchTask> prereqs = new ArrayList<>();
      for(ResourceKey<ResearchTask> prerequisite : this.prerequisites){
         ResearchTasks.RESEARCH_TASKS.getOptional(prerequisite).ifPresent(prereqs::add);
      }
      return prereqs;
   }
   
   public abstract boolean isAcquired(ServerPlayer player);
   
   public Component getName(){
      return name.copy();
   }
   
   public String getId(){
      return id;
   }
   
   public Component[] getDescription(){
      Component[] copy = new Component[description.length];
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
