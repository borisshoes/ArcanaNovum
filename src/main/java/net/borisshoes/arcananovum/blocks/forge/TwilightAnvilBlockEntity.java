package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.twilightanvil.RenameGui;
import net.borisshoes.arcananovum.gui.twilightanvil.TwilightAnvilGui;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class TwilightAnvilBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private boolean seenForge;
   
   public TwilightAnvilBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.TWILIGHT_ANVIL).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof TwilightAnvilBlockEntity anvil){
         anvil.tick();
      }
   }
   
   private void tick(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return;
      }
      
      if(serverWorld.getServer().getTicks() % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
         this.seenForge = StarlightForge.findActiveForge(serverWorld,pos) != null;
      }
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.assembled && this.seenForge){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
   }
   
   public void openGui(int screen, ServerPlayerEntity player, String data){  // 0 - Menu (hopper), 1 - Anvil (9x3), 2 - Augmenting (9x4), 3 - Rename (anvil), 4 - Item View (9x6)
      SimpleGui gui = null;
      if(screen == 0){
         gui = new TwilightAnvilGui(ScreenHandlerType.HOPPER,player,this,screen);
         ((TwilightAnvilGui)gui).buildMenuGui();
      }else if(screen == 1){
         gui = new TwilightAnvilGui(ScreenHandlerType.GENERIC_9X3,player,this,screen);
         ((TwilightAnvilGui)gui).buildAnvilGui();
      }else if(screen == 2){
         gui = new TwilightAnvilGui(ScreenHandlerType.GENERIC_9X4,player,this,screen);
         ((TwilightAnvilGui)gui).buildTinkerGui();
      }else if(screen == 3){
         gui = new RenameGui(player, this);
         ((RenameGui)gui).build();
      }else if(screen == 4){
         gui = new TwilightAnvilGui(ScreenHandlerType.GENERIC_9X6,player,this,screen);
         TomeGui.buildItemGui(((TwilightAnvilGui)gui),player,data);
      }
      if(gui != null){
         gui.open();
      }
   }
   
   public AnvilOutputSet calculateOutput(ItemStack input1, ItemStack input2){
      ItemStack output = input1.copy();
      int repairItemUsage = 0;
      int runningLevelCost = 0;
      int repairedDamage;
      long combinedRepairCost = 0L;
      if(input1.isEmpty() || !EnchantmentHelper.canHaveEnchantments(input1)){
         return new AnvilOutputSet(input1,input2,ItemStack.EMPTY,0,0);
      }
      ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(output));
      combinedRepairCost += (long) input1.getOrDefault(DataComponentTypes.REPAIR_COST, 0) + (long) input2.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
      if(!input2.isEmpty()){
         boolean input2Enchanted = input2.contains(DataComponentTypes.STORED_ENCHANTMENTS);
         if(output.isDamageable() && input1.canRepairWith(input2)){
            int repairCount;
            repairedDamage = Math.min(output.getDamage(), output.getMaxDamage() / 4);
            if(repairedDamage <= 0){
               return new AnvilOutputSet(input1,input2,ItemStack.EMPTY,0,0);
            }
            for (repairCount = 0; repairedDamage > 0 && repairCount < input2.getCount(); ++repairCount){
               int newDamage = output.getDamage() - repairedDamage;
               output.setDamage(newDamage);
               ++runningLevelCost;
               repairedDamage = Math.min(output.getDamage(), output.getMaxDamage() / 4);
            }
            repairItemUsage = repairCount;
         }else{
            if(!(input2Enchanted || output.isOf(input2.getItem()) && output.isDamageable())){
               return new AnvilOutputSet(input1,input2,ItemStack.EMPTY,0,0);
            }
            if(output.isDamageable() && !input2Enchanted){
               int input1Durability = input1.getMaxDamage() - input1.getDamage();
               int input2Durability = input2.getMaxDamage() - input2.getDamage();
               int bonusDurability = input2Durability + output.getMaxDamage() * 12 / 100;
               int otherRepairedDamage = input1Durability + bonusDurability;
               int newDamage = output.getMaxDamage() - otherRepairedDamage;
               if(newDamage < 0){
                  newDamage = 0;
               }
               if(newDamage < output.getDamage()){
                  output.setDamage(newDamage);
                  runningLevelCost += 2;
               }
            }
            ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.getEnchantments(input2);
            boolean hasCompatibleEnchant = false;
            boolean hasIncompatibleEnchant = false;
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()){
               int newLevel;
               RegistryEntry<Enchantment> enchantmentEntry = entry.getKey();
               Enchantment enchantment = (Enchantment)enchantmentEntry.value();
               int level = builder.getLevel(enchantmentEntry);
               newLevel = level == (newLevel = entry.getIntValue()) ? newLevel + 1 : Math.max(newLevel, level);
               boolean canCombine = enchantment.isAcceptableItem(input1);
               if(input1.isOf(Items.ENCHANTED_BOOK)){
                  canCombine = true;
               }
               for (RegistryEntry<Enchantment> enchantmentEntry2 : builder.getEnchantments()){
                  if(enchantmentEntry2.equals(enchantmentEntry) || Enchantment.canBeCombined(enchantmentEntry,enchantmentEntry2)) continue;
                  canCombine = false;
                  ++runningLevelCost;
               }
               if(!canCombine){
                  hasIncompatibleEnchant = true;
                  continue;
               }
               hasCompatibleEnchant = true;
               if(newLevel > enchantment.getMaxLevel()){
                  newLevel = enchantment.getMaxLevel();
               }
               builder.set(enchantmentEntry, newLevel);
               int enchantCost = enchantment.getAnvilCost();
               if(input2Enchanted){
                  enchantCost = Math.max(1, enchantCost / 2);
               }
               runningLevelCost += enchantCost * newLevel;
               if(input1.getCount() <= 1) continue;
               runningLevelCost = 40;
            }
            boolean enhancingStats = false;
            
            if(input1.isOf(input2.getItem())){ // Enhanced Stats combining
               boolean enhanced1 = EnhancedStatUtils.isEnhanced(input1);
               boolean enhanced2 = EnhancedStatUtils.isEnhanced(input2);
               enhancingStats = enhanced2;
               if(enhanced1 && enhanced2){ // Perform combination calculation
                  double stat1 = ArcanaItem.getDoubleProperty(input1,EnhancedStatUtils.ENHANCED_STAT_TAG);
                  double stat2 = ArcanaItem.getDoubleProperty(input2,EnhancedStatUtils.ENHANCED_STAT_TAG);
                  double combined = Math.min(1,EnhancedStatUtils.combineStats(stat1,stat2) + 0.025*ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.ENHANCED_ENHANCEMENTS.id));
                  EnhancedStatUtils.enhanceItem(output,combined);
                  runningLevelCost += (int) (40*combined);
               }else if(enhanced2){ // Enhance output with stats of 2nd slot
                  double stat2 = ArcanaItem.getDoubleProperty(input2,EnhancedStatUtils.ENHANCED_STAT_TAG);
                  EnhancedStatUtils.enhanceItem(output,stat2);
                  runningLevelCost += (int) (20*stat2);
               }
            }
            
            if(hasIncompatibleEnchant && !hasCompatibleEnchant && !enhancingStats){
               return new AnvilOutputSet(input1,input2,ItemStack.EMPTY,0,0);
            }
         }
      }
      int levelCost = (int) MathHelper.clamp(combinedRepairCost + (long)runningLevelCost, 0L, Integer.MAX_VALUE);
      if(runningLevelCost <= 0){
         output = ItemStack.EMPTY;
      }
      if(!output.isEmpty()){
         repairedDamage = output.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
         if(repairedDamage < input2.getOrDefault(DataComponentTypes.REPAIR_COST, 0)){
            repairedDamage = input2.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
         }
         repairedDamage = AnvilScreenHandler.getNextCost(repairedDamage);
         output.set(DataComponentTypes.REPAIR_COST, repairedDamage);
         EnchantmentHelper.set(output, builder.build());
      }
      return new AnvilOutputSet(input1,input2,output,levelCost,repairItemUsage);
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(((MultiblockCore) ArcanaRegistry.TWILIGHT_ANVIL).getCheckOffset()),null);
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
      return ArcanaRegistry.TWILIGHT_ANVIL;
   }
   
   @Override
   public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
      super.readNbt(nbt, registryLookup);
      if(nbt.contains("arcanaUuid")){
         this.uuid = nbt.getString("arcanaUuid", "");
      }
      if(nbt.contains("crafterId")){
         this.crafterId = nbt.getString("crafterId", "");
      }
      if(nbt.contains("customName")){
         this.customName = nbt.getString("customName", "");
      }
      if(nbt.contains("synthetic")){
         this.synthetic = nbt.getBoolean("synthetic", false);
      }
      augments = new TreeMap<>();
      if(nbt.contains("arcanaAugments")){
         NbtCompound augCompound = nbt.getCompoundOrEmpty("arcanaAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug, augCompound.getInt(key, 0));
         }
      }
   }
   
   @Override
   protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
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
   }
   
   public record AnvilOutputSet(ItemStack input1, ItemStack input2, ItemStack output, int levelCost, int itemRepairUsage){
      public AnvilOutputSet {
         Objects.requireNonNull(input1);
         Objects.requireNonNull(input2);
         Objects.requireNonNull(output);
      }
   }
   
   
}
