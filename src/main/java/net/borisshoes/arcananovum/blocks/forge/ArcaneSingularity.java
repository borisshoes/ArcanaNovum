package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcaneSingularity extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "arcane_singularity";
   
   public static final String BOOKS_TAG = "books";
   public static final String SINGULARITY_TAG = "singularityId";
   
   private Multiblock multiblock;
   
   public ArcaneSingularity(){
      id = ID;
      name = "Arcane Singularity";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS, ArcaneTomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.LECTERN;
      block = new ArcaneSingularityBlock(BlockBehaviour.Properties.of().strength(2.5f,1200.0f).sound(SoundType.WOOD));
      item = new ArcaneSingularityItem(this.block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.OBTAIN_STARDUST,ResearchTasks.OBTAIN_NEBULOUS_ESSENCE,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.OBTAIN_NETHER_STAR,ResearchTasks.UNLOCK_STARLIGHT_FORGE,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,BOOKS_TAG,new ListTag());
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
            .append(Component.literal("Storing ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("enchantments").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" on books is ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("crude").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" and ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("primitive.").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Condensing ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("their ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("essence ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("into a ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("singularity ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("is much more ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("modern").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Allows for the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("storing of ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("enchantments").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" in a ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("virtual library.").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Works well").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" with the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Midnight Enchanter").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      
      if(itemStack != null){
         int size = getListProperty(itemStack,BOOKS_TAG).size();
         if(size > 0){
            lore.add(Component.literal(""));
            lore.add(Component.literal("")
                  .append(Component.literal("Contains ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(""+size).withStyle(ChatFormatting.GREEN))
                  .append(Component.literal(" Enchanted Books").withStyle(ChatFormatting.LIGHT_PURPLE)));
         }
      }
      
      addForgeLore(lore);
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ListTag targetsList = getListProperty(stack,BOOKS_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,BOOKS_TAG,targetsList);
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
      return new Vec3i(-1,-1,-2);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Arcane Singularity").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThe Midnight Enchanter has proven more useful than I imagined. Now I have more Enchanted Books than I can possibly store in a library. I need a new method of book-keeping.\nBy condensing raw ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Arcane Singularity").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nNebulous Essence down over and over, it forms a self-sustaining singularity. An Arcane black hole. Containing it was no easy feat, but now I have a massive storage space for my books…\n\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Arcane Singularity").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nThe Singularity is a single purpose, high density storage unit for Enchanted Books. Books can be sorted and filtered, and can be extracted at any time. \nBooks are bound to the Singularity, so they are kept with the block when moved.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ArcaneSingularityItem extends ArcanaPolymerBlockItem {
      
      public ArcaneSingularityItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class ArcaneSingularityBlock extends ArcanaPolymerBlockEntity {
      public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
      
      public ArcaneSingularityBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.LECTERN.defaultBlockState().setValue(HORIZONTAL_FACING,state.getValue(HORIZONTAL_FACING));
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(HORIZONTAL_FACING,ctx.getHorizontalDirection().getOpposite());
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
         return createTickerHelper(type, ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY, ArcaneSingularityBlockEntity::ticker);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         ArcaneSingularityBlockEntity singularity = (ArcaneSingularityBlockEntity) world.getBlockEntity(pos);
         if(singularity != null){
            if(playerEntity instanceof ServerPlayer player){
               if(singularity.isAssembled()){
                  if(StarlightForge.findActiveForge(player.level(),pos) == null){
                     player.sendSystemMessage(Component.literal("The Enchanter must be within the range of an active Starlight Forge"));
                  }else{
                     singularity.openGui(player);
                  }
               }else{
                  player.sendSystemMessage(Component.literal("Multiblock not constructed."));
                  multiblock.displayStructure(singularity.getMultiblockCheck(),player);
               }
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Nullable
      public static ArcaneSingularityBlockEntity getEntity(Level world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof ArcaneSingularityBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof ArcaneSingularityBlockEntity singularity ? singularity : null;
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new ArcaneSingularityBlockEntity(pos, state);
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof ArcaneSingularityBlockEntity singularity){
            initializeArcanaBlock(stack,singularity);
            singularity.initializeBooks(getListProperty(stack,BOOKS_TAG),world.registryAccess());
         }
      }
   }
}

