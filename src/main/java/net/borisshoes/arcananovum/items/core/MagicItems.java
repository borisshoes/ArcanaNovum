package net.borisshoes.arcananovum.items.core;

import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.items.arrows.*;
import net.borisshoes.arcananovum.items.catalysts.*;
import net.borisshoes.arcananovum.items.charms.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MagicItems {
   public static final HashMap<String,MagicItem> registry = new HashMap<>();
   
   // 1.0 Items
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
   
   // 1.1 Items
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
   
   // 1.3 Items
   public static final MagicItem TETHER_ARROWS = MagicItems.register("tether_arrows", new TetherArrows());
   public static final MagicItem DETONATION_ARROWS = MagicItems.register("detonation_arrows", new DetonationArrows());
   public static final MagicItem CONCUSSION_ARROWS = MagicItems.register("concussion_arrows", new ConcussionArrows());
   public static final MagicItem SMOKE_ARROWS = MagicItems.register("smoke_arrows", new SmokeArrows());
   public static final MagicItem TELESCOPING_BEACON = MagicItems.register("telescoping_beacon", new TelescopingBeacon());
   public static final MagicItem EXPULSION_ARROWS = MagicItems.register("expulsion_arrows", new ExpulsionArrows());
   public static final MagicItem GRAVITON_ARROWS = MagicItems.register("graviton_arrows", new GravitonArrows());
   public static final MagicItem SIPHONING_ARROWS = MagicItems.register("siphoning_arrows", new SiphoningArrows());
   public static final MagicItem STORM_ARROWS = MagicItems.register("storm_arrows", new StormArrows());
   public static final MagicItem ARCANE_FLAK_ARROWS = MagicItems.register("arcane_flak_arrows", new ArcaneFlakArrows());
   public static final MagicItem OVERFLOWING_QUIVER = MagicItems.register("overflowing_quiver", new OverflowingQuiver());
   public static final MagicItem CINDERS_CHARM = MagicItems.register("cinders_charm", new CindersCharm());
   public static final MagicItem PHOTONIC_ARROWS = MagicItems.register("photonic_arrows", new PhotonicArrows());
   public static final MagicItem SPAWNER_INFUSER = MagicItems.register("spawner_infuser", new SpawnerInfuser());
   public static final MagicItem RUNIC_QUIVER = MagicItems.register("runic_quiver", new RunicQuiver());
   
   // 1.4 Items
   public static final MagicItem CATALYST_MUNDANE = MagicItems.register("catalyst_mundane", new catalyst_mundane());
   public static final MagicItem CATALYST_EMPOWERED = MagicItems.register("catalyst_empowered", new catalyst_empowered());
   public static final MagicItem CATALYST_EXOTIC = MagicItems.register("catalyst_exotic", new catalyst_exotic());
   public static final MagicItem CATALYST_LEGENDARY = MagicItems.register("catalyst_legendary", new catalyst_legendary());
   public static final MagicItem CATALYST_MYTHICAL = MagicItems.register("catalyst_mythical", new catalyst_mythical());
   
   private static MagicItem register(String id, MagicItem item){
      registry.put(id,item);
      return item;
   }
   
   public static final ArrayList<MagicItem> RECOMMENDED_LIST = new ArrayList<>(Arrays.asList(
         ARCANE_TOME,
         TEMPORAL_MOMENT,
         RUNIC_MATRIX,
         EXOTIC_MATTER,
         MAGNETISM_CHARM,
         LIGHT_CHARM,
         FEASTING_CHARM,
         FELIDAE_CHARM,
         CINDERS_CHARM,
         TELESCOPING_BEACON,
         ANCIENT_DOWSING_ROD,
         SOULSTONE,
         ESSENCE_EGG,
         OVERFLOWING_QUIVER,
         BRAIN_JAR,
         PEARL_OF_RECALL,
         STASIS_PEARL,
         SPAWNER_HARNESS,
         FRACTAL_SPONGE,
         IGNEOUS_COLLIDER,
         CONTINUUM_ANCHOR,
         SPAWNER_INFUSER,
         SHULKER_CORE,
         LEVITATION_HARNESS,
         SOJOURNER_BOOTS,
         SHIELD_OF_FORTITUDE,
         SHADOW_STALKERS_GLAIVE,
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
         WINGS_OF_ZEPHYR,
         PICKAXE_OF_PLUTO,
         LEADERSHIP_CHARM,
         CATALYST_MUNDANE,
         CATALYST_EMPOWERED,
         CATALYST_EXOTIC,
         CATALYST_LEGENDARY,
         CATALYST_MYTHICAL
   ));
}
