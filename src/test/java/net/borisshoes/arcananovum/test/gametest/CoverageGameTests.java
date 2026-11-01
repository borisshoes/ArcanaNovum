package net.borisshoes.arcananovum.test.gametest;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Every achievement and augment needs a test that names it in @Covers, or an entry here that says why it has none.
 * A new achievement or augment fails these tests until one of the two exists.
 */
public class CoverageGameTests {
   private static final List<Class<?>> TEST_CLASSES = List.of(ArrowGameTests.class, CharmGameTests.class, ItemGameTests.class, GearGameTests.class, BlockGameTests.class, RegressionGameTests.class);

   private static final Map<String, String> UNTESTED_ACHIEVEMENTS = Map.ofEntries(
         Map.entry("air_traffic_control", "Needs twelve flying mob types around a player flying through Player Ability Lib"),
         Map.entry("amnesiac", "Fires at the end of the Memento dialog, which plays for several hundred ticks"),
         Map.entry("and_the_universe_said", "Scripted Gaialtus event"),
         Map.entry("another_touch_of_personality", "Fires when a Starlight Forge GUI forges with a selected skin, and the test run installs no skins"),
         Map.entry("arcane_quasar", "Fires from a click in the Arcane Singularity GUI"),
         Map.entry("beyond_irons_limit", "Fires from a click in the Twilight Anvil GUI"),
         Map.entry("carrier_has_arrived", "Needs a working Astral Gateway pair and a Happy Ghast flying through it"),
         Map.entry("close_call", "Depends on the concentration overload timer landing the player at exactly the right health"),
         Map.entry("construct_deconstructed", "Needs a full Nul Construct fight"),
         Map.entry("diverse_arsenal", "Fires when closing the Quiver GUI"),
         Map.entry("divine_favor", "Needs a full Nul Construct fight and a random drop"),
         Map.entry("divine_transmutation", "Needs a built Transmutation Altar and the Divine Catalyst recipe"),
         Map.entry("door_of_divinity", "Needs the full Nul Construct summoning structure, which then starts the boss fight"),
         Map.entry("enchanting_overkill", "Fires from a click in the Midnight Enchanter GUI"),
         Map.entry("fractal_attunement", "Fires from a click in the Transmutation recipe GUI"),
         Map.entry("frequent_flier", "Only counts flight granted through Player Ability Lib, which a mock player has no client to hold"),
         Map.entry("humble_necromancer", "Fires from a click in the Spawner Infuser GUI"),
         Map.entry("interloper", "Scripted Ceptyus event"),
         Map.entry("lost_knowledge", "Fires at the end of the Memento dialog, which plays for several hundred ticks"),
         Map.entry("masterpiece_to_nothing", "Fires from a click in the Midnight Enchanter GUI"),
         Map.entry("moderately_insane", "Needs a working Astral Gateway frame of 64 Netherite blocks"),
         Map.entry("mogged", "Needs the transmogrification recipe at a built altar"),
         Map.entry("nidavellir", "Needs a Starlight Forge with every addition built around it"),
         Map.entry("plane_rider", "Needs a real trip through the Nether, and portal search would generate terrain during the run"),
         Map.entry("price_of_knowledge", "Needs a built Transmutation Altar and the Aequalis Scientia recipe"),
         Map.entry("questionable_exchange", "Needs the Aequalis skill transmutation at a built altar with matching items"),
         Map.entry("sculk_hungers", "Fires from a click in the Spawner Infuser GUI"),
         Map.entry("security_rainbow", "Fires from a click in the Ender Crate channel GUI"),
         Map.entry("spectral_support", "Needs a live Alchemical Arbalest field to credit 1000 damage in 10 seconds"),
         Map.entry("starburst_stream", "Depends on hit timing with both Binary Blades at full energy"),
         Map.entry("state_alchemist", "Needs a built Transmutation Altar and a recipe laid out on it as item entities"),
         Map.entry("tinker_to_the_top", "Fires from a click in the Twilight Anvil GUI"),
         Map.entry("to_the_moon", "Only counts flight granted through Player Ability Lib, which a mock player has no client to hold"),
         Map.entry("touch_of_personality", "Fires from the Twilight Anvil rename GUI"),
         Map.entry("zeraiya", "Scripted Zeraiya event")
   );

