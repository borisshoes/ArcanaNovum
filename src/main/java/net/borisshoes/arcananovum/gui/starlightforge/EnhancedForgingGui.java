package net.borisshoes.arcananovum.gui.starlightforge;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.*;

import static net.borisshoes.borislib.BorisLib.BORISLIB_ITEM_DATA;

public class EnhancedForgingGui extends SimpleGui {
   private final StarlightForgeBlockEntity blockEntity;
   private final EnhancedForgingGame game;
   private final DefaultedList<ItemStack> ingredients;
   private final DefaultedList<ItemStack> remainders;
   private final ItemStack enhancedStack;
   
   private EFItem selectedItem = EFItem.NOVA;
   private long tickCount = 0;
   private int endingAnim = -1;
   private boolean turnMode = false;
   private boolean cinematicMode = false;
   private boolean animated = false;
   private boolean completed = false;
   private boolean paid = false;
   private boolean fast = false;
   private boolean showCodes = false;
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param player                the player to server this gui to
    */
   public EnhancedForgingGui(ServerPlayerEntity player, StarlightForgeBlockEntity blockEntity, ItemStack enhancedStack, DefaultedList<ItemStack> ingredients, DefaultedList<ItemStack> remainders){
      super(ScreenHandlerType.GENERIC_9X6, player, false);
      this.blockEntity = blockEntity;
      this.enhancedStack = enhancedStack;
      this.ingredients = ingredients;
      this.remainders = remainders;
      this.game = new EnhancedForgingGame(blockEntity.getStartingValue(), blockEntity.getPlanetCount(), blockEntity.getStarCount(), blockEntity.getSeed());
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      boolean onBoard = index % 9 < 7 && index < 54;
      if(animated || endingAnim != -1) return true;
      
      if(index == 8){
         this.turnMode = !turnMode;
         buildGui();
      }else if(index == 26){
         if(type.isRight){
            this.selectedItem = EFItem.cycleItem(selectedItem,true);
            buildGui();
         }else if(type.isLeft){
            this.selectedItem = EFItem.cycleItem(selectedItem,false);
            buildGui();
         }else if(type == ClickType.MOUSE_LEFT_SHIFT){
            this.selectedItem = EFItem.NOVA;
            buildGui();
         }
      }else if(index == 53){
         if(type == ClickType.MOUSE_RIGHT){
            this.showCodes = !showCodes;
            buildGui();
         }else{
            if(game.hasNextTurn()){
               if(game.getTurn() == 0 && !paid){
                  if(MinecraftUtils.removeItems(player,ArcanaRegistry.STARDUST,game.getTotalCost())){
                     turnMode = false;
                     animated = true;
                     cinematicMode = true;
                     paid = true;
                     fast = type == ClickType.MOUSE_LEFT_SHIFT;
                     buildGui();
                  }else{
                     player.sendMessage(Text.literal("You do not have enough ").formatted(Formatting.RED,Formatting.ITALIC)
                           .append(Text.translatable(ArcanaRegistry.STARDUST.getTranslationKey()).formatted(Formatting.YELLOW,Formatting.ITALIC)),false);
                     SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
                  }
               }
            }
         }
      }else if(index == 35 && type.isRight){
         game.resetBoard();
         game.applyChanges();
         buildGui();
      }else if(onBoard && !type.isDragging && game.getTurn() == 0){
         int x = (index % 9);
         int y = index / 9;
         
         if(type == ClickType.MOUSE_LEFT_SHIFT){ // Tile change / Reset
            if(this.selectedItem == game.getItemAt(x,y)){
               game.removeChanges(x,y);
            }else{
               game.addChange(new EnhancedForgingGame.EFChange(EnhancedForgingGame.EFChangeType.TILE_CHANGE,x,y,Optional.of(this.selectedItem)));
            }
         }else if(type == ClickType.MOUSE_LEFT){ // Turn Decrease
            game.addChange(new EnhancedForgingGame.EFChange(EnhancedForgingGame.EFChangeType.TURN_DECREASE,x,y,Optional.empty()));
         }else if(type == ClickType.MOUSE_RIGHT){ // Turn Increase
            game.addChange(new EnhancedForgingGame.EFChange(EnhancedForgingGame.EFChangeType.TURN_INCREASE,x,y,Optional.empty()));
         }
         game.applyChanges();
         buildGui();
      }
      
      return true;
   }
   
   public boolean advanceGameStep(){
      boolean changed = game.nextTurn();
      buildGui();
      return changed;
   }
   
   @Override
   public void onTick(){
      World world = blockEntity.getWorld();
      if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity || !blockEntity.isAssembled()){
         this.close();
      }
      
      tickCount++;
      super.onTick();
      if(!animated && endingAnim == -1) return;
      
      if(tickCount % (fast ? 5 : 10) != 0) return;
      
