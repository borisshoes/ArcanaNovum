package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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

public class NebulousEssenceItem extends NormalPolymerItem {
   
   public NebulousEssenceItem(String id, net.minecraft.world.item.Item.Properties settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.SCULK_VEIN;
   }
   
   @Override
   public ItemStack getDefaultInstance(){
      ItemStack defStack = super.getDefaultInstance();
      defStack.set(DataComponents.ITEM_NAME, Component.translatable("item." + MOD_ID + ".nebulous_essence").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
      return defStack;
   }
   
   @Override
   public Component getName(ItemStack stack){
      return Component.translatable("item." + MOD_ID + ".nebulous_essence").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
   }
   
   public static ItemLore getDefaultLore(){
      List<Component> loreList = new ArrayList<>();
      loreList.add(Component.literal("")
            .append(Component.literal("With precise ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("deconstruction").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(", an ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("amorphic essence").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" has been ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("distilled").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      loreList.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("ethereal substance").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" must be pure ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("enchantment ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Arcana").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      loreList.add(Component.literal("")
            .append(Component.literal("It ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("pulses ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("undulates").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(", changing in ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("color ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("texture").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      return new ItemLore(loreList.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new)));
   }
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      return new IngredientCompendiumEntry(Component.translatable(ArcanaRegistry.NEBULOUS_ESSENCE.getDescriptionId()), new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE), ArcanaRegistry.NEBULOUS_ESSENCE);
   }
}