package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class StarpathTargetGui extends AnvilInputGui {
   private final StarpathAltarBlockEntity blockEntity;
   private String text;
   private final boolean targetMode;
   private final SimpleGui returnGui;
   private final Consumer<Object> onCompletion;
   
   // TODO Multi-player access may have made this unsafe
   public StarpathTargetGui(ServerPlayerEntity player, StarpathAltarBlockEntity blockEntity, boolean targetMode, SimpleGui returnGui, Consumer<Object> onCompletion){
      super(player,false);
      this.blockEntity = blockEntity;
      this.targetMode = targetMode;
      this.returnGui = returnGui;
      this.onCompletion = onCompletion;
      
      setTitle(Text.literal(this.targetMode ? "Input Coordinates" : "Input Name"));
      if(targetMode){
         GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideDefaultTooltip();
         locationItem.setName((Text.literal("Enter a Location").formatted(Formatting.GOLD)));
         locationItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Use format: x,y,z").formatted(Formatting.YELLOW)))));
         setSlot(0,locationItem);
      }else{
         GuiElementBuilder locationItem = new GuiElementBuilder(Items.WRITABLE_BOOK).hideDefaultTooltip();
         locationItem.setName((Text.literal("Enter a Name").formatted(Formatting.GOLD)));
         setSlot(0,locationItem);
      }
      setSlot(1,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.STARPATH_COLOR)).hideTooltip());
   }
   
   private BlockPos parseValid(){
      String[] split = text.split(",");
      if(split.length != 3) return null;
      
      try{
         BlockPos target = new BlockPos(Integer.parseInt(split[0]),Integer.parseInt(split[1]),Integer.parseInt(split[2]));
         if(blockEntity.getWorld() != null && blockEntity.getWorld().isOutOfHeightLimit(target)){
            return null;
         }
         
         return target;
      }catch(Exception e){
         return null;
      }
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(index == 2){
         if(targetMode){
            BlockPos parsed = parseValid();
            if(parsed == null){
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }else{
               onCompletion.accept(parsed);
               this.close();
            }
         }else{
            String trimmedName = text.trim();
            if(trimmedName.isBlank() || trimmedName.length() > 50){
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }else{
               onCompletion.accept(trimmedName);
               this.close();
            }
         }
      }
      
      //ArcanaNovum.addTickTimerCallback(new GenericTimer(1, ()-> GuiHelpers.sendSlotUpdate(player, this.syncId, 2, getSlot(2).getItemStack())));
      return true;
   }
   
   
   @Override
   public void onInput(String input){
      text = input;
      
      if(targetMode){
         BlockPos parsed = parseValid();
         if(parsed == null){
            setSlot(2, GuiElementBuilder.from(GraphicalItem.with(GraphicItems.CANCEL)).setName(Text.literal("Invalid Location").formatted(Formatting.DARK_AQUA)));
         }else{
            setSlot(2, GuiElementBuilder.from(GraphicalItem.with(GraphicItems.CONFIRM)).hideDefaultTooltip().setName(Text.literal("Valid Location: "+parsed.toShortString()).formatted(Formatting.DARK_AQUA)));
         }
      }else{
         String trimmedName = text.trim();
         if(trimmedName.isBlank() || trimmedName.length() > 50){
            setSlot(2, GuiElementBuilder.from(GraphicalItem.with(GraphicItems.CANCEL)).setName(Text.literal("Invalid Name").formatted(Formatting.DARK_AQUA)));
         }else{
            setSlot(2, GuiElementBuilder.from(GraphicalItem.with(GraphicItems.CONFIRM)).setName(Text.literal("Valid Name: "+trimmedName).formatted(Formatting.DARK_AQUA)));
         }
      }
   }
   
   @Override
   public void onTick(){
      World world = blockEntity.getWorld();
      if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity || !blockEntity.isAssembled() || blockEntity.isActive()){
         this.close();
      }
      
      super.onTick();
   }
   
   @Override
   public void onClose(){
      if(returnGui != null){
         returnGui.open();
      }
   }
   
   
   @Override
   public void close(){
      super.close();
   }
}
