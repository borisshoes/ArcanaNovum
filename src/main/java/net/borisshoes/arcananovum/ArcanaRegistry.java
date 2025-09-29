package net.borisshoes.arcananovum;

import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import net.borisshoes.arcananovum.areaeffects.AftershockAreaEffectTracker;
import net.borisshoes.arcananovum.areaeffects.AlchemicalArrowAreaEffectTracker;
import net.borisshoes.arcananovum.areaeffects.AreaEffectTracker;
import net.borisshoes.arcananovum.areaeffects.SmokeArrowAreaEffectTracker;
import net.borisshoes.arcananovum.blocks.*;
import net.borisshoes.arcananovum.blocks.altars.*;
import net.borisshoes.arcananovum.blocks.forge.*;
import net.borisshoes.arcananovum.callbacks.XPLoginCallback;
import net.borisshoes.arcananovum.callbacks.login.*;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.MultiblockCore;
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
import net.borisshoes.arcananovum.recipes.transmutation.TransmutationRecipes;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ConfigUtils;
import net.borisshoes.arcananovum.world.structures.FabricStructurePoolRegistry;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DamageResistantComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;

import java.util.*;
import java.util.function.UnaryOperator;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcanaRegistry {
   public static final Registry<ArcanaItem> ARCANA_ITEMS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"arcana_item")), Lifecycle.stable());
   public static final Registry<Block> BLOCKS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"block")), Lifecycle.stable());
   public static final Registry<Item> ITEMS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"item")), Lifecycle.stable());
   //public static final Registry<PolymerModelData> MODELS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"model")), Lifecycle.stable());
   public static final Registry<AreaEffectTracker> AREA_EFFECTS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"area_effect")), Lifecycle.stable());
   public static final Registry<ArcanaConfig.ConfigSetting<?>> CONFIG_SETTINGS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"config_settings")), Lifecycle.stable());
   public static final ArrayList<CompendiumEntry> RECOMMENDED_LIST = new ArrayList<>();
   
   // Custom Tags
   public static final TagKey<Item> ALL_ARCANA_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"arcana_items"));
   public static final TagKey<Item> UNSTACKABLE_ARCANA_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"unstackable_arcana_items"));
   public static final TagKey<Item> VILLAGE_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"village_research_items"));
   public static final TagKey<Item> WORKSHOP_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"workshop_research_items"));
   public static final TagKey<Item> NUL_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"nul_research_items"));
   public static final TagKey<Item> EQUAYUS_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"equayus_research_items"));
   public static final TagKey<Item> ENDERIA_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"enderia_research_items"));
   public static final TagKey<Item> NONPROTECTIVE_ARMOR_REPAIR = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"nonprotective_armor_repair"));
   public static final TagKey<Item> ARCANISTS_BELT_SPECIAL_ALLOWED = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"arcanists_belt_special_allowed"));
   public static final TagKey<Item> ARCANISTS_BELT_SPECIAL_DISALLOWED = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"arcanists_belt_special_disallowed"));
   public static final TagKey<Item> FATE_ANCHOR_ENCHANTABLE = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"fate_anchor_enchantable"));
   public static final TagKey<Item> FATE_ANCHOR_UNENCHANTABLE = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"fate_anchor_unenchantable"));
   public static final TagKey<Item> NEODYMIUM_STEALABLE = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"neodymium_stealable"));
   public static final TagKey<Item> FLETCHERY_POTION_ITEMS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"fletchery_potion_items"));
   public static final TagKey<Item> RUNIC_ARROWS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,"runic_arrows"));
   
   public static final TagKey<Block> CEPTYUS_VEIN_MINEABLE = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID,"ceptyus_vein_mineable"));
   
   public static final TagKey<DamageType> NUL_CONSTRUCT_IMMUNE_TO = TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID,"nul_construct_immune_to"));
   public static final TagKey<DamageType> NUL_CONSTRUCT_RESISTANT_TO = TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID,"nul_construct_resistant_to"));
   public static final TagKey<DamageType> NUL_CONSTRUCT_VULNERABLE_TO = TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID,"nul_construct_vulnerable_to"));
   public static final TagKey<DamageType> ARCANA_ITEM_IMMUNE_TO = TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID,"arcana_item_immune_to"));
   public static final TagKey<DamageType> ALLOW_TOTEM_USAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID,"allow_totem_usage"));
   
   public static final TagKey<EntityType<?>> NUL_CONSTRUCT_FRIENDS = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID,"nul_construct_friends"));
   public static final TagKey<EntityType<?>> ESSENCE_EGG_DISALLOWED = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID,"essence_egg_disallowed"));
   public static final TagKey<EntityType<?>> SOULSTONE_DISALLOWED = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID,"soulstone_disallowed"));
   public static final TagKey<EntityType<?>> CONTAINMENT_CIRCLET_DISALLOWED = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID,"containment_circlet_disallowed"));
   public static final TagKey<EntityType<?>> IGNORES_GREATER_INVISIBILITY = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID,"ignores_greater_invisibility"));
   public static final TagKey<EntityType<?>> IGNORES_GREATER_BLINDNESS = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID,"ignores_greater_blindness"));
   public static final TagKey<EntityType<?>> TENBROUS_BONUS_DAMAGE = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID,"tenbrous_bonus_damage"));
   
   public static final TagKey<Enchantment> FATE_ANCHOR_EXCLUSIVE_SET = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID,"exclusive_set/fate_anchor"));
   
   // Enchantments
   public static final RegistryKey<Enchantment> FATE_ANCHOR = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID,"fate_anchor"));
   
   // Registering Banner Recipe
   public static final SpecialCraftingRecipe.SpecialRecipeSerializer<ArcanaShieldDecoratorRecipe> ARCANA_SHIELD_DECORATION_SERIALIZER;
   public static final RecipeType<ArcanaShieldDecoratorRecipe> ARCANA_SHIELD_DECORATION;
   static {
      ARCANA_SHIELD_DECORATION = Registry.register(Registries.RECIPE_TYPE, Identifier.of(MOD_ID, "arcana_shield_decoration"), new RecipeType<ArcanaShieldDecoratorRecipe>(){
         @Override
         public String toString(){return "arcana_shield_decoration_recipe";}
      });
      ARCANA_SHIELD_DECORATION_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "arcana_shield_decoration"), new ArcanaShieldDecoratorRecipe.ShieldRecipeSerializer(ArcanaShieldDecoratorRecipe::new));
   }
   
   public static final RegistryKey<? extends Registry<EquipmentAsset>> EQUIPMENT_ASSET_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("equipment_asset"));
   
   public static final ChunkTicketType ANCHOR_TICKET_TYPE = registerTicketType("arcananovum_anchor", 40L, true, ChunkTicketType.Use.LOADING_AND_SIMULATION);
   
   // Armor Materials
   public static final ArmorMaterial NON_PROTECTIVE_ARMOR_MATERIAL = new ArmorMaterial(1024, Util.make(new EnumMap<>(EquipmentType.class), map -> {
      map.put(EquipmentType.BOOTS, 0);
      map.put(EquipmentType.LEGGINGS, 0);
      map.put(EquipmentType.CHESTPLATE, 0);
      map.put(EquipmentType.HELMET, 0);
      map.put(EquipmentType.BODY, 0);
   }), 1, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0, 0, NONPROTECTIVE_ARMOR_REPAIR, RegistryKey.of(EQUIPMENT_ASSET_REGISTRY_KEY, Identifier.of(MOD_ID,"nonprotective")));
   
   // Entities
   public static final EntityType<RunicArrowEntity> RUNIC_ARROW_ENTITY = registerEntity( "runic_arrow",
         EntityType.Builder.<RunicArrowEntity>create(RunicArrowEntity::new,SpawnGroup.MISC).dimensions(0.5f,0.5f).maxTrackingRange(4).trackingTickInterval(20)
   );
   public static final EntityType<ArbalestArrowEntity> ARBALEST_ARROW_ENTITY = registerEntity( "arbalest_arrow",
         EntityType.Builder.<ArbalestArrowEntity>create(ArbalestArrowEntity::new, SpawnGroup.MISC).dimensions(0.5f,0.5f).maxTrackingRange(4).trackingTickInterval(20)
   );
   public static final EntityType<StasisPearlEntity> STASIS_PEARL_ENTITY = registerEntity( "stasis_pearl",
         EntityType.Builder.<StasisPearlEntity>create(StasisPearlEntity::new, SpawnGroup.MISC).dimensions(0.25f, 0.25f).maxTrackingRange(4).trackingTickInterval(10)
   );
   public static final EntityType<DragonWizardEntity> DRAGON_WIZARD_ENTITY = registerEntity( "dragon_wizard",
         EntityType.Builder.create(DragonWizardEntity::new, SpawnGroup.MISC).dimensions(0.6f, 1.95f).maxTrackingRange(8)
   );
   public static final EntityType<DragonPhantomEntity> DRAGON_PHANTOM_ENTITY = registerEntity( "dragon_phantom",
         EntityType.Builder.create(DragonPhantomEntity::new, SpawnGroup.MISC).dimensions(0.9f, 0.5f).maxTrackingRange(8)
   );
   public static final EntityType<NulConstructEntity> NUL_CONSTRUCT_ENTITY = registerEntity( "nul_construct",
         EntityType.Builder.create(NulConstructEntity::new, SpawnGroup.MISC).dimensions(0.9f, 3.5f).maxTrackingRange(10).makeFireImmune()
   );
   public static final EntityType<NulGuardianEntity> NUL_GUARDIAN_ENTITY = registerEntity( "nul_guardian",
         EntityType.Builder.<NulGuardianEntity>create(NulGuardianEntity::new, SpawnGroup.MISC).dimensions(0.7F, 2.4F).maxTrackingRange(10).makeFireImmune().allowSpawningInside(Blocks.WITHER_ROSE).eyeHeight(2.1F).vehicleAttachment(-0.875F)
   );
   public static final EntityType<SpearOfTenbrousEntity> SPEAR_OF_TENBROUS_ENTITY = registerEntity( "spear_of_tenbrous",
         EntityType.Builder.<SpearOfTenbrousEntity>create(SpearOfTenbrousEntity::new, SpawnGroup.MISC).dropsNothing().dimensions(0.5F, 0.5F).eyeHeight(0.13F).maxTrackingRange(4).trackingTickInterval(20)
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
   
   // Graphics Items
   public static final GraphicalItem.GraphicElement TRANSMUTATION_BOOK = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "transmutation_book"), Items.KNOWLEDGE_BOOK, false));
   public static final GraphicalItem.GraphicElement CASINO_CHIP = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "casino_chip"), Items.DIAMOND, true));
   public static final GraphicalItem.GraphicElement STAR = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "star"), Items.NETHER_STAR, false));
   public static final GraphicalItem.GraphicElement GAS = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "gas"), Items.GRAY_STAINED_GLASS_PANE, false));
   public static final GraphicalItem.GraphicElement PLASMA = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "plasma"), Items.ORANGE_STAINED_GLASS_PANE, false));
   public static final GraphicalItem.GraphicElement BLACK_HOLE = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "black_hole"), Items.ENDER_PEARL, false));
   public static final GraphicalItem.GraphicElement NOVA = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "nova"), Items.BLAZE_POWDER, false));
   public static final GraphicalItem.GraphicElement SUPERNOVA = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "supernova"), Items.MAGMA_CREAM, false));
   public static final GraphicalItem.GraphicElement QUASAR = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "quasar"), Items.ENDER_EYE, false));
   public static final GraphicalItem.GraphicElement PULSAR = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "pulsar"), Items.END_CRYSTAL, false));
   public static final GraphicalItem.GraphicElement NEBULA = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "nebula"), Items.PURPLE_STAINED_GLASS_PANE, false));
   public static final GraphicalItem.GraphicElement PLANET = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.of(MOD_ID, "planet"), Items.HEAVY_CORE, false));
   
   
   // Normal Items
   public static final Item NEBULOUS_ESSENCE = registerItem("nebulous_essence", new NebulousEssenceItem("nebulous_essence", new Item.Settings().maxCount(64).fireproof().rarity(Rarity.RARE)
         .component(DataComponentTypes.LORE, NebulousEssenceItem.getDefaultLore())
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item STARDUST = registerItem("stardust", new StardustItem("stardust", new Item.Settings().maxCount(64).fireproof().rarity(Rarity.RARE)
         .component(DataComponentTypes.LORE, StardustItem.getDefaultLore())
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item ARCANE_NOTES = registerItem("arcane_notes", new ArcaneNotesItem("arcane_notes", new Item.Settings().maxCount(1).fireproof().rarity(Rarity.EPIC)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item MUNDANE_ARCANE_PAPER = registerItem("mundane_arcane_paper", new MundaneArcanePaper("mundane_arcane_paper", new Item.Settings().maxCount(64).fireproof().rarity(Rarity.UNCOMMON)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item EMPOWERED_ARCANE_PAPER = registerItem("empowered_arcane_paper", new EmpoweredArcanePaper("empowered_arcane_paper", new Item.Settings().maxCount(64).fireproof().rarity(Rarity.UNCOMMON)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item EXOTIC_ARCANE_PAPER = registerItem("exotic_arcane_paper", new ExoticArcanePaper("exotic_arcane_paper", new Item.Settings().maxCount(64).fireproof().rarity(Rarity.RARE)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item SOVEREIGN_ARCANE_PAPER = registerItem("sovereign_arcane_paper", new SovereignArcanePaper("sovereign_arcane_paper", new Item.Settings().maxCount(64).fireproof().rarity(Rarity.RARE)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item DIVINE_ARCANE_PAPER = registerItem("divine_arcane_paper", new DivineArcanePaper("divine_arcane_paper", new Item.Settings().maxCount(64).rarity(Rarity.EPIC)
         .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
         .component(DataComponentTypes.DAMAGE_RESISTANT, new DamageResistantComponent(ARCANA_ITEM_IMMUNE_TO)))
   );
   
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
   
   // 3.1 Items
   public static final ArcanaItem BINARY_BLADES = ArcanaRegistry.register(new BinaryBlades());
   public static final ArcanaItem GRAVITON_MAUL = ArcanaRegistry.register(new GravitonMaul());
   public static final ArcanaItem SPEAR_OF_TENBROUS = ArcanaRegistry.register(new SpearOfTenbrous());
   public static final ArcanaItem GREAVES_OF_GAIALTUS = ArcanaRegistry.register(new GreavesOfGaialtus());
   public static final ArcanaItem CLEANSING_CHARM = ArcanaRegistry.register(new CleansingCharm());
   public static final ArcanaItem CETACEA_CHARM = ArcanaRegistry.register(new CetaceaCharm());
   
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
   
   // Loot Functions / Item Modifiers
   public static final LootFunctionType<? extends LootFunction> ARCANE_NOTES_LOOT_FUNCTION = registerLootFunction("arcane_notes", ArcaneNotesLootFunction.CODEC);
   public static final LootFunctionType<? extends LootFunction> ARCANA_BLOCK_ENTITY_LOOT_FUNCTION = registerLootFunction("arcana_block_entity", ArcanaBlockEntityLootFunction.CODEC);
   
   // PlayerAbilityLib Identifiers
   public static final AbilitySource LEVITATION_HARNESS_ABILITY = Pal.getAbilitySource(Identifier.of(MOD_ID, LEVITATION_HARNESS.getId()), AbilitySource.CONSUMABLE);
   public static final AbilitySource DRAGON_TOWER_ABILITY = Pal.getAbilitySource(Identifier.of(MOD_ID, "dragon_tower"), AbilitySource.FREE);
   
   // Login Callbacks
   public static final LoginCallback SHIELD_LOGIN = registerCallback(new ShieldLoginCallback());
   public static final LoginCallback ANCHOR_LOGIN = registerCallback(new AnchorTimeLoginCallback());
   public static final LoginCallback COLLIDER_LOGIN = registerCallback(new ColliderLoginCallback());
   public static final LoginCallback XP_LOGIN = registerCallback(new XPLoginCallback());
   public static final LoginCallback ACHIEVEMENT_LOGIN = registerCallback(new AchievementLoginCallback());
   public static final LoginCallback MAX_HP_LOGIN = registerCallback(new MaxHealthLoginCallback());
   public static final LoginCallback VENGEANCE_LOGIN = registerCallback(new VengeanceTotemLoginCallback());
   
   // Config Settings
   public static final ArcanaConfig.ConfigSetting<?> DO_CONCENTRATION_DAMAGE = registerConfigSetting(new ArcanaConfig.NormalConfigSetting<>(new ConfigUtils.BooleanConfigValue("doConcentrationDamage", true, "Whether players are damaged for going over their concentration limit",
         new ConfigUtils.Command("Do Concentration Damage is %s", "Do Concentration Damage is now %s"))));
   public static final ArcanaConfig.ConfigSetting<?> ANNOUNCE_ACHIEVEMENTS = registerConfigSetting(new ArcanaConfig.NormalConfigSetting<>(new ConfigUtils.BooleanConfigValue("announceAchievements", true, "Whether it is announced in chat when players complete achievements, reach levels, or craft new items",
         new ConfigUtils.Command("Announce Achievements is %s", "Announce Achievements is now %s"))));
   public static final ArcanaConfig.ConfigSetting<?> INGREDIENT_REDUCTION = registerConfigSetting(new ArcanaConfig.NormalConfigSetting<>(new ConfigUtils.IntegerConfigValue("ingredientReduction", 1, new ConfigUtils.IntegerConfigValue.IntLimits(1,64), "The divisor for recipe ingredient costs",
         new ConfigUtils.Command("Recipe ingredient counts are divided by %s", "Recipe ingredient count will now be divided by %s"))));
   
   public static final ArcanaConfig.ConfigSetting<?> STORMCALLER_ALTAR_ACTIVATE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"StormcallerAltarActivate","activating the altar"));
   public static final ArcanaConfig.ConfigSetting<?> CELESTIAL_ALTAR_ACTIVATE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"CelestialAltarActivate","activating the altar"));
   public static final ArcanaConfig.ConfigSetting<?> STARPATH_ALTAR_ACTIVATE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"StarpathAltarActivate","activating the altar"));
   public static final ArcanaConfig.ConfigSetting<?> IGNEOUS_COLLIDER_PRODUCE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"IgneousColliderProduce","producing 1 obsidian"));
   public static final ArcanaConfig.ConfigSetting<?> CONTINUUM_ANCHOR_PER_MINUTE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(15,"ContinuumAnchorPerMinute","having an anchor active for one minute"));
   public static final ArcanaConfig.ConfigSetting<?> FRACTAL_SPONGE_ABSORB_BLOCK = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1,"FractalSpongeAbsorbBlock","absorbing a single block of fluid"));
   public static final ArcanaConfig.ConfigSetting<?> WINGS_OF_ENDERIA_FLY = registerConfigSetting(new ArcanaConfig.XPConfigSetting(2,"WingsOfEnderiaFly","flying for 1 tick"));
   public static final ArcanaConfig.ConfigSetting<?> RADIANT_FLETCHERY_TIP_ARROWS = registerConfigSetting(new ArcanaConfig.XPConfigSetting(100,"RadiantFletcheryTipArrows","making tipped arrows"));
   public static final ArcanaConfig.ConfigSetting<?> STELLAR_CORE_SALVAGE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(100,"StellarCoreSalvage","salvaging an item"));
   public static final ArcanaConfig.ConfigSetting<?> STELLAR_CORE_SMELT = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1,"StellarCoreSmelt","smelting ore"));
   public static final ArcanaConfig.ConfigSetting<?> PICKAXE_OF_CEPTYUS_MINE_BLOCK = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1,"PickaxeOfCeptyusMineBlock","mining a block"));
   public static final ArcanaConfig.ConfigSetting<?> PICKAXE_OF_CEPTYUS_VEIN_MINE_BLOCK = registerConfigSetting(new ArcanaConfig.XPConfigSetting(5,"PickaxeOfCeptyusVeinMineBlock","vein mining an ore"));
   public static final ArcanaConfig.ConfigSetting<?> RUNIC_ARROW_SHOOT = registerConfigSetting(new ArcanaConfig.XPConfigSetting(50,"RunicArrowShoot","shooting a runic arrow"));
   public static final ArcanaConfig.ConfigSetting<?> SHIELD_OF_FORTITUDE_ABSORB_DAMAGE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"ShieldOfFortitudeAbsorbDamage","absorbing 1 damage point"));
   public static final ArcanaConfig.ConfigSetting<?> TOTEM_OF_VENGEANCE_ACTIVATE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"TotemOfVengeanceActivate","activating the totem"));
   public static final ArcanaConfig.ConfigSetting<?> TOTEM_OF_VENGEANCE_SURVIVE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(4000,"TotemOfVengeanceSurvive","getting revenge"));
   public static final ArcanaConfig.ConfigSetting<?> DAMAGE_AMP_PER_10 = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"DamageAmpPer10","amplifying 10 damage"));
   public static final ArcanaConfig.ConfigSetting<?> DAMAGE_AMP_CAP = registerConfigSetting(new ArcanaConfig.XPConfigSetting(200,"DamageAmpCap","amplifying damage",true));
   public static final ArcanaConfig.ConfigSetting<?> ALCHEMICAL_ARBALEST_SHOOT = registerConfigSetting(new ArcanaConfig.XPConfigSetting(25,"AlchemicalArbalestShoot","shooting an alchemical arrow"));
   public static final ArcanaConfig.ConfigSetting<?> TRANSMUTATION_ALTAR_TRANSMUTE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(100,"TransmutationAltarTransmute","successfully transmuting"));
   public static final ArcanaConfig.ConfigSetting<?> TRANSMUTATION_ALTAR_TRANSMUTE_PER_ITEM = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"TransmutationAltarTransmutePerItem","transmuting 1 item"));
   public static final ArcanaConfig.ConfigSetting<?> MIDNIGHT_ENCHANTER_DISENCHANT_PER_ESSENCE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"MidnightEnchanterDisenchantPerEssence","getting 1 essence from disenchanting"));
   public static final ArcanaConfig.ConfigSetting<?> STARDUST_INFUSION_PER_STARDUST = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"StardustInfusionPerStardust","spending 1 stardust on infusion"));
   public static final ArcanaConfig.ConfigSetting<?> TWILIGHT_ANVIL_PER_10 = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"TwilightAnvilPer10","spending 10 xp in the anvil"));
   public static final ArcanaConfig.ConfigSetting<?> TWILIGHT_ANVIL_CAP = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"TwilightAnvilCap","spending xp in the anvil",true));
   public static final ArcanaConfig.ConfigSetting<?> ANCIENT_DOWSING_ROD_PER_DEBRIS = registerConfigSetting(new ArcanaConfig.XPConfigSetting(15,"AncientDowsingRodPerDebris","finding 1 debris"));
   public static final ArcanaConfig.ConfigSetting<?> ANCIENT_DOWSING_ROD_CAP = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1500,"AncientDowsingRodCap","finding debris",true));
   public static final ArcanaConfig.ConfigSetting<?> AQUATIC_EVERSOURCE_USE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1,"AquaticEversourceUse","making water with the eversource"));
   public static final ArcanaConfig.ConfigSetting<?> MAGMATIC_EVERSOURCE_USE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(25,"MagmaticEversourceUse","making lava with the eversource"));
   public static final ArcanaConfig.ConfigSetting<?> BRAIN_JAR_MEND_PER_XP = registerConfigSetting(new ArcanaConfig.XPConfigSetting(5,"BrainJarMendPerXp","spending 1 xp to mend items"));
   public static final ArcanaConfig.ConfigSetting<?> CHEST_TRANSLOCATOR_USE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(25,"ChestTranslocatorUse","using the translocator"));
   public static final ArcanaConfig.ConfigSetting<?> CONTAINMENT_CIRCLET_USE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"ContainmentCircletUse","using the circlet"));
   public static final ArcanaConfig.ConfigSetting<?> ESSENCE_EGG_SPAWN = registerConfigSetting(new ArcanaConfig.XPConfigSetting(2500,"EssenceEggSpawn","spawning a creature"));
   public static final ArcanaConfig.ConfigSetting<?> ESSENCE_EGG_CONVERT = registerConfigSetting(new ArcanaConfig.XPConfigSetting(500,"EssenceEggConvert","converting a spawner"));
   public static final ArcanaConfig.ConfigSetting<?> EVERLASTING_ROCKET_USE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(100,"EverlastingRocketUse","using the rocket"));
   public static final ArcanaConfig.ConfigSetting<?> LEVITATION_HARNESS_PER_SECOND = registerConfigSetting(new ArcanaConfig.XPConfigSetting(25,"LevitationHarnessPerSecond","flying for one second"));
   public static final ArcanaConfig.ConfigSetting<?> NUL_MEMENTO_DEALLOCATE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(50000,"NulMementoDeallocate","deallocating your skill points"));
   public static final ArcanaConfig.ConfigSetting<?> NUL_MEMENTO_PROTECT = registerConfigSetting(new ArcanaConfig.XPConfigSetting(5000,"NulMementoProtect","having the memento protect you from death"));
   public static final ArcanaConfig.ConfigSetting<?> PEARL_OF_RECALL_USE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"PearlOfRecallUse","using the pearl"));
   public static final ArcanaConfig.ConfigSetting<?> PLANESHIFTER_USE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"PlaneshifterUse","using the shifter"));
   public static final ArcanaConfig.ConfigSetting<?> STASIS_PEARL_USE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(250,"StasisPearlUse","using the pearl"));
   public static final ArcanaConfig.ConfigSetting<?> QUIVER_REFILL = registerConfigSetting(new ArcanaConfig.XPConfigSetting(50,"QuiverRefill","having a quiver refill an arrow"));
   public static final ArcanaConfig.ConfigSetting<?> SHADOW_STALKERS_GLAIVE_STALK = registerConfigSetting(new ArcanaConfig.XPConfigSetting(500,"ShadowStalkersGlaiveStalk","using the glaive's stalk ability"));
   public static final ArcanaConfig.ConfigSetting<?> SHADOW_STALKERS_GLAIVE_BLINK = registerConfigSetting(new ArcanaConfig.XPConfigSetting(100,"ShadowStalkersGlaiveBlink","using the glaive's blink ability"));
   public static final ArcanaConfig.ConfigSetting<?> SHULKER_CORE_PER_SOUL = registerConfigSetting(new ArcanaConfig.XPConfigSetting(50,"ShulkerCorePerSoul","using a soul in the shulker core"));
   public static final ArcanaConfig.ConfigSetting<?> SOJOURNERS_BOOTS_RUN_PER_SECOND = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"SojournersBootsRunPerSecond","running for one second"));
   public static final ArcanaConfig.ConfigSetting<?> SOULSTONE_LEVEL_UP_PER_SOUL = registerConfigSetting(new ArcanaConfig.XPConfigSetting(50,"SoulstoneLevelUpPerSoul","levelling up the soulstone per soul"));
   public static final ArcanaConfig.ConfigSetting<?> SPAWNER_HARNESS_USE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(20000,"SpawnerHarnessUse","using the harness"));
   public static final ArcanaConfig.ConfigSetting<?> TELESCOPING_BEACON_PER_BLOCK = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1,"TelescopingBeaconPerBlock","placing the beacon per block in the base"));
   public static final ArcanaConfig.ConfigSetting<?> CINDERS_CHARM_IGNITE_BLOCK = registerConfigSetting(new ArcanaConfig.XPConfigSetting(15,"CindersCharmIgniteBlock","igniting a block"));
   public static final ArcanaConfig.ConfigSetting<?> CINDERS_CHARM_IGNITE_TNT = registerConfigSetting(new ArcanaConfig.XPConfigSetting(50,"CindersCharmIgniteTNT","igniting a block of TNT"));
   public static final ArcanaConfig.ConfigSetting<?> CINDERS_CHARM_IGNITE_ENTITY = registerConfigSetting(new ArcanaConfig.XPConfigSetting(15,"CindersCharmIgniteEntity","igniting an entity"));
   public static final ArcanaConfig.ConfigSetting<?> CINDERS_CHARM_IGNITE_CREEPER = registerConfigSetting(new ArcanaConfig.XPConfigSetting(50,"CindersCharmIgniteCreeper","igniting a creeper"));
   public static final ArcanaConfig.ConfigSetting<?> CINDERS_CHARM_LIGHT_BLOCK = registerConfigSetting(new ArcanaConfig.XPConfigSetting(15,"CindersCharmLightBlock","lighting a block (ex. candle)"));
   public static final ArcanaConfig.ConfigSetting<?> CINDERS_CHARM_SMELT_PER_CINDER = registerConfigSetting(new ArcanaConfig.XPConfigSetting(4,"CindersCharmSmeltPerCinder","spending a cinder on smelting"));
   public static final ArcanaConfig.ConfigSetting<?> CINDERS_CHARM_CONE_PER_TARGET = registerConfigSetting(new ArcanaConfig.XPConfigSetting(5,"CindersCharmConePerTarget","hitting a target with cone of flame"));
   public static final ArcanaConfig.ConfigSetting<?> CINDERS_CHARM_PYROBLAST_PER_TARGET = registerConfigSetting(new ArcanaConfig.XPConfigSetting(5,"CindersCharmPyroblastPerTarget","hitting a target with pyroblast"));
   public static final ArcanaConfig.ConfigSetting<?> CINDERS_CHARM_WEB_PER_TARGET = registerConfigSetting(new ArcanaConfig.XPConfigSetting(5,"CindersCharmWebPerTarget","hitting a target with web of fire"));
   public static final ArcanaConfig.ConfigSetting<?> FEASTING_CHARM_PER_FOOD_VALUE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(50,"FeastingCharmPerFoodValue","getting fed per hunger point"));
   public static final ArcanaConfig.ConfigSetting<?> LIGHT_CHARM_NOVA_PER_LIGHT = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1,"LightCharmNovaPerLight","placing a light with radiant nova"));
   public static final ArcanaConfig.ConfigSetting<?> LIGHT_CHARM_AUTOMATIC = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"LightCharmAutomatic","automatically placing a light"));
   public static final ArcanaConfig.ConfigSetting<?> LIGHT_CHARM_MANUAL = registerConfigSetting(new ArcanaConfig.XPConfigSetting(15,"LightCharmManual","manually placing a light"));
   public static final ArcanaConfig.ConfigSetting<?> MAGNETISM_CHARM_PER_ITEM = registerConfigSetting(new ArcanaConfig.XPConfigSetting(2,"MagnetismCharmPerItem","attracting an item with the active ability"));
   public static final ArcanaConfig.ConfigSetting<?> MAGNETISM_CHARM_CAP = registerConfigSetting(new ArcanaConfig.XPConfigSetting(25,"MagnetismCharmPerItemCap","attracting items with the active ability",true));
   public static final ArcanaConfig.ConfigSetting<?> WILD_GROWTH_CHARM_PER_MATURE_CROP = registerConfigSetting(new ArcanaConfig.XPConfigSetting(25,"WildGrowthCharm","growing a crop to maturity"));
   public static final ArcanaConfig.ConfigSetting<?> WILD_GROWTH_CHARM_PER_REAPED_CROP = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1,"WildGrowthCharmPerReapedCrop","reaping and replanting a crop"));
   public static final ArcanaConfig.ConfigSetting<?> WILD_GROWTH_CHARM_PASSIVE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1,"WildGrowthCharmPassive","having the charm affect nearby blocks"));
   public static final ArcanaConfig.ConfigSetting<?> FELIDAE_CHARM_FALL = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"FelidaeCharmFall","negating 1 point of fall damage"));
   public static final ArcanaConfig.ConfigSetting<?> FELIDAE_CHARM_FALL_CAP = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"FelidaeCharmFallCap","negating fall damage",true));
   public static final ArcanaConfig.ConfigSetting<?> FELIDAE_CHARM_SCARE_PHANTOM = registerConfigSetting(new ArcanaConfig.XPConfigSetting(2,"FelidaeCharmScarePhantom","scaring a phantom"));
   public static final ArcanaConfig.ConfigSetting<?> FELIDAE_CHARM_SCARE_CREEPER = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"FelidaeCharmScareCreeper","scaring a creeper"));
   public static final ArcanaConfig.ConfigSetting<?> WINGS_OF_ENDERIA_CUSHION = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"WingsOfEnderiaCushion","negating 1 point of kinetic damage"));
   public static final ArcanaConfig.ConfigSetting<?> WINGS_OF_ENDERIA_CUSHION_CAP = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"WingsOfEnderiaCushionCap","negating kinetic damage",true));
   public static final ArcanaConfig.ConfigSetting<?> AEQUALIS_SCIENTIA_CATALYST_TRANSMUTE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"AequalisScientiaCatalystTransmute","transmuting catalysts"));
   public static final ArcanaConfig.ConfigSetting<?> AEQUALIS_SCIENTIA_SKILL_TRANSMUTE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(1000,"AequalisScientiaSkillTransmute","transmuting skill points"));
   public static final ArcanaConfig.ConfigSetting<?> AEQUALIS_SCIENTIA_ATTUNED_TRANSMUTE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(500,"AequalisScientiaAttunedTransmute","using the attuned transmutation"));
   public static final ArcanaConfig.ConfigSetting<?> BINARY_BLADES_MAX_ENERGY_PER_SECOND = registerConfigSetting(new ArcanaConfig.XPConfigSetting(50,"BinaryBladesMaxEnergyPerSecond","keeping the blades at max energy for one second"));
   public static final ArcanaConfig.ConfigSetting<?> CETACEA_CHARM_PER_SECOND = registerConfigSetting(new ArcanaConfig.XPConfigSetting(10,"CetaceaCharmPerSecond","having the charm active for one second"));
   public static final ArcanaConfig.ConfigSetting<?> CLEANSING_CHARM_CLEANSE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(150,"CleansingCharmCleanse","cleansing an effect with the charm"));
   public static final ArcanaConfig.ConfigSetting<?> GRAVITON_MAUL_IMPACT_DAMAGE_PER_10 = registerConfigSetting(new ArcanaConfig.XPConfigSetting(5,"GravitonMaulImpactDamage","dealing 10 points of impact damage"));
   public static final ArcanaConfig.ConfigSetting<?> GRAVITON_MAUL_IMPACT_DAMAGE_CAP = registerConfigSetting(new ArcanaConfig.XPConfigSetting(250,"GravitonMaulImpactDamageCap","dealing impact damage",true));
   public static final ArcanaConfig.ConfigSetting<?> GREAVES_OF_GAIALTUS_REFILL_BLOCK_PER_10 = registerConfigSetting(new ArcanaConfig.XPConfigSetting(5,"GreavesOfGaialtusRefillBlock","refilling your hand with 10 blocks"));
   public static final ArcanaConfig.ConfigSetting<?> SPEAR_OF_TENBROUS_IMPALE = registerConfigSetting(new ArcanaConfig.XPConfigSetting(50,"SpearOfTenbrousImpale","impaling a creature with the spear"));
   
   public static void initialize(){
      PolymerResourcePackUtils.addModAssets(MOD_ID);
      FabricDefaultAttributeRegistry.register(DRAGON_WIZARD_ENTITY, DragonWizardEntity.createWizardAttributes());
      FabricDefaultAttributeRegistry.register(DRAGON_PHANTOM_ENTITY, DragonPhantomEntity.createPhantomAttributes());
      FabricDefaultAttributeRegistry.register(NUL_CONSTRUCT_ENTITY, NulConstructEntity.createConstructAttributes());
      FabricDefaultAttributeRegistry.register(NUL_GUARDIAN_ENTITY, NulGuardianEntity.createGuardianAttributes());
      
      for(Map.Entry<RegistryKey<ArcanaItem>, ArcanaItem> entry : ARCANA_ITEMS.getEntrySet()){
         String id = entry.getKey().getValue().getPath();
         ArcanaItem arcanaItem = entry.getValue();
         arcanaItem.initializePrefItem();
         registerItem(id, arcanaItem.getItem());
         
         if(arcanaItem instanceof ArcanaBlock arcanaBlock){
            registerBlock(id, arcanaBlock.getBlock());
         }
      }
      for(Map.Entry<RegistryKey<ArcanaItem>, ArcanaItem> entry : ARCANA_ITEMS.getEntrySet()){
         if(entry.getValue() instanceof MultiblockCore mc){
            mc.loadMultiblock(); // Must be done after all blocks are registered
         }
      }
      
      ResearchTasks.registerResearchTasks();
      
      FabricStructurePoolRegistry.registerSimple(Identifier.ofVanilla("village/plains/houses"),Identifier.of(MOD_ID,"village/plains_arcanists_house"),4);
      FabricStructurePoolRegistry.registerSimple(Identifier.ofVanilla("village/desert/houses"),Identifier.of(MOD_ID,"village/desert_arcanists_house"),16);
      FabricStructurePoolRegistry.registerSimple(Identifier.ofVanilla("village/savanna/houses"),Identifier.of(MOD_ID,"village/savanna_arcanists_house"),14);
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
            new ArcanaItemCompendiumEntry(CETACEA_CHARM),
            new ArcanaItemCompendiumEntry(LIGHT_CHARM),
            new ArcanaItemCompendiumEntry(TWILIGHT_ANVIL),
            new ArcanaItemCompendiumEntry(MIDNIGHT_ENCHANTER),
            NebulousEssenceItem.getCompendiumEntry(),
            ExoticArcanePaper.getCompendiumEntry(),
            new ArcanaItemCompendiumEntry(FELIDAE_CHARM),
            new ArcanaItemCompendiumEntry(FEASTING_CHARM),
            new ArcanaItemCompendiumEntry(ARCANISTS_BELT),
            new ArcanaItemCompendiumEntry(MAGMATIC_EVERSOURCE),
            new ArcanaItemCompendiumEntry(TEMPORAL_MOMENT),
            new ArcanaItemCompendiumEntry(WILD_GROWTH_CHARM),
            new ArcanaItemCompendiumEntry(PEARL_OF_RECALL),
            new ArcanaItemCompendiumEntry(PLANESHIFTER),
            new ArcanaItemCompendiumEntry(STASIS_PEARL),
            new ArcanaItemCompendiumEntry(EVERLASTING_ROCKET),
            new ArcanaItemCompendiumEntry(BRAIN_JAR),
            new ArcanaItemCompendiumEntry(CLEANSING_CHARM),
            new ArcanaItemCompendiumEntry(STELLAR_CORE),
            StardustItem.getCompendiumEntry(),
            SovereignArcanePaper.getCompendiumEntry(),
            new ArcanaItemCompendiumEntry(IGNEOUS_COLLIDER),
            new ArcanaItemCompendiumEntry(SPAWNER_HARNESS),
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
            new ArcanaItemCompendiumEntry(BINARY_BLADES),
            new ArcanaItemCompendiumEntry(GRAVITON_MAUL),
            new ArcanaItemCompendiumEntry(SHADOW_STALKERS_GLAIVE),
            new ArcanaItemCompendiumEntry(SHIELD_OF_FORTITUDE),
            new ArcanaItemCompendiumEntry(SOJOURNER_BOOTS),
            new ArcanaItemCompendiumEntry(TOTEM_OF_VENGEANCE),
            new ArcanaItemCompendiumEntry(ALCHEMICAL_ARBALEST),
            new ArcanaItemCompendiumEntry(RUNIC_BOW),
            new ArcanaItemCompendiumEntry(RUNIC_QUIVER),
            new ArcanaItemCompendiumEntry(TETHER_ARROWS),
            new ArcanaItemCompendiumEntry(BLINK_ARROWS),
            new ArcanaItemCompendiumEntry(ARCANE_FLAK_ARROWS),
            new ArcanaItemCompendiumEntry(CONCUSSION_ARROWS),
            new ArcanaItemCompendiumEntry(SMOKE_ARROWS),
            new ArcanaItemCompendiumEntry(DETONATION_ARROWS),
            new ArcanaItemCompendiumEntry(SIPHONING_ARROWS),
            new ArcanaItemCompendiumEntry(STORM_ARROWS),
            new ArcanaItemCompendiumEntry(GRAVITON_ARROWS),
            new ArcanaItemCompendiumEntry(EXPULSION_ARROWS),
            new ArcanaItemCompendiumEntry(PHOTONIC_ARROWS),
            new ArcanaItemCompendiumEntry(ENSNAREMENT_ARROWS),
            new ArcanaItemCompendiumEntry(TRACKING_ARROWS),
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
            new ArcanaItemCompendiumEntry(SPEAR_OF_TENBROUS),
            new ArcanaItemCompendiumEntry(PICKAXE_OF_CEPTYUS),
            new ArcanaItemCompendiumEntry(GREAVES_OF_GAIALTUS),
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
      final ItemGroup ARCANA_INGREDIENTS_GROUP = PolymerItemGroupUtils.builder().displayName(Text.translatable("itemGroup.arcana_ingredients")).icon(() -> MinecraftUtils.removeLore(new ItemStack(SOVEREIGN_ARCANE_PAPER))).entries((displayContext, entries) -> {
         RECOMMENDED_LIST.forEach(entry -> {
            if(entry instanceof IngredientCompendiumEntry ingredientEntry){
               entries.add(new ItemStack(ingredientEntry.getDisplayStack().getItem()));
            }
         });
      }).build();
      
      PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID,"arcana_items"), ARCANA_ITEMS_GROUP);
      PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID,"arcana_ingredients"), ARCANA_INGREDIENTS_GROUP);
   }
   
   public static void onServerStarted(MinecraftServer server){
      ARCANA_ITEMS.getEntrySet().forEach(entry -> entry.getValue().finalizePrefItem(server));
      TransmutationRecipes.initializeTransmutationRecipes(server);
   }
   
   private static ArcanaItem register(ArcanaItem arcanaItem){
      Registry.register(ARCANA_ITEMS,Identifier.of(MOD_ID, arcanaItem.getId()), arcanaItem);
      return arcanaItem;
   }
   
   private static Item registerItem(String id, Item item){
      Identifier identifier = Identifier.of(MOD_ID,id);
      Registry.register(ITEMS, identifier, Registry.register(Registries.ITEM, identifier, item));
      return item;
   }
   
   private static void registerBlock(String id, Block block){
      Identifier identifier = Identifier.of(MOD_ID,id);
      Registry.register(BLOCKS, identifier, Registry.register(Registries.BLOCK, identifier, block));
   }
   
   public static BlockEntityType<? extends BlockEntity> registerBlockEntity(String id, BlockEntityType<? extends BlockEntity> blockEntityType){
      Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, id), blockEntityType);
      PolymerBlockUtils.registerBlockEntity(blockEntityType);
      return blockEntityType;
   }
   
   public static <T extends Entity> EntityType<T> registerEntity(String id, EntityType.Builder<T> builder){
      Identifier identifier = Identifier.of(MOD_ID,id);
      EntityType<T> entityType = builder.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, identifier));
      Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID,id), entityType);
      PolymerEntityUtils.registerType(entityType);
      return entityType;
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
   
   private static ArcanaConfig.ConfigSetting<?> registerConfigSetting(ArcanaConfig.ConfigSetting<?> setting){
      Registry.register(CONFIG_SETTINGS,Identifier.of(MOD_ID,setting.getId()),setting);
      return setting;
   }
   
   private static LoginCallback registerCallback(LoginCallback callback){
      return Registry.register(BorisLib.LOGIN_CALLBACKS,callback.getId(),callback);
   }
   
   private static ChunkTicketType registerTicketType(String id, long expiryTicks, boolean persist, ChunkTicketType.Use use) {
      return Registry.register(Registries.TICKET_TYPE, id, new ChunkTicketType(expiryTicks, persist, use));
   }
   
   private static <T> ComponentType<T> registerEnchantmentEffectComponent(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
      return Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Identifier.of(MOD_ID,id), ((ComponentType.Builder)builderOperator.apply(ComponentType.builder())).build());
   }
   
   public static Identifier arcanaIdentifier(String id){
      return Identifier.of(MOD_ID,id);
   }
   
   public static ArcanaItem getArcanaItem(String id){
      return ARCANA_ITEMS.get(Identifier.of(MOD_ID,id));
   }
   
   private static IngredientCompendiumEntry getCryingObsidianEntry(){
      ExplainIngredient w = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LAPIS_COLOR),1,"",false)
            .withName(Text.literal("Water").formatted(Formatting.BLUE))
            .withLore(List.of(Text.literal("Throw the ingredients into water").formatted(Formatting.AQUA)));
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
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
