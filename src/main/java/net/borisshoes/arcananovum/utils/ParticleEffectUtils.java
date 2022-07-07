package net.borisshoes.arcananovum.utils;

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
   
   
   public static void recallTeleportCharge(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.PORTAL,pos.x,pos.y+.5,pos.z,20,.2,.5,.2,1);
      world.spawnParticles(ParticleTypes.WITCH,pos.x,pos.y+1,pos.z,2,.1,.2,.1,1);
   }
   
   public static void recallTeleportCancel(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.SMOKE,pos.x,pos.y+.5,pos.z,150,.5,.8,.5,0.05);
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
}
