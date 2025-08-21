package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
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

public class CatalyticMatrix extends ArcanaItem {
	public static final String ID = "catalytic_matrix";
   
   public CatalyticMatrix(){
      id = ID;
      name = "Catalytic Matrix";
      rarity = ArcanaRarity.MUNDANE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.CATALYSTS};
      itemVersion = 0;
      vanillaItem = Items.NETHER_STAR;
      item = new CatalyticMatrixItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.YELLOW);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_TWILIGHT_ANVIL,ResearchTasks.ADVANCEMENT_ENCHANT_ITEM};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A fragment of a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Matrix").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" specialized").formatted(Formatting.BLUE))
            .append(Text.literal(" in ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("augmenting ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Arcana").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("On its own, this ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("new matrix").formatted(Formatting.YELLOW))
            .append(Text.literal(" is ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("useless").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("matrix ").formatted(Formatting.YELLOW))
            .append(Text.literal("must be ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("built upon").formatted(Formatting.BLUE))
            .append(Text.literal(" to ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("unlock ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("full potential").formatted(Formatting.AQUA))
            .append(Text.literal(" of ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Arcana").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.AMETHYST_SHARD,12);
      ArcanaIngredient b = new ArcanaIngredient(Items.CRAFTER,3);
      ArcanaIngredient c = new ArcanaIngredient(Items.DIAMOND,2);
      ArcanaIngredient g = new ArcanaIngredient(Items.END_CRYSTAL,4);
      ArcanaIngredient h = new ArcanaIngredient(Items.NETHER_STAR,1);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
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
      list.add(List.of(TextUtils.withColor(Text.literal("  Catalytic Matrix").formatted(Formatting.BOLD), ArcanaColors.STARLIGHT_FORGE_COLOR),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThe full power of a Runic Matrix shouldn’t be necessary to further unlock abilities within the items I’ve made. Breaking one into self-contained fragments should be more efficient.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class CatalyticMatrixItem extends ArcanaPolymerItem {
      public CatalyticMatrixItem(){
         super(getThis(),getArcanaItemComponents().maxCount(4));
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

