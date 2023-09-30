package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

public class CatalyticMatrix extends MagicItem {
   
   public CatalyticMatrix(){
      id = "catalytic_matrix";
      name = "Catalytic Matrix";
      rarity = MagicRarity.MUNDANE;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MUNDANE, ArcaneTome.TomeFilter.CATALYSTS};
      itemVersion = 0;
      vanillaItem = Items.NETHER_STAR;
      item = new CatalyticMatrixItem(new FabricItemSettings().maxCount(4).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Catalytic Matrix\",\"italic\":false,\"color\":\"yellow\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"A fragment of a \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Runic Matrix\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"specialized\",\"color\":\"blue\"},{\"text\":\" in \",\"color\":\"dark_purple\"},{\"text\":\"augmenting \",\"color\":\"dark_aqua\"},{\"text\":\"Arcana\",\"color\":\"light_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"On its own, this \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"new matrix\",\"color\":\"yellow\"},{\"text\":\" is \",\"color\":\"dark_purple\"},{\"text\":\"useless\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"matrix \",\"color\":\"yellow\"},{\"text\":\"must be \",\"color\":\"dark_purple\"},{\"text\":\"built upon\",\"color\":\"blue\"},{\"text\":\" to \",\"color\":\"dark_purple\"},{\"text\":\"unlock \",\"color\":\"dark_aqua\"},{\"text\":\"the \",\"color\":\"dark_purple\"},{\"text\":\"full potential\",\"color\":\"aqua\"},{\"text\":\" of \",\"color\":\"dark_purple\"},{\"text\":\"Arcana\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mundane \",\"italic\":false,\"color\":\"gray\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      stack.setCount(4);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.AMETHYST_SHARD,32,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.CRAFTING_TABLE,16,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.END_CRYSTAL,16,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.DIAMOND,4,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHER_STAR,1,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Catalytic Matrix\\n\\nRarity: Mundane\\n\\nThe full power of a Runic Matrix shouldn't be necessary to further unlock abilities within the items I've made.\\nBreaking one into self-contained fragments should be more efficient.\"}");
      return list;
   }
   
   public class CatalyticMatrixItem extends MagicPolymerItem {
      public CatalyticMatrixItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
