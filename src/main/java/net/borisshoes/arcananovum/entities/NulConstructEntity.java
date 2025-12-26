package net.borisshoes.arcananovum.entities;

import com.google.common.collect.ImmutableList;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.mixins.LivingEntityAccessor;
import net.borisshoes.arcananovum.mixins.WitherBossAccessor;
import net.borisshoes.arcananovum.utils.*;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class NulConstructEntity extends Monster implements PolymerEntity, RangedAttackMob {
   
   private static final EntityDataAccessor<Integer> TRACKED_ENTITY_ID_1 = SynchedEntityData.defineId(NulConstructEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TRACKED_ENTITY_ID_2 = SynchedEntityData.defineId(NulConstructEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TRACKED_ENTITY_ID_3 = SynchedEntityData.defineId(NulConstructEntity.class, EntityDataSerializers.INT);
   private static final List<EntityDataAccessor<Integer>> TRACKED_ENTITY_IDS = ImmutableList.of(TRACKED_ENTITY_ID_1, TRACKED_ENTITY_ID_2, TRACKED_ENTITY_ID_3);
   private static final EntityDataAccessor<Integer> INVUL_TIMER = SynchedEntityData.defineId(NulConstructEntity.class, EntityDataSerializers.INT);
   private final int[] skullCooldowns = new int[2];
   private final int[] chargedSkullCooldowns = new int[2];
   private final ServerBossEvent bossBar = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(true);
   
   public static final double FIGHT_RANGE = 64.0;
   private static final double DECAY_RANGE = 32.0;
   private static final double BLAST_RANGE = 24.0;
   private static final double TELEPORT_RANGE = 16.0;
   private static final double RAY_RANGE = 32.0;
   private static final TargetingConditions.Selector CAN_ATTACK_PREDICATE = (entity, world) -> !entity.getType().is(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS) && entity.attackable() && (!entity.hasInfiniteMaterials() && !entity.isSpectator());
   private static final TargetingConditions HEAD_TARGET_PREDICATE = TargetingConditions.forCombat().range(RAY_RANGE).selector(CAN_ATTACK_PREDICATE);
   
   private Player summoner;
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
   private List<ServerPlayer> players;
   private List<Tuple<BlockPos,Integer>> blockPacketQueue;
   
   private ConstructMovementType movementType = ConstructMovementType.WAIT;
   public Vec3 targetPosition;
   public BlockPos circlingCenter;
   private int movementChangeTime;
   private int acquireTargetCooldown = 0;
   private float strafeYaw = 0;
   private float strafeRadius = 5;
   private float strafeHeight = 5;
   private float strafeRate = 2;
   private int attackCooldown = 0;
   
   public NulConstructEntity(EntityType<? extends NulConstructEntity> entityType, Level world){
      super(entityType, world);
      this.setHealth(this.getMaxHealth());
      this.xpReward = 1000;
      this.blockDamage = new HashMap<>();
      this.movementChangeTime = this.tickCount + this.random.nextIntBetweenInclusive(200,500);
      this.targetPosition = this.position();
      this.circlingCenter = this.blockPosition();
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
      getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024.0);
      getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.85f);
      getAttribute(Attributes.FLYING_SPEED).setBaseValue(0.85f);
      getAttribute(Attributes.ARMOR).setBaseValue(10.0);
      getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(10.0);
      getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(128);
      getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0);
      initializedAttributes = true;
   }
   
   public static AttributeSupplier.Builder createConstructAttributes(){
      return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 1024.0)
            .add(Attributes.MOVEMENT_SPEED, 0.85f)
            .add(Attributes.FLYING_SPEED, 0.85f)
            .add(Attributes.FOLLOW_RANGE, 128)
            .add(Attributes.ARMOR, 10.0)
            .add(Attributes.ARMOR_TOUGHNESS, 10.0)
            .add(Attributes.ATTACK_DAMAGE,10.0);
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.WITHER;
   }
   
   // ========== Tracking Data ==========
   
   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder){
      super.defineSynchedData(builder);
      builder.define(TRACKED_ENTITY_ID_1, 0);
      builder.define(TRACKED_ENTITY_ID_2, 0);
      builder.define(TRACKED_ENTITY_ID_3, 0);
      builder.define(INVUL_TIMER, 0);
   }
   
   @Override
   public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial){
      data.add(new SynchedEntityData.DataValue<>(WitherBossAccessor.getDATA_TARGET_A().id(), WitherBossAccessor.getDATA_TARGET_A().serializer(), getTrackedEntityId(0)));
      data.add(new SynchedEntityData.DataValue<>(WitherBossAccessor.getDATA_TARGET_B().id(), WitherBossAccessor.getDATA_TARGET_B().serializer(), getTrackedEntityId(1)));
      data.add(new SynchedEntityData.DataValue<>(WitherBossAccessor.getDATA_TARGET_C().id(), WitherBossAccessor.getDATA_TARGET_C().serializer(), getTrackedEntityId(2)));
      data.add(new SynchedEntityData.DataValue<>(WitherBossAccessor.getDATA_ID_INV().id(), WitherBossAccessor.getDATA_ID_INV().serializer(), getInvulnerableTimer()));
      data.add(new SynchedEntityData.DataValue<>(LivingEntityAccessor.getDATA_HEALTH_ID().id(), LivingEntityAccessor.getDATA_HEALTH_ID().serializer(), getTrackedHealth()));
   }
   
   private float getTrackedHealth(){
      if(this.isShieldActive()){
         return Math.min(this.getHealth(), this.getMaxHealth()/2 - 1);
      }else{
         return Math.max(this.getHealth(), this.getMaxHealth()/2 + 1);
      }
   }
   
   @Override
   public void startSeenByPlayer(ServerPlayer player){
      super.startSeenByPlayer(player);
      this.bossBar.addPlayer(player);
   }
   
   @Override
   public void stopSeenByPlayer(ServerPlayer player){
      super.stopSeenByPlayer(player);
      this.bossBar.removePlayer(player);
   }
   
   public int getInvulnerableTimer(){
      return this.entityData.get(INVUL_TIMER);
   }
   
   public void setInvulTimer(int ticks){
      this.entityData.set(INVUL_TIMER, ticks);
   }
   
   public int getTrackedEntityId(int headIndex){
      return this.entityData.<Integer>get(TRACKED_ENTITY_IDS.get(headIndex));
   }
   
   public void setTrackedEntityId(int headIndex, int id){
      this.entityData.set(TRACKED_ENTITY_IDS.get(headIndex), id);
   }
   
   private double getHeadX(int headIndex){
      if(headIndex <= 0){
         return this.getX();
      }else{
         float f = (this.yBodyRot + (float)(180 * (headIndex - 1))) * (float) (Math.PI / 180.0);
         float g = Mth.cos(f);
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
         float f = (this.yBodyRot + (float)(180 * (headIndex - 1))) * (float) (Math.PI / 180.0);
         float g = Mth.sin(f);
         return this.getZ() + (double)g * 1.3 * (double)this.getScale();
      }
   }
   
   public Player getSummoner(){
      return this.summoner;
   }
   
   public boolean isExalted(){
      return this.isExalted;
   }
   
   
   // ========== Normal Mob Stuff ==========
   
   @Override
   public void setCustomName(@Nullable Component name){
      super.setCustomName(name);
      this.bossBar.setName(this.getDisplayName());
   }
   
   @Override
   protected SoundEvent getAmbientSound(){
      return SoundEvents.WITHER_AMBIENT;
   }
   
   @Override
   protected SoundEvent getHurtSound(DamageSource source){
      return SoundEvents.WITHER_HURT;
   }
   
   @Override
   protected SoundEvent getDeathSound(){
      return SoundEvents.WITHER_DEATH;
   }
   
   @Override
   public void makeStuckInBlock(BlockState state, Vec3 multiplier){}
   
   @Override
   public boolean canBeAffected(MobEffectInstance effect){
      return effect.is(ArcanaRegistry.DAMAGE_AMP_EFFECT);
   }
   
   @Override
   protected boolean canRide(Entity entity){
      return false;
   }
   
   @Override
   public boolean canUsePortal(boolean allowVehicles){
      return false;
   }
   
   @Override
   protected void addAdditionalSaveData(ValueOutput view){
      super.addAdditionalSaveData(view);
      view.putInt("numPlayers",numPlayers);
      view.putInt("spellCooldown",spellCooldown);
      view.putBoolean("shouldHaveSummoner",shouldHaveSummoner);
      view.putBoolean("summonerHasDivine", summonerHasDivine);
      view.putBoolean("summonerHasWings",summonerHasWings);
      view.putBoolean("isExalted", isExalted);
      view.putFloat("prevHP",prevHP);
      view.putFloat("adaptiveResistance",adaptiveResistance);
      view.putInt("invulnerableTimer", this.getInvulnerableTimer());
      
      if(summoner != null){
         view.putString("summoner",summoner.getStringUUID());
      }
      
      CompoundTag spellsTag = new CompoundTag();
      for(Map.Entry<ConstructSpellType, ConstructSpell> entry : spells.entrySet()){
         spellsTag.put(entry.getKey().id,entry.getValue().toNbt());
      }
      view.store("spells", CompoundTag.CODEC,spellsTag);
      
      CompoundTag adaptationsTag = new CompoundTag();
      adaptations.forEach((adaptation, bool) -> adaptationsTag.putBoolean(adaptation.id,bool));
      view.store("adaptations", CompoundTag.CODEC,adaptationsTag);
      
      CompoundTag blockDamageTag = new CompoundTag();
      blockDamage.forEach((block, damage) -> {
         CompoundTag blockTag = new CompoundTag();
         blockTag.putInt("x",block.getX());
         blockTag.putInt("y",block.getY());
         blockTag.putInt("z",block.getZ());
         blockTag.putInt("damage",damage);
      });
      view.store("blockDamage", CompoundTag.CODEC,blockDamageTag);
      
      view.store("players", CodecUtils.STRING_LIST,players.stream().map(ServerPlayer::getStringUUID).toList());
   }
   
   @Override
   protected void readAdditionalSaveData(ValueInput view){
      super.readAdditionalSaveData(view);
      numPlayers = view.getIntOr("numPlayers", 0);
      spellCooldown = view.getIntOr("spellCooldown", 0);
      shouldHaveSummoner = view.getBooleanOr("shouldHaveSummoner", false);
      summonerHasDivine = view.getBooleanOr("summonerHasDivine", false);
      summonerHasWings = view.getBooleanOr("summonerHasWings", false);
      isExalted = view.getBooleanOr("isExalted", false);
      prevHP = view.getFloatOr("prevHP", 0.0f);
      adaptiveResistance = view.getFloatOr("adaptiveResistance", 0.0f);
      
      this.setInvulTimer(view.getIntOr("invulnerableTimer", 0));
      
      if(this.hasCustomName()){
         this.bossBar.setName(this.getDisplayName());
      }
      
      if(level() instanceof ServerLevel serverWorld && serverWorld.getEntity(AlgoUtils.getUUID(view.getStringOr("summoner", ""))) instanceof Player player){
         summoner = player;
      }
      
      spells = new HashMap<>();
      CompoundTag spellsTag = view.read("spells", CompoundTag.CODEC).orElse(new CompoundTag());
      for(String key : spellsTag.keySet()){
         spells.put(ConstructSpellType.fromString(key),ConstructSpell.fromNbt(spellsTag.getCompoundOrEmpty(key)));
      }
      
      adaptations = new HashMap<>();
      CompoundTag adaptationsTag = view.read("adaptations", CompoundTag.CODEC).orElse(new CompoundTag());
      for(String key : adaptationsTag.keySet()){
         adaptations.put(ConstructAdaptations.fromString(key), adaptationsTag.getBooleanOr(key, false));
      }
      
      blockDamage = new HashMap<>();
      CompoundTag blockDamageTag = view.read("blockDamage", CompoundTag.CODEC).orElse(new CompoundTag());
      for(String key : blockDamageTag.keySet()){
         CompoundTag compound = blockDamageTag.getCompoundOrEmpty(key);
         blockDamage.put(new BlockPos(compound.getIntOr("x", 0), compound.getIntOr("y", 0), compound.getIntOr("z", 0)), compound.getIntOr("damage", 0));
      }
      
      players = new ArrayList<>();
      for(String id : view.read("players", CodecUtils.STRING_LIST).orElse(new ArrayList<>())){
         if(level() instanceof ServerLevel serverWorld && serverWorld.getEntity(AlgoUtils.getUUID(id)) instanceof ServerPlayer player){
            players.add(player);
         }
      }
   }
   
   @Override
   protected void removeAfterChangingDimensions(){
      blockDamage.clear();
      super.removeAfterChangingDimensions();
   }
   
   
   // ========== Summoning / Death ==========
   
   public void onSummoned(Player summoner){
      this.onSummoned(summoner,false);
   }
   
   public void onSummoned(Player summoner, boolean mythic){
      this.setInvulTimer(220);
      this.bossBar.setProgress(0.0F);
      this.setHealth(this.getMaxHealth() / 3.0F);
      if(!(level() instanceof ServerLevel serverWorld)) return;
      this.summoner = summoner;
      this.shouldHaveSummoner = true;
      this.isExalted = mythic;
      if(summoner instanceof ServerPlayer serverPlayer) this.players.add(serverPlayer);
      this.numPlayers = 1;
      
      MutableComponent witherName;
      if(isExalted){
         witherName = Component.literal("")
               .append(Component.literal("❖").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
               .append(Component.literal("▓").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD, ChatFormatting.OBFUSCATED))
               .append(Component.literal("❖").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
               .append(Component.literal(" "))
               .append(Component.literal("Exalted Nul Construct").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
               .append(Component.literal(" "))
               .append(Component.literal("❖").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
               .append(Component.literal("▓").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD, ChatFormatting.OBFUSCATED))
               .append(Component.literal("❖").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
         ArcanaEffectUtils.exaltedConstructSummon(serverWorld, position().add(0,0,0),0);
         
         AttributeInstance entityAttributeInstance = getAttribute(Attributes.ATTACK_DAMAGE);
         AttributeModifier entityAttributeModifier = new AttributeModifier(Identifier.fromNamespaceAndPath(MOD_ID,"exalted"), 15.0f, AttributeModifier.Operation.ADD_VALUE);
         if(entityAttributeInstance != null && !entityAttributeInstance.hasModifier(Identifier.fromNamespaceAndPath(MOD_ID,"exalted"))) entityAttributeInstance.addPermanentModifier(entityAttributeModifier);
      }else{
         witherName = Component.literal("")
               .append(Component.literal("-").withStyle(ChatFormatting.DARK_GRAY))
               .append(Component.literal("=").withStyle(ChatFormatting.DARK_GRAY))
               .append(Component.literal("-").withStyle(ChatFormatting.DARK_GRAY))
               .append(Component.literal(" "))
               .append(Component.literal("Nul Construct").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
               .append(Component.literal(" "))
               .append(Component.literal("-").withStyle(ChatFormatting.DARK_GRAY))
               .append(Component.literal("=").withStyle(ChatFormatting.DARK_GRAY))
               .append(Component.literal("-").withStyle(ChatFormatting.DARK_GRAY));
         ArcanaEffectUtils.nulConstructSummon(serverWorld, position().add(0,0,0),0);
      }
      
      setCustomName(witherName);
      setCustomNameVisible(true);
      setPersistenceRequired();
      
      summonerHasWings = ArcanaNovum.data(summoner).hasCrafted(ArcanaRegistry.WINGS_OF_ENDERIA);
      summonerHasDivine = summonerHasWings || ArcanaNovum.data(summoner).hasCrafted(ArcanaRegistry.DIVINE_CATALYST);
      Inventory inv = summoner.getInventory();
      for(int i = 0; i < inv.getContainerSize(); i++){
         ItemStack stack = inv.getItem(i);
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
         if(arcanaItem == null) continue;
         if(arcanaItem.getRarity() == ArcanaRarity.DIVINE) summonerHasDivine = true;
      }
      
      prevHP = getHealth();
      
      NulConstructDialog.announce(summoner.level().getServer(),summoner,this, Announcements.SUMMON_TEXT);
      NulConstructEntity construct = this;
      BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(this.getInvulnerableTimer(), () -> {
         NulConstructDialog.announce(summoner.level().getServer(),summoner,construct, Announcements.SUMMON_DIALOG, new boolean[]{summonerHasDivine,summonerHasWings,!summonerHasWings, false, true, isExalted, !isExalted});
         setHealth(getMaxHealth());
      }));
   }
   
   @Override
   public void die(DamageSource damageSource){
      super.die(damageSource);
      
      MinecraftServer server = level().getServer();
      if(server == null) return;
      
      if(isExalted){ // TODO proper loot table?
         dropItem(level(), Items.NETHER_STAR.getDefaultInstance().copyWithCount(24), position());
         dropItem(level(), new ItemStack(ArcanaRegistry.DIVINE_ARCANE_PAPER,this.random.nextIntBetweenInclusive(8,16)), position());
      }else{
         for(int i = 0; i < this.random.nextIntBetweenInclusive(4,16); i++){
            ItemStack stack = Items.NETHER_STAR.getDefaultInstance().copy();
            dropItem(level(),stack, position());
            dropItem(level(),stack.copyWithCount(1), position());
         }
         dropItem(level(),new ItemStack(ArcanaRegistry.DIVINE_ARCANE_PAPER,this.random.nextIntBetweenInclusive(4,12)), position());
      }
      
      if(summoner == null) return;
      
      boolean dropped = isExalted || this.random.nextFloat() < 0.01;
      
      if(dropped){
         ItemStack stack = ArcanaRegistry.NUL_MEMENTO.addCrafter(ArcanaRegistry.NUL_MEMENTO.getNewItem(),summoner.getStringUUID(),0,server);
         ArcanaNovum.data(summoner).addCraftedSilent(stack);
         dropItem(level(), stack.copyWithCount(1), position());
      }
      
      if(!isExalted){
         ItemStack stack = ArcanaRegistry.DIVINE_CATALYST.addCrafter(ArcanaRegistry.DIVINE_CATALYST.getNewItem(),summoner.getStringUUID(),0,server);
         ArcanaNovum.data(summoner).addCraftedSilent(stack);
         dropItem(level(), stack.copyWithCount(1), position());
      }
      
      NulConstructDialog.announce(server,summoner,this, Announcements.SUCCESS, new boolean[]{summonerHasDivine,summonerHasWings,!summonerHasWings, dropped, !dropped, isExalted, !isExalted});
      
      if(summoner instanceof ServerPlayer player){
         ArcanaAchievements.grant(player,ArcanaAchievements.CONSTRUCT_DECONSTRUCTED.id);
         if(dropped){
            ArcanaAchievements.grant(player,ArcanaAchievements.DIVINE_FAVOR.id);
         }
      }
   }
   
   public void deconstruct(){
      if(summoner != null){
         NulConstructDialog.announce(level().getServer(),summoner,this, Announcements.FAILURE,new boolean[]{summonerHasDivine,summonerHasWings,!summonerHasWings, false, true, isExalted, !isExalted});
      }
      
      dropItem(level(),(new ItemStack(Items.NETHERITE_BLOCK)).copyWithCount(1), position());
      discard();
   }
   
   private void dropItem(Level world, ItemStack stack, Vec3 pos){
      ItemEntity itemEntity = new ItemEntity(world, pos.x(), pos.y(), pos.z(), stack);
      itemEntity.setPickUpDelay(40);
      itemEntity.setExtendedLifetime();
      
      float f = world.random.nextFloat() * 0.1F;
      float g = world.random.nextFloat() * 6.2831855F;
      itemEntity.setDeltaMovement((double)(-Mth.sin(g) * f), 0.20000000298023224, (double)(Mth.cos(g) * f));
      world.addFreshEntity(itemEntity);
   }
   
   public static BlockPattern getConstructPattern(){
      return BlockPatternBuilder.start().aisle("^^^", "#@#", "~#~")
            .where('#', (pos) -> pos.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
            .where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL))))
            .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
            .where('@', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.NETHERITE_BLOCK)))
            .build();
   }
   
   
   // ========== Damage ==========
   
   @Override
   public boolean hurtServer(ServerLevel world, DamageSource source, float amount){
      if(this.isInvulnerableTo(world, source)){
         return false;
      } else if(source.is(ArcanaRegistry.NUL_CONSTRUCT_IMMUNE_TO) || source.getEntity() instanceof NulConstructEntity){
         return false;
      } else if(this.getInvulnerableTimer() > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)){
         return false;
      }else{
         if(this.isShieldActive()){
            Entity entity = source.getDirectEntity();
            if(entity instanceof AbstractArrow || entity instanceof WindCharge){
               return false;
            }
         }
         
         Entity entity = source.getEntity();
         if(entity != null && entity.getType().is(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS)){
            return false;
         }else{
            if(entity instanceof ServerPlayer player && !this.players.contains(player)){
               triggerAdaptation(ConstructAdaptations.ADDITIONAL_PLAYERS);
               this.players.add(player);
               this.numPlayers = players.size();
               this.adaptiveResistance += 0.05f;
            }
            
            for (int i = 0; i < this.chargedSkullCooldowns.length; i++){
               this.chargedSkullCooldowns[i] = this.chargedSkullCooldowns[i] + 3;
            }
            
            return super.hurtServer(world, source, amount);
         }
      }
   }
   
   @Override
   protected float getDamageAfterMagicAbsorb(DamageSource source, float amount){
      float modified = super.getDamageAfterMagicAbsorb(source, amount);
      if(source.isCreativePlayer() || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return modified;
      double healthPercent = getHealth() / getMaxHealth();
      
      modified *= isExalted ? 0.6f : 0.75f;
      modified *= 1.0f - Math.min(0.5f,adaptiveResistance);
      
      if(modified > 100){
         modified = 100;
         triggerAdaptation(ConstructAdaptations.MASSIVE_BLOW);
         adaptiveResistance += 0.05f;
      }
      
      if(source.is(ArcanaRegistry.NUL_CONSTRUCT_VULNERABLE_TO)) modified *= isExalted ? 1.25f : 1.5f;
      if(source.is(ArcanaRegistry.NUL_CONSTRUCT_RESISTANT_TO)) modified *= isExalted ? 0.35f : 0.5f;
      if(healthPercent > 0.5 && source.is(DamageTypes.PLAYER_ATTACK)) modified *= isExalted ? 1.25f : 1.5f;
      if(healthPercent < 0.5 && source.getDirectEntity() instanceof AbstractArrow) modified *= isExalted ? 0.35f : 0.5f;
      
      if(source.getWeaponItem() != null && (source.getWeaponItem().is(Items.MACE) || source.getWeaponItem().is(ArcanaRegistry.GRAVITON_MAUL.getItem()))){
         triggerAdaptation(ConstructAdaptations.DAMAGED_BY_MACE);
      }
      
      if(this.isReflectionActive()){
         Entity attacker = source.getEntity();
         if(attacker instanceof LivingEntity){
            if (this.level() instanceof ServerLevel serverWorld) {
               attacker.hurtServer(serverWorld, damageSources().thorns(this), amount * 0.5f);
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
      return this.tickCount > this.movementChangeTime;
   }
   
   @Override
   protected void customServerAiStep(ServerLevel world){
      try{
         if(spells == null || spells.isEmpty()) createSpells();
         if(!initializedAttributes) initializeAttributes();
         
         if(this.getInvulnerableTimer() > 0){
            int i = this.getInvulnerableTimer() - 1;
            this.bossBar.setProgress(1.0F - (float)i / 220.0F);
            if(i <= 0){
               world.explode(this, this.getX(), this.getEyeY(), this.getZ(), 7.0F, false, Level.ExplosionInteraction.MOB);
               if(!this.isSilent()){
                  world.globalLevelEvent(LevelEvent.SOUND_WITHER_BOSS_SPAWN, this.blockPosition(), 0);
               }
            }
            
            this.setInvulTimer(i);
            if(this.tickCount % 10 == 0){
               this.heal(35.0F);
            }
         }else{
            super.customServerAiStep(world);
            
            for (int ix = 1; ix < 3; ix++){
               if(this.tickCount >= this.skullCooldowns[ix - 1]){
                  this.skullCooldowns[ix - 1] = this.tickCount + 10 + this.random.nextInt(10);
                  if((world.getDifficulty() == Difficulty.NORMAL || world.getDifficulty() == Difficulty.HARD)
                        && this.chargedSkullCooldowns[ix - 1]++ > 15){
                     float f = 10.0F;
                     float g = 5.0F;
                     double d = Mth.nextDouble(this.random, this.getX() - f, this.getX() + f);
                     double e = Mth.nextDouble(this.random, this.getY() - g, this.getY() + g);
                     double h = Mth.nextDouble(this.random, this.getZ() - f, this.getZ() + f);
                     this.shootSkullAt(ix + 1, d, e, h, true);
                     this.chargedSkullCooldowns[ix - 1] = 0;
                  }
                  
                  int j = this.getTrackedEntityId(ix);
                  if(j > 0){
                     LivingEntity livingEntity = (LivingEntity)this.level().getEntity(j);
                     if(livingEntity != null && this.canAttack(livingEntity) && !(this.distanceToSqr(livingEntity) > 900.0) && this.hasLineOfSight(livingEntity)){
                        this.shootSkullAt(ix + 1, livingEntity);
                        this.skullCooldowns[ix - 1] = this.tickCount + 40 + this.random.nextInt(20);
                        this.chargedSkullCooldowns[ix - 1] = 0;
                     }else{
                        this.setTrackedEntityId(ix, 0);
                     }
                  }else{
                     List<LivingEntity> list = world.getNearbyEntities(LivingEntity.class, HEAD_TARGET_PREDICATE, this, this.getBoundingBox().inflate(20.0, 8.0, 20.0));
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
            
            if(this.tickCount % 20 == 0){
               if(isConversionActive()){
                  this.heal(isExalted ? 10.0F : 5.0F);
               }else{
                  this.heal(isExalted ? 2.5F : 1.0F);
               }
            }
            
            this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
         }
         
         if(shouldHaveSummoner){
            if(summoner == null || summoner.isDeadOrDying() || !summoner.level().dimension().equals(level().dimension())){
               deconstruct();
            }
            if(distanceTo(summoner) >= FIGHT_RANGE){
               spells.get(ConstructSpellType.SHADOW_SHROUD).setCooldown(0);
               castSpell(spells.get(ConstructSpellType.SHADOW_SHROUD));
            }
            if(summoner.hasEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT)){
               triggerAdaptation(ConstructAdaptations.TRUE_INVISIBILITY);
               MobEffectInstance blindness = new MobEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT,110,3,false,false,false);
               summoner.addEffect(blindness);
               summoner.removeEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT);
            }
         }else if(this.getTarget() != null && this.getTarget() instanceof ServerPlayer player){
            this.summoner = player;
            this.shouldHaveSummoner = true;
            triggerAdaptation(ConstructAdaptations.SPAWNED_CONSTRUCT_FINDS_PLAYER);
         }
         
         if(hasActivatedAdaptation(ConstructAdaptations.DAMAGED_BY_MACE)){
            for(ServerPlayer player : players){
               if(player.level().dimension().equals(level().dimension()) && player.getY() > getY()+10){
                  ItemStack weapon = player.getWeaponItem();
                  if(weapon.is(Items.MACE) || weapon.is(ArcanaRegistry.GRAVITON_MAUL.getItem())){
                     castSpell(spells.get(ConstructSpellType.SHADOW_SHROUD));
                     spells.get(ConstructSpellType.SHADOW_SHROUD).setCooldown(80);
                     break;
                  }
               }
            }
         }
         
         if(this.tickCount % 20 == 0){
            if(world.getGameRules().get(GameRules.MOB_GRIEFING)){
               destructiveAura();
            }
            if(isExalted){
               List<Player> players = level().getEntities(EntityType.PLAYER,getBoundingBox().inflate(FIGHT_RANGE),(e) -> true);
               
               for(Player player : players){
                  MobEffectInstance amp = new MobEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT, 100, 1, false, true, true);
                  player.addEffect(amp);
               }
            }
         }
         
         if(spellCooldown > 0) spellCooldown--;
         for(ConstructSpell spell : spells.values()){
            if(spell.isActive()) tickSpell(spell);
            if(spell.getCooldown() > 0) spell.setCooldown(spell.getCooldown() - 1);
         }
         
         float curHP = getHealth();
         DamageSource recentDamage = getLastDamageSource();
         
         if(recentDamage != null && recentDamage.is(DamageTypes.IN_WALL) && spells.get(ConstructSpellType.SHADOW_SHROUD).getCooldown() <= 0){
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
   public void aiStep(){
      this.tickCustomAI();
      
      super.aiStep();
      
      if(this.getInvulnerableTimer() > 0){
         movementType = ConstructMovementType.SPAWNING;
      }else if(spells.get(ConstructSpellType.WITHERING_RAY).isActive()){
         movementType = ConstructMovementType.LASER;
      }else if(spells.get(ConstructSpellType.RELENTLESS_ONSLAUGHT).isActive()){
         movementType = ConstructMovementType.CHARGE;
      }else if(shouldChangeMovementType()){
         this.movementChangeTime = this.tickCount + this.random.nextIntBetweenInclusive(200,500);
         
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
         this.level().addParticle(ParticleTypes.SMOKE,p + this.random.nextGaussian() * (double)s,q + this.random.nextGaussian() * (double)s,r + this.random.nextGaussian() * (double)s,0.0,0.0,0.0);
         if(shieldActive && this.level().random.nextInt(4) == 0){
            ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.7F, 0.7F, 0.5F);
            this.level().addParticle(particle,p + this.random.nextGaussian() * (double)s,q + this.random.nextGaussian() * (double)s,r + this.random.nextGaussian() * (double)s,0.0,0.0,0.0);
         }
      }
      
      if(this.getInvulnerableTimer() > 0){
         float t = 3.3F * this.getScale();
         for (int u = 0; u < 3; u++){
            ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.7F, 0.7F, 0.9F);
            this.level().addParticle(particle,this.getX() + this.random.nextGaussian(),this.getY() + (double)(this.random.nextFloat() * t),this.getZ() + this.random.nextGaussian(), 0.0, 0.0, 0.0);
         }
      }
   }
   
   private void tickCustomAI(){
      if(!(this.level() instanceof ServerLevel serverWorld)) return;
      this.setNoGravity(true);
      double speed = getAttributeValue(Attributes.FLYING_SPEED);
      
      if(this.movementType == ConstructMovementType.SPAWNING){ // Do nothing, stay still
         if(this.summoner != null){
            this.lookAt(this.summoner,30f,30f);
         }
         setDeltaMovement(0,0,0);
         return;
      }else if(this.movementType == ConstructMovementType.WAIT){
         this.acquireTargetCooldown = 0;
         this.targetPosition = this.position();
         this.circlingCenter = this.blockPosition();
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
         this.lookAt(this.getTarget(),30.0f,30.0f);
      }
      
      if(this.movementType == ConstructMovementType.CHARGE || this.movementType == ConstructMovementType.MELEE_PURSUIT){
         double sqrDistToTarget = this.distanceToSqr(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
         double attackRange = (double)(this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F);
         
         if(this.isExalted){
            List<Player> players = level().getEntities(EntityType.PLAYER,getBoundingBox().inflate(FIGHT_RANGE),(e) -> true);
            MobEffectInstance blind = new MobEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, 30, 20, false, true, true);
            players.forEach(p -> p.addEffect(blind));
         }
         
         this.attackCooldown = Math.max(this.attackCooldown - 1, 0);
         if(!(sqrDistToTarget > attackRange)){
            if(this.attackCooldown == 0){
               this.attackCooldown = 15;
               this.doHurtTarget(serverWorld, this.getTarget());
               MobEffectInstance wither = new MobEffectInstance(MobEffects.WITHER, 80, 0, false, true, true);
               this.getTarget().addEffect(wither);
            }
         }
         
         speed *= 1.5;
         this.targetPosition = this.getTarget().position();
      }else if(this.movementType == ConstructMovementType.RANGED_PURSUIT){
         double sqrDistToTarget = this.distanceToSqr(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
         if(sqrDistToTarget < (RAY_RANGE * RAY_RANGE) && this.getSensing().hasLineOfSight(this.getTarget())){
            this.attackCooldown = Math.max(this.attackCooldown - 1, 0);
            if(this.attackCooldown == 0){
               this.attackCooldown = 45;
               this.performRangedAttack(this.getTarget(), 1);
            }
         }
         
         Vec3 targetDiff = this.getTarget().position().subtract(this.position());
         double lengthDiff = targetDiff.length() - RAY_RANGE*0.5;
         Vec3 newTargetPos = this.position().add(targetDiff.normalize().scale(lengthDiff));
         this.targetPosition = new Vec3(newTargetPos.x,this.getTarget().getY()+strafeHeight,newTargetPos.z);
      }else if(this.movementType == ConstructMovementType.STRAFE){
         this.circlingCenter = this.getTarget().blockPosition();
         int up = 0;
         while(this.level().getBlockState(this.circlingCenter.above()).isAir() && up < strafeHeight){
            this.circlingCenter = this.circlingCenter.above();
            up++;
         }
         
         if(this.tickCount % 100 == 0){
            this.strafeRadius = this.random.nextFloat()*8 + 8f;
            this.strafeRate = (this.random.nextFloat()*4 + 0.5f);
            this.strafeRate *= this.random.nextBoolean() ? -1 : 1;
         }
         
         this.strafeYaw = Mth.wrapDegrees(this.strafeYaw + this.strafeRate);
         Vec3 circleOffset = new Vec3(Math.cos(Math.toRadians(this.strafeYaw)), 0, Math.sin(Math.toRadians(this.strafeYaw))).scale(this.strafeRadius);
         this.targetPosition = circleOffset.add(this.circlingCenter.getCenter());
         
         double sqrDistToTarget = this.distanceToSqr(this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
         if(sqrDistToTarget < (RAY_RANGE * RAY_RANGE) && this.getSensing().hasLineOfSight(this.getTarget())){
            this.attackCooldown = Math.max(this.attackCooldown - 1, 0);
            if(this.attackCooldown == 0){
               this.attackCooldown = 45;
               this.performRangedAttack(this.getTarget(), 1);
            }
         }
      }else if(this.movementType == ConstructMovementType.LASER){
         double sqrDistToPosition = this.distanceToSqr(this.targetPosition);
         
         if(sqrDistToPosition <= 4){
            this.lookAt(this.getTarget(),360.0f,360.0f);
            for (int i = 0; i < 3; i++){
               int id = this.getTrackedEntityId(i);
               if(id <= 0) continue;
               
               LivingEntity livingEntity = (LivingEntity)serverWorld.getEntity(id);
               if(livingEntity != null && this.canAttack(livingEntity) && (this.distanceToSqr(livingEntity) < (RAY_RANGE*RAY_RANGE))){
                  Vec3 headPos = new Vec3(getHeadX(i),getHeadY(i),getHeadZ(i));
                  MinecraftUtils.LasercastResult lasercast = MinecraftUtils.lasercast(serverWorld, headPos, livingEntity.position().subtract(headPos).normalize(), RAY_RANGE, true, this);
                  if(this.tickCount % 10 == 0){
                     float damage = this.isExalted ? 2f : 4f;
                     
                     for(Entity hit : lasercast.sortedHits()){
                        if(!(hit instanceof LivingEntity livingHit) || hit.getType().is(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS)) continue;
                        if(hit instanceof ServerPlayer hitPlayer && hitPlayer.isBlocking()){
                           double dp = hitPlayer.getForward().normalize().dot(lasercast.direction().normalize());
                           if(dp < -0.6){
                              ArcanaUtils.blockWithShield(hitPlayer,damage);
                              continue;
                           }
                        }
                        
                        hit.hurtServer(serverWorld, ArcanaDamageTypes.of(level(),ArcanaDamageTypes.NUL,this), damage);
                        MobEffectInstance wither = new MobEffectInstance(MobEffects.WITHER, isExalted ? 100 : 40, 1, false, true, true);
                        MobEffectInstance blind = new MobEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, 40, 25, false, true, true);
                        MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS, isExalted ? 100 : 40, 1, false, true, true);
                        MobEffectInstance fatigue = new MobEffectInstance(MobEffects.MINING_FATIGUE, isExalted ? 100 : 40, 2, false, true, true);
                        MobEffectInstance weakness = new MobEffectInstance(MobEffects.WEAKNESS, isExalted ? 100 : 40, 1, false, true, true);
                        if(isExalted) livingHit.addEffect(blind);
                        livingHit.addEffect(slow);
                        livingHit.addEffect(fatigue);
                        livingHit.addEffect(weakness);
                        livingHit.addEffect(wither);
                        conversionHeal(damage*0.8f);
                     }
                  }
                  
                  if(this.tickCount % 3 == 0){
                     ParticleOptions dust = new DustParticleOptions(ArcanaColors.NUL_COLOR,1.5f);
                     int intervals = (int)(lasercast.startPos().subtract(lasercast.endPos()).length() * 4);
                     ArcanaEffectUtils.line(serverWorld,null,lasercast.startPos(),lasercast.endPos(),dust,intervals,1,0.08,0);
                  }
                  
                  if(this.tickCount % 5 == 0){
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
      
      Vec3 delta = this.targetPosition.subtract(this.position());
      speed *= 1 / (1 + Math.exp(-0.25*(delta.length() - 8))); // Sigmoid velocity scaling based on proximity
      setDeltaMovement(delta.normalize().scale(speed));
   }
   
   @Override
   protected PathNavigation createNavigation(Level world){
      FlyingPathNavigation birdNavigation = new FlyingPathNavigation(this, world);
      birdNavigation.canNavigateGround();
      birdNavigation.setCanFloat(true);
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
   
   public HashSet<ServerPlayer> getParticipatingPlayers(){
      CombatTracker tracker = this.getCombatTracker();
      HashSet<ServerPlayer> participatingPlayers = new HashSet<>();
      for(CombatEntry damageRecord : tracker.entries){
         if(damageRecord.source().getEntity() instanceof ServerPlayer player){
            participatingPlayers.add(player);
         }
      }
      return participatingPlayers;
   }
   
   private List<LivingEntity> getPrioritizedTargets(){
      if(!(level() instanceof ServerLevel serverWorld)) return new ArrayList<>();
      List<LivingEntity> targets = new ArrayList<>(level().getEntitiesOfClass(LivingEntity.class, this.getHitbox().inflate(FIGHT_RANGE), e -> CAN_ATTACK_PREDICATE.test(e,serverWorld)));
      HashSet<ServerPlayer> participatingPlayers = getParticipatingPlayers();
      targets.sort(Comparator.comparingDouble(entity -> {
         double distVal = entity.distanceTo(this);
         if(entity instanceof ServerPlayer serverPlayer){
            if(summoner != null && summoner.getStringUUID().equals(serverPlayer.getStringUUID())){
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
         this.level().levelEvent(null, LevelEvent.SOUND_WITHER_BOSS_SHOOT, this.blockPosition(), 0);
      }
      
      double d = this.getHeadX(headIndex);
      double e = this.getHeadY(headIndex);
      double f = this.getHeadZ(headIndex);
      double g = targetX - d;
      double h = targetY - e;
      double i = targetZ - f;
      Vec3 vec3d = new Vec3(g, h, i);
      WitherSkull witherSkullEntity = new WitherSkull(this.level(), this, vec3d.normalize());
      witherSkullEntity.setOwner(this);
      if(charged){
         witherSkullEntity.setDangerous(true);
      }
      
      witherSkullEntity.setPosRaw(d, e, f);
      this.level().addFreshEntity(witherSkullEntity);
   }
   
   private void shootSkullAt(int headIndex, LivingEntity target){
      this.shootSkullAt(headIndex, target.getX(), target.getY() + (double)target.getEyeHeight() * 0.5, target.getZ(), headIndex == 0 && this.random.nextFloat() < 0.001F);
   }
   
   @Override
   public void performRangedAttack(LivingEntity target, float pullProgress){
      this.shootSkullAt(0, target);
   }
   
   private boolean damageBlock(BlockPos pos, int damage){
      if(damage <= 0) return false;
      BlockState blockState = this.level().getBlockState(pos);
      if(!canDestroy(blockState)) return false;
      
      
      
      boolean blocked = true;
      for (Direction direction : Direction.values()){
         Vec3 vec3d3 = this.position().relative(direction, 1.0E-5F);
         if(level().isBlockInLine(new ClipBlockStateContext(vec3d3,pos.getCenter(), state -> state.is(BlockTags.WITHER_IMMUNE))).getType() != HitResult.Type.BLOCK){
            blocked = false;
            break;
         }
      }
      if(blocked){
         return false;
      }
      
      int maxDmg = (int) Math.ceil(5*Math.log10(Math.max(1,blockState.getBlock().defaultDestroyTime()+1)));
      int curDmg = blockDamage.getOrDefault(pos,0);
      
      if(curDmg + damage > maxDmg){
         // Break
         boolean broken = this.level().destroyBlock(pos, true, this);
         if(broken){
            this.level().levelEvent(null, LevelEvent.SOUND_WITHER_BLOCK_BREAK, this.blockPosition(), 0);
            blockDamage.remove(new BlockPos(pos.getX(),pos.getY(),pos.getZ()));
            blockPacketQueue.add(new Tuple<>(pos,0));
            return true;
         }
      }else{
         int dmgLvl = (int) Math.ceil(9.0 * (double) (curDmg+damage) / maxDmg); // Breaking range 0 - 9
         blockPacketQueue.add(new Tuple<>(pos,dmgLvl));
         blockDamage.put(new BlockPos(pos.getX(),pos.getY(),pos.getZ()),curDmg+damage);
      }
      
      return false;
   }
   
   public static boolean canDestroy(BlockState block){
      return !block.isAir() && !block.is(BlockTags.WITHER_IMMUNE);
   }
   
   private void sendBlockBreakPackets(){
      if(!(level() instanceof ServerLevel serverWorld)) return;
      
      int toSend = Math.min(4096, blockPacketQueue.size());
      for (ServerPlayer serverPlayerEntity : serverWorld.getServer().getPlayerList().getPlayers()) {
         if (serverPlayerEntity != null && serverPlayerEntity.level() == serverWorld && serverPlayerEntity.getId() != this.getId() && serverPlayerEntity.distanceTo(this) <= FIGHT_RANGE) {
            List<Packet<? super ClientGamePacketListener>> list = new ArrayList<>();
            for(int i = 0; i < toSend; i++){
               BlockPos pos = blockPacketQueue.get(i).getA();
               int prog = blockPacketQueue.get(i).getB();
               list.add(new ClientboundBlockDestructionPacket(this.random.nextInt(), pos, prog));
            }
            serverPlayerEntity.connection.send(new ClientboundBundlePacket(list));
         }
      }
      
      for(int i = 0; i < toSend; i++){
         blockPacketQueue.removeFirst();
      }
   }
   
   
   // ========== Spell Stuff ==========
   
   private void tickSpell(ConstructSpell spell){
      int tick = spell.tick();
      if(!(level() instanceof ServerLevel world)) return;
      Vec3 pos = position();
      
      if(spell.getType() == ConstructSpellType.CURSE_OF_DECAY){
         if(tick % 12 == 0){
            List<Entity> entities = world.getEntities(this,getBoundingBox().inflate(DECAY_RANGE*2), e -> !e.isSpectator() && e.distanceTo(this) < DECAY_RANGE && (e instanceof LivingEntity) && !e.getType().is(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS));
            for(Entity entity1 : entities){
               if(!(entity1 instanceof LivingEntity living)) continue;
               float dmg = living.getMaxHealth() / 15.0f;
               float mod = living instanceof ServerPlayer ? 0.35f : 0.75f;
               living.hurtServer(world, ArcanaDamageTypes.of(this.level(),ArcanaDamageTypes.NUL,this),dmg);
               
               conversionHeal(dmg*mod);
               MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS,isExalted ? 500 : 20,1,false,true,true);
               MobEffectInstance weak = new MobEffectInstance(MobEffects.WEAKNESS,isExalted ? 500 : 20,1,false,true,true);
               MobEffectInstance wither = new MobEffectInstance(MobEffects.WITHER, isExalted ? 250 : 40, 1, false, true, true);
               MobEffectInstance fatigue = new MobEffectInstance(MobEffects.MINING_FATIGUE, isExalted ? 500 : 20, 2, false, true, true);
               living.addEffect(wither);
               living.addEffect(slow);
               living.addEffect(weak);
               living.addEffect(fatigue);
               
               ArcanaEffectUtils.nulConstructCurseOfDecay(world,entity1.position());
            }
         }
      }else if(spell.getType() == ConstructSpellType.WITHERING_RAY){
         // Handled in AI method
      }else if(spell.getType() == ConstructSpellType.NECROTIC_CONVERSION){
         ArcanaEffectUtils.nulConstructNecroticConversion(world, position());
      }else if(spell.getType() == ConstructSpellType.REFLECTIVE_ARMOR){
         ArcanaEffectUtils.nulConstructReflectiveArmor(world, position());
      }else if(spell.getType() == ConstructSpellType.RELENTLESS_ONSLAUGHT){
         if(tick % 15 == 0){
            List<Entity> entities = world.getEntities(this,getBoundingBox().inflate(DECAY_RANGE*2), e -> !e.isSpectator() && e.distanceTo(this) < DECAY_RANGE && (e instanceof LivingEntity));
            
            for(Entity entity1 : entities){
               if(!(entity1 instanceof LivingEntity living)) continue;
               if(!isWithinMeleeAttackRange(living)) continue;
               
               Vec3 dirToEntity = entity1.position().subtract(position()).normalize();
               double dp = dirToEntity.dot(getLookAngle());
               if(Math.toDegrees(Math.acos(dp)) <= 60.0){
                  this.doHurtTarget(world, this.getTarget());
               }
            }
            SoundUtils.playSound(level(), blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE,0.75f,(float)(this.random.nextFloat()*0.5 + 0.75));
         }
         ArcanaEffectUtils.nulConstructChargeAttack(world, position(), getYRot());
      }
   }
   
   private void castSpell(ConstructSpell spell){
      if(spell == null || !(level() instanceof ServerLevel world)) return;
      if(spell.getCooldown() > 0) return;
      Vec3 pos = position();
      
      spellCooldown = this.isExalted ? 160 : 200;
      float cooldownMod = 1f;
      float durationMod = 1f;
      if(spell.spellType == ConstructSpellType.SHADOW_SHROUD){ // Teleport
         Vec3 tpPos = findConstructTpPos(new Vec3(0,1,0));
         ArcanaEffectUtils.nulConstructNecroticShroud(world, position());
         teleportTo(tpPos.x(),tpPos.y(),tpPos.z());
         ArcanaEffectUtils.nulConstructNecroticShroud(world, tpPos);
         
         if(this.isExalted){
            List<Player> players = level().getEntities(EntityType.PLAYER,getBoundingBox().inflate(FIGHT_RANGE),(e) -> true);
            MobEffectInstance blind = new MobEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, 60, 15, false, true, true);
            players.forEach(p -> p.addEffect(blind));
         }
      }else if(spell.spellType == ConstructSpellType.REFLEXIVE_BLAST){ // Blast
         List<Entity> entities = world.getEntities(this,getBoundingBox().inflate(2*BLAST_RANGE), e -> !e.isSpectator() && e.distanceTo(this) <= BLAST_RANGE && (e instanceof LivingEntity));
         for(Entity entity1 : entities){
            Vec3 diff = entity1.position().subtract(pos);
            double multiplier = Mth.clamp(BLAST_RANGE*.75-diff.length()*.5,.1,3);
            Vec3 motion = diff.multiply(1,0,1).add(0,0.5,0).normalize().scale(multiplier);
            entity1.setDeltaMovement(motion.x,motion.y,motion.z);
            entity1.hurtServer(world, ArcanaDamageTypes.of(this.level(),ArcanaDamageTypes.NUL,this),4f);
            if(entity1 instanceof ServerPlayer player) player.connection.send(new ClientboundSetEntityMotionPacket(player));
         }
         
         for(BlockPos blockPos : BlockPos.withinManhattan(this.blockPosition(), (int) BLAST_RANGE, (int) BLAST_RANGE, (int) BLAST_RANGE)){
            int damage = (int) (10 - 15*Math.pow((blockPos.distSqr(this.blockPosition()) / (BLAST_RANGE*BLAST_RANGE)),0.25));
            damageBlock(blockPos,damage);
         }
         ArcanaEffectUtils.nulConstructReflexiveBlast(world, position(),0);
      }else if(spell.spellType == ConstructSpellType.CURSE_OF_DECAY){ // AoE Damage
         // Nothing special at cast time
      }else if(spell.spellType == ConstructSpellType.FORGOTTEN_ARMY){ // Summon Skeletons
         List<BlockPos> poses = SpawnPile.makeSpawnLocations(32, (int) BLAST_RANGE,world, EntityType.WITHER_SKELETON, blockPosition());
         int numWarriors = this.isExalted ? this.random.nextIntBetweenInclusive(6,10) : this.random.nextIntBetweenInclusive(3,6);
         int numMages = this.isExalted ? this.random.nextIntBetweenInclusive(4,6) : this.random.nextIntBetweenInclusive(2,4);
         for(int i = 0; i < numWarriors+numMages; i++){
            Vec3 spawnPos = poses.get(i).getCenter();
            NulGuardianEntity skeleton = new NulGuardianEntity(world, this,i < numMages);
            skeleton.finalizeSpawn(world,world.getCurrentDifficultyAt(this.blockPosition()), EntitySpawnReason.MOB_SUMMONED,null);
            skeleton.setPosRaw(spawnPos.x(),spawnPos.y(),spawnPos.z());
            world.tryAddFreshEntityWithPassengers(skeleton);
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
         this.targetPosition = position();
         while(tries < 1000){
            tries++;
            
            double posX = getX() + this.random.nextIntBetweenInclusive(-8,8);
            double posY = getY() + this.random.nextIntBetweenInclusive(2,7);
            double posZ = getZ() + this.random.nextIntBetweenInclusive(-8,8);
            Vec3 newPos = new Vec3(posX,posY,posZ);
            
            if(getTarget() != null && newPos.distanceTo(getTarget().position()) >= RAY_RANGE*0.75) continue;
            Path path = this.getNavigation().createPath(posX,posY,posZ, (int) (RAY_RANGE*2));
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
      int thisX = Mth.floor(this.getX());
      int thisY = Mth.floor(this.getY());
      int thisZ = Mth.floor(this.getZ());
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
            List<Entity> entities = level().getEntities(this,getBoundingBox().inflate(DECAY_RANGE*2), e -> !e.isSpectator() && e.distanceTo(this) < DECAY_RANGE && (e instanceof LivingEntity));
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
   
   private Vec3 findConstructTpPos(Vec3 biasDirection){
      int tries = 0;
      Vec3 sourcePos = summoner != null ? summoner.position() : this.position();
      
      biasDirection = biasDirection.normalize();
      while(tries < 1000){
         Vec3 randomPoint = MathUtils.randomSpherePoint(Vec3.ZERO, 1).normalize();
         Vec3 dir = randomPoint.add(biasDirection).normalize().scale(this.random.nextFloat() * (NulConstructEntity.TELEPORT_RANGE - 4.0) + 4.0);
         Vec3 inWorld = sourcePos.add(dir);
         
         if(this.level().noCollision(this, this.getBoundingBox().move(this.position().reverse()).move(inWorld))){
            return inWorld;
         }
         tries++;
      }
      return this.position();
   }
   
   private enum ConstructSpellType {
      CURSE_OF_DECAY("Curse of Decay","curse_of_decay",115,550,new Component[]{
            Component.literal("The decay takes hold...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("You feel your soul being siphoned away...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("A curse permeates your soul...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      REFLEXIVE_BLAST("Reflexive Blast","reflexive_blast",0,200,new Component[]{
            Component.literal("The construct surges!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("A blast knocks you!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("A shockwave emanates from the construct!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      WITHERING_RAY("Withering Ray","withering_ray",150,275,new Component[]{
            Component.literal("A necrotic ray bursts forth!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("The construct emits a withering ray!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("A withering beam emanates from the construct!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      NECROTIC_CONVERSION("Necrotic Conversion","necrotic_conversion",300,475,new Component[]{
            Component.literal("The construct mends...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("The construct's bones heal...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("The construct regenerates...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      SHADOW_SHROUD("Shadow Shroud","shadow_shroud",0,200,new Component[]{
            Component.literal("The construct shifts...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("The construct vanishes...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Don't lose your mark.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      FORGOTTEN_ARMY("Forgotten Army","forgotten_army",0,625,new Component[]{
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   The forgotten army rises again!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   My brethren live as long as I remain.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   The ancient warriors march forth!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   You're outnumbered now...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Don't get surrounded.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      REFLECTIVE_ARMOR("Reflective Armor","reflective_armor",200,325,new Component[]{
            Component.literal("The construct shimmers...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("A shining field embraces the construct.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("The construct becomes reflective.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      RELENTLESS_ONSLAUGHT("Relentless Onslaught","relentless_onslaught",160,450,new Component[]{
            Component.literal("The construct pursues you!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("The construct charges you!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
            Component.literal("The construct approaches aggressively!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      });
      
      public final String name;
      public final String id;
      public final int duration;
      public final int baseCooldown;
      public final Component[] abilityTexts;
      
      ConstructSpellType(String name, String id, int duration, int baseCooldown, Component[] abilityTexts){
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
      USED_TOTEM("used_totem",false,new Component[]{
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Is that an attempt at mockery? Try that again!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   I permit you one extra chance. No more, no less.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Cheating death will only get you so far. Don't become reliant on it.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      USED_MEMENTO("used_memento",true,new Component[]{
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Now, now my champion, you have done this before. My ward is not to be used in this manner.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Using my gift like this is such a waste, I shall lessen it's benefit.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   You are making me question my faith in you. Don't rely on my power as a crutch.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      USED_VENGEANCE_TOTEM("used_vengeance_totem",false,new Component[]{
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   An interesting gambit! But will it pay off?").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   I like your spirit! But conviction is only half the battle.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   A curious trinket. Be wary, such soul magic is a dangerous game.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      DAMAGED_BY_MACE("damaged_by_mace",false,new Component[]{
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   A gravitic weapon? My construct will adapt accordingly.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   A creative approach, unfortunately you won't be able to land a solid hit.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   An intriguing weapon, but gravity won't be your ally in this fight.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      MASSIVE_BLOW("massive_blow",true,new Component[]{
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   A solid hit! However, my construct will adapt.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   A good blow! Let's make this harder...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("The construct grows more resilient...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR),
      }),
      TRUE_INVISIBILITY("true_invisibility",false,new Component[]{
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Can you fight without seeing your opponent? My construct can!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Cheap tricks will get you nowhere!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Hiding is a coward's game!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      ADDITIONAL_PLAYERS("additional_players",false,new Component[]{
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   You brought friends! Friends have a habit of dying on you.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   You think allies will make this easier? Don't trip over each other.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Regardless of aid, my challenge remains. My construct will ensure an adequate test.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   More swords aren't always better. Are these reinforcements worthy fighters?").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   So another soul wishes to get in on the action? What is one more life to wither away?").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   If you think you can make up for your lack of skill with numbers, you are mistaken...").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   Another foe into the fray! Another soul to wither away!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   If you wish to bring reinforcements, so be it. My construct only grows stronger.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   My construct adapts to any situation, multiple opponents included.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   If you wish to attempt my challenge together, at least try to not kill each other.").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      }),
      SPAWNED_CONSTRUCT_FINDS_PLAYER("spawned_construct_finds_player",false,new Component[]{
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   I yearn for a player to test! Are you up to the challenge?").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   You may not have summoned me, but you will do. Fight!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
            Component.literal("").append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)).append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)).append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\n   I challenge you! Defend yourself!").withStyle(ChatFormatting.ITALIC).withColor(ArcanaColors.CONSTRUCT_ABILITY_COLOR)),
      });
      
      public final String id;
      public final boolean repeatable;
      public final Component[] abilityTexts;
      
      ConstructAdaptations(String id, boolean repeatable, Component[] abilityTexts){
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
      
      public CompoundTag toNbt(){
         CompoundTag tag = new CompoundTag();
         tag.putString("type",spellType.id);
         tag.putInt("cooldown",cooldown);
         tag.putInt("weight",weight);
         tag.putInt("tick",tick);
         tag.putBoolean("active",active);
         return tag;
      }
      
      private static ConstructSpell fromNbt(CompoundTag tag){
         return new ConstructSpell(ConstructSpellType.fromString(tag.getStringOr("type", "")), tag.getIntOr("cooldown", 0), tag.getIntOr("weight", 0), tag.getBooleanOr("active", false), tag.getIntOr("tick", 0));
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
               Component.literal("")
                     .append(Component.literal("You Feel ").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                     .append(Component.literal("Divine Energy").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC, ChatFormatting.BOLD))
                     .append(Component.literal(" Flow Into The ").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                     .append(Component.literal("Construct").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
               Component.literal("")
                     .append(Component.literal("A ").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                     .append(Component.literal("Dark Presence").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC, ChatFormatting.BOLD))
                     .append(Component.literal(" Looms...").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},1,1,0b0));
         
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("This ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Player").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(" knocks on the door of the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal("? They know not what they are toying with...").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Those unworthy of ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("knowledge").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(" shall be reduced to ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("nothing").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("...").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("This ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Player").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(" grow bolder by the minute. Perhaps they need to be put in their place.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("A ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Player").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(" seeks to harness ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine Energy").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal("? Let them try...").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Of all the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Gods").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                     .append(Component.literal(" to call upon, you disturb me? You must be ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("ignorant").withStyle(ChatFormatting.DARK_RED))
                     .append(Component.literal(" of my domain, or ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("arrogant").withStyle(ChatFormatting.DARK_RED))
                     .append(Component.literal(" enough to tempt ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Death").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("I am the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("God").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                     .append(Component.literal(" of ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Death").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                     .append(Component.literal(" and ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Knowledge").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                     .append(Component.literal(". If my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Construct").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" does not give you the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("former").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(", you shall earn the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("latter").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},1,1,0b0));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("So you have defeated ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" and now knock on my door? You seek to challenge the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("God").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                     .append(Component.literal(" of ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Death").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                     .append(Component.literal("!?").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b10));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("I watched as you defeated ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(". Do not think that I am as ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("weak").withStyle(ChatFormatting.DARK_RED))
                     .append(Component.literal(" or ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("indolent").withStyle(ChatFormatting.DARK_RED))
                     .append(Component.literal(" as her.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b10));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("You have tasted the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" and want more? Lets hope your ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("greed").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(" is not your downfall.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Just because you already carry the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" does not mean you are ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("entitled").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(" to more.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b1));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("So you would sacrifice my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("gift").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" to curry my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("favor").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal("? Let's see if you're worth it...").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b100000));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("You reject my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("divine gift").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal("? If it's me you want, you must prove yourself ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("worthy").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal("!").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b100000));
         DIALOG.get(Announcements.SUMMON_DIALOG).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("If you want my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("favor").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(" you must face a ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("real").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                     .append(Component.literal(" challenge!").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b100000));
         
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("You have impressed me ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Player").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(", you have earned a taste of my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" power.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Impressive, I have imbued your ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Catalyst").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(" with ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine Energy").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(", I'm curious as to how you'll use it.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("You have defeated my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Construct").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(", no easy feat. Gather what ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine Energy").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" remains for your ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Catalyst").withStyle(ChatFormatting.GOLD)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Death").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" does not come for you today, I shall grant you what you have sought.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("This ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Catalytic Matrix").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(" of yours is a quaint toy, lets see if you can handle a taste of ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("true power").withStyle(ChatFormatting.LIGHT_PURPLE))
                     .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("A valiant fight! ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" must be getting nervous. Perhaps she will finally learn her lesson...").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},1,0,0b100));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("I can see how you defeated ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(", however I am not so ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("weak").withStyle(ChatFormatting.DARK_RED))
                     .append(Component.literal(". Be thankful I only sent a ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Construct").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" to greet you.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b10));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("You helped my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Sister").withStyle(ChatFormatting.DARK_PURPLE))
                     .append(Component.literal(" see the truth, and now you have proven yourself. Take this ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Boon").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" and may we meet again.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b10));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("You have earned your ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Catalyst").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(", but don't get ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("overzealous").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(", or else I will deal with you ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("personally").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC))
                     .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("It seems you are worthy enough to add another piece of the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" to your collection.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,10,0b1));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("You are unlike any I have seen before. Perhaps you are worthy of my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("guidance").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(". This ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Memento").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                     .append(Component.literal(" shall be my gift to you.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,-1,0b1001000));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("You did well to survive my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("stronger construct").withStyle(ChatFormatting.RED))
                     .append(Component.literal(", but not well enough to ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("impress").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(" me.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b110000));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("You may have survived, but your performance showed ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("weakness").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC))
                     .append(Component.literal(" that I do not ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("tolerate").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal("!").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b110000));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("So your").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal(" gambit").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(" paid off... I am ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("impressed").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(" Player, let my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Memento").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                     .append(Component.literal(" offer you ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("wisdom").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b101000));
         DIALOG.get(Announcements.SUCCESS).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("A ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("spectacular").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                     .append(Component.literal(" display of ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("competence").withStyle(ChatFormatting.AQUA))
                     .append(Component.literal("! Take my ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Memento").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                     .append(Component.literal(", and let my new gift ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("guide").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(" you well.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b101000));
         
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Another arrogant ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Player").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(", not worthy of my time.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},5,5,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Such a simple ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Construct").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" defeated you? You are not worthy of the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Such a small sample of ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine Power").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" overwhelmed you? How did you plan on ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("harnessing").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(" it in the first place?").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("An expected result from calling upon the ").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("God").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                     .append(Component.literal(" of Death").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                     .append(Component.literal(". Do not waste my time again.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Arrogant enough to tempt ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Death").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal("... I can't fathom how you expected to win.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},3,3,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("There is ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("knowledge").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(" in failure, but only if you have the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("wisdom").withStyle(ChatFormatting.AQUA))
                     .append(Component.literal(" to find it.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},1,1,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("This ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("death").withStyle(ChatFormatting.GRAY))
                     .append(Component.literal(" is a mercy. Do not be ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("foolish").withStyle(ChatFormatting.DARK_RED))
                     .append(Component.literal(" enough to find out why.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},1,1,0b0));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("A ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("weakling").withStyle(ChatFormatting.DARK_RED))
                     .append(Component.literal(" like you defeated ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal("? And I thought my opinion of ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("her").withStyle(ChatFormatting.DARK_PURPLE))
                     .append(Component.literal(" couldn't get any lower.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,20,0b10));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Whatever petty tricks got you the ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Divine").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" in the past won't work on me. ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("Knowledge").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(" must be earned!").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,20,0b1));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("I always knew you were too ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("weak").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC))
                     .append(Component.literal(" to handle real ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("power").withStyle(ChatFormatting.LIGHT_PURPLE))
                     .append(Component.literal("...").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,150,0b100000));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("An interesting ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("gambit").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal(", too bad you aren't ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("skilled").withStyle(ChatFormatting.BLUE))
                     .append(Component.literal(" enough to execute it.").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b100000));
         DIALOG.get(Announcements.FAILURE).add(new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("Your ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("weakness").withStyle(ChatFormatting.RED))
                     .append(Component.literal(" is ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("revolting!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC))
                     .append(Component.literal(" Your ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("divine catalyst").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                     .append(Component.literal(" is ").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal("forfeit").withStyle(ChatFormatting.GOLD))
                     .append(Component.literal("!").withStyle(ChatFormatting.DARK_GRAY)),
               Component.literal("")
         )),new ArrayList<>(),new int[]{},0,200,0b100000));
      }
      
      public static void abilityText(Player summoner, NulConstructEntity construct, Component text){
         List<ServerPlayer> playersInRange = construct.level().getEntitiesOfClass(ServerPlayer.class, construct.getBoundingBox().inflate(50.0));
         if(summoner instanceof ServerPlayer player && !playersInRange.contains(player)) playersInRange.add(player);
         for(ServerPlayer inRange : playersInRange){
            inRange.displayClientMessage(text,false);
         }
      }
      
      public static void announce(MinecraftServer server, Player summoner, NulConstructEntity construct, Announcements type){
         announce(server,summoner,construct,type,new boolean[]{});
      }
      
      // hasDivine, hasWings, droppedMemento & !isExalted, isExalted, droppedMemento & isExalted, !droppedMemento & isExalted
      // hasDivine, hasWings, !hasWings, droppedMemento, !droppedMemento, isExalted, !isExalted
      public static void announce(MinecraftServer server, Player summoner, NulConstructEntity construct, Announcements type, boolean[] args){
         DialogHelper dialogHelper = new DialogHelper(DIALOG.get(type),args);
         ArrayList<MutableComponent> message = dialogHelper.getWeightedResult().message();
         List<ServerPlayer> playersInRange = construct.level().getEntitiesOfClass(ServerPlayer.class, construct.getBoundingBox().inflate(50.0));
         if(summoner instanceof ServerPlayer player && !playersInRange.contains(player)) playersInRange.add(player);
         
         for(MutableComponent msg : message){
            boolean foundSummoner = false;
            for(ServerPlayer playerInRange : playersInRange){
               playerInRange.displayClientMessage(msg, false);
               if(playerInRange.getId() == summoner.getId()){
                  foundSummoner = true;
               }
            }
            if(type == Announcements.FAILURE && summoner != null && !foundSummoner){
               summoner.displayClientMessage(msg,false);
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


