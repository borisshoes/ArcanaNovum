package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltar;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.AequalisScientia;
import net.borisshoes.arcananovum.recipes.transmutation.*;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class TransmutationAltarRecipeGui extends SimpleGui {
   private int page = 1;
   private int commiePage = 1;
   private int costMode;
   private String curRecipeName = "";
   private CommutativeTransmutationRecipe curRecipe = null;
   private final List<TransmutationRecipe> recipes;
   private final TransmutationAltarBlockEntity blockEntity;
   private final SimpleGui returnGui;
   private ItemStack selectionModeStack;
   
   public TransmutationAltarRecipeGui(ServerPlayer player, SimpleGui returnGui, Optional<TransmutationAltarBlockEntity> altarOpt){
      super(MenuType.GENERIC_9x6, player, false);
      this.recipes = TransmutationAltar.getUnlockedRecipes(player);
      this.blockEntity = altarOpt.orElse(null);
      this.returnGui = returnGui;
      this.costMode = blockEntity == null ? 0 : ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      setTitle(Component.literal("Transmutation Altar"));
   }
   
   public void enableSelectionMode(ItemStack stack){
      selectionModeStack = stack;
      recipes.removeIf(recipe -> recipe instanceof AequalisCatalystTransmutationRecipe || recipe instanceof AequalisSkillTransmutationRecipe);
      if(stack.is(ArcanaRegistry.AEQUALIS_SCIENTIA.getItem())) this.costMode = -1;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
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
               if(recipe.getName().equals("Aequalis Reconfiguration")) ArcanaAchievements.grant(player,ArcanaAchievements.FRACTAL_ATTUNEMENT);
               ArcanaItem.putProperty(selectionModeStack, AequalisScientia.TRANSMUTATION_TAG,recipe.getName());
               aq.buildItemLore(selectionModeStack,player.level().getServer());
               SoundUtils.playSongToPlayer(player, SoundEvents.ALLAY_AMBIENT_WITH_ITEM,1,0.8f);
               close();
               return true;
            }
            
            if(!(recipe instanceof CommutativeTransmutationRecipe cr)) return true;
            curRecipe = cr;
            curRecipeName = cr.getName();
            buildRecipeViewGui(curRecipeName);
         }else if(index == 8){
            int bargainTiers = ArcanaAugments.HASTY_BARGAIN.getTiers().length;
            this.costMode += type.isRight ? 0 : 2;
            if(this.costMode >= (bargainTiers+2)) this.costMode = 0;
            if(this.costMode < 0) this.costMode = (bargainTiers+1);
            this.costMode--;
            buildRecipeListGui();
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
         }else if(index == 8){
            int bargainTiers = ArcanaAugments.HASTY_BARGAIN.getTiers().length;
            this.costMode += type.isRight ? 0 : 2;
            if(this.costMode >= (bargainTiers+2)) this.costMode = 0;
            if(this.costMode < 0) this.costMode = (bargainTiers+1);
            this.costMode--;
            buildRecipeViewGui(curRecipeName);
         }
      }
      return true;
   }
   
   
   public void buildRecipeListGui(){
      GuiHelper.outlineGUI(this, ArcanaColors.EQUAYUS_COLOR, Component.literal("Transmutation Recipes").withStyle(ChatFormatting.BLUE));
      
      List<TransmutationRecipe> pageRecipes = recipes;
      int numPages = (int) Math.ceil((float)pageRecipes.size()/28.0);
      
      if(numPages > 1){
         GuiElementBuilder nextArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW)).hideDefaultTooltip();
         nextArrow.setName(Component.literal("Next Page ("+page+"/"+numPages+")").withStyle(ChatFormatting.GOLD));
         nextArrow.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(" to go to the Next Page").withStyle(ChatFormatting.DARK_PURPLE))));
         setSlot(53,nextArrow);
         
         GuiElementBuilder prevArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW)).hideDefaultTooltip();
         prevArrow.setName(Component.literal("Previous Page ("+page+"/"+numPages+")").withStyle(ChatFormatting.GOLD));
         prevArrow.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(" to go to the Previous Page").withStyle(ChatFormatting.DARK_PURPLE))));
         setSlot(45,prevArrow);
      }
      
      GuiElementBuilder costItem;
      if(this.costMode == -1){
         costItem = GuiElementBuilder.from(ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore()).hideDefaultTooltip();
         costItem.setName(Component.literal("Cost Calculation Mode").withStyle(ChatFormatting.DARK_AQUA));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(ArcanaRegistry.AEQUALIS_SCIENTIA.getTranslatedName().withStyle(ChatFormatting.AQUA)))));
         costItem.addLoreLine(Component.literal(""));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(" to change cost calculation").withStyle(ChatFormatting.BLUE)))));
      }else if(this.costMode == 0){
         costItem = GuiElementBuilder.from(new ItemStack(Items.EMERALD)).hideDefaultTooltip();
         costItem.setName(Component.literal("Cost Calculation Mode").withStyle(ChatFormatting.DARK_AQUA));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Normal").withStyle(ChatFormatting.GREEN)))));
         costItem.addLoreLine(Component.literal(""));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(" to change cost calculation").withStyle(ChatFormatting.BLUE)))));
      }else{
         costItem = GuiElementBuilder.from(new ItemStack(Items.AMETHYST_SHARD,this.costMode)).hideDefaultTooltip();
         costItem.setName(Component.literal("Cost Calculation Mode").withStyle(ChatFormatting.DARK_AQUA));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.translatable(ArcanaAugments.HASTY_BARGAIN.getTranslationKey()).withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(Component.literal(" "+LevelUtils.intToRoman(this.costMode)).withStyle(ChatFormatting.LIGHT_PURPLE)))));
         costItem.addLoreLine(Component.literal(""));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(" to change cost calculation").withStyle(ChatFormatting.BLUE)))));
      }
      setSlot(8,costItem);
      
      int k = (page-1)*28;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageRecipes.size()){
               TransmutationRecipe recipe = pageRecipes.get(k);
               GuiElementBuilder viewItem = GuiElementBuilder.from(recipe.getViewStack()).hideDefaultTooltip();
               viewItem.setName(Component.literal(recipe.getName()).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
               
               int reagent1Count, reagent2Count;
               if(this.costMode == -1){
                  reagent1Count = recipe.getAequalisReagent(recipe.getReagent1()).getCount();
                  reagent2Count = recipe.getAequalisReagent(recipe.getReagent2()).getCount();
               }else{
                  reagent1Count = recipe.getBargainReagent(recipe.getReagent1(),this.costMode).getCount();
                  reagent2Count = recipe.getBargainReagent(recipe.getReagent2(),this.costMode).getCount();
               }
               
               if(recipe instanceof CommutativeTransmutationRecipe){
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("Commutative Transmutation").withStyle(ChatFormatting.GREEN)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent1Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent1().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent2().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Click").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" to view full recipe").withStyle(ChatFormatting.DARK_GREEN)))));
               }else if(recipe instanceof InfusionTransmutationRecipe infusion){
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("Infusion Transmutation").withStyle(ChatFormatting.DARK_PURPLE)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Input: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(infusion.getInput().getCount()+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(infusion.getInput().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Output: ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(infusion.getOutput().getCount()+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(infusion.getOutput().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent1Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent1().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent2().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
               }else if(recipe instanceof PermutationTransmutationRecipe permutation){
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("Permutation Transmutation").withStyle(ChatFormatting.DARK_AQUA)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Input: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(permutation.getInput().getCount()+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(permutation.getInput().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Output: ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(permutation.getOutputDescription()).withStyle(ChatFormatting.AQUA))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent1Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent1().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent2().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
               }else if(recipe instanceof AequalisUnattuneTransmutationRecipe){
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("Aequalis Transmutation").withStyle(ChatFormatting.AQUA)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Input: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("An Aequalis Scientia").withStyle(ChatFormatting.DARK_PURPLE)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Output: ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal("An Unattuned Aequalis Scientia").withStyle(ChatFormatting.DARK_PURPLE)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent1Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent1().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent2().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
               }else if(recipe instanceof AequalisSkillTransmutationRecipe){
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("Aequalis Transmutation").withStyle(ChatFormatting.AQUA)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Input: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("An Arcana Item").withStyle(ChatFormatting.DARK_PURPLE)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Input: ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal("An Arcana Item").withStyle(ChatFormatting.DARK_PURPLE)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Input: ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.literal("Your Aequalis Scientia").withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent1Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent1().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent2().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
               }else if(recipe instanceof AequalisCatalystTransmutationRecipe){
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("Aequalis Transmutation").withStyle(ChatFormatting.AQUA)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Input: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("An Arcana Item").withStyle(ChatFormatting.DARK_PURPLE)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Input: ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal("1-4 ").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal("Catalytic Matrices").withStyle(ChatFormatting.YELLOW)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Input: ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.literal("Your Aequalis Scientia").withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent1Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent1().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(recipe.getReagent2().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
               }
               setSlot((i*9+10)+j,viewItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      setTitle(Component.literal("Transmutation Recipes"));
   }
   
   public void buildRecipeViewGui(String recipeName){
      this.curRecipeName = recipeName;
      
      GuiHelper.outlineGUI(this, ArcanaColors.EQUAYUS_COLOR, Component.literal("Transmutation Recipes").withStyle(ChatFormatting.BLUE));
      
      for(TransmutationRecipe rec : recipes){
         if(rec.getName().equals(recipeName) && rec instanceof CommutativeTransmutationRecipe crec){
            this.curRecipe = crec;
            break;
         }
      }
      if(this.curRecipe == null) return;
      
      List<ItemStack> inputs = curRecipe.getCommunalInputs();
      int numPages = (int) Math.ceil((float)inputs.size()/28.0);
      
      if(numPages > 1){
         GuiElementBuilder nextArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW)).hideDefaultTooltip();
         nextArrow.setName((Component.literal("")
               .append(Component.literal("Next Page").withStyle(ChatFormatting.GOLD))));
         nextArrow.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("("+commiePage+" of "+numPages+")").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(53,nextArrow);
         
         GuiElementBuilder prevArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW)).hideDefaultTooltip();
         prevArrow.setName((Component.literal("")
               .append(Component.literal("Prev Page").withStyle(ChatFormatting.GOLD))));
         prevArrow.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("("+commiePage+" of "+numPages+")").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(45,prevArrow);
      }
      
      int k = (commiePage-1)*28;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < inputs.size()){
               ItemStack item = inputs.get(k);
               GuiElementBuilder viewItem = GuiElementBuilder.from(item).hideDefaultTooltip();
               viewItem.setName((Component.translatable(item.getItem().getDescriptionId()).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA)));
               viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                     .append(Component.literal("Input").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" or ").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal("Focus").withStyle(ChatFormatting.DARK_GRAY)))));
               
               setSlot((i*9+10)+j,viewItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      GuiElementBuilder costItem;
      if(this.costMode == -1){
         costItem = GuiElementBuilder.from(ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore()).hideDefaultTooltip();
         costItem.setName(Component.literal("Cost Calculation Mode").withStyle(ChatFormatting.DARK_AQUA));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(ArcanaRegistry.AEQUALIS_SCIENTIA.getTranslatedName().withStyle(ChatFormatting.AQUA)))));
         costItem.addLoreLine(Component.literal(""));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(" to change cost calculation").withStyle(ChatFormatting.BLUE)))));
      }else if(this.costMode == 0){
         costItem = GuiElementBuilder.from(new ItemStack(Items.EMERALD)).hideDefaultTooltip();
         costItem.setName(Component.literal("Cost Calculation Mode").withStyle(ChatFormatting.DARK_AQUA));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Normal").withStyle(ChatFormatting.GREEN)))));
         costItem.addLoreLine(Component.literal(""));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(" to change cost calculation").withStyle(ChatFormatting.BLUE)))));
      }else{
         costItem = GuiElementBuilder.from(new ItemStack(Items.AMETHYST_SHARD,this.costMode)).hideDefaultTooltip();
         costItem.setName(Component.literal("Cost Calculation Mode").withStyle(ChatFormatting.DARK_AQUA));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.translatable(ArcanaAugments.HASTY_BARGAIN.getTranslationKey()).withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(Component.literal(" "+LevelUtils.intToRoman(this.costMode)).withStyle(ChatFormatting.LIGHT_PURPLE)))));
         costItem.addLoreLine(Component.literal(""));
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(" to change cost calculation").withStyle(ChatFormatting.BLUE)))));
      }
      setSlot(8,costItem);
      
      ItemStack reagent1, reagent2;
      if(this.costMode == -1){
         reagent1 = this.curRecipe.getAequalisReagent(this.curRecipe.getReagent1());
         reagent2 = this.curRecipe.getAequalisReagent(this.curRecipe.getReagent2());
      }else{
         reagent1 = this.curRecipe.getBargainReagent(this.curRecipe.getReagent1(),this.costMode);
         reagent2 = this.curRecipe.getBargainReagent(this.curRecipe.getReagent2(),this.costMode);
      }

      GuiElementBuilder reagent1Item = GuiElementBuilder.from(reagent1).hideDefaultTooltip();
      reagent1Item.setName((Component.translatable(reagent1.getItem().getDescriptionId()).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN)));
      reagent1Item.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(reagent1.getCount()+" ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.translatable(reagent1.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
      setSlot(48,reagent1Item);
      
      GuiElementBuilder reagent2Item = GuiElementBuilder.from(reagent2).hideDefaultTooltip();
      reagent2Item.setName((Component.translatable(reagent2.getItem().getDescriptionId()).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN)));
      reagent2Item.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(reagent2.getCount()+" ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.translatable(reagent2.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
      setSlot(50,reagent2Item);
      
      GuiElementBuilder recipeItem = new GuiElementBuilder(GraphicalItem.with(ArcanaRegistry.TRANSMUTATION_BOOK)).hideDefaultTooltip();
      recipeItem.setName((Component.literal("")
            .append(Component.literal("Transmutation Recipes").withStyle(ChatFormatting.DARK_AQUA))));
      recipeItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to view all Transmutation Recipes").withStyle(ChatFormatting.BLUE)))));
      setSlot(49,recipeItem);
      
      setTitle(Component.literal(curRecipeName+" Transmutation"));
   }
   
   @Override
   public void onTick(){
      if(blockEntity != null){
         Level world = blockEntity.getLevel();
         if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity || !blockEntity.isAssembled()){
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
