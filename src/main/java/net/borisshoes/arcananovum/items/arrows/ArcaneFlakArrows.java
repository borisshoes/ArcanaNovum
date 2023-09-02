package net.borisshoes.arcananovum.items.arrows;

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
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArcaneFlakArrows extends RunicArrow {
   
   public static final int armTime = 5;
   
   public ArcaneFlakArrows(){
      id = "arcane_flak_arrows";
      name = "Arcane Flak Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new ArcaneFlakArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
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
      tag.putInt("HideFlags", 255);
      stack.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      double radius = 4 + 1.25*arrow.getAugment(ArcanaAugments.AIRBURST.id);
      detonate(arrow,radius);
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      double radius = 4 + 1.25*arrow.getAugment(ArcanaAugments.AIRBURST.id);
      detonate(arrow,radius);
   }
   
   public static void detonate(PersistentProjectileEntity arrow, double damageRange){
      int deadPhantomCount = 0;
      List<Entity> triggerTargets = arrow.getEntityWorld().getOtherEntities(arrow,arrow.getBoundingBox().expand(damageRange*2),
            e -> !e.isSpectator() && e.distanceTo(arrow) <= damageRange && e instanceof LivingEntity);
      for(Entity entity : triggerTargets){
         if(entity instanceof LivingEntity e){
            float damage = (float) MathHelper.clamp(arrow.getVelocity().length()*4,1,10);
            damage *= (e.isOnGround() ? 0.5f : 3.5f);
            damage *= e.distanceTo(arrow) > damageRange*.75 ? 0.5f : 1;
            DamageSource source = arrow.getDamageSources().explosion(arrow,arrow.getOwner());
            e.damage(source,damage);
            if(e instanceof PhantomEntity && e.isDead()) deadPhantomCount++;
         }
      }
      if(arrow.getOwner() instanceof ServerPlayerEntity player && deadPhantomCount >= 5) ArcanaAchievements.grant(player,ArcanaAchievements.AA_ARTILLERY.id);
   
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         ParticleEffectUtils.arcaneFlakArrowDetonate(serverWorld,arrow.getPos(),damageRange,0);
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
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {k,h,m,h,k},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" Arcane Flak Arrows\\n\\nRarity: Exotic\\n\\nPhantoms... Scourges of the night sky. I shall create a weapon that strikes fear into their undead hearts.\\nThese arrows detonate when near flying creatures doing massive bonus damage in a brilliant display.\"}");
      return list;
   }
   
   public class ArcaneFlakArrowsItem extends MagicPolymerArrowItem {
      public ArcaneFlakArrowsItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipContext context, @Nullable ServerPlayerEntity player){
         return super.getPolymerItemStack(itemStack, context, player);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
