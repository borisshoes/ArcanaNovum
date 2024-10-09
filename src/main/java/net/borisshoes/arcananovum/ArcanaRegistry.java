package net.borisshoes.arcananovum;

import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.areaeffects.AftershockAreaEffectTracker;
import net.borisshoes.arcananovum.areaeffects.AlchemicalArrowAreaEffectTracker;
import net.borisshoes.arcananovum.areaeffects.AreaEffectTracker;
import net.borisshoes.arcananovum.areaeffects.SmokeArrowAreaEffectTracker;
import net.borisshoes.arcananovum.blocks.*;
import net.borisshoes.arcananovum.blocks.altars.*;
import net.borisshoes.arcananovum.blocks.forge.*;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.effects.*;
import net.borisshoes.arcananovum.entities.*;
import net.borisshoes.arcananovum.gui.arcanetome.ArcanaItemCompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.CompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.TransmutationRecipesCompendiumEntry;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.items.arrows.*;
import net.borisshoes.arcananovum.items.catalysts.*;
import net.borisshoes.arcananovum.items.charms.*;
import net.borisshoes.arcananovum.items.normal.*;
import net.borisshoes.arcananovum.lootfunctions.ArcanaBlockEntityLootFunction;
import net.borisshoes.arcananovum.lootfunctions.ArcaneNotesLootFunction;
import net.borisshoes.arcananovum.recipes.ArcanaShieldDecoratorRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.world.structures.FabricStructurePoolRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.*;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentModels;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcanaRegistry {
   public static final Registry<ArcanaItem> ARCANA_ITEMS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"arcana_item")), Lifecycle.stable());
   public static final Registry<Block> BLOCKS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"block")), Lifecycle.stable());
   public static final Registry<Item> ITEMS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"item")), Lifecycle.stable());
   public static final Registry<PolymerModelData> MODELS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"model")), Lifecycle.stable());
   public static final Registry<AreaEffectTracker> AREA_EFFECTS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"area_effect")), Lifecycle.stable());
   public static final ArrayList<CompendiumEntry> RECOMMENDED_LIST = new ArrayList<>();
   
   // Armor Materials
   // TODO: Set durability?
   public static final ArmorMaterial NON_PROTECTIVE_ARMOR_MATERIAL = new ArmorMaterial(5, Util.make(new EnumMap(EquipmentType.class), map -> {
      map.put(EquipmentType.BOOTS, 0);
      map.put(EquipmentType.LEGGINGS, 0);
      map.put(EquipmentType.CHESTPLATE, 0);
      map.put(EquipmentType.HELMET, 0);
      map.put(EquipmentType.BODY, 0);
   }), 0, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, ItemTags.REPAIRS_LEATHER_ARMOR, EquipmentModels.LEATHER);

   // Registering Banner Recipe
   public static final SpecialCraftingRecipe.SpecialRecipeSerializer<ArcanaShieldDecoratorRecipe> ARCANA_SHIELD_DECORATION_SERIALIZER;
   public static final RecipeType<ArcanaShieldDecoratorRecipe> ARCANA_SHIELD_DECORATION;
   static {
      ARCANA_SHIELD_DECORATION = Registry.register(Registries.RECIPE_TYPE, Identifier.of(MOD_ID, "arcana_shield_decoration"), new RecipeType<ArcanaShieldDecoratorRecipe>() {
         @Override
         public String toString() {return "arcana_shield_decoration_recipe";}
      });
      ARCANA_SHIELD_DECORATION_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "arcana_shield_decoration"), new ArcanaShieldDecoratorRecipe.ShieldRecipeSerializer(ArcanaShieldDecoratorRecipe::new));
   }
   
   // Entities
   public static final EntityType<RunicArrowEntity> RUNIC_ARROW_ENTITY = EntityType.register( "runic_arrow",
         EntityType.Builder.<RunicArrowEntity>create(RunicArrowEntity::new,SpawnGroup.MISC).dimensions(0.5f,0.5f).maxTrackingRange(4).trackingTickInterval(20)
   );
   public static final EntityType<ArbalestArrowEntity> ARBALEST_ARROW_ENTITY = EntityType.register( "arbalest_arrow",
         EntityType.Builder.<ArbalestArrowEntity>create(ArbalestArrowEntity::new, SpawnGroup.MISC).dimensions(0.5f,0.5f).maxTrackingRange(4).trackingTickInterval(20)
   );
   public static final EntityType<StasisPearlEntity> STASIS_PEARL_ENTITY = EntityType.register( "stasis_pearl",
         EntityType.Builder.<StasisPearlEntity>create(StasisPearlEntity::new, SpawnGroup.MISC).dimensions(0.25f, 0.25f).maxTrackingRange(4).trackingTickInterval(10)
   );
   public static final EntityType<DragonWizardEntity> DRAGON_WIZARD_ENTITY = EntityType.register( "dragon_wizard",
         EntityType.Builder.<DragonWizardEntity>create(DragonWizardEntity::new, SpawnGroup.MISC).dimensions(0.6f, 1.95f).maxTrackingRange(8)
   );
   public static final EntityType<DragonPhantomEntity> DRAGON_PHANTOM_ENTITY = EntityType.register( "dragon_phantom",
         EntityType.Builder.<DragonPhantomEntity>create(DragonPhantomEntity::new, SpawnGroup.MISC).dimensions(0.9f, 0.5f).maxTrackingRange(8)
   );
   public static final EntityType<NulConstructEntity> NUL_CONSTRUCT_ENTITY = EntityType.register( "nul_construct",
         EntityType.Builder.<NulConstructEntity>create(NulConstructEntity::new, SpawnGroup.MONSTER).dimensions(0.9f, 3.5f).maxTrackingRange(10).makeFireImmune()
   );
   
   // Status Effects
   public static final RegistryEntry<StatusEffect> DAMAGE_AMP_EFFECT = registerStatusEffect("damage_amp",new DamageAmpEffect());
   public static final RegistryEntry<StatusEffect> GREATER_INVISIBILITY_EFFECT = registerStatusEffect("greater_invisibility",new GreaterInvisibilityEffect());
   public static final RegistryEntry<StatusEffect> GREATER_BLINDNESS_EFFECT = registerStatusEffect("greater_blindness",new GreaterBlindnessEffect());
   public static final RegistryEntry<StatusEffect> DEATH_WARD_EFFECT = registerStatusEffect("death_ward",new DeathWardEffect());
   public static final RegistryEntry<StatusEffect> ENSNAREMENT_EFFECT = registerStatusEffect("ensnarement",new EnsnarementEffect());
   
   // Area Effect Trackers
   public static final AreaEffectTracker SMOKE_ARROW_AREA_EFFECT_TRACKER = registerAreaEffectTracker(new SmokeArrowAreaEffectTracker());
   public static final AreaEffectTracker AFTERSHOCK_AREA_EFFECT_TRACKER = registerAreaEffectTracker(new AftershockAreaEffectTracker());
   public static final AreaEffectTracker ALCHEMICAL_ARROW_AREA_EFFECT_TRACKER = registerAreaEffectTracker(new AlchemicalArrowAreaEffectTracker());
   
   // Normal Items
   public static final Item NEBULOUS_ESSENCE = registerItem("nebulous_essence",new NebulousEssenceItem(new Item.Settings().maxCount(64).fireproof().rarity(Rarity.RARE)
         .component(DataComponentTypes.ITEM_NAME, Text.literal("Nebulous Essence").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
         .component(DataComponentTypes.LORE, NebulousEssenceItem.getDefaultLore())
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item STARDUST = registerItem("stardust",new StardustItem(new Item.Settings().maxCount(64).fireproof().rarity(Rarity.RARE)
         .component(DataComponentTypes.ITEM_NAME, Text.literal("Stardust").formatted(Formatting.YELLOW,Formatting.BOLD))
         .component(DataComponentTypes.LORE, StardustItem.getDefaultLore())
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item ARCANE_NOTES = registerItem("arcane_notes",new ArcaneNotesItem(new Item.Settings().maxCount(1).fireproof().rarity(Rarity.EPIC)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item MUNDANE_ARCANE_PAPER = registerItem("mundane_arcane_paper",new MundaneArcanePaper(new Item.Settings().maxCount(64).fireproof().rarity(Rarity.UNCOMMON)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item EMPOWERED_ARCANE_PAPER = registerItem("empowered_arcane_paper",new EmpoweredArcanePaper(new Item.Settings().maxCount(64).fireproof().rarity(Rarity.UNCOMMON)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item EXOTIC_ARCANE_PAPER = registerItem("exotic_arcane_paper",new ExoticArcanePaper(new Item.Settings().maxCount(64).fireproof().rarity(Rarity.RARE)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item SOVEREIGN_ARCANE_PAPER = registerItem("sovereign_arcane_paper",new SovereignArcanePaper(new Item.Settings().maxCount(64).fireproof().rarity(Rarity.RARE)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item DIVINE_ARCANE_PAPER = registerItem("divine_arcane_paper",new DivineArcanePaper(new Item.Settings().maxCount(64).fireproof().rarity(Rarity.EPIC)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item GRAPHICAL_ITEM = registerItem("graphical_item",new GraphicalItem(new Item.Settings().maxCount(64)));
   
   // 1.0 Items
   public static final ArcanaItem LEADERSHIP_CHARM = ArcanaRegistry.register(new LeadershipCharm());
   public static final ArcanaItem WINGS_OF_ENDERIA = ArcanaRegistry.register(new WingsOfEnderia());
   public static final ArcanaItem ARCANE_TOME = ArcanaRegistry.register(new ArcaneTome());
   public static final ArcanaItem EXOTIC_MATTER = ArcanaRegistry.register(new ExoticMatter());
   public static final ArcanaItem LIGHT_CHARM = ArcanaRegistry.register(new LightCharm());
   public static final ArcanaItem FEASTING_CHARM = ArcanaRegistry.register(new FeastingCharm());
   public static final ArcanaItem SOULSTONE = ArcanaRegistry.register(new Soulstone());
   public static final ArcanaItem ESSENCE_EGG = ArcanaRegistry.register(new EssenceEgg());
   public static final ArcanaItem BRAIN_JAR = ArcanaRegistry.register(new BrainJar());
   public static final ArcanaItem SPAWNER_HARNESS = ArcanaRegistry.register(new SpawnerHarness());
   public static final ArcanaItem SHIELD_OF_FORTITUDE = ArcanaRegistry.register(new ShieldOfFortitude());
   public static final ArcanaItem CONTINUUM_ANCHOR = ArcanaRegistry.register(new ContinuumAnchor());
   public static final ArcanaItem SOJOURNER_BOOTS = ArcanaRegistry.register(new SojournerBoots());
   
   // 1.1 Items
   public static final ArcanaItem PICKAXE_OF_CEPTYUS = ArcanaRegistry.register(new PickaxeOfCeptyus());
   public static final ArcanaItem RUNIC_MATRIX = ArcanaRegistry.register(new RunicMatrix());
   public static final ArcanaItem TEMPORAL_MOMENT = ArcanaRegistry.register(new TemporalMoment());
   public static final ArcanaItem ANCIENT_DOWSING_ROD = ArcanaRegistry.register(new AncientDowsingRod());
   public static final ArcanaItem FELIDAE_CHARM = ArcanaRegistry.register(new FelidaeCharm());
   public static final ArcanaItem MAGNETISM_CHARM = ArcanaRegistry.register(new MagnetismCharm());
   public static final ArcanaItem FRACTAL_SPONGE = ArcanaRegistry.register(new FractalSponge());
   public static final ArcanaItem IGNEOUS_COLLIDER = ArcanaRegistry.register(new IgneousCollider());
   public static final ArcanaItem PEARL_OF_RECALL = ArcanaRegistry.register(new PearlOfRecall());
   public static final ArcanaItem BLINK_ARROWS = ArcanaRegistry.register(new BlinkArrows());
   public static final ArcanaItem SHULKER_CORE = ArcanaRegistry.register(new ShulkerCore());
   public static final ArcanaItem STASIS_PEARL = ArcanaRegistry.register(new StasisPearl());
   public static final ArcanaItem LEVITATION_HARNESS = ArcanaRegistry.register(new LevitationHarness());
   public static final ArcanaItem RUNIC_BOW = ArcanaRegistry.register(new RunicBow());
   public static final ArcanaItem SHADOW_STALKERS_GLAIVE = ArcanaRegistry.register(new ShadowStalkersGlaive());
   
   // 1.3 Items
   public static final ArcanaItem TETHER_ARROWS = ArcanaRegistry.register(new TetherArrows());
   public static final ArcanaItem DETONATION_ARROWS = ArcanaRegistry.register(new DetonationArrows());
   public static final ArcanaItem CONCUSSION_ARROWS = ArcanaRegistry.register(new ConcussionArrows());
   public static final ArcanaItem SMOKE_ARROWS = ArcanaRegistry.register(new SmokeArrows());
   public static final ArcanaItem TELESCOPING_BEACON = ArcanaRegistry.register(new TelescopingBeacon());
   public static final ArcanaItem EXPULSION_ARROWS = ArcanaRegistry.register(new ExpulsionArrows());
   public static final ArcanaItem GRAVITON_ARROWS = ArcanaRegistry.register(new GravitonArrows());
   public static final ArcanaItem SIPHONING_ARROWS = ArcanaRegistry.register(new SiphoningArrows());
   public static final ArcanaItem STORM_ARROWS = ArcanaRegistry.register(new StormArrows());
   public static final ArcanaItem ARCANE_FLAK_ARROWS = ArcanaRegistry.register(new ArcaneFlakArrows());
   public static final ArcanaItem OVERFLOWING_QUIVER = ArcanaRegistry.register(new OverflowingQuiver());
   public static final ArcanaItem CINDERS_CHARM = ArcanaRegistry.register(new CindersCharm());
   public static final ArcanaItem PHOTONIC_ARROWS = ArcanaRegistry.register(new PhotonicArrows());
   public static final ArcanaItem SPAWNER_INFUSER = ArcanaRegistry.register(new SpawnerInfuser());
   public static final ArcanaItem RUNIC_QUIVER = ArcanaRegistry.register(new RunicQuiver());
   
   // 1.4 Items
   public static final ArcanaItem CATALYTIC_MATRIX = ArcanaRegistry.register(new CatalyticMatrix()); // Technically a 2.0 item, but registry order is a thing
   public static final ArcanaItem MUNDANE_CATALYST = ArcanaRegistry.register(new MundaneCatalyst());
   public static final ArcanaItem EMPOWERED_CATALYST = ArcanaRegistry.register(new EmpoweredCatalyst());
   public static final ArcanaItem EXOTIC_CATALYST = ArcanaRegistry.register(new ExoticCatalyst());
   public static final ArcanaItem SOVEREIGN_CATALYST = ArcanaRegistry.register(new SovereignCatalyst());
   public static final ArcanaItem DIVINE_CATALYST = ArcanaRegistry.register(new DivineCatalyst());
   public static final ArcanaItem NUL_MEMENTO = ArcanaRegistry.register(new NulMemento());
   
   // 2.0 Items
   public static final ArcanaItem STARLIGHT_FORGE = ArcanaRegistry.register(new StarlightForge());
   public static final ArcanaItem CHEST_TRANSLOCATOR = ArcanaRegistry.register(new ChestTranslocator());
   public static final ArcanaItem CONTAINMENT_CIRCLET = ArcanaRegistry.register(new ContainmentCirclet());
   public static final ArcanaItem WILD_GROWTH_CHARM = ArcanaRegistry.register(new WildGrowthCharm());
   public static final ArcanaItem EVERLASTING_ROCKET = ArcanaRegistry.register(new EverlastingRocket());
   public static final ArcanaItem TWILIGHT_ANVIL = ArcanaRegistry.register(new TwilightAnvil());
   public static final ArcanaItem RADIANT_FLETCHERY = ArcanaRegistry.register(new RadiantFletchery());
   public static final ArcanaItem ARCANISTS_BELT = ArcanaRegistry.register(new ArcanistsBelt());
   public static final ArcanaItem PLANESHIFTER = ArcanaRegistry.register(new Planeshifter());
   public static final ArcanaItem STELLAR_CORE = ArcanaRegistry.register(new StellarCore());
   public static final ArcanaItem MIDNIGHT_ENCHANTER = ArcanaRegistry.register(new MidnightEnchanter());
   public static final ArcanaItem STORMCALLER_ALTAR = ArcanaRegistry.register(new StormcallerAltar());
   public static final ArcanaItem CELESTIAL_ALTAR = ArcanaRegistry.register(new CelestialAltar());
   public static final ArcanaItem ALCHEMICAL_ARBALEST = ArcanaRegistry.register(new AlchemicalArbalest());
   public static final ArcanaItem ARCANE_SINGULARITY = ArcanaRegistry.register(new ArcaneSingularity());
   public static final ArcanaItem STARPATH_ALTAR = ArcanaRegistry.register(new StarpathAltar());
   
   // 2.1 Items
   public static final ArcanaItem AQUATIC_EVERSOURCE = ArcanaRegistry.register(new AquaticEversource());
   public static final ArcanaItem MAGMATIC_EVERSOURCE = ArcanaRegistry.register(new MagmaticEversource());
   public static final ArcanaItem TRACKING_ARROWS = ArcanaRegistry.register(new TrackingArrows());
   public static final ArcanaItem ENSNAREMENT_ARROWS = ArcanaRegistry.register(new EnsnarementArrows());
   public static final ArcanaItem TOTEM_OF_VENGEANCE = ArcanaRegistry.register(new TotemOfVengeance());
   public static final ArcanaItem TRANSMUTATION_ALTAR = ArcanaRegistry.register(new TransmutationAltar());
   public static final ArcanaItem AEQUALIS_SCIENTIA = ArcanaRegistry.register(new AequalisScientia());
   
   
   // Block Entities
   public static final BlockEntityType<? extends BlockEntity> IGNEOUS_COLLIDER_BLOCK_ENTITY = registerBlockEntity(IGNEOUS_COLLIDER.getId(), FabricBlockEntityTypeBuilder.create(IgneousColliderBlockEntity::new,((ArcanaBlock) IGNEOUS_COLLIDER).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> FRACTAL_SPONGE_BLOCK_ENTITY = registerBlockEntity(FRACTAL_SPONGE.getId(), FabricBlockEntityTypeBuilder.create(FractalSpongeBlockEntity::new,((ArcanaBlock) FRACTAL_SPONGE).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> CONTINUUM_ANCHOR_BLOCK_ENTITY = registerBlockEntity(CONTINUUM_ANCHOR.getId(), FabricBlockEntityTypeBuilder.create(ContinuumAnchorBlockEntity::new,((ArcanaBlock) CONTINUUM_ANCHOR).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> SPAWNER_INFUSER_BLOCK_ENTITY = registerBlockEntity(SPAWNER_INFUSER.getId(), FabricBlockEntityTypeBuilder.create(SpawnerInfuserBlockEntity::new,((ArcanaBlock) SPAWNER_INFUSER).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> CELESTIAL_ALTAR_BLOCK_ENTITY = registerBlockEntity(CELESTIAL_ALTAR.getId(), FabricBlockEntityTypeBuilder.create(CelestialAltarBlockEntity::new,((ArcanaBlock) CELESTIAL_ALTAR).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> STARPATH_ALTAR_BLOCK_ENTITY = registerBlockEntity(STARPATH_ALTAR.getId(), FabricBlockEntityTypeBuilder.create(StarpathAltarBlockEntity::new,((ArcanaBlock) STARPATH_ALTAR).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> STORMCALLER_ALTAR_BLOCK_ENTITY = registerBlockEntity(STORMCALLER_ALTAR.getId(), FabricBlockEntityTypeBuilder.create(StormcallerAltarBlockEntity::new,((ArcanaBlock) STORMCALLER_ALTAR).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> ARCANE_SINGULARITY_BLOCK_ENTITY = registerBlockEntity(ARCANE_SINGULARITY.getId(), FabricBlockEntityTypeBuilder.create(ArcaneSingularityBlockEntity::new,((ArcanaBlock) ARCANE_SINGULARITY).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> MIDNIGHT_ENCHANTER_BLOCK_ENTITY = registerBlockEntity(MIDNIGHT_ENCHANTER.getId(), FabricBlockEntityTypeBuilder.create(MidnightEnchanterBlockEntity::new,((ArcanaBlock) MIDNIGHT_ENCHANTER).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> RADIANT_FLETCHERY_BLOCK_ENTITY = registerBlockEntity(RADIANT_FLETCHERY.getId(), FabricBlockEntityTypeBuilder.create(RadiantFletcheryBlockEntity::new,((ArcanaBlock) RADIANT_FLETCHERY).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> STARLIGHT_FORGE_BLOCK_ENTITY = registerBlockEntity(STARLIGHT_FORGE.getId(), FabricBlockEntityTypeBuilder.create(StarlightForgeBlockEntity::new,((ArcanaBlock) STARLIGHT_FORGE).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> STELLAR_CORE_BLOCK_ENTITY = registerBlockEntity(STELLAR_CORE.getId(), FabricBlockEntityTypeBuilder.create(StellarCoreBlockEntity::new,((ArcanaBlock) STELLAR_CORE).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> TWILIGHT_ANVIL_BLOCK_ENTITY = registerBlockEntity(TWILIGHT_ANVIL.getId(), FabricBlockEntityTypeBuilder.create(TwilightAnvilBlockEntity::new,((ArcanaBlock) TWILIGHT_ANVIL).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> TRANSMUTATION_ALTAR_BLOCK_ENTITY = registerBlockEntity(TRANSMUTATION_ALTAR.getId(), FabricBlockEntityTypeBuilder.create(TransmutationAltarBlockEntity::new,((ArcanaBlock) TRANSMUTATION_ALTAR).getBlock()).build());
   
   
   // Custom Tags
   public static final TagKey<Item> VILLAGE_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"village_research_items"));
   public static final TagKey<Item> WORKSHOP_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"workshop_research_items"));
   public static final TagKey<Item> NUL_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"nul_research_items"));
   public static final TagKey<Item> EQUAYUS_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"equayus_research_items"));
   public static final TagKey<Item> ENDERIA_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"enderia_research_items"));
   
   
   // Loot Functions / Item Modifiers
   public static final LootFunctionType<? extends LootFunction> ARCANE_NOTES_LOOT_FUNCTION = registerLootFunction("arcane_notes", ArcaneNotesLootFunction.CODEC);
   public static final LootFunctionType<? extends LootFunction> ARCANA_BLOCK_ENTITY_LOOT_FUNCTION = registerLootFunction("arcana_block_entity", ArcanaBlockEntityLootFunction.CODEC);
   
   public static void initialize(){
      PolymerResourcePackUtils.addModAssets(MOD_ID);

      for(Map.Entry<RegistryKey<ArcanaItem>, ArcanaItem> entry : ARCANA_ITEMS.getEntrySet()){
         String id = entry.getKey().getValue().getPath();
         ArcanaItem arcanaItem = entry.getValue();
         arcanaItem.initializePrefItem();
         registerItem(id, arcanaItem.getItem());
         //System.out.println("  \"item.arcananovum."+id+"\": \"\",");
         
         if(arcanaItem instanceof ArcanaBlock arcanaBlock){
            registerBlock(id, arcanaBlock.getBlock());
         }
         
         if(arcanaItem.getModels() != null){
            List<String> reused = new ArrayList<>();
            List<String> found = new ArrayList<>();
            for(Pair<Item,String> model: arcanaItem.getModels()){
               String str = model.getRight();
               if(found.contains(str)){
                  reused.add(str);
               }
               found.add(str);
            }
            
            for(Pair<Item,String> model: arcanaItem.getModels()){
               String modelStr = model.getRight();
               Identifier identifier = Identifier.of(MOD_ID,modelStr);
               if(reused.contains(modelStr)){
                  Registry.register(MODELS, Identifier.of(MOD_ID,modelStr+"-"+model.getLeft().getTranslationKey()), PolymerResourcePackUtils.requestModel(model.getLeft(), identifier));
               }else{
                  Registry.register(MODELS, identifier, PolymerResourcePackUtils.requestModel(model.getLeft(), identifier));
               }
            }
         }
      }
      for(Map.Entry<RegistryKey<ArcanaItem>, ArcanaItem> entry : ARCANA_ITEMS.getEntrySet()){
         if(entry.getValue() instanceof MultiblockCore mc){
            mc.loadMultiblock(); // Must be done after all blocks are registered
         }
      }
      
      FabricDefaultAttributeRegistry.register(DRAGON_WIZARD_ENTITY, DragonWizardEntity.createWizardAttributes());
      FabricDefaultAttributeRegistry.register(DRAGON_PHANTOM_ENTITY, DragonPhantomEntity.createPhantomAttributes());
      FabricDefaultAttributeRegistry.register(NUL_CONSTRUCT_ENTITY, NulConstructEntity.createConstructAttributes());
      
      ResearchTasks.registerResearchTasks();
      
      FabricStructurePoolRegistry.registerSimple(Identifier.ofVanilla("village/plains/houses"),Identifier.of(MOD_ID,"village/plains_arcanists_house"),5);
      FabricStructurePoolRegistry.registerSimple(Identifier.ofVanilla("village/desert/houses"),Identifier.of(MOD_ID,"village/desert_arcanists_house"),7);
      FabricStructurePoolRegistry.registerSimple(Identifier.ofVanilla("village/savanna/houses"),Identifier.of(MOD_ID,"village/savanna_arcanists_house"),7);
      FabricStructurePoolRegistry.registerSimple(Identifier.ofVanilla("village/taiga/houses"),Identifier.of(MOD_ID,"village/taiga_arcanists_house"),5);
      FabricStructurePoolRegistry.registerSimple(Identifier.ofVanilla("village/snowy/houses"),Identifier.of(MOD_ID,"village/snowy_arcanists_house"),4);
      
      RECOMMENDED_LIST.addAll(Arrays.asList(
            getCryingObsidianEntry(),
            MundaneArcanePaper.getCompendiumEntry(),
            EmpoweredArcanePaper.getCompendiumEntry(),
            new ArcanaItemCompendiumEntry(ARCANE_TOME),
            new ArcanaItemCompendiumEntry(STARLIGHT_FORGE),
            new ArcanaItemCompendiumEntry(MAGNETISM_CHARM),
            new ArcanaItemCompendiumEntry(TELESCOPING_BEACON),
            new ArcanaItemCompendiumEntry(ANCIENT_DOWSING_ROD),
            new ArcanaItemCompendiumEntry(AQUATIC_EVERSOURCE),
            new ArcanaItemCompendiumEntry(CONTAINMENT_CIRCLET),
            new ArcanaItemCompendiumEntry(CHEST_TRANSLOCATOR),
            new ArcanaItemCompendiumEntry(FRACTAL_SPONGE),
            new ArcanaItemCompendiumEntry(LIGHT_CHARM),
            new ArcanaItemCompendiumEntry(FEASTING_CHARM),
            new ArcanaItemCompendiumEntry(TWILIGHT_ANVIL),
            new ArcanaItemCompendiumEntry(ARCANISTS_BELT),
            new ArcanaItemCompendiumEntry(MAGMATIC_EVERSOURCE),
            new ArcanaItemCompendiumEntry(TEMPORAL_MOMENT),
            new ArcanaItemCompendiumEntry(WILD_GROWTH_CHARM),
            new ArcanaItemCompendiumEntry(PEARL_OF_RECALL),
            new ArcanaItemCompendiumEntry(PLANESHIFTER),
            new ArcanaItemCompendiumEntry(STASIS_PEARL),
            new ArcanaItemCompendiumEntry(MIDNIGHT_ENCHANTER),
            NebulousEssenceItem.getCompendiumEntry(),
            ExoticArcanePaper.getCompendiumEntry(),
            new ArcanaItemCompendiumEntry(EVERLASTING_ROCKET),
            new ArcanaItemCompendiumEntry(BRAIN_JAR),
            new ArcanaItemCompendiumEntry(FELIDAE_CHARM),
            new ArcanaItemCompendiumEntry(SPAWNER_HARNESS),
            new ArcanaItemCompendiumEntry(STELLAR_CORE),
            StardustItem.getCompendiumEntry(),
            SovereignArcanePaper.getCompendiumEntry(),
            new ArcanaItemCompendiumEntry(IGNEOUS_COLLIDER),
            new ArcanaItemCompendiumEntry(CINDERS_CHARM),
            new ArcanaItemCompendiumEntry(SOULSTONE),
            new ArcanaItemCompendiumEntry(ESSENCE_EGG),
            new ArcanaItemCompendiumEntry(STORMCALLER_ALTAR),
            new ArcanaItemCompendiumEntry(CELESTIAL_ALTAR),
            new ArcanaItemCompendiumEntry(STARPATH_ALTAR),
            new ArcanaItemCompendiumEntry(TRANSMUTATION_ALTAR),
            new TransmutationRecipesCompendiumEntry(),
            DivineArcanePaper.getCompendiumEntry(),
            new ArcanaItemCompendiumEntry(EXOTIC_MATTER),
            new ArcanaItemCompendiumEntry(CONTINUUM_ANCHOR),
            new ArcanaItemCompendiumEntry(RUNIC_MATRIX),
            new ArcanaItemCompendiumEntry(RADIANT_FLETCHERY),
            new ArcanaItemCompendiumEntry(OVERFLOWING_QUIVER),
            new ArcanaItemCompendiumEntry(SHADOW_STALKERS_GLAIVE),
            new ArcanaItemCompendiumEntry(SHIELD_OF_FORTITUDE),
            new ArcanaItemCompendiumEntry(SOJOURNER_BOOTS),
            new ArcanaItemCompendiumEntry(TOTEM_OF_VENGEANCE),
            new ArcanaItemCompendiumEntry(ALCHEMICAL_ARBALEST),
            new ArcanaItemCompendiumEntry(RUNIC_BOW),
            new ArcanaItemCompendiumEntry(RUNIC_QUIVER),
            new ArcanaItemCompendiumEntry(TETHER_ARROWS),
            new ArcanaItemCompendiumEntry(DETONATION_ARROWS),
            new ArcanaItemCompendiumEntry(SMOKE_ARROWS),
            new ArcanaItemCompendiumEntry(CONCUSSION_ARROWS),
            new ArcanaItemCompendiumEntry(SIPHONING_ARROWS),
            new ArcanaItemCompendiumEntry(ARCANE_FLAK_ARROWS),
            new ArcanaItemCompendiumEntry(BLINK_ARROWS),
            new ArcanaItemCompendiumEntry(ENSNAREMENT_ARROWS),
            new ArcanaItemCompendiumEntry(EXPULSION_ARROWS),
            new ArcanaItemCompendiumEntry(GRAVITON_ARROWS),
            new ArcanaItemCompendiumEntry(STORM_ARROWS),
            new ArcanaItemCompendiumEntry(TRACKING_ARROWS),
            new ArcanaItemCompendiumEntry(PHOTONIC_ARROWS),
            new ArcanaItemCompendiumEntry(ARCANE_SINGULARITY),
            new ArcanaItemCompendiumEntry(SPAWNER_INFUSER),
            new ArcanaItemCompendiumEntry(SHULKER_CORE),
            new ArcanaItemCompendiumEntry(LEVITATION_HARNESS),
            new ArcanaItemCompendiumEntry(CATALYTIC_MATRIX),
            new ArcanaItemCompendiumEntry(MUNDANE_CATALYST),
            new ArcanaItemCompendiumEntry(EMPOWERED_CATALYST),
            new ArcanaItemCompendiumEntry(EXOTIC_CATALYST),
            new ArcanaItemCompendiumEntry(SOVEREIGN_CATALYST),
            new ArcanaItemCompendiumEntry(DIVINE_CATALYST),
            new ArcanaItemCompendiumEntry(AEQUALIS_SCIENTIA),
            new ArcanaItemCompendiumEntry(NUL_MEMENTO),
            new ArcanaItemCompendiumEntry(WINGS_OF_ENDERIA),
            new ArcanaItemCompendiumEntry(PICKAXE_OF_CEPTYUS),
            new ArcanaItemCompendiumEntry(LEADERSHIP_CHARM)
      ));
      
      final ItemGroup ARCANA_ITEMS_GROUP = PolymerItemGroupUtils.builder().displayName(Text.translatable("itemGroup.arcana_items")).icon(ARCANE_TOME::getPrefItemNoLore).entries((displayContext, entries) -> {
         RECOMMENDED_LIST.forEach(entry -> {
            if(entry instanceof ArcanaItemCompendiumEntry arcanaEntry){
               ItemStack entryStack = arcanaEntry.getArcanaItem().getPrefItem();
               entryStack.setCount(1);
               entries.add(entryStack);
            }
         });
      }).build();
      final ItemGroup ARCANA_INGREDIENTS_GROUP = PolymerItemGroupUtils.builder().displayName(Text.translatable("itemGroup.arcana_ingredients")).icon(() -> MiscUtils.removeLore(new ItemStack(SOVEREIGN_ARCANE_PAPER))).entries((displayContext, entries) -> {
         RECOMMENDED_LIST.forEach(entry -> {
            if(entry instanceof IngredientCompendiumEntry ingredientEntry){
               entries.add(new ItemStack(ingredientEntry.getDisplayStack().getItem()));
            }
         });
      }).build();
      
      PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID,"arcana_items"), ARCANA_ITEMS_GROUP);
      PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID,"arcana_ingredients"), ARCANA_INGREDIENTS_GROUP);
   }
   
   public static void onServerInitialize(MinecraftServer server){
      ARCANA_ITEMS.getEntrySet().forEach(entry -> entry.getValue().finalizePrefItem(server));
   }
   
   private static ArcanaItem register(ArcanaItem arcanaItem){
      Registry.register(ARCANA_ITEMS,Identifier.of(MOD_ID, arcanaItem.getId()), arcanaItem);
      return arcanaItem;
   }
   
   private static Item registerItem(String id, Item item) {
      if(item instanceof NormalPolymerItem nmpi){
         if(nmpi.getModels() != null){
            for(Pair<Item,String> model: nmpi.getModels()){
               String modelStr = model.getRight();
               Identifier identifier = Identifier.of(MOD_ID,modelStr);
               Registry.register(MODELS, identifier, PolymerResourcePackUtils.requestModel(model.getLeft(), identifier));
            }
         }
      }
      Identifier identifier = Identifier.of(MOD_ID,id);
      Registry.register(ITEMS, identifier, Registry.register(Registries.ITEM, identifier, item));
      return item;
   }
   
   private static void registerBlock(String id, Block block) {
      Identifier identifier = Identifier.of(MOD_ID,id);
      Registry.register(BLOCKS, identifier, Registry.register(Registries.BLOCK, identifier, block));
   }
   
   public static BlockEntityType<? extends BlockEntity> registerBlockEntity(String id, BlockEntityType<? extends BlockEntity> blockEntityType) {
      Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, id), blockEntityType);
      PolymerBlockUtils.registerBlockEntity(blockEntityType);
      return blockEntityType;
   }
   
   public static <T extends Entity> EntityType<T> registerEntity(String id, EntityType<T> build){
      Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID,id), build);
      PolymerEntityUtils.registerType(build);
      return build;
   }
   
   public static RegistryEntry<StatusEffect> registerStatusEffect(String id, StatusEffect effect){
      return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(MOD_ID,id), effect);
   }
   
   private static AreaEffectTracker registerAreaEffectTracker(AreaEffectTracker tracker){
      Registry.register(AREA_EFFECTS,tracker.getId(),tracker);
      return tracker;
   }
   
   private static LootFunctionType<? extends LootFunction> registerLootFunction(String id, MapCodec<? extends LootFunction> codec){
      return Registry.register(Registries.LOOT_FUNCTION_TYPE, Identifier.of(MOD_ID,id), new LootFunctionType<>(codec));
   }
   
   public static PolymerModelData getModelData(String id){
      return MODELS.get(Identifier.of(MOD_ID,id));
   }
   
   public static ArcanaItem getArcanaItem(String id){
      return ARCANA_ITEMS.get(Identifier.of(MOD_ID,id));
   }
   
   private static IngredientCompendiumEntry getCryingObsidianEntry(){
      ExplainIngredient w = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.LAPIS_COLOR),1,"",false)
            .withName(Text.literal("Water").formatted(Formatting.BLUE))
            .withLore(List.of(Text.literal("Throw the ingredients into water").formatted(Formatting.AQUA)));
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Text.literal("In World Recipe").formatted(Formatting.GRAY,Formatting.BOLD))
            .withLore(List.of(Text.literal("Do this in the World").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient o = new ExplainIngredient(Items.OBSIDIAN,1,"Obsidian")
            .withName(Text.literal("Obsidian").formatted(Formatting.DARK_PURPLE));
      ExplainIngredient r = new ExplainIngredient(Items.REDSTONE,16,"Redstone Dust")
            .withName(Text.literal("Redstone Dust").formatted(Formatting.RED))
            .withLore(List.of(Text.literal("Use Redstone OR Glowstone Dust").formatted(Formatting.GOLD)));
      ExplainIngredient g = new ExplainIngredient(Items.GLOWSTONE_DUST,4,"Glowstone Dust")
            .withName(Text.literal("Glowstone Dust").formatted(Formatting.YELLOW))
            .withLore(List.of(Text.literal("Use Redstone OR Glowstone Dust").formatted(Formatting.GOLD)));
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,r,a,g,a},
            {a,w,o,w,a},
            {a,w,w,w,a},
            {a,a,a,a,a}};
      
      ItemStack displayStack = new ItemStack(Items.CRYING_OBSIDIAN);
      displayStack.set(DataComponentTypes.RARITY,Rarity.UNCOMMON);
      return new IngredientCompendiumEntry(Text.translatable(Items.CRYING_OBSIDIAN.getTranslationKey()), displayStack, new ExplainRecipe(ingredients));
   }
}
