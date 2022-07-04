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

public class ShadowStalkersGlaive extends MagicItem{
   public ShadowStalkersGlaive(){
      id = "shadow_stalkers_glaive";
      name = "Shadow Stalkers Glaive";
      rarity = MagicRarity.LEGENDARY;
   
      ItemStack item = new ItemStack(Items.NETHERITE_SWORD);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Shadow Stalker's Glaive\",\"italic\":false,\"bold\":true,\"color\":\"#222222\"}]");
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"blade \",\"color\":\"gray\"},{\"text\":\"lets you move through your opponents \"},{\"text\":\"shadow\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"blade \",\"color\":\"gray\"},{\"text\":\"stores the \"},{\"text\":\"blood \",\"color\":\"dark_red\"},{\"text\":\"from every strike and uses it as \"},{\"text\":\"energy\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Stride \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"through the \",\"color\":\"dark_gray\"},{\"text\":\"darkness \",\"color\":\"blue\"},{\"text\":\"behind your opponent or \",\"color\":\"dark_gray\"},{\"text\":\"blink forward\"},{\"text\":\".\",\"color\":\"dark_gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"gray\"},{\"text\":\" to \",\"color\":\"dark_gray\"},{\"text\":\"teleport \",\"color\":\"dark_aqua\"},{\"text\":\"behind \",\"color\":\"blue\"},{\"text\":\"your most recently attacked foe.\",\"color\":\"dark_gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"gray\"},{\"text\":\" to \",\"color\":\"dark_gray\"},{\"text\":\"teleport \",\"color\":\"dark_aqua\"},{\"text\":\"a \",\"color\":\"dark_gray\"},{\"text\":\"short distance\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      //TODO Weapon stats n stuff
   
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
