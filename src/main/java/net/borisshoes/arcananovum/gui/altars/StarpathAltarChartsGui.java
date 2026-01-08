package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.gui.GuiSort;
import net.borisshoes.borislib.gui.PagedGui;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class StarpathAltarChartsGui extends PagedGui<StarpathAltarBlockEntity.TargetEntry> {
   private final StarpathAltarBlockEntity blockEntity;
   private final SimpleGui returnGui;
   private final boolean stargate;
   
   public StarpathAltarChartsGui(ServerPlayer player, SimpleGui returnGui, StarpathAltarBlockEntity blockEntity){
      super(MenuType.GENERIC_9x6, player, new ArrayList<>(blockEntity.getSavedTargets()));
      this.blockEntity = blockEntity;
      this.returnGui = returnGui;
      this.stargate = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.STARGATE.id) > 0;
      setTitle(Component.literal("Star Charts"));
      
      action1TextColor(ChatFormatting.AQUA.getColor().intValue());
      action2TextColor(ChatFormatting.GREEN.getColor().intValue());
      action3TextColor(ChatFormatting.YELLOW.getColor().intValue());
      primaryTextColor(ChatFormatting.DARK_AQUA.getColor().intValue());
      secondaryTextColor(ChatFormatting.BLUE.getColor().intValue());
      
      blankItem(GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,0x20224B)));
      
      itemElemBuilder((entry, index) -> {
         GuiElementBuilder destItem = new GuiElementBuilder(Items.FILLED_MAP).hideDefaultTooltip();
         destItem.setName(Component.literal(entry.name()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
         destItem.addLoreLine(TextUtils.removeItalics(Component.literal(entry.getBlockCoords().toShortString()).withStyle(ChatFormatting.YELLOW)));
         if(stargate){
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, Identifier.parse(entry.dimension()));
            destItem.addLoreLine(Component.literal("Dimension: ").withStyle(ChatFormatting.YELLOW).append(MinecraftUtils.getFormattedDimName(dim)));
         }
         destItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
         destItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to select this destination").withStyle(ChatFormatting.BLUE)))));
         destItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN))
               .append(Component.literal(" to rename this destination").withStyle(ChatFormatting.BLUE)))));
         destItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Shift Left Click").withStyle(ChatFormatting.RED))
               .append(Component.literal(" to delete this destination").withStyle(ChatFormatting.BLUE)))));
         return destItem;
      });
      
      elemClickFunction((item, index, clickType) -> {
         if(clickType == ClickType.MOUSE_RIGHT){ // Rename
            StarpathTargetGui gui = new StarpathTargetGui(player,blockEntity,false,this,(obj) -> {
               String newName = (String) obj;
               blockEntity.getSavedTargets().remove(item);
               blockEntity.getSavedTargets().add(new StarpathAltarBlockEntity.TargetEntry(newName, item.dimension(), item.x(), item.y(), item.z()));
               buildPage();
            });
            gui.open();
         }else if(clickType == ClickType.MOUSE_LEFT_SHIFT){ // Delete
            blockEntity.getSavedTargets().remove(item);
            buildPage();
         }else{ // Select
            blockEntity.setTarget(item);
            close();
         }
      });
      
      TargetSort.setBlockEntity(blockEntity);
      curSort(TargetSort.CLOSEST);
   }
   
   @Override
   public void buildPage(){
      GuiHelper.outlineGUI(this, ArcanaColors.STARPATH_COLOR, Component.literal("Saved Locations").withStyle(ChatFormatting.BLUE));
      items(new ArrayList<>(blockEntity.getSavedTargets()));
      TargetSort.setBlockEntity(blockEntity);
      super.buildPage();
      GuiElementBuilder labelItem = new GuiElementBuilder(Items.ENDER_EYE).hideDefaultTooltip();
      labelItem.setName((Component.literal("")
            .append(Component.literal("Starpath Altar Destinations").withStyle(ChatFormatting.DARK_AQUA))));
      labelItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to return to the altar").withStyle(ChatFormatting.BLUE)))));
      labelItem.setCallback((clickType) -> close());
      setSlot(4,labelItem);
      
      GuiElementBuilder newItem = new GuiElementBuilder(Items.WRITABLE_BOOK).hideDefaultTooltip();
      newItem.setName((Component.literal("")
            .append(Component.literal("New Destination").withStyle(ChatFormatting.DARK_AQUA))));
      newItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to add a new destination").withStyle(ChatFormatting.BLUE)))));
      newItem.setCallback((clickType) -> {
         AtomicReference<BlockPos> newDest = new AtomicReference<>();
         StarpathTargetGui nameGui = new StarpathTargetGui(player,blockEntity,false,this,(obj) -> {
            blockEntity.getSavedTargets().add(new StarpathAltarBlockEntity.TargetEntry((String) obj,player.level().dimension().identifier().toString(),newDest.get().getX(),newDest.get().getY(),newDest.get().getZ()));
            buildPage();
         });
         StarpathTargetGui targetGui = new StarpathTargetGui(player,blockEntity,true,nameGui,(obj) -> newDest.set((BlockPos) obj));
         targetGui.open();
      });
      setSlot(49,newItem);
   }
   
   @Override
   public void onTick(){
      if(blockEntity != null){
         Level world = blockEntity.getLevel();
         if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity || !blockEntity.isAssembled() || blockEntity.isActive()){
            this.close();
         }
      }
   }
   
   @Override
   public void onClose(){
      if(returnGui != null){
         returnGui.open();
      }
   }
   
   private static class TargetSort extends GuiSort<StarpathAltarBlockEntity.TargetEntry> {
      public static final List<TargetSort> SORTS = new ArrayList<>();
      public static StarpathAltarBlockEntity blockEntity;
      
      public static final TargetSort CLOSEST = new TargetSort("gui.arcananovum.closest", ChatFormatting.LIGHT_PURPLE.getColor().intValue(),
            Comparator.comparingInt(pair -> (int) pair.getBlockCoords().distSqr(getBlockEntity().getBlockPos())));
      public static final TargetSort FURTHEST = new TargetSort("gui.arcananovum.furthest", ChatFormatting.DARK_PURPLE.getColor().intValue(),
            Comparator.comparing(pair -> (int) -pair.getBlockCoords().distSqr(getBlockEntity().getBlockPos())));
      public static final TargetSort ALPHABETICAL = new TargetSort("gui.borislib.alphabetical", ChatFormatting.GREEN.getColor().intValue(),
            Comparator.comparing(StarpathAltarBlockEntity.TargetEntry::name));
      
      private TargetSort(String key, int color, Comparator<StarpathAltarBlockEntity.TargetEntry> comparator){
         super(key, color, comparator);
         SORTS.add(this);
      }
      
      public static void setBlockEntity(StarpathAltarBlockEntity blockEntity){
         TargetSort.blockEntity = blockEntity;
      }
      
      public static StarpathAltarBlockEntity getBlockEntity(){
         return blockEntity;
      }
      
      @Override
      protected List<TargetSort> getList(){
         return SORTS;
      }
      
      public TargetSort getStaticDefault(){
         return CLOSEST;
      }
   }
}
