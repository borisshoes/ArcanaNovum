package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.mixins.WitherEntityAccessor;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class NulConstructEntity extends WitherEntity implements PolymerEntity {
   
   private PlayerEntity summoner;
   private boolean shouldHaveSummoner;
   private boolean summonerHasWings;
   private boolean summonerHasMythical;
   private boolean isMythical;
   private float prevHP;
   private int numPlayers;
   private int spellCooldown;
   private HashMap<String,ConstructSpell> spells;
   
   public NulConstructEntity(EntityType<? extends NulConstructEntity> entityType, World world){
      super(entityType, world);
      createSpells();

      getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(1024f);
      getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.85f);
      getAttributeInstance(EntityAttributes.GENERIC_FLYING_SPEED).setBaseValue(0.85f);
      getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(10f);
      getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).setBaseValue(10f);
      getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).setBaseValue(128f);
   }
   
   private void createSpells(){
      spellCooldown = 220;
      spells = new HashMap<>();
      spells.put("necrotic_shroud",new ConstructSpell("necrotic_shroud"));
      spells.put("reflexive_blast",new ConstructSpell("reflexive_blast"));
      spells.put("summon_goons",new ConstructSpell("summon_goons"));
      spells.put("curse_of_decay",new ConstructSpell("curse_of_decay"));
      spells.put("reflective_armor",new ConstructSpell("reflective_armor"));
      spells.put("withering_ray",new ConstructSpell("withering_ray"));
      spells.put("dark_conversion",new ConstructSpell("dark_conversion"));
   }
   
   @Override
   public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial){
      data.add(new DataTracker.SerializedEntry<>(WitherEntityAccessor.getTRACKED_ENTITY_ID_1().getId(), WitherEntityAccessor.getTRACKED_ENTITY_ID_1().getType(), getTrackedEntityId(0)));
      data.add(new DataTracker.SerializedEntry<>(WitherEntityAccessor.getTRACKED_ENTITY_ID_2().getId(), WitherEntityAccessor.getTRACKED_ENTITY_ID_2().getType(), getTrackedEntityId(1)));
      data.add(new DataTracker.SerializedEntry<>(WitherEntityAccessor.getTRACKED_ENTITY_ID_3().getId(), WitherEntityAccessor.getTRACKED_ENTITY_ID_3().getType(), getTrackedEntityId(2)));
      data.add(new DataTracker.SerializedEntry<>(WitherEntityAccessor.getINVUL_TIMER().getId(), WitherEntityAccessor.getINVUL_TIMER().getType(), getInvulnerableTimer()));
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(ServerPlayerEntity player){
      return EntityType.WITHER;
   }
   
   public static DefaultAttributeContainer.Builder createConstructAttributes() {
      return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 1024.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.85f)
            .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.85f)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 128.0)
            .add(EntityAttributes.GENERIC_ARMOR, 10.0)
            .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 10.0);
   }
   
   @Override
   public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
      if (effect.getEffectType() != ArcanaRegistry.DAMAGE_AMP_EFFECT) {
         return false;
      }
      StatusEffectInstance statusEffectInstance = this.getActiveStatusEffects().get(effect.getEffectType());
      if (statusEffectInstance == null) {
         this.getActiveStatusEffects().put(effect.getEffectType(), effect);
         this.onStatusEffectApplied(effect, source);
         return true;
      }
      if (statusEffectInstance.upgrade(effect)) {
         this.onStatusEffectUpgraded(statusEffectInstance, true, source);
         return true;
      }
      return false;
   }
   
   @Override
   protected void mobTick() {
      try{
         MinecraftServer server = getServer();
         if(server == null) return;
         if(spells == null || spells.isEmpty()) createSpells();
         
         if(shouldHaveSummoner && (summoner == null || summoner.isDead() || !summoner.getWorld().getRegistryKey().equals(getWorld().getRegistryKey()) || distanceTo(summoner) > 128)){
            deconstruct();
         }
         
         for(ConstructSpell spell : spells.values()){
            if(spell.isActive()){
               tickSpell(spell);
            }
         }
         
        if(this.age % 20 == 0){
           if(this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)){
              destructiveAura();
           }
           if(isMythical){
              mythicalAura();
           }
        }
        
        
        placateSkeletons();
         
         float curHP = getHealth();
         if(spellCooldown > 0) spellCooldown--;
         DamageSource recentDamage = getRecentDamageSource();
         List<ServerPlayerEntity> nearbyPlayers = getWorld().getNonSpectatingEntities(ServerPlayerEntity.class, getBoundingBox().expand(25.0));
         boolean conversionActive = spells.get("dark_conversion").isActive();
         
         HashMap<ConstructSpell,Integer> availableSpells = new HashMap<>();
         for(String key : spells.keySet()){
            ConstructSpell spell = spells.get(key);
            int cd = spell.getCooldown();
            if(cd > 0) spell.setCooldown(--cd);
            if(cd == 0){
               int weight = spell.getWeight();
               if(key.equals("reflective_armor")){
                  if(curHP < getMaxHealth()/2.0){
                     weight += 5;
                  }else{
                     weight = 0;
                  }
               }else if(key.equals("dark_conversion")){
                  weight += 4 -(int)(5*curHP/getMaxHealth());
                  if(conversionActive){
                     weight = 0;
                  }
               }else if(key.equals("withering_ray") || key.equals("curse_of_decay")){
                  if(nearbyPlayers.isEmpty()) weight = 0;
               }else if(key.equals("necrotic_shroud")){
                  if(conversionActive){
                     weight = 0;
                  }
               }else if(key.equals("summon_goons")){
                  if(conversionActive){
                     weight = 0;
                  }
               }
               if(weight != 0) availableSpells.put(spell,weight);
            }
         }
         
         if((int)(curHP*4/(getMaxHealth())) < (int)(prevHP*4/(getMaxHealth())) && availableSpells.containsKey(spells.get("reflexive_blast"))){
            castSpell(spells.get("reflexive_blast"));
         }
         
         boolean canCastNewAbility = getInvulnerableTimer() <= 0 && spellCooldown <= 0;
         if(canCastNewAbility){
            if(recentDamage != null && recentDamage.getName().equals("inWall") && availableSpells.containsKey(spells.get("necrotic_shroud"))){
               castSpell(spells.get("necrotic_shroud"));
            }else if(!availableSpells.isEmpty()){
               castSpell(getWeightedResult(availableSpells));
            }
         }
         
         super.mobTick();
         prevHP = curHP;
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private void placateSkeletons(){
      List<WitherSkeletonEntity> skeletons = getWorld().getEntitiesByType(EntityType.WITHER_SKELETON,getBoundingBox().expand(32),(e) -> true);
      for(WitherSkeletonEntity skeleton : skeletons){
         if(skeleton.getTarget() != null && skeleton.getTarget().getId() == this.getId()){
            PlayerEntity nearestPlayer = getWorld().getClosestPlayer(this,64);
            skeleton.setTarget(nearestPlayer);
         }
      }
   }
   
   @Override
   protected float modifyAppliedDamage(DamageSource source, float amount){
      float modified = super.modifyAppliedDamage(source, amount);
      if(source.isSourceCreativePlayer() || source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return modified;
      
      modified = modified > 100 ? 100 : modified;
      
      if(spells.get("reflective_armor").isActive()){
         Entity attacker = source.getAttacker();
         if(attacker != null){
            attacker.damage(getDamageSources().thorns(this), amount * 0.5f);
            if(spells.get("dark_conversion").isActive()){
               heal(amount*0.5f);
            }
         }
      }
      if(source.isIn(DamageTypeTags.IS_EXPLOSION)){
         if(isMythical){
            modified = 0;
         }else{
            modified *= 0.25f;
         }
      }
      
      return isMythical ? modified * 0.3f : modified * 0.5f;
   }
   
   private void mythicalAura(){
      List<PlayerEntity> players = getWorld().getEntitiesByType(EntityType.PLAYER,getBoundingBox().expand(32),(e) -> true);
      
      for(PlayerEntity player : players){
         StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, 100, 13, false, true, true);
         StatusEffectInstance amp = new StatusEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT, 100, 1, false, true, true);
         StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1, false, true, true);
         StatusEffectInstance fatigue = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 100, 2, false, true, true);
         StatusEffectInstance weakness = new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1, false, true, true);
         player.addStatusEffect(blind);
         player.addStatusEffect(amp);
         player.addStatusEffect(slow);
         player.addStatusEffect(fatigue);
         player.addStatusEffect(weakness);
      }
      
      this.heal(1.0f);
   }
   
   private void destructiveAura(){
      int thisX = MathHelper.floor(this.getX());
      int thisY = MathHelper.floor(this.getY());
      int thisZ = MathHelper.floor(this.getZ());
      boolean bl = false;
      for (int xOff = -2; xOff <= 2; ++xOff) {
         for (int zOff = -2; zOff <= 2; ++zOff) {
            for (int yOff = 0; yOff <= 4; ++yOff) {
               int x = thisX + xOff;
               int y = thisY + yOff;
               int z = thisZ + zOff;
               BlockPos blockPos = new BlockPos(x, y, z);
               BlockState blockState = this.getWorld().getBlockState(blockPos);
               if (!WitherEntity.canDestroy(blockState)) continue;
               bl = this.getWorld().breakBlock(blockPos, true, this) || bl;
            }
         }
      }
      if (bl) {
         this.getWorld().syncWorldEvent(null, WorldEvents.WITHER_BREAKS_BLOCK, this.getBlockPos(), 0);
      }
   }
   
   public void onSummoned(PlayerEntity summoner) {
      this.onSummoned(summoner,false);
   }
   
   public void onSummoned(PlayerEntity summoner, boolean mythic) {
      super.onSummoned();
      if(!(getEntityWorld() instanceof ServerWorld serverWorld)) return;
      this.summoner = summoner;
      this.shouldHaveSummoner = true;
      this.isMythical = mythic;
      
      MutableText witherName;
      if(isMythical){
         witherName = Text.literal("")
               .append(Text.literal("❖").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
               .append(Text.literal("▓").formatted(Formatting.DARK_GRAY, Formatting.BOLD, Formatting.OBFUSCATED))
               .append(Text.literal("❖").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
               .append(Text.literal(" "))
               .append(Text.literal("Mythical Nul Construct").formatted(Formatting.DARK_GRAY, Formatting.BOLD, Formatting.UNDERLINE))
               .append(Text.literal(" "))
               .append(Text.literal("❖").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
               .append(Text.literal("▓").formatted(Formatting.DARK_GRAY, Formatting.BOLD, Formatting.OBFUSCATED))
               .append(Text.literal("❖").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
         ParticleEffectUtils.mythicalConstructSummon(serverWorld,getPos().add(0,0,0),0);
      }else{
         witherName = Text.literal("")
               .append(Text.literal("-").formatted(Formatting.DARK_GRAY))
               .append(Text.literal("=").formatted(Formatting.DARK_GRAY))
               .append(Text.literal("-").formatted(Formatting.DARK_GRAY))
               .append(Text.literal(" "))
               .append(Text.literal("Nul Construct").formatted(Formatting.DARK_GRAY, Formatting.BOLD, Formatting.UNDERLINE))
               .append(Text.literal(" "))
               .append(Text.literal("-").formatted(Formatting.DARK_GRAY))
               .append(Text.literal("=").formatted(Formatting.DARK_GRAY))
               .append(Text.literal("-").formatted(Formatting.DARK_GRAY));
         ParticleEffectUtils.nulConstructSummon(serverWorld,getPos().add(0,0,0),0);
      }
      
      setCustomName(witherName);
      setCustomNameVisible(true);
      setPersistent();
      
      summonerHasWings = PLAYER_DATA.get(summoner).hasCrafted(ArcanaRegistry.WINGS_OF_ENDERIA);
      summonerHasMythical = summonerHasWings || PLAYER_DATA.get(summoner).hasCrafted(ArcanaRegistry.MYTHICAL_CATALYST);
      PlayerInventory inv = summoner.getInventory();
      for(int i = 0; i < inv.size(); i++){
         ItemStack stack = inv.getStack(i);
         MagicItem magicItem = MagicItemUtils.identifyItem(stack);
         if(magicItem == null) continue;
         if(magicItem.getRarity() == MagicRarity.MYTHICAL) summonerHasMythical = true;
      }
      numPlayers = serverWorld.getPlayers((player) -> player.distanceTo(this) <= 64).size();
      
      prevHP = getHealth();
      
      NulConstructDialog.announce(summoner.getServer(),summoner,this, Announcements.SUMMON_TEXT);
      NulConstructEntity construct = this;
      ArcanaNovum.addTickTimerCallback(serverWorld, new GenericTimer(this.getInvulnerableTimer(), () -> {
         NulConstructDialog.announce(summoner.getServer(),summoner,construct, Announcements.SUMMON_DIALOG, new boolean[]{summonerHasMythical,summonerHasWings,false,isMythical});
         setHealth(getMaxHealth());
      }));
   }
   
   private void tickSpell(ConstructSpell spell){
      int tick = spell.tick();
      boolean conversionActive = spells.get("dark_conversion").isActive();
      ServerWorld world = (ServerWorld) getEntityWorld();
      Vec3d pos = getPos();
      
      if(spell.getName().equals("curse_of_decay")){
         if(tick % 8 == 0){
            int range = 35;
            List<Entity> entities = world.getOtherEntities(this,getBoundingBox().expand(50), e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 1.5*range*range && (e instanceof LivingEntity));
            for(Entity entity1 : entities){
               if(!(entity1 instanceof LivingEntity living)) continue;;
               float dmg = living.getMaxHealth() / 10.0f;
               float mod = living instanceof ServerPlayerEntity ? 1.0f : 0.33f;
               living.damage(ArcanaDamageTypes.of(this.getWorld(),ArcanaDamageTypes.NUL,this),dmg);
               
               if(conversionActive) heal(dmg*mod);
               StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS,10,1,false,true,true);
               StatusEffectInstance weak = new StatusEffectInstance(StatusEffects.WEAKNESS,10,1,false,true,true);
               living.addStatusEffect(slow);
               living.addStatusEffect(weak);
               ParticleEffectUtils.nulConstructCurseOfDecay(world,entity1.getPos());
            }
         }
      }else if(spell.getName().equals("withering_ray")){
         if(tick % 4 == 0){
            lookAtEntity(world.getClosestPlayer(this,40),360,360);
            Vec3d startPos = getEyePos();
            Vec3d view = getRotationVecClient();
            Vec3d rayEnd = startPos.add(view.multiply(50));
            BlockHitResult raycast = world.raycast(new RaycastContext(startPos,rayEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,this));
            Box box = new Box(startPos,raycast.getPos());
            box = box.expand(2);
            
            // Hitscan check
            List<Entity> hits = world.getOtherEntities(this, box, (e)-> e instanceof LivingEntity && !e.isSpectator() && inRange(e,startPos,raycast.getPos()));
            float damage = 2f;
            
            for(Entity hit : hits){
               if(hit instanceof LivingEntity living){
                  living.damage(ArcanaDamageTypes.of(this.getWorld(),ArcanaDamageTypes.NUL,this), damage);
                  StatusEffectInstance wither = new StatusEffectInstance(StatusEffects.WITHER, 40, 1, false, true, true);
                  living.addStatusEffect(wither);
                  if(conversionActive) heal(damage);
               }
            }
            
            ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(0x36332b).toVector3f(),1.5f);
            int intervals = (int)(startPos.subtract(raycast.getPos()).length() * 4);
            ParticleEffectUtils.line(world,null,startPos,raycast.getPos(),dust,intervals,1,0.08,0);
         }
      }else if(spell.getName().equals("dark_conversion")){
         ParticleEffectUtils.nulConstructDarkConversion(world,getPos());
      }else if(spell.getName().equals("reflective_armor")){
         ParticleEffectUtils.nulConstructReflectiveArmor(world,getPos());
      }
   }
   
   private void castSpell(ConstructSpell spell){
      if(spell == null) return;
      boolean conversionActive = spells.get("dark_conversion").isActive();
      ServerWorld world = (ServerWorld) getWorld();
      Vec3d pos = getPos();
      
      //System.out.println("Casting: "+ability);
      spellCooldown = 100;
      
      if(spell.getName().equals("reflexive_blast")){
         spellCooldown = 25;
         spell.setCooldown(200);
         
         int range = 25;
         List<Entity> entities = world.getOtherEntities(this,getBoundingBox().expand(30), e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 1.5*range*range && (e instanceof LivingEntity));
         for(Entity entity1 : entities){
            Vec3d diff = entity1.getPos().subtract(pos);
            double multiplier = MathHelper.clamp(range*.75-diff.length()*.5,.1,3);
            Vec3d motion = diff.multiply(1,0,1).add(0,1,0).normalize().multiply(multiplier);
            entity1.setVelocity(motion.x,motion.y,motion.z);
            entity1.damage(ArcanaDamageTypes.of(this.getWorld(),ArcanaDamageTypes.NUL,this),10f);
            if(conversionActive) heal(10f);
            if(entity1 instanceof ServerPlayerEntity player) player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         }
         
         ParticleEffectUtils.nulConstructReflexiveBlast(world,getPos(),0);
         NulConstructDialog.abilityText(summoner,this,"The Construct Surges!");
      }else if(spell.getName().equals("necrotic_shroud")){
         spell.setCooldown(450);
         BlockPos tpPos = SpawnPile.makeSpawnLocations(1,25,world,getBlockPos()).get(0);
         ParticleEffectUtils.nulConstructNecroticShroud(world, getPos());
         teleport(tpPos.getX(),tpPos.getY(),tpPos.getZ());
         ParticleEffectUtils.nulConstructNecroticShroud(world, tpPos.toCenterPos());
         NulConstructDialog.abilityText(summoner,this,"The Construct Shifts");
      }else if(spell.getName().equals("reflective_armor")){
         spell.setCooldown(225);
         spell.cast(120);
         NulConstructDialog.abilityText(summoner,this,"The Construct Shimmers");
         // Rest of ability in Entity Mixin
      }else if(spell.getName().equals("curse_of_decay")){
         spell.setCooldown(550);
         spell.cast(45);
         tickSpell(spell);
         NulConstructDialog.abilityText(summoner,this,"The Decay Takes Hold...");
      }else if(spell.getName().equals("summon_goons")){
         spell.setCooldown(525);
         List<BlockPos> poses = SpawnPile.makeSpawnLocations(10,25,world,getBlockPos());
         for(int i = 0; i < 5; i++){
            Vec3d spawnPos = poses.get(i).toCenterPos();
            WitherSkeletonEntity skeleton = makeWitherSkeleton(world);
            skeleton.setPos(spawnPos.getX(),spawnPos.getY(),spawnPos.getZ());
            world.spawnNewEntityAndPassengers(skeleton);
         }
         NulConstructDialog.abilityText(summoner,this,"The Construct Calls for Aid");
      }else if(spell.getName().equals("withering_ray")){
         spell.setCooldown(150);
         spell.cast(45);
         tickSpell(spell);
         NulConstructDialog.abilityText(summoner,this,"The Construct Emits a Withering Ray!");
      }else if(spell.getName().equals("dark_conversion")){
         spell.setCooldown(475);
         spell.cast(250);
         NulConstructDialog.abilityText(summoner,this,"The Construct Mends...");
      }
   }
   
   private boolean inRange(Entity e, Vec3d start, Vec3d end){
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
   
   @Override
   public void onDeath(DamageSource damageSource){
      super.onDeath(damageSource);
      
      MinecraftServer server = getServer();
      if(server == null) return;
      
      if(isMythical){
         dropItem(getWorld(),Items.NETHER_STAR.getDefaultStack().copyWithCount(63),getPos());
      }else{
         for(int i = 0; i < (int)(Math.random()*33+16); i++){
            ItemStack stack = Items.NETHER_STAR.getDefaultStack().copy();
            dropItem(getWorld(),stack,getPos());
         }
      }
      
      if(summoner == null) return;
      
      boolean dropped = isMythical ? Math.random() < 0.75 : Math.random() < 0.05;
      
      if(dropped){
         ItemStack stack = ArcanaRegistry.NUL_MEMENTO.addCrafter(ArcanaRegistry.NUL_MEMENTO.getNewItem(),summoner.getUuidAsString(),false,server);
         dropItem(getWorld(), stack.copyWithCount(1),getPos());
      }
      
      if(!isMythical){
         ItemStack stack = ArcanaRegistry.MYTHICAL_CATALYST.addCrafter(ArcanaRegistry.MYTHICAL_CATALYST.getNewItem(),summoner.getUuidAsString(),false,server);
         PLAYER_DATA.get(summoner).addCraftedSilent(stack);
         dropItem(getWorld(), stack.copyWithCount(1),getPos());
      }
      
      NulConstructDialog.announce(server,summoner,this, Announcements.SUCCESS, new boolean[]{summonerHasMythical,summonerHasWings,dropped&&!isMythical,isMythical,isMythical&&dropped,isMythical&&!dropped});
      
      if(summoner instanceof ServerPlayerEntity player){
         ArcanaAchievements.grant(player,ArcanaAchievements.CONSTRUCT_DECONSTRUCTED.id);
         if(dropped){
            ArcanaAchievements.grant(player,ArcanaAchievements.DIVINE_FAVOR.id);
         }
      }
   }
   
   public void deconstruct(){
      if(summoner != null){
         NulConstructDialog.announce(getServer(),summoner,this, Announcements.FAILURE,new boolean[]{summonerHasMythical,summonerHasWings,false,isMythical});
      }
      
      dropItem(getWorld(),(new ItemStack(Items.NETHERITE_BLOCK)).copyWithCount(1),getPos());
      discard();
   }
   
   private void dropItem(World world, ItemStack stack, Vec3d pos){
      ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
      itemEntity.setPickupDelay(40);
      itemEntity.setCovetedItem();
      
      float f = world.random.nextFloat() * 0.1F;
      float g = world.random.nextFloat() * 6.2831855F;
      itemEntity.setVelocity((double)(-MathHelper.sin(g) * f), 0.20000000298023224, (double)(MathHelper.cos(g) * f));
      world.spawnEntity(itemEntity);
   }
   
   private ConstructSpell getWeightedResult(HashMap<ConstructSpell,Integer> map){
      ArrayList<ConstructSpell> pool = new ArrayList<>();
      
      for(Map.Entry<ConstructSpell, Integer> entry : map.entrySet()){
         ConstructSpell key = entry.getKey();
         if(key.getName().equals("reflexive_blast")) continue;
         if(entry.getValue() == -1) return key;
         for(int j = 0; j < entry.getValue(); j++){
            pool.add(key);
         }
      }
      if(pool.isEmpty()) return null;
      
      return pool.get((int) (Math.random()*pool.size()));
   }
   
   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("numPlayers",numPlayers);
      nbt.putInt("spellCooldown",spellCooldown);
      nbt.putBoolean("shouldHaveSummoner",shouldHaveSummoner);
      nbt.putBoolean("summonerHasMythical",summonerHasMythical);
      nbt.putBoolean("summonerHasWings",summonerHasWings);
      nbt.putBoolean("isMythical",isMythical);
      nbt.putFloat("prevHP",prevHP);
      
      if(summoner != null){
         nbt.putString("summoner",summoner.getUuidAsString());
      }
      
      NbtCompound spellsTag = new NbtCompound();
      for(Map.Entry<String, ConstructSpell> entry : spells.entrySet()){
         spellsTag.put(entry.getKey(),entry.getValue().toNbt());
      }
      nbt.put("spells",spellsTag);
   }
   
   @Override
   public void readCustomDataFromNbt(NbtCompound nbt){
      super.readCustomDataFromNbt(nbt);
      numPlayers = nbt.getInt("numPlayers");
      spellCooldown = nbt.getInt("spellCooldown");
      shouldHaveSummoner = nbt.getBoolean("shouldHaveSummoner");
      summonerHasMythical = nbt.getBoolean("summonerHasMythical");
      summonerHasWings = nbt.getBoolean("summonerHasWings");
      isMythical = nbt.getBoolean("isMythical");
      prevHP = nbt.getFloat("prevHP");
      
      if(nbt.contains("summoner")){
         if(getEntityWorld() instanceof ServerWorld serverWorld && serverWorld.getEntity(UUID.fromString(nbt.getString("summoner"))) instanceof PlayerEntity player){
            summoner = player;
         }
      }
      
      spells = new HashMap<>();
      NbtCompound spellsTag = nbt.getCompound("spells");
      for(String key : spellsTag.getKeys()){
         spells.put(key,ConstructSpell.fromNbt(spellsTag.getCompound(key)));
      }
   }
   
   private WitherSkeletonEntity makeWitherSkeleton(ServerWorld world){
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
      StatusEffectInstance speed = new StatusEffectInstance(StatusEffects.SPEED,100000,0,false,false,false);
      skeleton.addStatusEffect(speed);
      skeleton.addStatusEffect(slowFall);
      skeleton.addStatusEffect(fireRes);
      skeleton.addStatusEffect(res);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(10f);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).setBaseValue(4f);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(8f);
      skeleton.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).setBaseValue(64f);
      return skeleton;
   }
   
   private static class ConstructSpell{
      private String name;
      private int cooldown;
      private final int weight;
      private boolean active;
      private int tick;
      
      private ConstructSpell(String name){
         this.name = name;
         this.cooldown = 0;
         this.weight = (int)(Math.random()*10+1);
         this.active = false;
         this.tick = 0;
      }
      
      public NbtCompound toNbt(){
         NbtCompound tag = new NbtCompound();
         tag.putString("name",name);
         tag.putInt("cooldown",cooldown);
         tag.putInt("weight",weight);
         tag.putInt("tick",tick);
         tag.putBoolean("active",active);
         return tag;
      }
      
      private static ConstructSpell fromNbt(NbtCompound tag){
         return new ConstructSpell(tag.getString("name"), tag.getInt("cooldown"), tag.getInt("weight"), tag.getBoolean("active"), tag.getInt("tick"));
      }
      
      private ConstructSpell(String name, int cooldown, int weight, boolean active, int tick){
         this.name = name;
         this.cooldown = cooldown;
         this.weight = weight;
         this.active = active;
         this.tick = tick;
      }
      
      public void cast(int tick){
         this.active = true;
         this.tick = tick;
      }
      
      public int tick(){
         tick--;
         if(tick <= 0){
            this.active = false;
         }
         return tick;
      }
      
      public boolean isActive(){
         return active;
      }
      
      public String getName(){
         return name;
      }
      
      public int getCooldown(){
         return cooldown;
      }
      
      public void setCooldown(int cooldown){
         this.cooldown = cooldown;
      }
      
      public int getWeight(){
         return weight;
      }
   }
   
   private static class NulConstructDialog {
      public static HashMap<Announcements,ArrayList<Dialog>> DIALOG = new HashMap<>();
      
      static{
         for(Announcements type : Announcements.values()){
            DIALOG.put(type,new ArrayList<>());
         }
         
         DIALOG.get(Announcements.SUMMON_TEXT).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You Feel ").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC))
                     .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC, Formatting.BOLD))
                     .append(Text.literal(" Flow Into The ").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC))
                     .append(Text.literal("Construct").formatted(Formatting.GRAY, Formatting.ITALIC)),
               Text.literal("")
                     .append(Text.literal("A ").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC))
                     .append(Text.literal("Dark Presence").formatted(Formatting.DARK_GRAY, Formatting.ITALIC, Formatting.BOLD))
                     .append(Text.literal(" Looms...").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC)),
               Text.literal("")
         )),1,1,-1));
         
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Mortal").formatted(Formatting.GOLD))
                     .append(Text.literal(" knocks on the door of the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal("? They know not what they are toying with...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Those unworthy of ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("knowledge").formatted(Formatting.BLUE))
                     .append(Text.literal(" shall be reduced to ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("nothing").formatted(Formatting.GRAY))
                     .append(Text.literal("...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Mortals").formatted(Formatting.GOLD))
                     .append(Text.literal(" grow bolder by the minute. Perhaps they need to be put in their place.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("A ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Mortal").formatted(Formatting.GOLD))
                     .append(Text.literal(" seeks to harness ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal("? Let them try...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Of all the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Gods").formatted(Formatting.AQUA, Formatting.BOLD))
                     .append(Text.literal(" to call upon, you disturb me? You must be ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("ignorant").formatted(Formatting.DARK_RED))
                     .append(Text.literal(" of my domain, or ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("arrogant").formatted(Formatting.DARK_RED))
                     .append(Text.literal(" enough to tempt ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Death").formatted(Formatting.GRAY))
                     .append(Text.literal(".").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("I am the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("God").formatted(Formatting.AQUA, Formatting.BOLD))
                     .append(Text.literal(" of ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Death").formatted(Formatting.GRAY, Formatting.BOLD))
                     .append(Text.literal(" and ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Knowledge").formatted(Formatting.BLUE, Formatting.BOLD))
                     .append(Text.literal(". If my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Construct").formatted(Formatting.GRAY))
                     .append(Text.literal(" does not give you the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("former").formatted(Formatting.GRAY))
                     .append(Text.literal(", you shall earn the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("latter").formatted(Formatting.BLUE))
                     .append(Text.literal(".").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),1,1,-1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("So you have defeated ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" and now knock on my door? You seek to challenge the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("God").formatted(Formatting.AQUA, Formatting.BOLD))
                     .append(Text.literal(" of ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Death").formatted(Formatting.GRAY, Formatting.BOLD))
                     .append(Text.literal("!?").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,10,1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("I watched as you defeated ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                     .append(Text.literal(". Do not think that I am as ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("weak").formatted(Formatting.DARK_RED))
                     .append(Text.literal(" or ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("indolent").formatted(Formatting.DARK_RED))
                     .append(Text.literal(" as her.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,10,1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You have tasted the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" and want more? Lets hope your ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("greed").formatted(Formatting.GOLD))
                     .append(Text.literal(" is not your downfall.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,10,0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Just because you already carry the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" does not mean you are ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("entitled").formatted(Formatting.GOLD))
                     .append(Text.literal(" to more.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,10,0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("So you would sacrifice my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("gift").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" to curry my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("favor").formatted(Formatting.GOLD))
                     .append(Text.literal("? Let's see if you're worth it...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,200,3));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You reject my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("divine gift").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal("? If it's me you want, you must prove yourself ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("worthy").formatted(Formatting.GOLD))
                     .append(Text.literal("!").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,200,3));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("If you want my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("favor").formatted(Formatting.BLUE))
                     .append(Text.literal(" you must face a ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("real").formatted(Formatting.GOLD, Formatting.ITALIC))
                     .append(Text.literal(" challenge!").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,200,3));
         
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You have impressed me ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Mortal").formatted(Formatting.GOLD))
                     .append(Text.literal(", you have earned a taste of my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" power.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Impressive, I have imbued your ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Catalyst").formatted(Formatting.GOLD))
                     .append(Text.literal(" with ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(", I'm curious as to how you'll use it.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You have defeated my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Construct").formatted(Formatting.GRAY))
                     .append(Text.literal(", no easy feat. Gather what ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" remains for your ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Catalyst").formatted(Formatting.GOLD)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Death").formatted(Formatting.GRAY))
                     .append(Text.literal(" does not come for you today, I shall grant you what you have sought.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Catalytic Matrix").formatted(Formatting.GOLD))
                     .append(Text.literal(" of yours is a quaint toy, lets see if you can handle a taste of ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("true power").formatted(Formatting.LIGHT_PURPLE))
                     .append(Text.literal(".").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("A valiant fight! ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" must be getting nervous. Perhaps she will finally learn her lesson...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),1,0,1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("I can see how you defeated ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                     .append(Text.literal(", however I am not so ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("weak").formatted(Formatting.DARK_RED))
                     .append(Text.literal(". Be thankful I only sent a ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Construct").formatted(Formatting.GRAY))
                     .append(Text.literal(" to greet you.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,10,1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You helped my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Sister").formatted(Formatting.DARK_PURPLE))
                     .append(Text.literal(" see the truth, and now you have proven yourself. Take this ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Boon").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" and may we meet again.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,10,1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You have earned your ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Catalyst").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(", but don't get ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("overzealous").formatted(Formatting.GOLD))
                     .append(Text.literal(", or else I will deal with you ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("personally").formatted(Formatting.DARK_RED, Formatting.ITALIC))
                     .append(Text.literal(".").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,10,1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("It seems you are worthy enough to add another piece of the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" to your collection.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,10,0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You are unlike any I have seen before. Perhaps you are worthy of my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("guidance").formatted(Formatting.BLUE))
                     .append(Text.literal(". This ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Memento").formatted(Formatting.BLACK, Formatting.BOLD))
                     .append(Text.literal(" shall be my gift to you.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,-1,2));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You did well to survive my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("stronger construct").formatted(Formatting.RED))
                     .append(Text.literal(", but not well enough to ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("impress").formatted(Formatting.GOLD))
                     .append(Text.literal(" me.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,200,5));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You may have survived, but your performance showed ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("weakness").formatted(Formatting.RED, Formatting.ITALIC))
                     .append(Text.literal(" that I do not ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("tolerate").formatted(Formatting.GOLD))
                     .append(Text.literal("!").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,200,5));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("So your").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal(" gambit").formatted(Formatting.GOLD))
                     .append(Text.literal(" paid off... I am ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("impressed").formatted(Formatting.BLUE))
                     .append(Text.literal(" mortal, let my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Memento").formatted(Formatting.BLACK, Formatting.BOLD))
                     .append(Text.literal(" offer you ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("wisdom").formatted(Formatting.BLUE))
                     .append(Text.literal(".").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,200,4));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("A ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("spectacular").formatted(Formatting.GOLD, Formatting.ITALIC))
                     .append(Text.literal(" display of ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("competence").formatted(Formatting.AQUA))
                     .append(Text.literal("! Take my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Memento").formatted(Formatting.BLACK, Formatting.BOLD))
                     .append(Text.literal(", and let my new gift ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("guide").formatted(Formatting.BLUE))
                     .append(Text.literal(" you well.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,200,4));
         
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Another arrogant ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Mortal").formatted(Formatting.GOLD))
                     .append(Text.literal(", not worthy of my time.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),5,5,-1));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Such a simple ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Construct").formatted(Formatting.GRAY))
                     .append(Text.literal(" defeated you? You are not worthy of the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(".").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Such a small sample of ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine Power").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" overwhelmed you? How did you plan on ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("harnessing").formatted(Formatting.BLUE))
                     .append(Text.literal(" it in the first place?").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("An expected result from calling upon the ").formatted(Formatting.GRAY))
                     .append(Text.literal("God").formatted(Formatting.AQUA, Formatting.BOLD))
                     .append(Text.literal(" of Death").formatted(Formatting.GRAY, Formatting.BOLD))
                     .append(Text.literal(". Do not waste my time again.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Arrogant enough to tempt ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Death").formatted(Formatting.GRAY))
                     .append(Text.literal("... I can't fathom how you expected to win.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),3,3,-1));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("There is ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("knowledge").formatted(Formatting.BLUE))
                     .append(Text.literal(" in failure, but only if you have the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("wisdom").formatted(Formatting.AQUA))
                     .append(Text.literal(" to find it.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),1,1,-1));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("death").formatted(Formatting.GRAY))
                     .append(Text.literal(" is a mercy. Do not be ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("foolish").formatted(Formatting.DARK_RED))
                     .append(Text.literal(" enough to find out why.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),1,1,-1));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("A ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("weakling").formatted(Formatting.DARK_RED))
                     .append(Text.literal(" like you defeated ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                     .append(Text.literal("? And I thought my opinion of ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("her").formatted(Formatting.DARK_PURPLE))
                     .append(Text.literal(" couldn't get any lower.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,20,1));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Whatever petty tricks got you the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" in the past won't work on me. ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Knowledge").formatted(Formatting.BLUE))
                     .append(Text.literal(" must be earned!").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,20,0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("I always knew you were too ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("weak").formatted(Formatting.RED, Formatting.ITALIC))
                     .append(Text.literal(" to handle real ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("power").formatted(Formatting.LIGHT_PURPLE))
                     .append(Text.literal("...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,150,3));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("An interesting ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("gambit").formatted(Formatting.GOLD))
                     .append(Text.literal(", too bad you aren't ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("skilled").formatted(Formatting.BLUE))
                     .append(Text.literal(" enough to execute it.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,200,3));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Your ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("weakness").formatted(Formatting.RED))
                     .append(Text.literal(" is ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("revolting!").formatted(Formatting.DARK_RED, Formatting.ITALIC))
                     .append(Text.literal(" Your ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("divine catalyst").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" is ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("forfeit").formatted(Formatting.GOLD))
                     .append(Text.literal("!").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),0,200,3));
      }
      
      public static void abilityText(PlayerEntity summoner, WitherEntity wither, String text){
         List<ServerPlayerEntity> playersInRange = wither.getWorld().getNonSpectatingEntities(ServerPlayerEntity.class, wither.getBoundingBox().expand(50.0));
         if(summoner instanceof ServerPlayerEntity player && !playersInRange.contains(player)) playersInRange.add(player);
         for(ServerPlayerEntity inRange : playersInRange){
            inRange.sendMessage(Text.literal(text).formatted(Formatting.DARK_GRAY,Formatting.ITALIC),true);
         }
      }
      
      public static void announce(MinecraftServer server, PlayerEntity summoner, WitherEntity wither, Announcements type){
         announce(server,summoner,wither,type,new boolean[]{});
      }
      
      // hasMythical, hasWings, droppedMemento & !isMythical, isMythical, droppedMemento & isMythical, !droppedMemento & isMythical
      public static void announce(MinecraftServer server, PlayerEntity summoner, WitherEntity wither, Announcements type, boolean[] args){
         ArrayList<MutableText> message = getWeightedResult(DIALOG.get(type),args);
         List<ServerPlayerEntity> playersInRange = wither.getWorld().getNonSpectatingEntities(ServerPlayerEntity.class, wither.getBoundingBox().expand(50.0));
         if(summoner instanceof ServerPlayerEntity player && !playersInRange.contains(player)) playersInRange.add(player);
         
         for(MutableText msg : message){
            boolean foundSummoner = false;
            for(ServerPlayerEntity playerInRange : playersInRange){
               playerInRange.sendMessage(msg, false);
               if(playerInRange.getId() == summoner.getId()){
                  foundSummoner = true;
               }
            }
            if(type == Announcements.FAILURE && summoner != null && !foundSummoner){
               summoner.sendMessage(msg,false);
            }
         }
      }
      
      private static ArrayList<MutableText> getWeightedResult(ArrayList<Dialog> dialogs, boolean[] args){
         ArrayList<Integer> pool = new ArrayList<>();
         int[] weights = new int[dialogs.size()];
         for(int i = 0; i < dialogs.size(); i++){
            weights[i] = dialogs.get(i).getWeight(args);
         }
         
         for(int i = 0; i < weights.length; i++){
            if(weights[i] == -1) return dialogs.get(i).message;
            for(int j = 0; j < weights[i]; j++){
               pool.add(i);
            }
         }
         
         return dialogs.get(pool.get((int) (Math.random()*pool.size()))).message;
      }
   }
   
   public enum Announcements{
      SUMMON_TEXT,
      SUMMON_DIALOG,
      SUCCESS,
      FAILURE
   }
   
   private record Dialog(ArrayList<MutableText> message, int weightNoCond, int weightWithCond, int condInd){
      public int getWeight(boolean[] args){
         if(condInd == -1) return weightWithCond;
         return (args.length > condInd && args[condInd]) ? weightWithCond : weightNoCond;
      }
   }
   
   public static BlockPattern getConstructPattern() {
      return BlockPatternBuilder.start().aisle("^^^", "#@#", "~#~")
            .where('#', (pos) -> pos.getBlockState().isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
            .where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL))))
            .where('~', CachedBlockPosition.matchesBlockState(AbstractBlock.AbstractBlockState::isAir))
            .where('@', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.NETHERITE_BLOCK)))
            .build();
   }
}


