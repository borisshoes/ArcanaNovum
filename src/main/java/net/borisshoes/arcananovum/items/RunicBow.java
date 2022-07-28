package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
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
import net.minecraft.util.registry.Registry;

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
      stack.setNbt(newTag);
      return stack;
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the Bow
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = toolStack.getNbt();
      if(nbt != null && nbt.contains("Enchantments")){
         NbtList enchants = nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
         for(int i = 0; i < enchants.size(); i++){
            if(((NbtCompound)enchants.get(i)).getString("id").equals(Registry.ENCHANTMENT.getId(Enchantments.POWER).toString())){
               NbtCompound power = new NbtCompound();
               power.putString("id","power");
               power.putShort("lvl", (short) 7);
               enchants.set(i,power);
            }
         }
         newMagicItem.getOrCreateNbt().put("Enchantments",enchants);
      }
      return newMagicItem;
   }
   
   private MagicItemRecipe makeRecipe(){
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
      MagicItemIngredient c = new MagicItemIngredient(Items.AMETHYST_SHARD,64,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.BOW,1,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENCHANTED_BOOK,1, EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.POWER,5)).getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {c,s,e,s,c},
            {s,n,m,n,s},
            {e,m,b,m,e},
            {s,n,m,n,s},
            {c,s,e,s,c}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"       Runic Bow\\n\\nRarity: Legendary\\n\\nThe Runic Bow is truely a masterpiece of adaptive Arcana. The integrated Runic Matrices reconfigure the Bow's ethereal structure based on the projectile being fired to unlock its Arcane effects. \"}");
      list.add("{\"text\":\"       Runic Bow\\n\\nThe Runic Bow is capable of utilizing Runic Arrows and activating their special abilities. The Bow also enhances normal arrows to do more damage than a traditional enchanted bow as well as being incredibly durable.\"}");
      return list;
   }
}
