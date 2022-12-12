package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.RunicArrow;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class SiphoningArrows extends MagicItem implements RunicArrow {
   
   public SiphoningArrows(){
      id = "siphoning_arrows";
      name = "Siphoning Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
      
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Siphoning\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Siphoning Arrows:\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"red\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"siphon health \",\"color\":\"dark_red\"},{\"text\":\"from hit \"},{\"text\":\"entities\",\"color\":\"gold\"},{\"text\":\".\",\"color\":\"red\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The amount \",\"italic\":false,\"color\":\"red\"},{\"text\":\"siphoned \",\"color\":\"dark_red\"},{\"text\":\"is proportional to the \"},{\"text\":\"damage \",\"color\":\"gold\"},{\"text\":\"dealt.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",15866018);
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
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         double damage = MathHelper.ceil(MathHelper.clamp(arrow.getVelocity().length() * arrow.getDamage(), 0.0, 2.147483647E9)) / 5.5;
         damage += arrow.isCritical() ? damage/4 : 0;
         player.heal((float)damage);
         player.getWorld().spawnParticles(ParticleTypes.HEART,player.getX(),player.getY()+player.getHeight()/2,player.getZ(),(int)Math.ceil(damage), .5,.5,.5,1);
      }
   }
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult){}
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.GLISTERING_MELON_SLICE,64,null);
      ItemStack potion6 = new ItemStack(Items.POTION);
      MagicItemIngredient g = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion6, Potions.STRONG_HEALING).getNbt());
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      ItemStack potion8 = new ItemStack(Items.POTION);
      MagicItemIngredient i = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion8, Potions.STRONG_HARMING).getNbt());
      MagicItemIngredient k = new MagicItemIngredient(Items.FERMENTED_SPIDER_EYE,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,i,a},
            {k,h,m,h,k},
            {a,i,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Siphoning Arrows\\n\\nRarity: Exotic\\n\\nLife force is something I have rarely explored. I've invoked some simple life runes to draw upon the health lost from my arrows and channel it back to me.\"}");
      return list;
   }
}
