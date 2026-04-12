package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.CelestialAltarBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

public class CelestialAltarGui extends SimpleGui {
   private final CelestialAltarBlockEntity blockEntity;
   private final boolean control;
   private final int[] lightLvl = {15, 11, 7, 3, 0, 3, 7, 11};
   
   public CelestialAltarGui(ServerPlayer player, CelestialAltarBlockEntity blockEntity){
      super(MenuType.HOPPER, player, false);
      this.blockEntity = blockEntity;
      setTitle(Component.literal("Celestial Altar"));
      control = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(), ArcanaAugments.STELLAR_CONTROL) >= 1;
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
      int phase = blockEntity.getPhase();
      int mode = blockEntity.getMode();
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         GuiElementBuilder menuItem = mode == 0 ? GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, ArcanaColors.CELESTIAL_DAY_COLOR)) : GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, ArcanaColors.CELESTIAL_NIGHT_COLOR));
         setSlot(i, menuItem.setName(Component.literal("Celestial Altar").withStyle(mode == 0 ? ChatFormatting.YELLOW : ChatFormatting.AQUA)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideDefaultTooltip();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Component.literal("")
               .append(Component.literal("Altar Ready").withStyle(ChatFormatting.YELLOW))));
      }else{
         cooldownItem.setName((Component.literal("")
               .append(Component.literal("Altar Recharging").withStyle(ChatFormatting.BLUE))));
         cooldownItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal((blockEntity.getCooldown() / 20) + " Seconds").withStyle(ChatFormatting.GOLD)))));
      }
      setSlot(0, cooldownItem);
      
      ItemStack lightItem = new ItemStack(Items.LIGHT);
      lightItem.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(BlockStateProperties.LEVEL, lightLvl[phase]));
      GuiElementBuilder phaseItem = GuiElementBuilder.from(lightItem).hideDefaultTooltip();
      
      if(mode == 0){
         String phaseStr = switch(phase){
            case 0 -> "Zenith";
            case 1 -> "Undern";
            case 2 -> "Twilight";
            case 3 -> "Dusk";
            case 4 -> "Nadir";
            case 5 -> "Witches' Hour";
            case 6 -> "Aurora";
            case 7 -> "Morn";
            default -> "-";
         };
         phaseItem.setName((Component.literal("")
               .append(Component.literal("Time of Day").withStyle(ChatFormatting.GOLD))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Selected: " + phaseStr).withStyle(ChatFormatting.YELLOW)))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Component.literal(""))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click to change the time").withStyle(ChatFormatting.GRAY)))));
      }else{
         String phaseStr = switch(phase){
            case 0 -> "Full Moon";
            case 1 -> "Waning Gibbous";
            case 2 -> "Last Quarter";
            case 3 -> "Waning Crescent";
            case 4 -> "New Moon";
            case 5 -> "Waxing Crescent";
            case 6 -> "First Quarter";
            case 7 -> "Waxing Gibbous";
            default -> "-";
         };
         phaseItem.setName((Component.literal("")
               .append(Component.literal("Moon Phase").withStyle(ChatFormatting.GOLD))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Selected: " + phaseStr).withStyle(ChatFormatting.YELLOW)))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Component.literal(""))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click to change the phase").withStyle(ChatFormatting.GRAY)))));
      }
      phaseItem.setCallback((clickType) -> {
         int curPhase = blockEntity.getPhase();
         if(!control){
            blockEntity.setPhase(curPhase == 4 ? 0 : 4);
         }else{
            if(clickType.isRight){
               blockEntity.setPhase((curPhase - 1 + 8) % 8);
            }else{
               blockEntity.setPhase((curPhase + 1) % 8);
            }
         }
      });
      setSlot(2, phaseItem);
      
      
      GuiElementBuilder activateItem = new GuiElementBuilder(mode == 0 ? Items.GLOWSTONE : Items.SEA_LANTERN);
      Tuple<Item, Integer> cost = CelestialAltarBlockEntity.getCost();
      activateItem.setName((Component.literal("")
            .append(Component.literal("Activate Altar").withStyle(mode == 0 ? ChatFormatting.GOLD : ChatFormatting.BLUE))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click to move the sky").withStyle(ChatFormatting.AQUA)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal(""))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Current Mode: " + (mode == 0 ? "Solar" : "Lunar")).withStyle(ChatFormatting.YELLOW)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Right Click to switch modes").withStyle(ChatFormatting.DARK_GRAY)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal(""))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("The Altar Requires " + cost.getB() + " ").withStyle(ChatFormatting.AQUA))
            .append(Component.translatable(cost.getA().getDescriptionId()).withStyle(ChatFormatting.AQUA)))));
      activateItem.setCallback((clickType) -> {
         int curMode = blockEntity.getMode();
         if(clickType == ClickType.MOUSE_RIGHT || clickType == ClickType.MOUSE_RIGHT_SHIFT){
            blockEntity.setMode((curMode + 1) % 2);
         }else{
            if(blockEntity.getCooldown() <= 0 && blockEntity.getLevel() instanceof ServerLevel serverWorld){
               Tuple<Item, Integer> curCost = CelestialAltarBlockEntity.getCost();
               if(MinecraftUtils.removeItems(player, curCost.getA(), curCost.getB())){
                  blockEntity.startStarChange(player);
                  close();
               }else{
                  player.sendSystemMessage(Component.literal("You do not have " + curCost.getB() + " ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                        .append(Component.translatable(curCost.getA().getDescriptionId()).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
                        .append(Component.literal(" to power the Altar").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)), false);
                  SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
                  close();
               }
            }else{
               player.sendSystemMessage(Component.literal("The Altar is on Cooldown").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), false);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
               close();
            }
         }
      });
      setSlot(4, activateItem);
   }
   
   
   @Override
   public void close(){
      super.close();
   }
}
