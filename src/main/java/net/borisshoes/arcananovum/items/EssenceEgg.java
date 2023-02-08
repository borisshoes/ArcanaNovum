package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.core.AttackingItem;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.recipes.SoulstoneIngredient;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class EssenceEgg extends MagicItem implements UsableItem, AttackingItem {
   public EssenceEgg(){
      id = "essence_egg";
      name = "Essence Egg";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ITEMS};
      
      ItemStack item = new ItemStack(Items.GHAST_SPAWN_EGG);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Essence Egg\",\"italic\":false,\"bold\":true,\"color\":\"aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Harness the power of a filled \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"Soulstone\",\"color\":\"dark_red\"},{\"text\":\"...\",\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"With enough \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"souls\",\"color\":\"dark_red\"},{\"text\":\" a new form can be \"},{\"text\":\"spawned\",\"color\":\"dark_aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Spawns\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" a mob of the \",\"color\":\"light_purple\"},{\"text\":\"attuned type\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Unattuned\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"0 Uses Left\",\"italic\":false,\"color\":\"gray\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered\",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.putString("type","unattuned");
      magicTag.putInt("uses",0);
      prefNBT = tag;
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      int uses = magicTag.getInt("uses");
      String type = magicTag.getString("type");
      NbtCompound newTag = super.updateItem(stack, server).getNbt();
      newTag.getCompound("arcananovum").putInt("uses",uses);
      newTag.getCompound("arcananovum").putString("type",type);
      stack.setNbt(newTag);
      setType(stack,type);
      setUses(stack,uses);
      return stack;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      if(playerEntity.isCreative()){
         ItemStack item = playerEntity.getStackInHand(hand);
         if(!getType(item).equals("unattuned"))
            addUses(item,1);
      }
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      try{
         ItemStack item = playerEntity.getStackInHand(hand);
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      
         if(!getType(item).equals("unattuned")){
            // Check for use on spawner
            BlockPos blockPos = result.getBlockPos();
            BlockEntity blockEntity;
            BlockState blockState = world.getBlockState(blockPos);
            if(blockState.isOf(Blocks.SPAWNER) && (blockEntity = world.getBlockEntity(blockPos)) instanceof MobSpawnerBlockEntity){
               int captiveLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"willing_captive"));
               if(getUses(item) >= 5-captiveLevel){
                  MobSpawnerBlockEntity mobSpawnerBlockEntity = (MobSpawnerBlockEntity)blockEntity;
                  MobSpawnerLogic mobSpawnerLogic = mobSpawnerBlockEntity.getLogic();
                  EntityType<?> entityType = EntityType.get(getType(item)).get();
                  mobSpawnerBlockEntity.setEntityType(entityType, world.getRandom());
                  blockEntity.markDirty();
                  world.updateListeners(blockPos, blockState, blockState, Block.NOTIFY_ALL);
                  
                  addUses(item, -5+captiveLevel);
                  if(playerEntity instanceof ServerPlayerEntity player){
                     player.sendMessage(Text.translatable("The Spawner Assumes the Essence of "+EntityType.get(getType(item)).get().getName().getString()).formatted(Formatting.DARK_AQUA, Formatting.ITALIC), true);
                     SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 1, .7f);
                     PLAYER_DATA.get(playerEntity).addXP(2500); // Add xp
                     ArcanaAchievements.grant(player,"soul_conversion");
                  }
               }
            }else{
               int splitLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"soul_split"));
               int efficiencyLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"determined_spirit"));
               if(getUses(item) > 0){
                  ServerWorld serverWorld = playerEntity.getServer().getWorld(world.getRegistryKey());
                  Vec3d summonPos = result.getPos().add(0,0.5,0);
      
                  NbtCompound nbtCompound = new NbtCompound();
                  nbtCompound.putString("id", getType(item));
                  int spawns = Math.random() >= 0.1*splitLevel ? 1 : 2;
                  
                  for(int i = 0; i < spawns; i++){
                     Entity newEntity = EntityType.loadEntityWithPassengers(nbtCompound, serverWorld, entity -> {
                        entity.refreshPositionAndAngles(summonPos.getX(), summonPos.getY(), summonPos.getZ(), entity.getYaw(), entity.getPitch());
                        return entity;
                     });
                     if(newEntity instanceof MobEntity){
                        ((MobEntity) newEntity).initialize(serverWorld, serverWorld.getLocalDifficulty(newEntity.getBlockPos()), SpawnReason.SPAWN_EGG, null, null);
                     }
                     serverWorld.spawnNewEntityAndPassengers(newEntity);
                  }
   
                  if(Math.random() >= 0.1*efficiencyLevel){
                     addUses(item,-1);
                  }
                  if(playerEntity instanceof ServerPlayerEntity player){
                     SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_FIRECHARGE_USE, 1, 1.5f);
                     PLAYER_DATA.get(playerEntity).addXP(500); // Add xp
                     ArcanaAchievements.progress(player,"soul_for_soul",1);
                  }
               }
            }
         }
      
         return false;
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static void setType(ItemStack item, String entityId){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
      String entityTypeName = EntityType.get(entityId).get().getName().getString();
      if(entityId.equals("unattuned")){
         loreList.set(4,NbtString.of("[{\"text\":\"Unattuned\",\"italic\":false,\"color\":\"light_purple\"}]"));
      }else{
         loreList.set(4,NbtString.of("[{\"text\":\"Attuned - "+entityTypeName+"\",\"italic\":false,\"color\":\"light_purple\"}]"));
      }
      magicNbt.putString("type",entityId);
   }
   
   public static String getType(ItemStack item){
      NbtCompound magicNbt = item.getNbt().getCompound("arcananovum");
      return magicNbt.getString("type");
   }
   
   public static int getUses(ItemStack item){
      NbtCompound magicNbt = item.getNbt().getCompound("arcananovum");
      return magicNbt.getInt("uses");
   }
   
   public static void setUses(ItemStack item, int uses){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      uses = MathHelper.clamp(uses,0,500);
      
      if(uses <= 0){
         item.decrement(item.getCount());
         item.setNbt(new NbtCompound());
      }else{
         magicNbt.putInt("uses", uses);
      
         NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
         loreList.set(5,NbtString.of("[{\"text\":\"Uses "+uses+" Left\",\"italic\":false,\"color\":\"gray\"}]"));
      }
   }
   
   public static void addUses(ItemStack item, int uses){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int curUses = magicNbt.getInt("uses");
      int newUses = MathHelper.clamp(uses+curUses,0,500);
      if(newUses <= 0){
         item.decrement(item.getCount());
         item.setNbt(new NbtCompound());
      }else{
         magicNbt.putInt("uses", newUses);
   
         NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
         loreList.set(5,NbtString.of("[{\"text\":\"Uses "+newUses+" Left\",\"italic\":false,\"color\":\"gray\"}]"));
      }
   }
   
   @Override
   public boolean attackEntity(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult){
      if(playerEntity.isCreative()){
         ItemStack item = playerEntity.getStackInHand(hand);
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         String type = magicNbt.getString("type");
   
         if(type.equals("unattuned") && entity instanceof MobEntity attackedEntity && playerEntity instanceof ServerPlayerEntity player && !(entity instanceof EnderDragonEntity || entity instanceof WitherEntity)){
            String entityTypeId = EntityType.getId(attackedEntity.getType()).toString();
            String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
      
            setType(item,entityTypeId);
            player.sendMessage(Text.translatable("The Essence Egg attunes to the essence of "+entityTypeName).formatted(Formatting.DARK_RED,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1,.5f);
         }
      }
      return true;
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      // Souls n stuff
      ItemStack soulstoneStack = inv.getStack(12); // Should be the Soulstone
      ItemStack newMagicItem = null;
      if(MagicItemUtils.identifyItem(soulstoneStack) instanceof Soulstone){
         int uses = (Soulstone.getSouls(soulstoneStack) / Soulstone.tiers[0]);
         String essenceType = Soulstone.getType(soulstoneStack);
      
         newMagicItem = getNewItem();
         setType(newMagicItem,essenceType);
         setUses(newMagicItem,uses);
      }
      return newMagicItem;
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Essence Egg\\n\\nRarity: Empowered\\n\\nAs making Soulstones has taught me, keeping souls imprisoned is hard.\\nMaking so they can be released controllably, is even harder, thankfully I can build off of a Soulstone's solid foundation.\"}");
      list.add("{\"text\":\"      Essence Egg\\n\\nThis 'Egg' should take the souls from a Soulstone and keep them captive just long enough for me to release them into a new form. \\nAlthough it takes 25 souls to make a new body for a single soul to inhabit.\"}");
      list.add("{\"text\":\"      Essence Egg\\n\\nThe Essence Egg can be used one at a time to spawn a mob, or 5 uses (125 souls) can be used on a spawner to change its attunement to the type of essence contained in the Egg.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      SoulstoneIngredient t = new SoulstoneIngredient(Soulstone.tiers[0],true,false, false,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.CRYING_OBSIDIAN,32,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.SOUL_SAND,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.IRON_BARS,64,null);
      
      MagicItemIngredient[][] ingredients = {
            {o,p,b,p,o},
            {p,b,s,b,p},
            {b,s,t,s,b},
            {p,b,s,b,p},
            {o,p,b,p,o}};
      return new MagicItemRecipe(ingredients);
   }
}
