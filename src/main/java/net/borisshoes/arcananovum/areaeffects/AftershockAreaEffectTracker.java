package net.borisshoes.arcananovum.areaeffects;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AftershockAreaEffectTracker extends AreaEffectTracker {
   
   private final List<AftershockSource> sources;
   
   public AftershockAreaEffectTracker(){
      super("aftershock");
      sources = new ArrayList<>();
   }
   
   
   @Override
   public void onTick(MinecraftServer server){
      if(sources.isEmpty()) return;
      
      for(ServerLevel world : server.getAllLevels()){
         ArrayList<BlockPos> affectedBlocks = new ArrayList<>();
         HashMap<Entity, AftershockSource> affectedEntities = new HashMap<>();
         for(AftershockSource source : sources){
            affectedBlocks.addAll(source.getAffectedBlocks(world).stream().filter(blockPos -> affectedBlocks.stream().noneMatch(block -> block.equals(blockPos))).toList());
            
            for(Entity affectedEntity : source.getAffectedEntities(world)){
               if(affectedEntities.containsKey(affectedEntity)){
                  if(source.getLevel() > affectedEntities.get(affectedEntity).getLevel()){
                     affectedEntities.put(affectedEntity, source);
                  }
               }else{
                  affectedEntities.put(affectedEntity, source);
               }
            }
         }
         
         if(server.getTickCount() % 5 == 0){
            for(Map.Entry<Entity, AftershockSource> entry : affectedEntities.entrySet()){
               entry.getValue().affectEntity(world, entry.getKey());
            }
         }
         
         for(BlockPos block : affectedBlocks){
            boolean aboveFloor = world.getBlockState(block).getCollisionShape(world, block).isEmpty() && (world.getBlockState(block.below()).isCollisionShapeFullBlock(world, block.below()) || !world.getBlockState(block.below()).getCollisionShape(world, block.below()).isEmpty());
            if(aboveFloor && Math.random() < 0.15)
               world.sendParticles(ParticleTypes.WAX_OFF, block.getX(), block.getY(), block.getZ(), 1, 0.5, 0.5, 0.5, .1);
         }
      }
      
      sources.removeIf(AftershockSource::age);
   }
   
   @Override
   public void addSource(AreaEffectSource source){
      if(source instanceof AftershockSource shockSource) sources.add(shockSource);
   }
   
   public static AftershockSource source(@Nullable Entity contributor, BlockPos sourceBlock, ServerLevel blockWorld, int level){
      return new AftershockSource(sourceBlock, blockWorld, level, contributor);
   }
   
   public static class AftershockSource extends AreaEffectSource {
      private final BlockPos sourceBlock;
      private final ServerLevel blockWorld;
      private final double range;
      private final float damage;
      private final int level;
      private int age;
      private final int duration;
      private final Entity contributor;
      
      private AftershockSource(BlockPos sourceBlock, ServerLevel blockWorld, int level, @Nullable Entity contributor){
         this.sourceBlock = sourceBlock;
         this.blockWorld = blockWorld;
         this.range = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.STORM_ARROW_AFTERSHOCK_RANGE_PER_LVL).get(level);
         this.damage = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.STORM_ARROW_AFTERSHOCK_DMG_PER_LVL).get(level);
         this.level = level;
         this.contributor = contributor;
         this.age = 0;
         this.duration = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.STORM_ARROW_AFTERSHOCK_DURATION_PER_LVL).get(level);
      }
      
      public Level getSourceWorld(){
         return blockWorld;
      }
      
      public BlockPos getBlockPos(){
         return sourceBlock;
      }
      
      public boolean age(){
         if(age % 2 == 0){
            SoundUtils.playSound(blockWorld, sourceBlock, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, .07f, 2f);
         }
         this.age++;
         return age >= duration;
      }
      
      public int getLevel(){
         return level;
      }
      
      public Entity getContributor(){
         return contributor;
      }
      
      public void affectEntity(ServerLevel world, Entity entity){
         if(entity instanceof LivingEntity e){
            DamageSource source = ArcanaDamageTypes.of(world, ArcanaDamageTypes.ARCANE_LIGHTNING, contributor);
            e.hurtServer(world, source, damage);
         }
      }
      
      @Override
      public List<BlockPos> getAffectedBlocks(ServerLevel world){
         if(getSourceWorld() instanceof ServerLevel thisWorld && thisWorld.dimension().identifier().toString().equals(world.dimension().identifier().toString())){
            ArrayList<BlockPos> blocks = new ArrayList<>();
            for(BlockPos block : BlockPos.withinManhattan(getBlockPos(), (int) range + 4, (int) range + 4, (int) range + 4)){
               if(block.getCenter().distanceTo(getBlockPos().getCenter()) <= range + 1){
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
            return world.getEntities((Entity) null, rangeBox, e -> !e.isSpectator() && e.distanceToSqr(blockPos.getCenter()) < 1.25 * range * range && e instanceof LivingEntity);
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
