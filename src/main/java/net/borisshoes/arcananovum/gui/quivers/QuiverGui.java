package net.borisshoes.arcananovum.gui.quivers;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QuiverGui extends SimpleGui {
   
   private final QuiverItem quiver;
   private final ItemStack item;
   private SimpleContainer inv;
   private final boolean runic;
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param player                the player to server this gui to
    *                              will be treated as slots of this gui
    */
   public QuiverGui(ServerPlayer player, QuiverItem quiver, ItemStack item, boolean runic){
      super(MenuType.GENERIC_3x3, player, false);
      this.quiver = quiver;
      this.item = item;
      this.runic = runic;
   }
   
   public void build(){
      inv = new SimpleContainer(QuiverItem.size);
      QuiverInventoryListener listener = new QuiverInventoryListener(quiver,this,item);
      inv.addListener(listener);
      listener.setUpdating();
      
      for(int i = 0; i < inv.getContainerSize(); i++){
         setSlotRedirect(i, new QuiverSlot(inv,runic,i,i%3,i/3));
      }
      
      ItemContainerContents arrows = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
      AtomicInteger i = new AtomicInteger();
      arrows.stream().forEachOrdered(stack -> {
         inv.setItem(i.get(),stack);
         i.getAndIncrement();
      });
      
      setTitle(quiver.getTranslatedName());
      listener.finishUpdate();
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      if(type == ClickType.OFFHAND_SWAP || action == net.minecraft.world.inventory.ClickType.SWAP){
         close();
      }else if(index > 9){
         int invSlot = index >= 36 ? index - 36 : index;
         ItemStack stack = player.getInventory().getItem(invSlot);
         if(ItemStack.isSameItemSameComponents(item,stack)){
            close();
            return false;
         }
      }
      
      return super.onAnyClick(index, type, action);
   }
   
   @Override
   public void onClose(){
      item.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inv.items));
   
      List<Integer> tippedTypes = new ArrayList<>();
      if(!runic){
         for(int i = 0; i < inv.getContainerSize(); i++){
            ItemStack arrowStack = inv.getItem(i);
            if(arrowStack.is(Items.TIPPED_ARROW) && arrowStack.has(DataComponents.POTION_CONTENTS)){
               int color = arrowStack.get(DataComponents.POTION_CONTENTS).getColor();
               if(!tippedTypes.contains(color)) tippedTypes.add(color);
            }
         }
         if(tippedTypes.size() == 9) ArcanaAchievements.grant(player,ArcanaAchievements.DIVERSE_ARSENAL.id);
      }
      quiver.buildItemLore(item,player.level().getServer());
   }
   
   
}
