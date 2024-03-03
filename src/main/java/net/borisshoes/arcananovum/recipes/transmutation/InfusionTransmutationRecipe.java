package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class InfusionTransmutationRecipe extends TransmutationRecipe{
   
   private final ItemStack input;
   private final ItemStack output;
   
   public InfusionTransmutationRecipe(String name, ItemStack input, ItemStack output, ItemStack reagent1, ItemStack reagent2){
      super(name,reagent1,reagent2);
      this.input = input;
      this.output = output;
   }
   
   @Override
   public List<Pair<ItemStack,String>> doTransmutation(ItemEntity input1Entity, ItemEntity input2Entity, ItemEntity reagent1Entity, ItemEntity reagent2Entity, ItemEntity aequalisEntity, TransmutationAltarBlockEntity altar, ServerPlayerEntity player){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      ItemStack re1 = getBargainReagent(reagent1,bargainLvl);
      ItemStack re2 = getBargainReagent(reagent2,bargainLvl);
      ItemStack reagent1Stack = reagent1Entity != null ? reagent1Entity.getStack() : ItemStack.EMPTY;
      ItemStack reagent2Stack = reagent2Entity != null ? reagent2Entity.getStack() : ItemStack.EMPTY;
      ItemStack inputStack;
      ItemEntity inputEntity;
      String outputPos;
      if(input1Entity != null && validStack(input,input1Entity.getStack())){
         inputStack = input1Entity.getStack();
         inputEntity = input1Entity;
         outputPos = "negative";
      }else if(input2Entity != null && validStack(input,input2Entity.getStack())){
         inputStack = input2Entity.getStack();
         inputEntity = input2Entity;
         outputPos = "positive";
      }else{
         return new ArrayList<>();
      }
      if(!canTransmute(inputStack,ItemStack.EMPTY,reagent1Stack,reagent2Stack,ItemStack.EMPTY,altar)) return new ArrayList<>();
      
      List<Pair<ItemStack,String>> outputs = new ArrayList<>();
      int iterations = inputStack.getCount() / getInput().getCount();
      int consumedInput = iterations * getInput().getCount();
      
      for(int i = 0; i < iterations; i++){
         ItemStack outputStack = output.copy();
         if(MagicItemUtils.isMagic(inputStack) && MagicItemUtils.isMagic(outputStack) && player != null){
            MagicItem magicInputItem = MagicItemUtils.identifyItem(inputStack);
            MagicItem magicOutputItem = MagicItemUtils.identifyItem(outputStack);
            outputStack = magicOutputItem.addCrafter(magicOutputItem.getNewItem(),magicInputItem.getCrafter(inputStack),false,player.getServer());
         }
         
         outputs.add(new Pair<>(outputStack,outputPos));
      }
      
      if(inputStack.getCount() == consumedInput){
         inputEntity.discard();
      }else{
         inputStack.decrement(consumedInput);
         inputEntity.setStack(inputStack);
      }
      
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
      
      return outputs;
   }
   
   @Override
   public boolean canTransmute(ItemStack input1, ItemStack input2, ItemStack reagent1Input, ItemStack reagent2Input, ItemStack aequalisInput, TransmutationAltarBlockEntity altar){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      ItemStack re1 = getBargainReagent(reagent1,bargainLvl);
      ItemStack re2 = getBargainReagent(reagent2,bargainLvl);
      boolean reagent1Check = validStack(re1,reagent1Input) || validStack(re1,reagent2Input);
      boolean reagent2Check = validStack(re2,reagent1Input) || validStack(re2,reagent2Input);
      if(!reagent1Check || !reagent2Check) return false;
      boolean inputCheck = validStack(input,input1) || validStack(input,input2);
      return inputCheck;
   }
   
   @Override
   public ItemStack getViewStack(){
      if(MagicItemUtils.isMagic(output)){
         return new ItemStack(output.getItem(),1);
      }
      return output;
   }
   
   public ItemStack getInput(){
      return input;
   }
   
   public ItemStack getOutput(){
      return output;
   }
}
