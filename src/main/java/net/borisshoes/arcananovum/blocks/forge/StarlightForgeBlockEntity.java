package net.borisshoes.arcananovum.blocks.forge;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.EnderCrateBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.starlightforge.StarlightForgeGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

public class StarlightForgeBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment, Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String skin;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private int seedUses;
   private final ArrayList<PendingOutput> pendingOutputs = new ArrayList<>(); // Forged items waiting for their animation to finish
   
   public StarlightForgeBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.STARLIGHT_FORGE_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.STARLIGHT_FORGE).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment, Integer> augments, String crafterId, String uuid, int origin, String skin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.skin = skin;
      this.customName = customName == null ? "" : customName;
      this.assembled = false;
   }
   
   public void setSeedUses(int uses){
      this.seedUses = uses;
   }
   
   public int getSeedUses(){
      return this.seedUses;
   }
   
   public void openMainGui(ServerPlayer player, ArcaneTomeGui tomeGui){
      StarlightForgeGui gui = new StarlightForgeGui(MenuType.HOPPER, player, this, level, 0, tomeGui);
      gui.buildMenuGui();
      gui.open();
   }
   
   public void openCraftingGui(ServerPlayer player, ArcanaRecipe recipe, ArcaneTomeGui tomeGui){
      StarlightForgeGui gui = new StarlightForgeGui(MenuType.GENERIC_9x5, player, this, level, 1, tomeGui);
      gui.buildCraftingGui(recipe);
      gui.open();
   }
   
   public void openForgeGui(ServerPlayer player, ArcaneTomeGui tomeGui){
      StarlightForgeGui gui = new StarlightForgeGui(MenuType.GENERIC_9x3, player, this, level, 2, tomeGui);
      gui.buildForgeGui();
      gui.open();
   }
   
   public void openRecipeGui(ServerPlayer player, ArcanaRecipe recipe, ArcaneTomeGui tomeGui){
      StarlightForgeGui gui = new StarlightForgeGui(MenuType.GENERIC_9x5, player, this, level, 3, tomeGui);
      gui.buildRecipeGui(recipe);
      gui.open();
   }
   
   public void openSkilledGui(ServerPlayer player, ArcanaItem item, ArcanaRecipe recipe, ArcaneTomeGui tomeGui){
      StarlightForgeGui gui = new StarlightForgeGui(MenuType.GENERIC_9x5, player, this, level, 5, tomeGui);
      gui.buildSkilledGui(item, recipe);
      gui.open();
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof StarlightForgeBlockEntity forge){
         forge.tick();
      }
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(!pendingOutputs.isEmpty()){
         if(pendingOutputs.removeIf(output -> {
            if(--output.ticks > 0) return false;
            finishOutput(serverWorld, output);
            return true;
         })) setChanged();
      }
      
      if(serverWorld.getServer().getTickCount() % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
      }
      
      if(serverWorld.getServer().getTickCount() % 100 == 0){
         boolean hasAll = true;
         if(getForgeAddition(serverWorld, ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY) == null) hasAll = false;
         if(getForgeAddition(serverWorld, ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY) == null) hasAll = false;
         if(getForgeAddition(serverWorld, ArcanaRegistry.MIDNIGHT_ENCHANTER_BLOCK_ENTITY) == null) hasAll = false;
         if(getForgeAddition(serverWorld, ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY) == null) hasAll = false;
         if(getForgeAddition(serverWorld, ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY) == null) hasAll = false;
         if(hasAll){
            if(crafterId != null && !crafterId.isEmpty()){
               ServerPlayer player = serverWorld.getServer().getPlayerList().getPlayer(AlgoUtils.getUUID(crafterId));
               if(player != null){
                  ArcanaAchievements.grant(player, ArcanaAchievements.NIDAVELLIR);
               }
            }
         }
      }
      
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.assembled){
         ArcanaNovum.addActiveBlock(Pair.of(this, this));
      }
   }
   
   // Saved with the forge, so the item still drops if the server stops before the delay is over
   public void addPendingOutput(ItemStack stack, int delay){
      addPendingOutput(new PendingOutput(stack, delay, Optional.empty(), false, false, 0, false));
   }
   
   // An Arcana Item craft, whose crafting credit, xp and achievements go to the crafter when the item drops
   public void addPendingCraft(ItemStack stack, int delay, UUID crafter, boolean usedFletchery){
      addPendingOutput(new PendingOutput(stack, delay, Optional.of(crafter), true, usedFletchery, 0, false));
   }
   
   // A stardust infusion, whose xp and achievement go to the crafter when the item drops
   public void addPendingInfusion(ItemStack stack, int delay, UUID crafter, int xp, boolean masterCraftsman){
      addPendingOutput(new PendingOutput(stack, delay, Optional.of(crafter), false, false, xp, masterCraftsman));
   }
   
   private void addPendingOutput(PendingOutput output){
      if(output.stack.isEmpty()) return;
      pendingOutputs.add(output);
      setChanged();
   }
   
   // The crafter is looked up by UUID, so they are rewarded even if they logged off during the animation
   private void finishOutput(Level world, PendingOutput output){
      UUID crafter = output.crafter.orElse(null);
      ArcanaPlayerData data = ArcanaNovum.data(crafter);
      if(data != null){
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(output.stack);
         if(output.arcanaCraft && arcanaItem != null){
            if(!data.addCrafted(output.stack) && !(arcanaItem instanceof ArcaneTome)){
               data.addXP(ArcanaRarity.getCraftXp(arcanaItem.getRarity()));
            }
            
            if(arcanaItem.getRarity() != ArcanaRarity.MUNDANE){
               ArcanaAchievements.grant(crafter, ArcanaAchievements.INTRO_ARCANA);
               ArcanaAchievements.progress(crafter, ArcanaAchievements.INTERMEDIATE_ARTIFICE, 1);
            }
            if(arcanaItem.getRarity() == ArcanaRarity.SOVEREIGN)
               ArcanaAchievements.grant(crafter, ArcanaAchievements.ARTIFICIAL_DIVINITY);
            if(output.usedFletchery){
               ArcanaAchievements.setCondition(crafter, ArcanaAchievements.OVERLY_EQUIPPED_ARCHER, arcanaItem.getNameString(), true);
            }
         }
         if(output.masterCraftsman) ArcanaAchievements.grant(crafter, ArcanaAchievements.MASTER_CRAFTSMAN);
         if(output.xp > 0) data.addXP(output.xp);
      }
      
      Vec3 pos = Vec3.atCenterOf(worldPosition).add(0, 2, 0);
      Containers.dropItemStack(world, pos.x, pos.y, pos.z, output.stack);
   }
   
   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState oldState){
      if(level != null) pendingOutputs.forEach(output -> finishOutput(level, output));
      pendingOutputs.clear();
   }
   
   public BlockPos getForgeRange(){
      int vertBonus = ArcanaNovum.CONFIG.getInt(ArcanaConfig.STARLIGHT_FORGE_STELLAR_RANGE_VERTICAL);
      int horizBonus = ArcanaNovum.CONFIG.getInt(ArcanaConfig.STARLIGHT_FORGE_STELLAR_RANGE_HORIZONTAL);
      return ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.STELLAR_RANGE) >= 1 ? new BlockPos(8 + horizBonus, 5 + vertBonus, 8 + horizBonus) : new BlockPos(8, 5, 8);
   }
   
   public ArcanaBlockEntity getForgeAddition(ServerLevel world, BlockEntityType<? extends BlockEntity> additionBlockEntity){
      BlockPos range = this.getForgeRange();
      for(BlockPos blockPos : BlockPos.betweenClosed(worldPosition.offset(range), worldPosition.subtract(range))){
         BlockEntity be = additionBlockEntity.getBlockEntity(world, blockPos);
         if(be instanceof ArcanaBlockEntity arcanaBlock && arcanaBlock.isAssembled()){
            return arcanaBlock;
         }
      }
      return null;
   }
   
   public HashSet<Container> getIngredientInventories(){
      HashSet<Container> invs = new HashSet<>();
      BlockPos range = this.getForgeRange();
      if(!(level instanceof ServerLevel serverWorld)) return invs;
      HashMap<EnderCrateChannel, EnderCrateBlockEntity> crates = new HashMap<>();
      
      for(BlockPos blockPos : BlockPos.betweenClosed(worldPosition.offset(range), worldPosition.subtract(range))){
         BlockEntity be = serverWorld.getBlockEntity(blockPos);
         BlockState state = serverWorld.getBlockState(blockPos);
         if(be instanceof ChestBlockEntity chestBe){
            invs.add(chestBe);
         }else if(be instanceof BarrelBlockEntity barrelBe){
            invs.add(barrelBe);
         }else if(be instanceof ShulkerBoxBlockEntity shulkerBox){
            invs.add(shulkerBox);
         }else if(be instanceof ArcaneSingularityBlockEntity singularity){
            invs.add(singularity);
         }else if (be instanceof EnderCrateBlockEntity crate){
            if(!crates.containsKey(crate.getChannel()) || crate.getBandwidth() > crates.get(crate.getChannel()).getBandwidth()){
               crates.put(crate.getChannel(),crate);
            }
         }
      }
      invs.addAll(crates.values());
      return invs;
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      Vec3i vec = ((MultiblockCore) ArcanaRegistry.STARLIGHT_FORGE).getCheckOffset();
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition, serverWorld.getBlockState(worldPosition), new BlockPos(vec.getX(), vec.getY(), vec.getZ()), null);
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
   
   public String getSkin(){
      return skin;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.STARLIGHT_FORGE;
   }
   
   public int getStartingValue(){
      return 40;
   }
   
   public int getPlanetCount(){
      int planetCount = 2;
      if(ArcanaAugments.getAugmentFromMap(getAugments(), ArcanaAugments.MOONLIT_FORGE) >= 1){
         long timeOfDay = level.getOverworldClockTime();
         int day = (int) (timeOfDay / 24000L % Integer.MAX_VALUE);
         int curPhase = day % 8;
         int influence = Math.abs(-curPhase + 4);
         
         if(influence == 0){
            planetCount = 3;
         }else if(influence == 1){
            planetCount = 3;
         }else if(influence == 2){
            planetCount = 2;
         }else if(influence == 3){
            planetCount = 1;
         }else if(influence == 4){
            planetCount = 0;
         }
      }
      return planetCount;
   }
   
   public int getStarCount(){
      int starCount = 2;
      if(ArcanaAugments.getAugmentFromMap(getAugments(), ArcanaAugments.MOONLIT_FORGE) >= 1){
         long timeOfDay = level.getOverworldClockTime();
         int day = (int) (timeOfDay / 24000L % Integer.MAX_VALUE);
         int curPhase = day % 8;
         int influence = Math.abs(-curPhase + 4);
         
         if(influence == 0){
            starCount = 0;
         }else if(influence == 1){
            starCount = 1;
         }else if(influence == 2){
            starCount = 3;
         }else if(influence == 3){
            starCount = 4;
         }else if(influence == 4){
            starCount = 5;
         }
      }
      return starCount;
   }
   
   public long getSeed(){
      return WorldOptions.parseSeed(uuid + this.seedUses).orElse(WorldOptions.randomSeed());
   }
   
   public void addSeedUse(){
      this.seedUses++;
      setChanged();
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.skin = view.getStringOr(ArcanaBlockEntity.SKIN_TAG, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.seedUses = view.getIntOr("seedUses", 0);
      this.pendingOutputs.clear();
      view.read("pendingOutputs", PendingOutput.CODEC.listOf()).ifPresent(this.pendingOutputs::addAll);
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
      view.putString(ArcanaBlockEntity.SKIN_TAG, this.skin == null ? "" : this.skin);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG, this.origin);
      view.putInt("seedUses", this.seedUses);
      if(!this.pendingOutputs.isEmpty()) view.store("pendingOutputs", PendingOutput.CODEC.listOf(), this.pendingOutputs);
   }
   
   private static final class PendingOutput {
      private static final Codec<PendingOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(output -> output.stack),
            Codec.INT.fieldOf("ticks").forGetter(output -> output.ticks),
            CodecUtils.UUID_CODEC.optionalFieldOf("crafter").forGetter(output -> output.crafter),
            Codec.BOOL.optionalFieldOf("arcanaCraft", false).forGetter(output -> output.arcanaCraft),
            Codec.BOOL.optionalFieldOf("usedFletchery", false).forGetter(output -> output.usedFletchery),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(output -> output.xp),
            Codec.BOOL.optionalFieldOf("masterCraftsman", false).forGetter(output -> output.masterCraftsman)
      ).apply(instance, PendingOutput::new));
      
      private final ItemStack stack;
      private int ticks;
      private final Optional<UUID> crafter;
      private final boolean arcanaCraft;
      private final boolean usedFletchery;
      private final int xp;
      private final boolean masterCraftsman;
      
      private PendingOutput(ItemStack stack, int ticks, Optional<UUID> crafter, boolean arcanaCraft, boolean usedFletchery, int xp, boolean masterCraftsman){
         this.stack = stack;
         this.ticks = ticks;
         this.crafter = crafter;
         this.arcanaCraft = arcanaCraft;
         this.usedFletchery = usedFletchery;
         this.xp = xp;
         this.masterCraftsman = masterCraftsman;
      }
   }
}
