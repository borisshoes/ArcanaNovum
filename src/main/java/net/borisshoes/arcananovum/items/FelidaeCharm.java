package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FelidaeCharm extends MagicItem implements UsableItem{
   
   public FelidaeCharm(){
      id = "felidae_charm";
      name = "Charm of Felidae";
      rarity = MagicRarity.EMPOWERED;
   
      ItemStack item = new ItemStack(Items.STRING);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Charm of Felidae\",\"italic\":false,\"bold\":true,\"color\":\"yellow\"}]");
      loreList.add(NbtString.of("[{\"text\":\"The charm \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"purrs \",\"color\":\"yellow\"},{\"text\":\"softly when worn.\",\"color\":\"gold\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Keeping this \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"charm \",\"color\":\"yellow\"},{\"text\":\"on your person gives you \"},{\"text\":\"cat-like\",\"color\":\"gray\"},{\"text\":\" abilities.\",\"color\":\"gold\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Your \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"falls \",\"color\":\"gray\"},{\"text\":\"become somewhat \"},{\"text\":\"graceful \",\"color\":\"aqua\"},{\"text\":\"and \"},{\"text\":\"cushioned\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Creepers \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"and \",\"color\":\"gold\"},{\"text\":\"Phantoms \",\"color\":\"blue\"},{\"text\":\"give you a \",\"color\":\"gold\"},{\"text\":\"wide berth\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
   
      item.setNbt(prefNBT);
      prefItem = item;
   
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return false;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient m = new MagicItemIngredient(Items.PHANTOM_MEMBRANE,32,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.STRING,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.GUNPOWDER,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.TROPICAL_FISH,64,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.PUFFERFISH,64,null);
      MagicItemIngredient f = new MagicItemIngredient(Items.COD,64,null);
      MagicItemIngredient l = new MagicItemIngredient(Items.SALMON,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.CREEPER_HEAD,1,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.ENCHANTED_BOOK,1, EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.FEATHER_FALLING,4)).getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {m,s,g,s,m},
            {s,b,c,b,s},
            {g,l,h,p,g},
            {s,b,f,b,s},
            {m,s,g,s,m}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Charm of Felidae\\n\\nRarity: Empowered\\n\\nCats are quite powerful creatures, managing to frighten phantoms and scare creepers. They can even fall from any height without care.\\nThis Charm seeks to mimic a fraction of that power.\"}");
      list.add("{\"text\":\"   Charm of Felidae\\n\\nThe Charm halves all fall damage, stops phantoms from swooping the holder, and gives creepers a good scare every now and then.\"}");
      return list;
   }
}
