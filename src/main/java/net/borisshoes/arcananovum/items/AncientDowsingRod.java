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

public class AncientDowsingRod extends MagicItem{
   
   public AncientDowsingRod(){
      id = "ancient_dowsing_rod";
      name = "Ancient Dowsing Rod";
      rarity = MagicRarity.EMPOWERED;
   
      ItemStack item = new ItemStack(Items.BLAZE_ROD);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Ancient Dowsing Rod\",\"italic\":false,\"bold\":true,\"color\":\"dark_red\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Ancient civilizations\",\"italic\":false,\"color\":\"gold\"},{\"text\":\" in the \",\"color\":\"red\"},{\"text\":\"nether \",\"color\":\"dark_red\"},{\"text\":\"had ways of finding \",\"color\":\"red\"},{\"text\":\"netherite\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"red\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"red\"},{\"text\":\"dowsing rod \",\"color\":\"dark_red\"},{\"text\":\"is based on \"},{\"text\":\"ancient designs\",\"color\":\"gold\"},{\"text\":\" to locate \"},{\"text\":\"netherite scrap\",\"color\":\"dark_red\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"gold\"},{\"text\":\" to search for \",\"color\":\"red\"},{\"text\":\"ancient debris\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"red\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
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
