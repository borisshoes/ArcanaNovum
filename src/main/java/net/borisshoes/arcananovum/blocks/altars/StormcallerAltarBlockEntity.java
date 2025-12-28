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
import net.borisshoes.arcananovum.gui.altars.StormcallerAltarGui;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

public class StormcallerAltarBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   public static final int[] DURATIONS = {-1,2,4,6,8,10,15,20,25,30,35,40,45,50,55,60};
   public static final Tuple<Item,Integer> COST = new Tuple<>(Items.DIAMOND_BLOCK,1);
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private int cooldown;
   private int mode = 0; // 0 - clear sky, 1 - raining, 2 - thunder
   private int duration = 0;
   private boolean active;
   private final Multiblock multiblock;
   
   public StormcallerAltarBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.STORMCALLER_ALTAR_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.STORMCALLER_ALTAR).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
      this.mode = 0;
      this.duration = 0;
      resetCooldown();
   }
   
   private void changeWeather(@Nullable ServerPlayer player){
      if(!(this.getLevel() instanceof ServerLevel serverWorld)) return;
      int duration = this.getDuration();
      int mode = this.getMode();
      SoundUtils.playSound(serverWorld, this.getBlockPos(), SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1, 0.5f);
      int dur = DURATIONS[duration];
      
      dur = switch(mode){
         case 0 -> dur == -1 ? ServerLevel.RAIN_DELAY.sample(serverWorld.getRandom()) : dur * 3600;
         case 1 -> dur == -1 ? ServerLevel.RAIN_DURATION.sample(serverWorld.getRandom()) : dur * 1200;
         case 2 -> dur == -1 ? ServerLevel.THUNDER_DURATION.sample(serverWorld.getRandom()) : dur * 600;
         default -> dur;
      };
      if(mode == 0 && serverWorld.isRaining() && player != null){
         ArcanaAchievements.grant(player,ArcanaAchievements.COME_AGAIN_RAIN.id);
      }
      
      serverWorld.setWeatherParameters(mode == 0 ? dur : 0, mode >= 1 ? dur : 0, mode >= 1, mode == 2);
      level.gameEvent(GameEvent.BLOCK_ACTIVATE, worldPosition, GameEvent.Context.of(getBlockState()));
      this.setActive(false);
   }
   
   public boolean startWeatherChange(@Nullable ServerPlayer player){
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
      ArcanaEffectUtils.stormcallerAltarAnim(serverWorld,this.getBlockPos().getCenter(), 0);
      BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(100, () -> {
         changeWeather(finalPlayer);
         if(finalPlayer != null) ArcanaNovum.data(finalPlayer).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.STORMCALLER_ALTAR_ACTIVATE));
      }));
      return true;
   }
   
   public void openGui(ServerPlayer player){
      if(active){
         player.sendSystemMessage(Component.literal("You cannot access an active Altar").withStyle(ChatFormatting.RED));
         return;
      }
      StormcallerAltarGui gui = new StormcallerAltarGui(player,this);;
      gui.build();
      gui.open();
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof StormcallerAltarBlockEntity altar){
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
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition,serverWorld.getBlockState(worldPosition),new BlockPos(((MultiblockCore) ArcanaRegistry.STORMCALLER_ALTAR).getCheckOffset()),null);
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
      
      boolean activatable = serverWorld.getBlockState(worldPosition).getOptionalValue(StormcallerAltar.StormcallerAltarBlock.ACTIVATABLE).orElse(false);
      boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
      if(activatable ^ shouldBeActivatable){
         serverWorld.setBlock(worldPosition, serverWorld.getBlockState(worldPosition).setValue(StormcallerAltar.StormcallerAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.UPDATE_CLIENTS);
      }
   }
   
   public boolean isActive(){
      return active;
   }
   
   public void setActive(boolean active){
      this.active = active;
      
      if(this.level instanceof ServerLevel serverWorld){
         boolean activatable = serverWorld.getBlockState(worldPosition).getOptionalValue(StormcallerAltar.StormcallerAltarBlock.ACTIVATABLE).orElse(false);
         boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
         if(activatable ^ shouldBeActivatable){
            serverWorld.setBlock(worldPosition, serverWorld.getBlockState(worldPosition).setValue(StormcallerAltar.StormcallerAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.UPDATE_CLIENTS);
         }
      }
   }
   
   public int getCooldown(){
      return this.cooldown;
   }
   
   public void resetCooldown(){
      this.cooldown = 36000 - ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.CLOUD_SEEDING.id) * 6000;
   }
   
   public int getMode(){
      return mode;
   }
   
   public void setMode(int mode){
      this.mode = mode;
   }
   
   public int getDuration(){
      return duration;
   }
   
   public void setDuration(int duration){
      this.duration = duration;
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
      return ArcanaRegistry.STORMCALLER_ALTAR;
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
      this.duration = view.getIntOr("duration", 0);
      this.augments = new TreeMap<>();
      view.read("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
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
      view.putInt("duration",this.duration);
   }
}
