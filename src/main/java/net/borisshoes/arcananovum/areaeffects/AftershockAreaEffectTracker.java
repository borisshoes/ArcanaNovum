package net.borisshoes.arcananovum.areaeffects;

import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AftershockAreaEffectTracker extends AreaEffectTracker{
   
   private final List<AftershockSource> sources;
   
   public AftershockAreaEffectTracker(){
      super("aftershock");
      sources = new ArrayList<>();
   }
   
   
   @Override
   public void onTick(MinecraftServer server){
      if(sources.isEmpty()) return;
      
      for(ServerWorld world : server.getWorlds()){
         ArrayList<BlockPos> affectedBlocks = new ArrayList<>();
         HashMap<Entity,AftershockSource> affectedEntities = new HashMap<>();
         for(AftershockSource source : sources){
            affectedBlocks.addAll(source.getAffectedBlocks(world).stream().filter(blockPos -> affectedBlocks.stream().noneMatch(block -> block.equals(blockPos))).toList());
            
            for(Entity affectedEntity : source.getAffectedEntities(world)){
               if(affectedEntities.containsKey(affectedEntity)){
                  if(source.getLevel() > affectedEntities.get(affectedEntity).getLevel()){
                     affectedEntities.put(affectedEntity,source);
                  }
               }else{
                  affectedEntities.put(affectedEntity,source);
               }
            }
         }
         
         if(server.getTicks() % 5 == 0){
            for(Map.Entry<Entity, AftershockSource> entry : affectedEntities.entrySet()){
               entry.getValue().affectEntity(world,entry.getKey());
            }
         }
         
         for(BlockPos block : affectedBlocks){
            boolean aboveFloor = world.getBlockState(block).getCollisionShape(world,block).isEmpty() && (world.getBlockState(block.down()).isFullCube(world,block.down()) || !world.getBlockState(block.down()).getCollisionShape(world,block.down()).isEmpty());
            if(aboveFloor && Math.random() < 0.15) world.spawnParticles(ParticleTypes.WAX_OFF,block.getX(),block.getY(),block.getZ(),1,0.5,0.5,0.5,.1);
         }
      }
      
      sources.removeIf(AftershockSource::age);
   }
   
   @Override
   public void addSource(AreaEffectSource source){
      if(source instanceof AftershockSource shockSource) sources.add(shockSource);
   }
   
   public static AftershockSource source(@Nullable Entity contributor, BlockPos sourceBlock, ServerWorld blockWorld, int level){
      return new AftershockSource(sourceBlock,blockWorld,level,contributor);
   }
   
   public static class AftershockSource extends AreaEffectSource{
      private final BlockPos sourceBlock;
      private final ServerWorld blockWorld;
      private final double range;
      private final float damage;
      private final int level;
      private int age;
      private final int duration;
      private final Entity contributor;
      
      private AftershockSource(BlockPos sourceBlock, ServerWorld blockWorld, int level, @Nullable Entity contributor){
         this.sourceBlock = sourceBlock;
         this.blockWorld = blockWorld;
         this.range = level >= 4 ? 4 : 2.5;
         this.damage = level >= 4 ? 4 : 2;
         this.level = level;
         this.contributor = contributor;
         this.age = 0;
         this.duration = 30 + 20*level;
      }
      
      public World getSourceWorld(){
         return blockWorld;
      }
      
      public BlockPos getBlockPos(){
         return sourceBlock;
      }
      
      public boolean age(){
         if(age % 2 == 0){
            SoundUtils.playSound(blockWorld,sourceBlock, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS,.07f,2f);
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
      
      public void affectEntity(ServerWorld world, Entity entity){
         if(entity instanceof LivingEntity e){
            DamageSource source = ArcanaDamageTypes.of(world,ArcanaDamageTypes.ARCANE_LIGHTNING,contributor);
            e.damage(world,source,damage);
         }
      }
      
      @Override
      public List<BlockPos> getAffectedBlocks(ServerWorld world){
         if(getSourceWorld() instanceof ServerWorld thisWorld && thisWorld.getRegistryKey().toString().equals(world.getRegistryKey().toString())){
            ArrayList<BlockPos> blocks = new ArrayList<>();
            for(BlockPos block : BlockPos.iterateOutwards(getBlockPos(), (int) range+4, (int) range+4, (int) range+4)){
               if(block.toCenterPos().distanceTo(getBlockPos().toCenterPos()) <= range+1){
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
            return world.getOtherEntities(null,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(blockPos.toCenterPos()) < 1.25*range*range && e instanceof LivingEntity);
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
