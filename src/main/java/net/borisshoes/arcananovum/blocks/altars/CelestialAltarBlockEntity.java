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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.TreeMap;

import static net.borisshoes.arcananovum.blocks.altars.CelestialAltar.CelestialAltarBlock.HORIZONTAL_FACING;

public class CelestialAltarBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   public static final int[] TIMES = {6000,9000,12000,14000,18000,20000,23000,2000};
   
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
   
   public static Tuple<Item,Integer> getCost(){
      try{
         String itemId = ArcanaNovum.CONFIG.getValue(ArcanaConfig.CELESTIAL_ALTAR_ITEM).toString();
         Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(Identifier.parse(itemId));
         assert opt.isPresent();
         return new Tuple<>(opt.get().value(),1);
      }catch(Exception e){
         return new Tuple<>(Items.NETHER_STAR,1);
      }
   }
   
   public void openGui(ServerPlayer player){
      if(active){
         player.sendSystemMessage(Component.literal("You cannot access an active Altar").withStyle(ChatFormatting.RED));
         return;
      }
      CelestialAltarGui gui = new CelestialAltarGui(player,this);;
      gui.build();
      gui.open();
   }
   
   private void changeTime(@Nullable ServerPlayer player){
      if(!(this.getLevel() instanceof ServerLevel serverWorld)) return;
      int phase = this.getPhase();
      int mode = this.getMode();
      long timeOfDay = serverWorld.getDayTime();
      if(mode == 0){
         int curTime = (int) (timeOfDay % 24000L);
         int targetTime = TIMES[phase];
         int timeDiff = (targetTime - curTime + 24000) % 24000;
         serverWorld.setDayTime(timeOfDay + timeDiff);
         
         if(player != null) ArcanaAchievements.grant(player,ArcanaAchievements.POWER_OF_THE_SUN);
      }else{
         int day = (int) (timeOfDay/24000L % Integer.MAX_VALUE);
         int curPhase = day % 8;
         int phaseDiff = (phase - curPhase + 8) % 8;
         serverWorld.setDayTime(timeOfDay + phaseDiff * 24000L);
         
         if(phase == 0 && player != null){
            ArcanaAchievements.grant(player,ArcanaAchievements.LYCANTHROPE);
         }
      }
      SoundUtils.playSound(serverWorld, this.getBlockPos(), SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1, 0.5f);
      this.setActive(false);
   }
   
   public boolean startStarChange(@Nullable ServerPlayer player){
      if(this.getCooldown() > 0 || !(this.getLevel() instanceof ServerLevel serverWorld)) return false;
      if(player == null && getCrafterId() != null){
         Player crafter = serverWorld.getPlayerByUUID(AlgoUtils.getUUID(getCrafterId()));
         if(crafter instanceof ServerPlayer){
            player = (ServerPlayer) crafter;
         }
      }
      @Nullable ServerPlayer finalPlayer = player;
      
      this.resetCooldown();
      this.setActive(true);
      ArcanaEffectUtils.celestialAltarAnim(serverWorld,this.getBlockPos().getCenter(), 0, serverWorld.getBlockState(this.getBlockPos()).getValue(HORIZONTAL_FACING));
      BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(400, () -> {
         changeTime(finalPlayer);
         if(finalPlayer != null) ArcanaNovum.data(finalPlayer).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_CELESTIAL_ALTAR_ACTIVATE));
      }));
      return true;
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof CelestialAltarBlockEntity altar){
         altar.tick();
      }
   }
   
   
   @Override
   public boolean isAssembled(){
      return multiblock.matches(getMultiblockCheck());
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition,serverWorld.getBlockState(worldPosition),new BlockPos(((MultiblockCore) ArcanaRegistry.CELESTIAL_ALTAR).getCheckOffset()), level.getBlockState(worldPosition).getValue(HORIZONTAL_FACING));
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(isAssembled() && cooldown > 0){
         cooldown--;
         this.setChanged();
      }
      
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.isAssembled()){
         ArcanaNovum.addActiveBlock(new Tuple<>(this,this));
      }
      
      boolean activatable = serverWorld.getBlockState(worldPosition).getOptionalValue(CelestialAltar.CelestialAltarBlock.ACTIVATABLE).orElse(false);
      boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
      if(activatable ^ shouldBeActivatable){
         serverWorld.setBlock(worldPosition, serverWorld.getBlockState(worldPosition).setValue(CelestialAltar.CelestialAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.UPDATE_CLIENTS);
      }
   }
   
   public boolean isActive(){
      return active;
   }
   
   public void setActive(boolean active){
      this.active = active;
      
      if(this.level instanceof ServerLevel serverWorld){
         boolean activatable = serverWorld.getBlockState(worldPosition).getOptionalValue(CelestialAltar.CelestialAltarBlock.ACTIVATABLE).orElse(false);
         boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
         if(activatable ^ shouldBeActivatable){
            serverWorld.setBlock(worldPosition, serverWorld.getBlockState(worldPosition).setValue(CelestialAltar.CelestialAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.UPDATE_CLIENTS);
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
      int cooldownLevel = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.ORBITAL_PERIOD);
      int baseCooldown = ArcanaNovum.CONFIG.getInt(ArcanaConfig.CELESTIAL_ALTAR_COOLDOWN);
      int cooldownReduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.CELESTIAL_ALTAR_COOLDOWN_PER_LVL).get(cooldownLevel);
      this.cooldown = Math.max(1, baseCooldown - cooldownReduction);
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
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.cooldown = view.getIntOr("cooldown", 0);
      this.mode = view.getIntOr("mode", 0);
      this.phase = view.getIntOr("phase", 0);
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
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
      view.putInt("mode",this.mode);
      view.putInt("phase",this.phase);
   }
}

