package net.borisshoes.arcananovum.blocks.astralgateway;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AstralGatewayPortalBlock extends BaseEntityBlock implements PolymerBlock {
   
   public AstralGatewayPortalBlock(BlockBehaviour.Properties properties){
      super(properties.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID,"astral_gateway_portal"))));
   }
   
   @Override
   protected MapCodec<? extends BaseEntityBlock> codec(){
      return null;
   }
   
   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
      super.createBlockStateDefinition(builder);
   }
   
   @Override
   public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext){
      return Blocks.END_GATEWAY.defaultBlockState();
   }
   
   @Override
   protected RenderShape getRenderShape(BlockState blockState){
      return RenderShape.INVISIBLE;
   }
   
   @Override
   public void onPolymerBlockSend(BlockState blockState, BlockPos.MutableBlockPos pos, PacketContext.NotNullWithPlayer contexts){
      CompoundTag main = new CompoundTag();
      main.putString("id", "minecraft:end_gateway");
      main.putInt("x", pos.getX());
      main.putInt("y", pos.getY());
      main.putInt("z", pos.getZ());
      main.putLong("Age", Long.MIN_VALUE);
      contexts.getPlayer().connection.send(PolymerBlockUtils.createBlockEntityPacket(pos.immutable(),BlockEntityType.END_GATEWAY,main));
   }
   
   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState){
      return new AstralGatewayPortalBlockEntity(blockPos, blockState);
   }
   
   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType){
      return createTickerHelper(blockEntityType, ArcanaRegistry.ASTRAL_GATEWAY_PORTAL_BLOCK_ENTITY, AstralGatewayPortalBlockEntity::ticker);
   }
   
   @Override
   protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl){
      BlockEntity blockEntity = level.getBlockEntity(blockPos);
      if(!(blockEntity instanceof AstralGatewayPortalBlockEntity portalEntity)) return;
      AstralGatewayBlockEntity gateway = portalEntity.getGateway();
      if(gateway == null) return;
      if(entity.canUsePortal(false)){
         entity.setAsInsidePortal(gateway, blockPos);
      }
   }
}
