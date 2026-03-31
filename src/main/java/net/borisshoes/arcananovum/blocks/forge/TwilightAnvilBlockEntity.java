package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.twilightanvil.RenameGui;
import net.borisshoes.arcananovum.gui.twilightanvil.TwilightAnvilGui;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.TreeMap;

public class TwilightAnvilBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment, Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private ArcanaSkin skin;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private boolean seenForge;
   
   public TwilightAnvilBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.TWILIGHT_ANVIL).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment, Integer> augments, String crafterId, String uuid, int origin, ArcanaSkin skin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.skin = skin;
      this.customName = customName == null ? "" : customName;
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof TwilightAnvilBlockEntity anvil){
         anvil.tick();
      }
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(serverWorld.getServer().getTickCount() % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
         this.seenForge = StarlightForge.findActiveForge(serverWorld, worldPosition) != null;
      }
      
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.assembled && this.seenForge){
         ArcanaNovum.addActiveBlock(new Tuple<>(this, this));
      }
   }
   
   public void openGui(int screen, ServerPlayer player, String data){  // 0 - Menu (hopper), 1 - Anvil (9x3), 2 - Augmenting (9x4), 3 - Rename (anvil), 4 - Item View (9x6)
      SimpleGui gui = null;
      if(screen == 0){
         gui = new TwilightAnvilGui(MenuType.HOPPER, player, this, screen);
         ((TwilightAnvilGui) gui).buildMenuGui();
      }else if(screen == 1){
         gui = new TwilightAnvilGui(MenuType.GENERIC_9x3, player, this, screen);
         ((TwilightAnvilGui) gui).buildAnvilGui();
      }else if(screen == 2){
         gui = new TwilightAnvilGui(MenuType.GENERIC_9x4, player, this, screen);
         ((TwilightAnvilGui) gui).buildTinkerGui();
      }else if(screen == 3){
         gui = new RenameGui(player, this);
         ((RenameGui) gui).build();
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
      if(input1.isEmpty() || !EnchantmentHelper.canStoreEnchantments(input1)){
         return new AnvilOutputSet(input1, input2, ItemStack.EMPTY, 0, 0);
      }
      ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(output));
      combinedRepairCost += (long) input1.getOrDefault(DataComponents.REPAIR_COST, 0) + (long) input2.getOrDefault(DataComponents.REPAIR_COST, 0);
      if(!input2.isEmpty()){
         boolean input2Enchanted = input2.has(DataComponents.STORED_ENCHANTMENTS);
         if(output.isDamageableItem() && input1.isValidRepairItem(input2)){
            int repairCount;
            repairedDamage = Math.min(output.getDamageValue(), output.getMaxDamage() / 4);
            if(repairedDamage <= 0){
               return new AnvilOutputSet(input1, input2, ItemStack.EMPTY, 0, 0);
            }
            for(repairCount = 0; repairedDamage > 0 && repairCount < input2.getCount(); ++repairCount){
               int newDamage = output.getDamageValue() - repairedDamage;
               output.setDamageValue(newDamage);
               ++runningLevelCost;
               repairedDamage = Math.min(output.getDamageValue(), output.getMaxDamage() / 4);
            }
            repairItemUsage = repairCount;
         }else{
            if(!(input2Enchanted || output.is(input2.getItem()) && output.isDamageableItem())){
               return new AnvilOutputSet(input1, input2, ItemStack.EMPTY, 0, 0);
            }
            if(output.isDamageableItem() && !input2Enchanted){
               int input1Durability = input1.getMaxDamage() - input1.getDamageValue();
               int input2Durability = input2.getMaxDamage() - input2.getDamageValue();
               int bonusDurability = input2Durability + output.getMaxDamage() * 12 / 100;
               int otherRepairedDamage = input1Durability + bonusDurability;
               int newDamage = output.getMaxDamage() - otherRepairedDamage;
               if(newDamage < 0){
                  newDamage = 0;
               }
               if(newDamage < output.getDamageValue()){
                  output.setDamageValue(newDamage);
                  runningLevelCost += 2;
               }
            }
            ItemEnchantments itemEnchantmentsComponent = EnchantmentHelper.getEnchantmentsForCrafting(input2);
            boolean hasCompatibleEnchant = false;
            boolean hasIncompatibleEnchant = false;
            for(Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantmentsComponent.entrySet()){
               int newLevel;
               Holder<Enchantment> enchantmentEntry = entry.getKey();
               Enchantment enchantment = (Enchantment) enchantmentEntry.value();
               int level = builder.getLevel(enchantmentEntry);
               newLevel = level == (newLevel = entry.getIntValue()) ? newLevel + 1 : Math.max(newLevel, level);
               boolean canCombine = enchantment.canEnchant(input1);
               if(input1.is(Items.ENCHANTED_BOOK)){
                  canCombine = true;
               }
               for(Holder<Enchantment> enchantmentEntry2 : builder.keySet()){
                  if(enchantmentEntry2.equals(enchantmentEntry) || Enchantment.areCompatible(enchantmentEntry, enchantmentEntry2))
                     continue;
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
            
            if(input1.is(input2.getItem())){ // Enhanced Stats combining
               boolean enhanced1 = EnhancedStatUtils.isEnhanced(input1);
               boolean enhanced2 = EnhancedStatUtils.isEnhanced(input2);
               enhancingStats = enhanced2;
               if(enhanced1 && enhanced2){ // Perform combination calculation
                  double stat1 = ArcanaItem.getDoubleProperty(input1, EnhancedStatUtils.ENHANCED_STAT_TAG);
                  double stat2 = ArcanaItem.getDoubleProperty(input2, EnhancedStatUtils.ENHANCED_STAT_TAG);
                  double buff = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.TWILIGHT_ANVIL_INFUSION_BUFF_PER_LVL).get(ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.ENHANCED_ENHANCEMENTS));
                  double combined = Math.min(1, EnhancedStatUtils.combineStats(stat1, stat2) + buff);
                  EnhancedStatUtils.enhanceItem(output, combined);
                  runningLevelCost += (int) (40 * combined);
               }else if(enhanced2){ // Enhance output with stats of 2nd slot
                  double stat2 = ArcanaItem.getDoubleProperty(input2, EnhancedStatUtils.ENHANCED_STAT_TAG);
                  EnhancedStatUtils.enhanceItem(output, stat2);
                  runningLevelCost += (int) (20 * stat2);
               }
            }
            
            if(hasIncompatibleEnchant && !hasCompatibleEnchant && !enhancingStats){
               return new AnvilOutputSet(input1, input2, ItemStack.EMPTY, 0, 0);
            }
         }
      }
      int levelCost = (int) Mth.clamp(combinedRepairCost + (long) runningLevelCost, 0L, Integer.MAX_VALUE);
      if(runningLevelCost <= 0){
         output = ItemStack.EMPTY;
      }
      if(!output.isEmpty()){
         repairedDamage = output.getOrDefault(DataComponents.REPAIR_COST, 0);
         if(repairedDamage < input2.getOrDefault(DataComponents.REPAIR_COST, 0)){
            repairedDamage = input2.getOrDefault(DataComponents.REPAIR_COST, 0);
         }
         repairedDamage = AnvilMenu.calculateIncreasedRepairCost(repairedDamage);
         output.set(DataComponents.REPAIR_COST, repairedDamage);
         EnchantmentHelper.setEnchantments(output, builder.toImmutable());
      }
      return new AnvilOutputSet(input1, input2, output, levelCost, repairItemUsage);
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition, serverWorld.getBlockState(worldPosition), new BlockPos(((MultiblockCore) ArcanaRegistry.TWILIGHT_ANVIL).getCheckOffset()), null);
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
      return ArcanaRegistry.TWILIGHT_ANVIL;
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.skin = ArcanaSkin.getSkinFromString(view.getStringOr(ArcanaBlockEntity.SKIN_TAG, ""));
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
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
   }
   
   public record AnvilOutputSet(ItemStack input1, ItemStack input2, ItemStack output, int levelCost,
                                int itemRepairUsage) {
      public AnvilOutputSet{
         Objects.requireNonNull(input1);
         Objects.requireNonNull(input2);
         Objects.requireNonNull(output);
      }
   }
   
   
}
