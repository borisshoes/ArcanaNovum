package net.borisshoes.arcananovum.research;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
   private final ItemStack displayItem;
   protected final ResourceKey<ResearchTask>[] prerequisites;
   
   protected ResearchTask(String id, Type type, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      this.id = id;
      this.type = type;
      this.displayItem = displayItem;
      this.prerequisites = prerequisites;
   }
   
   protected ResearchTask(String id, Type type, ItemStack displayItem){
      this.id = id;
      this.type = type;
      this.displayItem = displayItem;
      this.prerequisites = new ResourceKey[0];
   }
   
   public String getTranslationKey(){
      return "research."+MOD_ID+".name."+this.id;
   }
   
   public String getDescriptionTranslationKey(){
      return "research."+MOD_ID+".description."+this.id;
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
   
   public MutableComponent getName(){
      return Component.translatable(getTranslationKey());
   }
   
   public List<MutableComponent> getDescription(){
      String fullText = Component.translatable(getDescriptionTranslationKey()).getString();
      String[] lines = fullText.split("\n");
      List<MutableComponent> components = new ArrayList<>();
      for(String line : lines){
         components.add(Component.literal(line));
      }
      return components;
   }
   
   public String getId(){
      return id;
   }
   
   public List<MutableComponent> getColoredDescription(){
      List<MutableComponent> descLines = new ArrayList<>();
      boolean colorSwitch = false;
      for(MutableComponent descLine : getDescription()){
         String lineText = descLine.getString();
         if(!lineText.isEmpty() && lineText.charAt(0) != ' ') colorSwitch = !colorSwitch;
         boolean finalColor = colorSwitch;
         descLines.add(Component.literal(lineText).withStyle(style -> style.withColor(finalColor ? 0xe6d9bc : 0xb5a684)));
      }
      return descLines;
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
