package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
   public List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayerEntity player){
      List<ItemStack> returnItems = new ArrayList<>();
      int iterations = positiveInput.getCount() / getInput().getCount();
      for(int i = 0; i < iterations; i++){
         ItemStack outputStack = output.copy();
         if(ArcanaItemUtils.isArcane(outputStack)){
            ArcanaItem arcanaOutputItem = ArcanaItemUtils.identifyItem(outputStack);
            outputStack = arcanaOutputItem.addCrafter(arcanaOutputItem.getNewItem(),player.getUuidAsString(),0,player.getServer());
         }
         returnItems.add(outputStack);
      }
      returnItems.add(aequalisInput);
      return returnItems;
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
         if(ArcanaItemUtils.isArcane(outputStack)){
            ArcanaItem arcanaOutputItem = ArcanaItemUtils.identifyItem(outputStack);
            outputStack = arcanaOutputItem.addCrafter(arcanaOutputItem.getNewItem(),player == null ? null : player.getUuidAsString(),0, BorisLib.SERVER);
         }
         
         outputs.add(new Pair<>(outputStack,outputPos));
      }
      
      if(inputStack.getCount() == consumedInput){
         inputEntity.discard();
      }else{
         inputStack.decrement(consumedInput);
         inputEntity.setStack(inputStack);
      }
      
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
      
      return outputs;
   }
   
   @Override
   public boolean canTransmute(ItemStack input1, ItemStack input2, ItemStack reagent1Input, ItemStack reagent2Input, ItemStack aequalisInput, TransmutationAltarBlockEntity altar){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      ItemStack re1 = getBargainReagent(reagent1,bargainLvl);
      ItemStack re2 = getBargainReagent(reagent2,bargainLvl);
      boolean reagentCheck1 = validStack(re1, reagent1Input) && validStack(re2, reagent2Input);
      boolean reagentCheck2 = validStack(re1, reagent2Input) && validStack(re2, reagent1Input);
      if (!(reagentCheck1 || reagentCheck2)) return false;

      ItemStack inputStack;
      if(validStack(input,input1)){
         inputStack = input1;
      }else if(validStack(input,input2)){
         inputStack = input2;
      }else{
         return false;
      }
      
      if(ArcanaItemUtils.isArcane(inputStack) && ArcanaItemUtils.isArcane(output) && altar.getWorld() instanceof ServerWorld serverWorld){
         ArcanaItem arcanaInputItem = ArcanaItemUtils.identifyItem(inputStack);
         ArcanaItem arcanaOutputItem = ArcanaItemUtils.identifyItem(output);
         ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(AlgoUtils.getUUID(arcanaInputItem.getCrafter(inputStack)));
         return player == null || ArcanaNovum.data(player).hasResearched(arcanaOutputItem);
      }
      
      return true;
   }
   
   @Override
   public ItemStack getViewStack(){
      if(ArcanaItemUtils.isArcane(output)){
         return MinecraftUtils.removeLore(output.copyWithCount(1));
      }
      return MinecraftUtils.removeLore(output.copy());
   }
   
   public ItemStack getInput(){
      return input;
   }
   
   public ItemStack getOutput(){
      return output;
   }
}
