package net.borisshoes.arcananovum.bosses.dragon;

import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.SpawnPile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static net.borisshoes.borislib.utils.SpawnPile.makeSpawnLocations;

public class DragonAbilities {
   
   private final int swoopCD = 45*20;
   private final int gustCD = 30*20;
   private final int overloadCD = 90*20;
   private final int ampCD = 45*20;
   private final int conscriptCD = 240*20;
   private final int bombardCD = 45*20;
   private final int obliterateCD = 120*20;
   private final int resilienceCD = 250*20;
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
   private final ServerLevel endWorld;
   private final EnderDragon dragon;
   private final EnderDragonPhaseManager manager;
   private final List<EndCrystal> crystals;
   
   public DragonAbilities(MinecraftServer server, ServerLevel endWorld, EnderDragon dragon, List<EndCrystal> crystals){
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
   
   public ArrayList<Tuple<DragonAbilityTypes,Integer>> getCooldowns(int phase){
      ArrayList<Tuple<DragonAbilityTypes,Integer>> cooldowns = new ArrayList<>();
      
      if(phase == 1){
         cooldowns.add(new Tuple<>(DragonAbilityTypes.SWOOPING_CHARGE,swoopCD-swoopTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.WING_GUST,gustCD-gustTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.BOMBARDMENT,bombardCD-bombardTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.CORRUPT_ARCANA,corruptArcanaCD-corruptArcanaTicks));
      }else if(phase == 2){
         cooldowns.add(new Tuple<>(DragonAbilityTypes.SWOOPING_CHARGE,swoopCD-swoopTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.WING_GUST,gustCD-gustTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.OVERLOAD_CRYSTALS,overloadCD-overloadTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.GRAVITY_AMP,ampCD-ampTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.BOMBARDMENT,bombardCD-bombardTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.CORRUPT_ARCANA,corruptArcanaCD-corruptArcanaTicks));
      }else if(phase == 3){
         cooldowns.add(new Tuple<>(DragonAbilityTypes.SWOOPING_CHARGE,swoopCD-swoopTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.WING_GUST,gustCD-gustTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.CONSCRIPT_ARMY,conscriptCD-conscriptTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.BOMBARDMENT,bombardCD-bombardTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.OBLITERATE_TOWER,obliterateCD-obliterateTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.DRACONIC_RESILIENCE,resilienceCD-resilienceTicks));
         cooldowns.add(new Tuple<>(DragonAbilityTypes.CORRUPT_ARCANA,corruptArcanaCD-corruptArcanaTicks));
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
            for(EndCrystal crystal : crystals){
               if(crystal != null && crystal.isAlive()){
                  endWorld.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,crystal.getX(),crystal.getY(),crystal.getZ(),25,5,5,5,0);
               }
            }
         }
         if(overloadTicks % 10 == 0){
            for(EndCrystal crystal : crystals){
               if(crystal != null && crystal.isAlive()){
                  List<ServerPlayer> nearbyPlayers = endWorld.getPlayers(p -> p.distanceToSqr(crystal.position()) <= 5*5);
                  for(ServerPlayer player : nearbyPlayers){
                     if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
                     player.hurtServer(endWorld, new DamageSource(endWorld.damageSources().magic().typeHolder(), this.dragon,this.dragon),2f);
                  }
               }
            }
         }
      }
      
      if(resilienceTicks < 5*20 && resilienceTicks % 10 == 0){
         dragon.heal(dragon.getMaxHealth() / 100.0f);
      }
      
      if(bombardTicks < 12 * 5 && bombardTicks % 5 == 0){
         ServerPlayer randomPlayer;
         int attempts = 0;
         do{
            randomPlayer = endWorld.getRandomPlayer();
            if(randomPlayer == null){ break; }
            attempts++;
         }while((randomPlayer.distanceToSqr(dragon) > 75*75 || randomPlayer.isCreative() || randomPlayer.isSpectator()) && attempts < 25);
         
         if(randomPlayer != null){
            Vec3 vec3d3 = this.dragon.getViewVector(1.0F);
            double l = this.dragon.head.getX() - vec3d3.x * 1.0;
            double m = this.dragon.head.getY(0.5) + 0.5;
            double n = this.dragon.head.getZ() - vec3d3.z * 1.0;
            double o = randomPlayer.getX() - l;
            double p = randomPlayer.getY(0.5) - m;
            double q = randomPlayer.getZ() - n;
            if(!this.dragon.isSilent()){
               this.dragon.level().levelEvent((Player)null, 1017, this.dragon.blockPosition(), 0);
            }
            
            DragonFireball dragonFireballEntity = new DragonFireball(this.dragon.level(), this.dragon, new Vec3(o,p,q));
            dragonFireballEntity.snapTo(l, m, n, 0.0F, 0.0F);
            this.dragon.level().addFreshEntity(dragonFireballEntity);
         }
      }
      
