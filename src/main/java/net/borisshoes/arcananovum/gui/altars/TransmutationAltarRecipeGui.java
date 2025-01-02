package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltar;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.AequalisScientia;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.recipes.transmutation.*;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class TransmutationAltarRecipeGui extends SimpleGui {
   private int page = 1;
   private int commiePage = 1;
   private String curRecipeName = "";
   private CommutativeTransmutationRecipe curRecipe = null;
   private final List<TransmutationRecipe> recipes;
   private final TransmutationAltarBlockEntity blockEntity;
   private final SimpleGui returnGui;
   private ItemStack selectionModeStack;
   
   public TransmutationAltarRecipeGui(ServerPlayerEntity player, SimpleGui returnGui, Optional<TransmutationAltarBlockEntity> altarOpt){
      super(ScreenHandlerType.GENERIC_9X6, player, false);
      this.recipes = TransmutationAltar.getUnlockedRecipes(player);
      this.blockEntity = altarOpt.orElse(null);
      this.returnGui = returnGui;
      setTitle(Text.literal("Transmutation Altar"));
   }
   
   public void enableSelectionMode(ItemStack stack){
      selectionModeStack = stack;
      recipes.removeIf(recipe -> recipe instanceof AequalisCatalystTransmutationRecipe || recipe instanceof AequalisSkillTransmutationRecipe);
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      boolean indexInCenter = index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8;
      if(curRecipe == null){
         int numPages = (int) Math.ceil((float)recipes.size()/28.0);
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
            List<TransmutationRecipe> pageRecipes = recipes;
            int ind = (7*(index/9 - 1) + (index % 9 - 1)) + 28*(page-1);
            if(ind >= pageRecipes.size()) return true;
            TransmutationRecipe recipe = pageRecipes.get(ind);
            if(selectionModeStack != null && ArcanaItemUtils.identifyItem(selectionModeStack) instanceof AequalisScientia aq){
               ArcanaItem.putProperty(selectionModeStack, AequalisScientia.TRANSMUTATION_TAG,recipe.getName());
               aq.buildItemLore(selectionModeStack,player.getServer());
               SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM,1,0.8f);
               close();
               return true;
            }
            
            if(!(recipe instanceof CommutativeTransmutationRecipe cr)) return true;
            curRecipe = cr;
            curRecipeName = cr.getName();
            buildRecipeViewGui(curRecipeName);
         }
      }else{
         List<ItemStack> inputs = curRecipe.getCommunalInputs();
         int numPages = (int) Math.ceil((float)inputs.size()/28.0);
         if(index == 49){
            curRecipeName = "";
            curRecipe = null;
            buildRecipeListGui();
            commiePage = 1;
         }else if(index == 45){
            if(commiePage > 1){
               commiePage--;
               buildRecipeViewGui(curRecipeName);
            }
         }else if(index == 53){
            if(commiePage < numPages){
               commiePage++;
               buildRecipeViewGui(curRecipeName);
            }
         }
      }
      return true;
   }
   
   
   public void buildRecipeListGui(){
      MiscUtils.outlineGUI(this, ArcanaColors.EQUAYUS_COLOR,Text.literal("Transmutation Recipes").formatted(Formatting.BLUE));
      
      List<TransmutationRecipe> pageRecipes = recipes;
      int numPages = (int) Math.ceil((float)pageRecipes.size()/28.0);
      
      GuiElementBuilder nextArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideDefaultTooltip();
      nextArrow.setName((Text.literal("")
            .append(Text.literal("Next Page").formatted(Formatting.GOLD))));
      nextArrow.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("("+page+" of "+numPages+")").formatted(Formatting.DARK_PURPLE)))));
      setSlot(53,nextArrow);
      
      GuiElementBuilder prevArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideDefaultTooltip();
      prevArrow.setName((Text.literal("")
            .append(Text.literal("Prev Page").formatted(Formatting.GOLD))));
      prevArrow.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("("+page+" of "+numPages+")").formatted(Formatting.DARK_PURPLE)))));
      setSlot(45,prevArrow);
      
      int k = (page-1)*28;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageRecipes.size()){
               TransmutationRecipe recipe = pageRecipes.get(k);
               GuiElementBuilder viewItem = GuiElementBuilder.from(recipe.getViewStack()).hideDefaultTooltip();
               viewItem.setName(Text.literal(recipe.getName()).formatted(Formatting.AQUA,Formatting.BOLD));
               
               int bargainLvl = blockEntity == null ? 0 : ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
               int reagent1Count = recipe.getBargainReagent(recipe.getReagent1(),bargainLvl).getCount();
               int reagent2Count = recipe.getBargainReagent(recipe.getReagent2(),bargainLvl).getCount();
               
               if(recipe instanceof CommutativeTransmutationRecipe){
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("Commutative Transmutation").formatted(Formatting.GREEN)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(reagent1Count+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent1().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(reagent2Count+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent2().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Click").formatted(Formatting.GREEN))
                        .append(Text.literal(" to view full recipe").formatted(Formatting.DARK_GREEN)))));
               }else if(recipe instanceof InfusionTransmutationRecipe infusion){
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("Infusion Transmutation").formatted(Formatting.DARK_PURPLE)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.GRAY))
                        .append(Text.literal(infusion.getInput().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(infusion.getInput().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Output: ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(infusion.getOutput().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(infusion.getOutput().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(reagent1Count+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent1().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(reagent2Count+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent2().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
               }else if(recipe instanceof PermutationTransmutationRecipe permutation){
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("Permutation Transmutation").formatted(Formatting.DARK_AQUA)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.GRAY))
                        .append(Text.literal(permutation.getInput().getCount()+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(permutation.getInput().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Output: ").formatted(Formatting.DARK_GRAY))
                        .append(permutation.getOutputDescription()).formatted(Formatting.AQUA))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(reagent1Count+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent1().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(reagent2Count+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent2().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
               }else if(recipe instanceof AequalisSkillTransmutationRecipe){
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("Aequalis Transmutation").formatted(Formatting.AQUA)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.GRAY))
                        .append(Text.literal("An Arcana Item").formatted(Formatting.DARK_PURPLE)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("An Arcana Item").formatted(Formatting.DARK_PURPLE)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.DARK_AQUA))
                        .append(Text.literal("Your Aequalis Scientia").formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(reagent1Count+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent1().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(reagent2Count+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent2().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
               }else if(recipe instanceof AequalisCatalystTransmutationRecipe){
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("Aequalis Transmutation").formatted(Formatting.AQUA)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Text.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.GRAY))
                        .append(Text.literal("An Arcana Item").formatted(Formatting.DARK_PURPLE)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("1-4 ").formatted(Formatting.GOLD))
                        .append(Text.literal("Catalytic Matrices").formatted(Formatting.YELLOW)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Input: ").formatted(Formatting.DARK_AQUA))
                        .append(Text.literal("Your Aequalis Scientia").formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(reagent1Count+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent1().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(reagent2Count+" ").formatted(Formatting.DARK_AQUA))
                        .append(Text.translatable(recipe.getReagent2().getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
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
      
      MiscUtils.outlineGUI(this, ArcanaColors.EQUAYUS_COLOR,Text.literal("Transmutation Recipes").formatted(Formatting.BLUE));
      
      for(TransmutationRecipe rec : recipes){
         if(rec.getName().equals(recipeName) && rec instanceof CommutativeTransmutationRecipe crec){
            this.curRecipe = crec;
            break;
         }
      }
      if(this.curRecipe == null) return;
      
      List<ItemStack> inputs = curRecipe.getCommunalInputs();
      int numPages = (int) Math.ceil((float)inputs.size()/28.0);
      int bargainLvl = blockEntity == null ? 0 : ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      
      GuiElementBuilder nextArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.RIGHT_ARROW)).hideDefaultTooltip();
      nextArrow.setName((Text.literal("")
            .append(Text.literal("Next Page").formatted(Formatting.GOLD))));
      nextArrow.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("("+commiePage+" of "+numPages+")").formatted(Formatting.DARK_PURPLE)))));
      setSlot(53,nextArrow);
      
      GuiElementBuilder prevArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.LEFT_ARROW)).hideDefaultTooltip();
      prevArrow.setName((Text.literal("")
            .append(Text.literal("Prev Page").formatted(Formatting.GOLD))));
      prevArrow.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("("+commiePage+" of "+numPages+")").formatted(Formatting.DARK_PURPLE)))));
      setSlot(45,prevArrow);
      
      int k = (commiePage-1)*28;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < inputs.size()){
               ItemStack item = inputs.get(k);
               GuiElementBuilder viewItem = GuiElementBuilder.from(item).hideDefaultTooltip();
               viewItem.setName((Text.translatable(item.getItem().getTranslationKey()).formatted(Formatting.BOLD,Formatting.AQUA)));
               viewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                     .append(Text.literal("Input").formatted(Formatting.GRAY))
                     .append(Text.literal(" or ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal("Focus").formatted(Formatting.DARK_GRAY)))));
               
               setSlot((i*9+10)+j,viewItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      ItemStack reagent1 = this.curRecipe.getBargainReagent(this.curRecipe.getReagent1(),bargainLvl);
      GuiElementBuilder reagent1Item = GuiElementBuilder.from(reagent1).hideDefaultTooltip();
      reagent1Item.setName((Text.translatable(reagent1.getItem().getTranslationKey()).formatted(Formatting.BOLD,Formatting.GREEN)));
      reagent1Item.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(reagent1.getCount()+" ").formatted(Formatting.DARK_AQUA))
            .append(Text.translatable(reagent1.getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
      setSlot(48,reagent1Item);
      
      
      ItemStack reagent2 = this.curRecipe.getBargainReagent(this.curRecipe.getReagent2(),bargainLvl);
      GuiElementBuilder reagent2Item = GuiElementBuilder.from(reagent2).hideDefaultTooltip();
      reagent2Item.setName((Text.translatable(reagent2.getItem().getTranslationKey()).formatted(Formatting.BOLD,Formatting.GREEN)));
      reagent2Item.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Reagent: ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(reagent2.getCount()+" ").formatted(Formatting.DARK_AQUA))
            .append(Text.translatable(reagent2.getItem().getTranslationKey()).formatted(Formatting.AQUA)))));
      setSlot(50,reagent2Item);
      
      GuiElementBuilder recipeItem = new GuiElementBuilder(GraphicalItem.with(GraphicItems.TRANSMUTATION_BOOK)).hideDefaultTooltip();
      recipeItem.setName((Text.literal("")
            .append(Text.literal("Transmutation Recipes").formatted(Formatting.DARK_AQUA))));
      recipeItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to view all Transmutation Recipes").formatted(Formatting.BLUE)))));
      setSlot(49,recipeItem);
      
      setTitle(Text.literal(curRecipeName+" Transmutation"));
   }
   
   @Override
   public void onTick(){
      if(blockEntity != null){
         World world = blockEntity.getWorld();
         if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity || !blockEntity.isAssembled()){
            this.close();
         }
      }
   }
   
   @Override
   public void onClose(){
      if(returnGui != null){
         returnGui.open();
      }
   }
}
