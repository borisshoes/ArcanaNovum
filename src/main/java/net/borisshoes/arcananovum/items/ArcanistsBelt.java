package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.MagicItemContainer;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.gui.arcanistsbelt.ArcanistsBeltGui;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ArcanistsBelt extends MagicItem implements MagicItemContainer.MagicItemContainerHaver {
   
   public ArcanistsBelt(){
      id = "arcanists_belt";
      name = "Arcanist's Belt";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.LEAD;
      item = new ArcanistsBeltItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Arcanist's Belt\",\"italic\":false,\"color\":\"#996633\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"For when you have too many \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"tools \",\"color\":\"dark_aqua\"},{\"text\":\"and \"},{\"text\":\"Magic Items \",\"color\":\"dark_purple\"},{\"text\":\"to carry...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\" in the belt will maintain their \",\"color\":\"yellow\"},{\"text\":\"passive effects\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"yellow\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\" consume a \",\"color\":\"yellow\"},{\"text\":\"reduced\",\"color\":\"dark_aqua\"},{\"text\":\" amount of \",\"color\":\"yellow\"},{\"text\":\"concentration\"},{\"text\":\".\",\"color\":\"yellow\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to access the \",\"color\":\"yellow\"},{\"text\":\"Belt's\",\"color\":\"#996633\"},{\"text\":\" slots\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"yellow\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic\",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      NbtList storedItems = new NbtList();
      magicTag.put("items",storedItems);
      prefNBT = tag;
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtList itemsList = magicTag.getList("items", NbtElement.COMPOUND_TYPE).copy();
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").put("items",itemsList);
      stack.setNbt(newTag);
      return stack;
   }
   
   public static boolean checkBeltAndHasItem(ItemStack beltStack, Item searchItem){
      MagicItem magicItem = MagicItemUtils.identifyItem(beltStack);
      return (magicItem instanceof ArcanistsBelt belt && !belt.getMatchingItems(searchItem,beltStack).isEmpty());
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
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList items = magicNbt.getList("items", NbtElement.COMPOUND_TYPE);
      SimpleInventory inv = new SimpleInventory(9);
      
      for(int i = 0; i < items.size(); i++){ // De-serialize and Tick
         NbtCompound item = items.getCompound(i);
         int beltSlot = item.getByte("Slot");
         ItemStack itemStack = ItemStack.fromNbt(item);
         if(itemStack.getCount() > 0 && !itemStack.isEmpty())
            inv.setStack(beltSlot,itemStack);
      }
      return inv;
   }
   
   public void serialize(ItemStack stack, SimpleInventory inv){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList items = new NbtList();
      for(int i = 0; i < inv.size(); i++){ // Re-serialize
         ItemStack itemStack = inv.getStack(i);
         if(itemStack.isEmpty()) continue;
         NbtCompound item = itemStack.writeNbt(new NbtCompound());
         item.putByte("Slot", (byte) i);
         items.add(item);
      }
      magicNbt.put("items",items);
   }
   
   
   @Override
   public MagicItemContainer getMagicItemContainer(ItemStack item){
      int size = 9;
      boolean padding = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.MENTAL_PADDING.id) >= 1;
      return new MagicItemContainer(deserialize(item), size,1, "AB", "Arcanist's Belt", padding ? 0.25 : 0.5);
   }
   
   private ArcanistsBelt getOuter(){
      return this;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.NETHERITE_INGOT,2,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.LEATHER,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.GOLD_INGOT,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.CHEST,64,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.ENDER_CHEST,64,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Arcanist's Belt\\n\\nRarity: Exotic\\n\\nWith my collection of Magic Items rapidly increasing, my inventory has become cluttered with trinkets.\\nSome Magic Items only need their passive ability to be useful, so perhaps I can stuff them away in a pocket\"}");
      list.add("{\"text\":\"    Arcanist's Belt\\n\\nspace, like a mini ender chest and only channel enough Arcana to keep their passive abilities running.\\n\\nThe belt should be able to accommodate some plain old tools as well.\"}");
      return list;
   }
   
   public class ArcanistsBeltItem extends MagicPolymerItem {
      public ArcanistsBeltItem(Settings settings){
         super(getThis(),settings);
      }
      
      public static final int[] BELT_SLOT_COUNT = new int[]{3,4,5,7,9};
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         SimpleInventory inv = deserialize(stack);
         for(int i = 0; i < inv.size(); i++){
            ItemStack invStack = inv.getStack(i);
            invStack.getItem().inventoryTick(invStack,world,entity,-1,false);
         }
         serialize(stack,inv);
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         // Open GUI
         if(playerEntity instanceof ServerPlayerEntity player){
            ItemStack stack = playerEntity.getStackInHand(hand);
            ArcanistsBeltGui gui = new ArcanistsBeltGui(player, getOuter(), stack,BELT_SLOT_COUNT[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.POUCHES.id))]);
            gui.build();
            gui.open();
         }
         return TypedActionResult.success(playerEntity.getStackInHand(hand));
      }
   }
}
