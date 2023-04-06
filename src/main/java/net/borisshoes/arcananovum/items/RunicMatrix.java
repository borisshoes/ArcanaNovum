package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RunicMatrix extends MagicItem implements UsableItem {
   public RunicMatrix(){
      id = "runic_matrix";
      name = "Runic Matrix";
      rarity = MagicRarity.MUNDANE;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MUNDANE, ArcaneTome.TomeFilter.ITEMS};
   
      ItemStack item = new ItemStack(Items.END_CRYSTAL);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Matrix\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Runes \",\"color\":\"light_purple\"},{\"text\":\"engraved on this \",\"color\":\"dark_purple\"},{\"text\":\"crystalline \",\"color\":\"aqua\"},{\"text\":\"structure\",\"color\":\"dark_purple\"},{\"text\":\" shift\",\"color\":\"blue\"},{\"text\":\" constantly.\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"They \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"slide\",\"color\":\"blue\"},{\"text\":\" to form different combinations of \"},{\"text\":\"runic \",\"color\":\"light_purple\"},{\"text\":\"equations\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"matrix \",\"color\":\"light_purple\"},{\"text\":\"allows for the \"},{\"text\":\"invocation \",\"color\":\"aqua\"},{\"text\":\"of many different \"},{\"text\":\"effects\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mundane\",\"italic\":false,\"color\":\"gray\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
   
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient c = new MagicItemIngredient(Items.END_CRYSTAL,8,null);
      MagicItemIngredient t = new MagicItemIngredient(Items.CRAFTING_TABLE,64,null);
      MagicItemIngredient a = new MagicItemIngredient(Items.AMETHYST_SHARD,16,null);
      MagicItemIngredient d = new MagicItemIngredient(Items.DIAMOND,8,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHER_STAR,1,null, true);
   
      MagicItemIngredient[][] ingredients = {
            {a,d,c,d,a},
            {d,t,a,t,d},
            {c,a,n,a,c},
            {d,t,a,t,d},
            {a,d,c,d,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Runic Matrix\\n\\nRarity: Mundane\\n\\nRunic language is often used to create magical effects. Being able to freely combine multiple runic words like in a crafting table results in a device capable of producing a vast number of arcane spells.\"}");
      return list;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      return true;
   }
}
