package net.borisshoes.arcananovum.gui.arcanetome;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;

public class LoreGui extends BookGui {
   
   private final SimpleGui returnGui;
   
   public LoreGui(ServerPlayer player, BookElementBuilder book, SimpleGui returnGui){
      super(player, book);
      this.returnGui = returnGui;
   }
   
   @Override
   public void onTakeBookButton(){
      this.close();
   }
   
   @Override
   public void onClose(){
      if(returnGui != null){
         returnGui.open();
      }
   }
}
