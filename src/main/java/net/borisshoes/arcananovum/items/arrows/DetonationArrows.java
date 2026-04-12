package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class DetonationArrows extends RunicArrow {
   public static final String ID = "detonation_arrows";
   
   public DetonationArrows(){
      id = ID;
      name = "Detonation Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new DetonationArrowsItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX, ResearchTasks.UNLOCK_RADIANT_FLETCHERY, ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.OBTAIN_TNT};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Detonation Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" explode").withStyle(ChatFormatting.RED))
            .append(Component.literal(" on impact ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("destroying").withStyle(ChatFormatting.RED))
            .append(Component.literal(" nearby terrain.").withStyle(ChatFormatting.GOLD)));
      lore.add(Component.literal("")
            .append(Component.literal("The").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" explosion").withStyle(ChatFormatting.RED))
            .append(Component.literal(" does ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("reduced damage").withStyle(ChatFormatting.RED))
            .append(Component.literal(" to ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("players").withStyle(ChatFormatting.RED))
            .append(Component.literal(".").withStyle(ChatFormatting.GOLD)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int blastLvl = arrow.getAugment(ArcanaAugments.BLAST_MINE);
      int personLvl = arrow.getAugment(ArcanaAugments.ANTI_PERSONNEL);
      explode(arrow, entityHitResult.getLocation(), blastLvl, personLvl);
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      int blastLvl = arrow.getAugment(ArcanaAugments.BLAST_MINE);
      int personLvl = arrow.getAugment(ArcanaAugments.ANTI_PERSONNEL);
      explode(arrow, blockHitResult.getLocation(), blastLvl, personLvl);
   }
   
   private void explode(AbstractArrow arrow, Vec3 pos, int blastLvl, int personLvl){
      float power = 2.5f * MinecraftUtils.getArrowPercentage(arrow, 0.1f);
      DamageSource source1 = ArcanaDamageTypes.of(arrow.level(), ArcanaDamageTypes.DETONATION_TERRAIN, arrow, arrow.getOwner());
      DamageSource source2 = ArcanaDamageTypes.of(arrow.level(), ArcanaDamageTypes.DETONATION_DAMAGE, arrow, arrow.getOwner());
      float blastMineBoost = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.DETONATION_ARROW_BLAST_MINE_INCREASE_PER_LVL).get(blastLvl);
      float terrainPower = power * (1 + blastMineBoost) * ArcanaNovum.CONFIG.getFloat(ArcanaConfig.DETONATION_ARROW_BLOCK_DMG_MULTIPLIER);
      float entityBoost = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.DETONATION_ARROW_ANTI_PERSONNEL_INCREASE_PER_LVL).get(personLvl);
      float entityPower = 0.75f * power * (1 + entityBoost) * ArcanaNovum.CONFIG.getFloat(ArcanaConfig.DETONATION_ARROW_ENTITY_DMG_MULTIPLIER);
      if(personLvl != 3){ // Terrain explosion except when personnel lvl 3
         arrow.level().explode(null, source1, null, pos.x, pos.y, pos.z, terrainPower, false, Level.ExplosionInteraction.TNT);
      }
      if(blastLvl == 0){ // Damage explosion except when blast is present
         arrow.level().explode(null, source2, null, pos.x, pos.y, pos.z, entityPower, false, Level.ExplosionInteraction.NONE);
      }
      arrow.discard();
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Detonation Arrows").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThis Runic Matrix has been stuffed full of volatile Arcana, ready to blow at the slightest impact. However, the blast seems to be less effective on players compared to other creatures.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class DetonationArrowsItem extends ArcanaPolymerArrowItem {
      public DetonationArrowsItem(){
         super(getThis(), getArcanaArrowItemComponents(11035949));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

