package net.borisshoes.arcananovum.bosses.nulconstruct;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.SpawnPile;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.*;

import static net.borisshoes.arcananovum.Arcananovum.log;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_ENTITY_LIST;

public class NulConstructFight {
   
   public static boolean tickConstruct(WitherEntity construct, MagicEntity magicEntity){
      try{
         NbtCompound magicData = magicEntity.getData();
         NbtCompound abilitiesTag = magicData.getCompound("abilities");
         MinecraftServer server = construct.getServer();
         ServerPlayerEntity summoner = server.getPlayerManager().getPlayer(UUID.fromString(magicData.getString("summonerId")));
         if(summoner == null) log(2,"Null Summoner: "+magicData.getString("summonerId"));
         if(summoner == null || summoner.isDead()){
            deconstruct(construct,magicEntity,summoner);
            return true;
         }
         
         NbtCompound activeAbilitiesTag = magicData.getCompound("activeAbilities");
         Iterator<String> iter = activeAbilitiesTag.getKeys().iterator();
         while(iter.hasNext()){
            String key = iter.next();
            int tick = activeAbilitiesTag.getInt(key);
            if(tick > 0){
               activeAbilitiesTag.putInt(key,--tick);
               tickAbility(construct,magicEntity,key,tick);
            }else{
               iter.remove();
            }
         }
      
         float prevHP = magicData.getFloat("prevHP");
         float curHP = construct.getHealth();
         int castCD = magicData.getInt("castCD");
         if(castCD > 0) magicData.putInt("castCD",--castCD);
         DamageSource recentDamage = construct.getRecentDamageSource();
         List<ServerPlayerEntity> nearbyPlayers = construct.getWorld().getNonSpectatingEntities(ServerPlayerEntity.class, construct.getBoundingBox().expand(25.0));
         boolean conversionActive = activeAbilitiesTag.contains("dark_conversion");
      
         HashMap<String,Integer> availableAbilities = new HashMap<>();
         for(String key : abilitiesTag.getKeys()){
            NbtCompound ability = abilitiesTag.getCompound(key);
            int cd = ability.getInt("cooldown");
            if(cd > 0) ability.putInt("cooldown",--cd);
            if(cd == 0){
               int weight = ability.getInt("weight");
               if(key.equals("reflective_armor")){
                  if(curHP < construct.getMaxHealth()/2.0){
                     weight += 5;
                  }else{
                     weight = 0;
                  }
               }else if(key.equals("dark_conversion")){
                  weight += 4 -(int)(5*curHP/construct.getMaxHealth());
                  if(conversionActive){
                     weight = 0;
                  }
               }else if(key.equals("withering_ray") || key.equals("curse_of_decay")){
                  if(nearbyPlayers.size() == 0) weight = 0;
               }else if(key.equals("necrotic_shroud")){
                  if(conversionActive){
                     weight = 0;
                  }
               }else if(key.equals("summon_goons")){
                  if(conversionActive){
                     weight = 0;
                  }
               }
               if(weight != 0) availableAbilities.put(key,weight);
            }
         }
   
         if((int)(curHP*4/(construct.getMaxHealth())) < (int)(prevHP*4/(construct.getMaxHealth())) && availableAbilities.containsKey("reflexive_blast")){
            castAbility(construct,magicEntity,"reflexive_blast");
         }
   
         boolean canCastNewAbility = construct.getInvulnerableTimer() <= 0 && castCD <= 0;
         if(canCastNewAbility){
            if(recentDamage != null && recentDamage.name.equals("inWall") && availableAbilities.containsKey("necrotic_shroud")){
               castAbility(construct,magicEntity,"necrotic_shroud");
            }else if(availableAbilities.size() > 0){
               castAbility(construct,magicEntity,getWeightedResult(availableAbilities));
            }
         }
      
         magicData.putFloat("prevHP",curHP);
      }catch(Exception e){
         e.printStackTrace();
      }
      return false;
   }
   
   
   
