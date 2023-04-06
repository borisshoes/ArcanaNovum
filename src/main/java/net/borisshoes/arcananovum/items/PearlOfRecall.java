package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.core.EnergyItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.TickingItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class PearlOfRecall extends EnergyItem implements TickingItem, UsableItem {
   
   public static final int[] cdReduction = {0,60,120,240,360,480};
   
   public PearlOfRecall(){
      id = "pearl_of_recall";
      name = "Pearl of Recall";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS};
      initEnergy = 600;
   
      ItemStack item = new ItemStack(Items.ENDER_EYE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Pearl of Recall\",\"italic\":false,\"bold\":true,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"An \",\"italic\":false,\"color\":\"green\"},{\"text\":\"Ender Pearl\",\"color\":\"dark_aqua\"},{\"text\":\" whose \"},{\"text\":\"moment \",\"color\":\"blue\"},{\"text\":\"of \"},{\"text\":\"activation \",\"color\":\"dark_green\"},{\"text\":\"was \"},{\"text\":\"frozen \",\"color\":\"aqua\"},{\"text\":\"for later use.\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It requires the \",\"italic\":false,\"color\":\"green\"},{\"text\":\"flowing of time\",\"color\":\"blue\"},{\"text\":\" \",\"color\":\"blue\"},{\"text\":\"to \"},{\"text\":\"recharge \",\"color\":\"aqua\"},{\"text\":\"it.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to set its \",\"color\":\"green\"},{\"text\":\"location \",\"color\":\"light_purple\"},{\"text\":\"and \",\"color\":\"green\"},{\"text\":\"to \",\"color\":\"green\"},{\"text\":\"teleport \",\"color\":\"dark_green\"},{\"text\":\"to its \",\"color\":\"green\"},{\"text\":\"set point\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Location - \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"Unbound\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
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
      NbtCompound locTag = new NbtCompound();
      locTag.putString("dim","unattuned");
      magicTag.putInt("heat",0);
      magicTag.put("location",locTag);
      prefNBT = tag;
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 10 minute recharge time
      int cdLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"recall_acceleration"));
      return 600 - cdReduction[cdLvl];
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      int heat = magicTag.getInt("heat");
      
      if(heat == 100){
         teleport(item,player);
         magicTag.putInt("heat",0);
         PLAYER_DATA.get(player).addXP(1000); // Add xp
      }else if(heat > 0){
         magicTag.putInt("heat",heat+1);
         ParticleEffectUtils.recallTeleportCharge(world,player.getPos());
      }else if(heat == -1){
         // Teleport was cancelled by damage
         ParticleEffectUtils.recallTeleportCancel(world,player.getPos());
         SoundUtils.playSound(player.getWorld(), player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_HURT, SoundCategory.PLAYERS, 8,0.8f);
         magicTag.putInt("heat",0);
         setEnergy(item,(int)(getMaxEnergy(item)*0.75));
      }
      
      if((item.isItemEqual(player.getMainHandStack()) && ItemStack.areNbtEqual(item,player.getMainHandStack())) || (item.isItemEqual(player.getOffHandStack()) && ItemStack.areNbtEqual(item,player.getOffHandStack()))){
         NbtCompound locNbt = magicTag.getCompound("location");
         String dim = locNbt.getString("dim");
         double x = locNbt.getDouble("x");
         double y = locNbt.getDouble("y");
         double z = locNbt.getDouble("z");
         Vec3d loc = new Vec3d(x,y,z);
         if(player.getWorld().getRegistryKey().getValue().toString().equals(dim) && player.getPos().distanceTo(loc) < 30){
            ParticleEffectUtils.recallLocation(world,loc,player);
         }
      }
      
      
      if(world.getServer().getTicks() % 20 == 0){
         addEnergy(item, 1); // Recharge
         redoLore(item);
      }
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtCompound locNbt = magicTag.getCompound("location").copy();
      int heat = magicTag.getInt("heat");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").putInt("heat",heat);
      newTag.getCompound("arcananovum").put("location",locNbt);
      stack.setNbt(newTag);
      redoLore(stack);
      return stack;
   }
   
   private void redoLore(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtCompound locNbt = magicNbt.getCompound("location");
      String dim = locNbt.getString("dim");
      
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
      int charge = (getEnergy(stack)*100/getMaxEnergy(stack));
      String charging = charge == 100 ? "Charged" : "Charging";
      if(!dim.equals("unattuned")){
         int x = (int) locNbt.getDouble("x");
         int y = (int) locNbt.getDouble("y");
         int z = (int) locNbt.getDouble("z");
         String dimColor;
         String dimensionName;
         String location;
         switch(dim){
            case "minecraft:overworld":
               dimColor = "green";
               dimensionName = "Overworld";
               break;
            case "minecraft:the_nether":
               dimColor = "red";
               dimensionName = "The Nether";
               break;
            case "minecraft:the_end":
               dimColor = "yellow";
               dimensionName = "The End";
               break;
            default:
               dimColor = "aqua";
               dimensionName = dim;
               break;
         }
         location = dimensionName + " ("+x+","+y+","+z+")";
         
         loreList.set(4,NbtString.of("[{\"text\":\"Location - \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\""+location+"\",\"color\":\""+dimColor+"\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
         loreList.set(5,NbtString.of("[{\"text\":\""+charging+" - \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\""+charge+"%\",\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
      }else{
         loreList.set(4,NbtString.of("[{\"text\":\"Location - \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"Unbound\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
         loreList.set(5,NbtString.of("[{\"text\":\""+charging+" - \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\""+charge+"%\",\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
      }
   }
   
   private void teleport(ItemStack item, ServerPlayerEntity player){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtCompound locNbt = magicNbt.getCompound("location");
      String dim = locNbt.getString("dim");
      double x = locNbt.getDouble("x");
      double y = locNbt.getDouble("y");
      double z = locNbt.getDouble("z");
      float yaw = locNbt.getFloat("yaw");
      float pitch = locNbt.getFloat("pitch");
   
      ServerWorld to = player.getWorld();
      for (ServerWorld w : player.getServer().getWorlds()){
         if(w.getRegistryKey().getValue().toString().equals(dim)){
            to = w;
            break;
         }
      }
      
      player.teleport(to,x,y,z,yaw,pitch);
      setEnergy(item,0);
      if(to.getRegistryKey().getValue().toString().equals("minecraft:the_nether")) ArcanaAchievements.grant(player,"back_to_hell");
      if(to.getRegistryKey().getValue().toString().equals("minecraft:the_end")) ArcanaAchievements.grant(player,"ascending_to_heaven");
      SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_PORTAL_TRAVEL,1,2f);
      ParticleEffectUtils.recallTeleport(to,player.getPos());
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack item = playerEntity.getStackInHand(hand);
      boolean canClear = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"chrono_tear")) >= 1;
      if (playerEntity instanceof ServerPlayerEntity player){
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         NbtCompound locNbt = magicNbt.getCompound("location");
         String dim = locNbt.getString("dim");
         
         if(!(canClear && player.isSneaking())){
            if(dim.equals("unattuned")){
               locNbt.putString("dim", playerEntity.getWorld().getRegistryKey().getValue().toString());
               locNbt.putDouble("x", playerEntity.getPos().x);
               locNbt.putDouble("y", playerEntity.getPos().y);
               locNbt.putDouble("z", playerEntity.getPos().z);
               locNbt.putFloat("yaw", playerEntity.getYaw());
               locNbt.putFloat("pitch", playerEntity.getPitch());
               redoLore(item);
            }else{
               int curEnergy = getEnergy(item);
               if(curEnergy == getMaxEnergy(item)){
                  magicNbt.putInt("heat", 1); // Starts the heat up process
                  SoundUtils.playSound(player.getWorld(), player.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1, 1);
               }else{
                  playerEntity.sendMessage(Text.literal("Pearl Recharging: " + (curEnergy * 100 / getMaxEnergy(item)) + "%").formatted(Formatting.DARK_AQUA), true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
               }
            }
         }else{ // Clear location
            if(!dim.equals("unattuned")){
               locNbt = new NbtCompound();
               locNbt.putString("dim", "unattuned");
               magicNbt.put("location", locNbt);
   
               playerEntity.sendMessage(Text.literal("Saved Location Cleared").formatted(Formatting.DARK_AQUA), true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1, .7f);
            }
         }
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
      MagicItemIngredient t = new MagicItemIngredient(Items.CLOCK,32,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.TEMPORAL_MOMENT,1);
      
      MagicItemIngredient[][] ingredients = {
            {c,p,s,p,c},
            {p,t,e,t,p},
            {s,e,m,e,s},
            {p,t,e,t,p},
            {c,p,s,p,c}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Pearl of Recall\\n\\nRarity: Exotic\\n\\nBy freezing an Ender Pearl in time as it activates, I can keep the frozen Pearl with me and unfreeze it when I need to recall myself to where I froze it. I can even use it multiple times after a recharge.\"}");
      list.add("{\"text\":\"    Pearl of Recall\\n\\nRight Clicking sets the Pearl's Recall Point.\\n\\nRight Clicking again starts to unfreeze the pearl in time. Taking damage resets the process.\\n\\nAfter use, the Pearl takes a while to resync to the timeline.\"}");
      return list;
   }
}
