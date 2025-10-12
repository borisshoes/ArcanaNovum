package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.midnightenchanter.MidnightEnchanterGui;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

public class MidnightEnchanterBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private boolean seenForge;
   private boolean hasBooks;
   
   public MidnightEnchanterBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.MIDNIGHT_ENCHANTER_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.MIDNIGHT_ENCHANTER).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof MidnightEnchanterBlockEntity enchanter){
         enchanter.tick();
      }
   }
   
   private void tick(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return;
      }
      int ticks = serverWorld.getServer().getTicks();
      
      if(ticks % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
         this.seenForge = StarlightForge.findActiveForge(serverWorld,pos) != null;
         
         int bookshelfCount = 0;
         for(BlockPos blockPos : BlockPos.iterate(pos.add(-2, -2, -2), pos.add(2, 2, 2))){
            if(world.getBlockState(blockPos).isOf(Blocks.BOOKSHELF) || world.getBlockState(blockPos).isOf(Blocks.CHISELED_BOOKSHELF)){
               bookshelfCount++;
            }
         }
         hasBooks = bookshelfCount >= 20;
      }
      
      if(assembled && seenForge && hasBooks){
         ArcanaEffectUtils.midnightEnchanterAnim(serverWorld,pos.toCenterPos(),ticks % 300);
      }
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.assembled && this.seenForge){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
   }
   
   public void openGui(ServerPlayerEntity player){
      MidnightEnchanterGui gui = new MidnightEnchanterGui(player,this);
      gui.buildGui();
      gui.open();
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public boolean hasBooks(){
      return hasBooks;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(((MultiblockCore) ArcanaRegistry.MIDNIGHT_ENCHANTER).getCheckOffset()),null);
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
      return ArcanaRegistry.MIDNIGHT_ENCHANTER;
   }
   
   @Override
   public void readData(ReadView view){
      super.readData(view);
      this.uuid = view.getString(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getString(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getString(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getInt(ArcanaBlockEntity.ORIGIN_TAG, 0);
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
   }
}
