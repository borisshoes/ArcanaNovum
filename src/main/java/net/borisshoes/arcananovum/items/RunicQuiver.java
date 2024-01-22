package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItemContainer;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RunicQuiver extends QuiverItem implements MagicItemContainer.MagicItemContainerHaver {
   
   public static final int size = 9;
   private static final int[] refillReduction = {0,100,200,400,600,900};
   private static final double[] efficiencyChance = {0,.05,.1,.15,.2,.3};
   private static final String TXT = "item/runic_quiver";
   
   public RunicQuiver(){
      id = "runic_quiver";
      name = "Runic Quiver";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.ITEMS};
      color = Formatting.LIGHT_PURPLE;
      vanillaItem = Items.LEATHER;
      item = new RunicQuiverItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Quiver\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);

      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      NbtList storedArrows = new NbtList();
      magicTag.put("arrows",storedArrows);
      prefNBT = tag;
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"runes \",\"color\":\"light_purple\"},{\"text\":\"engraved \",\"color\":\"dark_aqua\"},{\"text\":\"upon the \"},{\"text\":\"quiver \",\"color\":\"light_purple\"},{\"text\":\"hum in the presence of \"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Runic Arrows\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" placed within the \",\"color\":\"dark_purple\"},{\"text\":\"quiver \"},{\"text\":\"regenerate \",\"color\":\"dark_aqua\"},{\"text\":\"over \",\"color\":\"dark_purple\"},{\"text\":\"time\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Arrows \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"do not take \",\"color\":\"dark_purple\"},{\"text\":\"concentration \",\"color\":\"dark_aqua\"},{\"text\":\"when in the \",\"color\":\"dark_purple\"},{\"text\":\"quiver\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to put \",\"color\":\"dark_purple\"},{\"text\":\"Arrows \",\"color\":\"light_purple\"},{\"text\":\"in the \",\"color\":\"dark_purple\"},{\"text\":\"quiver\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Left Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" with a \",\"color\":\"dark_purple\"},{\"text\":\"Runic Bow\",\"color\":\"light_purple\"},{\"text\":\" to swap which \",\"color\":\"dark_purple\"},{\"text\":\"Runic Arrow\",\"color\":\"light_purple\"},{\"text\":\" will be shot.\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   protected int getRefillMod(ItemStack item){ // Ticks between arrow refill, once per minute
      int refillLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"quiver_duplication"));
      return 1200 - refillReduction[refillLvl];
   }
   
   @Override
   protected double getEfficiencyMod(ItemStack item){ // Ticks between arrow refill, once per two and a half minutes
      int effLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"runic_bottomless"));
      return efficiencyChance[effLvl];
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack quiverStack = inv.getStack(12); // Should be the old quiver
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = quiverStack.getNbt();
      if(nbt != null && nbt.getCompound("arcananovum").contains("arrows")){
         NbtList arrows = nbt.getCompound("arcananovum").getList("arrows", NbtElement.COMPOUND_TYPE);
         newMagicItem.getOrCreateNbt().getCompound("arcananovum").put("arrows",arrows);
      }
      int o = ArcanaAugments.getAugmentOnItem(quiverStack, ArcanaAugments.OVERFLOWING_BOTTOMLESS.id);
      int r = ArcanaAugments.getAugmentOnItem(quiverStack, ArcanaAugments.ABUNDANT_AMMO.id);
      if(o > 0){
         ArcanaAugments.applyAugment(newMagicItem, ArcanaAugments.RUNIC_BOTTOMLESS.id, o);
      }
      if(r > 0){
         ArcanaAugments.applyAugment(newMagicItem, ArcanaAugments.QUIVER_DUPLICATION.id, r);
      }
      
      return newMagicItem;
   }
   
   private RunicQuiver getOuter(){
      return this;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      ItemStack enchantedBook1 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook1,new EnchantmentLevelEntry(Enchantments.INFINITY,1));
      MagicItemIngredient b = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook1.getNbt());
      MagicItemIngredient c = new MagicItemIngredient(Items.LEATHER,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      GenericMagicIngredient h = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.OVERFLOWING_QUIVER,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery().withEnchanter());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Runic Quiver\\n\\nRarity: Legendary\\n\\nMy improvments upon the overflowing quiver have been completed and now the quiver is capable of sending some of my Arcana to Runic Arrows within. I even managed to make the quiver take a set amount of Arcana\"}");
      list.add("{\"text\":\"      Runic Quiver\\n\\nregardless of how demanding the Runic Arrows within are, so its best to load the quiver up all the way.\\n\\nThe quiver acts the same as its base counterpart just with this added expansion and a quicker restock time.\"}");
      return list;
   }
   
   @Override
   public MagicItemContainer getMagicItemContainer(ItemStack item){
      int size = 9;
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList arrows = magicNbt.getList("arrows", NbtElement.COMPOUND_TYPE);
      SimpleInventory inv = new SimpleInventory(size);
      
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound stack = arrows.getCompound(i);
         ItemStack itemStack = ItemStack.fromNbt(stack);
         inv.setStack(i,itemStack);
      }
      
      return new MagicItemContainer(inv, size,3, "RQ", "Runic Quiver", 0);
   }
   
   public class RunicQuiverItem extends MagicPolymerItem {
      public RunicQuiverItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         if(world.getServer().getTicks() % getRefillMod(stack) == 0) refillArrow(player, stack);
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         // Open GUI
         if(playerEntity instanceof ServerPlayerEntity player){
            ItemStack stack = playerEntity.getStackInHand(hand);
            QuiverGui gui = new QuiverGui(player, getOuter(), stack,true);
            gui.build();
            gui.open();
         }
         return TypedActionResult.success(playerEntity.getStackInHand(hand));
      }
   }
}
