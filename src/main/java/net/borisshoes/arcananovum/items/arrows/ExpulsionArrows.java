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
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ExpulsionArrows extends RunicArrow {
	public static final String ID = "expulsion_arrows";
   
   public ExpulsionArrows(){
      id = ID;
      name = "Expulsion Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new ExpulsionArrowsItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.KILL_SLIME,ResearchTasks.OBTAIN_AMETHYST_SHARD,ResearchTasks.USE_ENDER_PEARL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Expulsion Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" repulse ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("entities").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" near the area of ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("impact").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("A hit ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("entity ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("is ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("not affected").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.level() instanceof ServerLevel serverWorld){
         boolean evict = arrow.getAugment(ArcanaAugments.EVICTION_BURST) > 0;
         if(evict){
            double minRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_EVICTION_RANGE_MIN);
            double maxRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_EVICTION_RANGE_MAX);
            double percentage = ArcanaUtils.getArrowPercentage(arrow);
            double range = Mth.clamp(percentage*maxRange,minRange,maxRange);
            BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(1, () -> evictionPulse(arrow, serverWorld,entityHitResult.getLocation(),range)));
         }else{
            double minDur = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_DURATION_MIN);
            double maxDur = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_DURATION_MAX);
            double percentage = ArcanaUtils.getArrowPercentage(arrow);
            int duration = (int) Mth.clamp(percentage*maxDur,minDur,maxDur); // Measured in quarter seconds
            double baseRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_RANGE);
            double extraRange = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.EXPULSION_ARROW_REPULSION_RANGE_PER_LVL).get(arrow.getAugment(ArcanaAugments.REPULSION)).floatValue();
            double range = baseRange + extraRange;
            expulsionPulse(arrow, serverWorld,null,entityHitResult.getEntity(),duration,range,0);
         }
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.level() instanceof ServerLevel serverWorld){
         boolean evict = arrow.getAugment(ArcanaAugments.EVICTION_BURST) > 0;
         if(evict){
            double minRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_EVICTION_RANGE_MIN);
            double maxRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_EVICTION_RANGE_MAX);
            double percentage = ArcanaUtils.getArrowPercentage(arrow);
            double range = Mth.clamp(percentage*maxRange,minRange,maxRange);
            BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(1, () -> evictionPulse(arrow, serverWorld,blockHitResult.getLocation(),range)));
         }else{
            double minDur = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_DURATION_MIN);
            double maxDur = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_DURATION_MAX);
            double percentage = ArcanaUtils.getArrowPercentage(arrow);
            int duration = (int) Mth.clamp(percentage*maxDur,minDur,maxDur); // Measured in quarter seconds
            double baseRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_RANGE);
            double extraRange = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.EXPULSION_ARROW_REPULSION_RANGE_PER_LVL).get(arrow.getAugment(ArcanaAugments.REPULSION));
            double range = baseRange + extraRange;
            expulsionPulse(arrow, serverWorld,blockHitResult.getLocation(),null,duration,range,0);
         }
      }
   }
   
   private void evictionPulse(AbstractArrow arrow, ServerLevel world, Vec3 pos, double range){
      AABB rangeBox = new AABB(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getEntities((Entity) null,rangeBox, e -> !e.isSpectator() && e.distanceToSqr(pos) < 1.5*range*range && !(e instanceof AbstractArrow) && !(e instanceof EnderDragon));
      for(Entity entity1 : entities){
         Vec3 diff = entity1.position().subtract(pos);
         double extraMod = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.EXPULSION_ARROW_EVICTION_POWER_PER_LVL).getFirst();
         double multiplier = extraMod*Mth.clamp(3-diff.length()*.5,.1,7.5);
         Vec3 motion = diff.add(0,0,0).normalize().scale(multiplier);
         entity1.setDeltaMovement(motion.x,motion.y,motion.z);
         if(entity1 instanceof ServerPlayer player){
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
            
            if(arrow.getOwner() != null && arrow.getOwner().getUUID().equals(player.getUUID()) && motion.y > 2) ArcanaAchievements.grant(player,ArcanaAchievements.JUMP_PAD);
         }
      }
      
      ArcanaEffectUtils.expulsionArrowEmit(world,pos,range,0);
      SoundUtils.playSound(world, BlockPos.containing(pos), SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.PLAYERS,.5f,.5f);
   }
   // TODO these repulsion power calcs might need standardizing
   private void expulsionPulse(AbstractArrow arrow, ServerLevel world, @Nullable Vec3 start, @Nullable Entity entity, int duration, double range, int calls){
      if(start == null && entity == null) return;
      Vec3 pos = entity == null ? start : entity.position();
      
      AABB rangeBox = new AABB(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getEntities(entity,rangeBox, e -> !e.isSpectator() && e.distanceToSqr(pos) < 1.5*range*range && !(e instanceof AbstractArrow) && !(e instanceof EnderDragon));
      for(Entity entity1 : entities){
         Vec3 diff = entity1.position().subtract(pos);
         double multiplier = Mth.clamp(range*.75-diff.length()*.5,.1,5);
         Vec3 motion = diff.add(0,0,0).normalize().scale(multiplier);
         entity1.setDeltaMovement(motion.x,motion.y,motion.z);
         if(entity1 instanceof ServerPlayer player){
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
            
            if(arrow.getOwner() != null && arrow.getOwner().getUUID().equals(player.getUUID()) && motion.y > 2) ArcanaAchievements.grant(player,ArcanaAchievements.JUMP_PAD);
         }
      }
      
      if(calls % 5 == 0){
         ArcanaEffectUtils.expulsionArrowEmit(world,pos,range,0);
         SoundUtils.playSound(world, BlockPos.containing(pos), SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.PLAYERS,.5f,.5f);
      }
      if(calls % 10 == 1){
         SoundUtils.playSound(world, BlockPos.containing(pos), SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundSource.PLAYERS,.5f,.9f);
      }
      
      if(calls < duration){
         BorisLib.addTickTimerCallback(world, new GenericTimer(5, () -> expulsionPulse(arrow, world, pos, entity,duration,range,calls + 1)));
      }
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Expulsion Arrows").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThis Runic Matrix is configured to repulse anything nearby like bouncing on a slime block. Great for jump pads, zoning off monsters, or sending foes off a steep cliff.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ExpulsionArrowsItem extends ArcanaPolymerArrowItem {
      public ExpulsionArrowsItem(){
         super(getThis(),getArcanaArrowItemComponents(889599));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

