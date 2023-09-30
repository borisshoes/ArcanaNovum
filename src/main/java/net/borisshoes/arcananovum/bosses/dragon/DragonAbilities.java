package net.borisshoes.arcananovum.bosses.dragon;

import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.SpawnPile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.ChargingPlayerPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.borisshoes.arcananovum.utils.SpawnPile.makeSpawnLocations;

public class DragonAbilities {
   
   private final int swoopCD = 45*20;
   private final int gustCD = 30*20;
   private final int overloadCD = 90*20;
   private final int ampCD = 45*20;
   private final int conscriptCD = 240*20;
   private final int bombardCD = 45*20;
   private final int obliterateCD = 120*20;
   private final int resilienceCD = 150*20;
   private final int corruptArcanaCD = 60*20;
   
   private int swoopTicks;
   private int gustTicks;
   private int overloadTicks;
   private int ampTicks;
   private int conscriptTicks;
   private int bombardTicks;
   private int obliterateTicks;
   private int resilienceTicks;
   private int corruptArcanaTicks;
   
   private final MinecraftServer server;
   private final ServerWorld endWorld;
   private final EnderDragonEntity dragon;
   private final PhaseManager manager;
   private final List<EndCrystalEntity> crystals;
   
   public DragonAbilities(MinecraftServer server, ServerWorld endWorld, EnderDragonEntity dragon, List<EndCrystalEntity> crystals){
      this.server = server;
      this.endWorld = endWorld;
      this.dragon = dragon;
      this.manager = dragon.getPhaseManager();
      this.crystals = crystals;
   
      swoopTicks = swoopCD;
      gustTicks = gustCD;
      overloadTicks = overloadCD;
      ampTicks = ampCD;
      conscriptTicks = conscriptCD;
      bombardTicks = bombardCD;
      obliterateTicks = obliterateCD;
      resilienceTicks = resilienceCD;
      corruptArcanaTicks = corruptArcanaCD;
   }
   
   public ArrayList<Pair<DragonAbilityTypes,Integer>> getCooldowns(int phase){
      ArrayList<Pair<DragonAbilityTypes,Integer>> cooldowns = new ArrayList<>();
      
      if(phase == 1){
         cooldowns.add(new Pair<>(DragonAbilityTypes.SWOOPING_CHARGE,swoopCD-swoopTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.WING_GUST,gustCD-gustTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.BOMBARDMENT,bombardCD-bombardTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.CORRUPT_ARCANA,corruptArcanaCD-corruptArcanaTicks));
      }else if(phase == 2){
         cooldowns.add(new Pair<>(DragonAbilityTypes.SWOOPING_CHARGE,swoopCD-swoopTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.WING_GUST,gustCD-gustTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.OVERLOAD_CRYSTALS,overloadCD-overloadTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.GRAVITY_AMP,ampCD-ampTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.BOMBARDMENT,bombardCD-bombardTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.CORRUPT_ARCANA,corruptArcanaCD-corruptArcanaTicks));
      }else if(phase == 3){
         cooldowns.add(new Pair<>(DragonAbilityTypes.SWOOPING_CHARGE,swoopCD-swoopTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.WING_GUST,gustCD-gustTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.CONSCRIPT_ARMY,conscriptCD-conscriptTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.BOMBARDMENT,bombardCD-bombardTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.OBLITERATE_TOWER,obliterateCD-obliterateTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.DRACONIC_RESILIENCE,resilienceCD-resilienceTicks));
         cooldowns.add(new Pair<>(DragonAbilityTypes.CORRUPT_ARCANA,corruptArcanaCD-corruptArcanaTicks));
      }
      return cooldowns;
   }
   
