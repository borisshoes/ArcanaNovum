package net.borisshoes.arcananovum.gui.arcanistsbelt;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArcanistsBeltSlot extends Slot {
   
   public static final List<ArcanaItem> BELT_ARCANA_ITEMS = new ArrayList<>(Arrays.asList(
         ArcanaRegistry.ALCHEMICAL_ARBALEST,
         ArcanaRegistry.ANCIENT_DOWSING_ROD,
         ArcanaRegistry.ARCANE_TOME,
         ArcanaRegistry.BRAIN_JAR,
         ArcanaRegistry.CHEST_TRANSLOCATOR,
         ArcanaRegistry.CONTAINMENT_CIRCLET,
         ArcanaRegistry.SOULSTONE,
         ArcanaRegistry.PEARL_OF_RECALL,
         ArcanaRegistry.STASIS_PEARL,
         ArcanaRegistry.PICKAXE_OF_CEPTYUS,
         ArcanaRegistry.PLANESHIFTER,
         ArcanaRegistry.RUNIC_BOW,
         ArcanaRegistry.SHADOW_STALKERS_GLAIVE,
         ArcanaRegistry.CINDERS_CHARM,
         ArcanaRegistry.FEASTING_CHARM,
         ArcanaRegistry.FELIDAE_CHARM,
         ArcanaRegistry.LIGHT_CHARM,
         ArcanaRegistry.LEADERSHIP_CHARM,
         ArcanaRegistry.MAGNETISM_CHARM,
         ArcanaRegistry.WILD_GROWTH_CHARM,
         ArcanaRegistry.TELESCOPING_BEACON,
         ArcanaRegistry.SPAWNER_HARNESS,
         ArcanaRegistry.EVERLASTING_ROCKET,
         ArcanaRegistry.AQUATIC_EVERSOURCE,
         ArcanaRegistry.TOTEM_OF_VENGEANCE,
         ArcanaRegistry.MAGMATIC_EVERSOURCE,
         ArcanaRegistry.AEQUALIS_SCIENTIA
   ));
   
   public static final List<Item> BELT_NORMAL_ITEMS = new ArrayList<>(Arrays.asList(
         Items.SPYGLASS
   ));
   
   public ArcanistsBeltSlot(Inventory inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean canInsert(ItemStack stack){
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(arcanaItem != null){
         return BELT_ARCANA_ITEMS.contains(arcanaItem);
      }
      return stack.isDamageable() || BELT_NORMAL_ITEMS.contains(stack.getItem());
   }
}
