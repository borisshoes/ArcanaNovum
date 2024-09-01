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
import net.borisshoes.arcananovum.gui.altars.TransmutationAltarGui;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static net.borisshoes.arcananovum.ArcanaNovum.ACTIVE_ARCANA_BLOCKS;
import static net.borisshoes.arcananovum.blocks.altars.TransmutationAltar.TransmutationAltarBlock.HORIZONTAL_FACING;

public class TransmutationAltarBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private int cooldown;
   private boolean active;
   private final Multiblock multiblock;
   
   public TransmutationAltarBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.TRANSMUTATION_ALTAR_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.TRANSMUTATION_ALTAR).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
      resetCooldown();
   }
   
   public void openGui(ServerPlayerEntity player){
      if(active){
         player.sendMessage(Text.literal("You cannot access an active Altar").formatted(Formatting.RED));
         return;
      }
      TransmutationAltarGui gui = new TransmutationAltarGui(ScreenHandlerType.HOPPER,player,this);
      gui.buildMenuGui();
      gui.open();
   }
   
   public HashMap<String, ItemEntity> getTransmutingStacks(){
      HashMap<String, ItemEntity> stacks = new HashMap<>();
      if(this.world == null || this.pos == null) return stacks;
      Direction direction = world.getBlockState(pos).get(HORIZONTAL_FACING);
      Vec3d centerPos = getPos().toCenterPos();
      Vec3d aequalisPos = centerPos.add(new Vec3d(0,0.6,0).rotateY((float) -(direction.getHorizontal()*(Math.PI/2.0f))));
      Vec3d negativePos = centerPos.add(new Vec3d(3,0.6,0).rotateY((float) -(direction.getHorizontal()*(Math.PI/2.0f))));
      Vec3d positivePos = centerPos.add(new Vec3d(-3,0.6,0).rotateY((float) -(direction.getHorizontal()*(Math.PI/2.0f))));
      Vec3d reagent1Pos = centerPos.add(new Vec3d(0,0.6,-3).rotateY((float) -(direction.getHorizontal()*(Math.PI/2.0f))));
      Vec3d reagent2Pos = centerPos.add(new Vec3d(0,0.6,3).rotateY((float) -(direction.getHorizontal()*(Math.PI/2.0f))));
      
      ItemEntity aequalisEntity = MiscUtils.getLargestItemEntity(this.world.getEntitiesByType(EntityType.ITEM,new Box(BlockPos.ofFloored(aequalisPos)), e -> true));
      ItemEntity positiveEntity = MiscUtils.getLargestItemEntity(this.world.getEntitiesByType(EntityType.ITEM,new Box(BlockPos.ofFloored(positivePos)), e -> true));
      ItemEntity negativeEntity = MiscUtils.getLargestItemEntity(this.world.getEntitiesByType(EntityType.ITEM,new Box(BlockPos.ofFloored(negativePos)), e -> true));
      ItemEntity reagent1Entity = MiscUtils.getLargestItemEntity(this.world.getEntitiesByType(EntityType.ITEM,new Box(BlockPos.ofFloored(reagent1Pos)), e -> true));
      ItemEntity reagent2Entity = MiscUtils.getLargestItemEntity(this.world.getEntitiesByType(EntityType.ITEM,new Box(BlockPos.ofFloored(reagent2Pos)), e -> true));
      
      stacks.put("aequalis",aequalisEntity);
      stacks.put("positive",positiveEntity);
      stacks.put("negative",negativeEntity);
      stacks.put("reagent1",reagent1Entity);
      stacks.put("reagent2",reagent2Entity);
      
      return stacks;
   }
   
   public Vec3d getOutputPos(String outputString){
      if(this.world == null || this.pos == null) return null;
      Direction direction = world.getBlockState(pos).get(HORIZONTAL_FACING);
      Vec3d centerPos = getPos().toCenterPos();
      Vec3d aequalisPos = centerPos.add(new Vec3d(0,0.6,0).rotateY((float) -(direction.getHorizontal()*(Math.PI/2.0f))));
      Vec3d negativePos = centerPos.add(new Vec3d(3,0.6,0).rotateY((float) -(direction.getHorizontal()*(Math.PI/2.0f))));
      Vec3d positivePos = centerPos.add(new Vec3d(-3,0.6,0).rotateY((float) -(direction.getHorizontal()*(Math.PI/2.0f))));
      Vec3d reagent1Pos = centerPos.add(new Vec3d(0,0.6,-3).rotateY((float) -(direction.getHorizontal()*(Math.PI/2.0f))));
      Vec3d reagent2Pos = centerPos.add(new Vec3d(0,0.6,3).rotateY((float) -(direction.getHorizontal()*(Math.PI/2.0f))));
      
      if(outputString.equals("positive")){
         return positivePos;
      }else if(outputString.equals("negative")){
         return negativePos;
      }else if(outputString.equals("reagent1")){
         return reagent1Pos;
      }else if(outputString.equals("reagent2")){
         return reagent2Pos;
      }else{
         return aequalisPos;
      }
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof TransmutationAltarBlockEntity altar){
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
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(-5,0,-5),world.getBlockState(pos).get(HORIZONTAL_FACING));
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
      this.cooldown = 36000 - ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.HASTY_BARGAIN.id) * 6000;
   }
   
   public void refundCooldown(){
      this.cooldown = 0;
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
      return ArcanaRegistry.TRANSMUTATION_ALTAR;
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
   }
}
