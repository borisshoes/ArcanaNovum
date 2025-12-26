package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.transmutation.*;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TransmutationAltar extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "transmutation_altar";
   
   private Multiblock multiblock;
   
   public TransmutationAltar(){
      id = ID;
      name = "Transmutation Altar";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity),TomeGui.TomeFilter.BLOCKS,TomeGui.TomeFilter.ALTARS};
      vanillaItem = Items.DIAMOND_BLOCK;
      block = new TransmutationAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).strength(5.0f,1200.0f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST));
      item = new TransmutationAltarItem(this.block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.ADVANCEMENT_TRADE,ResearchTasks.OBTAIN_AMETHYST_SHARD,ResearchTasks.OBTAIN_DIAMOND,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addAltarLore(lore);
      lore.add(Component.literal("Transmutation Altar:").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Altar").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" beckons to an ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("ancient entity").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" of ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("balance").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" and ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("trades").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Altar").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" can be called upon to").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" exchange ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("equivalent materials").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Every ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("barter").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" comes with its own ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("price.").withStyle(ChatFormatting.BLUE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public static List<TransmutationRecipe> getUnlockedRecipes(ServerPlayer player){
      return TransmutationRecipes.TRANSMUTATION_RECIPES.stream().filter(recipe -> {
         if(recipe instanceof InfusionTransmutationRecipe r && ArcanaItemUtils.isArcane(r.getOutput()) && !ArcanaNovum.data(player).hasResearched(ArcanaItemUtils.identifyItem(r.getOutput()))){
            return false;
         }
         if(recipe instanceof AequalisCatalystTransmutationRecipe && !(ArcanaNovum.data(player).hasResearched(ArcanaRegistry.AEQUALIS_SCIENTIA) && ArcanaNovum.data(player).getAugmentLevel(ArcanaAugments.EQUIVALENT_EXCHANGE.id) > 0)){
            return false;
         }
         if(recipe instanceof AequalisSkillTransmutationRecipe && !ArcanaNovum.data(player).hasResearched(ArcanaRegistry.AEQUALIS_SCIENTIA)){
            return false;
         }
         if(recipe instanceof AequalisUnattuneTransmutationRecipe && !(ArcanaNovum.data(player).hasResearched(ArcanaRegistry.AEQUALIS_SCIENTIA) && ArcanaNovum.data(player).getAugmentLevel(ArcanaAugments.IMPERMANENT_PERMUTATION.id) > 0)){
            return false;
         }
         return true;
      }).collect(Collectors.toCollection(ArrayList::new));
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
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.BLACKSTONE,24);
      ArcanaIngredient c = new ArcanaIngredient(Items.AMETHYST_BLOCK,16);
      ArcanaIngredient d = new ArcanaIngredient(Items.QUARTZ_BLOCK,24);
      ArcanaIngredient g = new ArcanaIngredient(Items.AMETHYST_SHARD,24);
      ArcanaIngredient h = new ArcanaIngredient(Items.DIAMOND,16);
      ArcanaIngredient m = new ArcanaIngredient(Items.DIAMOND_BLOCK,4);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,d,d},
            {a,g,h,g,d},
            {c,h,m,h,c},
            {d,g,h,g,a},
            {d,d,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Transmutation\n        Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThrough my research into leylines, I have discovered the essence of a living entity entwined within the leylines of the Overworld. From what I can tell, the entity is old, very old, and most ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Transmutation\n        Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nlikely of divine nature.\nIt is both singular and plural, like raindrops that become an ocean.\nI believe I can construct an altar that taps into the energy of this entity that rides the leylines. I wonder what capabilities this ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Transmutation\n        Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nstructure could yield.\n\nThe Altar I have created seems to mutate items when I drop them in specific configurations. The mutations are mostly consistent and follow some rule of conservation, but ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Transmutation\n        Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nconsumes reagents in the process.\nI will catalog all of the mutations I can find and document them in my Tome.\n\nThe points of interest are that there are 5 placement locations for items: \n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Transmutation\n        Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nA positive input, marked by quartz.\n\nA negative input marked by blackstone.\n\nTwo reagent inputs, marked by amethyst.\n\nAnd the Altar keystone itself seems  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Transmutation\n        Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nlike it can channel energy, however I  haven’t found an Item that works yet.\nWhen activated, the Altar will charge up and perform the transmutation.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Transmutation\n        Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nA gentle voice can be heard with each activation. The entity reminds me of the fae creatures that Illagers keep.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class TransmutationAltarItem extends ArcanaPolymerBlockItem {
      public TransmutationAltarItem(Block block){
         super(getThis(),block,getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
   }
   
   public class TransmutationAltarBlock extends ArcanaPolymerBlockEntity {
      public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
      public static final BooleanProperty ACTIVATABLE = BooleanProperty.create("activatable");
      
      public TransmutationAltarBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.DIAMOND_BLOCK.defaultBlockState();
      }
      
      @Nullable
      public static TransmutationAltarBlockEntity getEntity(Level world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof TransmutationAltarBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof TransmutationAltarBlockEntity altar ? altar : null;
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(HORIZONTAL_FACING,ctx.getHorizontalDirection().getOpposite()).setValue(ACTIVATABLE,false);
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(HORIZONTAL_FACING, ACTIVATABLE);
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
         return new TransmutationAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.TRANSMUTATION_ALTAR_BLOCK_ENTITY, TransmutationAltarBlockEntity::ticker);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         TransmutationAltarBlockEntity altar = (TransmutationAltarBlockEntity) world.getBlockEntity(pos);
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
         if(placer instanceof ServerPlayer player && entity instanceof TransmutationAltarBlockEntity altar){
            initializeArcanaBlock(stack,altar);
         }
      }
      
      private void tryActivate(BlockState state, Level world, BlockPos pos){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof TransmutationAltarBlockEntity altar){
            boolean started = altar.startTransmute(null);
            if(!started){
               SoundUtils.playSound(world, pos, SoundEvents.ALLAY_HURT, SoundSource.BLOCKS,0.5f,0.7f);
            }
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

