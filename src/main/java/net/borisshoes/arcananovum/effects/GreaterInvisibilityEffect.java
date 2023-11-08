package net.borisshoes.arcananovum.effects;

import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class GreaterInvisibilityEffect extends StatusEffect implements PolymerStatusEffect {
   
   public GreaterInvisibilityEffect(){
      super(StatusEffectCategory.BENEFICIAL,0xb7dded);
   }
   
   @Override
   public void applyUpdateEffect(LivingEntity entity, int amplifier){
      if(entity.getWorld() instanceof ServerWorld serverWorld && amplifier == 0){
         Vec3d pos = entity.getPos();
         serverWorld.spawnParticles(ParticleTypes.SMOKE,pos.x,pos.y+entity.getHeight()/2,pos.z,1,.4,.4,.4,0);
      }
   }
   
   @Override
   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }
   
   @Override
   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if(entity.getServer() != null){
         removeInvis(entity.getServer(),entity);
      }
      super.onRemoved(entity, attributes, amplifier);
   }
   
   @Override
   public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if(entity.getServer() != null){
         addInvis(entity.getServer(),entity);
      }
      super.onApplied(entity, attributes, amplifier);
   }
   
   private static void addInvis(MinecraftServer server, LivingEntity invisEntity){
      server.getPlayerManager().getPlayerList().forEach(playerEntity -> {
         if (!playerEntity.equals(invisEntity)) {
            
            AbstractTeam abstractTeam = invisEntity.getScoreboardTeam();
            if (abstractTeam != null && playerEntity.getScoreboardTeam() == abstractTeam && abstractTeam.shouldShowFriendlyInvisibles()) {
               return;
            }
            
            playerEntity.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(invisEntity.getId()));
            updateEquipment(invisEntity, playerEntity);
         }
      });
   }
   
   private static void removeInvis(MinecraftServer server, LivingEntity invisEntity){
      server.getPlayerManager().getPlayerList().forEach(playerEntity -> {
         if (!playerEntity.equals(invisEntity)) {
            
            AbstractTeam abstractTeam = invisEntity.getScoreboardTeam();
            if (abstractTeam != null && playerEntity.getScoreboardTeam() == abstractTeam && abstractTeam.shouldShowFriendlyInvisibles()) {
               return;
            }
            
            if(invisEntity instanceof ServerPlayerEntity invisPlayer){
               playerEntity.networkHandler.sendPacket(new PlayerSpawnS2CPacket(invisPlayer));
            }else{
               playerEntity.networkHandler.sendPacket(new EntitySpawnS2CPacket(invisEntity));
            }
            updateEquipment(invisEntity, playerEntity);
         }
      });
   }
   
   private static void updateEquipment(LivingEntity invisEntity, ServerPlayerEntity receiver) {
      List<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList<>();
      
      for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
         ItemStack itemStack = invisEntity.getEquippedStack(equipmentSlot);
         if (!itemStack.isEmpty()) {
            equipmentList.add(Pair.of(equipmentSlot, itemStack.copy()));
         }
      }
      
      if (!equipmentList.isEmpty()) {
         receiver.networkHandler.sendPacket(new EntityEquipmentUpdateS2CPacket(invisEntity.getId(), equipmentList));
      }
   }
}
