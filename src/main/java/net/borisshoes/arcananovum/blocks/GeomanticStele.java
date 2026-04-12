package net.borisshoes.arcananovum.blocks;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.util.LazyItemStack;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.core.polymer.PackAwareBlockModel;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class GeomanticStele extends ArcanaBlock implements MultiblockCore {
   public static final String ID = "geomantic_stele";
   
   private Multiblock multiblock;
   
   public GeomanticStele(){
      id = ID;
      name = "Geomantic Stele";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.REINFORCED_DEEPSLATE;
      block = new GeomanticSteleBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops().strength(6.0f, 1200.0f).sound(SoundType.LODESTONE));
      item = new GeomanticSteleItem(block);
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_NETHER_STAR, ResearchTasks.OBTAIN_NETHERITE_INGOT, ResearchTasks.UNLOCK_RUNIC_MATRIX, ResearchTasks.OBTAIN_AMETHYST_SHARD};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("keystone ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("activates a ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("multiblock construct").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("monolith ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("channels ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Arcana ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("into a single ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("item").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" become ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("activated ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("by the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("keystone").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("redstone signal").withStyle(ChatFormatting.RED))
            .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("activates ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("construct").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" a finished ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Stele ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("with an ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Arcana Item").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nMany of my items provide useful passive effects, and others interact with the world around me, however I am limited in having to wield them myself. While channeling Arcana to blocks to allow them").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nindependent activation has been a solved problem via Leyline transmission, I have only now solved the problem of directing the action of these passive effects autonomously.\nThe use of a Runic Matrix along with a geolithic structure").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nprovides the necessary adaptability and support to activate a select few of my items.\n\nUsing an acceptable item on the Stele keystone will place it within the structure.\nThe Stele must then be activated by a\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\ndirect redstone signal to the keystone.\n\nWhen using a Charm of Felidae in the Stele, creepers and phantoms become afraid of it, and the Charm's fall reduction gets applied in a cubic range of 15 blocks.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nWhen using a Charm of Magnetism in the Stele, the Charm's passive ability is activated and extended to a cubic range of 8 blocks. Items close to the Stele are unaffected.\n\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nWhen using a Brain in a Jar in the Stele, XP orbs become attracted to the Stele and absorbed by it. If the Jar's mending mode is activated, its effect will be applied in a cubic range of 16 blocks horizontally and 8 blocks vertically.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nWhen using the Charm of Cinders in the Stele, the Charm's fire resistance is applied in its range, and if its smelting ability is enabled, it will smelt items on the ground in a cubic range of 20 blocks.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nWhen using the Charm of Cleansing in the Stele, the Charm's passive ability is activated and extended to a cubic range of 15 blocks.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nWhen using the Charm of Cetacea in the Stele, the Charm's passive ability is activated and extended to a cubic range of 20 blocks.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nWhen using the Charm of Wild Growth in the Stele, the Charm's passive ability is activated and extended to a cubic range of 12 blocks horizontally, and 6 blocks vertically.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nWhen using a Magmatic or Aquatic Eversource in the Stele, it will continuously generate its fluid atop the Stele construct.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nWhen using an Overflowing or Runic Quiver in the Stele, it will continue to restock its arrows.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Geomantic Stele").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nWhen using the Charm of Leadership in the Stele, the Charm's passive ability is activated and extended to a cubic range of 12 blocks horizontally and 8 blocks vertically.").withStyle(ChatFormatting.BLACK)));
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
      return new Vec3i(-1, 0, -1);
   }
   
   public class GeomanticSteleItem extends ArcanaPolymerBlockItem {
      public GeomanticSteleItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
//         if(ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.) >= 1){
//            stringList.add("");
//         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(new ArrayList<>(), new ArrayList<>(), stringList, new ArrayList<>()));
         return baseStack;
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
   
   public class GeomanticSteleBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock {
      public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
      
      public GeomanticSteleBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context)){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
         }
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(ACTIVE);
      }
      
      @org.jspecify.annotations.Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext){
         return this.defaultBlockState().setValue(ACTIVE, false);
      }
      
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new GeomanticSteleBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.GEOMANTIC_STELE_BLOCK_ENTITY, GeomanticSteleBlockEntity::ticker);
      }
      
      @Override
      protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @org.jspecify.annotations.Nullable Orientation orientation, boolean bl){
         if(!level.isClientSide()){
            boolean currentlyActive = blockState.getValue(ACTIVE);
            if(currentlyActive != level.hasNeighborSignal(blockPos)){
               if(currentlyActive){
                  level.scheduleTick(blockPos, this, 4);
               }else if(level.getBlockEntity(blockPos) instanceof GeomanticSteleBlockEntity stele && stele.isAssembled() && !stele.getItem().isEmpty()){
                  level.setBlock(blockPos, blockState.setValue(ACTIVE, true), Block.UPDATE_ALL);
                  level.gameEvent(GameEvent.BLOCK_ACTIVATE, blockPos, GameEvent.Context.of(blockState));
               }
            }
         }
      }
      
      @Override
      protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource){
         if(blockState.getValue(ACTIVE) && !serverLevel.hasNeighborSignal(blockPos)){
            serverLevel.setBlock(blockPos, blockState.cycle(ACTIVE), 2);
            serverLevel.gameEvent(GameEvent.BLOCK_DEACTIVATE, blockPos, GameEvent.Context.of(blockState));
         }
      }
      
      @Override
      protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
         GeomanticSteleBlockEntity stele = (GeomanticSteleBlockEntity) world.getBlockEntity(pos);
         if(stele != null && player instanceof ServerPlayer serverPlayer && stele.interact(serverPlayer, stack))
            return InteractionResult.SUCCESS_SERVER;
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit){
         GeomanticSteleBlockEntity stele = (GeomanticSteleBlockEntity) world.getBlockEntity(pos);
         if(stele != null && player instanceof ServerPlayer serverPlayer && stele.interact(serverPlayer, ItemStack.EMPTY))
            return InteractionResult.SUCCESS_SERVER;
         return InteractionResult.PASS;
      }
      
      @Override
      public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState){
         return new Model(world, initialBlockState);
      }
   }
   
   public static final class Model extends PackAwareBlockModel {
      public static final LazyItemStack STELE = ItemDisplayElementUtil.getModel(ArcanaRegistry.arcanaId("block/geomantic_stele"));
      
      private final ServerLevel world;
      private final ItemDisplayElement main;
      
      public Model(ServerLevel world, BlockState state){
         this.world = world;
         this.main = ItemDisplayElementUtil.createSimple(STELE);
         this.main.setScale(new Vector3f(1f));
         this.addElement(this.main);
      }
   }
   
   public interface Interaction {
      void steleTick(ServerLevel world, GeomanticSteleBlockEntity stele, ItemStack stack, Vec3 range);
      
      Vec3 getBaseRange();
   }
}


