package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.greaves.GreavesOfGaialtusGui;
import net.borisshoes.arcananovum.gui.greaves.GreavesSlot;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
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
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.DIAMOND_LEGGINGS;
      item = new GreavesOfGaialtusItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.AQUA,Formatting.BOLD);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_GREAVES_OF_GAIALTUS};
      attributions = new Pair[]{new Pair<>(Text.translatable("credits_and_attribution.arcananovum.texture_by"),Text.literal("tcmEcho")), new Pair<>(Text.translatable("credits_and_attribution.arcananovum.model_by"),Text.literal("tcmEcho"))};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
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
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A manifestation of ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("nature's ").formatted(Formatting.GREEN))
            .append(Text.literal("nurturing ").formatted(Formatting.BLUE))
            .append(Text.literal("and ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("protective ").formatted(Formatting.BLUE))
            .append(Text.literal("embrace.").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Wearing them makes your mind ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("surge ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("with ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("creativity").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("pants ").formatted(Formatting.AQUA))
            .append(Text.literal("are ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("unbreakable").formatted(Formatting.BLUE))
            .append(Text.literal(" and act as ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("unenchanted netherite").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("greaves'").formatted(Formatting.AQUA))
            .append(Text.literal(" pockets are deep as the ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("ocean ").formatted(Formatting.BLUE))
            .append(Text.literal("and can hold many ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("blocks").formatted(Formatting.GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("greaves ").formatted(Formatting.AQUA))
            .append(Text.literal("will ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("refill ").formatted(Formatting.BLUE))
            .append(Text.literal("your inventory with ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("blocks").formatted(Formatting.GREEN))
            .append(Text.literal(" as you use them.").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.GREEN))
            .append(Text.literal(" to access the ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("greaves'").formatted(Formatting.AQUA))
            .append(Text.literal(" pockets.").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click in offhand").formatted(Formatting.GREEN))
            .append(Text.literal(" to toggle the ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("greaves'").formatted(Formatting.AQUA))
            .append(Text.literal(" auto-refill.").formatted(Formatting.DARK_GREEN)));
      
      if(itemStack != null){
         List<Pair<Item,Integer>> cargo = getCargoList(itemStack);
         
         if(cargo.isEmpty()){
            lore.add(Text.literal(""));
            lore.add(Text.literal("")
                  .append(Text.literal("Contents: ").formatted(Formatting.DARK_GREEN))
                  .append(Text.literal("Empty").formatted(Formatting.AQUA)));
         }else{
            lore.add(Text.literal(""));
            lore.add(Text.literal("").append(Text.literal("Contents: ").formatted(Formatting.DARK_GREEN)));
            int leftOverCount = 0;
            for(int i = 0; i < cargo.size(); i++){
               int count = cargo.get(i).getRight();
               if(i >= 10){
                  leftOverCount += count;
                  continue;
               }
               
               Item item = cargo.get(i).getLeft();
               int stacks = count / item.getMaxCount();
               int leftover = count % item.getMaxCount();
               
               if(count > item.getMaxCount()){
                  lore.add(Text.literal("")
                        .append(Text.literal(" - ").formatted(Formatting.DARK_GREEN))
                        .append(Text.literal(count+"").formatted(Formatting.GREEN))
                        .append(Text.literal(" (").formatted(Formatting.DARK_GREEN))
                        .append(Text.literal(stacks+" Stacks + "+leftover).formatted(Formatting.GREEN))
                        .append(Text.literal(") of ").formatted(Formatting.DARK_GREEN))
                        .append(item.getName().copy().formatted(Formatting.AQUA)));
               }else{
                  lore.add(Text.literal("")
                        .append(Text.literal(" - ").formatted(Formatting.DARK_GREEN))
                        .append(Text.literal(count+"").formatted(Formatting.GREEN))
                        .append(Text.literal(" of ").formatted(Formatting.DARK_GREEN))
                        .append(item.getName().copy().formatted(Formatting.AQUA)));
               }
            }
            
            if(leftOverCount > 0){
               lore.add(Text.literal("")
                     .append(Text.literal(" - ").formatted(Formatting.DARK_GREEN))
                     .append(Text.literal(leftOverCount+"").formatted(Formatting.AQUA))
                     .append(Text.literal(" more items of ").formatted(Formatting.GREEN))
                     .append(Text.literal((cargo.size()-10)+"").formatted(Formatting.AQUA))
                     .append(Text.literal(" types...").formatted(Formatting.GREEN)));
            }
         }
      }
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Greaves Of\n      Gaialtus").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nEquayus once spoke to me of Gaialtus, the long lost progenitor of the Overworld. They mentioned only good things, a seemingly rare attitude towards the so-called").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Greaves Of\n      Gaialtus").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\n‘progenitors’.\n\nEquayus said that they met once, and Gaialtus was kind enough to offer them something incredibly important; a wonderfully cryptic tale.\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Greaves Of\n      Gaialtus").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\nThese Greaves contain a condensed fraction of their essence, that of creativity and protection. \nThey are similar to Netherite, but are incredibly cozy, and have pockets that ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Greaves Of\n      Gaialtus").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\ncould fit mountains inside.\n\nSneak Using the Greaves access their pockets, where copious amounts of building blocks can be stored. \n\n ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Greaves Of\n      Gaialtus").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\nSneak Using in my off-hand toggles the Greaves ability to auto-refill my hands with the stored blocks.\n\nThe pockets can also be accessed similar to a Bundle in my inventory.").formatted(Formatting.BLACK)));
      
      return list;
   }
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(ArcanaItemUtils.identifyItem(stack) instanceof GreavesOfGaialtus){
         if(augment == ArcanaAugments.NATURES_EMBRACE && level >= 1){
            EnhancedStatUtils.enhanceItem(stack,1);
         }else if(augment == ArcanaAugments.CREATORS_TOUCH && level >= 1){
            AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
            List<AttributeModifiersComponent.Entry> attributeList = new ArrayList<>();
            
            // Don't duplicate attributes
            for(AttributeModifiersComponent.Entry entry : modifiers.modifiers()){
               if(!entry.matches(EntityAttributes.BLOCK_INTERACTION_RANGE,Identifier.of(MOD_ID,this.id))){
                  attributeList.add(entry);
               }
            }
            
            attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.BLOCK_INTERACTION_RANGE,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,this.id),1.5,EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.LEGS));
            AttributeModifiersComponent newComponent = new AttributeModifiersComponent(attributeList);
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS,newComponent);
         }
      }
      return stack;
   }
   
   public List<Pair<Item,Integer>> getCargoList(ItemStack greaves){
      List<Pair<Item,Integer>> list = new ArrayList<>();
      if(!(ArcanaItemUtils.identifyItem(greaves) instanceof GreavesOfGaialtus)) return list;
      ContainerComponent containerItems = greaves.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
      containerItems.stream().forEach(stack -> {
         if(stack.isEmpty()) return;
         Item item = stack.getItem();
         boolean found = false;
         for(Pair<Item, Integer> pair : list){
            if(pair.getLeft() == item){
               pair.setRight(pair.getRight() + stack.getCount());
               found = true;
               break;
            }
         }
         if(!found){
            list.add(new Pair<>(item,stack.getCount()));
         }
      });
      list.sort((pair1, pair2) -> pair2.getRight().compareTo(pair1.getRight()));
      return list;
   }
   
   public ItemStack getStackOf(ItemStack greaves, ItemStack refillStack){
      if(!(ArcanaItemUtils.identifyItem(greaves) instanceof GreavesOfGaialtus)) return ItemStack.EMPTY;
      List<ItemStack> stacks = MiscUtils.getMatchingItemsFromContainerComp(greaves,refillStack.getItem());
      ItemStack returnStack = ItemStack.EMPTY;
      for(ItemStack stack : stacks){
         if(ItemStack.areItemsAndComponentsEqual(refillStack,stack)){
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
               .armor(ArmorMaterials.NETHERITE, EquipmentType.LEGGINGS)
               .component(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT)
         );
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         EquippableComponent equippableComponent = baseStack.get(DataComponentTypes.EQUIPPABLE);
         EquippableComponent newComp = EquippableComponent.builder(equippableComponent.slot()).equipSound(equippableComponent.equipSound()).model(RegistryKey.of(EQUIPMENT_ASSET_REGISTRY_KEY, Identifier.of(MOD_ID,ID))).build();
         baseStack.set(DataComponentTypes.EQUIPPABLE,newComp);
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         // Open GUI
         if(playerEntity instanceof ServerPlayerEntity player && player.isSneaking()){
            ItemStack stack = playerEntity.getStackInHand(hand);
            if(hand == Hand.MAIN_HAND){
               GreavesOfGaialtusGui gui = new GreavesOfGaialtusGui(player, GreavesOfGaialtus.this, stack, GREAVES_SLOT_COUNT[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PLANETARY_POCKETS))]);
               gui.build();
               gui.open();
            }else if(hand == Hand.OFF_HAND){
               boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
               putProperty(stack,ACTIVE_TAG,active);
               if(active){
                  player.sendMessage(Text.literal("Your pockets zip open").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5f,1.2f);
               }else{
                  player.sendMessage(Text.literal("Your pockets zip closed").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5f,0.7f);
               }
            }
            PlayerInventory inv = player.getInventory();
            player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), hand == Hand.MAIN_HAND ? 36 + inv.getSelectedSlot() : 45, stack));
            player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 7, player.getEquippedStack(EquipmentSlot.LEGS)));
            return ActionResult.SUCCESS_SERVER;
         }
         return super.use(world,playerEntity,hand);
      }
      
      @Override
      public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity playerEntity, StackReference cursorStackReference) {
         if(playerEntity.getWorld().isClient || !(playerEntity instanceof ServerPlayerEntity player)) return false;
         if (clickType == ClickType.LEFT && otherStack.isEmpty()) {
            return false;
         } else {
            ContainerComponent beltItems = stack.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
            List<ItemStack> beltList = beltItems.stream().toList();
            
            if(clickType == ClickType.LEFT && !otherStack.isEmpty()){ // Try insert
               if(!GreavesSlot.isValidItem(otherStack)){
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BUNDLE_INSERT_FAIL,1f,1f);
               }else{
                  int size = GREAVES_SLOT_COUNT[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PLANETARY_POCKETS))];
                  int count = otherStack.getCount();
                  Pair<ContainerComponent,ItemStack> addPair = MiscUtils.tryAddStackToContainerComp(beltItems,size,otherStack);
                  if(count == addPair.getRight().getCount()){
                     SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUNDLE_INSERT_FAIL,1f,1f);
                  }else{
                     SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUNDLE_INSERT,0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
                     stack.set(DataComponentTypes.CONTAINER,addPair.getLeft());
                  }
               }
               buildItemLore(stack,ArcanaNovum.SERVER);
               return true;
            }else if(clickType == ClickType.RIGHT && otherStack.isEmpty()){ // Try remove
               boolean found = false;
               for(ItemStack itemStack : beltList.reversed()){
                  if(!itemStack.isEmpty()){
                     cursorStackReference.set(itemStack.copyAndEmpty());
                     SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUNDLE_REMOVE_ONE,0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
                     found = true;
                     break;
                  }
               }
               
               if(found){
                  stack.set(DataComponentTypes.CONTAINER,ContainerComponent.fromStacks(beltList));
                  buildItemLore(stack,ArcanaNovum.SERVER);
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