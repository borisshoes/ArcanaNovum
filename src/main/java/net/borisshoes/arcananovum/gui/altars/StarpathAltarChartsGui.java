package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class StarpathAltarChartsGui extends SimpleGui {
   
   private final StarpathAltarBlockEntity blockEntity;
   private final SimpleGui returnGui;
   private final HashMap<String, BlockPos> destinationMap;
   private List<Pair<String, BlockPos>> destinations;
   private DestinationSort sort = DestinationSort.ALPHABETICAL;
   private int page = 1;
   
   public StarpathAltarChartsGui(ServerPlayerEntity player, SimpleGui returnGui, StarpathAltarBlockEntity blockEntity){
      super(ScreenHandlerType.GENERIC_9X6, player, false);
      this.blockEntity = blockEntity;
      this.returnGui = returnGui;
      this.destinationMap = blockEntity.getSavedTargets();
      loadDestinations();
      
      setTitle(Text.literal("Star Charts"));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      boolean indexInCenter = index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8;
      int numPages = (int) Math.ceil((float) destinations.size()/28.0);
      if(index == 0){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
         if(shiftLeft){
            sort = DestinationSort.ALPHABETICAL;
         }else{
            sort = DestinationSort.cycleSort(sort,backwards);
         }
         buildGui();
      }else if(index == 45){
         if(page > 1){
            page--;
            buildGui();
         }
      }else if(index == 53){
         if(page < numPages){
            page++;
            buildGui();
         }
      }else if(index == 49){
         AtomicReference<BlockPos> newDest = new AtomicReference<>();
         StarpathTargetGui nameGui = new StarpathTargetGui(player,blockEntity,false,this,(obj) -> {
            destinationMap.put((String) obj,newDest.get());
            buildGui();
         });
         
         StarpathTargetGui targetGui = new StarpathTargetGui(player,blockEntity,true,nameGui,(obj) -> newDest.set((BlockPos) obj));
         targetGui.open();
      }else if(index == 4){
         close();
      }else if(indexInCenter){
         int ind = (7*(index/9 - 1) + (index % 9 - 1)) + 28*(page-1);
         if(ind >= destinations.size()) return true;
         
         if(type == ClickType.MOUSE_RIGHT){ // Rename
            StarpathTargetGui gui = new StarpathTargetGui(player,blockEntity,false,this,(obj) -> {
               String newName = (String) obj;
               destinationMap.remove(destinations.get(ind).getLeft());
               destinationMap.put(newName,destinations.get(ind).getRight());
               buildGui();
            });
            gui.open();
         }else if(type == ClickType.MOUSE_LEFT_SHIFT){ // Delete
            blockEntity.getSavedTargets().remove(destinations.get(ind).getLeft());
            buildGui();
         }else{ // Select
            blockEntity.setTargetCoords(destinations.get(ind).getRight());
            close();
         }
      }
      return true;
   }
   
   public void loadDestinations(){
      destinations = new ArrayList<>();
      for(Map.Entry<String, BlockPos> entry : destinationMap.entrySet()){
         destinations.add(new Pair<>(entry.getKey(),entry.getValue()));
      }
      
      switch(sort){
         case CLOSEST -> {
            destinations.sort(Comparator.comparingInt(pair -> (int) pair.getRight().getSquaredDistance(blockEntity.getPos())));
         }
         case FURTHEST -> {
            destinations.sort(Comparator.comparingInt(pair -> (int) -pair.getRight().getSquaredDistance(blockEntity.getPos())));
         }
         case ALPHABETICAL -> {
            destinations.sort(Comparator.comparing(Pair::getLeft));
         }
      }
   }
   
   public void buildGui(){
      MiscUtils.outlineGUI(this, 0x1a0136,Text.literal("Saved Locations").formatted(Formatting.BLUE));
      loadDestinations();
      
      int numPages = (int) Math.ceil((float) destinations.size()/28.0);
      
      GuiElementBuilder labelItem = new GuiElementBuilder(Items.ENDER_EYE).hideDefaultTooltip();
      labelItem.setName((Text.literal("")
            .append(Text.literal("Starpath Altar Destinations").formatted(Formatting.DARK_AQUA))));
      labelItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to return to the altar").formatted(Formatting.BLUE)))));
      setSlot(4,labelItem);
      
      GuiElementBuilder newItem = new GuiElementBuilder(Items.WRITABLE_BOOK).hideDefaultTooltip();
      newItem.setName((Text.literal("")
            .append(Text.literal("New Destination").formatted(Formatting.DARK_AQUA))));
      newItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to add a new destination").formatted(Formatting.BLUE)))));
      setSlot(49,newItem);
      
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
      
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.SORT)).hideDefaultTooltip();
      sortBuilt.setName(Text.literal("Sort Destinations").formatted(Formatting.DARK_PURPLE));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current sort type.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle sort backwards.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Shift Left Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset sort.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("")));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Sorting By: ").formatted(Formatting.AQUA)).append(DestinationSort.getColoredLabel(sort))));
      setSlot(0,sortBuilt);
      
      int k = (page-1)*28;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < destinations.size()){
               GuiElementBuilder destItem = new GuiElementBuilder(Items.FILLED_MAP).hideDefaultTooltip();
               destItem.setName(Text.literal(destinations.get(k).getLeft()).formatted(Formatting.GOLD,Formatting.BOLD));
               destItem.addLoreLine(TextUtils.removeItalics(Text.literal(destinations.get(k).getRight().toShortString()).formatted(Formatting.YELLOW)));
               destItem.addLoreLine(TextUtils.removeItalics(Text.literal("")));
               destItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                     .append(Text.literal("Click").formatted(Formatting.AQUA))
                     .append(Text.literal(" to select this destination").formatted(Formatting.BLUE)))));
               destItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                     .append(Text.literal("Right Click").formatted(Formatting.GREEN))
                     .append(Text.literal(" to rename this destination").formatted(Formatting.BLUE)))));
               destItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                     .append(Text.literal("Shift Left Click").formatted(Formatting.RED))
                     .append(Text.literal(" to delete this destination").formatted(Formatting.BLUE)))));
               setSlot((i*9+10)+j, destItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
   }
   
   
   @Override
   public void onTick(){
      if(blockEntity != null){
         World world = blockEntity.getWorld();
         if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity || !blockEntity.isAssembled() || blockEntity.isActive()){
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
   
   public enum DestinationSort {
      CLOSEST("Closest"),
      FURTHEST("Furthest"),
      ALPHABETICAL("Alphabetical");
      
      public final String label;
      
      DestinationSort(String label){
         this.label = label;
      }
      
      public static Text getColoredLabel(DestinationSort sort){
         MutableText text = Text.literal(sort.label);
         
         return switch(sort){
            case CLOSEST -> text.formatted(Formatting.LIGHT_PURPLE);
            case FURTHEST -> text.formatted(Formatting.DARK_PURPLE);
            case ALPHABETICAL -> text.formatted(Formatting.GREEN);
         };
      }
      
      public static DestinationSort cycleSort(DestinationSort sort, boolean backwards){
         DestinationSort[] sorts = DestinationSort.values();
         int ind = -1;
         for(int i = 0; i < sorts.length; i++){
            if(sort == sorts[i]){
               ind = i;
            }
         }
         ind += backwards ? -1 : 1;
         if(ind >= sorts.length) ind = 0;
         if(ind < 0) ind = sorts.length-1;
         return sorts[ind];
      }
   }
}
