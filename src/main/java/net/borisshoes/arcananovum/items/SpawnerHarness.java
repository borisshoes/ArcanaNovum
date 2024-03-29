package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class SpawnerHarness extends MagicItem {
   
   private static final String FULL_TXT = "item/spawner_harness";
   private static final String EMPTY_TXT = "item/spawner_harness_empty";
   
   public SpawnerHarness(){
      id = "spawner_harness";
      name = "Spawner Harness";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS, ArcaneTome.TomeFilter.BLOCKS};
      itemVersion = 1;
      vanillaItem = Items.SPAWNER;
      item = new SpawnerHarnessItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,FULL_TXT));
      models.add(new Pair<>(vanillaItem,EMPTY_TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Spawner Harness\",\"italic\":false,\"color\":\"dark_aqua\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags", 255);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      addMagicNbt(tag);
      tag.getCompound("arcananovum").put("spawner",new NbtCompound());
      stack.setNbt(tag);
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"While \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"silk touch\",\"color\":\"light_purple\"},{\"text\":\" fails to provide adequate finesse to obtain \"},{\"text\":\"spawners\",\"color\":\"dark_aqua\"},{\"text\":\",\",\"color\":\"dark_green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"through \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"magical enhancement\",\"color\":\"light_purple\"},{\"text\":\" this harness should suffice.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" on a \",\"color\":\"dark_green\"},{\"text\":\"mob spawner\",\"color\":\"dark_aqua\"},{\"text\":\" to obtain it as an \",\"color\":\"dark_green\"},{\"text\":\"item\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"dark_green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      
      if(itemStack != null){
         NbtCompound itemNbt = itemStack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         NbtCompound spawnerData = magicNbt.getCompound("spawner");
         if(spawnerData.contains("SpawnData")){
            NbtCompound spawnData = spawnerData.getCompound("SpawnData");
            NbtCompound entity = spawnData.getCompound("entity");
            if(entity.isEmpty()){
               loreList.add(NbtString.of("[{\"text\":\"Type - Empty\",\"italic\":false,\"color\":\"dark_aqua\"}]"));
            }else{
               String entityTypeId = entity.getString("id");
               String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
               loreList.add(NbtString.of("[{\"text\":\"Type - "+entityTypeName+"\",\"italic\":false,\"color\":\"dark_aqua\"}]"));
            }
         }else{
            loreList.add(NbtString.of("[{\"text\":\"Type - Uncaptured\",\"italic\":false,\"color\":\"dark_aqua\"}]"));
         }
      }else{
         loreList.add(NbtString.of("[{\"text\":\"Type - Uncaptured\",\"italic\":false,\"color\":\"dark_aqua\"}]"));
      }
      
      
      return loreList;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtCompound spawnerNbt = magicTag.getCompound("spawner").copy();
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").put("spawner",spawnerNbt);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   private void giveScrap(PlayerEntity player){
      ItemStack stack = new ItemStack(Items.NETHERITE_SCRAP);
      int reduction = (int) ArcanaNovum.config.getValue("ingredientReduction");
      int scrapCost = (int) Math.ceil(4.0 / reduction) * 4;
      stack.setCount(scrapCost/2);
      MiscUtils.giveStacks(player,stack);
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
      return new MagicItemRecipe(ingredients,new ForgeRequirement().withEnchanter());
   }
   
   public class SpawnerHarnessItem extends MagicPolymerItem {
      public SpawnerHarnessItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return ArcanaRegistry.MODELS.get(FULL_TXT).value();
         NbtCompound spawnerData = itemStack.getNbt().getCompound("arcananovum").getCompound("spawner");
         boolean hasSpawner = spawnerData.contains("SpawnData");
         return hasSpawner ? ArcanaRegistry.MODELS.get(FULL_TXT).value() : ArcanaRegistry.MODELS.get(EMPTY_TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         World world = context.getWorld();
         PlayerEntity player = context.getPlayer();
         try{
            ItemStack stack = context.getStack();
            NbtCompound itemNbt = stack.getNbt();
            NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
            NbtCompound spawnerData = magicNbt.getCompound("spawner");
            
            if(spawnerData.contains("SpawnData")){ // Has spawner, try to place
               Direction side = context.getSide();
               BlockPos placePos = context.getBlockPos().add(side.getVector());
               if(world.getBlockState(placePos).isAir()){
                  BlockEntity blockEntity;
                  world.setBlockState(placePos,Blocks.SPAWNER.getDefaultState(), Block.NOTIFY_ALL);
                  if ((blockEntity = world.getBlockEntity(placePos)) != null) {
                     blockEntity.readNbt(spawnerData);
                  }
                  
                  int reinforceLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REINFORCED_CHASSIS.id));
                  double breakChance = new double[]{.15,.13,.11,.09,.07,0}[reinforceLvl];
                  if(Math.random() > breakChance){ // Chance of the harness breaking after use
                     player.sendMessage(Text.literal("The harness successfully places the spawner.").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                     SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.BLOCK_CHAIN_PLACE, 1,.1f);
                     magicNbt.put("spawner",new NbtCompound());
                     buildItemLore(stack,player.getServer());
                  }else{
                     boolean scrap = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SALVAGEABLE_FRAME.id)) > 0;
                     player.sendMessage(Text.literal("The harness shatters upon placing the spawner.").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                     SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.ITEM_SHIELD_BREAK, 1,.5f);
                     stack.decrement(stack.getCount());
                     stack.setNbt(new NbtCompound());
                     if(scrap) giveScrap(player);
                  }
                  PLAYER_DATA.get(player).addXP((int) Math.max(0,20000*breakChance)); // Add xp
                  return ActionResult.SUCCESS;
               }else{
                  player.sendMessage(Text.literal("The harness cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               }
            }else if(world.getBlockState(context.getBlockPos()).getBlock() == Blocks.SPAWNER && world.getBlockEntity(context.getBlockPos()) instanceof MobSpawnerBlockEntity){
               MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) world.getBlockEntity(context.getBlockPos());
               NbtCompound spawnerNbt = spawner.createNbt();
               Entity renderedEntity = spawner.getLogic().getRenderedEntity(world,context.getBlockPos());
               if(renderedEntity != null){
                  String entityTypeId = EntityType.getId(renderedEntity.getType()).toString();
                  String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
                  player.sendMessage(Text.literal("The harness captures the "+entityTypeName+" spawner.").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                  if(entityTypeId.equals("minecraft:silverfish")) ArcanaAchievements.grant((ServerPlayerEntity) player,ArcanaAchievements.FINALLY_USEFUL.id);
               }
               
               magicNbt.put("spawner",spawnerNbt);
               world.breakBlock(context.getBlockPos(),false);
               
               SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.BLOCK_CHAIN_BREAK, 1,.1f);
               buildItemLore(stack,player.getServer());
               
               return ActionResult.SUCCESS;
            }
         }catch (Exception e){
            e.printStackTrace();
         }
         return ActionResult.PASS;
      }
   }
}
