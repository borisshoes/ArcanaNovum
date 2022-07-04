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

public class StasisPearl extends MagicItem{
   
   public StasisPearl(){
      id = "stasis_pearl";
      name = "Stasis Pearl";
      rarity = MagicRarity.EXOTIC;
   
      ItemStack item = new ItemStack(Items.ENDER_PEARL);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Stasis Pearl\",\"italic\":false,\"color\":\"blue\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Ender Pearl\",\"color\":\"dark_aqua\"},{\"text\":\" has the ability to \"},{\"text\":\"freeze \",\"color\":\"aqua\"},{\"text\":\"its passage through \"},{\"text\":\"time\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"frozen \",\"color\":\"aqua\"},{\"text\":\"the \"},{\"text\":\"pearl \",\"color\":\"dark_aqua\"},{\"text\":\"looks like its '\"},{\"text\":\"hanging\",\"color\":\"aqua\",\"italic\":true},{\"text\":\"' in the air.\",\"color\":\"gray\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"It requires the \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"flowing of time\",\"color\":\"blue\"},{\"text\":\" to \"},{\"text\":\"recharge \",\"color\":\"dark_aqua\"},{\"text\":\"it.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to \",\"color\":\"gray\"},{\"text\":\"freeze \",\"color\":\"aqua\"},{\"text\":\"the \",\"color\":\"gray\"},{\"text\":\"pearl \",\"color\":\"dark_aqua\"},{\"text\":\"in flight.\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click again\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to \",\"color\":\"gray\"},{\"text\":\"release \",\"color\":\"aqua\"},{\"text\":\"the \",\"color\":\"gray\"},{\"text\":\"pearl\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
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