   private static void tickAbility(WitherEntity construct, MagicEntity magicEntity, String ability, int tick){
      NbtCompound magicData = magicEntity.getData();
      NbtCompound activeAbilitiesTag = magicData.getCompound("activeAbilities");
      boolean conversionActive = activeAbilitiesTag.contains("dark_conversion");
      ServerWorld world = (ServerWorld) construct.getEntityWorld();
      Vec3d pos = construct.getPos();
   
      if(ability.equals("curse_of_decay")){
         if(tick % 8 == 0){
            int range = 35;
            List<Entity> entities = world.getOtherEntities(construct,construct.getBoundingBox().expand(50), e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 1.5*range*range && (e instanceof LivingEntity));
            for(Entity entity1 : entities){
               if(!(entity1 instanceof LivingEntity living)) continue;;
               float dmg = living.getMaxHealth() / 10.0f;
               living.damage(DamageSource.mob(construct).setBypassesArmor().setOutOfWorld(),dmg);
               if(conversionActive) conversionHeal(construct,dmg*0.5f)
                     ;
               StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS,10,1,false,true,true);
               StatusEffectInstance weak = new StatusEffectInstance(StatusEffects.WEAKNESS,10,1,false,true,true);
               living.addStatusEffect(slow);
               living.addStatusEffect(weak);
               ParticleEffectUtils.nulConstructCurseOfDecay(world,entity1.getPos());
            }
         }
      }else if(ability.equals("withering_ray")){
         if(tick % 4 == 0){
            construct.lookAtEntity(world.getClosestPlayer(construct,40),360,360);
            Vec3d startPos = construct.getEyePos();
            Vec3d view = construct.getRotationVecClient();
            Vec3d rayEnd = startPos.add(view.multiply(50));
            BlockHitResult raycast = world.raycast(new RaycastContext(startPos,rayEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,construct));
            Box box = new Box(startPos,raycast.getPos());
            box = box.expand(2);
   
            // Hitscan check
            List<Entity> hits = world.getOtherEntities(construct, box, (e)-> e instanceof LivingEntity && !e.isSpectator() && inRange(e,startPos,raycast.getPos()));
            float damage = 2f;
   
            for(Entity hit : hits){
               if(hit instanceof LivingEntity living){
                  living.damage(DamageSource.mob(construct).setBypassesArmor().setOutOfWorld(), damage);
                  StatusEffectInstance wither = new StatusEffectInstance(StatusEffects.WITHER, 40, 1, false, true, true);
                  living.addStatusEffect(wither);
                  if(conversionActive) conversionHeal(construct, damage);
               }
            }
   
            ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0x36332b).toVector3f(),1.5f);
            int intervals = (int)(startPos.subtract(raycast.getPos()).length() * 4);
            ParticleEffectUtils.line(world,null,startPos,raycast.getPos(),dust,intervals,1,0.08,0);
         }
      }else if(ability.equals("dark_conversion")){
         ParticleEffectUtils.nulConstructDarkConversion(world,construct.getPos());
      }else if(ability.equals("reflective_armor")){
         ParticleEffectUtils.nulConstructReflectiveArmor(world,construct.getPos());
      }
   }
   
   private static void castAbility(WitherEntity construct, MagicEntity magicEntity, String ability){
      if(ability.isEmpty()) return;
      NbtCompound magicData = magicEntity.getData();
      NbtCompound abilitiesTag = magicData.getCompound("abilities");
      NbtCompound activeAbilitiesTag = magicData.getCompound("activeAbilities");
      NbtCompound abilityTag = abilitiesTag.getCompound(ability);
      boolean conversionActive = activeAbilitiesTag.contains("dark_conversion");
      ServerWorld world = (ServerWorld) construct.getWorld();
      Vec3d pos = construct.getPos();
      
      //System.out.println("Casting: "+ability);
      magicData.putInt("castCD",100);
      
      if(ability.equals("reflexive_blast")){
         abilityTag.putInt("cooldown",100);
         
         int range = 25;
         List<Entity> entities = world.getOtherEntities(construct,construct.getBoundingBox().expand(30), e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 1.5*range*range && (e instanceof LivingEntity));
         for(Entity entity1 : entities){
            Vec3d diff = entity1.getPos().subtract(pos);
            double multiplier = MathHelper.clamp(range*.75-diff.length()*.5,.1,3);
            Vec3d motion = diff.multiply(1,0,1).add(0,1,0).normalize().multiply(multiplier);
            entity1.setVelocity(motion.x,motion.y,motion.z);
            entity1.damage(DamageSource.mob(construct),10f);
            if(conversionActive) conversionHeal(construct,10f);
            if(entity1 instanceof ServerPlayerEntity player) player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         }
         
         ParticleEffectUtils.nulConstructReflexiveBlast(world,construct.getPos(),0);
         NulConstructDialog.abilityText(construct.getServer().getPlayerManager().getPlayer(UUID.fromString(magicData.getString("summonerId"))),construct,"The Construct Surges!");
      }else if(ability.equals("necrotic_shroud")){
         abilityTag.putInt("cooldown",450);
         BlockPos tpPos = SpawnPile.makeSpawnLocations(1,25,world,construct.getBlockPos()).get(0);
         ParticleEffectUtils.nulConstructNecroticShroud(world, construct.getPos());
         construct.teleport(tpPos.getX(),tpPos.getY(),tpPos.getZ());
         ParticleEffectUtils.nulConstructNecroticShroud(world, tpPos.toCenterPos());
         NulConstructDialog.abilityText(construct.getServer().getPlayerManager().getPlayer(UUID.fromString(magicData.getString("summonerId"))),construct,"The Construct Shifts");
      }else if(ability.equals("reflective_armor")){
         abilityTag.putInt("cooldown",225);
         activeAbilitiesTag.putInt(ability,120);
         NulConstructDialog.abilityText(construct.getServer().getPlayerManager().getPlayer(UUID.fromString(magicData.getString("summonerId"))),construct,"The Construct Shimmers");
         // Rest of ability in Entity Mixin
      }else if(ability.equals("curse_of_decay")){
         abilityTag.putInt("cooldown",550);
         activeAbilitiesTag.putInt(ability,45);
         tickAbility(construct,magicEntity,ability,40);
         NulConstructDialog.abilityText(construct.getServer().getPlayerManager().getPlayer(UUID.fromString(magicData.getString("summonerId"))),construct,"The Decay Takes Hold...");
      }else if(ability.equals("summon_goons")){
         abilityTag.putInt("cooldown",525);
         List<BlockPos> poses = SpawnPile.makeSpawnLocations(10,25,world,construct.getBlockPos());
         for(int i = 0; i < 5; i++){
            Vec3d spawnPos = poses.get(i).toCenterPos();
            WitherSkeletonEntity skeleton = makeWitherSkeleton(world);
            skeleton.setPos(spawnPos.getX(),spawnPos.getY(),spawnPos.getZ());
            world.spawnNewEntityAndPassengers(skeleton);
         }
         NulConstructDialog.abilityText(construct.getServer().getPlayerManager().getPlayer(UUID.fromString(magicData.getString("summonerId"))),construct,"The Construct Calls for Aid");
      }else if(ability.equals("withering_ray")){
         abilityTag.putInt("cooldown",150);
         activeAbilitiesTag.putInt(ability,45);
         tickAbility(construct,magicEntity,ability,40);
         NulConstructDialog.abilityText(construct.getServer().getPlayerManager().getPlayer(UUID.fromString(magicData.getString("summonerId"))),construct,"The Emits a Withering Ray!");
      }else if(ability.equals("dark_conversion")){
         abilityTag.putInt("cooldown",475);
         activeAbilitiesTag.putInt(ability,250);
         NulConstructDialog.abilityText(construct.getServer().getPlayerManager().getPlayer(UUID.fromString(magicData.getString("summonerId"))),construct,"The Construct Mends...");
      }
   }
   
   private static boolean inRange(Entity e, Vec3d start, Vec3d end){
      double range = .25;
      Box entityBox = e.getBoundingBox().expand((double)e.getTargetingMargin());
      double len = end.subtract(start).length();
      Vec3d trace = end.subtract(start).normalize().multiply(range);
      int i = 0;
      Vec3d t2 = trace.multiply(i);
      while(t2.length() < len){
         Vec3d t3 = start.add(t2);
         Box hitBox = new Box(t3.x-range,t3.y-range,t3.z-range,t3.x+range,t3.y+range,t3.z+range);
         if(entityBox.intersects(hitBox)){
            return true;
         }
         t2 = trace.multiply(i);
         i++;
      }
      return false;
   }
   
   public static void conversionHeal(LivingEntity construct, float dmg){
      construct.heal(dmg);
   }
   
   public static void onDeath(WitherEntity construct, MagicEntity magicEntity){
      NbtCompound magicData = magicEntity.getData();
      MinecraftServer server = construct.getServer();
      boolean hasMythical = magicData.getBoolean("summonerHasMythical");
      boolean hasWings = magicData.getBoolean("summonerHasWings");
      boolean dropped = false;
   
      PlayerEntity summoner = server.getPlayerManager().getPlayer(UUID.fromString(magicData.getString("summonerId")));
      
      for(int i = 0; i < (int)(Math.random()*25+7); i++){
         ItemStack stack = Items.NETHER_STAR.getDefaultStack();
         dropItem(construct.getWorld(),stack,construct.getPos());
      }
      if(Math.random() < 0.01){
         ItemStack stack = MagicItems.NUL_MEMENTO.addCrafter(MagicItems.NUL_MEMENTO.getNewItem(),magicData.getString("summonerId"),false,construct.getServer());
         dropItem(construct.getWorld(),stack,construct.getPos());
         dropped = true;
      }
   
      ItemStack stack = MagicItems.CATALYST_MYTHICAL.addCrafter(MagicItems.CATALYST_MYTHICAL.getNewItem(),magicData.getString("summonerId"),false,construct.getServer());
      dropItem(construct.getWorld(),stack,construct.getPos());
   
      
      NulConstructDialog.announce(server,summoner,construct, NulConstructDialog.Announcements.SUCCESS, new boolean[]{hasMythical,hasWings,dropped});
   
      if(summoner instanceof ServerPlayerEntity player){
         ArcanaAchievements.grant(player,ArcanaAchievements.CONSTRUCT_DECONSTRUCTED.id);
         if(dropped){
            ArcanaAchievements.grant(player,ArcanaAchievements.DIVINE_FAVOR.id);
         }
      }
   }
   
   public static void deconstruct(WitherEntity construct, MagicEntity magicEntity, ServerPlayerEntity summoner){
      if(summoner != null){
         NbtCompound magicData = magicEntity.getData();
         boolean hasMythical = magicData.getBoolean("summonerHasMythical");
         boolean hasWings = magicData.getBoolean("summonerHasWings");
         NulConstructDialog.announce(construct.getServer(),summoner,construct, NulConstructDialog.Announcements.FAILURE,new boolean[]{hasMythical,hasWings});
      }
      
      dropItem(construct.getWorld(),new ItemStack(Items.NETHERITE_BLOCK),construct.getPos());
      construct.discard();
   }
   
   private static void dropItem(World world, ItemStack stack, Vec3d pos){
      stack.setCount(1);
      ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
      itemEntity.setPickupDelay(40);
      itemEntity.setCovetedItem();
   
      float f = world.random.nextFloat() * 0.1F;
      float g = world.random.nextFloat() * 6.2831855F;
      itemEntity.setVelocity((double)(-MathHelper.sin(g) * f), 0.20000000298023224, (double)(MathHelper.cos(g) * f));
      world.spawnEntity(itemEntity);
   }
   
   private static String getWeightedResult(HashMap<String,Integer> map){
      ArrayList<String> pool = new ArrayList<>();
      
      for(Map.Entry<String, Integer> entry : map.entrySet()){
         String key = entry.getKey();
         if(key.equals("reflexive_blast")) continue;
         if(entry.getValue() == -1) return key;
         for(int j = 0; j < entry.getValue(); j++){
            pool.add(key);
         }
      }
      if(pool.size() == 0) return "";
      
      return pool.get((int) (Math.random()*pool.size()));
   }
   
   private static WitherSkeletonEntity makeWitherSkeleton(ServerWorld world){
      WitherSkeletonEntity skeleton = new WitherSkeletonEntity(EntityType.WITHER_SKELETON, world);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(50f);
      skeleton.setHealth(50f);
      skeleton.setPersistent();
      ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
      axe.addEnchantment(Enchantments.SHARPNESS,2);
      skeleton.equipStack(EquipmentSlot.MAINHAND, axe);
      skeleton.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0);
      StatusEffectInstance fireRes = new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE,100000,0,false,false,false);
      StatusEffectInstance res = new StatusEffectInstance(StatusEffects.RESISTANCE,100000,1,false,false,false);
      StatusEffectInstance slowFall = new StatusEffectInstance(StatusEffects.SLOW_FALLING,100000,0,false,false,false);
      skeleton.addStatusEffect(slowFall);
      skeleton.addStatusEffect(fireRes);
      skeleton.addStatusEffect(res);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(10f);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).setBaseValue(4f);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(8f);
      return skeleton;
   }
}
