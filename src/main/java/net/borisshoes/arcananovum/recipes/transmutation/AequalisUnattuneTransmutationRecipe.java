package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.AequalisScientia;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class AequalisUnattuneTransmutationRecipe extends TransmutationRecipe{
   protected AequalisUnattuneTransmutationRecipe(String name){
      super(name,new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE,48),new ItemStack(Items.AMETHYST_SHARD,48));
   }
   
   @Override
   public List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayerEntity player){
      List<ItemStack> returnItems = new ArrayList<>();
      if(ArcanaItemUtils.identifyItem(positiveInput) instanceof AequalisScientia aeq){
         ArcanaItem.putProperty(positiveInput, AequalisScientia.TRANSMUTATION_TAG,"");
         aeq.buildItemLore(positiveInput,player.getServer());
      }
      returnItems.add(positiveInput);
      returnItems.add(aequalisInput);
      return returnItems;
   }
   
   @Override
   public List<Pair<ItemStack, String>> doTransmutation(ItemEntity input1Entity, ItemEntity input2Entity, ItemEntity reagent1Entity, ItemEntity reagent2Entity, ItemEntity aequalisEntity, TransmutationAltarBlockEntity altar, ServerPlayerEntity player){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      ItemStack re1 = getBargainReagent(reagent1,bargainLvl);
      ItemStack re2 = getBargainReagent(reagent2,bargainLvl);
      ItemStack reagent1Stack = reagent1Entity != null ? reagent1Entity.getStack() : ItemStack.EMPTY;
      ItemStack reagent2Stack = reagent2Entity != null ? reagent2Entity.getStack() : ItemStack.EMPTY;
      ItemStack inputStack;
      ItemEntity inputEntity;
      String outputPos;
      if(input1Entity != null && validStack(ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItem(),input1Entity.getStack())){
         inputStack = input1Entity.getStack();
         inputEntity = input1Entity;
         outputPos = "negative";
      }else if(input2Entity != null && validStack(ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItem(),input2Entity.getStack())){
         inputStack = input2Entity.getStack();
         inputEntity = input2Entity;
         outputPos = "positive";
      }else{
         return new ArrayList<>();
      }
      if(!canTransmute(inputStack,ItemStack.EMPTY,reagent1Stack,reagent2Stack,ItemStack.EMPTY,altar)) return new ArrayList<>();
      
      List<Pair<ItemStack,String>> outputs = new ArrayList<>();
      int consumedInput = 1;
      
      if(ArcanaItemUtils.identifyItem(inputStack) instanceof AequalisScientia aeq){
         ArcanaItem.putProperty(inputStack, AequalisScientia.TRANSMUTATION_TAG,"");
         aeq.buildItemLore(inputStack,altar.getWorld().getServer());
      }
      
      outputs.add(new Pair<>(inputStack,outputPos));
      
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
      
      ItemStack inputStack;
      if(validStack(ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItem(),input1)){
         inputStack = input1;
      }else if(validStack(ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItem(),input2)){
         inputStack = input2;
      }else{
         return false;
      }
      
      boolean hasAugmentAndAttuned = ArcanaAugments.getAugmentOnItem(inputStack,ArcanaAugments.IMPERMANENT_PERMUTATION.id) > 0 && !ArcanaItem.getStringProperty(inputStack,AequalisScientia.TRANSMUTATION_TAG).isEmpty();;
      if(!hasAugmentAndAttuned) return false;
      
      if(ArcanaItemUtils.isArcane(inputStack) && altar.getWorld() instanceof ServerWorld serverWorld){
         ArcanaItem arcanaInputItem = ArcanaItemUtils.identifyItem(inputStack);
         ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(MiscUtils.getUUID(arcanaInputItem.getCrafter(inputStack)));
         return player == null || (ArcanaNovum.data(player).hasResearched(arcanaInputItem) && ArcanaNovum.data(player).getAugmentLevel(ArcanaAugments.IMPERMANENT_PERMUTATION.id) > 0);
      }
      
      return true;
   }
   
   @Override
   public ItemStack getViewStack(){
      return MiscUtils.removeLore(ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore().copyWithCount(1));
   }
}
