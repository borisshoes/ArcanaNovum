package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerMobEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

import java.util.ArrayList;
import java.util.List;

public class GreaterInvisibilityEffect extends MobEffect implements PolymerMobEffect {
   
   public GreaterInvisibilityEffect(){
      super(MobEffectCategory.BENEFICIAL, 0xb7dded);
   }
   
   @Override
   public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier){
      Vec3 pos = entity.position();
      world.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y + entity.getBbHeight() / 2, pos.z, 1, .4, .4, .4, 0);
      return super.applyEffectTick(world, entity, amplifier);
   }
   
   @Override
   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier){
      return true;
   }
   
   @Override
   public void onEffectStarted(LivingEntity entity, int amplifier){
      if(entity.level().getServer() != null){
         addInvis(entity.level().getServer(), entity);
      }
      super.onEffectStarted(entity, amplifier);
   }
   
   private static void addInvis(MinecraftServer server, LivingEntity invisEntity){
      server.getPlayerList().getPlayers().forEach(playerEntity -> {
         if(!playerEntity.equals(invisEntity)){
            
            Team abstractTeam = invisEntity.getTeam();
            if(abstractTeam != null && playerEntity.getTeam() == abstractTeam && abstractTeam.canSeeFriendlyInvisibles()){
               return;
            }
            
            playerEntity.connection.send(new ClientboundRemoveEntitiesPacket(invisEntity.getId()));
         }
      });
   }
   
   public static void removeInvis(MinecraftServer server, LivingEntity invisEntity){
      ClientboundAddEntityPacket addPacket = new ClientboundAddEntityPacket(invisEntity.getId(), invisEntity.getUUID(), invisEntity.getX(), invisEntity.getY(), invisEntity.getZ(), invisEntity.getXRot(), invisEntity.getYRot(), invisEntity.getType(), 0, Vec3.ZERO, invisEntity.yHeadRot);
      server.getPlayerList().getPlayers().forEach(playerEntity -> {
         if(!playerEntity.equals(invisEntity) && invisEntity.level().equals(playerEntity.level())){
            
            Team abstractTeam = invisEntity.getTeam();
            if(abstractTeam != null && playerEntity.getTeam() == abstractTeam && abstractTeam.canSeeFriendlyInvisibles()){
               return;
            }
            
            Vec3 distVec = playerEntity.position().subtract(invisEntity.position());
            int viewDist = Mth.clamp(playerEntity.requestedViewDistance(), 2, playerEntity.level().getChunkSource().chunkMap.serverViewDistance);
            ChunkMap.TrackedEntity tracker = playerEntity.level().getChunkSource().chunkMap.entityMap.get(playerEntity.getId());
            double maxTrackDist = viewDist * 16;
            if(tracker != null){
               maxTrackDist = Math.min(tracker.getEffectiveRange(), maxTrackDist);
            }
            double horizDistSq = distVec.x * distVec.x + distVec.z * distVec.z;
            double maxTrackDistSq = maxTrackDist * maxTrackDist;
            boolean reveal = horizDistSq <= maxTrackDistSq && !invisEntity.isSpectator();
            
            if(reveal){
               List<Packet<? super ClientGamePacketListener>> list = new ArrayList<>();
               playerEntity.level().getChunkSource().chunkMap.entityMap.get(invisEntity.getId()).serverEntity.sendPairingData(playerEntity, list::add);
               playerEntity.connection.send(new ClientboundBundlePacket(list));
            }
         }
      });
      
      // This might be simpler to use in the future, but loses some of the logic and might cause issues
      // server.getWorlds().forEach(serverWorld -> serverWorld.getChunkManager().chunkLoadingManager.entityTrackers.get(invisEntity.getId()).sendToOtherNearbyPlayers(addPacket));
   }
}
