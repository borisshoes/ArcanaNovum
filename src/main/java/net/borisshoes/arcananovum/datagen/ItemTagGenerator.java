package net.borisshoes.arcananovum.datagen;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static net.borisshoes.arcananovum.ArcanaRegistry.ARCANA_ITEM_KEY_MAP;

public class ItemTagGenerator extends FabricTagsProvider.ItemTagsProvider {
   public ItemTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, @Nullable FabricTagsProvider.BlockTagsProvider blockTagsProvider){
      super(output, registriesFuture, blockTagsProvider);
   }
   
   @Override
   protected void addTags(HolderLookup.Provider lookup){
      TagAppender<Item> allItemsBuilder = builder(ArcanaRegistry.ALL_ARCANA_ITEMS);
      TagAppender<Item> unstackableItemsBuilder = builder(ArcanaRegistry.UNSTACKABLE_ARCANA_ITEMS);
      TagAppender<Item> runicArrowsBuilder = builder(ArcanaRegistry.RUNIC_ARROWS);
      ArcanaRegistry.ARCANA_ITEMS.forEach(item -> {
         allItemsBuilder.add(ARCANA_ITEM_KEY_MAP.get(item.getItem()));
         if(item.getMaxCount() == 1){
            unstackableItemsBuilder.add(ARCANA_ITEM_KEY_MAP.get(item.getItem()));
         }
         if(item instanceof RunicArrow){
            runicArrowsBuilder.add(ARCANA_ITEM_KEY_MAP.get(item.getItem()));
         }
      });
      
      builder(ArcanaRegistry.FLETCHERY_POTION_ITEMS)
            .add(ItemIds.POTION)
            .add(ItemIds.LINGERING_POTION)
            .add(ItemIds.SPLASH_POTION)
      ;
      
      builder(ArcanaRegistry.ARCANISTS_BELT_SPECIAL_ALLOWED)
            .add(ItemIds.ENDER_PEARL)
            .add(ItemIds.FIREWORK_ROCKET)
            .add(ItemIds.WIND_CHARGE)
      ;
      
      builder(ArcanaRegistry.ARCANISTS_BELT_SPECIAL_DISALLOWED)
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ARCANISTS_BELT.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SHIELD_OF_FORTITUDE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.OVERFLOWING_QUIVER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.RUNIC_QUIVER.getItem()))
            .add(ItemIds.SHIELD)
            .forceAddTag(ItemTags.BUNDLES)
            .forceAddTag(ItemTags.SHULKER_BOXES)
      ;
      
      builder(ArcanaRegistry.FATE_ANCHOR_ENCHANTABLE)
            .forceAddTag(ItemTags.DURABILITY_ENCHANTABLE)
            .forceAddTag(ItemTags.VANISHING_ENCHANTABLE)
            .forceAddTag(ArcanaRegistry.UNSTACKABLE_ARCANA_ITEMS)
      ;
      
      builder(ArcanaRegistry.FATE_ANCHOR_UNENCHANTABLE);
      
      builder(ArcanaRegistry.NONPROTECTIVE_ARMOR_REPAIR)
            .add(ItemIds.LEATHER)
            .add(ItemIds.RABBIT_HIDE)
            .add(ItemIds.PHANTOM_MEMBRANE)
      ;
      
      builder(ArcanaRegistry.NEODYMIUM_STEALABLE)
            .add(ItemIds.IRON_INGOT)
            .add(BlockItemIds.IRON_BLOCK)
            .add(BlockItemIds.IRON_BARS)
            .add(ItemIds.IRON_AXE)
            .add(ItemIds.IRON_BOOTS)
            .add(ItemIds.IRON_CHESTPLATE)
            .add(BlockItemIds.IRON_DOOR)
            .add(ItemIds.IRON_HELMET)
            .add(ItemIds.IRON_HOE)
            .add(ItemIds.IRON_HORSE_ARMOR)
            .add(ItemIds.IRON_LEGGINGS)
            .add(ItemIds.IRON_NUGGET)
            .add(ItemIds.IRON_PICKAXE)
            .add(ItemIds.IRON_SHOVEL)
            .add(ItemIds.IRON_SWORD)
            .add(BlockItemIds.IRON_TRAPDOOR)
            .add(ItemIds.GOLD_INGOT)
            .add(BlockItemIds.GOLD_BLOCK)
            .add(ItemIds.GOLDEN_AXE)
            .add(ItemIds.GOLDEN_BOOTS)
            .add(ItemIds.GOLDEN_CHESTPLATE)
            .add(ItemIds.GOLDEN_HELMET)
            .add(ItemIds.GOLDEN_HOE)
            .add(ItemIds.GOLDEN_HORSE_ARMOR)
            .add(ItemIds.GOLDEN_LEGGINGS)
            .add(ItemIds.GOLD_NUGGET)
            .add(ItemIds.GOLDEN_PICKAXE)
            .add(ItemIds.GOLDEN_SHOVEL)
            .add(ItemIds.GOLDEN_SWORD)
            .add(ItemIds.COPPER_HELMET)
            .add(ItemIds.COPPER_CHESTPLATE)
            .add(ItemIds.COPPER_LEGGINGS)
            .add(ItemIds.COPPER_BOOTS)
            .add(ItemIds.COPPER_SWORD)
            .add(ItemIds.COPPER_AXE)
            .add(ItemIds.COPPER_SHOVEL)
            .add(ItemIds.COPPER_PICKAXE)
            .add(ItemIds.COPPER_HOE)
            .add(ItemIds.COPPER_HORSE_ARMOR)
            .add(ItemIds.COPPER_NUGGET)
            .add(BlockItemIds.COPPER_BULB.weathering().unaffected())
            .add(BlockItemIds.COPPER_BARS.weathering().unaffected())
            .add(BlockItemIds.COPPER_CHAIN.weathering().unaffected())
            .add(BlockItemIds.COPPER_GOLEM_STATUE.weathering().unaffected())
            .add(BlockItemIds.COPPER_CHEST.weathering().unaffected())
            .add(BlockItemIds.COPPER_GRATE.weathering().unaffected())
            .add(BlockItemIds.CHISELED_COPPER.weathering().unaffected())
            .add(BlockItemIds.COPPER_DOOR.weathering().unaffected())
            .add(BlockItemIds.COPPER_TRAPDOOR.weathering().unaffected())
            .add(ItemIds.SHIELD)
            .add(ItemIds.CROSSBOW)
            .add(BlockItemIds.IRON_CHAIN)
            .add(BlockItemIds.LIGHTNING_ROD.weathering().unaffected())
            .add(BlockItemIds.COPPER_BLOCK.weathering().unaffected())
            .add(ItemIds.COPPER_INGOT)
            .add(BlockItemIds.TRIPWIRE_HOOK)
            .add(ItemIds.CHAINMAIL_BOOTS)
            .add(ItemIds.CHAINMAIL_CHESTPLATE)
            .add(ItemIds.CHAINMAIL_HELMET)
            .add(ItemIds.CHAINMAIL_LEGGINGS)
            .add(ItemIds.BUCKET)
            .add(ItemIds.LAVA_BUCKET)
            .add(ItemIds.WATER_BUCKET)
            .add(ItemIds.MILK_BUCKET)
            .add(ItemIds.AXOLOTL_BUCKET)
            .add(ItemIds.COD_BUCKET)
            .add(BlockItemIds.POWDER_SNOW)
            .add(ItemIds.PUFFERFISH_BUCKET)
            .add(ItemIds.SALMON_BUCKET)
            .add(ItemIds.TROPICAL_FISH_BUCKET)
            .add(ItemIds.TADPOLE_BUCKET)
            .add(BlockItemIds.CAULDRON)
            .add(ItemIds.MINECART)
            .add(ItemIds.HOPPER_MINECART)
            .add(BlockItemIds.BLAST_FURNACE)
            .add(BlockItemIds.SMITHING_TABLE)
            .add(BlockItemIds.BELL)
            .add(BlockItemIds.LANTERN)
            .add(BlockItemIds.SOUL_LANTERN)
            .add(BlockItemIds.HOPPER)
            .add(ItemIds.IRON_SPEAR)
            .add(ItemIds.GOLDEN_SPEAR)
            .add(ItemIds.COPPER_SPEAR)
            .add(ItemIds.COPPER_NAUTILUS_ARMOR)
            .add(ItemIds.IRON_NAUTILUS_ARMOR)
            .add(ItemIds.GOLDEN_NAUTILUS_ARMOR)
      ;
      
      builder(ArcanaRegistry.ENDERIA_ITEMS)
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SOULSTONE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.WINGS_OF_ENDERIA.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.LEADERSHIP_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SHULKER_CORE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.LEVITATION_HARNESS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SPEAR_OF_TENBROUS.getItem()))
      ;
      
      builder(ArcanaRegistry.EQUAYUS_ITEMS)
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.EXOTIC_MATTER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.LIGHT_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.BRAIN_JAR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CONTINUUM_ANCHOR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.LEADERSHIP_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.RUNIC_MATRIX.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.FRACTAL_SPONGE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.IGNEOUS_COLLIDER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TELESCOPING_BEACON.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CATALYTIC_MATRIX.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.MUNDANE_CATALYST.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.EMPOWERED_CATALYST.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.EXOTIC_CATALYST.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SOVEREIGN_CATALYST.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.DIVINE_CATALYST.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.PLANESHIFTER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.MIDNIGHT_ENCHANTER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.STORMCALLER_ALTAR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CELESTIAL_ALTAR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ARCANE_SINGULARITY.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.STARPATH_ALTAR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.AEQUALIS_SCIENTIA.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CETACEA_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CLEANSING_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.GREAVES_OF_GAIALTUS.getItem()))
      ;
      
      builder(ArcanaRegistry.NUL_ITEMS)
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.EXOTIC_MATTER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SOULSTONE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ESSENCE_EGG.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.BRAIN_JAR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CONTINUUM_ANCHOR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.LEADERSHIP_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ANCIENT_DOWSING_ROD.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.IGNEOUS_COLLIDER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SHADOW_STALKERS_GLAIVE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TELESCOPING_BEACON.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CINDERS_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SPAWNER_HARNESS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SPAWNER_INFUSER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.NUL_MEMENTO.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.PLANESHIFTER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.STELLAR_CORE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TOTEM_OF_VENGEANCE.getItem()))
      ;
      
      builder(ArcanaRegistry.VILLAGE_ITEMS)
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.FEASTING_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.LIGHT_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.BRAIN_JAR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SPAWNER_HARNESS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.MAGNETISM_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.FELIDAE_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.OVERFLOWING_QUIVER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CHEST_TRANSLOCATOR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CONTAINMENT_CIRCLET.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.WILD_GROWTH_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TEMPORAL_MOMENT.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.EVERLASTING_ROCKET.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TWILIGHT_ANVIL.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.RADIANT_FLETCHERY.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ARCANISTS_BELT.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CETACEA_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CLEANSING_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.WAYSTONE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CLOCKWORK_MULTITOOL.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.NEGOTIATION_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ITINERANTEUR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.GEOMANTIC_STELE.getItem()))
      ;
      
      builder(ArcanaRegistry.WORKSHOP_ITEMS)
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.EXOTIC_MATTER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.FEASTING_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.LIGHT_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.BRAIN_JAR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SPAWNER_HARNESS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SHIELD_OF_FORTITUDE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SOJOURNER_BOOTS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TEMPORAL_MOMENT.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.RUNIC_MATRIX.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.MAGNETISM_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ANCIENT_DOWSING_ROD.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.STASIS_PEARL.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.PEARL_OF_RECALL.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.IGNEOUS_COLLIDER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.BLINK_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.RUNIC_BOW.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TETHER_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SMOKE_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CONCUSSION_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.SIPHONING_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ARCANE_FLAK_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.EXPULSION_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.GRAVITON_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.STORM_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.PHOTONIC_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TELESCOPING_BEACON.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.OVERFLOWING_QUIVER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CATALYTIC_MATRIX.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.MUNDANE_CATALYST.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.EMPOWERED_CATALYST.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.EXOTIC_CATALYST.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CHEST_TRANSLOCATOR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CONTAINMENT_CIRCLET.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.EVERLASTING_ROCKET.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TWILIGHT_ANVIL.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.RADIANT_FLETCHERY.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.PLANESHIFTER.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ARCANISTS_BELT.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.STORMCALLER_ALTAR.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ENSNAREMENT_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.TRACKING_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CETACEA_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CLEANSING_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.BINARY_BLADES.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.GRAVITON_ARROWS.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.WAYSTONE.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.CLOCKWORK_MULTITOOL.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.NEGOTIATION_CHARM.getItem()))
            .add(ARCANA_ITEM_KEY_MAP.get(ArcanaRegistry.ENDER_CRATE.getItem()))
      ;
   }
   
   @Override
   public String getName(){
      return "Arcana Novum - Item Tag Generator";
   }
}
