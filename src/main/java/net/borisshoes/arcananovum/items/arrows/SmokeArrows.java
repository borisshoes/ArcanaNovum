package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.Arcananovum;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class SmokeArrows extends RunicArrow {
   
   public SmokeArrows(){
      id = "smoke_arrows";
      name = "Smoke Arrows";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new SmokeArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Smoke\",\"italic\":false,\"color\":\"dark_gray\",\"bold\":true}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Smoke Arrows:\",\"italic\":false,\"color\":\"dark_gray\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" emit \"},{\"text\":\"smoke\",\"color\":\"dark_gray\"},{\"text\":\" particles near where they land.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Smoke\",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\" gives \",\"color\":\"gray\"},{\"text\":\"blindness\"},{\"text\":\" and \",\"color\":\"gray\"},{\"text\":\"weakness\"},{\"text\":\" to those inside it.\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",6908265);
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
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         float range = (float) MathHelper.clamp(arrow.getVelocity().length()*.8,.3,2.5);
         int gasLvl = arrow.getAugment(ArcanaAugments.TEAR_GAS.id);
         ParticleEffectUtils.smokeArrowEmit(serverWorld,null,entityHitResult.getEntity(),range,0);
         smokeEffects(arrow,serverWorld,null,entityHitResult.getEntity(),range,gasLvl,0);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         float range = (float) MathHelper.clamp(arrow.getVelocity().length()*.8,.3,2.5);
         int gasLvl = arrow.getAugment(ArcanaAugments.TEAR_GAS.id);
         ParticleEffectUtils.smokeArrowEmit(serverWorld,blockHitResult.getPos(),null,range,0);
         smokeEffects(arrow,serverWorld,blockHitResult.getPos(),null,range,gasLvl,0);
      }
   }
   
   private void smokeEffects(PersistentProjectileEntity arrow, ServerWorld world, @Nullable Vec3d start, @Nullable Entity entity, double range, int gasLvl, int calls){
      if(start == null && entity == null) return;
      Vec3d pos = entity == null ? start : entity.getPos();
      
      Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getOtherEntities(null,rangeBox,e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 4*range*range && e instanceof LivingEntity);
      int mobCount = 0;
      boolean withOwner = false;
      for(Entity entity1 : entities){
         if(entity1 instanceof LivingEntity e){
            int amp = e instanceof MobEntity ? 5 : 0;
            StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, 60*(gasLvl+1), 7, false, false, true);
            StatusEffectInstance weakness = new StatusEffectInstance(StatusEffects.WEAKNESS, 60*(gasLvl+1), amp+gasLvl, false, false, true);
            e.addStatusEffect(blind);
            e.addStatusEffect(weakness);
            
            if(e instanceof HostileEntity mob){
               mob.setAttacking(false);
               //mob.clearGoalsAndTasks();
               mobCount++;
            }
            if(arrow.getOwner() instanceof ServerPlayerEntity player && player.getUuid().equals(e.getUuid())) withOwner = true;
         }
      }
      
      if(arrow.getOwner() instanceof ServerPlayerEntity player && withOwner && mobCount >= 3) ArcanaAchievements.grant(player,ArcanaAchievements.SMOKE_SCREEN.id);
   
      SoundUtils.playSound(world,BlockPos.ofFloored(pos), SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.PLAYERS,.5f,1);
   
      if(calls < 20){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(5, new TimerTask() {
            @Override
            public void run(){
               smokeEffects(arrow,world, pos, entity,range,gasLvl,calls + 1);
            }
         }));
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
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
