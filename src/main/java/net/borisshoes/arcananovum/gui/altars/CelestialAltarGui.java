package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.CelestialAltarBlockEntity;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import static net.borisshoes.arcananovum.blocks.altars.CelestialAltar.CelestialAltarBlock.HORIZONTAL_FACING;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class CelestialAltarGui extends SimpleGui {
   private final CelestialAltarBlockEntity blockEntity;
   private final boolean control;
   private final int[] lightLvl = {15,11,7,3,0,3,7,11};
   private final int[] times = {6000,9000,12000,14000,18000,20000,23000,2000};
   
   public CelestialAltarGui(ServerPlayerEntity player, CelestialAltarBlockEntity blockEntity){
      super(ScreenHandlerType.HOPPER, player, false);
      this.blockEntity = blockEntity;
      setTitle(Text.literal("Celestial Altar"));
      control = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.STELLAR_CONTROL.id) >= 1;
   }
   
   private void changeTime(){
      if(!(blockEntity.getWorld() instanceof ServerWorld serverWorld)) return;
      int phase = blockEntity.getPhase();
      int mode = blockEntity.getMode();
      long timeOfDay = serverWorld.getTimeOfDay();
      if(mode == 0){
         int curTime = (int) (timeOfDay % 24000L);
         int targetTime = times[phase];
         int timeDiff = (targetTime - curTime + 24000) % 24000;
         serverWorld.setTimeOfDay(timeOfDay + timeDiff);
         
         ArcanaAchievements.grant(player,ArcanaAchievements.POWER_OF_THE_SUN.id);
      }else{
         int day = (int) (timeOfDay/24000L % Integer.MAX_VALUE);
         int curPhase = day % 8;
         int phaseDiff = (phase - curPhase + 8) % 8;
         serverWorld.setTimeOfDay(timeOfDay + phaseDiff * 24000L);
         
         if(phase == 0){
            ArcanaAchievements.grant(player,ArcanaAchievements.LYCANTHROPE.id);
         }
      }
      SoundUtils.playSound(serverWorld, blockEntity.getPos(), SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 1, 0.5f);
      blockEntity.setActive(false);
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      int phase = blockEntity.getPhase();
      int mode = blockEntity.getMode();
      if(index == 2){
         if(!control){
            blockEntity.setPhase(phase == 4 ? 0 : 4);
         }else{
            blockEntity.setPhase((phase+1) % 8);
         }
      }else if(index == 4){
         if(type == ClickType.MOUSE_RIGHT || type == ClickType.MOUSE_RIGHT_SHIFT){
            blockEntity.setMode((mode+1) % 2);
         }else{
            if(blockEntity.getCooldown() <= 0 && blockEntity.getWorld() instanceof ServerWorld serverWorld){
               if(MiscUtils.removeItems(player,Items.NETHER_STAR,1)){
                  ParticleEffectUtils.celestialAltarAnim(serverWorld,blockEntity.getPos().toCenterPos(), 0, serverWorld.getBlockState(blockEntity.getPos()).get(HORIZONTAL_FACING));
                  blockEntity.resetCooldown();
                  blockEntity.setActive(true);
                  ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(400, () -> {
                     changeTime();
                     PLAYER_DATA.get(player).addXP(1000);
                  }));
                  close();
               }else{
                  player.sendMessage(Text.literal("You do not have a ").formatted(Formatting.RED,Formatting.ITALIC)
                        .append(Text.translatable(Items.NETHER_STAR.getTranslationKey()).formatted(Formatting.YELLOW,Formatting.ITALIC))
                        .append(Text.literal(" to power the Altar").formatted(Formatting.RED,Formatting.ITALIC)),false);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
                  close();
               }
            }else{
               player.sendMessage(Text.literal("The Altar is on Cooldown").formatted(Formatting.RED,Formatting.ITALIC),false);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
               close();
            }
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
      
      build();
   }
   
   public void build(){
      int phase = blockEntity.getPhase();
      int mode = blockEntity.getMode();
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         GuiElementBuilder menuItem = mode == 0 ? GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,0xf7d10f)) : GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,0x365aad));
         setSlot(i,menuItem.setName(Text.literal("Celestial Altar").formatted(mode == 0 ? Formatting.YELLOW : Formatting.AQUA)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideDefaultTooltip();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Ready").formatted(Formatting.YELLOW))));
      }else{
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Recharging").formatted(Formatting.BLUE))));
         cooldownItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal((blockEntity.getCooldown()/20)+" Seconds").formatted(Formatting.GOLD)))));
      }
      setSlot(0,cooldownItem);
      
      ItemStack lightItem = new ItemStack(Items.LIGHT);
      lightItem.set(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT.with(Properties.LEVEL_15,lightLvl[phase]));
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
         phaseItem.setName((Text.literal("")
               .append(Text.literal("Time of Day").formatted(Formatting.GOLD))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Selected: "+phaseStr).formatted(Formatting.YELLOW)))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Text.literal(""))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Click to change the time").formatted(Formatting.GRAY)))));
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
         phaseItem.setName((Text.literal("")
               .append(Text.literal("Moon Phase").formatted(Formatting.GOLD))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Selected: "+phaseStr).formatted(Formatting.YELLOW)))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Text.literal(""))));
         phaseItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Click to change the phase").formatted(Formatting.GRAY)))));
      }
      setSlot(2,phaseItem);
      
      
      GuiElementBuilder activateItem = new GuiElementBuilder(mode == 0 ? Items.GLOWSTONE : Items.SEA_LANTERN);
      activateItem.setName((Text.literal("")
            .append(Text.literal("Activate Altar").formatted(mode == 0 ? Formatting.GOLD : Formatting.BLUE))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click to move the sky").formatted(Formatting.AQUA)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal(""))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Current Mode: "+(mode == 0 ? "Solar" : "Lunar")).formatted(Formatting.YELLOW)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Right Click to switch modes").formatted(Formatting.DARK_GRAY)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal(""))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("The Altar Requires 1 Nether Star").formatted(Formatting.AQUA)))));
      setSlot(4,activateItem);
   }
   
   
   @Override
   public void close(){
      super.close();
   }
}
