package net.borisshoes.arcananovum.blocks.altars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.altars.StarpathAltarGui;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.SpawnPile;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class StarpathAltarBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   public static final Item COST = Items.ENDER_EYE;
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private int cooldown;
   private TargetEntry target;
   private List<TargetEntry> savedTargets;
   private int activeTicks;
   private final Multiblock multiblock;
   
   public StarpathAltarBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.STARPATH_ALTAR_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.STARPATH_ALTAR).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
      this.target = new TargetEntry("","", getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
      this.activeTicks = 0;
      this.savedTargets = new ArrayList<>();
      resetCooldown();
   }
   
   public List<TargetEntry> getSavedTargets(){
      return this.savedTargets;
   }
   
   public void openGui(ServerPlayer player){
      if(isActive()){
         player.sendSystemMessage(Component.literal("You cannot access an active Altar").withStyle(ChatFormatting.RED));
         return;
      }
      StarpathAltarGui gui = new StarpathAltarGui(player,this);;
      gui.build();
      gui.open();
   }
   
   private void teleport(@Nullable ServerPlayer player){
      if(!(this.getLevel() instanceof ServerLevel serverWorld)) return;
      ServerLevel destWorld = getLevel().getServer().getLevel(getTargetDimension());
      if(!destWorld.dimension().identifier().equals(getLevel().dimension().identifier()) && ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.STARGATE) < 1) return;
      AABB teleportBox = (new AABB(this.getBlockPos().offset(0,2,0))).inflate(5,2,5);
      List<Entity> targets = this.getLevel().getEntities((Entity) null,teleportBox,(e)->e instanceof LivingEntity || e.getType().is(ArcanaRegistry.STARPATH_ALLOWED));
      
      int tries = 0; int range = 4; int height = 2;
      ArrayList<BlockPos> locations;
      do{
         locations = SpawnPile.makeSpawnLocations(targets.size(),range, this.getTarget().getY()+height, destWorld, this.getTarget());
         tries++; range++; height += 16; // Expand search area
      }while(locations.size() != targets.size() && tries < 5);
      if(locations.size() != targets.size()){
         for(Entity target : targets){
            if(target instanceof ServerPlayer targetPlayer) targetPlayer.displayClientMessage(Component.literal("The teleport goes awry, everyone is shunted uncontrollably!").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),false);
         }
         locations = new ArrayList<>();
         for(int i = 0; i < targets.size(); i++){
            locations.add(this.getTarget());
         }
      }
      
      for(int i = 0; i < targets.size(); i++){
         Entity target = targets.get(i);
         BlockPos location = locations.get(i);
         target.teleport(new TeleportTransition(destWorld, location.getCenter(), Vec3.ZERO, target.getYRot(), target.getXRot(), TeleportTransition.PLACE_PORTAL_TICKET));
         ArcanaEffectUtils.recallTeleport(destWorld,target.position());
         
         if(target instanceof ServerPlayer p && Math.sqrt(this.getBlockPos().distSqr(this.getTarget())) >= 100000){
            ArcanaAchievements.grant(p,ArcanaAchievements.FAR_FROM_HOME);
         }
         if(player != null && (target instanceof TamableAnimal tameable && tameable.isOwnedBy(player) || (target instanceof ServerPlayer && target != player)) && targets.contains(player)){
            ArcanaAchievements.grant(player,ArcanaAchievements.ADVENTURING_PARTY);
         }
      }
      SoundUtils.playSound(destWorld,this.getTarget(), SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 2, 1.5f);
   }
   
   public boolean startTeleport(@Nullable ServerPlayer player){
      if(this.getCooldown() > 0 || !(this.getLevel() instanceof ServerLevel serverWorld)) return false;
      ServerLevel destWorld = getLevel().getServer().getLevel(getTargetDimension());
      if(!destWorld.dimension().identifier().equals(getLevel().dimension().identifier()) && ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.STARGATE) < 1) return false;
      
      this.setActiveTicks(500);
      this.resetCooldown();
      ArcanaEffectUtils.starpathAltarAnim(destWorld,this.getBlockPos().getCenter());
      BorisLib.addTickTimerCallback(new GenericTimer(500, () -> {
         teleport(player);
         if(player == null && getCrafterId() != null && !getCrafterId().isEmpty()){
            UUID parsedId = AlgoUtils.getUUID(getCrafterId());
            ArcanaNovum.data(parsedId).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_STARPATH_ALTAR_ACTIVATE));
         }else if(player != null){
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_STARPATH_ALTAR_ACTIVATE));
         }
      }));
      return true;
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof StarpathAltarBlockEntity altar){
         altar.tick();
      }
   }
   
   public int calculateCost(){
      BlockPos origin = getBlockPos().mutable();
      BlockPos target = this.target.getBlockCoords();
      int multiplier = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.ASTRAL_PATHFINDER);
      int blocksPerUnit = 64 * (1 << multiplier);
      int cost = Math.max(1,(int) (Math.sqrt(origin.distSqr(target)) / blocksPerUnit));
      if(!getTargetDimension().identifier().equals(getLevel().dimension().identifier())){
         cost += cost + 16;
      }
      return cost;
   }
   
   @Override
   public boolean isAssembled(){
      return multiblock.matches(getMultiblockCheck());
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition,serverWorld.getBlockState(worldPosition),new BlockPos(((MultiblockCore) ArcanaRegistry.STARPATH_ALTAR).getCheckOffset()),null);
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(isAssembled() && cooldown > 0) cooldown--;
      
      boolean isActive = isActive();
      
      for(BlockPos blockPos : BlockPos.withinManhattan(worldPosition, 4, 0, 4)){
         BlockState state =  level.getBlockState(blockPos);
         if((state.is(Blocks.SCULK_CATALYST) || blockPos.equals(worldPosition)) && state.getValue(BlockStateProperties.BLOOM) != isActive){
            level.setBlock(blockPos,state.setValue(BlockStateProperties.BLOOM, isActive), Block.UPDATE_ALL);
         }
      }
      
      if(isActive){
         activeTicks--;
      }
      this.setChanged();
      
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.isAssembled()){
         ArcanaNovum.addActiveBlock(new Tuple<>(this,this));
      }
      
      boolean activatable = serverWorld.getBlockState(worldPosition).getOptionalValue(StarpathAltar.StarpathAltarBlock.ACTIVATABLE).orElse(false);
      boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
      if(activatable ^ shouldBeActivatable){
         serverWorld.setBlock(worldPosition, serverWorld.getBlockState(worldPosition).setValue(StarpathAltar.StarpathAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.UPDATE_CLIENTS);
      }
   }
   
   public boolean isActive(){
      return this.activeTicks > 0;
   }
   
   public void setActiveTicks(int ticks){
      this.activeTicks = ticks;
      
      if(this.level instanceof ServerLevel serverWorld){
         boolean activatable = serverWorld.getBlockState(worldPosition).getOptionalValue(StarpathAltar.StarpathAltarBlock.ACTIVATABLE).orElse(false);
         boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
         if(activatable ^ shouldBeActivatable){
            serverWorld.setBlock(worldPosition, serverWorld.getBlockState(worldPosition).setValue(StarpathAltar.StarpathAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.UPDATE_CLIENTS);
         }
      }
   }
   
   public int getCooldown(){
      return this.cooldown;
   }
   
   public void resetCooldown(){
      this.cooldown = 36000 - ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.CONSTELLATION_DRIFT) * 6000;
   }
   
   public void setTarget(BlockPos pos){
      this.target = new TargetEntry("","",pos.getX(), pos.getY(), pos.getZ());
   }
   
   public void setTarget(TargetEntry pos){
      this.target = pos;
   }
   
   public BlockPos getTarget(){
      if(this.target == null){
         this.target = new TargetEntry("","", getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
      }
      return this.target.getBlockCoords();
   }
   
   public ResourceKey<Level> getTargetDimension(){
      ServerLevel fetchedWorld = getLevel().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(this.target.dimension())));
      if(fetchedWorld == null){
         return getLevel().dimension();
      }
      return fetchedWorld.dimension();
   }
   
   public TreeMap<ArcanaAugment, Integer> getAugments(){
      return augments;
   }
   
   public String getCrafterId(){
      return crafterId;
   }
   
   public String getUuid(){
      return uuid;
   }
   
   public int getOrigin(){
      return origin;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.STARPATH_ALTAR;
   }
   
   public ListTag writeTargets(){
      if(this.savedTargets != null){
         ListTag targetList = new ListTag();
         for(TargetEntry entry : this.savedTargets){
            CompoundTag target = new CompoundTag();
            target.putString("name",entry.name);
            target.putString("dimension", entry.dimension);
            target.putInt("x",entry.x);
            target.putInt("y",entry.y);
            target.putInt("z",entry.z);
            targetList.add(target);
         }
         return targetList;
      }else{
         return new ListTag();
      }
   }
   
   public void readTargets(ListTag targetList){
      this.savedTargets = new ArrayList<>();
      for(Tag e : targetList){
         CompoundTag target = ((CompoundTag) e);
         this.savedTargets.add(new TargetEntry(
               target.getStringOr("name",""),
               target.getStringOr("dimension",""),
               target.getIntOr("x", 0),
               target.getIntOr("y", 0),
               target.getIntOr("z", 0)));
      }
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.cooldown = view.getIntOr("cooldown", 0);
      this.target = new TargetEntry("","", getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());;
      view.read("target",ENTRY_CODEC).ifPresent(data -> {
         this.target = data;
      });
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      
      view.read("targets",ENTRY_CODEC.listOf()).ifPresent(targets -> {
         this.savedTargets = new ArrayList<>(targets);
      });
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG,this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG,this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME,this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG,this.origin);
      view.putInt("cooldown",this.cooldown);
      view.storeNullable("target",ENTRY_CODEC, target);
      view.storeNullable("targets",ENTRY_CODEC.listOf(),this.savedTargets);
   }
   
   public record TargetEntry(String name, String dimension, int x, int y, int z) {
      public BlockPos getBlockCoords(){
         return new BlockPos(x,y,z);
      }
   }
   
   private static final Codec<TargetEntry> ENTRY_CODEC = RecordCodecBuilder.create(i -> i.group(
         Codec.STRING.fieldOf("name").forGetter(TargetEntry::name),
         Codec.STRING.fieldOf("dimension").forGetter(TargetEntry::dimension),
         Codec.INT.fieldOf("x").forGetter(TargetEntry::x),
         Codec.INT.fieldOf("y").forGetter(TargetEntry::y),
         Codec.INT.fieldOf("z").forGetter(TargetEntry::z)
   ).apply(i, TargetEntry::new));
}