      if(endingAnim >= 0){
         
         if(endingAnim == 0){
            List<Integer> slots = new ArrayList<>();
            for(int i = 0; i < getSize(); i++){
               ItemStack stack = getSlot(i).getItemStack();
               if(stack.isOf(BorisLib.GRAPHICAL_ITEM)){
                  Identifier id = Identifier.of(BORISLIB_ITEM_DATA.getStringProperty(stack, GraphicalItem.GRAPHICS_TAG));
                  if(!id.equals(EFItem.STAR.displayElement.id()) && !id.equals(EFItem.PULSAR.displayElement.id()) && !id.equals(EFItem.GAS.displayElement.id()) && !id.equals(GraphicalItem.BLACK.id())){
                     slots.add(i);
                  }
               }
            }
            
            if(slots.isEmpty()){
               endingAnim = 1;
            }else{
               GuiElementBuilder elem = EFItem.getGuiElement(EFItem.GAS).hideTooltip();
               
               Collections.shuffle(slots);
               for(int i = 0; i < 3 && i < slots.size(); i++){
                  setSlot(slots.get(i),elem);
               }
               return;
            }
         }
         
         if(endingAnim == 5){
            onCompletion();
            endingAnim = -1;
            return;
         }
         
         endingAnim ++;
         return;
      }
      
      if(tickCount % 20 != 0) return;
      
      boolean didTurn = false;
      
