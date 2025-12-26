package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
      item = new TemporalMomentItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_CLOCK,ResearchTasks.ADVANCEMENT_SLEEP_IN_BED,ResearchTasks.USE_ENDER_PEARL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A piece of ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("spacetime ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("collapsing").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA))
            .append(Component.literal(" down to a single ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("moment").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("clock").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" itself is stuck between ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("one ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("instant of ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("time").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Component.literal(" and ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("another").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("discovery").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" unlocks a whole ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("world").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(" of ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("possibilities").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
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
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Temporal Moment").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nTime always moves forwards, but its rate can be changed from fluctuation in spacetime. With enough energy, perhaps it can be slowed to a halt, freezing a moment in time to use later.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class TemporalMomentItem extends ArcanaPolymerItem {
      public TemporalMomentItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

