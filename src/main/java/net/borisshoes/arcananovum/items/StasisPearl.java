package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.entities.StasisPearlEntity;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class StasisPearl extends EnergyItem {
   
   public StasisPearl(){
      id = "stasis_pearl";
      name = "Stasis Pearl";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS};
      initEnergy = 60;
      vanillaItem = Items.ENDER_PEARL;
      item = new StasisPearlItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Stasis Pearl\",\"italic\":false,\"color\":\"blue\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Ender Pearl\",\"color\":\"dark_aqua\"},{\"text\":\" has the ability to \"},{\"text\":\"freeze \",\"color\":\"aqua\"},{\"text\":\"its passage through \"},{\"text\":\"time\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"frozen \",\"color\":\"aqua\"},{\"text\":\"the \"},{\"text\":\"pearl \",\"color\":\"dark_aqua\"},{\"text\":\"looks like it's '\"},{\"text\":\"hanging\",\"color\":\"aqua\",\"italic\":true},{\"text\":\"' in the air.\",\"color\":\"gray\",\"italic\":false}]"));
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
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 1 minute base recharge time
      return 60 - 10*Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.STASIS_ACCELERATION.id));
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
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtElement.STRING_TYPE);
      
      int charge = (getEnergy(stack)*100/getMaxEnergy(stack));
      String charging = charge == 100 ? "Charged" : "Charging";
      loreList.set(6,NbtString.of("[{\"text\":\""+charging+" - \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\""+charge+"%\",\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
   
      
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient c = new MagicItemIngredient(Items.END_CRYSTAL,32,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.ENDER_PEARL,16,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,1,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      
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
   
   public class StasisPearlItem extends MagicPolymerItem {
      public StasisPearlItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return vanillaItem;
         NbtCompound itemNbt = itemStack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         boolean active = magicNbt.getBoolean("active");
         
         return active ? Items.ENDER_EYE : vanillaItem;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         String pearlID = magicNbt.getString("pearlID");
         boolean active = magicNbt.getBoolean("active");
         
         if(!pearlID.isEmpty()){
            Entity foundEntity = null;
            for(ServerWorld possibleWorlds : serverWorld.getServer().getWorlds()){
               Entity possibleEntity = possibleWorlds.getEntity(UUID.fromString(pearlID));
               if(foundEntity == null && possibleEntity != null){
                  foundEntity = possibleEntity;
               }
            }
            if(!(foundEntity instanceof StasisPearlEntity pearlEntity)){
               magicNbt.putString("pearlID","");
               magicNbt.putBoolean("active",false);
            }
         }
         
         if(world.getServer().getTicks() % 20 == 0){
            addEnergy(stack, 1); // Recharge
            redoLore(stack);
         }
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack item = playerEntity.getStackInHand(hand);
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         String pearlID = magicNbt.getString("pearlID");
         boolean active = magicNbt.getBoolean("active");
         boolean canDelete = playerEntity.isSneaking() && Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SPATIAL_FOLD.id)) >= 1;
         
         try{
            if(pearlID.isEmpty()){ // Throw new pearl
               if(getEnergy(item) == getMaxEnergy(item)){
                  SoundUtils.playSound(world,playerEntity.getBlockPos(),SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F);
                  playerEntity.getItemCooldownManager().set(this, 0);
                  if (!world.isClient) {
                     StasisPearlEntity stasisPearlEntity = new StasisPearlEntity(world, playerEntity, getUUID(item), magicNbt.getCompound("augments"));
                     stasisPearlEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, 1.5F, 1.0F);
                     world.spawnEntity(stasisPearlEntity);
                     String newPearlID = stasisPearlEntity.getUuidAsString();
                     magicNbt.putString("pearlID",newPearlID);
                     
                     setEnergy(item,0);
                     PLAYER_DATA.get(playerEntity).addXP(250);
                  }
               }else{
                  playerEntity.getItemCooldownManager().set(this, 0);
                  if(playerEntity instanceof ServerPlayerEntity player){
                     playerEntity.sendMessage(Text.literal("Pearl Recharging: "+(getEnergy(item)*100/getMaxEnergy(item))+"%").formatted(Formatting.BLUE),true);
                     SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
                  }
               }
            }else if(canDelete){ // Delete current pearl
               if(world instanceof ServerWorld serverWorld){
                  Entity foundEntity = null;
                  for(ServerWorld possibleWorlds : serverWorld.getServer().getWorlds()){
                     Entity possibleEntity = possibleWorlds.getEntity(UUID.fromString(pearlID));
                     if(foundEntity == null && possibleEntity != null){
                        foundEntity = possibleEntity;
                     }
                  }
                  if(foundEntity != null) foundEntity.kill();
                  playerEntity.getItemCooldownManager().set(this, 0);
               }
               // Reset data
               magicNbt.putString("pearlID","");
               magicNbt.putBoolean("active",false);
               if(playerEntity instanceof ServerPlayerEntity player){
                  playerEntity.sendMessage(Text.literal("Pearl Cancelled").formatted(Formatting.BLUE),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1, .5f);
               }
            }else if(active){ // Un-stasis to pearl
               if(world instanceof ServerWorld serverWorld){
                  Entity foundEntity = null;
                  for(ServerWorld possibleWorlds : serverWorld.getServer().getWorlds()){
                     Entity possibleEntity = possibleWorlds.getEntity(UUID.fromString(pearlID));
                     if(foundEntity == null && possibleEntity != null){
                        foundEntity = possibleEntity;
                     }
                  }
                  if(foundEntity instanceof StasisPearlEntity pearlEntity){
                     pearlEntity.setStasis(false);
                     magicNbt.putBoolean("active", false);
                     playerEntity.getItemCooldownManager().set(this, 0);
                     return TypedActionResult.success(item);
                  }
               }
               
               // If this is reached, something went wrong and the pearl needs to be reset
               magicNbt.putString("pearlID","");
               magicNbt.putBoolean("active",false);
            }else{ // Stasis the pearl
               if(world instanceof ServerWorld serverWorld){
                  Entity foundEntity = null;
                  for(ServerWorld possibleWorlds : serverWorld.getServer().getWorlds()){
                     Entity possibleEntity = possibleWorlds.getEntity(UUID.fromString(pearlID));
                     if(foundEntity == null && possibleEntity != null){
                        foundEntity = possibleEntity;
                     }
                  }
                  if(foundEntity instanceof StasisPearlEntity pearlEntity){
                     pearlEntity.setStasis(true);
                     magicNbt.putBoolean("active", true);
                     playerEntity.getItemCooldownManager().set(this, 0);
                     return TypedActionResult.success(item);
                  }
               }
               // If this is reached, something went wrong and the pearl needs to be reset
               magicNbt.putString("pearlID","");
               magicNbt.putBoolean("active",false);
            }
         }catch(Exception e){
            e.printStackTrace();
         }
         return TypedActionResult.success(item);
      }
   }
}
