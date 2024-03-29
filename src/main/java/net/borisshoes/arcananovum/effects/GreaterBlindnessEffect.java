package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.ManualAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.utils.RepeatTimer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class GreaterBlindnessEffect extends StatusEffect implements PolymerStatusEffect {
   
   public GreaterBlindnessEffect(){
      super(StatusEffectCategory.HARMFUL,0x13181a);
   }
   
   @Override
   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }
   
   @Override
   public void applyUpdateEffect(LivingEntity entity, int amplifier){
      MinecraftServer server = entity.getServer();
      
      if(entity instanceof ServerPlayerEntity player && server != null && server.getTicks() % 10 == 0){
         ElementHolder holder = new ElementHolder() {
            int lifeTime = 15;
            
            @Override
            protected void onTick(){
               super.onTick();
               
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
         
         HolderAttachment attachment = new ManualAttachment(holder, player.getServerWorld(), player::getPos);
         holder.setAttachment(attachment);
         attachment.startWatching(player);
         attachment.updateTracking(player.networkHandler);
         attachment.holder().startWatching(player);
         
         ArcanaNovum.addTickTimerCallback(new RepeatTimer(1,16, attachment::tick,null));
      }
   }
}
