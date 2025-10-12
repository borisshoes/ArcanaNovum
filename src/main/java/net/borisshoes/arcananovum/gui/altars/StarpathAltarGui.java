package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity.COST;

public class StarpathAltarGui extends SimpleGui {
   private final StarpathAltarBlockEntity blockEntity;
   private final boolean starcharts;
   private final boolean stargate;
   
   public StarpathAltarGui(ServerPlayerEntity player, StarpathAltarBlockEntity blockEntity){
      super(ScreenHandlerType.HOPPER, player, false);
      this.blockEntity = blockEntity;
      starcharts = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.STAR_CHARTS.id) > 0;
      stargate = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.STARGATE.id) > 0;
      
      setTitle(Text.literal("Starpath Altar"));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(index == 2){
         
         if(starcharts){
            StarpathAltarChartsGui gui = new StarpathAltarChartsGui(player,this,blockEntity);
            gui.buildGui();
            gui.open();
         }else{
            StarpathTargetGui gui = new StarpathTargetGui(player,blockEntity,true,this,(obj) -> blockEntity.setTarget((BlockPos) obj));
            gui.open();
         }
      }else if(index == 4){
         if(blockEntity.getCooldown() <= 0){
            if(MinecraftUtils.removeItems(player,COST,blockEntity.calculateCost())){
               blockEntity.startTeleport(player);
               close();
            }else{
               player.sendMessage(Text.literal("You do not have enough ").formatted(Formatting.RED,Formatting.ITALIC)
                     .append(Text.translatable(COST.getTranslationKey()).formatted(Formatting.DARK_AQUA,Formatting.ITALIC))
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
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         
         setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, ArcanaColors.STARPATH_COLOR)).setName(Text.literal("Starpath Altar").formatted(Formatting.DARK_PURPLE)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideDefaultTooltip();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Ready").formatted(Formatting.AQUA))));
      }else{
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Recharging").formatted(Formatting.DARK_AQUA))));
         cooldownItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal((blockEntity.getCooldown()/20)+" Seconds").formatted(Formatting.GRAY)))));
      }
      setSlot(0,cooldownItem);
      
      BlockPos target = blockEntity.getTarget();
      GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideDefaultTooltip();
      locationItem.setName((Text.literal("")
            .append(Text.literal("Target Location").formatted(Formatting.GOLD))));
      locationItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("X: "+target.getX()).formatted(Formatting.YELLOW)))));
      locationItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Y: "+target.getY()).formatted(Formatting.YELLOW)))));
      locationItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Z: "+target.getZ()).formatted(Formatting.YELLOW)))));
      if(stargate){
         locationItem.addLoreLine(Text.literal("Dimension: ").formatted(Formatting.YELLOW).append(ArcanaUtils.getFormattedDimName(blockEntity.getTargetDimension())));
      }
      locationItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("").formatted(Formatting.YELLOW)))));
      locationItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click to Change Target").formatted(Formatting.YELLOW)))));
      setSlot(2,locationItem);
      
      int cost = blockEntity.calculateCost();
      int stacks = cost / 64;
      int leftover = cost % 64;
      GuiElementBuilder activateItem = new GuiElementBuilder(COST);
      activateItem.setName((Text.literal("")
            .append(Text.literal("Activate Altar").formatted(Formatting.LIGHT_PURPLE))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click to travel the Star's Path").formatted(Formatting.DARK_PURPLE)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("").formatted(Formatting.DARK_PURPLE)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("This Journey Costs: ").formatted(Formatting.AQUA)))));
      
      MutableText text = Text.literal("")
            .append(Text.literal(cost+"").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" - ").formatted(Formatting.DARK_PURPLE))
            .append(Text.translatable(COST.getTranslationKey()).formatted(Formatting.DARK_AQUA));
      if(cost > COST.getMaxCount()){
         text.append(Text.literal(" - ").formatted(Formatting.DARK_PURPLE));
         if(cost > 0){
            text.append("("+stacks+" Stacks + "+leftover+")").formatted(Formatting.YELLOW);
         }else{
            text.append("("+stacks+" Stacks)").formatted(Formatting.YELLOW);
         }
      }
      activateItem.addLoreLine(TextUtils.removeItalics(text));
      setSlot(4,activateItem);
   }
   
   
   @Override
   public void close(){
      super.close();
   }
}
