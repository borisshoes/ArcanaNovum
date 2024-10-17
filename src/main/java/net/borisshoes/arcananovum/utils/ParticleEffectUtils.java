package net.borisshoes.arcananovum.utils;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.block.Blocks;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ShriekParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;
import java.util.*;

public class ParticleEffectUtils {
   
   public static final double PHI = (1 + Math.sqrt(5)) / 2.0;
   
   public static void arcaneNotesFinish(ServerPlayerEntity player, ArcanaItem arcanaItem){
      ServerWorld world = player.getServerWorld();
      world.spawnParticles(ParticleTypes.ENCHANT,player.getX(),player.getY()+player.getHeight()/2.0,+player.getZ(),100,0.4,0.8,0.4,0);
      world.spawnParticles(ParticleTypes.WITCH,player.getX(),player.getY()+player.getHeight()/1.5,+player.getZ(),100,0.25,0.6,0.25,0.3);
      
      Integer color = ArcanaRarity.getColor(arcanaItem.getRarity()).getColorValue();
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(color == null ? 0xffffff : color).toVector3f(),1.4f);
      world.spawnParticles(dust,player.getX(),player.getY()+player.getHeight()/2.0,+player.getZ(),30,0.4,0.8,0.4,1);
   }
   
   public static void arcaneNotesAnim(ServerPlayerEntity player, ArcanaItem arcanaItem, int usageTick){
      ServerWorld world = player.getServerWorld();
      world.spawnParticles(ParticleTypes.ENCHANT,player.getX(),player.getY()+player.getHeight()/2.0,+player.getZ(),3,0.25,0.6,0.25,0);
      world.spawnParticles(player,ParticleTypes.ENCHANT,false,player.getX(),player.getY()+player.getHeight()/2.0,+player.getZ(),5,0.25,0.6,0.25,1);
      
      Integer color = ArcanaRarity.getColor(arcanaItem.getRarity()).getColorValue();
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(color == null ? 0xffffff : color).toVector3f(),0.5f);
      world.spawnParticles(dust,player.getX(),player.getY()+player.getHeight()/2.0,+player.getZ(),4,0.4,0.8,0.4,1);
   }
   
   public static void enhancedForgingAnim(ServerWorld world, BlockPos forgePos, ItemStack stack, double tickRaw, double speedMod){
      Vec3d center = forgePos.toCenterPos();
      int tick = (int) tickRaw;
      if(tick < 350){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> enhancedForgingAnim(world, forgePos, stack, tickRaw+(1*speedMod),speedMod)));
      }
      if(tick == 0){
         ItemDisplayElement item = new ItemDisplayElement(stack);
         item.setGlowColorOverride(0xf7ed57);
         item.setBrightness(new Brightness(15,15));
         item.setScale(new Vector3f(0.5f));
         
         ElementHolder holder = new ElementHolder() {
            int lifeTime = (int) (350 / speedMod);
            
            @Override
            protected void onTick(){
               super.onTick();
               
               if(lifeTime-- <= 0) {
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
         
         SoundUtils.playSound(world, forgePos, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 2f, 0.8f);
      }
      
      ParticleEffect yellow = new DustParticleEffect(Vec3d.unpackRgb(0xf7ed57).toVector3f(),0.7f);
      
      double starTicks = 75;
      for(float i = 0; i < Math.PI*2; i+= (float) (Math.PI/4.0f)){
         double radius = tick >= starTicks ? 1.15 : (-0.000782113805012*tick*tick + 0.0739918687092*tick); // Quadratic from https://www.desmos.com/calculator/vuyttamm67
         double height = tick >= starTicks ? 2.5 : 2.5*tick/starTicks;
         float rotation = i - 0.01f * tick;
         Vec3d starPos = center.add(new Vec3d(radius, 0.25+height, 0).rotateY(rotation));
         world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,starPos.x,starPos.y,starPos.z,1,0,0,0,0);
         if(tick >= starTicks && tick < 320){
            world.spawnParticles(yellow,starPos.x,starPos.y,starPos.z,1,0.1,0.1,0.1,0);
         }
      }
      
      Vec3d itemCenter = new Vec3d(center.x,center.y+1.6,center.z);
      
      if(tick >= starTicks && tick < 300){
         world.spawnParticles(ParticleTypes.VAULT_CONNECTION,center.x,center.y+2.5,center.z,3,0.2,0.2,0.2,1);
         world.spawnParticles(yellow,center.x,center.y+2.5,center.z,3,0.8,0.8,0.8,0);
      }
      
      if(tick == adjustTime(50,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 2f, 0.8f);
      }
      
      if(tick >= 120 && tick <= 270){
         if(tick % 19 == 0){
            animatedLightningBolt(world,itemCenter,MiscUtils.randomSpherePoint(itemCenter,4,2.5),8,0.5,ParticleTypes.ELECTRIC_SPARK,8,1,0,0,false,2,30);
            SoundUtils.playSound(world, forgePos, SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.BLOCKS, 0.25f, 1.75f + 0.25f*(float)Math.random());
         }
      }
      
      if(tick == adjustTime(130,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 0.7f, 0.7f);
      }
      
      if(tick >= 200 && tick <= 280){
         int count = (int) Math.min(4,(tick-200) * 0.05) + 1;
         world.spawnParticles(ParticleTypes.OMINOUS_SPAWNING,itemCenter.x,itemCenter.y,itemCenter.z,count,0.2,0.2,0.2,1);
      }
      
      if(tick == adjustTime(280,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.BLOCKS, 1.25f, 0.7f);
      }
      
      if(tick == adjustTime(330,speedMod)){
         world.spawnParticles(ParticleTypes.FLASH,center.x,center.y+2,center.z,3,0.1,0.1,0.1,0.02);
      }
      
      if(tick % 2 == 0){
         return;
      }
      
      if(tick < 300){
         world.spawnParticles(ParticleTypes.END_ROD,center.x,center.y+5.5,center.z,1,1.5,1,1.5,0);
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
      
      
      ParticleEffect blue = new DustParticleEffect(Vec3d.unpackRgb(0x79e0fc).toVector3f(),0.7f);
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
      
      ParticleEffect white = new DustParticleEffect(Vec3d.unpackRgb(0xe6fff6).toVector3f(),0.7f);
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
   
   public static void arcanaCraftingAnim(ServerWorld world, BlockPos forgePos, ItemStack stack, double tickRaw, double speedMod){
      Vec3d center = forgePos.toCenterPos();
      int tick = (int) tickRaw;
      if(tick < 350){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> arcanaCraftingAnim(world, forgePos, stack, tickRaw+(1*speedMod), speedMod)));
      }
      if(tick == 0){
         ItemDisplayElement item = new ItemDisplayElement(stack);
         item.setGlowColorOverride(0x9404d6);
         item.setBrightness(new Brightness(15,15));
         item.setScale(new Vector3f(0.5f));
         
         ElementHolder holder = new ElementHolder() {
            int lifeTime = (int) (350 / speedMod);
            
            @Override
            protected void onTick(){
               super.onTick();
               
               if(lifeTime-- <= 0) {
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
         
         SoundUtils.playSound(world, forgePos, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 2f, 0.8f);
      }
      
      ParticleEffect purple = new DustParticleEffect(Vec3d.unpackRgb(0x9404d6).toVector3f(),0.7f);
      
      double starTicks = 75;
      for(float i = 0; i < Math.PI*2; i+= (float) (Math.PI/4.0f)){
         double radius = tick >= starTicks ? 1.15 : (-0.000782113805012*tick*tick + 0.0739918687092*tick); // Quadratic from https://www.desmos.com/calculator/vuyttamm67
         double height = tick >= starTicks ? 2.5 : 2.5*tick/starTicks;
         float rotation = i - 0.01f * tick;
         Vec3d starPos = center.add(new Vec3d(radius, 0.25+height, 0).rotateY(rotation));
         world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,starPos.x,starPos.y,starPos.z,1,0,0,0,0);
         if(tick >= starTicks && tick < 320){
            world.spawnParticles(purple,starPos.x,starPos.y,starPos.z,1,0.1,0.1,0.1,0);
         }
      }
      
      Vec3d itemCenter = new Vec3d(center.x,center.y+1.6,center.z);
      
      if(tick >= starTicks && tick < 300){
         world.spawnParticles(ParticleTypes.ENCHANT,center.x,center.y+2.5,center.z,3,0.2,0.2,0.2,1);
         world.spawnParticles(purple,center.x,center.y+2.5,center.z,3,0.8,0.8,0.8,0);
      }
      
      if(tick == adjustTime(50,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 2f, 0.8f);
      }
      
      if(tick >= 120 && tick <= 270){
         if(tick % 19 == 0){
            animatedLightningBolt(world,itemCenter,MiscUtils.randomSpherePoint(itemCenter,4,2.5),8,0.5,ParticleTypes.ELECTRIC_SPARK,8,1,0,0,false,2,30);
            SoundUtils.playSound(world, forgePos, SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.BLOCKS, 0.25f, 1.75f + 0.25f*(float)Math.random());
         }
      }
      
      if(tick == adjustTime(130,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 0.7f, 0.7f);
      }
      
      if(tick >= 200 && tick <= 280){
         int count = (int) Math.min(4,(tick-200) * 0.05) + 1;
         world.spawnParticles(ParticleTypes.OMINOUS_SPAWNING,itemCenter.x,itemCenter.y,itemCenter.z,count,0.2,0.2,0.2,1);
         world.spawnParticles(ParticleTypes.WITCH,itemCenter.x,itemCenter.y,itemCenter.z,count,0.2,0.5,0.2,0.05);
      }
      
      if(tick == adjustTime(280,speedMod)){
         SoundUtils.playSound(world, forgePos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.BLOCKS, 1.25f, 0.7f);
      }
      
      if(tick == adjustTime(330,speedMod)){
         world.spawnParticles(ParticleTypes.FLASH,center.x,center.y+2,center.z,3,0.1,0.1,0.1,0.02);
         world.spawnParticles(ParticleTypes.WITCH,itemCenter.x,itemCenter.y,itemCenter.z,100,0.2,0.5,0.2,0.1);
      }
      
      if(tick % 2 == 0){
         return;
      }
      
      if(tick < 300){
         world.spawnParticles(ParticleTypes.END_ROD,center.x,center.y+5.5,center.z,1,1.5,1,1.5,0);
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
      
      
      ParticleEffect blue = new DustParticleEffect(Vec3d.unpackRgb(0x79e0fc).toVector3f(),0.7f);
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
      
      ParticleEffect white = new DustParticleEffect(Vec3d.unpackRgb(0xd9daff).toVector3f(),0.7f);
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
      if(!living.isAlive() || living.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) == null || !(living.getEntityWorld() instanceof ServerWorld world)){
         return;
      }
      double eHeight = living.getHeight();
      double eWidth = living.getWidth();
      double circleHeight = eHeight*0.6;
      double circleRadius = eWidth / 1.6;
      Vec3d circleCenter = living.getPos().add(0,eHeight/1.8,0);
      ParticleEffect purple = new DustParticleEffect(Vec3d.unpackRgb(0xa100e6).toVector3f(),0.7f);
      
      int intervals = (int) (15 * Math.sqrt(circleRadius*circleRadius+circleHeight*circleHeight));
      double dA = Math.PI * 2 / intervals;
      for(int i = 0; i < intervals; i++){
         double angle = dA * i + (tick / Math.PI);
         double xOff = circleRadius * Math.cos(angle);
         double zOff = circleRadius * Math.sin(angle);
         double yOff = (xOff+zOff) * 0.3536 * circleHeight/circleRadius;
         
         world.spawnParticles(purple,xOff+circleCenter.x,yOff+circleCenter.y,zOff+circleCenter.z,1,0,0,0,0);
         world.spawnParticles(purple,xOff+circleCenter.x,-yOff+circleCenter.y,zOff+circleCenter.z,1,0,0,0,0);
      }
      
      if(amplifier > 0 && tick % 5 == 0){
         circle(world,null,circleCenter,ParticleTypes.WITCH,circleRadius*1.2,intervals/2,1,0,0);
         circle(world,null,circleCenter.add(0,-circleHeight,0),ParticleTypes.WITCH,circleRadius*1.2,intervals/2,1,0,0);
      }
      ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> ensnaredEffect(living, amplifier,tick < 40 ? tick+1 : 0)));
   }
   
   public static void aequalisTransmuteAnim(ServerWorld world, Vec3d center, double rawTick, Vec2f rotation, double speedMod, ItemStack input, ItemStack output, ItemStack reagent1, ItemStack reagent2, ItemStack aequalis){
      ParticleEffect blue = new DustParticleEffect(Vec3d.unpackRgb(0x12ccff).toVector3f(),0.7f);
      ParticleEffect blueSmall = new DustParticleEffect(Vec3d.unpackRgb(0x12ccff).toVector3f(),0.4f);
      ParticleEffect purple = new DustParticleEffect(Vec3d.unpackRgb(0xa100e6).toVector3f(),0.5f);
      ParticleEffect pink = new DustParticleEffect(Vec3d.unpackRgb(0xd300e6).toVector3f(),0.8f);
      
      int tick = (int)(rawTick);
      int intBonus = tick % 3;
      int n = output == null || output.isEmpty() ? 3 : 4;
      
      List<Vec3d> itemCenters = getCirclePoints(center,1.75+0.5*Math.sin(-Math.PI*tick/60.0) / 30.0,n,tick * 6 * Math.PI / 500.0);
      
      if(tick == 0){
         SoundUtils.playSound(world, BlockPos.ofFloored(center),SoundEvents.BLOCK_BEACON_POWER_SELECT,SoundCategory.BLOCKS,1,1.5f);
         
         ItemDisplayElement aequalisElem = new ItemDisplayElement(aequalis);
         aequalisElem.setGlowColorOverride(ArcanaColors.EQUAYUS_COLOR);
         aequalisElem.setBrightness(new Brightness(15,15));
         aequalisElem.setScale(new Vector3f(0.5f));
         aequalisElem.setTranslation(center.subtract(BlockPos.ofFloored(center).toCenterPos()).toVector3f());
         
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
         
         ElementHolder aequalisHolder = new ElementHolder() {
            int lifeTime = (int) (500 / speedMod);
            
            @Override
            protected void onTick(){
               super.onTick();
               
               if(lifeTime-- <= 0) {
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
         ChunkAttachment.ofTicking(aequalisHolder, world, BlockPos.ofFloored(center));
         
         
         ElementHolder inputHolder = makeAequalisItemHolder(inputElem,center,n,0,speedMod);
         ElementHolder reagent1Holder = makeAequalisItemHolder(reagent1Elem,center,n,1,speedMod);
         ElementHolder reagent2Holder = makeAequalisItemHolder(reagent2Elem,center,n,2,speedMod);
         
         inputHolder.addElement(inputElem);
         ChunkAttachment.ofTicking(inputHolder, world, BlockPos.ofFloored(center));
         
         reagent1Holder.addElement(reagent1Elem);
         ChunkAttachment.ofTicking(reagent1Holder, world, BlockPos.ofFloored(center));
         
         reagent2Holder.addElement(reagent2Elem);
         ChunkAttachment.ofTicking(reagent2Holder, world, BlockPos.ofFloored(center));
         
         if(output != null && !output.isEmpty()){
            ItemDisplayElement outputElem = new ItemDisplayElement(output);
            outputElem.setGlowColorOverride(ArcanaColors.EQUAYUS_COLOR);
            outputElem.setBrightness(new Brightness(15,15));
            outputElem.setScale(new Vector3f(0.0f));
            
            ElementHolder outputHolder = makeAequalisItemHolder(outputElem,center,n,3,speedMod);
            outputHolder.addElement(outputElem);
            ChunkAttachment.ofTicking(outputHolder, world, BlockPos.ofFloored(center));
         }
      }
      
      double innerSize = tick < 50 ? tick/100.0 : 0.2*Math.sin(-Math.PI*tick/50.0-0.25)+0.45;
      List<Pair<Vec3d, Vec3d>> innerPairs = getIcosahedronPairs(getIcosahedronPoints().stream().map(
            point -> point.rotateZ(-0.55357f).rotateY((float) (rawTick * 2*Math.PI / 500.0f)).multiply(innerSize).add(center)
      ).toList());
      double outerSize = tick < 75 ? tick*2/75.0 : tick > 450 ? 15 - 0.03*tick : 0.25*Math.sin(-Math.PI*tick/75.0-Math.PI/2.0)+1.75;
      List<Pair<Vec3d, Vec3d>> outerPairs = getIcosahedronPairs(getIcosahedronPoints().stream().map(
            point -> point.rotateZ(-0.55357f).rotateY((float) (rawTick * 2*Math.PI / 500.0f)).multiply(outerSize).add(center)
      ).toList());
      
      for(Pair<Vec3d, Vec3d> pair :innerPairs){
         line(world,null,pair.getRight(),pair.getLeft(),blueSmall,5+intBonus,1,0,0,1);
      }
      
      if(tick < 490){
         for(Pair<Vec3d, Vec3d> pair : outerPairs){
            line(world,null,pair.getRight(),pair.getLeft(),pink,10+intBonus,1,0,0,1);
         }
      }
      
      if(tick > 50){
         int i = 0;
         double radius = tick < 450 ? 0.5 : 5 - 0.01 * tick;
         for(Vec3d itemCenter : itemCenters){
            List<Vec3d> circlePoints1 = getCirclePoints(new Vec3d(0,0,0),radius,24,Math.PI*tick / 30.0).stream().map(point -> point.rotateX((float) (Math.PI/2.0f)).rotateY((float) (tick * 6 * Math.PI / 500.0)).add(itemCenter)).toList();
            List<Vec3d> circlePoints2 = getCirclePoints(new Vec3d(0,0,0),radius,24,Math.PI*tick / 30.0).stream().map(point -> point.rotateX((float) (Math.PI/2.0f)).rotateY((float) (-tick * 6 * Math.PI / 500.0)).add(itemCenter)).toList();
            double itemDY = 0.5 * Math.sin(Math.PI * tick / 100.0 + i * Math.PI * 2.0 / n);
            for(Vec3d circlePoint : circlePoints1){
               world.spawnParticles(purple,circlePoint.x,circlePoint.y + itemDY,circlePoint.z,1,0,0,0,0);
            }
            for(Vec3d circlePoint : circlePoints2){
               world.spawnParticles(purple,circlePoint.x,circlePoint.y + itemDY,circlePoint.z,1,0,0,0,0);
            }
            
            if(tick > 120 && tick < 450){
               world.spawnParticles(ParticleTypes.WITCH,itemCenter.x,itemCenter.y + itemDY + 0.1,itemCenter.z,3,0.15,0.15,0.15,0);
            }
            i++;
         }
      }
      
      if(tick > 60 && tick < 450){
         if(Math.random() < 0.1){
            animatedLightningBolt(world,center,outerPairs.get((int)(Math.random()*outerPairs.size())).getRight(),12,0.5,ParticleTypes.ELECTRIC_SPARK,16,1,0,0,false,0,15);
         }
         if(tick % 6 == 0){
            world.spawnParticles(ParticleTypes.END_ROD,center.x,center.y,center.z,1,1.6,1.6,1.6,0);
         }
      }
      
      if(tick % 70 == 20){
         SoundUtils.playSound(world, BlockPos.ofFloored(center),SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,SoundCategory.BLOCKS,1,((float)Math.random())*.5f + 0.7f);
      }
      if(tick % 100 == 35){
         SoundUtils.playSound(world, BlockPos.ofFloored(center),SoundEvents.BLOCK_PORTAL_AMBIENT,SoundCategory.BLOCKS,0.5f,((float)Math.random())*.4f + 1.2f);
      }
      
      
      if(tick < 500){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> aequalisTransmuteAnim(world, center, rawTick+(1*speedMod), rotation, speedMod, input, output, reagent1, reagent2, aequalis)));
      }else{
         world.spawnParticles(ParticleTypes.FLASH,center.x,center.y,center.z,5,0.3,0.3,0.3,0);
         SoundUtils.playSound(world, BlockPos.ofFloored(center), SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM, SoundCategory.BLOCKS,1,0.8f);
      }
   }
   
   private static ElementHolder makeAequalisItemHolder(ItemDisplayElement element, Vec3d center, int n, int i, double speedMod){
      return new ElementHolder() {
         int lifeTime = (int) (500 / speedMod);
         
         @Override
         protected void onTick(){
            super.onTick();
            
            if(lifeTime-- <= 0) {
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
                        .subtract(center).add(0,itemDY,0).add(center.subtract(BlockPos.ofFloored(center).toCenterPos())).toVector3f());
               }
            }
         }
      };
   }
   
   public static void transmutationAltarAnim(ServerWorld world, Vec3d center, double rawTick, Direction direction, double speedMod){
      ParticleEffect blue = new DustParticleEffect(Vec3d.unpackRgb(0x12ccff).toVector3f(),0.7f);
      ParticleEffect purple = new DustParticleEffect(Vec3d.unpackRgb(0xa100e6).toVector3f(),0.7f);
      ParticleEffect pink = new DustParticleEffect(Vec3d.unpackRgb(0xd300e6).toVector3f(),0.7f);
      Vec3d effectCenter = center.add(0,0.6,0);
      
      int tick = (int)(rawTick);
      double theta = Math.PI*tick / 30.0;
      int intBonus = tick % 3;
      int itemCI = 20; double itemCR = 0.7; double itemOutset = 3;
      int nodeCI = 8; double nodeCR = 0.25; double nodeOutset = 2.2;
      double outerRadius = 4.4; double innerRadius = 4.0;
      
      //MathHelper.clamp((tick-20.0) / (40.0-20.0),0,1)
      circle(world,null,effectCenter,pink,innerRadius*MathHelper.clamp(tick/60.0,0,1),125,1,0,0, theta);
      circle(world,null,effectCenter,pink,outerRadius*MathHelper.clamp(tick/60.0,0,1),125,1,0,0, theta);
      circle(world,null,effectCenter,purple,itemCR*MathHelper.clamp(tick/20.0,0,1),itemCI,1,0,0, theta);
      
      if(tick > 260){
         circle(world,null,effectCenter,ParticleTypes.WITCH,(outerRadius+innerRadius)/2,50,1,0.1,0, theta);
      }
      
      for(float i = 0; i < Math.PI*2; i+= (float) (Math.PI/2.0f)){
         if(tick < 70) continue;
         
         Vec3d itemCenter = effectCenter.add(new Vec3d(itemOutset,0,0).rotateY(i));
         circle(world,null,itemCenter,purple,itemCR*MathHelper.clamp((tick-70.0) / 40.0,0,1),itemCI,1,0,0, theta);
         
         if(tick < 90) continue;
         Vec3d centerLine1P1 = effectCenter.add(new Vec3d((itemCR+0.1)*.71,0,itemCR*.71).rotateY(i));
         Vec3d centerLine1P2 = effectCenter.add(new Vec3d(nodeOutset-nodeCR*.71,0,nodeOutset-nodeCR*.71).rotateY(i));
         line(world,null,centerLine1P1,centerLine1P2,blue,15+intBonus,1,0,0,MathHelper.clamp((tick-90.0) / 50.0,0,1));
         
         Vec3d centerLine2P1 = effectCenter.add(new Vec3d(itemCR+0.1,0,0).rotateY(i));
         Vec3d centerLine2P2 = effectCenter.add(new Vec3d(itemOutset-itemCR,0,0).rotateY(i));
         line(world,null,centerLine2P1,centerLine2P2,blue,10+intBonus,1,0,0,MathHelper.clamp((tick-90.0) / 30.0,0,1));
         
         if(tick < 110) continue;
         
         Vec3d crossLine1aP1 = effectCenter.add(new Vec3d(itemOutset-(itemCR*.71+0.1),0,itemCR*.71+0.1).rotateY(i));
         Vec3d crossLine1bP1 = effectCenter.add(new Vec3d(itemCR*.71,0,itemOutset-itemCR*.71).rotateY(i));
         Vec3d crossLine1P2 = effectCenter.add(new Vec3d(itemOutset*.5,0,itemOutset*.5).rotateY(i));
         line(world,null,crossLine1aP1,crossLine1P2,blue,7+intBonus,1,0,0,MathHelper.clamp((tick-110.0) / 50.0,0,1));
         line(world,null,crossLine1bP1,crossLine1P2,blue,7+intBonus,1,0,0,MathHelper.clamp((tick-110.0) / 50.0,0,1));
         
         Vec3d crossLine2P1 = effectCenter.add(new Vec3d(itemOutset,0,itemCR+0.1).rotateY(i));
         Vec3d crossLine2P2 = effectCenter.add(new Vec3d(nodeOutset+nodeCR*.71,0,nodeOutset-nodeCR*.71).rotateY(i));
         line(world,null,crossLine2P1,crossLine2P2,blue,7+intBonus,1,0,0,MathHelper.clamp((tick-110.0) / 40.0,0,1));
         
         Vec3d crossLine3P1 = effectCenter.add(new Vec3d(itemCR+0.1,0,itemOutset).rotateY(i));
         Vec3d crossLine3P2 = effectCenter.add(new Vec3d(nodeOutset-nodeCR*.71,0,nodeOutset+nodeCR*.71).rotateY(i));
         line(world,null,crossLine3P1,crossLine3P2,blue,7+intBonus,1,0,0,MathHelper.clamp((tick-110.0) / 40.0,0,1));
         
         if(tick < 150) continue;
         
         Vec3d nodeCenter = effectCenter.add(new Vec3d(nodeOutset,0,nodeOutset).rotateY(i));
         circle(world,null,nodeCenter,purple,nodeCR*MathHelper.clamp((tick-150.0) / 20.0,0,1),nodeCI,1,0,0, theta);
         
         Vec3d outerLine1P1 = effectCenter.add(new Vec3d(itemOutset+itemCR+0.1,0,0).rotateY(i));
         Vec3d outerLine1P2 = effectCenter.add(new Vec3d(outerRadius-0.02,0,0).rotateY(i));
         line(world,null,outerLine1P1,outerLine1P2,blue,3+intBonus,1,0,0,MathHelper.clamp((tick-150.0) / 40.0,0,1));
         
         double outerIZ = 0.5*(-itemOutset+Math.sqrt(2*outerRadius*outerRadius-itemOutset*itemOutset));
         double outerIX = outerIZ + itemOutset;
         Vec3d outerLine2P1 = effectCenter.add(new Vec3d(itemOutset+itemCR*.71+0.1,0,itemCR*.71+0.1).rotateY(i));
         Vec3d outerLine2P2 = effectCenter.add(new Vec3d(outerIX-0.02,0,outerIZ-0.02).rotateY(i));
         line(world,null,outerLine2P1,outerLine2P2,blue,5+intBonus,1,0,0,MathHelper.clamp((tick-150.0) / 40.0,0,1));
         
         Vec3d outerLine3P1 = effectCenter.add(new Vec3d(itemOutset+itemCR*.71+0.1,0,-(itemCR*.71+0.1)).rotateY(i));
         Vec3d outerLine3P2 = effectCenter.add(new Vec3d(outerIX-0.02,0,-(outerIZ-0.02)).rotateY(i));
         line(world,null,outerLine3P1,outerLine3P2,blue,5+intBonus,1,0,0,MathHelper.clamp((tick-150.0) / 40.0,0,1));
         
         if(tick < 160) continue;
         
         Vec3d outerLine4P1 = effectCenter.add(new Vec3d(nodeOutset+nodeCR*.71+0.1,0,nodeOutset+nodeCR*.71+0.1).rotateY(i));
         Vec3d outerLine4P2 = effectCenter.add(new Vec3d(innerRadius*.71-0.02,0,innerRadius*.71-0.02).rotateY(i));
         line(world,null,outerLine4P1,outerLine4P2,blue,3+intBonus,1,0,0,MathHelper.clamp((tick-150.0) / 30.0,0,1));
         
         if(tick < 450) continue;
         Vec3d itemSpot = effectCenter.add(new Vec3d(itemOutset,0,0).rotateY(i));
         world.spawnParticles(ParticleTypes.WITCH,itemSpot.x,itemSpot.y,itemSpot.z,1,0.15,0.15,0.15,0);
         world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,itemSpot.x,itemSpot.y,itemSpot.z,3,0.25,0.25,0.25,0);
         world.spawnParticles(ParticleTypes.END_ROD,itemSpot.x,itemSpot.y,itemSpot.z,1,0.25,0.25,0.25,0.02);
         
         if(tick == 500){
            world.spawnParticles(ParticleTypes.FLASH,itemSpot.x,itemSpot.y+0.25,itemSpot.z,3,0.25,0.25,0.25,0);
         }
      }
      
      if(tick > 180){
         double dA = Math.PI * 2 / 50;
         double angle = dA * tick;
         double x = (outerRadius+innerRadius)/2 * Math.cos(angle) + effectCenter.x;
         double z = (outerRadius+innerRadius)/2 * Math.sin(angle) + effectCenter.z;
         double y = tick > 280 ? effectCenter.y + 1: effectCenter.y;
         
         world.spawnParticles(ParticleTypes.WITCH,x,y,z,12,0.25,0.25,0.25,0);
      }
      
      if(tick == 0){
         SoundUtils.playSound(world, BlockPos.ofFloored(center),SoundEvents.BLOCK_BEACON_POWER_SELECT,SoundCategory.BLOCKS,1,1.5f);
      }
      if(tick % 70 == 20){
         SoundUtils.playSound(world, BlockPos.ofFloored(center),SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,SoundCategory.BLOCKS,1,((float)Math.random())*.5f + 0.7f);
      }
      if(tick % 100 == 35){
         SoundUtils.playSound(world, BlockPos.ofFloored(center),SoundEvents.BLOCK_PORTAL_AMBIENT,SoundCategory.BLOCKS,0.5f,((float)Math.random())*.4f + 1.2f);
      }
      
      if(tick < 500){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> transmutationAltarAnim(world,center,rawTick+(1*speedMod), direction,speedMod)));
      }
   }
   
   public static void craftForge(ServerWorld world, BlockPos pos, int tick){
      Vec3d center = pos.toCenterPos();
      if(tick == 100){
         world.spawnParticles(ParticleTypes.FLASH,center.x,center.y,center.z,3,0.4,0.4,0.4,0);
         world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,center.x,center.y,center.z,25,0.6,0.8,0.6,0);
         SoundUtils.playSound(world,pos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.BLOCKS, 2, 0.8f);
      }else{
         world.spawnParticles(ParticleTypes.END_ROD,center.x,center.y,center.z,1,0.6,0.8,0.6,0);
         world.spawnParticles(ParticleTypes.WITCH,center.x,center.y,center.z,1,0.6,0.8,0.6,0);
      }
   }
   
   public static void craftTome(ServerWorld world, BlockPos pos, int tick){
      Vec3d center = pos.toCenterPos();
      if(tick == 100){
         world.spawnParticles(ParticleTypes.FLASH,center.x,center.y,center.z,3,0.4,0.4,0.4,0);
         world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,center.x,center.y,center.z,25,0.6,0.8,0.6,0);
         SoundUtils.playSound(world,pos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.BLOCKS, 2, 0.8f);
      }else{
         world.spawnParticles(ParticleTypes.ENCHANT,center.x,center.y+1,center.z,10,0.3,0.3,0.3,1);
         world.spawnParticles(ParticleTypes.WITCH,center.x,center.y,center.z,2,0.6,0.8,0.6,0);
      }
   }
   
   public static void stormcallerAltarAnim(ServerWorld world, Vec3d center, int tick){
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
            
            world.spawnParticles(ParticleTypes.FISHING,x,y,z,3,0,0,0,0.01);
            world.spawnParticles(ParticleTypes.FALLING_WATER,x,y,z,1,0,0,0,0.01);
         }
      }
      
      for(int i = 0; i < 2; i++){
         double angle = Math.random()*Math.PI*2;
         double r = Math.random()*1+3;
         double x = r * Math.cos(angle) + center.x;
         double z = r * Math.sin(angle) + center.z;
         double y = center.y + 4.5;
         
         world.spawnParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,x,y,z,2,0.2,0.2,0.2,0.002);
         world.spawnParticles(ParticleTypes.FALLING_WATER,x,y,z,5,0.3,0.3,0.3,1);
      }
      for(int i = 0; i < 5; i++){
         double angle = Math.random()*Math.PI*2;
         double r = Math.random()*1+3;
         double x = r * Math.cos(angle) + center.x;
         double z = r * Math.sin(angle) + center.z;
         double y = center.y + 4.5;
         
         world.spawnParticles(ParticleTypes.CLOUD,x,y,z,4,0.2,0.2,0.2,0.002);
         world.spawnParticles(ParticleTypes.FALLING_WATER,x,y,z,3,0.3,0.3,0.3,1);
      }
      
      if(tick < 100){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> stormcallerAltarAnim(world,center,tick+1)));
      }else{
         LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
         lightning.setPosition(center);
         world.spawnEntity(lightning);
      }
   }
   
   public static void celestialAltarAnim(ServerWorld world, Vec3d center, int tick, Direction direction){
      if(tick == 0){
         SoundUtils.playSound(world,BlockPos.ofFloored(center), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 2, 0.5f);
      }
      
      double phi = Math.PI * (3 - Math.sqrt(5));
      double theta = 2*Math.PI / 100 * tick;
      int points = 100;
      ParticleEffect black = new DustParticleEffect(Vec3d.unpackRgb(0x000000).toVector3f(),2.0f);
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
         Vec3d point = new Vec3d(x,y,z);
         point = point.multiply(5).add(center.x, center.y, center.z);
         
         world.spawnParticles(black,point.x,point.y,point.z,blackCount,blackDelta,blackDelta,blackDelta,0);
      }
      
      ParticleEffect sun = new DustParticleEffect(Vec3d.unpackRgb(0xd1a400).toVector3f(),2.0f);
      ParticleEffect moon = new DustParticleEffect(Vec3d.unpackRgb(0x1670f0).toVector3f(),2.0f);
      
      if(tick > 100){
         if(tick % 3 == 0){
            world.spawnParticles(ParticleTypes.END_ROD,center.x,center.y+2.5,center.z,8,3,1.5,3,0);
         }
         
         Vec3d rotVec = switch(direction){
            case SOUTH -> new Vec3d(-1,1,-1);
            case EAST -> new Vec3d(1,1,-1);
            case WEST -> new Vec3d(-1,1,1);
            default -> new Vec3d(1,1,1);
         };
         
         if(tick < 400 && tick % 2 == 0){
            Vec3d celestPos;
            
            if(tick < 175){
               double y = (tick-100) / 25.0;
               celestPos = new Vec3d(2.5,y,-2.5).multiply(rotVec);
            }else if(tick < 325){
               double t = Math.PI*2 * (tick-175)/150 - (Math.PI/4);
               double x = Math.cos(t) * 2.5;
               double z = Math.sin(t) * 2.5;
               celestPos = new Vec3d(x,3,z).multiply(rotVec);
            }else{
               double y = 3-((tick-325) / 25.0);
               celestPos = new Vec3d(2.5,y,-2.5).multiply(rotVec);
            }
            
            sphere(world,null,center.add(celestPos),sun,0.5,10,3,0.15,0,theta);
            sphere(world,null,center.add(celestPos.multiply(-1,1,-1)),moon,0.5,10,3,0.15,0,theta);
         }
      }
      
      
      if(tick < 500){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> celestialAltarAnim(world,center,tick+1, direction)));
      }
   }
   
   public static void starpathAltarAnim(ServerWorld world, Vec3d center){
      SoundUtils.playSound(world,BlockPos.ofFloored(center), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 2, 0.5f);
      starpathAltarAnim(world,center,0,new ArrayList<>(),new ArrayList<>());
   }
   
   private static void starpathAltarAnim(ServerWorld world, Vec3d center, int tick, List<Pair<Vec3d,Integer>> groundStars, List<Vec3d> skyStars){
      double phi = Math.PI * (3 - Math.sqrt(5));
      double theta = 2*Math.PI / 100 * tick;
      int points = 100;
      ParticleEffect black = new DustParticleEffect(Vec3d.unpackRgb(0x000000).toVector3f(),2.0f);
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
         Vec3d point = new Vec3d(x,y,z);
         point = point.multiply(5).add(center.x, center.y, center.z);
         
         world.spawnParticles(black,point.x,point.y,point.z,blackCount,blackDelta,blackDelta,blackDelta,0);
      }
      
      if(tick >= 100){
         if(tick % 2 == 0){
            for(int i = 0; i < groundStars.size(); i++){
               Pair<Vec3d,Integer> groundStar = groundStars.get(i);
               Vec3d starPos = groundStar.getLeft();
               world.spawnParticles(ParticleTypes.END_ROD,starPos.x,starPos.y,starPos.z,1,0,0,0,0);
               groundStars.set(i,new Pair<>(starPos.add(0,0.125,0),groundStar.getRight()-1));
            }
            groundStars.removeIf((p)->p.getRight()<=0);
            if(groundStars.size() < 8){ // Re-add stars
               for(int i = 0; i < 2; i++){
                  double t = Math.random()*Math.PI*2;
                  double r = (Math.random()*3+1);
                  double x = Math.cos(t) * r;
                  double z = Math.sin(t) * r;
                  int lifeTime = (int)(Math.random()*8+4);
                  groundStars.add(new Pair<>(new Vec3d(x,0,z).add(center.x, center.y+0.5, center.z),lifeTime));
               }
            }
            
         }
         if(tick % 3 == 0){
            for(Vec3d skyStar : skyStars){
               world.spawnParticles(ParticleTypes.END_ROD,skyStar.x,skyStar.y,skyStar.z,1,0.05,0.05,0.05,0);
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
               skyStars.add(new Vec3d(center.x+x,center.y+y,center.z+z));
            }
         }
         
         if(tick >= 140){
            ParticleEffect white = new DustParticleEffect(Vec3d.unpackRgb(0x944ec7).toVector3f(),0.5f);
            int connections = Math.min(8,(tick-140) / 30);
            for(int i = 0; i < connections+1; i++){
               line(world,null,skyStars.get(i),skyStars.get(i+1),white,20,1,0.05,0);
            }
            if(tick <= 380 && (tick-140) % 30 == 0){
               SoundUtils.playSound(world,BlockPos.ofFloored(center), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 2, 0.5f + (connections*0.2f));
            }
         }
      }
      
      if(tick == 440){
         SoundUtils.playSound(world,BlockPos.ofFloored(center), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 2, 1.5f);
      }
      
      if(tick < 500){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> starpathAltarAnim(world,center,tick+1,groundStars,skyStars)));
      }
   }
   
   public static void midnightEnchanterAnim(ServerWorld world, Vec3d center, int tick){
      if(tick % 2 == 0){
         return;
      }
      
      world.spawnParticles(ParticleTypes.ENCHANT,center.getX(),center.getY()+0.75,center.getZ(),5,0.1,0.1,0.1,1);
      
      ParticleEffect blue = new DustParticleEffect(Vec3d.unpackRgb(0x12ccff).toVector3f(),0.7f);
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
      
      
      ParticleEffect purple = new DustParticleEffect(Vec3d.unpackRgb(0xa100e6).toVector3f(),0.7f);
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
      
      ParticleEffect pink = new DustParticleEffect(Vec3d.unpackRgb(0xd300e6).toVector3f(),0.7f);
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
   
   public static void stellarCoreAnim(ServerWorld world, Vec3d center, int tick, Direction direction){
      if(tick % 2 == 0) return;
      sphere(world,null,center,ParticleTypes.FLAME,1.2,30,2,0.2,0.03,Math.PI*2*tick/300);
      sphere(world,null,center,ParticleTypes.LAVA,1.2,10,2,0.2,0.02,Math.PI*2*tick/300);
      sphere(world,null,center,ParticleTypes.WAX_ON,.5,10,2,0.05,0.02,Math.PI*2*tick/300);
      world.spawnParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,center.getX(),center.getY(),center.getZ(),1,0.5,0.5,0.5,0.02);
      
      Vec3d pos = center.subtract(Vec3d.of(direction.getVector())).add(0,3.5,0);
      world.spawnParticles(ParticleTypes.LAVA,pos.getX(),pos.getY(),pos.getZ(),1,0.25,0.05,0.25,0.02);
   }
   
   public static void arcaneSingularityAnim(ServerWorld world, Vec3d center, int tick, Direction direction, double fillPercent){
      if(tick % 2 == 0) return;
      double L = 300.0;
      double animPercent = tick/L;
      double piPercent = Math.PI*2*animPercent;
      ParticleEffect black = new DustParticleEffect(Vec3d.unpackRgb(0x000000).toVector3f(),2.0f);
      ParticleEffect blue = new DustParticleEffect(Vec3d.unpackRgb(0x00ECFF).toVector3f(),0.75f);
      sphere(world,null,center,black,0.2+0.65*fillPercent,(int)(20*fillPercent+5),1,0.025,0,5*piPercent);
      sphere(world,null,center,ParticleTypes.WITCH,0.5+0.85*fillPercent,(int)(30*fillPercent+12),1,0.05,0,3*piPercent);
      sphere(world,null,center,blue,0.4+0.75*fillPercent,(int)(70*fillPercent+12),1,0.01,0,-3*piPercent);
      world.spawnParticles(ParticleTypes.WITCH,center.getX(),center.getY()-1.2,center.getZ(),4,0.3,0.4,0.3,0);
      
      List<Vec3d> rods = new ArrayList<>(Arrays.asList(
            new Vec3d(0,-1,2), new Vec3d(-2,-1,0), new Vec3d(0,-1,-2), new Vec3d(2,-1,0),
            new Vec3d(-1,-2,-1), new Vec3d(1,-2,-1), new Vec3d(-1,-2,1), new Vec3d(1,-2,1)
      ));
      rods.remove(direction.getHorizontal());
      
      int N = 3;
      double[] R = new double[N];
      for(int i = 0; i < R.length; i++){
         R[i] = 0.2*(1-((animPercent+((double) i /N)) % 1))+.1;
      }
      double W = 4.3;
      for(int i = 0; i < rods.size(); i++){
         Vec3d pos = center.add(rods.get(i));
         for(int j = 0; j < R.length; j++){
            world.spawnParticles(blue,pos.getX()+R[j]*Math.cos(W*(piPercent+((double) j /N))),pos.getY()+1.25*((animPercent+((double) j /N)) % 1),pos.getZ()+R[j]*Math.sin(W*(piPercent+((double) j /N))),3,0.01,0.01,0.01,1);
         }
      }
   }
   
   public static void nulConstructSummon(ServerWorld world, Vec3d pos, int tick){
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
            
            world.spawnParticles(ParticleTypes.SOUL,x,y,z,1,0,0,0,0.01);
            world.spawnParticles(ParticleTypes.FALLING_OBSIDIAN_TEAR,x,y,z,1,0,0,0,0.01);
         }
      }
      
      world.spawnParticles(ParticleTypes.PORTAL,pos.x,pos.y,pos.z,20,0.3,0.3,0.3,1);
      
      
      if(tick < 220){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> nulConstructSummon(world,pos,tick+1)));
      }else{
         world.spawnParticles(ParticleTypes.WITCH,pos.x,pos.y,pos.z,150,1,1,1,0.01);
      }
   }
   
   public static void exaltedConstructSummon(ServerWorld world, Vec3d pos, int tick){
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
            
            world.spawnParticles(ParticleTypes.SOUL,x,y,z,1,0,0,0,0.01);
            world.spawnParticles(ParticleTypes.FALLING_OBSIDIAN_TEAR,x,y,z,1,0,0,0,0.01);
         }
      }
      
      world.spawnParticles(ParticleTypes.PORTAL,pos.x,pos.y,pos.z,20,0.3,0.3,0.3,1);
      world.spawnParticles(ParticleTypes.DRAGON_BREATH,pos.x,pos.y+0.75,pos.z,3,0.3,0.3,0.3,0.03);
      
      if(tick%2 == 0){
         ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0xFF00D4).toVector3f(),0.75f);
         Vec3d circleCenter = pos.add(0,-1,0);
         double r = 2.5;
         float t = (float)(Math.PI/220.0*tick);
         double sqrt3 = Math.sqrt(3);
         
         circle(world,null,circleCenter,dust,r,40,1,0,1);
         
         Vec3d[] tri1 = {new Vec3d(0, 0, r),new Vec3d(-r*sqrt3/2, 0, -r/2),new Vec3d(r*sqrt3/2, 0, -r/2)};
         Vec3d[] tri2 = {new Vec3d(0, 0, -r),new Vec3d(-r*sqrt3/2, 0, r/2),new Vec3d(r*sqrt3/2, 0, +r/2)};
         for(int i = 0; i < 3; i++){
            Vec3d p1 = tri1[i].rotateY(t).add(circleCenter);
            Vec3d p2 = tri1[(i+1)%3].rotateY(t).add(circleCenter);
            Vec3d p3 = tri2[i].rotateY(t).add(circleCenter);
            Vec3d p4 = tri2[(i+1)%3].rotateY(t).add(circleCenter);
            line(world,null,p1,p2,dust,12,1,0,1);
            line(world,null,p3,p4,dust,12,1,0,1);
         }
      }
      
      if(tick < 220){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> exaltedConstructSummon(world,pos,tick+1)));
      }else{
         world.spawnParticles(ParticleTypes.WITCH,pos.x,pos.y,pos.z,150,1,1,1,0.01);
      }
   }
   
   public static void nulConstructNecroticShroud(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.LARGE_SMOKE,pos.getX(),pos.getY()+1.5,pos.getZ(),150,1.5,1.5,1.5,0.07);
   }
   
   public static void nulConstructDarkConversion(ServerWorld world, Vec3d pos){
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0x9e0945).toVector3f(),0.8f);
      world.spawnParticles(dust, pos.getX(), pos.getY() + 1.75, pos.getZ(), 10,0.75,1,0.75,0.03);
   }
   
   public static void nulConstructReflectiveArmor(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.END_ROD, pos.getX(), pos.getY() + 1.75, pos.getZ(), 3,0.75,1,0.75,0.03);
   }
   
   public static void nulConstructCurseOfDecay(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.SOUL, pos.getX(), pos.getY() + 1, pos.getZ(), 20,0.5,1,0.5,0.07);
   }
   
   public static void nulConstructReflexiveBlast(ServerWorld world, Vec3d pos, int calls){
      double radius = .5+calls*4;
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0x36332b).toVector3f(),1.5f);
      sphere(world,null,pos,dust,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,calls*Math.PI*2/5);
      if(calls < 5){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> nulConstructReflexiveBlast(world,pos,calls + 1)));
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
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> webOfFireCast(world, type,caster,hits,range,calls + 1)));
      }
   }
   
   public static void pyroblastExplosion(ServerWorld world, ParticleEffect type, Vec3d pos, double range, int calls){
      double radius = .5+calls*(range/5);
      sphere(world,null,pos,type,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,calls*Math.PI*2/5);
      if(calls < 5){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> pyroblastExplosion(world, type,pos,range,calls + 1)));
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
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> arcaneFlakArrowDetonate(world, pos,range,calls + 1)));
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
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> expulsionArrowEmit(world, pos,range,calls + 1)));
      }
   }
   
   public static void smokeArrowEmit(ServerWorld world, Vec3d pos){
      if(Math.random() < 0.1){
         spawnLongParticle(world,ParticleTypes.LARGE_SMOKE,pos.x,pos.y,pos.z,0.5,0.5,0.5,.01,1);
      }
      if(Math.random() < 0.05){
         spawnLongParticle(world,ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,pos.x,pos.y,pos.z,0.5,0.5,0.5,.01,1);
      }
   }
   
   public static void concussionArrowShot(ServerWorld world, Vec3d pos, double range, int calls){
      double radius = .5+calls*(range/5);
      sphere(world,null,pos,ParticleTypes.SQUID_INK,radius,(int)(radius*radius+radius*20+10),3,0.3,0.05,0);
      if(calls < 5){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> concussionArrowShot(world, pos, range,calls + 1)));
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
         float trueBrightness = (float) Math.min(1,-0.01*(new Vec3d(x,y,z).distanceTo(entity.getEyePos())-100)+0.25) * brightness;
         Color c = Color.getHSBColor(hue, 1f, trueBrightness);
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
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(2, () -> harnessFly(world, player,duration-1)));
      }
   }
   
   public static void harnessStall(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.SMOKE,pos.x,pos.y+0.5,pos.z,100,.4,.6,.4,0.05);
      world.spawnParticles(ParticleTypes.ANGRY_VILLAGER,pos.x,pos.y+0.5,pos.z,15,.4,.6,.4,1);
      world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,pos.x,pos.y+0.5,pos.z,15,.4,.6,.4,0.07);
   }
   
   public static void dowsingRodEmitter(ServerWorld world, Vec3d pos, int calls, int duration){
      if(world.getBlockState(BlockPos.ofFloored(pos)).getBlock() != Blocks.ANCIENT_DEBRIS) return;
      
      spawnLongParticle(world,ParticleTypes.FLAME,pos.x+0.5,pos.y+0.5,pos.z+0.5,.4,.4,.4,.05,3);
      
      if(calls < (duration)){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(3, () -> dowsingRodEmitter(world, pos, calls + 1, duration)));
      }
   }
   
   public static void dowsingRodArrow(ServerWorld world, Vec3d start, Vec3d end, int calls){
      line(world,null,start,end,ParticleTypes.FLAME,8,3,.08,0);
      if(calls < (16)){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(5, () -> dowsingRodArrow(world, start, end, calls + 1)));
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
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> shulkerCoreLevitate(world, player,duration-1)));
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
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> recallTeleport(world,pos,tick+1)));
      }
   }
   
   public static void stasisPearl(ServerWorld world, Vec3d pos){
      world.spawnParticles(ParticleTypes.REVERSE_PORTAL,pos.x,pos.y,pos.z,1,.2,.2,.2,0.01);
      world.spawnParticles(ParticleTypes.GLOW,pos.x,pos.y,pos.z,1,.15,.15,.15,0);
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
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(2, () -> dragonBossTowerCircleInvuln(world, center, period, calls + 1)));
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
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(2, () -> dragonBossTowerCirclePush(world, center, period, calls + 1)));
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
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(2, () -> dragonReclaimTowerCircle(world, center, period, calls + 1)));
      }
   }
   
   public static void dragonReclaimTowerShield(ServerWorld world, Vec3d center, int calls){
      int period = 15000;
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(9694975).toVector3f(),1.5f);
      float t = -(float)(Math.PI/(period/200)*calls + Math.PI);
      
      longDistSphere(world,center.add(0,2,0),dust,5.5,75,1,0,1,-t);
      
      if(calls < (period/200)){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(2, () -> dragonReclaimTowerShield(world, center, calls + 1)));
      }
   }
   
   public static void dragonBossWizardPulse(ServerWorld world, Vec3d center, int ticks){
      double radius = ticks/4.0;
      double theta = 2*Math.PI / 20.0;
      ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(16711892).toVector3f(),(float)radius/2);
      sphere(world,null,center,dust,radius,(int)(radius*radius+radius*10+radius),1,0,1,theta*ticks);
   }
   
   public static void lightningBolt(ServerWorld world, Vec3d p1, Vec3d p2, int numSegments, double maxDevDist, ParticleEffect type, int particlesPerBlock, int count, double delta, double speed, boolean longDist){
      if(numSegments <= 0) return;
      List<Vec3d> points = new ArrayList<>();
      points.add(p1);
      double dx = (p2.x-p1.x)/numSegments;
      double dy = (p2.y-p1.y)/numSegments;
      double dz = (p2.z-p1.z)/numSegments;
      for(int i = 0; i < numSegments-1; i++){
         double x = p1.x + dx*i;
         double y = p1.y + dy*i;
         double z = p1.z + dz*i;
         points.add(MiscUtils.randomSpherePoint(new Vec3d(x,y,z),maxDevDist));
      }
      points.add(p2);
      
      for(int i = 1; i < points.size(); i++){
         Vec3d ps = points.get(i-1);
         Vec3d pe = points.get(i);
         int intervals = (int) (pe.subtract(ps).length() * particlesPerBlock);
         
         if(longDist){
            line(world,null,ps,pe,type,intervals,count,delta,speed);
         }else{
            longDistLine(world,ps,pe,type,intervals,count,delta,speed);
         }
      }
   }
   
   public static void animatedLightningBolt(ServerWorld world, Vec3d p1, Vec3d p2, int numSegments, double maxDevDist, ParticleEffect type, int particlesPerBlock, int count, double delta, double speed, boolean longDist, int persistMod, int duration){
      if(numSegments <= 0) return;
      List<Vec3d> points = new ArrayList<>();
      points.add(p1);
      double dx = (p2.x-p1.x)/numSegments;
      double dy = (p2.y-p1.y)/numSegments;
      double dz = (p2.z-p1.z)/numSegments;
      for(int i = 0; i < numSegments-1; i++){
         double x = p1.x + dx*i;
         double y = p1.y + dy*i;
         double z = p1.z + dz*i;
         points.add(MiscUtils.randomSpherePoint(new Vec3d(x,y,z),maxDevDist));
      }
      points.add(p2);
      
      int particleCount = 0;
      for(int i = 1; i < points.size(); i++){
         Vec3d ps = points.get(i-1);
         Vec3d pe = points.get(i);
         int intervals = (int) (pe.subtract(ps).length() * particlesPerBlock);
         
         particleCount += intervals;
      }
      
      float particlesPerTick = (float) particleCount / duration;
      HashMap<Vec3d, Integer> pp = new HashMap<>();
      
      int c = 0;
      for(int i = 1; i < points.size(); i++){
         Vec3d ps = points.get(i-1);
         Vec3d pe = points.get(i);
         int intervals = (int) (pe.subtract(ps).length() * particlesPerBlock);
         
         dx = (pe.x-ps.x)/intervals;
         dy = (pe.y-ps.y)/intervals;
         dz = (pe.z-ps.z)/intervals;
         for(int j = 0; j < intervals; j++){
            double x = ps.x + dx * j;
            double y = ps.y + dy * j;
            double z = ps.z + dz * j;
            
            pp.put(new Vec3d(x,y,z),Math.round(c / particlesPerTick));
            c++;
         }
      }
      
      animatedLightningBoltHelper(world,pp,type,count,delta,speed,longDist,persistMod,0);
   }
   
   private static void animatedLightningBoltHelper(ServerWorld world, HashMap<Vec3d, Integer> points, ParticleEffect type, int count, double delta, double speed, boolean longDist, int persistMod, int tick){
      int highestTick = 0;
      for(Map.Entry<Vec3d, Integer> entry : points.entrySet()){
         int pTick = entry.getValue();
         Vec3d point = entry.getKey();
         if(pTick > highestTick) highestTick = pTick;
         
         if(!(persistMod > 0 && tick % persistMod == 0 && pTick < tick) && pTick != tick) continue;
         
         if(longDist){
            spawnLongParticle(world,type,point.x,point.y,point.z,delta,delta,delta,speed,count);
         }else{
            world.spawnParticles(type,point.x,point.y,point.z,count,delta,delta,delta,speed);
         }
      }
      
      if(tick < highestTick){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(1, () -> animatedLightningBoltHelper(world, points, type, count, delta, speed, longDist, persistMod, tick+1)));
      }
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
      line(world, player, p1, p2, type, intervals, count, delta, speed,1);
   }
   
   public static void line(ServerWorld world, @Nullable ServerPlayerEntity player, Vec3d p1, Vec3d p2, ParticleEffect type, int intervals, int count, double delta, double speed, double percent){
      percent = MathHelper.clamp(percent,0,1);
      double dx = (p2.x-p1.x)/intervals;
      double dy = (p2.y-p1.y)/intervals;
      double dz = (p2.z-p1.z)/intervals;
      for(int i = 0; i < intervals; i++){
         if((double)i/intervals > percent && percent != 1) continue;
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
      circle(world,player,center,type,radius,intervals,count,delta,speed,0);
   }
   
   public static void circle(ServerWorld world, @Nullable ServerPlayerEntity player, Vec3d center, ParticleEffect type, double radius, int intervals, int count, double delta, double speed, double theta){
      double dA = Math.PI * 2 / intervals;
      for(int i = 0; i < intervals; i++){
         double angle = dA * i + theta;
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
   
   public static List<Vec3d> getCirclePoints(Vec3d center, double radius, int intervals, double theta){
      List<Vec3d> points = new ArrayList<>();
      double dA = Math.PI * 2 / intervals;
      for(int i = 0; i < intervals; i++){
         double angle = dA * i + theta;
         double x = radius * Math.cos(angle) + center.x;
         double z = radius * Math.sin(angle) + center.z;
         double y = center.y;
         points.add(new Vec3d(x,y,z));
      }
      return points;
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
   
   public static int adjustTime(int tick, double speedMod){
      return (int) (((int)(tick / speedMod)) * speedMod);
   }
   
   public static List<Pair<Vec3d,Vec3d>> getIcosahedronPairs(List<Vec3d> icosPoints){
      List<Pair<Vec3d,Vec3d>> icosOutlines = new ArrayList<>();
      
      icosOutlines.add(new Pair<>(icosPoints.get(0),icosPoints.get(1)));
      icosOutlines.add(new Pair<>(icosPoints.get(0),icosPoints.get(4)));
      icosOutlines.add(new Pair<>(icosPoints.get(0),icosPoints.get(7)));
      icosOutlines.add(new Pair<>(icosPoints.get(0),icosPoints.get(8)));
      icosOutlines.add(new Pair<>(icosPoints.get(0),icosPoints.get(11)));
      icosOutlines.add(new Pair<>(icosPoints.get(9),icosPoints.get(2)));
      icosOutlines.add(new Pair<>(icosPoints.get(9),icosPoints.get(3)));
      icosOutlines.add(new Pair<>(icosPoints.get(9),icosPoints.get(4)));
      icosOutlines.add(new Pair<>(icosPoints.get(9),icosPoints.get(5)));
      icosOutlines.add(new Pair<>(icosPoints.get(9),icosPoints.get(8)));
      icosOutlines.add(new Pair<>(icosPoints.get(10),icosPoints.get(7)));
      icosOutlines.add(new Pair<>(icosPoints.get(10),icosPoints.get(3)));
      icosOutlines.add(new Pair<>(icosPoints.get(10),icosPoints.get(6)));
      icosOutlines.add(new Pair<>(icosPoints.get(10),icosPoints.get(11)));
      icosOutlines.add(new Pair<>(icosPoints.get(10),icosPoints.get(2)));
      icosOutlines.add(new Pair<>(icosPoints.get(4),icosPoints.get(8)));
      icosOutlines.add(new Pair<>(icosPoints.get(4),icosPoints.get(7)));
      icosOutlines.add(new Pair<>(icosPoints.get(4),icosPoints.get(3)));
      icosOutlines.add(new Pair<>(icosPoints.get(7),icosPoints.get(3)));
      icosOutlines.add(new Pair<>(icosPoints.get(3),icosPoints.get(2)));
      icosOutlines.add(new Pair<>(icosPoints.get(6),icosPoints.get(2)));
      icosOutlines.add(new Pair<>(icosPoints.get(6),icosPoints.get(1)));
      icosOutlines.add(new Pair<>(icosPoints.get(6),icosPoints.get(11)));
      icosOutlines.add(new Pair<>(icosPoints.get(6),icosPoints.get(5)));
      icosOutlines.add(new Pair<>(icosPoints.get(7),icosPoints.get(11)));
      icosOutlines.add(new Pair<>(icosPoints.get(2),icosPoints.get(5)));
      icosOutlines.add(new Pair<>(icosPoints.get(1),icosPoints.get(5)));
      icosOutlines.add(new Pair<>(icosPoints.get(8),icosPoints.get(5)));
      icosOutlines.add(new Pair<>(icosPoints.get(1),icosPoints.get(11)));
      icosOutlines.add(new Pair<>(icosPoints.get(1),icosPoints.get(8)));
      return icosOutlines;
   }
   
   public static List<Vec3d> getIcosahedronPoints(){
      List<Vec3d> icosPoints = new ArrayList<>();
      icosPoints.add(new Vec3d(0,1,PHI));
      icosPoints.add(new Vec3d(0,-1,PHI));
      icosPoints.add(new Vec3d(0,-1,-PHI));
      icosPoints.add(new Vec3d(0,1,-PHI));
      icosPoints.add(new Vec3d(-1,PHI,0));
      icosPoints.add(new Vec3d(-1,-PHI,0));
      icosPoints.add(new Vec3d(1,-PHI,0));
      icosPoints.add(new Vec3d(1,PHI,0));
      icosPoints.add(new Vec3d(-PHI,0,1));
      icosPoints.add(new Vec3d(-PHI,0,-1));
      icosPoints.add(new Vec3d(PHI,0,-1));
      icosPoints.add(new Vec3d(PHI,0,1));
      
      return icosPoints;
   }
}
