package net.borisshoes.arcananovum.gui.twilightanvil;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TinkerInventoryListener implements InventoryChangedListener {
   private final SimpleGui gui;
   private final TwilightAnvilBlockEntity blockEntity;
   private boolean updating = false;
   private final int[][] dynamicSlots = {{},{3},{1,5},{1,3,5},{0,2,4,6},{1,2,3,4,5},{0,1,2,4,5,6},{0,1,2,3,4,5,6}};
   private final int mode; // 0 = tinkering, 1 = renaming, 2 = anvil
   
   public TinkerInventoryListener(SimpleGui gui, int mode, TwilightAnvilBlockEntity blockEntity){
      this.gui = gui;
      this.mode = mode;
      this.blockEntity = blockEntity;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         setUpdating();
         for(int i = 0; i < inv.size(); i++){
            ItemStack stack = inv.getStack(i);
            if(stack.getCount() != 0){
               if(mode == 1 && i == 0 && gui instanceof RenameGui renameGui){
                  renameGui.setItem(stack);
               }
            }
         }
         //Update gui
         if(gui instanceof TwilightAnvilGui tgui){
            tgui.redrawGui(inv);
         }else{
            if(mode == 1){
               ItemStack item = inv.getStack(0);
               if(item.isEmpty()){
                  gui.setSlot(2,ItemStack.EMPTY);
               }
            }
         }
         
         finishUpdate();
      }
   }
   
   
   public void finishUpdate(){
      updating = false;
   }
   public void setUpdating(){
      updating = true;
   }
}
