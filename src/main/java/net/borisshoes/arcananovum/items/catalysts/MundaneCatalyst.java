package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
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

public class MundaneCatalyst extends ArcanaItem {
	public static final String ID = "mundane_catalyst";
   
   public MundaneCatalyst(){
      id = ID;
      name = "Mundane Augment Catalyst";
      rarity = ArcanaRarity.MUNDANE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.CATALYSTS};
      vanillaItem = Items.QUARTZ;
      item = new MundaneCatalystItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.GRAY);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_CATALYTIC_MATRIX,ResearchTasks.OBTAIN_QUARTZ,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Augment ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Catalysts").formatted(Formatting.BLUE))
            .append(Text.literal(" can be used to ").formatted(Formatting.GRAY))
            .append(Text.literal("augment ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("your ").formatted(Formatting.GRAY))
            .append(Text.literal("Arcana Items").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Augments ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("require more ").formatted(Formatting.GRAY))
            .append(Text.literal("powerful ").formatted(Formatting.GREEN))
            .append(Text.literal("Catalysts ").formatted(Formatting.BLUE))
            .append(Text.literal("at higher levels").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Apply ").formatted(Formatting.GREEN))
            .append(Text.literal("these ").formatted(Formatting.GRAY))
            .append(Text.literal("Catalysts ").formatted(Formatting.BLUE))
            .append(Text.literal("in the ").formatted(Formatting.GRAY))
            .append(Text.literal("Tinkering Menu").formatted(Formatting.BLUE))
            .append(Text.literal(" of a ").formatted(Formatting.GRAY))
            .append(Text.literal("Twilight Anvil").formatted(Formatting.GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.OBSIDIAN,8);
      ArcanaIngredient g = new ArcanaIngredient(Items.CRYING_OBSIDIAN,8);
      ArcanaIngredient h = new ArcanaIngredient(Items.QUARTZ,6);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.CATALYTIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {c,h,m,h,c},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Mundane\n   Augmentation\n      Catalyst").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThe Catalytic Matrix is truly incredible. Everyday, I discover new equations that it can implement. With some extra crystals, I can adapt it to augment the abilities ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Mundane\n   Augmentation\n      Catalyst").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\nof every item I’ve made.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class MundaneCatalystItem extends ArcanaPolymerItem {
      public MundaneCatalystItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

