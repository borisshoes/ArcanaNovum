package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.List;

public abstract class TransmutationRecipe {
   
   protected final ItemStack reagent1;
   protected final ItemStack reagent2;
   protected final String name;
   
   protected TransmutationRecipe(String name, ItemStack reagent1, ItemStack reagent2){
      this.name = name;
      this.reagent1 = reagent1;
      this.reagent2 = reagent2;
   }
   
   public abstract List<Pair<ItemStack,String>> doTransmutation(ItemEntity positiveInput, ItemEntity negativeInput, ItemEntity reagent1, ItemEntity reagent2, ItemEntity aequalisInput, TransmutationAltarBlockEntity altar, ServerPlayerEntity player);
   
   public abstract boolean canTransmute(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, TransmutationAltarBlockEntity altar);
   
   public abstract ItemStack getViewStack();
   
   public ItemStack getReagent1(){
      return reagent1;
   }
   
   public ItemStack getReagent2(){
      return reagent2;
   }
   
   public String getName(){
      return name;
   }
   
   public boolean validStack(ItemStack recipeStack, ItemStack input){
      if(recipeStack.isEmpty()) return true;
      if(MagicItemUtils.isMagic(recipeStack)){
         MagicItem stackItem = MagicItemUtils.identifyItem(input);
         return stackItem != null && stackItem.getId().equals(MagicItemUtils.identifyItem(recipeStack).getId());
      }
      
      if(recipeStack.hasNbt()){
         if(!recipeStack.isOf(input.getItem())) return false;
         if(input.getCount() < recipeStack.getCount()) return false;
         if(!input.hasNbt()) return false;
         return MagicItemIngredient.validNbt(input.getNbt(),recipeStack.getNbt());
      }else{
         if(!recipeStack.isOf(input.getItem())) return false;
         int reqCount = recipeStack.getCount();
         return input.getCount() >= reqCount;
      }
   }
   
   protected ItemStack getBargainReagent(ItemStack stack, int bargainlvl){
      final double[] bargainMod = new double[]{1,1.5,1.4,1.3,1.2,1.1};
      int count = stack.getCount();
      if(MagicItemUtils.isMagic(stack)) return stack.copy();
      count = (int) Math.min(stack.getMaxCount(),count * bargainMod[bargainlvl]);
      return stack.copyWithCount(count);
   }
}
