package net.borisshoes.arcananovum.gui.endercrate;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.blocks.EnderCrate;
import net.borisshoes.arcananovum.blocks.EnderCrateBlockEntity;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannels;
import net.borisshoes.arcananovum.gui.ClickCooldown;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.datastorage.DefaultPlayerData;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class EnderCrateChannelGui extends SimpleGui implements ClickCooldown {
   
   private EnderCrateChannel channel;
   private final boolean canLock;
   private Consumer<EnderCrateChannel> onConfirm;
   private EnderCrateBlockEntity watched;
   private int clickCooldown = 0;
   
   public EnderCrateChannelGui(ServerPlayer player, @Nullable EnderCrateChannel channel, boolean canLock){
      super(MenuType.GENERIC_9x3, player, false);
      this.channel = channel == null ? EnderCrateChannels.getChannel(EnderCrate.DEFAULT_CHANNEL) : channel;
      this.canLock = canLock;
      setTitle(Component.literal("Ender Crate Channel Tuning"));
      build();
   }
   
   public void setOnConfirm(Consumer<EnderCrateChannel> onConfirm){
      this.onConfirm = onConfirm;
   }
   
   public void setWatched(EnderCrateBlockEntity watched){
      this.watched = watched;
   }
   
   public void build(){
      DyeColor[] colors = channel.getColors();
      for(int i = 0; i < 9; i++){
         GuiElementBuilder bottom = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_CONNECTOR, channel.getColor())).setName(Component.literal("")).hideTooltip();
         GuiElementBuilder top = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_CONNECTOR, channel.getColor())).setName(Component.literal("")).hideTooltip();
         DyeColor color = colors[i];
         int finalI = i;
         Consumer<ClickType> click = (type) -> {
            if(isOnClickCooldown() || type == ClickType.MOUSE_DOUBLE_CLICK) return;
            resetClickCooldown();
            ArrayList<DyeColor> options = new ArrayList<>();
            options.add(null);
            options.addAll(List.of(DyeColor.values()));
            int curInd = options.indexOf(color);
            
            if(type.shift){
               curInd = 0;
            }else if(type.isRight){
               curInd = (curInd - 1) % options.size();
               if(curInd < 0) curInd = options.size()-1;
            }else{
               curInd = (curInd + 1) % options.size();
            }
            colors[finalI] = options.get(curInd);
            this.channel = EnderCrateChannels.getChannel(this.channel.getIdLock(),colors);
            build();
         };
         
         setSlot(i,top.setCallback(click));
         setSlot(i+18,bottom.setCallback(click));
         
         GuiElementBuilder dye = GuiElementBuilder.from(GraphicalItem.with(EnderCrateChannel.colorToGraphicElement(color))).hideDefaultTooltip();
         MutableComponent dyeComp = color == null ? MinecraftUtils.getAtlasedTexture(Blocks.GLASS) : MinecraftUtils.getAtlasedTexture(DyeItem.byColor(color));
         dye.setName(Component.literal("")
               .append(Component.literal("Frequency "+(i+1)+": ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(dyeComp));
         dye.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.GREEN))
               .append(Component.literal(" to cycle the frequency").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         dye.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_AQUA))
               .append(Component.literal(" to reverse cycle the frequency").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         dye.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Shift Click").withStyle(ChatFormatting.DARK_AQUA))
               .append(Component.literal(" to reset the frequency").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         setSlot(i+9,dye.setCallback(click));
      }
      
      GuiElementBuilder channelType = new GuiElementBuilder(channel.isLocked() ? Items.IRON_BARS : Items.ENDER_EYE).hideDefaultTooltip();
      if(channel.isLocked()){
         DefaultPlayerData playerData = DataAccess.getPlayer(channel.getIdLock(),BorisLib.PLAYER_DATA_KEY);
         if(playerData.getUsername().isEmpty()) playerData.tryResolve(BorisLib.SERVER);
         channelType.setName(Component.literal("\uD83D\uDD12 Private Channel \uD83D\uDD12").withStyle(ChatFormatting.LIGHT_PURPLE));
         channelType.addLoreLine(Component.literal("")
               .append(playerData.getFaceTextComponent())
               .append(Component.literal(" "+playerData.getUsername()+"'s Channel").withStyle(ChatFormatting.GREEN)));
         if(canLock && player.getUUID().equals(channel.getIdLock())){
            channelType.addLoreLine(Component.literal(""));
            channelType.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Click").withStyle(ChatFormatting.GREEN))
                  .append(Component.literal(" to go to the public channel").withStyle(ChatFormatting.DARK_AQUA)))));
         }
      }else{
         channelType.setName(Component.literal("\uD83D\uDD13 Public Channel \uD83D\uDD13").withStyle(ChatFormatting.GREEN));
         if(canLock){
            channelType.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Click").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal(" to go to the private channel").withStyle(ChatFormatting.DARK_AQUA)))));
         }
      }
      if(canLock){
         channelType.setCallback((type) -> {
            if(channel.isLocked()){
               this.channel = EnderCrateChannels.getChannel(this.channel.getColors());
            }else{
               this.channel = EnderCrateChannels.getChannel(player.getUUID(), this.channel.getColors());
            }
            build();
         });
      }
      setSlot(4,channelType);
      
      GuiElementBuilder confirm = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CONFIRM)).setName(Component.literal("Confirm Channel").withStyle(ChatFormatting.GREEN)).hideDefaultTooltip();
      confirm.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to confirm the channel").withStyle(ChatFormatting.DARK_AQUA)))));
      confirm.setCallback((type) -> {
         close();
      });
      setSlot(22,confirm);
   }
   
   @Override
   public void onTick(){
      if(watched != null){
         Level world = watched.getLevel();
         if(world == null || world.getBlockEntity(watched.getBlockPos()) != watched){
            world.gameEvent(GameEvent.CONTAINER_CLOSE, watched.getBlockPos(), GameEvent.Context.of(watched.getBlockState()));
            this.close();
         }
      }
      tickClickCooldown();
      super.onTick();
   }
   
   @Override
   public void onClose(){
      if(onConfirm != null){
         onConfirm.accept(this.channel);
         Set<DyeColor> colors = new HashSet<>();
         for(DyeColor color : this.channel.getColors()){
            if(color != null) colors.add(color);
         }
         if(colors.size() == 9){
            ArcanaAchievements.grant(player,ArcanaAchievements.SECURITY_RAINBOW);
         }
      }
      super.onClose();
   }
   
   @Override
   public int getClickCooldown(){
      return clickCooldown;
   }
   
   @Override
   public void setClickCooldown(int cooldown){
      this.clickCooldown = cooldown;
   }
   
   @Override
   public int getCooldownDuration(){
      return 2;
   }
}
