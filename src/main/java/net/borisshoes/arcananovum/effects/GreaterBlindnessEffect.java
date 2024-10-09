package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static net.borisshoes.arcananovum.ArcanaNovum.PLAYER_MOVEMENT_TRACKER;

public class GreaterBlindnessEffect extends StatusEffect implements PolymerStatusEffect {
   
   public GreaterBlindnessEffect(){
      super(StatusEffectCategory.HARMFUL,0x13181a);
   }
   
   @Override
   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }
   
   @Override
   public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier){
      MinecraftServer server = entity.getServer();
      
      if(entity instanceof ServerPlayerEntity player && server != null && server.getTicks() % 10 == 0){
         ElementHolder holder = new ElementHolder() {
            int lifeTime = 15;
            
            @Override
            protected void onTick(){
               super.onTick();
               
               if(lifeTime % 5 == 0){
                  Pair<Vec3d,Vec3d> tracker = PLAYER_MOVEMENT_TRACKER.get(player);
                  Vec3d vel = tracker == null ? new Vec3d(0,0,0) : tracker.getRight();
                  for(VirtualElement e : getElements()){
                     if(e instanceof BlockDisplayElement element){
                        element.setTranslation(vel.toVector3f().mul(1));
                        element.setStartInterpolation(-1);
                        element.setInterpolationDuration(5);
                        element.startInterpolation();
                     }
                  }
               }

               if(lifeTime-- <= 0) {
                  setAttachment(null);
                  destroy(); // Time expired, remove
               }
            }
         };
         
         final float size = 1.5F + 1F*amplifier;
         for(Direction value : Direction.values()){
            BlockDisplayElement element = new BlockDisplayElement();
            Vec3d directionOffset = Vec3d.of(value.getVector()).multiply(value.getDirection().offset());
            Vec3d scale = directionOffset.multiply(-size + 0.25f).add(new Vec3d(size, size, size));
            element.setScale(scale.toVector3f());
            element.setBlockState(Blocks.BLACK_CONCRETE.getDefaultState());
            Vec3d centerOff = scale.multiply(-0.5).add(0,entity.getEyeHeight(entity.getPose()),0);
            element.setOffset(centerOff.add(Vec3d.of(value.getVector()).multiply(size/2)));
            
            holder.addElement(element);
         }
         
         
         EntityAttachment attachment = new EntityAttachment(holder,player,true);
         attachment.startWatching(player);
         
         for(ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()){
            if(serverPlayer != player){
               holder.stopWatching(serverPlayer);
               attachment.stopWatching(serverPlayer);
            }
         }
      }
      return super.applyUpdateEffect(world,entity,amplifier);
   }
}
