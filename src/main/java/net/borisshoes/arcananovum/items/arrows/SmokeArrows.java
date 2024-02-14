package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.areaeffects.SmokeArrowAreaEffectTracker;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SmokeArrows extends RunicArrow {
   
   private static final String TXT = "item/runic_arrow";
   
   public SmokeArrows(){
      id = "smoke_arrows";
      name = "Smoke Arrows";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new SmokeArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Smoke\",\"italic\":false,\"color\":\"dark_gray\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",6908265);
      tag.putInt("HideFlags", 255);
      stack.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      stack.setNbt(addMagicNbt(tag));
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Smoke Arrows:\",\"italic\":false,\"color\":\"dark_gray\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" emit \"},{\"text\":\"smoke\",\"color\":\"dark_gray\"},{\"text\":\" particles near where they land.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Smoke\",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\" gives \",\"color\":\"gray\"},{\"text\":\"blindness\"},{\"text\":\" and \",\"color\":\"gray\"},{\"text\":\"weakness\"},{\"text\":\" to those inside it.\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      
      return loreList;
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         float range = (float) MathHelper.clamp(arrow.getVelocity().length()*.8,.3,2.5);
         int gasLvl = arrow.getAugment(ArcanaAugments.TEAR_GAS.id);
         ArcanaRegistry.AREA_EFFECTS.get(ArcanaRegistry.SMOKE_ARROW_AREA_EFFECT_TRACKER.getType()).addSource(SmokeArrowAreaEffectTracker.source(arrow.getOwner(),entityHitResult.getEntity(),null,null,range,gasLvl));
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         float range = (float) MathHelper.clamp(arrow.getVelocity().length()*.8,.3,2.5);
         int gasLvl = arrow.getAugment(ArcanaAugments.TEAR_GAS.id);
         ArcanaRegistry.AREA_EFFECTS.get(ArcanaRegistry.SMOKE_ARROW_AREA_EFFECT_TRACKER.getType()).addSource(SmokeArrowAreaEffectTracker.source(arrow.getOwner(),null,blockHitResult.getBlockPos(),serverWorld,range,gasLvl));
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.CAMPFIRE,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.GLOW_INK_SAC,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.INK_SAC,64,null);
      ItemStack potion10 = new ItemStack(Items.LINGERING_POTION);
      MagicItemIngredient k = new MagicItemIngredient(Items.LINGERING_POTION,1, PotionUtil.setPotion(potion10, Potions.LONG_WEAKNESS).getNbt());
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,i,a},
            {k,h,m,h,k},
            {a,i,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"     Smoke Arrows\\n\\nRarity: Empowered\\n\\nThis Runic Matrix has been configured to summon copious amounts of campfire smoke. Those inside will have trouble seeing, and even breathing, making it harder to land a solid blow.\"}");
      return list;
   }
   
   public class SmokeArrowsItem extends MagicPolymerArrowItem {
      public SmokeArrowsItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
