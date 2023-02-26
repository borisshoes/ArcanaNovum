package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class SpawnerHarness extends MagicItem implements UsableItem {
   public SpawnerHarness(){
      id = "spawner_harness";
      name = "Spawner Harness";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS, ArcaneTome.TomeFilter.BLOCKS};
      
      ItemStack item = new ItemStack(Items.SPAWNER);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Spawner Harness\",\"italic\":false,\"color\":\"dark_aqua\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"While \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"silk touch\",\"color\":\"light_purple\"},{\"text\":\" fails to provide adequate finesse to obtain \"},{\"text\":\"spawners\",\"color\":\"dark_aqua\"},{\"text\":\",\",\"color\":\"dark_green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"through \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"magical enhancement\",\"color\":\"light_purple\"},{\"text\":\" this harness should suffice.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" on a \",\"color\":\"dark_green\"},{\"text\":\"mob spawner\",\"color\":\"dark_aqua\"},{\"text\":\" to obtain it as an \",\"color\":\"dark_green\"},{\"text\":\"item\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"dark_green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Type - Uncaptured\",\"italic\":false,\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic\",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags",127);
   
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.put("spawner",new NbtCompound());
      prefNBT = tag;
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtCompound spawnerNbt = magicTag.getCompound("spawner").copy();
      String loreData = itemNbt.getCompound("display").getList("Lore", NbtElement.STRING_TYPE).getString(4);
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").put("spawner",spawnerNbt);
      newTag.getCompound("display").getList("Lore", NbtElement.STRING_TYPE).set(4,NbtString.of(loreData));
      stack.setNbt(newTag);
      return stack;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity player, World world, Hand hand, BlockHitResult result){
      try{
         ItemStack item = player.getStackInHand(hand);
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         NbtCompound spawnerData = magicNbt.getCompound("spawner");
   
         if(spawnerData.contains("SpawnData")){ // Has spawner, try to place
            Direction side = result.getSide();
            BlockPos placePos = result.getBlockPos().add(side.getVector());
            if(world.getBlockState(placePos).isAir()){
               BlockEntity blockEntity;
               world.setBlockState(placePos,Blocks.SPAWNER.getDefaultState(), Block.NOTIFY_ALL);
               if ((blockEntity = world.getBlockEntity(placePos)) != null) {
                  blockEntity.readNbt(spawnerData);
               }
   
               int reinforceLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(item,"reinforced_chassis"));
               double breakChance = new double[]{.15,.13,.11,.09,.07,0}[reinforceLvl];
               if(Math.random() > breakChance){ // Chance of the harness breaking after use
                  NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
                  loreList.set(4,NbtString.of("[{\"text\":\"Type - Uncaptured\",\"italic\":false,\"color\":\"dark_aqua\"}]"));
                  player.sendMessage(Text.literal("The harness successfully places the spawner.").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.BLOCK_CHAIN_PLACE, 1,.1f);
                  magicNbt.put("spawner",new NbtCompound());
               }else{
                  int scrapLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(item,"salvageable_frame"));
                  player.sendMessage(Text.literal("The harness shatters upon placing the spawner.").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.ITEM_SHIELD_BREAK, 1,.5f);
                  item.decrement(item.getCount());
                  item.setNbt(new NbtCompound());
                  if(scrapLvl > 0) giveScrap(player,scrapLvl);
               }
               PLAYER_DATA.get(player).addXP((int) Math.max(0,20000*breakChance)); // Add xp
            }else{
               player.sendMessage(Text.literal("The harness cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }
         }else if(world.getBlockState(result.getBlockPos()).getBlock() == Blocks.SPAWNER && world.getBlockEntity(result.getBlockPos()) instanceof MobSpawnerBlockEntity){
            MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) world.getBlockEntity(result.getBlockPos());
            NbtCompound spawnerNbt = spawner.createNbt();
            String entityTypeId = EntityType.getId(spawner.getLogic().getRenderedEntity(world,world.getRandom(),result.getBlockPos()).getType()).toString();
            String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
            
            magicNbt.put("spawner",spawnerNbt);
            world.breakBlock(result.getBlockPos(),false);
            
            NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
            loreList.set(4,NbtString.of("[{\"text\":\"Type - "+entityTypeName+"\",\"italic\":false,\"color\":\"dark_aqua\"}]"));
            player.sendMessage(Text.literal("The harness captures the "+entityTypeName+" spawner.").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.BLOCK_CHAIN_BREAK, 1,.1f);
            
            if(entityTypeId.equals("minecraft:silverfish")) ArcanaAchievements.grant((ServerPlayerEntity) player,"finally_useful");
         }
      }catch (Exception e){
      
      }
      return false;
   }
   
   private void giveScrap(PlayerEntity player, int level){
      int scrapNum = (int)(Math.random() * (level*3+1) + level);
      
      ItemStack stack = new ItemStack(Items.NETHERITE_SCRAP);
      stack.setCount(scrapNum);
      if(!stack.isEmpty()){
      
         ItemEntity itemEntity;
         boolean bl = player.getInventory().insertStack(stack);
         if (!bl || !stack.isEmpty()) {
            itemEntity = player.dropItem(stack, false);
            if (itemEntity == null) return;
            itemEntity.resetPickupDelay();
            itemEntity.setOwner(player.getUuid());
            return;
         }
         stack.setCount(1);
         itemEntity = player.dropItem(stack, false);
         if (itemEntity != null) {
            itemEntity.setDespawnImmediately();
         }
      }
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Spawner Harness\\n\\nRarity: Exotic\\n\\nSpawners have always been one of the few blocks that have beyond the reach of the silk touch enchantment.\\nPerhaps I can enhance the enchant a bit further by giving the magic a Harness\"}");
      list.add("{\"text\":\"   Spawner Harness\\n\\nto channel additional Arcana to.\\n\\nThe Harness itself has to be incredibly durable to withstand the Arcana driving the enchant into overdrive, however even with my best efforts the Harness can break after use.\"}");
      list.add("{\"text\":\"   Spawner Harness\\n\\nRight click on a spawner with the Harness to capture the spawner. \\n\\nThe Harness can then place the spawner elsewhere in the world with a 15% chance of breaking after use.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.BLAZE_POWDER,64,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.CHAIN,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.IRON_BARS,64,null);
      ItemStack book = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.SILK_TOUCH,1));
      MagicItemIngredient s = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,book.getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {e,p,s,p,e},
            {p,c,b,c,p},
            {s,b,n,b,s},
            {p,c,b,c,p},
            {e,p,s,p,e}};
      return new MagicItemRecipe(ingredients);
   }
}
