package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.starlightforge.StarlightForgeGui;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.TreeMap;

public class StarlightForgeBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private int seedUses;
   
   public StarlightForgeBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.STARLIGHT_FORGE_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.STARLIGHT_FORGE).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
      this.assembled = false;
   }
   
   public void setSeedUses(int uses){
      this.seedUses = uses;
   }
   
   public int getSeedUses(){
      return this.seedUses;
   }
   
   public void openGui(int screen, ServerPlayer player, String data, ArcaneTomeGui tomeGui){ // 0 - Menu (hopper), 1 - Arcana Crafting (9x5), 2 - Equipment Forging (9x3), 3 - Recipe (9x5), 4 - (Deprecated), 5 - Skilled Selection (9x5)
      StarlightForgeGui gui = null;
      if(screen == 0){
         gui = new StarlightForgeGui(MenuType.HOPPER,player,this,level,screen,tomeGui);
         gui.buildMenuGui();
      }else if(screen == 1){
         gui = new StarlightForgeGui(MenuType.GENERIC_9x5,player,this,level,screen,tomeGui);
         gui.buildCraftingGui(data);
      }else if(screen == 2){
         gui = new StarlightForgeGui(MenuType.GENERIC_9x3,player,this,level,screen,tomeGui);
         gui.buildForgeGui();
      }else if(screen == 3){
         gui = new StarlightForgeGui(MenuType.GENERIC_9x5,player,this,level,screen,tomeGui);
         gui.buildRecipeGui(data);
      }else if(screen == 5){
         gui = new StarlightForgeGui(MenuType.GENERIC_9x5,player,this,level,screen,tomeGui);
         gui.buildSkilledGui(data);
      }
      if(gui != null){
         gui.open();
      }
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
                  ArcanaAchievements.grant(player,ArcanaAchievements.NIDAVELLIR.id);
               }
            }
         }
      }
      
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.assembled){
         ArcanaNovum.addActiveBlock(new Tuple<>(this,this));
      }
   }
   
   public BlockPos getForgeRange(){
      return ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.STELLAR_RANGE.id) >= 1 ? new BlockPos(15, 8, 15) : new BlockPos(8, 5, 8);
   }
   
   public ArcanaBlockEntity getForgeAddition(ServerLevel world, BlockEntityType<? extends BlockEntity> additionBlockEntity){
      BlockPos range = this.getForgeRange();
      for(BlockPos blockPos : BlockPos.betweenClosed(worldPosition.offset(range), worldPosition.subtract(range))){
         BlockEntity be = additionBlockEntity.getBlockEntity(world,blockPos);
         if(be instanceof ArcanaBlockEntity arcanaBlock && arcanaBlock.isAssembled()){
            return arcanaBlock;
         }
      }
      return null;
   }
   
   public ArrayList<Container> getIngredientInventories(){
      ArrayList<Container> invs = new ArrayList<>();
      BlockPos range = this.getForgeRange();
      if(!(level instanceof ServerLevel serverWorld)) return invs;
      for(BlockPos blockPos : BlockPos.betweenClosed(worldPosition.offset(range), worldPosition.subtract(range))){
         BlockEntity be = serverWorld.getBlockEntity(blockPos);
         BlockState state = serverWorld.getBlockState(blockPos);
         if(be instanceof ChestBlockEntity chestBe){
            if(!invs.contains(chestBe)){
               invs.add(chestBe);
            }
         }else if(be instanceof BarrelBlockEntity barrelBe){
            if(!invs.contains(barrelBe)){
               invs.add(barrelBe);
            }
         }else if(be instanceof ShulkerBoxBlockEntity shulkerBox){
            if(!invs.contains(shulkerBox)){
               invs.add(shulkerBox);
            }
         }else if(be instanceof ArcaneSingularityBlockEntity singularity){
            if(!invs.contains(singularity)){
               invs.add(singularity);
            }
         }
      }
      return invs;
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition,serverWorld.getBlockState(worldPosition),new BlockPos(((MultiblockCore) ArcanaRegistry.STARLIGHT_FORGE).getCheckOffset()),null);
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
      if(ArcanaAugments.getAugmentFromMap(getAugments(),ArcanaAugments.MOONLIT_FORGE.id) >= 1){
         long timeOfDay = level.getDayTime();
         int day = (int) (timeOfDay/24000L % Integer.MAX_VALUE);
         int curPhase = day % 8;
         int influence = Math.abs(-curPhase+4);
         
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
      if(ArcanaAugments.getAugmentFromMap(getAugments(),ArcanaAugments.MOONLIT_FORGE.id) >= 1){
         long timeOfDay = level.getDayTime();
         int day = (int) (timeOfDay/24000L % Integer.MAX_VALUE);
         int curPhase = day % 8;
         int influence = Math.abs(-curPhase+4);
         
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
      return WorldOptions.parseSeed(uuid+this.seedUses).orElse(WorldOptions.randomSeed());
   }
   
   public void addSeedUse(){
      this.seedUses++;
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.seedUses = view.getIntOr("seedUses", 0);
      this.augments = new TreeMap<>();
      view.read("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG,this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG,this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME,this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG,this.origin);
      view.putInt("seedUses",this.seedUses);
   }
}
