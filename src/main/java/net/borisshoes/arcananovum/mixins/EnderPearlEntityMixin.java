package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IMagicEntityComponent;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_ENTITY_LIST;

@Mixin(EnderPearlEntity.class)
public class EnderPearlEntityMixin {
   
   @Inject(method="onCollision",at=@At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/Random;nextFloat()F", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
   private void arcananovum_pearlActivate1(HitResult hitResult, CallbackInfo ci, Entity entity, ServerPlayerEntity player){
      EnderPearlEntity pearlEntity = (EnderPearlEntity) (Object) this;
      String uuid = pearlEntity.getUuidAsString();
      IMagicEntityComponent entityComponent = MAGIC_ENTITY_LIST.get(player.getWorld());
      List<MagicEntity> entities = entityComponent.getEntities();
      for(MagicEntity magicEntity : entities){
         NbtCompound magicData = magicEntity.getData();
         String id = magicData.getString("id");
         if(id.equals(MagicItems.STASIS_PEARL.getId())){
            if(uuid.equals(magicEntity.getUuid())){
               if(player.getPos().distanceTo(pearlEntity.getPos()) >= 1000){
                  ArcanaAchievements.grant(player, "instant_transmission");
               }
               int reconstructLvl = Math.max(0, ArcanaAugments.getAugmentFromCompound(magicData,"stasis_reconstruction"));
               if(reconstructLvl > 0){
                  StatusEffectInstance regen = new StatusEffectInstance(StatusEffects.REGENERATION, 100, reconstructLvl, false, true, true);
                  StatusEffectInstance resist = new StatusEffectInstance(StatusEffects.RESISTANCE, 60, reconstructLvl-1, false, true, true);
                  player.addStatusEffect(regen);
                  player.addStatusEffect(resist);
   
                  player.getWorld().spawnParticles(ParticleTypes.HAPPY_VILLAGER,pearlEntity.getX(),pearlEntity.getY()+player.getHeight()/2,pearlEntity.getZ(),10*reconstructLvl, .5,.5,.5,1);
               }
               break;
            }
         }
      }
   }
}
