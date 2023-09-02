package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
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

public class MythicalCatalyst extends MagicItem {
   
   public MythicalCatalyst(){
      id = "mythical_catalyst";
      name = "Mythical Augment Catalyst";
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MYTHICAL, ArcaneTome.TomeFilter.CATALYSTS};
      rarity = MagicRarity.MYTHICAL;
      itemVersion = 1;
      vanillaItem = Items.AMETHYST_CLUSTER;
      item = new MythicalCatalystItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Mythical Augment Catalyst\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Augment \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Catalysts\",\"color\":\"blue\"},{\"text\":\" can be used to \",\"color\":\"gray\"},{\"text\":\"augment \"},{\"text\":\"your \",\"color\":\"gray\"},{\"text\":\"Magic Items\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Augments \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"require more \",\"color\":\"gray\"},{\"text\":\"powerful \",\"color\":\"green\"},{\"text\":\"Catalysts \",\"color\":\"blue\"},{\"text\":\"at higher levels\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Apply \",\"italic\":false,\"color\":\"green\"},{\"text\":\"these \",\"color\":\"gray\"},{\"text\":\"Catalysts \",\"color\":\"blue\"},{\"text\":\"in the \",\"color\":\"gray\"},{\"text\":\"Tinkering Menu\",\"color\":\"blue\"},{\"text\":\" of your \",\"color\":\"gray\"},{\"text\":\"Tome\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mythical \",\"italic\":false,\"color\":\"light_purple\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
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
      ItemStack soulSand = new ItemStack(Items.SOUL_SAND);
      NbtCompound tag = soulSand.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Soul Sand or Soil\",\"italic\":false,\"color\":\"gray\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Construct a Wither Base with a heart of Netherite.\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
   
      ItemStack skull = new ItemStack(Items.WITHER_SKELETON_SKULL);
      tag = skull.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Wither Skeleton Skull\",\"italic\":false,\"color\":\"dark_gray\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Construct a Wither Base with a heart of Netherite.\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      
      ItemStack netherite = new ItemStack(Items.NETHERITE_BLOCK);
      tag = netherite.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Block of Netherite\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Construct a Wither Base with a heart of Netherite.\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
   
      ItemStack catalyst = new ItemStack(ArcanaRegistry.LEGENDARY_CATALYST.getItem());
      tag = catalyst.getOrCreateNbt();
      display = tag.getCompound("display");
      loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"gold\"},{\"text\":\" the \",\"color\":\"dark_purple\"},{\"text\":\"Catalyst \"},{\"text\":\"on the \",\"color\":\"dark_purple\"},{\"text\":\"Netherite Heart\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Divine Energy\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" will flow into the \",\"color\":\"dark_purple\"},{\"text\":\"Construct\",\"color\":\"gray\"},{\"text\":\", empowering it.\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Defeat the \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Construct \",\"color\":\"gray\"},{\"text\":\"without dying to receive a \"},{\"text\":\"Mythical Catalyst\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Warning! This fight is difficult, preparation is necessary.\",\"italic\":false,\"color\":\"red\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
   
      ItemStack pane = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
      tag = pane.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"In World Construct\",\"italic\":false,\"color\":\"gray\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Build this in the World\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      
      ExplainIngredient a = new ExplainIngredient(pane,"",false);
      ExplainIngredient s = new ExplainIngredient(soulSand,"Soul Sand or Soil");
      ExplainIngredient k = new ExplainIngredient(skull,"Wither Skeleton Skull");
      ExplainIngredient n = new ExplainIngredient(netherite,"Netherite Block");
      ExplainIngredient c = new ExplainIngredient(catalyst,"Legendary Augment Catalyst");
   
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,k,k,k,a},
            {a,s,n,s,c},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" Mythical Augmentation\\n         Catalyst\\n\\nRarity: Mythical\\n\\nThe Mythical Artifacts are examples of divine Arcana, the Runic Matrix should be able to replicate that divine magic to some degree. But how? Perhaps I need to expose my strongest\"}");
      list.add("{\"text\":\" Mythical Augmentation\\n         Catalyst\\n\\nCatalyst to some divine Arcana. I just need to get the attention of a got without risking too much of their wrath. The Wither is an interesting creature with some divine connection, perhaps that is my answer.\"}");
      list.add("{\"text\":\" Mythical Augmentation\\n         Catalyst\\n\\nI know not what God is responsible for the Wither, but the God of Death would be a good guess. Reinforcing the construct pattern of the Wither and placing my Catalyst inside as it comes to life should give it the divine energy it requires.\"}");
      return list;
   }
   
   public class MythicalCatalystItem extends MagicPolymerItem {
      public MythicalCatalystItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}