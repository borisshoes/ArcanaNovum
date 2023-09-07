package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.bosses.nulconstruct.NulConstructDialog;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.WingsOfEnderia;
import net.borisshoes.arcananovum.mixins.WitherEntityAccessor;
import net.borisshoes.arcananovum.utils.*;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
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
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.*;

public class NulConstructEntity extends WitherEntity implements PolymerEntity {
   
   private PlayerEntity summoner;
   private boolean shouldHaveSummoner;
   private boolean summonerHasWings;
   private boolean summonerHasMythical;
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
   protected void mobTick() {
      try{
         MinecraftServer server = getServer();
         if(server == null) return;
         if(spells == null || spells.isEmpty()) createSpells();
         
         if(shouldHaveSummoner && (summoner == null || summoner.isDead())){
            deconstruct();
         }
         
         for(ConstructSpell spell : spells.values()){
            if(spell.isActive()){
               tickSpell(spell);
            }
         }
         
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
   
   @Override
   protected float modifyAppliedDamage(DamageSource source, float amount){
      float modified = super.modifyAppliedDamage(source, amount);
      
      if(spells.get("reflective_armor").isActive()){
         Entity attacker = source.getAttacker();
         if(attacker != null){
            attacker.damage(getDamageSources().thorns(this), amount * 0.5f);
            heal(amount*0.5f);
         }
      }
      if(source.isIn(DamageTypeTags.IS_EXPLOSION)){
         modified *= 0.25f;
      }
      
      return modified * 0.5f;
   }
   
   public void onSummoned(PlayerEntity summoner) {
      super.onSummoned();
      if(!(getEntityWorld() instanceof ServerWorld serverWorld)) return;
      this.summoner = summoner;
      this.shouldHaveSummoner = true;
      
      MutableText witherName = Text.literal("")
            .append(Text.literal("-").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("=").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("-").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" "))
            .append(Text.literal("Nul Construct").formatted(Formatting.DARK_GRAY, Formatting.BOLD, Formatting.UNDERLINE))
            .append(Text.literal(" "))
            .append(Text.literal("-").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("=").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("-").formatted(Formatting.DARK_GRAY));
      setCustomName(witherName);
      setCustomNameVisible(true);
      setPersistent();
      
      summonerHasMythical = false;
      summonerHasWings = false;
      PlayerInventory inv = summoner.getInventory();
      for(int i = 0; i < inv.size(); i++){
         ItemStack stack = inv.getStack(i);
         MagicItem magicItem = MagicItemUtils.identifyItem(stack);
         if(magicItem == null) continue;
         if(magicItem.getRarity() == MagicRarity.MYTHICAL) summonerHasMythical = true;
         if(magicItem instanceof WingsOfEnderia) summonerHasWings = true;
      }
      numPlayers = serverWorld.getPlayers((player) -> player.distanceTo(this) <= 64).size();
      
      prevHP = getHealth();
      
      NulConstructDialog.announce(summoner.getServer(),summoner,this, NulConstructDialog.Announcements.SUMMON_TEXT);
      boolean finalHasMythical = summonerHasMythical;
      boolean finalHasWings = summonerHasWings;
      NulConstructEntity construct = this;
      Arcananovum.addTickTimerCallback(serverWorld, new GenericTimer(this.getInvulnerableTimer(), new TimerTask() {
         @Override
         public void run(){
            NulConstructDialog.announce(summoner.getServer(),summoner,construct, NulConstructDialog.Announcements.SUMMON_DIALOG, new boolean[]{finalHasMythical,finalHasWings});
            setHealth(getMaxHealth());
         }
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
               living.damage(new DamageSource(getDamageSources().outOfWorld().getTypeRegistryEntry(), this,this),dmg);
               
               if(conversionActive) heal(dmg*0.5f);
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
                  living.damage(new DamageSource(getDamageSources().outOfWorld().getTypeRegistryEntry(), this,this), damage);
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
         
         int range = 25;
         List<Entity> entities = world.getOtherEntities(this,getBoundingBox().expand(30), e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 1.5*range*range && (e instanceof LivingEntity));
         for(Entity entity1 : entities){
            Vec3d diff = entity1.getPos().subtract(pos);
            double multiplier = MathHelper.clamp(range*.75-diff.length()*.5,.1,3);
            Vec3d motion = diff.multiply(1,0,1).add(0,1,0).normalize().multiply(multiplier);
            entity1.setVelocity(motion.x,motion.y,motion.z);
            entity1.damage(getDamageSources().mobAttack(this),10f);
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
         NulConstructDialog.abilityText(summoner,this,"The Emits a Withering Ray!");
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
      boolean dropped = false;
      
      for(int i = 0; i < (int)(Math.random()*33+16); i++){
         ItemStack stack = Items.NETHER_STAR.getDefaultStack().copy();
         dropItem(getWorld(),stack,getPos());
      }
      
      if(summoner == null) return;
      
      if(Math.random() < 0.05){
         ItemStack stack = ArcanaRegistry.NUL_MEMENTO.addCrafter(ArcanaRegistry.NUL_MEMENTO.getNewItem(),summoner.getUuidAsString(),false,server);
         dropItem(getWorld(),stack,getPos());
         dropped = true;
      }
      
      ItemStack stack = ArcanaRegistry.MYTHICAL_CATALYST.addCrafter(ArcanaRegistry.MYTHICAL_CATALYST.getNewItem(),summoner.getUuidAsString(),false,server);
      dropItem(getWorld(),stack,getPos());
      
      
      NulConstructDialog.announce(server,summoner,this, NulConstructDialog.Announcements.SUCCESS, new boolean[]{summonerHasMythical,summonerHasWings,dropped});
      
      if(summoner instanceof ServerPlayerEntity player){
         ArcanaAchievements.grant(player,ArcanaAchievements.CONSTRUCT_DECONSTRUCTED.id);
         if(dropped){
            ArcanaAchievements.grant(player,ArcanaAchievements.DIVINE_FAVOR.id);
         }
      }
   }
   
   public void deconstruct(){
      if(summoner != null){
         NulConstructDialog.announce(getServer(),summoner,this, NulConstructDialog.Announcements.FAILURE,new boolean[]{summonerHasMythical,summonerHasWings});
      }
      
      dropItem(getWorld(),new ItemStack(Items.NETHERITE_BLOCK),getPos());
      discard();
   }
   
   private void dropItem(World world, ItemStack stack, Vec3d pos){
      stack.setCount(1);
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
}
