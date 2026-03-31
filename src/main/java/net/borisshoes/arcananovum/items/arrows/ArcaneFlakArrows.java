package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcaneFlakArrows extends RunicArrow {
   public static final String ID = "arcane_flak_arrows";
   
   public static final int armTime = 5;
   
   public ArcaneFlakArrows(){
      id = ID;
      name = "Arcane Flak Arrows";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new ArcaneFlakArrowsItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX, ResearchTasks.UNLOCK_RADIANT_FLETCHERY, ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.USE_FIREWORK, ResearchTasks.ADVANCEMENT_DRAGON_BREATH};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Arcane Flak Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" explode").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" when passing by a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("flying creature").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Deals ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("increased damage").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("airborne entities").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      double baseR = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.FLAK_ARROW_RANGE);
      double extraR = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.FLAK_ARROW_AIRBURST_RANGE_BUFF_PER_LVL).get(arrow.getAugment(ArcanaAugments.AIRBURST));
      double radius = baseR + extraR;
      detonate(arrow, radius);
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      double baseR = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.FLAK_ARROW_RANGE);
      double extraR = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.FLAK_ARROW_AIRBURST_RANGE_BUFF_PER_LVL).get(arrow.getAugment(ArcanaAugments.AIRBURST));
      double radius = baseR + extraR;
      detonate(arrow, radius);
   }
   
   public static void detonate(AbstractArrow arrow, double damageRange){
      if(!(arrow.level() instanceof ServerLevel serverWorld)) return;
      int deadPhantomCount = 0;
      float percentage = ArcanaUtils.getArrowPercentage(arrow, 0.1f);
      float damageMax = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.FLAK_ARROW_DAMAGE);
      float damage = percentage * damageMax;
      float multiplier = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.FLAK_ARROW_DAMAGE_MULTIPLIER);
      List<Entity> triggerTargets = arrow.level().getEntities(arrow, arrow.getBoundingBox().inflate(damageRange * 2),
            e -> !e.isSpectator() && e.distanceTo(arrow) <= damageRange && e instanceof LivingEntity);
      for(Entity entity : triggerTargets){
         if(entity instanceof LivingEntity e){
            damage *= e.onGround() ? 1f : multiplier;
            damage *= e.distanceTo(arrow) > damageRange * .66 ? 0.5f : 1;
            DamageSource source = arrow.damageSources().explosion(arrow, arrow.getOwner());
            e.hurtServer(serverWorld, source, damage);
            if(e instanceof Phantom && e.isDeadOrDying()) deadPhantomCount++;
         }
      }
      if(arrow.getOwner() instanceof ServerPlayer player && deadPhantomCount >= 5)
         ArcanaAchievements.grant(player, ArcanaAchievements.AA_ARTILLERY);
      
      ArcanaEffectUtils.arcaneFlakArrowDetonate(serverWorld, arrow.position(), damageRange, 0);
      SoundUtils.playSound(serverWorld, arrow.blockPosition(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1f, 1f);
      SoundUtils.playSound(serverWorld, arrow.blockPosition(), SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.PLAYERS, 1f, 1f);
      arrow.discard();
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Arcane Flak\n       Arrows").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nPhantoms… Scourges of the night sky. I shall create a weapon that strikes fear into their undead hearts. These arrows detonate when near flying creatures, doing massive bonus ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Arcane Flak\n       Arrows").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\ndamage in a brilliant display.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ArcaneFlakArrowsItem extends ArcanaPolymerArrowItem {
      public ArcaneFlakArrowsItem(){
         super(getThis(), getArcanaArrowItemComponents(7802273));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}
