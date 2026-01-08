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
import net.borisshoes.arcananovum.datagen.DefaultRecipeGenerator;
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
import net.borisshoes.arcananovum.lootfunctions.FoundArcanaItemLootFunction;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.vanilla.AquaticEversourceFillRecipe;
import net.borisshoes.arcananovum.recipes.vanilla.ArcanaShieldDecoratorRecipe;
import net.borisshoes.arcananovum.recipes.vanilla.MagmaticEversourceFillRecipe;
import net.borisshoes.arcananovum.recipes.vanilla.WaystoneCleanseRecipe;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.world.structures.FabricStructurePoolRegistry;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.borisshoes.borislib.config.ConfigSetting;
import net.borisshoes.borislib.config.IConfigSetting;
import net.borisshoes.borislib.config.values.BooleanConfigValue;
import net.borisshoes.borislib.config.values.IntConfigValue;
import net.borisshoes.borislib.config.values.StringConfigValue;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcanaRegistry {
   public static final Registry<ArcanaItem> ARCANA_ITEMS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"arcana_item")), Lifecycle.stable());
   public static final Registry<Block> BLOCKS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"block")), Lifecycle.stable());
   public static final Registry<Item> ITEMS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"item")), Lifecycle.stable());
   //public static final Registry<PolymerModelData> MODELS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"model")), Lifecycle.stable());
   public static final Registry<AreaEffectTracker> AREA_EFFECTS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"area_effect")), Lifecycle.stable());
   public static final Registry<IConfigSetting<?>> CONFIG_SETTINGS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"config_settings")), Lifecycle.stable());
   public static final ArrayList<CompendiumEntry> RECOMMENDED_LIST = new ArrayList<>();
   
   // Custom Tags
   public static final TagKey<Item> ALL_ARCANA_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"arcana_items"));
   public static final TagKey<Item> UNSTACKABLE_ARCANA_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"unstackable_arcana_items"));
   public static final TagKey<Item> VILLAGE_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"village_research_items"));
   public static final TagKey<Item> WORKSHOP_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"workshop_research_items"));
   public static final TagKey<Item> NUL_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"nul_research_items"));
   public static final TagKey<Item> EQUAYUS_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"equayus_research_items"));
   public static final TagKey<Item> ENDERIA_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"enderia_research_items"));
   public static final TagKey<Item> NONPROTECTIVE_ARMOR_REPAIR = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"nonprotective_armor_repair"));
   public static final TagKey<Item> ARCANISTS_BELT_SPECIAL_ALLOWED = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"arcanists_belt_special_allowed"));
   public static final TagKey<Item> ARCANISTS_BELT_SPECIAL_DISALLOWED = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"arcanists_belt_special_disallowed"));
   public static final TagKey<Item> FATE_ANCHOR_ENCHANTABLE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"fate_anchor_enchantable"));
   public static final TagKey<Item> FATE_ANCHOR_UNENCHANTABLE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"fate_anchor_unenchantable"));
   public static final TagKey<Item> NEODYMIUM_STEALABLE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"neodymium_stealable"));
   public static final TagKey<Item> FLETCHERY_POTION_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"fletchery_potion_items"));
   public static final TagKey<Item> RUNIC_ARROWS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,"runic_arrows"));
   
   public static final TagKey<Block> CEPTYUS_VEIN_MINEABLE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID,"ceptyus_vein_mineable"));
   
   public static final TagKey<DamageType> NUL_CONSTRUCT_IMMUNE_TO = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"nul_construct_immune_to"));
   public static final TagKey<DamageType> NUL_CONSTRUCT_RESISTANT_TO = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"nul_construct_resistant_to"));
   public static final TagKey<DamageType> NUL_CONSTRUCT_VULNERABLE_TO = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"nul_construct_vulnerable_to"));
   public static final TagKey<DamageType> ARCANA_ITEM_IMMUNE_TO = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"arcana_item_immune_to"));
   public static final TagKey<DamageType> ALLOW_TOTEM_USAGE = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"allow_totem_usage"));
   
   public static final TagKey<EntityType<?>> NUL_CONSTRUCT_FRIENDS = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"nul_construct_friends"));
   public static final TagKey<EntityType<?>> ESSENCE_EGG_DISALLOWED = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"essence_egg_disallowed"));
   public static final TagKey<EntityType<?>> SOULSTONE_DISALLOWED = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"soulstone_disallowed"));
   public static final TagKey<EntityType<?>> CONTAINMENT_CIRCLET_DISALLOWED = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"containment_circlet_disallowed"));
   public static final TagKey<EntityType<?>> IGNORES_GREATER_INVISIBILITY = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"ignores_greater_invisibility"));
   public static final TagKey<EntityType<?>> IGNORES_GREATER_BLINDNESS = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"ignores_greater_blindness"));
   public static final TagKey<EntityType<?>> TENBROUS_BONUS_DAMAGE = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"tenbrous_bonus_damage"));
   public static final TagKey<EntityType<?>> STARPATH_ALLOWED = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,"starpath_allowed"));
   
   public static final TagKey<Enchantment> FATE_ANCHOR_EXCLUSIVE_SET = TagKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(MOD_ID,"exclusive_set/fate_anchor"));
   
   // Enchantments
   public static final ResourceKey<Enchantment> FATE_ANCHOR = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(MOD_ID,"fate_anchor"));
   
   // Registering Banner Recipe
   public static final CustomRecipe.Serializer<ArcanaShieldDecoratorRecipe> ARCANA_SHIELD_DECORATION_SERIALIZER;
   public static final RecipeType<ArcanaShieldDecoratorRecipe> ARCANA_SHIELD_DECORATION;
   static {
      ARCANA_SHIELD_DECORATION = Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "arcana_shield_decoration"), new RecipeType<ArcanaShieldDecoratorRecipe>(){
         @Override
         public String toString(){return "arcana_shield_decoration_recipe";}
      });
      ARCANA_SHIELD_DECORATION_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, "arcana_shield_decoration"), new ArcanaShieldDecoratorRecipe.ShieldRecipeSerializer(ArcanaShieldDecoratorRecipe::new));
   }
   
   // Registering Eversource Recipes
   public static final CustomRecipe.Serializer<AquaticEversourceFillRecipe> AQUATIC_EVERSOURCE_FILL_RECIPE_SERIALIZER;
   public static final RecipeType<AquaticEversourceFillRecipe> AQUATIC_EVERSOURCE_FILL_RECIPE;
   public static final CustomRecipe.Serializer<MagmaticEversourceFillRecipe> MAGMATIC_EVERSOURCE_FILL_RECIPE_SERIALIZER;
   public static final RecipeType<MagmaticEversourceFillRecipe> MAGMATIC_EVERSOURCE_FILL_RECIPE;
   static {
      AQUATIC_EVERSOURCE_FILL_RECIPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "aquatic_eversource_fill"), new RecipeType<AquaticEversourceFillRecipe>(){
         @Override
         public String toString(){return "aquatic_eversource_fill_recipe";}
      });
      AQUATIC_EVERSOURCE_FILL_RECIPE_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, "aquatic_eversource_fill"), new AquaticEversourceFillRecipe.AquaticEversourceRecipeSerializer(AquaticEversourceFillRecipe::new));
      
      MAGMATIC_EVERSOURCE_FILL_RECIPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "magmatic_eversource_fill"), new RecipeType<MagmaticEversourceFillRecipe>(){
         @Override
         public String toString(){return "magmatic_eversource_fill_recipe";}
      });
      MAGMATIC_EVERSOURCE_FILL_RECIPE_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, "magmatic_eversource_fill"), new MagmaticEversourceFillRecipe.MagmaticEversourceRecipeSerializer(MagmaticEversourceFillRecipe::new));
   }
   
   // Registering Waystone Cook Recipe
   public static final AbstractCookingRecipe.Serializer<WaystoneCleanseRecipe> WAYSTONE_CLEANSE_RECIPE_SERIALIZER;
   public static final RecipeType<WaystoneCleanseRecipe> WAYSTONE_CLEANSE_RECIPE;
   static {
      WAYSTONE_CLEANSE_RECIPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "waystone_cleanse"), new RecipeType<WaystoneCleanseRecipe>(){
         @Override
         public String toString(){return "waystone_cleanse";}
      });
      WAYSTONE_CLEANSE_RECIPE_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, "waystone_cleanse"), new WaystoneCleanseRecipe.WaystoneCleanseRecipeSerializer(WaystoneCleanseRecipe::new));
   }
   
   public static final ResourceKey<? extends Registry<EquipmentAsset>> EQUIPMENT_ASSET_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("equipment_asset"));
   
   public static final TicketType ANCHOR_TICKET_TYPE = registerTicketType("arcananovum_anchor", 40L, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_PERSIST | TicketType.FLAG_KEEP_DIMENSION_ACTIVE);
   
   // Armor Materials
   public static final ArmorMaterial NON_PROTECTIVE_ARMOR_MATERIAL = new ArmorMaterial(1024, Util.make(new EnumMap<>(ArmorType.class), map -> {
      map.put(ArmorType.BOOTS, 0);
      map.put(ArmorType.LEGGINGS, 0);
      map.put(ArmorType.CHESTPLATE, 0);
      map.put(ArmorType.HELMET, 0);
      map.put(ArmorType.BODY, 0);
   }), 1, SoundEvents.ARMOR_EQUIP_LEATHER, 0, 0, NONPROTECTIVE_ARMOR_REPAIR, ResourceKey.create(EQUIPMENT_ASSET_REGISTRY_KEY, Identifier.fromNamespaceAndPath(MOD_ID,"nonprotective")));
   
   // Entities
   public static final EntityType<RunicArrowEntity> RUNIC_ARROW_ENTITY = registerEntity( "runic_arrow",
         EntityType.Builder.<RunicArrowEntity>of(RunicArrowEntity::new, MobCategory.MISC).sized(0.5f,0.5f).clientTrackingRange(4).updateInterval(20)
   );
   public static final EntityType<ArbalestArrowEntity> ARBALEST_ARROW_ENTITY = registerEntity( "arbalest_arrow",
         EntityType.Builder.<ArbalestArrowEntity>of(ArbalestArrowEntity::new, MobCategory.MISC).sized(0.5f,0.5f).clientTrackingRange(4).updateInterval(20)
   );
   public static final EntityType<StasisPearlEntity> STASIS_PEARL_ENTITY = registerEntity( "stasis_pearl",
         EntityType.Builder.<StasisPearlEntity>of(StasisPearlEntity::new, MobCategory.MISC).sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10)
   );
   public static final EntityType<DragonWizardEntity> DRAGON_WIZARD_ENTITY = registerEntity( "dragon_wizard",
         EntityType.Builder.of(DragonWizardEntity::new, MobCategory.MISC).sized(0.6f, 1.95f).clientTrackingRange(8)
   );
   public static final EntityType<DragonPhantomEntity> DRAGON_PHANTOM_ENTITY = registerEntity( "dragon_phantom",
         EntityType.Builder.of(DragonPhantomEntity::new, MobCategory.MISC).sized(0.9f, 0.5f).clientTrackingRange(8)
   );
   public static final EntityType<NulConstructEntity> NUL_CONSTRUCT_ENTITY = registerEntity( "nul_construct",
         EntityType.Builder.of(NulConstructEntity::new, MobCategory.MISC).sized(0.9f, 3.5f).clientTrackingRange(10).fireImmune()
   );
   public static final EntityType<NulGuardianEntity> NUL_GUARDIAN_ENTITY = registerEntity( "nul_guardian",
         EntityType.Builder.<NulGuardianEntity>of(NulGuardianEntity::new, MobCategory.MISC).sized(0.7F, 2.4F).clientTrackingRange(10).fireImmune().immuneTo(Blocks.WITHER_ROSE).eyeHeight(2.1F).ridingOffset(-0.875F)
   );
   public static final EntityType<SpearOfTenbrousEntity> SPEAR_OF_TENBROUS_ENTITY = registerEntity( "spear_of_tenbrous",
         EntityType.Builder.<SpearOfTenbrousEntity>of(SpearOfTenbrousEntity::new, MobCategory.MISC).noLootTable().sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20)
   );
   
   // Status Effects
   public static final Holder<MobEffect> DAMAGE_AMP_EFFECT = registerStatusEffect("damage_amp",new DamageAmpEffect());
   public static final Holder<MobEffect> GREATER_INVISIBILITY_EFFECT = registerStatusEffect("greater_invisibility",new GreaterInvisibilityEffect());
   public static final Holder<MobEffect> GREATER_BLINDNESS_EFFECT = registerStatusEffect("greater_blindness",new GreaterBlindnessEffect());
   public static final Holder<MobEffect> DEATH_WARD_EFFECT = registerStatusEffect("death_ward",new DeathWardEffect());
   public static final Holder<MobEffect> ENSNAREMENT_EFFECT = registerStatusEffect("ensnarement",new EnsnarementEffect());
   
   // Area Effect Trackers
   public static final AreaEffectTracker SMOKE_ARROW_AREA_EFFECT_TRACKER = registerAreaEffectTracker(new SmokeArrowAreaEffectTracker());
   public static final AreaEffectTracker AFTERSHOCK_AREA_EFFECT_TRACKER = registerAreaEffectTracker(new AftershockAreaEffectTracker());
   public static final AreaEffectTracker ALCHEMICAL_ARROW_AREA_EFFECT_TRACKER = registerAreaEffectTracker(new AlchemicalArrowAreaEffectTracker());
   
   // Graphics Items
   public static final GraphicalItem.GraphicElement TRANSMUTATION_BOOK = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "transmutation_book"), Items.KNOWLEDGE_BOOK, false));
   public static final GraphicalItem.GraphicElement CASINO_CHIP = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "casino_chip"), Items.DIAMOND, true));
   public static final GraphicalItem.GraphicElement STAR = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "star"), Items.NETHER_STAR, false));
   public static final GraphicalItem.GraphicElement GAS = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "gas"), Items.GRAY_STAINED_GLASS_PANE, false));
   public static final GraphicalItem.GraphicElement PLASMA = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "plasma"), Items.ORANGE_STAINED_GLASS_PANE, false));
   public static final GraphicalItem.GraphicElement BLACK_HOLE = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "black_hole"), Items.ENDER_PEARL, false));
   public static final GraphicalItem.GraphicElement NOVA = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "nova"), Items.BLAZE_POWDER, false));
   public static final GraphicalItem.GraphicElement SUPERNOVA = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "supernova"), Items.MAGMA_CREAM, false));
   public static final GraphicalItem.GraphicElement QUASAR = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "quasar"), Items.ENDER_EYE, false));
   public static final GraphicalItem.GraphicElement PULSAR = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "pulsar"), Items.END_CRYSTAL, false));
   public static final GraphicalItem.GraphicElement NEBULA = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "nebula"), Items.PURPLE_STAINED_GLASS_PANE, false));
   public static final GraphicalItem.GraphicElement PLANET = BorisLib.registerGraphicItem(new GraphicalItem.GraphicElement(Identifier.fromNamespaceAndPath(MOD_ID, "planet"), Items.HEAVY_CORE, false));
   
   
   // Normal Items
   public static final Item NEBULOUS_ESSENCE = registerItem("nebulous_essence", new NebulousEssenceItem("nebulous_essence", new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.RARE)
         .component(DataComponents.LORE, NebulousEssenceItem.getDefaultLore())
         .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item STARDUST = registerItem("stardust", new StardustItem("stardust", new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.RARE)
         .component(DataComponents.LORE, StardustItem.getDefaultLore())
         .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item ARCANE_NOTES = registerItem("arcane_notes", new ArcaneNotesItem("arcane_notes", new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)
         .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item MUNDANE_ARCANE_PAPER = registerItem("mundane_arcane_paper", new MundaneArcanePaper("mundane_arcane_paper", new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.UNCOMMON)
         .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item EMPOWERED_ARCANE_PAPER = registerItem("empowered_arcane_paper", new EmpoweredArcanePaper("empowered_arcane_paper", new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.UNCOMMON)
         .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item EXOTIC_ARCANE_PAPER = registerItem("exotic_arcane_paper", new ExoticArcanePaper("exotic_arcane_paper", new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.RARE)
         .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item SOVEREIGN_ARCANE_PAPER = registerItem("sovereign_arcane_paper", new SovereignArcanePaper("sovereign_arcane_paper", new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.RARE)
         .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
   );
   public static final Item DIVINE_ARCANE_PAPER = registerItem("divine_arcane_paper", new DivineArcanePaper("divine_arcane_paper", new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)
         .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
         .component(DataComponents.DAMAGE_RESISTANT, new DamageResistant(ARCANA_ITEM_IMMUNE_TO)))
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
   
   // 3.2 Items
   public static final ArcanaItem WAYSTONE = ArcanaRegistry.register(new Waystone());
   
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
   public static final LootItemFunctionType<? extends LootItemFunction> ARCANE_NOTES_LOOT_FUNCTION = registerLootFunction("arcane_notes", ArcaneNotesLootFunction.CODEC);
   public static final LootItemFunctionType<? extends LootItemFunction> ARCANA_BLOCK_ENTITY_LOOT_FUNCTION = registerLootFunction("arcana_block_entity", ArcanaBlockEntityLootFunction.CODEC);
   public static final LootItemFunctionType<? extends LootItemFunction> FOUND_ARCANA_ITEM_LOOT_FUNCTION = registerLootFunction("found_arcana_item", FoundArcanaItemLootFunction.CODEC);
   
   // PlayerAbilityLib Identifiers
   public static final AbilitySource LEVITATION_HARNESS_ABILITY = Pal.getAbilitySource(Identifier.fromNamespaceAndPath(MOD_ID, LEVITATION_HARNESS.getId()), AbilitySource.CONSUMABLE);
   public static final AbilitySource DRAGON_TOWER_ABILITY = Pal.getAbilitySource(Identifier.fromNamespaceAndPath(MOD_ID, "dragon_tower"), AbilitySource.FREE);
   
   // Login Callbacks
   public static final LoginCallback SHIELD_LOGIN = registerCallback(new ShieldLoginCallback());
   public static final LoginCallback ANCHOR_LOGIN = registerCallback(new AnchorTimeLoginCallback());
   public static final LoginCallback COLLIDER_LOGIN = registerCallback(new ColliderLoginCallback());
   public static final LoginCallback XP_LOGIN = registerCallback(new XPLoginCallback());
   public static final LoginCallback ACHIEVEMENT_LOGIN = registerCallback(new AchievementLoginCallback());
   public static final LoginCallback MAX_HP_LOGIN = registerCallback(new MaxHealthLoginCallback());
   public static final LoginCallback VENGEANCE_LOGIN = registerCallback(new VengeanceTotemLoginCallback());
   
   // Config Settings
   public static final IConfigSetting<?> RECIPE_FOLDER = registerConfigSetting(new ConfigSetting<>(
         new StringConfigValue("recipeFolder","default", "default","classic")));
   public static final IConfigSetting<?> DO_CONCENTRATION_DAMAGE = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("doConcentrationDamage", true)));
   public static final IConfigSetting<?> ANNOUNCE_ACHIEVEMENTS = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("announceAchievements", true)));
   public static final IConfigSetting<?> RESEARCH_ENABLED = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("researchEnabled", true)));
   public static final IConfigSetting<?> STARDUST_PARTICLES = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("stardustParticles", true)));
   
   public static final IConfigSetting<?> XP_STORMCALLER_ALTAR_ACTIVATE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpStormcallerAltarActivate", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CELESTIAL_ALTAR_ACTIVATE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCelestialAltarActivate", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_STARPATH_ALTAR_ACTIVATE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpStarpathAltarActivate", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_IGNEOUS_COLLIDER_PRODUCE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpIgneousColliderProduce", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CONTINUUM_ANCHOR_PER_MINUTE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpContinuumAnchorPerMinute", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FRACTAL_SPONGE_ABSORB_BLOCK = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpFractalSpongeAbsorbBlock", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WINGS_OF_ENDERIA_FLY = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpWingsOfEnderiaFly", 2, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_RADIANT_FLETCHERY_TIP_ARROWS = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpRadiantFletcheryTipArrows", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_STELLAR_CORE_SALVAGE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpStellarCoreSalvage", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_STELLAR_CORE_SMELT = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpStellarCoreSmelt", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_PICKAXE_OF_CEPTYUS_MINE_BLOCK = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpPickaxeOfCeptyusMineBlock", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_PICKAXE_OF_CEPTYUS_VEIN_MINE_BLOCK = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpPickaxeOfCeptyusVeinMineBlock", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_RUNIC_ARROW_SHOOT = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpRunicArrowShoot", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SHIELD_OF_FORTITUDE_ABSORB_DAMAGE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpShieldOfFortitudeAbsorbDamage", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TOTEM_OF_VENGEANCE_ACTIVATE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpTotemOfVengeanceActivate", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TOTEM_OF_VENGEANCE_SURVIVE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpTotemOfVengeanceSurvive", 4000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_DAMAGE_AMP_PER_10 = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpDamageAmpPer10", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_DAMAGE_AMP_CAP = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpDamageAmpCap", 200, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ALCHEMICAL_ARBALEST_SHOOT = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpAlchemicalArbalestShoot", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TRANSMUTATION_ALTAR_TRANSMUTE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpTransmutationAltarTransmute", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TRANSMUTATION_ALTAR_TRANSMUTE_PER_ITEM = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpTransmutationAltarTransmutePerItem", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_MIDNIGHT_ENCHANTER_DISENCHANT_PER_ESSENCE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpMidnightEnchanterDisenchantPerEssence", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_STARDUST_INFUSION_PER_STARDUST = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpStardustInfusionPerStardust", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TWILIGHT_ANVIL_PER_10 = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpTwilightAnvilPer10", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TWILIGHT_ANVIL_CAP = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpTwilightAnvilCap", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ANCIENT_DOWSING_ROD_PER_DEBRIS = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpAncientDowsingRodPerDebris", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ANCIENT_DOWSING_ROD_CAP = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpAncientDowsingRodCap", 1500, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_AQUATIC_EVERSOURCE_USE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpAquaticEversourceUse", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_MAGMATIC_EVERSOURCE_USE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpMagmaticEversourceUse", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_BRAIN_JAR_MEND_PER_XP = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpBrainJarMendPerXp", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CHEST_TRANSLOCATOR_USE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpChestTranslocatorUse", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CONTAINMENT_CIRCLET_USE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpContainmentCircletUse", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ESSENCE_EGG_SPAWN = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpEssenceEggSpawn", 2500, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ESSENCE_EGG_CONVERT = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpEssenceEggConvert", 500, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_EVERLASTING_ROCKET_USE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpEverlastingRocketUse", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_LEVITATION_HARNESS_PER_SECOND = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpLevitationHarnessPerSecond", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_NUL_MEMENTO_DEALLOCATE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpNulMementoDeallocate", 50000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_NUL_MEMENTO_PROTECT = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpNulMementoProtect", 5000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_PEARL_OF_RECALL_USE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpPearlOfRecallUse", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_PLANESHIFTER_USE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpPlaneshifterUse", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_STASIS_PEARL_USE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpStasisPearlUse", 250, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_QUIVER_REFILL = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpQuiverRefill", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SHADOW_STALKERS_GLAIVE_STALK = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpShadowStalkersGlaiveStalk", 500, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SHADOW_STALKERS_GLAIVE_BLINK = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpShadowStalkersGlaiveBlink", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SHULKER_CORE_PER_SOUL = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpShulkerCorePerSoul", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SOJOURNERS_BOOTS_RUN_PER_SECOND = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpSojournersBootsRunPerSecond", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SOULSTONE_LEVEL_UP_PER_SOUL = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpSoulstoneLevelUpPerSoul", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SPAWNER_HARNESS_USE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpSpawnerHarnessUse", 20000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TELESCOPING_BEACON_PER_BLOCK = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpTelescopingBeaconPerBlock", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_IGNITE_BLOCK = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmIgniteBlock", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_IGNITE_TNT = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmIgniteTnt", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_IGNITE_ENTITY = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmIgniteEntity", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_IGNITE_CREEPER = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmIgniteCreeper", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_LIGHT_BLOCK = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmLightBlock", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_SMELT_PER_CINDER = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmSmeltPerCinder", 4, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_CONE_PER_TARGET = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmConePerTarget", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_PYROBLAST_PER_TARGET = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmPyroblastPerTarget", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_WEB_PER_TARGET = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmWebPerTarget", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FEASTING_CHARM_PER_FOOD_VALUE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpFeastingCharmPerFoodValue", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_LIGHT_CHARM_NOVA_PER_LIGHT = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpLightCharmNovaPerLight", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_LIGHT_CHARM_AUTOMATIC = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpLightCharmAutomatic", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_LIGHT_CHARM_MANUAL = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpLightCharmManual", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_MAGNETISM_CHARM_PER_ITEM = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpMagnetismCharmPerItem", 2, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_MAGNETISM_CHARM_CAP = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpMagnetismCharmCap", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WILD_GROWTH_CHARM_PER_MATURE_CROP = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpWildGrowthCharmPerMatureCrop", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WILD_GROWTH_CHARM_PER_REAPED_CROP = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpWildGrowthCharmPerReapedCrop", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WILD_GROWTH_CHARM_PASSIVE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpWildGrowthCharmPassive", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FELIDAE_CHARM_FALL = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpFelidaeCharmFall", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FELIDAE_CHARM_FALL_CAP = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpFelidaeCharmFallCap", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FELIDAE_CHARM_SCARE_PHANTOM = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpFelidaeCharmScarePhantom", 2, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FELIDAE_CHARM_SCARE_CREEPER = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpFelidaeCharmScareCreeper", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WINGS_OF_ENDERIA_CUSHION = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpWingsOfEnderiaCushion", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WINGS_OF_ENDERIA_CUSHION_CAP = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpWingsOfEnderiaCushionCap", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_AEQUALIS_SCIENTIA_CATALYST_TRANSMUTE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpAequalisScientiaTransmuteCatalyst", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_AEQUALIS_SCIENTIA_SKILL_TRANSMUTE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpAequalisScientiaTransmuteSkill", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_AEQUALIS_SCIENTIA_ATTUNED_TRANSMUTE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpAequalisScientiaTransmuteAttuned", 500, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_BINARY_BLADES_MAX_ENERGY_PER_SECOND = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpBinaryBladesMaxEnergyPerSecond", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CETACEA_CHARM_PER_SECOND = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCetaceaCharmPerSecond", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CLEANSING_CHARM_CLEANSE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpCleansingCharmCleanse", 150, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_GRAVITON_MAUL_IMPACT_DAMAGE_PER_10 = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpGravitonMaulImpactDamagePer10", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_GRAVITON_MAUL_IMPACT_DAMAGE_CAP = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpGravitonMaulImpactDamageCap", 250, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_GREAVES_OF_GAIALTUS_REFILL_BLOCK_PER_10 = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpGreavesOfGaialtusRefillBlockPer10", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SPEAR_OF_TENBROUS_IMPALE = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("xpSpearOfTenbrousImpale", 50, new IntConfigValue.IntLimits(0))));
   
   public static void initialize(){
      PolymerResourcePackUtils.addModAssets(MOD_ID);
      FabricDefaultAttributeRegistry.register(DRAGON_WIZARD_ENTITY, DragonWizardEntity.createWizardAttributes());
      FabricDefaultAttributeRegistry.register(DRAGON_PHANTOM_ENTITY, DragonPhantomEntity.createPhantomAttributes());
      FabricDefaultAttributeRegistry.register(NUL_CONSTRUCT_ENTITY, NulConstructEntity.createConstructAttributes());
      FabricDefaultAttributeRegistry.register(NUL_GUARDIAN_ENTITY, NulGuardianEntity.createGuardianAttributes());
      
      for(Map.Entry<ResourceKey<ArcanaItem>, ArcanaItem> entry : ARCANA_ITEMS.entrySet()){
         String id = entry.getKey().identifier().getPath();
         ArcanaItem arcanaItem = entry.getValue();
         arcanaItem.initializePrefItem();
         registerItem(id, arcanaItem.getItem());
         
         if(arcanaItem instanceof ArcanaBlock arcanaBlock){
            registerBlock(id, arcanaBlock.getBlock());
         }
      }
      for(Map.Entry<ResourceKey<ArcanaItem>, ArcanaItem> entry : ARCANA_ITEMS.entrySet()){
         if(entry.getValue() instanceof MultiblockCore mc){
            mc.loadMultiblock(); // Must be done after all blocks are registered
         }
      }
      
      ResearchTasks.registerResearchTasks();
      
      FabricStructurePoolRegistry.registerSimple(Identifier.withDefaultNamespace("village/plains/houses"), Identifier.fromNamespaceAndPath(MOD_ID,"village/plains_arcanists_house"),4);
      FabricStructurePoolRegistry.registerSimple(Identifier.withDefaultNamespace("village/desert/houses"), Identifier.fromNamespaceAndPath(MOD_ID,"village/desert_arcanists_house"),16);
      FabricStructurePoolRegistry.registerSimple(Identifier.withDefaultNamespace("village/savanna/houses"), Identifier.fromNamespaceAndPath(MOD_ID,"village/savanna_arcanists_house"),14);
      FabricStructurePoolRegistry.registerSimple(Identifier.withDefaultNamespace("village/taiga/houses"), Identifier.fromNamespaceAndPath(MOD_ID,"village/taiga_arcanists_house"),5);
      FabricStructurePoolRegistry.registerSimple(Identifier.withDefaultNamespace("village/snowy/houses"), Identifier.fromNamespaceAndPath(MOD_ID,"village/snowy_arcanists_house"),4);
      
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
            new ArcanaItemCompendiumEntry(WAYSTONE),
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
      
      final CreativeModeTab ARCANA_ITEMS_GROUP = PolymerItemGroupUtils.builder().title(Component.translatable("itemGroup.arcana_items")).icon(ARCANE_TOME::getPrefItemNoLore).displayItems((displayContext, entries) -> {
         RECOMMENDED_LIST.forEach(entry -> {
            if(entry instanceof ArcanaItemCompendiumEntry arcanaEntry){
               ItemStack entryStack = arcanaEntry.getArcanaItem().getPrefItem();
               entryStack.setCount(1);
               entries.accept(entryStack);
            }
         });
      }).build();
      final CreativeModeTab ARCANA_INGREDIENTS_GROUP = PolymerItemGroupUtils.builder().title(Component.translatable("itemGroup.arcana_ingredients")).icon(() -> MinecraftUtils.removeLore(new ItemStack(SOVEREIGN_ARCANE_PAPER))).displayItems((displayContext, entries) -> {
         RECOMMENDED_LIST.forEach(entry -> {
            if(entry instanceof IngredientCompendiumEntry ingredientEntry){
               entries.accept(new ItemStack(ingredientEntry.getDisplayStack().getItem()));
            }
         });
      }).build();
      
      PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.fromNamespaceAndPath(MOD_ID,"arcana_items"), ARCANA_ITEMS_GROUP);
      PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.fromNamespaceAndPath(MOD_ID,"arcana_ingredients"), ARCANA_INGREDIENTS_GROUP);
   }
   
   public static void onServerStarted(MinecraftServer server){
      ARCANA_ITEMS.entrySet().forEach(entry -> entry.getValue().finalizePrefItem(server));
      DefaultRecipeGenerator.generateBuiltInRecipes();
      RecipeManager.refreshRecipes(server);
   }
   
   private static ArcanaItem register(ArcanaItem arcanaItem){
      Registry.register(ARCANA_ITEMS, Identifier.fromNamespaceAndPath(MOD_ID, arcanaItem.getId()), arcanaItem);
      return arcanaItem;
   }
   
   private static Item registerItem(String id, Item item){
      Identifier identifier = Identifier.fromNamespaceAndPath(MOD_ID,id);
      Registry.register(ITEMS, identifier, Registry.register(BuiltInRegistries.ITEM, identifier, item));
      return item;
   }
   
   private static void registerBlock(String id, Block block){
      Identifier identifier = Identifier.fromNamespaceAndPath(MOD_ID,id);
      Registry.register(BLOCKS, identifier, Registry.register(BuiltInRegistries.BLOCK, identifier, block));
   }
   
   public static BlockEntityType<? extends BlockEntity> registerBlockEntity(String id, BlockEntityType<? extends BlockEntity> blockEntityType){
      Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, id), blockEntityType);
      PolymerBlockUtils.registerBlockEntity(blockEntityType);
      return blockEntityType;
   }
   
   public static <T extends Entity> EntityType<T> registerEntity(String id, EntityType.Builder<T> builder){
      Identifier identifier = Identifier.fromNamespaceAndPath(MOD_ID,id);
      EntityType<T> entityType = builder.build(ResourceKey.create(Registries.ENTITY_TYPE, identifier));
      Registry.register(BuiltInRegistries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,id), entityType);
      PolymerEntityUtils.registerType(entityType);
      return entityType;
   }
   
   public static Holder<MobEffect> registerStatusEffect(String id, MobEffect effect){
      return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(MOD_ID,id), effect);
   }
   
   private static AreaEffectTracker registerAreaEffectTracker(AreaEffectTracker tracker){
      Registry.register(AREA_EFFECTS,tracker.getId(),tracker);
      return tracker;
   }
   
   private static LootItemFunctionType<? extends LootItemFunction> registerLootFunction(String id, MapCodec<? extends LootItemFunction> codec){
      return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,id), new LootItemFunctionType<>(codec));
   }
   
   private static IConfigSetting<?> registerConfigSetting(IConfigSetting<?> setting){
      Registry.register(CONFIG_SETTINGS, Identifier.fromNamespaceAndPath(MOD_ID,setting.getId()),setting);
      return setting;
   }
   
   private static LoginCallback registerCallback(LoginCallback callback){
      return Registry.register(BorisLib.LOGIN_CALLBACKS,callback.getId(),callback);
   }
   
   private static TicketType registerTicketType(String id, long expiryTicks, int flags) {
      return Registry.register(BuiltInRegistries.TICKET_TYPE, id, new TicketType(expiryTicks, flags));
   }
   
   private static <T> DataComponentType<T> registerEnchantmentEffectComponent(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
      return Registry.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MOD_ID,id), ((DataComponentType.Builder)builderOperator.apply(DataComponentType.builder())).build());
   }
   
   public static Identifier arcanaIdentifier(String id){
      return Identifier.fromNamespaceAndPath(MOD_ID,id);
   }
   
   public static ArcanaItem getArcanaItem(String id){
      return ARCANA_ITEMS.getValue(Identifier.fromNamespaceAndPath(MOD_ID,id));
   }
   
   private static IngredientCompendiumEntry getCryingObsidianEntry(){
      ItemStack displayStack = new ItemStack(Items.CRYING_OBSIDIAN);
      displayStack.set(DataComponents.RARITY, Rarity.UNCOMMON);
      return new IngredientCompendiumEntry(Component.translatable(Items.CRYING_OBSIDIAN.getDescriptionId()), displayStack, Items.CRYING_OBSIDIAN);
   }
}
