package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.items.AequalisScientia;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.*;

public class AequalisCatalystTransmutationRecipe extends TransmutationRecipe{
   
   public AequalisCatalystTransmutationRecipe(String name){
      super(name,new ItemStack(ArcanaRegistry.STARDUST,64),new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE,64));
   }
   
   @Override // This transmutation cannot be done by an Aequalis
   public List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayerEntity player){
      return List.of(positiveInput,negativeInput,reagent1,reagent2,aequalisInput);
   }
   
   @Override
   public List<Pair<ItemStack,String>> doTransmutation(ItemEntity input1Entity, ItemEntity input2Entity, ItemEntity reagent1Entity, ItemEntity reagent2Entity, ItemEntity aequalisEntity, TransmutationAltarBlockEntity altar, ServerPlayerEntity player){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      ItemStack re1 = getBargainReagent(reagent1,bargainLvl);
      ItemStack re2 = getBargainReagent(reagent2,bargainLvl);
      ItemStack input1Stack = input1Entity != null ? input1Entity.getStack() : ItemStack.EMPTY;
      ItemStack input2Stack = input2Entity != null ? input2Entity.getStack() : ItemStack.EMPTY;
      ItemStack reagent1Stack = reagent1Entity != null ? reagent1Entity.getStack() : ItemStack.EMPTY;
      ItemStack reagent2Stack = reagent2Entity != null ? reagent2Entity.getStack() : ItemStack.EMPTY;
      ItemStack aequalisStack = aequalisEntity != null ? aequalisEntity.getStack() : ItemStack.EMPTY;
      if(!canTransmute(input1Stack,input2Stack,reagent1Stack,reagent2Stack,aequalisStack,altar)) return new ArrayList<>();
      
      ItemStack arcanaItemStack,catalystStack;
      ItemEntity arcanaItemEntity,catalystEntity;
      String outputPos;
      if(ArcanaItemUtils.isArcane(input1Stack) && !input1Stack.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem())){
         arcanaItemStack = input1Stack;
         catalystStack = input2Stack;
         arcanaItemEntity = input1Entity;
         catalystEntity = input2Entity;
         outputPos = "negative";
      }else if(ArcanaItemUtils.isArcane(input2Stack) && !input2Stack.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem())){
         arcanaItemStack = input2Stack;
         catalystStack = input1Stack;
         arcanaItemEntity = input2Entity;
         catalystEntity = input1Entity;
         outputPos = "positive";
      }else{
         return new ArrayList<>();
      }
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(arcanaItemStack);
      if(arcanaItem instanceof RunicArrow) return new ArrayList<>();
      
      List<Pair<ItemStack,String>> outputs = new ArrayList<>();
      int consumedCatas = 0;
      NbtList catas = ArcanaItem.getListProperty(arcanaItemStack, ArcanaItem.CATALYSTS_TAG);
      
      TreeMap<ArcanaAugment, Integer> curAugments = ArcanaAugments.getAugmentsOnItem(arcanaItemStack);
      if(curAugments == null) return new ArrayList<>();
      
      while(consumedCatas < catalystStack.getCount()){
         ArrayList<Pair<ArcanaAugment,Integer>> options = new ArrayList<>();
         ArrayList<ArcanaAugment> augTypes = new ArrayList<>(curAugments.keySet().stream().toList());
         Collections.shuffle(augTypes);
         for(ArcanaAugment augType : augTypes){
            options.add(new Pair<>(augType,curAugments.get(augType)));
         }
         
         boolean cataFound = false;
         for(Pair<ArcanaAugment, Integer> option : options){
            ArcanaAugment augment = option.getLeft();
            int level = option.getRight();
            
            Iterator<NbtElement> iter = catas.iterator();
            while(iter.hasNext()){
               NbtCompound cata = (NbtCompound) iter.next();
               
               String aug = cata.getString("augment", "");
               int lvl = cata.getInt("level", 0);
               ArcanaRarity rarity = ArcanaRarity.rarityFromInt(cata.getInt("rarity", 0));
               
               if(aug.equals(augment.id) && level == lvl){
                  ArcanaItem arcanaCata = ArcanaRarity.getAugmentCatalyst(rarity);
                  ItemStack catalyst = arcanaCata.addCrafter(arcanaCata.getNewItem(), arcanaItem.getCrafter(arcanaItemStack),0,altar.getWorld().getServer());
                  outputs.add(new Pair<>(catalyst,outputPos));
                  consumedCatas++;
                  cataFound = true;
                  if(lvl-1 <= 0){
                     curAugments.remove(augment);
                  }else{
                     curAugments.put(augment,lvl-1);
                  }
                  iter.remove();
                  break;
               }
            }
            if(cataFound) break;
         }
         if(!cataFound) break;
      }
      
      ArcanaItem.putProperty(arcanaItemStack, ArcanaItem.CATALYSTS_TAG,catas);
      ArcanaAugments.setAugmentsOnItem(arcanaItemStack,curAugments);
      
      if(aequalisEntity != null){
         boolean timeless = ArcanaAugments.getAugmentOnItem(aequalisStack,ArcanaAugments.TIMELESS_WISDOM.id) > 0;
         int uses = ArcanaItem.getIntProperty(aequalisStack,AequalisScientia.USES_TAG);
         if(!timeless && uses <= 1){
            aequalisEntity.discard();
         }else if(!timeless){
            ArcanaItem.putProperty(aequalisStack,AequalisScientia.USES_TAG,uses-1);
            ArcanaRegistry.AEQUALIS_SCIENTIA.buildItemLore(aequalisStack, BorisLib.SERVER);
            aequalisEntity.setStack(aequalisStack);
         }
      }
      
      if(arcanaItemEntity != null){
         arcanaItemEntity.setStack(arcanaItemStack);
      }
      
      if(catalystEntity != null){
         if(catalystStack.getCount() == consumedCatas){
            catalystEntity.discard();
         }else{
            catalystStack.decrement(consumedCatas);
            catalystEntity.setStack(catalystStack);
         }
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
      if(player != null){
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.AEQUALIS_SCIENTIA_CATALYST_TRANSMUTE));
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
      boolean matrixCheck = (input1.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem()) || input2.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem()));
      boolean arcanaItemCheck = (ArcanaItemUtils.isArcane(input1) && !input1.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem())) || (ArcanaItemUtils.isArcane(input2) && !input2.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem()));
      boolean arrowCheck = ArcanaItemUtils.identifyItem(input1) instanceof RunicArrow || ArcanaItemUtils.identifyItem(input2) instanceof RunicArrow;
      if(!matrixCheck || !arcanaItemCheck || arrowCheck) return false;
      if(!(ArcanaItemUtils.identifyItem(aequalisInput) instanceof AequalisScientia as)) return false;
      try{
         boolean hasAugment = ArcanaAugments.getAugmentOnItem(aequalisInput,ArcanaAugments.EQUIVALENT_EXCHANGE.id) > 0;
         if(!hasAugment) return false;
         ServerPlayerEntity player = altar.getWorld().getServer().getPlayerManager().getPlayer(AlgoUtils.getUUID(as.getCrafter(aequalisInput)));
         if(player != null){
            return ArcanaNovum.data(player).hasResearched(ArcanaRegistry.AEQUALIS_SCIENTIA);
         }else{
            return false;
         }
      }catch(Exception e){
         return false;
      }
   }
   
   @Override
   public ItemStack getViewStack(){
      return ArcanaRegistry.CATALYTIC_MATRIX.getPrefItemNoLore().copyWithCount(1);
   }
   
}
