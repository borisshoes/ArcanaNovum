package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItemContainer;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Clearable;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ChestTranslocator extends EnergyItem implements ArcanaItemContainer.ArcanaItemContainerHaver {
   public static final String ID = "chest_translocator";
   
   public static final String CONTENTS_TAG = "contents";
   public static final String STATE_TAG = "state";
   
   public ChestTranslocator(){
      id = ID;
      name = "Chest Translocator";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.SPRUCE_BOAT;
      item = new ChestTranslocatorItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.GOLD);
      researchTasks = new RegistryKey[]{ResearchTasks.USE_ENDER_CHEST,ResearchTasks.EFFECT_STRENGTH};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,CONTENTS_TAG,new NbtCompound());
      putProperty(stack,STATE_TAG,new NbtCompound());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Moving items").formatted(Formatting.DARK_RED))
            .append(Text.literal(" from one ").formatted(Formatting.GRAY))
            .append(Text.literal("chest ").formatted(Formatting.GOLD))
            .append(Text.literal("to ").formatted(Formatting.GRAY))
            .append(Text.literal("another ").formatted(Formatting.GOLD))
            .append(Text.literal("is a ").formatted(Formatting.GRAY))
            .append(Text.literal("hassle").formatted(Formatting.RED))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Chests").formatted(Formatting.GOLD))
            .append(Text.literal(" are ").formatted(Formatting.GRAY))
            .append(Text.literal("heavy").formatted(Formatting.RED))
            .append(Text.literal(" and carrying them ").formatted(Formatting.GRAY))
            .append(Text.literal("slows ").formatted(Formatting.RED))
            .append(Text.literal("you down ").formatted(Formatting.GRAY))
            .append(Text.literal("significantly").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.DARK_RED))
            .append(Text.literal(" a ").formatted(Formatting.GRAY))
            .append(Text.literal("chest ").formatted(Formatting.GOLD))
            .append(Text.literal("to ").formatted(Formatting.GRAY))
            .append(Text.literal("store ").formatted(Formatting.RED))
            .append(Text.literal("it in the ").formatted(Formatting.GRAY))
            .append(Text.literal("Translocator").formatted(Formatting.GOLD))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_RED))
            .append(Text.literal(" to ").formatted(Formatting.GRAY))
            .append(Text.literal("place ").formatted(Formatting.RED))
            .append(Text.literal("a ").formatted(Formatting.GRAY))
            .append(Text.literal("stored ").formatted(Formatting.RED))
            .append(Text.literal("chest").formatted(Formatting.GOLD))
            .append(Text.literal(" down.").formatted(Formatting.GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      int cdLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.RAPID_TRANSLOCATION.id));
      return 30 - 8*cdLvl;
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public ArcanaItemContainer getArcanaItemContainer(ItemStack item){
      NbtCompound contents = getCompoundProperty(item,CONTENTS_TAG);
      SimpleInventory inv = new SimpleInventory(27);
      for(int i = 0; i < inv.size(); i++){
         inv.setStack(i,ItemStack.EMPTY.copy());
      }
      
      if(!contents.isEmpty()){
         NbtList items = contents.getListOrEmpty("Items");
         
         for(int i = 0; i < items.size(); i++){
            NbtCompound stack = items.getCompoundOrEmpty(i);
            ItemStack itemStack = ItemStack.fromNbt(ArcanaNovum.SERVER.getRegistryManager(),stack).orElse(ItemStack.EMPTY);
            inv.setStack(stack.getByte("Slot", (byte) 0), itemStack);
         }
      }
      
      return new ArcanaItemContainer(inv, 27,25, "CT", "Chest Translocator", 0.5);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound contents = getCompoundProperty(stack,CONTENTS_TAG);
      NbtCompound state = getCompoundProperty(stack,STATE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,CONTENTS_TAG,contents);
      putProperty(newStack,STATE_TAG,state);
      return buildItemLore(newStack,server);
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CHEST,12);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,2);
      ArcanaIngredient c = new ArcanaIngredient(Items.BARREL,12);
      ArcanaIngredient g = new ArcanaIngredient(Items.ENDER_EYE,4);
      ArcanaIngredient h = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_STRENGTH);
      ArcanaIngredient m = new ArcanaIngredient(Items.ENDER_CHEST,4);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("       Chest\n   Translocator").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nChests are great for storage and organization. However, whenever I try to move them, their contents spill all over the place.\nMaybe I can do something about that").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("       Chest\n   Translocator").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nusing an augmented Ender Chest and some additional strength.\n\nUsing the Translocator on a Chest, Trapped Chest, or Barrel will pick it up at the cost of a significant loss in dexterity.\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("       Chest\n   Translocator").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nUsing the Translocator again places the container down, contents intact.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class ChestTranslocatorItem extends ArcanaPolymerItem {
      public ChestTranslocatorItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         NbtCompound contents = getCompoundProperty(itemStack,CONTENTS_TAG);
         NbtCompound state = getCompoundProperty(itemStack,STATE_TAG);
         if(!contents.isEmpty()){
            BlockState blockState = NbtHelper.toBlockState(Registries.BLOCK,state);
            if(blockState.isOf(Blocks.CHEST) || blockState.isOf(Blocks.TRAPPED_CHEST)){
               stringList.add("chest");
            }else if(blockState.isOf(Blocks.BARREL)){
               stringList.add("barrel");
            }
         }
         
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         ItemStack stack = context.getStack();
         PlayerEntity playerEntity = context.getPlayer();
         if(!ArcanaItemUtils.isArcane(stack) || !(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         
         World world = context.getWorld();
         BlockPos blockPos = context.getBlockPos();
         NbtCompound contents = getCompoundProperty(stack,CONTENTS_TAG);
         NbtCompound stateTag = getCompoundProperty(stack,STATE_TAG);
         int cooldown = getEnergy(stack);
         BlockState state = world.getBlockState(blockPos);
         
         if(contents.isEmpty()){
            if(state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST) || state.isOf(Blocks.BARREL)){
               if(cooldown == 0){
                  BlockEntity be = world.getBlockEntity(blockPos);
                  if(be == null) return ActionResult.PASS;
                  NbtCompound contentData = be.createNbtWithId(ArcanaNovum.SERVER.getRegistryManager());
                  putProperty(stack,CONTENTS_TAG,contentData);
                  putProperty(stack,STATE_TAG,NbtHelper.fromBlockState(state));
                  if(be instanceof Clearable clearable) clearable.clear();
                  world.setBlockState(blockPos,Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                  SoundUtils.playSound(world,blockPos,SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1,1);
                  setEnergy(stack,getMaxEnergy(stack));
               }else{
                  player.sendMessage(Text.literal("Translocator Cooldown: "+cooldown+(cooldown != 1 ? " seconds" : " second")).formatted(Formatting.GOLD), true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
               }
            }else{
               return ActionResult.PASS;
            }
         }else{
            Direction side = context.getSide();
            BlockPos placePos = blockPos.add(side.getVector());
            if(world.getBlockState(placePos).isAir()){
               BlockState blockState = NbtHelper.toBlockState(world.createCommandRegistryWrapper(RegistryKeys.BLOCK),stateTag);
               ItemPlacementContext ipc = new ItemPlacementContext(player,context.getHand(),context.getStack(),new BlockHitResult(context.getHitPos(),context.getSide(),context.getBlockPos(),context.hitsInsideBlock()));
               if(blockState.isOf(Blocks.CHEST)){
                  blockState = Blocks.CHEST.getPlacementState(ipc);
               }else if(blockState.isOf(Blocks.TRAPPED_CHEST)){
                  blockState = Blocks.TRAPPED_CHEST.getPlacementState(ipc);
               }else if(blockState.isOf(Blocks.BARREL)){
                  blockState = Blocks.BARREL.getPlacementState(ipc);
               }
               world.setBlockState(placePos,blockState,Block.NOTIFY_ALL);
               BlockEntity blockEntity = world.getBlockEntity(placePos);
               if(blockEntity != null){
                  blockEntity.read(contents,ArcanaNovum.SERVER.getRegistryManager());
                  if(blockEntity instanceof LootableContainerBlockEntity container){
                     int size = container.size();
                     int filled = 0;
                     for(int i = 0; i < size; i++){
                        filled += container.getStack(i).isEmpty() ? 0 : 1;
                     }
                     if(filled == size){
                        ArcanaAchievements.progress(player,ArcanaAchievements.STORAGE_RELOCATION.id, 1);
                     }else if(filled == 0){
                        ArcanaAchievements.grant(player,ArcanaAchievements.PEAK_LAZINESS.id);
                     }
                  }
               }
               
               putProperty(stack,CONTENTS_TAG,new NbtCompound());
               putProperty(stack,STATE_TAG,new NbtCompound());
               ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.CHEST_TRANSLOCATOR_USE)); // Add xp
               SoundUtils.playSound(world,placePos,SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1,1);
            }else{
               player.sendMessage(Text.literal("The chest cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }
         }
         return ActionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         NbtCompound contents = getCompoundProperty(stack,CONTENTS_TAG);
         
         if(!contents.isEmpty()){
            StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 2, false, false, true);
            StatusEffectInstance fatigue = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 0, false, false, true);
            player.addStatusEffect(slow);
            player.addStatusEffect(fatigue);
         }
         
         if(world.getServer().getTicks() % 20 == 0){
            addEnergy(stack, -1); // Recharge
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, PacketContext context){
         if(!ArcanaItemUtils.isArcane(itemStack)) return vanillaItem;
         NbtCompound contents = getCompoundProperty(itemStack,CONTENTS_TAG);
         
         return !contents.isEmpty() ? Items.SPRUCE_CHEST_BOAT : vanillaItem;
      }
   }
}

