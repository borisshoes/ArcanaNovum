package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.areaeffects.SmokeArrowAreaEffectTracker;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class SmokeArrows extends RunicArrow {
   public static final String ID = "smoke_arrows";
   
   public SmokeArrows(){
      id = ID;
      name = "Smoke Arrows";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new SmokeArrowsItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GRAY);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX, ResearchTasks.UNLOCK_RADIANT_FLETCHERY, ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.KILL_SQUID, ResearchTasks.USE_CAMPFIRE, ResearchTasks.ADVANCEMENT_DRAGON_BREATH, ResearchTasks.EFFECT_BLINDNESS, ResearchTasks.EFFECT_WEAKNESS};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Smoke Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GRAY));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" emit ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("smoke").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(" particles near where they land.").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Smoke").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(" gives ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("blindness").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(" and ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("weakness").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(" to those inside it.").withStyle(ChatFormatting.GRAY)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.level() instanceof ServerLevel serverWorld){
         float percentage = ArcanaUtils.getArrowPercentage(arrow);
         double maxRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.SMOKE_ARROW_RANGE_MIN);
         double minRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.SMOKE_ARROW_RANGE_MAX);
         float range = (float) Mth.clamp(percentage * maxRange, minRange, maxRange);
         int gasLvl = arrow.getAugment(ArcanaAugments.TEAR_GAS);
         ArcanaRegistry.AREA_EFFECTS.getValue(ArcanaRegistry.SMOKE_ARROW_AREA_EFFECT_TRACKER.getId()).addSource(SmokeArrowAreaEffectTracker.source(arrow.getOwner(), entityHitResult.getEntity(), null, null, range, gasLvl));
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.level() instanceof ServerLevel serverWorld){
         float percentage = ArcanaUtils.getArrowPercentage(arrow);
         double maxRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.SMOKE_ARROW_RANGE_MIN);
         double minRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.SMOKE_ARROW_RANGE_MAX);
         float range = (float) Mth.clamp(percentage * maxRange, minRange, maxRange);
         int gasLvl = arrow.getAugment(ArcanaAugments.TEAR_GAS);
         ArcanaRegistry.AREA_EFFECTS.getValue(ArcanaRegistry.SMOKE_ARROW_AREA_EFFECT_TRACKER.getId()).addSource(SmokeArrowAreaEffectTracker.source(arrow.getOwner(), null, blockHitResult.getBlockPos(), serverWorld, range, gasLvl));
      }
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Smoke Arrows").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThis Runic Matrix has been configured to summon copious amounts of smoke. Those inside will have trouble seeing and even breathing, making it harder to land a solid blow.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class SmokeArrowsItem extends ArcanaPolymerArrowItem {
      public SmokeArrowsItem(){
         super(getThis(), getArcanaArrowItemComponents(6908265));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

