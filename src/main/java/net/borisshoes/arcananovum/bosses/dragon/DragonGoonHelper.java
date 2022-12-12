package net.borisshoes.arcananovum.bosses.dragon;

import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

import static net.borisshoes.arcananovum.Arcananovum.log;

public class DragonGoonHelper {
   
   
   public static PhantomEntity makeGuardianPhantom(ServerWorld endWorld, int numPlayers){
      PhantomEntity guardian = new PhantomEntity(EntityType.PHANTOM, endWorld);
      MutableText phantomName = Text.literal("")
            .append(Text.literal("-").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("=").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("-").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" "))
            .append(Text.literal("Guardian Phantom").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD, Formatting.UNDERLINE))
            .append(Text.literal(" "))
            .append(Text.literal("-").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("=").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("-").formatted(Formatting.LIGHT_PURPLE));
      guardian.setCustomName(phantomName);
      guardian.setCustomNameVisible(true);
      guardian.setPhantomSize(20);
      guardian.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(Math.min(1000,numPlayers * 100 + 50));
      guardian.setHealth(Math.min(1000,numPlayers * 100 + 50));
      guardian.setPersistent();
      guardian.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(20f);
      guardian.setPos(Math.random()*50-25,100,Math.random()*50-25);
      
      return guardian;
   }
   
   public static WizardEntity makeWizard(ServerWorld endWorld, int numPlayers){
      WizardEntity wizard = new WizardEntity(EntityType.ILLUSIONER,endWorld,numPlayers);
      MutableText wizardName = Text.literal("")
            .append(Text.literal("~").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" "))
            .append(Text.literal("Crystal Defender").formatted(Formatting.AQUA, Formatting.BOLD, Formatting.UNDERLINE))
            .append(Text.literal(" "))
            .append(Text.literal("~").formatted(Formatting.DARK_AQUA));
      wizard.setCustomName(wizardName);
      wizard.setAbleToJoinRaid(false);
      wizard.setNoGravity(true);
      wizard.setPersistent();
      wizard.setAiDisabled(true);
      wizard.setHealth(Math.min(500,numPlayers * 50 + 25));
      wizard.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(Math.min(500,numPlayers * 50 + 25));
      
      return wizard;
   }
   
   private static SkeletonEntity makeSkeletons(ServerWorld endWorld, int numPlayers){
      SkeletonEntity skeleton = new SkeletonEntity(EntityType.SKELETON, endWorld);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(Math.min(40,15+numPlayers * 2));
      skeleton.setHealth(Math.min(40,15+numPlayers * 2));
      skeleton.setPersistent();
      ItemStack bow = new ItemStack(Items.BOW);
      bow.addEnchantment(Enchantments.PUNCH,2);
      bow.addEnchantment(Enchantments.POWER,1);
      ItemStack helm = new ItemStack(Items.IRON_HELMET);
      ItemStack chest = new ItemStack(Items.IRON_CHESTPLATE);
      ItemStack legs = new ItemStack(Items.IRON_LEGGINGS);
      ItemStack boots = new ItemStack(Items.IRON_BOOTS);
      helm.addEnchantment(Enchantments.PROTECTION,1);
      chest.addEnchantment(Enchantments.PROTECTION,1);
      legs.addEnchantment(Enchantments.PROTECTION,1);
      boots.addEnchantment(Enchantments.PROTECTION,1);
      boots.addEnchantment(Enchantments.FEATHER_FALLING,4);
      skeleton.equipStack(EquipmentSlot.MAINHAND, bow);
      skeleton.equipStack(EquipmentSlot.HEAD, helm);
      skeleton.equipStack(EquipmentSlot.CHEST, chest);
      skeleton.equipStack(EquipmentSlot.LEGS, legs);
      skeleton.equipStack(EquipmentSlot.FEET, boots);
      skeleton.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0);
      skeleton.setEquipmentDropChance(EquipmentSlot.HEAD, 0);
      skeleton.setEquipmentDropChance(EquipmentSlot.CHEST, 0);
      skeleton.setEquipmentDropChance(EquipmentSlot.LEGS, 0);
      skeleton.setEquipmentDropChance(EquipmentSlot.FEET, 0);
      StatusEffectInstance fireRes = new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE,100000,0,false,false,false);
      fireRes.setPermanent(true);
      StatusEffectInstance slowFall = new StatusEffectInstance(StatusEffects.SLOW_FALLING,100000,0,false,false,false);
      slowFall.setPermanent(true);
      skeleton.addStatusEffect(fireRes);
      skeleton.addStatusEffect(slowFall);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
      skeleton.setInvulnerable(true);
      skeleton.setAiDisabled(true);
      return skeleton;
   }
   
   public static class WizardEntity extends IllusionerEntity{
      private final int laserCD = 600; // 30 Seconds
      private final int summonCD = 1200; // 60 Seconds
      private final int pulseCD = 900; // 45 Seconds
      public int laserTick = laserCD;
      public int summonTick = summonCD;
      public int pulseTick = pulseCD;
      
      private final int numPlayers;
      private SkeletonEntity[] skeletons;
      
      // Wizard is 3 blocks above ground, 2 above crystal pos
      public WizardEntity(EntityType<? extends IllusionerEntity> entityType, World world, int numPlayers){
         super(entityType, world);
         this.numPlayers = numPlayers;
      }
   
      @Override
      public void onDeath(DamageSource damageSource){
         super.onDeath(damageSource);
         try{
            if(skeletons != null){
               for(SkeletonEntity skeleton : skeletons){
                  if(skeleton != null){
                     skeleton.setInvulnerable(false);
                     skeleton.setAiDisabled(false);
                  }
               }
            }
         }catch(Exception e){
            e.printStackTrace();
         }
      }
   
      public void tick(){
         super.tick();
         try{
            boolean castingLaser = laserTick < 100; // 5 Second channel
            boolean castingSummon = summonTick < 60; // 3 Second channel
            boolean castingPulse = pulseTick < 20; // 1 Second channel
   
            if(castingLaser) castLaser();
            if(castingSummon) castSummon();
            if(castingPulse) castPulse();
   
            if(!castingLaser && !castingSummon && !castingPulse){ // Determine if can cast spell
               // Summon conditions: player within 15 blocks
               // Pulse conditions: player within 7 blocks
               // Laser conditions: player within 25 blocks
               PlayerEntity player = world.getClosestPlayer(this,25);
               if(player != null){
                  double dist = player.getPos().distanceTo(this.getPos());
                  if(dist <= 15 && summonTick == summonCD){
                     List<SkeletonEntity> skeles = world.getEntitiesByType(EntityType.SKELETON, new Box(new BlockPos(getX()-15,40,getZ()-15), new BlockPos(getX()+15,160,getZ()+15)), e -> true);
                     if(skeles.size() < 8)
                        summonTick = 0;
                  }else if(dist <= 7 && pulseTick == pulseCD){
                     pulseTick = 0;
                  }else if(dist < 20 && laserTick == laserCD){
                     laserTick = 0;
                  }
               }
            }
   
            if(laserTick < laserCD) laserTick++;
            if(summonTick < summonCD) summonTick++;
            if(pulseTick < pulseCD) pulseTick++;
         }catch(Exception e){
            e.printStackTrace();
         }
      }
      
      private void castLaser(){
         if(world instanceof ServerWorld endWorld){
            PlayerEntity player = world.getClosestPlayer(this,25);
            if(player != null){
               double dist = player.getPos().distanceTo(this.getPos());
               if(laserTick % 10 == 0){ // Damage every half second
                  player.damage(DamageSource.MAGIC,1.25f);
               }
               if(laserTick % 2 == 0){ // Particles every other tick
                  ParticleEffectUtils.line(endWorld,null,this.getPos(),player.getPos(),ParticleTypes.WITCH,(int)(dist*1.75),1,0.2,0);
               }
            }
         }
      }
      
      private void castSummon(){
         if(world instanceof ServerWorld endWorld){
            if(summonTick == 1){
               skeletons = new SkeletonEntity[4];
               for(int i = 0; i < skeletons.length; i++){
                  skeletons[i] = makeSkeletons(endWorld,numPlayers);
                  skeletons[i].setPos(getX()+.5+(Math.random()*2-1),getY()-5,getZ()+.5+(Math.random()*2-1));
                  endWorld.spawnNewEntityAndPassengers(skeletons[i]);
               }
            }else if(summonTick == 59){
               for(int i = 0; i < skeletons.length; i++){
                  skeletons[i].setInvulnerable(false);
                  skeletons[i].setAiDisabled(false);
               }
            }else if(summonTick > 1){
               for(int i = 0; i < skeletons.length; i++){
                  Vec3d pos = skeletons[i].getPos();
                  skeletons[i].setPos(pos.getX(),pos.getY()+(1/20.0),pos.getZ());
                  endWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OBSIDIAN.getDefaultState()), pos.getX(), getY()-3, pos.getZ(), 5, .8, 0.5, .8, .5);
               }
            }
         }
      }
      
      private void castPulse(){
         if(world instanceof ServerWorld endWorld){
            ParticleEffectUtils.dragonBossWizardPulse(endWorld,getPos().add(0,-2.5,0),pulseTick);
            if(pulseTick == 10){ // Actual pulse halfway thru animation
               List<ServerPlayerEntity> inRangePlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(new Vec3d(getX()+.5,getY()-2,getZ()+.5)) <= 5.5*5.5);
               for(ServerPlayerEntity player : inRangePlayers){
                  BlockPos target = new BlockPos(getX()+.5,getY()-2,getZ()+.5);
                  BlockPos playerPos = player.getBlockPos();
                  Vec3d vec = new Vec3d(target.getX()-playerPos.getX(),0,target.getZ()-playerPos.getZ());
                  vec = vec.normalize().multiply(3);
      
                  player.setVelocity(-vec.x,1,-vec.z);
                  player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
      
                  player.sendMessage(Text.literal("The Crystal Pulses Violently!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC),true);
               }
            }
         }
      }
   }
}
