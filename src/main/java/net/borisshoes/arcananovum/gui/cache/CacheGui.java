package net.borisshoes.arcananovum.gui.cache;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcanaItemCompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.CompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class CacheGui extends SimpleGui {
   private TomeGui.CompendiumSettings settings;
   private SimpleInventory inv;
   
   public CacheGui(ServerPlayerEntity player){
      super(ScreenHandlerType.GENERIC_9X6, player, false);
      this.settings = new TomeGui.CompendiumSettings(0,0);
      this.inv = new SimpleInventory(28);
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(index == 0){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
         if(shiftLeft){
            settings.setSortType(TomeGui.TomeSort.RECOMMENDED);
         }else{
            settings.setSortType(TomeGui.TomeSort.cycleSort(settings.getSortType(),backwards));
         }
      
         buildCompendiumGui();
      }else if(index == 8){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
         if(shiftLeft){
            settings.setFilterType(TomeGui.TomeFilter.NONE);
         }else{
            settings.setFilterType(TomeGui.TomeFilter.cycleFilter(settings.getFilterType(),backwards));
         }
      
         List<CompendiumEntry> items = TomeGui.sortedFilteredEntryList(settings, player);
         int numPages = (int) Math.ceil((float)items.size()/28.0);
         if(settings.getPage() > numPages){
            settings.setPage(numPages);
         }
         buildCompendiumGui();
      }else if(index == 45){
         if(settings.getPage() > 1){
            settings.setPage(settings.getPage()-1);
            buildCompendiumGui();
         }
      }else if(index == 53){
         List<CompendiumEntry> items = TomeGui.sortedFilteredEntryList(settings, player);
         int numPages = (int) Math.ceil((float)items.size()/28.0);
         if(settings.getPage() < numPages){
            settings.setPage(settings.getPage()+1);
            buildCompendiumGui();
         }
      }
      return true;
   }
   
   public void buildCompendiumGui(){
      List<CompendiumEntry> items = TomeGui.sortedFilteredEntryList(settings, player);
      items = items.stream().filter(entry -> entry instanceof ArcanaItemCompendiumEntry || entry instanceof IngredientCompendiumEntry).toList();
      List<CompendiumEntry> pageItems = MiscUtils.listToPage(items, settings.getPage(),28);
      int numPages = (int) Math.ceil((float)items.size()/28.0);
      
      MiscUtils.outlineGUI(this, ArcanaColors.ARCANA_COLOR,Text.empty());
      
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.FILTER)).hideDefaultTooltip();
      filterBuilt.setName(Text.literal("Filter Arcana Items").formatted(Formatting.DARK_PURPLE));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current filter.").formatted(Formatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle filter backwards.").formatted(Formatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Shift Left Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset filter.").formatted(Formatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("")));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Current Filter: ").formatted(Formatting.AQUA)).append(TomeGui.TomeFilter.getColoredLabel(settings.getFilterType()))));
      setSlot(8,filterBuilt);
      
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.SORT)).hideDefaultTooltip();
      sortBuilt.setName(Text.literal("Sort Arcana Items").formatted(Formatting.DARK_PURPLE));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current sort type.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle sort backwards.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Shift Left Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset sort.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("")));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Sorting By: ").formatted(Formatting.AQUA)).append(TomeGui.TomeSort.getColoredLabel(settings.getSortType()))));
      setSlot(0,sortBuilt);
      
      GuiElementBuilder nextPage = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.RIGHT_ARROW)).hideDefaultTooltip();
      nextPage.setName(Text.literal("Next Page ("+settings.getPage()+"/"+numPages+")").formatted(Formatting.DARK_PURPLE));
      nextPage.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to go to the Next Page").formatted(Formatting.LIGHT_PURPLE))));
      setSlot(53,nextPage);
      
      GuiElementBuilder prevPage = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.LEFT_ARROW)).hideDefaultTooltip();
      prevPage.setName(Text.literal("Previous Page ("+settings.getPage()+"/"+numPages+")").formatted(Formatting.DARK_PURPLE));
      prevPage.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to go to the Previous Page").formatted(Formatting.LIGHT_PURPLE))));
      setSlot(45,prevPage);
   
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            setSlotRedirect((i*9+10)+j,new Slot(inv,k,j,i));
            if(k < pageItems.size()){
               CompendiumEntry entry = pageItems.get(k);
               if(entry instanceof ArcanaItemCompendiumEntry arcanaEntry){
                  ArcanaItem item = arcanaEntry.getArcanaItem();
                  inv.setStack(k,item.addCrafter(item.getNewItem(),player.getUuidAsString(),true,player.getServer()));
               }else if(entry instanceof IngredientCompendiumEntry ingredientEntry){
                  inv.setStack(k,new ItemStack(ingredientEntry.getDisplayStack().getItem(),ingredientEntry.getDisplayStack().getItem().getMaxCount()));
               }
            }else{
               inv.setStack(k,ItemStack.EMPTY);
            }
            k++;
         }
      }
   
      setTitle(Text.literal("Item Cache"));
   }
}
