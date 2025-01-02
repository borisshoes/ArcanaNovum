package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TemporalMoment extends ArcanaItem {
	public static final String ID = "temporal_moment";
   
   public TemporalMoment(){
      id = ID;
      name = "Temporal Moment";
      rarity = ArcanaRarity.MUNDANE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.CLOCK;
      item = new TemporalMomentItem(addArcanaItemComponents(new Item.Settings().maxCount(1)));
      displayName = TextUtils.withColor(Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD), ArcanaColors.BETTER_DARK_BLUE);
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
            .append(TextUtils.withColor(Text.literal("moment"), ArcanaColors.BETTER_DARK_BLUE))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.BLUE))
            .append(Text.literal("clock").formatted(Formatting.AQUA))
            .append(Text.literal(" itself is stuck between ").formatted(Formatting.BLUE))
            .append(Text.literal("one ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("instant of ").formatted(Formatting.BLUE))
            .append(TextUtils.withColor(Text.literal("time"), ArcanaColors.BETTER_DARK_BLUE))
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
      list.add(List.of(TextUtils.withColor(Text.literal(" Temporal Moment").formatted(Formatting.BOLD),ArcanaColors.BETTER_DARK_BLUE),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nTime always moves forwards, but its rate can be changed from fluctuation in spacetime. With enough energy, perhaps it can be slowed to a halt, freezing a moment in time to use later.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class TemporalMomentItem extends ArcanaPolymerItem {
      public TemporalMomentItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

