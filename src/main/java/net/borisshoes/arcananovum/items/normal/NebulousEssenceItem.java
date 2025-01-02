package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class NebulousEssenceItem extends NormalPolymerItem {
   
   public NebulousEssenceItem(String id, Settings settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.SCULK_VEIN;
   }
   
   @Override
   public ItemStack getDefaultStack(){
      ItemStack defStack = super.getDefaultStack();
      defStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+".nebulous_essence").formatted(Formatting.DARK_PURPLE,Formatting.BOLD));
      return defStack;
   }
   
   @Override
   public Text getName(ItemStack stack) {
      return Text.translatable("item."+MOD_ID+".nebulous_essence").formatted(Formatting.DARK_PURPLE,Formatting.BOLD);
   }
   
   public static LoreComponent getDefaultLore(){
      List<Text> loreList = new ArrayList<>();
      loreList.add(Text.literal("")
            .append(Text.literal("With precise ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("deconstruction").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(", an ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("amorphic essence").formatted(Formatting.AQUA))
            .append(Text.literal(" has been ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("distilled").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      loreList.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("ethereal substance").formatted(Formatting.AQUA))
            .append(Text.literal(" must be pure ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("enchantment ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Arcana").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      loreList.add(Text.literal("")
            .append(Text.literal("It ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("pulses ").formatted(Formatting.AQUA))
            .append(Text.literal("and ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("undulates").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(", changing in ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("color ").formatted(Formatting.AQUA))
            .append(Text.literal("and ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("texture").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      return new LoreComponent(loreList.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new)));
   }
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.LAPIS_COLOR),1,"",false)
            .withName(Text.literal("In Midnight Enchanter").formatted(Formatting.DARK_AQUA));
      ExplainIngredient m = new ExplainIngredient(ArcanaRegistry.MIDNIGHT_ENCHANTER.getItem(),1,"",false)
            .withName(Text.literal("Midnight Enchanter").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
            .withLore(List.of(Text.literal("Disenchant an item using the Midnight Enchanter").formatted(Formatting.BLUE)));
      ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
      chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,true);
      ExplainIngredient b = new ExplainIngredient(chestplate,1,"Enchanted Item", true)
            .withName(Text.literal("Enchanted Item").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE))
            .withLore(List.of(Text.literal("Better enchantments yield more Essence").formatted(Formatting.DARK_PURPLE)));
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,a,a,a,a},
            {a,a,b,a,a},
            {a,a,m,a,a},
            {a,a,a,a,a}};
      
      return new IngredientCompendiumEntry(Text.translatable(ArcanaRegistry.NEBULOUS_ESSENCE.getTranslationKey()), new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE), new ExplainRecipe(ingredients));
   }
}