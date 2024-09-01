package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StormcallerAltarBlockEntity;
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

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class StormcallerAltarGui  extends SimpleGui {
   private final StormcallerAltarBlockEntity blockEntity;
   private final int[] durations = {-1,2,4,6,8,10,15,20,25,30,35,40,45,50,55,60};
   private final boolean tempest;
   
   public StormcallerAltarGui(ServerPlayerEntity player, StormcallerAltarBlockEntity blockEntity){
      super(ScreenHandlerType.HOPPER, player, false);
      this.blockEntity = blockEntity;
      setTitle(Text.literal("Altar of the Stormcaller"));
      tempest = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.PERSISTENT_TEMPEST.id) >= 1;
   }
   
   private void changeWeather(){
      if(!(blockEntity.getWorld() instanceof ServerWorld serverWorld)) return;
      int duration = blockEntity.getDuration();
      int mode = blockEntity.getMode();
      SoundUtils.playSound(serverWorld, blockEntity.getPos(), SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 1, 0.5f);
      int dur = durations[duration];
      
      dur = switch(mode){
         case 0 -> dur == -1 ? ServerWorld.CLEAR_WEATHER_DURATION_PROVIDER.get(player.getWorld().getRandom()) : dur * 3600;
         case 1 -> dur == -1 ? ServerWorld.RAIN_WEATHER_DURATION_PROVIDER.get(player.getWorld().getRandom()) : dur * 1200;
         case 2 -> dur == -1 ? ServerWorld.THUNDER_WEATHER_DURATION_PROVIDER.get(player.getWorld().getRandom()) : dur * 600;
         default -> dur;
      };
      if(mode == 0 && player.getServerWorld().isRaining()){
         ArcanaAchievements.grant(player,ArcanaAchievements.COME_AGAIN_RAIN.id);
      }
      
      player.getServerWorld().setWeather(mode == 0 ? dur : 0, mode >= 1 ? dur : 0, mode >= 1, mode == 2);
      blockEntity.setActive(false);
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      int duration = blockEntity.getDuration();
      int mode = blockEntity.getMode();
      if(index == 2 && tempest){
         blockEntity.setDuration((duration+1) % 16);
      }else if(index == 4){
         if(type == ClickType.MOUSE_RIGHT || type == ClickType.MOUSE_RIGHT_SHIFT){
            blockEntity.setMode((mode+1) % 3);
         }else{
            if(blockEntity.getCooldown() <= 0){
               if(MiscUtils.removeItems(player,Items.DIAMOND_BLOCK,1)){
                  ParticleEffectUtils.stormcallerAltarAnim(player.getServerWorld(),blockEntity.getPos().toCenterPos(), 0);
                  blockEntity.resetCooldown();
                  blockEntity.setActive(true);
                  ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(100, () -> {
                     changeWeather();
                     PLAYER_DATA.get(player).addXP(1000);
                  }));
                  close();
               }else{
                  player.sendMessage(Text.literal("You do not have a ").formatted(Formatting.RED,Formatting.ITALIC)
                        .append(Text.translatable(Items.DIAMOND_BLOCK.getTranslationKey()).formatted(Formatting.AQUA,Formatting.ITALIC))
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
      int duration = blockEntity.getDuration();
      int mode = blockEntity.getMode();
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         GuiElementBuilder menuItem = switch(mode){
            case 1 -> GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,0x8895b3));
            case 2 -> GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,0x525261));
            default -> GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,0x1cffff));
         };
         setSlot(i,menuItem.setName(Text.literal("Altar of the Stormcaller").formatted(Formatting.DARK_GRAY)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideDefaultTooltip();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Ready").formatted(Formatting.AQUA))));
      }else{
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Recharging").formatted(Formatting.DARK_GRAY))));
         cooldownItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal((blockEntity.getCooldown()/20)+" Seconds").formatted(Formatting.GRAY)))));
      }
      setSlot(0,cooldownItem);
      
      ItemStack lightItem = new ItemStack(Items.LIGHT);
      lightItem.set(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT.with(Properties.LEVEL_15,duration));
      GuiElementBuilder durationItem = GuiElementBuilder.from(lightItem).hideDefaultTooltip();
      durationItem.setName((Text.literal("")
            .append(Text.literal("Weather Duration").formatted(Formatting.YELLOW))));
      
      if(tempest){
         int dur = switch(mode){
            case 0 ->  durations[duration] * 3;
            case 1 ->  durations[duration];
            case 2 ->  durations[duration] / 2;
            default -> 0;
         };
         String durStr = dur <= 0 ? "Random" : dur + " Minutes";
         durationItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Current Duration: "+durStr).formatted(Formatting.GOLD)))));
         durationItem.addLoreLine(TextUtils.removeItalics((Text.literal(""))));
         durationItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Click to change duration").formatted(Formatting.GRAY)))));
      }else{
         durationItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Unlock this ability with Augmentation").formatted(Formatting.RED)))));
      }
      
      
      setSlot(2,durationItem);
      
      String modeString = switch(mode){
         case 0 -> "Clear the Sky";
         case 1 -> "Condense the Moisture";
         case 2 -> "Charge the Clouds";
         default -> "-";
      };
      GuiElementBuilder activateItem = new GuiElementBuilder(Items.LIGHTNING_ROD);
      activateItem.setName((Text.literal("")
            .append(Text.literal("Activate Altar").formatted(Formatting.BLUE))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click to harness the clouds").formatted(Formatting.GOLD)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal(""))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Current Mode: "+modeString).formatted(Formatting.BLUE)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Right Click to switch modes").formatted(Formatting.GOLD)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal(""))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("The Altar Requires 1 Diamond Block").formatted(Formatting.AQUA)))));
      setSlot(4,activateItem);
   }
   
   
   @Override
   public void close(){
      super.close();
   }
}
