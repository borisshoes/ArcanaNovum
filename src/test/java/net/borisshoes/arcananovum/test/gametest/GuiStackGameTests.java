package net.borisshoes.arcananovum.test.gametest;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.BaseSlotGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.altars.TransmutationAltarRecipeGui;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds GUI pages for a mock player and checks every slot holds a stack the client can receive.
 * SGUI's setCount no longer clamps to the max stack size, and an oversized stack errors when it is sent.
 */
public class GuiStackGameTests {
   
   @GameTest(maxTicks = 200)
   @SuppressWarnings("removal")
   public void arcaneTomePagesHaveValidStacks(GameTestHelper helper){
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      List<String> problems = new ArrayList<>();
      ArcaneTomeGui gui = new ArcaneTomeGui(player, ArcaneTomeGui.TomeMode.PROFILE).addModes();
      
      for(ArcaneTomeGui.TomeMode mode : List.of(ArcaneTomeGui.TomeMode.PROFILE, ArcaneTomeGui.TomeMode.COMPENDIUM, ArcaneTomeGui.TomeMode.ACHIEVEMENTS, ArcaneTomeGui.TomeMode.LEADERBOARD, ArcaneTomeGui.TomeMode.RESEARCH)){
         build(problems, gui, "Tome " + mode, () -> gui.buildGui(mode));
      }
      for(ArcanaItem item : ArcanaRegistry.ARCANA_ITEMS){
         build(problems, gui, "Tome item page " + item.getId(), () -> gui.buildGui(ArcaneTomeGui.TomeMode.ITEM, item));
         List<ArcanaRecipe> recipes = RecipeManager.getRecipesFor(item.getItem());
         for(int i = 0; i < recipes.size(); i++){
            int index = i;
            build(problems, gui, "Tome recipe page " + item.getId() + " #" + i, () -> gui.buildGui(ArcaneTomeGui.TomeMode.RECIPE, item, recipes, index));
         }
      }
      
      helper.assertTrue(problems.isEmpty(), "Arcane Tome GUI problems:\n" + String.join("\n", problems));
      helper.succeed();
   }
   
   @GameTest
   @SuppressWarnings("removal")
   public void transmutationRecipeBookHasValidStacks(GameTestHelper helper){
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      List<String> problems = new ArrayList<>();
      TransmutationAltarRecipeGui gui = new TransmutationAltarRecipeGui(player, null, Optional.empty());
      build(problems, gui, "Transmutation recipes", gui::buildPage);
      helper.assertTrue(problems.isEmpty(), "Transmutation recipe GUI problems:\n" + String.join("\n", problems));
      helper.succeed();
   }
   
   /** Runs a page builder and records any exception or invalid slot stack */
   static void build(List<String> problems, BaseSlotGui gui, String label, Runnable builder){
      try{
         builder.run();
      }catch(Exception e){
         problems.add(label + ": threw " + e);
         return;
      }
      for(int i = 0; i < gui.getSize(); i++){
         GuiElement element = gui.getGuiElement(i);
         if(element == null) continue;
         ItemStack stack = element.getItemStack();
         int slot = i;
         ItemStack.validateStrict(stack).error().ifPresent(error -> problems.add(label + " slot " + slot + " (" + stack.getItem() + "): " + error.message()));
      }
   }
}
