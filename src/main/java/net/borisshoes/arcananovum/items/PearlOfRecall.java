package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
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
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class PearlOfRecall extends EnergyItem {
   
   public static final int[] cdReduction = {0,60,120,240,360,480};
   private static final String CHARGED_TXT = "item/pearl_of_recall_charged";
   private static final String COOLDOWN_TXT = "item/pearl_of_recall_cooldown";
   
   public PearlOfRecall(){
      id = "pearl_of_recall";
      name = "Pearl of Recall";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS};
      initEnergy = 600;
      vanillaItem = Items.ENDER_EYE;
      item = new PearlOfRecallItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,CHARGED_TXT));
      models.add(new Pair<>(vanillaItem,COOLDOWN_TXT));
   
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Pearl of Recall\",\"italic\":false,\"bold\":true,\"color\":\"dark_aqua\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      stack.setNbt(addMagicNbt(tag));
      NbtCompound locTag = new NbtCompound();
      locTag.putString("dim","unattuned");
      tag.getCompound("arcananovum").putInt("heat",0);
      tag.getCompound("arcananovum").put("location",locTag);
      stack.setNbt(tag);
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtCompound itemNbt = itemStack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtCompound locNbt = magicNbt.getCompound("location");
      String dim = locNbt.getString("dim");
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"An \",\"italic\":false,\"color\":\"green\"},{\"text\":\"Ender Pearl\",\"color\":\"dark_aqua\"},{\"text\":\" whose \"},{\"text\":\"moment \",\"color\":\"blue\"},{\"text\":\"of \"},{\"text\":\"activation \",\"color\":\"dark_green\"},{\"text\":\"was \"},{\"text\":\"frozen \",\"color\":\"aqua\"},{\"text\":\"for later use.\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It requires the \",\"italic\":false,\"color\":\"green\"},{\"text\":\"flowing of time\",\"color\":\"blue\"},{\"text\":\" \",\"color\":\"blue\"},{\"text\":\"to \"},{\"text\":\"recharge \",\"color\":\"aqua\"},{\"text\":\"it.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to set its \",\"color\":\"green\"},{\"text\":\"location \",\"color\":\"light_purple\"},{\"text\":\"and \",\"color\":\"green\"},{\"text\":\"to \",\"color\":\"green\"},{\"text\":\"teleport \",\"color\":\"dark_green\"},{\"text\":\"to its \",\"color\":\"green\"},{\"text\":\"set point\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      
      if(itemStack != null){
         int charge = (getEnergy(itemStack)*100/getMaxEnergy(itemStack));
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
            
            loreList.add(NbtString.of("[{\"text\":\"Location - \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\""+location+"\",\"color\":\""+dimColor+"\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
            loreList.add(NbtString.of("[{\"text\":\""+charging+" - \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\""+charge+"%\",\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
         }else{
            loreList.add(NbtString.of("[{\"text\":\"Location - \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"Unbound\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
            loreList.add(NbtString.of("[{\"text\":\""+charging+" - \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\""+charge+"%\",\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
         }
      }else{
         loreList.add(NbtString.of("[{\"text\":\"Location - \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"Unbound\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Charged - \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"100%\",\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
      }
      
      return loreList;
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 10 minute recharge time
      int cdLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.RECALL_ACCELERATION.id));
      return 600 - cdReduction[cdLvl];
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
      return buildItemLore(stack,server);
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
   
      ServerWorld to = player.getServerWorld();
      for (ServerWorld w : player.getServer().getWorlds()){
         if(w.getRegistryKey().getValue().toString().equals(dim)){
            to = w;
            break;
         }
      }
      
      player.teleport(to,x,y,z,yaw,pitch);
      setEnergy(item,0);
      if(to.getRegistryKey().getValue().toString().equals("minecraft:the_nether")) ArcanaAchievements.grant(player,ArcanaAchievements.BACK_TO_HELL.id);
      if(to.getRegistryKey().getValue().toString().equals("minecraft:the_end")) ArcanaAchievements.grant(player,ArcanaAchievements.ASCENDING_TO_HEAVEN.id);
      SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_PORTAL_TRAVEL,1,2f);
      ParticleEffectUtils.recallTeleport(to,player.getPos());
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient c = new MagicItemIngredient(Items.END_CRYSTAL,32,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.ENDER_PEARL,16,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient t = new MagicItemIngredient(Items.CLOCK,32,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      
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
   
   public class PearlOfRecallItem extends MagicPolymerItem {
      public PearlOfRecallItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return ArcanaRegistry.MODELS.get(CHARGED_TXT).value();
         return getEnergy(itemStack) >= getMaxEnergy(itemStack) ? ArcanaRegistry.MODELS.get(CHARGED_TXT).value() : ArcanaRegistry.MODELS.get(COOLDOWN_TXT).value();
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
         NbtCompound magicTag = itemNbt.getCompound("arcananovum");
         int heat = magicTag.getInt("heat");
         
         if(heat == 100){
            teleport(stack,player);
            magicTag.putInt("heat",0);
            PLAYER_DATA.get(player).addXP(1000); // Add xp
         }else if(heat > 0){
            magicTag.putInt("heat",heat+1);
            ParticleEffectUtils.recallTeleportCharge(serverWorld,player.getPos());
         }else if(heat == -1){
            // Teleport was cancelled by damage
            ParticleEffectUtils.recallTeleportCancel(serverWorld,player.getPos());
            SoundUtils.playSound(player.getServerWorld(), player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_HURT, SoundCategory.PLAYERS, 8,0.8f);
            magicTag.putInt("heat",0);
            setEnergy(stack,(int)(getMaxEnergy(stack)*0.75));
         }
         
         if(ItemStack.canCombine(stack,player.getMainHandStack()) || ItemStack.canCombine(stack,player.getOffHandStack())){
            NbtCompound locNbt = magicTag.getCompound("location");
            String dim = locNbt.getString("dim");
            double x = locNbt.getDouble("x");
            double y = locNbt.getDouble("y");
            double z = locNbt.getDouble("z");
            Vec3d loc = new Vec3d(x,y,z);
            if(player.getServerWorld().getRegistryKey().getValue().toString().equals(dim) && player.getPos().distanceTo(loc) < 30){
               ParticleEffectUtils.recallLocation(serverWorld,loc,player);
            }
         }
         
         
         if(world.getServer().getTicks() % 20 == 0){
            addEnergy(stack, 1); // Recharge
            buildItemLore(stack,world.getServer());
         }
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack item = playerEntity.getStackInHand(hand);
         boolean canClear = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.CHRONO_TEAR.id) >= 1;
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
                  buildItemLore(item,playerEntity.getServer());
               }else{
                  int curEnergy = getEnergy(item);
                  if(curEnergy >= getMaxEnergy(item)){
                     magicNbt.putInt("heat", 1); // Starts the heat up process
                     SoundUtils.playSound(player.getServerWorld(), player.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1, 1);
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
         return TypedActionResult.success(playerEntity.getStackInHand(hand));
      }
   }
}
