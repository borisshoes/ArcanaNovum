package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class OverflowingQuiver extends QuiverItem{
	public static final String ID = "overflowing_quiver";
   
   private static final int[] refillReduction = {0,300,600,900,1200,1800};
   private static final double[] efficiencyChance = {0,.05,.1,.15,.2,.3};
   private static final String TXT = "item/overflowing_quiver";
   private static final Item textureItem = Items.ARROW;
   
   public OverflowingQuiver(){
      id = ID;
      name = "Overflowing Quiver";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ITEMS};
      color = Formatting.DARK_AQUA;
      vanillaItem = Items.RABBIT_HIDE;
      itemVersion = 1;
      item = new OverflowingQuiverItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      models.add(new Pair<>(textureItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_SHOOT_ARROW,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.OBTAIN_TIPPED_ARROW,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("One can never have enough ").formatted(Formatting.AQUA))
            .append(Text.literal("arrows").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("...").formatted(Formatting.AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Tipped Arrows").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" placed within the ").formatted(Formatting.AQUA))
            .append(Text.literal("quiver ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("restock ").formatted(Formatting.BLUE))
            .append(Text.literal("over ").formatted(Formatting.AQUA))
            .append(Text.literal("time").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.BLUE))
            .append(Text.literal(" to put ").formatted(Formatting.AQUA))
            .append(Text.literal("arrows ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("in the ").formatted(Formatting.AQUA))
            .append(Text.literal("quiver").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Left Click ").formatted(Formatting.BLUE))
            .append(Text.literal("with a ").formatted(Formatting.AQUA))
            .append(Text.literal("bow ").formatted(Formatting.BLUE))
            .append(Text.literal("to ").formatted(Formatting.AQUA))
            .append(Text.literal("swap ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("which type of ").formatted(Formatting.AQUA))
            .append(Text.literal("arrow ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("will be shot.").formatted(Formatting.AQUA)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
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
   
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHER_STAR,1);
      ArcanaIngredient b = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.INFINITY),1));
      ArcanaIngredient c = new ArcanaIngredient(Items.RABBIT_HIDE,12);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,32);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,c,h,c,b},
            {c,h,m,h,c},
            {b,c,h,c,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withFletchery().withCore().withEnchanter());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("  Overflowing Quiver\n\nRarity: Exotic\n\nMore experienced archers keep a variety of arrows on hand, however it is difficult to switch between them in the heat of a fight. This quiver has slots that not only save space, and keep arrows").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Overflowing Quiver\n\norganized, but it also contains a mechanism to help guide the archer's hand to the desired arrow type.\n\nLeft clicking with any bow cycles the preferred arrows.\n\nI have also managed to unlock greater").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Overflowing Quiver\n\npotential from the Infinity enchantment and imbued it within the quiver.\n\nThe quiver now slowly regenerates all arrows placed inside of it.\n\nIt is worth noting that this quiver is not").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Overflowing Quiver\n\nsturdy enough to channel Arcana to arrows placed inside,  restricting Runic Arrows from being contained within.\n\nI am looking into possible improvements to this design to accommodate more powerful projectiles.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class OverflowingQuiverItem extends ArcanaPolymerItem {
      public OverflowingQuiverItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(PolymerResourcePackUtils.hasMainPack(player)){
            return textureItem;
         }
         return super.getPolymerItem(itemStack, player);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.getModelData(TXT+"-"+getPolymerItem(itemStack,player).getTranslationKey()).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
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

