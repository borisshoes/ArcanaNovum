package net.borisshoes.arcananovum.gui.radiantfletchery;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.forge.RadiantFletcheryBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class RadiantFletcheryGui extends SimpleGui {
   private final RadiantFletcheryBlockEntity blockEntity;
   
   public RadiantFletcheryGui(ServerPlayer player, RadiantFletcheryBlockEntity blockEntity){
      super(MenuType.GENERIC_3x3, player, false);
      this.blockEntity = blockEntity;
      setTitle(Component.literal("Radiant Fletchery"));
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
      setSlot(1, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_CONNECTOR, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(6, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_LEFT, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(8, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_RIGHT, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      GuiElementBuilder arrowsItem = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideDefaultTooltip();
      arrowsItem.setName((Component.literal("")
            .append(Component.literal("Place Arrows Above").withStyle(ChatFormatting.YELLOW))));
      setSlot(3, arrowsItem);
      
      GuiElementBuilder brewItem = new GuiElementBuilder(Items.BREWING_STAND).hideDefaultTooltip();
      brewItem.setName((Component.literal("")
            .append(Component.literal("Create Tipped Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))));
      brewItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("1 Potion Makes " + blockEntity.getPotionRatio() + " Arrows").withStyle(ChatFormatting.DARK_PURPLE)))));
      setSlot(4, brewItem);
      
      GuiElementBuilder potionItem = new GuiElementBuilder(Items.POTION).hideDefaultTooltip();
      potionItem.setName((Component.literal("")
            .append(Component.literal("Place Potions Above").withStyle(ChatFormatting.DARK_AQUA))));
      setSlot(5, potionItem);
      
      Container inv = blockEntity.getInventory();
      setSlotRedirect(0, new RadiantFletcherySlot(inv, 0, 0, 0, 0));
      setSlotRedirect(2, new RadiantFletcherySlot(inv, 1, 1, 0, 1));
      setSlotRedirect(7, new RadiantFletcherySlot(inv, 2, 2, 0, 2));
   }
   
   @Override
   public void close(){
      blockEntity.removePlayer(player);
      super.close();
   }
}
