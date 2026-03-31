package net.borisshoes.arcananovum.blocks;

import com.google.common.collect.Lists;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.core.polymer.PackAwareBlockModel;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.minecraft.world.level.block.Block.dropResources;

public class FractalSponge extends ArcanaBlock {
   public static final String ID = "fractal_sponge";
   
   public FractalSponge(){
      id = ID;
      name = "Fractal Sponge";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      vanillaItem = Items.SPONGE;
      block = new FractalSpongeBlock(BlockBehaviour.Properties.of().noOcclusion().strength(.6f, 1200.0f).sound(SoundType.GRASS));
      item = new FractalSpongeItem(this.block);
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_SPONGE, ResearchTasks.OBTAIN_END_CRYSTAL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Fractals ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("are known for having ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("infinite ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("surface area").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("effectiveness").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" of a ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("sponge ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("is based on said ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("surface area").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("combination ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("of the two seems only ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("natural").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("The resulting ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("sponge ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("is ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("much more effective").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" than most ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("sponges").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("It even works on ").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA))
            .append(Component.literal("lava").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("!").withStyle(ChatFormatting.DARK_AQUA)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   private int absorb(ItemStack item, Level world, BlockPos pos){
      int depthLevel = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.MANDELBROT);
      int absorbLevel = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.SIERPINSKI);
      int baseDepth = ArcanaNovum.CONFIG.getInt(ArcanaConfig.FRACTAL_SPONGE_RANGE);
      int extraDepth = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.FRACTAL_SPONGE_RANGE_PER_LVL).get(depthLevel);
      int baseBlocks = ArcanaNovum.CONFIG.getInt(ArcanaConfig.FRACTAL_SPONGE_BLOCKS);
      int extraBlocks = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.FRACTAL_SPONGE_BLOCKS_PER_LVL).get(absorbLevel);
      
      int maxDepth = baseDepth + extraDepth;
      int maxBlocks = baseBlocks + extraBlocks;
      
      Queue<Tuple<BlockPos, Integer>> queue = Lists.newLinkedList();
      queue.add(new Tuple<>(pos, 0));
      int blocksAbsorbed = 0;
      
      while(!queue.isEmpty()){
         Tuple<BlockPos, Integer> pair = queue.poll();
         BlockPos blockPos = pair.getA();
         int depth = pair.getB();
         Direction[] dirs = Direction.values();
         int numDirs = dirs.length;
         
         for(int side = 0; side < numDirs; ++side){
            Direction direction = dirs[side];
            BlockPos blockPos2 = blockPos.relative(direction);
            BlockState blockState = world.getBlockState(blockPos2);
            FluidState fluidState = world.getFluidState(blockPos2);
            if(fluidState.is(FluidTags.WATER)){
               if(blockState.getBlock() instanceof BucketPickup && !((BucketPickup) blockState.getBlock()).pickupBlock(null, world, blockPos2, blockState).isEmpty()){
                  ++blocksAbsorbed;
                  if(depth < maxDepth){
                     queue.add(new Tuple<>(blockPos2, depth + 1));
                  }
               }else if(blockState.getBlock() instanceof LiquidBlock){
                  world.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);
                  ++blocksAbsorbed;
                  if(depth < maxDepth){
                     queue.add(new Tuple<>(blockPos2, depth + 1));
                  }
               }else if(blockState.is(Blocks.KELP) || blockState.is(Blocks.KELP_PLANT) || blockState.is(Blocks.SEAGRASS) || blockState.is(Blocks.TALL_SEAGRASS)){
                  BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(blockPos2) : null;
                  dropResources(blockState, world, blockPos2, blockEntity);
                  world.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);
                  ++blocksAbsorbed;
                  if(depth < maxDepth){
                     queue.add(new Tuple<>(blockPos2, depth + 1));
                  }
               }
            }else if(fluidState.is(FluidTags.LAVA)){
               if(blockState.getBlock() instanceof BucketPickup && !((BucketPickup) blockState.getBlock()).pickupBlock(null, world, blockPos2, blockState).isEmpty()){
                  ++blocksAbsorbed;
                  if(depth < maxDepth){
                     queue.add(new Tuple<>(blockPos2, depth + 1));
                  }
               }else if(blockState.getBlock() instanceof LiquidBlock){
                  world.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);
                  ++blocksAbsorbed;
                  if(depth < maxDepth){
                     queue.add(new Tuple<>(blockPos2, depth + 1));
                  }
               }
            }
         }
         
         if(blocksAbsorbed > maxBlocks){
            break;
         }
      }
      
      return blocksAbsorbed;
   }
   
   private int absorbHelper(@Nullable LivingEntity placer, Level world, ItemStack item, BlockPos pos, boolean doCheck){
      if(doCheck && !(world.getBlockState(pos).is(getBlock()))) return 0;
      int absorbed = absorb(item, world, pos);
      if(absorbed > 0){
         SoundUtils.playSound(world, pos, SoundEvents.ELDER_GUARDIAN_HURT, SoundSource.BLOCKS, 1, .8f);
         if(placer instanceof ServerPlayer player){
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FRACTAL_SPONGE_ABSORB_BLOCK) * absorbed); // Add xp
            ArcanaAchievements.progress(player, ArcanaAchievements.OCEAN_CLEANUP, absorbed);
         }
      }
      return absorbed;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Fractal Sponge").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nHave you ever heard of the coastline paradox? I thought about it while staring at an ocean monument offshore, and now I’m off to shove as many sponges into a fractal as I can.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Fractal Sponge").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nThe Fractal Sponge in practice is only 8 times better than a regular sponge due to it taking time for fluid to soak into the fractal. But, it never gets fully soaked and the reinforced frame lets it contain hotter fluids like lava.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class FractalSpongeItem extends ArcanaPolymerBlockItem {
      public FractalSpongeItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class FractalSpongeBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock {
      public FractalSpongeBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.SPONGE.defaultBlockState();
         }
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.FRACTAL_SPONGE_BLOCK_ENTITY, FractalSpongeBlockEntity::ticker);
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new FractalSpongeBlockEntity(pos, state);
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(world instanceof ServerLevel serverWorld && entity instanceof FractalSpongeBlockEntity sponge){
            initializeArcanaBlock(stack, sponge);
            
            try{
               int absorbed = absorbHelper(placer, world, stack, pos, false);
               boolean cantor = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.CANTOR) >= 1;
               if(cantor && absorbed > 0){
                  int times = ArcanaNovum.CONFIG.getInt(ArcanaConfig.FRACTAL_SPONGE_PULSES);
                  int duration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.FRACTAL_SPONGE_PULSE_DURATION);
                  for(int i = 1; i <= times; i++){
                     BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(duration * i, () -> absorbHelper(placer, world, stack, pos, true)));
                  }
               }
            }catch(Exception e){
               e.printStackTrace();
            }
         }
      }
      
      @Override
      public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState){
         return new Model(world, initialBlockState);
      }
   }
   
   public static final class Model extends PackAwareBlockModel {
      public static final ItemStack SPONGE = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/fractal_sponge"));
      public static final ItemStack SPONGE_WET = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/fractal_sponge_wet"));
      
      private final ServerLevel world;
      private final ItemDisplayElement main;
      
      public Model(ServerLevel world, BlockState state){
         this.world = world;
         
         this.main = ItemDisplayElementUtil.createSimple(SPONGE);
         this.main.setScale(new Vector3f(1f));
         this.addElement(this.main);
      }
   }
}

