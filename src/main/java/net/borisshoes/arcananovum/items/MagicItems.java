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
   
   private static MagicItem register(String id, MagicItem item){
      registry.put(id,item);
      return item;
   }
   
}
