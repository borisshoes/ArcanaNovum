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

public class TemporalMoment extends MagicItem{
   public TemporalMoment(){
      id = "temporal_moment";
      name = "Temporal Moment";
      rarity = MagicRarity.NONE;
      
      ItemStack item = new ItemStack(Items.CLOCK);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Temporal Moment\",\"italic\":false,\"bold\":true,\"color\":\"dark_blue\"}]");
      loreList.add(NbtString.of("[{\"text\":\"A piece of \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"spacetime \",\"color\":\"dark_gray\"},{\"text\":\"collapsing\",\"color\":\"dark_aqua\",\"italic\":true},{\"text\":\" \",\"italic\":true},{\"text\":\"down to a single \"},{\"text\":\"moment\",\"color\":\"dark_blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"clock\",\"color\":\"aqua\"},{\"text\":\" itself is stuck between \"},{\"text\":\"one \",\"color\":\"dark_aqua\"},{\"text\":\"instant of \"},{\"text\":\"time\",\"color\":\"dark_blue\"},{\"text\":\" and \"},{\"text\":\"another\",\"color\":\"dark_aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"discovery\",\"color\":\"aqua\"},{\"text\":\" unlocks a whole \"},{\"text\":\"world\",\"color\":\"dark_gray\"},{\"text\":\" of \"},{\"text\":\"possibilites\",\"color\":\"dark_aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mundane \",\"italic\":false,\"color\":\"gray\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
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
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Temporal Moment\\n\\nRarity: Mundane\\n\\nTime always moves forwards, but its rate can be changed from fluxuations in spacetime. With enough energy perhaps it could be slowed to a halt, freezing a moment in time to use later.\"}");
      return list;
   }
}
