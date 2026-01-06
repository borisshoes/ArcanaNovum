package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
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

public class EmpoweredCatalyst extends ArcanaItem {
	public static final String ID = "empowered_catalyst";
   
   public EmpoweredCatalyst(){
      id = ID;
      name = "Empowered Augment Catalyst";
      rarity = ArcanaRarity.MUNDANE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.CATALYSTS};
      vanillaItem = Items.EMERALD;
      item = new EmpoweredCatalystItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_MUNDANE_CATALYST,ResearchTasks.OBTAIN_EMERALD,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
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
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("     Empowered\n    Augmentation\n       Catalyst").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nMy previous endeavor was successful, at least somewhat. The Matrix destabilized with more demanding augmentations. I believe I can press it ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Empowered\n    Augmentation\n       Catalyst").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), Component.literal("\nfurther if I use some better crystals.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class EmpoweredCatalystItem extends ArcanaPolymerItem {
      public EmpoweredCatalystItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

