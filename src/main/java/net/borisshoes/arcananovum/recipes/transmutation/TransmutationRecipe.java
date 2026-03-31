package net.borisshoes.arcananovum.recipes.transmutation;

import com.mojang.datafixers.util.Either;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class TransmutationRecipe {
   
   protected final List<Either<Item, TagKey<Item>>> reagent1;
   protected final int reagent1Count;
   protected final List<Either<Item, TagKey<Item>>> reagent2;
   protected final int reagent2Count;
   protected final String id;
   
   protected TransmutationRecipe(String id, List<Either<Item, TagKey<Item>>> reagent1, int reagent1Count, List<Either<Item, TagKey<Item>>> reagent2, int reagent2Count){
      this.id = id;
      this.reagent1 = reagent1;
      this.reagent1Count = reagent1Count;
      this.reagent2 = reagent2;
      this.reagent2Count = reagent2Count;
   }
   
   protected TransmutationRecipe(String id, ItemStack reagent1, ItemStack reagent2){
      this.id = id;
      this.reagent1 = List.of(Either.left(reagent1.getItem()));
      this.reagent1Count = reagent1.getCount();
      this.reagent2 = List.of(Either.left(reagent2.getItem()));
      this.reagent2Count = reagent2.getCount();
   }
   
   public abstract List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayer player);
   
   public abstract List<Tuple<ItemStack, String>> doTransmutation(ItemEntity positiveInput, ItemEntity negativeInput, ItemEntity reagent1, ItemEntity reagent2, ItemEntity aequalisInput, TransmutationAltarBlockEntity altar, ServerPlayer player);
   
   public abstract boolean canTransmute(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, TransmutationAltarBlockEntity altar);
   
   public abstract ItemStack getViewStack();
   
   public int getReagent1Count(){
      return reagent1Count;
   }
   
   public int getReagent2Count(){
      return reagent2Count;
   }
   
   public String getId(){
      return this.id;
   }
   
   public MutableComponent getName(){
      return Component.translatable("transmutation.arcananovum." + id);
   }
   
   public boolean validStack(Predicate<ItemStack> recipePred, ItemStack input){
      return recipePred.test(input);
   }
   
   public boolean validStack(ItemStack recipeStack, ItemStack input){
      if(recipeStack.isEmpty()) return true;
      if(ArcanaItemUtils.isArcane(recipeStack)){
         ArcanaItem stackItem = ArcanaItemUtils.identifyItem(input);
         return stackItem != null && stackItem.getId().equals(ArcanaItemUtils.identifyItem(recipeStack).getId());
      }
      
      if(!recipeStack.is(input.getItem())) return false;
      int reqCount = recipeStack.getCount();
      return input.getCount() >= reqCount;
   }
   
   
   public ItemStack getExampleReagent1(){
      return getExampleReagent(this.reagent1, reagent1Count);
   }
   
   public ItemStack getExampleReagent2(){
      return getExampleReagent(this.reagent2, reagent2Count);
   }
   
   public ItemStack getComputedReagent1(ItemStack stack, int bargainLvl){
      return getComputedReagent(stack, bargainLvl, this.reagent1, reagent1Count);
   }
   
   public ItemStack getComputedReagent2(ItemStack stack, int bargainLvl){
      return getComputedReagent(stack, bargainLvl, this.reagent2, reagent2Count);
   }
   
   public boolean validReagent1(ItemStack stack, int bargainLvl){
      ItemStack comp = getComputedReagent1(stack, bargainLvl);
      if(comp == null) return false;
      return stack.getCount() >= comp.getCount();
   }
   
   public boolean validReagent2(ItemStack stack, int bargainLvl){
      ItemStack comp = getComputedReagent2(stack, bargainLvl);
      if(comp == null) return false;
      return stack.getCount() >= comp.getCount();
   }
   
   private ItemStack getExampleReagent(List<Either<Item, TagKey<Item>>> reagent, int count){
      Item first = null;
      Item second = null;
      for(Either<Item, TagKey<Item>> either : reagent){
         if(either.left().isPresent()){
            first = either.left().get();
            break;
         }else if(either.right().isPresent()){
            TagKey<Item> itemTag = either.right().get();
            Optional<HolderSet.Named<Item>> opt = BuiltInRegistries.ITEM.get(itemTag);
            if(opt.isPresent()){
               for(Holder<Item> holder : opt.get()){
                  second = holder.value();
                  break;
               }
            }
         }
      }
      if(first != null) return new ItemStack(first, count);
      if(second != null) return new ItemStack(second, count);
      ArcanaNovum.log(2, "Transmutation Recipe " + this.id + " has invalid reagent item");
      return null;
   }
   
   private ItemStack getComputedReagent(ItemStack stack, int bargainLvl, List<Either<Item, TagKey<Item>>> reagent, int count){
      final double[] bargainMod = new double[]{1.0, 2.0, 1.8, 1.6, 1.4, 1.2};
      final double costMod = bargainLvl < 0 ? 0.5 : bargainMod[bargainLvl];
      boolean matches = reagent.stream().anyMatch(e ->
            ((e.left().isPresent() && stack.is(e.left().get())) || (e.right().isPresent() && stack.is(e.right().get()))));
      if(!matches) return null;
      return stack.copyWithCount(Mth.ceil(Mth.clamp(count * costMod, 1, stack.getMaxStackSize())));
   }
}
