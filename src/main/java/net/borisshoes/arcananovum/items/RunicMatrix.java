package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
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

public class RunicMatrix extends ArcanaItem {
   public static final String ID = "runic_matrix";
   
   public RunicMatrix(){
      id = ID;
      name = "Runic Matrix";
      rarity = ArcanaRarity.MUNDANE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.END_CRYSTAL;
      item = new RunicMatrixItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_END_CRYSTAL, ResearchTasks.ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS, ResearchTasks.OBTAIN_AMETHYST_SHARD};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runes ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("engraved on this ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("crystalline ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("structure").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" shift").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" constantly.").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("They ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("slide").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" to form different combinations of ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("runic ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("equations").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("matrix ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("allows for the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("invocation ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("of many different ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("effects").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Runic Matrix").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nRunes are part of the old Arcana that are able to evoke targeted effects. Being able to freely swap and combine runes to dynamically create runic equations, like ingredients in a ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Runic Matrix").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\ncrafting recipe, results in a device capable of producing a vast number of Arcane effects. This device should be capable of quickly switching and recombining runes and activating them with its internal power source.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class RunicMatrixItem extends ArcanaPolymerItem {
      public RunicMatrixItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

