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
import net.borisshoes.arcananovum.gui.stellarcore.StellarCoreGui;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.ACTIVE_ARCANA_BLOCKS;
import static net.borisshoes.arcananovum.blocks.forge.StellarCore.MOLTEN_CORE_ITEMS;
import static net.borisshoes.arcananovum.blocks.forge.StellarCore.StellarCoreBlock.HORIZONTAL_FACING;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class StellarCoreBlockEntity extends LootableContainerBlockEntity implements PolymerObject, SidedInventory, InventoryChangedListener, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private boolean seenForge;
   private boolean updating;
   private SimpleInventory inventory = new SimpleInventory(size());
   private final Set<ServerPlayerEntity> watchingPlayers = new HashSet<>();
   
   public StellarCoreBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.STELLAR_CORE).getMultiblock();
      this.inventory.addListener(this);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof StellarCoreBlockEntity core){
         core.tick();
      }
   }
   
   private void tick(){
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return;
      }
      int ticks = serverWorld.getServer().getTicks();
      BlockState blockState = serverWorld.getBlockState(pos);
      
      if(ticks % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
         this.seenForge = StarlightForge.findActiveForge(serverWorld,pos) != null;
      }
      
      if(assembled && seenForge){
         Direction dir = blockState.get(StellarCore.StellarCoreBlock.HORIZONTAL_FACING);
         ParticleEffectUtils.stellarCoreAnim(serverWorld,pos.add(dir.getVector().multiply(-2)).toCenterPos().add(0,1,0),ticks % 300, dir);
      }
      
      
      boolean lit = blockState.get(StellarCore.StellarCoreBlock.LIT);
      if(lit ^ assembled){
         blockState = blockState.with(StellarCore.StellarCoreBlock.LIT,assembled);
         world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
      }
      
      watchingPlayers.removeIf(player -> player.currentScreenHandler == player.playerScreenHandler);
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.assembled && this.seenForge){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
   }
   
   public List<ItemStack> salvageItem(ItemStack stack){
      List<ItemStack> salvage = new ArrayList<>();
      double salvageLvl = .25*(1+ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.DYSON_SPHERE.id));
      Item item = stack.getItem();
      if(ArcanaItemUtils.isArcane(stack)) return salvage;
      
      if(item instanceof ArmorItem armor){
         EquipmentSlot slot = armor.getSlotType();
         int baseAmt = switch(slot){
            case MAINHAND, OFFHAND -> 1;
            case FEET -> 4;
            case LEGS -> 7;
            case CHEST -> 8;
            case HEAD -> 5;
            case BODY -> 6;
         };
         ItemStack[] repairItems = armor.getMaterial().value().repairIngredient().get().getMatchingStacks();
         if(repairItems[0].isOf(Items.NETHERITE_INGOT)){
            salvage.add(new ItemStack(Items.NETHERITE_SCRAP,(int) Math.round(4*salvageLvl)));
         }else{
            salvage.add(repairItems[0].copyWithCount((int) Math.round(baseAmt*salvageLvl)));
         }
      }else if(item instanceof ToolItem tool){
         int baseAmt;
         if(item instanceof PickaxeItem){
            baseAmt = 3;
         }else if(item instanceof HoeItem){
            baseAmt = 2;
         }else if(item instanceof SwordItem){
            baseAmt = 2;
         }else if(item instanceof AxeItem){
            baseAmt = 3;
         }else if(item instanceof ShovelItem){
            baseAmt = 1;
         }else{
            baseAmt = 1;
         }
         
         ItemStack[] repairItems = tool.getMaterial().getRepairIngredient().getMatchingStacks();
         if(repairItems.length > 0){
            if(repairItems[0].isOf(Items.NETHERITE_INGOT)){
               salvage.add(new ItemStack(Items.NETHERITE_SCRAP,(int) Math.max(1,Math.round(4*salvageLvl))));
            }else{
               salvage.add(repairItems[0].copyWithCount((int) Math.max(1,Math.round(baseAmt*salvageLvl))));
            }
         }
      }
      
      int stardust = (int) (MiscUtils.calcEssenceFromEnchants(stack) * (1 + .15*ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.FUSION_INJECTORS.id)));
      if(stardust > 0){
         while(stardust > 64){
            salvage.add(ArcanaRegistry.STARDUST.getDefaultStack().copyWithCount(64));
            stardust -= 64;
         }
         if(stardust > 0){
            salvage.add(ArcanaRegistry.STARDUST.getDefaultStack().copyWithCount(stardust));
         }
      }
      
      List<ItemStack> salvageReturn = new ArrayList<>();
      for(int i=0; i<stack.getCount(); i++){
         salvageReturn.addAll(salvage);
      }
      
      return salvageReturn;
   }
   
   public void openGui(ServerPlayerEntity player){
      StellarCoreGui gui = new StellarCoreGui(player,this);
      gui.buildGui();
      gui.open();
      watchingPlayers.add(player);
   }
   
   public void removePlayer(ServerPlayerEntity player){
      watchingPlayers.remove(player);
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(-2,-1,-4),serverWorld.getBlockState(pos).get(HORIZONTAL_FACING));
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
      return ArcanaRegistry.STELLAR_CORE;
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
      this.inventory = new SimpleInventory(size());
      this.inventory.addListener(this);
      if (!this.readLootTable(nbt) && nbt.contains("Items", NbtElement.LIST_TYPE)) {
         Inventories.readNbt(nbt, this.inventory.getHeldStacks(), registryLookup);
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
      if (!this.writeLootTable(nbt)) {
         Inventories.writeNbt(nbt, this.inventory.getHeldStacks(), false, registryLookup);
      }
   }
   
   @Override
   protected Text getContainerName(){
      return Text.literal("Stellar Core");
   }
   
   public Inventory getInventory(){
      return this.inventory;
   }
   
   @Override
   protected DefaultedList<ItemStack> getHeldStacks(){
      return this.inventory.getHeldStacks();
   }
   
   @Override
   protected void setHeldStacks(DefaultedList<ItemStack> list) {
      for(int i = 0; i < list.size(); i++){
         this.inventory.setStack(i,list.get(i));
      }
   }
   
   @Override
   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory){
      return null;
   }
   
   @Override
   public int[] getAvailableSlots(Direction side){
      return new int[0];
   }
   
   @Override
   public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir){
      return false;
   }
   
   @Override
   public boolean canExtract(int slot, ItemStack stack, Direction dir){
      return false;
   }
   
   @Override
   public int size(){
      return 1;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         if(!(getWorld() instanceof ServerWorld serverWorld)){
            updating = false;
            return;
         }
         boolean moltenCore = ArcanaAugments.getAugmentFromMap(getAugments(),ArcanaAugments.MOLTEN_CORE.id) >= 1;
         BlockState blockState = serverWorld.getBlockState(pos);
         Direction dir = blockState.get(StellarCore.StellarCoreBlock.HORIZONTAL_FACING);
         Vec3d itemSpawnPos = pos.add(dir.getVector()).toCenterPos();
         
         ItemStack stack = inv.getStack(0);
         List<ItemStack> salvage = salvageItem(stack);
         if(!salvage.isEmpty()){
            salvage = salvage.stream().filter(s -> !s.isEmpty() && s.getCount()>0).toList();
            watchingPlayers.forEach(player -> ArcanaAchievements.progress(player,ArcanaAchievements.RECLAMATION.id, stack.getCount()));
            if(salvage.stream().anyMatch(s -> s.isOf(Items.NETHERITE_SCRAP))){
               watchingPlayers.forEach(player -> ArcanaAchievements.grant(player,ArcanaAchievements.SCRAP_TO_SCRAP.id));
            }
            
            inv.setStack(0,ItemStack.EMPTY);
            
            for(ItemStack itemStack : salvage){
               ItemScatterer.spawn(getWorld(), itemSpawnPos.getX(),itemSpawnPos.getY(),itemSpawnPos.getZ(), itemStack);
            }
            
            watchingPlayers.forEach(player -> PLAYER_DATA.get(player).addXP(100));
            
            SoundUtils.playSound(serverWorld,getPos(), SoundEvents.ENTITY_BLAZE_DEATH, SoundCategory.BLOCKS, 1, 0.8f);
            SoundUtils.playSound(serverWorld,getPos(), SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.BLOCKS, 1, 1.2f);
         }else if(moltenCore){
            Item moltenItem = MOLTEN_CORE_ITEMS.get(stack.getItem());
            if(moltenItem != null){
               int returnCount = stack.getCount() * 2;
               inv.setStack(0,ItemStack.EMPTY);
               int finalReturnCount = returnCount;
               watchingPlayers.forEach(player -> PLAYER_DATA.get(player).addXP(moltenItem instanceof BlockItem ? finalReturnCount * 18 : finalReturnCount * 2));
               ArrayList<ItemStack> items = new ArrayList<>();
               
               while(returnCount > 64){
                  items.add(new ItemStack(moltenItem,64));
                  returnCount -= 64;
               }
               if(returnCount > 0){
                  items.add(new ItemStack(moltenItem,returnCount));
               }
               for(ItemStack itemStack : items){
                  ItemScatterer.spawn(serverWorld, itemSpawnPos.getX(),itemSpawnPos.getY(),itemSpawnPos.getZ(), itemStack);
               }
               
               SoundUtils.playSound(serverWorld,getPos(), SoundEvents.ENTITY_BLAZE_DEATH, SoundCategory.BLOCKS, 1, 0.8f);
               SoundUtils.playSound(serverWorld,getPos(), SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.BLOCKS, 1, 1.2f);
            }
         }
         
         updating = false;
      }
   }
}