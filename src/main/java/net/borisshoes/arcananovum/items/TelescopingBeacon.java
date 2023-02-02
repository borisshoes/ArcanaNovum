package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.*;

import static net.borisshoes.arcananovum.Arcananovum.log;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;

public class TelescopingBeacon extends MagicItem implements UsableItem {
   
   public TelescopingBeacon(){
      id = "telescoping_beacon";
      name = "Telescoping Beacon";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ITEMS, ArcaneTome.TomeFilter.BLOCKS};
      
      ItemStack item = new ItemStack(Items.BEACON);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Telescoping Beacon\",\"italic\":false,\"color\":\"aqua\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"beacon \",\"color\":\"aqua\"},{\"text\":\"automatically \",\"color\":\"blue\"},{\"text\":\"deploys a \"},{\"text\":\"fully powered\",\"color\":\"aqua\"},{\"text\":\" base when placed.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Using \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"the item again on a \",\"color\":\"dark_aqua\"},{\"text\":\"fully powered\",\"color\":\"aqua\"},{\"text\":\" base \",\"color\":\"dark_aqua\"},{\"text\":\"re-captures\"},{\"text\":\" the construct.\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"There must be \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"adequate space\",\"color\":\"aqua\"},{\"text\":\" to \"},{\"text\":\"deploy \",\"color\":\"blue\"},{\"text\":\"the \"},{\"text\":\"beacon\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Construct Status - \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Ready - Tier 4\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
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
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtCompound blocksNbt = magicTag.getCompound("blocks").copy();
      boolean ready = magicTag.getBoolean("beacon");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").put("blocks",blocksNbt);
      newTag.getCompound("arcananovum").putBoolean("beacon",ready);
      stack.setNbt(newTag);
      NbtList loreList = newTag.getCompound("display").getList("Lore", NbtType.STRING);
      if(ready){
         loreList.set(4,NbtString.of("[{\"text\":\"Construct Status - \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Ready\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      }else{
         loreList.set(4,NbtString.of("[{\"text\":\"Construct Status - \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Empty\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      }
      return stack;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      ItemStack item = playerEntity.getStackInHand(hand);
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList blocks = magicNbt.getList("blocks", NbtElement.COMPOUND_TYPE);
      boolean hasBeacon = magicNbt.getBoolean("beacon");
      
      Direction side = result.getSide();
      BlockPos placePos = hasBeacon ? result.getBlockPos().add(side.getVector()) : result.getBlockPos();
      
      if(hasBeacon){ // Place beacon
         int blockCount = 0;
         for(int i = 0; i < blocks.size(); i++){
            NbtCompound blockType = blocks.getCompound(i);
            int count = blockType.getInt("count");
            blockCount+=count;
         }
         int tier = blocksToTier(blockCount);
         placePos = placePos.add(0,tier,0);
         
         if(hasSpace(world, placePos, tier) && world.getBlockState(placePos).canReplace(new ItemPlacementContext(playerEntity, hand, item, result)) && playerEntity instanceof ServerPlayerEntity player){
            placeBeacon(player, world, placePos, tier, blocks);
   
            magicNbt.put("blocks",new NbtList());
            magicNbt.putBoolean("beacon",false);
            
            NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
            loreList.set(4,NbtString.of("[{\"text\":\"Construct Status - \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Empty\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
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
            return false;
         }
         
         // Scan for support blocks
         List<Pair<BlockPos,BlockState>> baseBlocks = getBaseBlocks(world,placePos);
         int tier = blocksToTier(baseBlocks.size());
         // Remove support blocks and add them to NBT
         blocks = new NbtList();
         if(tier != 0){
            HashMap<Block,Integer> blockTypes = new HashMap<>();
            for(int i = 0; i < tiers[tier-1]; i++){
               BlockState blockState = baseBlocks.get(i).getRight();
               Block blockType = blockState.getBlock();
               if(blockTypes.containsKey(blockType)){
                  blockTypes.put(blockType,blockTypes.get(blockType)+1);
               }else{
                  blockTypes.put(blockType,1);
               }
               world.setBlockState(baseBlocks.get(i).getLeft(), Blocks.AIR.getDefaultState(), 3);
            }
            for(Map.Entry<Block, Integer> entry : blockTypes.entrySet()){
               NbtCompound blockType = new NbtCompound();
               blockType.putString("id",Registry.BLOCK.getId(entry.getKey()).toString());
               blockType.putInt("count",entry.getValue());
               blocks.add(blockType);
            }
         }
         magicNbt.put("blocks",blocks);
         magicNbt.putBoolean("beacon",true);
   
         world.setBlockState(placePos, Blocks.AIR.getDefaultState(), 3);
   
   
         if(world instanceof ServerWorld serverWorld){
            for(int i = 0; i <= tier; i++){
               int j = i;
               BlockPos finalPlacePos = placePos;
               Arcananovum.addTickTimerCallback(serverWorld, new GenericTimer(2*(i+1), new TimerTask() {
                  @Override
                  public void run(){
                     SoundUtils.playSound(world, finalPlacePos,SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, SoundCategory.PLAYERS,1,2f-(.3f*j));
                  }
               }));
            }
         }
         
         NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
         loreList.set(4,NbtString.of("[{\"text\":\"Construct Status - \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Ready - Tier "+tier+"\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      }
      
      return false;
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
               
               if(!(blockState.getMaterial().isReplaceable())){
                  //log("Block not replaceable at: "+blockPos);
                  return false;
               }
            }
         }
      
      }
      return true;
   }
   
   private void placeBeacon(ServerPlayerEntity player, World world, BlockPos pos, int tier, NbtList blockTypes){
      try{
         ArrayList<BlockState> blocks = new ArrayList<>();
         
         for(int i = 0; i < blockTypes.size(); i++){
            NbtCompound blockType = blockTypes.getCompound(i);
            int count = blockType.getInt("count");
            String id = blockType.getString("id");
            Block block = Registry.BLOCK.getOrEmpty(new Identifier(id)).orElse(null);
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
         
         player.teleport(pos.getX()+.5,pos.getY()+2,pos.getZ()+.5);
         PLAYER_DATA.get(player).addXP(index); // Add xp
   
         
         for(int i = 0; i <= tier; i++){
            int j = i;
            Arcananovum.addTickTimerCallback(player.getWorld(), new GenericTimer(2*(i+1), new TimerTask() {
               @Override
               public void run(){
                  SoundUtils.playSound(world,pos,SoundEvents.ENTITY_IRON_GOLEM_REPAIR, SoundCategory.PLAYERS,1,.8f+(.2f*j));
               }
            }));
         }
         PLAYER_DATA.get(player).addXP(10); // Add xp
   
         if(blockTypes.size() == 1 && blockTypes.getCompound(0).getInt("count") >= 164){
            BlockState blockType = blocks.get(0);
            if(blockType.isOf(Blocks.DIAMOND_BLOCK)){
               ArcanaAchievements.grant(player,"bejeweled");
            }else if(blockType.isOf(Blocks.EMERALD_BLOCK)){
               ArcanaAchievements.grant(player,"art_of_the_deal");
            }else if(blockType.isOf(Blocks.GOLD_BLOCK)){
               ArcanaAchievements.grant(player,"acquisition_rules");
            }else if(blockType.isOf(Blocks.NETHERITE_BLOCK)){
               ArcanaAchievements.grant(player,"clinically_insane");
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public static int[] tiers = {9,34,83,164};
   public static int blocksToTier(int blocks){
      for(int i=0; i<tiers.length; i++){
         if(blocks >= tiers[i]){
            continue;
         }else if(blocks < tiers[i]){
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
      MagicItemIngredient m = new MagicItemIngredient(Items.BEACON,1,null);
   
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
}
