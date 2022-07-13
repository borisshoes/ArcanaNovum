package net.borisshoes.arcananovum.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Timer;
import java.util.TimerTask;

public class ParticleEffectUtils {
   
   public static void harnessFly(ServerWorld world, ServerPlayerEntity player, int duration){
      Vec3d pos = player.getPos();
      world.spawnParticles(ParticleTypes.END_ROD,pos.x,pos.y,pos.z,1,.3,.3,.3,0.05);
      world.spawnParticles(ParticleTypes.INSTANT_EFFECT,pos.x,pos.y,pos.z,1,.3,.3,.3,1);
      
      if(0 < duration){
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
            @Override
            public void run(){
               harnessFly(world, player,duration-1);
            }
         }, 100);
      }
   }
   
   public static void harnessStall(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.SMOKE,pos.x,pos.y+0.5,pos.z,100,.4,.6,.4,0.05);
      world.spawnParticles(ParticleTypes.ANGRY_VILLAGER,pos.x,pos.y+0.5,pos.z,15,.4,.6,.4,1);
      world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,pos.x,pos.y+0.5,pos.z,15,.4,.6,.4,0.07);
   }
   
   public static void shadowGlaiveTp(ServerWorld world, ServerPlayerEntity player){
      Vec3d pos = player.getPos();
      world.spawnParticles(ParticleTypes.LARGE_SMOKE,pos.x,pos.y+0.5,pos.z,100,.4,.4,.4,0.07);
   }
   
   public static void shulkerCoreLevitate(ServerWorld world, PlayerEntity player, int duration){
      Vec3d pos = player.getPos();
      world.spawnParticles(ParticleTypes.END_ROD,pos.x,pos.y+1,pos.z,1,.3,.3,.3,0.05);
   
      if(0 < duration){
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
            @Override
            public void run(){
               shulkerCoreLevitate(world, player,duration-1);
            }
         }, 50);
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
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
            @Override
            public void run(){
               recallTeleport(world,pos,tick+1);
            }
         }, 50);
      }
   }
   
   public static void stasisPearl(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.REVERSE_PORTAL,pos.x,pos.y,pos.z,5,.15,.15,.15,0.01);
      world.spawnParticles(ParticleTypes.GLOW,pos.x,pos.y,pos.z,5,.1,.1,.1,0);
   }
   
   private static void circle(ServerWorld world, @Nullable ServerPlayerEntity player, Vec3d center, ParticleEffect type, double radius, int intervals, int count, double delta, double speed){
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
   
   private static void sphere(ServerWorld world, @Nullable ServerPlayerEntity player, Vec3d center, ParticleEffect type, double radius, int points, int count, double delta, double speed){
      double phi = Math.PI * (3 - Math.sqrt(5));
      
      for(int i = 0; i < points; i++){
         // Fibonacci Sphere Equations
         double y = 1 - (i / (double)(points-1)) * 2;
         double r = Math.sqrt(1-y*y);
         double theta = phi*i;
         double x = Math.cos(theta) * r;
         double z = Math.sin(theta) * r;
         
         // Center Offset and Radius Scale
         Vec3d point = new Vec3d(x,y,z);
         point = point.multiply(radius).add(center.x, center.y, center.z);
      
         if(player == null){
            world.spawnParticles(type,point.x,point.y,point.z,count,delta,delta,delta,speed);
         }else{
            world.spawnParticles(player,type,false,x,y,z,count,delta,delta,delta,speed);
         }
      }
   }
}
