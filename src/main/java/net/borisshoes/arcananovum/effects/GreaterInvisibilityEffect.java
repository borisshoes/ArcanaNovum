package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class GreaterInvisibilityEffect extends StatusEffect implements PolymerStatusEffect {
   
   public GreaterInvisibilityEffect(){
      super(StatusEffectCategory.BENEFICIAL,0xb7dded);
   }
   
   @Override
   public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier){
      Vec3d pos = entity.getPos();
      world.spawnParticles(ParticleTypes.SMOKE,pos.x,pos.y+entity.getHeight()/2,pos.z,1,.4,.4,.4,0);
      return super.applyUpdateEffect(world, entity,amplifier);
   }
   
   @Override
   public boolean canApplyUpdateEffect(int duration, int amplifier){
      return true;
   }
   
   @Override
   public void onApplied(LivingEntity entity, int amplifier){
      if(entity.getServer() != null){
         addInvis(entity.getServer(),entity);
      }
      super.onApplied(entity, amplifier);
   }
   
   private static void addInvis(MinecraftServer server, LivingEntity invisEntity){
      server.getPlayerManager().getPlayerList().forEach(playerEntity -> {
         if(!playerEntity.equals(invisEntity)){
            
            AbstractTeam abstractTeam = invisEntity.getScoreboardTeam();
            if(abstractTeam != null && playerEntity.getScoreboardTeam() == abstractTeam && abstractTeam.shouldShowFriendlyInvisibles()){
               return;
            }
            
            playerEntity.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(invisEntity.getId()));
         }
      });
   }
   
   public static void removeInvis(MinecraftServer server, LivingEntity invisEntity){
      EntitySpawnS2CPacket addPacket = new EntitySpawnS2CPacket(invisEntity.getId(), invisEntity.getUuid(), invisEntity.getX(), invisEntity.getY(), invisEntity.getZ(), invisEntity.getPitch(), invisEntity.getYaw(), invisEntity.getType(), 0, Vec3d.ZERO, invisEntity.headYaw);
      server.getPlayerManager().getPlayerList().forEach(playerEntity -> {
         if(!playerEntity.equals(invisEntity) && invisEntity.getEntityWorld().equals(playerEntity.getEntityWorld())){
            
            AbstractTeam abstractTeam = invisEntity.getScoreboardTeam();
            if(abstractTeam != null && playerEntity.getScoreboardTeam() == abstractTeam && abstractTeam.shouldShowFriendlyInvisibles()){
               return;
            }
            
            Vec3d distVec = playerEntity.getPos().subtract(invisEntity.getPos());
            int viewDist = MathHelper.clamp(playerEntity.getViewDistance(), 2, playerEntity.getServerWorld().getChunkManager().chunkLoadingManager.watchDistance);
            ServerChunkLoadingManager.EntityTracker tracker = playerEntity.getServerWorld().getChunkManager().chunkLoadingManager.entityTrackers.get(playerEntity.getId());
            double maxTrackDist = viewDist * 16;
            if(tracker != null){
               maxTrackDist = Math.min(tracker.getMaxTrackDistance(), maxTrackDist);
            }
            double horizDistSq = distVec.x * distVec.x + distVec.z * distVec.z;
            double maxTrackDistSq = maxTrackDist * maxTrackDist;
            boolean reveal = horizDistSq <= maxTrackDistSq && !invisEntity.isSpectator();
            
            if(reveal){
               List<Packet<? super ClientPlayPacketListener>> list = new ArrayList<>();
               playerEntity.getServerWorld().getChunkManager().chunkLoadingManager.entityTrackers.get(invisEntity.getId()).entry.sendPackets(playerEntity, list::add);
               playerEntity.networkHandler.sendPacket(new BundleS2CPacket(list));
            }
         }
      });
      
      // This might be simpler to use in the future, but loses some of the logic and might cause issues
      // server.getWorlds().forEach(serverWorld -> serverWorld.getChunkManager().chunkLoadingManager.entityTrackers.get(invisEntity.getId()).sendToOtherNearbyPlayers(addPacket));
   }
}
