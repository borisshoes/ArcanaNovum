package net.borisshoes.arcananovum.blocks.altars;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.altars.TransmutationAltarGui;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.transmutation.TransmutationRecipe;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static net.borisshoes.arcananovum.blocks.altars.TransmutationAltar.TransmutationAltarBlock.HORIZONTAL_FACING;

public class TransmutationAltarBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   
   private TreeMap<ArcanaAugment, Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private ArcanaSkin skin;
   private String customName;
   private int cooldown;
   private boolean active;
   private final Multiblock multiblock;
   
   public TransmutationAltarBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.TRANSMUTATION_ALTAR_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.TRANSMUTATION_ALTAR).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment, Integer> augments, String crafterId, String uuid, int origin, ArcanaSkin skin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.skin = skin;
      this.customName = customName == null ? "" : customName;
      resetCooldown();
   }
   
   public void openGui(ServerPlayer player){
      if(active){
         player.sendSystemMessage(Component.literal("You cannot access an active Altar").withStyle(ChatFormatting.RED));
         return;
      }
      TransmutationAltarGui gui = new TransmutationAltarGui(MenuType.HOPPER, player, this);
      gui.buildMenuGui();
      gui.open();
   }
   
   public boolean startTransmute(@Nullable ServerPlayer player){
      if(this.getCooldown() > 0 || this.checkTransmute() == null || !(this.getLevel() instanceof ServerLevel serverWorld))
         return false;
      this.resetCooldown();
      boolean hastyBargain = ArcanaAugments.getAugmentFromMap(this.getAugments(), ArcanaAugments.HASTY_BARGAIN) > 0;
      double speedMod = hastyBargain ? 2 : 1;
      int castTime = (int) (500.0 / speedMod);
      this.setActive(true);
      
      if(player == null && getCrafterId() != null){
         Player crafter = serverWorld.getPlayerByUUID(AlgoUtils.getUUID(getCrafterId()));
         if(crafter instanceof ServerPlayer){
            player = (ServerPlayer) crafter;
         }
      }
      @Nullable ServerPlayer finalPlayer = player;
      
      ArcanaEffectUtils.transmutationAltarAnim(serverWorld, this.getBlockPos().getCenter(), 0, this.getLevel().getBlockState(this.getBlockPos()).getValue(CelestialAltar.CelestialAltarBlock.HORIZONTAL_FACING), speedMod);
      BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(castTime, () -> this.transmute(finalPlayer, false)));
      return true;
   }
   
   public TransmutationRecipe checkTransmute(){
      HashMap<String, ItemEntity> stacks = this.getTransmutingStacks();
      ItemEntity positiveEntity = stacks.get("positive");
      ItemEntity negativeEntity = stacks.get("negative");
      ItemEntity reagent1Entity = stacks.get("reagent1");
      ItemEntity reagent2Entity = stacks.get("reagent2");
      ItemEntity aequalisEntity = stacks.get("aequalis");
      ItemStack positiveStack = positiveEntity == null ? ItemStack.EMPTY : positiveEntity.getItem();
      ItemStack negativeStack = negativeEntity == null ? ItemStack.EMPTY : negativeEntity.getItem();
      ItemStack reagent1Stack = reagent1Entity == null ? ItemStack.EMPTY : reagent1Entity.getItem();
      ItemStack reagent2Stack = reagent2Entity == null ? ItemStack.EMPTY : reagent2Entity.getItem();
      ItemStack aequalisStack = aequalisEntity == null ? ItemStack.EMPTY : aequalisEntity.getItem();
      
      return RecipeManager.findMatchingRecipe(positiveStack, negativeStack, reagent1Stack, reagent2Stack, aequalisStack, this);
   }
   
   public void transmute(@Nullable ServerPlayer player, boolean recursed){
      HashMap<String, ItemEntity> stacks = this.getTransmutingStacks();
      ItemEntity positiveEntity = stacks.get("positive");
      ItemEntity negativeEntity = stacks.get("negative");
      ItemEntity reagent1Entity = stacks.get("reagent1");
      ItemEntity reagent2Entity = stacks.get("reagent2");
      ItemEntity aequalisEntity = stacks.get("aequalis");
      ItemStack positiveStack = positiveEntity == null ? ItemStack.EMPTY : positiveEntity.getItem();
      ItemStack negativeStack = negativeEntity == null ? ItemStack.EMPTY : negativeEntity.getItem();
      ItemStack reagent1Stack = reagent1Entity == null ? ItemStack.EMPTY : reagent1Entity.getItem();
      ItemStack reagent2Stack = reagent2Entity == null ? ItemStack.EMPTY : reagent2Entity.getItem();
      ItemStack aequalisStack = aequalisEntity == null ? ItemStack.EMPTY : aequalisEntity.getItem();
      
      TransmutationRecipe recipe = RecipeManager.findMatchingRecipe(positiveStack, negativeStack, reagent1Stack, reagent2Stack, aequalisStack, this);
      this.setActive(false);
      if(recipe != null){
         List<Tuple<ItemStack, String>> outputs = recipe.doTransmutation(positiveEntity, negativeEntity, reagent1Entity, reagent2Entity, aequalisEntity, this, player);
         
         int transmuteCount = 0;
         for(Tuple<ItemStack, String> outputPair : outputs){
            ItemStack output = outputPair.getA();
            Vec3 outputPos = this.getOutputPos(outputPair.getB());
            transmuteCount += output.getCount();
            if(output.is(ArcanaRegistry.DIVINE_CATALYST.getItem()) && player != null){
               ArcanaNovum.data(player).addCraftedSilent(output);
               ArcanaAchievements.grant(player, ArcanaAchievements.DIVINE_TRANSMUTATION);
            }
            if(output.is(ArcanaRegistry.AEQUALIS_SCIENTIA.getItem()) && player != null){
               ArcanaNovum.data(player).addCraftedSilent(output);
               ArcanaAchievements.grant(player, ArcanaAchievements.PRICE_OF_KNOWLEDGE);
            }
            
            this.getLevel().addFreshEntity(new ItemEntity(this.getLevel(), outputPos.x, outputPos.y + 0.25, outputPos.z, output, 0, 0, 0));
         }
         if(transmuteCount > 0 && player != null){
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_TRANSMUTATION_ALTAR_TRANSMUTE_PER_ITEM) * transmuteCount + ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_TRANSMUTATION_ALTAR_TRANSMUTE));
            ArcanaAchievements.progress(player, ArcanaAchievements.STATE_ALCHEMIST, transmuteCount);
         }
         
         boolean canRecurse = ArcanaAugments.getAugmentFromMap(this.getAugments(), ArcanaAugments.TRADE_AGREEMENT) > 0;
         if(canRecurse && checkTransmute() == recipe && this.getLevel() instanceof ServerLevel serverWorld){
            this.resetCooldown();
            boolean hastyBargain = ArcanaAugments.getAugmentFromMap(this.getAugments(), ArcanaAugments.HASTY_BARGAIN) > 0;
            double speedMod = hastyBargain ? 2 : 1;
            int castTime = (int) (500.0 / speedMod);
            this.setActive(true);
            ArcanaEffectUtils.transmutationAltarAnim(serverWorld, this.getBlockPos().getCenter(), 0, this.getLevel().getBlockState(this.getBlockPos()).getValue(HORIZONTAL_FACING), speedMod);
            BorisLib.addTickTimerCallback(serverWorld, new GenericTimer(castTime, () -> transmute(player, true)));
         }
         
         SoundUtils.playSound(this.getLevel(), this.getBlockPos(), SoundEvents.ALLAY_AMBIENT_WITH_ITEM, SoundSource.BLOCKS, 1, 0.8f);
         level.gameEvent(GameEvent.BLOCK_ACTIVATE, worldPosition, GameEvent.Context.of(getBlockState()));
      }else{
         if(!recursed) this.refundCooldown();
         SoundUtils.playSound(this.getLevel(), this.getBlockPos(), SoundEvents.ALLAY_HURT, SoundSource.BLOCKS, 1, 0.7f);
         level.gameEvent(GameEvent.BLOCK_DEACTIVATE, worldPosition, GameEvent.Context.of(getBlockState()));
      }
   }
   
   public HashMap<String, ItemEntity> getTransmutingStacks(){
      HashMap<String, ItemEntity> stacks = new HashMap<>();
      if(this.level == null || this.worldPosition == null) return stacks;
      Direction direction = level.getBlockState(worldPosition).getValue(HORIZONTAL_FACING);
      Vec3 centerPos = getBlockPos().getCenter();
      Vec3 aequalisPos = centerPos.add(new Vec3(0, 0.6, 0).yRot((float) -(direction.get2DDataValue() * (Math.PI / 2.0f))));
      Vec3 negativePos = centerPos.add(new Vec3(3, 0.6, 0).yRot((float) -(direction.get2DDataValue() * (Math.PI / 2.0f))));
      Vec3 positivePos = centerPos.add(new Vec3(-3, 0.6, 0).yRot((float) -(direction.get2DDataValue() * (Math.PI / 2.0f))));
      Vec3 reagent1Pos = centerPos.add(new Vec3(0, 0.6, -3).yRot((float) -(direction.get2DDataValue() * (Math.PI / 2.0f))));
      Vec3 reagent2Pos = centerPos.add(new Vec3(0, 0.6, 3).yRot((float) -(direction.get2DDataValue() * (Math.PI / 2.0f))));
      
      ItemEntity aequalisEntity = MinecraftUtils.getLargestItemEntity(this.level.getEntities(EntityType.ITEM, new AABB(BlockPos.containing(aequalisPos)), e -> true));
      ItemEntity positiveEntity = MinecraftUtils.getLargestItemEntity(this.level.getEntities(EntityType.ITEM, new AABB(BlockPos.containing(positivePos)), e -> true));
      ItemEntity negativeEntity = MinecraftUtils.getLargestItemEntity(this.level.getEntities(EntityType.ITEM, new AABB(BlockPos.containing(negativePos)), e -> true));
      ItemEntity reagent1Entity = MinecraftUtils.getLargestItemEntity(this.level.getEntities(EntityType.ITEM, new AABB(BlockPos.containing(reagent1Pos)), e -> true));
      ItemEntity reagent2Entity = MinecraftUtils.getLargestItemEntity(this.level.getEntities(EntityType.ITEM, new AABB(BlockPos.containing(reagent2Pos)), e -> true));
      
      stacks.put("aequalis", aequalisEntity);
      stacks.put("positive", positiveEntity);
      stacks.put("negative", negativeEntity);
      stacks.put("reagent1", reagent1Entity);
      stacks.put("reagent2", reagent2Entity);
      
      return stacks;
   }
   
   public Vec3 getOutputPos(String outputString){
      if(this.level == null || this.worldPosition == null) return null;
      Direction direction = level.getBlockState(worldPosition).getValue(HORIZONTAL_FACING);
      Vec3 centerPos = getBlockPos().getCenter();
      Vec3 aequalisPos = centerPos.add(new Vec3(0, 0.6, 0).yRot((float) -(direction.get2DDataValue() * (Math.PI / 2.0f))));
      Vec3 negativePos = centerPos.add(new Vec3(3, 0.6, 0).yRot((float) -(direction.get2DDataValue() * (Math.PI / 2.0f))));
      Vec3 positivePos = centerPos.add(new Vec3(-3, 0.6, 0).yRot((float) -(direction.get2DDataValue() * (Math.PI / 2.0f))));
      Vec3 reagent1Pos = centerPos.add(new Vec3(0, 0.6, -3).yRot((float) -(direction.get2DDataValue() * (Math.PI / 2.0f))));
      Vec3 reagent2Pos = centerPos.add(new Vec3(0, 0.6, 3).yRot((float) -(direction.get2DDataValue() * (Math.PI / 2.0f))));
      
      if(outputString.equals("positive")){
         return positivePos;
      }else if(outputString.equals("negative")){
         return negativePos;
      }else if(outputString.equals("reagent1")){
         return reagent1Pos;
      }else if(outputString.equals("reagent2")){
         return reagent2Pos;
      }else{
         return aequalisPos;
      }
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof TransmutationAltarBlockEntity altar){
         altar.tick();
      }
   }
   
   @Override
   public boolean isAssembled(){
      return multiblock.matches(getMultiblockCheck());
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition, serverWorld.getBlockState(worldPosition), new BlockPos(((MultiblockCore) ArcanaRegistry.TRANSMUTATION_ALTAR).getCheckOffset()), level.getBlockState(worldPosition).getValue(HORIZONTAL_FACING));
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(isAssembled() && cooldown > 0){
         cooldown--;
         this.setChanged();
      }
      
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.isAssembled()){
         ArcanaNovum.addActiveBlock(new Tuple<>(this, this));
      }
      
      boolean activatable = serverWorld.getBlockState(worldPosition).getOptionalValue(TransmutationAltar.TransmutationAltarBlock.ACTIVATABLE).orElse(false);
      boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
      if(activatable ^ shouldBeActivatable){
         serverWorld.setBlock(worldPosition, serverWorld.getBlockState(worldPosition).setValue(TransmutationAltar.TransmutationAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.UPDATE_CLIENTS);
      }
   }
   
   public boolean isActive(){
      return active;
   }
   
   public void setActive(boolean active){
      this.active = active;
      
      if(this.level instanceof ServerLevel serverWorld){
         boolean activatable = serverWorld.getBlockState(worldPosition).getOptionalValue(TransmutationAltar.TransmutationAltarBlock.ACTIVATABLE).orElse(false);
         boolean shouldBeActivatable = this.cooldown <= 0 && this.isAssembled() && !this.isActive();
         if(activatable ^ shouldBeActivatable){
            serverWorld.setBlock(worldPosition, serverWorld.getBlockState(worldPosition).setValue(TransmutationAltar.TransmutationAltarBlock.ACTIVATABLE, shouldBeActivatable), Block.UPDATE_CLIENTS);
         }
      }
   }
   
   public int getCooldown(){
      return this.cooldown;
   }
   
   public void resetCooldown(){
      int cooldownLevel = ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.HASTY_BARGAIN);
      int baseCooldown = ArcanaNovum.CONFIG.getInt(ArcanaConfig.TRANSMUTATION_ALTAR_COOLDOWN);
      int cooldownReduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.TRANSMUTATION_ALTAR_COOLDOWN_PER_LVL).get(cooldownLevel);
      this.cooldown = Math.max(1, baseCooldown - cooldownReduction);
   }
   
   public void refundCooldown(){
      this.cooldown = 0;
   }
   
   public TreeMap<ArcanaAugment, Integer> getAugments(){
      return augments;
   }
   
   public String getCrafterId(){
      return crafterId;
   }
   
   public String getUuid(){
      return uuid;
   }
   
   public int getOrigin(){
      return origin;
   }
   
   public ArcanaSkin getSkin(){
      return skin;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.TRANSMUTATION_ALTAR;
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.skin = ArcanaSkin.getSkinFromString(view.getStringOr(ArcanaBlockEntity.SKIN_TAG, ""));
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.cooldown = view.getIntOr("cooldown", 0);
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC, this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG, this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG, this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME, this.customName == null ? "" : this.customName);
      view.putString(ArcanaBlockEntity.SKIN_TAG, this.skin == null ? "" : this.skin.getSerializedName());
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG, this.origin);
      view.putInt("cooldown", this.cooldown);
   }
}
