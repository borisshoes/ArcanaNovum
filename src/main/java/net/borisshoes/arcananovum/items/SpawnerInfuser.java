package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.core.BlockItem;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;

public class SpawnerInfuser extends MagicItem implements UsableItem,BlockItem {
   
   public static final int[] pointsFromTier = {0,16,32,64,128,256,512,1024};
   public static final Item pointsItem = Items.NETHER_STAR;
   
   public SpawnerInfuser(){
      id = "spawner_infuser";
      name = "Spawner Infuser";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.BLOCKS};
      
      ItemStack item = new ItemStack(Items.SCULK_SHRIEKER);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Spawner Infuser\",\"italic\":false,\"color\":\"dark_green\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Spawners\",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\" have their \",\"color\":\"dark_aqua\"},{\"text\":\"natural limit\",\"color\":\"yellow\"},{\"text\":\", \",\"color\":\"dark_aqua\"},{\"text\":\"Arcana\",\"color\":\"dark_purple\"},{\"text\":\" can now push it \",\"color\":\"dark_aqua\"},{\"text\":\"further\",\"color\":\"yellow\",\"italic\":true},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The Infuser\",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\" is to be placed two blocks \",\"color\":\"dark_aqua\"},{\"text\":\"below \",\"color\":\"yellow\"},{\"text\":\"a \",\"color\":\"dark_aqua\"},{\"text\":\"spawner\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When given a \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"soulstone\",\"color\":\"dark_red\"},{\"text\":\" of the \"},{\"text\":\"same type\",\"color\":\"yellow\"},{\"text\":\" as the \"},{\"text\":\"spawner \",\"color\":\"dark_green\"},{\"text\":\"it can be \"},{\"text\":\"activated\",\"color\":\"green\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"A \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"redstone \",\"color\":\"red\"},{\"text\":\"signal \"},{\"text\":\"activates \",\"color\":\"green\"},{\"text\":\"the \"},{\"text\":\"infuser\",\"color\":\"dark_green\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\" the \",\"color\":\"dark_aqua\"},{\"text\":\"Infuser \"},{\"text\":\"to \",\"color\":\"dark_aqua\"},{\"text\":\"configure \",\"color\":\"green\"},{\"text\":\"the \",\"color\":\"dark_aqua\"},{\"text\":\"infusion \"},{\"text\":\"type\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      //setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
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
         placeInfuser(player, world, item, placePos);
      }else{
         playerEntity.sendMessage(Text.literal("The Infuser cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
      }
      return false;
   }
   
   private void placeInfuser(ServerPlayerEntity player, World world, ItemStack item, BlockPos pos){
      try{
         MagicBlock infuserBlock = new MagicBlock(pos);
         NbtCompound infuserData = new NbtCompound();
         infuserData.putString("UUID",getUUID(item));
         infuserData.putString("id",this.id);
         
         infuserData.putBoolean("active",false);
         infuserData.put("soulstone",new NbtCompound());
         infuserData.putInt("points",0);
         infuserData.putInt("SpentPoints",0);
   
         NbtCompound stats = new NbtCompound();
         stats.putShort("MinSpawnDelay", (short)200);
         stats.putShort("MaxSpawnDelay", (short)800);
         stats.putShort("SpawnCount", (short)4);
         stats.putShort("MaxNearbyEntities", (short)6);
         stats.putShort("RequiredPlayerRange", (short)16);
         stats.putShort("SpawnRange", (short)4);
         infuserData.put("stats",stats);
         
         infuserBlock.setData(infuserData);
         world.setBlockState(pos, Blocks.SCULK_SHRIEKER.getDefaultState(), Block.NOTIFY_ALL);
         MAGIC_BLOCK_LIST.get(world).addBlock(infuserBlock);
         
         SoundUtils.soulSounds(player.getWorld(),pos,5,30);
         SoundUtils.playSound(world,pos,SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK,SoundCategory.BLOCKS,1,.6f);
         player.sendMessage(Text.literal("The Infuser makes a most unsettling sound...").formatted(Formatting.DARK_GREEN),true);
         item.decrement(item.getCount());
         item.setNbt(new NbtCompound());
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public List<ItemStack> dropFromBreak(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity, NbtCompound blockData){
      List<ItemStack> drops = new ArrayList<>();
      String uuid = blockData.getString("UUID");
      ItemStack drop = getPrefItem();
      drop.getNbt().getCompound("arcananovum").putString("UUID",uuid);
      drops.add(drop);
      
      int points = blockData.getInt("points");
      if(points > 0){
         while(points > 64){
            ItemStack dropItem = new ItemStack(pointsItem);
            dropItem.setCount(64);
            drops.add(dropItem.copy());
            points -= 64;
         }
         ItemStack dropItem = new ItemStack(pointsItem);
         dropItem.setCount(points);
         drops.add(dropItem.copy());
      }
      
      NbtCompound soulstone = blockData.getCompound("soulstone");
      if(!soulstone.isEmpty()){
         ItemStack stone = ItemStack.fromNbt(soulstone);
         drops.add(stone.copy());
      }
      
      return drops;
   }
   
   public static void tickActiveInfuser(World world, BlockPos pos, NbtCompound blockData, MobSpawnerBlockEntity blockEntity){
      MobSpawnerLogic logic = blockEntity.getLogic();
      NbtCompound savedLogic = logic.writeNbt(new NbtCompound()); // Save default data
      NbtCompound newLogic = blockData.getCompound("stats").copy(); // Get data from infuser
   
      newLogic.put("SpawnData",savedLogic.get("SpawnData").copy()); // Copy some default data into new data
      newLogic.put("SpawnPotentials",savedLogic.get("SpawnPotentials").copy());
      short oldDelay = savedLogic.getShort("Delay");
      short maxDelay = newLogic.getShort("MaxSpawnDelay");
      newLogic.putShort("Delay", (short) Math.min(oldDelay,maxDelay));
      
      logic.readNbt(world,pos,newLogic); // Inject new data
      logic.serverTick((ServerWorld)world, pos); // Tick with new data
      short newDelay = logic.writeNbt(new NbtCompound()).getShort("Delay");
      savedLogic.putShort("Delay",newDelay); // Extract new delay and put in saved data
      logic.readNbt(world,pos,savedLogic); // Return saved default data with new delay
   }
   
   //TODO: Make Recipe
   private MagicItemRecipe makeRecipe(){
      return null;
   }
   
   //TODO: Make Lore
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"TODO\"}");
      return list;
   }
}
