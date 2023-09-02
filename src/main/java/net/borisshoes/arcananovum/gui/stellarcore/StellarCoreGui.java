package net.borisshoes.arcananovum.gui.stellarcore;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.forge.StellarCoreBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StellarCoreGui  extends SimpleGui implements WatchedGui {
   private final StellarCoreBlockEntity blockEntity;
   private StellarCoreInventory inv;
   private StellarCoreInventoryListener listener;
   
   public StellarCoreGui(ServerPlayerEntity player, StellarCoreBlockEntity blockEntity){
      super(ScreenHandlerType.GENERIC_3X3, player, false);
      this.blockEntity = blockEntity;
      setTitle(Text.literal("Stellar Core"));
   }
   
   public void buildGui(){
      GuiElementBuilder magmaItem = new GuiElementBuilder(Items.MAGMA_BLOCK).hideFlags();
      magmaItem.setName((Text.literal("")
            .append(Text.literal("Insert Item to Salvage It").formatted(Formatting.GOLD))));
      setSlot(0,magmaItem);
      setSlot(2,magmaItem);
      setSlot(6,magmaItem);
      setSlot(8,magmaItem);
      
      GuiElementBuilder fireItem = new GuiElementBuilder(Items.BLAZE_POWDER).hideFlags();
      fireItem.setName((Text.literal("")
            .append(Text.literal("Insert Item to Salvage It").formatted(Formatting.GOLD))));
      setSlot(1,fireItem);
      setSlot(3,fireItem);
      setSlot(5,fireItem);
      setSlot(7,fireItem);
      
      inv = new StellarCoreInventory();
      listener = new StellarCoreInventoryListener(this,blockEntity);
      inv.addListener(listener);
      setSlotRedirect(4,new Slot(inv,0,0,0));
   }
   
   @Override
   public void onClose(){
      MiscUtils.returnItems(inv,player);
   }
   
   @Override
   public void close(){
      super.close();
   }
   
   @Override
   public BlockEntity getBlockEntity(){
      return blockEntity;
   }
   
   @Override
   public SimpleGui getGui(){
      return this;
   }
}
