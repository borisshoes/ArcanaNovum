package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.RunicArrow;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;

import java.util.ArrayList;
import java.util.List;

public class DetonationArrows extends MagicItem implements RunicArrow {
   
   public DetonationArrows(){
      id = "detonation_arrows";
      name = "Detonation Arrows";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ARROWS};
      
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Detonation\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Detonation Arrows:\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"explode\",\"color\":\"red\"},{\"text\":\" on impact \"},{\"text\":\"destroying\",\"color\":\"red\"},{\"text\":\" nearby terrain.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gold\"},{\"text\":\" \"},{\"text\":\"explosion\",\"color\":\"red\"},{\"text\":\" does \"},{\"text\":\"reduced damage\",\"color\":\"red\"},{\"text\":\" to \"},{\"text\":\"players\",\"color\":\"red\"},{\"text\":\".\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",11035949);
      tag.putInt("HideFlags",127);
      item.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult, MagicEntity magicEntity){
      int blastLvl = Math.max(0, ArcanaAugments.getAugmentFromCompound(magicEntity.getData(),"blast_mine"));
      int personLvl = Math.max(0, ArcanaAugments.getAugmentFromCompound(magicEntity.getData(),"anti_personnel"));
      explode(arrow,entityHitResult.getPos(),blastLvl,personLvl);
   }
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult, MagicEntity magicEntity){
      int blastLvl = Math.max(0, ArcanaAugments.getAugmentFromCompound(magicEntity.getData(),"blast_mine"));
      int personLvl = Math.max(0, ArcanaAugments.getAugmentFromCompound(magicEntity.getData(),"anti_personnel"));
      explode(arrow,blockHitResult.getPos(),blastLvl,personLvl);
   }
   
   private void explode(PersistentProjectileEntity arrow, Vec3d pos, int blastLvl, int personLvl){
      double power = MathHelper.clamp(2*arrow.getVelocity().length(),1.5,8);
      DamageSource source1 = DamageSource.GENERIC;
      DamageSource source2 = DamageSource.GENERIC;
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         source1 = (new EntityDamageSource("explosion.player.ArcanaNovum.DetonationArrows.Terrain"+blastLvl+"-"+personLvl, player)).setScaledWithDifficulty().setExplosive();
         source2 = (new EntityDamageSource("explosion.player.ArcanaNovum.DetonationArrows.Damage"+blastLvl+"-"+personLvl, player)).setScaledWithDifficulty().setExplosive();
      }
      if(personLvl < 3) arrow.getEntityWorld().createExplosion(null, source1, null,pos.x,pos.y,pos.z,(float)(power*(1+.4*blastLvl)),power > 7.5, Explosion.DestructionType.BREAK);
      arrow.getEntityWorld().createExplosion(null, source2, null,pos.x,pos.y,pos.z,(float)(power/2.0),power > 7.5, Explosion.DestructionType.NONE);
      
      arrow.discard();
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.TNT,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,c,h,c,a},
            {c,h,m,h,c},
            {a,c,h,c,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients);
   
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Detonation Arrows\\n\\nRarity: Empowered\\n\\nThis Runic Matrix has been stuffed full of volatile Arcana, ready to blow at the slightest impact. \\nHowever, the blast seems to effect terrain slightly more than creatures.\"}");
      return list;
   }
}
