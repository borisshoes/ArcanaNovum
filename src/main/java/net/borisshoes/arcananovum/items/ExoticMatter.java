package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ExoticMatter extends EnergyItem implements UsableItem{
   public ExoticMatter(){
      id = "exotic_matter";
      name = "Exotic Matter";
      rarity = MagicRarity.NONE;
      maxEnergy = 600000; // Maximum seconds of chunk loading per exotic matter fuel (1 week)
      
      ItemStack item = new ItemStack(Items.STRUCTURE_BLOCK);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Exotic Matter\",\"italic\":false,\"color\":\"blue\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"strange matter\",\"color\":\"blue\"},{\"text\":\" seems to warp \"},{\"text\":\"spacetime\",\"color\":\"dark_purple\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Perhaps it could be \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"useful\",\"italic\":true,\"color\":\"gray\"},{\"text\":\" for something...\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Used as \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"fuel\",\"color\":\"gold\"},{\"text\":\" for the \"},{\"text\":\"Continuum Anchor\",\"color\":\"dark_blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Fuel - \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"600000\",\"color\":\"blue\"},{\"text\":\" Seconds\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mundane\",\"italic\":false,\"color\":\"gray\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      prefNBT.getCompound("arcananovum").putInt("energy",maxEnergy);
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
   
   public int useFuel(ItemStack item, int fuel){
      NbtCompound itemNbt = item.getNbt();
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
      
      int newFuel = MathHelper.clamp(getEnergy(item)-fuel, 0, maxEnergy);
      setEnergy(item,newFuel);
      loreList.set(4, NbtString.of("[{\"text\":\"Fuel - \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\""+newFuel+"\",\"color\":\"blue\"},{\"text\":\" Seconds\"}]"));
      return newFuel;
   }
   
   public void setFuel(ItemStack item, int fuel){
      NbtCompound itemNbt = item.getNbt();
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
   
      int newFuel = MathHelper.clamp(fuel, 0, maxEnergy);
      setEnergy(item,newFuel);
      loreList.set(4, NbtString.of("[{\"text\":\"Fuel - \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\""+newFuel+"\",\"color\":\"blue\"},{\"text\":\" Seconds\"}]"));
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"     Exotic Matter\\n\\nRarity: Mundane\\n\\nThe components of this matter seem to spontaneously generate low amounts of Arcana when combined, as well as a small warping in Spacetime. Perhaps this could be exploited further...\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient c = new MagicItemIngredient(Items.END_CRYSTAL,8,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.CRYING_OBSIDIAN,32,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,32,null);
      MagicItemIngredient d = new MagicItemIngredient(Items.DIAMOND,16,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      
      MagicItemIngredient[][] ingredients = {
            {p,d,o,c,p},
            {c,d,c,d,d},
            {o,c,n,c,o},
            {d,d,c,d,c},
            {p,c,o,d,p}};
      return new MagicItemRecipe(ingredients);
   }
}
