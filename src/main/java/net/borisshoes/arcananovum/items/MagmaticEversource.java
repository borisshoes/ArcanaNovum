package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
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

public class MagmaticEversource extends EnergyItem {
   
   private static final String TXT_PLACE = "item/magmatic_eversource_place";
   private static final String TXT_REMOVE = "item/magmatic_eversource_remove";
   private static final String TXT_PLACE_COOLDOWN = "item/magmatic_eversource_place_cooldown";
   private static final String TXT_REMOVE_COOLDOWN = "item/magmatic_eversource_remove_cooldown";
   
   public MagmaticEversource(){
      id = "magmatic_eversource";
      name = "Magmatic Eversource";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC,ArcaneTome.TomeFilter.ITEMS};
      vanillaItem = Items.LAVA_BUCKET;
      item = new MagmaticEversourceItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT_PLACE));
      models.add(new Pair<>(vanillaItem,TXT_REMOVE));
      models.add(new Pair<>(vanillaItem,TXT_PLACE_COOLDOWN));
      models.add(new Pair<>(vanillaItem,TXT_REMOVE_COOLDOWN));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Magmatic Eversource\",\"italic\":false,\"bold\":true,\"color\":\"gold\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      addMagicNbt(tag);
      tag.getCompound("arcananovum").putInt("mode",0); // 0 place, 1 remove
      tag.getCompound("arcananovum").putInt("charges",1);
      stack.setNbt(tag);
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      int mode = magicTag.getInt("mode");
      int charges = magicTag.getInt("charges");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").putInt("mode",mode);
      newTag.getCompound("arcananovum").putInt("charges",charges);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"Lava \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"is harder to create than \",\"color\":\"red\"},{\"text\":\"water\",\"color\":\"blue\"},{\"text\":\", luckily there's a \",\"color\":\"red\"},{\"text\":\"dimension \",\"color\":\"dark_red\"},{\"text\":\"made of it.\",\"color\":\"red\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Unfortunately, it takes \",\"italic\":false,\"color\":\"red\"},{\"text\":\"time \",\"color\":\"blue\"},{\"text\":\"to pull \"},{\"text\":\"lava \",\"color\":\"gold\"},{\"text\":\"between worlds.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_red\"},{\"text\":\" to \",\"color\":\"red\"},{\"text\":\"materialize \"},{\"text\":\"or \",\"color\":\"red\"},{\"text\":\"dismiss \"},{\"text\":\"lava \",\"color\":\"gold\"},{\"text\":\"from the world.\",\"color\":\"red\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"dark_red\"},{\"text\":\" to switch between \",\"color\":\"red\"},{\"text\":\"placing \"},{\"text\":\"and \",\"color\":\"red\"},{\"text\":\"removing \"},{\"text\":\"lava\",\"color\":\"gold\"},{\"text\":\".\",\"color\":\"red\"}]"));
      if(MagicItemUtils.isMagic(itemStack) && getMaxCharges(itemStack) > 1){
         NbtCompound itemNbt = itemStack.getNbt();
         NbtCompound magicTag = itemNbt.getCompound("arcananovum");
         int charges = magicTag.getInt("charges");
         loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"gold\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Charges \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"- \",\"color\":\"dark_red\"},{\"text\":\""+charges+"\",\"color\":\"red\"},{\"text\":\"\",\"color\":\"dark_red\"}]"));
      }
      return loreList;
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
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.BUCKET,16,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.BLAZE_POWDER,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.NETHERITE_INGOT,2,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.MAGMA_BLOCK,64,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.MAGMA_CREAM,64,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients,new ForgeRequirement().withCore());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("\" Magmatic Eversource\\n\\nRarity: Exotic\\n\\nMy inventory issue expands to lava as well as water.\\n\\nUnfortunately, there isn't lava in the air I can pull from and condense like I can with water.\"");
      list.add("\" Magmatic Eversource\\n\\nA different solution is in order: The Nether.\\n\\nA limitless realm of molten rock that I can pull from through a microscopic portal.\\n\\nThe only downside is that it takes time to siphon the lava through the portal.\"");
      list.add("\" Magmatic Eversource\\n\\nThe Magmatic Eversource functions almost exactly like the Aquatic Eversource, however after creating a lava source it requires time to recharge.\\n\\nAugmentation should help mitigate this drawback.\"");
      return list;
   }
   
   public class MagmaticEversourceItem extends MagicPolymerItem {
      public MagmaticEversourceItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return ArcanaRegistry.MODELS.get(TXT_PLACE).value();
         int mode = itemStack.getNbt().getCompound("arcananovum").getInt("mode");
         boolean onCD = itemStack.getNbt().getCompound("arcananovum").getInt("charges") <= 0;
         if(onCD){
            return mode == 1 ? ArcanaRegistry.MODELS.get(TXT_REMOVE_COOLDOWN).value() : ArcanaRegistry.MODELS.get(TXT_PLACE_COOLDOWN).value();
         }else{
            return mode == 1 ? ArcanaRegistry.MODELS.get(TXT_REMOVE).value() : ArcanaRegistry.MODELS.get(TXT_PLACE).value();
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld)) return;
         if(world.getServer().getTicks() % 20 == 0){
            NbtCompound itemNbt = stack.getNbt();
            NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
            int charges = magicNbt.getInt("charges");
            int maxCharges = getMaxCharges(stack);
            if(charges < maxCharges){
               addEnergy(stack, 1); // Recharge
               if(getEnergy(stack) >= getMaxEnergy(stack)){
                  setEnergy(stack,0);
                  magicNbt.putInt("charges",charges+1);
                  buildItemLore(stack, entity.getServer());
               }
            }
         }
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return TypedActionResult.pass(stack);
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         int charges = magicNbt.getInt("charges");
         
         if(playerEntity.isSneaking()){
            int newMode = (mode+1) % 2;
            magicNbt.putInt("mode",newMode);
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
               magicNbt.putInt("charges",charges-1);
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
