package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

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
   
   public abstract List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayer player);
   
   public abstract List<Tuple<ItemStack,String>> doTransmutation(ItemEntity positiveInput, ItemEntity negativeInput, ItemEntity reagent1, ItemEntity reagent2, ItemEntity aequalisInput, TransmutationAltarBlockEntity altar, ServerPlayer player);
   
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
      if(ArcanaItemUtils.isArcane(recipeStack)){
         ArcanaItem stackItem = ArcanaItemUtils.identifyItem(input);
         return stackItem != null && stackItem.getId().equals(ArcanaItemUtils.identifyItem(recipeStack).getId());
      }
      
      // TODO maybe have optional item predicate check?
      if(!recipeStack.is(input.getItem())) return false;
      int reqCount = recipeStack.getCount();
      return input.getCount() >= reqCount;
   }
   
   public ItemStack getBargainReagent(ItemStack stack, int bargainlvl){
      final double[] bargainMod = new double[]{1,1.5,1.4,1.3,1.2,1.1};
      int count = stack.getCount();
      if(ArcanaItemUtils.isArcane(stack)) return stack.copy();
      count = (int) Math.min(stack.getMaxStackSize(),count * bargainMod[bargainlvl]);
      return stack.copyWithCount(count);
   }
   
   public ItemStack getAequalisReagent(ItemStack stack){
      int count = stack.getCount();
      if(ArcanaItemUtils.isArcane(stack)) return stack.copy();
      count = Mth.ceil(Mth.clamp(count*0.5,1,stack.getMaxStackSize()));
      return stack.copyWithCount(count);
   }
}
