package net.borisshoes.arcananovum.test.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.skins.ArcanaSkins;
import net.borisshoes.arcananovum.skins.SkinCatalog;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * /arcana setSkinPreference: items a player gets without forging them use the preferred skin, the preference is checked
 * again when the item is made, and it is reset after a skin lookup shows the player lost the skin.
 * Installs a one-skin catalog and fake unlocks for the test, and puts the real ones back afterwards.
 */
public class SkinPreferenceGameTests {

   private static final String SKIN_ID = "test_tome_skin";

   @GameTest
   public void preferredSkinIsUsedForItemsNotForged(GameTestHelper helper){
      ServerPlayer player = RegressionGameTests.survivalPlayer(helper, new BlockPos(1, 2, 1));
      MinecraftServer server = helper.getLevel().getServer();
      ArcanaItem tome = ArcanaRegistry.ARCANE_TOME;
      withTestSkin(player.getUUID(), true, () -> {
         ArcanaPlayerData profile = ArcanaNovum.data(player);

         run(server, player, "arcana setSkinPreference arcane_tome arcananovum:" + SKIN_ID);
         helper.assertTrue(SKIN_ID.equals(profile.getSkinPreferences().get(tome.getId())), "The command did not store the preference: " + profile.getSkinPreferences());
         ItemStack stack = tome.addCrafterWithPreferredSkin(tome.getNewItem(), player.getStringUUID(), 0, server);
         helper.assertTrue(ArcanaItem.hasSkin(stack, SKIN_ID), "A new Arcane Tome did not get the preferred skin");

         ItemStack otherItem = ArcanaRegistry.WAYSTONE.addCrafterWithPreferredSkin(ArcanaRegistry.WAYSTONE.getNewItem(), player.getStringUUID(), 2, server);
         helper.assertTrue(ArcanaItem.getStringProperty(otherItem, ArcanaItem.SKIN_TAG).isEmpty(), "The Arcane Tome preference was used for a Waystone");

         run(server, player, "arcana setSkinPreference arcane_tome none");
         helper.assertTrue(profile.getSkinPreferences().isEmpty(), "\"none\" did not clear the preference: " + profile.getSkinPreferences());
         ItemStack plain = tome.addCrafterWithPreferredSkin(tome.getNewItem(), player.getStringUUID(), 0, server);
         helper.assertTrue(ArcanaItem.getStringProperty(plain, ArcanaItem.SKIN_TAG).isEmpty(), "A new Arcane Tome got a skin after the preference was cleared");
      });
      RegressionGameTests.finish(helper, player);
   }

   @GameTest
   public void skinPreferenceNeedsAnUnlockedSkin(GameTestHelper helper){
      ServerPlayer player = RegressionGameTests.survivalPlayer(helper, new BlockPos(1, 2, 1));
      MinecraftServer server = helper.getLevel().getServer();
      withTestSkin(player.getUUID(), false, () -> {
         run(server, player, "arcana setSkinPreference arcane_tome arcananovum:" + SKIN_ID);
         helper.assertTrue(ArcanaNovum.data(player).getSkinPreferences().isEmpty(), "A skin the player has not unlocked was accepted");
      });
      RegressionGameTests.finish(helper, player);
   }

   @GameTest
   public void lostSkinIsNotUsedAndItsPreferenceIsReset(GameTestHelper helper){
      ServerPlayer player = RegressionGameTests.survivalPlayer(helper, new BlockPos(1, 2, 1));
      MinecraftServer server = helper.getLevel().getServer();
      ArcanaItem tome = ArcanaRegistry.ARCANE_TOME;
      withTestSkin(player.getUUID(), true, () -> {
         ArcanaPlayerData profile = ArcanaNovum.data(player);
         profile.setSkinPreference(tome, ArcanaSkin.getSkinFromString(SKIN_ID));
         setGrants(player.getUUID(), List.of());

         ItemStack stack = tome.addCrafterWithPreferredSkin(tome.getNewItem(), player.getStringUUID(), 0, server);
         helper.assertTrue(ArcanaItem.getStringProperty(stack, ArcanaItem.SKIN_TAG).isEmpty(), "A new Arcane Tome got a skin the player no longer has");
         helper.assertTrue(profile.getSkinPreferences().containsKey(tome.getId()), "Making an item cleared the preference; only a skin lookup should");

         checkSkinPreferences(server, player.getUUID());
         helper.assertTrue(profile.getSkinPreferences().isEmpty(), "The skin lookup did not reset the preference for a lost skin");
      });
      RegressionGameTests.finish(helper, player);
   }

