package net.borisshoes.arcananovum.areaeffects;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SmokeArrowAreaEffectTracker extends AreaEffectTracker {
   
   private final List<SmokeArrowSource> sources;
   
   public SmokeArrowAreaEffectTracker(){
      super("smoke_arrow");
      sources = new ArrayList<>();
   }
   
   @Override
   public void onTick(MinecraftServer server){
      if(sources.isEmpty()) return;
      
      for(ServerWorld world : server.getWorlds()){
         ArrayList<BlockPos> affectedBlocks = new ArrayList<>();
         for(SmokeArrowSource source : sources){
            affectedBlocks.addAll(source.getAffectedBlocks(world).stream().filter(blockPos -> affectedBlocks.stream().noneMatch(block -> block.equals(blockPos))).toList());
            source.affectEntities(world);
         }
         
         for(BlockPos block : affectedBlocks){
            ParticleEffectUtils.smokeArrowEmit(world,block.toCenterPos());
         }
      }
      
      sources.removeIf(SmokeArrowSource::age);
   }
   
   @Override
   public void addSource(AreaEffectSource source){
      if(source instanceof SmokeArrowSource smokeSource) sources.add(smokeSource);
   }
   
   public static SmokeArrowSource source(@Nullable Entity contributor, @Nullable Entity sourceEntity, @Nullable BlockPos sourceBlock, @Nullable ServerWorld blockWorld, double range, int gasLvl){
      return new SmokeArrowSource(sourceEntity,sourceBlock,blockWorld,range,gasLvl,contributor);
   }
   
   public static class SmokeArrowSource extends AreaEffectSource{
      private final Entity sourceEntity;
      private final BlockPos sourceBlock;
      private final ServerWorld blockWorld;
      private final double range;
      private final int gasLvl;
      private final boolean fromEntity;
      private int age;
      private final int duration;
      private final Entity contributor;
      
      private SmokeArrowSource(@Nullable Entity sourceEntity, @Nullable BlockPos sourceBlock, @Nullable ServerWorld blockWorld, double range, int gasLvl, @Nullable Entity contributor){
         this.sourceEntity = sourceEntity;
         this.sourceBlock = sourceBlock;
         this.blockWorld = blockWorld;
         this.range = range;
         this.gasLvl = gasLvl;
         this.fromEntity = sourceEntity != null;
         this.contributor = contributor;
         this.age = 0;
         this.duration = 100;
      }
      
      public World getSourceWorld(){
         if(fromEntity){
            return sourceEntity.getWorld();
         }else{
            return blockWorld;
         }
      }
      
      public BlockPos getBlockPos(){
         if(fromEntity){
            return sourceEntity.getBlockPos();
         }else{
            return sourceBlock;
         }
      }
      
      public boolean age(){
         this.age++;
         return age >= duration;
      }
      
      public int getGasLvl(){
         return gasLvl;
      }
      
      public Entity getContributor(){
         return contributor;
      }
      
      public void affectEntities(ServerWorld world){
         if(age % 5 != 0) return;
         
         int mobCount = 0;
         boolean withOwner = false;
         for(Entity affectedEntity : getAffectedEntities(world)){
            if(affectedEntity instanceof LivingEntity e){
               int amp = e instanceof MobEntity ? 5 : 0;
               StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, 60*(gasLvl+1), 7, false, false, true);
               StatusEffectInstance weakness = new StatusEffectInstance(StatusEffects.WEAKNESS, 60*(gasLvl+1), amp+gasLvl, false, false, true);
               StatusEffectInstance invis = new StatusEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, 60*(gasLvl+1), 0, false, false, true);
               e.addStatusEffect(blind);
               e.addStatusEffect(weakness);
               if(e instanceof ServerPlayerEntity){
                  e.addStatusEffect(invis);
               }
               
               if(e instanceof HostileEntity mob){
                  mob.setAttacking(false);
                  mob.setAttacker(null);
                  mobCount++;
               }
               if(contributor instanceof ServerPlayerEntity player && player.getUuid().equals(e.getUuid())) withOwner = true;
            }
         }
         if(contributor instanceof ServerPlayerEntity player && withOwner && mobCount >= 3) ArcanaAchievements.grant(player,ArcanaAchievements.SMOKE_SCREEN.id);
         
         SoundUtils.playSound(world,getBlockPos(), SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.PLAYERS,.5f,1);
      }
      
      @Override
      public List<BlockPos> getAffectedBlocks(ServerWorld world){
         if(getSourceWorld() instanceof ServerWorld thisWorld && thisWorld.getRegistryKey().toString().equals(world.getRegistryKey().toString())){
            ArrayList<BlockPos> blocks = new ArrayList<>();
            for(BlockPos block : BlockPos.iterateOutwards(getBlockPos(), (int) range+4, (int) range+4, (int) range+4)){
               if(block.toCenterPos().distanceTo(getBlockPos().toCenterPos()) <= range+2){
                  blocks.add(block.mutableCopy());
               }
            }
            return blocks;
         }else{
            return new ArrayList<>();
         }
      }
      
      @Override
      public List<Entity> getAffectedEntities(ServerWorld world){
         if(getSourceWorld() instanceof ServerWorld thisWorld && thisWorld.getRegistryKey().toString().equals(world.getRegistryKey().toString())){
            BlockPos blockPos = getBlockPos();
            Box rangeBox = Box.from(blockPos.toCenterPos()).expand(range+4);
            return world.getOtherEntities(null,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(blockPos.toCenterPos()) < 4*range*range && e instanceof LivingEntity);
         }else{
            return new ArrayList<>();
         }
      }
      
      @Override
      public int getDuration(){
         return duration;
      }
      
      
   }
}
