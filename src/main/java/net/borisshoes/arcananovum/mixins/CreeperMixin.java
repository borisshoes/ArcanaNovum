package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ai.goals.FleeFelidaeCharmGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster {
   
   protected CreeperMixin(EntityType<? extends Monster> entityType, Level world){
      super(entityType, world);
   }
   
   @Inject(method = "registerGoals", at = @At("HEAD"))
   protected void initGoals(CallbackInfo ci){
      Creeper creeper = (Creeper) (Object) this;
      this.goalSelector.addGoal(1, new FleeFelidaeCharmGoal<>(creeper, Player.class, 8.0F, 1.0, 1.2));
   }
}
