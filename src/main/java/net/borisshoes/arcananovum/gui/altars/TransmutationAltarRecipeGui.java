package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltar;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.AequalisScientia;
import net.borisshoes.arcananovum.recipes.transmutation.*;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.gui.*;
import net.borisshoes.borislib.testmod.TestGui;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Predicate;

public class TransmutationAltarRecipeGui extends PagedMultiGui {
   
   private final TransmutationAltarBlockEntity blockEntity;
   private final SimpleGui returnGui;
   private ItemStack selectionModeStack;
   private int costMode;
   private CommutativeTransmutationRecipe curRecipe = null;
   
   public TransmutationAltarRecipeGui(ServerPlayer player, SimpleGui returnGui, Optional<TransmutationAltarBlockEntity> altarOpt){
      super(MenuType.GENERIC_9x6, player);
      this.blockEntity = altarOpt.orElse(null);
      this.returnGui = returnGui;
      this.costMode = blockEntity == null ? 0 : ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      setTitle(Component.literal("Transmutation Altar"));
      
      action1TextColor(ChatFormatting.LIGHT_PURPLE.getColor().intValue());
      action2TextColor(ChatFormatting.AQUA.getColor().intValue());
      action3TextColor(ChatFormatting.DARK_AQUA.getColor().intValue());
      primaryTextColor(ChatFormatting.AQUA.getColor().intValue());
      secondaryTextColor(ChatFormatting.DARK_PURPLE.getColor().intValue());
      
      blankItem(GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,0x9af7ff)).hideTooltip());
      
      addMode(TransmutationAltar.getUnlockedRecipes(player),
            (recipe, index) -> {
               GuiElementBuilder viewItem = GuiElementBuilder.from(recipe.getViewStack()).hideDefaultTooltip();
               viewItem.setName(recipe.getName().withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
               
               ItemStack reagent1 = recipe.getComputedReagent1(recipe.getExampleReagent1(),this.costMode);
               ItemStack reagent2 = recipe.getComputedReagent2(recipe.getExampleReagent2(),this.costMode);
               int reagent1Count = reagent1.getCount();
               int reagent2Count = reagent2.getCount();
               
               if(recipe instanceof CommutativeTransmutationRecipe){
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("Commutative Transmutation").withStyle(ChatFormatting.GREEN)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent1Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(reagent1.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(reagent2.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Click").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" to view full recipe").withStyle(ChatFormatting.DARK_GREEN)))));
               }else if(recipe instanceof InfusionTransmutationRecipe infusion){
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("Infusion Transmutation").withStyle(ChatFormatting.DARK_PURPLE)));
                  viewItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Input: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(infusion.getInputCount()+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(infusion.getInputName().withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Output: ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(infusion.getOutput().getCount()+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(infusion.getOutput().getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent1Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(reagent1.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(reagent2.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
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
                        .append(Component.translatable(reagent1.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(reagent2.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
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
                        .append(Component.translatable(reagent1.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(reagent2.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
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
                        .append(Component.translatable(reagent1.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(reagent2.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
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
                        .append(Component.translatable(reagent1.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
                  viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Reagent: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(reagent2Count+" ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable(reagent2.getItem().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
               }
               return viewItem;
            },
            (recipe, index, clickType) -> {
               if(selectionModeStack != null && ArcanaItemUtils.identifyItem(selectionModeStack) instanceof AequalisScientia aq){
                  if(recipe.getId().equals("aequalis_reconfiguration")) ArcanaAchievements.grant(player,ArcanaAchievements.FRACTAL_ATTUNEMENT);
                  ArcanaItem.putProperty(selectionModeStack, AequalisScientia.TRANSMUTATION_TAG,recipe.getId());
                  aq.buildItemLore(selectionModeStack,player.level().getServer());
                  SoundUtils.playSongToPlayer(player, SoundEvents.ALLAY_AMBIENT_WITH_ITEM,1,0.8f);
                  close();
                  return;
               }
               
               if(!(recipe instanceof CommutativeTransmutationRecipe cr)) return;
               curRecipe = cr;
               switchMode(1);
            },
            TransmutationSort.CATEGORY,
            TransmutationFilter.NONE
      );
      
      addMode(new ArrayList<>(),
            (item, index) -> {
               GuiElementBuilder viewItem = GuiElementBuilder.from(item).hideDefaultTooltip();
               viewItem.setName((Component.translatable(item.getItem().getDescriptionId()).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA)));
               viewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                     .append(Component.literal("Input").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" or ").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal("Focus").withStyle(ChatFormatting.DARK_GRAY)))));
               return viewItem;
            },
            (item, index, clickType) -> {},
            ItemSort.ALPHABETICAL, null
      );
      
      
   }
   
   public void enableSelectionMode(ItemStack stack){
      selectionModeStack = stack;
      GuiMode<TransmutationRecipe> recipeMode = getMode(0);
      List<TransmutationRecipe> filtered = TransmutationAltar.getUnlockedRecipes(player);
      filtered.removeIf(recipe -> recipe instanceof AequalisCatalystTransmutationRecipe || recipe instanceof AequalisSkillTransmutationRecipe);
      recipeMode.setItems(new ArrayList<>(filtered));
      if(stack.is(ArcanaRegistry.AEQUALIS_SCIENTIA.getItem())) this.costMode = -1;
   }
   
   @Override
   public void buildPage(){
      if(getCurrentModeInd() == 1){
         if(curRecipe == null) return;
         GuiMode<ItemStack> config = getCurrentMode();
         config.setItems(new ArrayList<>(curRecipe.getDisplayStacks()));
      }
      GuiHelper.outlineGUI(this, ArcanaColors.EQUAYUS_COLOR, Component.literal("Transmutation Recipes").withStyle(ChatFormatting.BLUE));
      super.buildPage();
      
      GuiElementBuilder costItem;
      if(this.costMode == -1){
         costItem = GuiElementBuilder.from(ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore()).hideDefaultTooltip();
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(ArcanaRegistry.AEQUALIS_SCIENTIA.getTranslatedName().withStyle(ChatFormatting.AQUA)))));
      }else if(this.costMode == 0){
         costItem = GuiElementBuilder.from(new ItemStack(Items.EMERALD)).hideDefaultTooltip();
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Normal").withStyle(ChatFormatting.GREEN)))));
      }else{
         costItem = GuiElementBuilder.from(new ItemStack(Items.AMETHYST_SHARD,this.costMode)).hideDefaultTooltip();
         costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.translatable(ArcanaAugments.HASTY_BARGAIN.getTranslationKey()).withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(Component.literal(" "+ LevelUtils.intToRoman(this.costMode)).withStyle(ChatFormatting.LIGHT_PURPLE)))));
      }
      costItem.setName(Component.literal("Cost Calculation Mode").withStyle(ChatFormatting.DARK_AQUA));
      costItem.addLoreLine(Component.literal(""));
      costItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" to change cost calculation").withStyle(ChatFormatting.BLUE)))));
      costItem.setCallback((clickType) -> {
         int bargainTiers = ArcanaAugments.HASTY_BARGAIN.getTiers().length;
         this.costMode += clickType.isRight ? 0 : 2;
         if(this.costMode >= (bargainTiers+2)) this.costMode = 0;
         if(this.costMode < 0) this.costMode = (bargainTiers+1);
         this.costMode--;
         buildPage();
      });
      setSlot(4,costItem);
      
      if(getCurrentModeInd() == 0){
         setTitle(Component.literal("Transmutation Recipes"));
      }else if(getCurrentModeInd() == 1){
         
         ItemStack reagent1 = this.curRecipe.getComputedReagent1(this.curRecipe.getExampleReagent1(),this.costMode);
         ItemStack reagent2 = this.curRecipe.getComputedReagent2(this.curRecipe.getExampleReagent2(),this.costMode);
         
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
         recipeItem.setCallback((clickType) -> {
            curRecipe = null;
            getMode(1).setPageNum(1);
            switchMode(0);
         });
         setSlot(49,recipeItem);
         
         setTitle(curRecipe.getName().append(Component.literal(" Transmutation")));
      }
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
   
   private static class ItemSort extends GuiSort<ItemStack> {
      public static final List<ItemSort> SORTS = new ArrayList<>();
      
      public static final ItemSort ALPHABETICAL = new ItemSort("gui.borislib.alphabetical", ChatFormatting.GREEN.getColor().intValue(),
            Comparator.comparing((stack) -> stack.getDisplayName().getString()));
      
      private ItemSort(String key, int color, Comparator<ItemStack> comparator){
         super(key, color, comparator);
         SORTS.add(this);
      }
      
      @Override
      protected List<ItemSort> getList(){
         return SORTS;
      }
      
      public ItemSort getStaticDefault(){
         return ALPHABETICAL;
      }
   }
   
   private static class TransmutationFilter extends GuiFilter<TransmutationRecipe> {
      public static final List<TransmutationFilter> FILTERS = new ArrayList<>();
      
      public static final TransmutationFilter NONE = new TransmutationFilter("gui.borislib.none", ChatFormatting.WHITE.getColor().intValue(), entry -> true);
      public static final TransmutationFilter COMMUTATIVE = new TransmutationFilter("gui.arcananovum.commutative", ChatFormatting.GREEN.getColor().intValue(), entry -> entry instanceof CommutativeTransmutationRecipe);
      public static final TransmutationFilter INFUSION = new TransmutationFilter("gui.arcananovum.infusion", ChatFormatting.DARK_PURPLE.getColor().intValue(), entry -> entry instanceof InfusionTransmutationRecipe);
      public static final TransmutationFilter PERMUTATION = new TransmutationFilter("gui.arcananovum.permutation", ChatFormatting.DARK_AQUA.getColor().intValue(), entry -> entry instanceof PermutationTransmutationRecipe);
      public static final TransmutationFilter AEQUALIS = new TransmutationFilter("gui.arcananovum.aequalis", ChatFormatting.AQUA.getColor().intValue(), entry -> (entry instanceof AequalisSkillTransmutationRecipe) || (entry instanceof AequalisCatalystTransmutationRecipe) || (entry instanceof AequalisUnattuneTransmutationRecipe));
      
      private TransmutationFilter(String key, int color, Predicate<TransmutationRecipe> predicate){
         super(key, color, predicate);
         FILTERS.add(this);
      }
      
      @Override
      protected List<TransmutationFilter> getList(){
         return FILTERS;
      }
      
      public TransmutationFilter getStaticDefault(){
         return NONE;
      }
   }
   
   private static class TransmutationSort extends GuiSort<TransmutationRecipe> {
      public static final List<TransmutationSort> SORTS = new ArrayList<>();
      
      public static final TransmutationSort CATEGORY = new TransmutationSort("gui.arcananovum.category", ChatFormatting.LIGHT_PURPLE.getColor().intValue(),
            Comparator.<TransmutationRecipe>comparingInt((recipe) -> {
               if(recipe instanceof CommutativeTransmutationRecipe) return 1;
               if(recipe instanceof InfusionTransmutationRecipe) return 2;
               if(recipe instanceof PermutationTransmutationRecipe) return 3;
               if(recipe instanceof AequalisSkillTransmutationRecipe) return 4;
               if(recipe instanceof AequalisCatalystTransmutationRecipe) return 5;
               if(recipe instanceof AequalisUnattuneTransmutationRecipe) return 6;
               return 7;
            }).thenComparing(TransmutationRecipe::getId));
      public static final TransmutationSort ALPHABETICAL = new TransmutationSort("gui.borislib.alphabetical", ChatFormatting.GREEN.getColor().intValue(),
            Comparator.comparing(TransmutationRecipe::getId));
      
      private TransmutationSort(String key, int color, Comparator<TransmutationRecipe> comparator){
         super(key, color, comparator);
         SORTS.add(this);
      }
      
      @Override
      protected List<TransmutationSort> getList(){
         return SORTS;
      }
      
      public TransmutationSort getStaticDefault(){
         return CATEGORY;
      }
   }
}
