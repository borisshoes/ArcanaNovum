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
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;

public class TrackingArrows extends RunicArrow {
   public static final String ID = "tracking_arrows";
   
   public TrackingArrows(){
      id = ID;
      name = "Tracking Arrows";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new TrackingArrowsItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX, ResearchTasks.UNLOCK_RADIANT_FLETCHERY, ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.USE_ENDER_EYE, ResearchTasks.ADVANCEMENT_USE_LODESTONE, ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Tracking Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" lock on").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" to a ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("creature ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("in front of it.").withStyle(ChatFormatting.AQUA)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int broadheads = arrow.getAugment(ArcanaAugments.BROADHEADS);
      int duration = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.TRACKING_ARROW_BROADHEAD_DMG_AMP_DURATION_PER_LVL).get(broadheads);
      float damageMod = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.TRACKING_ARROW_BROADHEAD_DMG_AMP_PER_LVL).get(broadheads);
      if(entityHitResult.getEntity() instanceof LivingEntity living && broadheads > 0){
         ConditionInstance vulnerability = new ConditionInstance(Conditions.VULNERABILITY, arcanaId(ID), duration, damageMod, true, true, false, AttributeModifier.Operation.ADD_VALUE, arrow.getOwner() != null ? arrow.getOwner().getUUID() : null);
         Conditions.addCondition(living.level().getServer(), living, vulnerability);
      }
      CompoundTag arrowData = arrow.getData();
      if(arrow.getOwner() instanceof ServerPlayer player && arrowData.contains("initPos")){
         ListTag posList = arrowData.getListOrEmpty("initPos");
         Vec3 initPos = new Vec3(posList.getDoubleOr(0, 0.0), posList.getDoubleOr(1, 0.0), posList.getDoubleOr(2, 0.0));
         double dist = initPos.multiply(1, 0, 1).distanceTo(arrow.position().multiply(1, 0, 1));
         if(dist >= 250){
            ArcanaAchievements.grant(player, ArcanaAchievements.ACTUAL_AIMBOT);
         }
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Tracking Arrows").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThese arrows take advantage of Ender Eyes’ ability to home in on a location. \nThe arrow will look ahead a short distance and correct its angle to head for a creature in sight.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class TrackingArrowsItem extends ArcanaPolymerArrowItem {
      public TrackingArrowsItem(){
         super(getThis(), getArcanaArrowItemComponents(16777063));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

