package net.borisshoes.arcananovum.utils;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.MathUtils;
import net.borisshoes.borislib.utils.ParticleEffectUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArcanaEffectUtils extends ParticleEffectUtils {
   
   public static final double PHI = (1 + Math.sqrt(5)) / 2.0;
   
   public static void pulsarBladeShoot(ServerLevel world, Vec3 p1, Vec3 p2, int tick){
      Vec3 diff = p2.subtract(p1);
      int intervals = (int) (p1.subtract(p2).length() * 10);
      double delta = 0.03;
      double speed = 1;
      double portion = 0.5;
      int numTicks = 5;
      int count = 3;
      double dx = diff.x/intervals;
      double dy = diff.y/intervals;
      double dz = diff.z/intervals;
      int upperInt = (int) (intervals * ((tick+1.0) / numTicks));
      int lowerInt = (int) Math.max(0,upperInt - (intervals*portion));
      for(int i = 0; i < intervals; i++){
         if(i < lowerInt || i > upperInt) continue;
         double x = p1.x + dx*i;
         double y = p1.y + dy*i;
         double z = p1.z + dz*i;
         
         float hue = 178.0f/360.0f;
         float sat = 0.8f*(1 - i/((float)intervals));
         Color c = Color.getHSBColor(hue, sat, 1f);
         ParticleOptions dust = new DustParticleOptions(c.getRGB(),.6f);
         
         spawnLongParticle(world,dust,x,y,z,delta,delta,delta,speed,count);
      }
      if(upperInt >= intervals) spawnLongParticle(world, ParticleTypes.WAX_OFF,p2.x,p2.y,p2.z,0.2,0.2,0.2,1,10);
      
      if(tick < numTicks-1){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> pulsarBladeShoot(world,p1,p2,tick+1)));
      }
   }
   
   public static void gravitonMaulSlam(ServerLevel world, BlockPos pos, double range, int tick){
      BlockParticleOption dust = new BlockParticleOption(ParticleTypes.DUST_PILLAR, world.getBlockState(pos));
      double r = range/3.0 * (tick+1);
      circle(world,null,pos.getCenter(),dust,r,36,4,0.1,1);
      circle(world,null,pos.getCenter().add(0,1,0),dust,r,36,4,0.1,1,Math.PI/3.0);
      circle(world,null,pos.getCenter().add(0,2,0),dust,r,36,4,0.1,1,2*Math.PI/3.0);
      SoundUtils.playSound(world, pos, SoundEvents.MACE_SMASH_AIR, SoundSource.PLAYERS,.5f,0.4f+(tick*0.2f));
      
      if(tick < 3){
         BorisLib.addTickTimerCallback(world, new GenericTimer(4, () -> gravitonMaulSlam(world,pos,range,tick+1)));
      }
   }
   
   public static void gravitonMaulMaelstrom(ServerPlayer player, int tick){
      ServerLevel world = player.level();
      Vec3 center = player.position().add(0,0.1,0);
      ParticleOptions dust = new DustParticleOptions(0x000ea8,1f);
      ParticleOptions dust2 = new DustParticleOptions(0x000754,1.5f);
      
      int effectiveTick = tick % 60;
      double or = 5.5 * (1 - effectiveTick/60.0);
      double inter = 0.4;
      int num = 5;
      double theta = (0.00185*effectiveTick*effectiveTick); // Magic quadratic value (sets theta to 6pi at tick 100)
      double dt = Math.PI*2 * 0.05;
      int times = 5;
      for(int i = 0; i < num; i++){
         double r = or - (i*inter);
         if(r <= 0){
            break;
         }
         
         double dA = Math.PI * 2 / times;
         for(int j = 0; j < times; j++){
            double angle = dA * j + (theta + dt*i);
            double x = r * Math.cos(angle) + center.x;
            double z = r * Math.sin(angle) + center.z;
            double y = center.y;
            world.sendParticles(dust2,x,y,z,1,0.1,0.1,0.1,0.01);
         }
      }
      
      if(tick % 80 == 0){
         SoundUtils.playSound(player.level(), player.blockPosition(), SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS,.5f,1.6f);
      }
      
      world.sendParticles(dust,center.x,center.y+0.15,center.z,60,2.5,0.2,2.5,0.01);
      world.sendParticles(ParticleTypes.PORTAL,center.x,center.y,center.z,15,1,.5,1,1);
   }
   
   public static void arcaneNotesFinish(ServerPlayer player, ArcanaItem arcanaItem){
      ServerLevel world = player.level();
      world.sendParticles(ParticleTypes.ENCHANT,player.getX(),player.getY()+player.getBbHeight()/2.0,+player.getZ(),100,0.4,0.8,0.4,0);
      world.sendParticles(ParticleTypes.WITCH,player.getX(),player.getY()+player.getBbHeight()/1.5,+player.getZ(),100,0.25,0.6,0.25,0.3);
      
      Integer color = ArcanaRarity.getColor(arcanaItem.getRarity()).getColor();
      ParticleOptions dust = new DustParticleOptions(color == null ? 0xffffff : color,1.4f);
      world.sendParticles(dust,player.getX(),player.getY()+player.getBbHeight()/2.0,+player.getZ(),30,0.4,0.8,0.4,1);
   }
   
   public static void arcaneNotesAnim(ServerPlayer player, ArcanaItem arcanaItem, int usageTick){
      ServerLevel world = player.level();
      world.sendParticles(ParticleTypes.ENCHANT,player.getX(),player.getY()+player.getBbHeight()/2.0,+player.getZ(),3,0.25,0.6,0.25,0);
      world.sendParticles(player, ParticleTypes.ENCHANT,false,true,player.getX(),player.getY()+player.getBbHeight()/2.0,+player.getZ(),5,0.25,0.6,0.25,1);
      
      Integer color = ArcanaRarity.getColor(arcanaItem.getRarity()).getColor();
      ParticleOptions dust = new DustParticleOptions(color == null ? 0xffffff : color,0.5f);
      world.sendParticles(dust,player.getX(),player.getY()+player.getBbHeight()/2.0,+player.getZ(),4,0.4,0.8,0.4,1);
   }
   
   public static void enhancedForgingAnim(ServerLevel world, BlockPos forgePos, ItemStack stack, double tickRaw, double speedMod){
      Vec3 center = forgePos.getCenter();
      int tick = (int) tickRaw;
      if(tick < 350){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> enhancedForgingAnim(world, forgePos, stack, tickRaw+(1*speedMod),speedMod)));
      }
      if(tick == 0){
         ItemDisplayElement item = new ItemDisplayElement(stack);
         item.setGlowColorOverride(0xf7ed57);
         item.setBrightness(new Brightness(15,15));
         item.setScale(new Vector3f(0.5f));
         
         ElementHolder holder = new ElementHolder(){
            int lifeTime = (int) (350 / speedMod);
            
            @Override
            protected void onTick(){
               super.onTick();
               
               if(lifeTime-- <= 0){
                  setAttachment(null);
                  destroy(); // Time expired, remove
                  return;
               }
               if(lifeTime < (int) (80 / speedMod)){
                  item.setGlowing(true);
               }
               
               float rotateRate = (float) (0.1f * speedMod);
               float scaleRate = (float) (0.01f * speedMod);
               float translateRate = (float) (0.02f * speedMod);
               
               for(VirtualElement element : getElements()){
                  if(element instanceof ItemDisplayElement elem){
                     elem.setLeftRotation(elem.getLeftRotation().rotateY(rotateRate,new Quaternionf()));
                     
                     if(elem.getScale().y() < 1){
                        elem.setScale(elem.getScale().add(scaleRate,scaleRate,scaleRate,new Vector3f()));
                     }
                     
                     if(elem.getTranslation().y() < 1.5){
                        elem.setTranslation(elem.getTranslation().add(0,translateRate,0,new Vector3f()));
                     }
                  }
               }
            }
         };
         holder.addElement(item);
         HolderAttachment attachment = ChunkAttachment.ofTicking(holder, world, forgePos);
         
         SoundUtils.playSound(world, forgePos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 2f, 0.8f);
      }
      
      ParticleOptions yellow = new DustParticleOptions(0xf7ed57,0.7f);
      
      double starTicks = 75;
      for(float i = 0; i < Math.PI*2; i+= (float) (Math.PI/4.0f)){
         double radius = tick >= starTicks ? 1.15 : (-0.000782113805012*tick*tick + 0.0739918687092*tick); // Quadratic from https://www.desmos.com/calculator/vuyttamm67
         double height = tick >= starTicks ? 2.5 : 2.5*tick/starTicks;
         float rotation = i - 0.01f * tick;
         Vec3 starPos = center.add(new Vec3(radius, 0.25+height, 0).yRot(rotation));
         world.sendParticles(ParticleTypes.ELECTRIC_SPARK,starPos.x,starPos.y,starPos.z,1,0,0,0,0);
         if(tick >= starTicks && tick < 320){
            world.sendParticles(yellow,starPos.x,starPos.y,starPos.z,1,0.1,0.1,0.1,0);
         }
      }
      
      Vec3 itemCenter = new Vec3(center.x,center.y+1.6,center.z);
      
      if(tick >= starTicks && tick < 300){
         world.sendParticles(ParticleTypes.VAULT_CONNECTION,center.x,center.y+2.5,center.z,3,0.2,0.2,0.2,1);
         world.sendParticles(yellow,center.x,center.y+2.5,center.z,3,0.8,0.8,0.8,0);
      }
      
      if(tick == adjustTime(50,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 2f, 0.8f);
      }
      
      if(tick >= 120 && tick <= 270){
         if(tick % 19 == 0){
            animatedLightningBolt(world,itemCenter, MathUtils.randomSpherePoint(itemCenter,4,2.5),8,0.5, ParticleTypes.ELECTRIC_SPARK,8,1,0,0,false,2,30);
            SoundUtils.playSound(world, forgePos, SoundEvents.TRIDENT_THUNDER, SoundSource.BLOCKS, 0.25f, 1.75f + 0.25f*(float)Math.random());
         }
      }
      
      if(tick == adjustTime(130,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.7f, 0.7f);
      }
      
      if(tick >= 200 && tick <= 280){
         int count = (int) Math.min(4,(tick-200) * 0.05) + 1;
         world.sendParticles(ParticleTypes.OMINOUS_SPAWNING,itemCenter.x,itemCenter.y,itemCenter.z,count,0.2,0.2,0.2,1);
      }
      
      if(tick == adjustTime(280,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.BLOCKS, 1.25f, 0.7f);
      }
      
      if(tick == adjustTime(330,speedMod)){
         world.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH,0xffffff),center.x,center.y+2,center.z,3,0.1,0.1,0.1,0.02);
      }
      
      if(tick % 2 == 0){
         return;
      }
      
      if(tick < 300){
         world.sendParticles(ParticleTypes.END_ROD,center.x,center.y+5.5,center.z,1,1.5,1,1.5,0);
      }
      
      double padScale = tick > 150 ? Math.min(1,(350-tick)/50.0) : Math.min(1,tick/50.0);
      
      final double L1 = 1.5 * padScale;
      final double S1 = 0.5 * padScale;
      final int I1 = tick % 4 == 1 ? 10 : 11;
      final double D1 = 0.02;
      final int C1 = 1;
      
      line(world,null,center.add(L1,-0.4,0),center.add(S1,-0.4,S1),yellow,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,L1),center.add(S1,-0.4,S1),yellow,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,L1),center.add(-S1,-0.4,S1),yellow,I1,C1,D1,1);
      line(world,null,center.add(-L1,-0.4,0),center.add(-S1,-0.4,S1),yellow,I1,C1,D1,1);
      line(world,null,center.add(-L1,-0.4,0),center.add(-S1,-0.4,-S1),yellow,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,-L1),center.add(S1,-0.4,-S1),yellow,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,-L1),center.add(-S1,-0.4,-S1),yellow,I1,C1,D1,1);
      line(world,null,center.add(L1,-0.4,0),center.add(S1,-0.4,-S1),yellow,I1,C1,D1,1);
      
      
      ParticleOptions blue = new DustParticleOptions(0x79e0fc,0.7f);
      final double L2 = 1.2 * padScale;
      final double S2 = 0.55 * padScale;
      final int I2 = tick % 4 == 1 ? 10 : 11;
      final double D2 = 0.02;
      final int C2 = 1;
      
      line(world,null,center.add(L2,-0.4,L2),center.add(0,-0.4,S2),blue,I2,C2,D2,1);
      line(world,null,center.add(L2,-0.4,L2),center.add(S2,-0.4,0),blue,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,L2),center.add(0,-0.4,S2),blue,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,L2),center.add(-S2,-0.4,0),blue,I2,C2,D2,1);
      line(world,null,center.add(L2,-0.4,-L2),center.add(0,-0.4,-S2),blue,I2,C2,D2,1);
      line(world,null,center.add(L2,-0.4,-L2),center.add(S2,-0.4,0),blue,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,-L2),center.add(0,-0.4,-S2),blue,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,-L2),center.add(-S2,-0.4,0),blue,I2,C2,D2,1);
      
      ParticleOptions white = new DustParticleOptions(0xe6fff6,0.7f);
      final double L3 = 1.8 * padScale;
      final double S3 = 1.15 * padScale;
      final int I3 = tick % 4 == 1 ? 30 : 31;
      final double D3 = 0.02;
      final int C3 = 1;
      
      line(world,null,center.add(L3,-0.4,0),center.add(-L3,-0.4,0),white,I3,C3,D3,1);
      line(world,null,center.add(0,-0.4,L3),center.add(0,-0.4,-L3),white,I3,C3,D3,1);
      line(world,null,center.add(S3,-0.4,S3),center.add(-S3,-0.4,-S3),white,I3,C3,D3,1);
      line(world,null,center.add(-S3,-0.4,S3),center.add(S3,-0.4,-S3),white,I3,C3,D3,1);
   }
   
   public static void arcanaCraftingAnim(ServerLevel world, BlockPos forgePos, ItemStack stack, double tickRaw, double speedMod){
      Vec3 center = forgePos.getCenter();
      int tick = (int) tickRaw;
      if(tick < 350){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> arcanaCraftingAnim(world, forgePos, stack, tickRaw+(1*speedMod), speedMod)));
      }
      if(tick == 0){
         ItemDisplayElement item = new ItemDisplayElement(stack);
         item.setGlowColorOverride(0x9404d6);
         item.setBrightness(new Brightness(15,15));
         item.setScale(new Vector3f(0.5f));
         
         ElementHolder holder = new ElementHolder(){
            int lifeTime = (int) (350 / speedMod);
            
            @Override
            protected void onTick(){
               super.onTick();
               
               if(lifeTime-- <= 0){
                  setAttachment(null);
                  destroy(); // Time expired, remove
                  return;
               }
               if(lifeTime < (int) (80 / speedMod)){
                  item.setGlowing(true);
               }
               
               float rotateRate = (float) (0.1f * speedMod);
               float scaleRate = (float) (0.0075f * speedMod);
               float translateRate = (float) (0.02f * speedMod);
               
               for(VirtualElement element : getElements()){
                  if(element instanceof ItemDisplayElement elem){
                     elem.setLeftRotation(elem.getLeftRotation().rotateY(rotateRate,new Quaternionf()));
                     
                     if(elem.getScale().y() < 1){
                        elem.setScale(elem.getScale().add(scaleRate,scaleRate,scaleRate,new Vector3f()));
                     }
                     
                     if(elem.getTranslation().y() < 1.5){
                        elem.setTranslation(elem.getTranslation().add(0,translateRate,0,new Vector3f()));
                     }
                  }
               }
            }
         };
         holder.addElement(item);
         HolderAttachment attachment = ChunkAttachment.ofTicking(holder, world, forgePos);
         
         SoundUtils.playSound(world, forgePos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 2f, 0.8f);
      }
      
      ParticleOptions purple = new DustParticleOptions(0x9404d6,0.7f);
      
      double starTicks = 75;
      for(float i = 0; i < Math.PI*2; i+= (float) (Math.PI/4.0f)){
         double radius = tick >= starTicks ? 1.15 : (-0.000782113805012*tick*tick + 0.0739918687092*tick); // Quadratic from https://www.desmos.com/calculator/vuyttamm67
         double height = tick >= starTicks ? 2.5 : 2.5*tick/starTicks;
         float rotation = i - 0.01f * tick;
         Vec3 starPos = center.add(new Vec3(radius, 0.25+height, 0).yRot(rotation));
         world.sendParticles(ParticleTypes.ELECTRIC_SPARK,starPos.x,starPos.y,starPos.z,1,0,0,0,0);
         if(tick >= starTicks && tick < 320){
            world.sendParticles(purple,starPos.x,starPos.y,starPos.z,1,0.1,0.1,0.1,0);
         }
      }
      
      Vec3 itemCenter = new Vec3(center.x,center.y+1.6,center.z);
      
      if(tick >= starTicks && tick < 300){
         world.sendParticles(ParticleTypes.ENCHANT,center.x,center.y+2.5,center.z,3,0.2,0.2,0.2,1);
         world.sendParticles(purple,center.x,center.y+2.5,center.z,3,0.8,0.8,0.8,0);
      }
      
      if(tick == adjustTime(50,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 2f, 0.8f);
      }
      
      if(tick >= 120 && tick <= 270){
         if(tick % 19 == 0){
            animatedLightningBolt(world,itemCenter,MathUtils.randomSpherePoint(itemCenter,4,2.5),8,0.5, ParticleTypes.ELECTRIC_SPARK,8,1,0,0,false,2,30);
            SoundUtils.playSound(world, forgePos, SoundEvents.TRIDENT_THUNDER, SoundSource.BLOCKS, 0.25f, 1.75f + 0.25f*(float)Math.random());
         }
      }
      
      if(tick == adjustTime(130,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.7f, 0.7f);
      }
      
      if(tick >= 200 && tick <= 280){
         int count = (int) Math.min(4,(tick-200) * 0.05) + 1;
         world.sendParticles(ParticleTypes.OMINOUS_SPAWNING,itemCenter.x,itemCenter.y,itemCenter.z,count,0.2,0.2,0.2,1);
         world.sendParticles(ParticleTypes.WITCH,itemCenter.x,itemCenter.y,itemCenter.z,count,0.2,0.5,0.2,0.05);
      }
      
      if(tick == adjustTime(280,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.BLOCKS, 1.25f, 0.7f);
      }
      
      if(tick == adjustTime(330,speedMod)){
         world.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH,0xffffff),center.x,center.y+2,center.z,3,0.1,0.1,0.1,0.02);
         world.sendParticles(ParticleTypes.WITCH,itemCenter.x,itemCenter.y,itemCenter.z,100,0.2,0.5,0.2,0.1);
      }
      
      if(tick % 2 == 0){
         return;
      }
      
      if(tick < 300){
         world.sendParticles(ParticleTypes.END_ROD,center.x,center.y+5.5,center.z,1,1.5,1,1.5,0);
      }
      
      double padScale = tick > 150 ? Math.min(1,(350-tick)/50.0) : Math.min(1,tick/50.0);
      
      final double L1 = 1.5 * padScale;
      final double S1 = 0.5 * padScale;
      final int I1 = tick % 4 == 1 ? 10 : 11;
      final double D1 = 0.02;
      final int C1 = 1;
      
      line(world,null,center.add(L1,-0.4,0),center.add(S1,-0.4,S1),purple,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,L1),center.add(S1,-0.4,S1),purple,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,L1),center.add(-S1,-0.4,S1),purple,I1,C1,D1,1);
      line(world,null,center.add(-L1,-0.4,0),center.add(-S1,-0.4,S1),purple,I1,C1,D1,1);
      line(world,null,center.add(-L1,-0.4,0),center.add(-S1,-0.4,-S1),purple,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,-L1),center.add(S1,-0.4,-S1),purple,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,-L1),center.add(-S1,-0.4,-S1),purple,I1,C1,D1,1);
      line(world,null,center.add(L1,-0.4,0),center.add(S1,-0.4,-S1),purple,I1,C1,D1,1);
      
      
      ParticleOptions blue = new DustParticleOptions(0x79e0fc,0.7f);
      final double L2 = 1.2 * padScale;
      final double S2 = 0.55 * padScale;
      final int I2 = tick % 4 == 1 ? 10 : 11;
      final double D2 = 0.02;
      final int C2 = 1;
      
      line(world,null,center.add(L2,-0.4,L2),center.add(0,-0.4,S2),blue,I2,C2,D2,1);
      line(world,null,center.add(L2,-0.4,L2),center.add(S2,-0.4,0),blue,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,L2),center.add(0,-0.4,S2),blue,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,L2),center.add(-S2,-0.4,0),blue,I2,C2,D2,1);
      line(world,null,center.add(L2,-0.4,-L2),center.add(0,-0.4,-S2),blue,I2,C2,D2,1);
      line(world,null,center.add(L2,-0.4,-L2),center.add(S2,-0.4,0),blue,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,-L2),center.add(0,-0.4,-S2),blue,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,-L2),center.add(-S2,-0.4,0),blue,I2,C2,D2,1);
      
      ParticleOptions white = new DustParticleOptions(0xd9daff,0.7f);
      final double L3 = 1.8 * padScale;
      final double S3 = 1.15 * padScale;
      final int I3 = tick % 4 == 1 ? 30 : 31;
      final double D3 = 0.02;
      final int C3 = 1;
      
      line(world,null,center.add(L3,-0.4,0),center.add(-L3,-0.4,0),white,I3,C3,D3,1);
      line(world,null,center.add(0,-0.4,L3),center.add(0,-0.4,-L3),white,I3,C3,D3,1);
      line(world,null,center.add(S3,-0.4,S3),center.add(-S3,-0.4,-S3),white,I3,C3,D3,1);
      line(world,null,center.add(-S3,-0.4,S3),center.add(S3,-0.4,-S3),white,I3,C3,D3,1);
   }
   
   
   public static void ensnaredEffect(LivingEntity living, int amplifier, int tick){
      if(!living.isAlive() || living.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) == null || !(living.level() instanceof ServerLevel world)){
         return;
      }
      double eHeight = living.getBbHeight();
      double eWidth = living.getBbWidth();
      double circleHeight = eHeight*0.6;
      double circleRadius = eWidth / 1.6;
      Vec3 circleCenter = living.position().add(0,eHeight/1.8,0);
      ParticleOptions purple = new DustParticleOptions(0xa100e6,0.7f);
      
      int intervals = (int) (15 * Math.sqrt(circleRadius*circleRadius+circleHeight*circleHeight));
      double dA = Math.PI * 2 / intervals;
      for(int i = 0; i < intervals; i++){
         double angle = dA * i + (tick / Math.PI);
         double xOff = circleRadius * Math.cos(angle);
         double zOff = circleRadius * Math.sin(angle);
         double yOff = (xOff+zOff) * 0.3536 * circleHeight/circleRadius;
         
         world.sendParticles(purple,xOff+circleCenter.x,yOff+circleCenter.y,zOff+circleCenter.z,1,0,0,0,0);
         world.sendParticles(purple,xOff+circleCenter.x,-yOff+circleCenter.y,zOff+circleCenter.z,1,0,0,0,0);
      }
      
      if(amplifier > 0 && tick % 5 == 0){
         circle(world,null,circleCenter, ParticleTypes.WITCH,circleRadius*1.2,intervals/2,1,0,0);
         circle(world,null,circleCenter.add(0,-circleHeight,0), ParticleTypes.WITCH,circleRadius*1.2,intervals/2,1,0,0);
      }
      BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> ensnaredEffect(living, amplifier,tick < 40 ? tick+1 : 0)));
   }
   
   public static void aequalisTransmuteAnim(ServerLevel world, Vec3 center, double rawTick, Vec2 rotation, double speedMod, ItemStack input, ItemStack output, ItemStack reagent1, ItemStack reagent2, ItemStack aequalis){
      ParticleOptions blue = new DustParticleOptions(0x12ccff,0.7f);
      ParticleOptions blueSmall = new DustParticleOptions(0x12ccff,0.4f);
      ParticleOptions purple = new DustParticleOptions(0xa100e6,0.5f);
      ParticleOptions pink = new DustParticleOptions(0xd300e6,0.8f);
      
      int tick = (int)(rawTick);
      int intBonus = tick % 3;
      int n = output == null || output.isEmpty() ? 3 : 4;
      
      List<Vec3> itemCenters = getCirclePoints(center,1.75+0.5*Math.sin(-Math.PI*tick/60.0) / 30.0,n,tick * 6 * Math.PI / 500.0);
      
      if(tick == 0){
         SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS,1,1.5f);
         
         ItemDisplayElement aequalisElem = new ItemDisplayElement(aequalis);
         aequalisElem.setGlowColorOverride(ArcanaColors.EQUAYUS_COLOR);
         aequalisElem.setBrightness(new Brightness(15,15));
         aequalisElem.setScale(new Vector3f(0.5f));
         aequalisElem.setTranslation(center.subtract(BlockPos.containing(center).getCenter()).toVector3f());
         
         ItemDisplayElement inputElem = new ItemDisplayElement(input);
         inputElem.setGlowColorOverride(ArcanaColors.EQUAYUS_COLOR);
         inputElem.setBrightness(new Brightness(15,15));
         inputElem.setScale(new Vector3f(0.0f));
         
         ItemDisplayElement reagent1Elem = new ItemDisplayElement(reagent1);
         reagent1Elem.setGlowColorOverride(ArcanaColors.EQUAYUS_COLOR);
         reagent1Elem.setBrightness(new Brightness(15,15));
         reagent1Elem.setScale(new Vector3f(0.0f));
         
         ItemDisplayElement reagent2Elem = new ItemDisplayElement(reagent2);
         reagent2Elem.setGlowColorOverride(ArcanaColors.EQUAYUS_COLOR);
         reagent2Elem.setBrightness(new Brightness(15,15));
         reagent2Elem.setScale(new Vector3f(0.0f));
         
         ElementHolder aequalisHolder = new ElementHolder(){
            int lifeTime = (int) (500 / speedMod);
            
            @Override
            protected void onTick(){
               super.onTick();
               
               if(lifeTime-- <= 0){
                  setAttachment(null);
                  destroy(); // Time expired, remove
                  return;
               }
               if(lifeTime < (int) (80 / speedMod)){
                  aequalisElem.setGlowing(true);
               }
               
               float rotateRate = (float) (0.1f * speedMod);
               float scaleRate = (float) (0.0075f * speedMod);
               
               for(VirtualElement element : getElements()){
                  if(element instanceof ItemDisplayElement elem){
                     elem.setLeftRotation(elem.getLeftRotation().rotateY(rotateRate,new Quaternionf()));
                     
                     if(elem.getScale().y() < 1){
                        elem.setScale(elem.getScale().add(scaleRate,scaleRate,scaleRate,new Vector3f()));
                     }
                  }
               }
            }
         };
         aequalisHolder.addElement(aequalisElem);
         ChunkAttachment.ofTicking(aequalisHolder, world, BlockPos.containing(center));
         
         
         ElementHolder inputHolder = makeAequalisItemHolder(inputElem,center,n,0,speedMod);
         ElementHolder reagent1Holder = makeAequalisItemHolder(reagent1Elem,center,n,1,speedMod);
         ElementHolder reagent2Holder = makeAequalisItemHolder(reagent2Elem,center,n,2,speedMod);
         
         inputHolder.addElement(inputElem);
         ChunkAttachment.ofTicking(inputHolder, world, BlockPos.containing(center));
         
         reagent1Holder.addElement(reagent1Elem);
         ChunkAttachment.ofTicking(reagent1Holder, world, BlockPos.containing(center));
         
         reagent2Holder.addElement(reagent2Elem);
         ChunkAttachment.ofTicking(reagent2Holder, world, BlockPos.containing(center));
         
         if(output != null && !output.isEmpty()){
            ItemDisplayElement outputElem = new ItemDisplayElement(output);
            outputElem.setGlowColorOverride(ArcanaColors.EQUAYUS_COLOR);
            outputElem.setBrightness(new Brightness(15,15));
            outputElem.setScale(new Vector3f(0.0f));
            
            ElementHolder outputHolder = makeAequalisItemHolder(outputElem,center,n,3,speedMod);
            outputHolder.addElement(outputElem);
            ChunkAttachment.ofTicking(outputHolder, world, BlockPos.containing(center));
         }
      }
      
      double innerSize = tick < 50 ? tick/100.0 : 0.2*Math.sin(-Math.PI*tick/50.0-0.25)+0.45;
      List<Tuple<Vec3, Vec3>> innerPairs = getIcosahedronPairs(getIcosahedronPoints().stream().map(
            point -> point.zRot(-0.55357f).yRot((float) (rawTick * 2*Math.PI / 500.0f)).scale(innerSize).add(center)
      ).toList());
      double outerSize = tick < 75 ? tick*2/75.0 : tick > 450 ? 15 - 0.03*tick : 0.25*Math.sin(-Math.PI*tick/75.0-Math.PI/2.0)+1.75;
      List<Tuple<Vec3, Vec3>> outerPairs = getIcosahedronPairs(getIcosahedronPoints().stream().map(
            point -> point.zRot(-0.55357f).yRot((float) (rawTick * 2*Math.PI / 500.0f)).scale(outerSize).add(center)
      ).toList());
      
      for(Tuple<Vec3, Vec3> pair :innerPairs){
         line(world,null,pair.getB(),pair.getA(),blueSmall,5+intBonus,1,0,0,1);
      }
      
      if(tick < 490){
         for(Tuple<Vec3, Vec3> pair : outerPairs){
            line(world,null,pair.getB(),pair.getA(),pink,10+intBonus,1,0,0,1);
         }
      }
      
      if(tick > 50){
         int i = 0;
         double radius = tick < 450 ? 0.5 : 5 - 0.01 * tick;
         for(Vec3 itemCenter : itemCenters){
            List<Vec3> circlePoints1 = getCirclePoints(new Vec3(0,0,0),radius,24,Math.PI*tick / 30.0).stream().map(point -> point.xRot((float) (Math.PI/2.0f)).yRot((float) (tick * 6 * Math.PI / 500.0)).add(itemCenter)).toList();
            List<Vec3> circlePoints2 = getCirclePoints(new Vec3(0,0,0),radius,24,Math.PI*tick / 30.0).stream().map(point -> point.xRot((float) (Math.PI/2.0f)).yRot((float) (-tick * 6 * Math.PI / 500.0)).add(itemCenter)).toList();
            double itemDY = 0.5 * Math.sin(Math.PI * tick / 100.0 + i * Math.PI * 2.0 / n);
            for(Vec3 circlePoint : circlePoints1){
               world.sendParticles(purple,circlePoint.x,circlePoint.y + itemDY,circlePoint.z,1,0,0,0,0);
            }
            for(Vec3 circlePoint : circlePoints2){
               world.sendParticles(purple,circlePoint.x,circlePoint.y + itemDY,circlePoint.z,1,0,0,0,0);
            }
            
            if(tick > 120 && tick < 450){
               world.sendParticles(ParticleTypes.WITCH,itemCenter.x,itemCenter.y + itemDY + 0.1,itemCenter.z,3,0.15,0.15,0.15,0);
            }
            i++;
         }
      }
      
      if(tick > 60 && tick < 450){
         if(Math.random() < 0.1){
            animatedLightningBolt(world,center,outerPairs.get((int)(Math.random()*outerPairs.size())).getB(),12,0.5, ParticleTypes.ELECTRIC_SPARK,16,1,0,0,false,0,15);
         }
         if(tick % 6 == 0){
            world.sendParticles(ParticleTypes.END_ROD,center.x,center.y,center.z,1,1.6,1.6,1.6,0);
         }
      }
      
      if(tick % 70 == 20){
         SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundSource.BLOCKS,1,((float)Math.random())*.5f + 0.7f);
      }
      if(tick % 100 == 35){
         SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS,0.5f,((float)Math.random())*.4f + 1.2f);
      }
      
      
      if(tick < 500){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> aequalisTransmuteAnim(world, center, rawTick+(1*speedMod), rotation, speedMod, input, output, reagent1, reagent2, aequalis)));
      }else{
         world.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH,0xffffff),center.x,center.y,center.z,5,0.3,0.3,0.3,0);
         SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.ALLAY_AMBIENT_WITH_ITEM, SoundSource.BLOCKS,1,0.8f);
      }
   }
   
   private static ElementHolder makeAequalisItemHolder(ItemDisplayElement element, Vec3 center, int n, int i, double speedMod){
      return new ElementHolder(){
         int lifeTime = (int) (500 / speedMod);
         
         @Override
         protected void onTick(){
            super.onTick();
            
            if(lifeTime-- <= 0){
               setAttachment(null);
               destroy(); // Time expired, remove
               return;
            }
            if(lifeTime < (int) (80 / speedMod)){
               element.setGlowing(true);
            }
            
            float rotateRate = (float) (0.1f * speedMod);
            float scaleRate = (float) (0.0075f * speedMod);
            
            for(VirtualElement element : getElements()){
               if(element instanceof ItemDisplayElement elem){
                  elem.setLeftRotation(elem.getLeftRotation().rotateY(rotateRate,new Quaternionf()));
                  
                  if((500-lifeTime) > (int) (450 / speedMod) && elem.getScale().y() > 0){
                     elem.setScale(elem.getScale().add(-scaleRate,-scaleRate,-scaleRate,new Vector3f()));
                  }else if((500-lifeTime) > (int) (50 / speedMod) && elem.getScale().y() < 0.5){
                     elem.setScale(elem.getScale().add(scaleRate,scaleRate,scaleRate,new Vector3f()));
                  }
                  
                  double itemDY = 0.5 * Math.sin(Math.PI * (500-lifeTime) / 100.0 + i * Math.PI * 2.0 / n);
                  elem.setTranslation(getCirclePoints(center,1.75+0.5*Math.sin(-Math.PI*(500-lifeTime)/60.0) / 30.0,n,(500-lifeTime) * 6 * Math.PI / 500.0).get(i)
                        .subtract(center).add(0,itemDY,0).add(center.subtract(BlockPos.containing(center).getCenter())).toVector3f());
               }
            }
         }
      };
   }
   
   public static void transmutationAltarAnim(ServerLevel world, Vec3 center, double rawTick, Direction direction, double speedMod){
      ParticleOptions blue = new DustParticleOptions(0x12ccff,0.7f);
      ParticleOptions purple = new DustParticleOptions(0xa100e6,0.7f);
      ParticleOptions pink = new DustParticleOptions(0xd300e6,0.7f);
      Vec3 effectCenter = center.add(0,0.6,0);
      
      int tick = (int)(rawTick);
      double theta = Math.PI*tick / 30.0;
      int intBonus = tick % 3;
      int itemCI = 20; double itemCR = 0.7; double itemOutset = 3;
      int nodeCI = 8; double nodeCR = 0.25; double nodeOutset = 2.2;
      double outerRadius = 4.4; double innerRadius = 4.0;
      
      //MathHelper.clamp((tick-20.0) / (40.0-20.0),0,1)
      circle(world,null,effectCenter,pink,innerRadius* Mth.clamp(tick/60.0,0,1),125,1,0,0, theta);
      circle(world,null,effectCenter,pink,outerRadius* Mth.clamp(tick/60.0,0,1),125,1,0,0, theta);
      circle(world,null,effectCenter,purple,itemCR* Mth.clamp(tick/20.0,0,1),itemCI,1,0,0, theta);
      
      if(tick > 260){
         circle(world,null,effectCenter, ParticleTypes.WITCH,(outerRadius+innerRadius)/2,50,1,0.1,0, theta);
      }
      
      for(float i = 0; i < Math.PI*2; i+= (float) (Math.PI/2.0f)){
         if(tick < 70) continue;
         
         Vec3 itemCenter = effectCenter.add(new Vec3(itemOutset,0,0).yRot(i));
         circle(world,null,itemCenter,purple,itemCR* Mth.clamp((tick-70.0) / 40.0,0,1),itemCI,1,0,0, theta);
         
         if(tick < 90) continue;
         Vec3 centerLine1P1 = effectCenter.add(new Vec3((itemCR+0.1)*.71,0,itemCR*.71).yRot(i));
         Vec3 centerLine1P2 = effectCenter.add(new Vec3(nodeOutset-nodeCR*.71,0,nodeOutset-nodeCR*.71).yRot(i));
         line(world,null,centerLine1P1,centerLine1P2,blue,15+intBonus,1,0,0, Mth.clamp((tick-90.0) / 50.0,0,1));
         
         Vec3 centerLine2P1 = effectCenter.add(new Vec3(itemCR+0.1,0,0).yRot(i));
         Vec3 centerLine2P2 = effectCenter.add(new Vec3(itemOutset-itemCR,0,0).yRot(i));
         line(world,null,centerLine2P1,centerLine2P2,blue,10+intBonus,1,0,0, Mth.clamp((tick-90.0) / 30.0,0,1));
         
         if(tick < 110) continue;
         
         Vec3 crossLine1aP1 = effectCenter.add(new Vec3(itemOutset-(itemCR*.71+0.1),0,itemCR*.71+0.1).yRot(i));
         Vec3 crossLine1bP1 = effectCenter.add(new Vec3(itemCR*.71,0,itemOutset-itemCR*.71).yRot(i));
         Vec3 crossLine1P2 = effectCenter.add(new Vec3(itemOutset*.5,0,itemOutset*.5).yRot(i));
         line(world,null,crossLine1aP1,crossLine1P2,blue,7+intBonus,1,0,0, Mth.clamp((tick-110.0) / 50.0,0,1));
         line(world,null,crossLine1bP1,crossLine1P2,blue,7+intBonus,1,0,0, Mth.clamp((tick-110.0) / 50.0,0,1));
         
         Vec3 crossLine2P1 = effectCenter.add(new Vec3(itemOutset,0,itemCR+0.1).yRot(i));
         Vec3 crossLine2P2 = effectCenter.add(new Vec3(nodeOutset+nodeCR*.71,0,nodeOutset-nodeCR*.71).yRot(i));
         line(world,null,crossLine2P1,crossLine2P2,blue,7+intBonus,1,0,0, Mth.clamp((tick-110.0) / 40.0,0,1));
         
         Vec3 crossLine3P1 = effectCenter.add(new Vec3(itemCR+0.1,0,itemOutset).yRot(i));
         Vec3 crossLine3P2 = effectCenter.add(new Vec3(nodeOutset-nodeCR*.71,0,nodeOutset+nodeCR*.71).yRot(i));
         line(world,null,crossLine3P1,crossLine3P2,blue,7+intBonus,1,0,0, Mth.clamp((tick-110.0) / 40.0,0,1));
         
         if(tick < 150) continue;
         
         Vec3 nodeCenter = effectCenter.add(new Vec3(nodeOutset,0,nodeOutset).yRot(i));
         circle(world,null,nodeCenter,purple,nodeCR* Mth.clamp((tick-150.0) / 20.0,0,1),nodeCI,1,0,0, theta);
         
         Vec3 outerLine1P1 = effectCenter.add(new Vec3(itemOutset+itemCR+0.1,0,0).yRot(i));
         Vec3 outerLine1P2 = effectCenter.add(new Vec3(outerRadius-0.02,0,0).yRot(i));
         line(world,null,outerLine1P1,outerLine1P2,blue,3+intBonus,1,0,0, Mth.clamp((tick-150.0) / 40.0,0,1));
         
         double outerIZ = 0.5*(-itemOutset+Math.sqrt(2*outerRadius*outerRadius-itemOutset*itemOutset));
         double outerIX = outerIZ + itemOutset;
         Vec3 outerLine2P1 = effectCenter.add(new Vec3(itemOutset+itemCR*.71+0.1,0,itemCR*.71+0.1).yRot(i));
         Vec3 outerLine2P2 = effectCenter.add(new Vec3(outerIX-0.02,0,outerIZ-0.02).yRot(i));
         line(world,null,outerLine2P1,outerLine2P2,blue,5+intBonus,1,0,0, Mth.clamp((tick-150.0) / 40.0,0,1));
         
         Vec3 outerLine3P1 = effectCenter.add(new Vec3(itemOutset+itemCR*.71+0.1,0,-(itemCR*.71+0.1)).yRot(i));
         Vec3 outerLine3P2 = effectCenter.add(new Vec3(outerIX-0.02,0,-(outerIZ-0.02)).yRot(i));
         line(world,null,outerLine3P1,outerLine3P2,blue,5+intBonus,1,0,0, Mth.clamp((tick-150.0) / 40.0,0,1));
         
         if(tick < 160) continue;
         
         Vec3 outerLine4P1 = effectCenter.add(new Vec3(nodeOutset+nodeCR*.71+0.1,0,nodeOutset+nodeCR*.71+0.1).yRot(i));
         Vec3 outerLine4P2 = effectCenter.add(new Vec3(innerRadius*.71-0.02,0,innerRadius*.71-0.02).yRot(i));
         line(world,null,outerLine4P1,outerLine4P2,blue,3+intBonus,1,0,0, Mth.clamp((tick-150.0) / 30.0,0,1));
         
         if(tick < 450) continue;
         Vec3 itemSpot = effectCenter.add(new Vec3(itemOutset,0,0).yRot(i));
         world.sendParticles(ParticleTypes.WITCH,itemSpot.x,itemSpot.y,itemSpot.z,1,0.15,0.15,0.15,0);
         world.sendParticles(ParticleTypes.ELECTRIC_SPARK,itemSpot.x,itemSpot.y,itemSpot.z,3,0.25,0.25,0.25,0);
         world.sendParticles(ParticleTypes.END_ROD,itemSpot.x,itemSpot.y,itemSpot.z,1,0.25,0.25,0.25,0.02);
         
         if(tick == 500){
            world.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH,0xffffff),itemSpot.x,itemSpot.y+0.25,itemSpot.z,3,0.25,0.25,0.25,0);
         }
      }
      
      if(tick > 180){
         double dA = Math.PI * 2 / 50;
         double angle = dA * tick;
         double x = (outerRadius+innerRadius)/2 * Math.cos(angle) + effectCenter.x;
         double z = (outerRadius+innerRadius)/2 * Math.sin(angle) + effectCenter.z;
         double y = tick > 280 ? effectCenter.y + 1: effectCenter.y;
         
         world.sendParticles(ParticleTypes.WITCH,x,y,z,12,0.25,0.25,0.25,0);
      }
      
      if(tick == 0){
         SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS,1,1.5f);
      }
      if(tick % 70 == 20){
         SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundSource.BLOCKS,1,((float)Math.random())*.5f + 0.7f);
      }
      if(tick % 100 == 35){
         SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS,0.5f,((float)Math.random())*.4f + 1.2f);
      }
      
      if(tick < 500){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> transmutationAltarAnim(world,center,rawTick+(1*speedMod), direction,speedMod)));
      }
   }
   
   public static void craftForge(ServerLevel world, BlockPos pos, int tick){
      Vec3 center = pos.getCenter();
      if(tick == 100){
         world.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH,0xffffff),center.x,center.y,center.z,3,0.4,0.4,0.4,0);
         world.sendParticles(ParticleTypes.ELECTRIC_SPARK,center.x,center.y,center.z,25,0.6,0.8,0.6,0);
         SoundUtils.playSound(world,pos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.BLOCKS, 2, 0.8f);
      }else{
         world.sendParticles(ParticleTypes.END_ROD,center.x,center.y,center.z,1,0.6,0.8,0.6,0);
         world.sendParticles(ParticleTypes.WITCH,center.x,center.y,center.z,1,0.6,0.8,0.6,0);
      }
   }
   
   public static void craftTome(ServerLevel world, BlockPos pos, int tick){
      Vec3 center = pos.getCenter();
      if(tick == 100){
         world.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH,0xffffff),center.x,center.y,center.z,3,0.4,0.4,0.4,0);
         world.sendParticles(ParticleTypes.ELECTRIC_SPARK,center.x,center.y,center.z,25,0.6,0.8,0.6,0);
         SoundUtils.playSound(world,pos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.BLOCKS, 2, 0.8f);
      }else{
         world.sendParticles(ParticleTypes.ENCHANT,center.x,center.y+1,center.z,10,0.3,0.3,0.3,1);
         world.sendParticles(ParticleTypes.WITCH,center.x,center.y,center.z,2,0.6,0.8,0.6,0);
      }
   }
   
   public static void stormcallerAltarAnim(ServerLevel world, Vec3 center, int tick){
      double or = 5*(1-tick/100.0);
      double inter = 0.15;
      int num = 5;
      double theta = (0.001885*tick*tick); // Magic quadratic value (sets theta to 6pi at tick 100)
      double dt = Math.PI*2 * 0.05;
      int times = 3;
      for(int i = 0; i < num; i++){
         double r = or - (i*inter);
         if(r <= 0) break;
         
         double dA = Math.PI * 2 / times;
         for(int j = 0; j < times; j++){
            double angle = dA * j + (theta + dt*i);
            double x = r * Math.cos(angle) + center.x;
            double z = r * Math.sin(angle) + center.z;
            double y = center.y + 0.6;
            
            world.sendParticles(ParticleTypes.FISHING,x,y,z,3,0,0,0,0.01);
            world.sendParticles(ParticleTypes.FALLING_WATER,x,y,z,1,0,0,0,0.01);
         }
      }
      
      for(int i = 0; i < 2; i++){
         double angle = Math.random()*Math.PI*2;
         double r = Math.random()*1+3;
         double x = r * Math.cos(angle) + center.x;
         double z = r * Math.sin(angle) + center.z;
         double y = center.y + 4.5;
         
         world.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,x,y,z,2,0.2,0.2,0.2,0.002);
         world.sendParticles(ParticleTypes.FALLING_WATER,x,y,z,5,0.3,0.3,0.3,1);
      }
      for(int i = 0; i < 5; i++){
         double angle = Math.random()*Math.PI*2;
         double r = Math.random()*1+3;
         double x = r * Math.cos(angle) + center.x;
         double z = r * Math.sin(angle) + center.z;
         double y = center.y + 4.5;
         
         world.sendParticles(ParticleTypes.CLOUD,x,y,z,4,0.2,0.2,0.2,0.002);
         world.sendParticles(ParticleTypes.FALLING_WATER,x,y,z,3,0.3,0.3,0.3,1);
      }
      
      if(tick < 100){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> stormcallerAltarAnim(world,center,tick+1)));
      }else{
         LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
         lightning.setPos(center);
         world.addFreshEntity(lightning);
      }
   }
   
   public static void celestialAltarAnim(ServerLevel world, Vec3 center, int tick, Direction direction){
      if(tick == 0){
         SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 2, 0.5f);
      }
      
      double phi = Math.PI * (3 - Math.sqrt(5));
      double theta = 2*Math.PI / 100 * tick;
      int points = 100;
      ParticleOptions black = new DustParticleOptions(0x000000,2.0f);
      double blackDelta = tick < 100 || tick > 400 ? 0.05 : 0.4;
      int blackCount = tick < 100 || tick > 400 ? 1 : 4;
      
      for(int i = 0; i < points; i++){
         // Fibonacci Sphere Equations
         double y = (i / (double)(points-1));
         double r = Math.sqrt(1-y*y);
         double t = phi*i + theta;
         double x = Math.cos(t) * r;
         double z = Math.sin(t) * r;
         if(y > tick/100.0 || (tick > 400 && y > 1-(tick-400)/100.0)){
            continue;
         }
         
         // Center Offset and Radius Scale
         Vec3 point = new Vec3(x,y,z);
         point = point.scale(5).add(center.x, center.y, center.z);
         
         world.sendParticles(black,point.x,point.y,point.z,blackCount,blackDelta,blackDelta,blackDelta,0);
      }
      
      ParticleOptions sun = new DustParticleOptions(0xd1a400,2.0f);
      ParticleOptions moon = new DustParticleOptions(0x1670f0,2.0f);
      
      if(tick > 100){
         if(tick % 3 == 0){
            world.sendParticles(ParticleTypes.END_ROD,center.x,center.y+2.5,center.z,8,3,1.5,3,0);
         }
         
         Vec3 rotVec = switch(direction){
            case SOUTH -> new Vec3(-1,1,-1);
            case EAST -> new Vec3(1,1,-1);
            case WEST -> new Vec3(-1,1,1);
            default -> new Vec3(1,1,1);
         };
         
         if(tick < 400 && tick % 2 == 0){
            Vec3 celestPos;
            
            if(tick < 175){
               double y = (tick-100) / 25.0;
               celestPos = new Vec3(2.5,y,-2.5).multiply(rotVec);
            }else if(tick < 325){
               double t = Math.PI*2 * (tick-175)/150 - (Math.PI/4);
               double x = Math.cos(t) * 2.5;
               double z = Math.sin(t) * 2.5;
               celestPos = new Vec3(x,3,z).multiply(rotVec);
            }else{
               double y = 3-((tick-325) / 25.0);
               celestPos = new Vec3(2.5,y,-2.5).multiply(rotVec);
            }
            
            sphere(world,null,center.add(celestPos),sun,0.5,10,3,0.15,0,theta);
            sphere(world,null,center.add(celestPos.multiply(-1,1,-1)),moon,0.5,10,3,0.15,0,theta);
         }
      }
      
      
      if(tick < 500){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> celestialAltarAnim(world,center,tick+1, direction)));
      }
   }
   
   public static void starpathAltarAnim(ServerLevel world, Vec3 center){
      SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 2, 0.5f);
      starpathAltarAnim(world,center,0,new ArrayList<>(),new ArrayList<>());
   }
   
   private static void starpathAltarAnim(ServerLevel world, Vec3 center, int tick, List<Tuple<Vec3,Integer>> groundStars, List<Vec3> skyStars){
      double phi = Math.PI * (3 - Math.sqrt(5));
      double theta = 2*Math.PI / 100 * tick;
      int points = 100;
      ParticleOptions black = new DustParticleOptions(0x000000,2.0f);
      double blackDelta = tick < 100 ? 0.05 : 0.4;
      int blackCount = tick < 100 ? 1 : 4;
      
      for(int i = 0; i < points; i++){
         // Fibonacci Sphere Equations
         double y = (i / (double)(points-1));
         double r = Math.sqrt(1-y*y);
         double t = phi*i + theta;
         double x = Math.cos(t) * r;
         double z = Math.sin(t) * r;
         if(y > tick/100.0){
            continue;
         }
         
         // Center Offset and Radius Scale
         Vec3 point = new Vec3(x,y,z);
         point = point.scale(5).add(center.x, center.y, center.z);
         
         world.sendParticles(black,point.x,point.y,point.z,blackCount,blackDelta,blackDelta,blackDelta,0);
      }
      
      if(tick >= 100){
         if(tick % 2 == 0){
            for(int i = 0; i < groundStars.size(); i++){
               Tuple<Vec3,Integer> groundStar = groundStars.get(i);
               Vec3 starPos = groundStar.getA();
               world.sendParticles(ParticleTypes.END_ROD,starPos.x,starPos.y,starPos.z,1,0,0,0,0);
               groundStars.set(i,new Tuple<>(starPos.add(0,0.125,0),groundStar.getB()-1));
            }
            groundStars.removeIf((p)->p.getB()<=0);
            if(groundStars.size() < 8){ // Re-add stars
               for(int i = 0; i < 2; i++){
                  double t = Math.random()*Math.PI*2;
                  double r = (Math.random()*3+1);
                  double x = Math.cos(t) * r;
                  double z = Math.sin(t) * r;
                  int lifeTime = (int)(Math.random()*8+4);
                  groundStars.add(new Tuple<>(new Vec3(x,0,z).add(center.x, center.y+0.5, center.z),lifeTime));
               }
            }
            
         }
         if(tick % 3 == 0){
            for(Vec3 skyStar : skyStars){
               world.sendParticles(ParticleTypes.END_ROD,skyStar.x,skyStar.y,skyStar.z,1,0.05,0.05,0.05,0);
            }
         }
         
         if(skyStars.size() < 30){
            double starRadius = 4.5;
            for(int i = 0; i < 30; i++){
               double t = Math.random()*Math.PI*2;
               double r = (Math.random()*starRadius + 0.75);
               double x = Math.cos(t) * r;
               double z = Math.sin(t) * r;
               double y = Math.sqrt(starRadius*starRadius - r*r);
               skyStars.add(new Vec3(center.x+x,center.y+y,center.z+z));
            }
         }
         
         if(tick >= 140){
            ParticleOptions white = new DustParticleOptions(0x944ec7,0.5f);
            int connections = Math.min(8,(tick-140) / 30);
            for(int i = 0; i < connections+1; i++){
               line(world,null,skyStars.get(i),skyStars.get(i+1),white,20,1,0.05,0);
            }
            if(tick <= 380 && (tick-140) % 30 == 0){
               SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 2, 0.5f + (connections*0.2f));
            }
         }
      }
      
      if(tick == 440){
         SoundUtils.playSound(world, BlockPos.containing(center), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 2, 1.5f);
      }
      
      if(tick < 500){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> starpathAltarAnim(world,center,tick+1,groundStars,skyStars)));
      }
   }
   
   public static void midnightEnchanterAnim(ServerLevel world, Vec3 center, int tick){
      if(tick % 2 == 0){
         return;
      }
      
      world.sendParticles(ParticleTypes.ENCHANT,center.x(),center.y()+0.75,center.z(),5,0.1,0.1,0.1,1);
      
      ParticleOptions blue = new DustParticleOptions(0x12ccff,0.7f);
      final double L1 = 2.35;
      final double S1 = 0.85;
      final int I1 = tick % 4 == 1 ? 10 : 11;
      final double D1 = 0.02;
      final int C1 = 1;
      
      line(world,null,center.add(L1,-0.4,0),center.add(S1,-0.4,S1),blue,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,L1),center.add(S1,-0.4,S1),blue,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,L1),center.add(-S1,-0.4,S1),blue,I1,C1,D1,1);
      line(world,null,center.add(-L1,-0.4,0),center.add(-S1,-0.4,S1),blue,I1,C1,D1,1);
      line(world,null,center.add(-L1,-0.4,0),center.add(-S1,-0.4,-S1),blue,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,-L1),center.add(S1,-0.4,-S1),blue,I1,C1,D1,1);
      line(world,null,center.add(0,-0.4,-L1),center.add(-S1,-0.4,-S1),blue,I1,C1,D1,1);
      line(world,null,center.add(L1,-0.4,0),center.add(S1,-0.4,-S1),blue,I1,C1,D1,1);
      
      
      ParticleOptions purple = new DustParticleOptions(0xa100e6,0.7f);
      final double L2 = 1.4;
      final double S2 = 0.6;
      final int I2 = tick % 4 == 1 ? 10 : 11;
      final double D2 = 0.02;
      final int C2 = 1;
      
      line(world,null,center.add(L2,-0.4,L2),center.add(0,-0.4,S2),purple,I2,C2,D2,1);
      line(world,null,center.add(L2,-0.4,L2),center.add(S2,-0.4,0),purple,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,L2),center.add(0,-0.4,S2),purple,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,L2),center.add(-S2,-0.4,0),purple,I2,C2,D2,1);
      line(world,null,center.add(L2,-0.4,-L2),center.add(0,-0.4,-S2),purple,I2,C2,D2,1);
      line(world,null,center.add(L2,-0.4,-L2),center.add(S2,-0.4,0),purple,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,-L2),center.add(0,-0.4,-S2),purple,I2,C2,D2,1);
      line(world,null,center.add(-L2,-0.4,-L2),center.add(-S2,-0.4,0),purple,I2,C2,D2,1);
      
      ParticleOptions pink = new DustParticleOptions(0xd300e6,0.7f);
      final double L3 = 2.0;
      final double S3 = 1.15;
      final int I3 = tick % 4 == 1 ? 30 : 31;
      final double D3 = 0.02;
      final int C3 = 1;
      
      line(world,null,center.add(L3,-0.4,0),center.add(-L3,-0.4,0),pink,I3,C3,D3,1);
      line(world,null,center.add(0,-0.4,L3),center.add(0,-0.4,-L3),pink,I3,C3,D3,1);
      line(world,null,center.add(S3,-0.4,S3),center.add(-S3,-0.4,-S3),pink,I3,C3,D3,1);
      line(world,null,center.add(-S3,-0.4,S3),center.add(S3,-0.4,-S3),pink,I3,C3,D3,1);
   }
   
   public static void stellarCoreAnim(ServerLevel world, Vec3 center, int tick, Direction direction){
      if(tick % 2 == 0) return;
      sphere(world,null,center, ParticleTypes.FLAME,1.2,30,2,0.2,0.03,Math.PI*2*tick/300);
      sphere(world,null,center, ParticleTypes.LAVA,1.2,10,2,0.2,0.02,Math.PI*2*tick/300);
      sphere(world,null,center, ParticleTypes.WAX_ON,.5,10,2,0.05,0.02,Math.PI*2*tick/300);
      world.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,center.x(),center.y(),center.z(),1,0.5,0.5,0.5,0.02);
      
      Vec3 pos = center.subtract(Vec3.atLowerCornerOf(direction.getUnitVec3i())).add(0,3.5,0);
      world.sendParticles(ParticleTypes.LAVA,pos.x(),pos.y(),pos.z(),1,0.25,0.05,0.25,0.02);
   }
   
   public static void arcaneSingularityAnim(ServerLevel world, Vec3 center, int tick, Direction direction, double fillPercent){
      if(tick % 2 == 0) return;
      double L = 300.0;
      double animPercent = tick/L;
      double piPercent = Math.PI*2*animPercent;
      ParticleOptions black = new DustParticleOptions(0x000000,2.0f);
      ParticleOptions blue = new DustParticleOptions(0x00ECFF,0.75f);
      sphere(world,null,center,black,0.2+0.65*fillPercent,(int)(20*fillPercent+5),1,0.025,0,5*piPercent);
      sphere(world,null,center, ParticleTypes.WITCH,0.5+0.85*fillPercent,(int)(30*fillPercent+12),1,0.05,0,3*piPercent);
      sphere(world,null,center,blue,0.4+0.75*fillPercent,(int)(70*fillPercent+12),1,0.01,0,-3*piPercent);
      world.sendParticles(ParticleTypes.WITCH,center.x(),center.y()-1.2,center.z(),4,0.3,0.4,0.3,0);
      
      List<Vec3> rods = new ArrayList<>(Arrays.asList(
            new Vec3(0,-1,2), new Vec3(-2,-1,0), new Vec3(0,-1,-2), new Vec3(2,-1,0),
            new Vec3(-1,-2,-1), new Vec3(1,-2,-1), new Vec3(-1,-2,1), new Vec3(1,-2,1)
      ));
      rods.remove(direction.get2DDataValue());
      
      int N = 3;
      double[] R = new double[N];
      for(int i = 0; i < R.length; i++){
         R[i] = 0.2*(1-((animPercent+((double) i /N)) % 1))+.1;
      }
      double W = 4.3;
      for(int i = 0; i < rods.size(); i++){
         Vec3 pos = center.add(rods.get(i));
         for(int j = 0; j < R.length; j++){
            world.sendParticles(blue,pos.x()+R[j]*Math.cos(W*(piPercent+((double) j /N))),pos.y()+1.25*((animPercent+((double) j /N)) % 1),pos.z()+R[j]*Math.sin(W*(piPercent+((double) j /N))),3,0.01,0.01,0.01,1);
         }
      }
   }
   
   public static void nulConstructSummon(ServerLevel world, Vec3 pos, int tick){
      double or = 5*(1-tick/220.0);
      double inter = 0.15;
      int num = 2;
      double theta = (0.000259635756495*tick*tick); // Magic quadratic value (sets theta to 6pi at tick 100)
      double dt = Math.PI*2 * 0.05;
      int times = 5;
      for(int i = 0; i < num; i++){
         double r = or - (i*inter);
         if(r <= 0) break;
         
         double dA = Math.PI * 2 / times;
         for(int j = 0; j < times; j++){
            double angle = dA * j + (theta + dt*i);
            double x = r * Math.cos(angle) + pos.x;
            double z = r * Math.sin(angle) + pos.z;
            double y = pos.y + 0.6;
            
            world.sendParticles(ParticleTypes.SOUL,x,y,z,1,0,0,0,0.01);
            world.sendParticles(ParticleTypes.FALLING_OBSIDIAN_TEAR,x,y,z,1,0,0,0,0.01);
         }
      }
      
      world.sendParticles(ParticleTypes.PORTAL,pos.x,pos.y,pos.z,20,0.3,0.3,0.3,1);
      
      
      if(tick < 220){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> nulConstructSummon(world,pos,tick+1)));
      }else{
         world.sendParticles(ParticleTypes.WITCH,pos.x,pos.y,pos.z,150,1,1,1,0.01);
      }
   }
   
   public static void exaltedConstructSummon(ServerLevel world, Vec3 pos, int tick){
      double or = 5*(1-tick/220.0);
      double inter = 0.15;
      int num = 2;
      double theta = (0.000259635756495*tick*tick); // Magic quadratic value (sets theta to 6pi at tick 100)
      double dt = Math.PI*2 * 0.05;
      int times = 5;
      for(int i = 0; i < num; i++){
         double r = or - (i*inter);
         if(r <= 0) break;
         
         double dA = Math.PI * 2 / times;
         for(int j = 0; j < times; j++){
            double angle = dA * j + (theta + dt*i);
            double x = r * Math.cos(angle) + pos.x;
            double z = r * Math.sin(angle) + pos.z;
            double y = pos.y + 0.6;
            
            world.sendParticles(ParticleTypes.SOUL,x,y,z,1,0,0,0,0.01);
            world.sendParticles(ParticleTypes.FALLING_OBSIDIAN_TEAR,x,y,z,1,0,0,0,0.01);
         }
      }
      
      world.sendParticles(ParticleTypes.PORTAL,pos.x,pos.y,pos.z,20,0.3,0.3,0.3,1);
      world.sendParticles(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH,1),pos.x,pos.y+0.75,pos.z,3,0.3,0.3,0.3,0.03);
      
      if(tick%2 == 0){
         ParticleOptions dust = new DustParticleOptions(0xFF00D4,0.75f);
         Vec3 circleCenter = pos.add(0,-1,0);
         double r = 2.5;
         float t = (float)(Math.PI/220.0*tick);
         double sqrt3 = Math.sqrt(3);
         
         circle(world,null,circleCenter,dust,r,40,1,0,1);
         
         Vec3[] tri1 = {new Vec3(0, 0, r),new Vec3(-r*sqrt3/2, 0, -r/2),new Vec3(r*sqrt3/2, 0, -r/2)};
         Vec3[] tri2 = {new Vec3(0, 0, -r),new Vec3(-r*sqrt3/2, 0, r/2),new Vec3(r*sqrt3/2, 0, r/2)};
         for(int i = 0; i < 3; i++){
            Vec3 p1 = tri1[i].yRot(t).add(circleCenter);
            Vec3 p2 = tri1[(i+1)%3].yRot(t).add(circleCenter);
            Vec3 p3 = tri2[i].yRot(t).add(circleCenter);
            Vec3 p4 = tri2[(i+1)%3].yRot(t).add(circleCenter);
            line(world,null,p1,p2,dust,12,1,0,1);
            line(world,null,p3,p4,dust,12,1,0,1);
         }
      }
      
      if(tick < 220){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> exaltedConstructSummon(world,pos,tick+1)));
      }else{
         world.sendParticles(ParticleTypes.WITCH,pos.x,pos.y,pos.z,150,1,1,1,0.01);
      }
   }
   
   public static void nulConstructNecroticShroud(ServerLevel world, Vec3 pos){
      world.sendParticles(ParticleTypes.LARGE_SMOKE,pos.x(),pos.y()+1.5,pos.z(),150,1.5,1.5,1.5,0.07);
   }
   
   public static void nulConstructNecroticConversion(ServerLevel world, Vec3 pos){
      ParticleOptions dust = new DustParticleOptions(0x9e0945,0.8f);
      world.sendParticles(dust, pos.x(), pos.y() + 1.75, pos.z(), 10,0.75,1,0.75,0.03);
   }
   
   public static void nulConstructReflectiveArmor(ServerLevel world, Vec3 pos){
      world.sendParticles(ParticleTypes.END_ROD, pos.x(), pos.y() + 1.75, pos.z(), 3,0.75,1,0.75,0.03);
   }
   
   public static void nulConstructChargeAttack(ServerLevel world, Vec3 pos, float yaw){
      double xOff = -Mth.sin(yaw * (float) (Math.PI / 180.0));
      double yOff = Mth.cos(yaw * (float) (Math.PI / 180.0));
      world.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x() + 2*xOff, pos.y()+1, pos.z() + 2*yOff, 2, 2*xOff, 0.0, 2*yOff, 0.0);
   }
   
   public static void nulConstructCurseOfDecay(ServerLevel world, Vec3 pos){
      world.sendParticles(ParticleTypes.SOUL, pos.x(), pos.y() + 1, pos.z(), 20,0.5,1,0.5,0.07);
   }
   
   public static void nulConstructReflexiveBlast(ServerLevel world, Vec3 pos, int calls){
      double radius = .5+calls*4;
      ParticleOptions dust = new DustParticleOptions(0x36332b,1.5f);
      sphere(world,null,pos,dust,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,calls*Math.PI*2/5);
      if(calls < 5){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> nulConstructReflexiveBlast(world,pos,calls + 1)));
      }
   }
   
   public static void webOfFireCast(ServerLevel world, ParticleOptions type, ServerPlayer caster, List<LivingEntity> hits, double range, int calls){
      final int totalCalls = 15;
      
      Vec3 center = caster.position().add(0,caster.getBbHeight()*.25,0);
      if(calls%2 == 0 && calls < 5){
         circle(world,null,center,type,range,(int)(10*range),1,0.05,0.01);
         circle(world,null,center,type,2,20,1,0.05,0.01);
   
         for(LivingEntity hit : hits){
            Vec3 hitCircle = new Vec3(hit.getX(),center.y(),hit.getZ());
            circle(world,null,hitCircle,type,hit.getBbWidth(),12,1,0,0);
            line(world,null,center,hitCircle,type,(int)(center.distanceTo(hitCircle)*4),1,0,0);
            
            for(LivingEntity other : hits){
               if(other.getStringUUID().equals(hit.getStringUUID())) continue;
               Vec3 otherCircle = new Vec3(other.getX(),center.y(),other.getZ());
               line(world,null,otherCircle,hitCircle,type,(int)(otherCircle.distanceTo(hitCircle)*2.5),1,0,0);
            }
         }
      }
   
      for(LivingEntity hit : hits){
         double heightMod = (double)calls/totalCalls;
         double height = hit.getY() + hit.getBbHeight()*heightMod;
         double radiusMod = 1.0 - (double)calls/(totalCalls*1.5);
         double radius = hit.getBbWidth()*.75 * radiusMod;
         Vec3 circlePos = new Vec3(hit.getX(),height,hit.getZ());
         circle(world,null,circlePos,type,radius,12,1,0,0.01);
      }
      
      if(calls < totalCalls){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> webOfFireCast(world, type,caster,hits,range,calls + 1)));
      }
   }
   
   public static void pyroblastExplosion(ServerLevel world, ParticleOptions type, Vec3 pos, double range, int calls){
      double radius = .5+calls*(range/5);
      sphere(world,null,pos,type,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,calls*Math.PI*2/5);
      if(calls < 5){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> pyroblastExplosion(world, type,pos,range,calls + 1)));
      }
   }
   
   public static void spawnerInfuser(ServerLevel world, BlockPos pos, int duration){
      for(int i = 0; i < duration; i++){
         world.sendParticles(new ShriekParticleOption(i * 5), (double)pos.getX() + 0.5, (double)pos.getY() + SculkShriekerBlock.TOP_Y -0.5, (double)pos.getZ() + 0.5, 1,0.0, 0.0, 0.0,0);
         world.sendParticles(new ShriekParticleOption(i * 5+2), (double)pos.getX() + 0.5, (double)pos.getY() + SculkShriekerBlock.TOP_Y -0.5, (double)pos.getZ() + 0.5, 1,0.0, 0.0, 0.0,0);
      }
      world.sendParticles(ParticleTypes.SCULK_SOUL, (double)pos.getX() + 0.5, (double)pos.getY() + 2.5, (double)pos.getZ() + 0.5, 5,0.5, 0.5, 0.5,0.02);
      world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, (double)pos.getX() + 0.5, (double)pos.getY() + 2.5, (double)pos.getZ() + 0.5, 5,0.3, 0.3, 0.3,0.02);
   }
   
   public static void arcaneFlakArrowDetonate(ServerLevel world, Vec3 pos, double range, int calls){
      //ParticleEffect dust = new DustParticleEffect(new Vector3f(Vec3d.unpackRgb(0x0085de)),1.4f);
      double radius = .5+calls*(range/5.0);
      double radius2 = radius*.75;
      sphere(world,null,pos, ParticleTypes.WITCH,radius,(int)(radius*radius+radius*10+10),3,0.3,0,0);
      sphere(world,null,pos, PowerParticleOption.create(ParticleTypes.DRAGON_BREATH,1),radius2,(int)(radius2*radius2+radius2*5+10),3,0.3,0,0);
      world.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH,0xd7aeff),pos.x,pos.y,pos.z,1,0,0,0,1);
      
      if(calls < 5){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> arcaneFlakArrowDetonate(world, pos,range,calls + 1)));
      }
   }
   
   public static void gravitonArrowEmit(ServerLevel world, Vec3 center, List<Entity> entities){
      ParticleOptions dust = new DustParticleOptions(0x000ea8,1f);
      ParticleOptions dust2 = new DustParticleOptions(0x000754,1.5f);
      int count = 30;
      double range = .3;
   
      world.sendParticles(dust,center.x,center.y,center.z,300,1.5,1.5,1.5,.01);
      world.sendParticles(ParticleTypes.PORTAL,center.x,center.y,center.z,100,.5,.5,.5,1);
      sphere(world,null,center,dust2,.6,50,2,0.1,0,0);
      
      for(Entity e : entities){
         Vec3 pos = e.position().add(0,e.getBbHeight()/2,0);
         world.sendParticles(dust,pos.x,pos.y,pos.z,count,range,range,range,.01);
      }
   }
   
   public static void expulsionArrowEmit(ServerLevel world, Vec3 pos, double range, int calls){
      ParticleOptions dust = new DustParticleOptions(0x0085de,1.4f);
      double radius = .5+calls*(range/5);
      sphere(world,null,pos,dust,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,0);
      if(calls < 5){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> expulsionArrowEmit(world, pos,range,calls + 1)));
      }
   }
   
   public static void smokeArrowEmit(ServerLevel world, Vec3 pos){
      if(Math.random() < 0.1){
         spawnLongParticle(world, ParticleTypes.LARGE_SMOKE,pos.x,pos.y,pos.z,0.5,0.5,0.5,.01,1);
      }
      if(Math.random() < 0.05){
         spawnLongParticle(world, ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,pos.x,pos.y,pos.z,0.5,0.5,0.5,.01,1);
      }
   }
   
   public static void concussionArrowShot(ServerLevel world, Vec3 pos, double range, int calls){
      double radius = .5+calls*(range/5);
      sphere(world,null,pos, ParticleTypes.SQUID_INK,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,0);
      if(calls < 5){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> concussionArrowShot(world, pos, range,calls + 1)));
      }
   }
   
   public static void photonArrowShot(ServerLevel world, Vec3 p1, Vec3 p2, float brightness, int tick){
      Vec3 diff = p2.subtract(p1);
      int intervals = (int) (p1.subtract(p2).length() * 10);
      double delta = 0.03;
      double speed = 1;
      double portion = 0.35;
      int numTicks = 4;
      int count = 3;
      double dx = diff.x/intervals;
      double dy = diff.y/intervals;
      double dz = diff.z/intervals;
      int upperInt = (int) (intervals * ((tick+1.0) / numTicks));
      int lowerInt = (int) Math.max(0,upperInt - (intervals*portion));
      for(int i = 0; i < intervals; i++){
         if(i < lowerInt || i > upperInt) continue;
         double x = p1.x + dx*i;
         double y = p1.y + dy*i;
         double z = p1.z + dz*i;
         
         float hue = i/((float)intervals);
         float trueBrightness = (float) Math.min(1,-0.01*(new Vec3(x,y,z).distanceTo(p1)-100)+0.25) * brightness;
         Color c = Color.getHSBColor(hue, 1f, trueBrightness);
         ParticleOptions dust = new DustParticleOptions(c.getRGB(),.6f);
         
         spawnLongParticle(world,dust,x,y,z,delta,delta,delta,speed,count);
      }
      if(upperInt >= intervals) spawnLongParticle(world, ParticleTypes.WAX_OFF,p2.x,p2.y,p2.z,0.2,0.2,0.2,1,10);
      
      if(tick < numTicks-1){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> photonArrowShot(world,p1,p2,brightness,tick+1)));
      }
   }
   
   public static void tetherArrowEntity(ServerLevel world, LivingEntity entity, ServerPlayer player){
      ParticleOptions dust = new DustParticleOptions(0xa6a58a,.4f);
      double len = player.position().subtract(entity.position()).length();
      longDistLine(world,player.position().add(0,player.getBbHeight()/2,0),entity.position().add(0,entity.getBbHeight()/2,0),dust,(int)(20*len),3,0.03,1);
   }
   
   public static void tetherArrowGrapple(ServerLevel world, ServerPlayer player, Vec3 pos){
      ParticleOptions dust = new DustParticleOptions(0xa6a58a,.4f);
      double len = player.position().subtract(pos).length();
      longDistLine(world,player.position(),pos,dust,(int)(20*len),3,0.03,1);
   }
   
   public static void blinkArrowTp(ServerLevel world, Vec3 pos){
      world.sendParticles(ParticleTypes.REVERSE_PORTAL,pos.x,pos.y,pos.z,100,.3,.5,.3,0.05);
   }
   
   public static void harnessFly(ServerLevel world, ServerPlayer player, int duration){
      Vec3 pos = player.position();
      world.sendParticles(ParticleTypes.END_ROD,pos.x,pos.y,pos.z,1,.3,.3,.3,0.05);
      world.sendParticles(SpellParticleOption.create(ParticleTypes.INSTANT_EFFECT,0xffffff,1),pos.x,pos.y,pos.z,1,.3,.3,.3,1);
      
      if(0 < duration){
         BorisLib.addTickTimerCallback(world, new GenericTimer(2, () -> harnessFly(world, player,duration-1)));
      }
   }
   
   public static void harnessStall(ServerLevel world, Vec3 pos){
      world.sendParticles(ParticleTypes.SMOKE,pos.x,pos.y+0.5,pos.z,100,.4,.6,.4,0.05);
      world.sendParticles(ParticleTypes.ANGRY_VILLAGER,pos.x,pos.y+0.5,pos.z,15,.4,.6,.4,1);
      world.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,pos.x,pos.y+0.5,pos.z,15,.4,.6,.4,0.07);
   }
   
   public static void dowsingRodEmitter(ServerLevel world, Vec3 pos, int calls, int duration){
      if(world.getBlockState(BlockPos.containing(pos)).getBlock() != Blocks.ANCIENT_DEBRIS) return;
      
      spawnLongParticle(world, ParticleTypes.FLAME,pos.x+0.5,pos.y+0.5,pos.z+0.5,.4,.4,.4,.05,3);
      
      if(calls < (duration)){
         BorisLib.addTickTimerCallback(world, new GenericTimer(3, () -> dowsingRodEmitter(world, pos, calls + 1, duration)));
      }
   }
   
   public static void dowsingRodArrow(ServerLevel world, Vec3 start, Vec3 end, int calls){
      line(world,null,start,end, ParticleTypes.FLAME,8,3,.08,0);
      if(calls < (16)){
         BorisLib.addTickTimerCallback(world, new GenericTimer(5, () -> dowsingRodArrow(world, start, end, calls + 1)));
      }
   }
   
   public static void shadowGlaiveTp(ServerLevel world, ServerPlayer player){
      Vec3 pos = player.position();
      world.sendParticles(ParticleTypes.LARGE_SMOKE,pos.x,pos.y+0.5,pos.z,100,.4,.4,.4,0.07);
   }
   
   public static void shulkerCoreLevitate(ServerLevel world, Player player, int duration){
      if(player.getEffect(MobEffects.LEVITATION) == null) return;
      Vec3 pos = player.position();
      world.sendParticles(ParticleTypes.END_ROD,pos.x,pos.y+1,pos.z,1,.3,.3,.3,0.05);
   
      if(0 < duration){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> shulkerCoreLevitate(world, player,duration-1)));
      }
   }
   
   public static void recallTeleportCharge(ServerLevel world, Vec3 pos){
      world.sendParticles(ParticleTypes.PORTAL,pos.x,pos.y+.5,pos.z,20,.2,.5,.2,1);
      world.sendParticles(ParticleTypes.WITCH,pos.x,pos.y+1,pos.z,2,.1,.2,.1,1);
   }
   
   public static void recallTeleportCancel(ServerLevel world, Vec3 pos){
      world.sendParticles(ParticleTypes.SMOKE,pos.x,pos.y+.5,pos.z,150,.5,.8,.5,0.05);
   }
   
   public static void recallLocation(ServerLevel world, Vec3 pos, ServerPlayer player){
      circle(world,player,pos.subtract(0,0,0), ParticleTypes.ENCHANTED_HIT,0.5,12,1,0.1,0);
      world.sendParticles(player, ParticleTypes.WITCH, false,true, pos.x,pos.y,pos.z,5,.15,.15,.15,0);
   }
   
   public static void recallTeleport(ServerLevel world, Vec3 pos){ recallTeleport(world, pos, 0); }
   
   private static void recallTeleport(ServerLevel world, Vec3 pos, int tick){
      int animLength = 30;
      
      if(tick < 5){
         world.sendParticles(ParticleTypes.REVERSE_PORTAL,pos.x,pos.y+.5,pos.z,30,.1,.4,.1,0.2);
         world.sendParticles(ParticleTypes.FALLING_OBSIDIAN_TEAR,pos.x,pos.y+.5,pos.z,10,.6,.6,.6,0.2);
      }
      circle(world,null,pos.subtract(0,0.5,0), ParticleTypes.WITCH,1,20,1,0.1,0);
      
      if(tick < animLength){
         BorisLib.addTickTimerCallback(world, new GenericTimer(1, () -> recallTeleport(world,pos,tick+1)));
      }
   }
   
   public static void stasisPearl(ServerLevel world, Vec3 pos){
      world.sendParticles(ParticleTypes.REVERSE_PORTAL,pos.x,pos.y,pos.z,1,.2,.2,.2,0.01);
      world.sendParticles(ParticleTypes.GLOW,pos.x,pos.y,pos.z,1,.15,.15,.15,0);
   }
   
   public static void dragonBossTowerCircleInvuln(ServerLevel world, Vec3 center, int period, int calls){
      ParticleOptions dust = new DustParticleOptions(9109665,.8f);
      ParticleOptions dust2 = new DustParticleOptions(9109665,1.5f);
      double r = 2.5;
      float t = (float)(Math.PI/((double) period /100)*calls);
      double sqrt3 = Math.sqrt(3);
      
      circle(world,null,center,dust,r,60,1,0,1);
      //circle(world,null,center,dust,1.1*r,100,1,0,1);
      //circle(world,null,center,dust,r/2,30,1,0.,1);
      //circle(world,null,center,dust,2*sqrt3/3,30,1,0,1);
      
      Vec3[] tri1 = {new Vec3(0, 0, r),new Vec3(-r*sqrt3/2, 0, -r/2),new Vec3(r*sqrt3/2, 0, -r/2)};
      Vec3[] tri2 = {new Vec3(0, 0, -r),new Vec3(-r*sqrt3/2, 0, r/2),new Vec3(r*sqrt3/2, 0, r/2)};
      for(int i = 0; i < 3; i++){
         Vec3 p1 = tri1[i].yRot(t).add(center);
         Vec3 p2 = tri1[(i+1)%3].yRot(t).add(center);
         Vec3 p3 = tri2[i].yRot(t).add(center);
         Vec3 p4 = tri2[(i+1)%3].yRot(t).add(center);
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
            world.sendParticles(dust2, x, y, z, 1, 0, 0, 0,1);
         }
      }
   
      if(calls < (period/100)){
         BorisLib.addTickTimerCallback(world, new GenericTimer(2, () -> dragonBossTowerCircleInvuln(world, center, period, calls + 1)));
      }
   }
   
   public static void dragonBossTowerCirclePush(ServerLevel world, Vec3 center, int period, int calls){
      ParticleOptions dust = new DustParticleOptions(16711892,2f);
      ParticleOptions dustLarge = new DustParticleOptions(16711892,3f);
      double r = 1.05*4;
      float t = -(float)(Math.PI/((double) period /100)*calls + Math.PI);
      double sqrt3 = Math.sqrt(3);
   
      circle(world,null,center,dust,r,40,1,0,1);
      
      Vec3[] tri1 = {new Vec3(0, 0, r),new Vec3(-r*sqrt3/2, 0, -r/2),new Vec3(r*sqrt3/2, 0, -r/2)};
      Vec3[] tri2 = {new Vec3(0, 0, -r),new Vec3(-r*sqrt3/2, 0, r/2),new Vec3(r*sqrt3/2, 0, r/2)};
      for(int i = 0; i < 3; i++){
         Vec3 p1 = tri1[i].yRot(t).add(center);
         Vec3 p2 = tri1[(i+1)%3].yRot(t).add(center);
         Vec3 p3 = tri2[i].yRot(t).add(center);
         Vec3 p4 = tri2[(i+1)%3].yRot(t).add(center);
         line(world,null,p1,p2,dust,12,1,0,1);
         line(world,null,p3,p4,dust,12,1,0,1);
      }
      
      sphere(world,null,center.add(0,2,0),dustLarge,5.5,25,1,0,1,-t);
      
      if(calls < (period/100)){
         BorisLib.addTickTimerCallback(world, new GenericTimer(2, () -> dragonBossTowerCirclePush(world, center, period, calls + 1)));
      }
   }
   
   public static void dragonReclaimTowerCircle(ServerLevel world, Vec3 center, int period, int calls){
      ParticleOptions dust = new DustParticleOptions(4044031,1.5f);
      double r = 1.05*4;
      float t = -(float)(Math.PI/((double) period /100)*calls + Math.PI);
      double sqrt3 = Math.sqrt(3);
      
      circle(world,null,center,dust,r,40,1,0,1);
      
      Vec3[] tri1 = {new Vec3(0, 0, r),new Vec3(-r*sqrt3/2, 0, -r/2),new Vec3(r*sqrt3/2, 0, -r/2)};
      Vec3[] tri2 = {new Vec3(0, 0, -r),new Vec3(-r*sqrt3/2, 0, r/2),new Vec3(r*sqrt3/2, 0, r/2)};
      for(int i = 0; i < 3; i++){
         Vec3 p1 = tri1[i].yRot(t).add(center);
         Vec3 p2 = tri1[(i+1)%3].yRot(t).add(center);
         Vec3 p3 = tri2[i].yRot(t).add(center);
         Vec3 p4 = tri2[(i+1)%3].yRot(t).add(center);
         line(world,null,p1,p2,dust,16,1,0,1);
         line(world,null,p3,p4,dust,16,1,0,1);
      }
      
      if(calls < (period/100)){
         BorisLib.addTickTimerCallback(world, new GenericTimer(2, () -> dragonReclaimTowerCircle(world, center, period, calls + 1)));
      }
   }
   
   public static void dragonReclaimTowerShield(ServerLevel world, Vec3 center, int calls){
      int period = 15000;
      ParticleOptions dust = new DustParticleOptions(9694975,1.5f);
      float t = -(float)(Math.PI/((double) period /200)*calls + Math.PI);
      
      longDistSphere(world,center.add(0,2,0),dust,5.5,75,1,0,1,-t);
      
      if(calls < (period/200)){
         BorisLib.addTickTimerCallback(world, new GenericTimer(2, () -> dragonReclaimTowerShield(world, center, calls + 1)));
      }
   }
   
   public static void dragonBossWizardPulse(ServerLevel world, Vec3 center, int ticks){
      double radius = ticks/4.0;
      double theta = 2*Math.PI / 20.0;
      ParticleOptions dust = new DustParticleOptions(16711892,(float)radius/2);
      sphere(world,null,center,dust,radius,(int)(radius*radius+radius*10+radius),1,0,1,theta*ticks);
   }
}
