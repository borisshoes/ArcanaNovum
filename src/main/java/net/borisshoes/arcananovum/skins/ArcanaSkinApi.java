package net.borisshoes.arcananovum.skins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ArcanaSkinApi {
   public static final long NEVER_EXPIRES = -1;
   private static final int MAX_BUNDLE_BYTES = 64 * 1024 * 1024;
   
   private final HttpClient http;
   private final Duration requestTimeout;
   private final String baseUrl;
   private final String instanceId;
   private final String salt;
   private final Versions versions;
   private final String modVersion;
   private volatile long clockOffsetMillis = 0;
   
   public ArcanaSkinApi(String baseUrl, String instanceId, String salt, Versions versions, String modVersion, Duration requestTimeout){
      this.baseUrl = baseUrl;
      this.instanceId = instanceId;
      this.salt = salt;
      this.versions = versions;
      this.modVersion = modVersion;
      this.requestTimeout = requestTimeout;
      Duration connect = requestTimeout.compareTo(Duration.ofSeconds(10)) < 0 ? requestTimeout : Duration.ofSeconds(10);
      this.http = HttpClient.newBuilder().connectTimeout(connect).build();
   }
   
   public void setClockOffset(long offsetMillis){
      this.clockOffsetMillis = offsetMillis;
   }
   
   // Three version parameters. Pack formats are "major.minor"
   public record Versions(int skinSchema, String resourcePackFormat, String dataPackFormat) {
      private static final Pattern FORMAT = Pattern.compile("\\d+\\.\\d+");
      
      public Versions{
         if(skinSchema < 0) throw new IllegalArgumentException("skin_schema must not be negative");
         if(!FORMAT.matcher(resourcePackFormat).matches())
            throw new IllegalArgumentException("bad resource_pack_format " + resourcePackFormat);
         if(!FORMAT.matcher(dataPackFormat).matches())
            throw new IllegalArgumentException("bad data_pack_format " + dataPackFormat);
      }
      
      String query(){
         return "skin_schema=" + skinSchema + "&resource_pack_format=" + resourcePackFormat + "&data_pack_format=" + dataPackFormat;
      }
   }
   
   public record SkinList(String packHash, List<JsonObject> skins) {
   }
   
   public record Bundle(String hash, List<JsonObject> skins, Map<String, byte[]> files) {
   }
   
   public record OwnedSkin(String id, long expires) {
      public boolean isActive(long nowMillis){
         return expires == NEVER_EXPIRES || expires > nowMillis;
      }
   }
   
   public static final class ApiException extends IOException {
      @Serial
      private static final long serialVersionUID = 1L;
      public final int status;
      public final String error;
      public final transient JsonObject body;
      public final long retryAfterSeconds;
      
      ApiException(int status, String error, String message, JsonObject body, long retryAfterSeconds){
         super(status + " " + error + ": " + message);
         this.status = status;
         this.error = error;
         this.body = body;
         this.retryAfterSeconds = retryAfterSeconds;
      }
   }
   
   public SkinList listSkins() throws IOException, InterruptedException{
      JsonObject body = json(send(signed("/arcananovum/skins/list").GET().build()));
      return new SkinList(body.get("pack_hash").getAsString(), objects(body.getAsJsonArray("skins")));
   }
   
   public Bundle downloadSkins(Collection<String> skinIds) throws IOException, InterruptedException{
      if(skinIds.isEmpty()) throw new IllegalArgumentException("no skin ids to download");
      JsonArray ids = new JsonArray();
      skinIds.forEach(ids::add);
      JsonObject request = new JsonObject();
      request.add("skins", ids);
      
      HttpRequest httpRequest = signed("/arcananovum/skins/data")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(request.toString(), StandardCharsets.UTF_8))
            .build();
      Map<String, byte[]> files = unzip(send(httpRequest).body());
      
      byte[] manifestBytes = files.get("manifest.json");
      if(manifestBytes == null) throw new IOException("bundle has no manifest.json");
      JsonObject manifest = JsonParser.parseString(new String(manifestBytes, StandardCharsets.UTF_8)).getAsJsonObject();
      String hash = manifest.get("hash").getAsString();
      if(!hash.equals(computeSkinHash(files))) throw new IOException("bundle does not match its manifest hash");
      return new Bundle(hash, objects(manifest.getAsJsonArray("skins")), files);
   }
   
   public List<OwnedSkin> getPlayerSkins(UUID player) throws IOException, InterruptedException{
      JsonObject body = json(send(signed("/arcananovum/skins/player/" + player).GET().build()));
      List<OwnedSkin> skins = new ArrayList<>();
      for(JsonObject skin : objects(body.getAsJsonArray("skins"))){
         skins.add(new OwnedSkin(skin.get("id").getAsString(), skin.get("expires").getAsLong()));
      }
      return skins;
   }
   
   private HttpRequest.Builder signed(String route){
      String timestamp = Long.toString(System.currentTimeMillis() + clockOffsetMillis);
      String checksum = sha256Hex((instanceId + ":" + timestamp + ":" + route + ":" + salt).getBytes(StandardCharsets.UTF_8));
      HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + route + "?" + versions.query()))
            .timeout(requestTimeout)
            .header("X-Server-Instance-ID", instanceId)
            .header("X-Request-Timestamp", timestamp)
            .header("X-Request-Checksum", checksum);
      if(modVersion != null && !modVersion.isEmpty()) builder.header("X-Mod-Version", modVersion);
      return builder;
   }
   
   private HttpResponse<byte[]> send(HttpRequest request) throws IOException, InterruptedException{
      long started = System.currentTimeMillis();
      HttpResponse<byte[]> response;
      try{
         response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
      }catch(IOException | InterruptedException e){
         ArcanaSkins.dev("{} {} failed after {}ms: {}", request.method(), request.uri(), System.currentTimeMillis() - started, e.toString());
         throw e;
      }
      ArcanaSkins.dev("{} {} -> {} in {}ms, {} bytes, RateLimit: {}", request.method(), request.uri(), response.statusCode(), System.currentTimeMillis() - started,
            response.body().length, response.headers().firstValue("RateLimit").orElse("none"));
      if(response.statusCode() == 200) return response;
      
      JsonObject body = new JsonObject();
      try{
         body = json(response);
      }catch(RuntimeException _){
      }
      String error = body.has("error") ? body.get("error").getAsString() : "http_" + response.statusCode();
      String message = body.has("message") ? body.get("message").getAsString() : "";
      long retryAfter = response.headers().firstValue("Retry-After").map(ArcanaSkinApi::parseRetryAfter).orElse(-1L);
      throw new ApiException(response.statusCode(), error, message, body, retryAfter);
   }
   
   private static long parseRetryAfter(String value){
      try{
         return Long.parseLong(value.trim());
      }catch(NumberFormatException notSeconds){
         return -1L;
      }
   }
   
   private static JsonObject json(HttpResponse<byte[]> response){
      return JsonParser.parseString(new String(response.body(), StandardCharsets.UTF_8)).getAsJsonObject();
   }
   
   private static List<JsonObject> objects(JsonArray array){
      List<JsonObject> list = new ArrayList<>();
      array.forEach(element -> list.add(element.getAsJsonObject()));
      return list;
   }
   
   static Map<String, byte[]> unzip(byte[] zip) throws IOException{
      Map<String, byte[]> files = new LinkedHashMap<>();
      long total = 0;
      try(ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip), StandardCharsets.UTF_8)){
         for(ZipEntry entry; (entry = in.getNextEntry()) != null; ){
            if(entry.isDirectory()) continue;
            String name = entry.getName();
            boolean unsafe = name.startsWith("/") || name.contains("\\") || name.contains(":")
                  || List.of(name.split("/")).contains("..");
            if(unsafe) throw new IOException("unsafe path in bundle: " + name);
            byte[] content = in.readNBytes(MAX_BUNDLE_BYTES);
            total += content.length;
            if(total >= MAX_BUNDLE_BYTES) throw new IOException("bundle is too large");
            files.put(name, content);
         }
      }
      return files;
   }
   
   static String computeSkinHash(Map<String, byte[]> files){
      StringBuilder lines = new StringBuilder();
      for(Map.Entry<String, byte[]> file : new TreeMap<>(files).entrySet()){
         byte[] content = file.getValue();
         if(file.getKey().equals("manifest.json")){
            // "hash" is always the second line; drop it together with its line break.
            String manifest = new String(content, StandardCharsets.UTF_8);
            int lineStart = manifest.indexOf('\n') + 1;
            int lineEnd = manifest.indexOf('\n', lineStart) + 1;
            content = (manifest.substring(0, lineStart) + manifest.substring(lineEnd)).getBytes(StandardCharsets.UTF_8);
         }
         lines.append(sha256Hex(content)).append("  ").append(file.getKey()).append('\n');
      }
      return sha256Hex(lines.toString().getBytes(StandardCharsets.UTF_8));
   }
   
   static String sha256Hex(byte[] data){
      try{
         return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
      }catch(NoSuchAlgorithmException e){
         throw new IllegalStateException(e);
      }
   }
   
   public static Map<String, Map<String, String>> translations(JsonObject skin){
      Map<String, Map<String, String>> byLanguage = new LinkedHashMap<>();
      for(Map.Entry<String, JsonElement> language : skin.getAsJsonObject("translation_keys").entrySet()){
         Map<String, String> entries = byLanguage.computeIfAbsent(language.getKey(), key -> new LinkedHashMap<>());
         for(JsonElement group : language.getValue().getAsJsonArray()){
            group.getAsJsonObject().entrySet().forEach(entry -> entries.put(entry.getKey(), entry.getValue().getAsString()));
         }
      }
      return byLanguage;
   }
   
   public static List<Pair<String, String>> attributions(JsonObject skin){
      List<Pair<String, String>> credits = new ArrayList<>();
      for(JsonElement credit : skin.getAsJsonArray("attributions")){
         credit.getAsJsonObject().entrySet().forEach(entry -> credits.add(Pair.of(entry.getKey(), entry.getValue().getAsString())));
      }
      return credits;
   }
   
   // TODO this prob should be in BorisLib at some point
   // "#7a04c9" -> 0x7a04c9
   public static int colorStringToInt(String hex){
      return Integer.parseInt(hex.substring(1), 16);
   }
}
