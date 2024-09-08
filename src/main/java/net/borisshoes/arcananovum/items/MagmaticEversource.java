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
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
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
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class MagmaticEversource extends EnergyItem {
	public static final String ID = "magmatic_eversource";
   
   public static final String USES_TAG = "charges";
   
   private static final String TXT_PLACE = "item/magmatic_eversource_place";
   private static final String TXT_REMOVE = "item/magmatic_eversource_remove";
   private static final String TXT_PLACE_COOLDOWN = "item/magmatic_eversource_place_cooldown";
   private static final String TXT_REMOVE_COOLDOWN = "item/magmatic_eversource_remove_cooldown";
   
   public MagmaticEversource(){
      id = ID;
      name = "Magmatic Eversource";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC,TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.LAVA_BUCKET;
      item = new MagmaticEversourceItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.GOLD))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT_PLACE));
      models.add(new Pair<>(vanillaItem,TXT_REMOVE));
      models.add(new Pair<>(vanillaItem,TXT_PLACE_COOLDOWN));
      models.add(new Pair<>(vanillaItem,TXT_REMOVE_COOLDOWN));
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_LAVA_BUCKET,ResearchTasks.ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,MODE_TAG,0); // 0 place, 1 remove
      putProperty(stack,USES_TAG,1);
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int mode = getIntProperty(stack,MODE_TAG);
      int charges = getIntProperty(stack,USES_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,MODE_TAG,mode);
      putProperty(newStack,USES_TAG,charges);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Lava ").formatted(Formatting.GOLD))
            .append(Text.literal("is harder to create than ").formatted(Formatting.RED))
            .append(Text.literal("water").formatted(Formatting.BLUE))
            .append(Text.literal(", luckily there's a ").formatted(Formatting.RED))
            .append(Text.literal("dimension ").formatted(Formatting.DARK_RED))
            .append(Text.literal("made of it.").formatted(Formatting.RED)));
      lore.add(Text.literal("")
            .append(Text.literal("Unfortunately, it takes ").formatted(Formatting.RED))
            .append(Text.literal("time ").formatted(Formatting.BLUE))
            .append(Text.literal("to pull ").formatted(Formatting.RED))
            .append(Text.literal("lava ").formatted(Formatting.GOLD))
            .append(Text.literal("between worlds.").formatted(Formatting.RED)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_RED))
            .append(Text.literal(" to ").formatted(Formatting.RED))
            .append(Text.literal("materialize ").formatted(Formatting.DARK_RED))
            .append(Text.literal("or ").formatted(Formatting.RED))
            .append(Text.literal("dismiss ").formatted(Formatting.DARK_RED))
            .append(Text.literal("lava ").formatted(Formatting.GOLD))
            .append(Text.literal("from the world.").formatted(Formatting.RED)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.DARK_RED))
            .append(Text.literal(" to switch between ").formatted(Formatting.RED))
            .append(Text.literal("placing ").formatted(Formatting.DARK_RED))
            .append(Text.literal("and ").formatted(Formatting.RED))
            .append(Text.literal("removing ").formatted(Formatting.DARK_RED))
            .append(Text.literal("lava").formatted(Formatting.GOLD))
            .append(Text.literal(".").formatted(Formatting.RED)));
      
      if(itemStack != null && getMaxCharges(itemStack) > 1){
         int charges = getIntProperty(itemStack, USES_TAG);
         lore.add(Text.literal(""));
         lore.add(Text.literal("")
               .append(Text.literal("Charges ").formatted(Formatting.GOLD))
               .append(Text.literal("- ").formatted(Formatting.DARK_RED))
               .append(Text.literal(""+charges).formatted(Formatting.RED)));
      }
      
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 30 second recharge time
      int cdLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.ERUPTION.id));
      return 30 - 8*cdLvl;
   }
   
   public int getMaxCharges(ItemStack item){
      int[] chargeCount = new int[]{1, 3, 5, 10, 25};
      return chargeCount[Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.VOLCANIC_CHAMBER.id))];
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.BUCKET,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.NETHERITE_SCRAP,1);
      ArcanaIngredient c = new ArcanaIngredient(Items.BLAZE_POWDER,32);
      ArcanaIngredient g = new ArcanaIngredient(Items.BLAZE_ROD,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.MAGMA_BLOCK,24);
      ArcanaIngredient m = new ArcanaIngredient(Items.MAGMA_CREAM,48);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Magmatic Eversource\n\nRarity: Exotic\n\nMy inventory issue expands to lava as well as water.\n\nUnfortunately, there isn't lava in the air I can pull from and condense like I can with water.")));
      list.add(List.of(Text.literal(" Magmatic Eversource\n\nA different solution is in order: The Nether.\n\nA limitless realm of molten rock that I can pull from through a microscopic portal.\n\nThe only downside is that it takes time to siphon the lava through the portal.")));
      list.add(List.of(Text.literal(" Magmatic Eversource\n\nThe Magmatic Eversource functions almost exactly like the Aquatic Eversource, however after creating a lava source it requires time to recharge.\n\nAugmentation should help mitigate this drawback.")));
      return list;
   }
   
   public class MagmaticEversourceItem extends ArcanaPolymerItem {
      public MagmaticEversourceItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!ArcanaItemUtils.isArcane(itemStack)) return ArcanaRegistry.getModelData(TXT_PLACE).value();
         int mode = getIntProperty(itemStack,MODE_TAG);
         boolean onCD = getIntProperty(itemStack,USES_TAG) <= 0;
         if(onCD){
            return mode == 1 ? ArcanaRegistry.getModelData(TXT_REMOVE_COOLDOWN).value() : ArcanaRegistry.getModelData(TXT_PLACE_COOLDOWN).value();
         }else{
            return mode == 1 ? ArcanaRegistry.getModelData(TXT_REMOVE).value() : ArcanaRegistry.getModelData(TXT_PLACE).value();
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld)) return;
         if(world.getServer().getTicks() % 20 == 0){
            int charges = getIntProperty(stack,USES_TAG);
            int maxCharges = getMaxCharges(stack);
            if(charges < maxCharges){
               addEnergy(stack, 1); // Recharge
               if(getEnergy(stack) >= getMaxEnergy(stack)){
                  setEnergy(stack,0);
                  putProperty(stack,USES_TAG,charges+1);
                  buildItemLore(stack, entity.getServer());
               }
            }
         }
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return TypedActionResult.pass(stack);
         int mode = getIntProperty(stack,MODE_TAG);
         int charges = getIntProperty(stack,USES_TAG);
         
         if(playerEntity.isSneaking()){
            int newMode = (mode+1) % 2;
            putProperty(stack,MODE_TAG,newMode);
            if(newMode == 1){
               player.sendMessage(Text.literal("The Eversource Evaporates").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BUCKET_EMPTY_LAVA,1.0f,1.0f);
            }else{
               player.sendMessage(Text.literal("The Eversource Condenses").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUCKET_FILL_LAVA,1.0f,1.0f);
            }
            return TypedActionResult.success(stack);
         }
         
         if(mode != 1 && charges <= 0){
            player.sendMessage(Text.literal("The Eversource is Recharging").formatted(Formatting.RED,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
            return TypedActionResult.pass(stack);
         }
         
         Fluid fluid = mode == 1 ? Fluids.EMPTY : Fluids.LAVA;
         
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
                     PLAYER_DATA.get(player).addXP(5); // Add xp
                  }
                  return TypedActionResult.success(stack, world.isClient());
               }
               return TypedActionResult.fail(stack);
            }
            BlockState blockState = world.getBlockState(blockPos);
            BlockPos blockPos3 = blockState.getBlock() instanceof FluidFillable ? blockPos : blockPos2;
            if (placeFluid(fluid,playerEntity, world, blockPos3, blockHitResult)) {
               PLAYER_DATA.get(player).addXP(25); // Add xp
               ArcanaAchievements.progress(player,ArcanaAchievements.HELLGATE.id,1);
               putProperty(stack,USES_TAG,charges-1);
               buildItemLore(stack, playerEntity.getServer());
               playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
               return TypedActionResult.success(stack);
            }
            return TypedActionResult.fail(stack);
         }
         return TypedActionResult.pass(stack);
      }
      
      
      public boolean placeFluid(Fluid fluid, @Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {
         boolean bl2;
         if (!(fluid instanceof FlowableFluid flowableFluid)) {
            return false;
         }
         BlockState blockState = world.getBlockState(pos);
         Block block = blockState.getBlock();
         boolean bl = blockState.canBucketPlace(fluid);
         bl2 = blockState.isAir() || bl || block instanceof FluidFillable && ((FluidFillable) block).canFillWithFluid(player, world, pos, blockState, fluid);
         if (!bl2) {
            return hitResult != null && placeFluid(fluid,player, world, hitResult.getBlockPos().offset(hitResult.getSide()), null);
         }
         if (block instanceof FluidFillable fluidFillable) {
            if (fluid == Fluids.LAVA) {
               fluidFillable.tryFillWithFluid(world, pos, blockState, flowableFluid.getStill(false));
               world.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
               return true;
            }
         }
         if (!world.isClient && bl && !blockState.isLiquid()) {
            world.breakBlock(pos, true);
         }
         if (world.setBlockState(pos, fluid.getDefaultState().getBlockState(), Block.NOTIFY_ALL_AND_REDRAW) || blockState.getFluidState().isStill()) {
            world.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
            return true;
         }
         return false;
      }
   }
}

