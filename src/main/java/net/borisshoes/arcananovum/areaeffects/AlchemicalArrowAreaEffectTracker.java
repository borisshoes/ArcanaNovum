package net.borisshoes.arcananovum.areaeffects;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlchemicalArrowAreaEffectTracker extends AreaEffectTracker {
   
   private static final WeakHashMap<LivingEntity, UUID> CONDITION_CONTRIBUTORS = new WeakHashMap<>();
   private final List<AlchemicalArrowSource> sources;
   
   public AlchemicalArrowAreaEffectTracker(){
      super("alchemical_arrow");
      sources = new ArrayList<>();
   }
   
   @Override
   public void onTick(MinecraftServer server){
      if(sources.isEmpty()) return;
      
      for(ServerLevel world : server.getAllLevels()){
         HashMap<BlockPos, List<Pair<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>>> affectedBlocks = new HashMap<>();
         HashMap<Entity, List<Pair<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>>> affectedEntities = new HashMap<>();
         for(AlchemicalArrowSource source : sources){
            for(BlockPos affectedBlock : source.getAffectedBlocks(world)){
               if(affectedBlocks.containsKey(affectedBlock)){
                  affectedBlocks.get(affectedBlock).addAll(source.getEffects().stream().map(e -> Pair.of(e, source)).toList());
               }else{
                  affectedBlocks.put(affectedBlock, source.getEffects().stream().map(e -> Pair.of(e, source)).collect(Collectors.toCollection(ArrayList::new)));
               }
            }
            
            for(Entity affectedEntity : source.getAffectedEntities(world)){
               if(affectedEntities.containsKey(affectedEntity)){
                  affectedEntities.get(affectedEntity).addAll(source.getEffects().stream().map(e -> Pair.of(e, source)).toList());
               }else{
                  affectedEntities.put(affectedEntity, source.getEffects().stream().map(e -> Pair.of(e, source)).collect(Collectors.toCollection(ArrayList::new)));
               }
            }
         }
         
         for(Map.Entry<Entity, List<Pair<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>>> entry : affectedEntities.entrySet()){
            Entity entity = entry.getKey();
            if(!(entity instanceof LivingEntity living)) continue;
            List<Pair<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>> effects = entry.getValue();
            HashMap<MobEffect, Pair<Integer, AlchemicalArrowSource>> instantEffects = new HashMap<>();
            
            for(Pair<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource> pair : effects){
               Either<MobEffectInstance, ConditionInstance> either = pair.getFirst();
               AlchemicalArrowSource source = pair.getSecond();
               
               if(either.left().isPresent()){
                  MobEffectInstance effect = either.left().get();
                  if(effect.getEffect().value().isInstantaneous() && server.getTickCount() % 20 == 0){
                     if(instantEffects.containsKey(effect.getEffect().value())){
                        if(effect.getAmplifier() > instantEffects.get(effect.getEffect().value()).getFirst()){
                           instantEffects.put(effect.getEffect().value(), Pair.of(effect.getAmplifier(), source));
                        }
                     }else{
                        instantEffects.put(effect.getEffect().value(), Pair.of(effect.getAmplifier(), source));
                     }
                  }else if(!effect.getEffect().value().isInstantaneous()){
                     source.applyEffect(world, living, effect);
                  }
               }else if(either.right().isPresent()){
                  source.applyCondition(world, living, either.right().get());
               }
               
            }
            
            for(Map.Entry<MobEffect, Pair<Integer, AlchemicalArrowSource>> instantEntry : instantEffects.entrySet()){
               instantEntry.getValue().getSecond().applyEffect(world, living, new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(instantEntry.getKey()), 1, instantEntry.getValue().getFirst()));
            }
         }
         
         for(Map.Entry<BlockPos, List<Pair<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>>> entry : affectedBlocks.entrySet()){
            BlockPos pos = entry.getKey();
            List<Pair<Either<MobEffectInstance, ConditionInstance>, AlchemicalArrowSource>> eithers = entry.getValue();
            List<MobEffectInstance> effects = new ArrayList<>();
            eithers.forEach(t -> t.getFirst().ifLeft(effects::add));
            
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
   
   /**
    * @param directSource the entity the field came from (the arrow), which instant effects use as their direct
    *                     source like a vanilla potion or cloud, so knockback pushes away from the field
    */
   public static AlchemicalArrowSource source(@Nullable Entity contributor, @Nullable Entity directSource, BlockPos sourceBlock, ServerLevel blockWorld, double range, int level, List<Either<MobEffectInstance, ConditionInstance>> effects){
      return new AlchemicalArrowSource(sourceBlock, blockWorld, range, level, effects, contributor, directSource);
   }
   
   public static @Nullable UUID getConditionContributor(LivingEntity entity){
      return CONDITION_CONTRIBUTORS.get(entity);
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
      private final Entity directSource;
      
      private AlchemicalArrowSource(BlockPos sourceBlock, ServerLevel blockWorld, double range, int level, List<Either<MobEffectInstance, ConditionInstance>> effects, @Nullable Entity contributor, @Nullable Entity directSource){
         this.sourceBlock = sourceBlock;
         this.blockWorld = blockWorld;
         this.range = range;
         this.level = level;
         this.effects = new ArrayList<>();
         this.effects.addAll(effects);
         this.contributor = contributor;
         this.directSource = directSource;
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
         if(effect.getEffect().value().isInstantaneous()){
            // Same call as vanilla splash and lingering potions: the damage is indirect magic from the shooter, so kill credit,
            // PvP and friendly fire work like a thrown potion. A scale of 1 keeps the damage the field always dealt.
            effect.getEffect().value().applyInstantaneousEffect(world, directSource, contributor, entity, effect.getAmplifier(), 1.0);
            return;
         }
         boolean harmful = effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL;
         if(harmful && !canHarm(entity)) return;
         MobEffectInstance existing = entity.getEffect(effect.getEffect());
         if(existing == null || existing.getAmplifier() < effect.getAmplifier()){
            entity.addEffect(new MobEffectInstance(effect), contributor);
         }
         // Damage from effects like Wither has no attacker, so keep the shooter as the player who last hurt the entity, like a hit does
         if(harmful && contributor instanceof Player shooter && entity.hasEffect(effect.getEffect())){
            entity.setLastHurtByPlayer(shooter, LivingEntity.PLAYER_HURT_EXPERIENCE_TIME);
         }
      }
      
      public void applyCondition(ServerLevel world, LivingEntity entity, ConditionInstance condition){
         if(!world.dimension().identifier().toString().equals(blockWorld.dimension().identifier().toString())) return;
         if(condition.getCondition().value().isHarmful() && !canHarm(entity)) return;
         if(Conditions.addConditionIfStronger(world.getServer(), entity, condition) == Conditions.AddResult.REJECTED) return;
         // The condition itself is unattributed (canHarm already filtered who it reaches), so the contributor is remembered here
         if(contributor != null){
            CONDITION_CONTRIBUTORS.put(entity, contributor.getUUID());
         }else{
            CONDITION_CONTRIBUTORS.remove(entity);
         }
      }
      
      // The check vanilla runs before Harming hurts a player (PvP and friendly fire), used for every harmful effect and condition.
      // BorisLib's inflictedBy check is not used, since it always spares the inflicter and their allies, even with friendly fire on.
      private boolean canHarm(LivingEntity entity){
         if(!(contributor instanceof Player shooter) || !(entity instanceof Player target)) return true;
         return target.canHarmPlayer(shooter);
      }
      
      @Override
      public List<BlockPos> getAffectedBlocks(ServerLevel world){
         if(getSourceWorld() instanceof ServerLevel thisWorld && thisWorld.dimension().identifier().toString().equals(world.dimension().identifier().toString())){
            ArrayList<BlockPos> blocks = new ArrayList<>();
            for(BlockPos block : BlockPos.withinBoxByManhattanDistance(getBlockPos(), (int) range + 4, (int) range + 4, (int) range + 4)){
               if(Vec3.atCenterOf(block).distanceTo(Vec3.atCenterOf(getBlockPos())) <= range + 0.4){
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
            AABB rangeBox = AABB.unitCubeFromLowerCorner(Vec3.atCenterOf(blockPos)).inflate(range + 4);
            return world.getEntities((Entity) null, rangeBox, e -> !e.isSpectator() && e.distanceToSqr(Vec3.atCenterOf(blockPos)) < range * range && e instanceof LivingEntity);
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