   public void tick(){
      if(swoopTicks < swoopCD) swoopTicks++;
      if(gustTicks < gustCD) gustTicks++;
      if(overloadTicks < overloadCD) overloadTicks++;
      if(ampTicks < ampCD) ampTicks++;
      if(conscriptTicks < conscriptCD) conscriptTicks++;
      if(bombardTicks < bombardCD) bombardTicks++;
      if(obliterateTicks < obliterateCD) obliterateTicks++;
      if(resilienceTicks < resilienceCD) resilienceTicks++;
      if(corruptArcanaTicks < corruptArcanaCD) corruptArcanaTicks++;
      
      if(overloadTicks < 400){
         if(overloadTicks % 2 == 0){
            for(EndCrystalEntity crystal : crystals){
               if(crystal != null && crystal.isAlive()){
                  endWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,crystal.getX(),crystal.getY(),crystal.getZ(),25,5,5,5,0);
               }
            }
         }
         if(overloadTicks % 10 == 0){
            for(EndCrystalEntity crystal : crystals){
               if(crystal != null && crystal.isAlive()){
                  List<ServerPlayerEntity> nearbyPlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(crystal.getPos()) <= 5*5);
                  for(ServerPlayerEntity player : nearbyPlayers){
                     if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
                     player.damage(new DamageSource(endWorld.getDamageSources().magic().getTypeRegistryEntry(), this.dragon,this.dragon),2f);
                  }
               }
            }
         }
      }
      
      if(resilienceTicks < 3*20 && resilienceTicks % 20 == 0){
         dragon.heal(dragon.getMaxHealth() / 20);
      }
      
      if(bombardTicks < 12 * 5 && bombardTicks % 5 == 0){
         ServerPlayerEntity randomPlayer;
         int attempts = 0;
         do{
            randomPlayer = endWorld.getRandomAlivePlayer();
            if(randomPlayer == null){ break; }
            attempts++;
         }while((randomPlayer.squaredDistanceTo(dragon) > 75*75 || randomPlayer.isCreative() || randomPlayer.isSpectator()) && attempts < 25);
         
         if(randomPlayer != null){
            Vec3d vec3d3 = this.dragon.getRotationVec(1.0F);
            double l = this.dragon.head.getX() - vec3d3.x * 1.0;
            double m = this.dragon.head.getBodyY(0.5) + 0.5;
            double n = this.dragon.head.getZ() - vec3d3.z * 1.0;
            double o = randomPlayer.getX() - l;
            double p = randomPlayer.getBodyY(0.5) - m;
            double q = randomPlayer.getZ() - n;
            if (!this.dragon.isSilent()) {
               this.dragon.getWorld().syncWorldEvent((PlayerEntity)null, 1017, this.dragon.getBlockPos(), 0);
            }
   
            DragonFireballEntity dragonFireballEntity = new DragonFireballEntity(this.dragon.getWorld(), this.dragon, o, p, q);
            dragonFireballEntity.refreshPositionAndAngles(l, m, n, 0.0F, 0.0F);
            this.dragon.getWorld().spawnEntity(dragonFireballEntity);
         }
      }
      
      if(corruptArcanaTicks < 200 && corruptArcanaTicks % 20 == 0){
         List<ServerPlayerEntity> nearbyPlayers300 = endWorld.getPlayers(p -> p.squaredDistanceTo(new Vec3d(0,100,0)) <= 300*300);
         for(ServerPlayerEntity player : nearbyPlayers300){
            float damage = MagicItemUtils.getUsedConcentration(player)/8f * (player.getMaxHealth()/20f);
            if(player.isCreative() || player.isSpectator() || damage < 0.1) continue; // Skip creative and spectator players
            
            player.damage(new DamageSource(endWorld.getDamageSources().magic().getTypeRegistryEntry(), this.dragon,this.dragon),damage);
            player.sendMessage(Text.literal("Your Magic Items surge with corrupted Arcana!").formatted(Formatting.DARK_PURPLE,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,2,.1f);
         }
         
      }
   }
   
   public void resetCooldowns(){
      swoopTicks = swoopCD;
      gustTicks = gustCD;
      overloadTicks = overloadCD;
      ampTicks = ampCD;
      conscriptTicks = conscriptCD;
      bombardTicks = bombardCD;
      obliterateTicks = obliterateCD;
      resilienceTicks = resilienceCD;
      corruptArcanaTicks = corruptArcanaCD;
   }
   
   public boolean doAbility(int phase){
      PhaseType phaseType = manager.getCurrent().getType();
      HashMap<DragonAbilityTypes,Integer> actions = new HashMap<>(); // Action, weight
      List<ServerPlayerEntity> nearbyPlayers300 = endWorld.getPlayers(p -> p.squaredDistanceTo(new Vec3d(0,100,0)) <= 300*300);
   
      if(phase == 1){
         if(phaseType == PhaseType.HOLDING_PATTERN){
            actions.put(DragonAbilityTypes.SWOOPING_CHARGE,2);
         }
         if(phaseType == PhaseType.SITTING_ATTACKING || phaseType == PhaseType.SITTING_SCANNING){
            actions.put(DragonAbilityTypes.WING_GUST,5);
         }
         actions.put(DragonAbilityTypes.BOMBARDMENT,5);
         actions.put(DragonAbilityTypes.CORRUPT_ARCANA,5);
      }else if(phase == 2){
         if(phaseType == PhaseType.HOLDING_PATTERN){
            actions.put(DragonAbilityTypes.SWOOPING_CHARGE,2);
         }
         if(phaseType == PhaseType.SITTING_ATTACKING || phaseType == PhaseType.SITTING_SCANNING){
            actions.put(DragonAbilityTypes.WING_GUST,5);
         }
         actions.put(DragonAbilityTypes.BOMBARDMENT,8);
         actions.put(DragonAbilityTypes.CORRUPT_ARCANA,5);
      
         int aerialCount = 0;
         for(ServerPlayerEntity player : nearbyPlayers300){
            if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
            if(player.getStatusEffect(StatusEffects.LEVITATION) != null || player.getStatusEffect(StatusEffects.JUMP_BOOST) != null){
               aerialCount++;
            }
         }
         if(aerialCount != 0) actions.put(DragonAbilityTypes.GRAVITY_AMP,aerialCount*5);
      
         int crystalPlayerCount = 0;
         for(EndCrystalEntity crystal : crystals){
            if(crystal != null && crystal.isAlive()){
               List<ServerPlayerEntity> nearbyPlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(crystal.getPos()) <= 6*6);
               crystalPlayerCount += nearbyPlayers.size();
            }
         }
         if(crystalPlayerCount != 0) actions.put(DragonAbilityTypes.OVERLOAD_CRYSTALS,crystalPlayerCount*3);
      }else if(phase == 3){
         if(phaseType == PhaseType.HOLDING_PATTERN){
            actions.put(DragonAbilityTypes.SWOOPING_CHARGE,2);
         }
         if(phaseType == PhaseType.SITTING_ATTACKING || phaseType == PhaseType.SITTING_SCANNING){
            actions.put(DragonAbilityTypes.WING_GUST,5);
         }
         actions.put(DragonAbilityTypes.BOMBARDMENT,8);
         actions.put(DragonAbilityTypes.CONSCRIPT_ARMY,5);
         actions.put(DragonAbilityTypes.CORRUPT_ARCANA,5);
         
         List<DragonBossFight.ReclaimState> reclaimStates = DragonBossFight.getReclaimStates();
         if(reclaimStates != null){
            int count = 0;
            for(DragonBossFight.ReclaimState state : reclaimStates){
               if(state.getState() == 2) count++;
            }
            actions.put(DragonAbilityTypes.OBLITERATE_TOWER,10*count);
         }
      
         int healthCount = 30 - (int)(30 * dragon.getHealth() / dragon.getMaxHealth());
         if(healthCount != 0) actions.put(DragonAbilityTypes.DRACONIC_RESILIENCE,healthCount);
      }
      
      DragonAbilityTypes ability = rollAbility(actions);
      if(ability == null){
         return false;
      }else if(ability == DragonAbilityTypes.SWOOPING_CHARGE){
         manager.setPhase(PhaseType.CHARGING_PLAYER);
         PlayerEntity player = endWorld.getClosestPlayer(dragon,75);
         if(player != null && manager.getCurrent() instanceof ChargingPlayerPhase charge){
            charge.setPathTarget(player.getPos());
            swoopTicks = 0;
            return true;
         }
         swoopTicks = swoopCD/2;
         return false;
      }else if(ability == DragonAbilityTypes.WING_GUST){
         Vec3d pos = dragon.getPos();
         SoundUtils.playSound(endWorld,dragon.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.HOSTILE,1f,0.5f);
         List<ServerPlayerEntity> nearbyPlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(pos) <= 10*10);
         for(ServerPlayerEntity player : nearbyPlayers){
            if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
            BlockPos target = BlockPos.ofFloored(pos.getX()+.5,pos.getY()-1,pos.getZ()+.5);
            BlockPos playerPos = player.getBlockPos();
            Vec3d vec = new Vec3d(target.getX()-playerPos.getX(),0,target.getZ()-playerPos.getZ());
            vec = vec.normalize().multiply(3);
   
            player.setVelocity(-vec.x,1,-vec.z);
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
   
            player.sendMessage(Text.literal("The Dragon's Wings Knock You Away!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC),true);
         }
         gustTicks = 0;
      }else if(ability == DragonAbilityTypes.OVERLOAD_CRYSTALS){
         overloadTicks = 0;
         DragonDialog.announce(DragonDialog.Announcements.ABILITY_OVERLOAD_CRYSTALS,server,null);
      }else if(ability == DragonAbilityTypes.GRAVITY_AMP){
         for(ServerPlayerEntity player : nearbyPlayers300){
            if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
            Vec3d vec = player.getVelocity();
      
            player.setVelocity(vec.x,-3,vec.z);
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
            int dist = player.getBlockY()-SpawnPile.getSurfaceY(endWorld,player.getBlockY(),player.getBlockX(),player.getBlockZ());
            player.damage(endWorld.getDamageSources().fall(),(dist*0.25f));
         }
         
         ampTicks = 0;
         DragonDialog.announce(DragonDialog.Announcements.ABILITY_GRAVITY_AMP,server,null);
      }else if(ability == DragonAbilityTypes.CONSCRIPT_ARMY){
         EndermanEntity[] goons = new EndermanEntity[25];
         ArrayList<BlockPos> poses = makeSpawnLocations(goons.length,50,endWorld);
         for(int i=0;i<goons.length;i++){
            goons[i] = new EndermanEntity(EntityType.ENDERMAN, endWorld);
            goons[i].getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(MathHelper.clamp(20 + 4*nearbyPlayers300.size(),40,100));
            goons[i].setHealth(MathHelper.clamp(20 + 4*nearbyPlayers300.size(),40,100));
            goons[i].getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(8f);
            BlockPos pos = poses.get(i);
            goons[i].setPos(pos.getX(),pos.getY(),pos.getZ());
      
            endWorld.spawnEntityAndPassengers(goons[i]);
         }
         
         List<EndermanEntity> endermen = endWorld.getEntitiesByType(EntityType.ENDERMAN, new Box(new BlockPos(-300,25,-300), new BlockPos(300,115,300)), e -> true);
   
         for(EndermanEntity enderman : endermen){
            PlayerEntity closestPlayer = endWorld.getClosestPlayer(enderman,30);
            if(closestPlayer != null){
               if(closestPlayer.isCreative() || closestPlayer.isSpectator()) continue; // Skip creative and spectator players
               enderman.setProvoked();
               enderman.setTarget(closestPlayer);
               enderman.setAngryAt(closestPlayer.getUuid());
               enderman.setAngerTime(1200);
            }else{
               PlayerEntity randomPlayer = endWorld.getRandomAlivePlayer();
               if(randomPlayer != null){
                  if(randomPlayer.isCreative() || randomPlayer.isSpectator()) continue; // Skip creative and spectator players
                  enderman.setProvoked();
                  enderman.setTarget(randomPlayer);
                  enderman.setAngryAt(randomPlayer.getUuid());
                  enderman.setAngerTime(1200);
               }
            }
         }
         conscriptTicks = 0;
         DragonDialog.announce(DragonDialog.Announcements.ABILITY_CONSCRIPT_ARMY,server,null);
      }else if(ability == DragonAbilityTypes.BOMBARDMENT){
         bombardTicks = 0;
         DragonDialog.announce(DragonDialog.Announcements.ABILITY_BOMBARDMENT,server,null);
      }else if(ability == DragonAbilityTypes.CORRUPT_ARCANA){
         corruptArcanaTicks = 0;
         DragonDialog.announce(DragonDialog.Announcements.ABILITY_CORRUPT_ARCANA,server,null);
         for(ServerPlayerEntity player : nearbyPlayers300){
            if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
            
            PlayerInventory playerInv = player.getInventory();
            ItemCooldownManager manager = player.getItemCooldownManager();
            for(int i=0; i<playerInv.size();i++){
               ItemStack item = playerInv.getStack(i);
               if(item.isEmpty()){
                  continue;
               }
               if(MagicItemUtils.isMagic(item) && !manager.isCoolingDown(item.getItem())){
                  manager.set(item.getItem(),200);
               }
            }
         }
      }else if(ability == DragonAbilityTypes.OBLITERATE_TOWER){
         List<DragonBossFight.ReclaimState> reclaimStates = DragonBossFight.getReclaimStates();
         if(reclaimStates != null){
            for(DragonBossFight.ReclaimState state : reclaimStates){
               if(state.getState() == 2){
                  state.destroyTower();
               }
            }
         }
         obliterateTicks = 0;
         DragonDialog.announce(DragonDialog.Announcements.ABILITY_OBLITERATE_TOWER,server,null);
      }else if(ability == DragonAbilityTypes.DRACONIC_RESILIENCE){
         StatusEffectInstance res = new StatusEffectInstance(StatusEffects.RESISTANCE, 20*90, 2, false, false, true);
         dragon.addStatusEffect(res);
   
         resilienceTicks = 0;
         DragonDialog.announce(DragonDialog.Announcements.ABILITY_DRACONIC_RESILIENCE,server,null);
      }
      return true;
   }
   
   public DragonAbilityTypes rollAbility(HashMap<DragonAbilityTypes,Integer> actions){
      ArrayList<DragonAbilityTypes> abilityPool = new ArrayList<>();
   
      for(Map.Entry<DragonAbilityTypes, Integer> action : actions.entrySet()){
         DragonAbilityTypes act = action.getKey();
         int prio = action.getValue();
         if(!onCD(act)){
            for(int i = 0; i < prio; i++){
               abilityPool.add(act);
            }
         }
      }
      if(abilityPool.size() == 0) return null;
      return abilityPool.get((int)(Math.random() * abilityPool.size()));
   }
   
   private boolean onCD(DragonAbilityTypes action){
      if(action == DragonAbilityTypes.SWOOPING_CHARGE){
         return swoopTicks < swoopCD;
      }else if(action == DragonAbilityTypes.WING_GUST){
         return gustTicks < gustCD;
      }else if(action == DragonAbilityTypes.OVERLOAD_CRYSTALS){
         return overloadTicks < overloadCD;
      }else if(action == DragonAbilityTypes.GRAVITY_AMP){
         return ampTicks < ampCD;
      }else if(action == DragonAbilityTypes.CONSCRIPT_ARMY){
         return conscriptTicks < conscriptCD;
      }else if(action == DragonAbilityTypes.BOMBARDMENT){
         return bombardTicks < bombardCD;
      }else if(action == DragonAbilityTypes.CORRUPT_ARCANA){
         return corruptArcanaTicks < corruptArcanaCD;
      }else if(action == DragonAbilityTypes.OBLITERATE_TOWER){
         return obliterateTicks < obliterateCD;
      }else if(action == DragonAbilityTypes.DRACONIC_RESILIENCE){
         return resilienceTicks < resilienceCD;
      }else{
         return true;
      }
   }
   
   public enum DragonAbilityTypes{
      SWOOPING_CHARGE,
      WING_GUST,
      OVERLOAD_CRYSTALS,
      GRAVITY_AMP,
      CONSCRIPT_ARMY,
      BOMBARDMENT,
      OBLITERATE_TOWER,
      DRACONIC_RESILIENCE,
      CORRUPT_ARCANA;
      
      public static DragonAbilityTypes fromLabel(String id){ return DragonAbilityTypes.valueOf(id.toUpperCase()); }
   }
}
