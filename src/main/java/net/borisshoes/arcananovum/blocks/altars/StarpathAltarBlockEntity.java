package net.borisshoes.arcananovum.blocks.altars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaConfig;
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
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StarpathAltarBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   public static final Item COST = Items.ENDER_EYE;
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private int cooldown;
   private BlockPos targetCoords;
   private Map<String,BlockPos> savedTargets;
   private int activeTicks;
   private final Multiblock multiblock;
   
   public StarpathAltarBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.STARPATH_ALTAR_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.STARPATH_ALTAR).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
      this.targetCoords = getPos().mutableCopy();
      this.activeTicks = 0;
      this.savedTargets = new HashMap<>();
      resetCooldown();
   }
   
   public Map<String,BlockPos> getSavedTargets(){
      return this.savedTargets;
   }
   
   public void openGui(ServerPlayerEntity player){
      if(isActive()){
         player.sendMessage(Text.literal("You cannot access an active Altar").formatted(Formatting.RED));
         return;
      }
      StarpathAltarGui gui = new StarpathAltarGui(player,this);;
      gui.build();
      gui.open();
   }
   
   private void teleport(@Nullable ServerPlayerEntity player){
      if(!(this.getWorld() instanceof ServerWorld serverWorld)) return;
      Box teleportBox = (new Box(this.getPos().add(0,2,0))).expand(5,2,5);
      List<LivingEntity> targets = this.getWorld().getEntitiesByClass(LivingEntity.class,teleportBox,(e)->true);
      
      int tries = 0; int range = 4; int height = 2;
      ArrayList<BlockPos> locations;
      do{
         locations = SpawnPile.makeSpawnLocations(targets.size(),range, this.getTargetCoords().getY()+height, serverWorld, this.getTargetCoords());
         tries++; range++; height += 16; // Expand search area
      }while(locations.size() != targets.size() && tries < 5);
      if(locations.size() != targets.size()){
         for(LivingEntity target : targets){
            if(target instanceof ServerPlayerEntity targetPlayer) targetPlayer.sendMessage(Text.literal("The teleport goes awry, everyone is shunted uncontrollably!").formatted(Formatting.RED,Formatting.ITALIC),false);
         }
         locations = new ArrayList<>();
         for(int i = 0; i < targets.size(); i++){
            locations.add(this.getTargetCoords());
         }
      }
      
      for(int i = 0; i < targets.size(); i++){
         LivingEntity target = targets.get(i);
         BlockPos location = locations.get(i);
         target.teleportTo(new TeleportTarget(serverWorld, location.toCenterPos(), Vec3d.ZERO, target.getYaw(), target.getPitch(), TeleportTarget.ADD_PORTAL_CHUNK_TICKET));
         ParticleEffectUtils.recallTeleport(serverWorld,target.getPos());
         
         if(target instanceof ServerPlayerEntity p && Math.sqrt(this.getPos().getSquaredDistance(this.getTargetCoords())) >= 100000){
            ArcanaAchievements.grant(p,ArcanaAchievements.FAR_FROM_HOME.id);
         }
         if(player != null && (target instanceof TameableEntity tameable && tameable.isOwner(player) || (target instanceof ServerPlayerEntity && target != player)) && targets.contains(player)){
            ArcanaAchievements.grant(player,ArcanaAchievements.ADVENTURING_PARTY.id);
         }
      }
      SoundUtils.playSound(serverWorld,this.getTargetCoords(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 2, 1.5f);
   }
   
   public boolean startTeleport(@Nullable ServerPlayerEntity player){
      if(this.getCooldown() > 0 || !(this.getWorld() instanceof ServerWorld serverWorld)) return false;
      if(player == null && getCrafterId() != null){
         PlayerEntity crafter = serverWorld.getPlayerByUuid(MiscUtils.getUUID(getCrafterId()));
         if(crafter instanceof ServerPlayerEntity){
            player = (ServerPlayerEntity) crafter;
         }
      }
      @Nullable ServerPlayerEntity finalPlayer = player;
      
      this.setActiveTicks(500);
      this.resetCooldown();
      ParticleEffectUtils.starpathAltarAnim(serverWorld,this.getPos().toCenterPos());
      ArcanaNovum.addTickTimerCallback(serverWorld, new GenericTimer(500, () -> {
         teleport(finalPlayer);
         if(finalPlayer != null) ArcanaNovum.data(finalPlayer).addXP(ArcanaConfig.getInt(ArcanaRegistry.STARPATH_ALTAR_ACTIVATE));
      }));
      return true;
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof StarpathAltarBlockEntity altar){
         altar.tick();
      }
   }
   
   public int calculateCost(){
      BlockPos origin = getPos().mutableCopy();
      BlockPos target = targetCoords.mutableCopy();
      int multiplier = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.ASTRAL_PATHFINDER.id);
      int blocksPerUnit = 64 * (1 << multiplier);
      return Math.max(1,(int) (Math.sqrt(origin.getSquaredDistance(target)) / blocksPerUnit));
   }
   
   @Override
   public boolean isAssembled(){
      return multiblock.matches(getMultiblockCheck());
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(((MultiblockCore) ArcanaRegistry.STARPATH_ALTAR).getCheckOffset()),null);
   }
   
   private void tick(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return;
      }
      
      if(isAssembled() && cooldown > 0) cooldown--;
      
      boolean isActive = isActive();
      
      for(BlockPos blockPos : BlockPos.iterateOutwards(pos, 4, 0, 4)){
         BlockState state =  world.getBlockState(blockPos);
         if((state.isOf(Blocks.SCULK_CATALYST) || blockPos.equals(pos)) && state.get(Properties.BLOOM) != isActive){
            world.setBlockState(blockPos,state.with(Properties.BLOOM, isActive), Block.NOTIFY_ALL);
         }
      }
      
      if(isActive){
         activeTicks--;
      }
      this.markDirty();
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.isAssembled()){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
      
      boolean activatable = serverWorld.getBlockState(pos).getOrEmpty(StarpathAltar.StarpathAltarBlock.ACTIVATABLE).orElse(false);
      boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
      if(activatable ^ shouldBeActivatable){
         serverWorld.setBlockState(pos, serverWorld.getBlockState(pos).with(StarpathAltar.StarpathAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.NOTIFY_LISTENERS);
      }
   }
   
   public boolean isActive(){
      return this.activeTicks > 0;
   }
   
   public void setActiveTicks(int ticks){
      this.activeTicks = ticks;
      
      if(this.world instanceof ServerWorld serverWorld){
         boolean activatable = serverWorld.getBlockState(pos).getOrEmpty(StarpathAltar.StarpathAltarBlock.ACTIVATABLE).orElse(false);
         boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
         if(activatable ^ shouldBeActivatable){
            serverWorld.setBlockState(pos, serverWorld.getBlockState(pos).with(StarpathAltar.StarpathAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.NOTIFY_LISTENERS);
         }
      }
   }
   
   public int getCooldown(){
      return this.cooldown;
   }
   
   public void resetCooldown(){
      this.cooldown = 36000 - ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.CONSTELLATION_DRIFT.id) * 6000;
   }
   
   public void setTargetCoords(BlockPos pos){
      this.targetCoords = pos.mutableCopy();
   }
   
   public void setTargetCoords(int x, int y, int z){
      this.targetCoords = new BlockPos(x,y,z);
   }
   
   public BlockPos getTargetCoords(){
      if(this.targetCoords == null){
         this.targetCoords = this.getPos();
      }
      return this.targetCoords;
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
   
   public boolean isSynthetic(){
      return synthetic;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.STARPATH_ALTAR;
   }
   
   public NbtList writeTargets(){
      if(this.savedTargets != null){
         NbtList targetList = new NbtList();
         for(Map.Entry<String, BlockPos> entry : this.savedTargets.entrySet()){
            NbtCompound target = new NbtCompound();
            target.putString("name",entry.getKey());
            target.putInt("x",entry.getValue().getX());
            target.putInt("y",entry.getValue().getY());
            target.putInt("z",entry.getValue().getZ());
            targetList.add(target);
         }
         return targetList;
      }else{
         return new NbtList();
      }
   }
   
   public void readTargets(NbtList targetList){
      this.savedTargets = new HashMap<>();
      for(NbtElement e : targetList){
         NbtCompound target = ((NbtCompound) e);
         this.savedTargets.put(target.getString("name", ""), new BlockPos(target.getInt("x", 0), target.getInt("y", 0), target.getInt("z", 0)));
      }
   }
   
   @Override
   public void readData(ReadView view){
      super.readData(view);
      this.uuid = view.getString("arcanaUuid", "");
      this.crafterId = view.getString("crafterId", "");
      this.customName = view.getString("customName", "");
      this.synthetic = view.getBoolean("synthetic", false);
      this.cooldown = view.getInt("cooldown", 0);
      this.targetCoords = getPos();
      if(view.contains("target")){
         view.read("target",BlockPos.CODEC).ifPresent(data -> {
            this.targetCoords = data;
         });
      }
      this.augments = new TreeMap<>();
      if(view.contains("arcanaAugments")){
         view.read("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
            this.augments = data;
         });
      }
      
      if(view.contains("targets")){
         view.read("targets",StarpathTargets.CODEC).ifPresent(targets -> {
            this.savedTargets = targets.targets();
         });
      }
   }
   
   @Override
   protected void writeData(WriteView view){
      super.writeData(view);
      view.putNullable("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString("arcanaUuid",this.uuid == null ? "" : this.uuid);
      view.putString("crafterId",this.crafterId == null ? "" : this.crafterId);
      view.putString("customName",this.customName == null ? "" : this.customName);
      view.putBoolean("synthetic",this.synthetic);
      view.putInt("cooldown",this.cooldown);
      view.putNullable("target",BlockPos.CODEC,targetCoords);
      view.putNullable("targets",StarpathTargets.CODEC,new StarpathTargets(this.savedTargets));
   }
   
   public record StarpathTargets(Map<String, BlockPos> targets) {
      
      public StarpathTargets {
         if (targets == null) targets = new HashMap<>();
      }
      
      private record TargetEntry(String name, int x, int y, int z) {}
      private static final Codec<TargetEntry> ENTRY_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(TargetEntry::name),
            Codec.INT.fieldOf("x").forGetter(TargetEntry::x),
            Codec.INT.fieldOf("y").forGetter(TargetEntry::y),
            Codec.INT.fieldOf("z").forGetter(TargetEntry::z)
      ).apply(i, TargetEntry::new));
      
      // CODEC encodes/decodes directly as a list of entry objects (no wrapper), matching the NBT list produced/consumed below
      public static final Codec<StarpathTargets> CODEC = ENTRY_CODEC.listOf().xmap(list -> {
         Map<String, BlockPos> map = new HashMap<>();
         for (TargetEntry e : list) map.put(e.name(), new BlockPos(e.x(), e.y(), e.z()));
         return new StarpathTargets(map);
      }, sp -> {
         List<TargetEntry> out = new ArrayList<>(sp.targets.size());
         sp.targets.forEach((name, pos) -> out.add(new TargetEntry(name, pos.getX(), pos.getY(), pos.getZ())));
         return out;
      });
   }
}
