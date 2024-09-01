package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArmorItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WingsOfEnderia extends EnergyItem {
	public static final String ID = "wings_of_enderia";
   
   private static final String TXT = "item/wings_of_enderia";
   
   public WingsOfEnderia(){
      id = ID;
      name = "Armored Wings of Enderia";
      rarity = ArcanaRarity.DIVINE;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.DIVINE, TomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 1;
      vanillaItem = Items.ELYTRA;
      item = new WingsOfEnderiaItem(new Item.Settings().maxCount(1).fireproof().maxDamage(1024)
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Armored Wings of Enderia").formatted(Formatting.BOLD,Formatting.DARK_PURPLE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.UNBREAKABLE,new UnbreakableComponent(false))
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_WINGS_OF_ENDERIA};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponentTypes.ENCHANTMENTS, MiscUtils.makeEnchantComponent(
            new EnchantmentLevelEntry(MiscUtils.getEnchantment(server.getRegistryManager(),Enchantments.PROTECTION),4)
      ).withShowInTooltip(false));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Armored Wings").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" will shield you from the ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("dangers ").formatted(Formatting.YELLOW))
            .append(Text.literal("of the land.").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Wings ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("act as a ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Netherite Chestplate").formatted(Formatting.DARK_RED))
            .append(Text.literal(" with ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Protection IV").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("They store ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("energy ").formatted(Formatting.YELLOW))
            .append(Text.literal("as you fly to ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("cushion impacts").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" and are ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("unbreakable").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 10000; // Store up to 100 points of dmg mitigation at 5 seconds of flight per damage point stored aka 100 ticks/energy per 1 dmg point
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("     Armored Wings \n       of Enderia\n\nRarity: Divine\n\nThe first discovered Divine Item. They offer unsurmounted protection equivalent to the strongest of armor.\n\nThe Wings themselves are hardened to the ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("     Armored Wings \n       of Enderia\n\npoint of being impervious to structural damage.\n\nAs study of them furthers a new ability has been discovered.\nThe Wings collect and store kinetic energy when in flight that can be re-emitted when").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("     Armored Wings \n       of Enderia\n\nthe wearer suffers a large impact.\n\nThis effect seems to negate up to half of all fall damage and kinetic damage taken as long as the Wings have stored enough energy.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class WingsOfEnderiaItem extends ArcanaPolymerArmorItem {
      public WingsOfEnderiaItem(Item.Settings settings){
         super(getThis(),ArmorMaterials.NETHERITE,Type.CHESTPLATE,settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.getModelData(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

