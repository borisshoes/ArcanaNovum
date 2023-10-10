package net.borisshoes.arcananovum;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.borisshoes.arcananovum.blocks.*;
import net.borisshoes.arcananovum.blocks.altars.*;
import net.borisshoes.arcananovum.blocks.forge.*;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.effects.DamageAmpEffect;
import net.borisshoes.arcananovum.entities.*;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.items.arrows.*;
import net.borisshoes.arcananovum.items.catalysts.*;
import net.borisshoes.arcananovum.items.charms.*;
import net.borisshoes.arcananovum.items.nonmagic.NebulousEssenceItem;
import net.borisshoes.arcananovum.items.nonmagic.StardustItem;
import net.borisshoes.arcananovum.materials.NonProtectiveArmorMaterial;
import net.borisshoes.arcananovum.recipes.ArcanaShieldDecoratorRecipe;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArcanaRegistry {
   public static final HashMap<String, MagicItem> registry = new HashMap<>();
   public static final HashMap<String, Item> ITEMS = new HashMap<>();
   public static final HashMap<String, Block> BLOCKS = new HashMap<>();
   
   //Armor Materials
   public static final ArmorMaterial NON_PROTECTIVE_ARMOR_MATERIAL = new NonProtectiveArmorMaterial();
   
   //Registering Banner Recipe
   public static final SpecialRecipeSerializer<ArcanaShieldDecoratorRecipe> ARCANA_SHIELD_DECORATION_SERIALIZER;
   public static final RecipeType<ArcanaShieldDecoratorRecipe> ARCANA_SHIELD_DECORATION;
   static {
      ARCANA_SHIELD_DECORATION = Registry.register(Registries.RECIPE_TYPE, new Identifier(Arcananovum.MOD_ID, "arcana_shield_decoration"), new RecipeType<ArcanaShieldDecoratorRecipe>() {
         @Override
         public String toString() {return "arcana_shield_decoration_recipe";}
      });
      ARCANA_SHIELD_DECORATION_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(Arcananovum.MOD_ID, "arcana_shield_decoration"), new SpecialRecipeSerializer<>(ArcanaShieldDecoratorRecipe::new));
   }
   
   // Entities
   public static final EntityType<RunicArrowEntity> RUNIC_ARROW_ENTITY = registerEntity( "runic_arrow",
         FabricEntityTypeBuilder.<RunicArrowEntity>create(SpawnGroup.MISC, (RunicArrowEntity::new)).dimensions(EntityDimensions.fixed(0.5f,0.5f)).trackRangeChunks(4).trackedUpdateRate(20).build()
   );
   public static final EntityType<ArbalestArrowEntity> ARBALEST_ARROW_ENTITY = registerEntity( "arbalest_arrow",
         FabricEntityTypeBuilder.<ArbalestArrowEntity>create(SpawnGroup.MISC, (ArbalestArrowEntity::new)).dimensions(EntityDimensions.fixed(0.5f,0.5f)).trackRangeChunks(4).trackedUpdateRate(20).build()
   );
   public static final EntityType<StasisPearlEntity> STASIS_PEARL_ENTITY = registerEntity( "stasis_pearl",
         FabricEntityTypeBuilder.<StasisPearlEntity>create(SpawnGroup.MISC, (StasisPearlEntity::new)).dimensions(EntityDimensions.fixed(0.25f, 0.25f)).trackRangeChunks(4).trackedUpdateRate(10).build()
   );
   public static final EntityType<DragonWizardEntity> DRAGON_WIZARD_ENTITY = registerEntity( "dragon_wizard",
         FabricEntityTypeBuilder.<DragonWizardEntity>create(SpawnGroup.MISC, (DragonWizardEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.95f)).trackRangeChunks(8).build()
   );
   public static final EntityType<DragonPhantomEntity> DRAGON_PHANTOM_ENTITY = registerEntity( "dragon_phantom",
         FabricEntityTypeBuilder.<DragonPhantomEntity>create(SpawnGroup.MISC, (DragonPhantomEntity::new)).dimensions(EntityDimensions.fixed(0.9f, 0.5f)).trackRangeChunks(8).build()
   );
   public static final EntityType<NulConstructEntity> NUL_CONSTRUCT_ENTITY = registerEntity( "nul_construct",
         FabricEntityTypeBuilder.<NulConstructEntity>create(SpawnGroup.MONSTER, (NulConstructEntity::new)).dimensions(EntityDimensions.fixed(0.9f, 3.5f)).trackRangeChunks(10).fireImmune().build()
   );
   
   // Status Effects
   public static final StatusEffect DAMAGE_AMP_EFFECT = registerStatusEffect("damage_amp",new DamageAmpEffect());
   
   // Non-magic Items
   public static final Item NEBULOUS_ESSENCE = registerItem("nebulous_essence",new NebulousEssenceItem(new FabricItemSettings().maxCount(64).fireproof()));
   public static final Item STARDUST = registerItem("stardust",new StardustItem(new FabricItemSettings().maxCount(64).fireproof()));;
   
   // 1.0 Items
   public static final MagicItem LEADERSHIP_CHARM = ArcanaRegistry.register(new LeadershipCharm());
   public static final MagicItem WINGS_OF_ENDERIA = ArcanaRegistry.register(new WingsOfEnderia());
   public static final MagicItem ARCANE_TOME = ArcanaRegistry.register(new ArcaneTome());
   public static final MagicItem EXOTIC_MATTER = ArcanaRegistry.register(new ExoticMatter());
   public static final MagicItem LIGHT_CHARM = ArcanaRegistry.register(new LightCharm());
   public static final MagicItem FEASTING_CHARM = ArcanaRegistry.register(new FeastingCharm());
   public static final MagicItem SOULSTONE = ArcanaRegistry.register(new Soulstone());
   public static final MagicItem ESSENCE_EGG = ArcanaRegistry.register(new EssenceEgg());
   public static final MagicItem BRAIN_JAR = ArcanaRegistry.register(new BrainJar());
   public static final MagicItem SPAWNER_HARNESS = ArcanaRegistry.register(new SpawnerHarness());
   public static final MagicItem SHIELD_OF_FORTITUDE = ArcanaRegistry.register(new ShieldOfFortitude());
   public static final MagicItem CONTINUUM_ANCHOR = ArcanaRegistry.register(new ContinuumAnchor());
   public static final MagicItem SOJOURNER_BOOTS = ArcanaRegistry.register(new SojournerBoots());
   
   // 1.1 Items
   public static final MagicItem PICKAXE_OF_CEPTYUS = ArcanaRegistry.register(new PickaxeOfCeptyus());
   public static final MagicItem RUNIC_MATRIX = ArcanaRegistry.register(new RunicMatrix());
   public static final MagicItem TEMPORAL_MOMENT = ArcanaRegistry.register(new TemporalMoment());
   public static final MagicItem ANCIENT_DOWSING_ROD = ArcanaRegistry.register(new AncientDowsingRod());
   public static final MagicItem FELIDAE_CHARM = ArcanaRegistry.register(new FelidaeCharm());
   public static final MagicItem MAGNETISM_CHARM = ArcanaRegistry.register(new MagnetismCharm());
   public static final MagicItem FRACTAL_SPONGE = ArcanaRegistry.register(new FractalSponge());
   public static final MagicItem IGNEOUS_COLLIDER = ArcanaRegistry.register(new IgneousCollider());
   public static final MagicItem PEARL_OF_RECALL = ArcanaRegistry.register(new PearlOfRecall());
   public static final MagicItem BLINK_ARROWS = ArcanaRegistry.register(new BlinkArrows());
   public static final MagicItem SHULKER_CORE = ArcanaRegistry.register(new ShulkerCore());
   public static final MagicItem STASIS_PEARL = ArcanaRegistry.register(new StasisPearl());
   public static final MagicItem LEVITATION_HARNESS = ArcanaRegistry.register(new LevitationHarness());
   public static final MagicItem RUNIC_BOW = ArcanaRegistry.register(new RunicBow());
   public static final MagicItem SHADOW_STALKERS_GLAIVE = ArcanaRegistry.register(new ShadowStalkersGlaive());
   
   // 1.3 Items
   public static final MagicItem TETHER_ARROWS = ArcanaRegistry.register(new TetherArrows());
   public static final MagicItem DETONATION_ARROWS = ArcanaRegistry.register(new DetonationArrows());
   public static final MagicItem CONCUSSION_ARROWS = ArcanaRegistry.register(new ConcussionArrows());
   public static final MagicItem SMOKE_ARROWS = ArcanaRegistry.register(new SmokeArrows());
   public static final MagicItem TELESCOPING_BEACON = ArcanaRegistry.register(new TelescopingBeacon());
   public static final MagicItem EXPULSION_ARROWS = ArcanaRegistry.register(new ExpulsionArrows());
   public static final MagicItem GRAVITON_ARROWS = ArcanaRegistry.register(new GravitonArrows());
   public static final MagicItem SIPHONING_ARROWS = ArcanaRegistry.register(new SiphoningArrows());
   public static final MagicItem STORM_ARROWS = ArcanaRegistry.register(new StormArrows());
   public static final MagicItem ARCANE_FLAK_ARROWS = ArcanaRegistry.register(new ArcaneFlakArrows());
   public static final MagicItem OVERFLOWING_QUIVER = ArcanaRegistry.register(new OverflowingQuiver());
   public static final MagicItem CINDERS_CHARM = ArcanaRegistry.register(new CindersCharm());
   public static final MagicItem PHOTONIC_ARROWS = ArcanaRegistry.register(new PhotonicArrows());
   public static final MagicItem SPAWNER_INFUSER = ArcanaRegistry.register(new SpawnerInfuser());
   public static final MagicItem RUNIC_QUIVER = ArcanaRegistry.register(new RunicQuiver());
   
   // 1.4 Items
   public static final MagicItem CATALYTIC_MATRIX = ArcanaRegistry.register(new CatalyticMatrix()); // Technically a 2.0 item, but registry order is a thing
   public static final MagicItem MUNDANE_CATALYST = ArcanaRegistry.register(new MundaneCatalyst());
   public static final MagicItem EMPOWERED_CATALYST = ArcanaRegistry.register(new EmpoweredCatalyst());
   public static final MagicItem EXOTIC_CATALYST = ArcanaRegistry.register(new ExoticCatalyst());
   public static final MagicItem LEGENDARY_CATALYST = ArcanaRegistry.register(new LegendaryCatalyst());
   public static final MagicItem MYTHICAL_CATALYST = ArcanaRegistry.register(new MythicalCatalyst());
   public static final MagicItem NUL_MEMENTO = ArcanaRegistry.register(new NulMemento());
   
   // 2.0 Items
   public static final MagicItem STARLIGHT_FORGE = ArcanaRegistry.register(new StarlightForge());
   public static final MagicItem CHEST_TRANSLOCATOR = ArcanaRegistry.register(new ChestTranslocator());
   public static final MagicItem CONTAINMENT_CIRCLET = ArcanaRegistry.register(new ContainmentCirclet());
   public static final MagicItem WILD_GROWTH_CHARM = ArcanaRegistry.register(new WildGrowthCharm());
   public static final MagicItem EVERLASTING_ROCKET = ArcanaRegistry.register(new EverlastingRocket());
   public static final MagicItem TWILIGHT_ANVIL = ArcanaRegistry.register(new TwilightAnvil());
   public static final MagicItem RADIANT_FLETCHERY = ArcanaRegistry.register(new RadiantFletchery());
   public static final MagicItem ARCANISTS_BELT = ArcanaRegistry.register(new ArcanistsBelt());
   public static final MagicItem PLANESHIFTER = ArcanaRegistry.register(new Planeshifter());
   public static final MagicItem STELLAR_CORE = ArcanaRegistry.register(new StellarCore());
   public static final MagicItem MIDNIGHT_ENCHANTER = ArcanaRegistry.register(new MidnightEnchanter());
   public static final MagicItem STORMCALLER_ALTAR = ArcanaRegistry.register(new StormcallerAltar());
   public static final MagicItem CELESTIAL_ALTAR = ArcanaRegistry.register(new CelestialAltar());
   public static final MagicItem ALCHEMICAL_ARBALEST = ArcanaRegistry.register(new AlchemicalArbalest());
   public static final MagicItem ARCANE_SINGULARITY = ArcanaRegistry.register(new ArcaneSingularity());
   public static final MagicItem STARPATH_ALTAR = ArcanaRegistry.register(new StarpathAltar());
   
   public static final BlockEntityType<? extends BlockEntity> IGNEOUS_COLLIDER_BLOCK_ENTITY = registerBlockEntity(IGNEOUS_COLLIDER.getId(), FabricBlockEntityTypeBuilder.create(IgneousColliderBlockEntity::new,((MagicBlock) IGNEOUS_COLLIDER).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> FRACTAL_SPONGE_BLOCK_ENTITY = registerBlockEntity(FRACTAL_SPONGE.getId(), FabricBlockEntityTypeBuilder.create(FractalSpongeBlockEntity::new,((MagicBlock) FRACTAL_SPONGE).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> CONTINUUM_ANCHOR_BLOCK_ENTITY = registerBlockEntity(CONTINUUM_ANCHOR.getId(), FabricBlockEntityTypeBuilder.create(ContinuumAnchorBlockEntity::new,((MagicBlock) CONTINUUM_ANCHOR).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> SPAWNER_INFUSER_BLOCK_ENTITY = registerBlockEntity(SPAWNER_INFUSER.getId(), FabricBlockEntityTypeBuilder.create(SpawnerInfuserBlockEntity::new,((MagicBlock) SPAWNER_INFUSER).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> CELESTIAL_ALTAR_BLOCK_ENTITY = registerBlockEntity(CELESTIAL_ALTAR.getId(), FabricBlockEntityTypeBuilder.create(CelestialAltarBlockEntity::new,((MagicBlock) CELESTIAL_ALTAR).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> STARPATH_ALTAR_BLOCK_ENTITY = registerBlockEntity(STARPATH_ALTAR.getId(), FabricBlockEntityTypeBuilder.create(StarpathAltarBlockEntity::new,((MagicBlock) STARPATH_ALTAR).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> STORMCALLER_ALTAR_BLOCK_ENTITY = registerBlockEntity(STORMCALLER_ALTAR.getId(), FabricBlockEntityTypeBuilder.create(StormcallerAltarBlockEntity::new,((MagicBlock) STORMCALLER_ALTAR).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> ARCANE_SINGULARITY_BLOCK_ENTITY = registerBlockEntity(ARCANE_SINGULARITY.getId(), FabricBlockEntityTypeBuilder.create(ArcaneSingularityBlockEntity::new,((MagicBlock) ARCANE_SINGULARITY).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> MIDNIGHT_ENCHANTER_BLOCK_ENTITY = registerBlockEntity(MIDNIGHT_ENCHANTER.getId(), FabricBlockEntityTypeBuilder.create(MidnightEnchanterBlockEntity::new,((MagicBlock) MIDNIGHT_ENCHANTER).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> RADIANT_FLETCHERY_BLOCK_ENTITY = registerBlockEntity(RADIANT_FLETCHERY.getId(), FabricBlockEntityTypeBuilder.create(RadiantFletcheryBlockEntity::new,((MagicBlock) RADIANT_FLETCHERY).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> STARLIGHT_FORGE_BLOCK_ENTITY = registerBlockEntity(STARLIGHT_FORGE.getId(), FabricBlockEntityTypeBuilder.create(StarlightForgeBlockEntity::new,((MagicBlock) STARLIGHT_FORGE).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> STELLAR_CORE_BLOCK_ENTITY = registerBlockEntity(STELLAR_CORE.getId(), FabricBlockEntityTypeBuilder.create(StellarCoreBlockEntity::new,((MagicBlock) STELLAR_CORE).getBlock()).build());
   public static final BlockEntityType<? extends BlockEntity> TWILIGHT_ANVIL_BLOCK_ENTITY = registerBlockEntity(TWILIGHT_ANVIL.getId(), FabricBlockEntityTypeBuilder.create(TwilightAnvilBlockEntity::new,((MagicBlock) TWILIGHT_ANVIL).getBlock()).build());
   
   public static void initialize(){
      for(Map.Entry<String, MagicItem> entry : registry.entrySet()){
         String id = entry.getKey();
         MagicItem magicItem = entry.getValue();
         registerItem(id, magicItem.getItem());
         //System.out.println("  \"item.arcananovum."+id+"\": \"\",");
         
         if(magicItem instanceof MagicBlock magicBlock){
            registerBlock(id, magicBlock.getBlock());
         }
      }
      for(Map.Entry<String, MagicItem> entry : registry.entrySet()){
         if(entry.getValue() instanceof MultiblockCore mc){
            mc.loadMultiblock(); // Must be done after all blocks are registered
         }
      }
      
      FabricDefaultAttributeRegistry.register(DRAGON_WIZARD_ENTITY, DragonWizardEntity.createWizardAttributes());
      FabricDefaultAttributeRegistry.register(DRAGON_PHANTOM_ENTITY, DragonPhantomEntity.createPhantomAttributes());
      FabricDefaultAttributeRegistry.register(NUL_CONSTRUCT_ENTITY, NulConstructEntity.createConstructAttributes());
   }
   
   private static MagicItem register(MagicItem magicItem){
      registry.put(magicItem.getId(),magicItem);
      return magicItem;
   }
   
   private static Item registerItem(String id, Item item) {
      ITEMS.put(id, Registry.register(Registries.ITEM, new Identifier(Arcananovum.MOD_ID, id), item));
      return item;
   }
   
   private static void registerBlock(String id, Block block) {
      BLOCKS.put(id, block);
      Registry.register(Registries.BLOCK, new Identifier(Arcananovum.MOD_ID, id), block);
   }
   
   public static BlockEntityType<? extends BlockEntity> registerBlockEntity(String id, BlockEntityType<? extends BlockEntity> blockEntityType) {
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(Arcananovum.MOD_ID, id), blockEntityType);
      PolymerBlockUtils.registerBlockEntity(blockEntityType);
      return blockEntityType;
   }
   
   public static <T extends Entity> EntityType<T> registerEntity(String id, EntityType<T> build){
      Registry.register(Registries.ENTITY_TYPE, new Identifier(Arcananovum.MOD_ID,id), build);
      PolymerEntityUtils.registerType(build);
      //System.out.println("  \""+build.getTranslationKey()+"\": \"\",");
      return build;
   }
   
   public static StatusEffect registerStatusEffect(String id, StatusEffect effect){
      Registry.register(Registries.STATUS_EFFECT, new Identifier(Arcananovum.MOD_ID,id), effect);
      //System.out.println("  \""+effect.getTranslationKey()+"\": \"\",");
      return effect;
   }
   
   public static Item getItem(String id) {
      return ITEMS.getOrDefault(id, Items.AIR);
   }
   
   public static final ArrayList<MagicItem> RECOMMENDED_LIST = new ArrayList<>(Arrays.asList(
         ARCANE_TOME,
         STARLIGHT_FORGE,
         MAGNETISM_CHARM,
         LIGHT_CHARM,
         FEASTING_CHARM,
         WILD_GROWTH_CHARM,
         FELIDAE_CHARM,
         CINDERS_CHARM,
         TELESCOPING_BEACON,
         ANCIENT_DOWSING_ROD,
         CHEST_TRANSLOCATOR,
         CONTAINMENT_CIRCLET,
         EVERLASTING_ROCKET,
         SOULSTONE,
         ESSENCE_EGG,
         TWILIGHT_ANVIL,
         BRAIN_JAR,
         ARCANISTS_BELT,
         OVERFLOWING_QUIVER,
         TEMPORAL_MOMENT,
         PLANESHIFTER,
         PEARL_OF_RECALL,
         STASIS_PEARL,
         SPAWNER_HARNESS,
         FRACTAL_SPONGE,
         IGNEOUS_COLLIDER,
         RADIANT_FLETCHERY,
         STELLAR_CORE,
         MIDNIGHT_ENCHANTER,
         ARCANE_SINGULARITY,
         STORMCALLER_ALTAR,
         CELESTIAL_ALTAR,
         STARPATH_ALTAR,
         EXOTIC_MATTER,
         CONTINUUM_ANCHOR,
         SPAWNER_INFUSER,
         SHULKER_CORE,
         LEVITATION_HARNESS,
         SOJOURNER_BOOTS,
         SHIELD_OF_FORTITUDE,
         SHADOW_STALKERS_GLAIVE,
         ALCHEMICAL_ARBALEST,
         RUNIC_MATRIX,
         RUNIC_BOW,
         RUNIC_QUIVER,
         TETHER_ARROWS,
         DETONATION_ARROWS,
         SMOKE_ARROWS,
         CONCUSSION_ARROWS,
         SIPHONING_ARROWS,
         ARCANE_FLAK_ARROWS,
         BLINK_ARROWS,
         EXPULSION_ARROWS,
         GRAVITON_ARROWS,
         STORM_ARROWS,
         PHOTONIC_ARROWS,
         WINGS_OF_ENDERIA,
         PICKAXE_OF_CEPTYUS,
         LEADERSHIP_CHARM,
         CATALYTIC_MATRIX,
         MUNDANE_CATALYST,
         EMPOWERED_CATALYST,
         EXOTIC_CATALYST,
         LEGENDARY_CATALYST,
         MYTHICAL_CATALYST,
         NUL_MEMENTO
   ));
}
