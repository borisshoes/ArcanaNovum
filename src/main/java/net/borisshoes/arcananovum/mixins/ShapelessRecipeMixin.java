package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {
   
   @Inject(method="matches(Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/world/World;)Z", at= @At("HEAD"), cancellable = true)
   public void arcananovum_matches(CraftingInventory craftingInventory, World world, CallbackInfoReturnable<Boolean> cir){
      for(int i = 0; i < craftingInventory.size(); ++i){
         ItemStack item = craftingInventory.getStack(i);
         if(MagicItemUtils.isMagic(item)){
            cir.setReturnValue(false);
         }
      }
   }
   
   @Inject(method="craft(Lnet/minecraft/inventory/CraftingInventory;)Lnet/minecraft/item/ItemStack;", at= @At("RETURN"), cancellable = true)
   public void arcananovum_tomeCraft(CraftingInventory craftingInventory, CallbackInfoReturnable<ItemStack> cir){
      ShapelessRecipe recipe = (ShapelessRecipe) (Object) this;
      if(!cir.getReturnValue().isOf(Items.BOOK)) return;
      boolean hasTable = false;
      boolean hasEye = false;
      for(int i = 0; i < craftingInventory.size(); ++i){
         ItemStack item = craftingInventory.getStack(i);
         if(item.isOf(Items.ENCHANTING_TABLE)) hasTable = true;
         if(item.isOf(Items.ENDER_EYE)) hasEye = true;
      }
   
      if(hasTable && hasEye){ // Matches Tome Recipe
         ItemStack tome = new ItemStack(Items.BOOK);
         NbtCompound tag = tome.getOrCreateNbt();
         NbtCompound display = new NbtCompound();
         NbtList loreList = new NbtList();
         NbtList enchants = new NbtList();
         enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
         display.putString("Name","[{\"text\":\"Mysterious Tome\",\"italic\":true,\"bold\":true,\"color\":\"dark_purple\"}]");
         loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Ender Eye\",\"color\":\"green\"},{\"text\":\" gravitates to the table.\",\"color\":\"blue\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"It\\'s presence causes the table to \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"transform\",\"italic\":true,\"color\":\"light_purple\"},{\"text\":\"!\"}]"));
         display.put("Lore",loreList);
         tag.put("display",display);
         tag.putBoolean("ArcanaGuideBook",true);
         tome.setNbt(tag);
         cir.setReturnValue(tome);
      }
   }
   
   @Inject(method="getOutput", at= @At("RETURN"), cancellable = true)
   public void arcananovum_tomeOutput(CallbackInfoReturnable<ItemStack> cir){
      ShapelessRecipe recipe = (ShapelessRecipe) (Object) this;
      ItemStack stack = cir.getReturnValue();
      if(stack.isOf(Items.BOOK)){
         List<Ingredient> inputs = recipe.getIngredients();
         boolean hasTable = false;
         boolean hasEye = false;
         for(Ingredient input : inputs){
            for(ItemStack inputStack : input.getMatchingStacks()){
               if(inputStack.isOf(Items.ENCHANTING_TABLE)) hasTable = true;
               if(inputStack.isOf(Items.ENDER_EYE)) hasEye = true;
            }
         }
         if(hasTable && hasEye){ // Matches Tome Recipe
            ItemStack tome = new ItemStack(Items.BOOK);
            NbtCompound tag = tome.getOrCreateNbt();
            NbtCompound display = new NbtCompound();
            NbtList loreList = new NbtList();
            NbtList enchants = new NbtList();
            enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
            display.putString("Name","[{\"text\":\"Mysterious Tome\",\"italic\":true,\"bold\":true,\"color\":\"dark_purple\"}]");
            loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Ender Eye\",\"color\":\"green\"},{\"text\":\" gravitates to the table.\",\"color\":\"blue\"}]"));
            loreList.add(NbtString.of("[{\"text\":\"It\\'s presence causes the table to \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"transform\",\"italic\":true,\"color\":\"light_purple\"},{\"text\":\"!\"}]"));
            display.put("Lore",loreList);
            tag.put("display",display);
            tag.putBoolean("ArcanaGuideBook",true);
            tome.setNbt(tag);
            cir.setReturnValue(tome);
         }
      }
   }
   
}
