package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.NulConstructEntity;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ConcussionArrows extends RunicArrow {
	public static final String ID = "concussion_arrows";
   
   public ConcussionArrows(){
      id = ID;
      name = "Concussion Arrows";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new ConcussionArrowsItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.GOLD);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.KILL_SQUID,ResearchTasks.ADVANCEMENT_DRAGON_BREATH,ResearchTasks.EFFECT_BLINDNESS,ResearchTasks.EFFECT_WEAKNESS,ResearchTasks.USE_FIREWORK};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Concussion Arrows:").formatted(Formatting.BOLD,Formatting.GOLD));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.GRAY))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" concuss ").formatted(Formatting.GOLD))
            .append(Text.literal("entities ").formatted(Formatting.YELLOW))
            .append(Text.literal("near where the arrow ").formatted(Formatting.GRAY))
            .append(Text.literal("impacts").formatted(Formatting.GOLD))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int lvl = arrow.getAugment(ArcanaAugments.SHELLSHOCK.id);
      concuss(arrow, arrow.getWorld(),entityHitResult.getPos(), lvl);
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      int lvl = arrow.getAugment(ArcanaAugments.SHELLSHOCK.id);
      concuss(arrow, arrow.getWorld(),blockHitResult.getPos(), lvl);
   }
   
   private void concuss(PersistentProjectileEntity arrow, World world, Vec3d pos, int levelBoost){
      Box rangeBox = new Box(pos.x+10,pos.y+10,pos.z+10,pos.x-10,pos.y-10,pos.z-10);
      float range = (float) MathHelper.clamp(arrow.getVelocity().length()*2.5,1,6);
      List<Entity> entities = world.getOtherEntities(null,rangeBox,e -> !e.isSpectator() && e.squaredDistanceTo(pos) < range*range && e instanceof LivingEntity);
      float percent = (1+levelBoost*.75f)*range/6;
      int mobsHit = 0;
      for(Entity entity : entities){
         if(entity instanceof LivingEntity e && !(entity instanceof EnderDragonEntity || entity instanceof WitherEntity || entity instanceof NulConstructEntity)){
            if(e instanceof MobEntity) mobsHit++;
            
            StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, (int)(25*percent), 2, false, false, true);
            StatusEffectInstance nausea = new StatusEffectInstance(StatusEffects.NAUSEA, (int)(120*percent), 0, false, false, true);
            StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(40*percent), 4, false, false, true);
            StatusEffectInstance slow2 = new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(120*percent), 2, false, false, true);
            StatusEffectInstance fatigue = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (int)(80*percent), 2+levelBoost, false, false, true);
            StatusEffectInstance weakness = new StatusEffectInstance(StatusEffects.WEAKNESS, (int)(120*percent), 1+levelBoost, false, false, true);
            e.addStatusEffect(blind);
            e.addStatusEffect(nausea);
            e.addStatusEffect(slow);
            e.addStatusEffect(slow2);
            e.addStatusEffect(fatigue);
            e.addStatusEffect(weakness);
            
            if(world instanceof ServerWorld serverWorld){
               if(e instanceof MobEntity mob){
                  mob.setAiDisabled(true);
                  ArcanaNovum.addTickTimerCallback(serverWorld, new GenericTimer(100, () -> {
                     if(mob.isAlive()){
                        mob.setAiDisabled(false);
                     }
                  }));
               }
            }
         }
      }
      if(arrow.getOwner() instanceof ServerPlayerEntity player && mobsHit >= 10) ArcanaAchievements.grant(player,ArcanaAchievements.SHOCK_AWE.id);
      if(world instanceof ServerWorld serverWorld){
         SoundUtils.playSound(world, BlockPos.ofFloored(pos), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS, 1, .8f);
         ParticleEffectUtils.concussionArrowShot(serverWorld, pos, range, 0);
      }
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.LINGERING_POTION,1).withPotions(Potions.STRONG_SLOWNESS);
      ArcanaIngredient g = new ArcanaIngredient(Items.GLOW_INK_SAC,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      ArcanaIngredient i = new ArcanaIngredient(Items.INK_SAC,16);
      ArcanaIngredient k = new ArcanaIngredient(Items.LINGERING_POTION,1).withPotions(Potions.LONG_WEAKNESS);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,i,a},
            {k,h,m,h,k},
            {a,i,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("Concussion Arrows").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThis Runic Matrix has been configured to unleash a plethora of unpleasant effects at the area of impact. Anyone caught in its range will have a hard time doing anything for a few seconds after being hit.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class ConcussionArrowsItem extends ArcanaPolymerArrowItem {
      public ConcussionArrowsItem(){
         super(getThis(),getArcanaArrowItemComponents(14391821));
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

