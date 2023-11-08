package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OverflowingQuiver extends QuiverItem{
   
   private static final int[] refillReduction = {0,300,600,900,1200,1800};
   private static final double[] efficiencyChance = {0,.05,.1,.15,.2,.3};
   
   public OverflowingQuiver(){
      id = "overflowing_quiver";
      name = "Overflowing Quiver";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS};
      color = Formatting.DARK_AQUA;
      vanillaItem = Items.RABBIT_HIDE;
      item = new OverflowingQuiverItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Overflowing Quiver\",\"italic\":false,\"color\":\"dark_aqua\",\"bold\":true}]");
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
      loreList.add(NbtString.of("[{\"text\":\"One can never have enough \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"arrows\",\"color\":\"dark_aqua\"},{\"text\":\"...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Tipped Arrows\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" placed within the \",\"color\":\"aqua\"},{\"text\":\"quiver \"},{\"text\":\"restock \",\"color\":\"blue\"},{\"text\":\"over \",\"color\":\"aqua\"},{\"text\":\"time\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"blue\"},{\"text\":\" to put \",\"color\":\"aqua\"},{\"text\":\"arrows \",\"color\":\"dark_aqua\"},{\"text\":\"in the \",\"color\":\"aqua\"},{\"text\":\"quiver\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Left Click \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"with a \",\"color\":\"aqua\"},{\"text\":\"bow \"},{\"text\":\"to \",\"color\":\"aqua\"},{\"text\":\"swap \",\"color\":\"dark_aqua\"},{\"text\":\"which type of \",\"color\":\"aqua\"},{\"text\":\"arrow \",\"color\":\"dark_aqua\"},{\"text\":\"will be shot.\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   protected int getRefillMod(ItemStack item){ // Ticks between arrow refill, once per two and a half minutes
      int refillLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"abundant_ammo"));
      return 3000 - refillReduction[refillLvl];
   }
   
   @Override
   protected double getEfficiencyMod(ItemStack item){ // Ticks between arrow refill, once per two and a half minutes
      int effLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"overflowing_bottomless"));
      return efficiencyChance[effLvl];
   }
   
   private OverflowingQuiver getOuter(){
      return this;
   }
   
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.NETHER_STAR,2,null);
      ItemStack enchantedBook1 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook1,new EnchantmentLevelEntry(Enchantments.INFINITY,1));
      MagicItemIngredient b = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook1.getNbt());
      MagicItemIngredient c = new MagicItemIngredient(Items.RABBIT_HIDE,32,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHERITE_INGOT,2,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.ARROW,64,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
   
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
      list.add("{\"text\":\"  Overflowing Quiver\\n\\nRarity: Exotic\\n\\nMore experienced archers keep a variety of arrows on hand, however it is difficult to switch between them in the heat of a fight. This quiver has slots that not only save space, and keep arrows\"}");
      list.add("{\"text\":\"  Overflowing Quiver\\n\\norganized, but it also contains a mechanism to help guide the archer's hand to the desired arrow type.\\n\\nLeft clicking with any bow cycles the preferred arrows.\\n\\nI have also managed to unlock greater\"}");
      list.add("{\"text\":\"  Overflowing Quiver\\n\\npotential from the Infinity enchantment and imbued it within the quiver.\\n\\nThe quiver now slowly regenerates all arrows placed inside of it.\\n\\nIt is worth noting that this quiver is not\"}");
      list.add("{\"text\":\"  Overflowing Quiver\\n\\nsturdy enough to channel Arcana to arrows placed inside,  restricting Runic Arrows from being contained within.\\n\\nI am looking into possible improvments to this design to accommodate more powerful projectiles.\"}");
      return list;
   }
   
   public class OverflowingQuiverItem extends MagicPolymerItem {
      public OverflowingQuiverItem(Settings settings){
         super(getThis(),settings);
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
            QuiverGui gui = new QuiverGui(player, getOuter(), stack,false);
            gui.build();
            gui.open();
         }
         return TypedActionResult.success(playerEntity.getStackInHand(hand));
      }
   }
}
