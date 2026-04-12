package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticStele;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class MagmaticEversource extends EnergyItem implements GeomanticStele.Interaction {
   public static final String ID = "magmatic_eversource";
   
   public static final String USES_TAG = "charges";
   
   public MagmaticEversource(){
      id = ID;
      name = "Magmatic Eversource";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.LAVA_BUCKET;
      item = new MagmaticEversourceItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.ADVANCEMENT_LAVA_BUCKET, ResearchTasks.ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS, ResearchTasks.UNLOCK_TWILIGHT_ANVIL, ResearchTasks.UNLOCK_AQUATIC_EVERSOURCE};
   }
   
   @Override
   public ItemStack initializeArcanaTag(ItemStack stack){
      super.initializeArcanaTag(stack);
      putProperty(stack, MODE_TAG, 0); // 0 place, 1 remove
      putProperty(stack, USES_TAG, 1);
      return stack;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int mode = getIntProperty(stack, MODE_TAG);
      int charges = getIntProperty(stack, USES_TAG);
      ItemStack newStack = super.updateItem(stack, server);
      putProperty(newStack, MODE_TAG, mode);
      putProperty(newStack, USES_TAG, charges);
      return buildItemLore(newStack, server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Lava ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("is harder to create than ").withStyle(ChatFormatting.RED))
            .append(Component.literal("water").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(", luckily there's a ").withStyle(ChatFormatting.RED))
            .append(Component.literal("dimension ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("made of it.").withStyle(ChatFormatting.RED)));
      lore.add(Component.literal("")
            .append(Component.literal("Unfortunately, it takes ").withStyle(ChatFormatting.RED))
            .append(Component.literal("time ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("to pull ").withStyle(ChatFormatting.RED))
            .append(Component.literal("lava ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("between worlds.").withStyle(ChatFormatting.RED)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" to ").withStyle(ChatFormatting.RED))
            .append(Component.literal("materialize ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("or ").withStyle(ChatFormatting.RED))
            .append(Component.literal("dismiss ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("lava ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("from the world.").withStyle(ChatFormatting.RED)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" to switch between ").withStyle(ChatFormatting.RED))
            .append(Component.literal("placing ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("and ").withStyle(ChatFormatting.RED))
            .append(Component.literal("removing ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("lava").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(".").withStyle(ChatFormatting.RED)));
      
      if(itemStack != null && getMaxCharges(itemStack) > 1){
         int charges = getIntProperty(itemStack, USES_TAG);
         lore.add(Component.literal(""));
         lore.add(Component.literal("")
               .append(Component.literal("Charges ").withStyle(ChatFormatting.GOLD))
               .append(Component.literal("- ").withStyle(ChatFormatting.DARK_RED))
               .append(Component.literal("" + charges).withStyle(ChatFormatting.RED)));
      }
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 30 second recharge time
      int baseCooldown = ArcanaNovum.CONFIG.getInt(ArcanaConfig.MAGMATIC_EVERSOURCE_COOLDOWN);
      int cooldownReduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.MAGMATIC_EVERSOURCE_COOLDOWN_PER_LVL).get(ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.ERUPTION));
      return Math.max(1, baseCooldown - cooldownReduction);
   }
   
   public int getMaxCharges(ItemStack item){
      return ArcanaNovum.CONFIG.getIntList(ArcanaConfig.MAGMATIC_EVERSOURCE_CHARGES_PER_LVL).get(ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.VOLCANIC_CHAMBER));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("      Magmatic\n    Eversource").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nMy inventory issue expands to lava as well as water. Unfortunately, there isn’t lava in the air I can pull from and condense.\nA different solution is in order: The Nether.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Magmatic\n    Eversource").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nA limitless realm of molten rock that I can pull from through a microscopic portal. The only downside is that it takes time to siphon lava through the portal. The Magmatic Eversource functions exactly like the Aquatic  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Magmatic\n    Eversource").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nEversource, however it takes time to recharge after creating lava.\n\nUsing the Eversource will generate or drain lava.\nSneak Using will switch the mode of the Eversource.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
   public Vec3 getBaseRange(){
      return new Vec3(0, 0, 0);
   }
   
   @Override
   public void steleTick(ServerLevel world, GeomanticSteleBlockEntity stele, ItemStack stack, Vec3 range){
      Vec3 stackPos = stele.getBlockPos().getCenter().add(0, 1, 0);
      
      if(world.getRandom().nextFloat() < 0.15){
         world.sendParticles(ParticleTypes.DRIPPING_LAVA, stackPos.x(), stackPos.y(), stackPos.z(), 5, 0.25, 0.25, 0.25, .02);
      }
      if(world.getRandom().nextFloat() < 0.15){
         world.sendParticles(ParticleTypes.FALLING_LAVA, stackPos.x(), stackPos.y(), stackPos.z(), 5, 0.25, 0.25, 0.25, .02);
      }
      
      int charges = getIntProperty(stack, USES_TAG);
      if(world.getServer().getTickCount() % 20 == 0){
         int maxCharges = getMaxCharges(stack);
         if(charges < maxCharges){
            addEnergy(stack, 1); // Recharge
            if(getEnergy(stack) >= getMaxEnergy(stack)){
               setEnergy(stack, 0);
               putProperty(stack, USES_TAG, charges + 1);
               buildItemLore(stack, world.getServer());
            }
            stele.setChanged();
         }
      }
      
      BlockPos geyserPos = stele.getBlockPos().above(5);
      if(world.getFluidState(geyserPos).isSource() && world.getFluidState(geyserPos).is(Fluids.LAVA)) return;
      if(charges > 0 && placeFluid(Fluids.LAVA, null, world, geyserPos, null, true)){
         stele.giveXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_MAGMATIC_EVERSOURCE_USE));
         putProperty(stack, USES_TAG, charges - 1);
         buildItemLore(stack, world.getServer());
         stele.setChanged();
      }
   }
   
   public static boolean placeFluid(Fluid fluid, @Nullable Player player, Level world, BlockPos pos, @Nullable BlockHitResult hitResult, boolean silent){
      boolean bl2;
      if(!(fluid instanceof FlowingFluid flowableFluid)){
         return false;
      }
      BlockState blockState = world.getBlockState(pos);
      Block block = blockState.getBlock();
      boolean bl = blockState.canBeReplaced(fluid);
      bl2 = blockState.isAir() || bl || block instanceof LiquidBlockContainer && ((LiquidBlockContainer) block).canPlaceLiquid(player, world, pos, blockState, fluid);
      if(!bl2){
         return hitResult != null && placeFluid(fluid, player, world, hitResult.getBlockPos().relative(hitResult.getDirection()), null, silent);
      }
      if(block instanceof LiquidBlockContainer fluidFillable){
         if(fluid == Fluids.LAVA){
            fluidFillable.placeLiquid(world, pos, blockState, flowableFluid.getSource(false));
            if(!silent) world.playSound(player, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0f, 1.0f);
            world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
            return true;
         }
      }
      if(!world.isClientSide() && bl && !blockState.liquid()){
         world.destroyBlock(pos, true);
      }
      if(world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE) || blockState.getFluidState().isSource()){
         if(!silent) world.playSound(player, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0f, 1.0f);
         world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
         return true;
      }
      return false;
   }
   
   public class MagmaticEversourceItem extends ArcanaPolymerItem {
      public MagmaticEversourceItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         int mode = getIntProperty(itemStack, MODE_TAG);
         boolean onCD = getIntProperty(itemStack, USES_TAG) <= 0;
         
         List<String> stringList = new ArrayList<>();
         if(!onCD){
            if(mode == 1){
               stringList.add("remove");
            }else{
               stringList.add("place");
            }
         }else{
            if(mode == 1){
               stringList.add("remove_cooldown");
            }else{
               stringList.add("place_cooldown");
            }
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
         if(world.getServer().getTickCount() % 20 == 0){
            int charges = getIntProperty(stack, USES_TAG);
            int maxCharges = getMaxCharges(stack);
            if(charges < maxCharges){
               addEnergy(stack, 1); // Recharge
               if(getEnergy(stack) >= getMaxEnergy(stack)){
                  setEnergy(stack, 0);
                  putProperty(stack, USES_TAG, charges + 1);
                  buildItemLore(stack, entity.level().getServer());
               }
            }
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         int mode = getIntProperty(stack, MODE_TAG);
         int charges = getIntProperty(stack, USES_TAG);
         
         if(playerEntity.isShiftKeyDown()){
            int newMode = (mode + 1) % 2;
            putProperty(stack, MODE_TAG, newMode);
            if(newMode == 1){
               player.sendSystemMessage(Component.literal("The Eversource Evaporates").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BUCKET_EMPTY_LAVA, 1.0f, 1.0f);
            }else{
               player.sendSystemMessage(Component.literal("The Eversource Condenses").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BUCKET_FILL_LAVA, 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS_SERVER;
         }
         
         if(mode != 1 && charges <= 0){
            player.sendSystemMessage(Component.literal("The Eversource is Recharging").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, 0.8f);
            return InteractionResult.PASS;
         }
         
         Fluid fluid = mode == 1 ? Fluids.EMPTY : Fluids.LAVA;
         
         BlockHitResult blockHitResult = BucketItem.getPlayerPOVHitResult(world, playerEntity, fluid == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
         if(blockHitResult.getType() == HitResult.Type.MISS){
            return InteractionResult.PASS;
         }
         if(blockHitResult.getType() == HitResult.Type.BLOCK){
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getDirection();
            BlockPos blockPos2 = blockPos.relative(direction);
            if(!world.mayInteract(playerEntity, blockPos) || !playerEntity.mayUseItemAt(blockPos2, direction, stack)){
               return InteractionResult.FAIL;
            }
            if(fluid == Fluids.EMPTY){
               BucketPickup fluidDrainable;
               BlockState blockState = world.getBlockState(blockPos);
               Block block = blockState.getBlock();
               if(block instanceof BucketPickup && !(fluidDrainable = (BucketPickup) block).pickupBlock(playerEntity, world, blockPos, blockState).isEmpty()){
                  playerEntity.awardStat(Stats.ITEM_USED.get(this));
                  fluidDrainable.getPickupSound().ifPresent(sound -> playerEntity.playSound(sound, 1.0f, 1.0f));
                  world.gameEvent(playerEntity, GameEvent.FLUID_PICKUP, blockPos);
                  return InteractionResult.SUCCESS_SERVER;
               }
               return InteractionResult.FAIL;
            }
            BlockState blockState = world.getBlockState(blockPos);
            BlockPos blockPos3 = blockState.getBlock() instanceof LiquidBlockContainer ? blockPos : blockPos2;
            if(placeFluid(fluid, playerEntity, world, blockPos3, blockHitResult, false)){
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_MAGMATIC_EVERSOURCE_USE)); // Add xp
               ArcanaAchievements.progress(player, ArcanaAchievements.HELLGATE, 1);
               putProperty(stack, USES_TAG, charges - 1);
               buildItemLore(stack, playerEntity.level().getServer());
               playerEntity.awardStat(Stats.ITEM_USED.get(this));
               return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.FAIL;
         }
         return InteractionResult.PASS;
      }
   }
}

