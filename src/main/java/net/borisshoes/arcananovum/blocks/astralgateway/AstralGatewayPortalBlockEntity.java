package net.borisshoes.arcananovum.blocks.astralgateway;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class AstralGatewayPortalBlockEntity extends BlockEntity implements PolymerObject {
   
   private static final String GATEWAY_POS_TAG = "gatewayPos";
   private static final String KEEP_ALIVE_TAG = "keepAlive";
   
   private BlockPos gatewayPos;
   private AstralGatewayBlockEntity gateway;
   private int keepAlive;
   
   public AstralGatewayPortalBlockEntity(BlockPos blockPos, BlockState blockState){
      super(ArcanaRegistry.ASTRAL_GATEWAY_PORTAL_BLOCK_ENTITY, blockPos, blockState);
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof AstralGatewayPortalBlockEntity portal){
         portal.tick();
      }
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(--keepAlive < 0){
         serverWorld.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 11);
         return;
      }
      
      if(serverWorld.random.nextFloat() < 0.015f){
         serverWorld.sendParticles(ParticleTypes.END_ROD, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, 1, 0.3, 0.3, 0.3, 0.015);
      }
      
      // Validate gateway reference
      if(gateway == null && gatewayPos != null){
         BlockEntity be = serverWorld.getBlockEntity(gatewayPos);
         if(be instanceof AstralGatewayBlockEntity gatewayEntity){
            this.gateway = gatewayEntity;
         }
      }
   }
   
   public void initialize(BlockPos gatewayPos, int keepAlive){
      this.gatewayPos = gatewayPos;
      this.keepAlive = keepAlive;
      this.gateway = null;
      setChanged();
   }
   
   public void refreshKeepAlive(int keepAlive){
      this.keepAlive = keepAlive;
      setChanged();
   }
   
   @Nullable
   public AstralGatewayBlockEntity getGateway(){
      if(gateway == null && gatewayPos != null && level != null){
         BlockEntity be = level.getBlockEntity(gatewayPos);
         if(be instanceof AstralGatewayBlockEntity gatewayEntity){
            this.gateway = gatewayEntity;
         }
      }
      return gateway;
   }
   
   public BlockPos getGatewayPos(){
      return gatewayPos;
   }
   
   public int getKeepAlive(){
      return keepAlive;
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.keepAlive = view.getIntOr(KEEP_ALIVE_TAG, 0);
      int[] posArray = view.getIntArray(GATEWAY_POS_TAG).orElse(new int[0]);
      if(posArray.length == 3){
         this.gatewayPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
      }else{
         this.gatewayPos = null;
      }
      this.gateway = null; // Will be resolved on next tick or getGateway call
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.putInt(KEEP_ALIVE_TAG, this.keepAlive);
      if(this.gatewayPos != null){
         view.putIntArray(GATEWAY_POS_TAG, new int[]{gatewayPos.getX(), gatewayPos.getY(), gatewayPos.getZ()});
      }
   }
}
