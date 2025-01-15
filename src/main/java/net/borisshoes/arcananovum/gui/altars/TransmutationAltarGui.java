package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.Optional;

public class TransmutationAltarGui extends SimpleGui {
   private final TransmutationAltarBlockEntity blockEntity;
   
   public TransmutationAltarGui(ScreenHandlerType<?> type, ServerPlayerEntity player, TransmutationAltarBlockEntity blockEntity){
      super(type, player, false);
      this.blockEntity = blockEntity;
      setTitle(Text.literal("Transmutation Altar"));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(index == 2){
         TransmutationAltarRecipeGui recipeGui = new TransmutationAltarRecipeGui(player, this, Optional.of(blockEntity));
         recipeGui.buildRecipeListGui();
         recipeGui.open();
      }else if(index == 4  && blockEntity.getWorld() instanceof ServerWorld serverWorld){
         if(blockEntity.getCooldown() <= 0){
            if(blockEntity.checkTransmute() != null){
               blockEntity.startTransmute(player);
            }else{
               player.sendMessage(Text.literal("No Transmutation Items Found").formatted(Formatting.RED,Formatting.ITALIC));
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
            }
            close();
         }else{
            player.sendMessage(Text.literal("The Altar is on Cooldown").formatted(Formatting.RED,Formatting.ITALIC),false);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
            close();
         }
      }
      return true;
   }
   
   @Override
   public void onTick(){
      World world = blockEntity.getWorld();
      if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity || !blockEntity.isAssembled() || blockEntity.isActive()){
         this.close();
      }
      
      buildMenuGui();
   }
   
   public void buildMenuGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,ArcanaColors.EQUAYUS_COLOR)).setName(Text.literal("Transmutation Altar").formatted(Formatting.BLUE)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideDefaultTooltip();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Ready").formatted(Formatting.AQUA))));
      }else{
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Recharging").formatted(Formatting.BLUE))));
         cooldownItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal((blockEntity.getCooldown()/20)+" Seconds").formatted(Formatting.DARK_PURPLE)))));
      }
      setSlot(0,cooldownItem);
      
      GuiElementBuilder recipeItem = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.TRANSMUTATION_BOOK)).hideDefaultTooltip();
      recipeItem.setName((Text.literal("")
            .append(Text.literal("Transmutation Recipes").formatted(Formatting.DARK_AQUA))));
      recipeItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to view all Transmutation Recipes").formatted(Formatting.BLUE)))));
      
      setSlot(2,recipeItem);
      

      GuiElementBuilder activateItem = new GuiElementBuilder(Items.DIAMOND);
      activateItem.setName((Text.literal("")
            .append(Text.literal("Activate Altar").formatted(Formatting.LIGHT_PURPLE))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.BLUE))
            .append(Text.literal(" to begin a Transmutation").formatted(Formatting.DARK_PURPLE)))));
      setSlot(4,activateItem);
   }
   
   @Override
   public void close(){
      super.close();
   }
}
