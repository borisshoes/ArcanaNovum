package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
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
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      initEnergy = 600;
      vanillaItem = Items.RECOVERY_COMPASS;
      item = new PlaneshifterItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_PURPLE);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.DIMENSION_TRAVEL, ResearchTasks.OBTAIN_EYE_OF_ENDER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,DIMENSIONS_TAG, new NbtList());
      putProperty(stack,SELECTED_TAG,"");
      putProperty(stack,HEAT_TAG,0);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A handheld ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("End").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" and ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Nether ").formatted(Formatting.RED))
            .append(Text.literal("Portal").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("shifter ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("must be taken to each ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Dimension ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("to unlock their ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("mode").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("When in ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Nether mode").formatted(Formatting.RED))
            .append(Text.literal(", the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("shifter ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("tries to find the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("nearest portal").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("When in ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("End mode").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(", the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("shifter teleports").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" between ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("world spawns").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("It requires the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("flowing of time").formatted(Formatting.BLUE))
            .append(Text.literal(" to ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("recharge it.").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("activate ").formatted(Formatting.BLUE))
            .append(Text.literal("the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("shifter").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("switch modes").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal(""));
      
      if(itemStack == null){
         lore.add(Text.literal("")
               .append(Text.literal("Charged - ").formatted(Formatting.DARK_PURPLE))
               .append(Text.literal("100%").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE)));
      }else{
         int charge = (getEnergy(itemStack)*100/getMaxEnergy(itemStack));
         String charging = charge == 100 ? "Charged" : "Charging";
         lore.add(Text.literal("")
               .append(Text.literal(charging+" - ").formatted(Formatting.DARK_PURPLE))
               .append(Text.literal(charge+"%").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE)));
      }

     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean hasNether = getBooleanProperty(stack,NETHER_UNLOCK_TAG);
      boolean hasEnd = getBooleanProperty(stack,END_UNLOCK_TAG);
      int heat = getIntProperty(stack,HEAT_TAG);
      NbtList dimensions = getListProperty(stack,DIMENSIONS_TAG);
      String selected = getStringProperty(stack,SELECTED_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      if(hasNether && !dimensions.contains(NbtString.of(ServerWorld.NETHER.getValue().toString()))){
         dimensions.add(NbtString.of(ServerWorld.NETHER.getValue().toString()));
      }
      if(hasEnd && !dimensions.contains(NbtString.of(ServerWorld.END.getValue().toString()))){
         dimensions.add(NbtString.of(ServerWorld.END.getValue().toString()));
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
      int cdLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.PLANAR_FLOW.id));
      return 600 - cdReduction[cdLvl];
   }
   
   private void findPortalAndTeleport(ServerPlayerEntity player, ServerWorld destWorld, boolean destIsNether){
      double scale = DimensionType.getCoordinateScaleFactor(player.getWorld().getDimension(), destWorld.getDimension());
      WorldBorder worldBorder = destWorld.getWorldBorder();
      Vec3d destPos = worldBorder.clamp(player.getX() * scale, player.getY(), player.getZ() * scale);
      Optional<BlockPos> portalRect = destWorld.getPortalForcer().getPortalPos(BlockPos.ofFloored(destPos), destIsNether, worldBorder);
      if(portalRect.isPresent()){
         player.teleportTo(new TeleportTarget(destWorld,portalRect.get().toCenterPos(),player.getVelocity(),player.getYaw(),player.getPitch(),TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(entityx -> entityx.addPortalChunkTicketAt(portalRect.get()))));
         player.resetPortalCooldown();
         player.sendMessage(Text.literal("The Planeshifter syncs up with a Nether Portal").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
      }else{
         player.sendMessage(Text.literal("The Planeshifter could not find a Nether Portal").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
         directTeleport(player,destWorld);
      }
   }
   
   private void directTeleport(ServerPlayerEntity player, ServerWorld destWorld){
      double scale = DimensionType.getCoordinateScaleFactor(player.getWorld().getDimension(), destWorld.getDimension());
      WorldBorder worldBorder = destWorld.getWorldBorder();
      Vec3d destPos = worldBorder.clamp(player.getX() * scale, player.getY(), player.getZ() * scale);
      player.teleportTo(new TeleportTarget(destWorld, destPos, Vec3d.ZERO, player.getYaw(),player.getPitch(), TeleportTarget.ADD_PORTAL_CHUNK_TICKET));
      for(int y = player.getBlockY(); y >= player.getBlockY()-destWorld.getHeight(); y--){
         BlockPos blockPos = new BlockPos(player.getBlockX(),y,player.getBlockZ());
         BlockState state = destWorld.getBlockState(blockPos);
         if(state.isOf(Blocks.LAVA)){
            ArcanaAchievements.grant(player,ArcanaAchievements.UNFORTUNATE_MATERIALIZATION.id);
            break;
         }else if(!(state.isAir() || state.getCollisionShape(destWorld,blockPos).isEmpty())){
            break;
         }
      }
   }
   
   private void teleport(ItemStack stack, ServerPlayerEntity player){
      ServerWorld world = player.getWorld();
      ServerWorld target = getTargetDim(stack,world.getServer());
      if(target == null) return;
      
      boolean inOverworld = world.getRegistryKey().getValue().equals(ServerWorld.OVERWORLD.getValue());
      boolean inEnd = world.getRegistryKey().getValue().equals(ServerWorld.END.getValue());
      boolean inNether = world.getRegistryKey().getValue().equals(ServerWorld.NETHER.getValue());
      if(inNether) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "From The Nether",true);
      if(inEnd) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id,"From The End",true);
      if(inOverworld) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id,"From The Overworld",true);
      boolean destOverworld = target.getRegistryKey().getValue().equals(ServerWorld.OVERWORLD.getValue());
      boolean destEnd = target.getRegistryKey().getValue().equals(ServerWorld.END.getValue());
      boolean destNether = target.getRegistryKey().getValue().equals(ServerWorld.NETHER.getValue());
      if(destNether) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "To The Nether",true);
      if(destEnd) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id,"To The End",true);
      if(destOverworld) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id,"To The Overworld",true);
      
      if(inNether && destOverworld){
         findPortalAndTeleport(player,target,false);
      }else if(destNether){
         findPortalAndTeleport(player,target,true);
      }else if(destEnd || (inEnd && destOverworld)){
         EndPortalBlock endPortalBlock = (EndPortalBlock) Blocks.END_PORTAL;
         player.teleportTo(endPortalBlock.createTeleportTarget(player.getWorld(),player,player.getBlockPos()));
      }else{
         directTeleport(player,target);
      }
      
      ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.PLANESHIFTER_USE)); // Add xp
      setEnergy(stack,0);
      SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_PORTAL_TRAVEL,1,2f);
      ArcanaEffectUtils.recallTeleport(world,player.getPos());
   }
   
   private ServerWorld getTargetDim(ItemStack stack, MinecraftServer server){
      String selected = getStringProperty(stack,SELECTED_TAG);
      for(ServerWorld serverWorld : server.getWorlds()){
         if(serverWorld.getRegistryKey().getValue().toString().equals(selected)){
            return serverWorld;
         }
      }
      return null;
   }
   
   public boolean hasDimension(ItemStack stack, RegistryKey<World> world){
      return getListProperty(stack,DIMENSIONS_TAG).contains(NbtString.of(world.getValue().toString()));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,12);
      ArcanaIngredient c = new ArcanaIngredient(Items.ENDER_EYE,8);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHER_STAR,1);
      ArcanaIngredient h = new ArcanaIngredient(Items.END_CRYSTAL,8);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Planeshifter").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nPortals are nice, they create a safe, stable connection between worlds, but they take setup. Safety is not a concern for an Arcanist of my caliber. I can just make an unstable rift long enough to slip ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Planeshifter").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nthrough.\n\nHowever, the Shifter needs some dimensional energy from the destination before it can rip open a rift to the destination.\n\nFor some fraction of safety, if the Shifter \n\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Planeshifter").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nfinds a portal near the destination, it should sync my rift to its location.\n\nSneak Use to switch the Shifter’s target dimension.\n\nUsing the Shifter activates its warmup.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Planeshifter").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nTaking damage prematurely disrupts the rift, causing the Shifter to require time to recalibrate.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class PlaneshifterItem extends ArcanaPolymerItem {
      public PlaneshifterItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         List<String> stringList = new ArrayList<>();
         
         String selected = getStringProperty(itemStack,SELECTED_TAG);
         if(context.getPlayer() != null){
            if(getEnergy(itemStack) < getMaxEnergy(itemStack) || selected.isBlank()){
               stringList.add("none");
            }else if(selected.equals(World.OVERWORLD.getValue().toString())){
               stringList.add("overworld");
            }else if(selected.equals(World.END.getValue().toString())){
               stringList.add("end");
            }else if(selected.equals(World.NETHER.getValue().toString())){
               stringList.add("nether");
            }else{
               stringList.add("other");
            }
         }else{
            stringList.add("none");
         }
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!ArcanaItemUtils.isArcane(stack) || !(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         
         if(playerEntity.isSneaking()){
            rotateDimension(stack,player,true);
         }else{
            int curEnergy = getEnergy(stack);
            String selected = getStringProperty(stack,SELECTED_TAG);
            
            if(curEnergy < getMaxEnergy(stack)){
               playerEntity.sendMessage(Text.literal("Planeshifter Recharging: " + (curEnergy * 100 / getMaxEnergy(stack)) + "%").formatted(Formatting.DARK_AQUA), true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
               return ActionResult.SUCCESS_SERVER;
            }
            if(selected.isBlank()){
               playerEntity.sendMessage(Text.literal("The Planeshifter has not unlocked any other dimensions").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
               return ActionResult.SUCCESS_SERVER;
            }
            if(selected.equals(world.getRegistryKey().getValue().toString())){
               playerEntity.sendMessage(Text.literal("The Planeshifter cannot teleport within the same dimension").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
               return ActionResult.SUCCESS_SERVER;
            }
            
            ServerWorld targetWorld = getTargetDim(stack,player.getServer());
            if(targetWorld == null){
               playerEntity.sendMessage(Text.literal("The Planeshifter cannot find its target").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
            }else if(!playerEntity.getServer().isWorldAllowed(playerEntity.getServer().getWorld(targetWorld.getRegistryKey()))){
               playerEntity.sendMessage(Text.literal("The targeted world is not enabled on this Server").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
            }else if(curEnergy >= getMaxEnergy(stack)){
               putProperty(stack,HEAT_TAG,1); // Starts the heat up process
               SoundUtils.playSound(playerEntity.getWorld(), playerEntity.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1, 1);
            }
         }
         return ActionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         int heat = getIntProperty(stack,HEAT_TAG);
         
         String selected = getStringProperty(stack,SELECTED_TAG);
         if(selected.equals(world.getRegistryKey().getValue().toString())){
            rotateDimension(stack,player,false);
         }
         
         if(heat == 100){
            teleport(stack,player);
            putProperty(stack,HEAT_TAG,0);
         }else if(heat > 0){
            putProperty(stack,HEAT_TAG,heat+1);
            ArcanaEffectUtils.recallTeleportCharge(serverWorld,player.getPos());
         }else if(heat == -1){
            // Teleport was cancelled by damage
            ArcanaEffectUtils.recallTeleportCancel(serverWorld,player.getPos());
            SoundUtils.playSound(player.getWorld(), player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_HURT, SoundCategory.PLAYERS, 8,0.8f);
            putProperty(stack,HEAT_TAG,0);
            setEnergy(stack,(int)(getMaxEnergy(stack)*0.75));
         }
         
         if(world.getServer().getTicks() % 20 == 0){
            unlockDimension(stack,world.getRegistryKey(),player);
            List<ItemStack> waystones = ArcanaUtils.getArcanaItems(player,ArcanaRegistry.WAYSTONE);
            for(ItemStack waystone : waystones){
               Waystone.WaystoneTarget target = Waystone.getTarget(waystone);
               if(target != null) unlockDimension(stack, target.world(), player);
            }
            
            addEnergy(stack, 1); // Recharge
            buildItemLore(stack,world.getServer());
         }
      }
      
      private void rotateDimension(ItemStack stack, ServerPlayerEntity player, boolean announce){
         NbtList dimensions = getListProperty(stack,DIMENSIONS_TAG);
         ArrayList<RegistryKey<World>> dims = new ArrayList<>();
         for(ServerWorld world : player.getServer().getWorlds()){
            if(dimensions.contains(NbtString.of(world.getRegistryKey().getValue().toString())) && !player.getWorld().getRegistryKey().getValue().equals(world.getRegistryKey().getValue())){
               dims.add(world.getRegistryKey());
            }
         }
         dims.sort(Comparator.comparing(s -> s.getValue().getPath()));
         String selected = getStringProperty(stack,SELECTED_TAG);
         RegistryKey<World> world = null;
         if(dims.isEmpty()){
            putProperty(stack,SELECTED_TAG,"");
            buildItemLore(stack,player.getServer());
            return;
         }else if(selected.isEmpty()){
            world = dims.getFirst();
         }else{
            int ind = -1;
            for(int i = 0; i < dims.size(); i++){
               if(dims.get(i).getValue().toString().equals(selected)){
                  ind = i;
                  break;
               }
            }
            ind = (ind+1) % dims.size();
            world = dims.get(ind);
         }
         
         if(announce){
            if(world.equals(World.OVERWORLD)){
               player.sendMessage(Text.literal("Planeshifter set to The Overworld").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            }else if(world.equals(World.NETHER)){
               player.sendMessage(Text.literal("Planeshifter set to The Nether").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            }else if(world.equals(World.END)){
               player.sendMessage(Text.literal("Planeshifter set to The End").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            }else{
               player.sendMessage(Text.literal("Planeshifter set to "+world.getValue().toString()).formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            }
            SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, 1, 0.7f);
         }
         putProperty(stack,SELECTED_TAG,world.getValue().toString());
      }
      
      private void unlockDimension(ItemStack stack, RegistryKey<World> world, ServerPlayerEntity player){
         if(hasDimension(stack,world)) return;
         NbtList dimensions = getListProperty(stack,DIMENSIONS_TAG);
         
         if(world.equals(World.OVERWORLD)){
            player.sendMessage(Text.literal("The Planeshifter has Unlocked The Overworld").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
         }else if(world.equals(World.NETHER)){
            player.sendMessage(Text.literal("The Planeshifter has Unlocked The Nether").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
         }else if(world.equals(World.END)){
            player.sendMessage(Text.literal("The Planeshifter has Unlocked The End").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
         }else{
            player.sendMessage(Text.literal("The Planeshifter has Unlocked "+world.getValue().toString()).formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
         }
         dimensions.add(NbtString.of(world.getValue().toString()));
         
         SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.3f,2f);
         putProperty(stack,DIMENSIONS_TAG,dimensions);
         buildItemLore(stack,player.getServer());
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