      while(game.hasNextTurn() && !advanceGameStep()){
         didTurn = true;
      }
      if(!didTurn && !game.hasNextTurn()){
         animated = false;
         endingAnim = 0;
      }
   }
   
   public void buildGui(){
      setTitle(Text.literal("Stardust Infusion"));
      
      setSlot(7,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.LAPIS_COLOR)).setName(Text.empty()).hideTooltip());
      setSlot(16,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.LAPIS_COLOR)).setName(Text.empty()).hideTooltip());
      setSlot(17,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,ArcanaColors.LAPIS_COLOR)).setName(Text.empty()).hideTooltip());
      setSlot(25,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.LAPIS_COLOR)).setName(Text.empty()).hideTooltip());
      setSlot(34,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.LAPIS_COLOR)).setName(Text.empty()).hideTooltip());
      setSlot(43,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.LAPIS_COLOR)).setName(Text.empty()).hideTooltip());
      setSlot(44,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,ArcanaColors.LAPIS_COLOR)).setName(Text.empty()).hideTooltip());
      setSlot(52,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.LAPIS_COLOR)).setName(Text.empty()).hideTooltip());
      
      GuiElementBuilder orderItem = GuiElementBuilder.from(Items.CLOCK.getDefaultStack()).hideDefaultTooltip();
      orderItem.setName(Text.literal("Show Item Order").formatted(Formatting.YELLOW));
      orderItem.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to show the order of tiles.").formatted(Formatting.LIGHT_PURPLE))));
      orderItem.addLoreLine(Text.empty());
      orderItem.addLoreLine(TextUtils.removeItalics(Text.literal("Stack size indicates the order of tiles.").formatted(Formatting.GRAY)));
      orderItem.addLoreLine(TextUtils.removeItalics(Text.literal("Newly created items activate after all queued tiles.").formatted(Formatting.GRAY)));
      orderItem.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Changing turn order costs ").formatted(Formatting.GRAY))
            .append(Text.literal(""+ EnhancedForgingGame.EFChange.TURN_CHANGE_COST).formatted(Formatting.GOLD))
            .append(Text.literal(" stardust.").formatted(Formatting.YELLOW))));
      orderItem.setMaxCount(99);
      orderItem.setCount(game.getTurn()+1);
      setSlot(8,orderItem);
      
      GuiElementBuilder runItem = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CONFIRM)).hideDefaultTooltip();
      runItem.setName(Text.literal("Activate Forge").formatted(Formatting.YELLOW));
      runItem.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to activate the forge.").formatted(Formatting.LIGHT_PURPLE))));
      if(showCodes){
         runItem.addLoreLine(Text.literal(""));
         runItem.addLoreLine(TextUtils.removeItalics(Text.literal("Board ID: "+game.getStartingCode()).formatted(Formatting.DARK_GRAY)));
         runItem.addLoreLine(TextUtils.removeItalics(Text.literal("Solution ID: "+game.getGameCode()).formatted(Formatting.DARK_GRAY)));
      }
      setSlot(53,runItem);
      
      int playCost = EnhancedForgingGame.PLAY_COST;
      int turnCost = game.getTurnChangeCost();
      int tileCost = game.getTileChangeCost();
      int totalCost = game.getTotalCost();
      
      GuiElementBuilder stardustItem = GuiElementBuilder.from(MinecraftUtils.removeLore(ArcanaRegistry.STARDUST.getDefaultStack())).hideDefaultTooltip();
      stardustItem.setName(Text.literal("Requires 10 Stardust").formatted(Formatting.YELLOW));
      stardustItem.setName(Text.literal("")
            .append(Text.literal("Requires ").formatted(Formatting.YELLOW))
            .append(Text.literal(""+totalCost).formatted(Formatting.GOLD))
            .append(Text.literal(" Stardust").formatted(Formatting.YELLOW)));
      stardustItem.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Base Cost: ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(playCost+" Stardust").formatted(Formatting.GOLD))));
      stardustItem.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Turn Order Cost: ").formatted(Formatting.GREEN))
            .append(Text.literal(turnCost+" Stardust").formatted(Formatting.GOLD))));
      stardustItem.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Tile Cost: ").formatted(Formatting.AQUA))
            .append(Text.literal(tileCost+" Stardust").formatted(Formatting.GOLD))));
      stardustItem.addLoreLine(Text.literal(""));
      stardustItem.addLoreLine(TextUtils.removeItalics(Text.literal("Changing tiles or tile order costs stardust.").formatted(Formatting.GRAY)));
      stardustItem.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Forging has a base cost of ").formatted(Formatting.GRAY))
            .append(Text.literal(""+playCost).formatted(Formatting.GOLD))
            .append(Text.literal(" stardust.").formatted(Formatting.YELLOW))));
      stardustItem.addLoreLine(Text.literal(""));
      stardustItem.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.AQUA)).append(Text.literal(" to reset the layout.").formatted(Formatting.LIGHT_PURPLE))));
      stardustItem.setMaxCount(99);
      stardustItem.setCount(totalCost);
      setSlot(35,stardustItem);
      
      GuiElementBuilder placementItem = GuiElementBuilder.from(GraphicalItem.with(this.selectedItem.displayElement)).hideDefaultTooltip();
      placementItem.setName(Text.literal("")
            .append(Text.literal("Change tile to ").formatted(Formatting.YELLOW))
            .append(this.selectedItem.name));
      placementItem.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("This tile costs ").formatted(Formatting.GRAY))
            .append(Text.literal(""+this.selectedItem.placementCost).formatted(Formatting.GOLD))
            .append(Text.literal(" stardust.").formatted(Formatting.YELLOW))));
      placementItem.addLoreLine(Text.literal(""));
      for(Text text : this.selectedItem.description){
         placementItem.addLoreLine(text);
      }
      placementItem.addLoreLine(Text.literal(""));
      placementItem.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Click or Right Click").formatted(Formatting.AQUA)).append(Text.literal(" to cycle tile types.").formatted(Formatting.LIGHT_PURPLE))));
      placementItem.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Shift Click").formatted(Formatting.AQUA)).append(Text.literal(" a tile to convert it.").formatted(Formatting.LIGHT_PURPLE))));
      placementItem.setCount(Math.max(1,this.selectedItem.placementCost));
      setSlot(26,placementItem);
      
      Pair<EFItem,Integer>[][] board = game.getBoard();
      
      for(int x = 0; x < game.width; x++){
         for(int y = 0; y < game.height; y++){
            Pair<EFItem,Integer> tile = board[x][y];
            GuiElementBuilder elem = EFItem.getGuiElement(tile.getLeft());
            if(tile.getLeft() != EFItem.PLANET){
               elem.addLoreLine(Text.literal(""));
               if(tile.getLeft() == this.selectedItem){
                  elem.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Shift Click").formatted(Formatting.AQUA)).append(Text.literal(" to reset this tile.").formatted(Formatting.LIGHT_PURPLE))));
               }else{
                  elem.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Shift Click").formatted(Formatting.AQUA)).append(Text.literal(" to change tile type to ").formatted(Formatting.LIGHT_PURPLE)).append(this.selectedItem.name)));
               }
               if(EFItem.hasTurn(tile.getLeft())){
                  elem.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Left Click").formatted(Formatting.GREEN)).append(Text.literal(" to hasten this tile's turn.").formatted(Formatting.LIGHT_PURPLE))));
                  elem.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to delay this tile's turn.").formatted(Formatting.LIGHT_PURPLE))));
               }
            }
            
            if(turnMode){
               elem.setMaxCount(99);
               elem.setCount(tile.getRight()+1);
            }
            
            if(cinematicMode){
               elem.hideTooltip();
               setSlot((x+1) + 9*y, elem);
            }else{
               setSlot(x + 9*y, elem);
            }
         }
      }
      
      if(cinematicMode){
         for(int i = 0; i < 6; i++){
            GuiElementBuilder elem = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.BLACK)).hideTooltip();
            setSlot(i*9, elem);
            setSlot(i*9+8, elem);
         }
      }
   }
   
   public void onCompletion(){
      if(!(blockEntity.getWorld() instanceof ServerWorld world)) return;
      completed = true;
      blockEntity.addSeedUse();
      
      double percentile = EnhancedStatUtils.generatePercentile(game.getStarCount());
      
      if(enhancedStack.isOf(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER)){
         enhancedStack.setCount((int)(6*Math.pow(percentile,5) + 5*percentile + 5));
      }else{
         EnhancedStatUtils.enhanceItem(enhancedStack, percentile);
         ArcanaNovum.data(player).setResearchTask(ResearchTasks.INFUSE_ITEM, true);
      }
      
      ArcanaEffectUtils.enhancedForgingAnim(world,blockEntity.getPos(),enhancedStack,0,fast ? 1.75 : 1);
      
      final int finalCost = game.getTotalCost();
      
      BorisLib.addTickTimerCallback(world, new GenericTimer(fast ? (int) (350 / 1.75) : 350, () -> {
         if(percentile >= 0.99 && !enhancedStack.isOf(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER)){
            ArcanaAchievements.grant(player,ArcanaAchievements.MASTER_CRAFTSMAN.id);
         }
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.STARDUST_INFUSION_PER_STARDUST)*finalCost);
         Vec3d pos = blockEntity.getPos().toCenterPos().add(0,2,0);
         ItemScatterer.spawn(world,pos.x,pos.y,pos.z,enhancedStack);
      }));
      
      paid = false;
      close();
   }
   
   @Override
   public void close(){
      super.close();
   }
   
   @Override
   public void onClose(){
      DefaultedList<ItemStack> list = completed ? remainders : ingredients;
      
      SimpleInventory returnInv = new SimpleInventory(list.size()+1);
      for(ItemStack stack : list){
         returnInv.addStack(stack);
      }
      if(paid){
         int cost = game.getTotalCost();
         while(cost > 0){
            int amnt = Math.min(cost,ArcanaRegistry.STARDUST.getMaxCount());
            returnInv.addStack(new ItemStack(ArcanaRegistry.STARDUST,amnt));
            cost -= amnt;
         }
      }
      MinecraftUtils.returnItems(returnInv,player);
   }
}

