package net.borisshoes.arcananovum.gui.clockworkmultitool;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.EnderCrate;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.borisshoes.arcananovum.gui.endercrate.EnderCrateChannelGui;
import net.borisshoes.arcananovum.gui.endercrate.EnderCrateGui;
import net.borisshoes.arcananovum.utils.ArcanaColors;
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
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class ClockworkMultitoolEnderGui extends SimpleGui {
   
   private final ItemStack stack;
   private EnderCrateChannel channel;
   
   public ClockworkMultitoolEnderGui(ServerPlayer player, ItemStack stack){
      super(MenuType.HOPPER, player, false);
      this.stack = stack;
      this.channel = EnderCrate.getChannelOrDefault(stack);
      setTitle(ArcanaRegistry.CLOCKWORK_MULTITOOL.getTranslatedName());
   }
   
   public void build(){
      GuiElementBuilder mid = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL, ArcanaColors.ENDER_COLOR)).setName(Component.literal("")).hideTooltip();
      setSlot(0, mid);
      setSlot(2, mid);
      setSlot(4, mid);
      
      GuiElementBuilder echest = new GuiElementBuilder(Items.ENDER_CHEST).hideDefaultTooltip().setName(Items.ENDER_CHEST.getDefaultInstance().getItemName().copy().withStyle(ChatFormatting.DARK_AQUA));
      echest.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" to access your Ender Chest").withStyle(ChatFormatting.DARK_AQUA)))));
      echest.setCallback((type) -> {
         player.openMenu(new SimpleMenuProvider((i, inventory, p) -> ChestMenu.threeRows(i, inventory, player.getEnderChestInventory()), Component.translatable("container.enderchest")));
         close();
      });
      setSlot(1, echest);
      
      GuiElementBuilder crate = new GuiElementBuilder(Items.BARREL).hideDefaultTooltip().setName(ArcanaRegistry.ENDER_CRATE.getTranslatedName().withStyle(ChatFormatting.LIGHT_PURPLE));
      DyeColor[] colors = channel.getColors();
      MutableComponent channelComp = Component.literal("");
      if(channel.isLocked()){
         channelComp.append(Component.literal("\uD83D\uDD12 Private Channel: ").withStyle(ChatFormatting.GREEN));
         DefaultPlayerData playerData = DataAccess.getPlayer(channel.getIdLock(), BorisLib.PLAYER_DATA_KEY);
         if(playerData.getUsername().isEmpty()) playerData.tryResolve(BorisLib.SERVER);
         channelComp.append(playerData.getFaceTextComponent());
         channelComp.append(Component.literal(" - "));
      }else{
         channelComp.append(Component.literal("\uD83D\uDD13 Public Channel: ").withStyle(ChatFormatting.LIGHT_PURPLE));
      }
      for(DyeColor color : colors){
         MutableComponent dyeComp = color == null ? MinecraftUtils.getAtlasedTexture(Blocks.GLASS) : MinecraftUtils.getAtlasedTexture(MinecraftUtils.getVanillaDyeItem(color));
         channelComp.append(dyeComp);
         channelComp.append(" ");
      }
      crate.addLoreLine(channelComp);
      crate.addLoreLine(Component.literal(""));
      crate.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" to access this Ender Crate").withStyle(ChatFormatting.DARK_AQUA)))));
      crate.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to change the Ender Crate channel").withStyle(ChatFormatting.DARK_AQUA)))));
      crate.setCallback((type) -> {
         if(type.isRight){
            EnderCrateChannelGui gui = new EnderCrateChannelGui(player, channel, true);
            gui.setOnConfirm(newChannel -> {
               ArcanaItem.putProperty(stack, EnderCrate.CHANNEL_TAG, EnderCrate.colorsToTag(newChannel.getColors()));
               ArcanaItem.putProperty(stack, EnderCrate.LOCK_TAG, newChannel.isLocked() ? newChannel.getIdLock().toString() : "");
               this.channel = newChannel;
               this.build();
               this.open();
            });
            gui.build();
            gui.open();
         }else{
            int bandwidth = Math.max(0, ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.ENDER_MECHANISM) - 2);
            EnderCrateGui gui = new EnderCrateGui(player, channel, bandwidth);
            gui.open();
         }
      });
      setSlot(3, crate);
   }
}
