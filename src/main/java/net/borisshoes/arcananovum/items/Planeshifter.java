package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class Planeshifter extends EnergyItem {
	public static final String ID = "planeshifter";
   
   public static final String NETHER_UNLOCK_TAG = "netherUnlocked";
   public static final String END_UNLOCK_TAG = "endUnlocked";
   public static final String DIMENSIONS_TAG = "dimensions";
   public static final String SELECTED_TAG = "selected";
   public static final String HEAT_TAG = "heat";
   
   public static final int[] cdReduction = {0,60,120,240,360,480};
   
   public Planeshifter(){
      id = ID;
      name = "Planeshifter";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      initEnergy = 600;
      vanillaItem = Items.RECOVERY_COMPASS;
      item = new PlaneshifterItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.DIMENSION_TRAVEL, ResearchTasks.OBTAIN_EYE_OF_ENDER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,DIMENSIONS_TAG, new ListTag());
      putProperty(stack,SELECTED_TAG,"");
      putProperty(stack,HEAT_TAG,0);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A handheld device that rips ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("rifts").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" in reality like a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("portal").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("shifter ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("must be taken to each ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Dimension ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("to unlock their ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("mode").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("When in ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Nether mode").withStyle(ChatFormatting.RED))
            .append(Component.literal(", the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("shifter ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("tries to find the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("nearest portal").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("When in ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("End mode").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(", the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("shifter teleports").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" between ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("world spawns").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("It requires the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("flowing of time").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("recharge it.").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("activate ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("shifter").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("switch modes").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal(""));
      
      if(itemStack == null){
         lore.add(Component.literal("")
               .append(Component.literal("Charged - ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.literal("100%").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE)));
      }else{
         int charge = (getEnergy(itemStack)*100/getMaxEnergy(itemStack));
         String charging = charge == 100 ? "Charged" : "Charging";
         lore.add(Component.literal("")
               .append(Component.literal(charging+" - ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.literal(charge+"%").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE)));
      }

     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean hasNether = getBooleanProperty(stack,NETHER_UNLOCK_TAG);
      boolean hasEnd = getBooleanProperty(stack,END_UNLOCK_TAG);
      int heat = getIntProperty(stack,HEAT_TAG);
      ListTag dimensions = getListProperty(stack,DIMENSIONS_TAG);
      String selected = getStringProperty(stack,SELECTED_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      if(hasNether && !dimensions.contains(StringTag.valueOf(ServerLevel.NETHER.identifier().toString()))){
         dimensions.add(StringTag.valueOf(ServerLevel.NETHER.identifier().toString()));
      }
      if(hasEnd && !dimensions.contains(StringTag.valueOf(ServerLevel.END.identifier().toString()))){
         dimensions.add(StringTag.valueOf(ServerLevel.END.identifier().toString()));
      }
      putProperty(newStack,DIMENSIONS_TAG,dimensions);
      putProperty(newStack,SELECTED_TAG,selected);
      putProperty(newStack,HEAT_TAG,heat);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 10 minute recharge time
      int cdLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.PLANAR_FLOW));
      return 600 - cdReduction[cdLvl];
   }
   
   private void findPortalAndTeleport(ServerPlayer player, ServerLevel destWorld, boolean destIsNether){
      double scale = DimensionType.getTeleportationScale(player.level().dimensionType(), destWorld.dimensionType());
      WorldBorder worldBorder = destWorld.getWorldBorder();
      Vec3 destPos = worldBorder.clampVec3ToBound(player.getX() * scale, player.getY(), player.getZ() * scale);
      Optional<BlockPos> portalRect = destWorld.getPortalForcer().findClosestPortalPosition(BlockPos.containing(destPos), destIsNether, worldBorder);
      if(portalRect.isPresent()){
         player.teleport(new TeleportTransition(destWorld,portalRect.get().getCenter(),player.getDeltaMovement(),player.getYRot(),player.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND.then(entityx -> entityx.placePortalTicket(portalRect.get()))));
         player.setPortalCooldown();
         player.displayClientMessage(Component.literal("The Planeshifter syncs up with a Nether Portal").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
      }else{
         player.displayClientMessage(Component.literal("The Planeshifter could not find a Nether Portal").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
         directTeleport(player,destWorld);
      }
   }
   
   private void directTeleport(ServerPlayer player, ServerLevel destWorld){
      double scale = DimensionType.getTeleportationScale(player.level().dimensionType(), destWorld.dimensionType());
      WorldBorder worldBorder = destWorld.getWorldBorder();
      Vec3 destPos = worldBorder.clampVec3ToBound(player.getX() * scale, Mth.clamp(player.getY(),destWorld.getMinY()+5,destWorld.getMaxY()-5), player.getZ() * scale);
      player.teleport(new TeleportTransition(destWorld, destPos, Vec3.ZERO, player.getYRot(),player.getXRot(), TeleportTransition.PLACE_PORTAL_TICKET));
      for(int y = player.getBlockY(); y >= player.getBlockY()-destWorld.getHeight(); y--){
         BlockPos blockPos = new BlockPos(player.getBlockX(),y,player.getBlockZ());
         BlockState state = destWorld.getBlockState(blockPos);
         if(state.is(Blocks.LAVA)){
            ArcanaAchievements.grant(player,ArcanaAchievements.UNFORTUNATE_MATERIALIZATION);
            break;
         }else if(!(state.isAir() || state.getCollisionShape(destWorld,blockPos).isEmpty())){
            break;
         }
      }
   }
   
   private void teleport(ItemStack stack, ServerPlayer player){
      ServerLevel world = player.level();
      ServerLevel target = getTargetDim(stack,world.getServer());
      if(target == null) return;
      
      boolean inOverworld = world.dimension().identifier().equals(ServerLevel.OVERWORLD.identifier());
      boolean inEnd = world.dimension().identifier().equals(ServerLevel.END.identifier());
      boolean inNether = world.dimension().identifier().equals(ServerLevel.NETHER.identifier());
      if(inNether) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER, "From The Nether",true);
      if(inEnd) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER,"From The End",true);
      if(inOverworld) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER,"From The Overworld",true);
      boolean destOverworld = target.dimension().identifier().equals(ServerLevel.OVERWORLD.identifier());
      boolean destEnd = target.dimension().identifier().equals(ServerLevel.END.identifier());
      boolean destNether = target.dimension().identifier().equals(ServerLevel.NETHER.identifier());
      if(destNether) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER, "To The Nether",true);
      if(destEnd) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER,"To The End",true);
      if(destOverworld) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER,"To The Overworld",true);
      
      if(inNether && destOverworld){
         findPortalAndTeleport(player,target,false);
      }else if(destNether){
         findPortalAndTeleport(player,target,true);
      }else if(destEnd || (inEnd && destOverworld)){
         EndPortalBlock endPortalBlock = (EndPortalBlock) Blocks.END_PORTAL;
         player.teleport(endPortalBlock.getPortalDestination(player.level(),player,player.blockPosition()));
      }else{
         directTeleport(player,target);
      }
      
      ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_PLANESHIFTER_USE)); // Add xp
      setEnergy(stack,0);
      SoundUtils.playSongToPlayer(player, SoundEvents.PORTAL_TRAVEL,1,2f);
      ArcanaEffectUtils.recallTeleport(world,player.position());
   }
   
   private ServerLevel getTargetDim(ItemStack stack, MinecraftServer server){
      String selected = getStringProperty(stack,SELECTED_TAG);
      for(ServerLevel serverWorld : server.getAllLevels()){
         if(serverWorld.dimension().identifier().toString().equals(selected)){
            return serverWorld;
         }
      }
      return null;
   }
   
   public boolean hasDimension(ItemStack stack, ResourceKey<Level> world){
      return getListProperty(stack,DIMENSIONS_TAG).contains(StringTag.valueOf(world.identifier().toString()));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Planeshifter").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nPortals are nice, they create a safe, stable connection between worlds, but they take setup. Safety is not a concern for an Arcanist of my caliber. I can just make an unstable rift long enough to slip ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Planeshifter").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nthrough.\n\nHowever, the Shifter needs some dimensional energy from the destination before it can rip open a rift to the destination.\n\nFor some fraction of safety, if the Shifter \n\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Planeshifter").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nfinds a portal near the destination, it should sync my rift to its location.\n\nSneak Use to switch the Shifter’s target dimension.\n\nUsing the Shifter activates its warmup.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Planeshifter").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nTaking damage prematurely disrupts the rift, causing the Shifter to require time to recalibrate.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class PlaneshifterItem extends ArcanaPolymerItem {
      public PlaneshifterItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         List<String> stringList = new ArrayList<>();
         
         String selected = getStringProperty(itemStack,SELECTED_TAG);
         if(context.getPlayer() != null){
            if(getEnergy(itemStack) < getMaxEnergy(itemStack) || selected.isBlank()){
               stringList.add("none");
            }else if(selected.equals(Level.OVERWORLD.identifier().toString())){
               stringList.add("overworld");
            }else if(selected.equals(Level.END.identifier().toString())){
               stringList.add("end");
            }else if(selected.equals(Level.NETHER.identifier().toString())){
               stringList.add("nether");
            }else{
               stringList.add("other");
            }
         }else{
            stringList.add("none");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!ArcanaItemUtils.isArcane(stack) || !(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         
         if(playerEntity.isShiftKeyDown()){
            rotateDimension(stack,player,true);
         }else{
            int curEnergy = getEnergy(stack);
            String selected = getStringProperty(stack,SELECTED_TAG);
            
            if(curEnergy < getMaxEnergy(stack)){
               playerEntity.displayClientMessage(Component.literal("Planeshifter Recharging: " + (curEnergy * 100 / getMaxEnergy(stack)) + "%").withStyle(ChatFormatting.DARK_AQUA), true);
               SoundUtils.playSongToPlayer((ServerPlayer) playerEntity, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
               return InteractionResult.SUCCESS_SERVER;
            }
            if(selected.isBlank()){
               playerEntity.displayClientMessage(Component.literal("The Planeshifter has not unlocked any other dimensions").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayer) playerEntity, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
               return InteractionResult.SUCCESS_SERVER;
            }
            if(selected.equals(world.dimension().identifier().toString())){
               playerEntity.displayClientMessage(Component.literal("The Planeshifter cannot teleport within the same dimension").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayer) playerEntity, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
               return InteractionResult.SUCCESS_SERVER;
            }
            
            ServerLevel targetWorld = getTargetDim(stack,player.level().getServer());
            if(targetWorld == null){
               playerEntity.displayClientMessage(Component.literal("The Planeshifter cannot find its target").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayer) playerEntity, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
            }else if(!((ServerLevel) playerEntity.level()).isAllowedToEnterPortal(playerEntity.level().getServer().getLevel(targetWorld.dimension()))){
                playerEntity.displayClientMessage(Component.literal("The targeted world is not enabled on this Server").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayer) playerEntity, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
            }else if(curEnergy >= getMaxEnergy(stack)){
               putProperty(stack,HEAT_TAG,1); // Starts the heat up process
               SoundUtils.playSound(playerEntity.level(), playerEntity.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1, 1);
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         int heat = getIntProperty(stack,HEAT_TAG);
         
         String selected = getStringProperty(stack,SELECTED_TAG);
         if(selected.equals(world.dimension().identifier().toString())){
            rotateDimension(stack,player,false);
         }
         
         if(heat == 100){
            teleport(stack,player);
            putProperty(stack,HEAT_TAG,0);
         }else if(heat > 0){
            putProperty(stack,HEAT_TAG,heat+1);
            ArcanaEffectUtils.recallTeleportCharge(serverWorld,player.position());
         }else if(heat == -1){
            // Teleport was cancelled by damage
            ArcanaEffectUtils.recallTeleportCancel(serverWorld,player.position());
            SoundUtils.playSound(player.level(), player.blockPosition(), SoundEvents.ENDERMAN_HURT, SoundSource.PLAYERS, 8,0.8f);
            putProperty(stack,HEAT_TAG,0);
            setEnergy(stack,(int)(getMaxEnergy(stack)*0.75));
         }
         
         if(world.getServer().getTickCount() % 20 == 0){
            unlockDimension(stack,world.dimension(),player);
            List<ItemStack> waystones = ArcanaUtils.getArcanaItems(player,ArcanaRegistry.WAYSTONE);
            for(ItemStack waystone : waystones){
               Waystone.WaystoneTarget target = Waystone.getTarget(waystone);
               if(target != null) unlockDimension(stack, target.world(), player);
            }
            
            addEnergy(stack, 1); // Recharge
            buildItemLore(stack,world.getServer());
         }
      }
      
      private void rotateDimension(ItemStack stack, ServerPlayer player, boolean announce){
         ListTag dimensions = getListProperty(stack,DIMENSIONS_TAG);
         ArrayList<ResourceKey<Level>> dims = new ArrayList<>();
         for(ServerLevel world : player.level().getServer().getAllLevels()){
            if(dimensions.contains(StringTag.valueOf(world.dimension().identifier().toString())) && !player.level().dimension().identifier().equals(world.dimension().identifier())){
               dims.add(world.dimension());
            }
         }
         dims.sort(Comparator.comparing(s -> s.identifier().getPath()));
         String selected = getStringProperty(stack,SELECTED_TAG);
         ResourceKey<Level> world = null;
         if(dims.isEmpty()){
            putProperty(stack,SELECTED_TAG,"");
            buildItemLore(stack,player.level().getServer());
            return;
         }else if(selected.isEmpty()){
            world = dims.getFirst();
         }else{
            int ind = -1;
            for(int i = 0; i < dims.size(); i++){
               if(dims.get(i).identifier().toString().equals(selected)){
                  ind = i;
                  break;
               }
            }
            ind = (ind+1) % dims.size();
            world = dims.get(ind);
         }
         
         if(announce){
            if(world.equals(Level.OVERWORLD)){
               player.displayClientMessage(Component.literal("Planeshifter set to The Overworld").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
            }else if(world.equals(Level.NETHER)){
               player.displayClientMessage(Component.literal("Planeshifter set to The Nether").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
            }else if(world.equals(Level.END)){
               player.displayClientMessage(Component.literal("Planeshifter set to The End").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
            }else{
               player.displayClientMessage(Component.literal("Planeshifter set to "+world.identifier().toString()).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
            }
            SoundUtils.playSongToPlayer(player, SoundEvents.LODESTONE_COMPASS_LOCK, 1, 0.7f);
         }
         putProperty(stack,SELECTED_TAG,world.identifier().toString());
      }
      
      private void unlockDimension(ItemStack stack, ResourceKey<Level> world, ServerPlayer player){
         if(hasDimension(stack,world)) return;
         ListTag dimensions = getListProperty(stack,DIMENSIONS_TAG);
         
         if(world.equals(Level.OVERWORLD)){
            player.displayClientMessage(Component.literal("The Planeshifter has Unlocked The Overworld").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
         }else if(world.equals(Level.NETHER)){
            player.displayClientMessage(Component.literal("The Planeshifter has Unlocked The Nether").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
         }else if(world.equals(Level.END)){
            player.displayClientMessage(Component.literal("The Planeshifter has Unlocked The End").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
         }else{
            player.displayClientMessage(Component.literal("The Planeshifter has Unlocked "+world.identifier().toString()).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
         }
         dimensions.add(StringTag.valueOf(world.identifier().toString()));
         
         SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.3f,2f);
         putProperty(stack,DIMENSIONS_TAG,dimensions);
         buildItemLore(stack,player.level().getServer());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

