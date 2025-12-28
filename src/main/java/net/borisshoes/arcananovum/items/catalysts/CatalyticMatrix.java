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
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_TWILIGHT_ANVIL,ResearchTasks.ADVANCEMENT_ENCHANT_ITEM};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A fragment of a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Matrix").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" specialized").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" in ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("augmenting ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Arcana").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("On its own, this ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("new matrix").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" is ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("useless").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("matrix ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("must be ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("built upon").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("unlock ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("full potential").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" of ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Arcana").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
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
      return new ArcanaRecipe(this, ingredients,new ForgeRequirement().withAnvil());
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Catalytic Matrix").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThe full power of a Runic Matrix shouldn’t be necessary to further unlock abilities within the items I’ve made. Breaking one into self-contained fragments should be more efficient.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class CatalyticMatrixItem extends ArcanaPolymerItem {
      public CatalyticMatrixItem(){
         super(getThis(),getArcanaItemComponents().stacksTo(4));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

