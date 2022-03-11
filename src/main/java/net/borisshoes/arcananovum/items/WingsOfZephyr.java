package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

public class WingsOfZephyr extends EnergyItem{
   public WingsOfZephyr(){
      id = "wings_of_zephyr";
      name = "Armored Wings of Zephyr";
      rarity = MagicRarity.MYTHICAL;
      maxEnergy = 10000; // Store up to 100 points of dmg mitigation at 5 seconds of flight per damage point stored
      // aka 100 ticks/energy per 1 dmg point
      
      ItemStack item = new ItemStack(Items.ELYTRA);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      NbtList attributes = new NbtList();
      NbtCompound prot = new NbtCompound();
      prot.putString("id","protection");
      prot.putInt("lvl",4);
      enchants.add(prot);
      display.putString("Name","[{\"text\":\"Armored Wings of Zephyr\",\"italic\":false,\"color\":\"gray\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Let these \",\"italic\":true,\"color\":\"white\"},{\"text\":\"gentle wings\",\"color\":\"gray\"},{\"text\":\" shield you from the dangers of the land.\"},{\"text\":\"\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These wings are equivalent to a \",\"italic\":false,\"color\":\"white\"},{\"text\":\"Netherite Chestplate\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"They also have\",\"italic\":false,\"color\":\"white\"},{\"text\":\" \",\"color\":\"gray\"},{\"text\":\"Protection 4\",\"color\":\"aqua\"},{\"text\":\" and are\"},{\"text\":\" \",\"color\":\"gray\"},{\"text\":\"Unbreakable\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mythical\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      NbtCompound kbRes = new NbtCompound();
      kbRes.putDouble("Amount",0.1);
      kbRes.putString("AttributeName","generic.knockback_resistance");
      kbRes.putString("Name","generic.knockback_resistance");
      kbRes.putString("Slot","chest");
      kbRes.putIntArray("UUID", new int[]{-122030, 92433, 23139, -184866});
      attributes.add(kbRes);
      NbtCompound toughness = new NbtCompound();
      toughness.putInt("Amount",3);
      toughness.putString("AttributeName","generic.armor_toughness");
      toughness.putString("Name","generic.armor_toughness");
      toughness.putString("Slot","chest");
      toughness.putIntArray("UUID", new int[]{-122030, 92533, 23139, -185066});
      attributes.add(toughness);
      NbtCompound armor = new NbtCompound();
      armor.putInt("Amount",8);
      armor.putString("AttributeName","generic.armor");
      armor.putString("Name","generic.armor");
      armor.putString("Slot","chest");
      armor.putIntArray("UUID", new int[]{-122030, 92633, 23139, -185266});
      attributes.add(armor);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.put("AttributeModifiers",attributes);
      tag.putInt("HideFlags",7);
      tag.putInt("Unbreakable",1);
   
      setBookLore(makeLore());
      prefNBT = addMagicNbt(tag);
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"     Armored Wings \\n       of Zephyr\\n\\nRarity: Mythical\\n\\nThe first discovered Mythical Item. They offer unsurmounted protection equivalent to the strongest of armor.\\n\\nThe Wings themselves are hardened to the \"}");
      list.add("{\"text\":\"     Armored Wings \\n       of Zephyr\\n\\npoint of being impervious to structural damage.\\n\\nAs study of them furthers a new ability has been discovered.\\nThe Wings collect and store kinetic energy when in flight than can be re-emitted when\"}");
      list.add("{\"text\":\"     Armored Wings \\n       of Zephyr\\n\\nthe wearer suffers a large impact.\\n\\nThis effect seems to negate up to half of all fall damage and kinetic damage taken as long as the Wings have stored enough energy.\"}");
      return list;
   }
}
