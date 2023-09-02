package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class StarpathTargetGui extends AnvilInputGui implements WatchedGui {
   private final StarpathAltarBlockEntity blockEntity;
   private String text;
   
   /**
    * Constructs a new SignGui for the provided player
    *
    * @param player the player to serve this gui to
    */
   public StarpathTargetGui(ServerPlayerEntity player, StarpathAltarBlockEntity blockEntity){
      super(player,false);
      this.blockEntity = blockEntity;
      
      setTitle(Text.literal("Input Coordinates"));
      setSlot(1, GuiElementBuilder.from(Items.STRUCTURE_VOID.getDefaultStack()).setName(Text.literal("")));
      
      GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideFlags();
      locationItem.setName((Text.literal("")
            .append(Text.literal("Use format: x,y,z").formatted(Formatting.GOLD))));
      setSlot(0,locationItem);
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
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      if(index == 2){
         this.close();
      }
      return true;
   }
   
   
   @Override
   public void onInput(String input) {
      text = input;
      BlockPos parsed = parseValid();
      
      if(parsed == null){
         GuiElementBuilder locationItem = new GuiElementBuilder(Items.BARRIER).hideFlags();
         locationItem.setName((Text.literal("")
               .append(Text.literal("Invalid Location").formatted(Formatting.RED))));
         setSlot(2,locationItem);
      }else{
         GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideFlags();
         locationItem.setName((Text.literal("")
               .append(Text.literal("Valid Location: "+parsed.toShortString()).formatted(Formatting.DARK_AQUA))));
         setSlot(2,locationItem);
      }
   }
   
   @Override
   public void onClose(){
      BlockPos parsed = parseValid();
      if(parsed != null){
         blockEntity.setTargetCoords(parsed);
      }
      blockEntity.openGui(player);
   }
   
   
   @Override
   public void close(){
      super.close();
   }
   
   @Override
   public BlockEntity getBlockEntity(){
      return blockEntity;
   }
   
   @Override
   public SimpleGui getGui(){
      return this;
   }
}
