package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity.COST;

public class StarpathAltar extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "starpath_altar";
   
   public static final String TARGETS_TAG = "targets";
   
   private Multiblock multiblock;
   
   public StarpathAltar(){
      id = ID;
      name = "Starpath Altar";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS, ArcaneTomeGui.TomeFilter.ALTARS};
      itemVersion = 0;
      vanillaItem = Items.SCULK_CATALYST;
      block = new StarpathAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0f,1200.0f).lightLevel(state -> 6).sound(SoundType.SCULK_CATALYST));
      item = new StarpathAltarItem(this.block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_STARDUST,ResearchTasks.USE_ENDER_EYE,ResearchTasks.USE_ENDER_PEARL,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN,ResearchTasks.UNLOCK_WAYSTONE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,TARGETS_TAG,new ListTag());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addAltarLore(lore);
      lore.add(Component.literal("Starpath Altar:").withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("finds a ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("path ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("through the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("stars ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("to ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("anywhere ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("in the world.").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("All creatures").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" standing in the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("will be ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("teleported").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("requires ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Eyes of Ender").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("activate").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      
      if(itemStack != null){
         int size = getListProperty(itemStack,TARGETS_TAG).size();
         if(size > 0){
            lore.add(Component.literal(""));
            lore.add(Component.literal("")
                  .append(Component.literal("Targets Stored: ").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal(""+size).withStyle(ChatFormatting.DARK_AQUA)));
         }
      }
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ListTag targetsList = getListProperty(stack,TARGETS_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,TARGETS_TAG,targetsList);
      return buildItemLore(newStack,server);
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
      return new Vec3i(-5,0,-5);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Starpath Altar").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThe leylines flow like rivers through the world, yet they are almost indistinct and would be impossible to navigate. However, the stars above pull on them like the moon on the tide. By charting the stars, and using ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Starpath Altar").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nWaystones to mark them, I should be able to send teleportation energy through the leylines along a charted course to exactly where I want to go in the world. It should even be capable of taking a group of creatures").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Starpath Altar").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nall at once to the same destination!\n\nUnfortunately, an Eye of Ender only contains so much teleportation energy, so the farther I wish to travel, the more Eyes I need to provide to have ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Starpath Altar").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nenough energy to create a continuous pathway along the leylines.\n\nI can Sneak Use a Waystone to encode a location into the Altar, or enter one manually.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class StarpathAltarItem extends ArcanaPolymerBlockItem {
      public StarpathAltarItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class StarpathAltarBlock extends ArcanaPolymerBlockEntity {
      public static final BooleanProperty BLOOM = BlockStateProperties.BLOOM;
      public static final BooleanProperty ACTIVATABLE = BooleanProperty.create("activatable");
      public StarpathAltarBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.SCULK_CATALYST.defaultBlockState().setValue(BLOOM,state.getValue(BLOOM));
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new StarpathAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(BLOOM,false).setValue(ACTIVATABLE,false);
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(BLOOM,ACTIVATABLE);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.STARPATH_ALTAR_BLOCK_ENTITY, StarpathAltarBlockEntity::ticker);
      }
      
      @Override
      protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         StarpathAltarBlockEntity altar = (StarpathAltarBlockEntity) world.getBlockEntity(pos);
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
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof StarpathAltarBlockEntity altar){
            initializeArcanaBlock(stack,altar);
            altar.readTargets(getListProperty(stack,TARGETS_TAG));
         }
      }
      
      private void tryActivate(BlockState state, Level world, BlockPos pos){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof StarpathAltarBlockEntity altar && world instanceof ServerLevel serverWorld){
            boolean stargate = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.STARGATE) > 0;
            Optional<ItemEntity> waystone = serverWorld.getEntitiesOfClass(ItemEntity.class,new AABB(pos.above()), e ->
                  e.getItem().is(ArcanaRegistry.WAYSTONE.getItem())
                        && Waystone.getTarget(e.getItem()) != null
                        && (stargate || Waystone.getTarget(e.getItem()).world().identifier().equals(world.dimension().identifier()))).stream().findAny();
            if(waystone.isPresent()){
               Waystone.WaystoneTarget target = Waystone.getTarget(waystone.get().getItem());
               altar.setTarget(new StarpathAltarBlockEntity.TargetEntry(
                     MinecraftUtils.getFormattedDimName(target.world()).getString()+" "+ BlockPos.containing(target.position()).toShortString(),
                     target.world().identifier().toString(),
                     (int) target.position().x(),
                     (int) target.position().y(),
                     (int) target.position().z()
               ));
            }
            
            boolean paid = MinecraftUtils.removeItemEntities(serverWorld,new AABB(pos.above()),(itemStack) -> itemStack.is(COST),altar.calculateCost());
            if(paid) altar.startTeleport(null);
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

