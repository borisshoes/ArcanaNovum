package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.arcanesingularity.ArcaneSingularityGui;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.utils.CodecUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.borisshoes.arcananovum.blocks.forge.StellarCore.StellarCoreBlock.HORIZONTAL_FACING;

public class ArcaneSingularityBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, PolymerObject, ContainerListener, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private SimpleContainer inventory = new SimpleContainer(getContainerSize());
   private boolean seenForge;
   private boolean updating;
   private final HashMap<ServerPlayer,ArcaneSingularityGui> watchingPlayers = new HashMap<>();
   
   public ArcaneSingularityBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.ARCANE_SINGULARITY).getMultiblock();
      this.inventory.addListener(this);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof ArcaneSingularityBlockEntity singularity){
         singularity.tick();
      }
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      int ticks = serverWorld.getServer().getTickCount();
      
      if(ticks % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
         this.seenForge = StarlightForge.findActiveForge(serverWorld, worldPosition) != null;
      }
      
      if(assembled && seenForge){
         Direction dir = serverWorld.getBlockState(worldPosition).getValue(ArcaneSingularity.ArcaneSingularityBlock.HORIZONTAL_FACING);
         Vec3 center = worldPosition.offset(dir.getUnitVec3i().multiply(-1)).getCenter().add(0,2.5,0);
         double fillPercent = (0.75+0.05*ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.SUPERMASSIVE.id)) * ((double) getNumBooks() / getCapacity());
         ArcanaEffectUtils.arcaneSingularityAnim(serverWorld,center,ticks % 300,dir,fillPercent);
         
         if(Math.random() < 0.001){
            SoundUtils.playSound(serverWorld, BlockPos.containing(center), SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS,0.3f,1+(float)Math.random());
         }
         if(Math.random() < 0.0005){
            SoundUtils.playSound(serverWorld, BlockPos.containing(center), SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS,1f,0.5f);
         }
      }
      
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.assembled && this.seenForge){
         ArcanaNovum.addActiveBlock(new Tuple<>(this,this));
      }
      
      watchingPlayers.entrySet().removeIf(entry -> entry.getKey().containerMenu == entry.getKey().inventoryMenu);
   }
   
   public void removePlayer(ServerPlayer player){
      watchingPlayers.remove(player);
   }
   
   public int getNumBooks(){
      int count = 0;
      for(int i = 0; i < getContainerSize(); i++){
         if(!inventory.getItem(i).isEmpty()){
            count++;
         }
      }
      return count;
   }
   
   public SingularityResult addBook(ItemStack book){
      if(getNumBooks() >= getCapacity() || !inventory.canAddItem(book)){
         return SingularityResult.FULL;
      }
      ArcanaItem.putProperty(book, ArcaneSingularity.SINGULARITY_TAG, StringTag.valueOf(UUID.randomUUID().toString()));
      inventory.addItem(book);
      setChanged();
      return SingularityResult.SUCCESS;
   }
   
   public SingularityResult mergeBooks(ItemStack book1, ItemStack book2){
      boolean book1Found = inventory.getItems().contains(book1);
      boolean book2Found = inventory.getItems().contains(book2);
      if(!book1Found || !book2Found){
         return SingularityResult.NOT_FOUND;
      }
      
      ItemEnchantments comp1 = EnchantmentHelper.getEnchantmentsForCrafting(book1);
      ItemEnchantments comp2 = EnchantmentHelper.getEnchantmentsForCrafting(book2);
      ItemEnchantments.Mutable enchantBuilder = new ItemEnchantments.Mutable(comp2);
      
      boolean hasCompatibleEnchant = false;
      boolean hasIncompatibleEnchant = false;
      for(Object2IntMap.Entry<Holder<Enchantment>> entry1 : comp1.entrySet()){
         int combinedLvl = entry1.getIntValue();
         boolean canCombine = true;
         for(Object2IntMap.Entry<Holder<Enchantment>> entry2 : comp2.entrySet()){
            if(entry1.getKey().value() == entry2.getKey().value()){
               combinedLvl = entry1.getIntValue() == entry2.getIntValue() ? combinedLvl+1 : Math.max(entry1.getIntValue(), entry2.getIntValue());
            }
            
            if(entry1.getKey().value() == entry2.getKey().value() || Enchantment.areCompatible(entry1.getKey(),entry2.getKey())) continue;
            canCombine = false;
         }
         if(!canCombine){
            hasIncompatibleEnchant = true;
            continue;
         }
         hasCompatibleEnchant = true;
         if(combinedLvl > entry1.getKey().value().getMaxLevel()){
            combinedLvl = entry1.getKey().value().getMaxLevel();
         }
         enchantBuilder.upgrade(entry1.getKey(), combinedLvl);
      }
      if(hasIncompatibleEnchant && !hasCompatibleEnchant){
         return SingularityResult.FAIL;
      }
      for(int i = 0; i < inventory.getItems().size(); i++){
         if(inventory.getItem(i).equals(book1)){
            inventory.removeItemNoUpdate(i);
            break;
         }
      }
      for(int i = 0; i < inventory.getItems().size(); i++){
         if(inventory.getItem(i).equals(book2)){
            inventory.removeItemNoUpdate(i);
            break;
         }
      }
      ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantmentHelper.setEnchantments(newBook,enchantBuilder.toImmutable());
      ArcanaItem.putProperty(newBook, ArcaneSingularity.SINGULARITY_TAG, StringTag.valueOf(UUID.randomUUID().toString()));
      inventory.addItem(newBook);
      setChanged();
      return SingularityResult.SUCCESS;
   }
   
   public SingularityResult removeBook(ItemStack book){
      boolean bookFound = inventory.getItems().contains(book);
      if(!bookFound){
         return SingularityResult.NOT_FOUND;
      }
      
      for(int i = 0; i < inventory.getItems().size(); i++){
         if(inventory.getItem(i).equals(book)){
            inventory.removeItemNoUpdate(i);
            break;
         }
      }
      
      setChanged();
      return SingularityResult.SUCCESS;
   }
   
   public SingularityResult splitBook(ItemStack book){
      boolean bookFound = inventory.getItems().contains(book);
      if(!bookFound){
         return SingularityResult.NOT_FOUND;
      }
      if(getNumBooks() >= getCapacity() || !inventory.canAddItem(book)){
         return SingularityResult.FULL;
      }
      if(getLevel() == null) return SingularityResult.FAIL;
      
      ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(book);
      Object2IntOpenHashMap<Holder<Enchantment>> enchants = new Object2IntOpenHashMap<>();
      comp.entrySet().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
      
      if(enchants.size() == 1){ // Split enchantment level
         ObjectIterator<Object2IntMap.Entry<Holder<Enchantment>>> iter = enchants.object2IntEntrySet().fastIterator();
         Object2IntMap.Entry<Holder<Enchantment>> entry = iter.next();
         if(entry.getIntValue() <= entry.getKey().value().getMinLevel()) return SingularityResult.FAIL;
         
         for(int i = 0; i < inventory.getItems().size(); i++){
            if(inventory.getItem(i).equals(book)){
               inventory.removeItemNoUpdate(i);
               break;
            }
         }
         
         ItemStack newBook = EnchantmentHelper.createBook(new EnchantmentInstance(entry.getKey(), entry.getIntValue()-1));
         ArcanaItem.putProperty(newBook, ArcaneSingularity.SINGULARITY_TAG, StringTag.valueOf(UUID.randomUUID().toString()));
         inventory.addItem(newBook.copy());
         ArcanaItem.putProperty(newBook, ArcaneSingularity.SINGULARITY_TAG, StringTag.valueOf(UUID.randomUUID().toString()));
         inventory.addItem(newBook.copy());
      }else{ // Remove top enchant
         HolderSet<Enchantment> registryEntryList = null;
         Optional<HolderSet.Named<Enchantment>> optional = getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.TOOLTIP_ORDER);
         if(optional.isPresent()){
            registryEntryList = optional.get();
         }
         
         Holder<Enchantment> registryEntry = null;
         int value = 0;
         int index = Integer.MAX_VALUE;
         
         ObjectIterator<Object2IntMap.Entry<Holder<Enchantment>>> iter = enchants.object2IntEntrySet().fastIterator();
         while(iter.hasNext()){
            Object2IntMap.Entry<Holder<Enchantment>> entry = iter.next();
            if(registryEntryList == null){
               registryEntry = entry.getKey();
               value = entry.getIntValue();
               iter.remove();
               break;
            }
            
            for(int i = 0; i < registryEntryList.size(); i++){
               if(registryEntryList.get(i).value() == entry.getKey().value() && i < index){
                  index = i;
                  registryEntry = entry.getKey();
                  value = entry.getIntValue();
               }
            }
         }
         
         if(index != Integer.MAX_VALUE && registryEntry != null){
            Holder<Enchantment> finalRegistryEntry = registryEntry;
            int finalValue = value;
            enchants.object2IntEntrySet().removeIf(e -> e.getKey().value() == finalRegistryEntry.value() && finalValue == e.getIntValue());
            
            for(int i = 0; i < inventory.getItems().size(); i++){
               if(inventory.getItem(i).equals(book)){
                  inventory.removeItemNoUpdate(i);
                  break;
               }
            }
            ItemStack newBook1 = EnchantmentHelper.createBook(new EnchantmentInstance(registryEntry, value));
            ArcanaItem.putProperty(newBook1, ArcaneSingularity.SINGULARITY_TAG, StringTag.valueOf(UUID.randomUUID().toString()));
            inventory.addItem(newBook1.copy());
         }
         
         ItemStack newBook2 = new ItemStack(Items.ENCHANTED_BOOK);
         ItemEnchantments.Mutable enchantBuilder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
         enchants.forEach(enchantBuilder::upgrade);
         EnchantmentHelper.setEnchantments(newBook2,enchantBuilder.toImmutable());
         ArcanaItem.putProperty(newBook2, ArcaneSingularity.SINGULARITY_TAG, StringTag.valueOf(UUID.randomUUID().toString()));
         inventory.addItem(newBook2.copy());
      }
      setChanged();
      return SingularityResult.SUCCESS;
   }
   
   public List<ItemStack> getBooks(){
      return this.inventory.getItems().stream().filter(stack -> !stack.isEmpty()).toList();
   }
   
   public Container getInventory(){
      return this.inventory;
   }
   
   public int getCapacity(){
      return (7*4*4)*(1 + ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.SUPERMASSIVE.id)); // 4 pages per level
   }
   
   public void openGui(ServerPlayer player){
      ArcaneSingularityGui gui = new ArcaneSingularityGui(player,this,getCapacity());
      gui.buildGui();
      gui.open();
      watchingPlayers.put(player,gui);
   }
   
   private void sendRefresh(){
      watchingPlayers.forEach((player, gui) -> gui.buildGui());
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition,serverWorld.getBlockState(worldPosition),new BlockPos(((MultiblockCore) ArcanaRegistry.ARCANE_SINGULARITY).getCheckOffset()),serverWorld.getBlockState(worldPosition).getValue(HORIZONTAL_FACING));
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
      return ArcanaRegistry.ARCANE_SINGULARITY;
   }
   
   public ListTag saveBooks(HolderLookup.Provider registryLookup){
      if(this.inventory != null){
         ListTag bookList = new ListTag();
         for(ItemStack book : inventory.getItems()){
            if(!book.isEmpty()) bookList.add(ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,registryLookup),book).getOrThrow());
         }
         return bookList;
      }else{
         return new ListTag();
      }
   }
   
   public void initializeBooks(ListTag bookList, HolderLookup.Provider registryLookup){
      inventory = new SimpleContainer(getContainerSize());
      inventory.addListener(this);
      for(Tag e : bookList){
         inventory.addItem(ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE,registryLookup),e).result().orElse(ItemStack.EMPTY));
      }
   }
   
   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState oldState){}
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.augments = new TreeMap<>();
      view.read("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      this.inventory = new SimpleContainer(getContainerSize());
      if (!this.tryLoadLootTable(view)) {
         CodecUtils.readBigInventory(view, this.inventory.getItems());
      }
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG,this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG,this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME,this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG,this.origin);
      if (!this.trySaveLootTable(view)) {
         CodecUtils.writeBigInventory(view, this.inventory.getItems(), true);
      }
   }
   
   @Override
   protected Component getDefaultName(){
      return Component.literal("Arcane Singularity");
   }
   
   @Override
   protected NonNullList<ItemStack> getItems(){
      return this.inventory.getItems();
   }
   
   @Override
   protected void setItems(NonNullList<ItemStack> list){
      for(int i = 0; i < list.size(); i++){
         this.inventory.setItem(i,list.get(i));
      }
   }
   
   @Override
   protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory){
      return null;
   }
   
   @Override
   public int[] getSlotsForFace(Direction side){
      return new int[0];
   }
   
   @Override
   public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir){
      return false;
   }
   
   @Override
   public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir){
      return false;
   }
   
   @Override
   public int getContainerSize(){
      return 1024;
   }
   
   @Override
   public void containerChanged(Container sender){
      sendRefresh();
   }
   
   public enum SingularityResult {
      SUCCESS,
      NOT_FOUND,
      FULL,
      FAIL
   }
}
