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
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static net.borisshoes.borislib.BorisLib.BORISLIB_ITEM_DATA;

public class EnhancedForgingGui extends SimpleGui {
   private final StarlightForgeBlockEntity blockEntity;
   private final EnhancedForgingGame game;
   private final NonNullList<ItemStack> ingredients;
   private final NonNullList<ItemStack> remainders;
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
   public EnhancedForgingGui(ServerPlayer player, StarlightForgeBlockEntity blockEntity, ItemStack enhancedStack, NonNullList<ItemStack> ingredients, NonNullList<ItemStack> remainders){
      super(MenuType.GENERIC_9x6, player, false);
      this.blockEntity = blockEntity;
      this.enhancedStack = enhancedStack;
      this.ingredients = ingredients;
      this.remainders = remainders;
      this.game = new EnhancedForgingGame(blockEntity.getStartingValue(), blockEntity.getPlanetCount(), blockEntity.getStarCount(), blockEntity.getSeed());
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
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
                     player.displayClientMessage(Component.literal("You do not have enough ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                           .append(Component.translatable(ArcanaRegistry.STARDUST.getDescriptionId()).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC)),false);
                     SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH,1,.5f);
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
      Level world = blockEntity.getLevel();
      if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity || !blockEntity.isAssembled()){
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
               if(stack.is(BorisLib.GRAPHICAL_ITEM)){
                  Identifier id = Identifier.parse(BORISLIB_ITEM_DATA.getStringProperty(stack, GraphicalItem.GRAPHICS_TAG));
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
      setTitle(Component.literal("Stardust Infusion"));
      
      setSlot(7,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.LAPIS_COLOR)).setName(Component.empty()).hideTooltip());
      setSlot(16,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.LAPIS_COLOR)).setName(Component.empty()).hideTooltip());
      setSlot(17,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,ArcanaColors.LAPIS_COLOR)).setName(Component.empty()).hideTooltip());
      setSlot(25,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.LAPIS_COLOR)).setName(Component.empty()).hideTooltip());
      setSlot(34,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.LAPIS_COLOR)).setName(Component.empty()).hideTooltip());
      setSlot(43,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.LAPIS_COLOR)).setName(Component.empty()).hideTooltip());
      setSlot(44,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,ArcanaColors.LAPIS_COLOR)).setName(Component.empty()).hideTooltip());
      setSlot(52,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.LAPIS_COLOR)).setName(Component.empty()).hideTooltip());
      
      GuiElementBuilder orderItem = GuiElementBuilder.from(Items.CLOCK.getDefaultInstance()).hideDefaultTooltip();
      orderItem.setName(Component.literal("Show Item Order").withStyle(ChatFormatting.YELLOW));
      orderItem.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to show the order of tiles.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      orderItem.addLoreLine(Component.empty());
      orderItem.addLoreLine(TextUtils.removeItalics(Component.literal("Stack size indicates the order of tiles.").withStyle(ChatFormatting.GRAY)));
      orderItem.addLoreLine(TextUtils.removeItalics(Component.literal("Newly created items activate after all queued tiles.").withStyle(ChatFormatting.GRAY)));
      orderItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Changing turn order costs ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(""+ EnhancedForgingGame.EFChange.TURN_CHANGE_COST).withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" stardust.").withStyle(ChatFormatting.YELLOW))));
      orderItem.setMaxCount(99);
      orderItem.setCount(game.getTurn()+1);
      setSlot(8,orderItem);
      
      GuiElementBuilder runItem = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CONFIRM)).hideDefaultTooltip();
      runItem.setName(Component.literal("Activate Forge").withStyle(ChatFormatting.YELLOW));
      runItem.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to activate the forge.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      if(showCodes){
         runItem.addLoreLine(Component.literal(""));
         runItem.addLoreLine(TextUtils.removeItalics(Component.literal("Board ID: "+game.getStartingCode()).withStyle(ChatFormatting.DARK_GRAY)));
         runItem.addLoreLine(TextUtils.removeItalics(Component.literal("Solution ID: "+game.getGameCode()).withStyle(ChatFormatting.DARK_GRAY)));
      }
      setSlot(53,runItem);
      
      int playCost = EnhancedForgingGame.PLAY_COST;
      int turnCost = game.getTurnChangeCost();
      int tileCost = game.getTileChangeCost();
      int totalCost = game.getTotalCost();
      
      GuiElementBuilder stardustItem = GuiElementBuilder.from(MinecraftUtils.removeLore(ArcanaRegistry.STARDUST.getDefaultInstance())).hideDefaultTooltip();
      stardustItem.setName(Component.literal("Requires 10 Stardust").withStyle(ChatFormatting.YELLOW));
      stardustItem.setName(Component.literal("")
            .append(Component.literal("Requires ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(""+totalCost).withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" Stardust").withStyle(ChatFormatting.YELLOW)));
      stardustItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Base Cost: ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(playCost+" Stardust").withStyle(ChatFormatting.GOLD))));
      stardustItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Turn Order Cost: ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(turnCost+" Stardust").withStyle(ChatFormatting.GOLD))));
      stardustItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Tile Cost: ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(tileCost+" Stardust").withStyle(ChatFormatting.GOLD))));
      stardustItem.addLoreLine(Component.literal(""));
      stardustItem.addLoreLine(TextUtils.removeItalics(Component.literal("Changing tiles or tile order costs stardust.").withStyle(ChatFormatting.GRAY)));
      stardustItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Forging has a base cost of ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(""+playCost).withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" stardust.").withStyle(ChatFormatting.YELLOW))));
      stardustItem.addLoreLine(Component.literal(""));
      stardustItem.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to reset the layout.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      stardustItem.setMaxCount(99);
      stardustItem.setCount(totalCost);
      setSlot(35,stardustItem);
      
      GuiElementBuilder placementItem = GuiElementBuilder.from(GraphicalItem.with(this.selectedItem.displayElement)).hideDefaultTooltip();
      placementItem.setName(Component.literal("")
            .append(Component.literal("Change tile to ").withStyle(ChatFormatting.YELLOW))
            .append(this.selectedItem.name));
      placementItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("This tile costs ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(""+this.selectedItem.placementCost).withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" stardust.").withStyle(ChatFormatting.YELLOW))));
      placementItem.addLoreLine(Component.literal(""));
      for(Component text : this.selectedItem.description){
         placementItem.addLoreLine(text);
      }
      placementItem.addLoreLine(Component.literal(""));
      placementItem.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click or Right Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to cycle tile types.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      placementItem.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" a tile to convert it.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      placementItem.setCount(Math.max(1,this.selectedItem.placementCost));
      setSlot(26,placementItem);
      
      Tuple<EFItem,Integer>[][] board = game.getBoard();
      
      for(int x = 0; x < game.width; x++){
         for(int y = 0; y < game.height; y++){
            Tuple<EFItem,Integer> tile = board[x][y];
            GuiElementBuilder elem = EFItem.getGuiElement(tile.getA());
            if(tile.getA() != EFItem.PLANET){
               elem.addLoreLine(Component.literal(""));
               if(tile.getA() == this.selectedItem){
                  elem.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to reset this tile.").withStyle(ChatFormatting.LIGHT_PURPLE))));
               }else{
                  elem.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change tile type to ").withStyle(ChatFormatting.LIGHT_PURPLE)).append(this.selectedItem.name)));
               }
               if(EFItem.hasTurn(tile.getA())){
                  elem.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Left Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to hasten this tile's turn.").withStyle(ChatFormatting.LIGHT_PURPLE))));
                  elem.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to delay this tile's turn.").withStyle(ChatFormatting.LIGHT_PURPLE))));
               }
            }
            
            if(turnMode){
               elem.setMaxCount(99);
               elem.setCount(tile.getB()+1);
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
      if(!(blockEntity.getLevel() instanceof ServerLevel world)) return;
      completed = true;
      blockEntity.addSeedUse();
      
      double percentile = EnhancedStatUtils.generatePercentile(game.getStarCount());
      
      if(enhancedStack.is(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER)){
         enhancedStack.setCount((int)(6*Math.pow(percentile,5) + 5*percentile + 5));
      }else{
         EnhancedStatUtils.enhanceItem(enhancedStack, percentile);
         ArcanaNovum.data(player).setResearchTask(ResearchTasks.INFUSE_ITEM, true);
      }
      
      ArcanaEffectUtils.enhancedForgingAnim(world,blockEntity.getBlockPos(),enhancedStack,0,fast ? 1.75 : 1);
      
      final int finalCost = game.getTotalCost();
      
      BorisLib.addTickTimerCallback(world, new GenericTimer(fast ? (int) (350 / 1.75) : 350, () -> {
         if(percentile >= 0.99 && !enhancedStack.is(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER)){
            ArcanaAchievements.grant(player,ArcanaAchievements.MASTER_CRAFTSMAN.id);
         }
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.STARDUST_INFUSION_PER_STARDUST)*finalCost);
         Vec3 pos = blockEntity.getBlockPos().getCenter().add(0,2,0);
         Containers.dropItemStack(world,pos.x,pos.y,pos.z,enhancedStack);
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
      NonNullList<ItemStack> list = completed ? remainders : ingredients;
      
      SimpleContainer returnInv = new SimpleContainer(list.size()+1);
      for(ItemStack stack : list){
         returnInv.addItem(stack);
      }
      if(paid){
         int cost = game.getTotalCost();
         while(cost > 0){
            int amnt = Math.min(cost,ArcanaRegistry.STARDUST.getDefaultMaxStackSize());
            returnInv.addItem(new ItemStack(ArcanaRegistry.STARDUST,amnt));
            cost -= amnt;
         }
      }
      MinecraftUtils.returnItems(returnInv,player);
   }
}

enum EFItem {
   STAR("star",
         Component.literal("Star").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
         List.of(
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Turns").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" 4 adjacent ").withStyle(ChatFormatting.RED))
                     .append(Component.literal("Gas").withStyle(ChatFormatting.DARK_PURPLE))
                     .append(Component.literal(" into ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Plasma").withStyle(ChatFormatting.GOLD))),
               Component.literal(""),
               TextUtils.removeItalics(Component.literal("-- Increases Infusion Result --").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
         ), 64,Integer.MAX_VALUE, ArcanaRegistry.STAR),
   GAS("gas",
         Component.literal("Gas").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
         List.of(
               TextUtils.removeItalics(Component.literal("Does Nothing").withStyle(ChatFormatting.GRAY))
         ), 0,Integer.MAX_VALUE, ArcanaRegistry.GAS),
   PLASMA("plasma",
         Component.literal("Plasma").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
         List.of(
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Fuel for other tiles.").withStyle(ChatFormatting.GRAY)))
         ), 7,1, ArcanaRegistry.PLASMA),
   BLACK_HOLE("black_hole",
         Component.literal("Black Hole").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD),
         List.of(
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Converts").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" 8 surrounding tiles ").withStyle(ChatFormatting.RED))
                     .append(Component.literal("into ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Plasma").withStyle(ChatFormatting.GOLD))),
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Turns into ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Quasar").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal(" if it destroys a ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Star").withStyle(ChatFormatting.YELLOW))
                     .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Quasar").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Black Hole").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(" or ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Pulsar").withStyle(ChatFormatting.AQUA)))
         ), 5,7, ArcanaRegistry.BLACK_HOLE),
   NOVA("nova",
         Component.literal("Nova").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD),
         List.of(
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Turns into ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Star").withStyle(ChatFormatting.DARK_PURPLE))
                     .append(Component.literal(" if").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" touching 3 ").withStyle(ChatFormatting.RED))
                     .append(Component.literal("Plasma").withStyle(ChatFormatting.GOLD))),
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Turns into ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Plasma").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(" otherwise.").withStyle(ChatFormatting.GRAY)))
         ), 5,5, ArcanaRegistry.NOVA),
   SUPERNOVA("supernova",
         Component.literal("Supernova").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
         List.of(
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Converts").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" 12 surrounding ").withStyle(ChatFormatting.RED))
                     .append(Component.literal("Gas").withStyle(ChatFormatting.DARK_PURPLE))
                     .append(Component.literal(" into ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Plasma").withStyle(ChatFormatting.GOLD))),
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Turns into ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Quasar").withStyle(ChatFormatting.DARK_AQUA)))
         ), 24,18, ArcanaRegistry.SUPERNOVA),
   QUASAR("quasar",
         Component.literal("Quasar").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD),
         List.of(
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Converts").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" 8 surrounding").withStyle(ChatFormatting.RED))
                     .append(Component.literal(" Gas").withStyle(ChatFormatting.DARK_PURPLE))
                     .append(Component.literal(" and all ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Gas").withStyle(ChatFormatting.DARK_PURPLE))
                     .append(Component.literal(" in ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("column").withStyle(ChatFormatting.RED))
                     .append(Component.literal(" into ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Plasma").withStyle(ChatFormatting.GOLD)))
         ), 18,12, ArcanaRegistry.QUASAR),
   PULSAR("pulsar",
         Component.literal("Pulsar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
         List.of(
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Turns").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" 4 diagonal ").withStyle(ChatFormatting.RED))
                     .append(Component.literal("Gas").withStyle(ChatFormatting.DARK_PURPLE))
                     .append(Component.literal(" into ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Plasma").withStyle(ChatFormatting.GOLD))),
               Component.literal(""),
               TextUtils.removeItalics(Component.literal("-- Increases Infusion Result --").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
         ), 64,Integer.MAX_VALUE, ArcanaRegistry.PULSAR),
   NEBULA("nebula",
         Component.literal("Nebula").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
         List.of(
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Turns into ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Nova").withStyle(ChatFormatting.DARK_GREEN))
                     .append(Component.literal(" if").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" 4 of 8 surrounding tiles").withStyle(ChatFormatting.RED))
                     .append(Component.literal(" are ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Plasma").withStyle(ChatFormatting.GOLD))),
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Immune to ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Black Holes").withStyle(ChatFormatting.BLUE)))
         ), 3,3, ArcanaRegistry.NEBULA),
   PLANET("planet",
         Component.literal("Planet").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD),
         List.of(
               TextUtils.removeItalics(Component.literal("Does Nothing").withStyle(ChatFormatting.GRAY)),
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Can only be destroyed by a ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("Black Hole").withStyle(ChatFormatting.BLUE)))
         ), 1,Integer.MAX_VALUE, ArcanaRegistry.PLANET);
   
   public final String id;
   public final Component name;
   public final List<Component> description;
   public final int placementCost;
   public final int startingValue;
   public final GraphicalItem.GraphicElement displayElement;
   
   EFItem(String id, Component name, List<Component> description, int placementCost, int startingValue, GraphicalItem.GraphicElement display){
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
   
   public static EFItem randomPlacementItem(int costLimit, RandomSource random){
      List<EFItem> eligible = new ArrayList<>();
      for(EFItem item : EFItem.values()){
         if(item.startingValue <= costLimit) eligible.add(item);
      }
      
      return eligible.isEmpty() ? null : eligible.get(random.nextInt(eligible.size()));
   }
   
   public static GuiElementBuilder getGuiElement(EFItem item){
      GuiElementBuilder elem = GuiElementBuilder.from(GraphicalItem.with(item.displayElement)).hideDefaultTooltip();
      elem.setName(item.name);
      
      for(Component text : item.description){
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
   private final Tuple<EFItem,Integer>[][] board = new Tuple[width][height];
   private final Tuple<EFItem,Integer>[][] originalBoard;
   private final List<EFChange> changes;
   private int turn;
   private final RandomSource random = RandomSource.create();
   
   public EnhancedForgingGame(int startingValue, int planetCount, int starCount, long seed){
      random.setSeed(seed);
      originalBoard = new Tuple[width][height];
      createNewBoard(startingValue, planetCount, starCount);
      changes = new ArrayList<>();
   }
   
   private void createNewBoard(int startingValue, int planetCount, int starCount){
      turn = 0;
      
      ArrayList<Tuple<Integer,Integer>> eligible = new ArrayList<>();
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            board[x][y] = new Tuple<>(EFItem.GAS,0);
            eligible.add(new Tuple<>(x,y));
         }
      }
      
      for(int i = 0; i < planetCount && !eligible.isEmpty(); i++){
         Tuple<Integer,Integer> tile = eligible.get(random.nextInt(eligible.size()));
         board[tile.getA()][tile.getB()] = new Tuple<>(EFItem.PLANET,0);
         eligible.remove(tile);
      }
      
      for(int i = 0; i < starCount && !eligible.isEmpty(); i++){
         Tuple<Integer,Integer> tile = eligible.get(random.nextInt(eligible.size()));
         board[tile.getA()][tile.getB()] = new Tuple<>(random.nextFloat() < 0.66 ? EFItem.STAR : EFItem.PULSAR,0);
         eligible.remove(tile);
      }
      
      EFItem placeItem = EFItem.randomPlacementItem(startingValue, random);
      while(placeItem != null && !eligible.isEmpty()){
         Tuple<Integer,Integer> tile = eligible.get(random.nextInt(eligible.size()));
         board[tile.getA()][tile.getB()] = new Tuple<>(placeItem,0);
         eligible.remove(tile);
         startingValue -= placeItem.startingValue;
         placeItem = EFItem.randomPlacementItem(startingValue, random);
      }
      
      eligible.clear();
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(EFItem.hasTurn(board[x][y].getA())){
               eligible.add(new Tuple<>(x,y));
            }
         }
      }
      
      int itemTurn = 1;
      while(!eligible.isEmpty()){
         int index = random.nextInt(eligible.size());
         Tuple<Integer,Integer> tile = eligible.get(index);
         board[tile.getA()][tile.getB()].setB(itemTurn);
         itemTurn++;
         eligible.remove(tile);
      }
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            originalBoard[x][y] = new Tuple<>(board[x][y].getA(),board[x][y].getB());
         }
      }
   }
   
   public Tuple<EFItem, Integer>[][] getBoard(){
      return this.board;
   }
   
   public boolean nextTurn(){
      boolean tileChanged = false;
      turn++;
      
      Tuple<EFItem, Integer> turnPair = null;
      int itemX = -1;
      int itemY = -1;
      int highestTurn = 0;
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(board[x][y].getB() == turn){
               turnPair = board[x][y];
               itemX = x;
               itemY = y;
            }
            if(board[x][y].getB() > highestTurn){
               highestTurn = board[x][y].getB();
            }
         }
      }
      if(turnPair == null){
         if(turn < getHighestTurn()){
            tileChanged = nextTurn();
         }
         return tileChanged;
      }
      
      EFItem turnItem = turnPair.getA();
      
      if(turnItem == EFItem.NOVA){
         int count = 0;
         for(Tuple<Integer, Integer> slot : getValidEffectedSlots(itemX, itemY, turnItem)){
            EFItem slotItem = board[slot.getA()][slot.getB()].getA();
            if(slotItem == EFItem.PLASMA){
               count++;
            }
         }
         board[itemX][itemY] = new Tuple<>(count >= 3 ? EFItem.STAR : EFItem.PLASMA,count >= 3 ? ++highestTurn : 0);
         tileChanged = true;
      }else if(turnItem == EFItem.QUASAR || turnItem == EFItem.PULSAR || turnItem == EFItem.STAR){
         for(Tuple<Integer, Integer> slot : getValidEffectedSlots(itemX, itemY, turnItem)){
            EFItem slotItem = board[slot.getA()][slot.getB()].getA();
            if(slotItem == EFItem.GAS || slotItem == EFItem.PLASMA){
               EFItem before = board[slot.getA()][slot.getB()].getA();
               board[slot.getA()][slot.getB()] = new Tuple<>(EFItem.PLASMA, 0);
               if(board[slot.getA()][slot.getB()].getA() != before) tileChanged = true;
            }
         }
      }else if(turnItem == EFItem.SUPERNOVA){
         for(Tuple<Integer, Integer> slot : getValidEffectedSlots(itemX, itemY, turnItem)){
            EFItem slotItem = board[slot.getA()][slot.getB()].getA();
            if(slotItem == EFItem.GAS || slotItem == EFItem.PLASMA){
               board[slot.getA()][slot.getB()] = new Tuple<>(EFItem.PLASMA, 0);
            }
         }
         board[itemX][itemY] = new Tuple<>(EFItem.QUASAR,++highestTurn);
         tileChanged = true;
      }else if(turnItem == EFItem.NEBULA){
         int count = 0;
         for(Tuple<Integer, Integer> slot : getValidEffectedSlots(itemX, itemY, turnItem)){
            EFItem slotItem = board[slot.getA()][slot.getB()].getA();
            if(slotItem == EFItem.PLASMA){
               count++;
            }
         }
         if(count >= 4){
            board[itemX][itemY] = new Tuple<>(EFItem.NOVA,++highestTurn);
            tileChanged = true;
         }
      }else if(turnItem == EFItem.BLACK_HOLE){
         boolean convert = false;
         for(Tuple<Integer, Integer> slot : getValidEffectedSlots(itemX, itemY, turnItem)){
            EFItem slotItem = board[slot.getA()][slot.getB()].getA();
            if(slotItem == EFItem.STAR || slotItem == EFItem.QUASAR || slotItem == EFItem.PULSAR || slotItem == EFItem.BLACK_HOLE){
               convert = true;
            }
            if(slotItem != EFItem.NEBULA){
               EFItem before = board[slot.getA()][slot.getB()].getA();
               board[slot.getA()][slot.getB()] = new Tuple<>(EFItem.PLASMA, 0);
               if(board[slot.getA()][slot.getB()].getA() != before) tileChanged = true;
            }
         }
         if(convert){
            board[itemX][itemY] = new Tuple<>(EFItem.QUASAR,++highestTurn);
            tileChanged = true;
         }
      }
      return tileChanged;
   }
   
   private List<Tuple<Integer,Integer>> getValidEffectedSlots(int x, int y, EFItem item){
      List<Tuple<Integer,Integer>> list = new ArrayList<>();
      
      List<Tuple<Integer,Integer>> touching = new ArrayList<>();
      List<Tuple<Integer,Integer>> diagonal = new ArrayList<>();
      List<Tuple<Integer,Integer>> surrounding = new ArrayList<>();
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
   
   private boolean addIfValid(List<Tuple<Integer,Integer>> list, int x, int y){
      if(validSlot(x,y)){
         list.add(new Tuple<>(x,y));
         return true;
      }
      return false;
   }
   
   public int getStarCount(){
      int starCount = 0;
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(board[x][y].getA() == EFItem.STAR || board[x][y].getA() == EFItem.PULSAR){
               starCount++;
            }
         }
      }
      return starCount;
   }
   
   public boolean hasNextTurn(){
      return Arrays.stream(board).anyMatch(subboard -> Arrays.stream(subboard).anyMatch(pair -> pair.getB() > turn)) && turn < 99;
   }
   
   public int getTurn(){
      return this.turn;
   }
   
   private int getHighestTurn(){
      int highestTurn = 0;
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(board[x][y].getB() > highestTurn){
               highestTurn = board[x][y].getB();
            }
         }
      }
      return highestTurn;
   }
   
   private int getLowestTurn(){
      int lowestTurn = 999;
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(board[x][y].getB() < lowestTurn && board[x][y].getB() > 0){
               lowestTurn = board[x][y].getB();
            }
         }
      }
      return lowestTurn;
   }
   
   public void applyChanges(){
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            Tuple<EFItem,Integer> pair = originalBoard[x][y];
            board[x][y] = new Tuple<>(pair.getA(),pair.getB());
         }
      }
      
      for(EFChange change : changes){ // Apply all tile changes
         int turn = board[change.x][change.y].getB();
         if(change.type == EFChangeType.TILE_CHANGE){
            EFItem newTile = change.newTile.get();
            if(EFItem.hasTurn(newTile)){
               if(turn == 0){
                  board[change.x][change.y] = new Tuple<>(newTile,getHighestTurn()+1);
               }else{
                  board[change.x][change.y] = new Tuple<>(newTile,turn);
               }
            }else{
               board[change.x][change.y] = new Tuple<>(newTile,0);
            }
         }
      }
      ArrayList<Tuple<Integer, Tuple<Integer,Integer>>> turnArray = new ArrayList<>(); // Build turn order array
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            if(EFItem.hasTurn(board[x][y].getA())){
               int turn = board[x][y].getB();
               turnArray.add(new Tuple<>(turn,new Tuple<>(x,y)));
            }
         }
      }
      Comparator<Tuple<Integer, Tuple<Integer,Integer>>> turnComparator = Comparator.comparingInt(Tuple::getA);
      turnArray.sort(turnComparator); // Sort turn order array and then build into ordered list
      ArrayList<Tuple<Integer,Integer>> turnlessArray = new ArrayList<>();
      for(Tuple<Integer, Tuple<Integer, Integer>> majorPair : turnArray){
         turnlessArray.add(majorPair.getB());
      }
      
      for(EFChange change : changes){ // Apply turn changes
         boolean increase = false;
         if(change.type == EFChangeType.TURN_INCREASE){
            increase = true;
         }else if(change.type == EFChangeType.TILE_CHANGE){
            continue;
         }
         
         for(int i = 0; i < turnlessArray.size(); i++){
            int x = turnlessArray.get(i).getA();
            int y = turnlessArray.get(i).getB();
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
         int x = turnlessArray.get(i).getA();
         int y = turnlessArray.get(i).getB();
         board[x][y] = new Tuple<>(board[x][y].getA(),i+1);
      }
   }
   
   public void addChange(EFChange change){
      Iterator<EFChange> iter = changes.iterator();
      if(change.type == EFChangeType.TILE_CHANGE && board[change.x][change.y].getA() == EFItem.PLANET) return; // Planets are unchangeable
      if((change.type == EFChangeType.TURN_INCREASE || change.type == EFChangeType.TURN_DECREASE) && !EFItem.hasTurn(board[change.x][change.y].getA())) return; // Cant change turn of tile of different type
      
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
      if(change.type == EFChangeType.TILE_CHANGE && originalBoard[change.x][change.y].getA() == change.newTile.get()) return;
      if(change.type == EFChangeType.TURN_INCREASE && board[change.x][change.y].getB() == getHighestTurn()) return;
      if(change.type == EFChangeType.TURN_DECREASE && board[change.x][change.y].getB() == getLowestTurn()) return;
      
      changes.add(change);
   }
   
   public void removeChanges(int x, int y){
      if(!validSlot(x,y)) return;
      changes.removeIf(c -> c.y == y && c.x == x);
   }
   
   public EFItem getItemAt(int x, int y){
      if(!validSlot(x,y)) return null;
      return board[x][y].getA();
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
   
   private static String boardToCode(Tuple<EFItem,Integer>[][] board){
      StringBuilder binaryString = new StringBuilder();
      binaryString.append("0001"); // Version #
      
      int width = board.length;
      int height = board[0].length;
      binaryString.append(String.format("%4s", Integer.toBinaryString(width & ((1 << 4) - 1))).replace(' ', '0')); // Width (0111)
      binaryString.append(String.format("%4s", Integer.toBinaryString(height & ((1 << 4) - 1))).replace(' ', '0')); // Height (0110)
      
      for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
            EFItem item = board[x][y].getA();
            
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
               int turn = board[x][y].getB();
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
