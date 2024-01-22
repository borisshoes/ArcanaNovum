package net.borisshoes.arcananovum.blocks.altars;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlockEntity;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.gui.altars.StarpathAltarGui;
import net.borisshoes.arcananovum.gui.altars.StarpathTargetGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class StarpathAltarBlockEntity extends BlockEntity implements PolymerObject, MagicBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private int cooldown;
   private BlockPos targetCoords;
   private HashMap<String,BlockPos> savedTargets;
   private int activeTicks;
   
   public StarpathAltarBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.STARPATH_ALTAR_BLOCK_ENTITY, pos, state);
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
   
   public HashMap<String,BlockPos> getSavedTargets(){
      return this.savedTargets;
   }
   
   public void openTargetGui(ServerPlayerEntity player){
      StarpathTargetGui gui = new StarpathTargetGui(player,this);
      if(!gui.tryOpen(player)){
         player.sendMessage(Text.literal("Someone else is using the Altar").formatted(Formatting.RED),true);
      }
   }
   
   public void openGui(ServerPlayerEntity player){
      StarpathAltarGui gui = new StarpathAltarGui(player,this);;
      gui.build();
      if(!gui.tryOpen(player)){
         player.sendMessage(Text.literal("Someone else is using the Altar").formatted(Formatting.RED),true);
      }
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
   
   private void tick(){
      if(cooldown > 0) cooldown--;
      
      if(world != null){
         boolean value = activeTicks > 0;
         
         for(BlockPos blockPos : BlockPos.iterateOutwards(pos, 4, 0, 4)){
            BlockState state =  world.getBlockState(blockPos);
            if((state.isOf(Blocks.SCULK_CATALYST) || blockPos.equals(pos)) && state.get(Properties.BLOOM) != value){
               world.setBlockState(blockPos,state.with(Properties.BLOOM, value), Block.NOTIFY_ALL);
            }
         }
         
         if(value){
            activeTicks--;
         }
      }
      this.markDirty();
   }
   
   public void setActiveTicks(int ticks){
      this.activeTicks = ticks;
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
   
   public MagicItem getMagicItem(){
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
         this.savedTargets.put(target.getString("name"),new BlockPos(target.getInt("x"),target.getInt("y"),target.getInt("z")));
      }
   }
   
   @Override
   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
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
      if (nbt.contains("target")) {
         NbtCompound targetTag = nbt.getCompound("target");
         this.targetCoords = new BlockPos(targetTag.getInt("x"),targetTag.getInt("y"),targetTag.getInt("z"));
      }
      augments = new TreeMap<>();
      if(nbt.contains("arcanaAugments")){
         NbtCompound augCompound = nbt.getCompound("arcanaAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug,augCompound.getInt(key));
         }
      }
      if(nbt.contains("targets")){
         readTargets(nbt.getList("targets", NbtElement.COMPOUND_TYPE));
      }
   }
   
   @Override
   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
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
      if(this.targetCoords != null){
         NbtCompound targetTag = new NbtCompound();
         targetTag.putInt("x",targetCoords.getX());
         targetTag.putInt("y",targetCoords.getY());
         targetTag.putInt("z",targetCoords.getZ());
         nbt.put("target",targetTag);
      }
      nbt.putBoolean("synthetic",this.synthetic);
      nbt.putInt("cooldown",this.cooldown);
      nbt.put("targets",writeTargets());
   }
}
