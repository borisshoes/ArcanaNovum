package net.borisshoes.arcananovum.bosses.dragon.guis;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.HotbarGui;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TowerGui extends HotbarGui {
   
   DragonBossFight.ReclaimState reclaimState;
   private final int shieldCD = 600;
   private int shieldTicks = 0;
   
   public TowerGui(ServerPlayerEntity player, DragonBossFight.ReclaimState state){
      super(player);
      reclaimState = state;
   }
   
   public void buildGui(){
      this.setSlot(0, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP, ArcanaColors.EQUAYUS_COLOR)).setName(Text.literal("")).hideTooltip());
      this.setSlot(1, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP, ArcanaColors.EQUAYUS_COLOR)).setName(Text.literal("")).hideTooltip());
      this.setSlot(3, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP, ArcanaColors.EQUAYUS_COLOR)).setName(Text.literal("")).hideTooltip());
      this.setSlot(4, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP, ArcanaColors.EQUAYUS_COLOR)).setName(Text.literal("")).hideTooltip());
      this.setSlot(6, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP, ArcanaColors.EQUAYUS_COLOR)).setName(Text.literal("")).hideTooltip());
      this.setSlot(7, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP, ArcanaColors.EQUAYUS_COLOR)).setName(Text.literal("")).hideTooltip());
   
      this.setSlot(2, new GuiElementBuilder(Items.TWISTING_VINES).setName(Text.literal("Channel Laser").formatted(Formatting.BOLD,Formatting.DARK_AQUA)).glow());
      this.setSlot(5, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS).setName(Text.literal("Protect Allies").formatted(Formatting.BOLD,Formatting.AQUA)).glow());
      this.setSlot(8, new GuiElementBuilder(Items.BARRIER).setName(Text.literal("Exit").formatted(Formatting.BOLD,Formatting.RED)));
   }
   
   @Override
   public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
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
               player.sendMessage(Text.literal("The Shield Spell is on Cooldown!").formatted(Formatting.AQUA),true);
            }
         }
      }
      return super.onClick(index, type, action, element);
   }
   
   @Override
   public void onTick() {
      if(shieldTicks > 0){
         shieldTicks--;
         this.setSlot(5, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS).setName(Text.literal("Protect Allies").formatted(Formatting.BOLD,Formatting.AQUA)).setCount(shieldTicks/20 + 1));
      }
      if(shieldTicks == 0){
         this.setSlot(5, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS).setName(Text.literal("Protect Allies").formatted(Formatting.BOLD,Formatting.AQUA)).glow());
      }
      super.onTick();
   }
   
   @Override
   public void onClose(){
      reclaimState.playerExits();
   }
}
