package net.borisshoes.arcananovum.ai.goals;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.charms.FelidaeCharm;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class FleeFelidaeCharmGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
   
   public FleeFelidaeCharmGoal(Creeper creeper, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed){
      super(creeper, fleeFromType, distance, slowSpeed, fastSpeed, Objects.requireNonNull(EntitySelector.NO_CREATIVE_OR_SPECTATOR)::test);
   }
   
   @Override
   public boolean canUse(){
      this.toAvoid = getServerLevel(this.mob).getNearestEntity(
            this.mob.level().getEntitiesOfClass(this.avoidClass, this.mob.getBoundingBox().inflate((double) this.maxDist, 3.0, (double) this.maxDist), (livingEntity) -> true),
            TargetingConditions.forCombat()
                  .range((double)this.maxDist)
                  .selector((entity, world) -> predicateOnAvoidEntity.test(entity) && avoidPredicate.test(entity)),
            this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
      if(this.toAvoid == null){
         return false;
      }else{
         Vec3 vec3d = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.position());
         if(vec3d == null){
            return false;
         }else if(this.toAvoid.distanceToSqr(vec3d.x, vec3d.y, vec3d.z) < this.toAvoid.distanceToSqr(this.mob)){
            return false;
         }else{
            this.path = this.pathNav.createPath(vec3d.x, vec3d.y, vec3d.z, 0);
            if(this.path != null){
               if(this.toAvoid instanceof Player player){
                  Inventory inv = player.getInventory();
                  for(int i = 0; i<inv.getContainerSize(); i++){
                     ItemStack item = inv.getItem(i);
                     if(item.isEmpty()){
                        continue;
                     }
   
                     boolean isArcane = ArcanaItemUtils.isArcane(item);
                     if(!isArcane)
                        continue; // Item not arcane, skip
                     
                     ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
                     if(arcanaItem instanceof FelidaeCharm || ArcanistsBelt.checkBeltAndHasItem(item,ArcanaRegistry.FELIDAE_CHARM.getItem())){
                        return true;
                     }
                  }
               }
            }
            return false;
         }
      }
   }
   
   @Override
   public void start(){
      super.start();
      this.mob.playSound(SoundEvents.CREEPER_HURT, 1, 1);
      if(this.toAvoid instanceof ServerPlayer player){
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.FELIDAE_CHARM_SCARE_CREEPER)); // Add xp
         SoundUtils.playSongToPlayer(player, SoundEvents.CAT_HISS, .1f, 1);
      }
   }
}