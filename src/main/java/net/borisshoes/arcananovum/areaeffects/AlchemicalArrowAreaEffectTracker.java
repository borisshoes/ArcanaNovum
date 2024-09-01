package net.borisshoes.arcananovum.areaeffects;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.effects.DamageAmpEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlchemicalArrowAreaEffectTracker extends AreaEffectTracker{
   
   private final List<AlchemicalArrowSource> sources;
   
   public AlchemicalArrowAreaEffectTracker(){
      super("alchemical_arrow");
      sources = new ArrayList<>();
   }
   
   @Override
   public void onTick(MinecraftServer server){
      if(sources.isEmpty()) return;
      
      for(ServerWorld world : server.getWorlds()){
         HashMap<BlockPos, List<Pair<StatusEffectInstance,AlchemicalArrowSource>>> affectedBlocks = new HashMap<>();
         HashMap<Entity, List<Pair<StatusEffectInstance,AlchemicalArrowSource>>> affectedEntities = new HashMap<>();
         for(AlchemicalArrowSource source : sources){
            for(BlockPos affectedBlock : source.getAffectedBlocks(world)){
               if(affectedBlocks.containsKey(affectedBlock)){
                  affectedBlocks.get(affectedBlock).addAll(source.getEffects().stream().map(e -> new Pair<>(e,source)).toList());
               }else{
                  affectedBlocks.put(affectedBlock,source.getEffects().stream().map(e -> new Pair<>(e,source)).collect(Collectors.toCollection(ArrayList::new)));
               }
            }
            
            for(Entity affectedEntity : source.getAffectedEntities(world)){
               if(affectedEntities.containsKey(affectedEntity)){
                  affectedEntities.get(affectedEntity).addAll(source.getEffects().stream().map(e -> new Pair<>(e,source)).toList());
               }else{
                  affectedEntities.put(affectedEntity,source.getEffects().stream().map(e -> new Pair<>(e,source)).collect(Collectors.toCollection(ArrayList::new)));
               }
            }
         }
         
         for(Map.Entry<Entity, List<Pair<StatusEffectInstance, AlchemicalArrowSource>>> entry : affectedEntities.entrySet()){
            Entity entity = entry.getKey();
            if(!(entity instanceof LivingEntity living)) continue;
            List<Pair<StatusEffectInstance, AlchemicalArrowSource>> effects = entry.getValue();
            HashMap<StatusEffect,Pair<Integer,AlchemicalArrowSource>> instantEffects = new HashMap<>();
            
            for(Pair<StatusEffectInstance, AlchemicalArrowSource> pair : effects){
               StatusEffectInstance effect = pair.getLeft();
               AlchemicalArrowSource source = pair.getRight();
               
               if(effect.getEffectType().value().isInstant() && server.getTicks() % 20 == 0){
                  if(instantEffects.containsKey(effect.getEffectType().value())){
                     if(effect.getAmplifier() > instantEffects.get(effect.getEffectType().value()).getLeft()){
                        instantEffects.put(effect.getEffectType().value(),new Pair<>(effect.getAmplifier(),source));
                     }
                  }else{
                     instantEffects.put(effect.getEffectType().value(),new Pair<>(effect.getAmplifier(),source));
                  }
               }else if(!effect.getEffectType().value().isInstant()){
                  source.applyEffect(world, living, effect);
               }
            }
            
            for(Map.Entry<StatusEffect, Pair<Integer,AlchemicalArrowSource>> instantEntry : instantEffects.entrySet()){
               instantEntry.getValue().getRight().applyEffect(world, living, new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(instantEntry.getKey()),1,instantEntry.getValue().getLeft()));
            }
         }
         
         for(Map.Entry<BlockPos, List<Pair<StatusEffectInstance, AlchemicalArrowSource>>> entry : affectedBlocks.entrySet()){
            BlockPos pos = entry.getKey();
            List<Pair<StatusEffectInstance, AlchemicalArrowSource>> effects = entry.getValue();
            int random = (int) (Math.random()*effects.size());
            
            if(Math.random() < 0.1){
               ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(effects.get(random).getLeft().getEffectType().value().getColor()).toVector3f(),1.2f);
               world.spawnParticles(dust,pos.getX(),pos.getY(),pos.getZ(),1,0.5,0.5,0.5,0);
            }
         }
      }
      
      sources.removeIf(AlchemicalArrowSource::age);
   }
   
   @Override
   public void addSource(AreaEffectSource source){
      if(source instanceof AlchemicalArrowSource arrowSource) sources.add(arrowSource);
   }
   
   public static AlchemicalArrowSource source(@Nullable Entity contributor, BlockPos sourceBlock, ServerWorld blockWorld, double range, int level, List<StatusEffectInstance> effects){
      return new AlchemicalArrowSource(sourceBlock,blockWorld,range,level,effects,contributor);
   }
   
   public static class AlchemicalArrowSource extends AreaEffectSource{
      private final BlockPos sourceBlock;
      private final ServerWorld blockWorld;
      private final double range;
      private final int level;
      private final List<StatusEffectInstance> effects;
      private int age;
      private final int duration;
      private final Entity contributor;
      
      private AlchemicalArrowSource(BlockPos sourceBlock, ServerWorld blockWorld, double range, int level, List<StatusEffectInstance> effects, @Nullable Entity contributor){
         this.sourceBlock = sourceBlock;
         this.blockWorld = blockWorld;
         this.range = range;
         this.level = level;
         this.effects = new ArrayList<>();
         this.effects.addAll(effects);
         this.contributor = contributor;
         this.age = 0;
         this.duration = 200;
      }
      
      public World getSourceWorld(){
         return blockWorld;
      }
      
      public BlockPos getBlockPos(){
         return sourceBlock;
      }
      
      public boolean age(){
         this.age++;
         return age >= duration;
      }
      
      public int getLevel(){
         return level;
      }
      
      public Entity getContributor(){
         return contributor;
      }
      
      public List<StatusEffectInstance> getEffects(){
         return effects;
      }
      
      public void applyEffect(ServerWorld world, LivingEntity entity, StatusEffectInstance effect){
         if(!world.getRegistryKey().toString().equals(blockWorld.getRegistryKey().toString())) return;
         
         boolean applied = entity.addStatusEffect(new StatusEffectInstance(effect),contributor);
         if(applied && effect.getEffectType() == ArcanaRegistry.DAMAGE_AMP_EFFECT && contributor instanceof LivingEntity applier){
            DamageAmpEffect.AMP_TRACKER.put(entity,applier);
         }
      }
      
      @Override
      public List<BlockPos> getAffectedBlocks(ServerWorld world){
         if(getSourceWorld() instanceof ServerWorld thisWorld && thisWorld.getRegistryKey().toString().equals(world.getRegistryKey().toString())){
            ArrayList<BlockPos> blocks = new ArrayList<>();
            for(BlockPos block : BlockPos.iterateOutwards(getBlockPos(), (int) range+4, (int) range+4, (int) range+4)){
               if(block.toCenterPos().distanceTo(getBlockPos().toCenterPos()) <= range+0.4){
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
            return world.getOtherEntities(null,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(blockPos.toCenterPos()) < range*range && e instanceof LivingEntity);
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
