package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.*;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.gui.twilightanvil.RenameGui;
import net.borisshoes.arcananovum.gui.twilightanvil.TwilightAnvilGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class TwilightAnvilBlockEntity extends BlockEntity implements PolymerObject, MagicBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   
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
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return;
      }
      
      if(serverWorld.getServer().getTicks() % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
      }
   }
   
   public void openGui(int screen, ServerPlayerEntity player, String data){  // 0 - Menu (hopper), 1 - Anvil (9x3), 2 - Augmenting (9x4), 3 - Rename (anvil), 4 - Item View (9x6)
      WatchedGui gui = null;
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
         ArcaneTome.buildItemGui(((TwilightAnvilGui)gui),player,data);
      }
      if(gui != null){
         if(!gui.tryOpen(player)){
            player.sendMessage(Text.literal("Someone else is using the Anvil").formatted(Formatting.RED),true);
         }
      }
   }
   
   public AnvilOutputSet calculateOutput(ItemStack input1, ItemStack input2){
      ItemStack output = input1.copy();
      int levelCost;
      int repairItemUsage = 0;
      int runningLevelCost = 0;
      int existingRepairCost = 0;
      if (input1.isEmpty()) {
         return new AnvilOutputSet(input1,input2,ItemStack.EMPTY,0,0);
      }
      Map<Enchantment, Integer> map = EnchantmentHelper.get(output);
      existingRepairCost += input1.getRepairCost() + (input2.isEmpty() ? 0 : input2.getRepairCost());
      if (!input2.isEmpty()) {
         boolean slot2IsBook = input2.isOf(Items.ENCHANTED_BOOK) && !EnchantedBookItem.getEnchantmentNbt(input2).isEmpty();
         if (output.isDamageable() && output.getItem().canRepair(input1, input2)) {
            int repairCount;
            int nextRepairAmt = Math.min(output.getDamage(), output.getMaxDamage() / 4);
            if (nextRepairAmt <= 0) {
               return new AnvilOutputSet(input1,input2,ItemStack.EMPTY,0,0);
            }
            for (repairCount = 0; nextRepairAmt > 0 && repairCount < input2.getCount(); ++repairCount) {
               int n = output.getDamage() - nextRepairAmt;
               output.setDamage(n);
               ++runningLevelCost;
               nextRepairAmt = Math.min(output.getDamage(), output.getMaxDamage() / 4);
            }
            repairItemUsage = repairCount;
         } else {
            if (!(slot2IsBook || output.isOf(input2.getItem()) && output.isDamageable())) {
               return new AnvilOutputSet(input1,input2,ItemStack.EMPTY,0,0);
            }
            if (output.isDamageable() && !slot2IsBook) {
               int missingDmg1 = input1.getMaxDamage() - input1.getDamage();
               int missingDmg2 = input2.getMaxDamage() - input2.getDamage();
               int boostDmg = missingDmg2 + output.getMaxDamage() * 12 / 100;
               int combinedDmg = missingDmg1 + boostDmg;
               int remainingDmg = output.getMaxDamage() - combinedDmg;
               if (remainingDmg < 0) {
                  remainingDmg = 0;
               }
               if (remainingDmg < output.getDamage()) {
                  output.setDamage(remainingDmg);
                  runningLevelCost += 2;
               }
            }
            Map<Enchantment, Integer> map2 = EnchantmentHelper.get(input2);
            boolean hasCompatibleEnchant = false;
            boolean hasIncompatibleEnchant = false;
            boolean enhancingStats = false;
            for (Enchantment enchantment : map2.keySet()) {
               int combinedLvl;
               if (enchantment == null) continue;
               int enchantLvl = map.getOrDefault(enchantment, 0);
               combinedLvl = enchantLvl == (combinedLvl = map2.get(enchantment).intValue()) ? combinedLvl + 1 : Math.max(combinedLvl, enchantLvl);
               boolean canCombineEnchants = enchantment.isAcceptableItem(input1);
               if (input1.isOf(Items.ENCHANTED_BOOK)) {
                  canCombineEnchants = true;
               }
               for (Enchantment enchantment2 : map.keySet()) {
                  if (enchantment2 == enchantment || enchantment.canCombine(enchantment2)) continue;
                  canCombineEnchants = false;
                  ++runningLevelCost;
               }
               if (!canCombineEnchants) {
                  hasIncompatibleEnchant = true;
                  continue;
               }
               hasCompatibleEnchant = true;
               if (combinedLvl > enchantment.getMaxLevel()) {
                  combinedLvl = enchantment.getMaxLevel();
               }
               map.put(enchantment, combinedLvl);
               int rarityCost = 0;
               switch (enchantment.getRarity()) {
                  case COMMON: {
                     rarityCost = 1;
                     break;
                  }
                  case UNCOMMON: {
                     rarityCost = 2;
                     break;
                  }
                  case RARE: {
                     rarityCost = 4;
                     break;
                  }
                  case VERY_RARE: {
                     rarityCost = 8;
                  }
               }
               if (slot2IsBook) {
                  rarityCost = Math.max(1, rarityCost / 2);
               }
               runningLevelCost += rarityCost * combinedLvl;
               if (input1.getCount() <= 1) continue;
               runningLevelCost = 40;
            }
            if(input1.isOf(input2.getItem())){ // Enhanced Stats combining
               boolean enhanced1 = EnhancedStatUtils.isEnhanced(input1);
               boolean enhanced2 = EnhancedStatUtils.isEnhanced(input2);
               enhancingStats = enhanced2;
               if(enhanced1 && enhanced2){ // Perform combination calculation
                  double stat1 = input1.getNbt().getDouble("ArcanaStats");
                  double stat2 = input2.getNbt().getDouble("ArcanaStats");
                  double combined = Math.min(1,EnhancedStatUtils.combineStats(stat1,stat2) + 0.025*ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.ENHANCED_ENHANCEMENTS.id));
                  output.removeSubNbt("AttributeModifiers"); // Clear stats from first item
                  EnhancedStatUtils.enhanceItem(output,combined);
                  runningLevelCost += (int) (40*combined);
               }else if(enhanced2){ // Enhance output with stats of 2nd slot
                  double stat2 = input2.getNbt().getDouble("ArcanaStats");
                  EnhancedStatUtils.enhanceItem(output,stat2);
                  runningLevelCost += (int) (20*stat2);
               }
            }
            if (hasIncompatibleEnchant && !hasCompatibleEnchant && !enhancingStats) { // No compatible enchants, can't combine
               return new AnvilOutputSet(input1,input2,ItemStack.EMPTY,0,0);
            }
         }
      }
      
      levelCost = (existingRepairCost + runningLevelCost);
      if (runningLevelCost <= 0) {
         output = ItemStack.EMPTY;
      }
      if (!output.isEmpty()) {
         int newRepairCost = output.getRepairCost();
         if (!input2.isEmpty() && newRepairCost < input2.getRepairCost()) {
            newRepairCost = input2.getRepairCost();
         }
         newRepairCost = AnvilScreenHandler.getNextCost(newRepairCost);
         output.setRepairCost(newRepairCost);
         EnchantmentHelper.set(map, output);
      }
      return new AnvilOutputSet(input1,input2,output,levelCost,repairItemUsage);
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
      return ArcanaRegistry.TWILIGHT_ANVIL;
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
   
   public record AnvilOutputSet(ItemStack input1, ItemStack input2, ItemStack output, int levelCost, int itemRepairUsage){
      public AnvilOutputSet {
         Objects.requireNonNull(input1);
         Objects.requireNonNull(input2);
         Objects.requireNonNull(output);
      }
   }
   
   
}
