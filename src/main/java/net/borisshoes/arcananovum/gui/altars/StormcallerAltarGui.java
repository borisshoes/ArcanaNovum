package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StormcallerAltarBlockEntity;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static net.borisshoes.arcananovum.blocks.altars.StormcallerAltarBlockEntity.DURATIONS;

public class StormcallerAltarGui  extends SimpleGui {
   private final StormcallerAltarBlockEntity blockEntity;
   private final boolean tempest;
   
   public StormcallerAltarGui(ServerPlayer player, StormcallerAltarBlockEntity blockEntity){
      super(MenuType.HOPPER, player, false);
      this.blockEntity = blockEntity;
      setTitle(Component.literal("Altar of the Stormcaller"));
      tempest = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.PERSISTENT_TEMPEST) >= 1;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      int duration = blockEntity.getDuration();
      int mode = blockEntity.getMode();
      if(index == 2 && tempest){
         blockEntity.setDuration((duration+1) % 16);
      }else if(index == 4){
         if(type == ClickType.MOUSE_RIGHT || type == ClickType.MOUSE_RIGHT_SHIFT){
            blockEntity.setMode((mode+1) % 3);
         }else{
            if(blockEntity.getCooldown() <= 0){
               Tuple<Item,Integer> cost = StormcallerAltarBlockEntity.getCost();
               if(MinecraftUtils.removeItems(player, cost.getA(),cost.getB())){
                  blockEntity.startWeatherChange(player);
                  close();
               }else{
                  player.displayClientMessage(Component.literal("You do not have "+cost.getB()+" ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                        .append(Component.translatable(cost.getA().getDescriptionId()).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                        .append(Component.literal(" to power the Altar").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)),false);
                  SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH,1,.5f);
                  close();
               }
            }else{
               player.displayClientMessage(Component.literal("The Altar is on Cooldown").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),false);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH,1,.5f);
               close();
            }
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
      
      build();
   }
   
   public void build(){
      int duration = blockEntity.getDuration();
      int mode = blockEntity.getMode();
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         GuiElementBuilder menuItem = switch(mode){
            case 1 -> GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,0x8895b3));
            case 2 -> GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,0x525261));
            default -> GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,0x1cffff));
         };
         setSlot(i,menuItem.setName(Component.literal("Altar of the Stormcaller").withStyle(ChatFormatting.DARK_GRAY)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideDefaultTooltip();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Component.literal("")
               .append(Component.literal("Altar Ready").withStyle(ChatFormatting.AQUA))));
      }else{
         cooldownItem.setName((Component.literal("")
               .append(Component.literal("Altar Recharging").withStyle(ChatFormatting.DARK_GRAY))));
         cooldownItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal((blockEntity.getCooldown()/20)+" Seconds").withStyle(ChatFormatting.GRAY)))));
      }
      setSlot(0,cooldownItem);
      
      ItemStack lightItem = new ItemStack(Items.LIGHT);
      lightItem.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(BlockStateProperties.LEVEL,duration));
      GuiElementBuilder durationItem = GuiElementBuilder.from(lightItem).hideDefaultTooltip();
      durationItem.setName((Component.literal("")
            .append(Component.literal("Weather Duration").withStyle(ChatFormatting.YELLOW))));
      
      if(tempest){
         int dur = switch(mode){
            case 0 ->  DURATIONS[duration] * 3;
            case 1 ->  DURATIONS[duration];
            case 2 ->  DURATIONS[duration] / 2;
            default -> 0;
         };
         String durStr = dur <= 0 ? "Random" : dur + " Minutes";
         durationItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Current Duration: "+durStr).withStyle(ChatFormatting.GOLD)))));
         durationItem.addLoreLine(TextUtils.removeItalics((Component.literal(""))));
         durationItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click to change duration").withStyle(ChatFormatting.GRAY)))));
      }else{
         durationItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Unlock this ability with Augmentation").withStyle(ChatFormatting.RED)))));
      }
      
      
      setSlot(2,durationItem);
      
      String modeString = switch(mode){
         case 0 -> "Clear the Sky";
         case 1 -> "Condense the Moisture";
         case 2 -> "Charge the Clouds";
         default -> "-";
      };
      Tuple<Item,Integer> cost = StormcallerAltarBlockEntity.getCost();
      GuiElementBuilder activateItem = new GuiElementBuilder(Items.LIGHTNING_ROD);
      activateItem.setName((Component.literal("")
            .append(Component.literal("Activate Altar").withStyle(ChatFormatting.BLUE))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click to harness the clouds").withStyle(ChatFormatting.GOLD)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal(""))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Current Mode: "+modeString).withStyle(ChatFormatting.BLUE)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Right Click to switch modes").withStyle(ChatFormatting.GOLD)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal(""))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("The Altar Requires "+cost.getB()+" ").withStyle(ChatFormatting.AQUA))
            .append(Component.translatable(cost.getA().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
      setSlot(4,activateItem);
   }
   
   
   @Override
   public void close(){
      super.close();
   }
}
