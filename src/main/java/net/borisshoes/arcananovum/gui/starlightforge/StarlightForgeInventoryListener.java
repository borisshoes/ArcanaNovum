package net.borisshoes.arcananovum.gui.starlightforge;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Optional;

public class StarlightForgeInventoryListener implements InventoryChangedListener {
   private final StarlightForgeBlockEntity blockEntity;
   private final World world;
   private final StarlightForgeGui gui;
   private boolean updating = false;
   
   public StarlightForgeInventoryListener(StarlightForgeGui gui, StarlightForgeBlockEntity blockEntity, World world, int mode){
      this.blockEntity = blockEntity;
      this.gui = gui;
      this.world = world;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         //Check Valid Recipe, and update gui
         validRecipe(inv);
         finishUpdate();
      }
   }
   
   public void finishUpdate(){
      updating = false;
   }
   
   public void setUpdating(){
      updating = true;
   }
   
   public ItemStack getEnhancedStack(Inventory inv){
      if(!(world instanceof ServerWorld serverWorld)) return null;
      DefaultedList<ItemStack> craftingStacks = DefaultedList.of();
      boolean hasGold = true;
      boolean hasPaper = true;
      for(int i = 0; i < 9; i++){
         craftingStacks.add(inv.getStack(i));
         
         if(i == 4){
            hasGold = inv.getStack(i).isOf(Items.GOLD_INGOT);
         }else if(hasPaper && !inv.getStack(i).isOf(ArcanaRegistry.EXOTIC_ARCANE_PAPER)){
            hasPaper = false;
         }
      }
      
      CraftingRecipeInput input = CraftingRecipeInput.create(3,3,craftingStacks);
      Optional<RecipeEntry<CraftingRecipe>> optional = serverWorld.getRecipeManager().getFirstMatch(RecipeType.CRAFTING,input,world);
      if(optional.isPresent() && EnhancedStatUtils.isItemEnhanceable(optional.get().value().craft(input,world.getRegistryManager()))){
         return optional.get().value().craft(input,world.getRegistryManager()).copy();
      }
      
      return hasGold && hasPaper ? new ItemStack(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER) : ItemStack.EMPTY;
   }
   
   public DefaultedList<ItemStack> getRemainders(Inventory inv){
      if(!(world instanceof ServerWorld serverWorld)) return DefaultedList.of();
      DefaultedList<ItemStack> remainders = DefaultedList.of();
      DefaultedList<ItemStack> craftingStacks = DefaultedList.of();
      for(int i = 0; i < 9; i++){
         craftingStacks.add(inv.getStack(i));
      }
      CraftingRecipeInput input = CraftingRecipeInput.create(3,3,craftingStacks);
      Optional<RecipeEntry<CraftingRecipe>> optional = serverWorld.getRecipeManager().getFirstMatch(RecipeType.CRAFTING,input,world);
      if(optional.isPresent() && EnhancedStatUtils.isItemEnhanceable(optional.get().value().craft(input,world.getRegistryManager()))){
         return optional.get().value().getRecipeRemainders(input);
      }
      return remainders;
   }
   
   public void validRecipe(Inventory inv){
      if(gui.getMode() == 1){
         ItemStack[][] curItems = new ItemStack[5][5];
         for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
               curItems[i][j] = inv.getStack(i * 5 + j);
            }
         }
         ArcanaItem matchedItem = null;
         for(ArcanaItem item : ArcanaRegistry.ARCANA_ITEMS.stream().toList()){
            ArcanaRecipe recipe = item.getRecipe();
            if(recipe == null)
               continue;
            if(recipe.satisfiesRecipe(curItems,blockEntity)){
               matchedItem = item;
               break;
            }
         }
         if(matchedItem == null){
            GuiElementBuilder table = new GuiElementBuilder(Items.CRAFTING_TABLE).hideDefaultTooltip();
            table.setName(Text.literal("Forge Item").formatted(Formatting.DARK_PURPLE));
            table.addLoreLine(TextUtils.removeItalics(Text.literal("")
                  .append(Text.literal("Click Here").formatted(Formatting.GREEN))
                  .append(Text.literal(" to forge an Arcana Item once a recipe is loaded!").formatted(Formatting.LIGHT_PURPLE))));
            table.addLoreLine(TextUtils.removeItalics(Text.literal("")));
            table.addLoreLine(TextUtils.removeItalics(Text.literal("This slot will show an Arcana Item once a valid recipe is loaded.").formatted(Formatting.ITALIC,Formatting.AQUA)));
            gui.setSlot(25, table);
         }else{
            gui.setSlot(25, GuiElementBuilder.from(matchedItem.getPrefItem()).addLoreLine(Text.literal("")).addLoreLine(TextUtils.removeItalics(Text.literal("Click to Forge!").formatted(Formatting.AQUA, Formatting.BOLD))).glow());
         }
      }else if(gui.getMode() == 2 && world != null){
         ItemStack output = MinecraftUtils.removeLore(getEnhancedStack(inv).copy());
         
         if(!output.isEmpty()){
            GuiElementBuilder craftingItem = GuiElementBuilder.from(output).hideDefaultTooltip();
            craftingItem.setName((Text.literal("")
                  .append(Text.literal("Forge Item").formatted(Formatting.AQUA))));
            craftingItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Click Here ").formatted(Formatting.GREEN))
                  .append(Text.literal("to begin forging this ").formatted(Formatting.DARK_AQUA))
                  .append(Text.translatable(output.getItem().getTranslationKey()).formatted(Formatting.YELLOW))
                  .append(Text.literal("!").formatted(Formatting.DARK_AQUA)))));
            gui.setSlot(15,craftingItem);
         }else{
            GuiElementBuilder craftingItem = new GuiElementBuilder(Items.CRAFTING_TABLE);
            craftingItem.setName((Text.literal("")
                  .append(Text.literal("Forge Item").formatted(Formatting.AQUA))));
            craftingItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Click Here ").formatted(Formatting.GREEN))
                  .append(Text.literal("to forge an item once a recipe is loaded!").formatted(Formatting.DARK_AQUA)))));
            craftingItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("").formatted(Formatting.DARK_AQUA)))));
            craftingItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("This slot will show an item once a valid recipe is loaded.").formatted(Formatting.LIGHT_PURPLE)))));
            gui.setSlot(15,craftingItem);
         }
      }
   }
}
