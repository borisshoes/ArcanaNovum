package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaItemContainer;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.arcanistsbelt.ArcanistsBeltGui;
import net.borisshoes.arcananovum.gui.arcanistsbelt.ArcanistsBeltSlot;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.DataFixer;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
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
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.LEAD;
      item = new ArcanistsBeltItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD).withColor(ArcanaColors.BELT_COLOR);
      researchTasks = new RegistryKey[]{ResearchTasks.USE_ENDER_CHEST,ResearchTasks.CONCENTRATION_DAMAGE,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("For when you have too many ").formatted(Formatting.YELLOW))
            .append(Text.literal("tools ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("and ").formatted(Formatting.YELLOW))
            .append(Text.literal("Arcana Items ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("to carry...").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("Arcana Items").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" in the belt will maintain their ").formatted(Formatting.YELLOW))
            .append(Text.literal("passive effects").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("Arcana Items").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" consume a ").formatted(Formatting.YELLOW))
            .append(Text.literal("reduced").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" amount of ").formatted(Formatting.YELLOW))
            .append(Text.literal("concentration").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to access the ").formatted(Formatting.YELLOW))
            .append(Text.literal("Belt's").withColor(ArcanaColors.BELT_COLOR))
            .append(Text.literal(" slots").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
      
      if(itemStack != null){
         ContainerComponent beltItems = itemStack.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
         SimpleInventory inv = new SimpleInventory(9);
         List<ItemStack> streamList = beltItems.streamNonEmpty().toList();
         for(int i = 0; i < streamList.size(); i++){
            inv.setStack(i,streamList.get(i));
         }
         
         if(inv.isEmpty()){
            lore.add(Text.literal(""));
            lore.add(Text.literal("")
                  .append(Text.literal("Contents: ").withColor(ArcanaColors.BELT_COLOR))
                  .append(Text.literal("Empty").formatted(Formatting.YELLOW)));
         }else{
            lore.add(Text.literal(""));
            lore.add(Text.literal("").append(Text.literal("Contents:").withColor(ArcanaColors.BELT_COLOR)));
            for(int i = 0; i < inv.size(); i++){
               ItemStack stack = inv.getStack(i);
               if(stack.isEmpty()) continue;
               Style style = stack.getName().getStyle();
               boolean keepStyle = style.isBold() || style.isItalic() || style.isObfuscated() || style.isUnderlined() || style.isStrikethrough() || (style.getColor() != null && style.getColor().getRgb() != Formatting.WHITE.getColorValue());
               MutableText name = stack.getName().copy();
               if(!keepStyle) name = name.formatted(Formatting.YELLOW);
               
               if(stack.getCount() == 1 && stack.getMaxCount() == 1){
                  lore.add(Text.literal("")
                        .append(Text.literal(" - ").formatted(Formatting.DARK_AQUA))
                        .append(name));
               }else{
                  lore.add(Text.literal("")
                        .append(Text.literal(" - ").formatted(Formatting.YELLOW))
                        .append(Text.literal(stack.getCount()+"x ").formatted(Formatting.DARK_PURPLE))
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
         NbtList beltItems = getListProperty(stack,ITEMS_TAG).copy();
         stack.set(DataComponentTypes.CONTAINER, DataFixer.nbtListToComponent(beltItems,server));
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
      ContainerComponent beltItems = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
      SimpleInventory inv = new SimpleInventory(size);
      List<ItemStack> streamList = beltItems.streamNonEmpty().toList();
      for(int i = 0; i < streamList.size(); i++){
         inv.setStack(i,streamList.get(i));
      }
      return new ArcanaItemContainer(inv, size,10, "AB", "Arcanist's Belt", padding ? 0.25 : 0.5);
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHER_STAR,1);
      ArcanaIngredient b = new ArcanaIngredient(Items.LEATHER,12);
      ArcanaIngredient c = new ArcanaIngredient(Items.NETHERITE_SCRAP,2);
      ArcanaIngredient g = new ArcanaIngredient(Items.GOLD_INGOT,24);
      ArcanaIngredient h = new ArcanaIngredient(Items.CHEST,16);
      ArcanaIngredient m = new ArcanaIngredient(Items.ENDER_CHEST,8);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("  Arcanist's Belt").formatted(Formatting.BOLD).withColor(ArcanaColors.BELT_COLOR),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nWith my collection of Arcana items rapidly increasing, my inventory has become cluttered with trinkets. Some Arcana items are passive, so perhaps I can stuff them away in a mini Ender Chest of sorts and only ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Arcanist's Belt").formatted(Formatting.BOLD).withColor(ArcanaColors.BELT_COLOR),Text.literal("\nchannel enough Arcana to keep their passive abilities active.\n\nThe Belt should be able to accommodate unstackable items as well, as long as they aren’t too big. The Belt’s pouches can \n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Arcanist's Belt").formatted(Formatting.BOLD).withColor(ArcanaColors.BELT_COLOR),Text.literal("\nalso be accessed similar to a Bundle in my inventory.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class ArcanistsBeltItem extends ArcanaPolymerItem {
      public ArcanistsBeltItem(){
         super(getThis(),getArcanaItemComponents()
               .component(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT)
         );
      }
      
      public static final int[] BELT_SLOT_COUNT = new int[]{3,4,5,7,9};
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         ContainerComponent beltItems = stack.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
         for(ItemStack invStack : beltItems.iterateNonEmpty()){
            invStack.getItem().inventoryTick(invStack,world,entity,null);
         }
         buildItemLore(stack,ArcanaNovum.SERVER);
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         // Open GUI
         if(playerEntity instanceof ServerPlayerEntity player){
            ItemStack stack = playerEntity.getStackInHand(hand);
            ArcanistsBeltGui gui = new ArcanistsBeltGui(player, ArcanistsBelt.this, stack,BELT_SLOT_COUNT[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.POUCHES))]);
            gui.build();
            gui.open();
         }
         return ActionResult.SUCCESS_SERVER;
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
               if(!ArcanistsBeltSlot.isValidItem(otherStack)){
                  SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUNDLE_INSERT_FAIL,1f,1f);
               }else{
                  int size = BELT_SLOT_COUNT[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.POUCHES))];
                  int count = otherStack.getCount();
                  Pair<ContainerComponent,ItemStack> addPair = MinecraftUtils.tryAddStackToContainerComp(beltItems,size,otherStack);
                  if(count == addPair.getRight().getCount()){
                     SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUNDLE_INSERT_FAIL,1f,1f);
                  }else{
                     SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUNDLE_INSERT,0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
                     stack.set(DataComponentTypes.CONTAINER,addPair.getLeft());
                  }
               }
               buildItemLore(stack,player.getServer());
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
                  buildItemLore(stack,player.getServer());
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

