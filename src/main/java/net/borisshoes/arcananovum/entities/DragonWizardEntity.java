package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.UUID;

public class DragonWizardEntity extends IllusionerEntity implements PolymerEntity {
   private final int laserCD = 600; // 30 Seconds
   private final int summonCD = 1200; // 60 Seconds
   private final int pulseCD = 900; // 45 Seconds
   public int laserTick = laserCD;
   public int summonTick = summonCD;
   public int pulseTick = pulseCD;
   private UUID crystalId;
   
   private int numPlayers;
   private SkeletonEntity[] skeletons;
   
   // Wizard is 3 blocks above ground, 2 above crystal pos
   public DragonWizardEntity(EntityType<? extends DragonWizardEntity> entityType, World world){
      super(entityType, world);
      this.numPlayers = 5;
   }
   
   public static DefaultAttributeContainer.Builder createWizardAttributes(){
      return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GRAVITY,0.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.0)
            .add(EntityAttributes.FOLLOW_RANGE, 64.0)
            .add(EntityAttributes.MAX_HEALTH, 64.0)
            .add(EntityAttributes.ARMOR, 10)
            .add(EntityAttributes.ARMOR_TOUGHNESS, 10)
            .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0);
   }
   
   @Override
   public IllagerEntity.State getState(){
      boolean castingLaser = laserTick < 100; // 5 Second channel
      boolean castingSummon = summonTick < 60; // 3 Second channel
      boolean castingPulse = pulseTick < 20; // 1 Second channel
      
      if(castingLaser || castingSummon || castingPulse){
         return IllagerEntity.State.SPELLCASTING;
      }
      return IllagerEntity.State.CROSSED;
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.ILLUSIONER;
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
   
   public void setNumPlayers(int numPlayers){
      this.numPlayers = numPlayers;
   }
   
   public void setCrystalId(UUID crystalId){
      this.crystalId = crystalId;
   }
   
   public UUID getCrystalId(){
      return this.crystalId;
   }
   
   @Override
   protected float modifyAppliedDamage(DamageSource source, float amount){
      float scale = numPlayers > 0 ? 1.25f/numPlayers : 1;
      scale = Math.max(scale,0.1f);
      if(source.getAttacker() instanceof EnderDragonEntity) amount = 0;
      if(source.isIn(DamageTypeTags.BYPASSES_ARMOR)) amount *= 0.25f; // Reduce damage from magic sources and immune to the dragon
      if(amount > getMaxHealth() / 0.1) amount = getMaxHealth() / 0.1f;
      amount *= scale;
      return amount;
   }
   
   @Override
   public void tick(){
      super.tick();
      try{
         setAbleToJoinRaid(false);
         setNoGravity(true);
         setPersistent();
         setAiDisabled(true);
         
         if(getServer() != null && getServer().getTicks() % 4 == 0 && getWorld() instanceof ServerWorld entityWorld){
            entityWorld.spawnParticles(ParticleTypes.CLOUD,getX(),getY(),getZ(),5,0.25,0.25,0.25,0);
            PlayerEntity nearestPlayer = entityWorld.getClosestPlayer(this,25);
            if(nearestPlayer != null)
               lookAt(EntityAnchorArgumentType.EntityAnchor.EYES,nearestPlayer.getEyePos());
         }
         
         
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
            PlayerEntity player = getWorld().getClosestPlayer(this,25);
            if(player != null){
               double dist = player.getPos().distanceTo(this.getPos());
               if(dist <= 15 && summonTick == summonCD){
                  List<SkeletonEntity> skeles = getWorld().getEntitiesByType(EntityType.SKELETON, new Box(getX()-15,40,getZ()-15, getX()+15,160,getZ()+15), e -> true);
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
      if(getWorld() instanceof ServerWorld endWorld){
         PlayerEntity player = getWorld().getClosestPlayer(this,25);
         if(player != null){
            double dist = player.getPos().distanceTo(this.getPos());
            if(laserTick % 10 == 0){ // Damage every half second
               player.damage(endWorld, new DamageSource(endWorld.getDamageSources().magic().getTypeRegistryEntry(), this,this),1.25f);
            }
            if(laserTick % 2 == 0){ // Particles every other tick
               ParticleEffectUtils.line(endWorld,null,this.getPos(),player.getPos(), ParticleTypes.WITCH,(int)(dist*1.75),1,0.2,0);
            }
         }
      }
   }
   
   private void castSummon(){
      if(getWorld() instanceof ServerWorld endWorld){
         if(summonTick == 1){
            skeletons = new SkeletonEntity[4];
            for(int i = 0; i < skeletons.length; i++){
               skeletons[i] = makeSkeleton(endWorld,numPlayers);
               skeletons[i].setPos(getX()+.5+(Math.random()*2-1),getY()-5,getZ()+.5+(Math.random()*2-1));
               endWorld.spawnNewEntityAndPassengers(skeletons[i]);
            }
         }else if(summonTick == 59){
            for(int i = 0; i < skeletons.length; i++){
               if(skeletons[i] == null) continue;
               skeletons[i].setInvulnerable(false);
               skeletons[i].setAiDisabled(false);
            }
         }else if(summonTick > 1){
            for(int i = 0; i < skeletons.length; i++){
               if(skeletons[i] == null) continue;;
               Vec3d pos = skeletons[i].getPos();
               skeletons[i].setPos(pos.getX(),pos.getY()+(1/20.0),pos.getZ());
               endWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OBSIDIAN.getDefaultState()), pos.getX(), getY()-3, pos.getZ(), 5, .8, 0.5, .8, .5);
            }
         }
      }
   }
   
   private void castPulse(){
      if(getWorld() instanceof ServerWorld endWorld){
         ParticleEffectUtils.dragonBossWizardPulse(endWorld,getPos().add(0,-2.5,0),pulseTick);
         if(pulseTick == 10){ // Actual pulse halfway thru animation
            List<ServerPlayerEntity> inRangePlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(new Vec3d(getX()+.5,getY()-2,getZ()+.5)) <= 5.5*5.5);
            for(ServerPlayerEntity player : inRangePlayers){
               BlockPos target = BlockPos.ofFloored(getX()+.5,getY()-2,getZ()+.5);
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
   
   private SkeletonEntity makeSkeleton(ServerWorld endWorld, int numPlayers){
      SkeletonEntity skeleton = new SkeletonEntity(EntityType.SKELETON, endWorld);
      float skeletonHP = MathHelper.clamp(20+numPlayers * 2,20,80);
      skeleton.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(skeletonHP);
      skeleton.setHealth(skeletonHP);
      skeleton.setPersistent();
      ItemStack bow = new ItemStack(Items.BOW);
      bow.addEnchantment(MiscUtils.getEnchantment(Enchantments.PUNCH),2);
      bow.addEnchantment(MiscUtils.getEnchantment(Enchantments.POWER),1);
      ItemStack helm = new ItemStack(Items.IRON_HELMET);
      ItemStack chest = new ItemStack(Items.IRON_CHESTPLATE);
      ItemStack legs = new ItemStack(Items.IRON_LEGGINGS);
      ItemStack boots = new ItemStack(Items.IRON_BOOTS);
      helm.addEnchantment(MiscUtils.getEnchantment(Enchantments.PROTECTION),1);
      chest.addEnchantment(MiscUtils.getEnchantment(Enchantments.PROTECTION),1);
      legs.addEnchantment(MiscUtils.getEnchantment(Enchantments.PROTECTION),1);
      boots.addEnchantment(MiscUtils.getEnchantment(Enchantments.PROTECTION),1);
      boots.addEnchantment(MiscUtils.getEnchantment(Enchantments.FEATHER_FALLING),4);
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
      StatusEffectInstance slowFall = new StatusEffectInstance(StatusEffects.SLOW_FALLING,100000,0,false,false,false);
      skeleton.addStatusEffect(fireRes);
      skeleton.addStatusEffect(slowFall);
      skeleton.getAttributeInstance(EntityAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
      skeleton.setInvulnerable(true);
      skeleton.setAiDisabled(true);
      return skeleton;
   }
   
   @Override
   protected void writeCustomData(WriteView view){
      super.writeCustomData(view);
      view.putInt("laserTick",laserTick);
      view.putInt("summonTick",summonTick);
      view.putInt("pulseTick",pulseTick);
      view.putInt("numPlayers",numPlayers);
      if(crystalId != null) view.putString("crystalId",crystalId.toString());
      
      NbtList skeletonTag = new NbtList();
      if(skeletons != null){
         for(SkeletonEntity skeleton : skeletons){
            if(skeleton != null){
               skeletonTag.add(NbtString.of(skeleton.getUuidAsString()));
            }
         }
      }
      nbt.put("skeletons",skeletonTag);
   }
   
   @Override
   protected void readCustomData(ReadView view){
      super.readCustomData(view);
      laserTick = view.getInt("laserTick", 0);
      summonTick = view.getInt("summonTick", 0);
      pulseTick = view.getInt("pulseTick", 0);
      numPlayers = view.getInt("numPlayers", 0);
      if(nbt.contains("crystalId")) crystalId = MiscUtils.getUUID(nbt.getString("crystalId", ""));
      
      if(getWorld() instanceof ServerWorld serverWorld){
         NbtList skeleList = nbt.getListOrEmpty("skeletons");
         skeletons = new SkeletonEntity[skeleList.size()];
         for(int i = 0; i < skeletons.length; i++){
            if(serverWorld.getEntity(MiscUtils.getUUID(skeleList.getString(i, ""))) instanceof SkeletonEntity skele){
               skeletons[i] = skele;
            }
         }
      }
      
      
   }
}

