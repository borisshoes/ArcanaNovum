package net.borisshoes.arcananovum.gui.radiantfletchery;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.forge.RadiantFletcheryBlockEntity;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class RadiantFletcheryGui extends SimpleGui {
   private final RadiantFletcheryBlockEntity blockEntity;
   
   public RadiantFletcheryGui(ServerPlayerEntity player, RadiantFletcheryBlockEntity blockEntity){
      super(ScreenHandlerType.GENERIC_3X3, player, false);
      this.blockEntity = blockEntity;
      setTitle(Text.literal("Radiant Fletchery"));
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
      setSlot(1,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(6,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_BOTTOM_LEFT, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(8,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_BOTTOM_RIGHT, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      GuiElementBuilder arrowsItem = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideDefaultTooltip();
      arrowsItem.setName((Text.literal("")
            .append(Text.literal("Place Arrows Above").formatted(Formatting.YELLOW))));
      setSlot(3,arrowsItem);
      
      GuiElementBuilder brewItem = new GuiElementBuilder(Items.BREWING_STAND).hideDefaultTooltip();
      brewItem.setName((Text.literal("")
            .append(Text.literal("Create Tipped Arrows").formatted(Formatting.LIGHT_PURPLE))));
      brewItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("1 Potion Makes "+blockEntity.getPotionRatio()+" Arrows").formatted(Formatting.DARK_PURPLE)))));
      setSlot(4,brewItem);
      
      GuiElementBuilder potionItem = new GuiElementBuilder(Items.POTION).hideDefaultTooltip();
      potionItem.setName((Text.literal("")
            .append(Text.literal("Place Potions Above").formatted(Formatting.DARK_AQUA))));
      setSlot(5,potionItem);
      
      Inventory inv = blockEntity.getInventory();
      setSlotRedirect(0,new RadiantFletcherySlot(inv,0,0,0,0));
      setSlotRedirect(2,new RadiantFletcherySlot(inv,1,1,0,1));
      setSlotRedirect(7,new RadiantFletcherySlot(inv,2,2,0,2));
   }
   
   @Override
   public void close(){
      blockEntity.removePlayer(player);
      super.close();
   }
}
