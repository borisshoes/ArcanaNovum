package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicBlockEntity;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.starlightforge.StarlightForgeGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class StarlightForgeBlockEntity extends BlockEntity implements PolymerObject, MagicBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   
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
   
   public void openGui(int screen, ServerPlayerEntity player, String data, @Nullable TomeGui.CompendiumSettings settings){ // 0 - Menu (hopper), 1 - Magic Crafting (9x5), 2 - Equipment Forging (9x3), 3 - Recipe (9x5), 4 - Compendium (9x6)
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
         ArcaneTome.buildCompendiumGui(gui,player,settings);
      }
      if(gui != null){
         if(!gui.tryOpen(player)){
            player.sendMessage(Text.literal("Someone else is using the Forge").formatted(Formatting.RED),true);
         }
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
            if(!crafterId.isEmpty()){
               ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(UUID.fromString(crafterId));
               if(player != null){
                  ArcanaAchievements.grant(player,ArcanaAchievements.NIDAVELLIR.id);
               }
            }
         }
      }
   }
   
   public MagicBlockEntity getForgeAddition(ServerWorld world, BlockEntityType<? extends BlockEntity> additionBlockEntity){
      for(BlockPos blockPos : BlockPos.iterate(pos.add(-8, -5, -8), pos.add(8, 5, 8))){
         BlockEntity be = additionBlockEntity.get(world,blockPos);
         if(be instanceof MagicBlockEntity magicBlock && magicBlock.isAssembled()){
            return magicBlock;
         }
      }
      return null;
   }
   
   public ArrayList<Inventory> getIngredientInventories(){
      ArrayList<Inventory> invs = new ArrayList<>();
      if(!(world instanceof ServerWorld serverWorld)) return invs;
      for(BlockPos blockPos : BlockPos.iterate(pos.add(-8, -5, -8), pos.add(8, 5, 8))){
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
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(-1,-1,-1),null);
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
   
   public MagicItem getMagicItem(){
      return ArcanaRegistry.STARLIGHT_FORGE;
   }
   
   @Override
   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
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
   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
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
   }
}
