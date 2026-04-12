package net.borisshoes.arcananovum.gui.interdictor;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.InterdictorBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

public class InterdictorGui extends SimpleGui {
   private final InterdictorBlockEntity interdictor;
   
   public InterdictorGui(ServerPlayer player, InterdictorBlockEntity interdictor){
      super(ArcanaAugments.getAugmentFromMap(interdictor.getAugments(), ArcanaAugments.PRECISION_INTERDICTION) > 0 ? MenuType.GENERIC_3x3 : MenuType.HOPPER, player, false);
      this.interdictor = interdictor;
      setTitle(ArcanaRegistry.INTERDICTOR.getTranslatedName());
      if(ArcanaAugments.getAugmentFromMap(interdictor.getAugments(), ArcanaAugments.PRECISION_INTERDICTION) > 0){
         buildEditable();
      }else{
         buildUneditable();
      }
   }
   
   @Override
   public void onTick(){
      Level world = interdictor.getLevel();
      if(world == null || world.getBlockEntity(interdictor.getBlockPos()) != interdictor || !interdictor.isAssembled()){
         this.close();
      }
      super.onTick();
   }
   
   private void buildUneditable(){
      int xRange = interdictor.getxRange();
      int yRange = interdictor.getyRange();
      int zRange = interdictor.getzRange();
      
      GuiElementBuilder blank = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip();
      this.setSlot(0, blank);
      this.setSlot(1, blank);
      this.setSlot(3, blank);
      this.setSlot(4, blank);
      
      GuiElementBuilder inter = GuiElementBuilder.from(ArcanaRegistry.INTERDICTOR.getPrefItemNoLore()).hideDefaultTooltip();
      MutableComponent curRange = Component.literal("Range: " + xRange + ", " + yRange + ", " + zRange);
      inter.addLoreLine(curRange.withStyle(ChatFormatting.AQUA));
      this.setSlot(2, inter);
   }
   
   private void buildEditable(){
      int xRange = interdictor.getxRange();
      int yRange = interdictor.getyRange();
      int zRange = interdictor.getzRange();
      
      MutableComponent curRange = Component.literal("Range: " + xRange + ", " + yRange + ", " + zRange);
      
      GuiElementBuilder inter = GuiElementBuilder.from(ArcanaRegistry.INTERDICTOR.getPrefItemNoLore()).hideDefaultTooltip();
      inter.addLoreLine(curRange.withStyle(ChatFormatting.AQUA));
      this.setSlot(4, inter);
      this.setSlot(3, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR_LIGHT, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip().addLoreLine(curRange.withStyle(ChatFormatting.AQUA)));
      this.setSlot(5, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR_LIGHT, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip().addLoreLine(curRange.withStyle(ChatFormatting.AQUA)));
      
      GuiElementBuilder xUp = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_UP, 0x5deefa)).hideDefaultTooltip();
      GuiElementBuilder yUp = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_UP, 0x5deefa)).hideDefaultTooltip();
      GuiElementBuilder zUp = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_UP, 0x5deefa)).hideDefaultTooltip();
      xUp.setName(Component.literal("Increase X Range").withStyle(ChatFormatting.AQUA));
      yUp.setName(Component.literal("Increase Y Range").withStyle(ChatFormatting.AQUA));
      zUp.setName(Component.literal("Increase Z Range").withStyle(ChatFormatting.AQUA));
      Component upLine1 = Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to increase the range by ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(1 + "").withStyle(ChatFormatting.AQUA));
      Component upLine2 = Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to increase the range by ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(8 + "").withStyle(ChatFormatting.AQUA));
      Component upLine3 = Component.literal("").append(Component.literal("Shift Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to increase the range by ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(16 + "").withStyle(ChatFormatting.AQUA));
      xUp.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(upLine1).addLoreLine(upLine2).addLoreLine(upLine3);
      yUp.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(upLine1).addLoreLine(upLine2).addLoreLine(upLine3);
      zUp.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(upLine1).addLoreLine(upLine2).addLoreLine(upLine3);
      xUp.setCallback((click) -> {
         if(click.shift){
            interdictor.setxRange(xRange + 16);
         }else if(click.isRight){
            interdictor.setxRange(xRange + 8);
         }else{
            interdictor.setxRange(xRange + 1);
         }
         buildEditable();
      });
      yUp.setCallback((click) -> {
         if(click.shift){
            interdictor.setyRange(yRange + 16);
         }else if(click.isRight){
            interdictor.setyRange(yRange + 8);
         }else{
            interdictor.setyRange(yRange + 1);
         }
         buildEditable();
      });
      zUp.setCallback((click) -> {
         if(click.shift){
            interdictor.setzRange(zRange + 16);
         }else if(click.isRight){
            interdictor.setzRange(zRange + 8);
         }else{
            interdictor.setzRange(zRange + 1);
         }
         buildEditable();
      });
      setSlot(0, xUp);
      setSlot(1, yUp);
      setSlot(2, zUp);
      
      GuiElementBuilder xDown = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_DOWN, 0x1D8F95)).hideDefaultTooltip();
      GuiElementBuilder yDown = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_DOWN, 0x1D8F95)).hideDefaultTooltip();
      GuiElementBuilder zDown = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_DOWN, 0x1D8F95)).hideDefaultTooltip();
      xDown.setName(Component.literal("Decrease X Range").withStyle(ChatFormatting.DARK_AQUA));
      yDown.setName(Component.literal("Decrease Y Range").withStyle(ChatFormatting.DARK_AQUA));
      zDown.setName(Component.literal("Decrease Z Range").withStyle(ChatFormatting.DARK_AQUA));
      Component downLine1 = Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to decrease the range by ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(1 + "").withStyle(ChatFormatting.AQUA));
      Component downLine2 = Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to decrease the range by ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(8 + "").withStyle(ChatFormatting.AQUA));
      Component downLine3 = Component.literal("").append(Component.literal("Shift Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to decrease the range by ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(16 + "").withStyle(ChatFormatting.AQUA));
      xDown.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(downLine1).addLoreLine(downLine2).addLoreLine(downLine3);
      yDown.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(downLine1).addLoreLine(downLine2).addLoreLine(downLine3);
      zDown.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(downLine1).addLoreLine(downLine2).addLoreLine(downLine3);
      xDown.setCallback((click) -> {
         if(click.shift){
            interdictor.setxRange(xRange - 16);
         }else if(click.isRight){
            interdictor.setxRange(xRange - 8);
         }else{
            interdictor.setxRange(xRange - 1);
         }
         buildEditable();
      });
      yDown.setCallback((click) -> {
         if(click.shift){
            interdictor.setyRange(yRange - 16);
         }else if(click.isRight){
            interdictor.setyRange(yRange - 8);
         }else{
            interdictor.setyRange(yRange - 1);
         }
         buildEditable();
      });
      zDown.setCallback((click) -> {
         if(click.shift){
            interdictor.setzRange(zRange - 16);
         }else if(click.isRight){
            interdictor.setzRange(zRange - 8);
         }else{
            interdictor.setzRange(zRange - 1);
         }
         buildEditable();
      });
      setSlot(6, xDown);
      setSlot(7, yDown);
      setSlot(8, zDown);
   }
   
   
}
