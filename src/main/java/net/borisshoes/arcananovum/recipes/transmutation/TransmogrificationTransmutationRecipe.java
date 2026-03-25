package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.catalysts.TransmogrificationCatalyst;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class TransmogrificationTransmutationRecipe extends TransmutationRecipe{
   
   public TransmogrificationTransmutationRecipe(){
      super("item_transmogrification",new ItemStack(Items.END_CRYSTAL,1),new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE,8));
   }
   
   @Override
   public List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayer player){
      List<ItemStack> returnItems = new ArrayList<>();
      ArcanaSkin cataSkin = ArcanaSkin.getSkinFromString(ArcanaItem.getStringProperty(negativeInput, TransmogrificationCatalyst.SELECTED_SKIN_TAG));
      ArcanaItem inputItem = ArcanaItemUtils.identifyItem(positiveInput);
      if(inputItem == null) return new ArrayList<>(); // should be impossible
      if(cataSkin == null){
         ArcanaItem.removeProperty(positiveInput,ArcanaItem.SKIN_TAG);
      }else{
         ArcanaItem.putProperty(positiveInput,ArcanaItem.SKIN_TAG,cataSkin.getSerializedName());
      }
      inputItem.buildItemLore(positiveInput,player.level().getServer());
      returnItems.add(positiveInput);
      returnItems.add(aequalisInput);
      ArcanaAchievements.grant(player,ArcanaAchievements.MOGGED);
      return returnItems;
   }
   
   @Override
   public List<Tuple<ItemStack, String>> doTransmutation(ItemEntity input1Entity, ItemEntity input2Entity, ItemEntity reagent1Entity, ItemEntity reagent2Entity, ItemEntity aequalisEntity, TransmutationAltarBlockEntity altar, ServerPlayer player){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN);
      ItemStack reagent1Stack = reagent1Entity != null ? reagent1Entity.getItem() : ItemStack.EMPTY;
      ItemStack reagent2Stack = reagent2Entity != null ? reagent2Entity.getItem() : ItemStack.EMPTY;
      ItemStack inputStack;
      ItemEntity inputEntity;
      ItemStack cataStack;
      ItemEntity cataEntity;
      String outputPos;
      if(input1Entity == null || input2Entity == null) return new ArrayList<>();
      if(validStack(ArcanaRegistry.TRANSMOGRIFICATION_CATALYST.getPrefItem(),input1Entity.getItem())){
         inputStack = input2Entity.getItem();
         inputEntity = input2Entity;
         cataStack = input1Entity.getItem();
         cataEntity = input1Entity;
         outputPos = "positive";
      }else if(validStack(ArcanaRegistry.TRANSMOGRIFICATION_CATALYST.getPrefItem(),input2Entity.getItem())){
         inputStack = input1Entity.getItem();
         inputEntity = input1Entity;
         cataStack = input2Entity.getItem();
         cataEntity = input2Entity;
         outputPos = "negative";
      }else{
         return new ArrayList<>();
      }
      if(!canTransmute(inputStack, cataStack,reagent1Stack,reagent2Stack, ItemStack.EMPTY,altar)) return new ArrayList<>();
      
      boolean m11 = validReagent1(reagent1Stack,bargainLvl), m22 = validReagent2(reagent2Stack,bargainLvl), m12 = validReagent1(reagent2Stack,bargainLvl), m21 = validReagent2(reagent1Stack,bargainLvl);
      boolean straight = m11 && m22;
      boolean cross = !straight && m12 && m21;
      if (!straight && !cross) return new ArrayList<>(); // should be impossible
      
      List<Tuple<ItemStack,String>> outputs = new ArrayList<>();
      int consumedInput = 1;
      
      ArcanaSkin cataSkin = ArcanaSkin.getSkinFromString(ArcanaItem.getStringProperty(cataStack, TransmogrificationCatalyst.SELECTED_SKIN_TAG));
      ArcanaItem inputItem = ArcanaItemUtils.identifyItem(inputStack);
      if(inputItem == null) return new ArrayList<>(); // should be impossible
      if(cataSkin == null){
         ArcanaItem.removeProperty(inputStack,ArcanaItem.SKIN_TAG);
      }else{
         ArcanaItem.putProperty(inputStack,ArcanaItem.SKIN_TAG,cataSkin.getSerializedName());
      }
      inputItem.buildItemLore(inputStack,altar.getLevel().getServer());
      
      outputs.add(new Tuple<>(inputStack,outputPos));
      
      if(inputStack.getCount() == consumedInput){
         inputEntity.discard();
      }else{
         inputStack.shrink(consumedInput);
         inputEntity.setItem(inputStack);
      }
      
      if(cataStack.getCount() == consumedInput){
         cataEntity.discard();
      }else{
         cataStack.shrink(consumedInput);
         cataEntity.setItem(cataStack);
      }
      
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
      
      if(player != null){
         ArcanaAchievements.grant(player,ArcanaAchievements.MOGGED);
      }else{
         String crafter = inputItem.getCrafter(cataStack);
         if(crafter != null && !crafter.isEmpty()){
            ArcanaAchievements.grant(AlgoUtils.getUUID(crafter),ArcanaAchievements.MOGGED);
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
      
      ItemStack transmogStack;
      ItemStack arcanaStack;
      if(validStack(ArcanaRegistry.TRANSMOGRIFICATION_CATALYST.getPrefItem(),input1)){
         transmogStack = input1;
         arcanaStack = input2;
      }else if(validStack(ArcanaRegistry.TRANSMOGRIFICATION_CATALYST.getPrefItem(),input2)){
         transmogStack = input2;
         arcanaStack = input1;
      }else{
         return false;
      }
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(arcanaStack);
      if(arcanaItem == null || arcanaItem.getId().equals(ArcanaRegistry.TRANSMOGRIFICATION_CATALYST.getId())) return false;
      ArcanaSkin cataSkin = ArcanaSkin.getSkinFromString(ArcanaItem.getStringProperty(transmogStack, TransmogrificationCatalyst.SELECTED_SKIN_TAG));
      ArcanaSkin curSkin = ArcanaItem.getSkin(arcanaStack);
      if(cataSkin != null && !arcanaItem.getId().equals(cataSkin.getArcanaItem().getId())) return false;
      if(cataSkin == null && curSkin == null) return false;
      if(cataSkin != null && curSkin != null && cataSkin.getId().equals(curSkin.getId())) return false;
      
      if(altar.getLevel() instanceof ServerLevel serverWorld){
         ServerPlayer player = serverWorld.getServer().getPlayerList().getPlayer(AlgoUtils.getUUID(arcanaItem.getCrafter(arcanaStack)));
         return player == null || cataSkin == null || ArcanaNovum.data(player).hasSkin(cataSkin);
      }
      return true;
   }
   
   @Override
   public ItemStack getViewStack(){
      return MinecraftUtils.removeLore(ArcanaRegistry.TRANSMOGRIFICATION_CATALYST.getPrefItemNoLore().copyWithCount(1));
   }
}