      if(corruptArcanaTicks < 200 && corruptArcanaTicks % 20 == 0){
         List<ServerPlayer> nearbyPlayers300 = endWorld.getPlayers(p -> p.distanceToSqr(new Vec3(0,100,0)) <= 300*300);
         for(ServerPlayer player : nearbyPlayers300){
            float damage = ArcanaItemUtils.getUsedConcentration(player)/12f * (player.getMaxHealth()/20f);
            if(player.isCreative() || player.isSpectator() || damage < 0.1) continue; // Skip creative and spectator players
            
            player.hurtServer(endWorld, new DamageSource(endWorld.damageSources().magic().typeHolder(), this.dragon,this.dragon),damage);
            player.sendSystemMessage(Component.literal("Your Arcana Items surge with corrupted Arcana!").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.ILLUSIONER_CAST_SPELL,2,.1f);
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
      EnderDragonPhase phaseType = manager.getCurrentPhase().getPhase();
      HashMap<DragonAbilityTypes,Integer> actions = new HashMap<>(); // Action, weight
      List<ServerPlayer> nearbyPlayers300 = endWorld.getPlayers(p -> p.distanceToSqr(new Vec3(0,100,0)) <= 300*300);
   
      if(phase == 1){
         if(phaseType == EnderDragonPhase.HOLDING_PATTERN){
            actions.put(DragonAbilityTypes.SWOOPING_CHARGE,2);
         }
         if(phaseType == EnderDragonPhase.SITTING_ATTACKING || phaseType == EnderDragonPhase.SITTING_SCANNING){
            actions.put(DragonAbilityTypes.WING_GUST,5);
         }
         actions.put(DragonAbilityTypes.BOMBARDMENT,5);
         actions.put(DragonAbilityTypes.CORRUPT_ARCANA,5);
      }else if(phase == 2){
         if(phaseType == EnderDragonPhase.HOLDING_PATTERN){
            actions.put(DragonAbilityTypes.SWOOPING_CHARGE,2);
         }
         if(phaseType == EnderDragonPhase.SITTING_ATTACKING || phaseType == EnderDragonPhase.SITTING_SCANNING){
            actions.put(DragonAbilityTypes.WING_GUST,5);
         }
         actions.put(DragonAbilityTypes.BOMBARDMENT,8);
         actions.put(DragonAbilityTypes.CORRUPT_ARCANA,5);
      
         int aerialCount = 0;
         for(ServerPlayer player : nearbyPlayers300){
            if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
            if(player.getEffect(MobEffects.LEVITATION) != null || player.getEffect(MobEffects.JUMP_BOOST) != null){
               aerialCount++;
            }
         }
         if(aerialCount != 0) actions.put(DragonAbilityTypes.GRAVITY_AMP,aerialCount*5);
      
         int crystalPlayerCount = 0;
         for(EndCrystal crystal : crystals){
            if(crystal != null && crystal.isAlive()){
               List<ServerPlayer> nearbyPlayers = endWorld.getPlayers(p -> p.distanceToSqr(crystal.position()) <= 6*6);
               crystalPlayerCount += nearbyPlayers.size();
            }
         }
         if(crystalPlayerCount != 0) actions.put(DragonAbilityTypes.OVERLOAD_CRYSTALS,crystalPlayerCount*3);
      }else if(phase == 3){
         if(phaseType == EnderDragonPhase.HOLDING_PATTERN){
            actions.put(DragonAbilityTypes.SWOOPING_CHARGE,2);
         }
         if(phaseType == EnderDragonPhase.SITTING_ATTACKING || phaseType == EnderDragonPhase.SITTING_SCANNING){
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
            actions.put(DragonAbilityTypes.OBLITERATE_TOWER,20*count);
         }
      
         int healthCount = 30 - (int)(30 * dragon.getHealth() / dragon.getMaxHealth());
         if(healthCount != 0) actions.put(DragonAbilityTypes.DRACONIC_RESILIENCE,healthCount);
      }
      
      DragonAbilityTypes ability = rollAbility(actions);
      if(ability == null){
         return false;
      }else if(ability == DragonAbilityTypes.SWOOPING_CHARGE){
         manager.setPhase(EnderDragonPhase.CHARGING_PLAYER);
         Player player = endWorld.getNearestPlayer(dragon,75);
         if(player != null && manager.getCurrentPhase() instanceof DragonChargePlayerPhase charge){
            charge.setTarget(player.position());
            swoopTicks = 0;
            return true;
         }
         swoopTicks = swoopCD/2;
         return false;
      }else if(ability == DragonAbilityTypes.WING_GUST){
         Vec3 pos = dragon.position();
         SoundUtils.playSound(endWorld,dragon.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE,1f,0.5f);
         List<ServerPlayer> nearbyPlayers = endWorld.getPlayers(p -> p.distanceToSqr(pos) <= 10*10);
         for(ServerPlayer player : nearbyPlayers){
            if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
            BlockPos target = BlockPos.containing(pos.x()+.5,pos.y()-1,pos.z()+.5);
            BlockPos playerPos = player.blockPosition();
            Vec3 vec = new Vec3(target.getX()-playerPos.getX(),0,target.getZ()-playerPos.getZ());
            vec = vec.normalize().scale(3);
   
            player.setDeltaMovement(-vec.x,1,-vec.z);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
   
            player.sendSystemMessage(Component.literal("The Dragon's Wings Knock You Away!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC),true);
         }
         gustTicks = 0;
      }else if(ability == DragonAbilityTypes.OVERLOAD_CRYSTALS){
         overloadTicks = 0;
         DragonDialog.announce(DragonDialog.Announcements.ABILITY_OVERLOAD_CRYSTALS,server,null);
      }else if(ability == DragonAbilityTypes.GRAVITY_AMP){
         for(ServerPlayer player : nearbyPlayers300){
            if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
            Vec3 vec = player.getDeltaMovement();
      
            player.setDeltaMovement(vec.x,-3,vec.z);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
            int dist = player.getBlockY()-SpawnPile.getSurfaceY(endWorld,player.getBlockY(),player.getBlockX(),player.getBlockZ());
            player.hurtServer(endWorld, endWorld.damageSources().fall(),(dist*0.25f));
         }
         
         ampTicks = 0;
         DragonDialog.announce(DragonDialog.Announcements.ABILITY_GRAVITY_AMP,server,null);
      }else if(ability == DragonAbilityTypes.CONSCRIPT_ARMY){
         EnderMan[] goons = new EnderMan[25];
         ArrayList<BlockPos> poses = makeSpawnLocations(goons.length,50,endWorld);
         for(int i=0;i<goons.length;i++){
            goons[i] = new EnderMan(EntityType.ENDERMAN, endWorld);
            goons[i].getAttribute(Attributes.MAX_HEALTH).setBaseValue(Mth.clamp(20 + 4*nearbyPlayers300.size(),40,100));
            goons[i].setHealth(Mth.clamp(20 + 4*nearbyPlayers300.size(),40,100));
            goons[i].getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8f);
            BlockPos pos = poses.get(i);
            goons[i].setPosRaw(pos.getX(),pos.getY(),pos.getZ());
      
            endWorld.addFreshEntityWithPassengers(goons[i]);
         }
         
         List<EnderMan> endermen = endWorld.getEntities(EntityType.ENDERMAN, new AABB(new BlockPos(-300,25,-300).getCenter(), new BlockPos(300,115,300).getCenter()), e -> true);
   
         for(EnderMan enderman : endermen){
            Player closestPlayer = endWorld.getNearestPlayer(enderman,30);
            if(closestPlayer != null){
               if(closestPlayer.isCreative() || closestPlayer.isSpectator()) continue; // Skip creative and spectator players
               enderman.setBeingStaredAt();
               enderman.setTarget(closestPlayer);
               enderman.setPersistentAngerTarget(EntityReference.of(closestPlayer));
               enderman.setPersistentAngerEndTime(enderman.tickCount + 1200);
            }else{
               Player randomPlayer = endWorld.getRandomPlayer();
               if(randomPlayer != null){
                  if(randomPlayer.isCreative() || randomPlayer.isSpectator()) continue; // Skip creative and spectator players
                  enderman.setBeingStaredAt();
                  enderman.setTarget(randomPlayer);
                  enderman.setPersistentAngerTarget(EntityReference.of(randomPlayer));
                  enderman.setPersistentAngerEndTime(enderman.tickCount + 1200);
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
         for(ServerPlayer player : nearbyPlayers300){
            if(player.isCreative() || player.isSpectator()) continue; // Skip creative and spectator players
            
            Inventory playerInv = player.getInventory();
            ItemCooldowns manager = player.getCooldowns();
            for(int i = 0; i<playerInv.getContainerSize(); i++){
               ItemStack item = playerInv.getItem(i);
               if(item.isEmpty()){
                  continue;
               }
               if(ArcanaItemUtils.isArcane(item) && !manager.isOnCooldown(item)){
                  manager.addCooldown(item,200);
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
      if(abilityPool.isEmpty()) return null;
      return abilityPool.get(endWorld.getRandom().nextInt(abilityPool.size()));
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
      
      public static DragonAbilityTypes fromLabel(String id){ return DragonAbilityTypes.valueOf(id.toUpperCase(Locale.ROOT)); }
   }
}
