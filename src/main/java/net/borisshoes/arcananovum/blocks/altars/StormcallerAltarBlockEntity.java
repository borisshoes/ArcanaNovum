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
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
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
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

public class StormcallerAltarBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   public static final int[] DURATIONS = {-1,2,4,6,8,10,15,20,25,30,35,40,45,50,55,60};
   public static final Pair<Item,Integer> COST = new Pair<>(Items.DIAMOND_BLOCK,1);
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
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
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
      this.mode = 0;
      this.duration = 0;
      resetCooldown();
   }
   
   private void changeWeather(@Nullable ServerPlayerEntity player){
      if(!(this.getWorld() instanceof ServerWorld serverWorld)) return;
      int duration = this.getDuration();
      int mode = this.getMode();
      SoundUtils.playSound(serverWorld, this.getPos(), SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 1, 0.5f);
      int dur = DURATIONS[duration];
      
      dur = switch(mode){
         case 0 -> dur == -1 ? ServerWorld.CLEAR_WEATHER_DURATION_PROVIDER.get(serverWorld.getRandom()) : dur * 3600;
         case 1 -> dur == -1 ? ServerWorld.RAIN_WEATHER_DURATION_PROVIDER.get(serverWorld.getRandom()) : dur * 1200;
         case 2 -> dur == -1 ? ServerWorld.THUNDER_WEATHER_DURATION_PROVIDER.get(serverWorld.getRandom()) : dur * 600;
         default -> dur;
      };
      if(mode == 0 && serverWorld.isRaining() && player != null){
         ArcanaAchievements.grant(player,ArcanaAchievements.COME_AGAIN_RAIN.id);
      }
      
      serverWorld.setWeather(mode == 0 ? dur : 0, mode >= 1 ? dur : 0, mode >= 1, mode == 2);
      world.emitGameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Emitter.of(getCachedState()));
      this.setActive(false);
   }
   
   public boolean startWeatherChange(@Nullable ServerPlayerEntity player){
      if(this.getCooldown() > 0 || !(this.getWorld() instanceof ServerWorld serverWorld)) return false;
      if(player == null && getCrafterId() != null){
         PlayerEntity crafter = serverWorld.getPlayerByUuid(MiscUtils.getUUID(getCrafterId()));
         if(crafter instanceof ServerPlayerEntity){
            player = (ServerPlayerEntity) crafter;
         }
      }
      @Nullable ServerPlayerEntity finalPlayer = player;
      
      this.resetCooldown();
      this.setActive(true);
      ParticleEffectUtils.stormcallerAltarAnim(serverWorld,this.getPos().toCenterPos(), 0);
      ArcanaNovum.addTickTimerCallback(serverWorld, new GenericTimer(100, () -> {
         changeWeather(finalPlayer);
         if(finalPlayer != null) ArcanaNovum.data(finalPlayer).addXP(ArcanaConfig.getInt(ArcanaRegistry.STORMCALLER_ALTAR_ACTIVATE));
      }));
      return true;
   }
   
   public void openGui(ServerPlayerEntity player){
      if(active){
         player.sendMessage(Text.literal("You cannot access an active Altar").formatted(Formatting.RED));
         return;
      }
      StormcallerAltarGui gui = new StormcallerAltarGui(player,this);;
      gui.build();
      gui.open();
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof StormcallerAltarBlockEntity altar){
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
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(((MultiblockCore) ArcanaRegistry.STORMCALLER_ALTAR).getCheckOffset()),null);
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
      
      boolean activatable = serverWorld.getBlockState(pos).getOrEmpty(StormcallerAltar.StormcallerAltarBlock.ACTIVATABLE).orElse(false);
      boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
      if(activatable ^ shouldBeActivatable){
         serverWorld.setBlockState(pos, serverWorld.getBlockState(pos).with(StormcallerAltar.StormcallerAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.NOTIFY_LISTENERS);
      }
   }
   
   public boolean isActive(){
      return active;
   }
   
   public void setActive(boolean active){
      this.active = active;
      
      if(this.world instanceof ServerWorld serverWorld){
         boolean activatable = serverWorld.getBlockState(pos).getOrEmpty(StormcallerAltar.StormcallerAltarBlock.ACTIVATABLE).orElse(false);
         boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
         if(activatable ^ shouldBeActivatable){
            serverWorld.setBlockState(pos, serverWorld.getBlockState(pos).with(StormcallerAltar.StormcallerAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.NOTIFY_LISTENERS);
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
   
   public boolean isSynthetic(){
      return synthetic;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.STORMCALLER_ALTAR;
   }
   
   @Override
   public void readData(ReadView view){
      super.readData(view);
      this.uuid = view.getString("arcanaUuid", "");
      this.crafterId = view.getString("crafterId", "");
      this.customName = view.getString("customName", "");
      this.synthetic = view.getBoolean("synthetic", false);
      this.cooldown = view.getInt("cooldown", 0);
      this.mode = view.getInt("mode", 0);
      this.duration = view.getInt("duration", 0);
      this.augments = new TreeMap<>();
      view.read("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
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
      view.putInt("mode",this.mode);
      view.putInt("duration",this.duration);
   }
}
