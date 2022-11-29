package net.borisshoes.arcananovum.gui.quivers;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.items.core.QuiverItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class QuiverGui extends SimpleGui {
   
   private QuiverItem quiver;
   private ItemStack item;
   private QuiverInventory inv;
   private boolean runic;
   
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
   
      NbtCompound tag = item.getNbt();
      NbtCompound magicTag = tag.getCompound("arcananovum");
      NbtList arrows = magicTag.getList("arrows",NbtElement.COMPOUND_TYPE);
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound arrow = arrows.getCompound(i);
         int slot = arrow.getByte("Slot");
         ItemStack stack = ItemStack.fromNbt(arrow);
         if(stack.getCount() > 0 && !stack.isEmpty())
            inv.setStack(slot,stack);
      }
      
      setTitle(Text.literal(quiver.getName()));
      listener.finishUpdate();
   }
   
   @Override
   public void onClose(){
      NbtCompound tag = item.getNbt();
      NbtCompound magicTag = tag.getCompound("arcananovum");
      int slot = magicTag.getInt("slot");
      NbtList arrows = new NbtList();
      
      for(int i = 0; i < inv.size(); i++){
         ItemStack arrowStack = inv.getStack(i);
         if(arrowStack.isEmpty()) continue;
         if(slot == -1){
            slot = i;
            magicTag.putInt("slot",slot);
         }
         NbtCompound arrow = arrowStack.writeNbt(new NbtCompound());
         arrow.putByte("Slot", (byte) i);
         arrows.add(arrow);
      }
      if(arrows.isEmpty()) magicTag.putInt("slot",-1);
      magicTag.put("arrows",arrows);
   }
   
   
}
