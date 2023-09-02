package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.*;
import net.borisshoes.arcananovum.gui.arcanesingularity.ArcaneSingularityGui;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static net.borisshoes.arcananovum.blocks.forge.StellarCore.StellarCoreBlock.HORIZONTAL_FACING;

public class ArcaneSingularityBlockEntity extends BlockEntity implements PolymerObject, MagicBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private ArrayList<ItemStack> books;
   
   public ArcaneSingularityBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.ARCANE_SINGULARITY).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
      this.books = new ArrayList<>();
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof ArcaneSingularityBlockEntity singularity){
         singularity.tick();
      }
   }
   
   private void tick(){
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return;
      }
      int ticks = serverWorld.getServer().getTicks();
      
      if(ticks % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
      }
      
      if(assembled){
         Direction dir = serverWorld.getBlockState(pos).get(ArcaneSingularity.ArcaneSingularityBlock.HORIZONTAL_FACING);
         Vec3d center = pos.add(dir.getVector().multiply(-1)).toCenterPos().add(0,2.5,0);
         ParticleEffectUtils.arcaneSingularityAnim(serverWorld,center,ticks % 300,dir);
         
         if(Math.random() < 0.01){
            SoundUtils.playSound(serverWorld,BlockPos.ofFloored(center), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS,0.3f,1+(float)Math.random());
         }
         if(Math.random() < 0.005){
            SoundUtils.playSound(serverWorld,BlockPos.ofFloored(center), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS,1f,0.5f);
         }
      }
   }
   
   public ArrayList<ItemStack> getBooks(){
      return this.books;
   }
   
   public int getCapacity(){
      return 56*(1 + ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.SUPERMASSIVE.id));
   }
   
   public void openGui(ServerPlayerEntity player){
      
      ArcaneSingularityGui gui = new ArcaneSingularityGui(player,this,getCapacity());
      gui.buildGui();
      if(!gui.tryOpen(player)){
         player.sendMessage(Text.literal("Someone else is using the Singularity").formatted(Formatting.RED),true);
      }
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(-1,-1,-2),serverWorld.getBlockState(pos).get(HORIZONTAL_FACING));
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
      return ArcanaRegistry.ARCANE_SINGULARITY;
   }
   
   public NbtList writeBooks(){
      if(this.books != null){
         NbtList bookList = new NbtList();
         for(ItemStack book : books){
            bookList.add(book.writeNbt(new NbtCompound()));
         }
         return bookList;
      }else{
         return new NbtList();
      }
   }
   
   public void readBooks(NbtList bookList){
      this.books = new ArrayList<>();
      for(NbtElement e : bookList){
         this.books.add(ItemStack.fromNbt((NbtCompound) e));
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
      augments = new TreeMap<>();
      if(nbt.contains("arcanaAugments")){
         NbtCompound augCompound = nbt.getCompound("arcanaAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug,augCompound.getInt(key));
         }
      }
      if(nbt.contains("books")){
         readBooks(nbt.getList("books", NbtElement.COMPOUND_TYPE));
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
      nbt.put("books",writeBooks());
      nbt.putBoolean("synthetic",this.synthetic);
   }
}
