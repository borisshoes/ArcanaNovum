package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class PearlOfRecall extends EnergyItem {
   public static final String ID = "pearl_of_recall";
   
   public static final String HEAT_TAG = "heat";
   public static final String LOCATION_TAG = "location";
   
   public PearlOfRecall(){
      id = ID;
      name = "Pearl of Recall";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      initEnergy = 600;
      vanillaItem = Items.ENDER_EYE;
      item = new PearlOfRecallItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT, ResearchTasks.ADVANCEMENT_USE_LODESTONE, ResearchTasks.USE_ENDER_PEARL, ResearchTasks.UNLOCK_WAYSTONE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack, HEAT_TAG, 0);
      CompoundTag locTag = new CompoundTag();
      locTag.putString("dim", "unattuned");
      putProperty(stack, LOCATION_TAG, locTag);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("An ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("Ender Pearl").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" whose ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("moment ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("of ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("activation ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("was ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("frozen ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("for later use.").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("It requires the ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("flowing of time").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" to ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("recharge ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("it.").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to set its ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("location ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("and ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("teleport ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("to its ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("set point").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal(""));
      
      if(itemStack == null){
         lore.add(Component.literal("")
               .append(Component.literal("Location - ").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(Component.literal("Unbound").withStyle(ChatFormatting.GRAY)));
         lore.add(Component.literal("")
               .append(Component.literal("Charged - ").withStyle(ChatFormatting.DARK_AQUA))
               .append(Component.literal("100%").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE)));
      }else{
         CompoundTag locNbt = getCompoundProperty(itemStack, LOCATION_TAG);
         String dim = locNbt.getStringOr("dim", "");
         int x = (int) locNbt.getDoubleOr("x", 0.0);
         int y = (int) locNbt.getDoubleOr("y", 0.0);
         int z = (int) locNbt.getDoubleOr("z", 0.0);
         ChatFormatting dimColor;
         String dimensionName;
         String location;
         
         if(dim.equals(ServerLevel.OVERWORLD.identifier().toString())){
            dimColor = ChatFormatting.GREEN;
            dimensionName = "Overworld";
         }else if(dim.equals(ServerLevel.NETHER.identifier().toString())){
            dimColor = ChatFormatting.RED;
            dimensionName = "The Nether";
         }else if(dim.equals(ServerLevel.END.identifier().toString())){
            dimColor = ChatFormatting.DARK_PURPLE;
            dimensionName = "The End";
         }else{
            dimColor = ChatFormatting.YELLOW;
            dimensionName = dim;
         }
         
         
         if(!dim.equals("unattuned")){
            location = dimensionName + " (" + x + "," + y + "," + z + ")";
            lore.add(Component.literal("")
                  .append(Component.literal("Location - ").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal(location).withStyle(dimColor)));
         }else{
            lore.add(Component.literal("")
                  .append(Component.literal("Location - ").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal("Unbound").withStyle(ChatFormatting.GRAY)));
         }
         
         int charge = (getEnergy(itemStack) * 100 / getMaxEnergy(itemStack));
         String charging = charge == 100 ? "Charged" : "Charging";
         lore.add(Component.literal("")
               .append(Component.literal(charging + " - ").withStyle(ChatFormatting.DARK_AQUA))
               .append(Component.literal(charge + "%").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE)));
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 10 minute recharge time
      int baseCooldown = ArcanaNovum.CONFIG.getInt(ArcanaConfig.PEARL_OF_RECALL_COOLDOWN);
      int cooldownReduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PEARL_OF_RECALL_COOLDOWN_PER_LVL).get(ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.RECALL_ACCELERATION));
      return Math.max(1, baseCooldown - cooldownReduction);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      CompoundTag locNbt = getCompoundProperty(stack, LOCATION_TAG).copy();
      int heat = getIntProperty(stack, HEAT_TAG);
      ItemStack newItem = super.updateItem(stack, server);
      putProperty(newItem, LOCATION_TAG, locNbt);
      putProperty(newItem, HEAT_TAG, heat);
      return buildItemLore(newItem, server);
   }
   
   private void teleport(ItemStack item, ServerPlayer player){
      CompoundTag locNbt = getCompoundProperty(item, LOCATION_TAG);
      String dim = locNbt.getStringOr("dim", "");
      double x = locNbt.getDoubleOr("x", 0.0);
      double y = locNbt.getDoubleOr("y", 0.0);
      double z = locNbt.getDoubleOr("z", 0.0);
      float yaw = locNbt.getFloatOr("yaw", 0.0f);
      float pitch = locNbt.getFloatOr("pitch", 0.0f);
      
      ServerLevel to = player.level();
      for(ServerLevel w : player.level().getServer().getAllLevels()){
         if(w.dimension().identifier().toString().equals(dim)){
            to = w;
            break;
         }
      }
      
      player.teleport(new TeleportTransition(to, new Vec3(x, y, z), Vec3.ZERO, yaw, pitch, TeleportTransition.PLACE_PORTAL_TICKET));
      setEnergy(item, 0);
      if(to.dimension().identifier().toString().equals("minecraft:the_nether"))
         ArcanaAchievements.grant(player, ArcanaAchievements.BACK_TO_HELL);
      if(to.dimension().identifier().toString().equals("minecraft:the_end"))
         ArcanaAchievements.grant(player, ArcanaAchievements.ASCENDING_TO_HEAVEN);
      SoundUtils.playSongToPlayer(player, SoundEvents.PORTAL_TRAVEL, 1, 2f);
      ArcanaEffectUtils.recallTeleport(to, player.position());
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack waystone = inv.getItem(centerpieces.getFirst()); // Should be the Waystone
      
      Waystone.WaystoneTarget target = Waystone.getTarget(waystone);
      if(target != null){
         CompoundTag locNbt = getCompoundProperty(newArcanaItem, LOCATION_TAG);
         locNbt.putString("dim", target.world().identifier().toString());
         locNbt.putDouble("x", target.position().x);
         locNbt.putDouble("y", target.position().y);
         locNbt.putDouble("z", target.position().z);
         locNbt.putFloat("yaw", target.yaw());
         locNbt.putFloat("pitch", target.pitch());
         putProperty(newArcanaItem, LOCATION_TAG, locNbt);
         buildItemLore(newArcanaItem, starlightForge.getLevel().getServer());
      }
      
      return newArcanaItem;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Pearl of Recall").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nBy freezing an Ender Pearl in time as it activates, I can keep the frozen Pearl with me and unfreeze it when I need to recall myself to where I froze it by using a Waystone to encode the location. ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Pearl of Recall").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nI can even use it multiple times after a recharge.\n\nUsing the Pearl permanently sets its Recall point.\n\nUsing the Pearl again starts to unfreeze the Pearl in time.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Pearl of Recall").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nTaking damage resets the process and requires more recharging.\n\nAfter using the Pearl, it takes a while to resync to the timeline before use again.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class PearlOfRecallItem extends ArcanaPolymerItem {
      public PearlOfRecallItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         List<String> stringList = new ArrayList<>();
         if(getEnergy(itemStack) < getMaxEnergy(itemStack)){
            stringList.add("cooldown");
         }else{
            stringList.add("charged");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(new ArrayList<>(), new ArrayList<>(), stringList, new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         
         
         int heat = getIntProperty(stack, HEAT_TAG);
         int warmup = ArcanaNovum.CONFIG.getInt(ArcanaConfig.PEARL_OF_RECALL_WARMUP);
         
         if(heat >= warmup){
            teleport(stack, player);
            putProperty(stack, HEAT_TAG, 0);
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_PEARL_OF_RECALL_USE)); // Add xp
         }else if(heat > 0){
            putProperty(stack, HEAT_TAG, heat + 1);
            ArcanaEffectUtils.recallTeleportCharge(serverWorld, player.position());
         }else if(heat == -1){
            // Teleport was cancelled by damage
            double cancelPercent = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.PEARL_OF_RECALL_CANCEL_PERCENT);
            ArcanaEffectUtils.recallTeleportCancel(serverWorld, player.position());
            SoundUtils.playSound(player.level(), player.blockPosition(), SoundEvents.ENDERMAN_HURT, SoundSource.PLAYERS, 8, 0.8f);
            putProperty(stack, HEAT_TAG, 0);
            setEnergy(stack, (int) (getMaxEnergy(stack) * cancelPercent));
         }
         
         if(ItemStack.isSameItemSameComponents(stack, player.getMainHandItem()) || ItemStack.isSameItemSameComponents(stack, player.getOffhandItem())){
            CompoundTag locNbt = getCompoundProperty(stack, LOCATION_TAG);
            String dim = locNbt.getStringOr("dim", "");
            double x = locNbt.getDoubleOr("x", 0.0);
            double y = locNbt.getDoubleOr("y", 0.0);
            double z = locNbt.getDoubleOr("z", 0.0);
            Vec3 loc = new Vec3(x, y, z);
            if(player.level().dimension().identifier().toString().equals(dim) && player.position().distanceTo(loc) < 30){
               ArcanaEffectUtils.recallLocation(serverWorld, loc, player);
            }
         }
         
         
         if(world.getServer().getTickCount() % 20 == 0){
            addEnergy(stack, 1); // Recharge
            buildItemLore(stack, world.getServer());
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack item = playerEntity.getItemInHand(hand);
         boolean canClear = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.CHRONO_TEAR) >= 1;
         if(playerEntity instanceof ServerPlayer player){
            CompoundTag locNbt = getCompoundProperty(item, LOCATION_TAG);
            String dim = locNbt.getStringOr("dim", "");
            
            if(!(canClear && player.isShiftKeyDown())){
               if(dim.equals("unattuned")){
                  locNbt.putString("dim", playerEntity.level().dimension().identifier().toString());
                  locNbt.putDouble("x", playerEntity.position().x);
                  locNbt.putDouble("y", playerEntity.position().y);
                  locNbt.putDouble("z", playerEntity.position().z);
                  locNbt.putFloat("yaw", playerEntity.getYRot());
                  locNbt.putFloat("pitch", playerEntity.getXRot());
                  putProperty(item, LOCATION_TAG, locNbt);
                  buildItemLore(item, playerEntity.level().getServer());
               }else{
                  int curEnergy = getEnergy(item);
                  if(curEnergy >= getMaxEnergy(item)){
                     putProperty(item, HEAT_TAG, 1); // Starts the heat up process
                     SoundUtils.playSound(player.level(), player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1, 1);
                  }else{
                     playerEntity.displayClientMessage(Component.literal("Pearl Recharging: " + (curEnergy * 100 / getMaxEnergy(item)) + "%").withStyle(ChatFormatting.DARK_AQUA), true);
                     SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
                  }
               }
            }else{ // Clear location
               if(!dim.equals("unattuned")){
                  locNbt = new CompoundTag();
                  locNbt.putString("dim", "unattuned");
                  putProperty(item, LOCATION_TAG, locNbt);
                  buildItemLore(item, playerEntity.level().getServer());
                  
                  playerEntity.displayClientMessage(Component.literal("Saved Location Cleared").withStyle(ChatFormatting.DARK_AQUA), true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.RESPAWN_ANCHOR_DEPLETE, 1, .7f);
               }
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
   }
}

