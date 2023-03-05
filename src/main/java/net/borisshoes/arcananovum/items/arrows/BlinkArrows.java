package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
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
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class BlinkArrows extends MagicItem implements RunicArrow {
   
   private static final int[] phaseDur = {0,20,60,100};
   
   public BlinkArrows(){
      id = "blink_arrows";
      name = "Blink Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
   
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Blink\",\"italic\":false,\"bold\":true,\"color\":\"dark_aqua\"}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Blink Arrows:\",\"italic\":false,\"color\":\"dark_aqua\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" take after \"},{\"text\":\"Ender Pearls\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"dark_green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Upon \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"impact \",\"color\":\"green\"},{\"text\":\"or \"},{\"text\":\"hitting a target\",\"color\":\"green\"},{\"text\":\" you get \"},{\"text\":\"teleported\",\"color\":\"dark_aqua\"},{\"text\":\" to the \"},{\"text\":\"arrow\",\"color\":\"light_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",1404502);
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
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         Vec3d tpPos = entityHitResult.getPos();
         if(tpPos.distanceTo(player.getPos()) >= 100) ArcanaAchievements.grant(player,"now_you_see_me");
         player.teleport(player.getWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,player.getYaw(),player.getPitch());
         ParticleEffectUtils.blinkArrowTp(player.getWorld(),player.getPos());
         SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS,.8f,.9f);
         
         int phaseLvl = Math.max(0, ArcanaAugments.getAugmentFromCompound(magicEntity.getData(),"phase_in"));
         StatusEffectInstance invuln = new StatusEffectInstance(StatusEffects.RESISTANCE,phaseDur[phaseLvl], 4, false, false, true);
         player.addStatusEffect(invuln);
      }
   }
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult, MagicEntity magicEntity){
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         Vec3d offset = new Vec3d(blockHitResult.getSide().getUnitVector());
         Vec3d tpPos = blockHitResult.getPos().add(offset);
         if(tpPos.distanceTo(player.getPos()) >= 100) ArcanaAchievements.grant(player,"now_you_see_me");
         player.teleport(player.getWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,player.getYaw(),player.getPitch());
         ParticleEffectUtils.blinkArrowTp(player.getWorld(),player.getPos());
         SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS,.8f,.9f);
   
         int phaseLvl = Math.max(0, ArcanaAugments.getAugmentFromCompound(magicEntity.getData(),"phase_in"));
         StatusEffectInstance invuln = new StatusEffectInstance(StatusEffects.RESISTANCE,phaseDur[phaseLvl], 4, false, false, true);
         player.addStatusEffect(invuln);
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient s = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.ENDER_PEARL,16,null);
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,p,a,a},
            {a,p,s,p,a},
            {p,s,m,s,p},
            {a,p,s,p,a},
            {a,a,p,a,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Blink Arrows\\n\\nRarity: Exotic\\n\\nThe Runic Matrix has been configured for invoking teleportation spells, and now the arrows act like a thrown Ender Pearl when activated by a Runic Bow.\"}");
      return list;
   }
}
