package net.borisshoes.arcananovum.recipes.transmutation;

import com.mojang.datafixers.util.Either;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.log;

public class CommutativeTransmutationRecipe extends TransmutationRecipe{
   
   private final ArrayList<ItemStack> communalInputs;
   private ItemStack viewStack = new ItemStack(Items.BARRIER);
   
   public CommutativeTransmutationRecipe(String name, ItemStack reagent1, ItemStack reagent2){
      super(name,reagent1,reagent2);
      this.communalInputs = new ArrayList<>();
   }
   
   public CommutativeTransmutationRecipe with(Item... items){
      for(Item item : items){
         addItem(item);
      }
      return this;
   }
   
   public CommutativeTransmutationRecipe with(ItemStack... items){
      for(ItemStack item : items){
         addItemStack(item);
      }
      return this;
   }
   
   public CommutativeTransmutationRecipe with(Either<TagKey<Item>,TagKey<Block>> tag){
      if(tag.left().isPresent()){
         for(RegistryEntry<Item> itemEntry : Registries.ITEM.getIndexedEntries()){
            try{
               if(itemEntry.isIn(tag.left().get())){
                  addItem(itemEntry.value());
               }
            }catch(Exception e){
               log(2,"Error getting tags for "+itemEntry.getIdAsString());
            }
         }
      }else if(tag.right().isPresent()){
         for(RegistryEntry<Item> itemEntry : Registries.ITEM.getIndexedEntries()){
            try{
               if(itemEntry.value() instanceof BlockItem blockItem){
                  if(blockItem.getBlock() != null && blockItem.getBlock().getRegistryEntry().isIn(tag.right().get())){
                     addItem(itemEntry.value());
                  }
               }
            }catch(Exception e){
               log(2,"Error getting tags for "+itemEntry.getIdAsString());
            }
         }
      }
      return this;
   }
   
   public CommutativeTransmutationRecipe withViewStack(ItemStack viewStack){
      this.viewStack = viewStack;
      return this;
   }
   
   private void addItem(Item item){
      addItemStack(new ItemStack(item));
   }
   
   private void addItemStack(ItemStack stack){
      for(ItemStack communalInput : this.communalInputs){
         if(ItemStack.areItemsAndComponentsEqual(stack,communalInput)) return;
      }
      this.communalInputs.add(stack);
   }
   
   @Override
   public List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayerEntity player){
      return List.of(negativeInput.copyWithCount(positiveInput.getCount()),negativeInput,aequalisInput);
   }
   
   @Override
   public List<Pair<ItemStack,String>> doTransmutation(ItemEntity sourceEntity, ItemEntity focusEntity, ItemEntity reagent1Entity, ItemEntity reagent2Entity, ItemEntity aequalisEntity, TransmutationAltarBlockEntity altar, ServerPlayerEntity player){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      ItemStack re1 = getBargainReagent(reagent1,bargainLvl);
      ItemStack re2 = getBargainReagent(reagent2,bargainLvl);
      ItemStack sourceStack = sourceEntity != null ? sourceEntity.getStack() : ItemStack.EMPTY;
      ItemStack focusStack = focusEntity != null ? focusEntity.getStack() : ItemStack.EMPTY;
      ItemStack reagent1Stack = reagent1Entity != null ? reagent1Entity.getStack() : ItemStack.EMPTY;
      ItemStack reagent2Stack = reagent2Entity != null ? reagent2Entity.getStack() : ItemStack.EMPTY;
      if(!canTransmute(sourceStack,focusStack,reagent1Stack,reagent2Stack,ItemStack.EMPTY,altar)) return new ArrayList<>();
      
      ItemStack outputStack = focusStack.copyWithCount(sourceStack.getCount());
      if(sourceEntity != null) sourceEntity.discard();
      boolean m11 = validStack(re1, reagent1Stack), m22 = validStack(re2, reagent2Stack), m12 = validStack(re1, reagent2Stack), m21 = validStack(re2, reagent1Stack);
      boolean straight = m11 && m22;
      boolean cross = !straight && m12 && m21;
      if (!straight && !cross) return new ArrayList<>(); // should be impossible
      
      ItemStack reagent1 = straight ? re1 : re2;
      ItemStack reagent2 = straight ? re2 : re1;
      
      if (reagent1Entity != null) {
         int take = reagent1.isEmpty() ? 0 : reagent1.getCount();
         if(take > 0){
            if(reagent1Stack.getCount() == take){
               reagent1Entity.discard();
            }else{
               reagent1Stack.decrement(take);
               reagent1Entity.setStack(reagent1Stack);
            }
         }
      }
      
      if (reagent2Entity != null) {
         int take = reagent2.isEmpty() ? 0 : reagent2.getCount();
         if(take > 0){
            if(reagent2Stack.getCount() == take){
               reagent2Entity.discard();
            }else{
               reagent2Stack.decrement(take);
               reagent2Entity.setStack(reagent2Stack);
            }
         }
      }
      
      List<Pair<ItemStack,String>> outputs = new ArrayList<>();
      outputs.add(new Pair<>(outputStack,"negative"));
      return outputs;
   }
   
   @Override
   public boolean canTransmute(ItemStack sourceInput, ItemStack focusInput, ItemStack reagent1Input, ItemStack reagent2Input, ItemStack aequalisInput, TransmutationAltarBlockEntity altar){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      ItemStack re1 = getBargainReagent(reagent1,bargainLvl);
      ItemStack re2 = getBargainReagent(reagent2,bargainLvl);
      boolean reagentCheck1 = validStack(re1, reagent1Input) && validStack(re2, reagent2Input);
      boolean reagentCheck2 = validStack(re1, reagent2Input) && validStack(re2, reagent1Input);
      if (!(reagentCheck1 || reagentCheck2)) return false;
      if(!validCommunalInput(sourceInput)) return false;
      if(!validCommunalInput(focusInput)) return false;
      if(sourceInput.isOf(focusInput.getItem())) return false;
      return true;
   }
   
   public boolean validCommunalInput(ItemStack input){
      for(ItemStack comInput : this.communalInputs){
         if(validStack(comInput,input)) return true;
      }
      return false;
   }
   
   public List<ItemStack> getCommunalInputs(){
      return new ArrayList<>(this.communalInputs);
   }
   
   @Override
   public ItemStack getViewStack(){
      return viewStack.copy();
   }
}
