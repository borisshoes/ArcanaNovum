package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.AequalisScientia;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class AequalisSkillTransmutationRecipe extends TransmutationRecipe{
   
   public AequalisSkillTransmutationRecipe(String name){
      super(name,new ItemStack(ArcanaRegistry.STARDUST,64),new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE,64));
   }
   
   private Pair<ArrayList<Pair<ArcanaAugment,Integer>>,Integer> getCanSell(List<ArcanaAugment> item1Augments, MagicItem otherItem, IArcanaProfileComponent profile){
      ArrayList<Pair<ArcanaAugment,Integer>> canSell = new ArrayList<>();
      int sellingPower = 0;
      for(ArcanaAugment i1aug : item1Augments){
         boolean linked = false;
         List<ArcanaAugment> linkedAugments = ArcanaAugments.getLinkedAugments(i1aug.id);
         for(ArcanaAugment linkedAugment : linkedAugments){
            if(linkedAugment.getMagicItem().getId().equals(otherItem.getId())){
               linked = true;
               break;
            }
         }
         if(linked) continue;
         
         int curLvl = profile.getAugmentLevel(i1aug.id);
         if(curLvl > 0){
            canSell.add(new Pair<>(i1aug,curLvl));
            for(int i = 1; i <= i1aug.getTiers().length; i++){
               sellingPower += i1aug.getTiers()[i-1].rarity+1;
            }
         }
      }
      Collections.shuffle(canSell);
      
      return new Pair<>(canSell,sellingPower);
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
      MagicItem magicItem1 = MagicItemUtils.identifyItem(input1Stack);
      MagicItem magicItem2 = MagicItemUtils.identifyItem(input2Stack);
      MagicItem aequalis = MagicItemUtils.identifyItem(aequalisStack);
      
      try{
         ServerPlayerEntity asPlayer = altar.getWorld().getServer().getPlayerManager().getPlayer(UUID.fromString(aequalis.getCrafter(aequalisStack)));
         if(asPlayer == null) return new ArrayList<>();
         
         IArcanaProfileComponent profile = PLAYER_DATA.get(asPlayer);
         List<ArcanaAugment> item1Augments = ArcanaAugments.getAugmentsForItem(magicItem1);
         List<ArcanaAugment> item2Augments = ArcanaAugments.getAugmentsForItem(magicItem2);
         
         int cheapestBuy = Integer.MAX_VALUE;
         int sellingPower = 0;
         int liquidated = 0;
         
         while(true){
            ArrayList<Pair<ArcanaAugment,Integer>> canBuy = new ArrayList<>();
            for(ArcanaAugment i2aug : item2Augments){
               int maxLvl = i2aug.getTiers().length;
               int curLvl = profile.getAugmentLevel(i2aug.id);
               
               boolean linked = false;
               List<ArcanaAugment> linkedAugments = ArcanaAugments.getLinkedAugments(i2aug.id);
               for(ArcanaAugment linkedAugment : linkedAugments){
                  if(linkedAugment.getMagicItem().getId().equals(magicItem1.getId())){
                     linked = true;
                     break;
                  }
               }
               if(linked) continue;
               
               if(curLvl < maxLvl){
                  canBuy.add(new Pair<>(i2aug,curLvl+1));
                  if(i2aug.getTiers()[curLvl].rarity+1 < cheapestBuy){
                     cheapestBuy = i2aug.getTiers()[curLvl].rarity+1;
                  }
               }
            }
            Collections.shuffle(canBuy);
            
            Pair<ArrayList<Pair<ArcanaAugment,Integer>>,Integer> canSellRet = getCanSell(item1Augments,magicItem2,profile);
            ArrayList<Pair<ArcanaAugment,Integer>> canSell = canSellRet.getLeft();
            sellingPower = canSellRet.getRight();
            
            if(cheapestBuy > sellingPower+liquidated) break;
            
            for(Pair<ArcanaAugment, Integer> buyPair : canBuy){
               ArcanaAugment buyAug = buyPair.getLeft();
               int buyLvl = buyPair.getRight();
               int cost = buyAug.getTiers()[buyLvl-1].rarity+1;
               if(cost > sellingPower+liquidated) continue;
               
               while(cost > liquidated){
                  Pair<ArcanaAugment,Integer> toSell = canSell.get(0);
                  ArcanaAugment sellAug = toSell.getLeft();
                  int sellLvl = toSell.getRight();
                  profile.setAugmentLevel(sellAug.id,sellLvl-1);
                  liquidated += sellAug.getTiers()[sellLvl-1].rarity+1;
                  
                  canSellRet = getCanSell(item1Augments,magicItem2,profile);
                  canSell = canSellRet.getLeft();
               }
               
               profile.setAugmentLevel(buyAug.id,buyLvl);
               liquidated -= buyAug.getTiers()[buyLvl-1].rarity+1;
               break;
            }
         }
         
         if(magicItem1.getId().equals(magicItem2.getId())){
            ArcanaAchievements.grant(asPlayer,ArcanaAchievements.QUESTIONABLE_EXCHANGE.id);
         }
      }catch(Exception e){
         return new ArrayList<>();
      }
      
      if(aequalisEntity != null){
         int timelessLvl = ArcanaAugments.getAugmentOnItem(aequalisStack,ArcanaAugments.TIMELESS_WISDOM.id);
         boolean keep = Math.random() < 0.2*timelessLvl;
         if(!keep){
            aequalisEntity.discard();
         }
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
      
      PLAYER_DATA.get(player).addXP(1000);
      return new ArrayList<>();
   }
   
   @Override
   public boolean canTransmute(ItemStack input1, ItemStack input2, ItemStack reagent1Input, ItemStack reagent2Input, ItemStack aequalisInput, TransmutationAltarBlockEntity altar){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      ItemStack re1 = getBargainReagent(reagent1,bargainLvl);
      ItemStack re2 = getBargainReagent(reagent2,bargainLvl);
      boolean reagent1Check = validStack(re1,reagent1Input) || validStack(re1,reagent2Input);
      boolean reagent2Check = validStack(re2,reagent1Input) || validStack(re2,reagent2Input);
      if(!reagent1Check || !reagent2Check) return false;
      boolean magicItemCheck = (MagicItemUtils.isMagic(input1) && MagicItemUtils.isMagic(input2));
      boolean matrixCheck = !input1.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem()) && !input2.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem());
      if(!magicItemCheck || !matrixCheck) return false;
      if(!(MagicItemUtils.identifyItem(aequalisInput) instanceof AequalisScientia as)) return false;
      try{
         ServerPlayerEntity player = altar.getWorld().getServer().getPlayerManager().getPlayer(UUID.fromString(as.getCrafter(aequalisInput)));
         return player != null;
      }catch(Exception e){
         return false;
      }
   }
   
   @Override
   public ItemStack getViewStack(){
      return ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItem().copy();
   }
   
}
