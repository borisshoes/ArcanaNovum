package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import static net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity.COST;

public class StarpathAltarGui extends SimpleGui {
   private final StarpathAltarBlockEntity blockEntity;
   private final boolean starcharts;
   private final boolean stargate;
   
   public StarpathAltarGui(ServerPlayer player, StarpathAltarBlockEntity blockEntity){
      super(MenuType.HOPPER, player, false);
      this.blockEntity = blockEntity;
      starcharts = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.STAR_CHARTS.id) > 0;
      stargate = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.STARGATE.id) > 0;
      
      setTitle(Component.literal("Starpath Altar"));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      if(index == 2){
         
         if(starcharts){
            StarpathAltarChartsGui gui = new StarpathAltarChartsGui(player,this,blockEntity);
            gui.buildPage();
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
               player.displayClientMessage(Component.literal("You do not have enough ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                     .append(Component.translatable(COST.getDescriptionId()).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC))
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
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         
         setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, ArcanaColors.STARPATH_COLOR)).setName(Component.literal("Starpath Altar").withStyle(ChatFormatting.DARK_PURPLE)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideDefaultTooltip();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Component.literal("")
               .append(Component.literal("Altar Ready").withStyle(ChatFormatting.AQUA))));
      }else{
         cooldownItem.setName((Component.literal("")
               .append(Component.literal("Altar Recharging").withStyle(ChatFormatting.DARK_AQUA))));
         cooldownItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal((blockEntity.getCooldown()/20)+" Seconds").withStyle(ChatFormatting.GRAY)))));
      }
      setSlot(0,cooldownItem);
      
      BlockPos target = blockEntity.getTarget();
      GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideDefaultTooltip();
      locationItem.setName((Component.literal("")
            .append(Component.literal("Target Location").withStyle(ChatFormatting.GOLD))));
      locationItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("X: "+target.getX()).withStyle(ChatFormatting.YELLOW)))));
      locationItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Y: "+target.getY()).withStyle(ChatFormatting.YELLOW)))));
      locationItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Z: "+target.getZ()).withStyle(ChatFormatting.YELLOW)))));
      if(stargate){
         locationItem.addLoreLine(Component.literal("Dimension: ").withStyle(ChatFormatting.YELLOW).append(MinecraftUtils.getFormattedDimName(blockEntity.getTargetDimension())));
      }
      locationItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("").withStyle(ChatFormatting.YELLOW)))));
      locationItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click to Change Target").withStyle(ChatFormatting.YELLOW)))));
      setSlot(2,locationItem);
      
      int cost = blockEntity.calculateCost();
      int stacks = cost / 64;
      int leftover = cost % 64;
      GuiElementBuilder activateItem = new GuiElementBuilder(COST);
      activateItem.setName((Component.literal("")
            .append(Component.literal("Activate Altar").withStyle(ChatFormatting.LIGHT_PURPLE))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click to travel the Star's Path").withStyle(ChatFormatting.DARK_PURPLE)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("").withStyle(ChatFormatting.DARK_PURPLE)))));
      activateItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("This Journey Costs: ").withStyle(ChatFormatting.AQUA)))));
      
      MutableComponent text = Component.literal("")
            .append(Component.literal(cost+"").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.translatable(COST.getDescriptionId()).withStyle(ChatFormatting.DARK_AQUA));
      if(cost > COST.getDefaultMaxStackSize()){
         text.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_PURPLE));
         if(cost > 0){
            text.append("("+stacks+" Stacks + "+leftover+")").withStyle(ChatFormatting.YELLOW);
         }else{
            text.append("("+stacks+" Stacks)").withStyle(ChatFormatting.YELLOW);
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
