package net.borisshoes.arcananovum.items;

import java.util.HashMap;

public class MagicItems {
   public static HashMap<String,MagicItem> registry = new HashMap<>();
   
   public static final MagicItem LEADERSHIP_CHARM = MagicItems.register("leadership_charm",new LeadershipCharm());
   public static final MagicItem WINGS_OF_ZEPHYR = MagicItems.register("wings_of_zephyr",new WingsOfZephyr());
   public static final MagicItem ARCANE_TOME = MagicItems.register("arcane_tome", new ArcaneTome());
   public static final MagicItem EXOTIC_MATTER = MagicItems.register("exotic_matter", new ExoticMatter());
   public static final MagicItem LIGHT_CHARM = MagicItems.register("light_charm", new LightCharm());
   public static final MagicItem FEASTING_CHARM = MagicItems.register("feasting_charm", new FeastingCharm());
   public static final MagicItem SOULSTONE = MagicItems.register("soulstone", new Soulstone());
   public static final MagicItem ESSENCE_EGG = MagicItems.register("essence_egg", new EssenceEgg());
   public static final MagicItem BRAIN_JAR = MagicItems.register("brain_jar", new BrainJar());
   public static final MagicItem SPAWNER_HARNESS = MagicItems.register("spawner_harness", new SpawnerHarness());
   public static final MagicItem SHIELD_OF_FORTITUDE = MagicItems.register("shield_of_fortitude", new ShieldOfFortitude());
   public static final MagicItem CONTINUUM_ANCHOR = MagicItems.register("continuum_anchor", new ContinuumAnchor());
   public static final MagicItem SOJOURNER_BOOTS = MagicItems.register("sojourner_boots", new SojournerBoots());
   
   public static final MagicItem PICKAXE_OF_PLUTO = MagicItems.register("pickaxe_of_pluto",new PickaxeOfPluto());
   public static final MagicItem RUNIC_MATRIX = MagicItems.register("runic_matrix", new RunicMatrix());
   public static final MagicItem TEMPORAL_MOMENT = MagicItems.register("temporal_moment", new TemporalMoment());
   public static final MagicItem ANCIENT_DOWSING_ROD = MagicItems.register("ancient_dowsing_rod", new AncientDowsingRod());
   public static final MagicItem FELIDAE_CHARM = MagicItems.register("felidae_charm", new FelidaeCharm());
   public static final MagicItem MAGNETISM_CHARM = MagicItems.register("magnetism_charm", new MagnetismCharm());
   public static final MagicItem FRACTAL_SPONGE = MagicItems.register("fractal_sponge", new FractalSponge());
   public static final MagicItem IGNEOUS_COLLIDER = MagicItems.register("igneous_collider", new IgneousCollider());
   public static final MagicItem PEARL_OF_RECALL = MagicItems.register("pearl_of_recall", new PearlOfRecall());
   public static final MagicItem BLINK_ARROWS = MagicItems.register("blink_arrows", new BlinkArrows());
   public static final MagicItem SHULKER_CORE = MagicItems.register("shulker_core", new ShulkerCore());
   public static final MagicItem STASIS_PEARL = MagicItems.register("stasis_pearl", new StasisPearl());
   public static final MagicItem LEVITATION_HARNESS = MagicItems.register("levitation_harness", new LevitationHarness());
   public static final MagicItem RUNIC_BOW = MagicItems.register("runic_bow", new RunicBow());
   public static final MagicItem SHADOW_STALKERS_GLAIVE = MagicItems.register("shadow_stalkers_glaive", new ShadowStalkersGlaive());
   
   
   private static MagicItem register(String id, MagicItem item){
      registry.put(id,item);
      return item;
   }
   
}
