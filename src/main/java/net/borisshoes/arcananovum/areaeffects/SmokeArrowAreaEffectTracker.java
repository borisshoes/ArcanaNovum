package net.borisshoes.arcananovum.areaeffects;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.arrows.SmokeArrows;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;

public class SmokeArrowAreaEffectTracker extends AreaEffectTracker {
   
   private final List<SmokeArrowSource> sources;
   
   public SmokeArrowAreaEffectTracker(){
      super("smoke_arrow");
      sources = new ArrayList<>();
   }
   
   @Override
   public void onTick(MinecraftServer server){
      if(sources.isEmpty()) return;
      
      for(ServerLevel world : server.getAllLevels()){
         ArrayList<BlockPos> affectedBlocks = new ArrayList<>();
         for(SmokeArrowSource source : sources){
            affectedBlocks.addAll(source.getAffectedBlocks(world).stream().filter(blockPos -> affectedBlocks.stream().noneMatch(block -> block.equals(blockPos))).toList());
            source.affectEntities(world);
         }
         
         for(BlockPos block : affectedBlocks){
            ArcanaEffectUtils.smokeArrowEmit(world,block.getCenter());
         }
      }
      
      sources.removeIf(SmokeArrowSource::age);
   }
   
   @Override
   public void addSource(AreaEffectSource source){
      if(source instanceof SmokeArrowSource smokeSource) sources.add(smokeSource);
   }
   
   public static SmokeArrowSource source(@Nullable Entity contributor, @Nullable Entity sourceEntity, @Nullable BlockPos sourceBlock, @Nullable ServerLevel blockWorld, double range, int gasLvl){
      return new SmokeArrowSource(sourceEntity,sourceBlock,blockWorld,range,gasLvl,contributor);
   }
   
   public static class SmokeArrowSource extends AreaEffectSource{
      private final Entity sourceEntity;
      private final BlockPos sourceBlock;
      private final ServerLevel blockWorld;
      private final double range;
      private final int gasLvl;
      private final boolean fromEntity;
      private int age;
      private final int duration;
      private final Entity contributor;
      
      private SmokeArrowSource(@Nullable Entity sourceEntity, @Nullable BlockPos sourceBlock, @Nullable ServerLevel blockWorld, double range, int gasLvl, @Nullable Entity contributor){
         this.sourceEntity = sourceEntity;
         this.sourceBlock = sourceBlock;
         this.blockWorld = blockWorld;
         this.range = range;
         this.gasLvl = gasLvl;
         this.fromEntity = sourceEntity != null;
         this.contributor = contributor;
         this.age = 0;
         this.duration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SMOKE_ARROW_DURATION);
      }
      
      public Level getSourceWorld(){
         if(fromEntity){
            return sourceEntity.level();
         }else{
            return blockWorld;
         }
      }
      
      public BlockPos getBlockPos(){
         if(fromEntity){
            return sourceEntity.blockPosition();
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
      
      public void affectEntities(ServerLevel world){
         if(age % 5 != 0) return;
         
         int effectDur = duration + ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SMOKE_ARROW_GAS_DURATION_PER_LVL).get(gasLvl);
         int mobCount = 0;
         boolean withOwner = false;
         for(Entity affectedEntity : getAffectedEntities(world)){
            if(affectedEntity instanceof LivingEntity e){
               int amp = e instanceof Mob ? 5 : 0;
               ConditionInstance nearsight = new ConditionInstance(Conditions.NEARSIGHT,arcanaId(ArcanaRegistry.SMOKE_ARROWS.getId()+"_"+gasLvl),effectDur,7.5f,false,true,true, AttributeModifier.Operation.ADD_VALUE,contributor != null ? contributor.getUUID() : null);
               MobEffectInstance weakness = new MobEffectInstance(MobEffects.WEAKNESS, effectDur, amp+gasLvl, false, false, true);
               MobEffectInstance invis = new MobEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, effectDur, 0, false, false, true);
               Conditions.addCondition(world.getServer(),e,nearsight);
               e.addEffect(weakness);
               if(e instanceof ServerPlayer){
                  e.addEffect(invis);
               }
               
               if(e instanceof Monster mob){
                  mob.setAggressive(false);
                  mob.setLastHurtByMob(null);
                  mobCount++;
               }
               if(contributor instanceof ServerPlayer player && player.getUUID().equals(e.getUUID())) withOwner = true;
            }
         }
         if(contributor instanceof ServerPlayer player && withOwner && mobCount >= 3) ArcanaAchievements.grant(player,ArcanaAchievements.SMOKE_SCREEN);
         
         SoundUtils.playSound(world,getBlockPos(), SoundEvents.CAMPFIRE_CRACKLE, SoundSource.PLAYERS,.5f,1);
      }
      
      @Override
      public List<BlockPos> getAffectedBlocks(ServerLevel world){
         if(getSourceWorld() instanceof ServerLevel thisWorld && thisWorld.dimension().identifier().toString().equals(world.dimension().identifier().toString())){
            ArrayList<BlockPos> blocks = new ArrayList<>();
            for(BlockPos block : BlockPos.withinManhattan(getBlockPos(), (int) range+4, (int) range+4, (int) range+4)){
               if(block.getCenter().distanceTo(getBlockPos().getCenter()) <= range+2){
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
            AABB rangeBox = AABB.unitCubeFromLowerCorner(blockPos.getCenter()).inflate(range+4);
            return world.getEntities((Entity) null,rangeBox, e -> !e.isSpectator() && e.distanceToSqr(blockPos.getCenter()) < 4*range*range && e instanceof LivingEntity);
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
