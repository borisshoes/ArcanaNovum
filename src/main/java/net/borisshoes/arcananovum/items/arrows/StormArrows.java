package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.RunicArrow;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class StormArrows extends MagicItem implements RunicArrow {
   
   public StormArrows(){
      id = "storm_arrows";
      name = "Storm Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
      
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Storm\",\"italic\":false,\"color\":\"gray\",\"bold\":true}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Storm Arrows:\",\"italic\":false,\"color\":\"gray\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"gray\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" channel \"},{\"text\":\"lightning \",\"color\":\"yellow\"},{\"text\":\"from the \"},{\"text\":\"clouds \",\"color\":\"white\"},{\"text\":\"above.\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Only a \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"small chance\",\"color\":\"yellow\"},{\"text\":\" to work when not \"},{\"text\":\"raining\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",12040354);
      tag.putInt("HideFlags",127);
      item.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult){
      strike(arrow,entityHitResult.getPos());
   }
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult){
      strike(arrow,blockHitResult.getPos());
   }
   
   private void strike(PersistentProjectileEntity arrow, Vec3d pos){
      World world = arrow.getEntityWorld();
      if(world.isRaining() || world.isThundering() || Math.random() < .1){
         LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, arrow.getEntityWorld());
         lightning.setPosition(pos);
         world.spawnEntity(lightning);
         if(world instanceof ServerWorld serverWorld && !world.isRaining() && !world.isThundering()){
            serverWorld.setWeather(0, 1200, true, false);
         }
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.LIGHTNING_ROD,64,null);
      ItemStack enchantedBook6 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook6,new EnchantmentLevelEntry(Enchantments.CHANNELING,1));
      MagicItemIngredient g = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook6.getNbt());
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {c,h,m,h,c},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"     Storm Arrows\\n\\nRarity: Exotic\\n\\nThe channeling enchantment requires a storm to use. Throwing a bit of Arcana into it seems to force a storm, abeit briefly. The Matrix doesn't always seem to succeed in activating though.\"}");
      return list;
   }
}
