package net.borisshoes.arcananovum.gui.radiantfletchery;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.forge.RadiantFletcheryBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RadiantFletcheryGui extends SimpleGui implements WatchedGui {
   private final RadiantFletcheryBlockEntity blockEntity;
   private RadiantFletcheryInventory inv;
   private RadiantFletcheryInventoryListener listener;
   
   public RadiantFletcheryGui(ServerPlayerEntity player, RadiantFletcheryBlockEntity blockEntity){
      super(ScreenHandlerType.GENERIC_3X3, player, false);
      this.blockEntity = blockEntity;
      setTitle(Text.literal("Radiant Fletchery"));
   }
   
   public void buildGui(){
      GuiElementBuilder itemsPane = new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).hideFlags().setName(Text.empty());
      setSlot(1,itemsPane);
      setSlot(6,itemsPane);
      setSlot(8,itemsPane);
      
      GuiElementBuilder arrowsItem = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideFlags();
      arrowsItem.setName((Text.literal("")
            .append(Text.literal("Place Arrows Above").formatted(Formatting.YELLOW))));
      setSlot(3,arrowsItem);
      
      GuiElementBuilder brewItem = new GuiElementBuilder(Items.BREWING_STAND).hideFlags();
      brewItem.setName((Text.literal("")
            .append(Text.literal("Create Tipped Arrows").formatted(Formatting.LIGHT_PURPLE))));
      brewItem.addLoreLine((Text.literal("")
            .append(Text.literal("1 Potion Makes "+blockEntity.getPotionRatio()+" Arrows").formatted(Formatting.DARK_PURPLE))));
      setSlot(4,brewItem);
      
      GuiElementBuilder potionItem = new GuiElementBuilder(Items.POTION).hideFlags();
      potionItem.setName((Text.literal("")
            .append(Text.literal("Place Potions Above").formatted(Formatting.DARK_AQUA))));
      setSlot(5,potionItem);
      
      inv = new RadiantFletcheryInventory();
      listener = new RadiantFletcheryInventoryListener(this,blockEntity);
      inv.addListener(listener);
      setSlotRedirect(0,new RadiantFletcherySlot(inv,0,0,0,0));
      setSlotRedirect(2,new RadiantFletcherySlot(inv,1,1,0,1));
      setSlotRedirect(7,new RadiantFletcherySlot(inv,2,2,0,2));
      
      listener.setUpdating();
      for(int i = 0; i < 3; i++){
         inv.setStack(i,blockEntity.getInventory().get(i));
      }
      listener.finishUpdate();
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
