package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.recipes.transmutation.TransmutationRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.TransmutationRecipes;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static net.borisshoes.arcananovum.blocks.altars.CelestialAltar.CelestialAltarBlock.HORIZONTAL_FACING;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TransmutationAltarGui extends SimpleGui {
   private final TransmutationAltarBlockEntity blockEntity;
   
   public TransmutationAltarGui(ScreenHandlerType<?> type, ServerPlayerEntity player, TransmutationAltarBlockEntity blockEntity){
      super(type, player, false);
      this.blockEntity = blockEntity;
      setTitle(Text.literal("Transmutation Altar"));
   }
   
   private void transmute(boolean recursed){
      HashMap<String,ItemEntity> stacks = blockEntity.getTransmutingStacks();
      ItemEntity positiveEntity = stacks.get("positive");
      ItemEntity negativeEntity = stacks.get("negative");
      ItemEntity reagent1Entity = stacks.get("reagent1");
      ItemEntity reagent2Entity = stacks.get("reagent2");
      ItemEntity aequalisEntity = stacks.get("aequalis");
      ItemStack positiveStack = positiveEntity == null ? ItemStack.EMPTY : positiveEntity.getStack();
      ItemStack negativeStack = negativeEntity == null ? ItemStack.EMPTY : negativeEntity.getStack();
      ItemStack reagent1Stack = reagent1Entity == null ? ItemStack.EMPTY : reagent1Entity.getStack();
      ItemStack reagent2Stack = reagent2Entity == null ? ItemStack.EMPTY : reagent2Entity.getStack();
      ItemStack aequalisStack = aequalisEntity == null ? ItemStack.EMPTY : aequalisEntity.getStack();
      
      TransmutationRecipe recipe = TransmutationRecipes.findMatchingRecipe(positiveStack,negativeStack,reagent1Stack,reagent2Stack,aequalisStack,blockEntity);
      blockEntity.setActive(false);
      if(recipe != null){
         List<Pair<ItemStack,String>> outputs = recipe.doTransmutation(positiveEntity,negativeEntity,reagent1Entity,reagent2Entity,aequalisEntity,blockEntity,player);
         
         int transmuteCount = 0;
         for(Pair<ItemStack,String> outputPair : outputs){
            ItemStack output = outputPair.getLeft();
            Vec3d outputPos = blockEntity.getOutputPos(outputPair.getRight());
            transmuteCount += output.getCount();
            if(output.isOf(ArcanaRegistry.DIVINE_CATALYST.getItem())){
               PLAYER_DATA.get(player).addCraftedSilent(output);
               ArcanaAchievements.grant(player,ArcanaAchievements.DIVINE_TRANSMUTATION.id);
            }
            if(output.isOf(ArcanaRegistry.AEQUALIS_SCIENTIA.getItem())){
               PLAYER_DATA.get(player).addCraftedSilent(output);
               ArcanaAchievements.grant(player,ArcanaAchievements.PRICE_OF_KNOWLEDGE.id);
            }
            
            blockEntity.getWorld().spawnEntity(new ItemEntity(blockEntity.getWorld(),outputPos.x,outputPos.y+0.25,outputPos.z,output, 0, 0, 0));
         }
         if(transmuteCount > 0){
            PLAYER_DATA.get(player).addXP(transmuteCount*10+100);
         }
         
         boolean canRecurse = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.TRADE_AGREEMENT.id) > 0;
         if(canRecurse && checkTransmute() != null){
            blockEntity.resetCooldown();
            boolean hastyBargain = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.HASTY_BARGAIN.id) > 0;
            double speedMod = hastyBargain ? 2 : 1;
            int castTime = (int) (500.0 / speedMod);
            blockEntity.setActive(true);
            ParticleEffectUtils.transmutationAltarAnim(player.getServerWorld(),blockEntity.getPos().toCenterPos(), 0, blockEntity.getWorld().getBlockState(blockEntity.getPos()).get(HORIZONTAL_FACING), speedMod);
            ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(castTime, () -> transmute(true)));
         }
         
         ArcanaAchievements.progress(player,ArcanaAchievements.STATE_ALCHEMIST.id,transmuteCount);
         
         SoundUtils.playSound(blockEntity.getWorld(), blockEntity.getPos(), SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM, SoundCategory.BLOCKS,1,0.8f);
      }else{
         if(!recursed) blockEntity.refundCooldown();
         SoundUtils.playSound(blockEntity.getWorld(), blockEntity.getPos(), SoundEvents.ENTITY_ALLAY_HURT, SoundCategory.BLOCKS,1,0.7f);
      }
   }
   
   private TransmutationRecipe checkTransmute(){
      HashMap<String,ItemEntity> stacks = blockEntity.getTransmutingStacks();
      ItemEntity positiveEntity = stacks.get("positive");
      ItemEntity negativeEntity = stacks.get("negative");
      ItemEntity reagent1Entity = stacks.get("reagent1");
      ItemEntity reagent2Entity = stacks.get("reagent2");
      ItemEntity aequalisEntity = stacks.get("aequalis");
      ItemStack positiveStack = positiveEntity == null ? ItemStack.EMPTY : positiveEntity.getStack();
      ItemStack negativeStack = negativeEntity == null ? ItemStack.EMPTY : negativeEntity.getStack();
      ItemStack reagent1Stack = reagent1Entity == null ? ItemStack.EMPTY : reagent1Entity.getStack();
      ItemStack reagent2Stack = reagent2Entity == null ? ItemStack.EMPTY : reagent2Entity.getStack();
      ItemStack aequalisStack = aequalisEntity == null ? ItemStack.EMPTY : aequalisEntity.getStack();
      
      return TransmutationRecipes.findMatchingRecipe(positiveStack,negativeStack,reagent1Stack,reagent2Stack,aequalisStack,blockEntity);
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(index == 2){
         TransmutationAltarRecipeGui recipeGui = new TransmutationAltarRecipeGui(player, this, Optional.of(blockEntity));
         recipeGui.buildRecipeListGui();
         recipeGui.open();
      }else if(index == 4  && blockEntity.getWorld() instanceof ServerWorld serverWorld){
         if(blockEntity.getCooldown() <= 0){
            if(checkTransmute() != null){
               blockEntity.resetCooldown();
               boolean hastyBargain = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.HASTY_BARGAIN.id) > 0;
               double speedMod = hastyBargain ? 2 : 1;
               int castTime = (int) (500.0 / speedMod);
               blockEntity.setActive(true);
               ParticleEffectUtils.transmutationAltarAnim(player.getServerWorld(),blockEntity.getPos().toCenterPos(), 0, serverWorld.getBlockState(blockEntity.getPos()).get(HORIZONTAL_FACING), speedMod);
               ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(castTime, () -> transmute(false)));
            }else{
               player.sendMessage(Text.literal("No Transmutation Items Found").formatted(Formatting.RED,Formatting.ITALIC));
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
            }
            close();
         }else{
            player.sendMessage(Text.literal("The Altar is on Cooldown").formatted(Formatting.RED,Formatting.ITALIC),false);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
            close();
         }
      }
      return true;
   }
   
   @Override
   public void onTick(){
      World world = blockEntity.getWorld();
      if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity || !blockEntity.isAssembled() || blockEntity.isActive()){
         this.close();
      }
      
      buildMenuGui();
   }
   
   public void buildMenuGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,ArcanaColors.EQUAYUS_COLOR)).setName(Text.literal("Transmutation Altar").formatted(Formatting.BLUE)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideDefaultTooltip();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Ready").formatted(Formatting.AQUA))));
      }else{
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Recharging").formatted(Formatting.BLUE))));
         cooldownItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal((blockEntity.getCooldown()/20)+" Seconds").formatted(Formatting.DARK_PURPLE)))));
      }
      setSlot(0,cooldownItem);
      
      GuiElementBuilder recipeItem = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.TRANSMUTATION_BOOK)).hideDefaultTooltip();
      recipeItem.setName((Text.literal("")
            .append(Text.literal("Transmutation Recipes").formatted(Formatting.DARK_AQUA))));
      recipeItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to view all Transmutation Recipes").formatted(Formatting.BLUE)))));
      
      setSlot(2,recipeItem);
      

      GuiElementBuilder activateItem = new GuiElementBuilder(Items.DIAMOND);
      activateItem.setName((Text.literal("")
            .append(Text.literal("Activate Altar").formatted(Formatting.LIGHT_PURPLE))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.BLUE))
            .append(Text.literal(" to begin a Transmutation").formatted(Formatting.DARK_PURPLE)))));
      setSlot(4,activateItem);
   }
   
   @Override
   public void close(){
      super.close();
   }
}
