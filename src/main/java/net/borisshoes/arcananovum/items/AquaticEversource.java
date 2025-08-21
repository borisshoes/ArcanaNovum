package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AquaticEversource extends ArcanaItem {
	public static final String ID = "aquatic_eversource";
   
   public AquaticEversource(){
      id = ID;
      name = "Aquatic Eversource";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity),TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.WATER_BUCKET;
      item = new AquaticEversourceItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.BLUE);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_HEART_OF_THE_SEA,ResearchTasks.OBTAIN_BLUE_ICE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
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
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Two ").formatted(Formatting.BLUE))
            .append(Text.literal("buckets can make an ").formatted(Formatting.AQUA))
            .append(Text.literal("ocean").formatted(Formatting.BLUE))
            .append(Text.literal(", but ").formatted(Formatting.AQUA))
            .append(Text.literal("one ").formatted(Formatting.BLUE))
            .append(Text.literal("should be ").formatted(Formatting.AQUA))
            .append(Text.literal("enough").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.BLUE))
            .append(Text.literal(" to ").formatted(Formatting.AQUA))
            .append(Text.literal("create ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("or ").formatted(Formatting.AQUA))
            .append(Text.literal("evaporate ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("water.").formatted(Formatting.AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.BLUE))
            .append(Text.literal(" to switch between ").formatted(Formatting.AQUA))
            .append(Text.literal("placing ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("and ").formatted(Formatting.AQUA))
            .append(Text.literal("removing ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("water.").formatted(Formatting.AQUA)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.BUCKET,8);
      ArcanaIngredient b = new ArcanaIngredient(Items.GOLD_INGOT,6);
      ArcanaIngredient c = new ArcanaIngredient(Items.BLUE_ICE,4);
      ArcanaIngredient h = new ArcanaIngredient(Items.DIAMOND,2);
      ArcanaIngredient m = new ArcanaIngredient(Items.HEART_OF_THE_SEA,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,c,h,c,b},
            {c,h,m,h,c},
            {b,c,h,c,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("       Aquatic\n    Eversource").formatted(Formatting.BLUE,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nCarrying numerous water buckets is a waste of inventory space.\nA rudimentary contraption capable of portable condensation should alleviate this issue.\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("       Aquatic\n    Eversource").formatted(Formatting.BLUE,Formatting.BOLD),Text.literal("\nThe trinket I created can pull water straight from the air to produce limitless water.\n\nA reversal of the process lets me use the trinket to evaporate any fluid type.\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("       Aquatic\n    Eversource").formatted(Formatting.BLUE,Formatting.BOLD),Text.literal("\nUsing the Eversource will generate or drain water.\n\nSneak Using will switch the mode of the Eversource.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class AquaticEversourceItem extends ArcanaPolymerItem {
      public AquaticEversourceItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         int mode = getIntProperty(itemStack,MODE_TAG);
         
         List<String> stringList = new ArrayList<>();
         if(mode == 1){
            stringList.add("remove");
         }else{
            stringList.add("place");
         }
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         int mode = getIntProperty(stack,MODE_TAG);
         boolean floodgate = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.FLOODGATE.id) > 0;
         
         if(playerEntity.isSneaking()){
            int newMode = (mode+1) % (floodgate ? 3 : 2);
            putProperty(stack,MODE_TAG,newMode);
            if(newMode == 1){
               player.sendMessage(Text.literal("The Eversource Evaporates").formatted(Formatting.BLUE,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUCKET_EMPTY,1.0f,1.0f);
            }else if(newMode == 2){
               player.sendMessage(Text.literal("The Eversource Swells").formatted(Formatting.BLUE,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUCKET_FILL,1.0f,1.0f);
            }else{
               player.sendMessage(Text.literal("The Eversource Condenses").formatted(Formatting.BLUE,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUCKET_FILL,1.0f,1.0f);
            }
            return ActionResult.SUCCESS_SERVER;
         }
         
         Fluid fluid = mode == 1 ? Fluids.EMPTY : Fluids.WATER;
         
         BlockHitResult blockHitResult = BucketItem.raycast(world, playerEntity, fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE);
         if(blockHitResult.getType() == HitResult.Type.MISS){
            return ActionResult.PASS;
         }
         if(blockHitResult.getType() == HitResult.Type.BLOCK){
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if(!world.canEntityModifyAt(playerEntity, blockPos) || !playerEntity.canPlaceOn(blockPos2, direction, stack)){
               return ActionResult.FAIL;
            }
            if(fluid == Fluids.EMPTY){
               FluidDrainable fluidDrainable;
               BlockState blockState = world.getBlockState(blockPos);
               Block block = blockState.getBlock();
               if(block instanceof FluidDrainable && !(fluidDrainable = (FluidDrainable) block).tryDrainFluid(playerEntity, world, blockPos, blockState).isEmpty()){
                  playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
                  fluidDrainable.getBucketFillSound().ifPresent(sound -> playerEntity.playSound(sound, 1.0f, 1.0f));
                  world.emitGameEvent(playerEntity, GameEvent.FLUID_PICKUP, blockPos);
                  return ActionResult.SUCCESS_SERVER;
               }
               return ActionResult.FAIL;
            }
            BlockState blockState = world.getBlockState(blockPos);
            BlockPos blockPos3 = blockState.getBlock() instanceof FluidFillable ? blockPos : blockPos2;
            int placeStatus = placeFluid(fluid,playerEntity, world, blockPos3, blockHitResult, false);
            if(placeStatus > 0){
               if(mode == 2 && placeStatus == 2){
                  for(BlockPos floodPos : BlockPos.iterate(blockPos3.add(-1, 0, -1), blockPos3.add(1, 0, 1))){
                     if(floodPos.equals(blockPos3)) continue;
                     if(placeFluid(fluid,playerEntity, world, floodPos, null, true) > 0){
                        ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.AQUATIC_EVERSOURCE_USE)); // Add xp
                        ArcanaAchievements.progress(player,ArcanaAchievements.POCKET_OCEAN.id,1);
                     }
                  }
               }
               ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.AQUATIC_EVERSOURCE_USE)); // Add xp
               ArcanaAchievements.progress(player,ArcanaAchievements.POCKET_OCEAN.id,1);
               playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
               playerEntity.getItemCooldownManager().set(stack,5);
               return ActionResult.SUCCESS_SERVER;
            }
            return ActionResult.FAIL;
         }
         return ActionResult.PASS;
      }
      
      
      public int placeFluid(Fluid fluid, @Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult, boolean flood){
         FluidFillable fluidFillable;
         boolean bl2;
         if(!(fluid instanceof FlowableFluid flowableFluid)){
            return 0;
         }
         BlockState blockState = world.getBlockState(pos);
         Block block = blockState.getBlock();
         boolean bl = blockState.canBucketPlace(fluid);
         bl2 = blockState.isAir() || bl || block instanceof FluidFillable && ((FluidFillable) block).canFillWithFluid(player, world, pos, blockState, fluid) && !flood;
         if(!bl2){
            return hitResult == null ? 0 : placeFluid(fluid,player, world, hitResult.getBlockPos().offset(hitResult.getSide()), null, flood);
         }
         if(world.getDimension().ultrawarm() && fluid.isIn(FluidTags.WATER)){
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
            for (int l = 0; l < 8; ++l){
               world.addParticleClient(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0, 0.0, 0.0);
            }
            return 1;
         }
         if(block instanceof FluidFillable && !flood){
            fluidFillable = (FluidFillable) block;
            if(fluid == Fluids.WATER){
               fluidFillable.tryFillWithFluid(world, pos, blockState, flowableFluid.getStill(false));
               world.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
               return 1;
            }
         }
         if(!world.isClient && bl && !blockState.isLiquid()){
            world.breakBlock(pos, true);
         }
         if(world.setBlockState(pos, fluid.getDefaultState().getBlockState(), Block.NOTIFY_ALL_AND_REDRAW) || blockState.getFluidState().isStill()){
            if(!flood){
               world.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
            return 2;
         }
         return 0;
      }
   }
}

