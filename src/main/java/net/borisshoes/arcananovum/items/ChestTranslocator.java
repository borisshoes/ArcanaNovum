package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItemContainer;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.SPRUCE_BOAT;
      item = new ChestTranslocatorItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.USE_ENDER_CHEST,ResearchTasks.EFFECT_STRENGTH};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,CONTENTS_TAG,new CompoundTag());
      putProperty(stack,STATE_TAG,new CompoundTag());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Moving items").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" from one ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("chest ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("another ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("is a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("hassle").withStyle(ChatFormatting.RED))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Chests").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" are ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("heavy").withStyle(ChatFormatting.RED))
            .append(Component.literal(" and carrying them ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("slows ").withStyle(ChatFormatting.RED))
            .append(Component.literal("you down ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("significantly").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("chest ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("store ").withStyle(ChatFormatting.RED))
            .append(Component.literal("it in the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Translocator").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("place ").withStyle(ChatFormatting.RED))
            .append(Component.literal("a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("stored ").withStyle(ChatFormatting.RED))
            .append(Component.literal("chest").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" down.").withStyle(ChatFormatting.GRAY)));
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
      CompoundTag contents = getCompoundProperty(item,CONTENTS_TAG);
      SimpleContainer inv = new SimpleContainer(27);
      for(int i = 0; i < inv.getContainerSize(); i++){
         inv.setItem(i, ItemStack.EMPTY.copy());
      }
      
      if(!contents.isEmpty()){
         ListTag items = contents.getListOrEmpty("Items");
         
         for(int i = 0; i < items.size(); i++){
            CompoundTag stack = items.getCompoundOrEmpty(i);
            ItemStack itemStack = ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, BorisLib.SERVER.registryAccess()),stack).result().orElse(ItemStack.EMPTY);
            inv.setItem(stack.getByteOr("Slot", (byte) 0), itemStack);
         }
      }
      
      return new ArcanaItemContainer(inv, 27,25, "CT", "Chest Translocator", 0.5);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      CompoundTag contents = getCompoundProperty(stack,CONTENTS_TAG);
      CompoundTag state = getCompoundProperty(stack,STATE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,CONTENTS_TAG,contents);
      putProperty(newStack,STATE_TAG,state);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("       Chest\n   Translocator").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nChests are great for storage and organization. However, whenever I try to move them, their contents spill all over the place.\nMaybe I can do something about that").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("       Chest\n   Translocator").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nusing an augmented Ender Chest and some additional strength.\n\nUsing the Translocator on a Chest, Trapped Chest, or Barrel will pick it up at the cost of a significant loss in dexterity.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("       Chest\n   Translocator").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nUsing the Translocator again places the container down, contents intact.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ChestTranslocatorItem extends ArcanaPolymerItem {
      public ChestTranslocatorItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         CompoundTag contents = getCompoundProperty(itemStack,CONTENTS_TAG);
         CompoundTag state = getCompoundProperty(itemStack,STATE_TAG);
         if(!contents.isEmpty()){
            BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK,state);
            if(blockState.is(Blocks.CHEST) || blockState.is(Blocks.TRAPPED_CHEST)){
               stringList.add("chest");
            }else if(blockState.is(Blocks.BARREL)){
               stringList.add("barrel");
            }
         }
         
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         ItemStack stack = context.getItemInHand();
         Player playerEntity = context.getPlayer();
         if(!ArcanaItemUtils.isArcane(stack) || !(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         
         Level world = context.getLevel();
         BlockPos blockPos = context.getClickedPos();
         CompoundTag contents = getCompoundProperty(stack,CONTENTS_TAG);
         CompoundTag stateTag = getCompoundProperty(stack,STATE_TAG);
         int cooldown = getEnergy(stack);
         BlockState state = world.getBlockState(blockPos);
         
         if(contents.isEmpty()){
            if(state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL)){
               if(cooldown == 0){
                  BlockEntity be = world.getBlockEntity(blockPos);
                  if(be == null) return InteractionResult.PASS;
                  CompoundTag contentData = be.saveWithFullMetadata(BorisLib.SERVER.registryAccess());
                  putProperty(stack,CONTENTS_TAG,contentData);
                  putProperty(stack,STATE_TAG, NbtUtils.writeBlockState(state));
                  if(be instanceof Clearable clearable) clearable.clearContent();
                  world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                  SoundUtils.playSound(world,blockPos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1,1);
                  setEnergy(stack,getMaxEnergy(stack));
               }else{
                  player.displayClientMessage(Component.literal("Translocator Cooldown: "+cooldown+(cooldown != 1 ? " seconds" : " second")).withStyle(ChatFormatting.GOLD), true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
               }
            }else{
               return InteractionResult.PASS;
            }
         }else{
            Direction side = context.getClickedFace();
            BlockPos placePos = blockPos.offset(side.getUnitVec3i());
            if(world.getBlockState(placePos).isAir()){
               BlockState blockState = NbtUtils.readBlockState(world.holderLookup(Registries.BLOCK),stateTag);
               BlockPlaceContext ipc = new BlockPlaceContext(player,context.getHand(),context.getItemInHand(),new BlockHitResult(context.getClickLocation(),context.getClickedFace(),context.getClickedPos(),context.isInside()));
               if(blockState.is(Blocks.CHEST)){
                  blockState = Blocks.CHEST.getStateForPlacement(ipc);
               }else if(blockState.is(Blocks.TRAPPED_CHEST)){
                  blockState = Blocks.TRAPPED_CHEST.getStateForPlacement(ipc);
               }else if(blockState.is(Blocks.BARREL)){
                  blockState = Blocks.BARREL.getStateForPlacement(ipc);
               }
               world.setBlock(placePos,blockState, Block.UPDATE_ALL);
               BlockEntity blockEntity = BlockEntity.loadStatic(placePos,blockState,contents,world.registryAccess());
               if(blockEntity != null){
                  world.setBlockEntity(blockEntity);
                  if(blockEntity instanceof RandomizableContainerBlockEntity container){
                     int size = container.getContainerSize();
                     int filled = 0;
                     for(int i = 0; i < size; i++){
                        filled += container.getItem(i).isEmpty() ? 0 : 1;
                     }
                     if(filled == size){
                        ArcanaAchievements.progress(player,ArcanaAchievements.STORAGE_RELOCATION.id, 1);
                     }else if(filled == 0){
                        ArcanaAchievements.grant(player,ArcanaAchievements.PEAK_LAZINESS.id);
                     }
                  }
               }
               
               putProperty(stack,CONTENTS_TAG,new CompoundTag());
               putProperty(stack,STATE_TAG,new CompoundTag());
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CHEST_TRANSLOCATOR_USE)); // Add xp
               SoundUtils.playSound(world,placePos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1,1);
            }else{
               player.displayClientMessage(Component.literal("The chest cannot be placed here.").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         CompoundTag contents = getCompoundProperty(stack,CONTENTS_TAG);
         
         if(!contents.isEmpty()){
            MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS, 20, 2, false, false, true);
            MobEffectInstance fatigue = new MobEffectInstance(MobEffects.MINING_FATIGUE, 20, 0, false, false, true);
            player.addEffect(slow);
            player.addEffect(fatigue);
         }
         
         if(world.getServer().getTickCount() % 20 == 0){
            addEnergy(stack, -1); // Recharge
         }
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, PacketContext context){
         if(!ArcanaItemUtils.isArcane(itemStack)) return vanillaItem;
         CompoundTag contents = getCompoundProperty(itemStack,CONTENTS_TAG);
         
         return !contents.isEmpty() ? Items.SPRUCE_CHEST_BOAT : vanillaItem;
      }
   }
}

