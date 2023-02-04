package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.items.core.*;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RunicQuiver extends QuiverItem implements UsableItem, TickingItem{
   
   public static final int size = 9;
   private static final int[] refillReduction = {0,100,200,400,600,900};
   private static final double[] efficiencyChance = {0,.05,.1,.15,.2,.3};
   
   public RunicQuiver(){
      id = "runic_quiver";
      name = "Runic Quiver";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.ITEMS};
      color = Formatting.LIGHT_PURPLE;
      
      ItemStack item = new ItemStack(Items.LEATHER);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Quiver\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"runes \",\"color\":\"light_purple\"},{\"text\":\"engraved \",\"color\":\"dark_aqua\"},{\"text\":\"upon the \"},{\"text\":\"quiver \",\"color\":\"light_purple\"},{\"text\":\"hum in the presence of \"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Runic Arrows\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" placed within the \",\"color\":\"dark_purple\"},{\"text\":\"quiver \"},{\"text\":\"regenerate \",\"color\":\"dark_aqua\"},{\"text\":\"over \",\"color\":\"dark_purple\"},{\"text\":\"time\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Arrows \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"do not take \",\"color\":\"dark_purple\"},{\"text\":\"concentration \",\"color\":\"dark_aqua\"},{\"text\":\"when in the \",\"color\":\"dark_purple\"},{\"text\":\"quiver\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to put \",\"color\":\"dark_purple\"},{\"text\":\"Arrows \",\"color\":\"light_purple\"},{\"text\":\"in the \",\"color\":\"dark_purple\"},{\"text\":\"quiver\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Left Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" with a \",\"color\":\"dark_purple\"},{\"text\":\"Runic Bow\",\"color\":\"light_purple\"},{\"text\":\" to swap which \",\"color\":\"dark_purple\"},{\"text\":\"Runic Arrow\",\"color\":\"light_purple\"},{\"text\":\" will be shot.\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      NbtList storedArrows = new NbtList();
      magicTag.putInt("slot",0);
      magicTag.put("arrows",storedArrows);
      prefNBT = tag;
      
      item.setNbt(prefNBT);
      prefItem = item;
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
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      // Open GUI
      if(playerEntity instanceof ServerPlayerEntity player){
         ItemStack stack = playerEntity.getStackInHand(hand);
         QuiverGui gui = new QuiverGui(player,this,stack,true);
         gui.build();
         gui.open();
      }
      return false;
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item){
      if(world.getServer().getTicks() % getRefillMod(item) == 0) refillArrow(player, item);
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return false;
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
      return newMagicItem;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      ItemStack enchantedBook1 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook1,new EnchantmentLevelEntry(Enchantments.INFINITY,1));
      MagicItemIngredient b = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook1.getNbt());
      MagicItemIngredient c = new MagicItemIngredient(Items.LEATHER,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      GenericMagicIngredient h = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.OVERFLOWING_QUIVER,1);
   
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
      list.add("{\"text\":\"      Runic Quiver\\n\\nRarity: Legendary\\n\\nMy improvments upon the overflowing quiver have been completed and now the quiver is capable of sending some of my Arcana to Runic Arrows within. I even managed to make the quiver take a set amount of Arcana\"}");
      list.add("{\"text\":\"      Runic Quiver\\n\\nregardless of how demanding the Runic Arrows within are, so its best to load the quiver up all the way.\\n\\nThe quiver acts the same as its base counterpart just with this added expansion and a quiver restock time.\"}");
      return list;
   }
}
