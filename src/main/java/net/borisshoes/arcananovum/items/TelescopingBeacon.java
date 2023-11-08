package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.BeaconMiningLaserCallback;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.log;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TelescopingBeacon extends MagicItem {
   
   public TelescopingBeacon(){
      id = "telescoping_beacon";
      name = "Telescoping Beacon";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ITEMS, ArcaneTome.TomeFilter.BLOCKS};
      vanillaItem = Items.BEACON;
      item = new TelescopingBeaconItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Telescoping Beacon\",\"italic\":false,\"color\":\"aqua\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);

      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      NbtList blocks = new NbtList();
      NbtCompound initBlocks = new NbtCompound();
      initBlocks.putString("id","minecraft:iron_block");
      initBlocks.putInt("count",164);
      blocks.add(initBlocks);
      magicTag.put("blocks",blocks);
      magicTag.putBoolean("beacon",true);
      prefNBT = tag;
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"beacon \",\"color\":\"aqua\"},{\"text\":\"automatically \",\"color\":\"blue\"},{\"text\":\"deploys a \"},{\"text\":\"fully powered\",\"color\":\"aqua\"},{\"text\":\" base when placed.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Using \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"the item again on a \",\"color\":\"dark_aqua\"},{\"text\":\"fully powered\",\"color\":\"aqua\"},{\"text\":\" base \",\"color\":\"dark_aqua\"},{\"text\":\"re-captures\"},{\"text\":\" the construct.\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"There must be \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"adequate space\",\"color\":\"aqua\"},{\"text\":\" to \"},{\"text\":\"deploy \",\"color\":\"blue\"},{\"text\":\"the \"},{\"text\":\"beacon\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      
      if(itemStack != null){
         NbtCompound itemNbt = itemStack.getNbt();
         NbtCompound magicTag = itemNbt.getCompound("arcananovum");
         boolean ready = magicTag.getBoolean("beacon");
         if(ready){
            NbtList blocks = magicTag.getList("blocks", NbtElement.COMPOUND_TYPE);
            int blockCount = 0;
            for(int i = 0; i < blocks.size(); i++){
               NbtCompound blockType = blocks.getCompound(i);
               int count = blockType.getInt("count");
               blockCount+=count;
            }
            int tier = blocksToTier(blockCount);
            loreList.add(NbtString.of("[{\"text\":\"Construct Status - \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Ready - Tier "+tier+"\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
         }else{
            loreList.add(NbtString.of("[{\"text\":\"Construct Status - \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Empty\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
         }
      }else{
         loreList.add(NbtString.of("[{\"text\":\"Construct Status - \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Ready - Tier 4\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      }
      
      return loreList;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtElement blocksNbt = magicTag.getList("blocks",NbtElement.COMPOUND_TYPE).copy();
      boolean hasData = magicTag.contains("data");
      NbtCompound dataTag = new NbtCompound();
      if(hasData){
         dataTag = magicTag.getCompound("data").copy();
      }
      boolean ready = magicTag.getBoolean("beacon");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      if(hasData){
         newTag.getCompound("arcananovum").put("data",dataTag);
      }
      newTag.getCompound("arcananovum").put("blocks",blocksNbt);
      newTag.getCompound("arcananovum").putBoolean("beacon",ready);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   private static List<Pair<BlockPos,BlockState>> getBaseBlocks(World world, BlockPos pos) {
      ArrayList<Pair<BlockPos,BlockState>> blocks = new ArrayList<>();
      int beaconX = pos.getX();
      int beaconY = pos.getY();
      int beaconZ = pos.getZ();
      
      int index = 0;
      for(int curLevel = 1; curLevel <= 4; curLevel++) {
         int curY = beaconY - curLevel;
         if (curY < world.getBottomY()) {
            break;
         }
         
         for(int curX = beaconX - curLevel; curX <= beaconX + curLevel; ++curX) {
            for(int curZ = beaconZ - curLevel; curZ <= beaconZ + curLevel; ++curZ) {
               BlockPos blockPos = new BlockPos(curX, curY, curZ);
               BlockState blockState = world.getBlockState(blockPos);
               if (blockState.isIn(BlockTags.BEACON_BASE_BLOCKS)) {
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
      
      for(int curLevel = 1; curLevel <= tier; curLevel++) {
         int curY = beaconY - curLevel;
         if (curY < world.getBottomY()) {
            //log("Hit bottom of world, Failed");
            return false;
         }
      
         for(int curX = beaconX - curLevel; curX <= beaconX + curLevel; ++curX) {
            for(int curZ = beaconZ - curLevel; curZ <= beaconZ + curLevel; ++curZ) {
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
         
         for(int i = 0; i < blockTypes.size(); i++){
            NbtCompound blockType = blockTypes.getCompound(i);
            int count = blockType.getInt("count");
            String id = blockType.getString("id");
            Block block = Registries.BLOCK.getOrEmpty(new Identifier(id)).orElse(null);
            if(block == null){
               log(1,"Unknown Block Type Stored In Telescoping Beacon: "+id);
               return;
            }
            for(int j = 0; j < count; j++){
               blocks.add(block.getDefaultState());
            }
         }
   
         int beaconX = pos.getX();
         int beaconY = pos.getY();
         int beaconZ = pos.getZ();
   
         int index = 0;
         for(int curLevel = 1; curLevel <= tier; curLevel++) {
            int curY = beaconY - curLevel;
            if (curY < world.getBottomY()) {
               return;
            }
      
            for(int curX = beaconX - curLevel; curX <= beaconX + curLevel; ++curX) {
               for(int curZ = beaconZ - curLevel; curZ <= beaconZ + curLevel; ++curZ) {
                  BlockState blockState = blocks.get(index);
                  world.setBlockState(new BlockPos(curX,curY,curZ),blockState,3);
                  index++;
               }
            }
         }
         world.setBlockState(pos,Blocks.BEACON.getDefaultState(),3);
         if(data != null){
            BlockState placeState = world.getBlockState(pos);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if(placeState.isOf(Blocks.BEACON) && (blockEntity instanceof BeaconBlockEntity beaconBlock)){
               beaconBlock.readNbt(data);
            }
         }
         if(mining){
            ArcanaNovum.addTickTimerCallback(player.getServerWorld(),new BeaconMiningLaserCallback(player.getServerWorld(),pos,pos.up()));
         }
         

         player.teleport(pos.getX()+.5,pos.getY()+2,pos.getZ()+.5);
         PLAYER_DATA.get(player).addXP(index); // Add xp
   
         
         for(int i = 0; i <= tier; i++){
            int j = i;
            ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(2*(i+1), () -> SoundUtils.playSound(world,pos,SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.PLAYERS,1,.8f+(.2f*j))));
         }
         PLAYER_DATA.get(player).addXP(10); // Add xp
   
         if(blockTypes.size() == 1 && blockTypes.getCompound(0).getInt("count") >= 164){
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
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.PISTON,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.OBSIDIAN,32,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.NETHER_STAR,1,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHERITE_INGOT,1,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.IRON_BLOCK,64,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.BEACON,1,null, true);
   
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" Telescoping Beacon\\n\\nRarity: Empowered\\n\\nA fully powered beacon is a rather large construct. Breaking them down and setting them up is a lot of effort. Through a combination of pistons and a Netherite reinforced chassis, this beacon\"}");
      list.add("{\"text\":\" Telescoping Beacon\\n\\ncan expand and contract with the press of a button.\\n\\nCollecting it will store enough metallic blocks to redeploy at the highest possible tier without collecting extra.\\n\\nThere must be enough\"}");
      list.add("{\"text\":\" Telescoping Beacon\\n\\nroom for the beacon and its base to deploy in order to activate.\\n\\nThe beacon expands upwards from the location of placement.\"}");
      return list;
   }
   
   public class TelescopingBeaconItem extends MagicPolymerItem {
      public TelescopingBeaconItem(Settings settings){
         super(getThis(),settings);
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
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         NbtList blocks = magicNbt.getList("blocks", NbtElement.COMPOUND_TYPE);
         boolean hasBeacon = magicNbt.getBoolean("beacon");
         if(!(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.SUCCESS;
         
         Direction side = context.getSide();
         BlockPos placePos = hasBeacon ? context.getBlockPos().add(side.getVector()) : context.getBlockPos();
         
         if(hasBeacon){ // Place beacon
            int blockCount = 0;
            for(int i = 0; i < blocks.size(); i++){
               NbtCompound blockType = blocks.getCompound(i);
               int count = blockType.getInt("count");
               blockCount+=count;
            }
            int tier = blocksToTier(blockCount);
            placePos = placePos.add(0,tier,0);
            
            if(hasSpace(world, placePos, tier) && world.getBlockState(placePos).canReplace(new ItemPlacementContext(playerEntity, hand, stack, new BlockHitResult(context.getHitPos(),context.getSide(),context.getBlockPos(),context.hitsInsideBlock())))){
               boolean careful = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CAREFUL_RECONSTRUCTION.id) >= 1;
               boolean mining = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.MINING_LASER.id) >= 1;
               if(careful && magicNbt.contains("data",NbtElement.COMPOUND_TYPE)){
                  placeBeacon(player, world, placePos, tier, blocks,magicNbt.getCompound("data"),mining);
               }else{
                  placeBeacon(player, world, placePos, tier, blocks,null,mining);
               }
               
               magicNbt.put("blocks",new NbtList());
               magicNbt.putBoolean("beacon",false);
               buildItemLore(stack,player.getServer());
            }else{
               playerEntity.sendMessage(Text.translatable("The Beacon cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }
         }else{ // Capture beacon
            BlockState placeState = world.getBlockState(placePos);
            BlockEntity blockEntity = world.getBlockEntity(placePos);
            if(!placeState.isOf(Blocks.BEACON) || !(blockEntity instanceof BeaconBlockEntity beaconBlock)){
               playerEntity.sendMessage(Text.translatable("No Beacon Present").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               return ActionResult.SUCCESS;
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
            magicNbt.put("blocks",blocks);
            magicNbt.putBoolean("beacon",true);
            
            if(careful){
               magicNbt.put("data",beaconBlock.createNbt());
            }
            
            world.setBlockState(placePos, Blocks.AIR.getDefaultState(), 3);
            
            
            if(world instanceof ServerWorld serverWorld){
               for(int i = 0; i <= tier; i++){
                  int j = i;
                  BlockPos finalPlacePos = placePos;
                  ArcanaNovum.addTickTimerCallback(serverWorld, new GenericTimer(2*(i+1), () -> SoundUtils.playSound(world, finalPlacePos,SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, SoundCategory.PLAYERS,1,2f-(.3f*j))));
               }
            }
            
            buildItemLore(stack,player.getServer());
         }
         
         return ActionResult.SUCCESS;
      }
   }
}
