package net.borisshoes.arcananovum.items;

import com.google.common.collect.Lists;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.core.BlockItem;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.minecraft.block.Block.dropStacks;

public class FractalSponge extends MagicItem implements UsableItem, BlockItem {
   
   public FractalSponge(){
      id = "fractal_sponge";
      name = "Fractal Sponge";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.BLOCKS};
   
      ItemStack item = new ItemStack(Items.SPONGE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Fractal Sponge\",\"italic\":false,\"bold\":true,\"color\":\"yellow\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Fractals \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"are known for having \",\"color\":\"blue\"},{\"text\":\"infinite \",\"color\":\"light_purple\"},{\"text\":\"surface area\"},{\"text\":\".\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"effectiveness\",\"color\":\"aqua\"},{\"text\":\" of a \"},{\"text\":\"sponge \",\"color\":\"yellow\"},{\"text\":\"is based on said \"},{\"text\":\"surface area\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"combination \",\"color\":\"aqua\"},{\"text\":\"of the two seems only \"},{\"text\":\"natural\",\"color\":\"dark_aqua\",\"italic\":true},{\"text\":\".\",\"color\":\"blue\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"The resulting \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"sponge \",\"color\":\"yellow\"},{\"text\":\"is \"},{\"text\":\"much more effective\",\"color\":\"aqua\"},{\"text\":\" than most \"},{\"text\":\"sponges\",\"color\":\"yellow\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It even works on \",\"italic\":true,\"color\":\"dark_aqua\"},{\"text\":\"lava\",\"color\":\"gold\"},{\"text\":\"!\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
   
      item.setNbt(prefNBT);
      prefItem = item;
   
   }
   
   private int absorb(ItemStack item, World world, BlockPos pos) {
      int depthLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"mandelbrot"));
      int absorbLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"sierpinski"));
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
            Material material = blockState.getMaterial();
            if (fluidState.isIn(FluidTags.WATER)) {
               if (blockState.getBlock() instanceof FluidDrainable && !((FluidDrainable)blockState.getBlock()).tryDrainFluid(world, blockPos2, blockState).isEmpty()) {
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
               } else if (material == Material.UNDERWATER_PLANT || material == Material.REPLACEABLE_UNDERWATER_PLANT) {
                  BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(blockPos2) : null;
                  dropStacks(blockState, world, blockPos2, blockEntity);
                  world.setBlockState(blockPos2, Blocks.AIR.getDefaultState(), 3);
                  ++blocksAbsorbed;
                  if (depth < maxDepth) {
                     queue.add(new Pair(blockPos2, depth + 1));
                  }
               }
            }else if(fluidState.isIn(FluidTags.LAVA)){
               if (blockState.getBlock() instanceof FluidDrainable && !((FluidDrainable)blockState.getBlock()).tryDrainFluid(world, blockPos2, blockState).isEmpty()) {
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
   
   @Override
   public List<ItemStack> dropFromBreak(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity, NbtCompound blockData){
      List<ItemStack> drops = new ArrayList<>();
      String uuid = blockData.getString("UUID");
      ItemStack drop = addCrafter(getPrefItem(),blockData.getString("crafter"),blockData.getBoolean("synthetic"),world.getServer());
      NbtCompound magicTag = drop.getNbt().getCompound("arcananovum");
      if(blockData.contains("augments")) {
         magicTag.put("augments",magicTag.getCompound("augments"));
         redoAugmentLore(drop);
      }
      magicTag.putString("UUID",uuid);
      drops.add(drop);
      return drops;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      ItemStack item = playerEntity.getStackInHand(hand);
      Direction side = result.getSide();
      BlockPos placePos = result.getBlockPos().add(side.getVector());
      boolean placeable = world.getBlockState(placePos).canReplace(new ItemPlacementContext(playerEntity, hand, item, result));
      if(placeable && playerEntity instanceof ServerPlayerEntity player){
         placeSponge(player, world, item, placePos);
      }else{
         playerEntity.sendMessage(Text.literal("The Sponge cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
      }
      return false;
   }
   
   private void placeSponge(ServerPlayerEntity player, World world, ItemStack item, BlockPos pos){
      try{
         NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
         MagicBlock spongeBlock = new MagicBlock(pos);
         NbtCompound spongeData = new NbtCompound();
         spongeData.putString("UUID",getUUID(item));
         spongeData.putString("id",this.id);
         spongeData.putString("crafter",getCrafter(item));
         spongeData.putBoolean("synthetic",isSynthetic(item));
         if(magicTag.contains("augments")) spongeData.put("augments",spongeData.getCompound("augments"));
         spongeBlock.setData(spongeData);
         int absorbed = absorb(item, world, pos);
         Block block = absorbed > 0 ? Blocks.WET_SPONGE : Blocks.SPONGE;
         world.setBlockState(pos, block.getDefaultState(), Block.NOTIFY_ALL);
         MAGIC_BLOCK_LIST.get(world).addBlock(spongeBlock);
         
         SoundUtils.playSound(player.getWorld(),pos,SoundEvents.BLOCK_WET_GRASS_PLACE, SoundCategory.BLOCKS,1,.6f);
         item.decrement(item.getCount());
         item.setNbt(new NbtCompound());
         
         if(absorbed > 0){
            SoundUtils.playSound(player.getWorld(),pos,SoundEvents.ENTITY_ELDER_GUARDIAN_HURT, SoundCategory.BLOCKS,1,.8f);
            PLAYER_DATA.get(player).addXP(absorbed); // Add xp
            ArcanaAchievements.progress(player,"ocean_cleanup",absorbed);
         }
      }catch(Exception e){
         e.printStackTrace();
      }
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
}
