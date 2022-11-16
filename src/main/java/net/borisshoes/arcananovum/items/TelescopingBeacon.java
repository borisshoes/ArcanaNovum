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

public class TelescopingBeacon extends MagicItem {
   
   public TelescopingBeacon(){
      id = "telescoping_beacon";
      name = "Telescoping Beacon";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ITEMS, ArcaneTome.TomeFilter.BLOCKS};
      
      ItemStack item = new ItemStack(Items.BEACON);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Telescoping Beacon\",\"italic\":false,\"color\":\"aqua\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"beacon \",\"color\":\"aqua\"},{\"text\":\"automatically \",\"color\":\"blue\"},{\"text\":\"deploys a \"},{\"text\":\"fully powered\",\"color\":\"aqua\"},{\"text\":\" base when placed.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Using \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"the item again on a \",\"color\":\"dark_aqua\"},{\"text\":\"fully powered\",\"color\":\"aqua\"},{\"text\":\" base \",\"color\":\"dark_aqua\"},{\"text\":\"re-captures\"},{\"text\":\" the construct.\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"There must be \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"adequate space\",\"color\":\"aqua\"},{\"text\":\" to \"},{\"text\":\"deploy \",\"color\":\"blue\"},{\"text\":\"the \"},{\"text\":\"beacon\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Construct Status - \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Ready\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
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
