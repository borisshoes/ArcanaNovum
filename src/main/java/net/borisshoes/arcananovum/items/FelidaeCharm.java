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

public class FelidaeCharm extends MagicItem{
   
   public FelidaeCharm(){
      id = "felidae_charm";
      name = "Charm of Felidae";
      rarity = MagicRarity.EMPOWERED;
   
      ItemStack item = new ItemStack(Items.STRING);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Charm of Felidae\",\"italic\":false,\"bold\":true,\"color\":\"yellow\"}]");
      loreList.add(NbtString.of("[{\"text\":\"The charm \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"purrs \",\"color\":\"yellow\"},{\"text\":\"softly when worn.\",\"color\":\"gold\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Keeping this \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"charm \",\"color\":\"yellow\"},{\"text\":\"on your person gives you \"},{\"text\":\"cat-like\",\"color\":\"gray\"},{\"text\":\" abilities.\",\"color\":\"gold\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Your \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"falls \",\"color\":\"gray\"},{\"text\":\"become somewhat \"},{\"text\":\"graceful \",\"color\":\"aqua\"},{\"text\":\"and \"},{\"text\":\"cushioned\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Creepers \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"and \",\"color\":\"gold\"},{\"text\":\"Phantoms \",\"color\":\"blue\"},{\"text\":\"give you a \",\"color\":\"gold\"},{\"text\":\"wide berth\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
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
