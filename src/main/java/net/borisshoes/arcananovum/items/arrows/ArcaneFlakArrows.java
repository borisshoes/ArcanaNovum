package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.RunicArrow;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
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
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ArcaneFlakArrows extends MagicItem implements RunicArrow {
   
   public static final int armTime = 5;
   
   public ArcaneFlakArrows(){
      id = "arcane_flak_arrows";
      name = "Arcane Flak Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
      
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Arcane Flak\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Arcane Flak Arrows:\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"explode\",\"color\":\"dark_aqua\"},{\"text\":\" when passing by a \"},{\"text\":\"flying creature\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Deals \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"increased damage\",\"color\":\"dark_aqua\"},{\"text\":\" to \"},{\"text\":\"airborne entities\",\"color\":\"light_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",7802273);
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
      detonate(arrow);
   }
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult){
      detonate(arrow);
   }
   
   public static void detonate(PersistentProjectileEntity arrow){
      double damageRange = 4;
      List<Entity> triggerTargets = arrow.getEntityWorld().getOtherEntities(arrow,arrow.getBoundingBox().expand(damageRange*2),
            e -> !e.isSpectator() && e.distanceTo(arrow) <= damageRange && e instanceof LivingEntity);
      for(Entity entity : triggerTargets){
         if(entity instanceof LivingEntity e){
            float damage = (float) MathHelper.clamp(arrow.getVelocity().length()*4,1,10);
            damage *= e.isOnGround() ? 0.5 : 3.5;
            damage *= e.distanceTo(arrow) > 3 ? 0.5 : 1;
            DamageSource source = arrow.getOwner() == null ? (new DamageSource("explosion.player")).setExplosive() : (new EntityDamageSource("explosion.player", arrow.getOwner())).setExplosive();
            e.damage(source,damage);
         }
      }
   
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         ParticleEffectUtils.arcaneFlakArrowDetonate(serverWorld,arrow.getPos(),0);
         SoundUtils.playSound(serverWorld,arrow.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS,1f,1f);
         SoundUtils.playSound(serverWorld,arrow.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.PLAYERS,1f,1f);
      }
      
      arrow.discard();
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.PHANTOM_MEMBRANE,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.TNT,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient k = new MagicItemIngredient(Items.GLOWSTONE_DUST,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {k,h,m,h,k},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" Arcane Flak Arrows\\n\\nRarity: Exotic\\n\\nPhantoms... Scourges of the night sky. I shall create a weapon that strikes fear into their undead hearts.\\nThese arrows detonate when near flying creatures doing massive bonus damage in a brilliant display.\"}");
      return list;
   }
}
