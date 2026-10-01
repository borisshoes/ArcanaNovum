package net.borisshoes.arcananovum.skins;

import com.google.gson.JsonObject;
import net.borisshoes.arcananovum.core.ArcanaItem;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

public record SkinCatalog(String packHash, Map<String, ArcanaSkin> byId) {
   public static final SkinCatalog EMPTY = new SkinCatalog("", Map.of());
   private static final AtomicReference<SkinCatalog> INSTALLED = new AtomicReference<>(EMPTY);
   
   public static SkinCatalog getInstalled(){
      return INSTALLED.get();
   }
   
   static void setInstalled(SkinCatalog catalog){
      INSTALLED.set(catalog);
   }
   
   static void loadInstalledFromDisk(){
      SkinStore.Snapshot snapshot = SkinStore.readCurrentFiles();
      if(snapshot != null) setInstalled(snapshot.catalog());
   }
   
   static SkinCatalog of(String packHash, List<JsonObject> skins){
      Map<String, ArcanaSkin> byId = new TreeMap<>();
      for(JsonObject json : skins){
         ArcanaSkin skin = ArcanaSkin.parse(json);
         if(skin == null) continue;
         byId.put(skin.getId().getPath(), skin);
      }
      return new SkinCatalog(packHash, Collections.unmodifiableMap(byId));
   }
   
   @Nullable
   public ArcanaSkin get(String id){
      return byId.get(id);
   }
   
   public List<ArcanaSkin> getAll(){
      return byId.values().stream().toList();
   }
   
   public boolean isEmpty(){
      return byId.isEmpty();
   }
   
   public List<ArcanaSkin> getSkinsForItem(ArcanaItem item){
      return byId.values().stream().filter(skin -> skin.getArcanaItem().getId().equals(item.getId())).toList();
   }
   
   Map<String, Map<String, String>> translationsByLanguage(){
      Map<String, Map<String, String>> out = new TreeMap<>();
      for(ArcanaSkin skin : byId.values()){
         skin.getTranslations().forEach((lang, entries) -> out.computeIfAbsent(lang, k -> new TreeMap<>()).putAll(entries));
      }
      return out;
   }
}
