package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaItemContainer;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.arcanistsbelt.ArcanistsBeltGui;
import net.borisshoes.arcananovum.gui.arcanistsbelt.ArcanistsBeltSlot;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.DataFixer;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcanistsBelt extends ArcanaItem implements ArcanaItemContainer.ArcanaItemContainerHaver {
	public static final String ID = "arcanists_belt";
   
   public static final String ITEMS_TAG = "items";
   
   public ArcanistsBelt(){
      id = ID;
      name = "Arcanist's Belt";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.LEAD;
      item = new ArcanistsBeltItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BELT_COLOR);
      researchTasks = new ResourceKey[]{ResearchTasks.USE_ENDER_CHEST,ResearchTasks.CONCENTRATION_DAMAGE,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("For when you have too many ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("tools ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Arcana Items ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("to carry...").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" in the belt will maintain their ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("passive effects").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" consume a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("reduced").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" amount of ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("concentration").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to access the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Belt's").withColor(ArcanaColors.BELT_COLOR))
            .append(Component.literal(" slots").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      
      if(itemStack != null){
         ItemContainerContents beltItems = itemStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
         SimpleContainer inv = new SimpleContainer(9);
         List<ItemStack> streamList = beltItems.nonEmptyStream().toList();
         for(int i = 0; i < streamList.size(); i++){
            inv.setItem(i,streamList.get(i));
         }
         
         if(inv.isEmpty()){
            lore.add(Component.literal(""));
            lore.add(Component.literal("")
                  .append(Component.literal("Contents: ").withColor(ArcanaColors.BELT_COLOR))
                  .append(Component.literal("Empty").withStyle(ChatFormatting.YELLOW)));
         }else{
            lore.add(Component.literal(""));
            lore.add(Component.literal("").append(Component.literal("Contents:").withColor(ArcanaColors.BELT_COLOR)));
            for(int i = 0; i < inv.getContainerSize(); i++){
               ItemStack stack = inv.getItem(i);
               if(stack.isEmpty()) continue;
               Style style = stack.getHoverName().getStyle();
               boolean keepStyle = style.isBold() || style.isItalic() || style.isObfuscated() || style.isUnderlined() || style.isStrikethrough() || (style.getColor() != null && style.getColor().getValue() != ChatFormatting.WHITE.getColor());
               MutableComponent name = stack.getHoverName().copy();
               if(!keepStyle) name = name.withStyle(ChatFormatting.YELLOW);
               
               if(stack.getCount() == 1 && stack.getMaxStackSize() == 1){
                  lore.add(Component.literal("")
                        .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(name));
               }else{
                  lore.add(Component.literal("")
                        .append(Component.literal(" - ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(stack.getCount()+"x ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(name));
               }
            }
         }
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      if(getIntProperty(stack,VERSION_TAG) <= 12){ // Migrate from ITEMS_TAG to ContainerComponent
         ListTag beltItems = getListProperty(stack,ITEMS_TAG).copy();
         stack.set(DataComponents.CONTAINER, DataFixer.nbtListToComponent(beltItems,server));
         removeProperty(stack,ITEMS_TAG);
      }
      ItemStack newStack = super.updateItem(stack,server);
      return buildItemLore(newStack,server);
   }
   
   public static boolean checkBeltAndHasItem(ItemStack beltStack, Item searchItem){
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(beltStack);
      return (arcanaItem instanceof ArcanistsBelt && !MinecraftUtils.getMatchingItemsFromContainerComp(beltStack,searchItem).isEmpty());
   }
   
   @Override
   public ArcanaItemContainer getArcanaItemContainer(ItemStack item){
      int size = 9;
      boolean padding = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.MENTAL_PADDING.id) >= 1;
      ItemContainerContents beltItems = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
      SimpleContainer inv = new SimpleContainer(size);
      List<ItemStack> streamList = beltItems.nonEmptyStream().toList();
      for(int i = 0; i < streamList.size(); i++){
         inv.setItem(i,streamList.get(i));
      }
      return new ArcanaItemContainer(inv, size,10, "AB", "Arcanist's Belt", padding ? 0.25 : 0.5);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Arcanist's Belt").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BELT_COLOR), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nWith my collection of Arcana items rapidly increasing, my inventory has become cluttered with trinkets. Some Arcana items are passive, so perhaps I can stuff them away in a mini Ender Chest of sorts and only ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Arcanist's Belt").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BELT_COLOR), Component.literal("\nchannel enough Arcana to keep their passive abilities active.\n\nThe Belt should be able to accommodate unstackable items as well, as long as they aren’t too big. The Belt’s pouches can \n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Arcanist's Belt").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BELT_COLOR), Component.literal("\nalso be accessed similar to a Bundle in my inventory.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ArcanistsBeltItem extends ArcanaPolymerItem {
      public ArcanistsBeltItem(){
         super(getThis(),getArcanaItemComponents()
               .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
         );
      }
      
      public static final int[] BELT_SLOT_COUNT = new int[]{3,4,5,7,9};
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel && entity instanceof ServerPlayer player)) return;
         
         ItemContainerContents beltItems = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
         for(ItemStack invStack : beltItems.nonEmptyItems()){
            invStack.getItem().inventoryTick(invStack,world,entity,null);
         }
         buildItemLore(stack, BorisLib.SERVER);
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         // Open GUI
         if(playerEntity instanceof ServerPlayer player){
            ItemStack stack = playerEntity.getItemInHand(hand);
            ArcanistsBeltGui gui = new ArcanistsBeltGui(player, ArcanistsBelt.this, stack,BELT_SLOT_COUNT[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.POUCHES))]);
            gui.build();
            gui.open();
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player playerEntity, SlotAccess cursorStackReference) {
         if(playerEntity.level().isClientSide() || !(playerEntity instanceof ServerPlayer player)) return false;
         if (clickType == ClickAction.PRIMARY && otherStack.isEmpty()) {
            return false;
         } else {
            ItemContainerContents beltItems = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            List<ItemStack> beltList = beltItems.stream().toList();
            
            if(clickType == ClickAction.PRIMARY && !otherStack.isEmpty()){ // Try insert
               if(!ArcanistsBeltSlot.isValidItem(otherStack)){
                  SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT_FAIL,1f,1f);
               }else{
                  int size = BELT_SLOT_COUNT[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.POUCHES))];
                  int count = otherStack.getCount();
                  Tuple<ItemContainerContents, ItemStack> addPair = MinecraftUtils.tryAddStackToContainerComp(beltItems,size,otherStack);
                  if(count == addPair.getB().getCount()){
                     SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT_FAIL,1f,1f);
                  }else{
                     SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT,0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                     stack.set(DataComponents.CONTAINER,addPair.getA());
                  }
               }
               buildItemLore(stack,player.level().getServer());
               return true;
            }else if(clickType == ClickAction.SECONDARY && otherStack.isEmpty()){ // Try remove
               boolean found = false;
               for(ItemStack itemStack : beltList.reversed()){
                  if(!itemStack.isEmpty()){
                     cursorStackReference.set(itemStack.copyAndClear());
                     SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_REMOVE_ONE,0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                     found = true;
                     break;
                  }
               }
               
               if(found){
                  stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(beltList));
                  buildItemLore(stack,player.level().getServer());
                  return true;
               }else{
                  return false;
               }
            }else{ // Move item
               return false;
            }
         }
      }
   }
}

