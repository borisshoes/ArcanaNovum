package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TemporalMoment extends ArcanaItem {
	public static final String ID = "temporal_moment";
   
   private static final String TXT = "item/temporal_moment";
   
   public TemporalMoment(){
      id = ID;
      name = "Temporal Moment";
      rarity = ArcanaRarity.MUNDANE;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.MUNDANE, TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.CLOCK;
      item = new TemporalMomentItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Temporal Moment").formatted(Formatting.BOLD,Formatting.DARK_BLUE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_CLOCK,ResearchTasks.ADVANCEMENT_SLEEP_IN_BED,ResearchTasks.USE_ENDER_PEARL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A piece of ").formatted(Formatting.BLUE))
            .append(Text.literal("spacetime ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("collapsing").formatted(Formatting.ITALIC,Formatting.DARK_AQUA))
            .append(Text.literal(" down to a single ").formatted(Formatting.BLUE))
            .append(Text.literal("moment").formatted(Formatting.DARK_BLUE))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.BLUE))
            .append(Text.literal("clock").formatted(Formatting.AQUA))
            .append(Text.literal(" itself is stuck between ").formatted(Formatting.BLUE))
            .append(Text.literal("one ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("instant of ").formatted(Formatting.BLUE))
            .append(Text.literal("time").formatted(Formatting.DARK_BLUE))
            .append(Text.literal(" and ").formatted(Formatting.BLUE))
            .append(Text.literal("another").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.BLUE))
            .append(Text.literal("discovery").formatted(Formatting.AQUA))
            .append(Text.literal(" unlocks a whole ").formatted(Formatting.BLUE))
            .append(Text.literal("world").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" of ").formatted(Formatting.BLUE))
            .append(Text.literal("possibilites").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,4);
      ArcanaIngredient b = new ArcanaIngredient(Items.ENDER_PEARL,2);
      ArcanaIngredient c = new ArcanaIngredient(Items.OBSIDIAN,4);
      ArcanaIngredient g = new ArcanaIngredient(Items.DIAMOND,2);
      ArcanaIngredient h = new ArcanaIngredient(Items.LAPIS_LAZULI,16);
      ArcanaIngredient m = new ArcanaIngredient(Items.CLOCK,4);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Temporal Moment\n\nRarity: Mundane\n\nTime always moves forwards, but its rate can be changed from fluctuations in spacetime. With enough energy perhaps it could be slowed to a halt, freezing a moment in time to use later.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class TemporalMomentItem extends ArcanaPolymerItem {
      public TemporalMomentItem(Item.Settings settings){
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
   }
}

