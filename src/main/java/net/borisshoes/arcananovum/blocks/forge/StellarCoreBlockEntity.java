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
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.RepairableComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
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

import static net.borisshoes.arcananovum.blocks.forge.StellarCore.MOLTEN_CORE_ITEMS;
import static net.borisshoes.arcananovum.blocks.forge.StellarCore.StellarCoreBlock.HORIZONTAL_FACING;

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
      if(!(this.world instanceof ServerWorld serverWorld)){
         return;
      }
      int ticks = serverWorld.getServer().getTicks();
      BlockState blockState = serverWorld.getBlockState(pos);
      
      if(ticks % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
         this.seenForge = StarlightForge.findActiveForge(serverWorld,pos) != null;
      }
      
      if(assembled && seenForge){
         Direction dir = blockState.get(HORIZONTAL_FACING);
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
   
   private Map<Item,Integer> getCraftingSalvageIngredients(ItemStack stack, MinecraftServer server){
      Map<Item,Integer> bestRecipe = new HashMap<>();
      if(stack.contains(DataComponentTypes.REPAIRABLE)){
         RepairableComponent repairComp = stack.get(DataComponentTypes.REPAIRABLE);
         List<ItemStack> repairItems = repairComp.items().stream().map(entry -> entry.value().getDefaultStack()).toList();
         
         Collection<RecipeEntry<CraftingRecipe>> craftingRecipes = server.getRecipeManager().getAllOfType(RecipeType.CRAFTING);
         for(RecipeEntry<CraftingRecipe> entry : craftingRecipes){
            CraftingRecipe recipe = entry.value();
            List<Ingredient> ingredients = new ArrayList<>();
            if(recipe instanceof ShapedRecipe shaped && shaped.result.isOf(stack.getItem())){
               for(Optional<Ingredient> ingredient : shaped.getIngredients()){
                  if(ingredient.isEmpty() || ingredient.get().isEmpty()) continue;
                  ingredients.add(ingredient.get());
               }
            }else if(recipe instanceof ShapelessRecipe shapeless && shapeless.result.isOf(stack.getItem())){
               for(Ingredient ingredient : shapeless.ingredients){
                  if(ingredient.isEmpty()) continue;
                  ingredients.add(ingredient);
               }
            }
            
            Map<Item,Integer> ingreds = new HashMap<>();
            for(Ingredient ingredient : ingredients){
               ingredBlock: {
                  for(RegistryEntry<Item> ientry : ingredient.getMatchingItems().toList()){
                     for(ItemStack repairItem : repairItems){
                        if(repairItem.isOf(ientry.value())){
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
      double salvageLvl = .25*(1+ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.DYSON_SPHERE.id));
      Item item = stack.getItem();
      if(ArcanaItemUtils.isArcane(stack)) return salvage;
      
      if(stack.contains(DataComponentTypes.REPAIRABLE)){
         RepairableComponent repairComp = stack.get(DataComponentTypes.REPAIRABLE);
         List<ItemStack> repairItems = repairComp.items().stream().map(entry -> entry.value().getDefaultStack()).toList();
         
         Collection<RecipeEntry<CraftingRecipe>> craftingRecipes = server.getRecipeManager().getAllOfType(RecipeType.CRAFTING);
         Collection<RecipeEntry<SmithingRecipe>> smithingRecipes = server.getRecipeManager().getAllOfType(RecipeType.SMITHING);
         
         Item precursor = Items.AIR;
         Map<Item,Integer> precursorSalvage = new HashMap<>();
         for(RecipeEntry<SmithingRecipe> entry : smithingRecipes){
            if(entry.value() instanceof SmithingTransformRecipe smithingRecipe){
               if(!smithingRecipe.result.itemEntry().equals(stack.getItem())) continue;
               if(smithingRecipe.template().isEmpty() || !smithingRecipe.template().get().test(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE.getDefaultStack())) continue;
               if(smithingRecipe.addition().isEmpty() || repairItems.stream().noneMatch(repair -> smithingRecipe.addition().get().test(repair))) continue;
               
               for(RegistryEntry<Item> ientry : smithingRecipe.base().getMatchingItems().toList()){
                  Map<Item,Integer> salv = getCraftingSalvageIngredients(ientry.value().getDefaultStack(),server);
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
            if(this.getWorld().random.nextDouble() < (baseCount - salvCount - 1E-9)){
               salvCount++;
            }
            salvage.add(new ItemStack(salvItem,salvCount));
         });
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
      if(!(this.world instanceof ServerWorld serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(((MultiblockCore) ArcanaRegistry.STELLAR_CORE).getCheckOffset()),serverWorld.getBlockState(pos).get(HORIZONTAL_FACING));
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
      this.inventory = new SimpleInventory(size());
      this.inventory.addListener(this);
      if(!this.readLootTable(nbt) && nbt.contains("Items")){
         Inventories.readNbt(nbt, this.inventory.getHeldStacks(), registryLookup);
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
      if(!this.writeLootTable(nbt)){
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
         Direction dir = blockState.get(HORIZONTAL_FACING);
         Vec3d itemSpawnPos = pos.add(dir.getVector()).toCenterPos();
         
         ItemStack stack = inv.getStack(0);
         List<ItemStack> salvage = salvageItem(stack, serverWorld.getServer());
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
            
            watchingPlayers.forEach(player -> ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.STELLAR_CORE_SALVAGE)));
            
            SoundUtils.playSound(serverWorld,getPos(), SoundEvents.ENTITY_BLAZE_DEATH, SoundCategory.BLOCKS, 1, 0.8f);
            SoundUtils.playSound(serverWorld,getPos(), SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.BLOCKS, 1, 1.2f);
         }else if(moltenCore){
            ItemStack moltenItem = MOLTEN_CORE_ITEMS.get(stack.getItem());
            if(moltenItem != null){
               int returnCount = stack.getCount() * moltenItem.getCount();
               inv.setStack(0,ItemStack.EMPTY);
               int finalReturnCount = returnCount;
               final int xpPerSmelt = ArcanaConfig.getInt(ArcanaRegistry.STELLAR_CORE_SMELT) * ((stack.getItem().getTranslationKey().contains("raw") && stack.getItem().getTranslationKey().contains("block")) ? 9 : 1);
               
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