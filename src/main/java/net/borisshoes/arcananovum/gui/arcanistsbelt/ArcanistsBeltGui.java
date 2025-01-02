package net.borisshoes.arcananovum.gui.arcanistsbelt;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;

import java.util.concurrent.atomic.AtomicInteger;

public class ArcanistsBeltGui extends SimpleGui {
   
   private final ArcanistsBelt belt;
   private final ItemStack beltStack;
   private SimpleInventory inv;
   private final int slotCount;
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param player                the player to server this gui to
    *                              will be treated as slots of this gui
    */
   public ArcanistsBeltGui(ServerPlayerEntity player, ArcanistsBelt belt, ItemStack beltStack, int slotCount){
      super(ScreenHandlerType.GENERIC_9X1, player, false);
      this.belt = belt;
      this.beltStack = beltStack;
      this.slotCount = slotCount;
   }
   
   public void build(){
      inv = new SimpleInventory(9);
      for(int i = 0; i < inv.size(); i++){
         if(i < slotCount){
            setSlotRedirect(i, new ArcanistsBeltSlot(inv,i,i,0));
         }else{
            setSlot(i,GuiElementBuilder.from(GraphicalItem.with(GraphicItems.CANCEL))
                  .setName(Text.literal("Slot Locked").formatted(Formatting.DARK_PURPLE))
                  .addLoreLine(TextUtils.removeItalics(Text.literal("")
                        .append(Text.literal("Unlock this slot with Augments").formatted(Formatting.LIGHT_PURPLE)))));
         }
      }
      
      ContainerComponent beltItems = beltStack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
      AtomicInteger i = new AtomicInteger();
      beltItems.stream().forEachOrdered(stack -> {
         inv.setStack(i.get(),stack);
         i.getAndIncrement();
      });
      
      setTitle(belt.getTranslatedName());
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(type == ClickType.OFFHAND_SWAP || action == SlotActionType.SWAP){
         close();
      }else if(index > 9){
         int invSlot = index >= 36 ? index - 36 : index;
         ItemStack stack = player.getInventory().getStack(invSlot);
         if(ItemStack.areItemsAndComponentsEqual(beltStack,stack)){
            close();
            return false;
         }
      }
      
      return super.onAnyClick(index, type, action);
   }
   
   @Override
   public void onClose(){
      DefaultedList<ItemStack> items = DefaultedList.ofSize(9,ItemStack.EMPTY);
      int charmCount = 0;
      for(int i = 0; i < inv.size(); i++){
         ItemStack itemStack = inv.getStack(i);
         if(itemStack.isEmpty()) continue;
         items.set(i, itemStack);
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(itemStack);
         if(arcanaItem != null && arcanaItem.hasCategory(TomeGui.TomeFilter.CHARMS)) charmCount++;
      }
      beltStack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(items));
      belt.buildItemLore(beltStack,player.getServer());
      
      if(charmCount >= slotCount){
         ArcanaAchievements.grant(player,ArcanaAchievements.BELT_CHARMING.id);
      }
   }
}