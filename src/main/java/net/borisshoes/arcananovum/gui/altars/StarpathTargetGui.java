package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class StarpathTargetGui extends AnvilInputGui {
   private final StarpathAltarBlockEntity blockEntity;
   private String text;
   private final boolean targetMode;
   private final SimpleGui returnGui;
   private final Consumer<Object> onCompletion;
   
   public StarpathTargetGui(ServerPlayer player, StarpathAltarBlockEntity blockEntity, boolean targetMode, SimpleGui returnGui, Consumer<Object> onCompletion){
      super(player, false);
      this.blockEntity = blockEntity;
      this.targetMode = targetMode;
      this.returnGui = returnGui;
      this.onCompletion = onCompletion;
      
      setTitle(Component.literal(this.targetMode ? "Input Coordinates" : "Input Name"));
      if(targetMode){
         GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideDefaultTooltip();
         locationItem.setName((Component.literal("Enter a Location").withStyle(ChatFormatting.GOLD)));
         locationItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Use format: x,y,z").withStyle(ChatFormatting.YELLOW)))));
         setSlot(0, locationItem);
      }else{
         GuiElementBuilder locationItem = new GuiElementBuilder(Items.WRITABLE_BOOK).hideDefaultTooltip();
         locationItem.setName((Component.literal("Enter a Name").withStyle(ChatFormatting.GOLD)));
         setSlot(0, locationItem);
      }
      setSlot(1, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.STARPATH_COLOR)).hideTooltip());
   }
   
   private BlockPos parseValid(){
      String[] split = text.split(",");
      if(split.length != 3) return null;
      
      try{
         BlockPos target = new BlockPos(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
         if(blockEntity.getLevel() != null && blockEntity.getLevel().isOutsideBuildHeight(target)){
            return null;
         }
         
         return target;
      }catch(Exception e){
         return null;
      }
   }
   
   @Override
   public void onInput(String input){
      text = input;
      
      GuiElementBuilder elem = null;
      if(targetMode){
         BlockPos parsed = parseValid();
         if(parsed == null){
            elem = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CANCEL)).setName(Component.literal("Invalid Location").withStyle(ChatFormatting.DARK_AQUA));
            elem.setCallback((clickType) -> {
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, 1);
            });
            setSlot(2,elem);
         }else{
            elem = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CONFIRM)).hideDefaultTooltip().setName(Component.literal("Valid Location: " + parsed.toShortString()).withStyle(ChatFormatting.DARK_AQUA));
            elem.setCallback((clickType) -> {
               onCompletion.accept(parsed);
               this.close();
            });
            setSlot(2,elem);
         }
      }else if(text != null){
         String trimmedName = text.trim();
         if(trimmedName.isBlank() || trimmedName.length() > 50){
            elem = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CANCEL)).setName(Component.literal("Invalid Name").withStyle(ChatFormatting.DARK_AQUA));
            elem.setCallback((clickType) -> {
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, 1);
            });
            setSlot(2,elem);
         }else{
            elem = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CONFIRM)).setName(Component.literal("Valid Name: " + trimmedName).withStyle(ChatFormatting.DARK_AQUA));
            elem.setCallback((clickType) -> {
               onCompletion.accept(trimmedName);
               this.close();
            });
            setSlot(2,elem);
         }
      }
   }
   
   @Override
   public void onTick(){
      Level world = blockEntity.getLevel();
      if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity || !blockEntity.isAssembled() || blockEntity.isActive()){
         this.close();
      }
      
      super.onTick();
   }
   
   @Override
   public void afterRemoval(){
      if(returnGui != null){
         returnGui.open();
      }
   }
   
   
   @Override
   public void close(){
      super.close();
   }
}
