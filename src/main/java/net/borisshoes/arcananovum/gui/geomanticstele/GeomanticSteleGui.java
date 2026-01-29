package net.borisshoes.arcananovum.gui.geomanticstele;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.Vec3;

public class GeomanticSteleGui extends SimpleGui {
   private final GeomanticSteleBlockEntity stele;
   
   public GeomanticSteleGui(ServerPlayer player, GeomanticSteleBlockEntity stele){
      super(MenuType.GENERIC_3x3, player, false);
      this.stele = stele;
      setTitle(ArcanaRegistry.GEOMANTIC_STELE.getTranslatedName());
      build();
   }
   
   private void build(){
      int xRange = (int) stele.getRange().x();
      int yRange = (int) stele.getRange().y();
      int zRange = (int) stele.getRange().z();
      
      MutableComponent curRange = Component.literal("Range: "+xRange+", "+yRange+", "+zRange);
      
      GuiElementBuilder geoStele = GuiElementBuilder.from(ArcanaRegistry.GEOMANTIC_STELE.getPrefItemNoLore()).hideDefaultTooltip();
      geoStele.addLoreLine(curRange.withStyle(ChatFormatting.GRAY));
      this.setSlot(4,geoStele);
      this.setSlot(3, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR_LIGHT, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip().addLoreLine(curRange.withStyle(ChatFormatting.GRAY)));
      this.setSlot(5, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR_LIGHT, ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("")).hideTooltip().addLoreLine(curRange.withStyle(ChatFormatting.GRAY)));
      
      GuiElementBuilder xUp = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_UP, ChatFormatting.GRAY.getColor())).hideDefaultTooltip();
      GuiElementBuilder yUp = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_UP, ChatFormatting.GRAY.getColor())).hideDefaultTooltip();
      GuiElementBuilder zUp = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_UP, ChatFormatting.GRAY.getColor())).hideDefaultTooltip();
      xUp.setName(Component.literal("Increase X Range").withStyle(ChatFormatting.GRAY));
      yUp.setName(Component.literal("Increase Y Range").withStyle(ChatFormatting.GRAY));
      zUp.setName(Component.literal("Increase Z Range").withStyle(ChatFormatting.GRAY));
      Component upLine1 = Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" to increase the range by ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(1+"").withStyle(ChatFormatting.GRAY));
      Component upLine2 = Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" to increase the range by ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(8+"").withStyle(ChatFormatting.GRAY));
      Component upLine3 = Component.literal("").append(Component.literal("Shift Click").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" to increase the range by ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(16+"").withStyle(ChatFormatting.GRAY));
      xUp.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(upLine1).addLoreLine(upLine2).addLoreLine(upLine3);
      yUp.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(upLine1).addLoreLine(upLine2).addLoreLine(upLine3);
      zUp.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(upLine1).addLoreLine(upLine2).addLoreLine(upLine3);
      xUp.setCallback((click) -> {
         if(click.shift){
            stele.setRange(new Vec3(xRange+16,yRange,zRange));
         }else if(click.isRight){
            stele.setRange(new Vec3(xRange+8,yRange,zRange));
         }else{
            stele.setRange(new Vec3(xRange+1,yRange,zRange));
         }
         build();
      });
      yUp.setCallback((click) -> {
         if(click.shift){
            stele.setRange(new Vec3(xRange,yRange+16,zRange));
         }else if(click.isRight){
            stele.setRange(new Vec3(xRange,yRange+8,zRange));
         }else{
            stele.setRange(new Vec3(xRange,yRange+1,zRange));
         }
         build();
      });
      zUp.setCallback((click) -> {
         if(click.shift){
            stele.setRange(new Vec3(xRange,yRange,zRange+16));
         }else if(click.isRight){
            stele.setRange(new Vec3(xRange,yRange,zRange+8));
         }else{
            stele.setRange(new Vec3(xRange,yRange,zRange+1));
         }
         build();
      });
      setSlot(0,xUp);
      setSlot(1,yUp);
      setSlot(2,zUp);
      
      GuiElementBuilder xDown = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_DOWN, ChatFormatting.DARK_GRAY.getColor())).hideDefaultTooltip();
      GuiElementBuilder yDown = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_DOWN, ChatFormatting.DARK_GRAY.getColor())).hideDefaultTooltip();
      GuiElementBuilder zDown = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_DOWN, ChatFormatting.DARK_GRAY.getColor())).hideDefaultTooltip();
      xDown.setName(Component.literal("Decrease X Range").withStyle(ChatFormatting.DARK_GRAY));
      yDown.setName(Component.literal("Decrease Y Range").withStyle(ChatFormatting.DARK_GRAY));
      zDown.setName(Component.literal("Decrease Z Range").withStyle(ChatFormatting.DARK_GRAY));
      Component downLine1 = Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" to decrease the range by ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(1+"").withStyle(ChatFormatting.GRAY));
      Component downLine2 = Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" to decrease the range by ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(8+"").withStyle(ChatFormatting.GRAY));
      Component downLine3 = Component.literal("").append(Component.literal("Shift Click").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" to decrease the range by ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(16+"").withStyle(ChatFormatting.GRAY));
      xDown.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(downLine1).addLoreLine(downLine2).addLoreLine(downLine3);
      yDown.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(downLine1).addLoreLine(downLine2).addLoreLine(downLine3);
      zDown.addLoreLine(curRange.withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(Component.literal("")).addLoreLine(downLine1).addLoreLine(downLine2).addLoreLine(downLine3);
      xDown.setCallback((click) -> {
         if(click.shift){
            stele.setRange(new Vec3(xRange-16,yRange,zRange));
         }else if(click.isRight){
            stele.setRange(new Vec3(xRange-8,yRange,zRange));
         }else{
            stele.setRange(new Vec3(xRange-1,yRange,zRange));
         }
         build();
      });
      yDown.setCallback((click) -> {
         if(click.shift){
            stele.setRange(new Vec3(xRange,yRange-16,zRange));
         }else if(click.isRight){
            stele.setRange(new Vec3(xRange,yRange-8,zRange));
         }else{
            stele.setRange(new Vec3(xRange,yRange-1,zRange));
         }
         build();
      });
      zDown.setCallback((click) -> {
         if(click.shift){
            stele.setRange(new Vec3(xRange,yRange,zRange-16));
         }else if(click.isRight){
            stele.setRange(new Vec3(xRange,yRange,zRange-8));
         }else{
            stele.setRange(new Vec3(xRange,yRange,zRange-1));
         }
         build();
      });
      setSlot(6,xDown);
      setSlot(7,yDown);
      setSlot(8,zDown);
   }
}
