package net.borisshoes.arcananovum.entities;

import com.google.common.collect.ImmutableList;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.mixins.LivingEntityAccessor;
import net.borisshoes.arcananovum.mixins.WitherEntityAccessor;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class NulConstructEntity extends HostileEntity implements PolymerEntity, RangedAttackMob {
   
   private static final TrackedData<Integer> TRACKED_ENTITY_ID_1 = DataTracker.registerData(NulConstructEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Integer> TRACKED_ENTITY_ID_2 = DataTracker.registerData(NulConstructEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Integer> TRACKED_ENTITY_ID_3 = DataTracker.registerData(NulConstructEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final List<TrackedData<Integer>> TRACKED_ENTITY_IDS = ImmutableList.of(TRACKED_ENTITY_ID_1, TRACKED_ENTITY_ID_2, TRACKED_ENTITY_ID_3);
   private static final TrackedData<Integer> INVUL_TIMER = DataTracker.registerData(NulConstructEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private final int[] skullCooldowns = new int[2];
   private final int[] chargedSkullCooldowns = new int[2];
   private final ServerBossBar bossBar = (ServerBossBar)new ServerBossBar(this.getDisplayName(), BossBar.Color.BLUE, BossBar.Style.PROGRESS).setDarkenSky(true);
   
   public static final double FIGHT_RANGE = 64.0;
   private static final double DECAY_RANGE = 32.0;
   private static final double BLAST_RANGE = 24.0;
   private static final double TELEPORT_RANGE = 16.0;
   private static final double RAY_RANGE = 32.0;
   private static final TargetPredicate.EntityPredicate CAN_ATTACK_PREDICATE = (entity, world) -> !entity.getType().isIn(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS) && entity.isMobOrPlayer() && (!entity.isInCreativeMode() && !entity.isSpectator());
   private static final TargetPredicate HEAD_TARGET_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(RAY_RANGE).setPredicate(CAN_ATTACK_PREDICATE);
   
   private PlayerEntity summoner;
   private boolean shouldHaveSummoner;
   private boolean summonerHasWings;
   private boolean summonerHasDivine;
   private boolean isExalted;
   private boolean initializedAttributes = false;
   private float prevHP;
   private int numPlayers;
   private int spellCooldown;
   private float adaptiveResistance;
   private HashMap<ConstructSpellType,ConstructSpell> spells;
   private HashMap<ConstructAdaptations,Boolean> adaptations;
   private HashMap<BlockPos,Integer> blockDamage;
   private List<ServerPlayerEntity> players;
   private List<Pair<BlockPos,Integer>> blockPacketQueue;
   
   private ConstructMovementType movementType = ConstructMovementType.WAIT;
   public Vec3d targetPosition;
   public BlockPos circlingCenter;
   private int movementChangeTime;
   private int acquireTargetCooldown = 0;
   private float strafeYaw = 0;
   private float strafeRadius = 5;
   private float strafeHeight = 5;
   private float strafeRate = 2;
   private int attackCooldown = 0;
   
   public NulConstructEntity(EntityType<? extends NulConstructEntity> entityType, World world){
      super(entityType, world);
      this.setHealth(this.getMaxHealth());
      this.experiencePoints = 1000;
      this.blockDamage = new HashMap<>();
      this.movementChangeTime = this.age + this.random.nextBetween(200,500);
      this.targetPosition = this.getPos();
      this.circlingCenter = this.getBlockPos();
      this.adaptiveResistance = 0.0f;
      adaptations = new HashMap<>();
      players = new ArrayList<>();
      blockPacketQueue = new ArrayList<>();
      for(ConstructAdaptations value : ConstructAdaptations.values()){
         adaptations.put(value,false);
      }
      createSpells();
   }
   
   
   // ========== Initialization ==========
   
   private void createSpells(){
      spellCooldown = 220;
      spells = new HashMap<>();
      for(ConstructSpellType value : ConstructSpellType.values()){
         spells.put(value,new ConstructSpell(value));
      }
   }
   
   private void initializeAttributes(){
      getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(1024.0);
      getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.85f);
      getAttributeInstance(EntityAttributes.FLYING_SPEED).setBaseValue(0.85f);
      getAttributeInstance(EntityAttributes.ARMOR).setBaseValue(10.0);
      getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS).setBaseValue(10.0);
      getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(128);
      getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(10.0);
      initializedAttributes = true;
   }
   
   public static DefaultAttributeContainer.Builder createConstructAttributes(){
      return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, 1024.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.85f)
            .add(EntityAttributes.FLYING_SPEED, 0.85f)
            .add(EntityAttributes.FOLLOW_RANGE, 128)
            .add(EntityAttributes.ARMOR, 10.0)
            .add(EntityAttributes.ARMOR_TOUGHNESS, 10.0)
            .add(EntityAttributes.ATTACK_DAMAGE,10.0);
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.WITHER;
   }
   
   // ========== Tracking Data ==========
   
   @Override
   protected void initDataTracker(DataTracker.Builder builder){
      super.initDataTracker(builder);
      builder.add(TRACKED_ENTITY_ID_1, 0);
      builder.add(TRACKED_ENTITY_ID_2, 0);
      builder.add(TRACKED_ENTITY_ID_3, 0);
      builder.add(INVUL_TIMER, 0);
   }
   
   @Override
   public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial){
      data.add(new DataTracker.SerializedEntry<>(WitherEntityAccessor.getTRACKED_ENTITY_ID_1().id(), WitherEntityAccessor.getTRACKED_ENTITY_ID_1().dataType(), getTrackedEntityId(0)));
      data.add(new DataTracker.SerializedEntry<>(WitherEntityAccessor.getTRACKED_ENTITY_ID_2().id(), WitherEntityAccessor.getTRACKED_ENTITY_ID_2().dataType(), getTrackedEntityId(1)));
      data.add(new DataTracker.SerializedEntry<>(WitherEntityAccessor.getTRACKED_ENTITY_ID_3().id(), WitherEntityAccessor.getTRACKED_ENTITY_ID_3().dataType(), getTrackedEntityId(2)));
      data.add(new DataTracker.SerializedEntry<>(WitherEntityAccessor.getINVUL_TIMER().id(), WitherEntityAccessor.getINVUL_TIMER().dataType(), getInvulnerableTimer()));
      data.add(new DataTracker.SerializedEntry<>(LivingEntityAccessor.getHEALTH().id(), LivingEntityAccessor.getHEALTH().dataType(), getTrackedHealth()));
   }
   
   private float getTrackedHealth(){
      if(this.isShieldActive()){
         return Math.min(this.getHealth(), this.getMaxHealth()/2 - 1);
      }else{
         return Math.max(this.getHealth(), this.getMaxHealth()/2 + 1);
      }
   }
   
   @Override
   public void onStartedTrackingBy(ServerPlayerEntity player){
      super.onStartedTrackingBy(player);
      this.bossBar.addPlayer(player);
   }
   
   @Override
   public void onStoppedTrackingBy(ServerPlayerEntity player){
      super.onStoppedTrackingBy(player);
      this.bossBar.removePlayer(player);
   }
   
   public int getInvulnerableTimer(){
      return this.dataTracker.get(INVUL_TIMER);
   }
   
   public void setInvulTimer(int ticks){
      this.dataTracker.set(INVUL_TIMER, ticks);
   }
   
   public int getTrackedEntityId(int headIndex){
      return this.dataTracker.<Integer>get(TRACKED_ENTITY_IDS.get(headIndex));
   }
   
   public void setTrackedEntityId(int headIndex, int id){
      this.dataTracker.set(TRACKED_ENTITY_IDS.get(headIndex), id);
   }
   
   private double getHeadX(int headIndex){
      if(headIndex <= 0){
         return this.getX();
      }else{
         float f = (this.bodyYaw + (float)(180 * (headIndex - 1))) * (float) (Math.PI / 180.0);
         float g = MathHelper.cos(f);
         return this.getX() + (double)g * 1.3 * (double)this.getScale();
      }
   }
   
   private double getHeadY(int headIndex){
      float f = headIndex <= 0 ? 3.0F : 2.2F;
      return this.getY() + (double)(f * this.getScale());
   }
   
   private double getHeadZ(int headIndex){
      if(headIndex <= 0){
         return this.getZ();
      }else{
         float f = (this.bodyYaw + (float)(180 * (headIndex - 1))) * (float) (Math.PI / 180.0);
         float g = MathHelper.sin(f);
         return this.getZ() + (double)g * 1.3 * (double)this.getScale();
      }
   }
   
   public PlayerEntity getSummoner(){
      return this.summoner;
   }
   
   public boolean isExalted(){
      return this.isExalted;
   }
   
   
   // ========== Normal Mob Stuff ==========
   
   @Override
   public void setCustomName(@Nullable Text name){
      super.setCustomName(name);
      this.bossBar.setName(this.getDisplayName());
   }
   
   @Override
   protected SoundEvent getAmbientSound(){
      return SoundEvents.ENTITY_WITHER_AMBIENT;
   }
   
   @Override
   protected SoundEvent getHurtSound(DamageSource source){
      return SoundEvents.ENTITY_WITHER_HURT;
   }
   
   @Override
   protected SoundEvent getDeathSound(){
      return SoundEvents.ENTITY_WITHER_DEATH;
   }
   
   @Override
   public void slowMovement(BlockState state, Vec3d multiplier){}
   
   @Override
   public boolean canHaveStatusEffect(StatusEffectInstance effect){
      return effect.equals(ArcanaRegistry.DAMAGE_AMP_EFFECT);
   }
   
   @Override
   protected boolean canStartRiding(Entity entity){
      return false;
   }
   
   @Override
   public boolean canUsePortals(boolean allowVehicles){
      return false;
   }
   
   @Override
   public void writeCustomDataToNbt(NbtCompound nbt){
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("numPlayers",numPlayers);
      nbt.putInt("spellCooldown",spellCooldown);
      nbt.putBoolean("shouldHaveSummoner",shouldHaveSummoner);
      nbt.putBoolean("summonerHasDivine", summonerHasDivine);
      nbt.putBoolean("summonerHasWings",summonerHasWings);
      nbt.putBoolean("isExalted", isExalted);
      nbt.putFloat("prevHP",prevHP);
      nbt.putFloat("adaptiveResistance",adaptiveResistance);
      nbt.putInt("invulnerableTimer", this.getInvulnerableTimer());
      
      if(summoner != null){
         nbt.putString("summoner",summoner.getUuidAsString());
      }
      
      NbtCompound spellsTag = new NbtCompound();
      for(Map.Entry<ConstructSpellType, ConstructSpell> entry : spells.entrySet()){
         spellsTag.put(entry.getKey().id,entry.getValue().toNbt());
      }
      nbt.put("spells",spellsTag);
      
      NbtCompound adaptationsTag = new NbtCompound();
      adaptations.forEach((adaptation, bool) -> adaptationsTag.putBoolean(adaptation.id,bool));
      nbt.put("adaptations",adaptationsTag);
      
      NbtCompound blockDamageTag = new NbtCompound();
      blockDamage.forEach((block, damage) -> {
         NbtCompound blockTag = new NbtCompound();
         blockTag.putInt("x",block.getX());
         blockTag.putInt("y",block.getY());
         blockTag.putInt("z",block.getZ());
         blockTag.putInt("damage",damage);
      });
      nbt.put("blockDamage",blockDamageTag);
      
      NbtList playersTag = new NbtList();
      for(ServerPlayerEntity player : players){
         playersTag.add(NbtString.of(player.getUuidAsString()));
      }
      nbt.put("players",playersTag);
   }
   
   @Override
   public void readCustomDataFromNbt(NbtCompound nbt){
      super.readCustomDataFromNbt(nbt);
      numPlayers = nbt.getInt("numPlayers", 0);
      spellCooldown = nbt.getInt("spellCooldown", 0);
      shouldHaveSummoner = nbt.getBoolean("shouldHaveSummoner", false);
      summonerHasDivine = nbt.getBoolean("summonerHasDivine", false);
      summonerHasWings = nbt.getBoolean("summonerHasWings", false);
      isExalted = nbt.getBoolean("isExalted", false);
      prevHP = nbt.getFloat("prevHP", 0.0f);
      adaptiveResistance = nbt.getFloat("adaptiveResistance", 0.0f);
      
      this.setInvulTimer(nbt.getInt("invulnerableTimer", 0));
      
      if(this.hasCustomName()){
         this.bossBar.setName(this.getDisplayName());
      }
      
      if(nbt.contains("summoner")){
         if(getEntityWorld() instanceof ServerWorld serverWorld && serverWorld.getEntity(MiscUtils.getUUID(nbt.getString("summoner", ""))) instanceof PlayerEntity player){
            summoner = player;
         }
      }
      
      spells = new HashMap<>();
      NbtCompound spellsTag = nbt.getCompoundOrEmpty("spells");
      for(String key : spellsTag.getKeys()){
         spells.put(ConstructSpellType.fromString(key),ConstructSpell.fromNbt(spellsTag.getCompoundOrEmpty(key)));
      }
      
      adaptations = new HashMap<>();
      NbtCompound adaptationsTag = nbt.getCompoundOrEmpty("adaptations");
      for(String key : adaptationsTag.getKeys()){
         adaptations.put(ConstructAdaptations.fromString(key), adaptationsTag.getBoolean(key, false));
      }
      
      blockDamage = new HashMap<>();
      NbtCompound blockDamageTag = nbt.getCompoundOrEmpty("blockDamage");
      for(String key : blockDamageTag.getKeys()){
         NbtCompound compound = blockDamageTag.getCompoundOrEmpty(key);
         blockDamage.put(new BlockPos(compound.getInt("x", 0), compound.getInt("y", 0), compound.getInt("z", 0)), compound.getInt("damage", 0));
      }
      
      players = new ArrayList<>();
      NbtList playersList = nbt.getListOrEmpty("players");
      for(NbtElement nbtElement : playersList){
         if(getEntityWorld() instanceof ServerWorld serverWorld && serverWorld.getEntity(MiscUtils.getUUID(nbtElement.asString().orElse(""))) instanceof ServerPlayerEntity player){
            players.add(player);
         }
      }
   }
   
   @Override
   protected void removeFromDimension(){
      blockDamage.clear();
      super.removeFromDimension();
   }
   
   
   // ========== Summoning / Death ==========
   
   public void onSummoned(PlayerEntity summoner){
      this.onSummoned(summoner,false);
   }
   
   public void onSummoned(PlayerEntity summoner, boolean mythic){
      this.setInvulTimer(220);
      this.bossBar.setPercent(0.0F);
      this.setHealth(this.getMaxHealth() / 3.0F);
      if(!(getEntityWorld() instanceof ServerWorld serverWorld)) return;
      this.summoner = summoner;
      this.shouldHaveSummoner = true;
      this.isExalted = mythic;
      if(summoner instanceof ServerPlayerEntity serverPlayer) this.players.add(serverPlayer);
      this.numPlayers = 1;
      
      MutableText witherName;
      if(isExalted){
         witherName = Text.literal("")
               .append(Text.literal("❖").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
               .append(Text.literal("▓").formatted(Formatting.DARK_GRAY, Formatting.BOLD, Formatting.OBFUSCATED))
               .append(Text.literal("❖").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
               .append(Text.literal(" "))
               .append(Text.literal("Exalted Nul Construct").formatted(Formatting.DARK_GRAY, Formatting.BOLD, Formatting.UNDERLINE))
               .append(Text.literal(" "))
               .append(Text.literal("❖").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
               .append(Text.literal("▓").formatted(Formatting.DARK_GRAY, Formatting.BOLD, Formatting.OBFUSCATED))
               .append(Text.literal("❖").formatted(Formatting.DARK_GRAY, Formatting.BOLD));
         ParticleEffectUtils.exaltedConstructSummon(serverWorld,getPos().add(0,0,0),0);
         
         EntityAttributeInstance entityAttributeInstance = getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
         EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(Identifier.of(MOD_ID,"exalted"), 15.0f, EntityAttributeModifier.Operation.ADD_VALUE);
         if(entityAttributeInstance != null && !entityAttributeInstance.hasModifier(Identifier.of(MOD_ID,"exalted"))) entityAttributeInstance.addPersistentModifier(entityAttributeModifier);
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
      
      summonerHasWings = ArcanaNovum.data(summoner).hasCrafted(ArcanaRegistry.WINGS_OF_ENDERIA);
      summonerHasDivine = summonerHasWings || ArcanaNovum.data(summoner).hasCrafted(ArcanaRegistry.DIVINE_CATALYST);
      PlayerInventory inv = summoner.getInventory();
      for(int i = 0; i < inv.size(); i++){
         ItemStack stack = inv.getStack(i);
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
         if(arcanaItem == null) continue;
         if(arcanaItem.getRarity() == ArcanaRarity.DIVINE) summonerHasDivine = true;
      }
      
      prevHP = getHealth();
      
      NulConstructDialog.announce(summoner.getServer(),summoner,this, Announcements.SUMMON_TEXT);
      NulConstructEntity construct = this;
      ArcanaNovum.addTickTimerCallback(serverWorld, new GenericTimer(this.getInvulnerableTimer(), () -> {
         NulConstructDialog.announce(summoner.getServer(),summoner,construct, Announcements.SUMMON_DIALOG, new boolean[]{summonerHasDivine,summonerHasWings,!summonerHasWings, false, true, isExalted, !isExalted});
         setHealth(getMaxHealth());
      }));
   }
   
   @Override
   public void onDeath(DamageSource damageSource){
      super.onDeath(damageSource);
      
      MinecraftServer server = getServer();
      if(server == null) return;
      
      if(isExalted){ // TODO proper loot table?
         dropItem(getWorld(),Items.NETHER_STAR.getDefaultStack().copyWithCount(24),getPos());
         dropItem(getWorld(), new ItemStack(ArcanaRegistry.DIVINE_ARCANE_PAPER,this.random.nextBetween(8,16)),getPos());
      }else{
         for(int i = 0; i < this.random.nextBetween(4,16); i++){
            ItemStack stack = Items.NETHER_STAR.getDefaultStack().copy();
            dropItem(getWorld(),stack,getPos());
            dropItem(getWorld(),stack.copyWithCount(1),getPos());
         }
         dropItem(getWorld(),new ItemStack(ArcanaRegistry.DIVINE_ARCANE_PAPER,this.random.nextBetween(4,12)),getPos());
      }
      
      if(summoner == null) return;
      
      boolean dropped = isExalted || this.random.nextFloat() < 0.01;
      
      if(dropped){
         ItemStack stack = ArcanaRegistry.NUL_MEMENTO.addCrafter(ArcanaRegistry.NUL_MEMENTO.getNewItem(),summoner.getUuidAsString(),false,server);
         ArcanaNovum.data(summoner).addCraftedSilent(stack);
         dropItem(getWorld(), stack.copyWithCount(1),getPos());
      }
      
      if(!isExalted){
         ItemStack stack = ArcanaRegistry.DIVINE_CATALYST.addCrafter(ArcanaRegistry.DIVINE_CATALYST.getNewItem(),summoner.getUuidAsString(),false,server);
         ArcanaNovum.data(summoner).addCraftedSilent(stack);
         dropItem(getWorld(), stack.copyWithCount(1),getPos());
      }
      
      NulConstructDialog.announce(server,summoner,this, Announcements.SUCCESS, new boolean[]{summonerHasDivine,summonerHasWings,!summonerHasWings, dropped, !dropped, isExalted, !isExalted});
      
      if(summoner instanceof ServerPlayerEntity player){
         ArcanaAchievements.grant(player,ArcanaAchievements.CONSTRUCT_DECONSTRUCTED.id);
         if(dropped){
            ArcanaAchievements.grant(player,ArcanaAchievements.DIVINE_FAVOR.id);
         }
      }
   }
   
   public void deconstruct(){
      if(summoner != null){
         NulConstructDialog.announce(getServer(),summoner,this, Announcements.FAILURE,new boolean[]{summonerHasDivine,summonerHasWings,!summonerHasWings, false, true, isExalted, !isExalted});
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
   
   public static BlockPattern getConstructPattern(){
      return BlockPatternBuilder.start().aisle("^^^", "#@#", "~#~")
            .where('#', (pos) -> pos.getBlockState().isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
            .where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL))))
            .where('~', CachedBlockPosition.matchesBlockState(AbstractBlock.AbstractBlockState::isAir))
            .where('@', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.NETHERITE_BLOCK)))
            .build();
   }
   
   
   // ========== Damage ==========
   
   @Override
   public boolean damage(ServerWorld world, DamageSource source, float amount){
      if(this.isInvulnerableTo(world, source)){
         return false;
      } else if(source.isIn(ArcanaRegistry.NUL_CONSTRUCT_IMMUNE_TO) || source.getAttacker() instanceof NulConstructEntity){
         return false;
      } else if(this.getInvulnerableTimer() > 0 && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)){
         return false;
      }else{
         if(this.isShieldActive()){
            Entity entity = source.getSource();
            if(entity instanceof PersistentProjectileEntity || entity instanceof WindChargeEntity){
               return false;
            }
         }
         
         Entity entity = source.getAttacker();
         if(entity != null && entity.getType().isIn(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS)){
            return false;
         }else{
            if(entity instanceof ServerPlayerEntity player && !this.players.contains(player)){
               triggerAdaptation(ConstructAdaptations.ADDITIONAL_PLAYERS);
               this.players.add(player);
               this.numPlayers = players.size();
               this.adaptiveResistance += 0.05f;
            }
            
            for (int i = 0; i < this.chargedSkullCooldowns.length; i++){
               this.chargedSkullCooldowns[i] = this.chargedSkullCooldowns[i] + 3;
            }
            
            return super.damage(world, source, amount);
         }
      }
   }
   
   @Override
   protected float modifyAppliedDamage(DamageSource source, float amount){
      float modified = super.modifyAppliedDamage(source, amount);
      if(source.isSourceCreativePlayer() || source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return modified;
      double healthPercent = getHealth() / getMaxHealth();
      
      modified *= isExalted ? 0.6f : 0.75f;
      modified *= 1.0f - Math.min(0.5f,adaptiveResistance);
      
      if(modified > 100){
         modified = 100;
         triggerAdaptation(ConstructAdaptations.MASSIVE_BLOW);
         adaptiveResistance += 0.05f;
      }
      
      if(source.isIn(ArcanaRegistry.NUL_CONSTRUCT_VULNERABLE_TO)) modified *= isExalted ? 1.25f : 1.5f;
      if(source.isIn(ArcanaRegistry.NUL_CONSTRUCT_RESISTANT_TO)) modified *= isExalted ? 0.35f : 0.5f;
      if(healthPercent > 0.5 && source.isOf(DamageTypes.PLAYER_ATTACK)) modified *= isExalted ? 1.25f : 1.5f;
      if(healthPercent < 0.5 && source.getSource() instanceof PersistentProjectileEntity) modified *= isExalted ? 0.35f : 0.5f;
      
      if(source.getWeaponStack() != null && (source.getWeaponStack().isOf(Items.MACE) || source.getWeaponStack().isOf(ArcanaRegistry.GRAVITON_MAUL.getItem()))){
         triggerAdaptation(ConstructAdaptations.DAMAGED_BY_MACE);
      }
      
      if(this.isReflectionActive()){
         Entity attacker = source.getAttacker();
         if(attacker instanceof LivingEntity){
            if (this.getWorld() instanceof ServerWorld serverWorld) {
               attacker.damage(serverWorld, getDamageSources().thorns(this), amount * 0.5f);
            }
            conversionHeal(amount*0.25f);
         }
      }
      
      return modified;
   }
   
   
   // ========== Behavior ==========
   
   public void triggerAdaptation(ConstructAdaptations adaptation){
      if(!hasActivatedAdaptation(adaptation) || adaptation.repeatable){
         NulConstructDialog.abilityText(summoner,this,adaptation.abilityTexts[(int) (Math.random()*adaptation.abilityTexts.length)]);
      }
      adaptations.put(adaptation,true);
   }
   
   public boolean hasActivatedAdaptation(ConstructAdaptations adaptation){
      return adaptations.getOrDefault(adaptation,false);
   }
   
   private boolean shouldChangeMovementType(){
      if(movementType == ConstructMovementType.WAIT && getTarget() != null) return true;
      if(movementType == ConstructMovementType.SPAWNING && this.getInvulnerableTimer() <= 0) return true;
      if(movementType == ConstructMovementType.LASER && !spells.get(ConstructSpellType.WITHERING_RAY).isActive()) return true;
      if(movementType == ConstructMovementType.CHARGE && !spells.get(ConstructSpellType.RELENTLESS_ONSLAUGHT).isActive()) return true;
      return this.age > this.movementChangeTime;
   }
   
   @Override
   protected void mobTick(ServerWorld world){
      try{
         if(spells == null || spells.isEmpty()) createSpells();
         if(!initializedAttributes) initializeAttributes();
         
         if(this.getInvulnerableTimer() > 0){
            int i = this.getInvulnerableTimer() - 1;
            this.bossBar.setPercent(1.0F - (float)i / 220.0F);
            if(i <= 0){
               world.createExplosion(this, this.getX(), this.getEyeY(), this.getZ(), 7.0F, false, World.ExplosionSourceType.MOB);
               if(!this.isSilent()){
                  world.syncGlobalEvent(WorldEvents.WITHER_SPAWNS, this.getBlockPos(), 0);
               }
            }
            
            this.setInvulTimer(i);
            if(this.age % 10 == 0){
               this.heal(35.0F);
            }
         }else{
            super.mobTick(world);
            
            for (int ix = 1; ix < 3; ix++){
               if(this.age >= this.skullCooldowns[ix - 1]){
                  this.skullCooldowns[ix - 1] = this.age + 10 + this.random.nextInt(10);
                  if((world.getDifficulty() == Difficulty.NORMAL || world.getDifficulty() == Difficulty.HARD)
                        && this.chargedSkullCooldowns[ix - 1]++ > 15){
                     float f = 10.0F;
                     float g = 5.0F;
                     double d = MathHelper.nextDouble(this.random, this.getX() - f, this.getX() + f);
                     double e = MathHelper.nextDouble(this.random, this.getY() - g, this.getY() + g);
                     double h = MathHelper.nextDouble(this.random, this.getZ() - f, this.getZ() + f);
                     this.shootSkullAt(ix + 1, d, e, h, true);
                     this.chargedSkullCooldowns[ix - 1] = 0;
                  }
                  
                  int j = this.getTrackedEntityId(ix);
                  if(j > 0){
                     LivingEntity livingEntity = (LivingEntity)this.getWorld().getEntityById(j);
                     if(livingEntity != null && this.canTarget(livingEntity) && !(this.squaredDistanceTo(livingEntity) > 900.0) && this.canSee(livingEntity)){
                        this.shootSkullAt(ix + 1, livingEntity);
                        this.skullCooldowns[ix - 1] = this.age + 40 + this.random.nextInt(20);
                        this.chargedSkullCooldowns[ix - 1] = 0;
                     }else{
                        this.setTrackedEntityId(ix, 0);
                     }
                  }else{
                     List<LivingEntity> list = world.getTargets(LivingEntity.class, HEAD_TARGET_PREDICATE, this, this.getBoundingBox().expand(20.0, 8.0, 20.0));
                     if(!list.isEmpty()){
                        LivingEntity livingEntity2 = (LivingEntity)list.get(this.random.nextInt(list.size()));
                        this.setTrackedEntityId(ix, livingEntity2.getId());
                     }
                  }
               }
            }
            
            if(this.getTarget() != null){
               this.setTrackedEntityId(0, this.getTarget().getId());
            }else{
               this.setTrackedEntityId(0, 0);
            }
            
            if(this.age % 20 == 0){
               if(isConversionActive()){
                  this.heal(isExalted ? 10.0F : 5.0F);
               }else{
                  this.heal(isExalted ? 2.5F : 1.0F);
               }
            }
            
            this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
         }
         
         if(shouldHaveSummoner){
            if(summoner == null || summoner.isDead() || !summoner.getWorld().getRegistryKey().equals(getWorld().getRegistryKey())){
               deconstruct();
            }
            if(distanceTo(summoner) >= FIGHT_RANGE){
               spells.get(ConstructSpellType.SHADOW_SHROUD).setCooldown(0);
               castSpell(spells.get(ConstructSpellType.SHADOW_SHROUD));
            }
            if(summoner.hasStatusEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT)){
               triggerAdaptation(ConstructAdaptations.TRUE_INVISIBILITY);
               StatusEffectInstance blindness = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT,110,3,false,false,false);
               summoner.addStatusEffect(blindness);
               summoner.removeStatusEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT);
            }
         }else if(this.getTarget() != null && this.getTarget() instanceof ServerPlayerEntity player){
            this.summoner = player;
            this.shouldHaveSummoner = true;
            triggerAdaptation(ConstructAdaptations.SPAWNED_CONSTRUCT_FINDS_PLAYER);
         }
         
         if(hasActivatedAdaptation(ConstructAdaptations.DAMAGED_BY_MACE)){
            for(ServerPlayerEntity player : players){
               if(player.getWorld().getRegistryKey().equals(getWorld().getRegistryKey()) && player.getY() > getY()+10){
                  ItemStack weapon = player.getWeaponStack();
                  if(weapon.isOf(Items.MACE) || weapon.isOf(ArcanaRegistry.GRAVITON_MAUL.getItem())){
                     castSpell(spells.get(ConstructSpellType.SHADOW_SHROUD));
                     spells.get(ConstructSpellType.SHADOW_SHROUD).setCooldown(80);
                     break;
                  }
               }
            }
         }
         
         if(this.age % 20 == 0){
            if(world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)){
               destructiveAura();
            }
            if(isExalted){
               List<PlayerEntity> players = getWorld().getEntitiesByType(EntityType.PLAYER,getBoundingBox().expand(FIGHT_RANGE),(e) -> true);
               
               for(PlayerEntity player : players){
                  StatusEffectInstance amp = new StatusEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT, 100, 1, false, true, true);
                  player.addStatusEffect(amp);
               }
            }
         }
         
         if(spellCooldown > 0) spellCooldown--;
         for(ConstructSpell spell : spells.values()){
            if(spell.isActive()) tickSpell(spell);
            if(spell.getCooldown() > 0) spell.setCooldown(spell.getCooldown() - 1);
         }
         
         float curHP = getHealth();
         DamageSource recentDamage = getRecentDamageSource();
         
         if(recentDamage != null && recentDamage.isOf(DamageTypes.IN_WALL) && spells.get(ConstructSpellType.SHADOW_SHROUD).getCooldown() <= 0){
            castSpell(spells.get(ConstructSpellType.SHADOW_SHROUD));
         }else if((int)(curHP*4/(getMaxHealth())) < (int)(prevHP*4/(getMaxHealth())) && spells.get(ConstructSpellType.REFLEXIVE_BLAST).getCooldown() <= 0){
            castSpell(spells.get(ConstructSpellType.REFLEXIVE_BLAST));
         }else if(getInvulnerableTimer() <= 0 && spellCooldown <= 0){
            castSpell(spells.get(getWeightedResult(buildWeightedSpellList())));
         }
         
         if(!blockPacketQueue.isEmpty()) sendBlockBreakPackets();
         
         prevHP = curHP;
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void tickMovement(){
      this.tickCustomAI();
      
      super.tickMovement();
      
      if(this.getInvulnerableTimer() > 0){
         movementType = ConstructMovementType.SPAWNING;
      }else if(spells.get(ConstructSpellType.WITHERING_RAY).isActive()){
         movementType = ConstructMovementType.LASER;
      }else if(spells.get(ConstructSpellType.RELENTLESS_ONSLAUGHT).isActive()){
         movementType = ConstructMovementType.CHARGE;
      }else if(shouldChangeMovementType()){
         this.movementChangeTime = this.age + this.random.nextBetween(200,500);
         
         float healthThreshold = (getHealth() / getMaxHealth()) * 0.6f + 0.2f;
         float random = this.random.nextFloat();
         
         if(random > healthThreshold){
            this.movementType = ConstructMovementType.MELEE_PURSUIT;
         }else{
            this.movementType = this.random.nextFloat() > ((shouldHaveSummoner && summoner != null && distanceTo(summoner) > RAY_RANGE * 0.8) ? 0.2 : 0.8) ? ConstructMovementType.RANGED_PURSUIT : ConstructMovementType.STRAFE;
         }
      }
      
      boolean shieldActive = this.isShieldActive();
      
      for (int jx = 0; jx < 3; jx++){
         double p = this.getHeadX(jx);
         double q = this.getHeadY(jx);
         double r = this.getHeadZ(jx);
         float s = 0.3F * this.getScale();
         this.getWorld().addParticleClient(ParticleTypes.SMOKE,p + this.random.nextGaussian() * (double)s,q + this.random.nextGaussian() * (double)s,r + this.random.nextGaussian() * (double)s,0.0,0.0,0.0);
         if(shieldActive && this.getWorld().random.nextInt(4) == 0){
            EntityEffectParticleEffect particle = EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, 0.7F, 0.7F, 0.5F);
            this.getWorld().addParticleClient(particle,p + this.random.nextGaussian() * (double)s,q + this.random.nextGaussian() * (double)s,r + this.random.nextGaussian() * (double)s,0.0,0.0,0.0);
         }
      }
      
      if(this.getInvulnerableTimer() > 0){
         float t = 3.3F * this.getScale();
         for (int u = 0; u < 3; u++){
            EntityEffectParticleEffect particle = EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, 0.7F, 0.7F, 0.9F);
            this.getWorld().addParticleClient(particle,this.getX() + this.random.nextGaussian(),this.getY() + (double)(this.random.nextFloat() * t),this.getZ() + this.random.nextGaussian(), 0.0, 0.0, 0.0);
         }
      }
   }
   
   private void tickCustomAI(){
      if(!(this.getWorld() instanceof ServerWorld serverWorld)) return;
      this.setNoGravity(true);
      double speed = getAttributeValue(EntityAttributes.FLYING_SPEED);
      
      if(this.movementType == ConstructMovementType.SPAWNING){ // Do nothing, stay still
         if(this.summoner != null){
            this.lookAtEntity(this.summoner,30f,30f);
         }
         setVelocity(0,0,0);
         return;
      }else if(this.movementType == ConstructMovementType.WAIT){
         this.acquireTargetCooldown = 0;
         this.targetPosition = this.getPos();
         this.circlingCenter = this.getBlockPos();
      }
      
      if(this.acquireTargetCooldown-- <= 0){
         List<LivingEntity> targets = this.getPrioritizedTargets();
         
         int headIndex = 0;
         boolean initSet = false;
         for(LivingEntity target : targets){
            if(headIndex > 2) break;
            if(!initSet){
               this.setTarget(target);
               this.setTrackedEntityId(0,target.getId());
               this.setTrackedEntityId(1,target.getId());
               this.setTrackedEntityId(2,target.getId());
               initSet = true;
            }
            if(distanceTo(target) <= RAY_RANGE){
               this.setTrackedEntityId(headIndex, target.getId());
               headIndex++;
            }
         }
         
         if(initSet) this.acquireTargetCooldown = 20;
      }
      
      if(this.getTarget() == null){
         this.movementType = ConstructMovementType.WAIT;
      }else{
         this.lookAtEntity(this.getTarget(),30.0f,30.0f);
      }
      
      if(this.movementType == ConstructMovementType.CHARGE || this.movementType == ConstructMovementType.MELEE_PURSUIT){
         double sqrDistToTarget = this.squaredDistanceTo(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
         double attackRange = (double)(this.getWidth() * 2.0F * this.getWidth() * 2.0F);
         
         if(this.isExalted){
            List<PlayerEntity> players = getWorld().getEntitiesByType(EntityType.PLAYER,getBoundingBox().expand(FIGHT_RANGE),(e) -> true);
            StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, 30, 20, false, true, true);
            players.forEach(p -> p.addStatusEffect(blind));
         }
         
         this.attackCooldown = Math.max(this.attackCooldown - 1, 0);
         if(!(sqrDistToTarget > attackRange)){
            if(this.attackCooldown == 0){
               this.attackCooldown = 15;
               this.tryAttack(serverWorld, this.getTarget());
               StatusEffectInstance wither = new StatusEffectInstance(StatusEffects.WITHER, 80, 0, false, true, true);
               this.getTarget().addStatusEffect(wither);
            }
         }
         
         speed *= 1.5;
         this.targetPosition = this.getTarget().getPos();
      }else if(this.movementType == ConstructMovementType.RANGED_PURSUIT){
         double sqrDistToTarget = this.squaredDistanceTo(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
         if(sqrDistToTarget < (RAY_RANGE * RAY_RANGE) && this.getVisibilityCache().canSee(this.getTarget())){
            this.attackCooldown = Math.max(this.attackCooldown - 1, 0);
            if(this.attackCooldown == 0){
               this.attackCooldown = 45;
               this.shootAt(this.getTarget(), 1);
            }
         }
         
         Vec3d targetDiff = this.getTarget().getPos().subtract(this.getPos());
         double lengthDiff = targetDiff.length() - RAY_RANGE*0.5;
         Vec3d newTargetPos = this.getPos().add(targetDiff.normalize().multiply(lengthDiff));
         this.targetPosition = new Vec3d(newTargetPos.x,this.getTarget().getY()+strafeHeight,newTargetPos.z);
      }else if(this.movementType == ConstructMovementType.STRAFE){
         this.circlingCenter = this.getTarget().getBlockPos();
         int up = 0;
         while(this.getWorld().getBlockState(this.circlingCenter.up()).isAir() && up < strafeHeight){
            this.circlingCenter = this.circlingCenter.up();
            up++;
         }
         
         if(this.age % 100 == 0){
            this.strafeRadius = this.random.nextFloat()*8 + 8f;
            this.strafeRate = (this.random.nextFloat()*4 + 0.5f);
            this.strafeRate *= this.random.nextBoolean() ? -1 : 1;
         }
         
         this.strafeYaw = MathHelper.wrapDegrees(this.strafeYaw + this.strafeRate);
         Vec3d circleOffset = new Vec3d(Math.cos(Math.toRadians(this.strafeYaw)), 0, Math.sin(Math.toRadians(this.strafeYaw))).multiply(this.strafeRadius);
         this.targetPosition = circleOffset.add(this.circlingCenter.toCenterPos());
         
         double sqrDistToTarget = this.squaredDistanceTo(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
         if(sqrDistToTarget < (RAY_RANGE * RAY_RANGE) && this.getVisibilityCache().canSee(this.getTarget())){
            this.attackCooldown = Math.max(this.attackCooldown - 1, 0);
            if(this.attackCooldown == 0){
               this.attackCooldown = 45;
               this.shootAt(this.getTarget(), 1);
            }
         }
      }else if(this.movementType == ConstructMovementType.LASER){
         double sqrDistToPosition = this.squaredDistanceTo(this.targetPosition);
         
         if(sqrDistToPosition <= 4){
            this.lookAtEntity(this.getTarget(),360.0f,360.0f);
            for (int i = 0; i < 3; i++){
               int id = this.getTrackedEntityId(i);
               if(id <= 0) continue;
               
               LivingEntity livingEntity = (LivingEntity)serverWorld.getEntityById(id);
               if(livingEntity != null && this.canTarget(livingEntity) && (this.squaredDistanceTo(livingEntity) < (RAY_RANGE*RAY_RANGE))){
                  Vec3d headPos = new Vec3d(getHeadX(i),getHeadY(i),getHeadZ(i));
                  MiscUtils.LasercastResult lasercast = MiscUtils.lasercast(serverWorld, headPos, livingEntity.getPos().subtract(headPos).normalize(), RAY_RANGE, true, this);
                  if(this.age % 10 == 0){
                     float damage = this.isExalted ? 2f : 4f;
                     
                     for(Entity hit : lasercast.sortedHits()){
                        if(!(hit instanceof LivingEntity livingHit) || hit.getType().isIn(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS)) continue;
                        if(hit instanceof ServerPlayerEntity hitPlayer && hitPlayer.isBlocking()){
                           double dp = hitPlayer.getRotationVecClient().normalize().dotProduct(lasercast.direction().normalize());
                           if(dp < -0.6){
                              MiscUtils.blockWithShield(hitPlayer,damage);
                              continue;
                           }
                        }
                        
                        hit.damage(serverWorld, ArcanaDamageTypes.of(getEntityWorld(),ArcanaDamageTypes.NUL,this), damage);
                        StatusEffectInstance wither = new StatusEffectInstance(StatusEffects.WITHER, isExalted ? 100 : 40, 1, false, true, true);
                        StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, 40, 25, false, true, true);
                        StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, isExalted ? 100 : 40, 1, false, true, true);
                        StatusEffectInstance fatigue = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, isExalted ? 100 : 40, 2, false, true, true);
                        StatusEffectInstance weakness = new StatusEffectInstance(StatusEffects.WEAKNESS, isExalted ? 100 : 40, 1, false, true, true);
                        if(isExalted) livingHit.addStatusEffect(blind);
                        livingHit.addStatusEffect(slow);
                        livingHit.addStatusEffect(fatigue);
                        livingHit.addStatusEffect(weakness);
                        livingHit.addStatusEffect(wither);
                        conversionHeal(damage*0.8f);
                     }
                  }
                  
                  if(this.age % 3 == 0){
                     ParticleEffect dust = new DustParticleEffect(ArcanaColors.NUL_COLOR,1.5f);
                     int intervals = (int)(lasercast.startPos().subtract(lasercast.endPos()).length() * 4);
                     ParticleEffectUtils.line(serverWorld,null,lasercast.startPos(),lasercast.endPos(),dust,intervals,1,0.08,0);
                  }
                  
                  if(this.age % 5 == 0){
                     for (int xOff = -1; xOff <= 1; ++xOff){
                        for (int zOff = -1; zOff <= 1; ++zOff){
                           for (int yOff = -1; yOff <= 1; ++yOff){
                              int x = (int) (lasercast.endPos().x + xOff);
                              int y = (int) (lasercast.endPos().y + yOff);
                              int z = (int) (lasercast.endPos().z + zOff);
                              BlockPos blockPos = new BlockPos(x, y, z);
                              int blockDamage = (int) (2.5 - (0.8 * (xOff*xOff + zOff*zOff + yOff*yOff)));
                              damageBlock(blockPos,blockDamage);
                           }
                        }
                     }
                  }
               }else{
                  this.setTrackedEntityId(i, 0);
               }
            }
         }
      }
      
      Vec3d delta = this.targetPosition.subtract(this.getPos());
      speed *= 1 / (1 + Math.exp(-0.25*(delta.length() - 8))); // Sigmoid velocity scaling based on proximity
      setVelocity(delta.normalize().multiply(speed));
   }
   
   @Override
   protected EntityNavigation createNavigation(World world){
      BirdNavigation birdNavigation = new BirdNavigation(this, world);
      birdNavigation.setCanPathThroughDoors(false);
      birdNavigation.setCanSwim(true);
      birdNavigation.setCanPathThroughDoors(true);
      return birdNavigation;
   }
   
   enum ConstructMovementType {
      LASER,
      STRAFE,
      CHARGE,
      RANGED_PURSUIT,
      MELEE_PURSUIT,
      WAIT,
      SPAWNING;
   }
   
   // ========== Attacking ==========
   
   public HashSet<ServerPlayerEntity> getParticipatingPlayers(){
      DamageTracker tracker = this.getDamageTracker();
      HashSet<ServerPlayerEntity> participatingPlayers = new HashSet<>();
      for(DamageRecord damageRecord : tracker.recentDamage){
         if(damageRecord.damageSource().getAttacker() instanceof ServerPlayerEntity player){
            participatingPlayers.add(player);
         }
      }
      return participatingPlayers;
   }
   
   private List<LivingEntity> getPrioritizedTargets(){
      if(!(getWorld() instanceof ServerWorld serverWorld)) return new ArrayList<>();
      List<LivingEntity> targets = new ArrayList<>(getEntityWorld().getEntitiesByClass(LivingEntity.class, this.getHitbox().expand(FIGHT_RANGE), e -> CAN_ATTACK_PREDICATE.test(e,serverWorld)));
      HashSet<ServerPlayerEntity> participatingPlayers = getParticipatingPlayers();
      targets.sort(Comparator.comparingDouble(entity -> {
         double distVal = entity.distanceTo(this);
         if(entity instanceof ServerPlayerEntity serverPlayer){
            if(summoner != null && summoner.getUuidAsString().equals(serverPlayer.getUuidAsString())){
               distVal -= FIGHT_RANGE * 10;
            }else if(participatingPlayers.contains(serverPlayer)){
               distVal -= FIGHT_RANGE * 5;
            }else{
               distVal -= FIGHT_RANGE * 3;
            }
         }
         return distVal;
      }));
      return targets;
   }
   
   private void shootSkullAt(int headIndex, double targetX, double targetY, double targetZ, boolean charged){
      if(!this.isSilent()){
         this.getWorld().syncWorldEvent(null, WorldEvents.WITHER_SHOOTS, this.getBlockPos(), 0);
      }
      
      double d = this.getHeadX(headIndex);
      double e = this.getHeadY(headIndex);
      double f = this.getHeadZ(headIndex);
      double g = targetX - d;
      double h = targetY - e;
      double i = targetZ - f;
      Vec3d vec3d = new Vec3d(g, h, i);
      WitherSkullEntity witherSkullEntity = new WitherSkullEntity(this.getWorld(), this, vec3d.normalize());
      witherSkullEntity.setOwner(this);
      if(charged){
         witherSkullEntity.setCharged(true);
      }
      
      witherSkullEntity.setPos(d, e, f);
      this.getWorld().spawnEntity(witherSkullEntity);
   }
   
   private void shootSkullAt(int headIndex, LivingEntity target){
      this.shootSkullAt(headIndex, target.getX(), target.getY() + (double)target.getStandingEyeHeight() * 0.5, target.getZ(), headIndex == 0 && this.random.nextFloat() < 0.001F);
   }
   
   @Override
   public void shootAt(LivingEntity target, float pullProgress){
      this.shootSkullAt(0, target);
   }
   
   private boolean damageBlock(BlockPos pos, int damage){
      if(damage <= 0) return false;
      BlockState blockState = this.getWorld().getBlockState(pos);
      if(!canDestroy(blockState)) return false;
      
      
      
      boolean blocked = true;
      for (Direction direction : Direction.values()){
         Vec3d vec3d3 = this.getPos().offset(direction, 1.0E-5F);
         if(getWorld().raycast(new BlockStateRaycastContext(vec3d3,pos.toCenterPos(), state -> state.isIn(BlockTags.WITHER_IMMUNE))).getType() != HitResult.Type.BLOCK){
            blocked = false;
            break;
         }
      }
      if(blocked){
         return false;
      }
      
      int maxDmg = (int) Math.ceil(5*Math.log10(Math.max(1,blockState.getBlock().getHardness()+1)));
      int curDmg = blockDamage.getOrDefault(pos,0);
      
      if(curDmg + damage > maxDmg){
         // Break
         boolean broken = this.getWorld().breakBlock(pos, true, this);
         if(broken){
            this.getWorld().syncWorldEvent(null, WorldEvents.WITHER_BREAKS_BLOCK, this.getBlockPos(), 0);
            blockDamage.remove(new BlockPos(pos.getX(),pos.getY(),pos.getZ()));
            blockPacketQueue.add(new Pair<>(pos,0));
            return true;
         }
      }else{
         int dmgLvl = (int) Math.ceil(9.0 * (double) (curDmg+damage) / maxDmg); // Breaking range 0 - 9
         blockPacketQueue.add(new Pair<>(pos,dmgLvl));
         blockDamage.put(new BlockPos(pos.getX(),pos.getY(),pos.getZ()),curDmg+damage);
      }
      
      return false;
   }
   
   public static boolean canDestroy(BlockState block){
      return !block.isAir() && !block.isIn(BlockTags.WITHER_IMMUNE);
   }
   
   private void sendBlockBreakPackets(){
      if(!(getWorld() instanceof ServerWorld serverWorld)) return;
      
      int toSend = Math.min(4096, blockPacketQueue.size());
      for (ServerPlayerEntity serverPlayerEntity : serverWorld.getServer().getPlayerManager().getPlayerList()) {
         if (serverPlayerEntity != null && serverPlayerEntity.getWorld() == serverWorld && serverPlayerEntity.getId() != this.getId() && serverPlayerEntity.distanceTo(this) <= FIGHT_RANGE) {
            List<Packet<? super ClientPlayPacketListener>> list = new ArrayList<>();
            for(int i = 0; i < toSend; i++){
               BlockPos pos = blockPacketQueue.get(i).getLeft();
               int prog = blockPacketQueue.get(i).getRight();
               list.add(new BlockBreakingProgressS2CPacket(this.random.nextInt(), pos, prog));
            }
            serverPlayerEntity.networkHandler.sendPacket(new BundleS2CPacket(list));
         }
      }
      
      for(int i = 0; i < toSend; i++){
         blockPacketQueue.removeFirst();
      }
   }
   
   
   // ========== Spell Stuff ==========
   
   private void tickSpell(ConstructSpell spell){
      int tick = spell.tick();
      if(!(getEntityWorld() instanceof ServerWorld world)) return;
      Vec3d pos = getPos();
      
      if(spell.getType() == ConstructSpellType.CURSE_OF_DECAY){
         if(tick % 12 == 0){
            List<Entity> entities = world.getOtherEntities(this,getBoundingBox().expand(DECAY_RANGE*2), e -> !e.isSpectator() && e.distanceTo(this) < DECAY_RANGE && (e instanceof LivingEntity) && !e.getType().isIn(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS));
            for(Entity entity1 : entities){
               if(!(entity1 instanceof LivingEntity living)) continue;
               float dmg = living.getMaxHealth() / 15.0f;
               float mod = living instanceof ServerPlayerEntity ? 0.35f : 0.75f;
               living.damage(world, ArcanaDamageTypes.of(this.getWorld(),ArcanaDamageTypes.NUL,this),dmg);
               
               conversionHeal(dmg*mod);
               StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS,isExalted ? 500 : 20,1,false,true,true);
               StatusEffectInstance weak = new StatusEffectInstance(StatusEffects.WEAKNESS,isExalted ? 500 : 20,1,false,true,true);
               StatusEffectInstance wither = new StatusEffectInstance(StatusEffects.WITHER, isExalted ? 250 : 40, 1, false, true, true);
               StatusEffectInstance fatigue = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, isExalted ? 500 : 20, 2, false, true, true);
               living.addStatusEffect(wither);
               living.addStatusEffect(slow);
               living.addStatusEffect(weak);
               living.addStatusEffect(fatigue);
               
               ParticleEffectUtils.nulConstructCurseOfDecay(world,entity1.getPos());
            }
         }
      }else if(spell.getType() == ConstructSpellType.WITHERING_RAY){
         // Handled in AI method
      }else if(spell.getType() == ConstructSpellType.NECROTIC_CONVERSION){
         ParticleEffectUtils.nulConstructNecroticConversion(world,getPos());
      }else if(spell.getType() == ConstructSpellType.REFLECTIVE_ARMOR){
         ParticleEffectUtils.nulConstructReflectiveArmor(world,getPos());
      }else if(spell.getType() == ConstructSpellType.RELENTLESS_ONSLAUGHT){
         if(tick % 15 == 0){
            List<Entity> entities = world.getOtherEntities(this,getBoundingBox().expand(DECAY_RANGE*2), e -> !e.isSpectator() && e.distanceTo(this) < DECAY_RANGE && (e instanceof LivingEntity));
            
            for(Entity entity1 : entities){
               if(!(entity1 instanceof LivingEntity living)) continue;
               if(!isInAttackRange(living)) continue;
               
               Vec3d dirToEntity = entity1.getPos().subtract(getPos()).normalize();
               double dp = dirToEntity.dotProduct(getRotationVector());
               if(Math.toDegrees(Math.acos(dp)) <= 60.0){
                  this.tryAttack(world, this.getTarget());
               }
            }
            SoundUtils.playSound(getWorld(),getBlockPos(),SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,SoundCategory.HOSTILE,0.75f,(float)(this.random.nextFloat()*0.5 + 0.75));
         }
         ParticleEffectUtils.nulConstructChargeAttack(world,getPos(),getYaw());
      }
   }
   
   private void castSpell(ConstructSpell spell){
      if(spell == null || !(getWorld() instanceof ServerWorld world)) return;
      if(spell.getCooldown() > 0) return;
      Vec3d pos = getPos();
      
      spellCooldown = this.isExalted ? 160 : 200;
      float cooldownMod = 1f;
      float durationMod = 1f;
      if(spell.spellType == ConstructSpellType.SHADOW_SHROUD){ // Teleport
         Vec3d tpPos = findConstructTpPos(new Vec3d(0,1,0));
         ParticleEffectUtils.nulConstructNecroticShroud(world, getPos());
         requestTeleport(tpPos.getX(),tpPos.getY(),tpPos.getZ());
         ParticleEffectUtils.nulConstructNecroticShroud(world, tpPos);
         
         if(this.isExalted){
            List<PlayerEntity> players = getWorld().getEntitiesByType(EntityType.PLAYER,getBoundingBox().expand(FIGHT_RANGE),(e) -> true);
            StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, 60, 15, false, true, true);
            players.forEach(p -> p.addStatusEffect(blind));
         }
      }else if(spell.spellType == ConstructSpellType.REFLEXIVE_BLAST){ // Blast
         List<Entity> entities = world.getOtherEntities(this,getBoundingBox().expand(2*BLAST_RANGE), e -> !e.isSpectator() && e.distanceTo(this) <= BLAST_RANGE && (e instanceof LivingEntity));
         for(Entity entity1 : entities){
            Vec3d diff = entity1.getPos().subtract(pos);
            double multiplier = MathHelper.clamp(BLAST_RANGE*.75-diff.length()*.5,.1,3);
            Vec3d motion = diff.multiply(1,0,1).add(0,0.5,0).normalize().multiply(multiplier);
            entity1.setVelocity(motion.x,motion.y,motion.z);
            entity1.damage(world, ArcanaDamageTypes.of(this.getWorld(),ArcanaDamageTypes.NUL,this),4f);
            if(entity1 instanceof ServerPlayerEntity player) player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         }
         
         for(BlockPos blockPos : BlockPos.iterateOutwards(this.getBlockPos(), (int) BLAST_RANGE, (int) BLAST_RANGE, (int) BLAST_RANGE)){
            int damage = (int) (10 - 15*Math.pow((blockPos.getSquaredDistance(this.getBlockPos()) / (BLAST_RANGE*BLAST_RANGE)),0.25));
            damageBlock(blockPos,damage);
         }
         ParticleEffectUtils.nulConstructReflexiveBlast(world,getPos(),0);
      }else if(spell.spellType == ConstructSpellType.CURSE_OF_DECAY){ // AoE Damage
         // Nothing special at cast time
      }else if(spell.spellType == ConstructSpellType.FORGOTTEN_ARMY){ // Summon Skeletons
         List<BlockPos> poses = SpawnPile.makeSpawnLocations(32, (int) BLAST_RANGE,world,EntityType.WITHER_SKELETON,getBlockPos());
         int numWarriors = this.isExalted ? this.random.nextBetween(6,10) : this.random.nextBetween(3,6);
         int numMages = this.isExalted ? this.random.nextBetween(4,6) : this.random.nextBetween(2,4);
         for(int i = 0; i < numWarriors+numMages; i++){
            Vec3d spawnPos = poses.get(i).toCenterPos();
            NulGuardianEntity skeleton = new NulGuardianEntity(world, this,i < numMages);
            skeleton.initialize(world,world.getLocalDifficulty(this.getBlockPos()),SpawnReason.MOB_SUMMONED,null);
            skeleton.setPos(spawnPos.getX(),spawnPos.getY(),spawnPos.getZ());
            world.spawnNewEntityAndPassengers(skeleton);
         }
      }else if(spell.spellType == ConstructSpellType.REFLECTIVE_ARMOR){ // Activate Armor
         // Nothing special at cast time
      }else if(spell.spellType == ConstructSpellType.RELENTLESS_ONSLAUGHT){ // Charge Target
         // Nothing special at cast time
      }else if(spell.spellType == ConstructSpellType.NECROTIC_CONVERSION){ // Heal
         // Nothing special at cast time
      }else if(spell.spellType == ConstructSpellType.WITHERING_RAY){ // Laser
         // Set laser pos
         int tries = 0;
         this.targetPosition = getPos();
         while(tries < 1000){
            tries++;
            
            double posX = getX() + this.random.nextBetween(-8,8);
            double posY = getY() + this.random.nextBetween(2,7);
            double posZ = getZ() + this.random.nextBetween(-8,8);
            Vec3d newPos = new Vec3d(posX,posY,posZ);
            
            if(getTarget() != null && newPos.distanceTo(getTarget().getPos()) >= RAY_RANGE*0.75) continue;
            Path path = this.getNavigation().findPathTo(posX,posY,posZ, (int) (RAY_RANGE*2));
            if(path == null) continue;
            this.targetPosition = newPos;
            break;
         }
      }
      spell.setCooldown((int) (spell.spellType.baseCooldown * cooldownMod));
      spell.cast(this, (int) (spell.spellType.duration * durationMod));
      tickSpell(spell);
   }
   
   private void destructiveAura(){
      int thisX = MathHelper.floor(this.getX());
      int thisY = MathHelper.floor(this.getY());
      int thisZ = MathHelper.floor(this.getZ());
      for (int xOff = -3; xOff <= 3; ++xOff){
         for (int zOff = -3; zOff <= 3; ++zOff){
            for (int yOff = -1; yOff <= 4; ++yOff){
               int x = thisX + xOff;
               int y = thisY + yOff;
               int z = thisZ + zOff;
               BlockPos blockPos = new BlockPos(x, y, z);
               double yMod = (yOff < 0 ? 1.5*Math.sqrt(-yOff)+2 : yOff);
               int damage = (int) (10 - (0.8 * (xOff*xOff + zOff*zOff + yMod*yMod)));
               damageBlock(blockPos,damage);
            }
         }
      }
   }
   
   private boolean isShieldActive(){
      if(getInvulnerableTimer() > 0) return true;
      if(spells == null) return false;
      if(spells.containsKey(ConstructSpellType.REFLECTIVE_ARMOR) && spells.get(ConstructSpellType.REFLECTIVE_ARMOR).isActive()) return true;
      if(spells.containsKey(ConstructSpellType.RELENTLESS_ONSLAUGHT) && spells.get(ConstructSpellType.RELENTLESS_ONSLAUGHT).isActive()) return true;
      return false;
   }
   
   private boolean isReflectionActive(){
      if(spells == null) return false;
      if(spells.containsKey(ConstructSpellType.REFLECTIVE_ARMOR) && spells.get(ConstructSpellType.REFLECTIVE_ARMOR).isActive()) return true;
      return false;
   }
   
   private boolean isConversionActive(){
      if(spells == null) return false;
      if(spells.containsKey(ConstructSpellType.NECROTIC_CONVERSION) && spells.get(ConstructSpellType.NECROTIC_CONVERSION).isActive()) return true;
      return false;
   }
   
   private void conversionHeal(double damage){
      float healMod = isExalted ? 0.35f : 0.25f;
      if(isConversionActive()){
         healMod += isExalted ? 0.4f : 0.25f;
      }
      
      this.heal((float) (healMod * damage));
   }
   
   private HashMap<ConstructSpellType,Integer> buildWeightedSpellList(){
      HashMap<ConstructSpellType,Integer> availableSpells = new HashMap<>();
      
      for(ConstructSpellType spellType : spells.keySet()){
         ConstructSpell spell = spells.get(spellType);
         int cd = spell.getCooldown();
         if(cd > 0) continue;
         
         int weight = spell.getWeight(); // Start with the randomly favored spell weights (1-10)
         if(spellType == ConstructSpellType.CURSE_OF_DECAY){ // Favored when more mobs are around
            List<Entity> entities = getWorld().getOtherEntities(this,getBoundingBox().expand(DECAY_RANGE*2), e -> !e.isSpectator() && e.distanceTo(this) < DECAY_RANGE && (e instanceof LivingEntity));
            weight += (int)(entities.size() * 0.5);
         }else if(spellType == ConstructSpellType.WITHERING_RAY){ // Favored at high health, cannot be cast while onslaught is active
            double hpPercent = getHealth() / getMaxHealth();
            weight += (int) (hpPercent * 8);
            if(spells.get(ConstructSpellType.RELENTLESS_ONSLAUGHT).isActive()){
               weight -= 100;
            }
         }else if(spellType == ConstructSpellType.NECROTIC_CONVERSION){ // Favored at low health
            double hpPercent = getHealth() / getMaxHealth();
            weight += (int) ((1-hpPercent) * 10);
         }else if(spellType == ConstructSpellType.REFLECTIVE_ARMOR){ // Favored at low health
            double hpPercent = getHealth() / getMaxHealth();
            weight += (int) ((1-hpPercent) * 5);
         }else if(spellType == ConstructSpellType.RELENTLESS_ONSLAUGHT){ // Favored at low health, cannot be cast while ray is active
            double hpPercent = getHealth() / getMaxHealth();
            weight += (int) ((1-hpPercent) * 8);
            if(spells.get(ConstructSpellType.WITHERING_RAY).isActive()){
               weight -= 100;
            }
         }else if(spellType == ConstructSpellType.FORGOTTEN_ARMY){ // Favored always | Unfavored when conversion is active
            if(spells.get(ConstructSpellType.NECROTIC_CONVERSION).isActive()){
               weight -= 5;
            }else{
               weight += 8;
            }
         }else if(spellType == ConstructSpellType.SHADOW_SHROUD){ // No favored conditions | Unfavored when conversion is active
            if(spells.get(ConstructSpellType.NECROTIC_CONVERSION).isActive()){
               weight -= 8;
            }
         }else if(spellType == ConstructSpellType.REFLEXIVE_BLAST){ // Reflexive Blast cannot be used via normal spell casting
            weight = 0;
         }
         
         if(weight > 0){
            availableSpells.put(spellType, weight);
         }
      }
      
      return availableSpells;
   }
   
   private ConstructSpellType getWeightedResult(HashMap<ConstructSpellType,Integer> map){
      ArrayList<ConstructSpellType> pool = new ArrayList<>();
      
      for(Map.Entry<ConstructSpellType, Integer> entry : map.entrySet()){
         ConstructSpellType key = entry.getKey();
         for(int j = 0; j < entry.getValue(); j++){
            pool.add(key);
         }
      }
      if(pool.isEmpty()) return null;
      return pool.get(this.random.nextInt(pool.size()));
   }
   
   private Vec3d findConstructTpPos(Vec3d biasDirection){
      int tries = 0;
      Vec3d sourcePos = summoner != null ? summoner.getPos() : this.getPos();
      
      biasDirection = biasDirection.normalize();
      while(tries < 1000){
         Vec3d randomPoint = MiscUtils.randomSpherePoint(Vec3d.ZERO, 1).normalize();
         Vec3d dir = randomPoint.add(biasDirection).normalize().multiply(this.random.nextFloat() * (NulConstructEntity.TELEPORT_RANGE - 4.0) + 4.0);
         Vec3d inWorld = sourcePos.add(dir);
         
         if(this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(this.getPos().negate()).offset(inWorld))){
            return inWorld;
         }
         tries++;
      }
      return this.getPos();
   }
   
   private enum ConstructSpellType {
      CURSE_OF_DECAY("Curse of Decay","curse_of_decay",115,550,new Text[]{
            TextUtils.withColor(Text.literal("The decay takes hold...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("You feel your soul being siphoned away...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("A curse permeates your soul...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      REFLEXIVE_BLAST("Reflexive Blast","reflexive_blast",0,200,new Text[]{
            TextUtils.withColor(Text.literal("The construct surges!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("A blast knocks you!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("A shockwave emanates from the construct!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      WITHERING_RAY("Withering Ray","withering_ray",150,275,new Text[]{
            TextUtils.withColor(Text.literal("A necrotic ray bursts forth!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("The construct emits a withering ray!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("A withering beam emanates from the construct!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      NECROTIC_CONVERSION("Necrotic Conversion","necrotic_conversion",300,475,new Text[]{
            TextUtils.withColor(Text.literal("The construct mends...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("The construct's bones heal...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("The construct regenerates...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      SHADOW_SHROUD("Shadow Shroud","shadow_shroud",0,200,new Text[]{
            TextUtils.withColor(Text.literal("The construct shifts...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("The construct vanishes...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Don't lose your mark.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      FORGOTTEN_ARMY("Forgotten Army","forgotten_army",0,625,new Text[]{
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   The forgotten army rises again!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   My brethren live as long as I remain.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   The ancient warriors march forth!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   You're outnumbered now...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Don't get surrounded.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      REFLECTIVE_ARMOR("Reflective Armor","reflective_armor",200,325,new Text[]{
            TextUtils.withColor(Text.literal("The construct shimmers...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("A shining field embraces the construct.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("The construct becomes reflective.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      RELENTLESS_ONSLAUGHT("Relentless Onslaught","relentless_onslaught",160,450,new Text[]{
            TextUtils.withColor(Text.literal("The construct pursues you!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("The construct charges you!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            TextUtils.withColor(Text.literal("The construct approaches aggressively!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      });
      
      public final String name;
      public final String id;
      public final int duration;
      public final int baseCooldown;
      public final Text[] abilityTexts;
      
      ConstructSpellType(String name, String id, int duration, int baseCooldown, Text[] abilityTexts){
         this.name = name;
         this.id = id;
         this.duration = duration;
         this.baseCooldown = baseCooldown;
         this.abilityTexts = abilityTexts;
      }
      
      public static ConstructSpellType fromString(String id){
         for (ConstructSpellType spell : ConstructSpellType.values()){
            if(spell.id.equalsIgnoreCase(id)){
               return spell;
            }
         }
         return null;
      }
   }
   
   public enum ConstructAdaptations{
      USED_TOTEM("used_totem",false,new Text[]{
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Is that an attempt at mockery? Try that again!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   I permit you one extra chance. No more, no less.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Cheating death will only get you so far. Don't become reliant on it.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      USED_MEMENTO("used_memento",true,new Text[]{
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Now, now my champion, you have done this before. My ward is not to be used in this manner.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Using my gift like this is such a waste, I shall lessen it's benefit.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   You are making me question my faith in you. Don't rely on my power as a crutch.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      USED_VENGEANCE_TOTEM("used_vengeance_totem",false,new Text[]{
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   An interesting gambit! But will it pay off?").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   I like your spirit! But conviction is only half the battle.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   A curious trinket. Be wary, such soul magic is a dangerous game.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      DAMAGED_BY_MACE("damaged_by_mace",false,new Text[]{
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   A gravitic weapon? My construct will adapt accordingly.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   A creative approach, unfortunately you won't be able to land a solid hit.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   An intriguing weapon, but gravity won't be your ally in this fight.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      MASSIVE_BLOW("massive_blow",true,new Text[]{
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   A solid hit! However, my construct will adapt.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   A good blow! Let's make this harder...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            TextUtils.withColor(Text.literal("The construct grows more resilient...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      TRUE_INVISIBILITY("true_invisibility",false,new Text[]{
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Can you fight without seeing your opponent? My construct can!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Cheap tricks will get you nowhere!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Hiding is a coward's game!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      ADDITIONAL_PLAYERS("additional_players",false,new Text[]{
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   You brought friends! Friends have a habit of dying on you.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   You think allies will make this easier? Don't trip over each other.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Regardless of aid, my challenge remains. My construct will ensure an adequate test.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   More swords aren't always better. Are these reinforcements worthy fighters?").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   So another soul wishes to get in on the action? What is one more life to wither away?").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   If you think you can make up for your lack of skill with numbers, you are mistaken...").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   Another foe into the fray! Another soul to wither away!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   If you wish to bring reinforcements, so be it. My construct only grows stronger.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   My construct adapts to any situation, multiple opponents included.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   If you wish to attempt my challenge together, at least try to not kill each other.").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      SPAWNED_CONSTRUCT_FINDS_PLAYER("spawned_construct_finds_player",false,new Text[]{
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   I yearn for a player to test! Are you up to the challenge?").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   You may not have summoned me, but you will do. Fight!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Text.literal("").append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)).append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD)).append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(TextUtils.withColor(Text.literal("\n   I challenge you! Defend yourself!").formatted(Formatting.ITALIC),ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      });
      
      public final String id;
      public final boolean repeatable;
      public final Text[] abilityTexts;
      
      ConstructAdaptations(String id, boolean repeatable, Text[] abilityTexts){
         this.id = id;
         this.repeatable = repeatable;
         this.abilityTexts = abilityTexts;
      }
      
      public static ConstructAdaptations fromString(String id){
         for (ConstructAdaptations adaptation : ConstructAdaptations.values()){
            if(adaptation.id.equalsIgnoreCase(id)){
               return adaptation;
            }
         }
         return null;
      }
   }
   
   private static class ConstructSpell{
      private final ConstructSpellType spellType;
      private final int weight;
      private int cooldown;
      private int tick;
      private boolean active;
      
      private ConstructSpell(ConstructSpellType spellType){
         this.spellType = spellType;
         this.cooldown = 0;
         this.weight = (int)(Math.random()*10+1);
         this.active = false;
         this.tick = 0;
      }
      
      private ConstructSpell(ConstructSpellType spellType, int cooldown, int weight, boolean active, int tick){
         this.spellType = spellType;
         this.cooldown = cooldown;
         this.weight = weight;
         this.active = active;
         this.tick = tick;
      }
      
      public NbtCompound toNbt(){
         NbtCompound tag = new NbtCompound();
         tag.putString("type",spellType.id);
         tag.putInt("cooldown",cooldown);
         tag.putInt("weight",weight);
         tag.putInt("tick",tick);
         tag.putBoolean("active",active);
         return tag;
      }
      
      private static ConstructSpell fromNbt(NbtCompound tag){
         return new ConstructSpell(ConstructSpellType.fromString(tag.getString("type", "")), tag.getInt("cooldown", 0), tag.getInt("weight", 0), tag.getBoolean("active", false), tag.getInt("tick", 0));
      }
      
      public void cast(NulConstructEntity construct, int tick){
         this.active = true;
         this.tick = tick;
         NulConstructDialog.abilityText(construct.summoner,construct,spellType.abilityTexts[(int) (Math.random()*spellType.abilityTexts.length)]);
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
      
      public ConstructSpellType getType(){
         return spellType;
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
   
   
   // ========== Dialog ==========
   
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
         )),new ArrayList<>(),new int[]{},1,1,0b0));
         
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Player").formatted(Formatting.GOLD))
                     .append(Text.literal(" knocks on the door of the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal("? They know not what they are toying with...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Those unworthy of ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("knowledge").formatted(Formatting.BLUE))
                     .append(Text.literal(" shall be reduced to ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("nothing").formatted(Formatting.GRAY))
                     .append(Text.literal("...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Player").formatted(Formatting.GOLD))
                     .append(Text.literal(" grow bolder by the minute. Perhaps they need to be put in their place.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("A ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Player").formatted(Formatting.GOLD))
                     .append(Text.literal(" seeks to harness ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal("? Let them try...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
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
         )),new ArrayList<>(),new int[]{},3,3,0b0));
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
         )),new ArrayList<>(),new int[]{},1,1,0b0));
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
         )),new ArrayList<>(),new int[]{},0,10,0b10));
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
         )),new ArrayList<>(),new int[]{},0,10,0b10));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You have tasted the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" and want more? Lets hope your ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("greed").formatted(Formatting.GOLD))
                     .append(Text.literal(" is not your downfall.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Just because you already carry the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" does not mean you are ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("entitled").formatted(Formatting.GOLD))
                     .append(Text.literal(" to more.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("So you would sacrifice my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("gift").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" to curry my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("favor").formatted(Formatting.GOLD))
                     .append(Text.literal("? Let's see if you're worth it...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b100000));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You reject my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("divine gift").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal("? If it's me you want, you must prove yourself ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("worthy").formatted(Formatting.GOLD))
                     .append(Text.literal("!").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b100000));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("If you want my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("favor").formatted(Formatting.BLUE))
                     .append(Text.literal(" you must face a ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("real").formatted(Formatting.GOLD, Formatting.ITALIC))
                     .append(Text.literal(" challenge!").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b100000));
         
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You have impressed me ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Player").formatted(Formatting.GOLD))
                     .append(Text.literal(", you have earned a taste of my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" power.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Impressive, I have imbued your ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Catalyst").formatted(Formatting.GOLD))
                     .append(Text.literal(" with ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(", I'm curious as to how you'll use it.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You have defeated my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Construct").formatted(Formatting.GRAY))
                     .append(Text.literal(", no easy feat. Gather what ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" remains for your ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Catalyst").formatted(Formatting.GOLD)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Death").formatted(Formatting.GRAY))
                     .append(Text.literal(" does not come for you today, I shall grant you what you have sought.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Catalytic Matrix").formatted(Formatting.GOLD))
                     .append(Text.literal(" of yours is a quaint toy, lets see if you can handle a taste of ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("true power").formatted(Formatting.LIGHT_PURPLE))
                     .append(Text.literal(".").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("A valiant fight! ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" must be getting nervous. Perhaps she will finally learn her lesson...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},1,0,0b100));
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
         )),new ArrayList<>(),new int[]{},0,10,0b10));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You helped my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Sister").formatted(Formatting.DARK_PURPLE))
                     .append(Text.literal(" see the truth, and now you have proven yourself. Take this ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Boon").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" and may we meet again.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b10));
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
         )),new ArrayList<>(),new int[]{},0,10,0b1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("It seems you are worthy enough to add another piece of the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" to your collection.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You are unlike any I have seen before. Perhaps you are worthy of my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("guidance").formatted(Formatting.BLUE))
                     .append(Text.literal(". This ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Memento").formatted(Formatting.BLACK, Formatting.BOLD))
                     .append(Text.literal(" shall be my gift to you.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,-1,0b1001000));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You did well to survive my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("stronger construct").formatted(Formatting.RED))
                     .append(Text.literal(", but not well enough to ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("impress").formatted(Formatting.GOLD))
                     .append(Text.literal(" me.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b110000));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("You may have survived, but your performance showed ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("weakness").formatted(Formatting.RED, Formatting.ITALIC))
                     .append(Text.literal(" that I do not ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("tolerate").formatted(Formatting.GOLD))
                     .append(Text.literal("!").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b110000));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("So your").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal(" gambit").formatted(Formatting.GOLD))
                     .append(Text.literal(" paid off... I am ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("impressed").formatted(Formatting.BLUE))
                     .append(Text.literal(" Player, let my ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Memento").formatted(Formatting.BLACK, Formatting.BOLD))
                     .append(Text.literal(" offer you ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("wisdom").formatted(Formatting.BLUE))
                     .append(Text.literal(".").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b101000));
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
         )),new ArrayList<>(),new int[]{},0,200,0b101000));
         
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Another arrogant ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Player").formatted(Formatting.GOLD))
                     .append(Text.literal(", not worthy of my time.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},5,5,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Such a simple ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Construct").formatted(Formatting.GRAY))
                     .append(Text.literal(" defeated you? You are not worthy of the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(".").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Such a small sample of ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine Power").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" overwhelmed you? How did you plan on ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("harnessing").formatted(Formatting.BLUE))
                     .append(Text.literal(" it in the first place?").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("An expected result from calling upon the ").formatted(Formatting.GRAY))
                     .append(Text.literal("God").formatted(Formatting.AQUA, Formatting.BOLD))
                     .append(Text.literal(" of Death").formatted(Formatting.GRAY, Formatting.BOLD))
                     .append(Text.literal(". Do not waste my time again.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Arrogant enough to tempt ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Death").formatted(Formatting.GRAY))
                     .append(Text.literal("... I can't fathom how you expected to win.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("There is ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("knowledge").formatted(Formatting.BLUE))
                     .append(Text.literal(" in failure, but only if you have the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("wisdom").formatted(Formatting.AQUA))
                     .append(Text.literal(" to find it.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},1,1,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("death").formatted(Formatting.GRAY))
                     .append(Text.literal(" is a mercy. Do not be ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("foolish").formatted(Formatting.DARK_RED))
                     .append(Text.literal(" enough to find out why.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},1,1,0b0));
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
         )),new ArrayList<>(),new int[]{},0,20,0b10));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("Whatever petty tricks got you the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                     .append(Text.literal(" in the past won't work on me. ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Knowledge").formatted(Formatting.BLUE))
                     .append(Text.literal(" must be earned!").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,20,0b1));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("I always knew you were too ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("weak").formatted(Formatting.RED, Formatting.ITALIC))
                     .append(Text.literal(" to handle real ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("power").formatted(Formatting.LIGHT_PURPLE))
                     .append(Text.literal("...").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,150,0b100000));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("")
                     .append(Text.literal("An interesting ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("gambit").formatted(Formatting.GOLD))
                     .append(Text.literal(", too bad you aren't ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("skilled").formatted(Formatting.BLUE))
                     .append(Text.literal(" enough to execute it.").formatted(Formatting.DARK_GRAY)),
               Text.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b100000));
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
         )),new ArrayList<>(),new int[]{},0,200,0b100000));
      }
      
      public static void abilityText(PlayerEntity summoner, NulConstructEntity construct, Text text){
         List<ServerPlayerEntity> playersInRange = construct.getWorld().getNonSpectatingEntities(ServerPlayerEntity.class, construct.getBoundingBox().expand(50.0));
         if(summoner instanceof ServerPlayerEntity player && !playersInRange.contains(player)) playersInRange.add(player);
         for(ServerPlayerEntity inRange : playersInRange){
            inRange.sendMessage(text,false);
         }
      }
      
      public static void announce(MinecraftServer server, PlayerEntity summoner, NulConstructEntity construct, Announcements type){
         announce(server,summoner,construct,type,new boolean[]{});
      }
      
      // hasDivine, hasWings, droppedMemento & !isExalted, isExalted, droppedMemento & isExalted, !droppedMemento & isExalted
      // hasDivine, hasWings, !hasWings, droppedMemento, !droppedMemento, isExalted, !isExalted
      public static void announce(MinecraftServer server, PlayerEntity summoner, NulConstructEntity construct, Announcements type, boolean[] args){
         DialogHelper dialogHelper = new DialogHelper(DIALOG.get(type),args);
         ArrayList<MutableText> message = dialogHelper.getWeightedResult().message();
         List<ServerPlayerEntity> playersInRange = construct.getWorld().getNonSpectatingEntities(ServerPlayerEntity.class, construct.getBoundingBox().expand(50.0));
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
   }
   
   public enum Announcements{
      SUMMON_TEXT,
      SUMMON_DIALOG,
      SUCCESS,
      FAILURE
   }
   
}


