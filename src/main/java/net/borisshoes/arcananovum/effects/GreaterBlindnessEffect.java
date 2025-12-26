package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.tracker.PlayerMovementEntry;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class GreaterBlindnessEffect extends MobEffect implements PolymerStatusEffect {
   
   public GreaterBlindnessEffect(){
      super(MobEffectCategory.HARMFUL,0x13181a);
   }
   
   @Override
   public @Nullable MobEffect getPolymerReplacement(MobEffect potion, PacketContext context){
      return MobEffects.BLINDNESS.value();
   }
   
   @Override
   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier){
      return true;
   }
   
   @Override
   public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier){
      MinecraftServer server = entity.level().getServer();
      
      if(entity instanceof ServerPlayer player && server != null && server.getTickCount() % 10 == 0){
         ElementHolder holder = new ElementHolder(){
            int lifeTime = 15;
            
            @Override
            protected void onTick(){
               super.onTick();
               
               if(lifeTime % 5 == 0){
                  PlayerMovementEntry tracker = BorisLib.PLAYER_MOVEMENT_TRACKER.get(player);
                  Vec3 vel = tracker == null ? new Vec3(0,0,0) : tracker.velocity();
                  for(VirtualElement e : getElements()){
                     if(e instanceof BlockDisplayElement element){
                        element.setTranslation(vel.toVector3f().mul(1));
                        element.setStartInterpolation(-1);
                        element.setInterpolationDuration(5);
                        element.startInterpolation();
                     }
                  }
               }

               if(lifeTime-- <= 0 || player.isDeadOrDying() || player.hasDisconnected()){
                  setAttachment(null);
                  destroy(); // Time expired, remove
               }
            }
         };
         
         final float size = 1.5F + 1F*amplifier;
         for(Direction value : Direction.values()){
            BlockDisplayElement element = new BlockDisplayElement();
            Vec3 directionOffset = Vec3.atLowerCornerOf(value.getUnitVec3i()).scale(value.getAxisDirection().getStep());
            Vec3 scale = directionOffset.scale(-size + 0.25f).add(new Vec3(size, size, size));
            element.setScale(scale.toVector3f());
            element.setBlockState(Blocks.BLACK_CONCRETE.defaultBlockState());
            Vec3 centerOff = scale.scale(-0.5).add(0,entity.getEyeHeight(entity.getPose()),0);
            element.setOffset(centerOff.add(Vec3.atLowerCornerOf(value.getUnitVec3i()).scale(size/2)));
            
            holder.addElement(element);
         }
         
         
         EntityAttachment attachment = new EntityAttachment(holder,player,true);
         attachment.startWatching(player);
         
         for(ServerPlayer serverPlayer : server.getPlayerList().getPlayers()){
            if(serverPlayer != player){
               holder.stopWatching(serverPlayer);
               attachment.stopWatching(serverPlayer);
            }
         }
      }
      return super.applyEffectTick(world, entity,amplifier);
   }
}
