package net.borisshoes.arcananovum.bosses.dragon.guis;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

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
   public PuzzleGui(MenuType<?> type, ServerPlayer player, DragonBossFight.ReclaimState reclaimState){
      super(type, player, false);
      this.reclaimState = reclaimState;
      setTitle(Component.literal("Tower Reclamation"));
   }
   
   public void buildPuzzle(){
      curPuzzle = Puzzle.generatePuzzle(level);
      ArrayList<Item> puzzleItems = curPuzzle.getPuzzleItems();
      Item targetItem = curPuzzle.getTargetItem();
      
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR, Component.literal("Tower Reclamation").withStyle(ChatFormatting.LIGHT_PURPLE));
      
      GuiElementBuilder targetGuiItem = new GuiElementBuilder(targetItem).hideDefaultTooltip();
      targetGuiItem.setName((Component.literal("")
            .append(Component.translatable(targetItem.getDescriptionId()).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))));
      targetGuiItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click this item!").withStyle(ChatFormatting.DARK_PURPLE)))));
      setSlot(4,targetGuiItem);
      
      for(int i = 46; i < 53; i++){
         int lvl = i-45;
         GuiElementBuilder progressGuiItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,lvl <= level ? ArcanaColors.EQUAYUS_COLOR : ArcanaColors.DARK_COLOR)).hideDefaultTooltip();
         progressGuiItem.setName((Component.literal("("+level+"/7)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)));
         setSlot(i,progressGuiItem);
      }
      
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < puzzleItems.size()){
               GuiElementBuilder puzzleItem = new GuiElementBuilder(puzzleItems.get(k)).hideDefaultTooltip();
               puzzleItem.setName((Component.literal("")
                     .append(Component.translatable(puzzleItems.get(k).getDescriptionId()).withStyle(ChatFormatting.YELLOW))));
               
               setSlot((i*9+10)+j,puzzleItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
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
            player.displayClientMessage(Component.literal("The Tower's Arcana Surges Through You!").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA),true);
         }else{
            reclaimState.setPlayer(null);
            player.displayClientMessage(Component.literal("You Fail To Channel The Tower's Magic").withStyle(ChatFormatting.ITALIC, ChatFormatting.RED),true);
         }
      }else{
         if(complete){
            player.sendSystemMessage(Component.literal("Success!").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA));
         }else{
            player.sendSystemMessage(Component.literal("Fail!").withStyle(ChatFormatting.ITALIC, ChatFormatting.RED));
         }
      }
   }
   
   private static class Puzzle{
      private ArrayList<Item> puzzleItems;
      private Item targetItem;
      private static final int[] itemsPerLevel = {28,28,28,28,28,28,28};
      private static final Item[] targetItems = {Items.END_CRYSTAL, Items.OBSIDIAN, Items.ENDER_EYE, Items.AMETHYST_SHARD, Items.CRYING_OBSIDIAN, Items.NETHER_STAR, Items.END_CRYSTAL};
   
      public static Puzzle generatePuzzle(int level){
         Puzzle puzzle = new Puzzle();
         Item targetItem = targetItems[Mth.clamp(level-1,0,6)];
         int numItems = itemsPerLevel[Mth.clamp(level-1,0,6)];
         ArrayList<Item> puzzleItems = new ArrayList<>();
         
         for(int i = 0; i < 28; i++){
            Item item = Items.AIR;
            if(i == 0){
               item = targetItem;
            }else if(i < numItems){
               do{
                  item = BuiltInRegistries.ITEM.byId(((int)(Math.random()* BuiltInRegistries.ITEM.size())));
               }while(item == targetItem || !BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("minecraft"));
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

