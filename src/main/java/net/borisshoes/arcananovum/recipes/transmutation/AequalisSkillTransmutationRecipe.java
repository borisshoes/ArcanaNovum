package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.AequalisScientia;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AequalisSkillTransmutationRecipe extends TransmutationRecipe{
   
   public AequalisSkillTransmutationRecipe(String id){
      super(id,new ItemStack(ArcanaRegistry.STARDUST,64),new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE,64));
   }
   
   @Override // This transmutation cannot be done by an Aequalis
   public List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayer player){
      return List.of(positiveInput,negativeInput,reagent1,reagent2,aequalisInput);
   }
   
   private Tuple<ArrayList<Tuple<ArcanaAugment,Integer>>,Integer> getCanSell(List<ArcanaAugment> item1Augments, ArcanaItem otherItem, IArcanaProfileComponent profile){
      ArrayList<Tuple<ArcanaAugment,Integer>> canSell = new ArrayList<>();
      int sellingPower = 0;
      for(ArcanaAugment i1aug : item1Augments){
         boolean linked = false;
         List<ArcanaAugment> linkedAugments = ArcanaAugments.getLinkedAugments(i1aug.id);
         for(ArcanaAugment linkedAugment : linkedAugments){
            if(linkedAugment.getArcanaItem().getId().equals(otherItem.getId())){
               linked = true;
               break;
            }
         }
         if(linked) continue;
         
         int curLvl = profile.getAugmentLevel(i1aug.id);
         if(curLvl > 0){
            canSell.add(new Tuple<>(i1aug,curLvl));
            for(int i = 1; i <= i1aug.getTiers().length; i++){
               sellingPower += i1aug.getTiers()[i-1].rarity+1;
            }
         }
      }
      Collections.shuffle(canSell);
      
      return new Tuple<>(canSell,sellingPower);
   }
   
   @Override
   public List<Tuple<ItemStack,String>> doTransmutation(ItemEntity input1Entity, ItemEntity input2Entity, ItemEntity reagent1Entity, ItemEntity reagent2Entity, ItemEntity aequalisEntity, TransmutationAltarBlockEntity altar, ServerPlayer player){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      ItemStack input1Stack = input1Entity != null ? input1Entity.getItem() : ItemStack.EMPTY;
      ItemStack input2Stack = input2Entity != null ? input2Entity.getItem() : ItemStack.EMPTY;
      ItemStack reagent1Stack = reagent1Entity != null ? reagent1Entity.getItem() : ItemStack.EMPTY;
      ItemStack reagent2Stack = reagent2Entity != null ? reagent2Entity.getItem() : ItemStack.EMPTY;
      ItemStack aequalisStack = aequalisEntity != null ? aequalisEntity.getItem() : ItemStack.EMPTY;
      if(!canTransmute(input1Stack,input2Stack,reagent1Stack,reagent2Stack,aequalisStack,altar)) return new ArrayList<>();
      ArcanaItem arcanaItem1 = ArcanaItemUtils.identifyItem(input1Stack);
      ArcanaItem arcanaItem2 = ArcanaItemUtils.identifyItem(input2Stack);
      ArcanaItem aequalis = ArcanaItemUtils.identifyItem(aequalisStack);
      
      try{
         ServerPlayer asPlayer = altar.getLevel().getServer().getPlayerList().getPlayer(AlgoUtils.getUUID(aequalis.getCrafter(aequalisStack)));
         if(asPlayer == null) return new ArrayList<>();
         
         IArcanaProfileComponent profile = ArcanaNovum.data(asPlayer);
         List<ArcanaAugment> item1Augments = ArcanaAugments.getAugmentsForItem(arcanaItem1);
         List<ArcanaAugment> item2Augments = ArcanaAugments.getAugmentsForItem(arcanaItem2);
         
         int sellingPower = 0;
         int liquidated = 0;
         int cycles = 0;
         
         while(cycles < 100){
            cycles++;
            ArrayList<Tuple<ArcanaAugment,Integer>> canBuy = new ArrayList<>();
            int cheapestBuy = Integer.MAX_VALUE;
            for(ArcanaAugment i2aug : item2Augments){
               int maxLvl = i2aug.getTiers().length;
               int curLvl = profile.getAugmentLevel(i2aug.id);
               
               boolean linked = false;
               List<ArcanaAugment> linkedAugments = ArcanaAugments.getLinkedAugments(i2aug.id);
               for(ArcanaAugment linkedAugment : linkedAugments){
                  if(linkedAugment.getArcanaItem().getId().equals(arcanaItem1.getId())){
                     linked = true;
                     break;
                  }
               }
               if(linked) continue;
               
               if(curLvl < maxLvl){
                  canBuy.add(new Tuple<>(i2aug,curLvl+1));
                  if(i2aug.getTiers()[curLvl].rarity+1 < cheapestBuy){
                     cheapestBuy = i2aug.getTiers()[curLvl].rarity+1;
                  }
               }
            }
            Collections.shuffle(canBuy);
            
            Tuple<ArrayList<Tuple<ArcanaAugment,Integer>>,Integer> canSellRet = getCanSell(item1Augments, arcanaItem2,profile);
            ArrayList<Tuple<ArcanaAugment,Integer>> canSell = canSellRet.getA();
            sellingPower = canSellRet.getB();
            
            if(cheapestBuy > sellingPower+liquidated) break;
            
            for(Tuple<ArcanaAugment, Integer> buyPair : canBuy){
               ArcanaAugment buyAug = buyPair.getA();
               int buyLvl = buyPair.getB();
               int cost = buyAug.getTiers()[buyLvl-1].rarity+1;
               if(cost > sellingPower+liquidated) continue;
               
               boolean cantSell = false;
               while(cost > liquidated){
                  Tuple<ArcanaAugment,Integer> toSell = canSell.getFirst();
                  ArcanaAugment sellAug = toSell.getA();
                  int sellLvl = toSell.getB();
                  profile.setAugmentLevel(sellAug.id,sellLvl-1);
                  liquidated += sellAug.getTiers()[sellLvl-1].rarity+1;
                  
                  canSellRet = getCanSell(item1Augments, arcanaItem2,profile);
                  canSell = canSellRet.getA();
                  if(canSell.isEmpty()){
                     cantSell = true;
                     break;
                  }
               }
               if(cantSell) continue;
               
               profile.setAugmentLevel(buyAug.id,buyLvl);
               liquidated -= buyAug.getTiers()[buyLvl-1].rarity+1;
               break;
            }
         }
         
         if(arcanaItem1.getId().equals(arcanaItem2.getId())){
            ArcanaAchievements.grant(asPlayer,ArcanaAchievements.QUESTIONABLE_EXCHANGE.id);
         }
      }catch(Exception e){
         return new ArrayList<>();
      }
      
      if(aequalisEntity != null){
         boolean timeless = ArcanaAugments.getAugmentOnItem(aequalisStack,ArcanaAugments.TIMELESS_WISDOM.id) > 0;
         int uses = ArcanaItem.getIntProperty(aequalisStack,AequalisScientia.USES_TAG);
         if(!timeless && uses <= 1){
            aequalisEntity.discard();
         }else if(!timeless){
            ArcanaItem.putProperty(aequalisStack,AequalisScientia.USES_TAG,uses-1);
            ArcanaRegistry.AEQUALIS_SCIENTIA.buildItemLore(aequalisStack, BorisLib.SERVER);
            aequalisEntity.setItem(aequalisStack);
         }
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
      
      if(player != null) ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_AEQUALIS_SCIENTIA_SKILL_TRANSMUTE));
      return new ArrayList<>();
   }
   
   @Override
   public boolean canTransmute(ItemStack input1, ItemStack input2, ItemStack reagent1Input, ItemStack reagent2Input, ItemStack aequalisInput, TransmutationAltarBlockEntity altar){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      boolean reagentCheck1 = validReagent1(reagent1Input,bargainLvl) && validReagent2(reagent2Input,bargainLvl);
      boolean reagentCheck2 = validReagent1(reagent2Input,bargainLvl) && validReagent2(reagent1Input,bargainLvl);
      if (!(reagentCheck1 || reagentCheck2)) return false;
      boolean arcanaItemCheck = (ArcanaItemUtils.isArcane(input1) && ArcanaItemUtils.isArcane(input2));
      boolean matrixCheck = !input1.is(ArcanaRegistry.CATALYTIC_MATRIX.getItem()) && !input2.is(ArcanaRegistry.CATALYTIC_MATRIX.getItem());
      if(!arcanaItemCheck || !matrixCheck) return false;
      if(!(ArcanaItemUtils.identifyItem(aequalisInput) instanceof AequalisScientia as)) return false;
      try{
         ServerPlayer player = altar.getLevel().getServer().getPlayerList().getPlayer(AlgoUtils.getUUID(as.getCrafter(aequalisInput)));
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
      return ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore().copyWithCount(1);
   }
   
}
