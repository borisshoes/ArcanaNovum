package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class TransmutationAltarGui extends SimpleGui {
   private final TransmutationAltarBlockEntity blockEntity;
   
   public TransmutationAltarGui(MenuType<?> type, ServerPlayer player, TransmutationAltarBlockEntity blockEntity){
      super(type, player, false);
      this.blockEntity = blockEntity;
      setTitle(Component.literal("Transmutation Altar"));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      if(index == 2){
         TransmutationAltarRecipeGui recipeGui = new TransmutationAltarRecipeGui(player, this, Optional.of(blockEntity));
         recipeGui.buildPage();
         recipeGui.open();
      }else if(index == 4  && blockEntity.getLevel() instanceof ServerLevel serverWorld){
         if(blockEntity.getCooldown() <= 0){
            if(blockEntity.checkTransmute() != null){
               blockEntity.startTransmute(player);
            }else{
               player.sendSystemMessage(Component.literal("No Transmutation Items Found").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH,1,.5f);
            }
            close();
         }else{
            player.displayClientMessage(Component.literal("The Altar is on Cooldown").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),false);
            SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH,1,.5f);
            close();
         }
      }
      return true;
   }
   
   @Override
   public void onTick(){
      Level world = blockEntity.getLevel();
      if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity || !blockEntity.isAssembled() || blockEntity.isActive()){
         this.close();
      }
      
      buildMenuGui();
   }
   
   public void buildMenuGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,ArcanaColors.EQUAYUS_COLOR)).setName(Component.literal("Transmutation Altar").withStyle(ChatFormatting.BLUE)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideDefaultTooltip();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Component.literal("")
               .append(Component.literal("Altar Ready").withStyle(ChatFormatting.AQUA))));
      }else{
         cooldownItem.setName((Component.literal("")
               .append(Component.literal("Altar Recharging").withStyle(ChatFormatting.BLUE))));
         cooldownItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal((blockEntity.getCooldown()/20)+" Seconds").withStyle(ChatFormatting.DARK_PURPLE)))));
      }
      setSlot(0,cooldownItem);
      
      GuiElementBuilder recipeItem = GuiElementBuilder.from(GraphicalItem.with(ArcanaRegistry.TRANSMUTATION_BOOK)).hideDefaultTooltip();
      recipeItem.setName((Component.literal("")
            .append(Component.literal("Transmutation Recipes").withStyle(ChatFormatting.DARK_AQUA))));
      recipeItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to view all Transmutation Recipes").withStyle(ChatFormatting.BLUE)))));
      
      setSlot(2,recipeItem);
      
      
      if(blockEntity.getCooldown() <= 0){
         GuiElementBuilder activateItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.CONFIRM_COLOR, ArcanaColors.EQUAYUS_COLOR));
         activateItem.setName((Component.literal("")
               .append(Component.literal("Activate Altar").withStyle(ChatFormatting.LIGHT_PURPLE))));
         activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.BLUE))
               .append(Component.literal(" to begin a Transmutation").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(4,activateItem);
      }else{
         GuiElementBuilder activateItem = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.CANCEL_COLOR, ArcanaColors.EQUAYUS_COLOR));
         activateItem.setName((Component.literal("")
               .append(Component.literal("Altar Recharging").withStyle(ChatFormatting.BLUE))));
         activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal((blockEntity.getCooldown()/20)+" Seconds").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(4,activateItem);
      }
   }
   
   @Override
   public void close(){
      super.close();
   }
}
