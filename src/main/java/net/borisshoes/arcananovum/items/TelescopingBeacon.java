package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.BeaconMiningLaserCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaNovum.log;

public class TelescopingBeacon extends ArcanaItem {
	public static final String ID = "telescoping_beacon";
   
   public static final String BLOCKS_TAG = "blocks";
   public static final String BEACON_TAG = "beacon";
   public static final String DATA_TAG = "data";
   
   public TelescopingBeacon(){
      id = ID;
      name = "Telescoping Beacon";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS, TomeGui.TomeFilter.BLOCKS};
      vanillaItem = Items.BEACON;
      item = new TelescopingBeaconItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.AQUA);
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_CREATE_FULL_BEACON,ResearchTasks.OBTAIN_PISTON};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      NbtCompound initBlocks = new NbtCompound();
      initBlocks.putString("id",Registries.BLOCK.getId(Blocks.IRON_BLOCK).toString());
      initBlocks.putInt("count",164);
      NbtList blocks = new NbtList();
      blocks.add(initBlocks);
      putProperty(stack,BLOCKS_TAG,blocks);
      putProperty(stack,BEACON_TAG,true);
      putProperty(stack,DATA_TAG,new NbtCompound());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("beacon ").formatted(Formatting.AQUA))
            .append(Text.literal("automatically ").formatted(Formatting.BLUE))
            .append(Text.literal("deploys a ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("fully powered").formatted(Formatting.AQUA))
            .append(Text.literal(" base when placed.").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Using ").formatted(Formatting.BLUE))
            .append(Text.literal("the item again on a ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("fully powered").formatted(Formatting.AQUA))
            .append(Text.literal(" base ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("re-captures").formatted(Formatting.BLUE))
            .append(Text.literal(" the construct.").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("There must be ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("adequate space").formatted(Formatting.AQUA))
            .append(Text.literal(" to ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("deploy ").formatted(Formatting.BLUE))
            .append(Text.literal("the ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("beacon").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal(""));
      if(itemStack != null && getBooleanProperty(itemStack,BEACON_TAG)){
         NbtList blocks = getListProperty(itemStack,BLOCKS_TAG);
         int blockCount = 0;
         for(int i = 0; i < blocks.size(); i++){
            NbtCompound blockType = blocks.getCompoundOrEmpty(i);
            int count = blockType.getInt("count", 0);
            blockCount+=count;
         }
         int tier = blocksToTier(blockCount);
         lore.add(Text.literal("")
               .append(Text.literal("Construct Status - ").formatted(Formatting.BLUE))
               .append(Text.literal("Ready - Tier "+tier).formatted(Formatting.AQUA)));
      }else{
         lore.add(Text.literal("")
               .append(Text.literal("Construct Status - ").formatted(Formatting.BLUE))
               .append(Text.literal("Empty").formatted(Formatting.GRAY)));
      }
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtList blocksNbt = getListProperty(stack,BLOCKS_TAG).copy();
      boolean ready = getBooleanProperty(stack,BEACON_TAG);
      NbtCompound data = getCompoundProperty(stack,DATA_TAG).copy();
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(stack,DATA_TAG,data);
      putProperty(newStack,BLOCKS_TAG,blocksNbt);
      putProperty(newStack,BEACON_TAG,ready);
      return buildItemLore(newStack,server);
   }
   
   private static List<Pair<BlockPos,BlockState>> getBaseBlocks(World world, BlockPos pos){
      ArrayList<Pair<BlockPos,BlockState>> blocks = new ArrayList<>();
      int beaconX = pos.getX();
      int beaconY = pos.getY();
      int beaconZ = pos.getZ();
      
      int index = 0;
      for(int curLevel = 1; curLevel <= 4; curLevel++){
         int curY = beaconY - curLevel;
         if(curY < world.getBottomY()){
            break;
         }
         
         for(int curX = beaconX - curLevel; curX <= beaconX + curLevel; ++curX){
            for(int curZ = beaconZ - curLevel; curZ <= beaconZ + curLevel; ++curZ){
               BlockPos blockPos = new BlockPos(curX, curY, curZ);
               BlockState blockState = world.getBlockState(blockPos);
               if(blockState.isIn(BlockTags.BEACON_BASE_BLOCKS)){
                  blocks.add(index,new Pair<>(blockPos,blockState));
                  index++;
               }
            }
         }
         
      }
      
      return blocks;
   }
   
   private boolean hasSpace(World world, BlockPos pos, int tier){
      int beaconX = pos.getX();
      int beaconY = pos.getY();
      int beaconZ = pos.getZ();
      
      for(int curLevel = 1; curLevel <= tier; curLevel++){
         int curY = beaconY - curLevel;
         if(curY < world.getBottomY()){
            //log("Hit bottom of world, Failed");
            return false;
         }
         
         for(int curX = beaconX - curLevel; curX <= beaconX + curLevel; ++curX){
            for(int curZ = beaconZ - curLevel; curZ <= beaconZ + curLevel; ++curZ){
               BlockPos blockPos = new BlockPos(curX, curY, curZ);
               BlockState blockState = world.getBlockState(blockPos);
               
               if(!blockState.isIn(BlockTags.REPLACEABLE)){
                  //log("Block not replaceable at: "+blockPos);
                  return false;
               }
            }
         }
         
      }
      return true;
   }
   
   private void placeBeacon(ServerPlayerEntity player, World world, BlockPos pos, int tier, NbtList blockTypes, NbtCompound data, boolean mining){
      try{
         ArrayList<BlockState> blocks = new ArrayList<>();
         HashMap<Block,Integer> blockTotals = new HashMap<>();
         Block blockKey = null;
         
         for(int i = 0; i < blockTypes.size(); i++){
            NbtCompound blockType = blockTypes.getCompoundOrEmpty(i);
            int count = blockType.getInt("count", 0);
            String id = blockType.getString("id", "");
            Block block = Registries.BLOCK.getOptionalValue(Identifier.of(id)).orElse(null);
            if(block == null){
               log(1,"Unknown Block Type Stored In Telescoping Beacon: "+id);
               return;
            }
            for(int j = 0; j < count; j++){
               blocks.add(block.getDefaultState());
            }
            if(blockTotals.containsKey(block)){
               blockTotals.put(block,blockTotals.get(block)+count);
            }else{
               blockKey = block;
               blockTotals.put(block,count);
            }
         }
         
         int beaconX = pos.getX();
         int beaconY = pos.getY();
         int beaconZ = pos.getZ();
         
         int index = 0;
         for(int curLevel = 1; curLevel <= tier; curLevel++){
            int curY = beaconY - curLevel;
            if(curY < world.getBottomY()){
               return;
            }
            
            for(int curX = beaconX - curLevel; curX <= beaconX + curLevel; ++curX){
               for(int curZ = beaconZ - curLevel; curZ <= beaconZ + curLevel; ++curZ){
                  BlockState blockState = blocks.get(index);
                  world.setBlockState(new BlockPos(curX,curY,curZ),blockState,3);
                  index++;
               }
            }
         }
         world.setBlockState(pos,Blocks.BEACON.getDefaultState(),3);
         if(data != null){
            BlockState placeState = world.getBlockState(pos);
            BlockEntity blockEntity = BlockEntity.createFromNbt(pos,placeState,data,world.getRegistryManager());
            if(blockEntity != null){
               world.addBlockEntity(blockEntity);
            }
         }
         if(mining){
            BorisLib.addTickTimerCallback(player.getWorld(),new BeaconMiningLaserCallback(player.getWorld(),pos,pos.up()));
         }
         
         
         player.requestTeleport(pos.getX()+.5,pos.getY()+2,pos.getZ()+.5);
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.TELESCOPING_BEACON_PER_BLOCK)*index); // Add xp
         
         
         for(int i = 0; i <= tier; i++){
            int j = i;
            BorisLib.addTickTimerCallback(player.getWorld(), new GenericTimer(2*(i+1), () -> SoundUtils.playSound(world,pos,SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.PLAYERS,1,.8f+(.2f*j))));
         }
         
         if(blockTotals.size() == 1 && blockTotals.get(blockKey) >= 164){
            BlockState blockType = blocks.get(0);
            if(blockType.isOf(Blocks.DIAMOND_BLOCK)){
               ArcanaAchievements.grant(player,ArcanaAchievements.BEJEWELED.id);
            }else if(blockType.isOf(Blocks.EMERALD_BLOCK)){
               ArcanaAchievements.grant(player,ArcanaAchievements.ART_OF_THE_DEAL.id);
            }else if(blockType.isOf(Blocks.GOLD_BLOCK)){
               ArcanaAchievements.grant(player,ArcanaAchievements.ACQUISITION_RULES.id);
            }else if(blockType.isOf(Blocks.NETHERITE_BLOCK)){
               ArcanaAchievements.grant(player,ArcanaAchievements.CLINICALLY_INSANE.id);
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public static int[] tiers = {9,34,83,164};
   public static int blocksToTier(int blocks){
      for(int i=0; i<tiers.length; i++){
         if(blocks < tiers[i]){
            return i;
         }
      }
      return tiers.length;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,8);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,8);
      ArcanaIngredient c = new ArcanaIngredient(Items.PISTON,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.IRON_BLOCK,32, true);
      ArcanaIngredient m = new ArcanaIngredient(Items.BEACON,1, true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,c,h,c,b},
            {c,h,m,h,c},
            {b,c,h,c,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Telescoping\n       Beacon").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nA fully empowered beacon is a rather large construct. Their setup and breakdown is a great deal of effort. Through a combination of pistons and a reinforced chassis, the beacon   ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Telescoping\n       Beacon").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\ncan expand and retract with the press of a button.\n\nCollecting it will store enough base blocks to redeploy at the highest possible tier without collecting extra.\n ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Telescoping\n       Beacon").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\nThere must be enough room for the beacon and its base to deploy in order to activate.\n\nThe beacon expands upwards from the location of placement.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class TelescopingBeaconItem extends ArcanaPolymerItem {
      public TelescopingBeaconItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         if(!getBooleanProperty(itemStack,BEACON_TAG)){
            stringList.add("empty");
         }
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         PlayerEntity playerEntity = context.getPlayer();
         Hand hand = context.getHand();
         World world = context.getWorld();
         ItemStack stack = context.getStack();
         NbtList blocks = getListProperty(stack,BLOCKS_TAG);
         boolean hasBeacon = getBooleanProperty(stack,BEACON_TAG);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.SUCCESS_SERVER;
         
         Direction side = context.getSide();
         BlockPos placePos = hasBeacon ? context.getBlockPos().add(side.getVector()) : context.getBlockPos();
         
         if(hasBeacon){ // Place beacon
            int blockCount = 0;
            for(int i = 0; i < blocks.size(); i++){
               NbtCompound blockType = blocks.getCompoundOrEmpty(i);
               int count = blockType.getInt("count", 0);
               blockCount+=count;
            }
            int tier = blocksToTier(blockCount);
            placePos = placePos.add(0,tier,0);
            
            if(hasSpace(world, placePos, tier) && world.getBlockState(placePos).canReplace(new ItemPlacementContext(playerEntity, hand, stack, new BlockHitResult(context.getHitPos(),context.getSide(),context.getBlockPos(),context.hitsInsideBlock())))){
               boolean careful = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CAREFUL_RECONSTRUCTION.id) >= 1;
               boolean mining = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.MINING_LASER.id) >= 1;
               if(careful && !getCompoundProperty(stack,DATA_TAG).isEmpty()){
                  placeBeacon(player, world, placePos, tier, blocks,getCompoundProperty(stack,DATA_TAG),mining);
               }else{
                  placeBeacon(player, world, placePos, tier, blocks,null,mining);
               }
               
               putProperty(stack,BLOCKS_TAG,new NbtList());
               putProperty(stack,BEACON_TAG,false);
               buildItemLore(stack,player.getServer());
               player.getItemCooldownManager().set(stack,20);
            }else{
               playerEntity.sendMessage(Text.literal("The Beacon cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }
         }else{ // Capture beacon
            BlockState placeState = world.getBlockState(placePos);
            BlockEntity blockEntity = world.getBlockEntity(placePos);
            if(!placeState.isOf(Blocks.BEACON) || !(blockEntity instanceof BeaconBlockEntity beaconBlock)){
               playerEntity.sendMessage(Text.literal("No Beacon Present").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               return ActionResult.SUCCESS_SERVER;
            }
            
            // Scan for support blocks
            List<Pair<BlockPos,BlockState>> baseBlocks = getBaseBlocks(world,placePos);
            int tier = blocksToTier(baseBlocks.size());
            boolean careful = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CAREFUL_RECONSTRUCTION.id)) >= 1;
            // Remove support blocks and add them to NBT
            blocks = new NbtList();
            if(tier != 0){
               HashMap<Block,Integer> blockTypes = new HashMap<>();
               ArrayList<Block> orderedBlocks = new ArrayList<>();
               for(int i = 0; i < tiers[tier-1]; i++){
                  BlockState blockState = baseBlocks.get(i).getRight();
                  Block blockType = blockState.getBlock();
                  if(blockTypes.containsKey(blockType)){
                     blockTypes.put(blockType,blockTypes.get(blockType)+1);
                  }else{
                     blockTypes.put(blockType,1);
                  }
                  orderedBlocks.add(blockType);
                  world.setBlockState(baseBlocks.get(i).getLeft(), Blocks.AIR.getDefaultState(), 3);
               }
               
               if(careful){
                  for(Block orderedBlock : orderedBlocks){
                     NbtCompound blockType = new NbtCompound();
                     blockType.putString("id",Registries.BLOCK.getId(orderedBlock).toString());
                     blockType.putInt("count",1);
                     blocks.add(blockType);
                  }
               }else{
                  for(Map.Entry<Block, Integer> entry : blockTypes.entrySet()){
                     NbtCompound blockType = new NbtCompound();
                     blockType.putString("id",Registries.BLOCK.getId(entry.getKey()).toString());
                     blockType.putInt("count",entry.getValue());
                     blocks.add(blockType);
                  }
               }
            }
            putProperty(stack,BLOCKS_TAG,blocks);
            putProperty(stack,BEACON_TAG,true);
            
            if(careful){
               putProperty(stack,DATA_TAG,beaconBlock.createNbtWithIdentifyingData(player.getServer().getRegistryManager()));
            }else{
               putProperty(stack,DATA_TAG,new NbtCompound());
            }
            
            world.setBlockState(placePos, Blocks.AIR.getDefaultState(), 3);
            
            
            if(world instanceof ServerWorld serverWorld){
               for(int i = 0; i <= tier; i++){
                  int j = i;
                  BlockPos finalPlacePos = placePos;
                  BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(2*(i+1), () -> SoundUtils.playSound(world, finalPlacePos,SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, SoundCategory.PLAYERS,1,2f-(.3f*j))));
               }
            }
            
            buildItemLore(stack,player.getServer());
            player.getItemCooldownManager().set(stack,20);
         }
         
         return ActionResult.SUCCESS_SERVER;
      }
   }
}

