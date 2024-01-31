package net.borisshoes.arcananovum.blocks;

import com.google.common.collect.Lists;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.MagicBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.minecraft.block.Block.dropStacks;

public class FractalSponge extends MagicBlock {
   
   public FractalSponge(){
      id = "fractal_sponge";
      name = "Fractal Sponge";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.BLOCKS};
      vanillaItem = Items.SPONGE;
      block = new FractalSpongeBlock(FabricBlockSettings.create().strength(.6f,1200.0f).sounds(BlockSoundGroup.GRASS));
      item = new FractalSpongeItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
   
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Fractal Sponge\",\"italic\":false,\"bold\":true,\"color\":\"yellow\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
   
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"Fractals \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"are known for having \",\"color\":\"blue\"},{\"text\":\"infinite \",\"color\":\"light_purple\"},{\"text\":\"surface area\"},{\"text\":\".\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"effectiveness\",\"color\":\"aqua\"},{\"text\":\" of a \"},{\"text\":\"sponge \",\"color\":\"yellow\"},{\"text\":\"is based on said \"},{\"text\":\"surface area\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"combination \",\"color\":\"aqua\"},{\"text\":\"of the two seems only \"},{\"text\":\"natural\",\"color\":\"dark_aqua\",\"italic\":true},{\"text\":\".\",\"color\":\"blue\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"The resulting \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"sponge \",\"color\":\"yellow\"},{\"text\":\"is \"},{\"text\":\"much more effective\",\"color\":\"aqua\"},{\"text\":\" than most \"},{\"text\":\"sponges\",\"color\":\"yellow\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It even works on \",\"italic\":true,\"color\":\"dark_aqua\"},{\"text\":\"lava\",\"color\":\"gold\"},{\"text\":\"!\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   private int absorb(ItemStack item, World world, BlockPos pos) {
      int depthLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.MANDELBROT.id));
      int absorbLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.SIERPINSKI.id));
      int maxDepth = 16 + depthLevel*2;
      int maxBlocks = 512 + 256*absorbLevel;
      
      Queue<Pair<BlockPos, Integer>> queue = Lists.newLinkedList();
      queue.add(new Pair(pos, 0));
      int blocksAbsorbed = 0;
      
      while(!queue.isEmpty()) {
         Pair<BlockPos, Integer> pair = (Pair)queue.poll();
         BlockPos blockPos = (BlockPos)pair.getLeft();
         int depth = (Integer)pair.getRight();
         Direction[] dirs = Direction.values();
         int numDirs = dirs.length;
         
         for(int side = 0; side < numDirs; ++side) {
            Direction direction = dirs[side];
            BlockPos blockPos2 = blockPos.offset(direction);
            BlockState blockState = world.getBlockState(blockPos2);
            FluidState fluidState = world.getFluidState(blockPos2);
            if (fluidState.isIn(FluidTags.WATER)) {
               if (blockState.getBlock() instanceof FluidDrainable && !((FluidDrainable)blockState.getBlock()).tryDrainFluid(null,world, blockPos2, blockState).isEmpty()) {
                  ++blocksAbsorbed;
                  if (depth < maxDepth) {
                     queue.add(new Pair(blockPos2, depth + 1));
                  }
               } else if (blockState.getBlock() instanceof FluidBlock) {
                  world.setBlockState(blockPos2, Blocks.AIR.getDefaultState(), 3);
                  ++blocksAbsorbed;
                  if (depth < maxDepth) {
                     queue.add(new Pair(blockPos2, depth + 1));
                  }
               } else if (blockState.isOf(Blocks.KELP) || blockState.isOf(Blocks.KELP_PLANT) || blockState.isOf(Blocks.SEAGRASS) || blockState.isOf(Blocks.TALL_SEAGRASS)) {
                  BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(blockPos2) : null;
                  dropStacks(blockState, world, blockPos2, blockEntity);
                  world.setBlockState(blockPos2, Blocks.AIR.getDefaultState(), 3);
                  ++blocksAbsorbed;
                  if (depth < maxDepth) {
                     queue.add(new Pair(blockPos2, depth + 1));
                  }
               }
            }else if(fluidState.isIn(FluidTags.LAVA)){
               if (blockState.getBlock() instanceof FluidDrainable && !((FluidDrainable)blockState.getBlock()).tryDrainFluid(null,world, blockPos2, blockState).isEmpty()) {
                  ++blocksAbsorbed;
                  if (depth < maxDepth) {
                     queue.add(new Pair(blockPos2, depth + 1));
                  }
               } else if (blockState.getBlock() instanceof FluidBlock) {
                  world.setBlockState(blockPos2, Blocks.AIR.getDefaultState(), 3);
                  ++blocksAbsorbed;
                  if (depth < maxDepth) {
                     queue.add(new Pair(blockPos2, depth + 1));
                  }
               }
            }
         }
         
         if (blocksAbsorbed > maxBlocks) {
            break;
         }
      }
      
      return blocksAbsorbed;
   }
   
   private int absorbHelper(ServerPlayerEntity player, World world, ItemStack item, BlockPos pos, boolean doCheck){
      if(doCheck && !(world.getBlockState(pos).isOf(getBlock()))) return 0;
      int absorbed = absorb(item, world, pos);
      if(absorbed > 0){
         SoundUtils.playSound(player.getServerWorld(),pos,SoundEvents.ENTITY_ELDER_GUARDIAN_HURT, SoundCategory.BLOCKS,1,.8f);
         PLAYER_DATA.get(player).addXP(absorbed); // Add xp
         ArcanaAchievements.progress(player,ArcanaAchievements.OCEAN_CLEANUP.id, absorbed);
      }
      return absorbed;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient m = new MagicItemIngredient(Items.MAGMA_BLOCK,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.END_CRYSTAL,16,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.SPONGE,48,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.BLUE_ICE,64,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.NETHERITE_INGOT,1,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHER_STAR,8,null);
      
      MagicItemIngredient[][] ingredients = {
            {m,c,s,c,b},
            {c,i,s,i,c},
            {s,s,n,s,s},
            {c,i,s,i,c},
            {b,c,s,c,m}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Fractal Sponge\\n\\nRarity: Empowered\\n\\nEver heard of the coastline paradox?\\nI thought about it while staring at an ocean monument from the shore, and now I'm off to shove as many sponges into a Netherite reinforced fractal as I can.\"}");
      list.add("{\"text\":\"    Fractal Sponge\\n\\nThe Fractal Sponge in practice is only 8 times better than a regular sponge due to it taking time for fluid to soak into the fractal, but it never gets fully soaked and the Netherite frame lets it contain hotter fluids like lava.\"}");
      return list;
   }
   
   public class FractalSpongeItem extends MagicPolymerBlockItem {
      public FractalSpongeItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class FractalSpongeBlock extends MagicPolymerBlockEntity {
      public FractalSpongeBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.SPONGE;
      }
      
      @Nullable
      public static FractalSpongeBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof FractalSpongeBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof FractalSpongeBlockEntity sponge ? sponge : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new FractalSpongeBlockEntity(pos, state);
      }
      
      @Override
      public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
         if (state.isOf(newState.getBlock())) {
            return;
         }
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if(!(blockEntity instanceof MagicBlockEntity mbe)) return;
         DefaultedList<ItemStack> drops = DefaultedList.of();
         drops.add(getDroppedBlockItem(state,world,null,blockEntity));
         ItemScatterer.spawn(world, pos, drops);
         super.onStateReplaced(state, world, pos, newState, moved);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof FractalSpongeBlockEntity sponge) {
            initializeMagicBlock(stack,sponge);
            
            try{
               int absorbed = absorbHelper(player,world,stack,pos,false);
               
               boolean cantor = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CANTOR.id)) >= 1;
               if(cantor && absorbed > 0){
                  ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(50, () -> absorbHelper(player,world,stack,pos,true)));
                  ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(100, () -> absorbHelper(player,world,stack,pos,true)));
                  ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(150, () -> absorbHelper(player,world,stack,pos,true)));
               }
            }catch(Exception e){
               e.printStackTrace();
            }
         }
      }
   }
}
