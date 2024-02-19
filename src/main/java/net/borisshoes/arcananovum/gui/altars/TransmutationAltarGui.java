package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.recipes.transmutation.*;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.borisshoes.arcananovum.blocks.altars.CelestialAltar.CelestialAltarBlock.HORIZONTAL_FACING;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TransmutationAltarGui extends SimpleGui implements WatchedGui {
   private final TransmutationAltarBlockEntity blockEntity;
   private final int mode;
   private int page = 1;
   private String curRecipeName = "";
   private CommutativeTransmutationRecipe curRecipe = null;
   
   public TransmutationAltarGui(ScreenHandlerType<?> type, ServerPlayerEntity player, TransmutationAltarBlockEntity blockEntity, int mode){
      super(type, player, false);
      this.blockEntity = blockEntity;
      this.mode = mode;
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
      if(recipe != null){
         List<Pair<ItemStack,String>> outputs = recipe.doTransmutation(positiveEntity,negativeEntity,reagent1Entity,reagent2Entity,aequalisEntity,blockEntity,player);
         
         int transmuteCount = 0;
         for(Pair<ItemStack,String> outputPair : outputs){
            ItemStack output = outputPair.getLeft();
            Vec3d outputPos = blockEntity.getOutputPos(outputPair.getRight());
            transmuteCount += output.getCount();
            if(output.isOf(ArcanaRegistry.MYTHICAL_CATALYST.getItem())){
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
      boolean indexInCenter = index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8;
      if(mode == 0){
         if(index == 2){
            blockEntity.openGui(1,player,"");
         }else if(index == 4  && blockEntity.getWorld() instanceof ServerWorld serverWorld){
            if(blockEntity.getCooldown() <= 0){
               if(checkTransmute() != null){
                  blockEntity.resetCooldown();
                  boolean hastyBargain = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.HASTY_BARGAIN.id) > 0;
                  double speedMod = hastyBargain ? 2 : 1;
                  int castTime = (int) (500.0 / speedMod);
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
      }else if(mode == 1){
         int numPages = (int) Math.ceil((float)TransmutationRecipes.transmutationRecipes.size()/28.0);
         if(index == 45){
            if(page > 1){
               page--;
               buildRecipeListGui();
            }
         }else if(index == 53){
            if(page < numPages){
               page++;
               buildRecipeListGui();
            }
         }else if(indexInCenter){
            ArrayList<TransmutationRecipe> pageRecipes = TransmutationRecipes.transmutationRecipes;
            int ind = (7*(index/9 - 1) + (index % 9 - 1)) + 28*(page-1);
            if(ind >= pageRecipes.size()) return true;
            TransmutationRecipe recipe = pageRecipes.get(ind);
            if(!(recipe instanceof CommutativeTransmutationRecipe cr)) return true;
            blockEntity.openGui(2,player,cr.getName());
         }
      }else if(mode == 2 && index == 49){
         blockEntity.openGui(1,player,"");
      }else if(mode == 2 && curRecipe != null){
         List<ItemStack> inputs = curRecipe.getCommunalInputs();
         int numPages = (int) Math.ceil((float)inputs.size()/28.0);
         
         if(index == 45){
            if(page > 1){
               page--;
               buildRecipeViewGui(curRecipeName);
            }
         }else if(index == 53){
            if(page < numPages){
               page++;
               buildRecipeViewGui(curRecipeName);
            }
         }
      }
      return true;
   }
   
   @Override
   public void onTick(){
      if(this.mode == 0){
         buildMenuGui();
      }
   }
   
   public void buildRecipeListGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setName(Text.literal("Transmutation Recipes").formatted(Formatting.BLUE)));
      }
      
      ArrayList<TransmutationRecipe> pageRecipes = TransmutationRecipes.transmutationRecipes;
      int numPages = (int) Math.ceil((float)TransmutationRecipes.transmutationRecipes.size()/28.0);
      
      GuiElementBuilder nextArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideFlags();
      nextArrow.setName((Text.literal("")
            .append(Text.literal("Next Page").formatted(Formatting.GOLD))));
      nextArrow.addLoreLine((Text.literal("")
            .append(Text.literal("("+page+" of "+numPages+")").formatted(Formatting.DARK_PURPLE))));
      setSlot(53,nextArrow);
      
      GuiElementBuilder prevArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideFlags();
      prevArrow.setName((Text.literal("")
            .append(Text.literal("Prev Page").formatted(Formatting.GOLD))));
      prevArrow.addLoreLine((Text.literal("")
            .append(Text.literal("("+page+" of "+numPages+")").formatted(Formatting.DARK_PURPLE))));
      setSlot(45,prevArrow);
      
      int k = (page-1)*28;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageRecipes.size()){
               TransmutationRecipe recipe = pageRecipes.get(k);
               GuiElementBuilder viewItem = GuiElementBuilder.from(recipe.getViewStack()).hideFlags();
               viewItem.setName(Text.literal(recipe.getName()).formatted(Formatting.AQUA,Formatting.BOLD));
               
               if(recipe instanceof CommutativeTransmutationRecipe){
                  viewItem.addLoreLine(Text.literal("Commutative Transmutation").formatted(Formatting.GREEN));
                  viewItem.addLoreLine(Text.literal(""));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(recipe.getReagent1().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent1().getTranslationKey()).formatted(Formatting.AQUA))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(recipe.getReagent2().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent2().getTranslationKey()).formatted(Formatting.AQUA))));
                  viewItem.addLoreLine(Text.literal(""));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Click").formatted(Formatting.GREEN))
                        .append(Text.literal(" to view full recipe").formatted(Formatting.DARK_GREEN))));
               }else if(recipe instanceof InfusionTransmutationRecipe infusion){
                  viewItem.addLoreLine(Text.literal("Infusion Transmutation").formatted(Formatting.DARK_PURPLE));
                  viewItem.addLoreLine(Text.literal(""));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.GRAY))
                        .append(Text.literal(infusion.getInput().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(infusion.getInput().getTranslationKey()).formatted(Formatting.AQUA))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Output: ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(infusion.getOutput().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(infusion.getOutput().getTranslationKey()).formatted(Formatting.AQUA))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(recipe.getReagent1().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent1().getTranslationKey()).formatted(Formatting.AQUA))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(recipe.getReagent2().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent2().getTranslationKey()).formatted(Formatting.AQUA))));
               }else if(recipe instanceof AequalisSkillTransmutationRecipe){
                  viewItem.addLoreLine(Text.literal("Aequalis Transmutation").formatted(Formatting.AQUA));
                  viewItem.addLoreLine(Text.literal(""));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.GRAY))
                        .append(Text.literal("A Magic Item").formatted(Formatting.DARK_PURPLE))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("A Magic Item").formatted(Formatting.DARK_PURPLE))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.DARK_AQUA))
                        .append(Text.literal("Your Aequalis Scientia").formatted(Formatting.AQUA))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(recipe.getReagent1().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent1().getTranslationKey()).formatted(Formatting.AQUA))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(recipe.getReagent2().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent2().getTranslationKey()).formatted(Formatting.AQUA))));
               }else if(recipe instanceof AequalisCatalystTransmutationRecipe){
                  viewItem.addLoreLine(Text.literal("Aequalis Transmutation").formatted(Formatting.AQUA));
                  viewItem.addLoreLine(Text.literal(""));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.GRAY))
                        .append(Text.literal("A Magic Item").formatted(Formatting.DARK_PURPLE))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Catalytic Matrices").formatted(Formatting.YELLOW))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.DARK_AQUA))
                        .append(Text.literal("Your Aequalis Scientia").formatted(Formatting.AQUA))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(recipe.getReagent1().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent1().getTranslationKey()).formatted(Formatting.AQUA))));
                  viewItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(recipe.getReagent2().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent2().getTranslationKey()).formatted(Formatting.AQUA))));
               }
               setSlot((i*9+10)+j,viewItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      setTitle(Text.literal("Transmutation Recipes"));
   }
   
   public void buildRecipeViewGui(String recipeName){
      this.curRecipeName = recipeName;
      
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setName(Text.literal("Transmutation Recipes").formatted(Formatting.BLUE)));
      }
      
      for(TransmutationRecipe rec : TransmutationRecipes.transmutationRecipes){
         if(rec.getName().equals(recipeName) && rec instanceof CommutativeTransmutationRecipe crec){
            this.curRecipe = crec;
            break;
         }
      }
      if(this.curRecipe == null) return;
      
      List<ItemStack> inputs = curRecipe.getCommunalInputs();
      int numPages = (int) Math.ceil((float)inputs.size()/28.0);
      
      GuiElementBuilder nextArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideFlags();
      nextArrow.setName((Text.literal("")
            .append(Text.literal("Next Page").formatted(Formatting.GOLD))));
      nextArrow.addLoreLine((Text.literal("")
            .append(Text.literal("("+page+" of "+numPages+")").formatted(Formatting.DARK_PURPLE))));
      setSlot(53,nextArrow);
      
      GuiElementBuilder prevArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideFlags();
      prevArrow.setName((Text.literal("")
            .append(Text.literal("Prev Page").formatted(Formatting.GOLD))));
      prevArrow.addLoreLine((Text.literal("")
            .append(Text.literal("("+page+" of "+numPages+")").formatted(Formatting.DARK_PURPLE))));
      setSlot(45,prevArrow);
      
      int k = (page-1)*28;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < inputs.size()){
               ItemStack item = inputs.get(k);
               GuiElementBuilder viewItem = GuiElementBuilder.from(item).hideFlags();
               viewItem.setName((Text.translatable(item.getTranslationKey()).formatted(Formatting.BOLD,Formatting.AQUA)));
               viewItem.addLoreLine((Text.literal("")
                     .append(Text.literal("Input").formatted(Formatting.GRAY))
                     .append(Text.literal(" or ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal("Focus").formatted(Formatting.DARK_GRAY))));
               
               setSlot((i*9+10)+j,viewItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      ItemStack reagent1 = curRecipe.getReagent1();
      GuiElementBuilder reagent1Item = GuiElementBuilder.from(reagent1).hideFlags();
      reagent1Item.setName((Text.translatable(reagent1.getTranslationKey()).formatted(Formatting.BOLD,Formatting.GREEN)));
      reagent1Item.addLoreLine((Text.literal("")
            .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(reagent1.getCount()+" ").formatted(Formatting.DARK_AQUA))
            .append(Text.translatable(reagent1.getTranslationKey()).formatted(Formatting.AQUA))));
      setSlot(48,reagent1Item);
      
      
      ItemStack reagent2 = curRecipe.getReagent2();
      GuiElementBuilder reagent2Item = GuiElementBuilder.from(reagent2).hideFlags();
      reagent2Item.setName((Text.translatable(reagent2.getTranslationKey()).formatted(Formatting.BOLD,Formatting.GREEN)));
      reagent2Item.addLoreLine((Text.literal("")
            .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(reagent2.getCount()+" ").formatted(Formatting.DARK_AQUA))
            .append(Text.translatable(reagent2.getTranslationKey()).formatted(Formatting.AQUA))));
      setSlot(50,reagent2Item);
      
      GuiElementBuilder recipeItem = new GuiElementBuilder(Items.KNOWLEDGE_BOOK).hideFlags();
      recipeItem.setName((Text.literal("")
            .append(Text.literal("Transmutation Recipes").formatted(Formatting.DARK_AQUA))));
      recipeItem.addLoreLine((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to view all Transmutation Recipes").formatted(Formatting.BLUE))));
      setSlot(49,recipeItem);
      
      setTitle(Text.literal(curRecipeName+" Transmutation"));
   }
   
   public void buildMenuGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal("Transmutation Altar").formatted(Formatting.BLUE)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideFlags();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Ready").formatted(Formatting.AQUA))));
      }else{
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Recharging").formatted(Formatting.BLUE))));
         cooldownItem.addLoreLine((Text.literal("")
               .append(Text.literal((blockEntity.getCooldown()/20)+" Seconds").formatted(Formatting.DARK_PURPLE))));
      }
      setSlot(0,cooldownItem);
      
      GuiElementBuilder recipeItem = new GuiElementBuilder(Items.KNOWLEDGE_BOOK).hideFlags();
      recipeItem.setName((Text.literal("")
            .append(Text.literal("Transmutation Recipes").formatted(Formatting.DARK_AQUA))));
      recipeItem.addLoreLine((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to view all Transmutation Recipes").formatted(Formatting.BLUE))));
      
      setSlot(2,recipeItem);
      

      GuiElementBuilder activateItem = new GuiElementBuilder(Items.DIAMOND);
      activateItem.setName((Text.literal("")
            .append(Text.literal("Activate Altar").formatted(Formatting.LIGHT_PURPLE))));
      activateItem.addLoreLine((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.BLUE))
            .append(Text.literal(" to begin a Transmutation").formatted(Formatting.DARK_PURPLE))));
      setSlot(4,activateItem);
   }
   
   @Override
   public void onClose(){
      if(mode == 2){
         blockEntity.openGui(1,player,"");
      }
   }
   
   
   @Override
   public void close(){
      super.close();
   }
   
   @Override
   public BlockEntity getBlockEntity(){
      return blockEntity;
   }
   
   @Override
   public SimpleGui getGui(){
      return this;
   }
}
