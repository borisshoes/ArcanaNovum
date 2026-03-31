package net.borisshoes.arcananovum.gui.starlightforge;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class StarlightForgeInventoryListener implements ContainerListener {
   private final StarlightForgeBlockEntity blockEntity;
   private final Level world;
   private final StarlightForgeGui gui;
   private boolean updating = false;
   
   public StarlightForgeInventoryListener(StarlightForgeGui gui, StarlightForgeBlockEntity blockEntity, Level world, int mode){
      this.blockEntity = blockEntity;
      this.gui = gui;
      this.world = world;
   }
   
   @Override
   public void containerChanged(Container inv){
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
   
   public ItemStack getEnhancedStack(Container inv){
      if(!(world instanceof ServerLevel serverWorld)) return null;
      NonNullList<ItemStack> craftingStacks = NonNullList.create();
      boolean hasGold = true;
      boolean hasPaper = true;
      for(int i = 0; i < 9; i++){
         craftingStacks.add(inv.getItem(i));
         
         if(i == 4){
            hasGold = inv.getItem(i).is(Items.GOLD_INGOT);
         }else if(hasPaper && !inv.getItem(i).is(ArcanaRegistry.EXOTIC_ARCANE_PAPER)){
            hasPaper = false;
         }
      }
      
      if(!ArcanaNovum.CONFIG.getBoolean(ArcanaConfig.DISABLE_STARDUST_INFUSION)){
         CraftingInput input = CraftingInput.of(3, 3, craftingStacks);
         Optional<RecipeHolder<CraftingRecipe>> optional = serverWorld.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, world);
         if(optional.isPresent() && EnhancedStatUtils.isItemEnhanceable(optional.get().value().assemble(input, world.registryAccess()))){
            return optional.get().value().assemble(input, world.registryAccess()).copy();
         }
      }
      
      return hasGold && hasPaper ? new ItemStack(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER) : ItemStack.EMPTY;
   }
   
   public NonNullList<ItemStack> getRemainders(Container inv){
      if(!(world instanceof ServerLevel serverWorld)) return NonNullList.create();
      NonNullList<ItemStack> remainders = NonNullList.create();
      NonNullList<ItemStack> craftingStacks = NonNullList.create();
      for(int i = 0; i < 9; i++){
         craftingStacks.add(inv.getItem(i));
      }
      CraftingInput input = CraftingInput.of(3, 3, craftingStacks);
      Optional<RecipeHolder<CraftingRecipe>> optional = serverWorld.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, world);
      if(optional.isPresent() && EnhancedStatUtils.isItemEnhanceable(optional.get().value().assemble(input, world.registryAccess()))){
         return optional.get().value().getRemainingItems(input);
      }
      return remainders;
   }
   
   public void validRecipe(Container inv){
      if(gui.getMode() == 1){
         ArcanaRecipe recipe = RecipeManager.getMatchingRecipe(inv, blockEntity);
         
         if(recipe == null){
            GuiElementBuilder table = new GuiElementBuilder(Items.CRAFTING_TABLE).hideDefaultTooltip();
            table.setName(Component.literal("Forge Item").withStyle(ChatFormatting.DARK_PURPLE));
            table.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Click Here").withStyle(ChatFormatting.GREEN))
                  .append(Component.literal(" to forge an Arcana Item once a recipe is loaded!").withStyle(ChatFormatting.LIGHT_PURPLE))));
            table.addLoreLine(TextUtils.removeItalics(Component.literal("")));
            table.addLoreLine(TextUtils.removeItalics(Component.literal("This slot will show an Arcana Item once a valid recipe is loaded.").withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA)));
            gui.setSlot(25, table);
            gui.setLoadedRecipe(null);
         }else{
            gui.setSlot(25, GuiElementBuilder.from(recipe.getDisplayStack()).addLoreLine(Component.literal("")).addLoreLine(TextUtils.removeItalics(Component.literal("Click to Forge!").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))).glow());
            gui.setLoadedRecipe(recipe);
         }
      }else if(gui.getMode() == 2 && world != null){
         ItemStack output = MinecraftUtils.removeLore(getEnhancedStack(inv).copy());
         
         if(!output.isEmpty()){
            GuiElementBuilder craftingItem = GuiElementBuilder.from(output).hideDefaultTooltip();
            craftingItem.setName((Component.literal("")
                  .append(Component.literal("Forge Item").withStyle(ChatFormatting.AQUA))));
            craftingItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Click Here ").withStyle(ChatFormatting.GREEN))
                  .append(Component.literal("to begin forging this ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.translatable(output.getItem().getDescriptionId()).withStyle(ChatFormatting.YELLOW))
                  .append(Component.literal("!").withStyle(ChatFormatting.DARK_AQUA)))));
            gui.setSlot(15, craftingItem);
         }else{
            GuiElementBuilder craftingItem = new GuiElementBuilder(Items.CRAFTING_TABLE);
            craftingItem.setName((Component.literal("")
                  .append(Component.literal("Forge Item").withStyle(ChatFormatting.AQUA))));
            craftingItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Click Here ").withStyle(ChatFormatting.GREEN))
                  .append(Component.literal("to forge an item once a recipe is loaded!").withStyle(ChatFormatting.DARK_AQUA)))));
            if(ArcanaNovum.CONFIG.getBoolean(ArcanaConfig.DISABLE_STARDUST_INFUSION)){
               craftingItem.addLoreLine(Component.literal(""));
               craftingItem.addLoreLine(Component.literal("Stardust Infusion is disabled, only Sovereign Paper can be crafted").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            }
            craftingItem.addLoreLine(Component.literal(""));
            craftingItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("This slot will show an item once a valid recipe is loaded.").withStyle(ChatFormatting.LIGHT_PURPLE)))));
            gui.setSlot(15, craftingItem);
         }
      }
   }
}
