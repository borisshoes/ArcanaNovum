package net.borisshoes.arcananovum.gui.brainjar;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.BrainJar;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BrainJarGui extends SimpleGui {
   private BrainJar jar;
   private ItemStack stack;
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param type                        the screen handler that the client should display
    * @param player                      the player to server this gui to
    */
   public BrainJarGui(ScreenHandlerType<?> type, ServerPlayerEntity player, BrainJar jar, ItemStack stack){
      super(type, player, false);
      this.jar = jar;
      this.stack = stack;
   }
   
   public void makeGui(){
      boolean active = ArcanaItem.getBooleanProperty(stack, ArcanaItem.ACTIVE_TAG);
      
      // Try to fix the wierd xp give shenanigans
      player.totalExperience = (LevelUtils.vanillaLevelToTotalXp(player.experienceLevel) + (int)(player.experienceProgress*player.getNextLevelExperience()));
      player.experienceProgress = (float)(player.totalExperience - LevelUtils.vanillaLevelToTotalXp(player.experienceLevel)) / (float)player.getNextLevelExperience();
      
      GuiElementBuilder echest = new GuiElementBuilder(Items.ENDER_CHEST);
      echest.setName(Text.literal("Store Levels").formatted(Formatting.DARK_AQUA));
      echest.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Click to store ").formatted(Formatting.GREEN))
            .append(Text.literal("1").formatted(Formatting.AQUA))
            .append(Text.literal(" level").formatted(Formatting.DARK_AQUA))));
      echest.addLoreLine(TextUtils.removeItalics(Text.literal("")));
      echest.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Right click to store all (").formatted(Formatting.GREEN))
            .append(Text.literal(""+player.experienceLevel).formatted(Formatting.AQUA))
            .append(Text.literal(")").formatted(Formatting.GREEN))
            .append(Text.literal(" levels").formatted(Formatting.DARK_AQUA))));
      setSlot(0,echest);
      
      GuiElementBuilder bottle = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE);
      bottle.setName(Text.literal("Withdraw Levels").formatted(Formatting.DARK_AQUA));
      bottle.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Click to gain ").formatted(Formatting.GREEN))
            .append(Text.literal("1").formatted(Formatting.AQUA))
            .append(Text.literal(" level").formatted(Formatting.DARK_AQUA))));
      bottle.addLoreLine(TextUtils.removeItalics(Text.literal("")));
      bottle.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Right click to take all (").formatted(Formatting.GREEN))
            .append(Text.literal(""+jar.getEnergy(stack)).formatted(Formatting.AQUA))
            .append(Text.literal(")").formatted(Formatting.GREEN))
            .append(Text.literal(" XP").formatted(Formatting.DARK_AQUA))));
      setSlot(4,bottle);
      
      setSlot(1,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,ArcanaColors.XP_COLOR)).setName(Text.literal(LevelUtils.readableInt(jar.getEnergy(stack))+" XP Stored").formatted(Formatting.GREEN)));
      setSlot(3,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, ArcanaColors.XP_COLOR)).setName(Text.literal(LevelUtils.readableInt(jar.getEnergy(stack))+" XP Stored").formatted(Formatting.GREEN)));
      
      if(!active){
         GuiElementBuilder notmending = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CANCEL));
         notmending.setName(Text.literal("Not Mending Items").formatted(Formatting.DARK_AQUA));
         notmending.addLoreLine(TextUtils.removeItalics(Text.literal("Currently Not Mending Items").formatted(Formatting.RED)));
         notmending.addLoreLine(TextUtils.removeItalics(Text.literal("")));
         notmending.addLoreLine(TextUtils.removeItalics(Text.literal("Click to toggle ON").formatted(Formatting.GREEN)));
         setSlot(2,notmending);
      }else{
         GuiElementBuilder mending = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CONFIRM));
         mending.setName(Text.literal("Mending Items").formatted(Formatting.DARK_AQUA));
         mending.addLoreLine(TextUtils.removeItalics(Text.literal("Currently Mending Items").formatted(Formatting.GREEN)));
         mending.addLoreLine(TextUtils.removeItalics(Text.literal("")));
         mending.addLoreLine(TextUtils.removeItalics(Text.literal("Click to toggle OFF").formatted(Formatting.RED)));
         setSlot(2,mending);
      }
      
      setTitle(Text.literal("Brain in a Jar"));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(index == 0){
         jar.depositXP(player, stack, type != ClickType.MOUSE_RIGHT,this);
      }else if(index == 2){
         jar.toggleMending(this,player, stack);
      }else if(index == 4){
         jar.withdrawXP(player, stack, type != ClickType.MOUSE_RIGHT,this);
      }
      return true;
   }
   
   @Override
   public void onClose(){
   
   }
   
}
