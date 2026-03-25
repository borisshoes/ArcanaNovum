package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaConfig;
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
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.borisshoes.arcananovum.blocks.forge.StellarCore.MOLTEN_CORE_ITEMS;
import static net.borisshoes.arcananovum.blocks.forge.StellarCore.StellarCoreBlock.HORIZONTAL_FACING;

public class StellarCoreBlockEntity extends RandomizableContainerBlockEntity implements PolymerObject, WorldlyContainer, ContainerListener, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private boolean seenForge;
   private boolean updating;
   private SimpleContainer inventory = new SimpleContainer(getContainerSize());
   private final Set<ServerPlayer> watchingPlayers = new HashSet<>();
   
   public StellarCoreBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.STELLAR_CORE).getMultiblock();
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
      if(e instanceof StellarCoreBlockEntity core){
         core.tick();
      }
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      int ticks = serverWorld.getServer().getTickCount();
      BlockState blockState = serverWorld.getBlockState(worldPosition);
      
      if(ticks % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
         this.seenForge = StarlightForge.findActiveForge(serverWorld, worldPosition) != null;
      }
      
      if(assembled && seenForge){
         Direction dir = blockState.getValue(HORIZONTAL_FACING);
         ArcanaEffectUtils.stellarCoreAnim(serverWorld, worldPosition.offset(dir.getUnitVec3i().multiply(-2)).getCenter().add(0,1,0),ticks % 300, dir);
      }
      
      
      boolean lit = blockState.getValue(StellarCore.StellarCoreBlock.LIT);
      if(lit ^ assembled){
         blockState = blockState.setValue(StellarCore.StellarCoreBlock.LIT,assembled);
         level.setBlock(worldPosition, blockState, Block.UPDATE_ALL);
      }
      
      watchingPlayers.removeIf(player -> player.containerMenu == player.inventoryMenu);
      
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.assembled && this.seenForge){
         ArcanaNovum.addActiveBlock(new Tuple<>(this,this));
      }
   }
   
   private Map<Item,Integer> getCraftingSalvageIngredients(ItemStack stack, MinecraftServer server){
      Map<Item,Integer> bestRecipe = new HashMap<>();
      if(stack.has(DataComponents.REPAIRABLE)){
         Repairable repairComp = stack.get(DataComponents.REPAIRABLE);
         List<ItemStack> repairItems = repairComp.items().stream().map(entry -> entry.value().getDefaultInstance()).toList();
         
         Collection<RecipeHolder<CraftingRecipe>> craftingRecipes = server.getRecipeManager().getAllOfType(RecipeType.CRAFTING);
         for(RecipeHolder<CraftingRecipe> entry : craftingRecipes){
            CraftingRecipe recipe = entry.value();
            List<Ingredient> ingredients = new ArrayList<>();
            if(recipe instanceof ShapedRecipe shaped && shaped.result.is(stack.getItem())){
               for(Optional<Ingredient> ingredient : shaped.getIngredients()){
                  if(ingredient.isEmpty() || ingredient.get().isEmpty()) continue;
                  ingredients.add(ingredient.get());
               }
            }else if(recipe instanceof ShapelessRecipe shapeless && shapeless.result.is(stack.getItem())){
               for(Ingredient ingredient : shapeless.ingredients){
                  if(ingredient.isEmpty()) continue;
                  ingredients.add(ingredient);
               }
            }
            
            Map<Item,Integer> ingreds = new HashMap<>();
            for(Ingredient ingredient : ingredients){
               ingredBlock: {
                  for(Holder<Item> ientry : ingredient.items().toList()){
                     for(ItemStack repairItem : repairItems){
                        if(repairItem.is(ientry.value())){
                           ingreds.merge(ientry.value(), 1, Integer::sum);
                           break ingredBlock;
                        }
                     }
                  }
               }
            }
            
            if(bestRecipe.isEmpty()){
               bestRecipe = ingreds;
            }else{
               int curCount = bestRecipe.values().stream().mapToInt(Integer::intValue).sum();
               int newCount = ingreds.values().stream().mapToInt(Integer::intValue).sum();
               if(newCount > 0 && newCount < curCount){
                  bestRecipe = ingreds;
               }
            }
         }
         
      }
      return bestRecipe;
   }
   
   public List<ItemStack> salvageItem(ItemStack stack, MinecraftServer server){
      List<ItemStack> salvage = new ArrayList<>();
      double salvageLvl = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.STELLAR_CORE_SALVAGE_PER_LVL).get(ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.DYSON_SPHERE));
      if(ArcanaItemUtils.isArcane(stack)) return salvage;
      
      if(stack.has(DataComponents.REPAIRABLE)){
         Repairable repairComp = stack.get(DataComponents.REPAIRABLE);
         List<ItemStack> repairItems = repairComp.items().stream().map(entry -> entry.value().getDefaultInstance()).toList();
         
         Collection<RecipeHolder<SmithingRecipe>> smithingRecipes = server.getRecipeManager().getAllOfType(RecipeType.SMITHING);
         
         Item precursor = Items.AIR;
         Map<Item,Integer> precursorSalvage = new HashMap<>();
         for(RecipeHolder<SmithingRecipe> entry : smithingRecipes){
            if(entry.value() instanceof SmithingTransformRecipe smithingRecipe){
               if(!stack.is(smithingRecipe.result.item().value())) continue;
               if(smithingRecipe.templateIngredient().isEmpty() || !smithingRecipe.templateIngredient().get().test(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE.getDefaultInstance())) continue;
               if(smithingRecipe.additionIngredient().isEmpty() || repairItems.stream().noneMatch(repair -> smithingRecipe.additionIngredient().get().test(repair))) continue;
               
               for(Holder<Item> ientry : smithingRecipe.baseIngredient().items().toList()){
                  Map<Item,Integer> salv = getCraftingSalvageIngredients(ientry.value().getDefaultInstance(),server);
                  if(precursorSalvage.isEmpty()){
                     precursorSalvage = salv;
                     precursor = ientry.value();
                  }else{
                     int curCount = precursorSalvage.values().stream().mapToInt(Integer::intValue).sum();
                     int newCount = salv.values().stream().mapToInt(Integer::intValue).sum();
                     if(newCount > 0 && newCount < curCount){
                        precursorSalvage = salv;
                        precursor = ientry.value();
                     }
                  }
               }
            }
         }
         
         
         Map<Item,Integer> finalSalv;
         if(precursor != Items.AIR && !precursorSalvage.isEmpty()){
            precursorSalvage.merge(Items.NETHERITE_SCRAP, 4, Integer::sum);
            finalSalv = precursorSalvage;
         }else{
            finalSalv = getCraftingSalvageIngredients(stack,server);
         }
         
         finalSalv.forEach((salvItem, count) -> {
            double baseCount = Math.round(count*salvageLvl);
            int salvCount = (int) baseCount;
            if(this.getLevel().random.nextDouble() < (baseCount - salvCount - 1E-9)){
               salvCount++;
            }
            salvage.add(new ItemStack(salvItem,salvCount));
         });
      }
      
      int rawStardust = ArcanaUtils.calcEssenceFromEnchants(stack);
      double modifier = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.STELLAR_CORE_STARDUST_RATE) +
            ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.STELLAR_CORE_STARDUST_RATE_PER_LVL).get(ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.FUSION_INJECTORS));
      int stardust = (int) (rawStardust * modifier);
      if(stardust > 0){
         while(stardust > 64){
            salvage.add(ArcanaRegistry.STARDUST.getDefaultInstance().copyWithCount(64));
            stardust -= 64;
         }
         if(stardust > 0){
            salvage.add(ArcanaRegistry.STARDUST.getDefaultInstance().copyWithCount(stardust));
         }
      }
      
      List<ItemStack> salvageReturn = new ArrayList<>();
      for(int i=0; i<stack.getCount(); i++){
         salvageReturn.addAll(salvage);
      }
      
      return salvageReturn;
   }
   
   public void openGui(ServerPlayer player){
      StellarCoreGui gui = new StellarCoreGui(player,this);
      gui.buildGui();
      gui.open();
      watchingPlayers.add(player);
   }
   
   public void removePlayer(ServerPlayer player){
      watchingPlayers.remove(player);
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition,serverWorld.getBlockState(worldPosition),new BlockPos(((MultiblockCore) ArcanaRegistry.STELLAR_CORE).getCheckOffset()),serverWorld.getBlockState(worldPosition).getValue(HORIZONTAL_FACING));
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
      return ArcanaRegistry.STELLAR_CORE;
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.inventory = new SimpleContainer(getContainerSize());
      this.inventory.addListener(this);
      if (!this.tryLoadLootTable(view)) {
         ContainerHelper.loadAllItems(view, this.inventory.getItems());
      }
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
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
      if (!this.trySaveLootTable(view)) {
         ContainerHelper.saveAllItems(view, this.inventory.getItems());
      }
   }
   
   @Override
   protected Component getDefaultName(){
      return Component.literal("Stellar Core");
   }
   
   public Container getInventory(){
      return this.inventory;
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
      return 1;
   }
   
   @Override
   public void containerChanged(Container inv){
      if(!updating){
         updating = true;
         if(!(getLevel() instanceof ServerLevel serverWorld)){
            updating = false;
            return;
         }
         boolean moltenCore = ArcanaAugments.getAugmentFromMap(getAugments(),ArcanaAugments.MOLTEN_CORE) >= 1;
         BlockState blockState = serverWorld.getBlockState(worldPosition);
         Direction dir = blockState.getValue(HORIZONTAL_FACING);
         Vec3 itemSpawnPos = worldPosition.offset(dir.getUnitVec3i()).getCenter();
         
         ItemStack stack = inv.getItem(0);
         List<ItemStack> salvage = salvageItem(stack, serverWorld.getServer());
         if(!salvage.isEmpty()){
            salvage = salvage.stream().filter(s -> !s.isEmpty() && s.getCount()>0).toList();
            watchingPlayers.forEach(player -> ArcanaAchievements.progress(player,ArcanaAchievements.RECLAMATION, stack.getCount()));
            if(salvage.stream().anyMatch(s -> s.is(Items.NETHERITE_SCRAP))){
               watchingPlayers.forEach(player -> ArcanaAchievements.grant(player,ArcanaAchievements.SCRAP_TO_SCRAP));
            }
            
            inv.setItem(0, ItemStack.EMPTY);
            
            for(ItemStack itemStack : salvage){
               Containers.dropItemStack(getLevel(), itemSpawnPos.x(),itemSpawnPos.y(),itemSpawnPos.z(), itemStack);
            }
            
            watchingPlayers.forEach(player -> ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_STELLAR_CORE_SALVAGE)));
            
            SoundUtils.playSound(serverWorld, getBlockPos(), SoundEvents.BLAZE_DEATH, SoundSource.BLOCKS, 1, 0.8f);
            SoundUtils.playSound(serverWorld, getBlockPos(), SoundEvents.IRON_GOLEM_HURT, SoundSource.BLOCKS, 1, 1.2f);
         }else if(moltenCore){
            ItemStack moltenItem = MOLTEN_CORE_ITEMS.get(stack.getItem());
            if(moltenItem != null){
               int returnCount = stack.getCount() * moltenItem.getCount();
               inv.setItem(0, ItemStack.EMPTY);
               int finalReturnCount = returnCount;
               final int xpPerSmelt = ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_STELLAR_CORE_SMELT) * ((stack.getItem().getDescriptionId().contains("raw") && stack.getItem().getDescriptionId().contains("block")) ? 9 : 1);
               
               watchingPlayers.forEach(player -> ArcanaNovum.data(player).addXP(xpPerSmelt * finalReturnCount));
               ArrayList<ItemStack> items = new ArrayList<>();
               
               while(returnCount > 64){
                  items.add(moltenItem.copyWithCount(64));
                  returnCount -= 64;
               }
               if(returnCount > 0){
                  items.add(moltenItem.copyWithCount(returnCount));
               }
               for(ItemStack itemStack : items){
                  Containers.dropItemStack(serverWorld, itemSpawnPos.x(),itemSpawnPos.y(),itemSpawnPos.z(), itemStack);
               }
               
               SoundUtils.playSound(serverWorld, getBlockPos(), SoundEvents.BLAZE_DEATH, SoundSource.BLOCKS, 1, 0.8f);
               SoundUtils.playSound(serverWorld, getBlockPos(), SoundEvents.IRON_GOLEM_HURT, SoundSource.BLOCKS, 1, 1.2f);
            }
         }
         
         updating = false;
      }
   }
}