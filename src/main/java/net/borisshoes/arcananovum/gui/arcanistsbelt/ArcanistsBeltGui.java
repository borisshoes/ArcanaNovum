package net.borisshoes.arcananovum.gui.arcanistsbelt;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverInventory;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ArcanistsBeltGui extends SimpleGui {
   
   private final ArcanistsBelt belt;
   private final ItemStack beltStack;
   private QuiverInventory inv;
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
      inv = new QuiverInventory();
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
      
      NbtList items = ArcanaItem.getListProperty(beltStack,ArcanistsBelt.ITEMS_TAG, NbtElement.COMPOUND_TYPE);
      for(int i = 0; i < items.size(); i++){
         NbtCompound item = items.getCompound(i);
         int slot = item.getByte("Slot");
         ItemStack stack = ItemStack.fromNbt(player.getRegistryManager(),item).orElse(ItemStack.EMPTY);
         if(stack.getCount() > 0 && !stack.isEmpty())
            inv.setStack(slot,stack);
      }
      
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
      NbtList items = new NbtList();
      
      int charmCount = 0;
      for(int i = 0; i < inv.size(); i++){
         ItemStack itemStack = inv.getStack(i);
         if(itemStack.isEmpty()) continue;
         NbtCompound item = (NbtCompound) itemStack.toNbtAllowEmpty(player.getRegistryManager());
         item.putByte("Slot", (byte) i);
         items.add(item);
         
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(itemStack);
         if(arcanaItem != null && arcanaItem.hasCategory(TomeGui.TomeFilter.CHARMS)) charmCount++;
      }
      ArcanaItem.putProperty(beltStack,ArcanistsBelt.ITEMS_TAG,items);
      belt.buildItemLore(beltStack,player.getServer());
      
      if(charmCount >= slotCount){
         ArcanaAchievements.grant(player,ArcanaAchievements.BELT_CHARMING.id);
      }
   }
}