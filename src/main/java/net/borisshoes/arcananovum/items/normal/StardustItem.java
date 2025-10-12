package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.TextUtils;
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

public class StardustItem extends NormalPolymerItem {
   
   public StardustItem(String id, Settings settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.GLOWSTONE_DUST;
   }
   
   @Override
   public ItemStack getDefaultStack(){
      ItemStack defStack = super.getDefaultStack();
      defStack.set(DataComponentTypes.ITEM_NAME, getName(null));
      return defStack;
   }
   
   @Override
   public Text getName(ItemStack stack) {
      return Text.translatable("item."+MOD_ID+".stardust").formatted(Formatting.YELLOW,Formatting.BOLD);
   }
   
   public static LoreComponent getDefaultLore(){
      List<Text> loreList = new ArrayList<>();
      loreList.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GOLD))
            .append(Text.literal("dust ").formatted(Formatting.YELLOW))
            .append(Text.literal("sparkles ").formatted(Formatting.AQUA))
            .append(Text.literal("and ").formatted(Formatting.GOLD))
            .append(Text.literal("twinkles ").formatted(Formatting.AQUA))
            .append(Text.literal("like the ").formatted(Formatting.GOLD))
            .append(Text.literal("night sky").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GOLD)));
      loreList.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GOLD))
            .append(Text.literal("energy").formatted(Formatting.AQUA))
            .append(Text.literal(" from the ").formatted(Formatting.GOLD))
            .append(Text.literal("Stellar Core").formatted(Formatting.YELLOW))
            .append(Text.literal(" seems to ").formatted(Formatting.GOLD))
            .append(Text.literal("fuse ").formatted(Formatting.AQUA))
            .append(Text.literal("with ").formatted(Formatting.GOLD))
            .append(Text.literal("enchantments").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.GOLD)));
      loreList.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.GOLD))
            .append(Text.literal("Arcana ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("contained within should harbor ").formatted(Formatting.GOLD))
            .append(Text.literal("desirable properties").formatted(Formatting.AQUA))
            .append(Text.literal("...").formatted(Formatting.GOLD)));
      return new LoreComponent(loreList.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new)));
   }
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, 0xff8800),1,"",false)
            .withName(Text.literal("In Stellar Core").formatted(Formatting.GOLD));
      ExplainIngredient b = new ExplainIngredient(Items.BLAZE_POWDER,1,"",false)
            .withName(Text.literal("Salvage Enchanted Equipment").formatted(Formatting.GOLD))
            .withLore(List.of(Text.literal("Use a Stellar Core to salvage enchanted equipment").formatted(Formatting.YELLOW)));
      ExplainIngredient m = new ExplainIngredient(Items.MAGMA_BLOCK,1,"",false)
            .withName(Text.literal("Salvage Enchanted Equipment").formatted(Formatting.GOLD))
            .withLore(List.of(Text.literal("Use a Stellar Core to salvage enchanted equipment").formatted(Formatting.YELLOW)));
      ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
      chestplate.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,true);
      ExplainIngredient g = new ExplainIngredient(chestplate,1,"Piece of Enchanted Equipment", true)
            .withName(Text.literal("Enchanted Equipment").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE))
            .withLore(List.of(Text.literal("Better enchantments yield more Stardust").formatted(Formatting.DARK_PURPLE)));
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,m,b,m,a},
            {a,b,g,b,a},
            {a,m,b,m,a},
            {a,a,a,a,a}};

      return new IngredientCompendiumEntry(Text.translatable(ArcanaRegistry.STARDUST.getTranslationKey()), new ItemStack(ArcanaRegistry.STARDUST), new ExplainRecipe(ingredients));
   }
}