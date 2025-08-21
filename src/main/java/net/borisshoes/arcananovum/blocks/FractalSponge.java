package net.borisshoes.arcananovum.blocks;

import com.google.common.collect.Lists;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.minecraft.block.Block.dropStacks;

public class FractalSponge extends ArcanaBlock {
   public static final String ID = "fractal_sponge";
   
   public FractalSponge(){
      id = ID;
      name = "Fractal Sponge";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.BLOCKS};
      vanillaItem = Items.SPONGE;
      block = new FractalSpongeBlock(AbstractBlock.Settings.create().strength(.6f,1200.0f).sounds(BlockSoundGroup.GRASS));
      item = new FractalSpongeItem(this.block);
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.YELLOW);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_SPONGE,ResearchTasks.OBTAIN_END_CRYSTAL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Fractals ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("are known for having ").formatted(Formatting.BLUE))
            .append(Text.literal("infinite ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("surface area").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.BLUE))
            .append(Text.literal("effectiveness").formatted(Formatting.AQUA))
            .append(Text.literal(" of a ").formatted(Formatting.BLUE))
            .append(Text.literal("sponge ").formatted(Formatting.YELLOW))
            .append(Text.literal("is based on said ").formatted(Formatting.BLUE))
            .append(Text.literal("surface area").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.BLUE))
            .append(Text.literal("combination ").formatted(Formatting.AQUA))
            .append(Text.literal("of the two seems only ").formatted(Formatting.BLUE))
            .append(Text.literal("natural").formatted(Formatting.ITALIC,Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("The resulting ").formatted(Formatting.BLUE))
            .append(Text.literal("sponge ").formatted(Formatting.YELLOW))
            .append(Text.literal("is ").formatted(Formatting.BLUE))
            .append(Text.literal("much more effective").formatted(Formatting.AQUA))
            .append(Text.literal(" than most ").formatted(Formatting.BLUE))
            .append(Text.literal("sponges").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("It even works on ").formatted(Formatting.ITALIC,Formatting.DARK_AQUA))
            .append(Text.literal("lava").formatted(Formatting.GOLD))
            .append(Text.literal("!").formatted(Formatting.DARK_AQUA)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   private int absorb(ItemStack item, World world, BlockPos pos){
      int depthLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.MANDELBROT.id));
      int absorbLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.SIERPINSKI.id));
      int maxDepth = 16 + depthLevel*2;
      int maxBlocks = 512 + 256*absorbLevel;
      
      Queue<Pair<BlockPos, Integer>> queue = Lists.newLinkedList();
      queue.add(new Pair(pos, 0));
      int blocksAbsorbed = 0;
      
      while(!queue.isEmpty()){
         Pair<BlockPos, Integer> pair = (Pair)queue.poll();
         BlockPos blockPos = (BlockPos)pair.getLeft();
         int depth = (Integer)pair.getRight();
         Direction[] dirs = Direction.values();
         int numDirs = dirs.length;
         
         for(int side = 0; side < numDirs; ++side){
            Direction direction = dirs[side];
            BlockPos blockPos2 = blockPos.offset(direction);
            BlockState blockState = world.getBlockState(blockPos2);
            FluidState fluidState = world.getFluidState(blockPos2);
            if(fluidState.isIn(FluidTags.WATER)){
               if(blockState.getBlock() instanceof FluidDrainable && !((FluidDrainable)blockState.getBlock()).tryDrainFluid(null,world, blockPos2, blockState).isEmpty()){
                  ++blocksAbsorbed;
                  if(depth < maxDepth){
                     queue.add(new Pair<>(blockPos2, depth + 1));
                  }
               } else if(blockState.getBlock() instanceof FluidBlock){
                  world.setBlockState(blockPos2, Blocks.AIR.getDefaultState(), 3);
                  ++blocksAbsorbed;
                  if(depth < maxDepth){
                     queue.add(new Pair<>(blockPos2, depth + 1));
                  }
               } else if(blockState.isOf(Blocks.KELP) || blockState.isOf(Blocks.KELP_PLANT) || blockState.isOf(Blocks.SEAGRASS) || blockState.isOf(Blocks.TALL_SEAGRASS)){
                  BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(blockPos2) : null;
                  dropStacks(blockState, world, blockPos2, blockEntity);
                  world.setBlockState(blockPos2, Blocks.AIR.getDefaultState(), 3);
                  ++blocksAbsorbed;
                  if(depth < maxDepth){
                     queue.add(new Pair<>(blockPos2, depth + 1));
                  }
               }
            }else if(fluidState.isIn(FluidTags.LAVA)){
               if(blockState.getBlock() instanceof FluidDrainable && !((FluidDrainable)blockState.getBlock()).tryDrainFluid(null,world, blockPos2, blockState).isEmpty()){
                  ++blocksAbsorbed;
                  if(depth < maxDepth){
                     queue.add(new Pair<>(blockPos2, depth + 1));
                  }
               } else if(blockState.getBlock() instanceof FluidBlock){
                  world.setBlockState(blockPos2, Blocks.AIR.getDefaultState(), 3);
                  ++blocksAbsorbed;
                  if(depth < maxDepth){
                     queue.add(new Pair<>(blockPos2, depth + 1));
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
   
   private int absorbHelper(@Nullable LivingEntity placer, World world, ItemStack item, BlockPos pos, boolean doCheck){
      if(doCheck && !(world.getBlockState(pos).isOf(getBlock()))) return 0;
      int absorbed = absorb(item, world, pos);
      if(absorbed > 0){
         SoundUtils.playSound(world,pos,SoundEvents.ENTITY_ELDER_GUARDIAN_HURT, SoundCategory.BLOCKS,1,.8f);
         if(placer instanceof ServerPlayerEntity player){
            ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.FRACTAL_SPONGE_ABSORB_BLOCK) * absorbed); // Add xp
            ArcanaAchievements.progress(player, ArcanaAchievements.OCEAN_CLEANUP.id, absorbed);
         }
      }
      return absorbed;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.OBSIDIAN,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.MAGMA_BLOCK,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.SPONGE,6);
      ArcanaIngredient d = new ArcanaIngredient(Items.BLUE_ICE,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.END_CRYSTAL,4);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHER_STAR,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,d,a},
            {b,g,c,g,d},
            {c,c,m,c,c},
            {d,g,c,g,b},
            {a,d,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("  Fractal Sponge").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nHave you ever heard of the coastline paradox? I thought about it while staring at an ocean monument offshore, and now I’m off to shove as many sponges into a fractal as I can.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Fractal Sponge").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nThe Fractal Sponge in practice is only 8 times better than a regular sponge due to it taking time for fluid to soak into the fractal. But, it never gets fully soaked and the reinforced frame lets it contain hotter fluids like lava.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class FractalSpongeItem extends ArcanaPolymerBlockItem {
      public FractalSpongeItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class FractalSpongeBlock extends ArcanaPolymerBlockEntity {
      public FractalSpongeBlock(AbstractBlock.Settings settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.SPONGE.getDefaultState();
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
         return validateTicker(type, ArcanaRegistry.FRACTAL_SPONGE_BLOCK_ENTITY, FractalSpongeBlockEntity::ticker);
      }
      
      @Nullable
      public static FractalSpongeBlockEntity getEntity(World world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof FractalSpongeBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof FractalSpongeBlockEntity sponge ? sponge : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
         return new FractalSpongeBlockEntity(pos, state);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(world instanceof ServerWorld serverWorld && entity instanceof FractalSpongeBlockEntity sponge){
            initializeArcanaBlock(stack,sponge);
            
            try{
               int absorbed = absorbHelper(placer,world,stack,pos,false);
               
               boolean cantor = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CANTOR.id)) >= 1;
               if(cantor && absorbed > 0){
                  ArcanaNovum.addTickTimerCallback(serverWorld, new GenericTimer(50, () -> absorbHelper(placer,world,stack,pos,true)));
                  ArcanaNovum.addTickTimerCallback(serverWorld, new GenericTimer(100, () -> absorbHelper(placer,world,stack,pos,true)));
                  ArcanaNovum.addTickTimerCallback(serverWorld, new GenericTimer(150, () -> absorbHelper(placer,world,stack,pos,true)));
               }
            }catch(Exception e){
               e.printStackTrace();
            }
         }
      }
   }
}

