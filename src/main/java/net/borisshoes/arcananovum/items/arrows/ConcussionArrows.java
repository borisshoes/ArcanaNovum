package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.NulConstructEntity;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;

public class ConcussionArrows extends RunicArrow {
   public static final String ID = "concussion_arrows";
   
   public ConcussionArrows(){
      id = ID;
      name = "Concussion Arrows";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new ConcussionArrowsItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX, ResearchTasks.UNLOCK_RADIANT_FLETCHERY, ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.KILL_SQUID, ResearchTasks.ADVANCEMENT_DRAGON_BREATH, ResearchTasks.EFFECT_BLINDNESS, ResearchTasks.EFFECT_WEAKNESS, ResearchTasks.USE_FIREWORK};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Concussion Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" concuss ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("entities ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("near where the arrow ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("impacts").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int lvl = arrow.getAugment(ArcanaAugments.SHELLSHOCK);
      concuss(arrow, arrow.level(), entityHitResult.getLocation(), lvl);
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      int lvl = arrow.getAugment(ArcanaAugments.SHELLSHOCK);
      concuss(arrow, arrow.level(), blockHitResult.getLocation(), lvl);
   }
   
   private void concuss(AbstractArrow arrow, Level world, Vec3 pos, int levelBoost){
      AABB rangeBox = new AABB(pos.x + 10, pos.y + 10, pos.z + 10, pos.x - 10, pos.y - 10, pos.z - 10);
      float percentage = ArcanaUtils.getArrowPercentage(arrow);
      double maxRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.CONCUSSION_ARROW_RANGE_MAX);
      double minRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.CONCUSSION_ARROW_RANGE_MIN);
      double range = Mth.clamp(percentage * maxRange, minRange, maxRange);
      float durMod = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.CONCUSSION_ARROW_DURATION_MOD);
      float extraDuration = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.CONCUSSION_ARROW_SHELLSHOCK_BOOST_PER_LVL).get(levelBoost);
      
      List<Entity> entities = world.getEntities((Entity) null, rangeBox, e -> !e.isSpectator() && e.distanceToSqr(pos) < range * range && e instanceof LivingEntity);
      float percent = durMod * (1 + extraDuration);
      int mobsHit = 0;
      for(Entity entity : entities){
         if(entity instanceof LivingEntity e && !(entity instanceof EnderDragon || entity instanceof WitherBoss || entity instanceof NulConstructEntity)){
            if(e instanceof Mob) mobsHit++;
            
            ConditionInstance nearsight = new ConditionInstance(Conditions.NEARSIGHT, arcanaId(ID), (int) (25 * percent), 3.5f, false, true, false, AttributeModifier.Operation.ADD_VALUE, arrow.getOwner() != null ? arrow.getOwner().getUUID() : null);
            MobEffectInstance nausea = new MobEffectInstance(MobEffects.NAUSEA, (int) (120 * percent), 0, false, false, true);
            MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS, (int) (40 * percent), 4, false, false, true);
            MobEffectInstance slow2 = new MobEffectInstance(MobEffects.SLOWNESS, (int) (120 * percent), 2, false, false, true);
            MobEffectInstance fatigue = new MobEffectInstance(MobEffects.MINING_FATIGUE, (int) (80 * percent), 2, false, false, true);
            MobEffectInstance weakness = new MobEffectInstance(MobEffects.WEAKNESS, (int) (120 * percent), 2, false, false, true);
            Conditions.addCondition(world.getServer(), e, nearsight);
            e.addEffect(nausea);
            e.addEffect(slow);
            e.addEffect(slow2);
            e.addEffect(fatigue);
            e.addEffect(weakness);
            
            if(world instanceof ServerLevel serverWorld){
               if(e instanceof Mob mob){
                  mob.setNoAi(true);
                  BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(100, () -> {
                     if(mob.isAlive()){
                        mob.setNoAi(false);
                     }
                  }));
               }
            }
         }
      }
      if(arrow.getOwner() instanceof ServerPlayer player && mobsHit >= 10)
         ArcanaAchievements.grant(player, ArcanaAchievements.SHOCK_AWE);
      if(world instanceof ServerLevel serverWorld){
         SoundUtils.playSound(world, BlockPos.containing(pos), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1, .8f);
         ArcanaEffectUtils.concussionArrowShot(serverWorld, pos, range, 0);
      }
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Concussion Arrows").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThis Runic Matrix has been configured to unleash a plethora of unpleasant effects at the area of impact. Anyone caught in its range will have a hard time doing anything for a few seconds after being hit.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ConcussionArrowsItem extends ArcanaPolymerArrowItem {
      public ConcussionArrowsItem(){
         super(getThis(), getArcanaArrowItemComponents(14391821));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

