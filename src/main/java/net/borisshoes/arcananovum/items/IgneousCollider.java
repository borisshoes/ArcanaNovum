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

public class IgneousCollider extends MagicItem{

   public IgneousCollider(){
      id = "igneous_collider";
      name = "Igneous Collider";
      rarity = MagicRarity.EXOTIC;
   
      ItemStack item = new ItemStack(Items.LODESTONE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Igneous Collider\",\"italic\":false,\"bold\":true,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Mining \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"obsidian \",\"color\":\"dark_purple\"},{\"text\":\"is a pain, now this machine can do it \"},{\"text\":\"automatically\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Place \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"lava \",\"color\":\"gold\"},{\"text\":\"and \"},{\"text\":\"water \",\"color\":\"dark_blue\"},{\"text\":\"source blocks adjacent to the \"},{\"text\":\"Collider\",\"color\":\"dark_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Obsidian \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"will be \",\"color\":\"light_purple\"},{\"text\":\"spat out\",\"color\":\"dark_aqua\"},{\"text\":\" or into a \",\"color\":\"light_purple\"},{\"text\":\"chest \",\"color\":\"dark_aqua\"},{\"text\":\"above it \",\"color\":\"light_purple\"},{\"text\":\"every minute\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"If \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"four \",\"color\":\"blue\"},{\"text\":\"obsidian \",\"color\":\"dark_purple\"},{\"text\":\"surround it, a \"},{\"text\":\"crying obsidian\",\"color\":\"#660066\"},{\"text\":\" will be \"},{\"text\":\"spat out\",\"color\":\"dark_aqua\"},{\"text\":\" above it.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
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
