package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.blocks.altars.CelestialAltarBlockEntity.COST;

public class CelestialAltar extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "celestial_altar";
   
   private Multiblock multiblock;
   
   public CelestialAltar(){
      id = ID;
      name = "Celestial Altar";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS, ArcaneTomeGui.TomeFilter.ALTARS};
      itemVersion = 0;
      vanillaItem = Items.PEARLESCENT_FROGLIGHT;
      block = new CelestialAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(.3f,1200.0f).lightLevel(state -> 15).sound(SoundType.FROGLIGHT));
      item = new CelestialAltarItem(this.block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_STARDUST,ResearchTasks.OBTAIN_NETHER_STAR,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addAltarLore(lore);
      lore.add(Component.literal("Celestial Altar:").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("glistens ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("in the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("light ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("of the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Sun ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("and ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Moon").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("lets you change the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("time of day").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" and the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("phase").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" of the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Moon").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("requires a ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Nether Star").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("activate").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Vec3i getCheckOffset(){
      return new Vec3i(-5,0,-5);
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Celestial Altar").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nLeylines across the world have their influence extend into the space beyond the world. If I can provide a sufficient energy source to the structure, I should be able to accelerate the sun or moon!").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Celestial Altar").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nA Nether Star should be sufficient to let this altar change the time of day and the phase of the moon by accelerating the motion of the celestial bodies for a moment before the Star is depleted.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class CelestialAltarItem extends ArcanaPolymerBlockItem {
      public CelestialAltarItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class CelestialAltarBlock extends ArcanaPolymerBlockEntity {
      public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
      public static final BooleanProperty ACTIVATABLE = BooleanProperty.create("activatable");
      
      public CelestialAltarBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState();
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(HORIZONTAL_FACING,ctx.getHorizontalDirection().getOpposite()).setValue(ACTIVATABLE,false);
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(HORIZONTAL_FACING,ACTIVATABLE);
      }
      
      @Override
      public BlockState rotate(BlockState state, Rotation rotation){
         return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, Mirror mirror){
         return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new CelestialAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.CELESTIAL_ALTAR_BLOCK_ENTITY, CelestialAltarBlockEntity::ticker);
      }
      
      @Override
      protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         CelestialAltarBlockEntity altar = (CelestialAltarBlockEntity) world.getBlockEntity(pos);
         if(altar != null){
            if(playerEntity instanceof ServerPlayer player){
               if(altar.isAssembled()){
                  altar.openGui(player);
                  player.getCooldowns().addCooldown(playerEntity.getMainHandItem(),1);
                  player.getCooldowns().addCooldown(playerEntity.getOffhandItem(),1);
               }else{
                  player.sendSystemMessage(Component.literal("Multiblock not constructed."));
                  multiblock.displayStructure(altar.getMultiblockCheck(),player);
               }
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      private void tryActivate(BlockState state, Level world, BlockPos pos){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof CelestialAltarBlockEntity altar && world instanceof ServerLevel serverWorld){
            boolean paid = MinecraftUtils.removeItemEntities(serverWorld,new AABB(pos.above()),(itemStack) -> itemStack.is(COST.getA()),COST.getB());
            if(paid) altar.startStarChange(null);
         }
      }
      
      @Override
      protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
         boolean bl = world.hasNeighborSignal(pos);
         boolean bl2 = state.getOptionalValue(ACTIVATABLE).orElse(false);
         if (bl && bl2) {
            this.tryActivate(state, world, pos);
            world.setBlock(pos, state.setValue(ACTIVATABLE, false), Block.UPDATE_CLIENTS);
         }
      }
   }
}

