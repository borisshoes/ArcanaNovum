package net.borisshoes.arcananovum.skins;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ArcanaSkin implements StringRepresentable {
   
   // Skin ids with custom behavior
   public static final String ZEPHOS_LANCE = "zephos_lance";
   
   private static final String FALLBACK_LANGUAGE = "en_us";
   
   private final ArcanaItem arcanaItem;
   private final Identifier id;
   private final String hash;
   private final List<String> files;
   private final Map<String, Map<String, String>> translations;
   private final int primaryColor;
   private final int secondaryColor;
   private final List<Pair<String, String>> attributions;
   private final Set<String> blockParts; // The block parts this skin replaces, from its files in models/block/skins/<skin id>/
   
   private ArcanaSkin(ArcanaItem arcanaItem, Identifier id, String hash, List<String> files, Map<String, Map<String, String>> translations, int primaryColor, int secondaryColor, List<Pair<String, String>> attributions){
      this.arcanaItem = arcanaItem;
      this.id = id;
      this.hash = hash;
      this.files = files;
      this.translations = translations;
      this.primaryColor = primaryColor;
      this.secondaryColor = secondaryColor;
      this.attributions = attributions;
      
      String partFolder = "/assets/" + id.getNamespace() + "/models/block/skins/" + id.getPath() + "/";
      Set<String> parts = new TreeSet<>();
      for(String file : files){
         if(!file.startsWith(partFolder) || !file.endsWith(".json")) continue;
         String part = file.substring(partFolder.length(), file.length() - ".json".length());
         if(!part.isEmpty() && !part.contains("/")) parts.add(part);
      }
      this.blockParts = Collections.unmodifiableSet(parts);
   }
   
   @Nullable
   static ArcanaSkin parse(JsonObject skinObj){
      try{
         ArcanaItem arcanaItem = ArcanaRegistry.getArcanaItem(skinObj.get("item_id").getAsString());
         if(arcanaItem == null){
            ArcanaSkins.dev("Skipping skin {}: this mod version has no item {}", skinObj.get("id"), skinObj.get("item_id"));
            return null;
         }
         List<String> files = new ArrayList<>();
         skinObj.getAsJsonArray("files").forEach(file -> files.add(file.getAsString()));
         return new ArcanaSkin(arcanaItem, ArcanaRegistry.arcanaId(skinObj.get("id").getAsString()), skinObj.get("hash").getAsString(), List.copyOf(files),
               ArcanaSkinApi.translations(skinObj),
               ArcanaSkinApi.colorStringToInt(skinObj.get("primary_color").getAsString()),
               ArcanaSkinApi.colorStringToInt(skinObj.get("secondary_color").getAsString()),
               List.copyOf(ArcanaSkinApi.attributions(skinObj)));
      }catch(RuntimeException e){
         ArcanaSkins.debug("Skipping malformed skin entry " + skinObj, e);
         return null;
      }
   }
   
   public ArcanaItem getArcanaItem(){
      return arcanaItem;
   }
   
   public Identifier getId(){
      return id;
   }
   
   public String getHash(){
      return hash;
   }
   
   public List<String> getFiles(){
      return files;
   }
   
   public Map<String, Map<String, String>> getTranslations(){
      return translations;
   }
   
   public boolean is(String skinId){
      return id.getPath().equals(skinId);
   }
   
   public Identifier getModelId(){
      return ArcanaRegistry.arcanaId("skins/" + id.getPath());
   }
   
   public boolean hasEquipmentAsset(){
      return files.contains("/assets/" + id.getNamespace() + "/equipment/skins/" + id.getPath() + ".json");
   }
   
   public Set<String> getBlockParts(){
      return blockParts;
   }
   
   // A replacement keeps the name of the part it replaces, in the skin's own folder: models/block/skins/<skin id>/<part>.json
   @Nullable
   public Identifier getBlockPartModel(String part){
      return blockParts.contains(part) ? ArcanaRegistry.arcanaId("block/skins/" + id.getPath() + "/" + part) : null;
   }
   
   public int getPrimaryColor(){
      return primaryColor;
   }
   
   public int getSecondaryColor(){
      return secondaryColor;
   }
   
   public String getNameTranslationKey(){
      return "skin." + id.getNamespace() + "." + id.getPath() + ".name";
   }
   
   public String getDescriptionTranslationKey(){
      return "skin." + id.getNamespace() + "." + id.getPath() + ".description";
   }
   
   @Nullable
   private String getFallbackTranslation(String key){
      return translations.getOrDefault(FALLBACK_LANGUAGE, Map.of()).get(key);
   }
   
   public MutableComponent getName(){
      String fallback = getFallbackTranslation(getNameTranslationKey());
      return Component.translatableWithFallback(getNameTranslationKey(), fallback != null ? fallback : id.getPath());
   }
   
   public List<MutableComponent> getDescription(){
      List<MutableComponent> components = new ArrayList<>();
      String fullText = getFallbackTranslation(getDescriptionTranslationKey());
      if(fullText == null) return components;
      for(String line : fullText.split("\n")){
         components.add(Component.literal(line));
      }
      return components;
   }
   
   @SuppressWarnings("unchecked")
   public Pair<MutableComponent, MutableComponent>[] getAttributions(){
      Pair<MutableComponent, MutableComponent>[] pairs = new Pair[attributions.size()];
      for(int i = 0; i < pairs.length; i++){
         pairs[i] = Pair.of(Component.translatable(attributions.get(i).getFirst()), Component.literal(attributions.get(i).getSecond()));
      }
      return pairs;
   }
   
   public static List<ArcanaSkin> getAllSkinsForItem(ArcanaItem item){
      return SkinCatalog.getInstalled().getSkinsForItem(item);
   }
   
   public static String normalizeId(@Nullable String str){
      if(str == null) return "";
      String prefix = ArcanaNovum.MOD_ID + ":";
      return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
   }
   
   @Nullable
   public static ArcanaSkin getSkinFromString(@Nullable String str){
      String skinId = normalizeId(str);
      return skinId.isEmpty() ? null : SkinCatalog.getInstalled().get(skinId);
   }
   
   @Override
   public String getSerializedName(){
      return id.toString();
   }
   
   @Override
   public boolean equals(Object obj){
      return obj instanceof ArcanaSkin other && id.equals(other.id);
   }
   
   @Override
   public int hashCode(){
      return id.hashCode();
   }
   
   @Override
   public String toString(){
      return id.toString();
   }
}
