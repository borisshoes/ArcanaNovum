package net.borisshoes.arcananovum.gui.brainjar;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.items.BrainJar;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BrainJarGui extends SimpleGui {
   private BrainJar jar;
   private ItemStack stack;
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param type   the screen handler that the client should display
    * @param player the player to server this gui to
    */
   public BrainJarGui(MenuType<?> type, ServerPlayer player, BrainJar jar, ItemStack stack){
      super(type, player, false);
      this.jar = jar;
      this.stack = stack;
   }
   
   public void makeGui(){
      boolean active = ArcanaItem.getBooleanProperty(stack, ArcanaItem.ACTIVE_TAG);
      
      // Try to fix the wierd xp give shenanigans
      player.totalExperience = (LevelUtils.vanillaLevelToTotalXp(player.experienceLevel) + (int) (player.experienceProgress * player.getXpNeededForNextLevel()));
      player.experienceProgress = (float) (player.totalExperience - LevelUtils.vanillaLevelToTotalXp(player.experienceLevel)) / (float) player.getXpNeededForNextLevel();
      
      GuiElementBuilder echest = new GuiElementBuilder(Items.ENDER_CHEST);
      echest.setName(Component.literal("Store Levels").withStyle(ChatFormatting.DARK_AQUA));
      echest.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click to store ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("1").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" level").withStyle(ChatFormatting.DARK_AQUA))));
      echest.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      echest.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Right click to store all (").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("" + player.experienceLevel).withStyle(ChatFormatting.AQUA))
            .append(Component.literal(")").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" levels").withStyle(ChatFormatting.DARK_AQUA))));
      echest.setCallback((clickType) -> {
         jar.depositXP(player, stack, clickType != ClickType.MOUSE_RIGHT, this);
      });
      setSlot(0, echest);
      
      GuiElementBuilder bottle = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE);
      bottle.setName(Component.literal("Withdraw Levels").withStyle(ChatFormatting.DARK_AQUA));
      bottle.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click to gain ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("1").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" level").withStyle(ChatFormatting.DARK_AQUA))));
      bottle.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      bottle.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Right click to take all (").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("" + EnergyItem.getEnergy(stack)).withStyle(ChatFormatting.AQUA))
            .append(Component.literal(")").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))));
      bottle.setCallback((clickType) -> {
         jar.withdrawXP(player, stack, clickType != ClickType.MOUSE_RIGHT, this);
      });
      setSlot(4, bottle);
      
      setSlot(1, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, ArcanaColors.XP_COLOR)).setName(Component.literal(TextUtils.readableInt(EnergyItem.getEnergy(stack)) + " XP Stored").withStyle(ChatFormatting.GREEN)));
      setSlot(3, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, ArcanaColors.XP_COLOR)).setName(Component.literal(TextUtils.readableInt(EnergyItem.getEnergy(stack)) + " XP Stored").withStyle(ChatFormatting.GREEN)));
      
      if(!active){
         GuiElementBuilder notmending = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CANCEL));
         notmending.setName(Component.literal("Not Mending Items").withStyle(ChatFormatting.DARK_AQUA));
         notmending.addLoreLine(TextUtils.removeItalics(Component.literal("Currently Not Mending Items").withStyle(ChatFormatting.RED)));
         notmending.addLoreLine(TextUtils.removeItalics(Component.literal("")));
         notmending.addLoreLine(TextUtils.removeItalics(Component.literal("Click to toggle ON").withStyle(ChatFormatting.GREEN)));
         notmending.setCallback((clickType) -> {
            jar.toggleMending(this, player, stack);
         });
         setSlot(2, notmending);
      }else{
         GuiElementBuilder mending = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CONFIRM));
         mending.setName(Component.literal("Mending Items").withStyle(ChatFormatting.DARK_AQUA));
         mending.addLoreLine(TextUtils.removeItalics(Component.literal("Currently Mending Items").withStyle(ChatFormatting.GREEN)));
         mending.addLoreLine(TextUtils.removeItalics(Component.literal("")));
         mending.addLoreLine(TextUtils.removeItalics(Component.literal("Click to toggle OFF").withStyle(ChatFormatting.RED)));
         mending.setCallback((clickType) -> {
            jar.toggleMending(this, player, stack);
         });
         setSlot(2, mending);
      }
      
      setTitle(Component.literal("Brain in a Jar"));
   }
}
