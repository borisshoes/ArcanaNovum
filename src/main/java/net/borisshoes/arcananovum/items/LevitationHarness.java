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

public class LevitationHarness extends MagicItem{
   
   public LevitationHarness(){
      id = "levitation_harness";
      name = "Levitation Harness";
      rarity = MagicRarity.LEGENDARY;
   
      ItemStack item = new ItemStack(Items.LEATHER_CHESTPLATE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Levitation Harness\",\"italic\":false,\"color\":\"gray\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Mastery over the nature of \",\"italic\":false,\"color\":\"white\"},{\"text\":\"Shulkers \",\"color\":\"yellow\"},{\"text\":\"has yielded the \"},{\"text\":\"Levitation Harness!\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Grants \",\"italic\":false,\"color\":\"white\"},{\"text\":\"creative flight\",\"color\":\"aqua\"},{\"text\":\" while consuming \"},{\"text\":\"Shulker \",\"color\":\"yellow\"},{\"text\":\"souls \",\"color\":\"dark_red\"},{\"text\":\"and \"},{\"text\":\"Glowstone\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"white\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"white\"},{\"text\":\"Harness \",\"color\":\"gray\"},{\"text\":\"is quite \"},{\"text\":\"fragile \",\"color\":\"yellow\"},{\"text\":\"and the slightest bump causes it to \"},{\"text\":\"stall\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"white\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"white\"},{\"text\":\"Harness \",\"color\":\"gray\"},{\"text\":\"also provides \"},{\"text\":\"no protection\",\"color\":\"dark_red\"},{\"text\":\" against \"},{\"text\":\"damage\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"blue\"},{\"text\":\" while \",\"color\":\"white\"},{\"text\":\"holding \",\"color\":\"aqua\"},{\"text\":\"the \",\"color\":\"white\"},{\"text\":\"harness \",\"color\":\"gray\"},{\"text\":\"to open the refuelling menu.\",\"color\":\"white\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      display.putInt("color",9857430);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags",100);
      tag.putInt("Unbreakable",1);
      //TODO Armor Values n stuff
   
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
