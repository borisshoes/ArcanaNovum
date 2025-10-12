package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class StarpathAltarChartsGui extends SimpleGui {
   
   private final StarpathAltarBlockEntity blockEntity;
   private final SimpleGui returnGui;
   private List<StarpathAltarBlockEntity.TargetEntry> destinations;
   private DestinationSort sort = DestinationSort.ALPHABETICAL;
   private int page = 1;
   private final boolean stargate;
   
   public StarpathAltarChartsGui(ServerPlayerEntity player, SimpleGui returnGui, StarpathAltarBlockEntity blockEntity){
      super(ScreenHandlerType.GENERIC_9X6, player, false);
      this.blockEntity = blockEntity;
      this.returnGui = returnGui;
      this.destinations = new ArrayList<>(blockEntity.getSavedTargets());
      stargate = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.STARGATE.id) > 0;
      
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
            blockEntity.getSavedTargets().add(new StarpathAltarBlockEntity.TargetEntry((String) obj,player.getWorld().getRegistryKey().getValue().toString(),newDest.get().getX(),newDest.get().getY(),newDest.get().getZ()));
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
               StarpathAltarBlockEntity.TargetEntry toRemove = destinations.get(ind);
               blockEntity.getSavedTargets().remove(toRemove);
               blockEntity.getSavedTargets().add(new StarpathAltarBlockEntity.TargetEntry(newName,toRemove.dimension(),toRemove.x(),toRemove.y(),toRemove.z()));
               buildGui();
            });
            gui.open();
         }else if(type == ClickType.MOUSE_LEFT_SHIFT){ // Delete
            blockEntity.getSavedTargets().remove(destinations.get(ind));
            buildGui();
         }else{ // Select
            blockEntity.setTarget(destinations.get(ind));
            close();
         }
      }
      return true;
   }
   
   public void loadDestinations(){
      destinations = new ArrayList<>(blockEntity.getSavedTargets());
      
      switch(sort){
         case CLOSEST -> {
            destinations.sort(Comparator.comparingInt(pair -> (int) pair.getBlockCoords().getSquaredDistance(blockEntity.getPos())));
         }
         case FURTHEST -> {
            destinations.sort(Comparator.comparingInt(pair -> (int) -pair.getBlockCoords().getSquaredDistance(blockEntity.getPos())));
         }
         case ALPHABETICAL -> {
            destinations.sort(Comparator.comparing(StarpathAltarBlockEntity.TargetEntry::name));
         }
      }
   }
   
   public void buildGui(){
      GuiHelper.outlineGUI(this, ArcanaColors.STARPATH_COLOR,Text.literal("Saved Locations").formatted(Formatting.BLUE));
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
      
      if(numPages > 1){
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
      }
      
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.SORT)).hideDefaultTooltip();
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
               destItem.setName(Text.literal(destinations.get(k).name()).formatted(Formatting.GOLD,Formatting.BOLD));
               destItem.addLoreLine(TextUtils.removeItalics(Text.literal(destinations.get(k).getBlockCoords().toShortString()).formatted(Formatting.YELLOW)));
               if(stargate){
                  RegistryKey<World> dim = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(destinations.get(k).dimension()));
                  if(dim == null){
                     dim = blockEntity.getWorld().getRegistryKey();
                  }
                  destItem.addLoreLine(Text.literal("Dimension: ").formatted(Formatting.YELLOW).append(ArcanaUtils.getFormattedDimName(dim)));
               }
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
