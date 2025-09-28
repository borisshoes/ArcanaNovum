package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.TextUtils;
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

public class RunicMatrix extends ArcanaItem {
	public static final String ID = "runic_matrix";
   
   public RunicMatrix(){
      id = ID;
      name = "Runic Matrix";
      rarity = ArcanaRarity.MUNDANE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.END_CRYSTAL;
      item = new RunicMatrixItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_END_CRYSTAL,ResearchTasks.ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS,ResearchTasks.OBTAIN_AMETHYST_SHARD};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runes ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("engraved on this ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("crystalline ").formatted(Formatting.AQUA))
            .append(Text.literal("structure").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" shift").formatted(Formatting.BLUE))
            .append(Text.literal(" constantly.").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("They ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("slide").formatted(Formatting.BLUE))
            .append(Text.literal(" to form different combinations of ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("runic ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("equations").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("matrix ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("allows for the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("invocation ").formatted(Formatting.AQUA))
            .append(Text.literal("of many different ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("effects").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.AMETHYST_SHARD,12);
      ArcanaIngredient b = new ArcanaIngredient(Items.DIAMOND,2);
      ArcanaIngredient c = new ArcanaIngredient(Items.END_CRYSTAL,2);
      ArcanaIngredient g = new ArcanaIngredient(Items.CRAFTER,8);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHER_STAR,1, true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,a,g,b},
            {c,a,m,a,c},
            {b,g,a,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Runic Matrix").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nRunes are part of the old Arcana that are able to evoke targeted effects. Being able to freely swap and combine runes to dynamically create runic equations, like ingredients in a ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Runic Matrix").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),Text.literal("\ncrafting recipe, results in a device capable of producing a vast number of Arcane effects. This device should be capable of quickly switching and recombining runes and activating them with its internal power source.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class RunicMatrixItem extends ArcanaPolymerItem {
      public RunicMatrixItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

