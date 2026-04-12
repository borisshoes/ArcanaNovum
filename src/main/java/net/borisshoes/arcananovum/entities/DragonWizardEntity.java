package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.CodecUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DragonWizardEntity extends Illusioner implements PolymerEntity {
   private final int laserCD = 600; // 30 Seconds
   private final int summonCD = 1200; // 60 Seconds
   private final int pulseCD = 900; // 45 Seconds
   public int laserTick = laserCD;
   public int summonTick = summonCD;
   public int pulseTick = pulseCD;
   private UUID crystalId;
   
   private int numPlayers;
   private Skeleton[] skeletons;
   
   // Wizard is 3 blocks above ground, 2 above crystal pos
   public DragonWizardEntity(EntityType<? extends DragonWizardEntity> entityType, Level world){
      super(entityType, world);
      this.numPlayers = 5;
   }
   
   public static AttributeSupplier.Builder createWizardAttributes(){
      return Monster.createMonsterAttributes()
            .add(Attributes.GRAVITY, 0.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.FOLLOW_RANGE, 64.0)
            .add(Attributes.MAX_HEALTH, 64.0)
            .add(Attributes.ARMOR, 10)
            .add(Attributes.ARMOR_TOUGHNESS, 10)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
   }
   
   @Override
   public AbstractIllager.IllagerArmPose getArmPose(){
      boolean castingLaser = laserTick < 100; // 5 Second channel
      boolean castingSummon = summonTick < 60; // 3 Second channel
      boolean castingPulse = pulseTick < 20; // 1 Second channel
      
      if(castingLaser || castingSummon || castingPulse){
         return AbstractIllager.IllagerArmPose.SPELLCASTING;
      }
      return AbstractIllager.IllagerArmPose.CROSSED;
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.ILLUSIONER;
   }
   
   @Override
   public void die(DamageSource damageSource){
      super.die(damageSource);
      try{
         if(skeletons != null){
            for(Skeleton skeleton : skeletons){
               if(skeleton != null){
                  skeleton.setInvulnerable(false);
                  skeleton.setNoAi(false);
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
   protected float getDamageAfterMagicAbsorb(DamageSource source, float amount){
      float scale = numPlayers > 0 ? 1.25f / numPlayers : 1;
      scale = Math.max(scale, 0.1f);
      if(source.getEntity() instanceof EnderDragon) amount = 0;
      if(source.is(DamageTypeTags.BYPASSES_ARMOR))
         amount *= 0.25f; // Reduce damage from magic sources and immune to the dragon
      if(amount > getMaxHealth() / 0.1) amount = getMaxHealth() / 0.1f;
      amount *= scale;
      return amount;
   }
   
   @Override
   public void tick(){
      super.tick();
      try{
         setCanJoinRaid(false);
         setNoGravity(true);
         setPersistenceRequired();
         setNoAi(true);
         
         if(level().getServer() != null && level().getServer().getTickCount() % 4 == 0 && level() instanceof ServerLevel entityWorld){
            entityWorld.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 5, 0.25, 0.25, 0.25, 0);
            Player nearestPlayer = entityWorld.getNearestPlayer(this, 25);
            if(nearestPlayer != null)
               lookAt(EntityAnchorArgument.Anchor.EYES, nearestPlayer.getEyePosition());
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
            Player player = level().getNearestPlayer(this, 25);
            if(player != null){
               double dist = player.position().distanceTo(this.position());
               if(dist <= 15 && summonTick == summonCD){
                  List<Skeleton> skeles = level().getEntities(EntityType.SKELETON, new AABB(getX() - 15, 40, getZ() - 15, getX() + 15, 160, getZ() + 15), e -> true);
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
      if(level() instanceof ServerLevel endWorld){
         Player player = level().getNearestPlayer(this, 25);
         if(player != null){
            double dist = player.position().distanceTo(this.position());
            if(laserTick % 10 == 0){ // Damage every half second
               player.hurtServer(endWorld, new DamageSource(endWorld.damageSources().magic().typeHolder(), this, this), 1.25f);
            }
            if(laserTick % 3 == 0){ // Particles every third tick
               ArcanaEffectUtils.line(endWorld, null, this.position(), player.position(), ParticleTypes.WITCH, (int) (dist * 1.75), 1, 0.2, 0);
            }
         }
      }
   }
   
   private void castSummon(){
      if(level() instanceof ServerLevel endWorld){
         if(summonTick == 1){
            skeletons = new Skeleton[4];
            for(int i = 0; i < skeletons.length; i++){
               skeletons[i] = makeSkeleton(endWorld, numPlayers);
               skeletons[i].setPosRaw(getX() + .5 + (endWorld.getRandom().nextDouble() * 2 - 1), getY() - 5, getZ() + .5 + (endWorld.getRandom().nextDouble() * 2 - 1));
               endWorld.tryAddFreshEntityWithPassengers(skeletons[i]);
            }
         }else if(summonTick == 59){
            for(int i = 0; i < skeletons.length; i++){
               if(skeletons[i] == null) continue;
               skeletons[i].setInvulnerable(false);
               skeletons[i].setNoAi(false);
            }
         }else if(summonTick > 1){
            for(int i = 0; i < skeletons.length; i++){
               if(skeletons[i] == null) continue;
               ;
               Vec3 pos = skeletons[i].position();
               skeletons[i].setPosRaw(pos.x(), pos.y() + (1 / 20.0), pos.z());
               endWorld.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OBSIDIAN.defaultBlockState()), pos.x(), getY() - 3, pos.z(), 5, .8, 0.5, .8, .5);
            }
         }
      }
   }
   
   private void castPulse(){
      if(level() instanceof ServerLevel endWorld){
         ArcanaEffectUtils.dragonBossWizardPulse(endWorld, position().add(0, -2.5, 0), pulseTick);
         if(pulseTick == 10){ // Actual pulse halfway thru animation
            List<ServerPlayer> inRangePlayers = endWorld.getPlayers(p -> p.distanceToSqr(new Vec3(getX() + .5, getY() - 2, getZ() + .5)) <= 5.5 * 5.5);
            for(ServerPlayer player : inRangePlayers){
               BlockPos target = BlockPos.containing(getX() + .5, getY() - 2, getZ() + .5);
               BlockPos playerPos = player.blockPosition();
               Vec3 vec = new Vec3(target.getX() - playerPos.getX(), 0, target.getZ() - playerPos.getZ());
               vec = vec.normalize().scale(3);
               
               player.setDeltaMovement(-vec.x, 1, -vec.z);
               player.connection.send(new ClientboundSetEntityMotionPacket(player));
               
               player.sendSystemMessage(Component.literal("The Crystal Pulses Violently!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC), true);
            }
         }
      }
   }
   
   private Skeleton makeSkeleton(ServerLevel endWorld, int numPlayers){
      Skeleton skeleton = new Skeleton(EntityType.SKELETON, endWorld);
      float skeletonHP = Mth.clamp(20 + numPlayers * 2, 20, 80);
      skeleton.getAttribute(Attributes.MAX_HEALTH).setBaseValue(skeletonHP);
      skeleton.setHealth(skeletonHP);
      skeleton.setPersistenceRequired();
      ItemStack bow = new ItemStack(Items.BOW);
      bow.enchant(MinecraftUtils.getEnchantment(Enchantments.PUNCH), 2);
      bow.enchant(MinecraftUtils.getEnchantment(Enchantments.POWER), 1);
      ItemStack helm = new ItemStack(Items.IRON_HELMET);
      ItemStack chest = new ItemStack(Items.IRON_CHESTPLATE);
      ItemStack legs = new ItemStack(Items.IRON_LEGGINGS);
      ItemStack boots = new ItemStack(Items.IRON_BOOTS);
      helm.enchant(MinecraftUtils.getEnchantment(Enchantments.PROTECTION), 1);
      chest.enchant(MinecraftUtils.getEnchantment(Enchantments.PROTECTION), 1);
      legs.enchant(MinecraftUtils.getEnchantment(Enchantments.PROTECTION), 1);
      boots.enchant(MinecraftUtils.getEnchantment(Enchantments.PROTECTION), 1);
      boots.enchant(MinecraftUtils.getEnchantment(Enchantments.FEATHER_FALLING), 4);
      skeleton.setItemSlot(EquipmentSlot.MAINHAND, bow);
      skeleton.setItemSlot(EquipmentSlot.HEAD, helm);
      skeleton.setItemSlot(EquipmentSlot.CHEST, chest);
      skeleton.setItemSlot(EquipmentSlot.LEGS, legs);
      skeleton.setItemSlot(EquipmentSlot.FEET, boots);
      skeleton.setDropChance(EquipmentSlot.MAINHAND, 0);
      skeleton.setDropChance(EquipmentSlot.HEAD, 0);
      skeleton.setDropChance(EquipmentSlot.CHEST, 0);
      skeleton.setDropChance(EquipmentSlot.LEGS, 0);
      skeleton.setDropChance(EquipmentSlot.FEET, 0);
      MobEffectInstance fireRes = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100000, 0, false, false, false);
      MobEffectInstance slowFall = new MobEffectInstance(MobEffects.SLOW_FALLING, 100000, 0, false, false, false);
      skeleton.addEffect(fireRes);
      skeleton.addEffect(slowFall);
      skeleton.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
      skeleton.setInvulnerable(true);
      skeleton.setNoAi(true);
      return skeleton;
   }
   
   @Override
   protected void addAdditionalSaveData(ValueOutput view){
      super.addAdditionalSaveData(view);
      view.putInt("laserTick", laserTick);
      view.putInt("summonTick", summonTick);
      view.putInt("pulseTick", pulseTick);
      view.putInt("numPlayers", numPlayers);
      if(crystalId != null) view.putString("crystalId", crystalId.toString());
      if(skeletons != null)
         view.storeNullable("skeletons", CodecUtils.STRING_LIST, Arrays.stream(skeletons).map(Entity::getStringUUID).toList());
   }
   
   @Override
   protected void readAdditionalSaveData(ValueInput view){
      super.readAdditionalSaveData(view);
      laserTick = view.getIntOr("laserTick", 0);
      summonTick = view.getIntOr("summonTick", 0);
      pulseTick = view.getIntOr("pulseTick", 0);
      numPlayers = view.getIntOr("numPlayers", 0);
      crystalId = AlgoUtils.getUUID(view.getStringOr("crystalId", ""));
      
      if(level() instanceof ServerLevel serverWorld){
         Optional<List<String>> optional = view.read("skeletons", CodecUtils.STRING_LIST);
         if(optional.isEmpty()){
            skeletons = new Skeleton[]{};
         }else{
            List<String> ids = optional.get();
            skeletons = new Skeleton[ids.size()];
            for(int i = 0; i < skeletons.length; i++){
               if(serverWorld.getEntity(AlgoUtils.getUUID(ids.get(i))) instanceof Skeleton skele){
                  skeletons[i] = skele;
               }
            }
         }
      }
   }
}

