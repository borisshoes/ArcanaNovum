package net.borisshoes.arcananovum.gui.stellarcore;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.forge.StellarCoreBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class StellarCoreGui extends SimpleGui {
   private final StellarCoreBlockEntity blockEntity;
   
   public StellarCoreGui(ServerPlayer player, StellarCoreBlockEntity blockEntity){
      super(MenuType.GENERIC_3x3, player, false);
      this.blockEntity = blockEntity;
      setTitle(Component.literal("Stellar Core"));
   }
   
   @Override
   public void onTick(){
      Level world = blockEntity.getLevel();
      if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity || !blockEntity.isAssembled()){
         this.close();
      }
      
      super.onTick();
   }
   
   public void buildGui(){
      GuiElementBuilder magmaItem = new GuiElementBuilder(Items.MAGMA_BLOCK).hideDefaultTooltip();
      magmaItem.setName((Component.literal("")
            .append(Component.literal("Insert Item to Salvage It").withStyle(ChatFormatting.GOLD))));
      setSlot(0,magmaItem);
      setSlot(2,magmaItem);
      setSlot(6,magmaItem);
      setSlot(8,magmaItem);
      
      GuiElementBuilder fireItem = new GuiElementBuilder(Items.BLAZE_POWDER).hideDefaultTooltip();
      fireItem.setName((Component.literal("")
            .append(Component.literal("Insert Item to Salvage It").withStyle(ChatFormatting.GOLD))));
      setSlot(1,fireItem);
      setSlot(3,fireItem);
      setSlot(5,fireItem);
      setSlot(7,fireItem);
      
      Container inv = blockEntity.getInventory();
      setSlotRedirect(4,new Slot(inv,0,0,0));
   }
   
   @Override
   public void close(){
      blockEntity.removePlayer(player);
      super.close();
   }
   
}
