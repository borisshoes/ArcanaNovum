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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

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
   
   public StarpathAltarChartsGui(ServerPlayer player, SimpleGui returnGui, StarpathAltarBlockEntity blockEntity){
      super(MenuType.GENERIC_9x6, player, false);
      this.blockEntity = blockEntity;
      this.returnGui = returnGui;
      this.destinations = new ArrayList<>(blockEntity.getSavedTargets());
      stargate = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.STARGATE.id) > 0;
      
      setTitle(Component.literal("Star Charts"));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
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
            blockEntity.getSavedTargets().add(new StarpathAltarBlockEntity.TargetEntry((String) obj,player.level().dimension().identifier().toString(),newDest.get().getX(),newDest.get().getY(),newDest.get().getZ()));
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
            destinations.sort(Comparator.comparingInt(pair -> (int) pair.getBlockCoords().distSqr(blockEntity.getBlockPos())));
         }
         case FURTHEST -> {
            destinations.sort(Comparator.comparingInt(pair -> (int) -pair.getBlockCoords().distSqr(blockEntity.getBlockPos())));
         }
         case ALPHABETICAL -> {
            destinations.sort(Comparator.comparing(StarpathAltarBlockEntity.TargetEntry::name));
         }
      }
   }
   
   public void buildGui(){
      GuiHelper.outlineGUI(this, ArcanaColors.STARPATH_COLOR, Component.literal("Saved Locations").withStyle(ChatFormatting.BLUE));
      loadDestinations();
      
      int numPages = (int) Math.ceil((float) destinations.size()/28.0);
      
      GuiElementBuilder labelItem = new GuiElementBuilder(Items.ENDER_EYE).hideDefaultTooltip();
      labelItem.setName((Component.literal("")
            .append(Component.literal("Starpath Altar Destinations").withStyle(ChatFormatting.DARK_AQUA))));
      labelItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to return to the altar").withStyle(ChatFormatting.BLUE)))));
      setSlot(4,labelItem);
      
      GuiElementBuilder newItem = new GuiElementBuilder(Items.WRITABLE_BOOK).hideDefaultTooltip();
      newItem.setName((Component.literal("")
            .append(Component.literal("New Destination").withStyle(ChatFormatting.DARK_AQUA))));
      newItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to add a new destination").withStyle(ChatFormatting.BLUE)))));
      setSlot(49,newItem);
      
      if(numPages > 1){
         GuiElementBuilder nextArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideDefaultTooltip();
         nextArrow.setName((Component.literal("")
               .append(Component.literal("Next Page").withStyle(ChatFormatting.GOLD))));
         nextArrow.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("("+page+" of "+numPages+")").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(53,nextArrow);
         
         GuiElementBuilder prevArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideDefaultTooltip();
         prevArrow.setName((Component.literal("")
               .append(Component.literal("Prev Page").withStyle(ChatFormatting.GOLD))));
         prevArrow.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("("+page+" of "+numPages+")").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(45,prevArrow);
      }
      
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.SORT)).hideDefaultTooltip();
      sortBuilt.setName(Component.literal("Sort Destinations").withStyle(ChatFormatting.DARK_PURPLE));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current sort type.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle sort backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset sort.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Sorting By: ").withStyle(ChatFormatting.AQUA)).append(DestinationSort.getColoredLabel(sort))));
      setSlot(0,sortBuilt);
      
      int k = (page-1)*28;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < destinations.size()){
               GuiElementBuilder destItem = new GuiElementBuilder(Items.FILLED_MAP).hideDefaultTooltip();
               destItem.setName(Component.literal(destinations.get(k).name()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
               destItem.addLoreLine(TextUtils.removeItalics(Component.literal(destinations.get(k).getBlockCoords().toShortString()).withStyle(ChatFormatting.YELLOW)));
               if(stargate){
                  ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, Identifier.parse(destinations.get(k).dimension()));
                  if(dim == null){
                     dim = blockEntity.getLevel().dimension();
                  }
                  destItem.addLoreLine(Component.literal("Dimension: ").withStyle(ChatFormatting.YELLOW).append(ArcanaUtils.getFormattedDimName(dim)));
               }
               destItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
               destItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                     .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
                     .append(Component.literal(" to select this destination").withStyle(ChatFormatting.BLUE)))));
               destItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                     .append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN))
                     .append(Component.literal(" to rename this destination").withStyle(ChatFormatting.BLUE)))));
               destItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                     .append(Component.literal("Shift Left Click").withStyle(ChatFormatting.RED))
                     .append(Component.literal(" to delete this destination").withStyle(ChatFormatting.BLUE)))));
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
         Level world = blockEntity.getLevel();
         if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity || !blockEntity.isAssembled() || blockEntity.isActive()){
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
      
      public static Component getColoredLabel(DestinationSort sort){
         MutableComponent text = Component.literal(sort.label);
         
         return switch(sort){
            case CLOSEST -> text.withStyle(ChatFormatting.LIGHT_PURPLE);
            case FURTHEST -> text.withStyle(ChatFormatting.DARK_PURPLE);
            case ALPHABETICAL -> text.withStyle(ChatFormatting.GREEN);
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
