package net.borisshoes.arcananovum.gui.arcanetome;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class LoreGui extends BookGui {
   
   private final ArcaneTome tome;
   private final ArcaneTome.TomeMode returnMode;
   private final String returnItem;
   private final TomeGui.CompendiumSettings settings;
   
   public LoreGui(ServerPlayerEntity player, BookElementBuilder book, @Nullable ArcaneTome tome, ArcaneTome.TomeMode returnMode, TomeGui.CompendiumSettings settings){
      super(player, book);
      this.tome = tome;
      this.returnMode = returnMode;
      this.returnItem = "";
      this.settings = settings;
   }
   
   public LoreGui(ServerPlayerEntity player, BookElementBuilder book, @Nullable ArcaneTome tome, ArcaneTome.TomeMode returnMode, TomeGui.CompendiumSettings settings, String returnItem){
      super(player, book);
      this.tome = tome;
      this.returnMode = returnMode;
      this.returnItem = returnItem;
      this.settings = settings;
   }
   
   @Override
   public void onTakeBookButton() {
      this.close();
   }
   
   @Override
   public void onClose(){
      if(tome != null){
         tome.openGui(player,returnMode,settings,returnItem);
      }
   }
}
