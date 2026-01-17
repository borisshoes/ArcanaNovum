package net.borisshoes.arcananovum.blocks.astralgateway;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.astralgateway.AstralGatewayGui;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AstralGateway extends ArcanaBlock {
   public static final String ID = "astral_gateway";
   
   public static final String STARDUST_TAG = "stardust";
   public static final String WAYSTONES_TAG = "waystones";
   
   public AstralGateway(){
      id = ID;
      name = "Astral Gateway";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.END_PORTAL_FRAME;
      block = new AstralGatewayBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(4.0f, 1200.0f).sound(SoundType.HEAVY_CORE));
      item = new AstralGatewayItem(block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);
      researchTasks = new ResourceKey[]{};  // TODO
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack, STARDUST_TAG,0);
      putProperty(stack, WAYSTONES_TAG,new ListTag());
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ListTag stonesList = getListProperty(stack,WAYSTONES_TAG);
      long stardust = getLongProperty(stack,STARDUST_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,WAYSTONES_TAG,stonesList);
      putProperty(newStack,STARDUST_TAG,stardust);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Portals ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("have been such ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("unreliable constructs").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("...").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Stellar-Leyline").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("navigation ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("has proven much more ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("reliable").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Gateway").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("syncs ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("to another using a ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Waystone").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Gateway ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("will fill ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("any suitable frame").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" it is placed near.").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Maintaining ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("portal ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("requires a steady supply of ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Stardust").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("redstone signal").withStyle(ChatFormatting.RED))
            .append(Component.literal(" will ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("activate ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("this ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("and the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("synced Gateway").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      
      if(itemStack != null){
         int size = getListProperty(itemStack,WAYSTONES_TAG).size();
         int dust = getIntProperty(itemStack,STARDUST_TAG);
         if(size > 0 || dust > 0){
            lore.add(Component.literal(""));
         }
         if(size > 0){
            lore.add(Component.literal("")
                  .append(Component.literal("Contains ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(""+size).withStyle(ChatFormatting.WHITE))
                  .append(Component.literal(" Waystones").withStyle(ChatFormatting.GRAY)));
         }
         if(dust > 0){
            lore.add(Component.literal("")
                  .append(Component.literal("Contains ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(""+dust).withStyle(ChatFormatting.YELLOW))
                  .append(Component.literal(" Stardust").withStyle(ChatFormatting.GOLD)));
         }
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("TODO").withStyle(ChatFormatting.BLACK))); // TODO
      return list;
   }
   
   public class AstralGatewayItem extends ArcanaPolymerBlockItem {
      public AstralGatewayItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public @NonNull ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class AstralGatewayBlock extends ArcanaPolymerBlockEntity {
      public static final EnumProperty<GatewayState> STATE = EnumProperty.create("gateway_state", GatewayState.class);
      public static final EnumProperty<GatewayMode> MODE = EnumProperty.create("gateway_mode", GatewayMode.class);
      public static final EnumProperty<Direction> HORIZONTAL_FACING = HorizontalDirectionalBlock.FACING;
      public static final BooleanProperty HAS_EYE = BlockStateProperties.EYE;
      
      public AstralGatewayBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(HAS_EYE,state.getValue(HAS_EYE)).setValue(HORIZONTAL_FACING,state.getValue(HORIZONTAL_FACING));
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState()
               .setValue(STATE,GatewayState.CLOSED)
               .setValue(MODE,GatewayMode.BOTH)
               .setValue(HAS_EYE,false)
               .setValue(HORIZONTAL_FACING,ctx.getHorizontalDirection().getOpposite());
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(HORIZONTAL_FACING, HAS_EYE, STATE, MODE);
      }
      
      @Override
      public BlockState rotate(BlockState state, Rotation rotation){
         return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, Mirror mirror){
         return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.ASTRAL_GATEWAY_BLOCK_ENTITY, AstralGatewayBlockEntity::ticker);
      }
      
      @Override
      public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new AstralGatewayBlockEntity(pos,state);
      }
      
      @Override
      protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult){
         AstralGatewayBlockEntity gateway = (AstralGatewayBlockEntity) level.getBlockEntity(blockPos);
         if(gateway != null){
            boolean validWaystone = gateway.validWaystone(itemStack);
            if(itemStack.is(ArcanaRegistry.WAYSTONE.getItem()) && !validWaystone){
               return InteractionResult.PASS;
            }
            if(validWaystone && gateway.getInventory().getItem(0).isEmpty() && blockState.getValue(MODE) != GatewayMode.RECEIVE_ONLY){
               if(ArcanaAugments.getAugmentFromMap(gateway.getAugments(),ArcanaAugments.ASTRAL_STARGATE) < 1 && !Waystone.getTarget(itemStack).world().identifier().equals(level.dimension().identifier()))
                  return InteractionResult.TRY_WITH_EMPTY_HAND;
               gateway.getInventory().setItem(0,itemStack.copy());
               gateway.setChanged();
               player.getInventory().removeItem(itemStack);
               SoundUtils.playSound(level, gateway.getBlockPos(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.5f, 2.0f);
               return InteractionResult.SUCCESS_SERVER;
            }else if(itemStack.is(ArcanaRegistry.STARDUST)){
               gateway.getInventory().setItem(1,itemStack.copy());
               gateway.setChanged();
               player.getInventory().removeItem(itemStack);
               SoundUtils.playSound(level, gateway.getBlockPos(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 0.5f, 2f);
               return InteractionResult.SUCCESS_SERVER;
            }
         }
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      }
      
      @Override
      protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         AstralGatewayBlockEntity gateway = (AstralGatewayBlockEntity) world.getBlockEntity(pos);
         if(gateway != null){
            if(playerEntity instanceof ServerPlayer player){
               boolean hitTop = hit.getDirection() == Direction.UP || (hit.getLocation().y - pos.getY()) > 0.8125;
               if(!gateway.getInventory().getItem(0).isEmpty() && hitTop){
                  ItemStack stone = gateway.getInventory().getItem(0).copy();
                  gateway.getInventory().setItem(0, ItemStack.EMPTY);
                  gateway.setChanged();
                  SoundUtils.playSound(world, gateway.getBlockPos(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.5f, 2.0f);
                  if(!player.addItem(stone)){
                     ItemEntity itemEntity = player.drop(stone, false);
                     if(itemEntity == null) return InteractionResult.SUCCESS_SERVER;
                     itemEntity.setNoPickUpDelay();
                     itemEntity.setTarget(player.getUUID());
                  }
                  return InteractionResult.SUCCESS_SERVER;
               }else if(!player.isShiftKeyDown()){
                  AstralGatewayGui gui = new AstralGatewayGui(player,gateway);
                  player.getCooldowns().addCooldown(playerEntity.getMainHandItem(),1);
                  player.getCooldowns().addCooldown(playerEntity.getOffhandItem(),1);
               }
               
            }
         }
         return InteractionResult.PASS;
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof AstralGatewayBlockEntity gateway){
            initializeArcanaBlock(stack,gateway);
            gateway.readStardustAndStones(getIntProperty(stack, STARDUST_TAG),getListProperty(stack, WAYSTONES_TAG),world.registryAccess());
         }
      }
      
      @Override
      protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof AstralGatewayBlockEntity gateway){
            gateway.evaluateForOpenOrClose();
         }
      }
   }
}

