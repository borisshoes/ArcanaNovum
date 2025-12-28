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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.ADVANCEMENT_CREATE_FULL_BEACON,ResearchTasks.OBTAIN_PISTON};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      CompoundTag initBlocks = new CompoundTag();
      initBlocks.putString("id", BuiltInRegistries.BLOCK.getKey(Blocks.IRON_BLOCK).toString());
      initBlocks.putInt("count",164);
      ListTag blocks = new ListTag();
      blocks.add(initBlocks);
      putProperty(stack,BLOCKS_TAG,blocks);
      putProperty(stack,BEACON_TAG,true);
      putProperty(stack,DATA_TAG,new CompoundTag());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("beacon ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("automatically ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("deploys a ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("fully powered").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" base when placed.").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Using ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("the item again on a ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("fully powered").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" base ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("re-captures").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" the construct.").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("There must be ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("adequate space").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("deploy ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("beacon").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal(""));
      if(itemStack != null && getBooleanProperty(itemStack,BEACON_TAG)){
         ListTag blocks = getListProperty(itemStack,BLOCKS_TAG);
         int blockCount = 0;
         for(int i = 0; i < blocks.size(); i++){
            CompoundTag blockType = blocks.getCompoundOrEmpty(i);
            int count = blockType.getIntOr("count", 0);
            blockCount+=count;
         }
         int tier = blocksToTier(blockCount);
         lore.add(Component.literal("")
               .append(Component.literal("Construct Status - ").withStyle(ChatFormatting.BLUE))
               .append(Component.literal("Ready - Tier "+tier).withStyle(ChatFormatting.AQUA)));
      }else{
         lore.add(Component.literal("")
               .append(Component.literal("Construct Status - ").withStyle(ChatFormatting.BLUE))
               .append(Component.literal("Empty").withStyle(ChatFormatting.GRAY)));
      }
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ListTag blocksNbt = getListProperty(stack,BLOCKS_TAG).copy();
      boolean ready = getBooleanProperty(stack,BEACON_TAG);
      CompoundTag data = getCompoundProperty(stack,DATA_TAG).copy();
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(stack,DATA_TAG,data);
      putProperty(newStack,BLOCKS_TAG,blocksNbt);
      putProperty(newStack,BEACON_TAG,ready);
      return buildItemLore(newStack,server);
   }
   
   private static List<Tuple<BlockPos, BlockState>> getBaseBlocks(Level world, BlockPos pos){
      ArrayList<Tuple<BlockPos, BlockState>> blocks = new ArrayList<>();
      int beaconX = pos.getX();
      int beaconY = pos.getY();
      int beaconZ = pos.getZ();
      
      int index = 0;
      for(int curLevel = 1; curLevel <= 4; curLevel++){
         int curY = beaconY - curLevel;
         if(curY < world.getMinY()){
            break;
         }
         
         for(int curX = beaconX - curLevel; curX <= beaconX + curLevel; ++curX){
            for(int curZ = beaconZ - curLevel; curZ <= beaconZ + curLevel; ++curZ){
               BlockPos blockPos = new BlockPos(curX, curY, curZ);
               BlockState blockState = world.getBlockState(blockPos);
               if(blockState.is(BlockTags.BEACON_BASE_BLOCKS)){
                  blocks.add(index,new Tuple<>(blockPos,blockState));
                  index++;
               }
            }
         }
         
      }
      
      return blocks;
   }
   
   private boolean hasSpace(Level world, BlockPos pos, int tier){
      int beaconX = pos.getX();
      int beaconY = pos.getY();
      int beaconZ = pos.getZ();
      
      for(int curLevel = 1; curLevel <= tier; curLevel++){
         int curY = beaconY - curLevel;
         if(curY < world.getMinY()){
            //log("Hit bottom of world, Failed");
            return false;
         }
         
         for(int curX = beaconX - curLevel; curX <= beaconX + curLevel; ++curX){
            for(int curZ = beaconZ - curLevel; curZ <= beaconZ + curLevel; ++curZ){
               BlockPos blockPos = new BlockPos(curX, curY, curZ);
               BlockState blockState = world.getBlockState(blockPos);
               
               if(!blockState.is(BlockTags.REPLACEABLE)){
                  //log("Block not replaceable at: "+blockPos);
                  return false;
               }
            }
         }
         
      }
      return true;
   }
   
   private void placeBeacon(ServerPlayer player, Level world, BlockPos pos, int tier, ListTag blockTypes, CompoundTag data, boolean mining){
      try{
         ArrayList<BlockState> blocks = new ArrayList<>();
         HashMap<Block,Integer> blockTotals = new HashMap<>();
         Block blockKey = null;
         
         for(int i = 0; i < blockTypes.size(); i++){
            CompoundTag blockType = blockTypes.getCompoundOrEmpty(i);
            int count = blockType.getIntOr("count", 0);
            String id = blockType.getStringOr("id", "");
            Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElse(null);
            if(block == null){
               log(1,"Unknown Block Type Stored In Telescoping Beacon: "+id);
               return;
            }
            for(int j = 0; j < count; j++){
               blocks.add(block.defaultBlockState());
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
            if(curY < world.getMinY()){
               return;
            }
            
            for(int curX = beaconX - curLevel; curX <= beaconX + curLevel; ++curX){
               for(int curZ = beaconZ - curLevel; curZ <= beaconZ + curLevel; ++curZ){
                  BlockState blockState = blocks.get(index);
                  world.setBlock(new BlockPos(curX,curY,curZ),blockState,3);
                  index++;
               }
            }
         }
         world.setBlock(pos, Blocks.BEACON.defaultBlockState(),3);
         if(data != null){
            BlockState placeState = world.getBlockState(pos);
            BlockEntity blockEntity = BlockEntity.loadStatic(pos,placeState,data,world.registryAccess());
            if(blockEntity != null){
               world.setBlockEntity(blockEntity);
            }
         }
         if(mining){
            BorisLib.addTickTimerCallback(player.level(),new BeaconMiningLaserCallback(player.level(),pos,pos.above()));
         }
         
         
         player.teleportTo(pos.getX()+.5,pos.getY()+2,pos.getZ()+.5);
         ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.TELESCOPING_BEACON_PER_BLOCK)*index); // Add xp
         
         
         for(int i = 0; i <= tier; i++){
            int j = i;
            BorisLib.addTickTimerCallback(player.level(), new GenericTimer(2*(i+1), () -> SoundUtils.playSound(world,pos, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS,1,.8f+(.2f*j))));
         }
         
         if(blockTotals.size() == 1 && blockTotals.get(blockKey) >= 164){
            BlockState blockType = blocks.get(0);
            if(blockType.is(Blocks.DIAMOND_BLOCK)){
               ArcanaAchievements.grant(player,ArcanaAchievements.BEJEWELED.id);
            }else if(blockType.is(Blocks.EMERALD_BLOCK)){
               ArcanaAchievements.grant(player,ArcanaAchievements.ART_OF_THE_DEAL.id);
            }else if(blockType.is(Blocks.GOLD_BLOCK)){
               ArcanaAchievements.grant(player,ArcanaAchievements.ACQUISITION_RULES.id);
            }else if(blockType.is(Blocks.NETHERITE_BLOCK)){
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
      return new ArcanaRecipe(this, ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Telescoping\n       Beacon").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nA fully empowered beacon is a rather large construct. Their setup and breakdown is a great deal of effort. Through a combination of pistons and a reinforced chassis, the beacon   ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Telescoping\n       Beacon").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\ncan expand and retract with the press of a button.\n\nCollecting it will store enough base blocks to redeploy at the highest possible tier without collecting extra.\n ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Telescoping\n       Beacon").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nThere must be enough room for the beacon and its base to deploy in order to activate.\n\nThe beacon expands upwards from the location of placement.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class TelescopingBeaconItem extends ArcanaPolymerItem {
      public TelescopingBeaconItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         if(!getBooleanProperty(itemStack,BEACON_TAG)){
            stringList.add("empty");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         Player playerEntity = context.getPlayer();
         InteractionHand hand = context.getHand();
         Level world = context.getLevel();
         ItemStack stack = context.getItemInHand();
         ListTag blocks = getListProperty(stack,BLOCKS_TAG);
         boolean hasBeacon = getBooleanProperty(stack,BEACON_TAG);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.SUCCESS_SERVER;
         
         Direction side = context.getClickedFace();
         BlockPos placePos = hasBeacon ? context.getClickedPos().offset(side.getUnitVec3i()) : context.getClickedPos();
         
         if(hasBeacon){ // Place beacon
            int blockCount = 0;
            for(int i = 0; i < blocks.size(); i++){
               CompoundTag blockType = blocks.getCompoundOrEmpty(i);
               int count = blockType.getIntOr("count", 0);
               blockCount+=count;
            }
            int tier = blocksToTier(blockCount);
            placePos = placePos.offset(0,tier,0);
            
            if(hasSpace(world, placePos, tier) && world.getBlockState(placePos).canBeReplaced(new BlockPlaceContext(playerEntity, hand, stack, new BlockHitResult(context.getClickLocation(),context.getClickedFace(),context.getClickedPos(),context.isInside())))){
               boolean careful = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CAREFUL_RECONSTRUCTION.id) >= 1;
               boolean mining = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.MINING_LASER.id) >= 1;
               if(careful && !getCompoundProperty(stack,DATA_TAG).isEmpty()){
                  placeBeacon(player, world, placePos, tier, blocks,getCompoundProperty(stack,DATA_TAG),mining);
               }else{
                  placeBeacon(player, world, placePos, tier, blocks,null,mining);
               }
               
               putProperty(stack,BLOCKS_TAG,new ListTag());
               putProperty(stack,BEACON_TAG,false);
               buildItemLore(stack,player.level().getServer());
               player.getCooldowns().addCooldown(stack,20);
            }else{
               playerEntity.displayClientMessage(Component.literal("The Beacon cannot be placed here.").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayer) playerEntity, SoundEvents.FIRE_EXTINGUISH, 1,1);
            }
         }else{ // Capture beacon
            BlockState placeState = world.getBlockState(placePos);
            BlockEntity blockEntity = world.getBlockEntity(placePos);
            if(!placeState.is(Blocks.BEACON) || !(blockEntity instanceof BeaconBlockEntity beaconBlock)){
               playerEntity.displayClientMessage(Component.literal("No Beacon Present").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayer) playerEntity, SoundEvents.FIRE_EXTINGUISH, 1,1);
               return InteractionResult.SUCCESS_SERVER;
            }
            
            // Scan for support blocks
            List<Tuple<BlockPos, BlockState>> baseBlocks = getBaseBlocks(world,placePos);
            int tier = blocksToTier(baseBlocks.size());
            boolean careful = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CAREFUL_RECONSTRUCTION.id)) >= 1;
            // Remove support blocks and add them to NBT
            blocks = new ListTag();
            if(tier != 0){
               HashMap<Block,Integer> blockTypes = new HashMap<>();
               ArrayList<Block> orderedBlocks = new ArrayList<>();
               for(int i = 0; i < tiers[tier-1]; i++){
                  BlockState blockState = baseBlocks.get(i).getB();
                  Block blockType = blockState.getBlock();
                  if(blockTypes.containsKey(blockType)){
                     blockTypes.put(blockType,blockTypes.get(blockType)+1);
                  }else{
                     blockTypes.put(blockType,1);
                  }
                  orderedBlocks.add(blockType);
                  world.setBlock(baseBlocks.get(i).getA(), Blocks.AIR.defaultBlockState(), 3);
               }
               
               if(careful){
                  for(Block orderedBlock : orderedBlocks){
                     CompoundTag blockType = new CompoundTag();
                     blockType.putString("id", BuiltInRegistries.BLOCK.getKey(orderedBlock).toString());
                     blockType.putInt("count",1);
                     blocks.add(blockType);
                  }
               }else{
                  for(Map.Entry<Block, Integer> entry : blockTypes.entrySet()){
                     CompoundTag blockType = new CompoundTag();
                     blockType.putString("id", BuiltInRegistries.BLOCK.getKey(entry.getKey()).toString());
                     blockType.putInt("count",entry.getValue());
                     blocks.add(blockType);
                  }
               }
            }
            putProperty(stack,BLOCKS_TAG,blocks);
            putProperty(stack,BEACON_TAG,true);
            
            if(careful){
               putProperty(stack,DATA_TAG,beaconBlock.saveWithFullMetadata(player.level().getServer().registryAccess()));
            }else{
               putProperty(stack,DATA_TAG,new CompoundTag());
            }
            
            world.setBlock(placePos, Blocks.AIR.defaultBlockState(), 3);
            
            
            if(world instanceof ServerLevel serverWorld){
               for(int i = 0; i <= tier; i++){
                  int j = i;
                  BlockPos finalPlacePos = placePos;
                  BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(2*(i+1), () -> SoundUtils.playSound(world, finalPlacePos, SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.PLAYERS,1,2f-(.3f*j))));
               }
            }
            
            buildItemLore(stack,player.level().getServer());
            player.getCooldowns().addCooldown(stack,20);
         }
         
         return InteractionResult.SUCCESS_SERVER;
      }
   }
}

