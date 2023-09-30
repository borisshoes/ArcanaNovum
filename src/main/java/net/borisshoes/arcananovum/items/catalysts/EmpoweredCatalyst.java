package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
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

public class EmpoweredCatalyst extends MagicItem {
   
   public EmpoweredCatalyst(){
      id = "empowered_catalyst";
      name = "Empowered Augment Catalyst";
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MUNDANE, ArcaneTome.TomeFilter.CATALYSTS};
      rarity = MagicRarity.MUNDANE;
      vanillaItem = Items.EMERALD;
      item = new EmpoweredCatalystItem(new FabricItemSettings().maxCount(1).fireproof());
      
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Empowered Augment Catalyst\",\"italic\":false,\"color\":\"green\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Augment \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Catalysts\",\"color\":\"blue\"},{\"text\":\" can be used to \",\"color\":\"gray\"},{\"text\":\"augment \"},{\"text\":\"your \",\"color\":\"gray\"},{\"text\":\"Magic Items\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Augments \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"require more \",\"color\":\"gray\"},{\"text\":\"powerful \",\"color\":\"green\"},{\"text\":\"Catalysts \",\"color\":\"blue\"},{\"text\":\"at higher levels\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Apply \",\"italic\":false,\"color\":\"green\"},{\"text\":\"these \",\"color\":\"gray\"},{\"text\":\"Catalysts \",\"color\":\"blue\"},{\"text\":\"in the \",\"color\":\"gray\"},{\"text\":\"Tinkering Menu\",\"color\":\"blue\"},{\"text\":\" of a \",\"color\":\"gray\"},{\"text\":\"Twilight Anvil\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mundane \",\"italic\":false,\"color\":\"gray\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient b = new MagicItemIngredient(Items.OBSIDIAN,16,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.CRYING_OBSIDIAN,16,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.EMERALD,32,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.EMERALD,16,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHER_STAR,1,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.MUNDANE_CATALYST,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,i,h,i,b},
            {c,g,m,g,c},
            {b,i,h,i,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withAnvil());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Empowered Augment\\n         Catalyst\\n\\nRarity: Mundane\\n\\nMy previous endeavor was successful, at least somewhat. The Matrix destabilized with more demanding augmentations. I believe I can press it further if I use some better crystals.\"}");
      return list;
   }
   
   public class EmpoweredCatalystItem extends MagicPolymerItem {
      public EmpoweredCatalystItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
