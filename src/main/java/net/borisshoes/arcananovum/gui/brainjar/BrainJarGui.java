package net.borisshoes.arcananovum.gui.brainjar;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.BrainJar;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class BrainJarGui extends SimpleGui {
   private BrainJar jar;
   private ItemStack item;
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param type                        the screen handler that the client should display
    * @param player                      the player to server this gui to
    */
   public BrainJarGui(ScreenHandlerType<?> type, ServerPlayerEntity player, BrainJar jar, ItemStack item){
      super(type, player, false);
      this.jar = jar;
      this.item = item;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      if(index == 0){
         jar.depositXP(player,item, type != ClickType.MOUSE_RIGHT,this);
      }else if(index == 2){
         jar.toggleMending(this,player,item);
      }else if(index == 4){
         jar.withdrawXP(player,item, type != ClickType.MOUSE_RIGHT,this);
      }
      return true;
   }
   
   @Override
   public void onClose(){
   
   }
   
}
