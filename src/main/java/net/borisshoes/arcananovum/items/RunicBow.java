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

public class RunicBow extends MagicItem{
   
   public RunicBow(){
      id = "runic_bow";
      name = "Runic Bow";
      rarity = MagicRarity.LEGENDARY;
   
      ItemStack item = new ItemStack(Items.BOW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      NbtCompound power = new NbtCompound();
      power.putString("id","power");
      power.putInt("lvl",7);
      enchants.add(power);
      display.putString("Name","[{\"text\":\"Runic Bow\",\"italic\":false,\"bold\":true,\"color\":\"light_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false},{\"text\":\"Runic Bow\",\"color\":\"light_purple\"},{\"text\":\" makes use of the \",\"color\":\"dark_purple\"},{\"text\":\"Runic Matrix\",\"color\":\"light_purple\"},{\"text\":\" to create \",\"color\":\"dark_purple\"},{\"text\":\"unique effects\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Runic Bow\",\"color\":\"light_purple\"},{\"text\":\" can fire and \"},{\"text\":\"activate\",\"italic\":true,\"color\":\"dark_aqua\"},{\"text\":\" \",\"italic\":true},{\"text\":\"the effects of \"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"bow\",\"color\":\"light_purple\"},{\"text\":\" also acts as a \"},{\"text\":\"normal bow\",\"color\":\"yellow\"},{\"text\":\" with \"},{\"text\":\"Power VII\",\"color\":\"aqua\"},{\"text\":\" and is \"},{\"text\":\"unbreakable\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags",7);
      tag.putInt("Unbreakable",1);
   
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
