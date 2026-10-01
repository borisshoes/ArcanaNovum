package net.borisshoes.arcananovum.gui.transmogrification;

import com.mojang.datafixers.util.Pair;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcanaItemCompendiumEntry;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiFilter;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.gui.GuiSort;
import net.borisshoes.borislib.gui.PagedGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.borisshoes.arcananovum.ArcanaRegistry.RECOMMENDED_LIST;

public class TransmogrificationGui extends PagedGui<ArcanaSkin> {
   
   public TransmogrificationGui(ServerPlayer player, List<ArcanaSkin> items, Consumer<ArcanaSkin> onConfirm){
      super(getMenuType(items.size()), player, items);
      
      setTitle(Component.translatable("gui.arcananovum.skin_selection"));
      
      action1TextColor(TextColor.AQUA.getValue());
      action2TextColor(TextColor.GREEN.getValue());
      action3TextColor(TextColor.YELLOW.getValue());
      primaryTextColor(TextColor.LIGHT_PURPLE.getValue());
      secondaryTextColor(TextColor.DARK_PURPLE.getValue());
      
      blankItem(GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.PAGE_COLOR)).hideTooltip());
      
      curSort(SkinSort.RECOMMENDED);
      curFilter(SkinFilter.NONE);
      
      itemElemBuilder((skin, index) -> {
         GuiElementBuilder item;
         if(skin == null){
            item = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CANCEL));
            item.setName(Component.translatable("text.arcananovum.default"));
         }else{
            ArcanaItem arcanaItem = skin.getArcanaItem();
            ItemStack skinStack = arcanaItem.getPrefItemNoLore();
            ArcanaItem.putProperty(skinStack, ArcanaItem.SKIN_TAG, skin.getSerializedName());
            item = GuiElementBuilder.from(skinStack).hideDefaultTooltip();
            item.setName(skin.getName().withStyle(ChatFormatting.BOLD).withColor(skin.getPrimaryColor()));
            List<MutableComponent> descLines = skin.getDescription();
            for(MutableComponent descLine : descLines){
               item.addLoreLine(descLine.withStyle(ChatFormatting.ITALIC).withColor(skin.getSecondaryColor()));
            }
            item.addLoreLine(Component.literal(""));
            item.addLoreLine(Component.translatable("text.arcananovum.item_skin", skinStack.getItemName().copy().withStyle(s -> s.withBold(false))).withColor(skinStack.getItemName().getStyle().getColor().getValue()));
            for(Pair<MutableComponent, MutableComponent> attribution : skin.getAttributions()){
               item.addLoreLine(Component.literal("").withStyle(ChatFormatting.ITALIC)
                     .append(attribution.getFirst().withColor(skin.getSecondaryColor()))
                     .append(attribution.getSecond().withColor(skin.getPrimaryColor())));
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
   
   private static MenuType<?> getMenuType(int skinCount){
      int rows = (skinCount + 6) / 7;
      if(rows <= 1) return MenuType.GENERIC_9x3;
      if(rows == 2) return MenuType.GENERIC_9x4;
      if(rows == 3) return MenuType.GENERIC_9x5;
      return MenuType.GENERIC_9x6;
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
   
   private static class SkinFilter extends GuiFilter<ArcanaSkin> {
      public static final List<SkinFilter> FILTERS = new ArrayList<>();
      public static final SkinFilter NONE = new SkinFilter("gui.arcananovum.none", TextColor.WHITE.getValue(), skin -> true);
      public static final SkinFilter MUNDANE = new SkinFilter("gui.arcananovum.mundane", TextColor.GRAY.getValue(), skin -> skin.getArcanaItem().getRarity() == ArcanaRarity.MUNDANE);
      public static final SkinFilter EMPOWERED = new SkinFilter("gui.arcananovum.empowered", TextColor.GREEN.getValue(), skin -> skin.getArcanaItem().getRarity() == ArcanaRarity.EMPOWERED);
      public static final SkinFilter EXOTIC = new SkinFilter("gui.arcananovum.exotic", TextColor.AQUA.getValue(), skin -> skin.getArcanaItem().getRarity() == ArcanaRarity.EXOTIC);
      public static final SkinFilter SOVEREIGN = new SkinFilter("gui.arcananovum.sovereign", TextColor.GOLD.getValue(), skin -> skin.getArcanaItem().getRarity() == ArcanaRarity.SOVEREIGN);
      public static final SkinFilter DIVINE = new SkinFilter("gui.arcananovum.divine", TextColor.LIGHT_PURPLE.getValue(), skin -> skin.getArcanaItem().getRarity() == ArcanaRarity.DIVINE);
      public static final SkinFilter ITEMS = new SkinFilter("gui.arcananovum.items", TextColor.DARK_AQUA.getValue(), skin -> !(skin.getArcanaItem() instanceof ArcanaBlock));
      public static final SkinFilter BLOCKS = new SkinFilter("gui.arcananovum.blocks", TextColor.DARK_PURPLE.getValue(), skin -> skin.getArcanaItem() instanceof ArcanaBlock);
      
      private SkinFilter(String key, int color, Predicate<ArcanaSkin> predicate){
         super(key, color, skin -> skin == null || predicate.test(skin));
         FILTERS.add(this);
      }
      
      @Override
      protected List<SkinFilter> getList(){
         return FILTERS;
      }
      
      @SuppressWarnings("unchecked")
      public SkinFilter getStaticDefault(){
         return NONE;
      }
   }
   
   private static class SkinSort extends GuiSort<ArcanaSkin> {
      public static final List<SkinSort> SORTS = new ArrayList<>();
      
      private static final Comparator<ArcanaSkin> SKIN_NAME = Comparator.comparing(skin -> skin.getName().getString(), String.CASE_INSENSITIVE_ORDER);
      private static final Comparator<ArcanaSkin> ITEM_NAME = Comparator.comparing(skin -> skin.getArcanaItem().getTranslatedName().getString(), String.CASE_INSENSITIVE_ORDER);
      
      public static final SkinSort RECOMMENDED = new SkinSort("gui.arcananovum.item_recommended", TextColor.YELLOW.getValue(),
            Comparator.comparingInt(SkinSort::getRecommendedIndex).thenComparing(SKIN_NAME));
      public static final SkinSort SKIN_NAME_ASC = new SkinSort("gui.arcananovum.skin_name_ascending", TextColor.GREEN.getValue(),
            SKIN_NAME);
      public static final SkinSort SKIN_NAME_DESC = new SkinSort("gui.arcananovum.skin_name_descending", TextColor.DARK_GREEN.getValue(),
            SKIN_NAME.reversed());
      public static final SkinSort ITEM_NAME_ASC = new SkinSort("gui.arcananovum.item_name_ascending", TextColor.AQUA.getValue(),
            ITEM_NAME.thenComparing(SKIN_NAME));
      public static final SkinSort ITEM_NAME_DESC = new SkinSort("gui.arcananovum.item_name_descending", TextColor.DARK_AQUA.getValue(),
            ITEM_NAME.reversed().thenComparing(SKIN_NAME));
      
      private SkinSort(String key, int color, Comparator<ArcanaSkin> comparator){
         super(key, color, Comparator.nullsFirst(comparator));
         SORTS.add(this);
      }
      
      private static int getRecommendedIndex(ArcanaSkin skin){
         for(int i = 0; i < RECOMMENDED_LIST.size(); i++){
            if(RECOMMENDED_LIST.get(i) instanceof ArcanaItemCompendiumEntry entry && entry.getArcanaItem() == skin.getArcanaItem()) return i;
         }
         return Integer.MAX_VALUE;
      }
      
      @Override
      protected List<SkinSort> getList(){
         return SORTS;
      }
      
      @SuppressWarnings("unchecked")
      public SkinSort getStaticDefault(){
         return RECOMMENDED;
      }
   }
}
