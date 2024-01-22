package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class StarpathTargetGui extends AnvilInputGui implements WatchedGui {
   private final StarpathAltarBlockEntity blockEntity;
   private String text;
   private final boolean starcharts;
   private final HashMap<String,BlockPos> savedTargets;
   private final ArrayList<String> sortedKeys;
   private int selectedTarget = -1;
   
   /**
    * Constructs a new SignGui for the provided player
    *
    * @param player the player to serve this gui to
    */
   public StarpathTargetGui(ServerPlayerEntity player, StarpathAltarBlockEntity blockEntity){
      super(player,false);
      this.blockEntity = blockEntity;
      this.starcharts = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.STAR_CHARTS.id) >= 1;
      this.savedTargets = blockEntity.getSavedTargets();
      this.sortedKeys = new ArrayList<>(savedTargets.keySet());
      Collections.sort(this.sortedKeys);
      
      setTitle(Text.literal("Input Coordinates"));
      setSlot(1, GuiElementBuilder.from(Items.STRUCTURE_VOID.getDefaultStack()).setName(Text.literal("Invalid Location").formatted(Formatting.DARK_AQUA)));
      if(!starcharts){
         GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideFlags();
         locationItem.setName((Text.literal("")
               .append(Text.literal("Use format: x,y,z").formatted(Formatting.GOLD))));
         setSlot(0,locationItem);
      }else{
         GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideFlags();
         locationItem.setName((Text.literal("")
               .append(Text.literal("Use format: x,y,z").formatted(Formatting.GOLD))));
         locationItem.addLoreLine((Text.literal("")
               .append(Text.literal("- New Target -").formatted(Formatting.YELLOW))));
         locationItem.addLoreLine(Text.literal(""));
         locationItem.addLoreLine((Text.literal("")
               .append(Text.literal("Left").formatted(Formatting.GOLD))
               .append(Text.literal(" and ").formatted(Formatting.RED))
               .append(Text.literal("Right").formatted(Formatting.GOLD))
               .append(Text.literal(" click to cycle saved targets").formatted(Formatting.RED))));
         setSlot(0,locationItem);
      }
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
      BlockPos parsed = parseValid();
      if(index == 2){
         this.close();
      }else if(index == 0 && starcharts){
         if(type == ClickType.MOUSE_LEFT){
            selectedTarget = ((selectedTarget+2) % (sortedKeys.size()+1))-1;
         }else if(type == ClickType.MOUSE_RIGHT){
            selectedTarget = Math.floorMod(selectedTarget,sortedKeys.size()+1)-1;
         }
      }else if(index == 1 && starcharts){
         if(selectedTarget == -1 && parsed != null){ // Create
            String defaultName = parsed.toShortString();
            savedTargets.put(defaultName,parsed);
            sortedKeys.add(defaultName);
            Collections.sort(sortedKeys);
            selectedTarget = sortedKeys.indexOf(defaultName);
         }else if(selectedTarget != -1){
            String name = sortedKeys.get(selectedTarget);
            if(type == ClickType.MOUSE_LEFT){ // Update
               BlockPos target = savedTargets.remove(name);
               sortedKeys.remove(name);
               savedTargets.put(text,target);
               sortedKeys.add(text);
               Collections.sort(sortedKeys);
               selectedTarget = sortedKeys.indexOf(text);
            }else if(type == ClickType.MOUSE_RIGHT){ // Delete
               savedTargets.remove(name);
               sortedKeys.remove(name);
               selectedTarget = -1;
            }
         }
      }
      
      if(starcharts && (index == 1 || index == 0)){
         GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideFlags();
         if(selectedTarget == -1){
            locationItem.setName((Text.literal("")
                  .append(Text.literal("Use format: x,y,z").formatted(Formatting.GOLD))));
            locationItem.addLoreLine((Text.literal("")
                  .append(Text.literal("- New Target -").formatted(Formatting.YELLOW))));
         }else{
            String name = sortedKeys.get(selectedTarget);
            BlockPos target = savedTargets.get(name);
            locationItem.setName((Text.literal("")
                  .append(Text.literal(target.getX()+","+target.getY()+","+target.getZ()).formatted(Formatting.GOLD))));
            locationItem.addLoreLine((Text.literal("")
                  .append(Text.literal("- "+name+" -").formatted(Formatting.YELLOW))));
         }
         locationItem.addLoreLine(Text.literal(""));
         locationItem.addLoreLine((Text.literal("")
               .append(Text.literal("Left").formatted(Formatting.GOLD))
               .append(Text.literal(" and ").formatted(Formatting.RED))
               .append(Text.literal("Right").formatted(Formatting.GOLD))
               .append(Text.literal(" click to cycle saved targets").formatted(Formatting.RED))));
         setSlot(0,locationItem);
      }
      
      ArcanaNovum.addTickTimerCallback(new GenericTimer(1, ()-> GuiHelpers.sendSlotUpdate(player, this.syncId, 2, getSlot(2).getItemStack())));
      return true;
   }
   
   
   @Override
   public void onInput(String input) {
      text = input;
      
      BlockPos parsed = parseValid();
      
      GuiElementBuilder saveButton = GuiElementBuilder.from(Items.STRUCTURE_VOID.getDefaultStack()).setName(Text.literal("Invalid Location").formatted(Formatting.DARK_AQUA));
      GuiElementBuilder resultSlot;
      if(parsed == null){
         if(starcharts && selectedTarget == -1 && sortedKeys.contains(text)){
            this.selectedTarget = sortedKeys.indexOf(text);
            BlockPos target = savedTargets.get(text);
            
            GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideFlags();
            locationItem.setName((Text.literal("")
                  .append(Text.literal(target.getX()+","+target.getY()+","+target.getZ()).formatted(Formatting.GOLD))));
            locationItem.addLoreLine((Text.literal("")
                  .append(Text.literal("- "+text+" -").formatted(Formatting.YELLOW))));
            locationItem.addLoreLine(Text.literal(""));
            locationItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Left").formatted(Formatting.GOLD))
                  .append(Text.literal(" and ").formatted(Formatting.RED))
                  .append(Text.literal("Right").formatted(Formatting.GOLD))
                  .append(Text.literal(" click to cycle saved targets").formatted(Formatting.RED))));
            setSlot(0,locationItem);
            saveButton.setName(Text.literal("Valid Location").formatted(Formatting.DARK_AQUA));
            resultSlot = GuiElementBuilder.from(Items.FILLED_MAP.getDefaultStack()).hideFlags().setName(Text.literal("Valid Location: "+target.toShortString()).formatted(Formatting.DARK_AQUA));
         }else{
            saveButton.setName(Text.literal("Invalid Location").formatted(Formatting.DARK_AQUA));
            resultSlot = GuiElementBuilder.from(Items.BARRIER.getDefaultStack()).hideFlags().setName(Text.literal("Invalid Location").formatted(Formatting.RED));
         }
      }else{
         resultSlot = GuiElementBuilder.from(Items.FILLED_MAP.getDefaultStack()).hideFlags().setName(Text.literal("Valid Location: "+parsed.toShortString()).formatted(Formatting.DARK_AQUA));
         saveButton.setName(Text.literal("Valid Location").formatted(Formatting.DARK_AQUA));
      }
      
      if(starcharts){
         if(this.selectedTarget == -1 && parsed != null){
            saveButton.addLoreLine(Text.literal(""));
            saveButton.addLoreLine((Text.literal("")
                  .append(Text.literal("Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("save").formatted(Formatting.GREEN))
                  .append(Text.literal(" this target").formatted(Formatting.LIGHT_PURPLE))));
         }else if(this.selectedTarget != -1 && !sortedKeys.get(selectedTarget).equals(text)){
            saveButton.addLoreLine(Text.literal(""));
            saveButton.addLoreLine((Text.literal("")
                  .append(Text.literal("Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("rename").formatted(Formatting.YELLOW))
                  .append(Text.literal(" this target").formatted(Formatting.LIGHT_PURPLE))));
            saveButton.addLoreLine((Text.literal("")
                  .append(Text.literal("Right Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("delete").formatted(Formatting.RED))
                  .append(Text.literal(" this target").formatted(Formatting.LIGHT_PURPLE))));
         }
         
      }
      setSlot(1, saveButton);
      
      if(resultSlot != null){
         setSlot(2,resultSlot);
      }
   }
   
   @Override
   public void onClose(){
      BlockPos parsed = parseValid();
      if(parsed != null){
         blockEntity.setTargetCoords(parsed);
      }else if(selectedTarget != -1){
         blockEntity.setTargetCoords(savedTargets.get(sortedKeys.get(selectedTarget)));
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
