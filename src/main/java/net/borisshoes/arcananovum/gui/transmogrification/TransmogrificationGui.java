package net.borisshoes.arcananovum.gui.transmogrification;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.gui.PagedGui;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class TransmogrificationGui extends PagedGui<ArcanaSkin> {
   
   public TransmogrificationGui(ServerPlayer player, List<ArcanaSkin> items, Consumer<ArcanaSkin> onConfirm){
      super(MenuType.GENERIC_9x4, player, items);
      
      setTitle(Component.translatable("gui.arcananovum.skin_selection"));
      
      action1TextColor(ChatFormatting.AQUA.getColor().intValue());
      action2TextColor(ChatFormatting.GREEN.getColor().intValue());
      action3TextColor(ChatFormatting.YELLOW.getColor().intValue());
      primaryTextColor(ChatFormatting.LIGHT_PURPLE.getColor().intValue());
      secondaryTextColor(ChatFormatting.DARK_PURPLE.getColor().intValue());
      
      blankItem(GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.PAGE_COLOR)).hideTooltip());
      
      itemElemBuilder((skin, index) -> {
         GuiElementBuilder item;
         if(skin == null){
            item = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CANCEL));
            item.setName(Component.translatable("text.arcananovum.default"));
         }else{
            ArcanaItem arcanaItem = skin.getArcanaItem();
            ItemStack skinStack = arcanaItem.getPrefItemNoLore();
            ArcanaItem.putProperty(skinStack,ArcanaItem.SKIN_TAG,skin.getSerializedName());
            item = GuiElementBuilder.from(skinStack).hideDefaultTooltip();
            item.setName(skin.getName().withStyle(ChatFormatting.BOLD).withColor(skin.getPrimaryColor()));
            List<MutableComponent> descLines = skin.getDescription();
            for(MutableComponent descLine : descLines){
               item.addLoreLine(descLine.withStyle(ChatFormatting.ITALIC).withColor(skin.getSecondaryColor()));
            }
            item.addLoreLine(Component.literal(""));
            item.addLoreLine(Component.translatable("text.arcananovum.item_skin",skinStack.getItemName().copy().withStyle(s -> s.withBold(false))).withColor(skinStack.getItemName().getStyle().getColor().getValue()));
            for(Tuple<MutableComponent, MutableComponent> attribution : skin.getAttributions()){
               item.addLoreLine(Component.literal("").withStyle(ChatFormatting.ITALIC)
                     .append(attribution.getA().withColor(skin.getSecondaryColor()))
                     .append(attribution.getB().withColor(skin.getPrimaryColor())));
            }
         }
         
         item.addLoreLine(Component.literal(""));
         item.addLoreLine(Component.translatable("text.borislib.two_elements", Component.translatable("gui.borislib.click").withColor(this.action1TextColor), Component.translatable("gui.arcananovum.to_select").withColor(this.secondaryTextColor)));
         return item;
      });
      
      elemClickFunction((skin, index, type) -> {
         onConfirm.accept(skin);
         this.close();
      });
      
      buildPage();
   }
   
   @Override
   public void buildPage(){
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR, Component.literal(""));
      super.buildPage();
      GuiElementBuilder labelItem = GuiElementBuilder.from(ArcanaRegistry.TRANSMOGRIFICATION_CATALYST.getPrefItemNoLore()).hideDefaultTooltip();
      labelItem.setName(Component.translatable("gui.arcananovum.skin_selection").withStyle(ChatFormatting.DARK_AQUA));
      labelItem.addLoreLine(Component.translatable("gui.arcananovum.transmog_description").withStyle(ChatFormatting.GREEN));
      setSlot(4, labelItem);
   }
}
