package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaItemContainer;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.arcanistsbelt.ArcanistsBeltGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcanistsBelt extends ArcanaItem implements ArcanaItemContainer.ArcanaItemContainerHaver {
	public static final String ID = "arcanists_belt";
   
   public static final String ITEMS_TAG = "items";
   
   private static final String TXT = "item/arcanists_belt";
   
   public ArcanistsBelt(){
      id = ID;
      name = "Arcanist's Belt";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.LEAD;
      item = new ArcanistsBeltItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, TextUtils.withColor(Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD), ArcanaColors.BELT_COLOR))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.USE_ENDER_CHEST,ResearchTasks.CONCENTRATION_DAMAGE,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,ITEMS_TAG,new NbtList());
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
            .append(TextUtils.withColor(Text.literal("Belt's"),ArcanaColors.BELT_COLOR))
            .append(Text.literal(" slots").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
      
      if(itemStack != null){
         SimpleInventory inv = deserialize(itemStack);
         if(inv.isEmpty()){
            lore.add(Text.literal(""));
            lore.add(Text.literal("")
                  .append(TextUtils.withColor(Text.literal("Contents: "),ArcanaColors.BELT_COLOR))
                  .append(Text.literal("Empty").formatted(Formatting.YELLOW)));
         }else{
            lore.add(Text.literal(""));
            lore.add(Text.literal("").append(TextUtils.withColor(Text.literal("Contents:"),ArcanaColors.BELT_COLOR)));
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
      NbtList itemsList = getListProperty(stack,ITEMS_TAG,NbtElement.COMPOUND_TYPE);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ITEMS_TAG,itemsList);
      return buildItemLore(newStack,server);
   }
   
   public static boolean checkBeltAndHasItem(ItemStack beltStack, Item searchItem){
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(beltStack);
      return (arcanaItem instanceof ArcanistsBelt belt && !belt.getMatchingItems(searchItem,beltStack).isEmpty());
   }
   
   public ArrayList<ItemStack> getMatchingItemsWithAugment(Item item, ItemStack belt, ArcanaAugment augment, int minLevel){
      ArrayList<ItemStack> items = getMatchingItems(item,belt);
      ArrayList<ItemStack> filtered = new ArrayList<>();
      for(ItemStack itemStack : items){
         if(ArcanaAugments.getAugmentOnItem(itemStack, augment.id) >= minLevel){
            filtered.add(itemStack);
         }
      }
      return filtered;
   }
   
   public ArrayList<ItemStack> getMatchingItems(Item item, ItemStack belt){
      ArrayList<ItemStack> items = new ArrayList<>();
      SimpleInventory inv = deserialize(belt);
      for(int i = 0; i < inv.size(); i++){
         ItemStack itemStack = inv.getStack(i);
         if(itemStack.isOf(item)){
            items.add(itemStack);
         }
      }
      return items;
   }
   
   public SimpleInventory deserialize(ItemStack stack){
      NbtList items = getListProperty(stack,ITEMS_TAG,NbtElement.COMPOUND_TYPE);
      SimpleInventory inv = new SimpleInventory(9);
      
      for(int i = 0; i < items.size(); i++){ // De-serialize and Tick
         NbtCompound item = items.getCompound(i);
         int beltSlot = item.getByte("Slot");
         Optional<ItemStack> optional = ItemStack.fromNbt(ArcanaNovum.SERVER.getRegistryManager(),item);
         ItemStack itemStack = optional.orElse(ItemStack.EMPTY);
         if( itemStack.getCount() > 0 && !itemStack.isEmpty())
            inv.setStack(beltSlot,itemStack);
      }
      return inv;
   }
   
   public void serialize(ItemStack stack, SimpleInventory inv){
      NbtList items = new NbtList();
      for(int i = 0; i < inv.size(); i++){ // Re-serialize
         ItemStack itemStack = inv.getStack(i);
         if(itemStack.isEmpty()) continue;
         NbtCompound item = (NbtCompound) itemStack.toNbtAllowEmpty(ArcanaNovum.SERVER.getRegistryManager());
         item.putByte("Slot", (byte) i);
         items.add(item);
      }
      putProperty(stack,ITEMS_TAG,items);
      buildItemLore(stack,ArcanaNovum.SERVER);
   }
   
   
   @Override
   public ArcanaItemContainer getArcanaItemContainer(ItemStack item){
      int size = 9;
      boolean padding = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.MENTAL_PADDING.id) >= 1;
      return new ArcanaItemContainer(deserialize(item), size,1, "AB", "Arcanist's Belt", padding ? 0.25 : 0.5);
   }
   
   private ArcanistsBelt getOuter(){
      return this;
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
      list.add(List.of(Text.literal("    Arcanist's Belt\n\nRarity: Exotic\n\nWith my collection of Arcana Items rapidly increasing, my inventory has become cluttered with trinkets.\nSome Arcana Items only need their passive ability to be useful, so perhaps I can stuff them away in a pocket").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Arcanist's Belt\n\nspace, like a mini ender chest and only channel enough Arcana to keep their passive abilities running.\n\nThe belt should be able to accommodate some plain old tools as well.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class ArcanistsBeltItem extends ArcanaPolymerItem {
      public ArcanistsBeltItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      public static final int[] BELT_SLOT_COUNT = new int[]{3,4,5,7,9};
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.getModelData(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         SimpleInventory inv = deserialize(stack);
         for(int i = 0; i < inv.size(); i++){
            ItemStack invStack = inv.getStack(i);
            invStack.getItem().inventoryTick(invStack,world,entity,-1,false);
         }
         serialize(stack,inv);
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand) {
         // Open GUI
         if(playerEntity instanceof ServerPlayerEntity player){
            ItemStack stack = playerEntity.getStackInHand(hand);
            ArcanistsBeltGui gui = new ArcanistsBeltGui(player, getOuter(), stack,BELT_SLOT_COUNT[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.POUCHES.id))]);
            gui.build();
            gui.open();
         }
         return ActionResult.SUCCESS;
      }
   }
}

