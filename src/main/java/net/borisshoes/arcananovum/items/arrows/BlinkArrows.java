package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlinkArrows extends RunicArrow {
   
   private static final int[] phaseDur = {0,20,60,100};
   
   public BlinkArrows(){
      id = "blink_arrows";
      name = "Blink Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new BlinkArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
   
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Blink\",\"italic\":false,\"bold\":true,\"color\":\"dark_aqua\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",1404502);
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
      loreList.add(NbtString.of("[{\"text\":\"Blink Arrows:\",\"italic\":false,\"color\":\"dark_aqua\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" take after \"},{\"text\":\"Ender Pearls\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"dark_green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Upon \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"impact \",\"color\":\"green\"},{\"text\":\"or \"},{\"text\":\"hitting a target\",\"color\":\"green\"},{\"text\":\" you get \"},{\"text\":\"teleported\",\"color\":\"dark_aqua\"},{\"text\":\" to the \"},{\"text\":\"arrow\",\"color\":\"light_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         Vec3d tpPos = entityHitResult.getPos();
         if(tpPos.distanceTo(player.getPos()) >= 100) ArcanaAchievements.grant(player,ArcanaAchievements.NOW_YOU_SEE_ME.id);
         player.teleport(player.getServerWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,player.getYaw(),player.getPitch());
         ParticleEffectUtils.blinkArrowTp(player.getServerWorld(),player.getPos());
         SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS,.8f,.9f);
         
         int phaseLvl = arrow.getAugment(ArcanaAugments.PHASE_IN.id);
         StatusEffectInstance invuln = new StatusEffectInstance(StatusEffects.RESISTANCE,phaseDur[phaseLvl], 3, false, false, true);
         player.addStatusEffect(invuln);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         Vec3d offset = new Vec3d(blockHitResult.getSide().getUnitVector());
         Vec3d tpPos = blockHitResult.getPos().add(offset);
         if(tpPos.distanceTo(player.getPos()) >= 100) ArcanaAchievements.grant(player,ArcanaAchievements.NOW_YOU_SEE_ME.id);
         player.teleport(player.getServerWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,player.getYaw(),player.getPitch());
         ParticleEffectUtils.blinkArrowTp(player.getServerWorld(),player.getPos());
         SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS,.8f,.9f);
   
         int phaseLvl = arrow.getAugment(ArcanaAugments.PHASE_IN.id);
         StatusEffectInstance invuln = new StatusEffectInstance(StatusEffects.RESISTANCE,phaseDur[phaseLvl], 3, false, false, true);
         player.addStatusEffect(invuln);
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient s = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.ENDER_PEARL,16,null);
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,p,a,a},
            {a,p,s,p,a},
            {p,s,m,s,p},
            {a,p,s,p,a},
            {a,a,p,a,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Blink Arrows\\n\\nRarity: Exotic\\n\\nThe Runic Matrix has been configured for invoking teleportation spells, and now the arrows act like a thrown Ender Pearl when activated by a Runic Bow.\"}");
      return list;
   }
   
   public class BlinkArrowsItem extends MagicPolymerArrowItem {
      public BlinkArrowsItem(Settings settings){
         super(getThis(),settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
