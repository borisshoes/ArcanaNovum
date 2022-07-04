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

public class FractalSponge extends MagicItem{
   
   public FractalSponge(){
      id = "fractal_sponge";
      name = "Fractal Sponge";
      rarity = MagicRarity.EMPOWERED;
   
      ItemStack item = new ItemStack(Items.SPONGE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Fractal Sponge\",\"italic\":false,\"bold\":true,\"color\":\"yellow\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Fractals \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"are known for having \",\"color\":\"blue\"},{\"text\":\"infinite \",\"color\":\"light_purple\"},{\"text\":\"surface area\"},{\"text\":\".\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"effectiveness\",\"color\":\"aqua\"},{\"text\":\" of a \"},{\"text\":\"sponge \",\"color\":\"yellow\"},{\"text\":\"is based on said \"},{\"text\":\"surface area\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"combination \",\"color\":\"aqua\"},{\"text\":\"of the two seems only \"},{\"text\":\"natural\",\"color\":\"dark_aqua\",\"italic\":true},{\"text\":\".\",\"color\":\"blue\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"The resulting \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"sponge \",\"color\":\"yellow\"},{\"text\":\"is \"},{\"text\":\"much more effective\",\"color\":\"aqua\"},{\"text\":\" than most \"},{\"text\":\"sponges\",\"color\":\"yellow\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It even works on \",\"italic\":true,\"color\":\"dark_aqua\"},{\"text\":\"lava\",\"color\":\"gold\"},{\"text\":\"!\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
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
