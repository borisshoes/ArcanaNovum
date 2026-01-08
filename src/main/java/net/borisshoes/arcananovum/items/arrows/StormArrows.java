package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.areaeffects.AftershockAreaEffectTracker;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.MushroomCow;
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

public class StormArrows extends RunicArrow {
	public static final String ID = "storm_arrows";
   
   private static final double[] stormChance = {.1,.2,.4,.6,.8,1};
   
   public StormArrows(){
      id = ID;
      name = "Storm Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new StormArrowsItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE,ResearchTasks.OBTAIN_LIGHTNING_ROD};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Storm Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" channel ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("lightning ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("from the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("clouds ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("above.").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Only a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("small chance").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" to work when not ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("raining").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int stableLvl = arrow.getAugment(ArcanaAugments.STORM_STABILIZATION.id);
      int chainLvl = arrow.getAugment(ArcanaAugments.CHAIN_LIGHTNING.id);
      int shockLvl = arrow.getAugment(ArcanaAugments.AFTERSHOCK.id);
      strike(arrow,entityHitResult.getLocation(),stableLvl,shockLvl);
      if(chainLvl > 0) chainLightning(arrow,entityHitResult.getEntity(),chainLvl);
      entityHitResult.getEntity().invulnerableTime = 1;
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      int stableLvl = arrow.getAugment(ArcanaAugments.STORM_STABILIZATION.id);
      int shockLvl = arrow.getAugment(ArcanaAugments.AFTERSHOCK.id);
      strike(arrow,blockHitResult.getLocation(),stableLvl,shockLvl);
   }
   
   private void strike(AbstractArrow arrow, Vec3 pos, int stableLevel, int shockLvl){
      Level world = arrow.level();
      if(arrow.isCritArrow() && (world.isRaining() || world.isThundering() || Math.random() < stormChance[stableLevel])){
         LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, arrow.level());
         lightning.setPos(pos);
         world.addFreshEntity(lightning);
         
         if(arrow.getOwner() instanceof ServerPlayer player){
            BorisLib.addTickTimerCallback(player.level(), new GenericTimer(2, () -> {
               if(lightning.getHitEntities().anyMatch(e -> e instanceof MushroomCow)) ArcanaAchievements.grant(player,ArcanaAchievements.SHOCK_THERAPY.id);
            }));
         }
         
         if(shockLvl > 0 && world instanceof ServerLevel serverWorld){
            ArcanaRegistry.AREA_EFFECTS.getValue(ArcanaRegistry.AFTERSHOCK_AREA_EFFECT_TRACKER.getId()).addSource(AftershockAreaEffectTracker.source(arrow.getOwner(), BlockPos.containing(pos),serverWorld,shockLvl));
            SoundUtils.playSound(world, BlockPos.containing(pos), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS,.2f,1f);
         }
      }
   }
   
   private void chainLightning(AbstractArrow arrow, Entity hitEntity, int lvl){
      if(!(arrow.level() instanceof ServerLevel world)) return;
      Vec3 pos = hitEntity.position();
      double range = 5;
      
      AABB rangeBox = new AABB(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getEntities(arrow.getOwner(),rangeBox, e -> !e.isSpectator() && e.distanceToSqr(pos) < range*range && !(e instanceof AbstractArrow));
      
      List<LivingEntity> hits = new ArrayList<>();
      if(hitEntity instanceof LivingEntity le) hits.add(le);
      for(Entity entity : entities){
         if(entity instanceof LivingEntity e){
            if(hits.size() < lvl+1){
               if(!hits.isEmpty()){
                  LivingEntity lastHit = hits.getLast();
                  double dist = lastHit.position().distanceTo(e.position());
                  
                  if(world instanceof ServerLevel serverWorld)
                     ArcanaEffectUtils.line(serverWorld,null,lastHit.position().add(0,lastHit.getBbHeight()/2,0),e.position().add(0,e.getBbHeight()/2,0), ParticleTypes.WAX_OFF,(int)(dist*8),2,0.05,0.05);
               }
               
               DamageSource source = ArcanaDamageTypes.of(world,ArcanaDamageTypes.ARCANE_LIGHTNING,arrow,arrow.getOwner());
               e.invulnerableTime = 1;
               e.hurtServer(world,source,6);
               hits.add(e);
            }
         }
      }
      SoundUtils.playSound(world, BlockPos.containing(pos), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS,.1f,2f);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Storm Arrows").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThe Channeling enchantment requires a storm to use. Throwing a bit of Arcana into it seems to charge the air enough to mimic a storm, although it isn’t always successful in doing so.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class StormArrowsItem extends ArcanaPolymerArrowItem {
      public StormArrowsItem(){
         super(getThis(),getArcanaArrowItemComponents(12040354));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

