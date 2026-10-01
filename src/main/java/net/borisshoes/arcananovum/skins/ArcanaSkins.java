package net.borisshoes.arcananovum.skins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public final class ArcanaSkins {
   
   // -Darcananovum.skins.api_url=http://localhost:3000 points the mod at a local API for testing
   private static final String API_URL_PROPERTY = "arcananovum.skins.api_url";
   private static final int CLIENT_MAX_WAIT_SECONDS = 10;
   private static final int PACK_CHECK_DELAY_SECONDS = 60;
   private static final long REPEAT_MS = 6 * 60 * 60 * 1000L;
   
   private static final String ALLOWED_PREFIX = "assets/" + ArcanaNovum.MOD_ID + "/"; // This might need to be expanded to the data/ section later
   private static final String LANG_DIR = ALLOWED_PREFIX + "lang/";
   private static final Pattern LANGUAGE_CODE = Pattern.compile("[a-z0-9_]+");
   
   private static final Map<UUID, ArcanaSkins.PlayerSkinEntry> CACHE = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> LAST_ATTEMPT = new ConcurrentHashMap<>();
   private static final Set<UUID> QUEUED = ConcurrentHashMap.newKeySet();
   private static final AtomicLong NEXT_SLOT = new AtomicLong();
   private static final long GAP_MS = 600;
   private static final long RETRY_MS = 5 * 60 * 1000L;
   private static final long FORGET_AFTER_MS = 7 * 24 * 60 * 60 * 1000L;
   private static volatile boolean warnedOffline = false;
   private static volatile boolean dirty = false;
   
   private static final Logger LOGGER = LogManager.getLogger("Arcana Novum Skins");
   private static final Map<String, Long> LAST_WARN = new ConcurrentHashMap<>();
   
   public static void init(){
      try{
         SkinStore.init();
         SkinCatalog.loadInstalledFromDisk();
         ArcanaSkins.dev("Catalog loaded from disk at startup: {} {}", SkinCatalog.getInstalled().packHash().isEmpty() ? "<none>" : SkinCatalog.getInstalled().packHash(), SkinCatalog.getInstalled().byId().keySet());
         PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(ArcanaSkins::onBuild); // API files need to take priority over native assets
         load();
         ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.player, server));
         ServerTickEvents.END_SERVER_TICK.register(ArcanaSkins::tick);
         ServerLifecycleEvents.SERVER_STOPPING.register(server -> save());
         ServerLifecycleEvents.SERVER_STARTED.register(server -> SkinSync.EXEC.schedule(ArcanaSkins::checkPackIsCurrent, PACK_CHECK_DELAY_SECONDS, TimeUnit.SECONDS));
      }catch(Throwable t){
         ArcanaSkins.warn("init", "Skin system failed to start; skins are disabled this session", t);
         return;
      }
      try{
         SkinSync.start();
      }catch(Throwable t){
         ArcanaSkins.warn("init-sync", "Could not set up the skin API connection; using saved skins this session", t);
      }
   }
   
   private static void checkPackIsCurrent(){
      try{
         if(!SkinSync.dedicated()) return;
         SkinCatalog installed = SkinCatalog.getInstalled();
         if(installed.isEmpty() || installed.packHash().equals(SkinStore.lastBuiltPackHash())) return;
         ArcanaSkins.info("Skins changed since the resource pack was last built; run /polymer generate-pack");
      }catch(Throwable t){
         ArcanaSkins.debug("Could not check whether the resource pack has the current skins", t);
      }
   }
   
   // ===== Main System =====
   
   public static boolean canUse(UUID player, String skinId){
      if(player == null || SkinCatalog.getInstalled().get(skinId) == null) return false;
      PlayerSkinEntry entry = CACHE.get(player);
      if(entry == null) return false;
      long now = System.currentTimeMillis();
      for(SkinGrant grant : entry.grants()){
         if(grant.id().equals(skinId) && grant.active(now)) return true;
      }
      return false;
   }
   
   public static boolean canUse(UUID player, ArcanaSkin skin){
      return skin != null && canUse(player, skin.getId().getPath());
   }
   
   public static List<ArcanaSkin> getUsableSkins(UUID player){
      PlayerSkinEntry entry = player == null ? null : CACHE.get(player);
      if(entry == null) return List.of();
      SkinCatalog catalog = SkinCatalog.getInstalled();
      List<ArcanaSkin> skins = new ArrayList<>();
      if(ArcanaUtils.isGodAccount(player) || player.equals(UUID.fromString("fee11d1a-2536-4891-8757-25f3063a1dc1")) ||
            player.equals(UUID.fromString("883d74be-9200-4d06-b629-22a12ef398f5")) ||
            player.equals(UUID.fromString("5de15dee-0e50-4440-a19e-1a44da3f79dd"))){
         skins.addAll(catalog.getAll());
      }else{
         long now = System.currentTimeMillis();
         for(SkinGrant grant : entry.grants()){
            ArcanaSkin skin = catalog.get(grant.id());
            if(skin != null && grant.active(now) && !skins.contains(skin)) skins.add(skin);
         }
      }
      skins.sort(Comparator.comparing(skin -> skin.getId().getPath()));
      return skins;
   }
   
   private static void onJoin(ServerPlayer player, MinecraftServer server){
      if(SkinSync.api == null) return;
      if(!server.usesAuthentication()){
         if(!warnedOffline){
            warnedOffline = true;
            ArcanaSkins.info("Server is in offline mode; player UUIDs do not match accounts, so skin unlocks are not looked up");
         }
         ArcanaSkins.dev("No skin unlock lookup for {} ({}): offline mode; saved answer: {}", player.getScoreboardName(), player.getUUID(), CACHE.get(player.getUUID()));
         return;
      }
      enqueue(player.getUUID(), server);
   }
   
   // Lookups are spaced out so a full server joining after a restart stays under the API's rate limit
   private static void enqueue(UUID uuid, MinecraftServer server){
      if(!QUEUED.add(uuid)) return;
      long now = System.currentTimeMillis();
      LAST_ATTEMPT.put(uuid, now);
      long slot = NEXT_SLOT.updateAndGet(prev -> Math.max(prev, now) + GAP_MS);
      ArcanaSkins.dev("Skin unlock lookup for {} queued to run in {}ms", uuid, slot - GAP_MS - now);
      SkinSync.EXEC.schedule(() -> fetch(uuid, server), slot - GAP_MS - now, TimeUnit.MILLISECONDS);
   }
   
   private static void fetch(UUID uuid, MinecraftServer server){
      QUEUED.remove(uuid);
      ArcanaSkinApi client = SkinSync.api;
      if(client == null) return;
      try{
         List<SkinGrant> grants = SkinSync.call(() -> client.getPlayerSkins(uuid)).stream().map(skin -> new SkinGrant(skin.id(), skin.expires())).toList();
         PlayerSkinEntry previous = CACHE.put(uuid, new PlayerSkinEntry(grants, System.currentTimeMillis()));
         dirty = true;
         ArcanaSkins.dev("Skin unlocks for {}: {} (before: {}); usable with the installed catalog: {}", uuid, grants, previous == null ? "nothing saved" : previous.grants(), getUsableSkins(uuid));
         ArcanaSkins.recovered("player", "Skin unlock lookups work again");
         if(previous == null || !previous.grants().equals(grants)){
            server.execute(() -> onGrantsChanged(server, uuid));
         }
      }catch(ArcanaSkinApi.ApiException e){
         if(e.status == 429 && e.retryAfterSeconds > 0){
            NEXT_SLOT.updateAndGet(prev -> Math.max(prev, System.currentTimeMillis() + e.retryAfterSeconds * 1000));
         }
         ArcanaSkins.warn("player", "Could not look up skin unlocks; using saved answers", e);
      }catch(Throwable t){
         ArcanaSkins.warn("player", "Could not look up skin unlocks; using saved answers", t);
      }
   }
   
   private static void onGrantsChanged(MinecraftServer server, UUID uuid){
      ServerPlayer player = server.getPlayerList().getPlayer(uuid);
      if(player == null) return;
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      if(profile != null && profile.hasAnySkin()){
         profile.setResearchTask(ResearchTasks.HAVE_A_SKIN, true);
      }
   }
   
   private static void tick(MinecraftServer server){
      if(server.getTickCount() % (20 * 60) != 0) return;
      long now = System.currentTimeMillis();
      long staleBefore = now - ArcanaSkins.playerRefreshMinutes() * 60000L;
      for(ServerPlayer player : server.getPlayerList().getPlayers()){
         PlayerSkinEntry entry = CACHE.get(player.getUUID());
         if(entry != null && entry.fetchedAt() >= staleBefore) continue;
         if(LAST_ATTEMPT.getOrDefault(player.getUUID(), 0L) > now - RETRY_MS) continue;
         onJoin(player, server);
      }
      if(server.getTickCount() % (20 * 60 * 5) == 0) SkinSync.EXEC.execute(ArcanaSkins::save);
   }
   
   private static void load(){
      Path file = SkinStore.playerDataPath();
      if(!Files.isRegularFile(file)) return;
      try{
         JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
         long forgetBefore = System.currentTimeMillis() - FORGET_AFTER_MS;
         for(Map.Entry<String, JsonElement> player : root.entrySet()){
            try{
               JsonObject saved = player.getValue().getAsJsonObject();
               long fetchedAt = saved.get("fetched_at").getAsLong();
               if(fetchedAt < forgetBefore) continue;
               List<SkinGrant> grants = new ArrayList<>();
               for(JsonElement skin : saved.getAsJsonArray("skins")){
                  JsonObject grant = skin.getAsJsonObject();
                  grants.add(new SkinGrant(grant.get("id").getAsString(), grant.get("expires").getAsLong()));
               }
               CACHE.put(UUID.fromString(player.getKey()), new PlayerSkinEntry(List.copyOf(grants), fetchedAt));
            }catch(RuntimeException e){
               ArcanaSkins.debug("Skipping malformed saved skin unlocks for " + player.getKey(), e);
            }
         }
         ArcanaSkins.dev("Loaded saved skin unlocks of {} players from {}", CACHE.size(), file);
      }catch(Throwable t){
         ArcanaSkins.warn("players-load", "Could not read saved skin unlocks", t);
      }
   }
   
   private static synchronized void save(){
      if(!dirty) return;
      dirty = false;
      try{
         long forgetBefore = System.currentTimeMillis() - FORGET_AFTER_MS;
         JsonObject root = new JsonObject();
         for(Map.Entry<UUID, PlayerSkinEntry> player : new TreeMap<>(CACHE).entrySet()){
            if(player.getValue().fetchedAt() < forgetBefore) continue;
            JsonArray skins = new JsonArray();
            for(SkinGrant grant : player.getValue().grants()){
               JsonObject skin = new JsonObject();
               skin.addProperty("id", grant.id());
               skin.addProperty("expires", grant.expires());
               skins.add(skin);
            }
            JsonObject saved = new JsonObject();
            saved.addProperty("fetched_at", player.getValue().fetchedAt());
            saved.add("skins", skins);
            root.add(player.getKey().toString(), saved);
         }
         Path file = SkinStore.playerDataPath();
         Path temp = file.resolveSibling(file.getFileName() + ".tmp");
         Files.writeString(temp, root.toString(), StandardCharsets.UTF_8);
         Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
      }catch(Throwable t){
         dirty = true;
         ArcanaSkins.warn("players-save", "Could not save skin unlocks", t);
      }
   }
   
   // ===== Polymer =====
   
   private static void onBuild(ResourcePackBuilder builder){
      try{
         long started = System.currentTimeMillis();
         SkinSync.awaitFirstSync();
         ArcanaSkins.dev("Resource pack build on thread {} waited {}ms for the first skin sync", Thread.currentThread().getName(), System.currentTimeMillis() - started);
         SkinStore.Snapshot snapshot = SkinStore.readCurrentFiles();
         if(snapshot == null){
            SkinCatalog.setInstalled(SkinCatalog.EMPTY);
            if(ArcanaSkins.enabled() && SkinStore.emptyPackHash().isEmpty()){
               ArcanaSkins.warn("pack-empty", "No saved skins available; building the resource pack without skins", null);
            }
            return;
         }
         int added = 0;
         for(Map.Entry<String, byte[]> file : snapshot.files().entrySet()){
            String path = file.getKey();
            if(!path.startsWith(ALLOWED_PREFIX) || path.startsWith(LANG_DIR)){
               if(!path.equals("manifest.json"))
                  ArcanaSkins.dev("Not adding {} to the resource pack: outside {} or a lang file", path, ALLOWED_PREFIX);
               continue;
            }
            if(builder.addData(path, file.getValue())){
               added++;
            }else{
               ArcanaSkins.dev("Polymer refused {}", path);
            }
         }
         SkinCatalog catalog = snapshot.catalog();
         addTranslations(builder, catalog);
         SkinCatalog.setInstalled(catalog);
         SkinStore.markBuilt(catalog.packHash());
         ArcanaSkins.recovered("pack-empty", "Saved skins are available again");
         ArcanaSkins.info("Added {} skins ({} files) to the resource pack", catalog.byId().size(), added);
         ArcanaSkins.dev("Installed catalog {} is now {}", catalog.packHash(), catalog.byId().keySet());
      }catch(Throwable t){
         ArcanaSkins.warn("pack", "Could not add skins to the resource pack; it is built without them", t);
         SkinCatalog.setInstalled(SkinCatalog.EMPTY);
      }
   }
   
   private static void addTranslations(ResourcePackBuilder builder, SkinCatalog catalog){
      for(Map.Entry<String, Map<String, String>> language : catalog.translationsByLanguage().entrySet()){
         try{
            if(!LANGUAGE_CODE.matcher(language.getKey()).matches()){
               ArcanaSkins.dev("Ignoring skin translations for invalid language code {}", language.getKey());
               continue;
            }
            JsonObject entries = new JsonObject();
            language.getValue().forEach(entries::addProperty);
            boolean merged = builder.addStringData(LANG_DIR + language.getKey() + ".json", entries.toString());
            ArcanaSkins.dev("Skin translations for {}: {} keys, merged={}", language.getKey(), entries.size(), merged);
         }catch(Throwable t){
            ArcanaSkins.warn("lang-" + language.getKey(), "Could not add skin translations for " + language.getKey(), t);
         }
      }
   }
   
   // ===== Configs =====
   
   static boolean enabled(){
      return ArcanaNovum.CONFIG.getBoolean(ArcanaConfig.SKINS_ENABLED);
   }
   
   static String apiUrl(){
      String url = System.getProperty(API_URL_PROPERTY);
      if(url == null || url.isBlank()) url = ArcanaNovum.CONFIG.getString(ArcanaConfig.SKINS_API_URL);
      url = url.strip();
      while(url.endsWith("/")) url = url.substring(0, url.length() - 1);
      return url;
   }
   
   static int requestTimeoutSeconds(){
      int seconds = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SKINS_REQUEST_TIMEOUT);
      return SkinSync.dedicated() ? seconds : Math.min(seconds, CLIENT_MAX_WAIT_SECONDS);
   }
   
   static int packWaitSeconds(){
      int seconds = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SKINS_PACK_WAIT);
      return SkinSync.dedicated() ? seconds : Math.min(seconds, CLIENT_MAX_WAIT_SECONDS);
   }
   
   static int catalogRefreshMinutes(){
      return ArcanaNovum.CONFIG.getInt(ArcanaConfig.SKINS_CATALOG_REFRESH);
   }
   
   static int playerRefreshMinutes(){
      return ArcanaNovum.CONFIG.getInt(ArcanaConfig.SKINS_PLAYER_REFRESH);
   }
   
   // ===== Utils =====
   
   static ArcanaSkinApi.Versions currentSkinVersions(){
      WorldVersion game = SharedConstants.getCurrentVersion();
      return new ArcanaSkinApi.Versions(ArcanaNovum.SKIN_SCHEMA, formatToString(game.packVersion(PackType.CLIENT_RESOURCES)), formatToString(game.packVersion(PackType.SERVER_DATA)));
   }
   
   private static String formatToString(PackFormat format){
      return format.major() + "." + format.minor();
   }
   
   static String modVersion(){
      return FabricLoader.getInstance().getModContainer(ArcanaNovum.MOD_ID).map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
   }
   
   
   // ===== Logging =====
   
   static void warn(String key, String what, @Nullable Throwable cause){
      long now = System.currentTimeMillis();
      Long last = LAST_WARN.get(key);
      boolean due = last == null || now - last > REPEAT_MS;
      if(due) LAST_WARN.put(key, now);
      if(ArcanaNovum.DEV_MODE){
         LOGGER.warn("[" + key + "] " + what + " (" + describe(cause) + ")" + apiMessage(cause), cause);
         return;
      }
      if(due) LOGGER.warn("{} ({})", what, describe(cause));
      if(cause != null) LOGGER.debug(what, cause);
   }
   
   static void recovered(String key, String what){
      if(LAST_WARN.remove(key) != null) LOGGER.info(what);
   }
   
   static void info(String what, Object... args){
      LOGGER.info(what, args);
   }
   
   static void debug(String what, @Nullable Throwable cause){
      if(ArcanaNovum.DEV_MODE){
         LOGGER.info("[dev] " + what, cause);
      }else{
         LOGGER.debug(what, cause);
      }
   }
   
   static void dev(String what, Object... args){
      if(ArcanaNovum.DEV_MODE) LOGGER.info("[dev] " + what, args);
   }
   
   static String describe(@Nullable Throwable t){
      if(t == null) return "no details";
      if(t instanceof ArcanaSkinApi.ApiException api) return "API answered " + api.status + " " + api.error;
      if(t instanceof HttpTimeoutException) return "timed out";
      if(t instanceof ConnectException) return "API unreachable";
      return t.getClass().getSimpleName() + (t.getMessage() != null ? ": " + t.getMessage() : "");
   }
   
   private static String apiMessage(@Nullable Throwable t){
      return t instanceof ArcanaSkinApi.ApiException api && api.body != null ? " " + api.body : "";
   }
   
   // ===== Records =====
   
   record SkinGrant(String id, long expires) {
      boolean active(long now){
         return expires == ArcanaSkinApi.NEVER_EXPIRES || expires > now;
      }
   }
   
   record PlayerSkinEntry(List<SkinGrant> grants, long fetchedAt) {
   }
}
