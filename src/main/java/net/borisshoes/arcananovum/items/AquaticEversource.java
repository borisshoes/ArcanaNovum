package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class AquaticEversource extends MagicItem {
   
   private static final String TXT_PLACE = "item/aquatic_eversource_place";
   private static final String TXT_REMOVE = "item/aquatic_eversource_remove";
   
   public AquaticEversource(){
      id = "aquatic_eversource";
      name = "Aquatic Eversource";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED,ArcaneTome.TomeFilter.ITEMS};
      vanillaItem = Items.WATER_BUCKET;
      item = new AquaticEversourceItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT_PLACE));
      models.add(new Pair<>(vanillaItem,TXT_REMOVE));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Aquatic Eversource\",\"italic\":false,\"bold\":true,\"color\":\"blue\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      addMagicNbt(tag);
      tag.getCompound("arcananovum").putInt("mode",0); // 0 place, 1 remove
      stack.setNbt(tag);
      setPrefStack(stack);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      int mode = magicTag.getInt("mode");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").putInt("mode",mode);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"Two \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"buckets can make an \",\"color\":\"aqua\"},{\"text\":\"ocean\"},{\"text\":\", but \",\"color\":\"aqua\"},{\"text\":\"one \"},{\"text\":\"should be \",\"color\":\"aqua\"},{\"text\":\"enough\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"blue\"},{\"text\":\" to \",\"color\":\"aqua\"},{\"text\":\"create \",\"color\":\"dark_aqua\"},{\"text\":\"or \",\"color\":\"aqua\"},{\"text\":\"evaporate \",\"color\":\"dark_aqua\"},{\"text\":\"water.\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"blue\"},{\"text\":\" to switch between \",\"color\":\"aqua\"},{\"text\":\"placing \",\"color\":\"dark_aqua\"},{\"text\":\"and \",\"color\":\"aqua\"},{\"text\":\"removing \",\"color\":\"dark_aqua\"},{\"text\":\"water.\",\"color\":\"aqua\"}]"));
      return loreList;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.BUCKET,16,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.GOLD_INGOT,32,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.DIAMOND,4,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.BLUE_ICE,64,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.HEART_OF_THE_SEA,4,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,c,h,c,b},
            {c,h,m,h,c},
            {b,c,h,c,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients,new ForgeRequirement());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("\"  Aquatic Eversource\\n\\nRarity: Empowered\\n\\nCarrying numerous water buckets is a waste of inventory space.\\n\\nA rudimentary contraption capable of condensation should alleviate this issue. \"");
      list.add("\"  Aquatic Eversource\\n\\nThe trinket I created can pull water straight from the air to produce limitless water sources.\\n\\nA reversal of the process lets me use the trinket to remove any type of fluid as well.\"");
      return list;
   }
   
   public class AquaticEversourceItem extends MagicPolymerItem {
      public AquaticEversourceItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return ArcanaRegistry.MODELS.get(TXT_PLACE).value();
         int mode = itemStack.getNbt().getCompound("arcananovum").getInt("mode");
         return mode == 1 ? ArcanaRegistry.MODELS.get(TXT_REMOVE).value() : ArcanaRegistry.MODELS.get(TXT_PLACE).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return TypedActionResult.pass(stack);
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         boolean floodgate = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.FLOODGATE.id) > 0;
         
         if(playerEntity.isSneaking()){
            int newMode = (mode+1) % (floodgate ? 3 : 2);
            magicNbt.putInt("mode",newMode);
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
            return TypedActionResult.success(stack);
         }
         
         Fluid fluid = mode == 1 ? Fluids.EMPTY : Fluids.WATER;
         
         BlockHitResult blockHitResult = BucketItem.raycast(world, playerEntity, fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE);
         if (blockHitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(stack);
         }
         if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (!world.canPlayerModifyAt(playerEntity, blockPos) || !playerEntity.canPlaceOn(blockPos2, direction, stack)) {
               return TypedActionResult.fail(stack);
            }
            if (fluid == Fluids.EMPTY) {
               FluidDrainable fluidDrainable;
               BlockState blockState = world.getBlockState(blockPos);
               Block block = blockState.getBlock();
               if (block instanceof FluidDrainable && !(fluidDrainable = (FluidDrainable) block).tryDrainFluid(playerEntity, world, blockPos, blockState).isEmpty()) {
                  playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
                  fluidDrainable.getBucketFillSound().ifPresent(sound -> playerEntity.playSound(sound, 1.0f, 1.0f));
                  world.emitGameEvent(playerEntity, GameEvent.FLUID_PICKUP, blockPos);
                  if (!world.isClient) {
                     PLAYER_DATA.get(player).addXP(1); // Add xp
                  }
                  return TypedActionResult.success(stack);
               }
               return TypedActionResult.fail(stack);
            }
            BlockState blockState = world.getBlockState(blockPos);
            BlockPos blockPos3 = blockState.getBlock() instanceof FluidFillable ? blockPos : blockPos2;
            int placeStatus = placeFluid(fluid,playerEntity, world, blockPos3, blockHitResult, false);
            if (placeStatus > 0) {
               if(mode == 2 && placeStatus == 2){
                  for(BlockPos floodPos : BlockPos.iterate(blockPos3.add(-1, 0, -1), blockPos3.add(1, 0, 1))){
                     if(floodPos.equals(blockPos3)) continue;
                     if(placeFluid(fluid,playerEntity, world, floodPos, null, true) > 0){
                        PLAYER_DATA.get(player).addXP(5); // Add xp
                        ArcanaAchievements.progress(player,ArcanaAchievements.POCKET_OCEAN.id,1);
                     }
                  }
               }
               PLAYER_DATA.get(player).addXP(5); // Add xp
               ArcanaAchievements.progress(player,ArcanaAchievements.POCKET_OCEAN.id,1);
               playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
               return TypedActionResult.success(stack);
            }
            return TypedActionResult.fail(stack);
         }
         return TypedActionResult.pass(stack);
      }
      
      
      public int placeFluid(Fluid fluid, @Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult, boolean flood) {
         FluidFillable fluidFillable;
         boolean bl2;
         if (!(fluid instanceof FlowableFluid flowableFluid)) {
            return 0;
         }
         BlockState blockState = world.getBlockState(pos);
         Block block = blockState.getBlock();
         boolean bl = blockState.canBucketPlace(fluid);
         bl2 = blockState.isAir() || bl || block instanceof FluidFillable && ((FluidFillable) block).canFillWithFluid(player, world, pos, blockState, fluid) && !flood;
         if (!bl2) {
            return hitResult == null ? 0 : placeFluid(fluid,player, world, hitResult.getBlockPos().offset(hitResult.getSide()), null, flood);
         }
         if (world.getDimension().ultrawarm() && fluid.isIn(FluidTags.WATER)) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
            for (int l = 0; l < 8; ++l) {
               world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0, 0.0, 0.0);
            }
            return 1;
         }
         if (block instanceof FluidFillable && !flood) {
            fluidFillable = (FluidFillable) block;
            if (fluid == Fluids.WATER) {
               fluidFillable.tryFillWithFluid(world, pos, blockState, flowableFluid.getStill(false));
               world.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
               return 1;
            }
         }
         if (!world.isClient && bl && !blockState.isLiquid()) {
            world.breakBlock(pos, true);
         }
         if (world.setBlockState(pos, fluid.getDefaultState().getBlockState(), Block.NOTIFY_ALL_AND_REDRAW) || blockState.getFluidState().isStill()) {
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
