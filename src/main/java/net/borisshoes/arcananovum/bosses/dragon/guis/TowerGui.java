package net.borisshoes.arcananovum.bosses.dragon.guis;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.HotbarGui;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public class TowerGui extends HotbarGui {
   
   DragonBossFight.ReclaimState reclaimState;
   private final int shieldCD = 600;
   private int shieldTicks = 0;
   
   public TowerGui(ServerPlayer player, DragonBossFight.ReclaimState state){
      super(player);
      reclaimState = state;
   }
   
   public void buildGui(){
      this.setSlot(0, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip());
      this.setSlot(1, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip());
      this.setSlot(3, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip());
      this.setSlot(4, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip());
      this.setSlot(6, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip());
      this.setSlot(7, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip());
   
      this.setSlot(2, new GuiElementBuilder(Items.TWISTING_VINES).setName(Component.literal("Channel Laser").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA)).glow());
      this.setSlot(5, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS).setName(Component.literal("Protect Allies").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA)).glow());
      this.setSlot(8, new GuiElementBuilder(Items.BARRIER).setName(Component.literal("Exit").withStyle(ChatFormatting.BOLD, ChatFormatting.RED)));
   }
   
   @Override
   public boolean onClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action, GuiElementInterface element){
      if(type == ClickType.MOUSE_LEFT || type == ClickType.MOUSE_RIGHT){
         if(index == 8){
            this.close();
         }else if(index == 2){
            reclaimState.castLaser();
         }else if(index == 5){
            if(shieldTicks == 0){
               reclaimState.castShield();
               shieldTicks = shieldCD;
            }else{
               player.displayClientMessage(Component.literal("The Shield Spell is on Cooldown!").withStyle(ChatFormatting.AQUA),true);
            }
         }
      }
      return super.onClick(index, type, action, element);
   }
   
   @Override
   public void onTick(){
      if(shieldTicks > 0){
         shieldTicks--;
         this.setSlot(5, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS).setName(Component.literal("Protect Allies").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA)).setCount(shieldTicks/20 + 1));
      }
      if(shieldTicks == 0){
         this.setSlot(5, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS).setName(Component.literal("Protect Allies").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA)).glow());
      }
      super.onTick();
   }
   
   @Override
   public void onClose(){
      reclaimState.playerExits();
   }
}
