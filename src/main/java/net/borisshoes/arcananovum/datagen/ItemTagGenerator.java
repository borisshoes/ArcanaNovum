package net.borisshoes.arcananovum.datagen;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends FabricTagProvider.ItemTagProvider {
   public ItemTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture){
      super(output, registriesFuture);
   }
   
   @Override
   protected void configure(RegistryWrapper.WrapperLookup lookup){
      ProvidedTagBuilder<Item,Item> allItemsBuilder = valueLookupBuilder(ArcanaRegistry.ALL_ARCANA_ITEMS);
      ProvidedTagBuilder<Item,Item> unstackableItemsBuilder = valueLookupBuilder(ArcanaRegistry.UNSTACKABLE_ARCANA_ITEMS);
      ProvidedTagBuilder<Item,Item> runicArrowsBuilder = valueLookupBuilder(ArcanaRegistry.RUNIC_ARROWS);
      ArcanaRegistry.ARCANA_ITEMS.forEach(item -> {
         allItemsBuilder.add(item.getItem());
         if(item.getItem().getMaxCount() == 1){
            unstackableItemsBuilder.add(item.getItem());
         }
         if(item instanceof RunicArrow){
            runicArrowsBuilder.add(item.getItem());
         }
      });
      
      valueLookupBuilder(ArcanaRegistry.FLETCHERY_POTION_ITEMS)
            .add(Items.POTION)
            .add(Items.LINGERING_POTION)
            .add(Items.SPLASH_POTION)
      ;
      
      valueLookupBuilder(ArcanaRegistry.ARCANISTS_BELT_SPECIAL_ALLOWED)
            .add(Items.ENDER_PEARL)
            .add(Items.FIREWORK_ROCKET)
            .add(Items.WIND_CHARGE)
      ;
      
      valueLookupBuilder(ArcanaRegistry.ARCANISTS_BELT_SPECIAL_DISALLOWED)
            .add(ArcanaRegistry.ARCANISTS_BELT.getItem())
            .add(ArcanaRegistry.SHIELD_OF_FORTITUDE.getItem())
            .add(ArcanaRegistry.OVERFLOWING_QUIVER.getItem())
            .add(ArcanaRegistry.RUNIC_QUIVER.getItem())
            .add(Items.SHIELD)
            .forceAddTag(ItemTags.BUNDLES)
            .forceAddTag(ItemTags.SHULKER_BOXES)
      ;
      
      valueLookupBuilder(ArcanaRegistry.FATE_ANCHOR_ENCHANTABLE)
            .forceAddTag(ItemTags.DURABILITY_ENCHANTABLE)
            .forceAddTag(ItemTags.VANISHING_ENCHANTABLE)
            .forceAddTag(ArcanaRegistry.UNSTACKABLE_ARCANA_ITEMS)
      ;
      
      valueLookupBuilder(ArcanaRegistry.FATE_ANCHOR_UNENCHANTABLE);
      
      valueLookupBuilder(ArcanaRegistry.NONPROTECTIVE_ARMOR_REPAIR)
            .add(Items.LEATHER)
            .add(Items.RABBIT_HIDE)
            .add(Items.PHANTOM_MEMBRANE)
      ;
      
      valueLookupBuilder(ArcanaRegistry.NEODYMIUM_STEALABLE)
            .add(Items.IRON_INGOT)
            .add(Items.IRON_BLOCK)
            .add(Items.IRON_BARS)
            .add(Items.IRON_AXE)
            .add(Items.IRON_BOOTS)
            .add(Items.IRON_CHESTPLATE)
            .add(Items.IRON_DOOR)
            .add(Items.IRON_HELMET)
            .add(Items.IRON_HOE)
            .add(Items.IRON_HORSE_ARMOR)
            .add(Items.IRON_LEGGINGS)
            .add(Items.IRON_NUGGET)
            .add(Items.IRON_PICKAXE)
            .add(Items.IRON_SHOVEL)
            .add(Items.IRON_SWORD)
            .add(Items.IRON_TRAPDOOR)
            .add(Items.GOLD_INGOT)
            .add(Items.GOLD_BLOCK)
            .add(Items.GOLDEN_AXE)
            .add(Items.GOLDEN_BOOTS)
            .add(Items.GOLDEN_CHESTPLATE)
            .add(Items.GOLDEN_HELMET)
            .add(Items.GOLDEN_HOE)
            .add(Items.GOLDEN_HORSE_ARMOR)
            .add(Items.GOLDEN_LEGGINGS)
            .add(Items.GOLD_NUGGET)
            .add(Items.GOLDEN_PICKAXE)
            .add(Items.GOLDEN_SHOVEL)
            .add(Items.GOLDEN_SWORD)
            .add(Items.SHIELD)
            .add(Items.CROSSBOW)
            .add(Items.CHAIN)
            .add(Items.LIGHTNING_ROD)
            .add(Items.COPPER_BLOCK)
            .add(Items.COPPER_INGOT)
            .add(Items.TRIPWIRE_HOOK)
            .add(Items.CHAIN)
            .add(Items.CHAINMAIL_BOOTS)
            .add(Items.CHAINMAIL_CHESTPLATE)
            .add(Items.CHAINMAIL_HELMET)
            .add(Items.CHAINMAIL_LEGGINGS)
            .add(Items.BUCKET)
            .add(Items.LAVA_BUCKET)
            .add(Items.WATER_BUCKET)
            .add(Items.MILK_BUCKET)
            .add(Items.AXOLOTL_BUCKET)
            .add(Items.COD_BUCKET)
            .add(Items.POWDER_SNOW_BUCKET)
            .add(Items.PUFFERFISH_BUCKET)
            .add(Items.SALMON_BUCKET)
            .add(Items.TROPICAL_FISH_BUCKET)
            .add(Items.TADPOLE_BUCKET)
            .add(Items.CAULDRON)
            .add(Items.MINECART)
            .add(Items.HOPPER_MINECART)
            .add(Items.BLAST_FURNACE)
            .add(Items.SMITHING_TABLE)
            .add(Items.BELL)
            .add(Items.LANTERN)
            .add(Items.SOUL_LANTERN)
            .add(Items.HOPPER)
      ;
      
      valueLookupBuilder(ArcanaRegistry.ENDERIA_ITEMS)
            .add(ArcanaRegistry.SOULSTONE.getItem())
            .add(ArcanaRegistry.WINGS_OF_ENDERIA.getItem())
            .add(ArcanaRegistry.LEADERSHIP_CHARM.getItem())
            .add(ArcanaRegistry.SHULKER_CORE.getItem())
            .add(ArcanaRegistry.LEVITATION_HARNESS.getItem())
            .add(ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem())
            .add(ArcanaRegistry.SPEAR_OF_TENBROUS.getItem())
      ;
      
      valueLookupBuilder(ArcanaRegistry.EQUAYUS_ITEMS)
            .add(ArcanaRegistry.EXOTIC_MATTER.getItem())
            .add(ArcanaRegistry.LIGHT_CHARM.getItem())
            .add(ArcanaRegistry.BRAIN_JAR.getItem())
            .add(ArcanaRegistry.CONTINUUM_ANCHOR.getItem())
            .add(ArcanaRegistry.LEADERSHIP_CHARM.getItem())
            .add(ArcanaRegistry.RUNIC_MATRIX.getItem())
            .add(ArcanaRegistry.FRACTAL_SPONGE.getItem())
            .add(ArcanaRegistry.IGNEOUS_COLLIDER.getItem())
            .add(ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem())
            .add(ArcanaRegistry.TELESCOPING_BEACON.getItem())
            .add(ArcanaRegistry.CATALYTIC_MATRIX.getItem())
            .add(ArcanaRegistry.MUNDANE_CATALYST.getItem())
            .add(ArcanaRegistry.EMPOWERED_CATALYST.getItem())
            .add(ArcanaRegistry.EXOTIC_CATALYST.getItem())
            .add(ArcanaRegistry.SOVEREIGN_CATALYST.getItem())
            .add(ArcanaRegistry.DIVINE_CATALYST.getItem())
            .add(ArcanaRegistry.PLANESHIFTER.getItem())
            .add(ArcanaRegistry.MIDNIGHT_ENCHANTER.getItem())
            .add(ArcanaRegistry.STORMCALLER_ALTAR.getItem())
            .add(ArcanaRegistry.CELESTIAL_ALTAR.getItem())
            .add(ArcanaRegistry.ARCANE_SINGULARITY.getItem())
            .add(ArcanaRegistry.STARPATH_ALTAR.getItem())
            .add(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem())
            .add(ArcanaRegistry.AEQUALIS_SCIENTIA.getItem())
            .add(ArcanaRegistry.CETACEA_CHARM.getItem())
            .add(ArcanaRegistry.CLEANSING_CHARM.getItem())
            .add(ArcanaRegistry.GREAVES_OF_GAIALTUS.getItem())
      ;
      
      valueLookupBuilder(ArcanaRegistry.NUL_ITEMS)
            .add(ArcanaRegistry.EXOTIC_MATTER.getItem())
            .add(ArcanaRegistry.SOULSTONE.getItem())
            .add(ArcanaRegistry.ESSENCE_EGG.getItem())
            .add(ArcanaRegistry.BRAIN_JAR.getItem())
            .add(ArcanaRegistry.CONTINUUM_ANCHOR.getItem())
            .add(ArcanaRegistry.LEADERSHIP_CHARM.getItem())
            .add(ArcanaRegistry.ANCIENT_DOWSING_ROD.getItem())
            .add(ArcanaRegistry.IGNEOUS_COLLIDER.getItem())
            .add(ArcanaRegistry.SHADOW_STALKERS_GLAIVE.getItem())
            .add(ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem())
            .add(ArcanaRegistry.TELESCOPING_BEACON.getItem())
            .add(ArcanaRegistry.CINDERS_CHARM.getItem())
            .add(ArcanaRegistry.SPAWNER_HARNESS.getItem())
            .add(ArcanaRegistry.SPAWNER_INFUSER.getItem())
            .add(ArcanaRegistry.NUL_MEMENTO.getItem())
            .add(ArcanaRegistry.PLANESHIFTER.getItem())
            .add(ArcanaRegistry.STELLAR_CORE.getItem())
            .add(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem())
            .add(ArcanaRegistry.TOTEM_OF_VENGEANCE.getItem())
      ;
      
      valueLookupBuilder(ArcanaRegistry.VILLAGE_ITEMS)
            .add(ArcanaRegistry.FEASTING_CHARM.getItem())
            .add(ArcanaRegistry.LIGHT_CHARM.getItem())
            .add(ArcanaRegistry.BRAIN_JAR.getItem())
            .add(ArcanaRegistry.SPAWNER_HARNESS.getItem())
            .add(ArcanaRegistry.MAGNETISM_CHARM.getItem())
            .add(ArcanaRegistry.FELIDAE_CHARM.getItem())
            .add(ArcanaRegistry.OVERFLOWING_QUIVER.getItem())
            .add(ArcanaRegistry.CHEST_TRANSLOCATOR.getItem())
            .add(ArcanaRegistry.CONTAINMENT_CIRCLET.getItem())
            .add(ArcanaRegistry.WILD_GROWTH_CHARM.getItem())
            .add(ArcanaRegistry.TEMPORAL_MOMENT.getItem())
            .add(ArcanaRegistry.EVERLASTING_ROCKET.getItem())
            .add(ArcanaRegistry.TWILIGHT_ANVIL.getItem())
            .add(ArcanaRegistry.RADIANT_FLETCHERY.getItem())
            .add(ArcanaRegistry.ARCANISTS_BELT.getItem())
            .add(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem())
            .add(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem())
            .add(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem())
            .add(ArcanaRegistry.CETACEA_CHARM.getItem())
            .add(ArcanaRegistry.CLEANSING_CHARM.getItem())
      ;
      
      valueLookupBuilder(ArcanaRegistry.WORKSHOP_ITEMS)
            .add(ArcanaRegistry.EXOTIC_MATTER.getItem())
            .add(ArcanaRegistry.FEASTING_CHARM.getItem())
            .add(ArcanaRegistry.LIGHT_CHARM.getItem())
            .add(ArcanaRegistry.BRAIN_JAR.getItem())
            .add(ArcanaRegistry.SPAWNER_HARNESS.getItem())
            .add(ArcanaRegistry.SHIELD_OF_FORTITUDE.getItem())
            .add(ArcanaRegistry.SOJOURNER_BOOTS.getItem())
            .add(ArcanaRegistry.TEMPORAL_MOMENT.getItem())
            .add(ArcanaRegistry.RUNIC_MATRIX.getItem())
            .add(ArcanaRegistry.MAGNETISM_CHARM.getItem())
            .add(ArcanaRegistry.ANCIENT_DOWSING_ROD.getItem())
            .add(ArcanaRegistry.STASIS_PEARL.getItem())
            .add(ArcanaRegistry.PEARL_OF_RECALL.getItem())
            .add(ArcanaRegistry.IGNEOUS_COLLIDER.getItem())
            .add(ArcanaRegistry.BLINK_ARROWS.getItem())
            .add(ArcanaRegistry.RUNIC_BOW.getItem())
            .add(ArcanaRegistry.TETHER_ARROWS.getItem())
            .add(ArcanaRegistry.SMOKE_ARROWS.getItem())
            .add(ArcanaRegistry.CONCUSSION_ARROWS.getItem())
            .add(ArcanaRegistry.SIPHONING_ARROWS.getItem())
            .add(ArcanaRegistry.ARCANE_FLAK_ARROWS.getItem())
            .add(ArcanaRegistry.EXPULSION_ARROWS.getItem())
            .add(ArcanaRegistry.GRAVITON_ARROWS.getItem())
            .add(ArcanaRegistry.STORM_ARROWS.getItem())
            .add(ArcanaRegistry.PHOTONIC_ARROWS.getItem())
            .add(ArcanaRegistry.TELESCOPING_BEACON.getItem())
            .add(ArcanaRegistry.OVERFLOWING_QUIVER.getItem())
            .add(ArcanaRegistry.CATALYTIC_MATRIX.getItem())
            .add(ArcanaRegistry.MUNDANE_CATALYST.getItem())
            .add(ArcanaRegistry.EMPOWERED_CATALYST.getItem())
            .add(ArcanaRegistry.EXOTIC_CATALYST.getItem())
            .add(ArcanaRegistry.CHEST_TRANSLOCATOR.getItem())
            .add(ArcanaRegistry.CONTAINMENT_CIRCLET.getItem())
            .add(ArcanaRegistry.EVERLASTING_ROCKET.getItem())
            .add(ArcanaRegistry.TWILIGHT_ANVIL.getItem())
            .add(ArcanaRegistry.RADIANT_FLETCHERY.getItem())
            .add(ArcanaRegistry.PLANESHIFTER.getItem())
            .add(ArcanaRegistry.ARCANISTS_BELT.getItem())
            .add(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem())
            .add(ArcanaRegistry.STORMCALLER_ALTAR.getItem())
            .add(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem())
            .add(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem())
            .add(ArcanaRegistry.ENSNAREMENT_ARROWS.getItem())
            .add(ArcanaRegistry.TRACKING_ARROWS.getItem())
            .add(ArcanaRegistry.CETACEA_CHARM.getItem())
            .add(ArcanaRegistry.CLEANSING_CHARM.getItem())
            .add(ArcanaRegistry.BINARY_BLADES.getItem())
            .add(ArcanaRegistry.GRAVITON_ARROWS.getItem())
      ;
   }
   
   @Override
   public String getName(){
      return "Arcana Novum - Item Tag Generator";
   }
}
