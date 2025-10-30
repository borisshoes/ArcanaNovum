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
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.borisshoes.arcananovum.blocks.forge.StellarCore.StellarCoreBlock.HORIZONTAL_FACING;

public class ArcaneSingularityBlockEntity extends LootableContainerBlockEntity implements SidedInventory, PolymerObject, InventoryChangedListener, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private SimpleInventory inventory = new SimpleInventory(size());
   private boolean seenForge;
   private boolean updating;
   private final HashMap<ServerPlayerEntity,ArcaneSingularityGui> watchingPlayers = new HashMap<>();
   
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
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof ArcaneSingularityBlockEntity singularity){
         singularity.tick();
      }
   }
   
   private void tick(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return;
      }
      int ticks = serverWorld.getServer().getTicks();
      
      if(ticks % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
         this.seenForge = StarlightForge.findActiveForge(serverWorld,pos) != null;
      }
      
      if(assembled && seenForge){
         Direction dir = serverWorld.getBlockState(pos).get(ArcaneSingularity.ArcaneSingularityBlock.HORIZONTAL_FACING);
         Vec3d center = pos.add(dir.getVector().multiply(-1)).toCenterPos().add(0,2.5,0);
         double fillPercent = (0.75+0.05*ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.SUPERMASSIVE.id)) * ((double) getNumBooks() / getCapacity());
         ArcanaEffectUtils.arcaneSingularityAnim(serverWorld,center,ticks % 300,dir,fillPercent);
         
         if(Math.random() < 0.001){
            SoundUtils.playSound(serverWorld,BlockPos.ofFloored(center), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS,0.3f,1+(float)Math.random());
         }
         if(Math.random() < 0.0005){
            SoundUtils.playSound(serverWorld,BlockPos.ofFloored(center), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS,1f,0.5f);
         }
      }
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.assembled && this.seenForge){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
      
      watchingPlayers.entrySet().removeIf(entry -> entry.getKey().currentScreenHandler == entry.getKey().playerScreenHandler);
   }
   
   public void removePlayer(ServerPlayerEntity player){
      watchingPlayers.remove(player);
   }
   
   public int getNumBooks(){
      int count = 0;
      for(int i = 0; i < size(); i++){
         if(!inventory.getStack(i).isEmpty()){
            count++;
         }
      }
      return count;
   }
   
   public SingularityResult addBook(ItemStack book){
      if(getNumBooks() >= getCapacity() || !inventory.canInsert(book)){
         return SingularityResult.FULL;
      }
      ArcanaItem.putProperty(book, ArcaneSingularity.SINGULARITY_TAG,NbtString.of(UUID.randomUUID().toString()));
      inventory.addStack(book);
      markDirty();
      return SingularityResult.SUCCESS;
   }
   
   public SingularityResult mergeBooks(ItemStack book1, ItemStack book2){
      boolean book1Found = inventory.getHeldStacks().contains(book1);
      boolean book2Found = inventory.getHeldStacks().contains(book2);
      if(!book1Found || !book2Found){
         return SingularityResult.NOT_FOUND;
      }
      
      ItemEnchantmentsComponent comp1 = EnchantmentHelper.getEnchantments(book1);
      ItemEnchantmentsComponent comp2 = EnchantmentHelper.getEnchantments(book2);
      ItemEnchantmentsComponent.Builder enchantBuilder = new ItemEnchantmentsComponent.Builder(comp2);
      
      boolean hasCompatibleEnchant = false;
      boolean hasIncompatibleEnchant = false;
      for(Object2IntMap.Entry<RegistryEntry<Enchantment>> entry1 : comp1.getEnchantmentEntries()){
         int combinedLvl = entry1.getIntValue();
         boolean canCombine = true;
         for(Object2IntMap.Entry<RegistryEntry<Enchantment>> entry2 : comp2.getEnchantmentEntries()){
            if(entry1.getKey().value() == entry2.getKey().value()){
               combinedLvl = entry1.getIntValue() == entry2.getIntValue() ? combinedLvl+1 : Math.max(entry1.getIntValue(), entry2.getIntValue());
            }
            
            if(entry1.getKey().value() == entry2.getKey().value() || Enchantment.canBeCombined(entry1.getKey(),entry2.getKey())) continue;
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
         enchantBuilder.add(entry1.getKey(), combinedLvl);
      }
      if(hasIncompatibleEnchant && !hasCompatibleEnchant){
         return SingularityResult.FAIL;
      }
      for(int i = 0; i < inventory.getHeldStacks().size(); i++){
         if(inventory.getStack(i).equals(book1)){
            inventory.removeStack(i);
            break;
         }
      }
      for(int i = 0; i < inventory.getHeldStacks().size(); i++){
         if(inventory.getStack(i).equals(book2)){
            inventory.removeStack(i);
            break;
         }
      }
      ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantmentHelper.set(newBook,enchantBuilder.build());
      ArcanaItem.putProperty(newBook, ArcaneSingularity.SINGULARITY_TAG, NbtString.of(UUID.randomUUID().toString()));
      inventory.addStack(newBook);
      markDirty();
      return SingularityResult.SUCCESS;
   }
   
   public SingularityResult removeBook(ItemStack book){
      boolean bookFound = inventory.getHeldStacks().contains(book);
      if(!bookFound){
         return SingularityResult.NOT_FOUND;
      }
      
      for(int i = 0; i < inventory.getHeldStacks().size(); i++){
         if(inventory.getStack(i).equals(book)){
            inventory.removeStack(i);
            break;
         }
      }
      
      markDirty();
      return SingularityResult.SUCCESS;
   }
   
   public SingularityResult splitBook(ItemStack book){
      boolean bookFound = inventory.getHeldStacks().contains(book);
      if(!bookFound){
         return SingularityResult.NOT_FOUND;
      }
      if(getNumBooks() >= getCapacity() || !inventory.canInsert(book)){
         return SingularityResult.FULL;
      }
      if(getWorld() == null) return SingularityResult.FAIL;
      
      ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(book);
      Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchants = new Object2IntOpenHashMap<>();
      comp.getEnchantmentEntries().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
      
      if(enchants.size() == 1){ // Split enchantment level
         ObjectIterator<Object2IntMap.Entry<RegistryEntry<Enchantment>>> iter = enchants.object2IntEntrySet().fastIterator();
         Object2IntMap.Entry<RegistryEntry<Enchantment>> entry = iter.next();
         if(entry.getIntValue() <= entry.getKey().value().getMinLevel()) return SingularityResult.FAIL;
         
         for(int i = 0; i < inventory.getHeldStacks().size(); i++){
            if(inventory.getStack(i).equals(book)){
               inventory.removeStack(i);
               break;
            }
         }
         
         ItemStack newBook = EnchantmentHelper.getEnchantedBookWith(new EnchantmentLevelEntry(entry.getKey(), entry.getIntValue()-1));
         ArcanaItem.putProperty(newBook, ArcaneSingularity.SINGULARITY_TAG,NbtString.of(UUID.randomUUID().toString()));
         inventory.addStack(newBook.copy());
         ArcanaItem.putProperty(newBook, ArcaneSingularity.SINGULARITY_TAG,NbtString.of(UUID.randomUUID().toString()));
         inventory.addStack(newBook.copy());
      }else{ // Remove top enchant
         RegistryEntryList<Enchantment> registryEntryList = null;
         Optional<RegistryEntryList.Named<Enchantment>> optional = getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(EnchantmentTags.TOOLTIP_ORDER);
         if(optional.isPresent()){
            registryEntryList = optional.get();
         }
         
         RegistryEntry<Enchantment> registryEntry = null;
         int value = 0;
         int index = Integer.MAX_VALUE;
         
         ObjectIterator<Object2IntMap.Entry<RegistryEntry<Enchantment>>> iter = enchants.object2IntEntrySet().fastIterator();
         while(iter.hasNext()){
            Object2IntMap.Entry<RegistryEntry<Enchantment>> entry = iter.next();
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
            RegistryEntry<Enchantment> finalRegistryEntry = registryEntry;
            int finalValue = value;
            enchants.object2IntEntrySet().removeIf(e -> e.getKey().value() == finalRegistryEntry.value() && finalValue == e.getIntValue());
            
            for(int i = 0; i < inventory.getHeldStacks().size(); i++){
               if(inventory.getStack(i).equals(book)){
                  inventory.removeStack(i);
                  break;
               }
            }
            ItemStack newBook1 = EnchantmentHelper.getEnchantedBookWith(new EnchantmentLevelEntry(registryEntry, value));
            ArcanaItem.putProperty(newBook1, ArcaneSingularity.SINGULARITY_TAG,NbtString.of(UUID.randomUUID().toString()));
            inventory.addStack(newBook1.copy());
         }
         
         ItemStack newBook2 = new ItemStack(Items.ENCHANTED_BOOK);
         ItemEnchantmentsComponent.Builder enchantBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
         enchants.forEach(enchantBuilder::add);
         EnchantmentHelper.set(newBook2,enchantBuilder.build());
         ArcanaItem.putProperty(newBook2, ArcaneSingularity.SINGULARITY_TAG,NbtString.of(UUID.randomUUID().toString()));
         inventory.addStack(newBook2.copy());
      }
      markDirty();
      return SingularityResult.SUCCESS;
   }
   
   public List<ItemStack> getBooks(){
      return this.inventory.getHeldStacks().stream().filter(stack -> !stack.isEmpty()).toList();
   }
   
   public Inventory getInventory(){
      return this.inventory;
   }
   
   public int getCapacity(){
      return (7*4*4)*(1 + ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.SUPERMASSIVE.id)); // 4 pages per level
   }
   
   public void openGui(ServerPlayerEntity player){
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
      if(!(this.world instanceof ServerWorld serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(((MultiblockCore) ArcanaRegistry.ARCANE_SINGULARITY).getCheckOffset()),serverWorld.getBlockState(pos).get(HORIZONTAL_FACING));
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
   
   public NbtList saveBooks(RegistryWrapper.WrapperLookup registryLookup){
      if(this.inventory != null){
         NbtList bookList = new NbtList();
         for(ItemStack book : inventory.getHeldStacks()){
            if(!book.isEmpty()) bookList.add(ItemStack.CODEC.encodeStart(RegistryOps.of(NbtOps.INSTANCE,registryLookup),book).getOrThrow());
         }
         return bookList;
      }else{
         return new NbtList();
      }
   }
   
   public void initializeBooks(NbtList bookList, RegistryWrapper.WrapperLookup registryLookup){
      inventory = new SimpleInventory(size());
      inventory.addListener(this);
      for(NbtElement e : bookList){
         inventory.addStack(ItemStack.CODEC.parse(RegistryOps.of(NbtOps.INSTANCE,registryLookup),e).result().orElse(ItemStack.EMPTY));
      }
   }
   
   @Override
   public void onBlockReplaced(BlockPos pos, BlockState oldState){}
   
   @Override
   public void readData(ReadView view){
      super.readData(view);
      this.uuid = view.getString(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getString(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getString(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getInt(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.augments = new TreeMap<>();
      view.read("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      this.inventory = new SimpleInventory(size());
      this.inventory.addListener(this);
      if (!this.readLootTable(view)) {
         ArcanaUtils.readBigInventory(view, this.inventory.getHeldStacks());
      }
   }
   
   @Override
   protected void writeData(WriteView view){
      super.writeData(view);
      view.putNullable(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG,this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG,this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME,this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG,this.origin);
      if (!this.writeLootTable(view)) {
         ArcanaUtils.writeBigInventory(view, this.inventory.getHeldStacks(), true);
      }
   }
   
   @Override
   protected Text getContainerName(){
      return Text.literal("Arcane Singularity");
   }
   
   @Override
   protected DefaultedList<ItemStack> getHeldStacks(){
      return this.inventory.getHeldStacks();
   }
   
   @Override
   protected void setHeldStacks(DefaultedList<ItemStack> list){
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
      return 1024;
   }
   
   @Override
   public void onInventoryChanged(Inventory sender){
      sendRefresh();
   }
   
   public enum SingularityResult {
      SUCCESS,
      NOT_FOUND,
      FULL,
      FAIL
   }
}
