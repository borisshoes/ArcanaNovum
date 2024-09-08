package net.borisshoes.arcananovum.blocks.altars;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.altars.StormcallerAltarGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

public class StormcallerAltarBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
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
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(-5,0,-5),null);
   }
   
   private void tick(){
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return;
      }
      
      if(isAssembled() && cooldown > 0){
         cooldown--;
         this.markDirty();
      }
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.isAssembled()){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
   }
   
   public boolean isActive(){
      return active;
   }
   
   public void setActive(boolean active){
      this.active = active;
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
   public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
      super.readNbt(nbt, registryLookup);
      if (nbt.contains("arcanaUuid")) {
         this.uuid = nbt.getString("arcanaUuid");
      }
      if (nbt.contains("crafterId")) {
         this.crafterId = nbt.getString("crafterId");
      }
      if (nbt.contains("customName")) {
         this.customName = nbt.getString("customName");
      }
      if (nbt.contains("synthetic")) {
         this.synthetic = nbt.getBoolean("synthetic");
      }
      if (nbt.contains("cooldown")) {
         this.cooldown = nbt.getInt("cooldown");
      }
      if (nbt.contains("mode")) {
         this.mode = nbt.getInt("mode");
      }
      if (nbt.contains("duration")) {
         this.duration = nbt.getInt("duration");
      }
      augments = new TreeMap<>();
      if(nbt.contains("arcanaAugments")){
         NbtCompound augCompound = nbt.getCompound("arcanaAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug,augCompound.getInt(key));
         }
      }
   }
   
   @Override
   protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
      super.writeNbt(nbt, registryLookup);
      if(augments != null){
         NbtCompound augsCompound = new NbtCompound();
         for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
            augsCompound.putInt(entry.getKey().id,entry.getValue());
         }
         nbt.put("arcanaAugments",augsCompound);
      }
      if(this.uuid != null){
         nbt.putString("arcanaUuid",this.uuid);
      }
      if(this.crafterId != null){
         nbt.putString("crafterId",this.crafterId);
      }
      if(this.customName != null){
         nbt.putString("customName",this.customName);
      }
      nbt.putBoolean("synthetic",this.synthetic);
      nbt.putInt("cooldown",this.cooldown);
      nbt.putInt("mode",this.mode);
      nbt.putInt("duration",this.duration);
   }
}
