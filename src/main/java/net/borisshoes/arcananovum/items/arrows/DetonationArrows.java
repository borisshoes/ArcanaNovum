package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerArrowItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DetonationArrows extends RunicArrow {
   
   public DetonationArrows(){
      id = "detonation_arrows";
      name = "Detonation Arrows";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new DetonationArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Detonation\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",11035949);
      tag.putInt("HideFlags", 255);
      stack.setCount(64);
      buildItemLore(stack, ArcanaNovum.SERVER);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Detonation Arrows:\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"explode\",\"color\":\"red\"},{\"text\":\" on impact \"},{\"text\":\"destroying\",\"color\":\"red\"},{\"text\":\" nearby terrain.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The\",\"italic\":false,\"color\":\"gold\"},{\"text\":\" \"},{\"text\":\"explosion\",\"color\":\"red\"},{\"text\":\" does \"},{\"text\":\"reduced damage\",\"color\":\"red\"},{\"text\":\" to \"},{\"text\":\"players\",\"color\":\"red\"},{\"text\":\".\"}]"));
      
      return loreList;
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int blastLvl = arrow.getAugment(ArcanaAugments.BLAST_MINE.id);
      int personLvl = arrow.getAugment(ArcanaAugments.ANTI_PERSONNEL.id);
      explode(arrow,entityHitResult.getPos(),blastLvl,personLvl);
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      int blastLvl = arrow.getAugment(ArcanaAugments.BLAST_MINE.id);
      int personLvl = arrow.getAugment(ArcanaAugments.ANTI_PERSONNEL.id);
      explode(arrow,blockHitResult.getPos(),blastLvl,personLvl);
   }
   
   private void explode(PersistentProjectileEntity arrow, Vec3d pos, int blastLvl, int personLvl){
      double power = MathHelper.clamp(2*arrow.getVelocity().length(),1.5,8);
      DamageSource source1 = ArcanaDamageTypes.of(arrow.getWorld(),ArcanaDamageTypes.DETONATION_TERRAIN,arrow,arrow.getOwner());
      DamageSource source2 = ArcanaDamageTypes.of(arrow.getWorld(),ArcanaDamageTypes.DETONATION_DAMAGE,arrow,arrow.getOwner());
      if(personLvl != 3){ // Terrain explosion except when personnel lvl 3
         arrow.getEntityWorld().createExplosion(null, source1, null,pos.x,pos.y,pos.z,(float)(power*(1+.4*blastLvl)),false, World.ExplosionSourceType.TNT);
      }
      if(blastLvl != 3){ // Damage explosion except when blast lvl 3
         arrow.getEntityWorld().createExplosion(null, source2, null,pos.x,pos.y,pos.z,(float)(power*0.75*(1+.25*personLvl)),false, World.ExplosionSourceType.NONE);
      }
      
      
      
      arrow.discard();
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.TNT,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,c,h,c,a},
            {c,h,m,h,c},
            {a,c,h,c,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery());
   
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Detonation Arrows\\n\\nRarity: Empowered\\n\\nThis Runic Matrix has been stuffed full of volatile Arcana, ready to blow at the slightest impact. \\nHowever, the blast seems to effect terrain slightly more than creatures.\"}");
      return list;
   }
   
   public class DetonationArrowsItem extends MagicPolymerArrowItem {
      public DetonationArrowsItem(Settings settings){
         super(getThis(),settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
