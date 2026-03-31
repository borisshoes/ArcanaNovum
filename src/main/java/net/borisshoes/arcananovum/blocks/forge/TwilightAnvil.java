package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.factorytools.api.block.FactoryBlock;
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
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.blocks.forge.StellarCore.StellarCoreBlock.HORIZONTAL_FACING;

public class TwilightAnvil extends ArcanaBlock implements MultiblockCore {
   public static final String ID = "twilight_anvil";
   
   private Multiblock multiblock;
   
   public TwilightAnvil(){
      id = ID;
      name = "Twilight Anvil";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS, ArcaneTomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.ANVIL;
      block = new TwilightAnvilBlock(BlockBehaviour.Properties.of().noOcclusion().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0f, 1200.0f).sound(SoundType.ANVIL));
      item = new TwilightAnvilItem(this.block);
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_NETHERITE_INGOT, ResearchTasks.OBTAIN_ANVIL, ResearchTasks.UNLOCK_STARLIGHT_FORGE, ResearchTasks.OBTAIN_BOTTLES_OF_ENCHANTING};
      attributions = new Tuple[]{new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.texture_by"), Component.literal("ii_iridescent")), new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.model_by"), Component.literal("ii_iridescent"))};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Forge Structure").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" addon to the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Starlight Forge").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("An improved ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("anvil ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("with ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("no XP limit").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Anvil ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("can be used to rename ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(", and apply ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("augments").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Anvil ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("can also be used to combine ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("enhanced equipment").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      addForgeLore(lore);
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
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
      return new Vec3i(-1, -1, -1);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Twilight Anvil").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nAnvils made of iron have their limits. They don’t interact with Arcana, and they are less durable than the diamond and netherite equipment used on them, causing frequent damage. An anvil reinforced with").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Twilight Anvil").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nNetherite and infused with Arcana will have no such weaknesses.\n\nThe Anvil can act as a normal anvil, with no XP limit. The Anvil also enables the ability to Augment and rename Arcana items. It also can combine items infused with Stardust.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class TwilightAnvilItem extends ArcanaPolymerBlockItem {
      public TwilightAnvilItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class TwilightAnvilBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock {
      public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
      
      public TwilightAnvilBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.ANVIL.defaultBlockState().setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING));
         }
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(HORIZONTAL_FACING, ctx.getHorizontalDirection().getClockWise());
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(HORIZONTAL_FACING);
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
         return createTickerHelper(type, ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY, TwilightAnvilBlockEntity::ticker);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         TwilightAnvilBlockEntity anvil = (TwilightAnvilBlockEntity) world.getBlockEntity(pos);
         if(anvil != null){
            if(playerEntity instanceof ServerPlayer player){
               if(anvil.isAssembled()){
                  if(StarlightForge.findActiveForge(player.level(), pos) == null){
                     player.sendSystemMessage(Component.literal("The Anvil must be within the range of an active Starlight Forge"));
                  }else{
                     anvil.openGui(0, player, "");
                  }
               }else{
                  if(player.isShiftKeyDown() && player.isCreative()){
                     multiblock.build(anvil.getMultiblockCheck());
                  }else{
                     player.sendSystemMessage(Component.literal("Multiblock not constructed."));
                     multiblock.displayStructure(anvil.getMultiblockCheck(), player);
                  }
               }
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new TwilightAnvilBlockEntity(pos, state);
      }
      
      @Override
      public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState){
         return new Model(world, initialBlockState);
      }
   }
   
   public static final class Model extends PackAwareBlockModel {
      public static final ItemStack ANVIL = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/twilight_anvil"));
      
      private final ServerLevel world;
      private final ItemDisplayElement main;
      
      public Model(ServerLevel world, BlockState state){
         this.world = world;
         float direction = state.getValue(HORIZONTAL_FACING).toYRot();
         
         this.main = ItemDisplayElementUtil.createSimple(ANVIL);
         this.main.setScale(new Vector3f(1f));
         this.main.setYaw(direction);
         this.addElement(this.main);
      }
   }
}

