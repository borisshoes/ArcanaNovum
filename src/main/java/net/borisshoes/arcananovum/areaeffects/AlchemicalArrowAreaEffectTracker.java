package net.borisshoes.arcananovum.areaeffects;

import com.mojang.datafixers.util.Either;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlchemicalArrowAreaEffectTracker extends AreaEffectTracker {
   
   private final List<AlchemicalArrowSource> sources;
   
   public AlchemicalArrowAreaEffectTracker(){
      super("alchemical_arrow");
      sources = new ArrayList<>();
   }
   
   @Override
   public void onTick(MinecraftServer server){
      if(sources.isEmpty()) return;
      
      for(ServerLevel world : server.getAllLevels()){
         HashMap<BlockPos, List<Tuple<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>>> affectedBlocks = new HashMap<>();
         HashMap<Entity, List<Tuple<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>>> affectedEntities = new HashMap<>();
         for(AlchemicalArrowSource source : sources){
            for(BlockPos affectedBlock : source.getAffectedBlocks(world)){
               if(affectedBlocks.containsKey(affectedBlock)){
                  affectedBlocks.get(affectedBlock).addAll(source.getEffects().stream().map(e -> new Tuple<>(e, source)).toList());
               }else{
                  affectedBlocks.put(affectedBlock, source.getEffects().stream().map(e -> new Tuple<>(e, source)).collect(Collectors.toCollection(ArrayList::new)));
               }
            }
            
            for(Entity affectedEntity : source.getAffectedEntities(world)){
               if(affectedEntities.containsKey(affectedEntity)){
                  affectedEntities.get(affectedEntity).addAll(source.getEffects().stream().map(e -> new Tuple<>(e, source)).toList());
               }else{
                  affectedEntities.put(affectedEntity, source.getEffects().stream().map(e -> new Tuple<>(e, source)).collect(Collectors.toCollection(ArrayList::new)));
               }
            }
         }
         
         for(Map.Entry<Entity, List<Tuple<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>>> entry : affectedEntities.entrySet()){
            Entity entity = entry.getKey();
            if(!(entity instanceof LivingEntity living)) continue;
            List<Tuple<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>> effects = entry.getValue();
            HashMap<MobEffect, Tuple<Integer, AlchemicalArrowSource>> instantEffects = new HashMap<>();
            
            for(Tuple<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource> pair : effects){
               Either<MobEffectInstance, ConditionInstance> either = pair.getA();
               AlchemicalArrowSource source = pair.getB();
               
               if(either.left().isPresent()){
                  MobEffectInstance effect = either.left().get();
                  if(effect.getEffect().value().isInstantenous() && server.getTickCount() % 20 == 0){
                     if(instantEffects.containsKey(effect.getEffect().value())){
                        if(effect.getAmplifier() > instantEffects.get(effect.getEffect().value()).getA()){
                           instantEffects.put(effect.getEffect().value(), new Tuple<>(effect.getAmplifier(), source));
                        }
                     }else{
                        instantEffects.put(effect.getEffect().value(), new Tuple<>(effect.getAmplifier(), source));
                     }
                  }else if(!effect.getEffect().value().isInstantenous()){
                     source.applyEffect(world, living, effect);
                  }
               }else if(either.right().isPresent()){
                  source.applyCondition(world, living, either.right().get());
               }
               
            }
            
            for(Map.Entry<MobEffect, Tuple<Integer, AlchemicalArrowSource>> instantEntry : instantEffects.entrySet()){
               instantEntry.getValue().getB().applyEffect(world, living, new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(instantEntry.getKey()), 1, instantEntry.getValue().getA()));
            }
         }
         
         for(Map.Entry<BlockPos, List<Tuple<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>>> entry : affectedBlocks.entrySet()){
            BlockPos pos = entry.getKey();
            List<Tuple<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>> eithers = entry.getValue();
            List<MobEffectInstance> effects = new ArrayList<>();
            eithers.forEach(t -> t.getA().ifLeft(effects::add));
            
            int random = world.getRandom().nextInt(effects.size());
            if(world.getRandom().nextDouble() < 0.03){
               ParticleOptions dust = new DustParticleOptions(effects.get(random).getEffect().value().getColor(), 1.2f);
               world.sendParticles(dust, pos.getX(), pos.getY(), pos.getZ(), 1, 0.5, 0.5, 0.5, 0);
            }
         }
      }
      
      sources.removeIf(AlchemicalArrowSource::age);
   }
   
   @Override
   public void addSource(AreaEffectSource source){
      if(source instanceof AlchemicalArrowSource arrowSource) sources.add(arrowSource);
   }
   
   public static AlchemicalArrowSource source(@Nullable Entity contributor, BlockPos sourceBlock, ServerLevel blockWorld, double range, int level, List<Either<MobEffectInstance, ConditionInstance>> effects){
      return new AlchemicalArrowSource(sourceBlock, blockWorld, range, level, effects, contributor);
   }
   
   public static class AlchemicalArrowSource extends AreaEffectSource {
      private final BlockPos sourceBlock;
      private final ServerLevel blockWorld;
      private final double range;
      private final int level;
      private final List<Either<MobEffectInstance, ConditionInstance>> effects;
      private int age;
      private final int duration;
      private final Entity contributor;
      
      private AlchemicalArrowSource(BlockPos sourceBlock, ServerLevel blockWorld, double range, int level, List<Either<MobEffectInstance, ConditionInstance>> effects, @Nullable Entity contributor){
         this.sourceBlock = sourceBlock;
         this.blockWorld = blockWorld;
         this.range = range;
         this.level = level;
         this.effects = new ArrayList<>();
         this.effects.addAll(effects);
         this.contributor = contributor;
         this.age = 0;
         this.duration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.ALCHEMICAL_ARBALEST_FIELD_DURATION);
         ;
      }
      
      public Level getSourceWorld(){
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
      
      public List<Either<MobEffectInstance, ConditionInstance>> getEffects(){
         return effects;
      }
      
      public void applyEffect(ServerLevel world, LivingEntity entity, MobEffectInstance effect){
         if(!world.dimension().identifier().toString().equals(blockWorld.dimension().identifier().toString())) return;
         MobEffectInstance existing = entity.getEffect(effect.getEffect());
         if(existing != null && existing.getAmplifier() >= effect.getAmplifier()){
            return;
         }
         entity.addEffect(new MobEffectInstance(effect), contributor);
      }
      
      public void applyCondition(ServerLevel world, LivingEntity entity, ConditionInstance condition){
         if(!world.dimension().identifier().toString().equals(blockWorld.dimension().identifier().toString())) return;
         Conditions.addCondition(world.getServer(), entity, condition);
      }
      
      @Override
      public List<BlockPos> getAffectedBlocks(ServerLevel world){
         if(getSourceWorld() instanceof ServerLevel thisWorld && thisWorld.dimension().identifier().toString().equals(world.dimension().identifier().toString())){
            ArrayList<BlockPos> blocks = new ArrayList<>();
            for(BlockPos block : BlockPos.withinManhattan(getBlockPos(), (int) range + 4, (int) range + 4, (int) range + 4)){
               if(block.getCenter().distanceTo(getBlockPos().getCenter()) <= range + 0.4){
                  blocks.add(block.mutable());
               }
            }
            return blocks;
         }else{
            return new ArrayList<>();
         }
      }
      
      @Override
      public List<Entity> getAffectedEntities(ServerLevel world){
         if(getSourceWorld() instanceof ServerLevel thisWorld && thisWorld.dimension().identifier().toString().equals(world.dimension().identifier().toString())){
            BlockPos blockPos = getBlockPos();
            AABB rangeBox = AABB.unitCubeFromLowerCorner(blockPos.getCenter()).inflate(range + 4);
            return world.getEntities((Entity) null, rangeBox, e -> !e.isSpectator() && e.distanceToSqr(blockPos.getCenter()) < range * range && e instanceof LivingEntity);
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
