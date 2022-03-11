package net.borisshoes.arcananovum.gui;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.server.network.ServerPlayerEntity;

public class LoreGui extends BookGui {
   
   ArcaneTome tome;
   
   public LoreGui(ServerPlayerEntity player, BookElementBuilder book, ArcaneTome tome){
      super(player, book);
      this.tome = tome;
   }
   
   @Override
   public void onTakeBookButton() {
      this.close();
   }
   
   @Override
   public void onClose(){
      tome.openGui(player,1);
   }
}
