package net.borisshoes.arcananovum.gui.starlightforge;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
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
   private final int mode; // 0 = Magic Items, 1 = Enhanced Forging
   
   public StarlightForgeInventoryListener(StarlightForgeGui gui, StarlightForgeBlockEntity blockEntity, World world, int mode){
      this.blockEntity = blockEntity;
      this.gui = gui;
      this.world = world;
      this.mode = mode;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         for(int i = 0; i < 25; i++){
            ItemStack stack = inv.getStack(i);
            if(stack.getCount() != 0){
               //System.out.println("Slot " + i + ": " + stack.getItem().getName().getString() + " (" + stack.getCount() + ")");
            }
         }
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
      int stardustCount = inv.getStack(9).getCount();
      DefaultedList<ItemStack> craftingStacks = DefaultedList.of();
      for(int i = 0; i < 9; i++){
         craftingStacks.add(inv.getStack(i));
      }
      CraftingInventory craftingInventory = new CraftingInventory(null,3,3,craftingStacks);
      Optional<RecipeEntry<CraftingRecipe>> optional = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING,craftingInventory,world);
      if(optional.isPresent() && EnhancedStatUtils.isItemEnhanceable(optional.get().value().getResult(world.getRegistryManager()))){
         return stardustCount > 0 ? optional.get().value().getResult(world.getRegistryManager()).copy() : ItemStack.EMPTY;
      }
      return ItemStack.EMPTY;
   }
   
   public DefaultedList<ItemStack> getRemainders(Inventory inv){
      DefaultedList<ItemStack> remainders = DefaultedList.of();
      DefaultedList<ItemStack> craftingStacks = DefaultedList.of();
      int stardustCount = inv.getStack(9).getCount();
      for(int i = 0; i < 9; i++){
         craftingStacks.add(inv.getStack(i));
      }
      CraftingInventory craftingInventory = new CraftingInventory(null,3,3,craftingStacks);
      Optional<RecipeEntry<CraftingRecipe>> optional = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING,craftingInventory,world);
      if(optional.isPresent() && EnhancedStatUtils.isItemEnhanceable(optional.get().value().getResult(world.getRegistryManager()))){
         return stardustCount > 0 ? optional.get().value().getRemainder(craftingInventory) : DefaultedList.of();
      }
      return remainders;
   }
   
   public void validRecipe(Inventory inv){
      if(mode == 0){
         ItemStack[][] curItems = new ItemStack[5][5];
         for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
               curItems[i][j] = inv.getStack(i * 5 + j);
            }
         }
         MagicItem matchedItem = null;
         for(MagicItem item : ArcanaRegistry.registry.values()){
            MagicItemRecipe recipe = item.getRecipe();
            if(recipe == null)
               continue;
            if(recipe.satisfiesRecipe(curItems,blockEntity)){
               matchedItem = item;
               break;
            }
         }
         if(matchedItem == null){
            ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
            NbtCompound tag = table.getOrCreateNbt();
            NbtCompound display = new NbtCompound();
            NbtList loreList = new NbtList();
            display.putString("Name", "[{\"text\":\"Forge Item\",\"italic\":false,\"color\":\"dark_purple\"}]");
            loreList.add(NbtString.of("[{\"text\":\"Click Here\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to forge a Magic Item once a recipe is loaded!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
            loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
            loreList.add(NbtString.of("[{\"text\":\"This slot will show a Magic Item once a valid recipe is loaded.\",\"italic\":true,\"color\":\"aqua\"}]"));
            display.put("Lore", loreList);
            tag.put("display", display);
            tag.putInt("HideFlags", 103);
            gui.setSlot(25, GuiElementBuilder.from(table));
         }else{
            gui.setSlot(25, GuiElementBuilder.from(matchedItem.getPrefItem()).addLoreLine(Text.translatable("")).addLoreLine(Text.translatable("Click to Forge!").formatted(Formatting.AQUA, Formatting.BOLD)).glow());
         }
      }else if(mode == 1 && world != null){
         ItemStack output = getEnhancedStack(inv);
         int stardustCount = inv.getStack(9).getCount();
         
         if(!output.isEmpty() && stardustCount > 0){
            GuiElementBuilder craftingItem = new GuiElementBuilder(output.getItem()).hideFlags();
            craftingItem.setName((Text.literal("")
                  .append(Text.literal("Forge Item").formatted(Formatting.AQUA))));
            craftingItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Click Here ").formatted(Formatting.GREEN))
                  .append(Text.literal("to forge this ").formatted(Formatting.DARK_AQUA))
                  .append(Text.translatable(output.getTranslationKey()).formatted(Formatting.AQUA))
                  .append(Text.literal(" with ").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal(stardustCount+" Stardust").formatted(Formatting.YELLOW))
                  .append(Text.literal("!").formatted(Formatting.DARK_AQUA))));
            gui.setSlot(15,craftingItem);
         }else{
            GuiElementBuilder craftingItem = new GuiElementBuilder(Items.CRAFTING_TABLE);
            craftingItem.setName((Text.literal("")
                  .append(Text.literal("Forge Item").formatted(Formatting.AQUA))));
            craftingItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Click Here ").formatted(Formatting.GREEN))
                  .append(Text.literal("to forge an item once a recipe is loaded!").formatted(Formatting.DARK_AQUA))));
            craftingItem.addLoreLine((Text.literal("")
                  .append(Text.literal("").formatted(Formatting.DARK_AQUA))));
            craftingItem.addLoreLine((Text.literal("")
                  .append(Text.literal("This slot will show an item once a valid recipe is loaded.").formatted(Formatting.LIGHT_PURPLE))));
            gui.setSlot(15,craftingItem);
         }
      }
   }
}
