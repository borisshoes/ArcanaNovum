package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class PermutationTransmutationRecipe extends TransmutationRecipe{
   
   private final BiFunction<ItemStack,MinecraftServer, ItemStack> permutationFunction;
   private final ItemStack input;
   private final Component outputDescription;
   
   public PermutationTransmutationRecipe(String id, ItemStack input, ItemStack reagent1, ItemStack reagent2, BiFunction<ItemStack,MinecraftServer, ItemStack> permutationFunction, Component outputDescription){
      super(id, reagent1, reagent2);
      this.permutationFunction = permutationFunction;
      this.input = input;
      this.outputDescription = outputDescription;
   }
   
   @Override
   public List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayer player){
      List<ItemStack> returnItems = new ArrayList<>();
      int iterations = positiveInput.getCount() / getInput().getCount();
      for(int i = 0; i < iterations; i++){
         ItemStack outputStack = permutationFunction.apply(positiveInput,player.level().getServer()).copy();
         if(ArcanaItemUtils.isArcane(positiveInput) && ArcanaItemUtils.isArcane(outputStack)){
            ArcanaItem arcanaInputItem = ArcanaItemUtils.identifyItem(positiveInput);
            ArcanaItem arcanaOutputItem = ArcanaItemUtils.identifyItem(outputStack);
            if(!arcanaInputItem.getId().equals(arcanaOutputItem.getId())){
               outputStack = arcanaOutputItem.addCrafter(arcanaOutputItem.getNewItem(),player.getStringUUID(),0, player.level().getServer());
            }
         }
         returnItems.add(outputStack);
      }
      returnItems.add(aequalisInput);
      return returnItems;
   }
   
   @Override
   public List<Tuple<ItemStack, String>> doTransmutation(ItemEntity input1Entity, ItemEntity input2Entity, ItemEntity reagent1Entity, ItemEntity reagent2Entity, ItemEntity aequalisEntity, TransmutationAltarBlockEntity altar, ServerPlayer player){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN);
      ItemStack reagent1Stack = reagent1Entity != null ? reagent1Entity.getItem() : ItemStack.EMPTY;
      ItemStack reagent2Stack = reagent2Entity != null ? reagent2Entity.getItem() : ItemStack.EMPTY;
      ItemStack inputStack;
      ItemEntity inputEntity;
      String outputPos;
      if(input1Entity != null && validStack(input,input1Entity.getItem())){
         inputStack = input1Entity.getItem();
         inputEntity = input1Entity;
         outputPos = "negative";
      }else if(input2Entity != null && validStack(input,input2Entity.getItem())){
         inputStack = input2Entity.getItem();
         inputEntity = input2Entity;
         outputPos = "positive";
      }else{
         return new ArrayList<>();
      }
      if(!canTransmute(inputStack, ItemStack.EMPTY,reagent1Stack,reagent2Stack, ItemStack.EMPTY,altar)) return new ArrayList<>();
      
      
      
      List<Tuple<ItemStack,String>> outputs = new ArrayList<>();
      int iterations = inputStack.getCount() / getInput().getCount();
      int consumedInput = iterations * getInput().getCount();
      
      for(int i = 0; i < iterations; i++){
         ItemStack outputStack = permutationFunction.apply(inputStack,altar.getLevel().getServer()).copy();
         if(ArcanaItemUtils.isArcane(inputStack) && ArcanaItemUtils.isArcane(outputStack)){
            ArcanaItem arcanaInputItem = ArcanaItemUtils.identifyItem(inputStack);
            ArcanaItem arcanaOutputItem = ArcanaItemUtils.identifyItem(outputStack);
            if(!arcanaInputItem.getId().equals(arcanaOutputItem.getId())){
               outputStack = arcanaOutputItem.addCrafter(arcanaOutputItem.getNewItem(),arcanaInputItem.getCrafter(inputStack),0, BorisLib.SERVER);
            }
            
         }
         
         outputs.add(new Tuple<>(outputStack,outputPos));
      }
      
      if(inputStack.getCount() == consumedInput){
         inputEntity.discard();
      }else{
         inputStack.shrink(consumedInput);
         inputEntity.setItem(inputStack);
      }
      
      boolean m11 = validReagent1(reagent1Stack,bargainLvl), m22 = validReagent2(reagent2Stack,bargainLvl), m12 = validReagent1(reagent2Stack,bargainLvl), m21 = validReagent2(reagent1Stack,bargainLvl);
      boolean straight = m11 && m22;
      boolean cross = !straight && m12 && m21;
      if (!straight && !cross) return new ArrayList<>(); // should be impossible
      
      ItemStack reagent1 = straight ? getComputedReagent1(reagent1Stack,bargainLvl) : getComputedReagent2(reagent1Stack,bargainLvl);
      ItemStack reagent2 = straight ? getComputedReagent2(reagent2Stack,bargainLvl) : getComputedReagent1(reagent2Stack,bargainLvl);
      
      if (reagent1Entity != null) {
         int take = reagent1.isEmpty() ? 0 : reagent1.getCount();
         if(take > 0){
            if(reagent1Stack.getCount() == take){
               reagent1Entity.discard();
            }else{
               reagent1Stack.shrink(take);
               reagent1Entity.setItem(reagent1Stack);
            }
         }
      }
      
      if (reagent2Entity != null) {
         int take = reagent2.isEmpty() ? 0 : reagent2.getCount();
         if(take > 0){
            if(reagent2Stack.getCount() == take){
               reagent2Entity.discard();
            }else{
               reagent2Stack.shrink(take);
               reagent2Entity.setItem(reagent2Stack);
            }
         }
      }
      
      return outputs;
   }
   
   @Override
   public boolean canTransmute(ItemStack input1, ItemStack input2, ItemStack reagent1Input, ItemStack reagent2Input, ItemStack aequalisInput, TransmutationAltarBlockEntity altar){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN);
      boolean reagentCheck1 = validReagent1(reagent1Input,bargainLvl) && validReagent2(reagent2Input,bargainLvl);
      boolean reagentCheck2 = validReagent1(reagent2Input,bargainLvl) && validReagent2(reagent1Input,bargainLvl);
      if (!(reagentCheck1 || reagentCheck2)) return false;
      
      ItemStack inputStack;
      if(validStack(input,input1)){
         inputStack = input1;
      }else if(validStack(input,input2)){
         inputStack = input2;
      }else{
         return false;
      }
      
      if(ArcanaItemUtils.isArcane(inputStack) && altar.getLevel() instanceof ServerLevel serverWorld){
         ArcanaItem arcanaInputItem = ArcanaItemUtils.identifyItem(inputStack);
         ServerPlayer player = serverWorld.getServer().getPlayerList().getPlayer(AlgoUtils.getUUID(arcanaInputItem.getCrafter(inputStack)));
         return player == null || ArcanaNovum.data(player).hasResearched(arcanaInputItem);
      }
      
      return true;
   }
   
   @Override
   public ItemStack getViewStack(){
      if(ArcanaItemUtils.isArcane(input)){
         return MinecraftUtils.removeLore(input.copyWithCount(1));
      }
      return MinecraftUtils.removeLore(input.copy());
   }
   
   public ItemStack getInput(){
      return input;
   }
   
   public Component getOutputDescription(){
      return outputDescription;
   }
}
