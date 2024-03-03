package net.borisshoes.arcananovum.bosses.dragon;

import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SpawnPile;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragonLairActions {
   
   private final int terrainDur = 3*20;
   private final int dimensionDur = 3*20;
   private final int quakeDur = 10*20;
   private final int starfallDur = 30*20;
   private final int lapseDur = 30*20;
   
   private int terrainTicks;
   private int dimensionTicks;
   private int quakeTicks;
   private int starfallTicks;
   
   private DragonLairActionTypes curAct;
   private ArrayList<Vec3d> stars;
   private HashMap<String,Integer> starHits;
   private boolean chasm;
   private BlockPos chasmStart;
   private BlockPos chasmEnd;
   private HashMap<Integer,ArrayList<BlockPos>> chasmBlocks;
   private ArrayList<Spike> spikes;
   private HashMap<Integer,ArrayList<BlockPos>> spikeBlocks;
   
   private final EnderDragonEntity dragon;
   private final MinecraftServer server;
   private final ServerWorld endWorld;
   
   public DragonLairActions(MinecraftServer server, ServerWorld endWorld, EnderDragonEntity dragon){
      this.server = server;
      this.endWorld = endWorld;
      this.dragon = dragon;
   
      terrainTicks = 0;
      dimensionTicks = 0;
      quakeTicks = 0;
      starfallTicks = 0;
      
      curAct = null;
   }
   
   public void tick(){
      List<ServerPlayerEntity> nearbyPlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(new Vec3d(0,100,0)) <= 300*300 && !p.isSpectator() && !p.isCreative());
      if(terrainTicks > 0){
         if(chasm){
            if(terrainTicks % 4 == 0){
               int depthMod = 16-terrainTicks/4;
               for(BlockPos block : chasmBlocks.get(depthMod)){
                  endWorld.setBlockState(block,Blocks.AIR.getDefaultState());
               }
               
               
               if(spikeBlocks != null){
                  int iter = terrainTicks/4;
                  if(spikeBlocks.get(iter) != null){
                     for(BlockPos block : spikeBlocks.get(iter)){
                        endWorld.setBlockState(block,Blocks.AIR.getDefaultState());
                     }
                  }
               }
            }
         }else{
            if(terrainTicks % 4 == 0){
               if(chasmBlocks != null){ // Undo Chasm
                  int depthMod = terrainTicks/4;
                  if(chasmBlocks.get(depthMod) != null){
                     for(BlockPos block : chasmBlocks.get(depthMod)){
                        endWorld.setBlockState(block,Blocks.END_STONE.getDefaultState());
                     }
                  }
               }
   
               int iter = 16-terrainTicks/4;
               for(Spike spike : spikes){
                  double sizeMod = (spike.radius / 15 * iter);
                  //log("Spiking iter: "+iter+" size: "+sizeMod);
   
                  ArrayList<BlockPos> blocks = spike.generate(endWorld,sizeMod);
                  if(spikeBlocks.containsKey(iter)){
                     ArrayList<BlockPos> oldBlocks = spikeBlocks.get(iter);
                     oldBlocks.addAll(blocks);
                     spikeBlocks.put(iter,oldBlocks);
                  }else{
                     spikeBlocks.put(iter,blocks);
                  }
                  
                  
                  for(BlockPos block : blocks){
                     endWorld.setBlockState(block,Blocks.END_STONE.getDefaultState());
                  }
               }
            }
         }
         
         
         terrainTicks--;
      }
      if(dimensionTicks > 0){
         if(dimensionTicks == 1){
            ArrayList<BlockPos> locations = SpawnPile.makeSpawnLocations(nearbyPlayers.size(),50,endWorld);
            int i = 0;
            for(ServerPlayerEntity player : nearbyPlayers){
               BlockPos pos = locations.get(i);
               player.teleport(endWorld,pos.getX(),pos.getY(),pos.getZ(), (float) (Math.random()*360-180),(float) (Math.random()*360-180));
               endWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,pos.getX(),pos.getY()+1.5,pos.getZ(),500,.3,1,.3,3);
               player.sendMessage(Text.literal("Ender Energy Surges Through You!").formatted(Formatting.DARK_PURPLE,Formatting.ITALIC),true);
               i++;
            }
         }else{
            for(ServerPlayerEntity player : nearbyPlayers){
               endWorld.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getY() - .5, player.getZ(), 5, .3, 1, .3, .3);
            }
         }
         dimensionTicks--;
      }
      if(quakeTicks > 0){
         for(ServerPlayerEntity player : nearbyPlayers){
            endWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.END_STONE.getDefaultState()), player.getX(), player.getY()-.5, player.getZ(), 40, 3, 1, 3, .5);
            
            if(quakeTicks % 4 == 0){
               player.tiltScreen(Math.random()*10-5, Math.random()*10-5);
            }
         }
         quakeTicks--;
      }
      if(starfallTicks > 0){
         for(int i = 0; i < 40; i++){
            stars.add(new Vec3d(Math.random()*300-150,160,Math.random()*300-150));
         }
         for(int i = 0; i < stars.size(); i++){
            Vec3d star = stars.get(i);
            star = star.add(0,-1,0);
            stars.set(i,star);
            
            endWorld.spawnParticles(ParticleTypes.END_ROD, star.getX(), star.getY(), star.getZ(), 5, 0.1, 0.1, 0.1, 0);
            endWorld.spawnParticles(ParticleTypes.END_ROD, star.getX(), star.getY()-.25, star.getZ(), 1, 0, 0, 0, 0);
            endWorld.spawnParticles(ParticleTypes.END_ROD, star.getX(), star.getY()-.5, star.getZ(), 1, 0, 0, 0, 0);
            endWorld.spawnParticles(ParticleTypes.END_ROD, star.getX(), star.getY()-.75, star.getZ(), 1, 0, 0, 0, 0);
   
            Vec3d finalStar = star;
            List<ServerPlayerEntity> hitPlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(finalStar) <= 2*2 && !p.isSpectator() && !p.isCreative() && !starHits.containsKey(p.getUuidAsString()));
   
            for(ServerPlayerEntity player : hitPlayers){
               if(player.isBlocking() && (player.getMainHandStack().isOf(Items.SHIELD) || player.getOffHandStack().isOf(Items.SHIELD)) && player.getPitch() < -60){
                  player.disableShield(true);
               }else{
                  player.damage(new DamageSource(endWorld.getDamageSources().magic().getTypeRegistryEntry(), this.dragon,this.dragon),10);
               }
               starHits.put(player.getUuidAsString(),10);
            }
            
         }
         stars.removeIf(star -> star.getY() < 0);
         stars.removeIf(star -> endWorld.getBlockState(BlockPos.ofFloored((int)(star.getX()+0.5),(int)(star.getY()),(int)(star.getZ()+0.5))).isOpaque());
   
         for(Map.Entry<String, Integer> hitPlayer : starHits.entrySet()){
            hitPlayer.setValue(hitPlayer.getValue()-1);
         }
         starHits.entrySet().removeIf(entry -> entry.getValue() < 0);
         
         starfallTicks--;
      }
   }
   
   public boolean startAction(DragonLairActionTypes action){
      List<ServerPlayerEntity> nearbyPlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(new Vec3d(0,100,0)) <= 300*300 && !p.isSpectator() && !p.isCreative());
      switch(action){
         case TERRAIN_SHIFT: // Terrain Shift
            chasm = !chasm;
            if(chasm){
               double radius = 60;
               double theta = Math.random() * (Math.PI - 2) + 2;
               double offset = Math.random() * 2 * Math.PI;
               int x1 = (int) (radius*Math.cos(offset));
               int z1 = (int) (radius*Math.sin(offset));
               int x2 = (int) (radius*Math.cos(theta+offset));
               int z2 = (int) (radius*Math.sin(theta+offset));
               chasmBlocks = new HashMap<>();
               chasmStart = new BlockPos(x1,65,z1);
               chasmEnd = new BlockPos(x2,65,z2);
            
               makeChasm(chasmStart,chasmEnd);
            }else{
               int numSpikes = 4;
               spikes = new ArrayList<>();
               spikeBlocks = new HashMap<>();
            
               for(int i = 0; i < numSpikes; i++){
                  int x1 = (int)(Math.random()*100-50);
                  int y1 = 55;
                  int z1 = (int)(Math.random()*100-50);
                  Vec3d start = new Vec3d(x1+0.5,y1+0.5,z1+0.5);
                  double phi = Math.random() * 2 * Math.PI;
                  double theta = Math.random() * (Math.PI/3);
                  double length = Math.random() * 30 + 30;
                  double x2 = length * Math.sin(theta) * Math.cos(phi);
                  double z2 = length * Math.sin(theta) * Math.sin(phi);
                  double y2 = length * Math.cos(theta);
                  Vec3d end = start.add(x2,y2,z2);
                  double size = Math.random() * 5 + 5;
               
                  //log("New Spike at: "+start+" to "+end+" len: "+length+" with size: "+size);
                  spikes.add(new Spike(start,end,size));
               }
            }
         
            DragonDialog.announce(DragonDialog.Announcements.ABILITY_TERRAIN_SHIFT,endWorld.getServer(),null);
            terrainTicks = terrainDur;
            break;
         case GRAVITY_LAPSE: // Gravity Lapse
            for(ServerPlayerEntity player : nearbyPlayers){
               if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
               StatusEffectInstance jump = new StatusEffectInstance(StatusEffects.JUMP_BOOST, lapseDur, 14, false, false, true);
               StatusEffectInstance hover = new StatusEffectInstance(StatusEffects.SLOW_FALLING, lapseDur, 0, false, false, true);
               player.addStatusEffect(jump);
               player.addStatusEffect(hover);
            }
         
            DragonDialog.announce(DragonDialog.Announcements.ABILITY_GRAVITY_LAPSE,endWorld.getServer(),null);
            break;
         case DIMENSION_SHIFT: // Dimension Shift
            dimensionTicks = dimensionDur;
            DragonDialog.announce(DragonDialog.Announcements.ABILITY_DIMENSION_SHIFT,endWorld.getServer(),null);
            break;
         case QUAKE: // Quake
            quakeTicks = quakeDur;
            for(ServerPlayerEntity player : nearbyPlayers){
               StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, quakeDur, 0, false, false, true);
               player.addStatusEffect(slow);
               StatusEffectInstance fatigue = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, quakeDur, 0, false, false, true);
               player.addStatusEffect(fatigue);
            }
            DragonDialog.announce(DragonDialog.Announcements.ABILITY_QUAKE,endWorld.getServer(),null);
            break;
         case STARFALL: // Starfall
            stars = new ArrayList<>();
            starHits = new HashMap<>();
            starfallTicks = starfallDur;
            DragonDialog.announce(DragonDialog.Announcements.ABILITY_STARFALL,endWorld.getServer(),null);
            break;
      }
   
      return true;
   }
   
   public boolean startAction(int phase){
      DragonLairActionTypes action = rollAction(phase);
      return startAction(action);
   }
   
   public DragonLairActionTypes rollAction(int phase){
      ArrayList<Pair<DragonLairActionTypes,Integer>> actions = new ArrayList<>();
      ArrayList<DragonLairActionTypes> weighted = new ArrayList<>();
      
      if(phase == 1){
         actions.add(new Pair<>(DragonLairActionTypes.TERRAIN_SHIFT,8));
         actions.add(new Pair<>(DragonLairActionTypes.GRAVITY_LAPSE,1));
         actions.add(new Pair<>(DragonLairActionTypes.DIMENSION_SHIFT,3));
         actions.add(new Pair<>(DragonLairActionTypes.QUAKE,5));
         actions.add(new Pair<>(DragonLairActionTypes.STARFALL,1));
      }else if(phase == 2){
         actions.add(new Pair<>(DragonLairActionTypes.TERRAIN_SHIFT,4));
         actions.add(new Pair<>(DragonLairActionTypes.GRAVITY_LAPSE,6));
         actions.add(new Pair<>(DragonLairActionTypes.DIMENSION_SHIFT,2));
         actions.add(new Pair<>(DragonLairActionTypes.QUAKE,2));
         actions.add(new Pair<>(DragonLairActionTypes.STARFALL,1));
      }else if(phase == 3){
         actions.add(new Pair<>(DragonLairActionTypes.TERRAIN_SHIFT,3));
         actions.add(new Pair<>(DragonLairActionTypes.GRAVITY_LAPSE,1));
         actions.add(new Pair<>(DragonLairActionTypes.DIMENSION_SHIFT,5));
         actions.add(new Pair<>(DragonLairActionTypes.QUAKE,2));
         actions.add(new Pair<>(DragonLairActionTypes.STARFALL,4));
      }
   
      for(Pair<DragonLairActionTypes, Integer> action : actions){
         for(int i = 0; i < action.getRight(); i++){
            weighted.add(action.getLeft());
         }
      }
      
      return weighted.get((int)(Math.random()*weighted.size()));
   }
   
   private HashMap<Integer,ArrayList<BlockPos>> makeChasm(BlockPos start, BlockPos end){
      final int minX = Math.min(start.getX(),end.getX());
      final int minZ = Math.min(start.getZ(),end.getZ());
      final int maxX = Math.max(start.getX(),end.getX());
      final int maxZ = Math.max(start.getZ(),end.getZ());
      final int midY = (int)(0.5*(start.getY() + end.getY()));
      final int extra = 10;
      
      ArrayList<ArrayList<BlockPos>> tiers = new ArrayList<>();
      for(int i = 0; i <= 15; i++){
         tiers.add(new ArrayList<>());
      }
      
      //System.out.println("Making Chasm: "+ start.toShortString() +" "+end.toShortString());
      //System.out.println("Volume: "+((maxX-minX+2*extra)*(85)*(maxZ-minZ+2*extra)));
      
      //This can be further optimized by only looping through the surface once and adding the depth directly rather than reiterating for all 15 levels
      for(BlockPos blockPos : BlockPos.iterate(minX - extra, midY, minZ - extra, maxX + extra, midY, maxZ + extra)){
         double dist = weightDist(new Vec3d(start.getX()+.5,start.getY()+.5,start.getZ()+.5),new Vec3d(end.getX()+.5,end.getY()+.5,end.getZ()+.5),new Vec3d(blockPos.getX()+.5,blockPos.getY()+.5,blockPos.getZ()+.5));
         
         if(dist <= 15){
            double maxDepth = Math.min(15+1,2*15/Math.max(0,dist-0.5)+1);
            int depth = (int) ((4.0/12.0)*maxDepth*maxDepth);
            for(int i = -3; i < depth; i++){
               // Check each tier
               for(int dm = 1; dm <= 15; dm++){
                  double tierDepthMax = Math.min(dm+1,2*dm/Math.max(0,dist-0.5)+1);
                  int tierDepth = (int) ((4.0/12.0)*tierDepthMax*tierDepthMax);
                  if(dist <= dm && i < tierDepth){
                     BlockPos pos = blockPos.add(0,-i,0);
   
                     BlockState state = endWorld.getBlockState(pos);
                     float hardness = state.getHardness(endWorld,pos);
                     if(state.getBlock() != Blocks.END_STONE && !(hardness <= 4 && hardness > 0) || state.isAir()) continue;
   
                     tiers.get(dm).add(pos);
                     break;
                  }
               }
            }
         }
      }
   
      for(int i = 1; i <= 15; i++){
         chasmBlocks.put(i,tiers.get(i));
      }
      
      return chasmBlocks;
   }
   
   private double weightDist(Vec3d start, Vec3d end, Vec3d pos){
      final double weight = 0.8;
   
      double px = end.x-start.x;
      double pz = end.z-start.z;
      double temp = (px*px)+(pz*pz);
      double u = ((pos.x - start.x) * px + (pos.z - start.z) * pz) / (temp);
      u = u > 1 ? 1 : (u < 0 ? 0 : u); // Clamp to 1 and 0
      double x = start.x + u * px;
      double z = start.z + u * pz;
      double dx = x - pos.x;
      double dz = z - pos.z;
      double lineDist = Math.sqrt(dx*dx + dz*dz);

      double mx = 0.5*(start.x+end.x);
      double mz = 0.5*(start.z+end.z);
      double midDist = Math.sqrt((pos.x-mx)*(pos.x-mx)+(pos.z-mz)*(pos.z-mz));
      
      return weight*lineDist+(1-weight)*midDist;
   }
   
   private static class Spike{
      public Vec3d start;
      public Vec3d end;
      public double radius;
      
      private Spike(Vec3d s, Vec3d e, double r){
         start = s;
         end = e;
         radius = r;
         
      }
      
      public ArrayList<BlockPos> generate(ServerWorld endWorld, double sizeMod){
         ArrayList<BlockPos> blocks = new ArrayList<>();
         int intervals = 20;
   
         Vec3d dir = end.subtract(start);
         double len = dir.length();
         
         double fullSegLen = sizeMod*len/radius;
         double segLen = fullSegLen / intervals;
         Vec3d sizeEnd = dir.multiply(fullSegLen/len);
         //log("Full Seg Len: "+fullSegLen+" Full Seg End: "+sizeEnd+" SegLen: "+segLen);
         for(int i = 0; i < intervals; i++){
            Vec3d segDir = dir.multiply(segLen/len*(i+1));
            double segR = (intervals-i)*sizeMod/intervals+1;
            Vec3d segEnd = start.add(segDir);
            //log("Making Spike iter "+i+" SegEnd: "+segEnd+" SegR: "+segR);
   
            final double minX = Math.min(start.getX(),end.getX());
            final double minZ = Math.min(start.getZ(),end.getZ());
            final double maxY = Math.max(start.getY(),end.getY());
            final double minY = Math.min(start.getY(),end.getY());
            final double maxX = Math.max(start.getX(),end.getX());
            final double maxZ = Math.max(start.getZ(),end.getZ());
            final double extra = segR+3;
            
            BlockPos corner1 = new BlockPos((int) (minX-extra), (int) (minY-extra), (int) (minZ-extra));
            BlockPos corner2 = new BlockPos((int) (maxX+extra), (int) (maxY+extra), (int) (maxZ+extra));
            for(BlockPos blockPos : BlockPos.iterate(corner1,corner2)){
               
               BlockState state = endWorld.getBlockState(blockPos);
               if(!state.isAir()) continue;
               
               double dist = MiscUtils.distToLine(new Vec3d(blockPos.getX(),blockPos.getY(), blockPos.getZ()),start,segEnd);
               if(dist <= segR){
                  BlockPos copy = new BlockPos(blockPos.getX(),blockPos.getY(),blockPos.getZ());
                  blocks.add(copy);
               }
            }
         }
         
         //log("Returning "+blocks.size()+" blocks");
         return blocks;
      }
   }
   
   public enum DragonLairActionTypes{
      TERRAIN_SHIFT,
      GRAVITY_LAPSE,
      DIMENSION_SHIFT,
      QUAKE,
      STARFALL;
      
      public static DragonLairActionTypes fromLabel(String id){ return DragonLairActionTypes.valueOf(id.toUpperCase()); }
   }
}
