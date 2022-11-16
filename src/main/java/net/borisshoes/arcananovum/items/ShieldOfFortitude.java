package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

public class ShieldOfFortitude extends MagicItem {
   public ShieldOfFortitude(){
      id = "shield_of_fortitude";
      name = "Shield of Fortitude";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.EQUIPMENT};
      
      ItemStack item = new ItemStack(Items.SHIELD);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Shield of Fortitude\",\"italic\":false,\"bold\":true,\"color\":\"aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"This shield is \",\"italic\":false},{\"text\":\"overflowing\",\"italic\":true,\"color\":\"light_purple\"},{\"text\":\" with powerful \",\"color\":\"dark_purple\"},{\"text\":\"defensive\",\"color\":\"blue\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"magic\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Your will for \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"protection\",\"color\":\"aqua\"},{\"text\":\" becomes a tangible \"},{\"text\":\"fortitude\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Damage\",\"italic\":false,\"color\":\"red\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"blocked\",\"color\":\"blue\"},{\"text\":\" becomes \",\"color\":\"dark_purple\"},{\"text\":\"absorption\",\"color\":\"yellow\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"hearts\",\"color\":\"yellow\"},{\"text\":\" and the shield is \",\"color\":\"dark_purple\"},{\"text\":\"unbreakable\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary\",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags",103);
      tag.putInt("Unbreakable",1);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      // For default just replace everything but UUID
      NbtCompound newTag = prefNBT.copy();
      newTag.getCompound("arcananovum").putString("UUID",magicTag.getString("UUID"));
      NbtList enchants = itemNbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
      newTag.put("Enchantments",enchants);
      if(itemNbt.contains("BlockEntityTag"))
         newTag.put("BlockEntityTag",itemNbt.getCompound("BlockEntityTag"));
      stack.setNbt(newTag);
      return stack;
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the Sword
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = toolStack.getNbt();
      if(nbt != null){
         if(nbt.contains("Enchantments")){
            NbtList enchants = nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
            newMagicItem.getOrCreateNbt().put("Enchantments",enchants);
         }
         if(nbt.contains("BlockEntityTag")){
            newMagicItem.getOrCreateNbt().put("BlockEntityTag", nbt.getCompound("BlockEntityTag"));
         }
      }
      return newMagicItem;
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Shield of Fortitude\\n\\nRarity: Legendary\\n\\nTaking after the Wings of Zephyr I have successfully recreated their incredible durability. \\n\\nWhile keeping with the protective nature of the Wings I have been able to infuse extra\\n\"}");
      list.add("{\"text\":\"  Shield of Fortitude\\n\\nArcana into the four basic protection enchantments, fusing them with the ability of golden apples that grants the consumer a protective barrier.\\n\\nAs a result half of all damage blocked becomes a barrier lasting 10 seconds.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient s = new MagicItemIngredient(Items.SHIELD,1,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.GOLDEN_APPLE,64,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.PROTECTION,4)).getNbt());
      MagicItemIngredient j = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.PROJECTILE_PROTECTION,4)).getNbt());
      MagicItemIngredient b = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.BLAST_PROTECTION,4)).getNbt());
      MagicItemIngredient f = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.FIRE_PROTECTION,4)).getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {n,o,g,o,n},
            {o,i,b,i,o},
            {g,f,s,j,g},
            {o,i,p,i,o},
            {n,o,g,o,n}};
      return new MagicItemRecipe(ingredients);
   }
}
