package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItemContainer;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
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
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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

public class RunicQuiver extends QuiverItem implements ArcanaItemContainer.ArcanaItemContainerHaver {
	public static final String ID = "runic_quiver";
   
   public static final int size = 9;
   private static final int[] refillReduction = {0,100,200,400,600,900};
   private static final double[] efficiencyChance = {0,.05,.1,.15,.2,.3};
   private static final String TXT = "item/runic_quiver";
   
   public RunicQuiver(){
      id = ID;
      name = "Runic Quiver";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.SOVEREIGN, TomeGui.TomeFilter.ITEMS};
      color = Formatting.LIGHT_PURPLE;
      vanillaItem = Items.LEATHER;
      item = new RunicQuiverItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Runic Quiver").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_OVERFLOWING_QUIVER,ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.CONCENTRATION_DAMAGE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,ARROWS_TAG,new NbtList());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("runes ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("engraved ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("upon the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("quiver ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("hum in the presence of ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" placed within the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("quiver ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("regenerate ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("over ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("time").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Arrows ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("take reduced ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("concentration ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("when in the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("quiver").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to put ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Arrows ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("in the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("quiver").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Left Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" with a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Bow").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to swap which ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Arrow").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" will be shot.").formatted(Formatting.DARK_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
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
      ItemStack newArcanaItem = getNewItem();
      putProperty(newArcanaItem,ARROWS_TAG,getListProperty(quiverStack,ARROWS_TAG,NbtElement.COMPOUND_TYPE));
      ArcanaAugments.copyAugment(quiverStack,newArcanaItem,ArcanaAugments.OVERFLOWING_BOTTOMLESS.id,ArcanaAugments.RUNIC_BOTTOMLESS.id);
      ArcanaAugments.copyAugment(quiverStack,newArcanaItem,ArcanaAugments.ABUNDANT_AMMO.id,ArcanaAugments.QUIVER_DUPLICATION.id);
      return newArcanaItem;
   }
   
   private RunicQuiver getOuter(){
      return this;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.LEATHER,32);
      ArcanaIngredient b = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.INFINITY),1));
      ArcanaIngredient c = new ArcanaIngredient(Items.NETHER_STAR,2);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      GenericArcanaIngredient h = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.OVERFLOWING_QUIVER,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withCore().withFletchery().withEnchanter().withAnvil());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Runic Quiver\n\nRarity: Sovereign\n\nMy improvements upon the overflowing quiver have been completed and now the quiver is capable of sending some of my Arcana to Runic Arrows within. \nI even managed to make the quiver take a reduced amount of")));
      list.add(List.of(Text.literal("      Runic Quiver\n\nconcentration, allowing for more Runic Arrows to be stored without overburdening my mind.\n\nThe quiver acts the same as its base counterpart just with this added expansion and a quiver restock time.")));
      return list;
   }
   
   @Override
   public ArcanaItemContainer getArcanaItemContainer(ItemStack item){
      int size = 9;
      NbtList arrows = getListProperty(item,ARROWS_TAG,NbtElement.COMPOUND_TYPE);
      SimpleInventory inv = new SimpleInventory(size);
      
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound stack = arrows.getCompound(i);
         ItemStack itemStack = ItemStack.fromNbt(ArcanaNovum.SERVER.getRegistryManager(),stack).orElse(ItemStack.EMPTY);
         inv.setStack(i,itemStack);
      }
      double concMod = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SHUNT_RUNES.id) > 0 ? 0.25 : 0.5;
      
      return new ArcanaItemContainer(inv, size,3, "RQ", "Runic Quiver", concMod);
   }
   
   public class RunicQuiverItem extends ArcanaPolymerItem {
      public RunicQuiverItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
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

