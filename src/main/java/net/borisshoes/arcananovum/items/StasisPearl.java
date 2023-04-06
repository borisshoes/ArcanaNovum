package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.core.EnergyItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.TickingItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_ENTITY_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class StasisPearl extends EnergyItem implements TickingItem, UsableItem {
   
   public StasisPearl(){
      id = "stasis_pearl";
      name = "Stasis Pearl";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS};
      initEnergy = 60;
   
      ItemStack item = new ItemStack(Items.ENDER_PEARL);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Stasis Pearl\",\"italic\":false,\"color\":\"blue\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Ender Pearl\",\"color\":\"dark_aqua\"},{\"text\":\" has the ability to \"},{\"text\":\"freeze \",\"color\":\"aqua\"},{\"text\":\"its passage through \"},{\"text\":\"time\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"frozen \",\"color\":\"aqua\"},{\"text\":\"the \"},{\"text\":\"pearl \",\"color\":\"dark_aqua\"},{\"text\":\"looks like its '\"},{\"text\":\"hanging\",\"color\":\"aqua\",\"italic\":true},{\"text\":\"' in the air.\",\"color\":\"gray\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"It requires the \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"flowing of time\",\"color\":\"blue\"},{\"text\":\" to \"},{\"text\":\"recharge \",\"color\":\"dark_aqua\"},{\"text\":\"it.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to \",\"color\":\"gray\"},{\"text\":\"freeze \",\"color\":\"aqua\"},{\"text\":\"the \",\"color\":\"gray\"},{\"text\":\"pearl \",\"color\":\"dark_aqua\"},{\"text\":\"in flight.\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click again\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to \",\"color\":\"gray\"},{\"text\":\"release \",\"color\":\"aqua\"},{\"text\":\"the \",\"color\":\"gray\"},{\"text\":\"pearl\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Charged - \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"100%\",\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.putString("pearlID","");
      magicTag.putBoolean("active",false);
      prefNBT = tag;
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 1 minute base recharge time
      return 60 - 10*Math.max(0, ArcanaAugments.getAugmentOnItem(item,"stasis_acceleration"));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      boolean active = magicTag.getBoolean("active");
      String pearlID = magicTag.getString("pearlID");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").putBoolean("active",active);
      newTag.getCompound("arcananovum").putString("pearlID",pearlID);
      stack.setNbt(newTag);
      redoLore(stack);
      return stack;
   }
   
   private void redoLore(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
      
      int charge = (getEnergy(stack)*100/getMaxEnergy(stack));
      String charging = charge == 100 ? "Charged" : "Charging";
      loreList.set(6,NbtString.of("[{\"text\":\""+charging+" - \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\""+charge+"%\",\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
   
      
   }
   
   private void swapItem(ServerPlayerEntity player, ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      boolean pearl = magicNbt.getBoolean("active");
      
      Item newItem = null;
      if(pearl && stack.isOf(Items.ENDER_PEARL)){
         newItem = Items.ENDER_EYE;
      }else if(!pearl && stack.isOf(Items.ENDER_EYE)){
         newItem = Items.ENDER_PEARL;
      }
      
      if(newItem != null){
         Inventory inv = player.getInventory();
         for(int i = 0; i < inv.size(); i++){
            ItemStack item = inv.getStack(i);
            if(ItemStack.areNbtEqual(item,stack)){
               ItemStack newStack = new ItemStack(newItem);
               newStack.setNbt(stack.getNbt());
               inv.setStack(i,newStack);
            }
         }
      }
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      String pearlID = magicNbt.getString("pearlID");
      boolean active = magicNbt.getBoolean("active");
      swapItem(player,item);
      
      boolean exists = false;
      if(!pearlID.isEmpty()){
         for(MagicEntity entity : MAGIC_ENTITY_LIST.get(world).getEntities()){
            if(entity.getUuid().equals(pearlID)){
               exists = true;
               break;
            }
         }
      }
      if(!exists){
         magicNbt.putString("pearlID","");
         magicNbt.putBoolean("active",false);
      }
      
      if(world.getServer().getTicks() % 20 == 0){
         addEnergy(item, 1); // Recharge
         redoLore(item);
      }
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack item = playerEntity.getStackInHand(hand);
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      String pearlID = magicNbt.getString("pearlID");
      boolean active = magicNbt.getBoolean("active");
      boolean canDelete = playerEntity.isSneaking() && Math.max(0, ArcanaAugments.getAugmentOnItem(item,"spatial_fold")) >= 1;
      
      try{
         if(pearlID.isEmpty()){ // Throw new pearl
            if(getEnergy(item) == getMaxEnergy(item)){
               SoundUtils.playSound(world,playerEntity.getBlockPos(),SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F);
               playerEntity.getItemCooldownManager().set(Items.ENDER_PEARL, 0);
               if (!world.isClient) {
                  EnderPearlEntity enderPearlEntity = new EnderPearlEntity(world, playerEntity);
                  enderPearlEntity.setItem(item);
                  enderPearlEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, 1.5F, 1.0F);
                  world.spawnEntity(enderPearlEntity);
                  String newPearlID = enderPearlEntity.getUuidAsString();
                  magicNbt.putString("pearlID",newPearlID);
      
                  NbtCompound pearlData = new NbtCompound();
                  pearlData.putString("UUID",getUUID(item));
                  pearlData.putString("id",this.id);
                  pearlData.putBoolean("alive",true);
                  pearlData.putBoolean("stasis",false);
                  if(magicNbt.contains("augments")) pearlData.put("augments",magicNbt.getCompound("augments"));
                  pearlData.putInt("keepAlive",12000); // 10 minute lifespan for a stasis pearl
                  pearlData.putString("player", playerEntity.getUuidAsString());
                  MagicEntity magicPearl = new MagicEntity(newPearlID,pearlData);
                  MAGIC_ENTITY_LIST.get(world).addEntity(magicPearl);
                  
                  setEnergy(item,0);
                  PLAYER_DATA.get(playerEntity).addXP(250);
               }
            }else{
               playerEntity.getItemCooldownManager().set(Items.ENDER_PEARL, 0);
               if(playerEntity instanceof ServerPlayerEntity player){
                  playerEntity.sendMessage(Text.literal("Pearl Recharging: "+(getEnergy(item)*100/getMaxEnergy(item))+"%").formatted(Formatting.BLUE),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
               }
            }
         }else if(canDelete){ // Delete current pearl
            for(MagicEntity entity : MAGIC_ENTITY_LIST.get(world).getEntities()){
               if(entity.getUuid().equals(pearlID) && world instanceof ServerWorld serverWorld){
                  Entity pearlEntity = serverWorld.getEntity(UUID.fromString(pearlID));
                  if(pearlEntity != null) pearlEntity.kill(); // Kill any flying pearl
                  MAGIC_ENTITY_LIST.get(world).removeEntity(entity); // remove from list
                  break;
               }
            }
            // Reset data
            magicNbt.putString("pearlID","");
            magicNbt.putBoolean("active",false);
            if(playerEntity instanceof ServerPlayerEntity player){
               playerEntity.sendMessage(Text.literal("Pearl Cancelled").formatted(Formatting.BLUE),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1, .5f);
            }
         }else if(active){ // Un-stasis to pearl
            for(MagicEntity entity : MAGIC_ENTITY_LIST.get(world).getEntities()){
               if(entity.getUuid().equals(pearlID) && world instanceof ServerWorld serverWorld){
                  NbtCompound pearlData = entity.getData();
                  NbtCompound pearlEntityData = pearlData.getCompound("pearlData");
                  Entity pearlEntity = EntityType.loadEntityWithPassengers(pearlEntityData,world, (entityx) -> {
                     entityx.refreshPositionAndAngles(entityx.getX(), entityx.getY(), entityx.getZ(), entityx.getYaw(), entityx.getPitch());
                     return !serverWorld.tryLoadEntity(entityx) ? null : entityx;
                  });
                  if(pearlEntity != null){
                     magicNbt.putBoolean("active", false);
                     pearlData.putBoolean("stasis",false);
                     if(pearlData.getInt("keepAlive") <= 6000 && playerEntity instanceof ServerPlayerEntity player) ArcanaAchievements.grant(player,"pearl_hang");
                     pearlData.remove("pearlData");
                     playerEntity.getItemCooldownManager().set(Items.ENDER_PEARL, 0);
                     return false;
                  }
               }
            }
            // If this is reached, something went wrong and the pearl needs to be reset
            magicNbt.putString("pearlID","");
            magicNbt.putBoolean("active",false);
         }else{ // Stasis the pearl
            for(MagicEntity entity : MAGIC_ENTITY_LIST.get(world).getEntities()){
               if(entity.getUuid().equals(pearlID) && world instanceof ServerWorld serverWorld){
                  NbtCompound pearlData = entity.getData();
                  pearlData.putBoolean("stasis",true);
                  Entity pearlEntity = serverWorld.getEntity(UUID.fromString(pearlID));
                  if(pearlEntity != null){
                     NbtCompound pearlEntityData = new NbtCompound();
                     pearlEntity.saveNbt(pearlEntityData);
                     pearlData.put("pearlData",pearlEntityData);
                     pearlEntity.kill();
                     magicNbt.putBoolean("active",true);
                     playerEntity.getItemCooldownManager().set(Items.ENDER_PEARL, 0);
                     return false;
                  }else{ // Entity wasn't found, remove from list
                     MAGIC_ENTITY_LIST.get(world).removeEntity(entity);
                  }
               }
            }
            // If this is reached, something went wrong and the pearl needs to be reset
            magicNbt.putString("pearlID","");
            magicNbt.putBoolean("active",false);
         }
      }catch(Exception e){
         e.printStackTrace();
      }
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      return true;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient c = new MagicItemIngredient(Items.END_CRYSTAL,32,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.ENDER_PEARL,16,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,1,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.TEMPORAL_MOMENT,1);
      
      MagicItemIngredient[][] ingredients = {
            {e,p,n,p,e},
            {p,c,s,c,p},
            {n,s,m,s,n},
            {p,c,s,c,p},
            {e,p,n,p,e}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Stasis Pearl\\n\\nRarity: Exotic\\n\\nSimilar to the Pearl of\\nRecall, except instead of freezing the Pearl during activation its frozen while in flight.\\nThe Pearl is highly volitile and can dematerialize if frozen for too long, or unloaded in flight.\\n\"}");
      list.add("{\"text\":\"      Stasis Pearl\\n\\nRight Click throws the pearl like normal.\\nRight Clicking while it is in flight puts it in stasis.\\nRight Clicking while frozen will remove it from stasis and continue moving.\\nLike the Recall Pearl, this one takes time to resync to the timeline.\"}");
      return list;
   }
}
