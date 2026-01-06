package net.borisshoes.arcananovum.gui.arcanistsbelt;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.concurrent.atomic.AtomicInteger;

public class ArcanistsBeltGui extends SimpleGui {
   
   private final ArcanistsBelt belt;
   private final ItemStack beltStack;
   private SimpleContainer inv;
   private final int slotCount;
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param player                the player to server this gui to
    *                              will be treated as slots of this gui
    */
   public ArcanistsBeltGui(ServerPlayer player, ArcanistsBelt belt, ItemStack beltStack, int slotCount){
      super(MenuType.GENERIC_9x1, player, false);
      this.belt = belt;
      this.beltStack = beltStack;
      this.slotCount = slotCount;
   }
   
   public void build(){
      inv = new SimpleContainer(9);
      for(int i = 0; i < inv.getContainerSize(); i++){
         if(i < slotCount){
            setSlotRedirect(i, new ArcanistsBeltSlot(inv,i,i,0));
         }else{
            setSlot(i,GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CANCEL))
                  .setName(Component.literal("Slot Locked").withStyle(ChatFormatting.DARK_PURPLE))
                  .addLoreLine(TextUtils.removeItalics(Component.literal("")
                        .append(Component.literal("Unlock this slot with Augments").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         }
      }
      
      ItemContainerContents beltItems = beltStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
      AtomicInteger i = new AtomicInteger();
      beltItems.stream().forEachOrdered(stack -> {
         inv.setItem(i.get(),stack);
         i.getAndIncrement();
      });
      
      setTitle(belt.getTranslatedName());
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      if(type == ClickType.OFFHAND_SWAP || action == net.minecraft.world.inventory.ClickType.SWAP){
         close();
      }else if(index > 9){
         int invSlot = index >= 36 ? index - 36 : index;
         ItemStack stack = player.getInventory().getItem(invSlot);
         if(ItemStack.isSameItemSameComponents(beltStack,stack)){
            close();
            return false;
         }
      }
      
      return super.onAnyClick(index, type, action);
   }
   
   @Override
   public void onClose(){
      NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
      int charmCount = 0;
      for(int i = 0; i < inv.getContainerSize(); i++){
         ItemStack itemStack = inv.getItem(i);
         if(itemStack.isEmpty()) continue;
         items.set(i, itemStack);
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(itemStack);
         if(arcanaItem != null && arcanaItem.hasCategory(ArcaneTomeGui.TomeFilter.CHARMS)) charmCount++;
      }
      beltStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
      belt.buildItemLore(beltStack,player.level().getServer());
      
      if(charmCount >= slotCount){
         ArcanaAchievements.grant(player,ArcanaAchievements.BELT_CHARMING.id);
      }
   }
}