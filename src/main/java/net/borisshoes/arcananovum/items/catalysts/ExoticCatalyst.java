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

public class ExoticCatalyst extends ArcanaItem {
	public static final String ID = "exotic_catalyst";
   
   public ExoticCatalyst(){
      id = ID;
      name = "Exotic Augment Catalyst";
      rarity = ArcanaRarity.MUNDANE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.CATALYSTS};
      vanillaItem = Items.DIAMOND;
      item = new ExoticCatalystItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_EMPOWERED_CATALYST,ResearchTasks.OBTAIN_DIAMOND,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Augment ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Catalysts").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" can be used to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("augment ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("your ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Augments ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("require more ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("powerful ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("Catalysts ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("at higher levels").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Apply ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("these ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Catalysts ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("in the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Tinkering Menu").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" of a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Twilight Anvil").withStyle(ChatFormatting.GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.OBSIDIAN,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.DIAMOND,6);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.EMPOWERED_CATALYST,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {c,h,m,h,c},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(this, ingredients,new ForgeRequirement().withAnvil());
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("       Exotic\n   Augmentation\n      Catalyst").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nI think I’ve cracked it. Higher and higher quality gemstones can channel more of the Matrix’s power. But now the Matrix seems less malleable in its ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("       Exotic\n   Augmentation\n      Catalyst").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nrange of possible augments…").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ExoticCatalystItem extends ArcanaPolymerItem {
      public ExoticCatalystItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

