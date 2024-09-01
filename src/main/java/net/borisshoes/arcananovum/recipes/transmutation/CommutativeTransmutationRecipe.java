package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class CommutativeTransmutationRecipe extends TransmutationRecipe{
   
   private final List<ItemStack> communalInputs;
   
   public CommutativeTransmutationRecipe(String name, List<ItemStack> communalInputs, ItemStack reagent1, ItemStack reagent2){
      super(name,reagent1,reagent2);
      this.communalInputs = communalInputs;
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
      if(reagent1Entity != null){
         int reagentCount = 0;
         if(validStack(re1,reagent1Stack) && !re1.isEmpty()){
            reagentCount = re1.getCount();
         }else if(validStack(re2,reagent1Stack) && !re2.isEmpty()){
            reagentCount = re2.getCount();
         }
         if(reagent1Stack.getCount() == reagentCount){
            reagent1Entity.discard();
         }else{
            reagent1Stack.decrement(reagentCount);
            reagent1Entity.setStack(reagent1Stack);
         }
      }
      
      if(reagent2Entity != null){
         int reagentCount = 0;
         if(validStack(re1,reagent2Stack) && !re1.isEmpty()){
            reagentCount = re1.getCount();
         }else if(validStack(re2,reagent2Stack) && !re2.isEmpty()){
            reagentCount = re2.getCount();
         }
         if(reagent2Stack.getCount() == reagentCount){
            reagent2Entity.discard();
         }else{
            reagent2Stack.decrement(reagentCount);
            reagent2Entity.setStack(reagent2Stack);
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
      boolean reagent1Check = validStack(re1,reagent1Input) || validStack(re1,reagent2Input);
      boolean reagent2Check = validStack(re2,reagent1Input) || validStack(re2,reagent2Input);
      if(!reagent1Check || !reagent2Check) return false;
      if(!validCommunalInput(sourceInput)) return false;
      if(!validCommunalInput(focusInput)) return false;
      if(sourceInput.isOf(focusInput.getItem())) return false;
      return true;
   }
   
   public boolean validCommunalInput(ItemStack input){
      for(ItemStack comStack : communalInputs){
         if(validStack(comStack,input)) return true;
      }
      return false;
   }
   
   public List<ItemStack> getCommunalInputs(){
      return communalInputs;
   }
   
   @Override
   public ItemStack getViewStack(){
      if(ArcanaItemUtils.isArcane(communalInputs.get(0))){
         return new ItemStack(communalInputs.get(0).getItem(),1);
      }
      return communalInputs.get(0);
   }
}
