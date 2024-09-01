package net.borisshoes.arcananovum.gui.stellarcore;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.forge.StellarCoreBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class StellarCoreGui  extends SimpleGui {
   private final StellarCoreBlockEntity blockEntity;
   
   public StellarCoreGui(ServerPlayerEntity player, StellarCoreBlockEntity blockEntity){
      super(ScreenHandlerType.GENERIC_3X3, player, false);
      this.blockEntity = blockEntity;
      setTitle(Text.literal("Stellar Core"));
   }
   
   @Override
   public void onTick(){
      World world = blockEntity.getWorld();
      if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity || !blockEntity.isAssembled()){
         this.close();
      }
      
      super.onTick();
   }
   
   public void buildGui(){
      GuiElementBuilder magmaItem = new GuiElementBuilder(Items.MAGMA_BLOCK).hideDefaultTooltip();
      magmaItem.setName((Text.literal("")
            .append(Text.literal("Insert Item to Salvage It").formatted(Formatting.GOLD))));
      setSlot(0,magmaItem);
      setSlot(2,magmaItem);
      setSlot(6,magmaItem);
      setSlot(8,magmaItem);
      
      GuiElementBuilder fireItem = new GuiElementBuilder(Items.BLAZE_POWDER).hideDefaultTooltip();
      fireItem.setName((Text.literal("")
            .append(Text.literal("Insert Item to Salvage It").formatted(Formatting.GOLD))));
      setSlot(1,fireItem);
      setSlot(3,fireItem);
      setSlot(5,fireItem);
      setSlot(7,fireItem);
      
      Inventory inv = blockEntity.getInventory();
      setSlotRedirect(4,new Slot(inv,0,0,0));
   }
   
   @Override
   public void close(){
      blockEntity.removePlayer(player);
      super.close();
   }
   
}
