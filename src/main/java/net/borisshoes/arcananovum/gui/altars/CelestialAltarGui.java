package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.CelestialAltarBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.TimerTask;

import static net.borisshoes.arcananovum.blocks.altars.CelestialAltar.CelestialAltarBlock.HORIZONTAL_FACING;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class CelestialAltarGui extends SimpleGui implements WatchedGui {
   private final CelestialAltarBlockEntity blockEntity;
   private int mode = 0; // 0 - time, 1 - phase
   private int phase = 0;
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
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(index == 2){
         if(!control){
            phase = phase == 4 ? 0 : 4;
         }else{
            phase = (phase+1) % 8;
         }
      }else if(index == 4){
         if(type == ClickType.MOUSE_RIGHT || type == ClickType.MOUSE_RIGHT_SHIFT){
            mode = (mode+1) % 2;
         }else{
            if(blockEntity.getCooldown() <= 0 && blockEntity.getWorld() instanceof ServerWorld serverWorld){
               if(MiscUtils.removeItems(player,Items.NETHER_STAR,1)){
                  ParticleEffectUtils.celestialAltarAnim(serverWorld,blockEntity.getPos().toCenterPos(), 0, serverWorld.getBlockState(blockEntity.getPos()).get(HORIZONTAL_FACING));
                  blockEntity.resetCooldown();
                  Arcananovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(400, new TimerTask() {
                     @Override
                     public void run(){
                        changeTime();
                        PLAYER_DATA.get(player).addXP(1000);
                     }
                  }));
                  close();
               }else{
                  player.sendMessage(Text.literal("You do not have a Nether Star to power the Altar").formatted(Formatting.RED,Formatting.ITALIC),false);
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
      build();
   }
   
   public void build(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE).setName(Text.literal("Celestial Altar").formatted(Formatting.YELLOW)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideFlags();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Ready").formatted(Formatting.YELLOW))));
      }else{
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Recharging").formatted(Formatting.BLUE))));
         cooldownItem.addLoreLine((Text.literal("")
               .append(Text.literal((blockEntity.getCooldown()/20)+" Seconds").formatted(Formatting.GOLD))));
      }
      setSlot(0,cooldownItem);
      
      ItemStack lightItem = new ItemStack(Items.LIGHT);
      NbtCompound lightTag = new NbtCompound();
      lightTag.putString("level",""+lightLvl[phase]);
      lightItem.getOrCreateNbt().put("BlockStateTag",lightTag);
      GuiElementBuilder phaseItem = GuiElementBuilder.from(lightItem).hideFlags();
      
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
         phaseItem.addLoreLine((Text.literal("")
               .append(Text.literal("Selected: "+phaseStr).formatted(Formatting.YELLOW))));
         phaseItem.addLoreLine((Text.literal("")));
         phaseItem.addLoreLine((Text.literal("")
               .append(Text.literal("Click to change the time").formatted(Formatting.GRAY))));
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
         phaseItem.addLoreLine((Text.literal("")
               .append(Text.literal("Selected: "+phaseStr).formatted(Formatting.YELLOW))));
         phaseItem.addLoreLine((Text.literal("")));
         phaseItem.addLoreLine((Text.literal("")
               .append(Text.literal("Click to change the phase").formatted(Formatting.GRAY))));
      }
      setSlot(2,phaseItem);
      
      
      GuiElementBuilder activateItem = new GuiElementBuilder(mode == 0 ? Items.GLOWSTONE : Items.SEA_LANTERN);
      activateItem.setName((Text.literal("")
            .append(Text.literal("Activate Altar").formatted(mode == 0 ? Formatting.GOLD : Formatting.BLUE))));
      activateItem.addLoreLine((Text.literal("")
            .append(Text.literal("Click to move the sky").formatted(Formatting.AQUA))));
      activateItem.addLoreLine((Text.literal("")));
      activateItem.addLoreLine((Text.literal("")
            .append(Text.literal("Current Mode: "+(mode == 0 ? "Solar" : "Lunar")).formatted(Formatting.YELLOW))));
      activateItem.addLoreLine((Text.literal("")
            .append(Text.literal("Right Click to switch modes").formatted(Formatting.DARK_GRAY))));
      activateItem.addLoreLine((Text.literal("")));
      activateItem.addLoreLine((Text.literal("")
            .append(Text.literal("The Altar Requires 1 Nether Star").formatted(Formatting.AQUA))));
      setSlot(4,activateItem);
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
