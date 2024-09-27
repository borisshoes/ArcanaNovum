package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class PearlOfRecall extends EnergyItem {
	public static final String ID = "pearl_of_recall";
   
   public static final String HEAT_TAG = "heat";
   public static final String LOCATION_TAG = "location";
   
   public static final int[] cdReduction = {0,60,120,240,360,480};
   private static final String CHARGED_TXT = "item/pearl_of_recall_charged";
   private static final String COOLDOWN_TXT = "item/pearl_of_recall_cooldown";
   
   public PearlOfRecall(){
      id = ID;
      name = "Pearl of Recall";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ITEMS};
      initEnergy = 600;
      vanillaItem = Items.ENDER_EYE;
      item = new PearlOfRecallItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,CHARGED_TXT));
      models.add(new Pair<>(vanillaItem,COOLDOWN_TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.ADVANCEMENT_USE_LODESTONE,ResearchTasks.USE_ENDER_PEARL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,HEAT_TAG, 0);
      NbtCompound locTag = new NbtCompound();
      locTag.putString("dim","unattuned");
      putProperty(stack,LOCATION_TAG,locTag);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("An ").formatted(Formatting.GREEN))
            .append(Text.literal("Ender Pearl").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" whose ").formatted(Formatting.GREEN))
            .append(Text.literal("moment ").formatted(Formatting.BLUE))
            .append(Text.literal("of ").formatted(Formatting.GREEN))
            .append(Text.literal("activation ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("was ").formatted(Formatting.GREEN))
            .append(Text.literal("frozen ").formatted(Formatting.AQUA))
            .append(Text.literal("for later use.").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("It requires the ").formatted(Formatting.GREEN))
            .append(Text.literal("flowing of time").formatted(Formatting.BLUE))
            .append(Text.literal(" to ").formatted(Formatting.GREEN))
            .append(Text.literal("recharge ").formatted(Formatting.AQUA))
            .append(Text.literal("it.").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to set its ").formatted(Formatting.GREEN))
            .append(Text.literal("location ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("and ").formatted(Formatting.GREEN))
            .append(Text.literal("to ").formatted(Formatting.GREEN))
            .append(Text.literal("teleport ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("to its ").formatted(Formatting.GREEN))
            .append(Text.literal("set point").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.GREEN)));
      lore.add(Text.literal(""));
      
      if(itemStack == null){
         lore.add(Text.literal("")
               .append(Text.literal("Location - ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal("Unbound").formatted(Formatting.GRAY)));
         lore.add(Text.literal("")
               .append(Text.literal("Charged - ").formatted(Formatting.DARK_AQUA))
               .append(Text.literal("100%").formatted(Formatting.BOLD,Formatting.BLUE)));
      }else{
         NbtCompound locNbt = getCompoundProperty(itemStack,LOCATION_TAG);
         String dim = locNbt.getString("dim");
         int x = (int) locNbt.getDouble("x");
         int y = (int) locNbt.getDouble("y");
         int z = (int) locNbt.getDouble("z");
         Formatting dimColor;
         String dimensionName;
         String location;
         
         if(dim.equals(ServerWorld.OVERWORLD.getValue().toString())){
            dimColor = Formatting.GREEN;
            dimensionName = "Overworld";
         }else if(dim.equals(ServerWorld.NETHER.getValue().toString())){
            dimColor = Formatting.RED;
            dimensionName = "The Nether";
         }else if(dim.equals(ServerWorld.END.getValue().toString())){
            dimColor = Formatting.YELLOW;
            dimensionName = "The End";
         }else{
            dimColor = Formatting.AQUA;
            dimensionName = dim;
         }
         
         
         if(!dim.equals("unattuned")){
            location = dimensionName + " ("+x+","+y+","+z+")";
            lore.add(Text.literal("")
                  .append(Text.literal("Location - ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal(location).formatted(dimColor)));
         }else{
            lore.add(Text.literal("")
                  .append(Text.literal("Location - ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("Unbound").formatted(Formatting.GRAY)));
         }
         
         int charge = (getEnergy(itemStack)*100/getMaxEnergy(itemStack));
         String charging = charge == 100 ? "Charged" : "Charging";
         lore.add(Text.literal("")
               .append(Text.literal(charging+" - ").formatted(Formatting.DARK_AQUA))
               .append(Text.literal(charge+"%").formatted(Formatting.BOLD,Formatting.BLUE)));
      }
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
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
      NbtCompound locNbt = getCompoundProperty(stack,LOCATION_TAG).copy();
      int heat = getIntProperty(stack,HEAT_TAG);
      ItemStack newItem = super.updateItem(stack,server);
      putProperty(newItem,LOCATION_TAG,locNbt);
      putProperty(newItem,HEAT_TAG, heat);
      return buildItemLore(newItem,server);
   }
   
   private void teleport(ItemStack item, ServerPlayerEntity player){
      NbtCompound locNbt = getCompoundProperty(item,LOCATION_TAG);
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
      player.teleport(to, x, y, z, new HashSet(), yaw, pitch, false);
      setEnergy(item,0);
      if(to.getRegistryKey().getValue().toString().equals("minecraft:the_nether")) ArcanaAchievements.grant(player,ArcanaAchievements.BACK_TO_HELL.id);
      if(to.getRegistryKey().getValue().toString().equals("minecraft:the_end")) ArcanaAchievements.grant(player,ArcanaAchievements.ASCENDING_TO_HEAVEN.id);
      SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_PORTAL_TRAVEL,1,2f);
      ParticleEffectUtils.recallTeleport(to,player.getPos());
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.ENDER_PEARL,8);
      ArcanaIngredient b = new ArcanaIngredient(Items.GOLD_INGOT,8);
      ArcanaIngredient c = new ArcanaIngredient(Items.CLOCK,8);
      ArcanaIngredient g = new ArcanaIngredient(Items.ENDER_EYE,4);
      ArcanaIngredient h = new ArcanaIngredient(Items.LODESTONE,1, true);
      ArcanaIngredient l = new ArcanaIngredient(Items.NETHER_STAR,1);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,l,m,l,c},
            {b,g,l,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
      
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Pearl of Recall\n\nRarity: Exotic\n\nBy freezing an Ender Pearl in time as it activates, I can keep the frozen Pearl with me and unfreeze it when I need to recall myself to where I froze it. I can even use it multiple times after a recharge.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Pearl of Recall\n\nRight Clicking sets the Pearl's Recall Point.\n\nRight Clicking again starts to unfreeze the pearl in time. Taking damage resets the process.\n\nAfter use, the Pearl takes a while to resync to the timeline.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class PearlOfRecallItem extends ArcanaPolymerItem {
      public PearlOfRecallItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!ArcanaItemUtils.isArcane(itemStack)) return ArcanaRegistry.getModelData(CHARGED_TXT).value();
         return getEnergy(itemStack) >= getMaxEnergy(itemStack) ? ArcanaRegistry.getModelData(CHARGED_TXT).value() : ArcanaRegistry.getModelData(COOLDOWN_TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         

         int heat = getIntProperty(stack,HEAT_TAG);
         
         if(heat == 100){
            teleport(stack,player);
            putProperty(stack,HEAT_TAG, 0);
            PLAYER_DATA.get(player).addXP(1000); // Add xp
         }else if(heat > 0){
            putProperty(stack,HEAT_TAG, heat+1);
            ParticleEffectUtils.recallTeleportCharge(serverWorld,player.getPos());
         }else if(heat == -1){
            // Teleport was cancelled by damage
            ParticleEffectUtils.recallTeleportCancel(serverWorld,player.getPos());
            SoundUtils.playSound(player.getServerWorld(), player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_HURT, SoundCategory.PLAYERS, 8,0.8f);
            putProperty(stack,HEAT_TAG, 0);
            setEnergy(stack,(int)(getMaxEnergy(stack)*0.75));
         }
         
         if(ItemStack.areItemsAndComponentsEqual(stack,player.getMainHandStack()) || ItemStack.areItemsAndComponentsEqual(stack,player.getOffHandStack())){
            NbtCompound locNbt = getCompoundProperty(stack,LOCATION_TAG);
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
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack item = playerEntity.getStackInHand(hand);
         boolean canClear = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.CHRONO_TEAR.id) >= 1;
         if (playerEntity instanceof ServerPlayerEntity player){
            NbtCompound locNbt = getCompoundProperty(item,LOCATION_TAG);
            String dim = locNbt.getString("dim");
            
            if(!(canClear && player.isSneaking())){
               if(dim.equals("unattuned")){
                  locNbt.putString("dim", playerEntity.getWorld().getRegistryKey().getValue().toString());
                  locNbt.putDouble("x", playerEntity.getPos().x);
                  locNbt.putDouble("y", playerEntity.getPos().y);
                  locNbt.putDouble("z", playerEntity.getPos().z);
                  locNbt.putFloat("yaw", playerEntity.getYaw());
                  locNbt.putFloat("pitch", playerEntity.getPitch());
                  putProperty(item,LOCATION_TAG,locNbt);
                  buildItemLore(item,playerEntity.getServer());
               }else{
                  int curEnergy = getEnergy(item);
                  if(curEnergy >= getMaxEnergy(item)){
                     putProperty(item,HEAT_TAG, 1); // Starts the heat up process
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
                  putProperty(item,LOCATION_TAG,locNbt);
                  buildItemLore(item,playerEntity.getServer());
                  
                  playerEntity.sendMessage(Text.literal("Saved Location Cleared").formatted(Formatting.DARK_AQUA), true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1, .7f);
               }
            }
         }
         return ActionResult.SUCCESS;
      }
   }
}

