package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.core.BlockItem;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
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
      loreList.add(NbtString.of("[{\"text\":\"Spawners \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"have their \",\"color\":\"dark_aqua\"},{\"text\":\"natural limit\",\"color\":\"yellow\"},{\"text\":\", \",\"color\":\"dark_aqua\"},{\"text\":\"Arcana \",\"color\":\"dark_purple\"},{\"text\":\"can now push them \",\"color\":\"dark_aqua\"},{\"text\":\"further\",\"color\":\"yellow\",\"italic\":true},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Place \",\"italic\":false,\"color\":\"green\"},{\"text\":\"the \",\"color\":\"dark_aqua\"},{\"text\":\"Infuser \",\"color\":\"dark_green\"},{\"text\":\"two blocks \",\"color\":\"dark_aqua\"},{\"text\":\"below \",\"color\":\"yellow\"},{\"text\":\"a \",\"color\":\"dark_aqua\"},{\"text\":\"spawner\",\"color\":\"dark_green\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Infuser \",\"color\":\"dark_green\"},{\"text\":\"requires a \"},{\"text\":\"soulstone \",\"color\":\"dark_red\"},{\"text\":\"matching the \"},{\"text\":\"spawner \",\"color\":\"dark_green\"},{\"text\":\"type\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Infuser \",\"color\":\"dark_green\"},{\"text\":\"also requires \"},{\"text\":\"Nether Stars\",\"color\":\"aqua\"},{\"text\":\" to unlock \"},{\"text\":\"enhanced \",\"color\":\"yellow\"},{\"text\":\"infusions\",\"color\":\"dark_green\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Apply \",\"italic\":false,\"color\":\"green\"},{\"text\":\"a \",\"color\":\"dark_aqua\"},{\"text\":\"Redstone signal\",\"color\":\"red\"},{\"text\":\" to \",\"color\":\"dark_aqua\"},{\"text\":\"activate \"},{\"text\":\"the \",\"color\":\"dark_aqua\"},{\"text\":\"Infuser\",\"color\":\"dark_green\"},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"green\"},{\"text\":\" the \",\"color\":\"dark_aqua\"},{\"text\":\"Infuser \",\"color\":\"dark_green\"},{\"text\":\"to \",\"color\":\"dark_aqua\"},{\"text\":\"configure \",\"color\":\"yellow\"},{\"text\":\"its \",\"color\":\"dark_aqua\"},{\"text\":\"abilities\",\"color\":\"dark_green\"},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
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
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.ECHO_SHARD,16,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.SOUL_SAND,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.SCULK_SHRIEKER,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHER_STAR,8,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      MagicItemIngredient k = new MagicItemIngredient(Items.SCULK_CATALYST,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.SPAWNER_HARNESS,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {k,h,m,h,k},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Spawner Infuser\\n\\nRarity: Legendary\\n\\nOne of my most intricate and powerful creations to date.\\nThis behemouth exploits a fascinating organism from the Deep Dark called Sculk. It acts as if soulsand became alive. It grows and feeds \"}");
      list.add("{\"text\":\"   Spawner Infuser\\n\\nfrom souls. \\nBy combining the tech from one of my earlier works, the Spawner Infuser, I believe I can use Arcana to overload the innate magic that summons creatures.\\n\\nAll the Sculk mechanisms need are \"}");
      list.add("{\"text\":\"   Spawner Infuser\\n\\nsome souls, provided easily from a Soulstone, and some crystalline structure combined with a lot of energy. Nether Stars work as both a focusing crystal and a power source so that should do nicely.\\nA simple Redstone signal will activate it.\"}");
      list.add("{\"text\":\"   Spawner Infuser\\n\\nAll aspects of the spawner can now be configured from range, to spawn delay, and a whole lot more.\\n\\nAs long as the Sculk has an adequate base of souls from the Soulstone, more and more upgrades can be added.\"}");
      return list;
   }
}