enum EFItem {
   STAR("star",
         Text.literal("Star").formatted(Formatting.YELLOW,Formatting.BOLD),
         List.of(
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Turns").formatted(Formatting.GRAY))
                     .append(Text.literal(" 4 adjacent ").formatted(Formatting.RED))
                     .append(Text.literal("Gas").formatted(Formatting.DARK_PURPLE))
                     .append(Text.literal(" into ").formatted(Formatting.GRAY))
                     .append(Text.literal("Plasma").formatted(Formatting.GOLD))),
               Text.literal(""),
               TextUtils.removeItalics(Text.literal("-- Increases Infusion Result --").formatted(Formatting.YELLOW,Formatting.BOLD))
         ), 64,Integer.MAX_VALUE, ArcanaRegistry.STAR),
   GAS("gas",
         Text.literal("Gas").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),
         List.of(
               TextUtils.removeItalics(Text.literal("Does Nothing").formatted(Formatting.GRAY))
         ), 0,Integer.MAX_VALUE, ArcanaRegistry.GAS),
   PLASMA("plasma",
         Text.literal("Plasma").formatted(Formatting.GOLD,Formatting.BOLD),
         List.of(
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Fuel for other tiles.").formatted(Formatting.GRAY)))
         ), 7,1, ArcanaRegistry.PLASMA),
   BLACK_HOLE("black_hole",
         Text.literal("Black Hole").formatted(Formatting.BLUE,Formatting.BOLD),
         List.of(
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Converts").formatted(Formatting.GRAY))
                     .append(Text.literal(" 8 surrounding tiles ").formatted(Formatting.RED))
                     .append(Text.literal("into ").formatted(Formatting.GRAY))
                     .append(Text.literal("Plasma").formatted(Formatting.GOLD))),
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Turns into ").formatted(Formatting.GRAY))
                     .append(Text.literal("Quasar").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal(" if it destroys a ").formatted(Formatting.GRAY))
                     .append(Text.literal("Star").formatted(Formatting.YELLOW))
                     .append(Text.literal(", ").formatted(Formatting.GRAY))
                     .append(Text.literal("Quasar").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal(", ").formatted(Formatting.GRAY))
                     .append(Text.literal("Black Hole").formatted(Formatting.BLUE))
                     .append(Text.literal(" or ").formatted(Formatting.GRAY))
                     .append(Text.literal("Pulsar").formatted(Formatting.AQUA)))
         ), 5,7, ArcanaRegistry.BLACK_HOLE),
   NOVA("nova",
         Text.literal("Nova").formatted(Formatting.DARK_GREEN,Formatting.BOLD),
         List.of(
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Turns into ").formatted(Formatting.GRAY))
                     .append(Text.literal("Star").formatted(Formatting.DARK_PURPLE))
                     .append(Text.literal(" if").formatted(Formatting.GRAY))
                     .append(Text.literal(" touching 3 ").formatted(Formatting.RED))
                     .append(Text.literal("Plasma").formatted(Formatting.GOLD))),
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Turns into ").formatted(Formatting.GRAY))
                     .append(Text.literal("Plasma").formatted(Formatting.GOLD))
                     .append(Text.literal(" otherwise.").formatted(Formatting.GRAY)))
         ), 5,5, ArcanaRegistry.NOVA),
   SUPERNOVA("supernova",
         Text.literal("Supernova").formatted(Formatting.GREEN,Formatting.BOLD),
         List.of(
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Converts").formatted(Formatting.GRAY))
                     .append(Text.literal(" 12 surrounding ").formatted(Formatting.RED))
                     .append(Text.literal("Gas").formatted(Formatting.DARK_PURPLE))
                     .append(Text.literal(" into ").formatted(Formatting.GRAY))
                     .append(Text.literal("Plasma").formatted(Formatting.GOLD))),
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Turns into ").formatted(Formatting.GRAY))
                     .append(Text.literal("Quasar").formatted(Formatting.DARK_AQUA)))
         ), 24,18, ArcanaRegistry.SUPERNOVA),
   QUASAR("quasar",
         Text.literal("Quasar").formatted(Formatting.DARK_AQUA,Formatting.BOLD),
         List.of(
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Converts").formatted(Formatting.GRAY))
                     .append(Text.literal(" 8 surrounding").formatted(Formatting.RED))
                     .append(Text.literal(" Gas").formatted(Formatting.DARK_PURPLE))
                     .append(Text.literal(" and all ").formatted(Formatting.GRAY))
                     .append(Text.literal("Gas").formatted(Formatting.DARK_PURPLE))
                     .append(Text.literal(" in ").formatted(Formatting.GRAY))
                     .append(Text.literal("column").formatted(Formatting.RED))
                     .append(Text.literal(" into ").formatted(Formatting.GRAY))
                     .append(Text.literal("Plasma").formatted(Formatting.GOLD)))
         ), 18,12, ArcanaRegistry.QUASAR),
   PULSAR("pulsar",
         Text.literal("Pulsar").formatted(Formatting.AQUA,Formatting.BOLD),
         List.of(
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Turns").formatted(Formatting.GRAY))
                     .append(Text.literal(" 4 diagonal ").formatted(Formatting.RED))
                     .append(Text.literal("Gas").formatted(Formatting.DARK_PURPLE))
                     .append(Text.literal(" into ").formatted(Formatting.GRAY))
                     .append(Text.literal("Plasma").formatted(Formatting.GOLD))),
               Text.literal(""),
               TextUtils.removeItalics(Text.literal("-- Increases Infusion Result --").formatted(Formatting.YELLOW,Formatting.BOLD))
         ), 64,Integer.MAX_VALUE, ArcanaRegistry.PULSAR),
   NEBULA("nebula",
         Text.literal("Nebula").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),
         List.of(
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Turns into ").formatted(Formatting.GRAY))
                     .append(Text.literal("Nova").formatted(Formatting.DARK_GREEN))
                     .append(Text.literal(" if").formatted(Formatting.GRAY))
                     .append(Text.literal(" 4 of 8 surrounding tiles").formatted(Formatting.RED))
                     .append(Text.literal(" are ").formatted(Formatting.GRAY))
                     .append(Text.literal("Plasma").formatted(Formatting.GOLD))),
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Immune to ").formatted(Formatting.GRAY))
                     .append(Text.literal("Black Holes").formatted(Formatting.BLUE)))
         ), 3,3, ArcanaRegistry.NEBULA),
   PLANET("planet",
         Text.literal("Planet").formatted(Formatting.DARK_GRAY,Formatting.BOLD),
         List.of(
               TextUtils.removeItalics(Text.literal("Does Nothing").formatted(Formatting.GRAY)),
               TextUtils.removeItalics(Text.literal("")
                     .append(Text.literal("Can only be destroyed by a ").formatted(Formatting.GRAY))
                     .append(Text.literal("Black Hole").formatted(Formatting.BLUE)))
         ), 1,Integer.MAX_VALUE, ArcanaRegistry.PLANET);
   
   public final String id;
   public final Text name;
   public final List<Text> description;
   public final int placementCost;
   public final int startingValue;
   public final GraphicalItem.GraphicElement displayElement;
   
   EFItem(String id, Text name, List<Text> description, int placementCost, int startingValue, GraphicalItem.GraphicElement display){
      this.id = id;
      this.name = name;
      this.description = description;
      this.placementCost = placementCost;
      this.startingValue = startingValue;
      this.displayElement = display;
   }
   
   public static boolean hasTurn(EFItem item){
      return !(item == PLANET || item == GAS || item == PLASMA);
   }
   
   public static EFItem randomPlacementItem(int costLimit, Random random){
      List<EFItem> eligible = new ArrayList<>();
      for(EFItem item : EFItem.values()){
         if(item.startingValue <= costLimit) eligible.add(item);
      }
      
      return eligible.isEmpty() ? null : eligible.get(random.nextInt(eligible.size()));
   }
   
   public static GuiElementBuilder getGuiElement(EFItem item){
      GuiElementBuilder elem = GuiElementBuilder.from(GraphicalItem.with(item.displayElement)).hideDefaultTooltip();
      elem.setName(item.name);
      
      for(Text text : item.description){
         elem.addLoreLine(text);
      }
      return elem;
   }
   
   public static EFItem cycleItem(EFItem item, boolean backwards){
      EFItem[] sorts = EFItem.values();
      int ind = -1;
      for(int i = 0; i < sorts.length; i++){
         if(item == sorts[i]){
            ind = i;
         }
      }
      ind += backwards ? -1 : 1;
      if(ind >= sorts.length) ind = 0;
      if(ind < 0) ind = sorts.length-1;
      return sorts[ind] == PLANET ? cycleItem(PLANET, backwards) : sorts[ind];
   }
}


