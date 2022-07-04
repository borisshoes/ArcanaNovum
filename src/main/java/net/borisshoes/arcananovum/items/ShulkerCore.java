package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

public class ShulkerCore extends MagicItem{
   
   public ShulkerCore(){
      id = "shulker_core";
      name = "Shulker Core";
      rarity = MagicRarity.EXOTIC;
   
      ItemStack item = new ItemStack(Items.SHULKER_BOX);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Shulker Core\",\"italic\":false,\"color\":\"#ffff99\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Shulkers \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"possess a \",\"color\":\"gray\"},{\"text\":\"unique \",\"color\":\"dark_purple\"},{\"text\":\"ability to defy \",\"color\":\"gray\"},{\"text\":\"gravity\",\"color\":\"white\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"core \",\"color\":\"dark_purple\"},{\"text\":\"has \"},{\"text\":\"harnessed\",\"color\":\"yellow\"},{\"text\":\" that ability to allow \"},{\"text\":\"controlled\",\"color\":\"yellow\"},{\"text\":\" \"},{\"text\":\"levitation\",\"color\":\"white\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" to grant \",\"color\":\"gray\"},{\"text\":\"levitation\",\"color\":\"white\"},{\"text\":\".\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" to change the \",\"color\":\"gray\"},{\"text\":\"speed \",\"color\":\"dark_aqua\"},{\"text\":\"of \",\"color\":\"gray\"},{\"text\":\"levitation\",\"color\":\"white\"},{\"text\":\".\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      //setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
   
      item.setNbt(prefNBT);
      prefItem = item;
   
   }
   
   private MagicItemRecipe makeRecipe(){
      //TODO make recipe
      return null;
   }
   
   private List<String> makeLore(){
      //TODO make lore
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" TODO \"}");
      return list;
   }
}
