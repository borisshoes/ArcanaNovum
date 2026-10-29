package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.events.TetherFlightEvent;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.config.IConfigSetting;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TetherArrows extends RunicArrow {
   public static final String ID = "tether_arrows";
   
   public TetherArrows(){
      id = ID;
      name = "Tether Arrows";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new TetherArrowsItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX, ResearchTasks.UNLOCK_RADIANT_FLETCHERY, ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.RIPTIDE_TRIDENT, ResearchTasks.FISH_MOB, ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      relatedConfigs = new IConfigSetting[]{ArcanaConfig.TETHER_ARROW_MAX_RANGE, ArcanaConfig.TETHER_ARROW_MAX_SPEED};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Tether Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Runic Arrows ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("pull").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" you to a block like a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("grappling hook").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("They will also ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("pull").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" a hit ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("entity ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("towards you.").withStyle(ChatFormatting.AQUA)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getData().getBooleanOr("severed", false)) return;
      if(arrow.getOwner() instanceof ServerPlayer player && entityHitResult.getEntity() instanceof LivingEntity entity){
         // Waits a tick so the pull replaces the arrow's knockback
         BorisLib.addTickTimerCallback(player.level(), new GenericTimer(1, () -> {
            Tether tether = pull(entity, player);
            if(tether == null){
               fail(player);
               return;
            }
            launch(entity, tether);
            
            ArcanaEffectUtils.tetherArrowEntity(player.level(), entity, player);
            SoundUtils.playSound(arrow.level(), player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, .8f, .6f);
         }));
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getData().getBooleanOr("severed", false)) return;
      if(arrow.getOwner() instanceof ServerPlayer player){
         double startY = player.getY();
         Tether tether = grapple(player, blockHitResult);
         if(tether == null){
            fail(player);
            return;
         }
         launch(player, tether);
         ArcanaEffectUtils.tetherArrowGrapple(player.level(), player, blockHitResult.getLocation());
         SoundUtils.playSound(arrow.level(), player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2, SoundSource.PLAYERS, .8f, .6f);
         
         Vec3 spot = landingSpot(player, blockHitResult);
         if((spot != null ? spot.y : blockHitResult.getLocation().y) - startY >= 12)
            ArcanaAchievements.progress(player, ArcanaAchievements.SPIDERMAN, 1);
      }
   }
   
   // The target was out of range, so nothing moves
   private static void fail(ServerPlayer player){
      player.sendSystemMessage(Component.literal("The Arcane Tether Fails").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
      SoundUtils.playSound(player.level(), player.blockPosition(), SoundEvents.LEAD_BREAK, SoundSource.PLAYERS, 1f, 1f);
   }
   
   // ===== Tether physics =====
   // The velocity is solved against vanilla's air movement, so the entity arrives as long as it doesn't steer in the air.
   // Fluids, elytra flight and creative flight move differently, so the tether is weaker there.
   // Targets past tetherArrowMaxRange fail outright, so an arrow held still (in a cobweb, say) can't fling its shooter across the map.
   // Launches faster than tetherArrowMaxSpeed are slowed to it and fall short. A flung player gets a TetherFlightEvent,
   // which lets the flight past the server's "moved too quickly" check
   
   public static final double PULL_GAP = 1.0; // Blocks between the shooter and a pulled entity when it lands
   private static final double LANDING_SINK = 0.05; // Aims this far into the ground, so the last tick collides and lands instead of stopping just above it
   
   // A launch velocity and how many ticks the flight should take
   public record Tether(Vec3 velocity, int ticks) {
   }
   
   // Sends the entity off. A player also gets a TetherFlightEvent, or the server would rubber band a fast flight
   public static void launch(LivingEntity entity, Tether tether){
      entity.setDeltaMovement(tether.velocity());
      if(entity instanceof ServerPlayer player){
         player.connection.send(new ClientboundSetEntityMotionPacket(player));
         TetherFlightEvent.start(player, tether.velocity(), tether.ticks());
      }
   }
   
   // Lands the entity on top of the block it hit, or carries it up to the hit spot when there is no room on top. Null when out of range
   public static @Nullable Tether grapple(LivingEntity entity, BlockHitResult hit){
      Vec3 spot = landingSpot(entity, hit);
      if(spot != null) return tether(entity, spot, true, entryDistance(entity, spot));
      Vec3 face = hit.getLocation();
      if(hit.getDirection() == Direction.DOWN){
         face = face.subtract(0, entity.getBbHeight(), 0);
      }else if(hit.getDirection() != Direction.UP){
         face = face.add(hit.getDirection().getUnitVec3().scale(entity.getBbWidth() / 2)); // Feet just outside the wall
      }
      return tether(entity, face, false, 0);
   }
   
   // Drops the entity just in front of the puller. Null when out of range
   public static @Nullable Tether pull(LivingEntity entity, LivingEntity puller){
      Vec3 away = entity.position().subtract(puller.position()).multiply(1, 0, 1);
      double gap = (entity.getBbWidth() + puller.getBbWidth()) / 2 + PULL_GAP;
      Vec3 target = puller.position().add(away.length() > gap ? away.normalize().scale(gap) : away);
      return tether(entity, target, true, Double.POSITIVE_INFINITY);
   }
   
   // Carries the entity to target, or null when the target is out of range. See TetherPhysics.solve for land and clearFrom
   public static @Nullable Tether tether(LivingEntity entity, Vec3 target, boolean land, double clearFrom){
      Vec3 delta = target.subtract(entity.position());
      if(delta.length() > ArcanaNovum.CONFIG.getDouble(ArcanaConfig.TETHER_ARROW_MAX_RANGE)) return null;
      double height = land ? delta.y - LANDING_SINK : delta.y;
      TetherPhysics.Launch launch = TetherPhysics.of(entity).solve(delta.horizontalDistance(), height, land, clearFrom);
      if(launch == null) return null;
      launch = launch.capped(ArcanaNovum.CONFIG.getDouble(ArcanaConfig.TETHER_ARROW_MAX_SPEED));
      Vec3 dir = delta.multiply(1, 0, 1).normalize();
      return new Tether(new Vec3(dir.x * launch.horizontal(), launch.vertical(), dir.z * launch.horizontal()), launch.ticks());
   }
   
   // Where an entity grappling to this hit should stand: on the top face it hit, or on top of the hit block or the first one above it with room.
   // Null for ceilings and walls with no room on top within a few blocks
   public static @Nullable Vec3 landingSpot(LivingEntity entity, BlockHitResult hit){
      if(hit.getDirection() == Direction.DOWN) return null;
      if(hit.getDirection() == Direction.UP) return hit.getLocation();
      Level level = entity.level();
      BlockPos pos = hit.getBlockPos();
      for(int i = 0; i < 4; i++, pos = pos.above()){
         VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
         if(shape.isEmpty()) return null;
         Vec3 spot = new Vec3(pos.getX() + 0.5, pos.getY() + shape.max(Direction.Axis.Y), pos.getZ() + 0.5);
         if(level.noCollision(entity, entity.getDimensions(Pose.STANDING).makeBoundingBox(spot).deflate(1.0E-4)))
            return spot;
      }
      return null;
   }
   
   // How far the entity travels horizontally toward spot before its box is over the block that spot sits on
   private static double entryDistance(LivingEntity entity, Vec3 spot){
      Vec3 start = entity.position();
      Vec3 dir = spot.subtract(start).multiply(1, 0, 1).normalize();
      BlockPos block = BlockPos.containing(spot.x, spot.y - 1.0E-3, spot.z);
      double half = entity.getBbWidth() / 2;
      double x = slabEntry(start.x, dir.x, block.getX() - half, block.getX() + 1 + half);
      double z = slabEntry(start.z, dir.z, block.getZ() - half, block.getZ() + 1 + half);
      return Math.max(0, Math.max(x, z));
   }
   
   private static double slabEntry(double start, double dir, double min, double max){
      if(Math.abs(dir) < 1.0E-9) return 0; // Not moving on this axis, and already inside it since the spot is
      return Math.min((min - start) / dir, (max - start) / dir);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Tether Arrows").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThrough precise kinematic equations formed on the Matrix, these Arrows should form the perfect tether to pull me to the location I shoot. It can also pull creatures towards me.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class TetherArrowsItem extends ArcanaPolymerArrowItem {
      public TetherArrowsItem(){
         super(getThis(), getArcanaArrowItemComponents(10724259));
      }
   }
   
   // Air movement of a LivingEntity, stepped like LivingEntity.aiStep and travelInAir: tiny speeds snap to 0, move, then gravity or levitation, then drag.
   // firstAirDrag is the horizontal drag after the first tick, which includes block friction when the entity starts on the ground
   public record TetherPhysics(double gravity, double fallingGravity, boolean levitating, double levitation,
                               double verticalDrag, double airDrag, double firstAirDrag, boolean startsOnGround) {
      public static final int MAX_TICKS = 100;
      private static final double START_CLEARANCE = 1.0; // Blocks to travel before an entity that started on the ground may come back down
      
      public record Launch(double horizontal, double vertical, int ticks) {
         public double speed(){
            return Math.hypot(horizontal, vertical);
         }
         
         // The same direction, slowed to maxSpeed if it is faster. A slowed launch falls short
         public Launch capped(double maxSpeed){
            double speed = speed();
            if(speed <= maxSpeed) return this;
            double scale = maxSpeed / speed;
            return new Launch(horizontal * scale, vertical * scale, ticks);
         }
      }
      
      public static TetherPhysics of(LivingEntity entity){
         if(entity instanceof Ghast || entity instanceof Phantom || entity instanceof Allay || entity instanceof HappyGhast){
            return new TetherPhysics(0, 0, false, 0, 0.91F, 0.91F, 0.91F, false); // LivingEntity.travelFlying
         }
         double gravity = entity.getGravity();
         double fallingGravity = entity.hasEffect(MobEffects.SLOW_FALLING) ? Math.min(gravity, 0.01) : gravity;
         MobEffectInstance levitation = entity.getEffect(MobEffects.LEVITATION);
         double levitationSpeed = levitation == null ? 0 : 0.05 * (levitation.getAmplifier() + 1);
         if(entity.shouldDiscardFriction()){
            return new TetherPhysics(gravity, fallingGravity, levitation != null, levitationSpeed, 1, 1, 1, entity.onGround());
         }
         float dragModifier = (float) entity.getAttributeValue(Attributes.AIR_DRAG_MODIFIER);
         float airDrag = modifiedFriction(0.91F, dragModifier);
         float verticalDrag = entity.omnidirectionalAirMover() ? airDrag : modifiedFriction(0.98F, dragModifier);
         float blockFriction = 1.0F;
         if(entity.onGround()){
            float friction = entity.level().getBlockState(entity.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction();
            blockFriction = modifiedFriction(friction, (float) entity.getAttributeValue(Attributes.FRICTION_MODIFIER));
         }
         return new TetherPhysics(gravity, fallingGravity, levitation != null, levitationSpeed, verticalDrag, airDrag, blockFriction * airDrag, entity.onGround());
      }
      
      // LivingEntity.computeModifiedFriction
      private static float modifiedFriction(float friction, float modifier){
         return Mth.clamp(1.0F - (1.0F - friction) * modifier, 0.0F, 1.0F);
      }
      
      // The slowest launch that moves the entity distance blocks horizontally and height blocks up, arriving on the last tick of its flight.
      // land: it must come down onto the target, be above it from clearFrom blocks along, and leave the ground first if it starts there.
      // Otherwise it never goes above the target. Constraints are dropped when no flight time meets them
      public @Nullable Launch solve(double distance, double height, boolean land, double clearFrom){
         Launch launch = solve(distance, height, land, clearFrom, true);
         return launch != null ? launch : solve(distance, height, land, clearFrom, false);
      }
      
      private @Nullable Launch solve(double distance, double height, boolean land, double clearFrom, boolean constrained){
         // reach[k] is the horizontal distance covered in k ticks per unit of launch speed
         double[] reach = new double[MAX_TICKS + 1];
         double speed = 1;
         for(int k = 0; k < MAX_TICKS; k++){
            reach[k + 1] = reach[k] + speed;
            speed *= k == 0 ? firstAirDrag : airDrag;
         }
         
         Launch best = null;
         double[] heights = new double[MAX_TICKS + 1];
         for(int ticks = 2; ticks <= MAX_TICKS; ticks++){
            double horizontal = distance / reach[ticks];
            double vertical = verticalLaunch(height, ticks);
            trajectory(vertical, ticks, heights);
            if(Math.abs(heights[ticks] - height) > 0.01) continue;
            if(constrained && !allowed(heights, reach, horizontal, ticks, distance, height, land, clearFrom)) continue;
            Launch launch = new Launch(horizontal, vertical, ticks);
            if(best == null || launch.speed() < best.speed()) best = launch;
         }
         return best;
      }
      
      private boolean allowed(double[] heights, double[] reach, double horizontal, int ticks, double distance, double height, boolean land, double clearFrom){
         if(land && heights[ticks - 1] <= height) return false; // Has to come down onto the target
         for(int k = 1; k < ticks; k++){
            double along = horizontal * reach[k];
            if(land && startsOnGround && along < Math.min(START_CLEARANCE, distance / 2) && heights[k] <= 0)
               return false; // Would land back where it started
            if(land && along >= clearFrom && heights[k] < height)
               return false; // Would hit the side of the target block
            if(!land && heights[k] > height + 1.0E-6) return false;
         }
         return true;
      }
      
      // Launch speed that is height blocks up after ticks ticks
      private double verticalLaunch(double height, int ticks){
         double[] heights = new double[ticks + 1];
         double low = -1;
         double high = 1;
         for(int i = 0; i < 20 && trajectory(low, ticks, heights) > height; i++) low *= 2;
         for(int i = 0; i < 20 && trajectory(high, ticks, heights) < height; i++) high *= 2;
         for(int i = 0; i < 40; i++){
            double mid = (low + high) / 2;
            if(trajectory(mid, ticks, heights) < height){
               low = mid;
            }else{
               high = mid;
            }
         }
         return (low + high) / 2;
      }
      
      // Fills heights[0..ticks] with the height after each tick and returns the last one
      private double trajectory(double vertical, int ticks, double[] heights){
         double y = 0;
         heights[0] = 0;
         for(int k = 1; k <= ticks; k++){
            if(Math.abs(vertical) < 0.003) vertical = 0;
            y += vertical;
            heights[k] = y;
            if(levitating){
               vertical += (levitation - vertical) * 0.2;
            }else{
               vertical -= vertical <= 0 ? fallingGravity : gravity;
            }
            vertical *= verticalDrag;
         }
         return y;
      }
   }
}