class EnhancedForgingGame{
   
   public static final int PLAY_COST = 10;
   public final int width = 7;
   public final int height = 6;
   private final Pair<EFItem,Integer>[][] board = new Pair[width][height];
   private final Pair<EFItem,Integer>[][] originalBoard;
   private final List<EFChange> changes;
   private int turn;
   private final Random random = Random.create();
   
   public EnhancedForgingGame(int startingValue, int planetCount, int starCount, long seed){
      random.setSeed(seed);
      originalBoard = new Pair[width][height];
      createNewBoard(startingValue, planetCount, starCount);
      changes = new ArrayList<>();
   }
   
   private void createNewBoard(int startingValue, int planetCount, int starCount){
      turn = 0;
      
      ArrayList<Pair<Integer,Integer>> eligible = new ArrayList<>();
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            board[x][y] = new Pair<>(EFItem.GAS,0);
            eligible.add(new Pair<>(x,y));
         }
      }
      
      for(int i = 0; i < planetCount && !eligible.isEmpty(); i++){
         Pair<Integer,Integer> tile = eligible.get(random.nextInt(eligible.size()));
         board[tile.getLeft()][tile.getRight()] = new Pair<>(EFItem.PLANET,0);
         eligible.remove(tile);
      }
      
      for(int i = 0; i < starCount && !eligible.isEmpty(); i++){
         Pair<Integer,Integer> tile = eligible.get(random.nextInt(eligible.size()));
         board[tile.getLeft()][tile.getRight()] = new Pair<>(random.nextFloat() < 0.66 ? EFItem.STAR : EFItem.PULSAR,0);
         eligible.remove(tile);
      }
      
      EFItem placeItem = EFItem.randomPlacementItem(startingValue, random);
      while(placeItem != null && !eligible.isEmpty()){
         Pair<Integer,Integer> tile = eligible.get(random.nextInt(eligible.size()));
         board[tile.getLeft()][tile.getRight()] = new Pair<>(placeItem,0);
         eligible.remove(tile);
         startingValue -= placeItem.startingValue;
         placeItem = EFItem.randomPlacementItem(startingValue, random);
      }
      
      eligible.clear();
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(EFItem.hasTurn(board[x][y].getLeft())){
               eligible.add(new Pair<>(x,y));
            }
         }
      }
      
      int itemTurn = 1;
      while(!eligible.isEmpty()){
         int index = random.nextInt(eligible.size());
         Pair<Integer,Integer> tile = eligible.get(index);
         board[tile.getLeft()][tile.getRight()].setRight(itemTurn);
         itemTurn++;
         eligible.remove(tile);
      }
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            originalBoard[x][y] = new Pair<>(board[x][y].getLeft(),board[x][y].getRight());
         }
      }
   }
   
   public Pair<EFItem, Integer>[][] getBoard(){
      return this.board;
   }
   
   public boolean nextTurn(){
      boolean tileChanged = false;
      turn++;
      
      Pair<EFItem, Integer> turnPair = null;
      int itemX = -1;
      int itemY = -1;
      int highestTurn = 0;
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(board[x][y].getRight() == turn){
               turnPair = board[x][y];
               itemX = x;
               itemY = y;
            }
            if(board[x][y].getRight() > highestTurn){
               highestTurn = board[x][y].getRight();
            }
         }
      }
      if(turnPair == null){
         if(turn < getHighestTurn()){
            tileChanged = nextTurn();
         }
         return tileChanged;
      }
      
      EFItem turnItem = turnPair.getLeft();
      
      if(turnItem == EFItem.NOVA){
         int count = 0;
         for(Pair<Integer, Integer> slot : getValidEffectedSlots(itemX, itemY, turnItem)){
            EFItem slotItem = board[slot.getLeft()][slot.getRight()].getLeft();
            if(slotItem == EFItem.PLASMA){
               count++;
            }
         }
         board[itemX][itemY] = new Pair<>(count >= 3 ? EFItem.STAR : EFItem.PLASMA,count >= 3 ? ++highestTurn : 0);
         tileChanged = true;
      }else if(turnItem == EFItem.QUASAR || turnItem == EFItem.PULSAR || turnItem == EFItem.STAR){
         for(Pair<Integer, Integer> slot : getValidEffectedSlots(itemX, itemY, turnItem)){
            EFItem slotItem = board[slot.getLeft()][slot.getRight()].getLeft();
            if(slotItem == EFItem.GAS || slotItem == EFItem.PLASMA){
               EFItem before = board[slot.getLeft()][slot.getRight()].getLeft();
               board[slot.getLeft()][slot.getRight()] = new Pair<>(EFItem.PLASMA, 0);
               if(board[slot.getLeft()][slot.getRight()].getLeft() != before) tileChanged = true;
            }
         }
      }else if(turnItem == EFItem.SUPERNOVA){
         for(Pair<Integer, Integer> slot : getValidEffectedSlots(itemX, itemY, turnItem)){
            EFItem slotItem = board[slot.getLeft()][slot.getRight()].getLeft();
            if(slotItem == EFItem.GAS || slotItem == EFItem.PLASMA){
               board[slot.getLeft()][slot.getRight()] = new Pair<>(EFItem.PLASMA, 0);
            }
         }
         board[itemX][itemY] = new Pair<>(EFItem.QUASAR,++highestTurn);
         tileChanged = true;
      }else if(turnItem == EFItem.NEBULA){
         int count = 0;
         for(Pair<Integer, Integer> slot : getValidEffectedSlots(itemX, itemY, turnItem)){
            EFItem slotItem = board[slot.getLeft()][slot.getRight()].getLeft();
            if(slotItem == EFItem.PLASMA){
               count++;
            }
         }
         if(count >= 4){
            board[itemX][itemY] = new Pair<>(EFItem.NOVA,++highestTurn);
            tileChanged = true;
         }
      }else if(turnItem == EFItem.BLACK_HOLE){
         boolean convert = false;
         for(Pair<Integer, Integer> slot : getValidEffectedSlots(itemX, itemY, turnItem)){
            EFItem slotItem = board[slot.getLeft()][slot.getRight()].getLeft();
            if(slotItem == EFItem.STAR || slotItem == EFItem.QUASAR || slotItem == EFItem.PULSAR || slotItem == EFItem.BLACK_HOLE){
               convert = true;
            }
            if(slotItem != EFItem.NEBULA){
               EFItem before = board[slot.getLeft()][slot.getRight()].getLeft();
               board[slot.getLeft()][slot.getRight()] = new Pair<>(EFItem.PLASMA, 0);
               if(board[slot.getLeft()][slot.getRight()].getLeft() != before) tileChanged = true;
            }
         }
         if(convert){
            board[itemX][itemY] = new Pair<>(EFItem.QUASAR,++highestTurn);
            tileChanged = true;
         }
      }
      return tileChanged;
   }
   
   private List<Pair<Integer,Integer>> getValidEffectedSlots(int x, int y, EFItem item){
      List<Pair<Integer,Integer>> list = new ArrayList<>();
      
      List<Pair<Integer,Integer>> touching = new ArrayList<>();
      List<Pair<Integer,Integer>> diagonal = new ArrayList<>();
      List<Pair<Integer,Integer>> surrounding = new ArrayList<>();
      addIfValid(touching,x-1,y);
      addIfValid(touching,x+1,y);
      addIfValid(touching,x,y-1);
      addIfValid(touching,x,y+1);
      addIfValid(diagonal,x-1,y-1);
      addIfValid(diagonal,x+1,y+1);
      addIfValid(diagonal,x+1,y-1);
      addIfValid(diagonal,x-1,y+1);
      surrounding.addAll(touching);
      surrounding.addAll(diagonal);
      
      if(item == EFItem.BLACK_HOLE || item == EFItem.NEBULA){
         list.addAll(surrounding);
      }else if(item == EFItem.SUPERNOVA){
         list.addAll(surrounding);
         addIfValid(list,x-2,y);
         addIfValid(list,x+2,y);
         addIfValid(list,x,y-2);
         addIfValid(list,x,y+2);
      }else if(item == EFItem.NOVA || item == EFItem.STAR){
         list.addAll(touching);
      }else if(item == EFItem.PULSAR){
         list.addAll(diagonal);
      }else if(item == EFItem.QUASAR){
         list.addAll(surrounding);
         for(int i = 1; i < height; i++){
            addIfValid(list,x,y+i);
            addIfValid(list,x,y-i);
         }
      }
      
      return list;
   }
   
   private boolean validSlot(int x, int y){
      return x >= 0 && y >= 0 && x < width && y < height;
   }
   
   private boolean addIfValid(List<Pair<Integer,Integer>> list, int x, int y){
      if(validSlot(x,y)){
         list.add(new Pair<>(x,y));
         return true;
      }
      return false;
   }
   
   public int getStarCount(){
      int starCount = 0;
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(board[x][y].getLeft() == EFItem.STAR || board[x][y].getLeft() == EFItem.PULSAR){
               starCount++;
            }
         }
      }
      return starCount;
   }
   
   public boolean hasNextTurn(){
      return Arrays.stream(board).anyMatch(subboard -> Arrays.stream(subboard).anyMatch(pair -> pair.getRight() > turn)) && turn < 99;
   }
   
   public int getTurn(){
      return this.turn;
   }
   
   private int getHighestTurn(){
      int highestTurn = 0;
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(board[x][y].getRight() > highestTurn){
               highestTurn = board[x][y].getRight();
            }
         }
      }
      return highestTurn;
   }
   
   private int getLowestTurn(){
      int lowestTurn = 999;
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(board[x][y].getRight() < lowestTurn && board[x][y].getRight() > 0){
               lowestTurn = board[x][y].getRight();
            }
         }
      }
      return lowestTurn;
   }
   
   public void applyChanges(){
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            Pair<EFItem,Integer> pair = originalBoard[x][y];
            board[x][y] = new Pair<>(pair.getLeft(),pair.getRight());
         }
      }
      
      for(EFChange change : changes){ // Apply all tile changes
         int turn = board[change.x][change.y].getRight();
         if(change.type == EFChangeType.TILE_CHANGE){
            EFItem newTile = change.newTile.get();
            if(EFItem.hasTurn(newTile)){
               if(turn == 0){
                  board[change.x][change.y] = new Pair<>(newTile,getHighestTurn()+1);
               }else{
                  board[change.x][change.y] = new Pair<>(newTile,turn);
               }
            }else{
               board[change.x][change.y] = new Pair<>(newTile,0);
            }
         }
      }
      ArrayList<Pair<Integer,Pair<Integer,Integer>>> turnArray = new ArrayList<>(); // Build turn order array
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(EFItem.hasTurn(board[x][y].getLeft())){
               int turn = board[x][y].getRight();
               turnArray.add(new Pair<>(turn,new Pair<>(x,y)));
            }
         }
      }
      Comparator<Pair<Integer,Pair<Integer,Integer>>> turnComparator = Comparator.comparingInt(Pair::getLeft);
      turnArray.sort(turnComparator); // Sort turn order array and then build into ordered list
      ArrayList<Pair<Integer,Integer>> turnlessArray = new ArrayList<>();
      for(Pair<Integer, Pair<Integer, Integer>> majorPair : turnArray){
         turnlessArray.add(majorPair.getRight());
      }
      
      for(EFChange change : changes){ // Apply turn changes
         boolean increase = false;
         if(change.type == EFChangeType.TURN_INCREASE){
            increase = true;
         }else if(change.type == EFChangeType.TILE_CHANGE){
            continue;
         }
         
         for(int i = 0; i < turnlessArray.size(); i++){
            int x = turnlessArray.get(i).getLeft();
            int y = turnlessArray.get(i).getRight();
            if(change.x != x || change.y != y) continue;
            if(!increase && i == 0) continue;
            if(increase && i == turnlessArray.size()-1) continue;
            
            if(increase){
               Collections.swap(turnlessArray,i,i+1);
            }else{
               Collections.swap(turnlessArray,i,i-1);
            }
            break;
         }
      }
      
      for(int i = 0; i < turnlessArray.size(); i++){ // Rebuild turn numbers
         int x = turnlessArray.get(i).getLeft();
         int y = turnlessArray.get(i).getRight();
         board[x][y] = new Pair<>(board[x][y].getLeft(),i+1);
      }
   }
   
   public void addChange(EFChange change){
      Iterator<EFChange> iter = changes.iterator();
      if(change.type == EFChangeType.TILE_CHANGE && board[change.x][change.y].getLeft() == EFItem.PLANET) return; // Planets are unchangeable
      if((change.type == EFChangeType.TURN_INCREASE || change.type == EFChangeType.TURN_DECREASE) && !EFItem.hasTurn(board[change.x][change.y].getLeft())) return; // Cant change turn of tile of different type
      
      while(iter.hasNext()){
         EFChange c = iter.next();
         
         if(c.y != change.y || c.x != change.x) continue;
         
         if(change.type == EFChangeType.TILE_CHANGE && c.type == EFChangeType.TILE_CHANGE){
            iter.remove(); // Tile change overriden
         }else if(change.type == EFChangeType.TILE_CHANGE && !EFItem.hasTurn(change.newTile.get())){
            if(c.type == EFChangeType.TURN_DECREASE || c.type == EFChangeType.TURN_INCREASE){
               iter.remove(); // Changing tile to one without a turn removes previous turn changes
            }
         }else if(change.type == EFChangeType.TURN_INCREASE && c.type == EFChangeType.TURN_DECREASE){
            iter.remove(); // Turn changes cancel out
            return;
         }else if(change.type == EFChangeType.TURN_DECREASE && c.type == EFChangeType.TURN_INCREASE){
            iter.remove(); // Turn changes cancel out
            return;
         }
      }
      
      // Cases with no change from original (set tile to original, move lowest turn lower, move highest turn higher)
      if(change.type == EFChangeType.TILE_CHANGE && originalBoard[change.x][change.y].getLeft() == change.newTile.get()) return;
      if(change.type == EFChangeType.TURN_INCREASE && board[change.x][change.y].getRight() == getHighestTurn()) return;
      if(change.type == EFChangeType.TURN_DECREASE && board[change.x][change.y].getRight() == getLowestTurn()) return;
      
      changes.add(change);
   }
   
   public void removeChanges(int x, int y){
      if(!validSlot(x,y)) return;
      changes.removeIf(c -> c.y == y && c.x == x);
   }
   
   public EFItem getItemAt(int x, int y){
      if(!validSlot(x,y)) return null;
      return board[x][y].getLeft();
   }
   
   public void resetBoard(){
      changes.clear();
   }
   
   public int getTileChangeCost(){
      int cost = 0;
      for(EFChange change : changes){
         if(change.type == EFChangeType.TILE_CHANGE){
            cost += change.getCost();
         }
      }
      return cost;
   }
   
   public int getTurnChangeCost(){
      int cost = 0;
      for(EFChange change : changes){
         if(change.type == EFChangeType.TURN_DECREASE || change.type == EFChangeType.TURN_INCREASE){
            cost += change.getCost();
         }
      }
      return cost;
   }
   
   public int getTotalCost(){
      return getTurnChangeCost() + getTileChangeCost() + PLAY_COST;
   }
   
   private static String boardToCode(Pair<EFItem,Integer>[][] board){
      StringBuilder binaryString = new StringBuilder();
      binaryString.append("0001"); // Version #
      
      int width = board.length;
      int height = board[0].length;
      binaryString.append(String.format("%4s", Integer.toBinaryString(width & ((1 << 4) - 1))).replace(' ', '0')); // Width (0111)
      binaryString.append(String.format("%4s", Integer.toBinaryString(height & ((1 << 4) - 1))).replace(' ', '0')); // Height (0110)
      
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            EFItem item = board[x][y].getLeft();
            
            // Gas bit
            if(item == EFItem.GAS){
               binaryString.append("1");
               continue;
            }else{
               binaryString.append("0");
            }
            
            // Type bits (ex: 00101)
            binaryString.append(String.format("%5s", Integer.toBinaryString(item.ordinal() & ((1 << 5) - 1))).replace(' ', '0'));
            
            // Turn bits
            if(EFItem.hasTurn(item)){
               int turn = board[x][y].getRight();
               binaryString.append("1");
               binaryString.append(String.format("%7s", Integer.toBinaryString(turn & ((1 << 7) - 1))).replace(' ', '0'));
            }else{
               binaryString.append("0");
            }
         }
      }
      return AlgoUtils.convertToBase64(binaryString.toString());
   }
   
   public String getStartingCode(){
      return boardToCode(this.originalBoard);
   }
   
   public String getGameCode(){
      return boardToCode(this.board);
   }
   
   public record EFChange(EFChangeType type, int x, int y, Optional<EFItem> newTile){
      static final int TURN_CHANGE_COST = 1;
      
      public int getCost(){
         if(type == EFChangeType.TILE_CHANGE){
            return newTile.get().placementCost;
         }else{
            return TURN_CHANGE_COST;
         }
      }
   }
   
   public enum EFChangeType{
      TURN_INCREASE,
      TURN_DECREASE,
      TILE_CHANGE
   }
}
