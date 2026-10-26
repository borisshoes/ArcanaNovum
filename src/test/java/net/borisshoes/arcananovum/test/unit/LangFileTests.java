package net.borisshoes.arcananovum.test.unit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** The data pack and resource pack copies of each lang file must stay identical (see AGENTS.md) */
public class LangFileTests {
   
   @ParameterizedTest
   @ValueSource(strings = {"en_us", "es_es"})
   public void dataAndAssetLangFilesMatch(String lang) throws IOException{
      Map<String, String> data = readLang("/data/arcananovum/lang/" + lang + ".json");
      Map<String, String> assets = readLang("/assets/arcananovum/lang/" + lang + ".json");
      
      Set<String> onlyData = new TreeSet<>(data.keySet());
      onlyData.removeAll(assets.keySet());
      Set<String> onlyAssets = new TreeSet<>(assets.keySet());
      onlyAssets.removeAll(data.keySet());
      List<String> different = new ArrayList<>();
      for(String key : data.keySet()){
         if(assets.containsKey(key) && !assets.get(key).equals(data.get(key))) different.add(key);
      }
      
      assertTrue(onlyData.isEmpty(), lang + " keys only in the data pack: " + onlyData);
      assertTrue(onlyAssets.isEmpty(), lang + " keys only in the resource pack: " + onlyAssets);
      assertTrue(different.isEmpty(), lang + " keys with different text: " + different);
   }
   
   @ParameterizedTest
   @ValueSource(strings = {"en_us", "es_es"})
   public void langFilesHaveNoDuplicateKeys(String lang) throws IOException{
      for(String path : List.of("/data/arcananovum/lang/" + lang + ".json", "/assets/arcananovum/lang/" + lang + ".json")){
         String text = readText(path);
         Set<String> seen = new HashSet<>();
         Set<String> duplicates = new TreeSet<>();
         for(String line : text.split("\n")){
            String trimmed = line.trim();
            if(!trimmed.startsWith("\"")) continue;
            int end = trimmed.indexOf("\":");
            if(end < 0) continue;
            String key = trimmed.substring(1, end);
            if(!seen.add(key)) duplicates.add(key);
         }
         assertTrue(duplicates.isEmpty(), path + " has duplicate keys (only the last one is used): " + duplicates);
      }
   }
   
   static Map<String, String> readLang(String path) throws IOException{
      JsonObject json = JsonParser.parseString(readText(path)).getAsJsonObject();
      Map<String, String> map = new TreeMap<>();
      for(Map.Entry<String, JsonElement> entry : json.entrySet()) map.put(entry.getKey(), entry.getValue().getAsString());
      return map;
   }
   
   private static String readText(String path) throws IOException{
      try(InputStream in = LangFileTests.class.getResourceAsStream(path)){
         assertNotNull(in, path + " not found");
         return new String(in.readAllBytes(), StandardCharsets.UTF_8);
      }
   }
}
