package net.borisshoes.arcananovum.gui;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class LoreGui extends BookGui {
   
   private final ArcaneTome tome;
   private final int returnMode;
   
   public LoreGui(ServerPlayerEntity player, BookElementBuilder book, @Nullable ArcaneTome tome, int returnMode){
      super(player, book);
      this.tome = tome;
      this.returnMode = returnMode;
   }
   
   @Override
   public void onTakeBookButton() {
      this.close();
   }
   
   @Override
   public void onClose(){
      if(tome != null){
         tome.openGui(player,returnMode);
      }
   }
}
