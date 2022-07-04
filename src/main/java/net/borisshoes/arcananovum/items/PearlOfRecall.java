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

public class PearlOfRecall extends MagicItem{
   
   public PearlOfRecall(){
      id = "pearl_of_recall";
      name = "Pearl of Recall";
      rarity = MagicRarity.EXOTIC;
   
      ItemStack item = new ItemStack(Items.ENDER_EYE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Pearl of Recall\",\"italic\":false,\"bold\":true,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"An \",\"italic\":false,\"color\":\"green\"},{\"text\":\"Ender Pearl\",\"color\":\"dark_aqua\"},{\"text\":\" whose \"},{\"text\":\"moment \",\"color\":\"blue\"},{\"text\":\"of \"},{\"text\":\"activation \",\"color\":\"dark_green\"},{\"text\":\"was \"},{\"text\":\"frozen \",\"color\":\"aqua\"},{\"text\":\"for later use.\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It requires the \",\"italic\":false,\"color\":\"green\"},{\"text\":\"flowing of time\",\"color\":\"blue\"},{\"text\":\" \",\"color\":\"blue\"},{\"text\":\"to \"},{\"text\":\"recharge \",\"color\":\"aqua\"},{\"text\":\"it.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to set its \",\"color\":\"green\"},{\"text\":\"location \",\"color\":\"light_purple\"},{\"text\":\"and \",\"color\":\"green\"},{\"text\":\"to \",\"color\":\"green\"},{\"text\":\"teleport \",\"color\":\"dark_green\"},{\"text\":\"to its \",\"color\":\"green\"},{\"text\":\"set point\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Location - \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"Unbound\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Charged - \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"100%\",\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
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
