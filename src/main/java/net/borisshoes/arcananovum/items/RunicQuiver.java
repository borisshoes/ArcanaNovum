package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

public class RunicQuiver extends MagicItem {
   
   public RunicQuiver(){
      id = "runic_quiver";
      name = "Runic Quiver";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.ITEMS};
      
      ItemStack item = new ItemStack(Items.LEATHER);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Quiver\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"runes \",\"color\":\"light_purple\"},{\"text\":\"engraved \",\"color\":\"dark_aqua\"},{\"text\":\"upon the \"},{\"text\":\"quiver \",\"color\":\"light_purple\"},{\"text\":\"hum in the presence of \"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Runic Arrows\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" placed within the \",\"color\":\"dark_purple\"},{\"text\":\"quiver \"},{\"text\":\"regenerate \",\"color\":\"dark_aqua\"},{\"text\":\"over \",\"color\":\"dark_purple\"},{\"text\":\"time\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Arrows \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"do not take \",\"color\":\"dark_purple\"},{\"text\":\"concentration \",\"color\":\"dark_aqua\"},{\"text\":\"when in the \",\"color\":\"dark_purple\"},{\"text\":\"quiver\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to put \",\"color\":\"dark_purple\"},{\"text\":\"Arrows \",\"color\":\"light_purple\"},{\"text\":\"in the \",\"color\":\"dark_purple\"},{\"text\":\"quiver\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Left Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" with a \",\"color\":\"dark_purple\"},{\"text\":\"Runic Bow\",\"color\":\"light_purple\"},{\"text\":\" to swap which \",\"color\":\"dark_purple\"},{\"text\":\"Runic Arrow\",\"color\":\"light_purple\"},{\"text\":\" will be shot.\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      //setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   
   
   //TODO: Make Recipe
   private MagicItemRecipe makeRecipe(){
      return null;
   }
   
   //TODO: Make Lore
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"TODO\"}");
      return list;
   }
}
