package net.borisshoes.arcananovum.gui.cache;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class CacheGui extends SimpleGui {
   private TomeGui.CompendiumSettings settings;
   private CacheInventory inv;
   
   public CacheGui(ServerPlayerEntity player){
      super(ScreenHandlerType.GENERIC_9X6, player, false);
      this.settings = new TomeGui.CompendiumSettings();
      this.inv = new CacheInventory();
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      if(index == 0){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean middle = type == ClickType.MOUSE_MIDDLE;
         if(middle){
            settings.setSortType(ArcaneTome.TomeSort.RECOMMENDED);
         }else{
            settings.setSortType(ArcaneTome.TomeSort.cycleSort(settings.getSortType(),backwards));
         }
      
         buildCompendiumGui();
      }else if(index == 8){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean middle = type == ClickType.MOUSE_MIDDLE;
         if(middle){
            settings.setFilterType(ArcaneTome.TomeFilter.NONE);
         }else{
            settings.setFilterType(ArcaneTome.TomeFilter.cycleFilter(settings.getFilterType(),backwards));
         }
      
         List<MagicItem> items = ArcaneTome.sortedFilteredItemList(settings);
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
         List<MagicItem> items = ArcaneTome.sortedFilteredItemList(settings);
         int numPages = (int) Math.ceil((float)items.size()/28.0);
         if(settings.getPage() < numPages){
            settings.setPage(settings.getPage()+1);
            buildCompendiumGui();
         }
      }
      return true;
   }
   
   public void buildCompendiumGui(){
      List<MagicItem> items = ArcaneTome.sortedFilteredItemList(settings);
      List<MagicItem> pageItems = ArcaneTome.listToPage(items, settings.getPage());
      int numPages = (int) Math.ceil((float)items.size()/28.0);
   
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
   
      ItemStack filterItem = new ItemStack(Items.HOPPER);
      NbtCompound tag = filterItem.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      display.putString("Name","[{\"text\":\"Filter Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(filterItem);
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current filter.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle filter backwards.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Middle Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset filter.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal(""));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Current Filter: ").formatted(Formatting.AQUA)).append(ArcaneTome.TomeFilter.getColoredLabel(settings.getFilterType())));
      setSlot(8,filterBuilt);
   
      ItemStack sortItem = new ItemStack(Items.NETHER_STAR);
      tag = sortItem.getOrCreateNbt();
      display = new NbtCompound();
      display.putString("Name","[{\"text\":\"Sort Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(sortItem);
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current sort type.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle sort backwards.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Middle Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset sort.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal(""));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Sorting By: ").formatted(Formatting.AQUA)).append(ArcaneTome.TomeSort.getColoredLabel(settings.getSortType())));
      setSlot(0,sortBuilt);
   
      ItemStack nextPage = new ItemStack(Items.SPECTRAL_ARROW);
      tag = nextPage.getOrCreateNbt();
      display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Next Page ("+settings.getPage()+"/"+numPages+")\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to go to the Next Page\",\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      setSlot(53,GuiElementBuilder.from(nextPage));
   
      ItemStack prevPage = new ItemStack(Items.SPECTRAL_ARROW);
      tag = prevPage.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Previous Page ("+settings.getPage()+"/"+numPages+")\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to go to the Previous Page\",\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      setSlot(45,GuiElementBuilder.from(prevPage));
   
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            setSlotRedirect((i*9+10)+j,new Slot(inv,k,j,i));
            if(k < pageItems.size()){
               MagicItem item = pageItems.get(k);
               inv.setStack(k,item.addCrafter(item.getNewItem(),player.getUuidAsString()));
            }else{
               inv.setStack(k,ItemStack.EMPTY);
            }
            k++;
         }
      }
   
      setTitle(Text.literal("Item Cache"));
   }
}