   // A skin missing from the installed catalog may only be missing until the next pack build, so the lookup keeps its preference
   @GameTest
   public void preferenceForSkinMissingFromCatalogIsKept(GameTestHelper helper){
      ServerPlayer player = RegressionGameTests.survivalPlayer(helper, new BlockPos(1, 2, 1));
      MinecraftServer server = helper.getLevel().getServer();
      ArcanaItem tome = ArcanaRegistry.ARCANE_TOME;
      withTestSkin(player.getUUID(), true, () -> {
         ArcanaPlayerData profile = ArcanaNovum.data(player);
         profile.setSkinPreference(tome, ArcanaSkin.getSkinFromString(SKIN_ID));
         install(SkinCatalog.EMPTY);

         ItemStack stack = tome.addCrafterWithPreferredSkin(tome.getNewItem(), player.getStringUUID(), 0, server);
         helper.assertTrue(ArcanaItem.getStringProperty(stack, ArcanaItem.SKIN_TAG).isEmpty(), "A new Arcane Tome got a skin that is not installed");
         checkSkinPreferences(server, player.getUUID());
         helper.assertTrue(SKIN_ID.equals(profile.getSkinPreferences().get(tome.getId())), "The preference was reset although the skin is still unlocked");
      });
      RegressionGameTests.finish(helper, player);
   }

   private static void run(MinecraftServer server, ServerPlayer player, String command){
      server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
   }

   private interface TestBody {
      void run() throws ReflectiveOperationException;
   }

   /** Installs a catalog with one Arcane Tome skin, optionally unlocked for the player, and restores the real catalog and unlocks afterwards */
   private static void withTestSkin(UUID player, boolean unlocked, TestBody body){
      SkinCatalog before = SkinCatalog.getInstalled();
      Map<UUID, Object> cache = null;
      try{
         cache = cache();
         Object savedEntry = cache.get(player);
         try{
            install(testCatalog());
            setGrants(player, unlocked ? List.of(SKIN_ID) : List.of());
            body.run();
         }finally{
            if(savedEntry == null){
               cache.remove(player);
            }else{
               cache.put(player, savedEntry);
            }
         }
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("Could not set up test skins", e);
      }finally{
         try{
            install(before);
         }catch(ReflectiveOperationException e){
            throw new RuntimeException("Could not restore the skin catalog", e);
         }
      }
   }

   private static SkinCatalog testCatalog() throws ReflectiveOperationException{
      JsonObject skin = new JsonObject();
      skin.addProperty("id", SKIN_ID);
      skin.addProperty("item_id", ArcanaRegistry.ARCANE_TOME.getId());
      skin.addProperty("hash", "test");
      skin.add("files", new JsonArray());
      skin.add("translation_keys", new JsonObject());
      skin.addProperty("primary_color", "#ffffff");
      skin.addProperty("secondary_color", "#000000");
      skin.add("attributions", new JsonArray());
      Method of = SkinCatalog.class.getDeclaredMethod("of", String.class, List.class);
      of.setAccessible(true);
      return (SkinCatalog) of.invoke(null, "test", List.of(skin));
   }

   private static void install(SkinCatalog catalog) throws ReflectiveOperationException{
      Method setInstalled = SkinCatalog.class.getDeclaredMethod("setInstalled", SkinCatalog.class);
      setInstalled.setAccessible(true);
      setInstalled.invoke(null, catalog);
   }

   @SuppressWarnings("unchecked")
   private static Map<UUID, Object> cache() throws ReflectiveOperationException{
      Field cache = ArcanaSkins.class.getDeclaredField("CACHE");
      cache.setAccessible(true);
      return (Map<UUID, Object>) cache.get(null);
   }

   private static void setGrants(UUID player, List<String> skinIds) throws ReflectiveOperationException{
      Constructor<?> grant = Class.forName(ArcanaSkins.class.getName() + "$SkinGrant").getDeclaredConstructor(String.class, long.class);
      Constructor<?> entry = Class.forName(ArcanaSkins.class.getName() + "$PlayerSkinEntry").getDeclaredConstructor(List.class, long.class);
      grant.setAccessible(true);
      entry.setAccessible(true);
      List<Object> grants = new ArrayList<>();
      for(String skinId : skinIds) grants.add(grant.newInstance(skinId, Long.MAX_VALUE));
      cache().put(player, entry.newInstance(grants, System.currentTimeMillis()));
   }

   private static void checkSkinPreferences(MinecraftServer server, UUID player) throws ReflectiveOperationException{
      Method check = ArcanaSkins.class.getDeclaredMethod("checkSkinPreferences", MinecraftServer.class, UUID.class);
      check.setAccessible(true);
      check.invoke(null, server, player);
   }
}
