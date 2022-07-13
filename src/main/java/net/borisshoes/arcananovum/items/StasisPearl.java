package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.MagicBlocksComponentInitializer.MAGIC_BLOCK_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.MagicEntityComponentInitializer.MAGIC_ENTITY_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class StasisPearl extends EnergyItem implements TickingItem, UsableItem{
   
   public StasisPearl(){
      id = "stasis_pearl";
      name = "Stasis Pearl";
      rarity = MagicRarity.EXOTIC;
      maxEnergy = 60; // 1 minute recharge time
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
      //setRecipe(makeRecipe());
      
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.putString("pearlID","");
      magicTag.putBoolean("active",false);
      prefNBT = tag;
      item.setNbt(prefNBT);
      prefItem = item;
   
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtCompound newTag = prefNBT.copy();
      newTag.getCompound("arcananovum").putString("UUID",magicTag.getString("UUID"));
      newTag.getCompound("arcananovum").putInt("energy",magicTag.getInt("energy"));
      newTag.getCompound("arcananovum").putString("pearlID",magicTag.getString("pearlID"));
      newTag.getCompound("arcananovum").putBoolean("active",magicTag.getBoolean("active"));
      stack.setNbt(newTag);
      redoLore(stack);
      return stack;
   }
   
   private void redoLore(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
      
      int charge = (getEnergy(stack)*100/maxEnergy);
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
   
      try{
         if(pearlID.isEmpty()){ // Throw new pearl
            if(getEnergy(item) == maxEnergy){
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
                  playerEntity.sendMessage(new LiteralText("Pearl Recharging: "+(getEnergy(item)*100/maxEnergy)+"%").formatted(Formatting.BLUE),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
               }
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
   
   private MagicItemRecipe makeRecipe(){
      //TODO make recipe
      return null;
   }
   
   private List<String> makeLore(){
      //TODO make lore
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" TODO \"}");
      return list;
   }
}
