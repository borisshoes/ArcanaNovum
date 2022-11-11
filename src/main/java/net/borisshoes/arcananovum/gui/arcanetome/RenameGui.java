package net.borisshoes.arcananovum.gui.arcanetome;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class RenameGui extends AnvilInputGui {
   private final ArcaneTome tome;
   private final ItemStack item;
   private Text newName;
   private final TomeGui.CompendiumSettings settings;
   
   /**
    * Constructs a new input gui for the provided player.
    * @param player                the player to serve this gui to
    *                              will be treated as slots of this gui
    */
   public RenameGui(ServerPlayerEntity player, ArcaneTome tome, TomeGui.CompendiumSettings settings, ItemStack item){
      super(player, false);
      this.tome = tome;
      this.item = item;
      this.newName = item.getName();
      this.settings = settings;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      if(index == 2){
         this.close();
      }
      return true;
   }
   
   @Override
   public void onInput(String input) {
      setSlot(0, GuiElementBuilder.from(item));
      
      ItemStack newItem = item.copy();
      Text name = newItem.getName();
      newName = Text.literal(input);
      List<Text> textList = newName.getWithStyle(name.getStyle());
      if(!textList.isEmpty()){
         newName = textList.get(0);
         setSlot(2, GuiElementBuilder.from(newItem.setCustomName(newName)));
      }
   }
   
   @Override
   public void onClose(){
      if(tome != null){
         tome.openTinkerGui(player,settings,item.setCustomName(newName));
      }
   }
}
