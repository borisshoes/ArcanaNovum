package net.borisshoes.arcananovum.ai.goals;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class FleeSteleGoal extends Goal {
   protected final PathfinderMob mob;
   private final double walkSpeedModifier;
   private final double sprintSpeedModifier;
   protected BlockPos toFlee;
   protected GeomanticSteleBlockEntity.SteleZone zone;
   protected final float maxDist;
   @Nullable
   protected Path path;
   protected final PathNavigation pathNav;
   protected final Predicate<ItemStack> avoidPredicate;
   
   
   public FleeSteleGoal(PathfinderMob mob, double walkSpeedModifier, double sprintSpeedModifier, float maxDist, Predicate<ItemStack> avoidPredicate){
      this.mob = mob;
      this.walkSpeedModifier = walkSpeedModifier;
      this.sprintSpeedModifier = sprintSpeedModifier;
      this.maxDist = maxDist;
      this.pathNav = mob.getNavigation();
      this.avoidPredicate = avoidPredicate;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }
   
   @Override
   public boolean canUse(){
      List<GeomanticSteleBlockEntity.SteleZone> zones = GeomanticSteleBlockEntity.getZonesAtEntity(this.mob, avoidPredicate);
      if(zones.isEmpty()){
         return false;
      }else{
         zones.sort(Comparator.comparingDouble(stele -> stele.getBlockEntity().getBlockPos().distSqr(this.mob.blockPosition())));
         GeomanticSteleBlockEntity.SteleZone zone = zones.getFirst();
         this.toFlee = zone.getBlockEntity().getBlockPos();
         Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toFlee.getCenter());
         if(vec3 == null){
            this.toFlee = null;
            return false;
         }else if(this.toFlee.distToCenterSqr(vec3.x, vec3.y, vec3.z) < this.toFlee.distToCenterSqr(this.mob.position())){
            this.toFlee = null;
            return false;
         }else{
            this.zone = zone;
            this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
            return this.path != null;
         }
      }
   }
   
   @Override
   public boolean canContinueToUse(){
      return !this.pathNav.isDone();
   }
   
   @Override
   public void start(){
      this.pathNav.moveTo(this.path, this.walkSpeedModifier);
      this.mob.playSound(SoundEvents.CREEPER_HURT, 1, 1);
      this.zone.getBlockEntity().giveXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_SCARE_CREEPER));
   }
   
   @Override
   public void stop(){
      this.toFlee = null;
      this.zone = null;
   }
   
   @Override
   public void tick(){
      if(this.mob.distanceToSqr(this.toFlee.getCenter()) < 49.0){
         this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
      }else{
         this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
      }
   }
}
