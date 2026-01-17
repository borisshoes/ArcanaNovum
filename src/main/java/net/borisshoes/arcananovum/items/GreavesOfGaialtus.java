package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.greaves.GreavesOfGaialtusGui;
import net.borisshoes.arcananovum.gui.greaves.GreavesSlot;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.EQUIPMENT_ASSET_REGISTRY_KEY;

public class GreavesOfGaialtus extends ArcanaItem {
   public static final String ID = "greaves_of_gaialtus";
   
   public GreavesOfGaialtus(){
      id = ID;
      name = "Greaves Of Gaialtus";
      rarity = ArcanaRarity.DIVINE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.DIAMOND_LEGGINGS;
      item = new GreavesOfGaialtusItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_GREAVES_OF_GAIALTUS};
      attributions = new Tuple[]{new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.texture_by"), Component.literal("tcmEcho")), new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.model_by"), Component.literal("tcmEcho"))};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,ACTIVE_TAG,true);
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ACTIVE_TAG,active);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A manifestation of ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("nature's ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("nurturing ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("and ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("protective ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("embrace.").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Wearing them makes your mind ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("surge ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("with ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("creativity").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("pants ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("are ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("unbreakable").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" and act as ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("unenchanted netherite").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("greaves'").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" pockets are deep as the ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("ocean ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("and can hold many ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("blocks").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("greaves ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("will ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("refill ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("your inventory with ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("blocks").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" as you use them.").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" to access the ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("greaves'").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" pockets.").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click in offhand").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" to toggle the ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("greaves'").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" auto-refill.").withStyle(ChatFormatting.DARK_GREEN)));
      
      if(itemStack != null){
         List<Tuple<Item,Integer>> cargo = getCargoList(itemStack);
         
         if(cargo.isEmpty()){
            lore.add(Component.literal(""));
            lore.add(Component.literal("")
                  .append(Component.literal("Contents: ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal("Empty").withStyle(ChatFormatting.AQUA)));
         }else{
            lore.add(Component.literal(""));
            lore.add(Component.literal("").append(Component.literal("Contents: ").withStyle(ChatFormatting.DARK_GREEN)));
            int leftOverCount = 0;
            for(int i = 0; i < cargo.size(); i++){
               int count = cargo.get(i).getB();
               if(i >= 10){
                  leftOverCount += count;
                  continue;
               }
               
               Item item = cargo.get(i).getA();
               int stacks = count / item.getDefaultMaxStackSize();
               int leftover = count % item.getDefaultMaxStackSize();
               
               if(count > item.getDefaultMaxStackSize()){
                  lore.add(Component.literal("")
                        .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal(count+"").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" (").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal(stacks+" Stacks + "+leftover).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(") of ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(item.getName().copy().withStyle(ChatFormatting.AQUA)));
               }else{
                  lore.add(Component.literal("")
                        .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal(count+"").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" of ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(item.getName().copy().withStyle(ChatFormatting.AQUA)));
               }
            }
            
            if(leftOverCount > 0){
               lore.add(Component.literal("")
                     .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GREEN))
                     .append(Component.literal(leftOverCount+"").withStyle(ChatFormatting.AQUA))
                     .append(Component.literal(" more items of ").withStyle(ChatFormatting.GREEN))
                     .append(Component.literal((cargo.size()-10)+"").withStyle(ChatFormatting.AQUA))
                     .append(Component.literal(" types...").withStyle(ChatFormatting.GREEN)));
            }
         }
      }
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Greaves Of\n      Gaialtus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nEquayus once spoke to me of Gaialtus, the long lost progenitor of the Overworld. They mentioned only good things, a seemingly rare attitude towards the so-called").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Greaves Of\n      Gaialtus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\n‘progenitors’.\n\nEquayus said that they met once, and Gaialtus was kind enough to offer them something incredibly important; a wonderfully cryptic tale.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Greaves Of\n      Gaialtus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nThese Greaves contain a condensed fraction of their essence, that of creativity and protection. \nThey are similar to Netherite, but are incredibly cozy, and have pockets that ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Greaves Of\n      Gaialtus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\ncould fit mountains inside.\n\nSneak Using the Greaves access their pockets, where copious amounts of building blocks can be stored. \n\n ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Greaves Of\n      Gaialtus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nSneak Using in my off-hand toggles the Greaves ability to auto-refill my hands with the stored blocks.\n\nThe pockets can also be accessed similar to a Bundle in my inventory.").withStyle(ChatFormatting.BLACK)));
      
      return list;
   }
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(ArcanaItemUtils.identifyItem(stack) instanceof GreavesOfGaialtus){
         if(augment == ArcanaAugments.NATURES_EMBRACE && level >= 1){
            EnhancedStatUtils.enhanceItem(stack,1);
         }else if(augment == ArcanaAugments.CREATORS_TOUCH && level >= 1){
            ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            List<ItemAttributeModifiers.Entry> attributeList = new ArrayList<>();
            
            // Don't duplicate attributes
            for(ItemAttributeModifiers.Entry entry : modifiers.modifiers()){
               if(!entry.matches(Attributes.BLOCK_INTERACTION_RANGE, Identifier.fromNamespaceAndPath(MOD_ID,this.id))){
                  attributeList.add(entry);
               }
            }
            
            attributeList.add(new ItemAttributeModifiers.Entry(Attributes.BLOCK_INTERACTION_RANGE,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,this.id),1.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.LEGS));
            ItemAttributeModifiers newComponent = new ItemAttributeModifiers(attributeList);
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS,newComponent);
         }
      }
      return stack;
   }
   
   public List<Tuple<Item,Integer>> getCargoList(ItemStack greaves){
      List<Tuple<Item,Integer>> list = new ArrayList<>();
      if(!(ArcanaItemUtils.identifyItem(greaves) instanceof GreavesOfGaialtus)) return list;
      ItemContainerContents containerItems = greaves.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
      containerItems.stream().forEach(stack -> {
         if(stack.isEmpty()) return;
         Item item = stack.getItem();
         boolean found = false;
         for(Tuple<Item, Integer> pair : list){
            if(pair.getA() == item){
               pair.setB(pair.getB() + stack.getCount());
               found = true;
               break;
            }
         }
         if(!found){
            list.add(new Tuple<>(item,stack.getCount()));
         }
      });
      list.sort((pair1, pair2) -> pair2.getB().compareTo(pair1.getB()));
      return list;
   }
   
   public ItemStack getStackOf(ItemStack greaves, ItemStack refillStack){
      if(!(ArcanaItemUtils.identifyItem(greaves) instanceof GreavesOfGaialtus)) return ItemStack.EMPTY;
      List<ItemStack> stacks = MinecraftUtils.getMatchingItemsFromContainerComp(greaves,refillStack.getItem());
      ItemStack returnStack = ItemStack.EMPTY;
      for(ItemStack stack : stacks){
         if(ItemStack.isSameItemSameComponents(refillStack,stack)){
            if(returnStack.isEmpty() || stack.getCount() < returnStack.getCount()){
               returnStack = stack;
            }
         }
      }
      return returnStack;
   }
   
   public class GreavesOfGaialtusItem extends ArcanaPolymerItem {
      public static final int[] GREAVES_SLOT_COUNT = new int[]{18,27,36,45,54};
      
      public GreavesOfGaialtusItem(){
         super(getThis(),getEquipmentArcanaItemComponents()
               .humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.LEGGINGS)
               .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
         );
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         Equippable equippableComponent = baseStack.get(DataComponents.EQUIPPABLE);
         Equippable newComp = Equippable.builder(equippableComponent.slot()).setEquipSound(equippableComponent.equipSound()).setAsset(ResourceKey.create(EQUIPMENT_ASSET_REGISTRY_KEY, Identifier.fromNamespaceAndPath(MOD_ID,ID))).build();
         baseStack.set(DataComponents.EQUIPPABLE,newComp);
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
         
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         // Open GUI
         if(playerEntity instanceof ServerPlayer player && player.isShiftKeyDown()){
            ItemStack stack = playerEntity.getItemInHand(hand);
            if(hand == InteractionHand.MAIN_HAND){
               GreavesOfGaialtusGui gui = new GreavesOfGaialtusGui(player, GreavesOfGaialtus.this, stack, GREAVES_SLOT_COUNT[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PLANETARY_POCKETS))]);
               gui.build();
               gui.open();
            }else if(hand == InteractionHand.OFF_HAND){
               boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
               putProperty(stack,ACTIVE_TAG,active);
               if(active){
                  player.displayClientMessage(Component.literal("Your pockets zip open").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ARMOR_EQUIP_LEATHER, 0.5f,1.2f);
               }else{
                  player.displayClientMessage(Component.literal("Your pockets zip closed").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ARMOR_EQUIP_LEATHER, 0.5f,0.7f);
               }
            }
            Inventory inv = player.getInventory();
            player.connection.send(new ClientboundContainerSetSlotPacket(player.inventoryMenu.containerId, player.inventoryMenu.incrementStateId(), hand == InteractionHand.MAIN_HAND ? 36 + inv.getSelectedSlot() : 45, stack));
            player.connection.send(new ClientboundContainerSetSlotPacket(player.inventoryMenu.containerId, player.inventoryMenu.incrementStateId(), 7, player.getItemBySlot(EquipmentSlot.LEGS)));
            return InteractionResult.SUCCESS_SERVER;
         }
         return super.use(world,playerEntity,hand);
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
               if(!GreavesSlot.isValidItem(otherStack)){
                  SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT_FAIL,1f,1f);
               }else{
                  int size = GREAVES_SLOT_COUNT[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PLANETARY_POCKETS))];
                  int count = otherStack.getCount();
                  Tuple<ItemContainerContents, ItemStack> addPair = MinecraftUtils.tryAddStackToContainerComp(beltItems,size,otherStack);
                  if(count == addPair.getB().getCount()){
                     SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT_FAIL,1f,1f);
                  }else{
                     SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT,0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                     stack.set(DataComponents.CONTAINER,addPair.getA());
                  }
               }
               buildItemLore(stack, BorisLib.SERVER);
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
                  buildItemLore(stack,BorisLib.SERVER);
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