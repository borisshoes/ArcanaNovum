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

public class SpawnerInfuser extends MagicItem {
   
   public SpawnerInfuser(){
      id = "spawner_infuser";
      name = "Spawner Infuser";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.BLOCKS};
      
      ItemStack item = new ItemStack(Items.SCULK_SHRIEKER);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Spawner Infuser\",\"italic\":false,\"color\":\"dark_green\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Spawners\",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\" have their \",\"color\":\"dark_aqua\"},{\"text\":\"natural limit\",\"color\":\"yellow\"},{\"text\":\", \",\"color\":\"dark_aqua\"},{\"text\":\"Arcana\",\"color\":\"dark_purple\"},{\"text\":\" can now push it \",\"color\":\"dark_aqua\"},{\"text\":\"further\",\"color\":\"yellow\",\"italic\":true},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The Infuser\",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\" is to be placed two blocks \",\"color\":\"dark_aqua\"},{\"text\":\"below \",\"color\":\"yellow\"},{\"text\":\"a \",\"color\":\"dark_aqua\"},{\"text\":\"spawner\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When given a \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"soulstone\",\"color\":\"dark_red\"},{\"text\":\" of the \"},{\"text\":\"same type\",\"color\":\"yellow\"},{\"text\":\" as the \"},{\"text\":\"spawner \",\"color\":\"dark_green\"},{\"text\":\"it can be \"},{\"text\":\"activated\",\"color\":\"green\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"A \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"redstone \",\"color\":\"red\"},{\"text\":\"signal \"},{\"text\":\"activates \",\"color\":\"green\"},{\"text\":\"the \"},{\"text\":\"infuser\",\"color\":\"dark_green\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\" the \",\"color\":\"dark_aqua\"},{\"text\":\"Infuser \"},{\"text\":\"to \",\"color\":\"dark_aqua\"},{\"text\":\"configure \",\"color\":\"green\"},{\"text\":\"the \",\"color\":\"dark_aqua\"},{\"text\":\"infusion \"},{\"text\":\"type\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
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
