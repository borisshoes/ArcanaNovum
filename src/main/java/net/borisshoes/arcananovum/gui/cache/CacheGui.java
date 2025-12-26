package net.borisshoes.arcananovum.gui.cache;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcanaItemCompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.CompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CacheGui extends SimpleGui {
   private TomeGui.CompendiumSettings settings;
   private SimpleContainer inv;
   
   public CacheGui(ServerPlayer player){
      super(MenuType.GENERIC_9x6, player, false);
      this.settings = new TomeGui.CompendiumSettings(0,0);
      this.inv = new SimpleContainer(28);
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
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
      List<CompendiumEntry> pageItems = AlgoUtils.listToPage(items, settings.getPage(),28);
      int numPages = (int) Math.ceil((float)items.size()/28.0);
      
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR, Component.empty());
      
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.FILTER)).hideDefaultTooltip();
      filterBuilt.setName(Component.literal("Filter Arcana Items").withStyle(ChatFormatting.DARK_PURPLE));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current filter.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle filter backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset filter.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Current Filter: ").withStyle(ChatFormatting.AQUA)).append(TomeGui.TomeFilter.getColoredLabel(settings.getFilterType()))));
      setSlot(8,filterBuilt);
      
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.SORT)).hideDefaultTooltip();
      sortBuilt.setName(Component.literal("Sort Arcana Items").withStyle(ChatFormatting.DARK_PURPLE));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current sort type.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle sort backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset sort.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Sorting By: ").withStyle(ChatFormatting.AQUA)).append(TomeGui.TomeSort.getColoredLabel(settings.getSortType()))));
      setSlot(0,sortBuilt);
      
      GuiElementBuilder nextPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW)).hideDefaultTooltip();
      nextPage.setName(Component.literal("Next Page ("+settings.getPage()+"/"+numPages+")").withStyle(ChatFormatting.DARK_PURPLE));
      nextPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to go to the Next Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
      setSlot(53,nextPage);
      
      GuiElementBuilder prevPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW)).hideDefaultTooltip();
      prevPage.setName(Component.literal("Previous Page ("+settings.getPage()+"/"+numPages+")").withStyle(ChatFormatting.DARK_PURPLE));
      prevPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to go to the Previous Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
      setSlot(45,prevPage);
   
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            setSlotRedirect((i*9+10)+j,new Slot(inv,k,j,i));
            if(k < pageItems.size()){
               CompendiumEntry entry = pageItems.get(k);
               if(entry instanceof ArcanaItemCompendiumEntry arcanaEntry){
                  ArcanaItem item = arcanaEntry.getArcanaItem();
                  inv.setItem(k,item.addCrafter(item.getNewItem(),player.getStringUUID(),1,player.level().getServer()));
               }else if(entry instanceof IngredientCompendiumEntry ingredientEntry){
                  inv.setItem(k,new ItemStack(ingredientEntry.getDisplayStack().getItem(),ingredientEntry.getDisplayStack().getItem().getDefaultMaxStackSize()));
               }
            }else{
               inv.setItem(k, ItemStack.EMPTY);
            }
            k++;
         }
      }
   
      setTitle(Component.literal("Item Cache"));
   }
}
