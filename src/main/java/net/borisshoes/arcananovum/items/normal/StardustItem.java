package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class StardustItem extends NormalPolymerItem {
   
   public StardustItem(String id, net.minecraft.world.item.Item.Properties settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.GLOWSTONE_DUST;
   }
   
   @Override
   public ItemStack getDefaultInstance(){
      ItemStack defStack = super.getDefaultInstance();
      defStack.set(DataComponents.ITEM_NAME, getName(null));
      return defStack;
   }
   
   @Override
   public Component getName(ItemStack stack) {
      return Component.translatable("item."+MOD_ID+".stardust").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
   }
   
   public static ItemLore getDefaultLore(){
      List<Component> loreList = new ArrayList<>();
      loreList.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("dust ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("sparkles ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("twinkles ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("like the ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("night sky").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GOLD)));
      loreList.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("energy").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" from the ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("Stellar Core").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" seems to ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("fuse ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("with ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("enchantments").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.GOLD)));
      loreList.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("Arcana ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("contained within should harbor ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("desirable properties").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("...").withStyle(ChatFormatting.GOLD)));
      return new ItemLore(loreList.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new)));
   }
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      return new IngredientCompendiumEntry(Component.translatable(ArcanaRegistry.STARDUST.getDescriptionId()), new ItemStack(ArcanaRegistry.STARDUST), ArcanaRegistry.STARDUST);
   }
}