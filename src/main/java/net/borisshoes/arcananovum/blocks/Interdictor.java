package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGatewayBlockEntity;
import net.borisshoes.arcananovum.core.*;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.endercrate.EnderCrateChannelGui;
import net.borisshoes.arcananovum.gui.endercrate.EnderCrateGui;
import net.borisshoes.arcananovum.gui.interdictor.InterdictorGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class Interdictor extends ArcanaBlock implements MultiblockCore {
   public static final String ID = "interdictor";
   
   private Multiblock multiblock;
   
   public Interdictor(){
      id = ID;
      name = "Interdictor";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.BEACON;
      block = new InterdictorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(6.0f, 1200.0f).sound(SoundType.VAULT));
      item = new InterdictorItem(block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_EXOTIC_MATTER,ResearchTasks.OBTAIN_BEACON,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN,ResearchTasks.OBTAIN_END_CRYSTAL,ResearchTasks.USE_ENDER_EYE,ResearchTasks.ADVANCEMENT_KILL_A_MOB};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("beacon ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("that has been modified with ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("dimensional energy").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("It ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("blocks ").withStyle(ChatFormatting.RED))
            .append(Component.literal("the coalescence of new ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("mob essence").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Interdictor ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("is comprised of a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("3x3x3").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("multiblock").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("When active, it ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("stops ").withStyle(ChatFormatting.RED))
            .append(Component.literal("new ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("mob spawns").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" in a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("large area").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("redstone ").withStyle(ChatFormatting.RED))
            .append(Component.literal("signal to the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Interdictor ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("activates ").withStyle(ChatFormatting.RED))
            .append(Component.literal("it.").withStyle(ChatFormatting.YELLOW)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("     Interdictor").withStyle(ChatFormatting.AQUA,ChatFormatting.BOLD),Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Component.literal("\nCreepers have blown up my house for the last time! Sometimes, no amount of torches is enough! Taking the area effects of a Beacon, and adding in a bit of dimensional energy using some techniques I picked").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Interdictor").withStyle(ChatFormatting.AQUA,ChatFormatting.BOLD),Component.literal("\nup from the End-dwellers, I have created a contraption that broadcasts a jamming signal that prevents new mob essence from coalescing.\n\nLike a normal Beacon, the Interdictor requires some").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Interdictor").withStyle(ChatFormatting.AQUA,ChatFormatting.BOLD),Component.literal("\nstructural support, though a bit more involved than a Beacon, just something to broadcast the specialized field.\n\nWhen assembled and given a redstone signal through a lever or torch on the").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Interdictor").withStyle(ChatFormatting.AQUA,ChatFormatting.BOLD),Component.literal("\nstructure's diamond block, the Interdictor will stop hostile mobs from spawning in a 32 block radial cube.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   @Override
   public Vec3i getCheckOffset(){
      return new Vec3i(-1,-1,-1);
   }
   
   public class InterdictorItem extends ArcanaPolymerBlockItem {
      public InterdictorItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public @NonNull ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(@NonNull ItemStack stack, @NonNull ServerLevel world, @NonNull Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
         
      }
      
      @Override
      public @NonNull InteractionResult use(@NonNull Level world, Player playerEntity, @NonNull InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         
         return InteractionResult.PASS;
      }
   }
   
   public class InterdictorBlock extends ArcanaPolymerBlockEntity {
      public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
      
      public InterdictorBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.BEACON.defaultBlockState();
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(ACTIVE);
      }
      
      @org.jspecify.annotations.Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
         return this.defaultBlockState().setValue(ACTIVE, false);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.INTERDICTOR_BLOCK_ENTITY, InterdictorBlockEntity::ticker);
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState){
         return new InterdictorBlockEntity(blockPos, blockState);
      }
      
      @Override
      protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @org.jspecify.annotations.Nullable Orientation orientation, boolean bl) {
         if (!level.isClientSide()) {
            boolean currentlyActive = blockState.getValue(ACTIVE);
            if (currentlyActive != level.hasNeighborSignal(blockPos)) {
               if (currentlyActive) {
                  level.scheduleTick(blockPos, this, 4);
               } else if (level.getBlockEntity(blockPos) instanceof InterdictorBlockEntity interdictor && interdictor.isAssembled()) {
                  level.setBlock(blockPos,blockState.setValue(ACTIVE,true),Block.UPDATE_ALL);
               }
            }
         }
      }
      
      @Override
      protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
         if (blockState.getValue(ACTIVE) && !serverLevel.hasNeighborSignal(blockPos)) {
            serverLevel.setBlock(blockPos, blockState.cycle(ACTIVE), 2);
         }
      }
      
      @Override
      protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult){
         if(level.getBlockEntity(blockPos) instanceof InterdictorBlockEntity interdictor){
            if(player instanceof ServerPlayer serverPlayer && !player.isShiftKeyDown()){
               if(interdictor.isAssembled()){
                  InterdictorGui gui = new InterdictorGui(serverPlayer,interdictor);
                  gui.open();
               }else{
                  serverPlayer.sendSystemMessage(Component.literal("Multiblock not constructed."));
                  multiblock.displayStructure(interdictor.getMultiblockCheck(),serverPlayer);
               }
            }
            return InteractionResult.PASS;
         }else{
            return InteractionResult.PASS;
         }
      }
   }
}

