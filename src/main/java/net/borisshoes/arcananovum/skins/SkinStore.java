package net.borisshoes.arcananovum.skins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Owns config/arcananovum/skins. current/ holds the last downloaded bundle that passed its hash check and doubles
 * as the offline fallback; the lock keeps a pack build from reading it while a sync is replacing it.
 */
final class SkinStore {
   record Snapshot(SkinCatalog catalog, Map<String, byte[]> files) {
   }
   
   private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Pattern INSTANCE_ID = Pattern.compile("[A-Za-z0-9_-]{8,64}");
   private static final String LAST_BUILT_PACK_HASH = "last_built_pack_hash";
   private static final String EMPTY_PACK_HASH = "empty_pack_hash";
   private static final Object STATE_LOCK = new Object();
   private static Path root;
   
   private SkinStore(){
   }
   
   static void init() throws IOException{
      root = FabricLoader.getInstance().getConfigDir().resolve("arcananovum").resolve("skins").toAbsolutePath().normalize();
      Files.createDirectories(root);
      ArcanaSkins.dev("Skin store is at {}", root);
      deleteTree(root.resolve("incoming"));
      
      Path current = root.resolve("current");
      Path previous = root.resolve("previous");
      if(Files.exists(previous)){
         if(Files.exists(current)){
            deleteTree(previous);
         }else{
            Files.move(previous, current);
         }
      }
   }
   
   static Path playerDataPath(){
      return root.resolve("players.json");
   }
   
   @Nullable
   static Snapshot readCurrentFiles(){
      if(root == null) return null;
      LOCK.readLock().lock();
      try{
         Path dir = root.resolve("current");
         if(!Files.isRegularFile(dir.resolve("manifest.json"))){
            ArcanaSkins.dev("No saved skins: {} has no manifest.json", dir);
            return null;
         }
         Map<String, byte[]> files = new TreeMap<>();
         try(Stream<Path> walk = Files.walk(dir)){
            for(Path path : (Iterable<Path>) walk.filter(Files::isRegularFile)::iterator){
               files.put(dir.relativize(path).toString().replace('\\', '/'), Files.readAllBytes(path));
            }
         }
         JsonObject manifest = JsonParser.parseString(new String(files.get("manifest.json"), StandardCharsets.UTF_8)).getAsJsonObject();
         String hash = manifest.get("hash").getAsString();
         if(!hash.equals(ArcanaSkinApi.computeSkinHash(files))){
            ArcanaSkins.dev("Saved skins: manifest says {}, the {} files on disk hash to {}", hash, files.size(), ArcanaSkinApi.computeSkinHash(files));
            ArcanaSkins.warn("store-corrupt", "Saved skins in " + dir + " fail their hash check and are ignored", null);
            return null;
         }
         ArcanaSkins.recovered("store-corrupt", "Saved skins pass their hash check again");
         List<JsonObject> skins = new ArrayList<>();
         manifest.getAsJsonArray("skins").forEach(skin -> skins.add(skin.getAsJsonObject()));
         return new Snapshot(SkinCatalog.of(hash, skins), files);
      }catch(Throwable t){
         ArcanaSkins.warn("store-read", "Could not read saved skins", t);
         return null;
      }finally{
         LOCK.readLock().unlock();
      }
   }
   
   static String currentPackHash(){
      Snapshot snapshot = readCurrentFiles();
      if(snapshot != null) return snapshot.catalog().packHash();
      return emptyPackHash();
   }
   
   // Writes a verified bundle to incoming/ then swaps it in for current/
   static void install(ArcanaSkinApi.Bundle bundle) throws IOException{
      Path incoming = root.resolve("incoming");
      deleteTree(incoming);
      for(Map.Entry<String, byte[]> file : bundle.files().entrySet()){
         Path target = incoming.resolve(file.getKey()).normalize();
         if(!target.startsWith(incoming)) throw new IOException("unsafe path " + file.getKey());
         Files.createDirectories(target.getParent());
         Files.write(target, file.getValue());
      }
      ArcanaSkins.dev("Wrote {} files to {}; swapping it in for current/", bundle.files().size(), incoming);
      LOCK.writeLock().lock();
      try{
         Path current = root.resolve("current");
         Path previous = root.resolve("previous");
         deleteTree(previous);
         if(Files.exists(current)) Files.move(current, previous);
         try{
            Files.move(incoming, current);
         }catch(IOException e){
            if(Files.exists(previous) && !Files.exists(current)) Files.move(previous, current);
            throw e;
         }
         deleteTree(previous);
         setEmptyPackHash("");
      }finally{
         LOCK.writeLock().unlock();
      }
   }
   
   static void installEmpty(String packHash) throws IOException{
      LOCK.writeLock().lock();
      try{
         deleteTree(root.resolve("current"));
         setEmptyPackHash(packHash);
         ArcanaSkins.dev("Cleared current/ and saved empty pack hash {}", packHash);
      }finally{
         LOCK.writeLock().unlock();
      }
   }
   
   // Header X-Server-Instance-ID: a random UUID created once and kept across restarts, as rate limits are counted per id.
   static String getInstanceId() throws IOException{
      Path file = root.resolve("instance_id.txt");
      if(Files.isRegularFile(file)){
         String saved = Files.readString(file, StandardCharsets.UTF_8).strip();
         if(INSTANCE_ID.matcher(saved).matches()) return saved;
      }
      String created = UUID.randomUUID().toString();
      Files.writeString(file, created, StandardCharsets.UTF_8);
      ArcanaSkins.dev("Created a new instance id in {}", file);
      return created;
   }
   
   static void markBuilt(String packHash){
      writeState(LAST_BUILT_PACK_HASH, packHash);
   }
   
   static String lastBuiltPackHash(){
      return readState(LAST_BUILT_PACK_HASH);
   }
   
   static void setEmptyPackHash(String packHash){
      writeState(EMPTY_PACK_HASH, packHash);
   }
   
   static String emptyPackHash(){
      return readState(EMPTY_PACK_HASH);
   }
   
   private static JsonObject loadState(){
      try{
         Path file = root.resolve("state.json");
         if(Files.isRegularFile(file))
            return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
      }catch(Throwable t){
         ArcanaSkins.debug("Could not read the skin state file", t);
      }
      return new JsonObject();
   }
   
   private static String readState(String key){
      synchronized(STATE_LOCK){
         try{
            JsonObject state = loadState();
            return state.has(key) ? state.get(key).getAsString() : "";
         }catch(RuntimeException e){
            return "";
         }
      }
   }
   
   private static void writeState(String key, String value){
      synchronized(STATE_LOCK){
         try{
            JsonObject state = loadState();
            if(state.has(key) && value.equals(state.get(key).getAsString())) return;
            state.addProperty(key, value);
            Files.writeString(root.resolve("state.json"), GSON.toJson(state), StandardCharsets.UTF_8);
         }catch(Throwable t){
            ArcanaSkins.warn("store-state", "Could not save the skin state file", t);
         }
      }
   }
   
   static void deleteTree(Path dir) throws IOException{
      if(!Files.exists(dir)) return;
      try(Stream<Path> walk = Files.walk(dir)){
         for(Path path : (Iterable<Path>) walk.sorted(Comparator.reverseOrder())::iterator){
            Files.delete(path);
         }
      }
   }
}