   private static final Map<String, String> UNTESTED_AUGMENTS = Map.ofEntries(
         Map.entry("decoalescence", "Its sweep only runs in an active Interdictor with a matching multiblock"),
         Map.entry("accretion", "Unlocks a button in the Arcane Singularity GUI"),
         Map.entry("adaptability", "Depends on the concentration breakdown of a real item set"),
         Map.entry("anvil_expertise", "Changes what a click in the Twilight Anvil GUI costs"),
         Map.entry("astral_stabilizers", "Changes the Astral Gateway frame scan, which needs a built frame"),
         Map.entry("biometric_channels", "Needs a Tome click on a placed Ender Crate and a second player"),
         Map.entry("blinding_rage", "Runs when a thrown Spear hits, which needs a full flight"),
         Map.entry("cantor", "Repeats the absorb on a timer after placing a real sponge item"),
         Map.entry("careful_reconstruction", "Reads a beacon save made by the full Telescoping Beacon pickup"),
         Map.entry("enchanting_expertise", "Changes what a click in the Midnight Enchanter GUI costs"),
         Map.entry("equivalent_exchange", "Unlocks a recipe at a built Transmutation Altar"),
         Map.entry("essence_supernova", "Changes the output of a click in the Midnight Enchanter GUI"),
         Map.entry("fertilization", "Sets a tick interval read from config, with nothing else to observe beyond the passive tick tests"),
         Map.entry("focus", "Depends on the concentration breakdown across ender chest and shulker contents"),
         Map.entry("harmonic_feedback", "Only lengthens particles"),
         Map.entry("harness_recycler", "Fuel is only spent during flight granted through Player Ability Lib, which a mock player has no client to hold"),
         Map.entry("impermanent_permutation", "Unlocks a recipe at a built Transmutation Altar"),
         Map.entry("mining_laser", "Runs a timed drilling callback that breaks blocks up to the world surface"),
         Map.entry("moonlit_forge", "Only changes the starting layout in the Stardust Infusion minigame"),
         Map.entry("mystic_collection", "Changes where the Starlight Forge GUI looks for ingredients"),
         Map.entry("persistent_tempest", "Unlocks buttons in the Stormcaller Altar GUI"),
         Map.entry("precision_disenchanting", "Unlocks a mode in the Midnight Enchanter GUI"),
         Map.entry("precision_interdiction", "Unlocks buttons in the Interdictor GUI"),
         Map.entry("pulsar_blades", "Fires from a client swing with both Binary Blades in hand"),
         Map.entry("red_giant_blades", "Reads energy built up by real attacks with both Binary Blades in hand"),
         Map.entry("resourceful", "Rolled per ingredient by the Starlight Forge GUI"),
         Map.entry("runic_arbalest", "Changes how the Arbalest fires through the crossbow use and charge cycle"),
         Map.entry("scattershot", "Read while the crossbow charges through vanilla enchantment handling"),
         Map.entry("skilled", "Applied through a page of the Starlight Forge GUI"),
         Map.entry("spectral_amplification", "Needs a live Alchemical Arbalest field"),
         Map.entry("star_charts", "Unlocks buttons in the Starpath Altar GUI"),
         Map.entry("starlight_recyclers", "Stardust use is read while an Astral Gateway is open, which needs a built frame"),
         Map.entry("stellar_control", "Unlocks buttons in the Celestial Altar GUI"),
         Map.entry("timeless_wisdom", "Read by the Aequalis recipes at a built Transmutation Altar"),
         Map.entry("trade_agreement", "Repeats a recipe at a built Transmutation Altar"),
         Map.entry("unending_hatred", "Read when a thrown Spear returns, which needs a full flight and pickup"),
         Map.entry("void_storm", "Runs when a thrown Spear lands, which needs a full flight"),
         Map.entry("white_dwarf_blades", "A parry needs a use-and-release cycle while being attacked")
   );

   @GameTest
   public void everyAchievementHasATestOrAReason(GameTestHelper helper){
      TestKit.assertCoverage(helper, "Achievements", TEST_CLASSES, new TreeSet<>(ArcanaAchievements.ARCANA_ACHIEVEMENTS.keySet()), UNTESTED_ACHIEVEMENTS.keySet());
      helper.succeed();
   }

   @GameTest
   public void everyAugmentHasATestOrAReason(GameTestHelper helper){
      TestKit.assertCoverage(helper, "Augments", TEST_CLASSES, new TreeSet<>(ArcanaAugments.registry.keySet()), UNTESTED_AUGMENTS.keySet());
      helper.succeed();
   }
}
