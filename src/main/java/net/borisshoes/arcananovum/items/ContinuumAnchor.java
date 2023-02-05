package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.core.BlockItem;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;

// Credit to xZarex for some of the Chunk Loading mixin code
public class ContinuumAnchor extends MagicItem implements UsableItem, BlockItem {
   public static final ChunkTicketType<ChunkPos> TICKET_TYPE = ChunkTicketType.create("ArcanaNovum_Anchor", Comparator.comparingLong(ChunkPos::toLong));
   
   public ContinuumAnchor(){
      id = "continuum_anchor";
      name = "Continuum Anchor";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.BLOCKS};
      itemVersion = 1;
      
      ItemStack item = new ItemStack(Items.RESPAWN_ANCHOR);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Continuum Anchor\",\"italic\":false,\"bold\":true,\"color\":\"dark_blue\"}]");
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"anchor\",\"color\":\"dark_blue\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"has the extraordinary ability to manipulate \"},{\"text\":\"spacetime\",\"color\":\"dark_purple\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It just needs the \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"right type\",\"color\":\"gray\"},{\"text\":\" of\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"fuel\",\"color\":\"gold\"},{\"text\":\"...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"continuum anchor\",\"color\":\"dark_blue\"},{\"text\":\" consumes \"},{\"text\":\"exotic matter\",\"color\":\"blue\"},{\"text\":\" to \"},{\"text\":\"chunk load\",\"color\":\"aqua\"},{\"text\":\" a \"},{\"text\":\"5x5 area\",\"color\":\"dark_green\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"area \",\"color\":\"dark_green\"},{\"text\":\"also receives \"},{\"text\":\"random ticks\",\"color\":\"blue\"},{\"text\":\" and keeps \"},{\"text\":\"mobs \",\"color\":\"gray\"},{\"text\":\"loaded\",\"color\":\"aqua\"},{\"text\":\".\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary\",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   public static void addChunk(ServerWorld world, ChunkPos chunk){
      world.getChunkManager().threadedAnvilChunkStorage.getTicketManager().addTicket(ContinuumAnchor.TICKET_TYPE,chunk,2,chunk);
   }
   
   public static void removeChunk(ServerWorld world, ChunkPos chunk){
      world.getChunkManager().threadedAnvilChunkStorage.getTicketManager().removeTicket(ContinuumAnchor.TICKET_TYPE,chunk,2,chunk);
   }
   
   public static List<ChunkPos> getLoadedChunks(ServerWorld serverWorld){
      ArrayList<ChunkPos> chunks = new ArrayList<>();
      List<MagicBlock> blocks = MAGIC_BLOCK_LIST.get(serverWorld).getBlocks();
      for(MagicBlock magicBlock : blocks){
         BlockPos pos = magicBlock.getPos();
         ChunkPos chunkPos = new ChunkPos(pos);
      
         NbtCompound blockData = magicBlock.getData();
         if(blockData.contains("id")){
            String id = blockData.getString("id");
            if(!blockData.contains("UUID")){
               blockData.putString("UUID", UUID.randomUUID().toString());
            }
         
            if(id.equals(MagicItems.CONTINUUM_ANCHOR.getId())){ // Continuum Anchor Tick
               boolean active = blockData.getBoolean("active");
               int range = blockData.getInt("range");
               if(active){
                  for(int i = -range; i <= range; i++){
                     for(int j = -range; j <= range; j++){
                        chunks.add(new ChunkPos(chunkPos.x+i,chunkPos.z+j));
                     }
                  }
               }
            }
         }
      }
      return chunks;
   }
   
   public static boolean isChunkLoaded(ServerWorld serverWorld, ChunkPos chunk){
      List<ChunkPos> chunks = getLoadedChunks(serverWorld);
      for(ChunkPos chunkPos : chunks){
         if(chunkPos.toLong() == chunk.toLong()) return true;
      }
      return false;
   }
   
   public static void initLoadedChunks(MinecraftServer minecraftServer){
      for(ServerWorld serverWorld : minecraftServer.getWorlds()){
         //log("Initializing chunks in "+serverWorld.getRegistryKey().getValue().toString());
         List<ChunkPos> chunks = getLoadedChunks(serverWorld);
         for(ChunkPos chunk : chunks){
            addChunk(serverWorld,chunk);
         }
      }
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
         placeAnchor(player, world, item, placePos);
      }else{
         playerEntity.sendMessage(Text.literal("The Anchor cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
      }
      return false;
   }
   
   private void placeAnchor(ServerPlayerEntity player, World world, ItemStack item, BlockPos pos){
      try{
         NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
         MagicBlock anchorBlock = new MagicBlock(pos);
         NbtCompound anchorData = new NbtCompound();
         anchorData.putString("UUID",getUUID(item));
         anchorData.putString("id",this.id);
         anchorData.putString("crafter",getCrafter(item));
         anchorData.putBoolean("synthetic",isSynthetic(item));
         anchorData.putBoolean("active",false);
         anchorData.putInt("fuel",0);
         anchorData.putInt("range",2);
         if(magicTag.contains("augments")) anchorData.put("augments",magicTag.getCompound("augments"));
         anchorBlock.setData(anchorData);
         world.setBlockState(pos, Blocks.RESPAWN_ANCHOR.getDefaultState(), Block.NOTIFY_ALL);
         MAGIC_BLOCK_LIST.get(world).addBlock(anchorBlock);
      
         player.sendMessage(Text.literal("Placing the Continuum Anchor sends a ripple across spacetime.").formatted(Formatting.DARK_BLUE),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT, 5,.8f);
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
      NbtCompound augmentsTag = blockData.contains("augments") ? blockData.getCompound("augments").copy() : null;
      ItemStack drop = addCrafter(getPrefItem(),blockData.getString("crafter"),blockData.getBoolean("synthetic"),world.getServer());
      NbtCompound magicTag = drop.getNbt().getCompound("arcananovum");
      if(augmentsTag != null) {
         magicTag.put("augments",augmentsTag);
         redoAugmentLore(drop);
      }
      magicTag.putString("UUID",uuid);
      drops.add(drop);
      
      int fuel = blockData.getInt("fuel");
      if(fuel > 0){
         ExoticMatter exoticMatter = (ExoticMatter) MagicItems.EXOTIC_MATTER;
         ItemStack fuelDrop = MagicItems.EXOTIC_MATTER.getNewItem();
         exoticMatter.setFuel(fuelDrop,fuel);
         drops.add(fuelDrop);
      }
      return drops;
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Continuum Anchor\\n\\nRarity: Legendary\\n\\nExotic Matter has given useful insight into warping spacetime. On top of being more practiced in constructing sturdy casings that can withstand the flow of Arcana, I have made additional efforts to \"}");
      list.add("{\"text\":\"   Continuum Anchor\\n\\nreinforce this chassis against dimensional shear.\\nBy combining all known techniques of manipulating dimensional energy I believe I can cause a section of space to be locked in time so that the world cannot be unloaded.\"}");
      list.add("{\"text\":\"   Continuum Anchor\\n\\nWhen fed with Exotic Matter the Anchor chunk loads a 5x5 chunk area with Mob Spawning & Random Ticks. The Anchor can be turned off with a redstone signal and its fuel can be removed by an empty hand. It can be refueled in use.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient s = new MagicItemIngredient(Items.NETHER_STAR,16,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient a = new MagicItemIngredient(Items.RESPAWN_ANCHOR,32,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      
      MagicItemIngredient[][] ingredients = {
            {o,p,n,p,o},
            {p,e,a,e,p},
            {n,a,s,a,n},
            {p,e,a,e,p},
            {o,p,n,p,o}};
      return new MagicItemRecipe(ingredients);
   }
}
