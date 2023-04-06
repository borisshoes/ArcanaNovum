package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.Arcananovum;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.block.Blocks;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;
import java.util.TimerTask;

public class ParticleEffectUtils {
   
   public static void nulConstructNecroticShroud(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.LARGE_SMOKE,pos.getX(),pos.getY()+1.5,pos.getZ(),150,1.5,1.5,1.5,0.07);
   }
   
   public static void nulConstructDarkConversion(ServerWorld world, Vec3d pos){
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0x9e0945).toVector3f(),0.8f);
      world.spawnParticles(dust, pos.getX(), pos.getY() + 1.75, pos.getZ(), 20,0.75,1,0.75,0.03);
   }
   
   public static void nulConstructReflectiveArmor(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.END_ROD, pos.getX(), pos.getY() + 1.75, pos.getZ(), 5,0.75,1,0.75,0.03);
   }
   
   public static void nulConstructCurseOfDecay(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.SOUL, pos.getX(), pos.getY() + 1, pos.getZ(), 20,0.5,1,0.5,0.07);
   }
   
   public static void nulConstructReflexiveBlast(ServerWorld world, Vec3d pos, int calls){
      double radius = .5+calls*4;
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0x36332b).toVector3f(),1.5f);
      sphere(world,null,pos,dust,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,calls*Math.PI*2/5);
      if(calls < 5){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(1, new TimerTask() {
            @Override
            public void run(){
               nulConstructReflexiveBlast(world,pos,calls + 1);
            }
         }));
      }
   }
   
   public static void webOfFireCast(ServerWorld world, ParticleEffect type, ServerPlayerEntity caster, List<LivingEntity> hits, double range, int calls){
      final int totalCalls = 15;
      
      Vec3d center = caster.getPos().add(0,caster.getHeight()*.25,0);
      if(calls%2 == 0 && calls < 5){
         circle(world,null,center,type,range,(int)(10*range),1,0.05,0.01);
         circle(world,null,center,type,2,20,1,0.05,0.01);
   
         for(LivingEntity hit : hits){
            Vec3d hitCircle = new Vec3d(hit.getX(),center.getY(),hit.getZ());
            circle(world,null,hitCircle,type,hit.getWidth(),12,1,0,0);
            line(world,null,center,hitCircle,type,(int)(center.distanceTo(hitCircle)*4),1,0,0);
            
            for(LivingEntity other : hits){
               if(other.getUuidAsString().equals(hit.getUuidAsString())) continue;
               Vec3d otherCircle = new Vec3d(other.getX(),center.getY(),other.getZ());
               line(world,null,otherCircle,hitCircle,type,(int)(otherCircle.distanceTo(hitCircle)*2.5),1,0,0);
            }
         }
      }
   
      for(LivingEntity hit : hits){
         double heightMod = (double)calls/totalCalls;
         double height = hit.getY() + hit.getHeight()*heightMod;
         double radiusMod = 1.0 - (double)calls/(totalCalls*1.5);
         double radius = hit.getWidth()*.75 * radiusMod;
         Vec3d circlePos = new Vec3d(hit.getX(),height,hit.getZ());
         circle(world,null,circlePos,type,radius,12,1,0,0.01);
      }
      
      if(calls < totalCalls){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(1, new TimerTask() {
            @Override
            public void run(){
               webOfFireCast(world, type,caster,hits,range,calls + 1);
            }
         }));
      }
   }
   
   public static void pyroblastExplosion(ServerWorld world, ParticleEffect type, Vec3d pos, double range, int calls){
      double radius = .5+calls*(range/5);
      sphere(world,null,pos,type,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,calls*Math.PI*2/5);
      if(calls < 5){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(1, new TimerTask() {
            @Override
            public void run(){
               pyroblastExplosion(world, type,pos,range,calls + 1);
            }
         }));
      }
   }
   
   public static void spawnerInfuser(ServerWorld world, BlockPos pos, int duration){
      for(int i = 0; i < duration; i++) {
         world.spawnParticles(new ShriekParticleEffect(i * 5), (double)pos.getX() + 0.5, (double)pos.getY() + SculkShriekerBlock.TOP-0.5, (double)pos.getZ() + 0.5, 1,0.0, 0.0, 0.0,0);
         world.spawnParticles(new ShriekParticleEffect(i * 5+2), (double)pos.getX() + 0.5, (double)pos.getY() + SculkShriekerBlock.TOP-0.5, (double)pos.getZ() + 0.5, 1,0.0, 0.0, 0.0,0);
      }
      world.spawnParticles(ParticleTypes.SCULK_SOUL, (double)pos.getX() + 0.5, (double)pos.getY() + 2.5, (double)pos.getZ() + 0.5, 5,0.5, 0.5, 0.5,0.02);
      world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, (double)pos.getX() + 0.5, (double)pos.getY() + 2.5, (double)pos.getZ() + 0.5, 5,0.3, 0.3, 0.3,0.02);
   }
   
   public static void arcaneFlakArrowDetonate(ServerWorld world, Vec3d pos, double range, int calls){
      //ParticleEffect dust = new DustParticleEffect(new Vector3f(Vec3d.unpackRgb(0x0085de)),1.4f);
      double radius = .5+calls*(range/5.0);
      double radius2 = radius*.75;
      sphere(world,null,pos,ParticleTypes.WITCH,radius,(int)(radius*radius+radius*10+10),3,0.3,0,0);
      sphere(world,null,pos,ParticleTypes.DRAGON_BREATH,radius2,(int)(radius2*radius2+radius2*5+10),3,0.3,0,0);
      world.spawnParticles(ParticleTypes.FLASH,pos.x,pos.y,pos.z,1,0,0,0,1);
      
      if(calls < 5){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(1, new TimerTask() {
            @Override
            public void run(){
               arcaneFlakArrowDetonate(world, pos,range,calls + 1);
            }
         }));
      }
   }
   
   public static void gravitonArrowEmit(ServerWorld world, Vec3d center, List<Entity> entities){
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0x000ea8).toVector3f(),1f);
      ParticleEffect dust2 = new DustParticleEffect( Vec3d.unpackRgb(0x000754).toVector3f(),1.5f);
      int count = 30;
      double range = .3;
   
      world.spawnParticles(dust,center.x,center.y,center.z,300,1.5,1.5,1.5,.01);
      world.spawnParticles(ParticleTypes.PORTAL,center.x,center.y,center.z,100,.5,.5,.5,1);
      sphere(world,null,center,dust2,.6,50,2,0.1,0,0);
      
      for(Entity e : entities){
         Vec3d pos = e.getPos().add(0,e.getHeight()/2,0);
         world.spawnParticles(dust,pos.x,pos.y,pos.z,count,range,range,range,.01);
      }
   }
   
   public static void expulsionArrowEmit(ServerWorld world, Vec3d pos, double range, int calls){
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0x0085de).toVector3f(),1.4f);
      double radius = .5+calls*(range/5);
      sphere(world,null,pos,dust,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,0);
      if(calls < 5){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(1, new TimerTask() {
            @Override
            public void run(){
               expulsionArrowEmit(world, pos,range,calls + 1);
            }
         }));
      }
   }
   
   public static void smokeArrowEmit(ServerWorld world, @Nullable Vec3d start, @Nullable Entity entity, double range, int calls){
      if(start == null && entity == null) return;
      Vec3d pos = entity == null ? start : entity.getPos();
      int count = (int)(40*range*range);
      
      List<ServerPlayerEntity> players = world.getPlayers(player -> player.squaredDistanceTo(pos) < 15000);
      for(ServerPlayerEntity player : players){
         world.spawnParticles(player,ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,true,pos.x,pos.y,pos.z,count/2,range,range,range,.01);
         world.spawnParticles(player,ParticleTypes.LARGE_SMOKE,true,pos.x,pos.y,pos.z,count/3,range,range,range,.01);
         world.spawnParticles(player,ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,false,pos.x,pos.y,pos.z,count/2,range,range,range,.01);
         world.spawnParticles(player,ParticleTypes.LARGE_SMOKE,false,pos.x,pos.y,pos.z,2*count/3,range,range,range,.01);
      }
      
      if(calls < 20){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(5, new TimerTask() {
            @Override
            public void run(){
               smokeArrowEmit(world, pos, entity,range,calls + 1);
            }
         }));
      }
   }
   
   public static void concussionArrowShot(ServerWorld world, Vec3d pos, double range, int calls){
      double radius = .5+calls*(range/5);
      sphere(world,null,pos,ParticleTypes.SQUID_INK,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,0);
      if(calls < 5){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(1, new TimerTask() {
            @Override
            public void run(){
               concussionArrowShot(world, pos, range,calls + 1);
            }
         }));
      }
   }
   
   public static void photonArrowShot(ServerWorld world, LivingEntity entity, Vec3d p2, float brightness){
      Vec3d p1 = entity.getEyePos().subtract(0,entity.getHeight()/4,0);
      int intervals = (int) (p1.subtract(p2).length() * 10);
      double delta = 0.03;
      double speed = 1;
      int count = 3;
      double dx = (p2.x-p1.x)/intervals;
      double dy = (p2.y-p1.y)/intervals;
      double dz = (p2.z-p1.z)/intervals;
      for(int i = 0; i < intervals; i++){
         double x = p1.x + dx*i;
         double y = p1.y + dy*i;
         double z = p1.z + dz*i;
         
         float hue = i/((float)intervals);
         Color c = Color.getHSBColor(hue, 1f, brightness);
         ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(c.getRGB()).toVector3f(),.6f);
         
         spawnLongParticle(world,dust,x,y,z,delta,delta,delta,speed,count);
      }
      spawnLongParticle(world,ParticleTypes.WAX_OFF,p2.x,p2.y,p2.z,0.2,0.2,0.2,1,10);
   }
   
   public static void tetherArrowEntity(ServerWorld world, LivingEntity entity, ServerPlayerEntity player){
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0xa6a58a).toVector3f(),.4f);
      double len = player.getPos().subtract(entity.getPos()).length();
      longDistLine(world,player.getPos().add(0,player.getHeight()/2,0),entity.getPos().add(0,entity.getHeight()/2,0),dust,(int)(20*len),3,0.03,1);
   }
   
   public static void tetherArrowGrapple(ServerWorld world, ServerPlayerEntity player, Vec3d pos){
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0xa6a58a).toVector3f(),.4f);
      double len = player.getPos().subtract(pos).length();
      longDistLine(world,player.getPos(),pos,dust,(int)(20*len),3,0.03,1);
   }
   
   public static void blinkArrowTp(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.REVERSE_PORTAL,pos.x,pos.y,pos.z,100,.3,.5,.3,0.05);
   }
   
   public static void harnessFly(ServerWorld world, ServerPlayerEntity player, int duration){
      Vec3d pos = player.getPos();
      world.spawnParticles(ParticleTypes.END_ROD,pos.x,pos.y,pos.z,1,.3,.3,.3,0.05);
      world.spawnParticles(ParticleTypes.INSTANT_EFFECT,pos.x,pos.y,pos.z,1,.3,.3,.3,1);
      
      if(0 < duration){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(2, new TimerTask() {
            @Override
            public void run(){
               harnessFly(world, player,duration-1);
            }
         }));
      }
   }
   
   public static void harnessStall(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.SMOKE,pos.x,pos.y+0.5,pos.z,100,.4,.6,.4,0.05);
      world.spawnParticles(ParticleTypes.ANGRY_VILLAGER,pos.x,pos.y+0.5,pos.z,15,.4,.6,.4,1);
      world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,pos.x,pos.y+0.5,pos.z,15,.4,.6,.4,0.07);
   }
   
   public static void dowsingRodEmitter(ServerWorld world, Vec3d pos, int calls, int duration){
      if(world.getBlockState(new BlockPos(pos)).getBlock() != Blocks.ANCIENT_DEBRIS) return;
      
      spawnLongParticle(world,ParticleTypes.FLAME,pos.x+0.5,pos.y+0.5,pos.z+0.5,.4,.4,.4,.05,3);
      
      if(calls < (duration)){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(3, new TimerTask() {
            @Override
            public void run(){
               dowsingRodEmitter(world, pos, calls + 1, duration);
            }
         }));
      }
   }
   
   public static void dowsingRodArrow(ServerWorld world, Vec3d start, Vec3d end, int calls){
      line(world,null,start,end,ParticleTypes.FLAME,8,3,.08,0);
      if(calls < (16)){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(5, new TimerTask() {
            @Override
            public void run(){
               dowsingRodArrow(world, start, end, calls + 1);
            }
         }));
      }
   }
   
   public static void shadowGlaiveTp(ServerWorld world, ServerPlayerEntity player){
      Vec3d pos = player.getPos();
      world.spawnParticles(ParticleTypes.LARGE_SMOKE,pos.x,pos.y+0.5,pos.z,100,.4,.4,.4,0.07);
   }
   
   public static void shulkerCoreLevitate(ServerWorld world, PlayerEntity player, int duration){
      if(player.getStatusEffect(StatusEffects.LEVITATION) == null) return;
      Vec3d pos = player.getPos();
      world.spawnParticles(ParticleTypes.END_ROD,pos.x,pos.y+1,pos.z,1,.3,.3,.3,0.05);
   
      if(0 < duration){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(1, new TimerTask() {
            @Override
            public void run(){
               shulkerCoreLevitate(world, player,duration-1);
            }
         }));
      }
   }
   
   public static void recallTeleportCharge(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.PORTAL,pos.x,pos.y+.5,pos.z,20,.2,.5,.2,1);
      world.spawnParticles(ParticleTypes.WITCH,pos.x,pos.y+1,pos.z,2,.1,.2,.1,1);
   }
   
   public static void recallTeleportCancel(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.SMOKE,pos.x,pos.y+.5,pos.z,150,.5,.8,.5,0.05);
   }
   
   public static void recallLocation(ServerWorld world, Vec3d pos, ServerPlayerEntity player){
      circle(world,player,pos.subtract(0,0,0),ParticleTypes.ENCHANTED_HIT,0.5,12,1,0.1,0);
      world.spawnParticles(player, ParticleTypes.WITCH, false, pos.x,pos.y,pos.z,5,.15,.15,.15,0);
   }
   
   public static void recallTeleport(ServerWorld world, Vec3d pos){ recallTeleport(world, pos, 0); }
   
   private static void recallTeleport(ServerWorld world, Vec3d pos, int tick){
      int animLength = 30;
      
      if(tick < 5){
         world.spawnParticles(ParticleTypes.REVERSE_PORTAL,pos.x,pos.y+.5,pos.z,30,.1,.4,.1,0.2);
         world.spawnParticles(ParticleTypes.FALLING_OBSIDIAN_TEAR,pos.x,pos.y+.5,pos.z,10,.6,.6,.6,0.2);
      }
      circle(world,null,pos.subtract(0,0.5,0),ParticleTypes.WITCH,1,20,1,0.1,0);
      
      if(tick < animLength){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(1, new TimerTask() {
            @Override
            public void run(){
               recallTeleport(world,pos,tick+1);
            }
         }));
      }
   }
   
   public static void stasisPearl(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.REVERSE_PORTAL,pos.x,pos.y,pos.z,5,.15,.15,.15,0.01);
      world.spawnParticles(ParticleTypes.GLOW,pos.x,pos.y,pos.z,5,.1,.1,.1,0);
   }
   
   public static void dragonBossTowerCircleInvuln(ServerWorld world, Vec3d center, int period, int calls){
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(9109665).toVector3f(),.8f);
      ParticleEffect dust2 = new DustParticleEffect(Vec3d.unpackRgb(9109665).toVector3f(),1.5f);
      double r = 2.5;
      float t = (float)(Math.PI/(period/100)*calls);
      double sqrt3 = Math.sqrt(3);
      
      circle(world,null,center,dust,r,60,1,0,1);
      //circle(world,null,center,dust,1.1*r,100,1,0,1);
      //circle(world,null,center,dust,r/2,30,1,0.,1);
      //circle(world,null,center,dust,2*sqrt3/3,30,1,0,1);
      
      Vec3d[] tri1 = {new Vec3d(0, 0, r),new Vec3d(-r*sqrt3/2, 0, -r/2),new Vec3d(r*sqrt3/2, 0, -r/2)};
      Vec3d[] tri2 = {new Vec3d(0, 0, -r),new Vec3d(-r*sqrt3/2, 0, r/2),new Vec3d(r*sqrt3/2, 0, +r/2)};
      for(int i = 0; i < 3; i++){
         Vec3d p1 = tri1[i].rotateY(t).add(center);
         Vec3d p2 = tri1[(i+1)%3].rotateY(t).add(center);
         Vec3d p3 = tri2[i].rotateY(t).add(center);
         Vec3d p4 = tri2[(i+1)%3].rotateY(t).add(center);
         line(world,null,p1,p2,dust,20,1,0,1);
         line(world,null,p3,p4,dust,20,1,0,1);
      }
   
      double steps = 60.0;
      double radius = 1.75;
      double height = 5.5;
      int num = 6;
      int concurrent = 4;
      double[][] angles = new double[num][concurrent];
      for(int i = 0; i<angles[0].length;i++){
         int invulnAnimTick = Math.floorMod((int) (calls-steps*i/concurrent), (int) steps);
      
         r = -(2*radius / steps) * Math.abs(invulnAnimTick - (steps / 2.0)) + radius;
         for(int j = 0; j < angles.length; j++){
            angles[j][i] = -((2 * Math.PI / angles.length) * j + invulnAnimTick / 10.0);
            double x = r * Math.cos(angles[j][i]) + (center.x);
            double z = r * Math.sin(angles[j][i]) + (center.z);
            double y = height * invulnAnimTick / steps + (center.y-1.25);
            world.spawnParticles(dust2, x, y, z, 1, 0, 0, 0,1);
         }
      }
   
      if(calls < (period/100)){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(2, new TimerTask() {
            @Override
            public void run(){
               dragonBossTowerCircleInvuln(world, center, period, calls + 1);
            }
         }));
      }
   }
   
   public static void dragonBossTowerCirclePush(ServerWorld world, Vec3d center, int period, int calls){
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(16711892).toVector3f(),2f);
      ParticleEffect dustLarge = new DustParticleEffect(Vec3d.unpackRgb(16711892).toVector3f(),3f);
      double r = 1.05*4;
      float t = -(float)(Math.PI/(period/100)*calls + Math.PI);
      double sqrt3 = Math.sqrt(3);
   
      circle(world,null,center,dust,r,40,1,0,1);
      
      Vec3d[] tri1 = {new Vec3d(0, 0, r),new Vec3d(-r*sqrt3/2, 0, -r/2),new Vec3d(r*sqrt3/2, 0, -r/2)};
      Vec3d[] tri2 = {new Vec3d(0, 0, -r),new Vec3d(-r*sqrt3/2, 0, r/2),new Vec3d(r*sqrt3/2, 0, +r/2)};
      for(int i = 0; i < 3; i++){
         Vec3d p1 = tri1[i].rotateY(t).add(center);
         Vec3d p2 = tri1[(i+1)%3].rotateY(t).add(center);
         Vec3d p3 = tri2[i].rotateY(t).add(center);
         Vec3d p4 = tri2[(i+1)%3].rotateY(t).add(center);
         line(world,null,p1,p2,dust,12,1,0,1);
         line(world,null,p3,p4,dust,12,1,0,1);
      }
      
      sphere(world,null,center.add(0,2,0),dustLarge,5.5,25,1,0,1,-t);
      
      if(calls < (period/100)){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(2, new TimerTask() {
            @Override
            public void run(){
               dragonBossTowerCirclePush(world, center, period, calls + 1);
            }
         }));
      }
   }
   
   public static void dragonReclaimTowerCircle(ServerWorld world, Vec3d center, int period, int calls){
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(4044031).toVector3f(),1.5f);
      double r = 1.05*4;
      float t = -(float)(Math.PI/(period/100)*calls + Math.PI);
      double sqrt3 = Math.sqrt(3);
      
      circle(world,null,center,dust,r,40,1,0,1);
      
      Vec3d[] tri1 = {new Vec3d(0, 0, r),new Vec3d(-r*sqrt3/2, 0, -r/2),new Vec3d(r*sqrt3/2, 0, -r/2)};
      Vec3d[] tri2 = {new Vec3d(0, 0, -r),new Vec3d(-r*sqrt3/2, 0, r/2),new Vec3d(r*sqrt3/2, 0, +r/2)};
      for(int i = 0; i < 3; i++){
         Vec3d p1 = tri1[i].rotateY(t).add(center);
         Vec3d p2 = tri1[(i+1)%3].rotateY(t).add(center);
         Vec3d p3 = tri2[i].rotateY(t).add(center);
         Vec3d p4 = tri2[(i+1)%3].rotateY(t).add(center);
         line(world,null,p1,p2,dust,16,1,0,1);
         line(world,null,p3,p4,dust,16,1,0,1);
      }
      
      if(calls < (period/100)){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(2, new TimerTask() {
            @Override
            public void run(){
               dragonReclaimTowerCircle(world, center, period, calls + 1);
            }
         }));
      }
   }
   
   public static void dragonReclaimTowerShield(ServerWorld world, Vec3d center, int calls){
      int period = 15000;
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(9694975).toVector3f(),1.5f);
      float t = -(float)(Math.PI/(period/200)*calls + Math.PI);
      
      sphere(world,null,center.add(0,2,0),dust,5.5,75,1,0,1,-t);
      
      if(calls < (period/200)){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(2, new TimerTask() {
            @Override
            public void run(){
               dragonReclaimTowerShield(world, center, calls + 1);
            }
         }));
      }
   }
   
   public static void dragonBossWizardPulse(ServerWorld world, Vec3d center, int ticks){
      double radius = ticks/4.0;
      double theta = 2*Math.PI / 20.0;
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(16711892).toVector3f(),(float)radius/2);
      sphere(world,null,center,dust,radius,(int)(radius*radius+radius*10+radius),1,0,1,theta*ticks);
   }
   
   public static void longDistLine(ServerWorld world, Vec3d p1, Vec3d p2, ParticleEffect type, int intervals, int count, double delta, double speed){
      double dx = (p2.x-p1.x)/intervals;
      double dy = (p2.y-p1.y)/intervals;
      double dz = (p2.z-p1.z)/intervals;
      for(int i = 0; i < intervals; i++){
         double x = p1.x + dx*i;
         double y = p1.y + dy*i;
         double z = p1.z + dz*i;
   
         spawnLongParticle(world,type,x,y,z,delta,delta,delta,speed,count);
      }
   }
   
   public static void line(ServerWorld world, @Nullable ServerPlayerEntity player, Vec3d p1, Vec3d p2, ParticleEffect type, int intervals, int count, double delta, double speed){
      double dx = (p2.x-p1.x)/intervals;
      double dy = (p2.y-p1.y)/intervals;
      double dz = (p2.z-p1.z)/intervals;
      for(int i = 0; i < intervals; i++){
         double x = p1.x + dx*i;
         double y = p1.y + dy*i;
         double z = p1.z + dz*i;
         
         if(player == null){
            world.spawnParticles(type,x,y,z,count,delta,delta,delta,speed);
         }else{
            world.spawnParticles(player,type,false,x,y,z,count,delta,delta,delta,speed);
         }
      }
   }
   
   public static void longDistCircle(ServerWorld world, Vec3d center, ParticleEffect type, double radius, int intervals, int count, double delta, double speed){
      double dA = Math.PI * 2 / intervals;
      for(int i = 0; i < intervals; i++){
         double angle = dA * i;
         double x = radius * Math.cos(angle) + center.x;
         double z = radius * Math.sin(angle) + center.z;
         double y = center.y;
   
         spawnLongParticle(world,type,x,y,z,delta,delta,delta,speed,count);
      }
   }
   
   public static void circle(ServerWorld world, @Nullable ServerPlayerEntity player, Vec3d center, ParticleEffect type, double radius, int intervals, int count, double delta, double speed){
      double dA = Math.PI * 2 / intervals;
      for(int i = 0; i < intervals; i++){
         double angle = dA * i;
         double x = radius * Math.cos(angle) + center.x;
         double z = radius * Math.sin(angle) + center.z;
         double y = center.y;
         
         if(player == null){
            world.spawnParticles(type,x,y,z,count,delta,delta,delta,speed);
         }else{
            world.spawnParticles(player,type,false,x,y,z,count,delta,delta,delta,speed);
         }
      }
   }
   
   public static void longDistSphere(ServerWorld world, Vec3d center, ParticleEffect type, double radius, int points, int count, double delta, double speed, double theta){
      double phi = Math.PI * (3 - Math.sqrt(5));
      
      for(int i = 0; i < points; i++){
         // Fibonacci Sphere Equations
         double y = 1 - (i / (double)(points-1)) * 2;
         double r = Math.sqrt(1-y*y);
         double t = phi*i + theta;
         double x = Math.cos(t) * r;
         double z = Math.sin(t) * r;
         
         // Center Offset and Radius Scale
         Vec3d point = new Vec3d(x,y,z);
         point = point.multiply(radius).add(center.x, center.y, center.z);
   
         spawnLongParticle(world,type,point.x,point.y,point.z,delta,delta,delta,speed,count);
      }
   }
   
   public static void sphere(ServerWorld world, @Nullable ServerPlayerEntity player, Vec3d center, ParticleEffect type, double radius, int points, int count, double delta, double speed, double theta){
      double phi = Math.PI * (3 - Math.sqrt(5));
      
      for(int i = 0; i < points; i++){
         // Fibonacci Sphere Equations
         double y = 1 - (i / (double)(points-1)) * 2;
         double r = Math.sqrt(1-y*y);
         double t = phi*i + theta;
         double x = Math.cos(t) * r;
         double z = Math.sin(t) * r;
         
         // Center Offset and Radius Scale
         Vec3d point = new Vec3d(x,y,z);
         point = point.multiply(radius).add(center.x, center.y, center.z);
      
         if(player == null){
            world.spawnParticles(type,point.x,point.y,point.z,count,delta,delta,delta,speed);
         }else{
            world.spawnParticles(player,type,false,point.x,point.y,point.z,count,delta,delta,delta,speed);
         }
      }
   }
   // Notes about the Dust Particle, size goes from .01 to 4, you can use an int represented rgb value with new Vector3f(Vec3d.unpackRgb(int))
   
   public static void spawnLongParticle(ServerWorld world, ParticleEffect type, double x, double y, double z, double dx, double dy, double dz, double speed, int count){
      List<ServerPlayerEntity> players = world.getPlayers(player -> player.squaredDistanceTo(new Vec3d(x,y,z)) < 512*512);
      for(ServerPlayerEntity player : players){
         player.networkHandler.sendPacket(new ParticleS2CPacket(type,true,x,y,z,(float)dx,(float)dy,(float)dz,(float)speed,count));
      }
   }
}
