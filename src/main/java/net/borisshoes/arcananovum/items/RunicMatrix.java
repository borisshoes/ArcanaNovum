package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RunicMatrix extends MagicItem {
   public RunicMatrix(){
      id = "runic_matrix";
      name = "Runic Matrix";
      rarity = MagicRarity.MUNDANE;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MUNDANE, ArcaneTome.TomeFilter.ITEMS};
      vanillaItem = Items.END_CRYSTAL;
      item = new RunicMatrixItem(new FabricItemSettings().maxCount(1).fireproof());
   
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Matrix\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);

      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
   
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Runes \",\"color\":\"light_purple\"},{\"text\":\"engraved on this \",\"color\":\"dark_purple\"},{\"text\":\"crystalline \",\"color\":\"aqua\"},{\"text\":\"structure\",\"color\":\"dark_purple\"},{\"text\":\" shift\",\"color\":\"blue\"},{\"text\":\" constantly.\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"They \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"slide\",\"color\":\"blue\"},{\"text\":\" to form different combinations of \"},{\"text\":\"runic \",\"color\":\"light_purple\"},{\"text\":\"equations\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"matrix \",\"color\":\"light_purple\"},{\"text\":\"allows for the \"},{\"text\":\"invocation \",\"color\":\"aqua\"},{\"text\":\"of many different \"},{\"text\":\"effects\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient c = new MagicItemIngredient(Items.END_CRYSTAL,8,null);
      MagicItemIngredient t = new MagicItemIngredient(Items.CRAFTING_TABLE,64,null);
      MagicItemIngredient a = new MagicItemIngredient(Items.AMETHYST_SHARD,16,null);
      MagicItemIngredient d = new MagicItemIngredient(Items.DIAMOND,8,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHER_STAR,1,null, true);
   
      MagicItemIngredient[][] ingredients = {
            {a,d,c,d,a},
            {d,t,a,t,d},
            {c,a,n,a,c},
            {d,t,a,t,d},
            {a,d,c,d,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Runic Matrix\\n\\nRarity: Mundane\\n\\nRunic language is often used to create magical effects. Being able to freely combine multiple runic words like in a crafting table results in a device capable of producing a vast number of arcane spells.\"}");
      return list;
   }
   
   public class RunicMatrixItem extends MagicPolymerItem {
      public RunicMatrixItem(Settings settings){
         super(getThis(),settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
