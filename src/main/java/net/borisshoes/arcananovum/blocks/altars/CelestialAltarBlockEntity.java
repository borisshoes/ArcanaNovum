package net.borisshoes.arcananovum.blocks.altars;

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
import net.borisshoes.arcananovum.gui.altars.CelestialAltarGui;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

import static net.borisshoes.arcananovum.blocks.altars.CelestialAltar.CelestialAltarBlock.HORIZONTAL_FACING;

public class CelestialAltarBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   public static final int[] TIMES = {6000,9000,12000,14000,18000,20000,23000,2000};
   public static final Pair<Item,Integer> COST = new Pair<>(Items.NETHER_STAR,1);
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private int cooldown;
   private int mode = 0; // 0 - time, 1 - phase
   private int phase = 0;
   private boolean active;
   private final Multiblock multiblock;
   
   public CelestialAltarBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.CELESTIAL_ALTAR_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.CELESTIAL_ALTAR).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
      this.mode = 0;
      this.phase = 0;
      resetCooldown();
   }
   
   public void openGui(ServerPlayerEntity player){
      if(active){
         player.sendMessage(Text.literal("You cannot access an active Altar").formatted(Formatting.RED));
         return;
      }
      CelestialAltarGui gui = new CelestialAltarGui(player,this);;
      gui.build();
      gui.open();
   }
   
   private void changeTime(@Nullable ServerPlayerEntity player){
      if(!(this.getWorld() instanceof ServerWorld serverWorld)) return;
      int phase = this.getPhase();
      int mode = this.getMode();
      long timeOfDay = serverWorld.getTimeOfDay();
      if(mode == 0){
         int curTime = (int) (timeOfDay % 24000L);
         int targetTime = TIMES[phase];
         int timeDiff = (targetTime - curTime + 24000) % 24000;
         serverWorld.setTimeOfDay(timeOfDay + timeDiff);
         
         if(player != null) ArcanaAchievements.grant(player,ArcanaAchievements.POWER_OF_THE_SUN.id);
      }else{
         int day = (int) (timeOfDay/24000L % Integer.MAX_VALUE);
         int curPhase = day % 8;
         int phaseDiff = (phase - curPhase + 8) % 8;
         serverWorld.setTimeOfDay(timeOfDay + phaseDiff * 24000L);
         
         if(phase == 0 && player != null){
            ArcanaAchievements.grant(player,ArcanaAchievements.LYCANTHROPE.id);
         }
      }
      SoundUtils.playSound(serverWorld, this.getPos(), SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 1, 0.5f);
      this.setActive(false);
   }
   
   public boolean startStarChange(@Nullable ServerPlayerEntity player){
      if(this.getCooldown() > 0 || !(this.getWorld() instanceof ServerWorld serverWorld)) return false;
      if(player == null && getCrafterId() != null){
         PlayerEntity crafter = serverWorld.getPlayerByUuid(AlgoUtils.getUUID(getCrafterId()));
         if(crafter instanceof ServerPlayerEntity){
            player = (ServerPlayerEntity) crafter;
         }
      }
      @Nullable ServerPlayerEntity finalPlayer = player;
      
      this.resetCooldown();
      this.setActive(true);
      ArcanaEffectUtils.celestialAltarAnim(serverWorld,this.getPos().toCenterPos(), 0, serverWorld.getBlockState(this.getPos()).get(HORIZONTAL_FACING));
      BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(400, () -> {
         changeTime(finalPlayer);
         if(finalPlayer != null) ArcanaNovum.data(finalPlayer).addXP(ArcanaConfig.getInt(ArcanaRegistry.CELESTIAL_ALTAR_ACTIVATE));
      }));
      return true;
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof CelestialAltarBlockEntity altar){
         altar.tick();
      }
   }
   
   
   @Override
   public boolean isAssembled(){
      return multiblock.matches(getMultiblockCheck());
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(((MultiblockCore) ArcanaRegistry.CELESTIAL_ALTAR).getCheckOffset()),world.getBlockState(pos).get(HORIZONTAL_FACING));
   }
   
   private void tick(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return;
      }
      
      if(isAssembled() && cooldown > 0){
         cooldown--;
         this.markDirty();
      }
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.isAssembled()){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
      
      boolean activatable = serverWorld.getBlockState(pos).getOrEmpty(CelestialAltar.CelestialAltarBlock.ACTIVATABLE).orElse(false);
      boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
      if(activatable ^ shouldBeActivatable){
         serverWorld.setBlockState(pos, serverWorld.getBlockState(pos).with(CelestialAltar.CelestialAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.NOTIFY_LISTENERS);
      }
   }
   
   public boolean isActive(){
      return active;
   }
   
   public void setActive(boolean active){
      this.active = active;
      
      if(this.world instanceof ServerWorld serverWorld){
         boolean activatable = serverWorld.getBlockState(pos).getOrEmpty(CelestialAltar.CelestialAltarBlock.ACTIVATABLE).orElse(false);
         boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
         if(activatable ^ shouldBeActivatable){
            serverWorld.setBlockState(pos, serverWorld.getBlockState(pos).with(CelestialAltar.CelestialAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.NOTIFY_LISTENERS);
         }
      }
   }
   
   public int getMode(){
      return mode;
   }
   
   public void setMode(int mode){
      this.mode = mode;
   }
   
   public int getPhase(){
      return phase;
   }
   
   public void setPhase(int phase){
      this.phase = phase;
   }
   
   public int getCooldown(){
      return this.cooldown;
   }
   
   public void resetCooldown(){
      this.cooldown = 36000 - ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.ORBITAL_PERIOD.id) * 6000;
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
      return ArcanaRegistry.CELESTIAL_ALTAR;
   }
   
   
   @Override
   public void readData(ReadView view){
      super.readData(view);
      this.uuid = view.getString(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getString(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getString(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getInt(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.cooldown = view.getInt("cooldown", 0);
      this.mode = view.getInt("mode", 0);
      this.phase = view.getInt("phase", 0);
      this.augments = new TreeMap<>();
      view.read("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
   }
   
   @Override
   protected void writeData(WriteView view){
      super.writeData(view);
      view.putNullable(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG,this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG,this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME,this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG,this.origin);
      view.putInt("cooldown",this.cooldown);
      view.putInt("mode",this.mode);
      view.putInt("phase",this.phase);
   }
}

