package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticStele;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
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
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AquaticEversource extends ArcanaItem implements GeomanticStele.Interaction{
	public static final String ID = "aquatic_eversource";
   
   public AquaticEversource(){
      id = ID;
      name = "Aquatic Eversource";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity),ArcaneTomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.WATER_BUCKET;
      item = new AquaticEversourceItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_HEART_OF_THE_SEA,ResearchTasks.OBTAIN_BLUE_ICE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,MODE_TAG,0); // 0 place, 1 remove
      setPrefStack(stack);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int mode = getIntProperty(stack,MODE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,MODE_TAG,mode);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Two ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("buckets can make an ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("ocean").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(", but ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("one ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("should be ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("enough").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" to ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("create ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("or ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("evaporate ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("water.").withStyle(ChatFormatting.AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" to switch between ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("placing ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("removing ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("water.").withStyle(ChatFormatting.AQUA)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("       Aquatic\n    Eversource").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nCarrying numerous water buckets is a waste of inventory space.\nA rudimentary contraption capable of portable condensation should alleviate this issue.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("       Aquatic\n    Eversource").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nThe trinket I created can pull water straight from the air to produce limitless water.\n\nA reversal of the process lets me use the trinket to evaporate any fluid type.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("       Aquatic\n    Eversource").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nUsing the Eversource will generate or drain water.\n\nSneak Using will switch the mode of the Eversource.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
   public Vec3 getBaseRange(){
      return new Vec3(0,0,0);
   }
   
   @Override
   public void steleTick(ServerLevel world, GeomanticSteleBlockEntity stele, ItemStack stack, Vec3 range){
      Vec3 stackPos = stele.getBlockPos().getCenter().add(0, 1, 0);
      
      if(world.random.nextFloat() < 0.15){
         world.sendParticles(ParticleTypes.DRIPPING_WATER,stackPos.x(),stackPos.y(),stackPos.z(),5,0.25,0.25,0.25,.02);
      }
      if(world.random.nextFloat() < 0.15){
         world.sendParticles(ParticleTypes.FALLING_WATER,stackPos.x(),stackPos.y(),stackPos.z(),5,0.25,0.25,0.25,.02);
      }
      
      BlockPos geyserPos = stele.getBlockPos().above(5);
      if(world.getFluidState(geyserPos).isSource() && world.getFluidState(geyserPos).is(Fluids.WATER)) return;
      int mode = getIntProperty(stack,MODE_TAG);
      int placeStatus = placeFluid(Fluids.WATER,null, world, geyserPos, null, false, true);
      if(placeStatus > 0){
         if(mode == 2 && placeStatus == 2){
            for(BlockPos floodPos : BlockPos.betweenClosed(geyserPos.offset(-1, 0, -1), geyserPos.offset(1, 0, 1))){
               if(floodPos.equals(geyserPos)) continue;
               if(placeFluid(Fluids.WATER,null, world, floodPos, null, true, true) > 0){
                  stele.giveXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_AQUATIC_EVERSOURCE_USE));
               }
            }
         }
         stele.giveXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_AQUATIC_EVERSOURCE_USE));
      }
   }
   
   public static int placeFluid(Fluid fluid, @Nullable Player player, Level world, BlockPos pos, @Nullable BlockHitResult hitResult, boolean flood, boolean silent){
      LiquidBlockContainer fluidFillable;
      boolean bl2;
      if(!(fluid instanceof FlowingFluid flowableFluid)){
         return 0;
      }
      BlockState blockState = world.getBlockState(pos);
      Block block = blockState.getBlock();
      boolean bl = blockState.canBeReplaced(fluid);
      bl2 = blockState.isAir() || bl || block instanceof LiquidBlockContainer && ((LiquidBlockContainer) block).canPlaceLiquid(player, world, pos, blockState, fluid) && !flood;
      if(!bl2){
         return hitResult == null ? 0 : placeFluid(fluid,player, world, hitResult.getBlockPos().relative(hitResult.getDirection()), null, flood, silent);
      }
      
      if(world.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos) && fluid.is(FluidTags.WATER)){
         int i = pos.getX();
         int j = pos.getY();
         int k = pos.getZ();
         if(!silent) world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
         for (int l = 0; l < 8; ++l){
            world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0, 0.0, 0.0);
         }
         return 1;
      }
      if(block instanceof LiquidBlockContainer && !flood){
         fluidFillable = (LiquidBlockContainer) block;
         if(fluid == Fluids.WATER){
            fluidFillable.placeLiquid(world, pos, blockState, flowableFluid.getSource(false));
            if(!silent) world.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
            return 1;
         }
      }
      if(!world.isClientSide() && bl && !blockState.liquid()){
         world.destroyBlock(pos, true);
      }
      if(world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE) || blockState.getFluidState().isSource()){
         if(!flood){
            if(!silent) world.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
         }
         world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
         return 2;
      }
      return 0;
   }
   
   public class AquaticEversourceItem extends ArcanaPolymerItem {
      public AquaticEversourceItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         int mode = getIntProperty(itemStack,MODE_TAG);
         
         List<String> stringList = new ArrayList<>();
         if(mode == 1){
            stringList.add("remove");
         }else{
            stringList.add("place");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         int mode = getIntProperty(stack,MODE_TAG);
         boolean floodgate = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.FLOODGATE) > 0;
         
         if(playerEntity.isShiftKeyDown()){
            int newMode = (mode+1) % (floodgate ? 3 : 2);
            putProperty(stack,MODE_TAG,newMode);
            if(newMode == 1){
               player.displayClientMessage(Component.literal("The Eversource Evaporates").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BUCKET_EMPTY,1.0f,1.0f);
            }else if(newMode == 2){
               player.displayClientMessage(Component.literal("The Eversource Swells").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BUCKET_FILL,1.0f,1.0f);
            }else{
               player.displayClientMessage(Component.literal("The Eversource Condenses").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BUCKET_FILL,1.0f,1.0f);
            }
            return InteractionResult.SUCCESS_SERVER;
         }
         
         Fluid fluid = mode == 1 ? Fluids.EMPTY : Fluids.WATER;
         
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
            int placeStatus = placeFluid(fluid,playerEntity, world, blockPos3, blockHitResult, false, false);
            if(placeStatus > 0){
               if(mode == 2 && placeStatus == 2){
                  for(BlockPos floodPos : BlockPos.betweenClosed(blockPos3.offset(-1, 0, -1), blockPos3.offset(1, 0, 1))){
                     if(floodPos.equals(blockPos3)) continue;
                     if(placeFluid(fluid,playerEntity, world, floodPos, null, true, false) > 0){
                        ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_AQUATIC_EVERSOURCE_USE)); // Add xp
                        ArcanaAchievements.progress(player,ArcanaAchievements.POCKET_OCEAN,1);
                     }
                  }
               }
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_AQUATIC_EVERSOURCE_USE)); // Add xp
               ArcanaAchievements.progress(player,ArcanaAchievements.POCKET_OCEAN,1);
               playerEntity.awardStat(Stats.ITEM_USED.get(this));
               playerEntity.getCooldowns().addCooldown(stack,5);
               return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.FAIL;
         }
         return InteractionResult.PASS;
      }
   }
}

