package net.borisshoes.arcananovum.research;

import com.mojang.datafixers.util.Either;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ObtainResearchTask extends ResearchTask{
   
   private final Either<Item, TagKey<Item>> item;
   
   public ObtainResearchTask(String id, TagKey<Item> item, ItemStack displayItem){
      super(id, Type.OBTAIN_ITEM, displayItem);
      this.item = Either.right(item);
   }
   
   public ObtainResearchTask(String id, TagKey<Item> item, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      super(id, Type.OBTAIN_ITEM, displayItem, prerequisites);
      this.item = Either.right(item);
   }
   
   public ObtainResearchTask(String id, Item item, ItemStack displayItem){
      super(id, Type.OBTAIN_ITEM, displayItem);
      this.item = Either.left(item);
   }
   
   public ObtainResearchTask(String id, Item item, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      super(id, Type.OBTAIN_ITEM, displayItem, prerequisites);
      this.item = Either.left(item);
   }
   
   public Either<Item, TagKey<Item>> getItem(){
      return item;
   }
   
   @Override
   public boolean isAcquired(ServerPlayer player){
      return ArcanaNovum.data(player).completedResearchTask(id);
   }
}
