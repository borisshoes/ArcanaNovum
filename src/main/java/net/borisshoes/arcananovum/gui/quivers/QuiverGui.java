package net.borisshoes.arcananovum.gui.quivers;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class QuiverGui extends SimpleGui {
   
   private final QuiverItem quiver;
   private final ItemStack item;
   private QuiverInventory inv;
   private final boolean runic;
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param player                the player to server this gui to
    *                              will be treated as slots of this gui
    */
   public QuiverGui(ServerPlayerEntity player, QuiverItem quiver, ItemStack item, boolean runic){
      super(ScreenHandlerType.GENERIC_3X3, player, false);
      this.quiver = quiver;
      this.item = item;
      this.runic = runic;
   }
   
   public void build(){
      inv = new QuiverInventory();
      QuiverInventoryListener listener = new QuiverInventoryListener(quiver,this,item);
      inv.addListener(listener);
      listener.setUpdating();
      
      for(int i = 0; i < inv.size(); i++){
         setSlotRedirect(i, new QuiverSlot(inv,runic,i,i%3,i/3));
      }
      
      NbtList arrows = ArcanaItem.getListProperty(item, QuiverItem.ARROWS_TAG, NbtElement.COMPOUND_TYPE);
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound arrow = arrows.getCompound(i);
         int slot = arrow.getByte("Slot");
         ItemStack stack = ItemStack.fromNbt(player.getRegistryManager(),arrow).orElse(ItemStack.EMPTY);
         if(stack.getCount() > 0 && !stack.isEmpty())
            inv.setStack(slot,stack);
      }
      
      setTitle(quiver.getTranslatedName());
      listener.finishUpdate();
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(type == ClickType.OFFHAND_SWAP || action == SlotActionType.SWAP){
         close();
      }else if(index > 9){
         int invSlot = index >= 36 ? index - 36 : index;
         ItemStack stack = player.getInventory().getStack(invSlot);
         if(ItemStack.areItemsAndComponentsEqual(item,stack)){
            close();
            return false;
         }
      }
      
      return super.onAnyClick(index, type, action);
   }
   
   @Override
   public void onClose(){
      NbtList arrows = new NbtList();
      
      for(int i = 0; i < inv.size(); i++){
         ItemStack arrowStack = inv.getStack(i);
         if(arrowStack.isEmpty()) continue;
         NbtCompound arrow = (NbtCompound) arrowStack.encodeAllowEmpty(player.getRegistryManager());
         arrow.putByte("Slot", (byte) i);
         arrows.add(arrow);
      }
      ArcanaItem.putProperty(item,QuiverItem.ARROWS_TAG,arrows);
   
      List<Integer> tippedTypes = new ArrayList<>();
      if(!runic){
         for(int i = 0; i < inv.size(); i++){
            ItemStack arrowStack = inv.getStack(i);
            if(arrowStack.isOf(Items.TIPPED_ARROW) && arrowStack.contains(DataComponentTypes.POTION_CONTENTS)){
               int color = arrowStack.get(DataComponentTypes.POTION_CONTENTS).getColor();
               if(!tippedTypes.contains(color)) tippedTypes.add(color);
            }
         }
         if(tippedTypes.size() == 9) ArcanaAchievements.grant(player,ArcanaAchievements.DIVERSE_ARSENAL.id);
      }
   }
   
   
}
