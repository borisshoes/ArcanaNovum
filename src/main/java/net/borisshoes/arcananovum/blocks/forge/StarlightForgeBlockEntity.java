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
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.starlightforge.StarlightForgeGui;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.GeneratorOptions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class StarlightForgeBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private int seedUses;
   
   public StarlightForgeBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.STARLIGHT_FORGE_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.STARLIGHT_FORGE).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
      this.assembled = false;
   }
   
   public void setSeedUses(int uses){
      this.seedUses = uses;
   }
   
   public int getSeedUses(){
      return this.seedUses;
   }
   
   public void openGui(int screen, ServerPlayerEntity player, String data, @Nullable TomeGui.CompendiumSettings settings){ // 0 - Menu (hopper), 1 - Arcana Crafting (9x5), 2 - Equipment Forging (9x3), 3 - Recipe (9x5), 4 - Compendium (9x6), 5 - Skilled Selection (9x5)
      StarlightForgeGui gui = null;
      if(screen == 0){
         gui = new StarlightForgeGui(ScreenHandlerType.HOPPER,player,this,world,screen,settings);
         gui.buildMenuGui();
      }else if(screen == 1){
         gui = new StarlightForgeGui(ScreenHandlerType.GENERIC_9X5,player,this,world,screen,settings);
         gui.buildCraftingGui(data);
      }else if(screen == 2){
         gui = new StarlightForgeGui(ScreenHandlerType.GENERIC_9X3,player,this,world,screen,settings);
         gui.buildForgeGui();
      }else if(screen == 3){
         gui = new StarlightForgeGui(ScreenHandlerType.GENERIC_9X5,player,this,world,screen,settings);
         gui.buildRecipeGui(data);
      }else if(screen == 4){
         gui = new StarlightForgeGui(ScreenHandlerType.GENERIC_9X6,player,this,world,screen,settings);
         TomeGui.buildCompendiumGui(gui,player,settings);
      }else if(screen == 5){
         gui = new StarlightForgeGui(ScreenHandlerType.GENERIC_9X5,player,this,world,screen,settings);
         gui.buildSkilledGui(data);
      }
      if(gui != null){
         gui.open();
      }
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof StarlightForgeBlockEntity forge){
         forge.tick();
      }
   }
   
   private void tick(){
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return;
      }
      
      if(serverWorld.getServer().getTicks() % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
      }
      
      if(serverWorld.getServer().getTicks() % 100 == 0){
         boolean hasAll = true;
         if(getForgeAddition(serverWorld, ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY) == null) hasAll = false;
         if(getForgeAddition(serverWorld, ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY) == null) hasAll = false;
         if(getForgeAddition(serverWorld, ArcanaRegistry.MIDNIGHT_ENCHANTER_BLOCK_ENTITY) == null) hasAll = false;
         if(getForgeAddition(serverWorld, ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY) == null) hasAll = false;
         if(getForgeAddition(serverWorld, ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY) == null) hasAll = false;
         if(hasAll){
            if(crafterId != null && !crafterId.isEmpty()){
               ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(MiscUtils.getUUID(crafterId));
               if(player != null){
                  ArcanaAchievements.grant(player,ArcanaAchievements.NIDAVELLIR.id);
               }
            }
         }
      }
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.assembled){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
   }
   
   public BlockPos getForgeRange(){
      return ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.STELLAR_RANGE.id) >= 1 ? new BlockPos(15, 8, 15) : new BlockPos(8, 5, 8);
   }
   
   public ArcanaBlockEntity getForgeAddition(ServerWorld world, BlockEntityType<? extends BlockEntity> additionBlockEntity){
      BlockPos range = this.getForgeRange();
      for(BlockPos blockPos : BlockPos.iterate(pos.add(range), pos.subtract(range))){
         BlockEntity be = additionBlockEntity.get(world,blockPos);
         if(be instanceof ArcanaBlockEntity arcanaBlock && arcanaBlock.isAssembled()){
            return arcanaBlock;
         }
      }
      return null;
   }
   
   public ArrayList<Inventory> getIngredientInventories(){
      ArrayList<Inventory> invs = new ArrayList<>();
      BlockPos range = this.getForgeRange();
      if(!(world instanceof ServerWorld serverWorld)) return invs;
      for(BlockPos blockPos : BlockPos.iterate(pos.add(range), pos.subtract(range))){
         BlockEntity be = serverWorld.getBlockEntity(blockPos);
         BlockState state = serverWorld.getBlockState(blockPos);
         if(be instanceof ChestBlockEntity chestBe && state.getBlock() instanceof ChestBlock chestBlock){
            Inventory inv = ChestBlock.getInventory(chestBlock,state,serverWorld,blockPos,true);
            if(inv != null && !invs.contains(inv)){
               invs.add(inv);
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
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(((MultiblockCore) ArcanaRegistry.STARLIGHT_FORGE).getCheckOffset()),null);
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
   
   public boolean isSynthetic(){
      return synthetic;
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
         long timeOfDay = world.getTimeOfDay();
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
         long timeOfDay = world.getTimeOfDay();
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
      return GeneratorOptions.parseSeed(uuid+this.seedUses).orElse(GeneratorOptions.getRandomSeed());
   }
   
   public void addSeedUse(){
      this.seedUses++;
   }
   
   @Override
   public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
      super.readNbt(nbt, registryLookup);
      if (nbt.contains("arcanaUuid")) {
         this.uuid = nbt.getString("arcanaUuid");
      }
      if (nbt.contains("crafterId")) {
         this.crafterId = nbt.getString("crafterId");
      }
      if (nbt.contains("customName")) {
         this.customName = nbt.getString("customName");
      }
      if (nbt.contains("synthetic")) {
         this.synthetic = nbt.getBoolean("synthetic");
      }
      if (nbt.contains("seedUses")) {
         this.seedUses = nbt.getInt("seedUses");
      }
      augments = new TreeMap<>();
      if(nbt.contains("arcanaAugments")){
         NbtCompound augCompound = nbt.getCompound("arcanaAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug,augCompound.getInt(key));
         }
      }
   }
   
   @Override
   protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
      super.writeNbt(nbt, registryLookup);
      if(augments != null){
         NbtCompound augsCompound = new NbtCompound();
         for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
            augsCompound.putInt(entry.getKey().id,entry.getValue());
         }
         nbt.put("arcanaAugments",augsCompound);
      }
      if(this.uuid != null){
         nbt.putString("arcanaUuid",this.uuid);
      }
      if(this.crafterId != null){
         nbt.putString("crafterId",this.crafterId);
      }
      if(this.customName != null){
         nbt.putString("customName",this.customName);
      }
      nbt.putBoolean("synthetic",this.synthetic);
      nbt.putInt("seedUses",this.seedUses);
   }
}
