package net.borisshoes.arcananovum.bosses.dragon.guis;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;

public class PuzzleGui extends SimpleGui {
   
   private Puzzle curPuzzle;
   private int level = 1;
   private boolean complete = false;
   private final DragonBossFight.ReclaimState reclaimState;
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param type                        the screen handler that the client should display
    * @param player                      the player to server this gui to
    */
   public PuzzleGui(ScreenHandlerType<?> type, ServerPlayerEntity player, DragonBossFight.ReclaimState reclaimState){
      super(type, player, false);
      this.reclaimState = reclaimState;
      setTitle(Text.literal("Tower Reclamation"));
   }
   
   public void buildPuzzle(){
      curPuzzle = Puzzle.generatePuzzle(level);
      ArrayList<Item> puzzleItems = curPuzzle.getPuzzleItems();
      Item targetItem = curPuzzle.getTargetItem();
      
      MiscUtils.outlineGUI(this, ArcanaColors.ARCANA_COLOR,Text.literal("Tower Reclamation").formatted(Formatting.LIGHT_PURPLE));
      
      GuiElementBuilder targetGuiItem = new GuiElementBuilder(targetItem).hideDefaultTooltip();
      targetGuiItem.setName((Text.literal("")
            .append(Text.translatable(targetItem.getTranslationKey()).formatted(Formatting.AQUA,Formatting.BOLD))));
      targetGuiItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click this item!").formatted(Formatting.DARK_PURPLE)))));
      setSlot(4,targetGuiItem);
      
      for(int i = 46; i < 53; i++){
         int lvl = i-45;
         GuiElementBuilder progressGuiItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.PAGE_BG,lvl <= level ? ArcanaColors.EQUAYUS_COLOR : ArcanaColors.DARK_COLOR)).hideDefaultTooltip();
         progressGuiItem.setName((Text.literal("("+level+"/7)").formatted(Formatting.AQUA,Formatting.BOLD)));
         setSlot(i,progressGuiItem);
      }
      
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < puzzleItems.size()){
               GuiElementBuilder puzzleItem = new GuiElementBuilder(puzzleItems.get(k)).hideDefaultTooltip();
               puzzleItem.setName((Text.literal("")
                     .append(Text.translatable(puzzleItems.get(k).getTranslationKey()).formatted(Formatting.YELLOW))));
               
               setSlot((i*9+10)+j,puzzleItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      boolean indexInCenter = index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8;
      int ind = (7*(index/9 - 1) + (index % 9 - 1));
      
      if(indexInCenter){
         Item selected = curPuzzle.getPuzzleItems().get(ind);
         if(selected == Items.AIR) return true;
         
         if(selected == curPuzzle.getTargetItem()){
            if(level == 7){
               complete = true;
               close();
            }else{
               level++;
               buildPuzzle();
            }
         }else{
            complete = false;
            close();
         }
      }
      return true;
   }
   
   @Override
   public void onClose(){
      if(reclaimState != null){
         if(complete){
            reclaimState.playerSolved();
            player.sendMessage(Text.literal("The Tower's Arcana Surges Through You!").formatted(Formatting.BOLD,Formatting.DARK_AQUA),true);
         }else{
            reclaimState.setPlayer(null);
            player.sendMessage(Text.literal("You Fail To Channel The Tower's Magic").formatted(Formatting.ITALIC,Formatting.RED),true);
         }
      }else{
         if(complete){
            player.sendMessage(Text.literal("Success!").formatted(Formatting.BOLD,Formatting.DARK_AQUA));
         }else{
            player.sendMessage(Text.literal("Fail!").formatted(Formatting.ITALIC,Formatting.RED));
         }
      }
   }
   
   private static class Puzzle{
      private ArrayList<Item> puzzleItems;
      private Item targetItem;
      private static final int[] itemsPerLevel = {4,8,16,20,28,28,28};
      private static final Item[] targetItems = {Items.END_CRYSTAL,Items.OBSIDIAN,Items.ENDER_EYE,Items.AMETHYST_SHARD,Items.CRYING_OBSIDIAN,Items.NETHER_STAR,Items.END_CRYSTAL};
   
      public static Puzzle generatePuzzle(int level){
         Puzzle puzzle = new Puzzle();
         Item targetItem = targetItems[MathHelper.clamp(level-1,0,6)];
         int numItems = itemsPerLevel[MathHelper.clamp(level-1,0,6)];
         ArrayList<Item> puzzleItems = new ArrayList<>();
         
         for(int i = 0; i < 28; i++){
            Item item = Items.AIR;
            if(i == 0){
               item = targetItem;
            }else if(i < numItems){
               do{
                  item = Registries.ITEM.get(((int)(Math.random()*Registries.ITEM.size())));
               }while(item == targetItem || !Registries.ITEM.getId(item).getNamespace().equals("minecraft"));
            }
            puzzleItems.add(item);
         }
         
         Collections.shuffle(puzzleItems);
         puzzle.puzzleItems = puzzleItems;
         puzzle.targetItem = targetItem;
         
         return puzzle;
      }
      
      public ArrayList<Item> getPuzzleItems(){
         return puzzleItems;
      }
      
      public Item getTargetItem(){
         return targetItem;
      }
   }
}

