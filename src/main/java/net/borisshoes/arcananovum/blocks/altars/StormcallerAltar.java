package net.borisshoes.arcananovum.blocks.altars;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
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
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class StormcallerAltar extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "stormcaller_altar";
   
   private Multiblock multiblock;
   
   public StormcallerAltar(){
      id = ID;
      name = "Altar of the Stormcaller";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS, ArcaneTomeGui.TomeFilter.ALTARS};
      itemVersion = 0;
      vanillaItem = Items.RAW_COPPER_BLOCK;
      block = new StormcallerAltarBlock(BlockBehaviour.Properties.of().noOcclusion().mapColor(MapColor.COLOR_ORANGE).strength(5.0f,1200.0f).requiresCorrectToolForDrops().sound(SoundType.METAL));
      item = new StormcallerAltarItem(this.block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE,ResearchTasks.OBTAIN_HEART_OF_THE_SEA,ResearchTasks.OBTAIN_LIGHTNING_ROD,ResearchTasks.ADVANCEMENT_WAX_ON,ResearchTasks.ADVANCEMENT_WAX_OFF,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN};
      attributions = new Tuple[]{new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.texture_by"), Component.literal("tcmEcho")), new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.model_by"), Component.literal("tcmEcho"))};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addAltarLore(lore);
      lore.add(Component.literal("Stormcaller Altar:").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("calls upon the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("clouds ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("shift ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("darken").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("can be used to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("change ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("weather ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("to any state.").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("requires a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Diamond Block").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("activate").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
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
      return new Vec3i(-5,0,-5);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Altar of the\n    Stormcaller").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nIn order to influence the world, I need to tap into the leylines. I have devised a structure capable of such, with this at its heart. It should be able to cause changes in the  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Altar of the\n    Stormcaller").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\natmosphere so that I can dictate the weather. Thunder, Rain, or Shine at my command! However, in order to provide enough energy to the leylines, I need to use a Diamond Block as a catalyst.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class StormcallerAltarItem extends ArcanaPolymerBlockItem {
      public StormcallerAltarItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class StormcallerAltarBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock {
      public static final BooleanProperty ACTIVATABLE = BooleanProperty.create("activatable");
      
      public StormcallerAltarBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.RAW_COPPER_BLOCK.defaultBlockState();
         }
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(ACTIVATABLE,false);
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(ACTIVATABLE);
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new StormcallerAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.STORMCALLER_ALTAR_BLOCK_ENTITY, StormcallerAltarBlockEntity::ticker);
      }
      
      @Override
      protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         StormcallerAltarBlockEntity altar = (StormcallerAltarBlockEntity) world.getBlockEntity(pos);
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
         if(entity instanceof StormcallerAltarBlockEntity altar && world instanceof ServerLevel serverWorld){
            Tuple<Item,Integer> cost = StormcallerAltarBlockEntity.getCost();
            boolean paid = MinecraftUtils.removeItemEntities(serverWorld,new AABB(pos.above()),(itemStack) -> itemStack.is(cost.getA()),cost.getB());
            if(paid) altar.startWeatherChange(null);
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
      
      @Override
      public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
         return new Model(world, initialBlockState);
      }
   }
   
   public static final class Model extends BlockModel {
      public static final ItemStack STORMCALLER_ALTAR = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/stormcaller_altar"));
      
      private final ServerLevel world;
      private final ItemDisplayElement main;
      
      public Model(ServerLevel world, BlockState state){
         this.world = world;
         this.main = ItemDisplayElementUtil.createSimple(STORMCALLER_ALTAR);
         this.main.setScale(new Vector3f(1f));
         this.addElement(this.main);
      }
   }
}

