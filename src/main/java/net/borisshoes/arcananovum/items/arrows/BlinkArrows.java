package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;

public class BlinkArrows extends RunicArrow {
	public static final String ID = "blink_arrows";
   
   public BlinkArrows(){
      id = ID;
      name = "Blink Arrows";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new BlinkArrowsItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.USE_ENDER_PEARL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Blink Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" take after ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("Ender Pearls").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Upon ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("impact ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("or ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("hitting a target").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" you get ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("teleported").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to the ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("arrow").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getOwner() instanceof ServerPlayer player){
         Vec3 tpPos = entityHitResult.getLocation();
         teleport(player,arrow,tpPos);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getOwner() instanceof ServerPlayer player){
         Vec3 offset = new Vec3(blockHitResult.getDirection().step());
         Vec3 tpPos = blockHitResult.getLocation().add(offset);
         teleport(player,arrow,tpPos);
      }
   }
   
   private void teleport(ServerPlayer player, RunicArrowEntity arrow, Vec3 tpPos){
      if(tpPos.distanceTo(player.position()) >= 100) ArcanaAchievements.grant(player,ArcanaAchievements.NOW_YOU_SEE_ME);
      player.teleport(new TeleportTransition((ServerLevel) arrow.level(), tpPos.add(0,0.25,0), Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
      ArcanaEffectUtils.blinkArrowTp(player.level(),player.position());
      SoundUtils.playSound(arrow.level(),player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,.8f,.9f);
      
      float percentage = ArcanaUtils.getArrowPercentage(arrow);
      int phaseLvl = arrow.getAugment(ArcanaAugments.PHASE_IN);
      int phaseInDuration = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.BLINK_ARROW_PHASE_IN_DURATION_PER_LVL).get(phaseLvl);
      float phaseInDamageMod = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.BLINK_ARROW_PHASE_IN_DMG_MULTIPLIER);
      ConditionInstance fortitude = new ConditionInstance(Conditions.FORTITUDE,arcanaId(getId()),Math.min((int)(percentage*phaseInDuration),phaseInDuration),phaseInDamageMod,true,true,false, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, player.getUUID());
      Conditions.addCondition(player.level().getServer(),player,fortitude);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Blink Arrows").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThis Runic Matrix has been configured for invoking teleportation spells, and now the arrows act like a thrown Ender Pearl.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class BlinkArrowsItem extends ArcanaPolymerArrowItem {
      public BlinkArrowsItem(){
         super(getThis(),getArcanaArrowItemComponents(1404502));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

