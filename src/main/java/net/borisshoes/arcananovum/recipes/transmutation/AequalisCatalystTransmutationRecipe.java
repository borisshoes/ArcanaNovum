package net.borisshoes.arcananovum.recipes.transmutation;

import it.unimi.dsi.fastutil.Hash;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.AequalisScientia;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.*;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class AequalisCatalystTransmutationRecipe extends TransmutationRecipe{
   
   public AequalisCatalystTransmutationRecipe(String name){
      super(name,new ItemStack(ArcanaRegistry.STARDUST,64),new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE,64));
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
      
      ItemStack magicItemStack,catalystStack;
      ItemEntity magicItemEntity,catalystEntity;
      String outputPos;
      if(MagicItemUtils.isMagic(input1Stack) && !input1Stack.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem())){
         magicItemStack = input1Stack;
         catalystStack = input2Stack;
         magicItemEntity = input1Entity;
         catalystEntity = input2Entity;
         outputPos = "negative";
      }else if(MagicItemUtils.isMagic(input2Stack) && !input2Stack.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem())){
         magicItemStack = input2Stack;
         catalystStack = input1Stack;
         magicItemEntity = input2Entity;
         catalystEntity = input1Entity;
         outputPos = "positive";
      }else{
         return new ArrayList<>();
      }
      MagicItem magicItem = MagicItemUtils.identifyItem(magicItemStack);
      if(magicItem instanceof RunicArrow) return new ArrayList<>();
      
      List<Pair<ItemStack,String>> outputs = new ArrayList<>();
      int consumedCatas = 0;
      NbtCompound magicNbt = magicItemStack.getNbt().getCompound("arcananovum");
      NbtList catas = magicNbt.contains("catalysts") ? magicNbt.getList("catalysts", NbtElement.COMPOUND_TYPE) : new NbtList();
      
      TreeMap<ArcanaAugment, Integer> curAugments = ArcanaAugments.getAugmentsOnItem(magicItemStack);
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
               
               String aug = cata.getString("augment");
               int lvl = cata.getInt("level");
               MagicRarity rarity = MagicRarity.rarityFromInt(cata.getInt("rarity"));
               
               if(aug.equals(augment.id) && level == lvl){
                  MagicItem magicCata = MagicRarity.getAugmentCatalyst(rarity);
                  ItemStack catalyst = magicCata.addCrafter(magicCata.getNewItem(),magicItem.getCrafter(magicItemStack),false,altar.getWorld().getServer());
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
      
      magicNbt.put("catalysts",catas);
      ArcanaAugments.setAugmentsOnItem(magicItemStack,curAugments);
      
      if(aequalisEntity != null){
         int timelessLvl = ArcanaAugments.getAugmentOnItem(aequalisStack,ArcanaAugments.TIMELESS_WISDOM.id);
         boolean keep = Math.random() < 0.2*timelessLvl;
         if(!keep){
            aequalisEntity.discard();
         }
      }
      
      if(magicItemEntity != null){
         magicItemEntity.setStack(magicItemStack);
      }
      
      if(catalystEntity != null){
         if(catalystStack.getCount() == consumedCatas){
            catalystEntity.discard();
         }else{
            catalystStack.decrement(consumedCatas);
            catalystEntity.setStack(catalystStack);
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
      if(player != null){
         PLAYER_DATA.get(player).addXP(1000);
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
      boolean matrixCheck = (input1.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem()) || input2.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem()));
      boolean magicItemCheck = (MagicItemUtils.isMagic(input1) && !input1.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem())) || (MagicItemUtils.isMagic(input2) && !input2.isOf(ArcanaRegistry.CATALYTIC_MATRIX.getItem()));
      boolean arrowCheck = MagicItemUtils.identifyItem(input1) instanceof RunicArrow || MagicItemUtils.identifyItem(input2) instanceof RunicArrow;
      if(!matrixCheck || !magicItemCheck || arrowCheck) return false;
      if(!(MagicItemUtils.identifyItem(aequalisInput) instanceof AequalisScientia as)) return false;
      try{
         boolean hasAugment = ArcanaAugments.getAugmentOnItem(aequalisInput,ArcanaAugments.EQUIVALENT_EXCHANGE.id) > 0;
         if(!hasAugment) return false;
         ServerPlayerEntity player = altar.getWorld().getServer().getPlayerManager().getPlayer(UUID.fromString(as.getCrafter(aequalisInput)));
         return player != null;
      }catch(Exception e){
         return false;
      }
   }
   
   @Override
   public ItemStack getViewStack(){
      return new ItemStack(ArcanaRegistry.CATALYTIC_MATRIX.getItem(),1);
   }
   
}
