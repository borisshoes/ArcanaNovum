package net.borisshoes.arcananovum.gui.quivers;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QuiverGui extends SimpleGui {
   
   private final QuiverItem quiver;
   private final ItemStack item;
   private SimpleInventory inv;
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
      inv = new SimpleInventory(QuiverItem.size);
      QuiverInventoryListener listener = new QuiverInventoryListener(quiver,this,item);
      inv.addListener(listener);
      listener.setUpdating();
      
      for(int i = 0; i < inv.size(); i++){
         setSlotRedirect(i, new QuiverSlot(inv,runic,i,i%3,i/3));
      }
      
      ContainerComponent arrows = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
      AtomicInteger i = new AtomicInteger();
      arrows.stream().forEachOrdered(stack -> {
         inv.setStack(i.get(),stack);
         i.getAndIncrement();
      });
      
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
      item.set(DataComponentTypes.CONTAINER,ContainerComponent.fromStacks(inv.heldStacks));
   
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
      quiver.buildItemLore(item,player.getServer());
   }
   
   
}
