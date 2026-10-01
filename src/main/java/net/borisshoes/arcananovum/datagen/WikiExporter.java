package net.borisshoes.arcananovum.datagen;

import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.*;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.IngredientCondition;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.transmutation.*;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.skins.SkinCatalog;
import net.borisshoes.arcananovum.research.*;
import net.borisshoes.arcananovum.utils.ConfigUnits;
import net.borisshoes.borislib.config.ConfigValue;
import net.borisshoes.borislib.config.IConfigSetting;
import net.borisshoes.borislib.config.values.*;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

// THIS CLASS WAS AI GENERATED
/**
 * Builds the wiki export described by the contract in {@code wikitools/export-contract.md}.
 * Everything is read from the live registries so the output matches what the game uses.
 * The output is deterministic: every collection is sorted and no timestamps are written.
 * All text is plain ({@link Component#getString()}), with multi-line text split into arrays.
 */
public class WikiExporter {
   public static final String SCHEMA_VERSION = "4.1.0";
   public static final String EXPORT_FOLDER = "arcana-export";
   public static final String EXPORT_FILE = "export.json";
   
   private static final String KEY_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
   private static final Set<String> EQUIPMENT_SLOTS = Set.of("head", "chest", "legs", "feet", "offhand", "mainhand");
   
   private final MinecraftServer server;
   private final RegistryAccess registryAccess;
   private final List<String> warnings = new ArrayList<>();
   
   private WikiExporter(MinecraftServer server){
      this.server = server;
      this.registryAccess = server.registryAccess();
   }
   
   public record Result(Path path, boolean defaultConfig, int items, int modItems, int augments, int achievements, int research, int configs, int skins, int transmutations, List<String> warnings) {}
   
   public static Result export(MinecraftServer server) throws IOException{
      return new WikiExporter(server).run();
   }
   
   private Result run() throws IOException{
      boolean defaultConfig = isDefaultConfig();
      JsonObject root = new JsonObject();
      root.addProperty("schema_version", SCHEMA_VERSION);
      root.add("mod", buildMod(defaultConfig));
      root.add("rarities", buildRarities());
      JsonArray items = buildItems();
      JsonArray modItems = buildModItems();
      JsonArray augments = buildAugments();
      JsonArray achievements = buildAchievements();
      JsonArray research = buildResearch();
      JsonArray configs = buildConfigs();
      JsonArray skins = buildSkins();
      JsonArray transmutations = buildTransmutations();
      root.add("items", items);
      root.add("mod_items", modItems);
      root.add("augments", augments);
      root.add("achievements", achievements);
      root.add("research", research);
      root.add("configs", configs);
      root.add("skins", skins);
      root.add("transmutations", transmutations);
   
      Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create(); // The contract requires explicit nulls
      Path dir = FabricLoader.getInstance().getGameDir().resolve(EXPORT_FOLDER);
      Files.createDirectories(dir);
      Path file = dir.resolve(EXPORT_FILE);
      Files.writeString(file, gson.toJson(root).replace("\r\n", "\n") + "\n", StandardCharsets.UTF_8);
   
      for(String warning : warnings){
         ArcanaNovum.log(1, "[Wiki Export] " + warning);
      }
      return new Result(file, defaultConfig, items.size(), modItems.size(), augments.size(), achievements.size(), research.size(), configs.size(), skins.size(), transmutations.size(), warnings);
   }
   
   // ========== Mod & Rarities ==========
   
   private JsonObject buildMod(boolean defaultConfig){
      JsonObject mod = new JsonObject();
      mod.addProperty("id", MOD_ID);
      mod.addProperty("version", FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("unknown"));
      mod.addProperty("minecraft_version", SharedConstants.getCurrentVersion().name());
      mod.addProperty("default_config", defaultConfig);
      return mod;
   }
   
   // Augment text interpolates live config values, so the export is only valid on a default config
   private boolean isDefaultConfig(){
      boolean allDefault = true;
      for(IConfigSetting<?> setting : ArcanaConfig.CONFIG_SETTINGS.stream().sorted(Comparator.comparing(IConfigSetting::getName)).toList()){
         try{
            Object defaultValue = readField(ConfigValue.class, setting.makeConfigValue(), "defaultValue");
            Object currentValue = ArcanaNovum.CONFIG.getValue(setting);
            if(!Objects.equals(defaultValue, currentValue)){
               warnings.add("Config " + setting.getName() + " is " + currentValue + ", default is " + defaultValue);
               allDefault = false;
            }
         }catch(Exception e){
            warnings.add("Could not compare config " + setting.getName() + " to its default: " + e);
            allDefault = false;
         }
      }
      return allDefault;
   }
   
   private JsonArray buildRarities(){
      JsonArray rarities = new JsonArray();
      Arrays.stream(ArcanaRarity.values()).sorted(Comparator.comparingInt(r -> r.rarity)).forEach(rarity -> {
         JsonObject obj = new JsonObject();
         obj.addProperty("id", rarity.id);
         obj.addProperty("name", Component.translatableWithFallback(rarity.getTranslationKey(), rarity.label).getString());
         obj.addProperty("color", hexColor(ArcanaRarity.getColor(rarity).getValue()));
         rarities.add(obj);
      });
      return rarities;
   }
   
   // ========== Items ==========
   
   private JsonArray buildItems(){
      Map<String, ArcanaRecipe> defaultRecipes = loadRecipes("default");
      Map<String, ArcanaRecipe> classicRecipes = loadRecipes("classic");
   
      JsonArray items = new JsonArray();
      List<ArcanaItem> arcanaItems = ArcanaRegistry.ARCANA_ITEMS.stream().sorted(Comparator.comparing(ArcanaItem::getId)).toList();
      for(ArcanaItem arcanaItem : arcanaItems){
         try{
            items.add(buildItem(arcanaItem, defaultRecipes.get(arcanaItem.getId()), classicRecipes.get(arcanaItem.getId())));
         }catch(Exception e){
            warnings.add("Failed to export item " + arcanaItem.getId() + ": " + e);
         }
      }
      return items;
   }
   
   private JsonObject buildItem(ArcanaItem arcanaItem, ArcanaRecipe defaultRecipe, ArcanaRecipe classicRecipe){
      ItemStack prefItem = arcanaItem.getPrefItem();
      JsonObject obj = new JsonObject();
      obj.addProperty("id", arcanaItem.getId());
      obj.addProperty("name", arcanaItem.getNameString());
      obj.addProperty("display_name", arcanaDisplayName(arcanaItem));
      obj.addProperty("rarity", arcanaItem.getRarity().id);
      obj.addProperty("type", itemType(arcanaItem));
      obj.add("categories", buildCategories(arcanaItem));
      obj.add("base_item", itemRef(arcanaItem.getVanillaItem(), null, List.of()));
      obj.addProperty("max_stack", Math.clamp(arcanaItem.getItem().getDefaultMaxStackSize(), 1, 99));
   
      JsonArray research = new JsonArray();
      // Reference tasks by their own id, which can differ from their registry key (player data and lang files use the id)
      LinkedHashSet<String> taskIds = new LinkedHashSet<>();
      for(ResourceKey<ResearchTask> key : arcanaItem.getResearchTasks()){
         ResearchTask task = ResearchTasks.RESEARCH_TASKS.getValue(key);
         if(task == null){
            warnings.add("Item " + arcanaItem.getId() + " requires unregistered research task " + key.identifier());
            continue;
         }
         taskIds.add(task.getId());
      }
      taskIds.forEach(research::add);
      obj.add("research", research);
   
      List<Component> loreLines;
      try{
         loreLines = arcanaItem.getItemLore(prefItem);
      }catch(Exception e){
         loreLines = arcanaItem.getItemLore(null);
      }
      obj.add("tooltip", lines(loreLines, false));
   
      obj.add("lore", buildBookLore(arcanaItem));
   
      JsonObject recipes = new JsonObject();
      recipes.add("default", defaultRecipe == null ? JsonNull.INSTANCE : buildRecipe(defaultRecipe));
      recipes.add("classic", classicRecipe == null ? JsonNull.INSTANCE : buildRecipe(classicRecipe));
      obj.add("recipes", recipes);
   
      JsonArray attributions = buildAttributions(arcanaItem.getAttributions());
      if(!attributions.isEmpty()) obj.add("attributions", attributions);
   
      if(arcanaItem instanceof RunicArrow){
         PotionContents contents = prefItem.get(DataComponents.POTION_CONTENTS);
         if(contents != null && contents.customColor().isPresent()){
            JsonObject arrow = new JsonObject();
            arrow.addProperty("tip_color", hexColor(contents.customColor().get()));
            obj.add("arrow", arrow);
         }
      }
   
      ArcanaRarity catalystTier = catalystTier(arcanaItem);
      if(catalystTier != null){
         JsonObject catalyst = new JsonObject();
         catalyst.addProperty("tier", catalystTier.id);
         obj.add("catalyst", catalyst);
      }
   
      JsonObject equipment = buildEquipment(prefItem);
      if(equipment != null) obj.add("equipment", equipment);
   
      if(arcanaItem instanceof MultiblockCore core){
         JsonObject multiblock = buildMultiblock(arcanaItem, core);
         if(multiblock != null) obj.add("multiblock", multiblock);
      }
      return obj;
   }
   
   private String itemType(ArcanaItem arcanaItem){
      if(arcanaItem instanceof RunicArrow) return "runic_arrow";
      if(catalystTier(arcanaItem) != null) return "catalyst";
      if(arcanaItem instanceof ArcanaBlock) return "block";
      if(arcanaItem.hasCategory(ArcaneTomeGui.TomeFilter.EQUIPMENT)) return "equipment";
      return "item";
   }
   
   private ArcanaRarity catalystTier(ArcanaItem arcanaItem){
      for(ArcanaRarity rarity : ArcanaRarity.values()){
         if(ArcanaRarity.getAugmentCatalyst(rarity) == arcanaItem) return rarity;
      }
      return null;
   }
   
   private JsonArray buildCategories(ArcanaItem arcanaItem){
      Set<ArcaneTomeGui.TomeFilter> rarityFilters = new HashSet<>();
      for(ArcanaRarity rarity : ArcanaRarity.values()){
         rarityFilters.add(ArcanaRarity.getTomeFilter(rarity));
      }
      TreeSet<String> categories = new TreeSet<>();
      if(arcanaItem.getCategories() != null){
         for(ArcaneTomeGui.TomeFilter filter : arcanaItem.getCategories()){
            if(filter == null || rarityFilters.contains(filter)) continue;
            if(filter.getColoredLabel().getContents() instanceof TranslatableContents contents){
               String key = contents.getKey();
               categories.add(key.substring(key.lastIndexOf('.') + 1));
            }
         }
      }
      JsonArray arr = new JsonArray();
      categories.forEach(arr::add);
      return arr;
   }
   
   private JsonArray buildBookLore(ArcanaItem arcanaItem){
      // Each page is [title, (rarity header), body...]. The title and rarity are exported elsewhere,
      // so drop them and export the rest of each page verbatim, one string per line.
      JsonArray pages = new JsonArray();
      for(List<Component> page : arcanaItem.getBookLore()){
         StringBuilder text = new StringBuilder();
         for(int i = 1; i < page.size(); i++){
            text.append(page.get(i).getString());
         }
         String body = text.toString();
         if(body.startsWith("\nRarity: ")){
            int end = body.indexOf('\n', 1);
            body = end < 0 ? "" : body.substring(end);
         }
         if(body.startsWith("\n")) body = body.substring(1); // Separator between the title and the body
         JsonArray lines = new JsonArray();
         splitLines(body).forEach(lines::add);
         pages.add(lines);
      }
      return pages;
   }
   
   private JsonArray buildAttributions(Pair<MutableComponent, MutableComponent>[] attributions){
      JsonArray arr = new JsonArray();
      for(Pair<MutableComponent, MutableComponent> attribution : attributions){
         Component roleText = attribution.getFirst();
         Component nameText = attribution.getSecond();
         String role = "other";
         if(roleText.getContents() instanceof TranslatableContents contents){
            role = switch(contents.getKey()){
               case "credits_and_attribution.arcananovum.texture_by" -> "texture";
               case "credits_and_attribution.arcananovum.model_by" -> "model";
               case "credits_and_attribution.arcananovum.code_by" -> "code";
               case "credits_and_attribution.arcananovum.inspired_by" -> "inspiration";
               case "credits_and_attribution.arcananovum.skin_by" -> "skin";
               default -> "other";
            };
         }
         JsonObject obj = new JsonObject();
         obj.addProperty("role", role);
         obj.addProperty("name", nameText.getString());
         arr.add(obj);
      }
      return arr;
   }
   
   private JsonObject buildEquipment(ItemStack stack){
      Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
      if(equippable == null) return null;
      EquipmentSlot slot = equippable.slot();
      if(!EQUIPMENT_SLOTS.contains(slot.getName())) return null;
      ItemAttributeModifiers attrs = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
   
      JsonObject obj = new JsonObject();
      obj.addProperty("slot", slot.getName());
      obj.add("armor", number(Math.max(0, attrs.compute(Attributes.ARMOR, 0, slot))));
      obj.add("toughness", number(Math.max(0, attrs.compute(Attributes.ARMOR_TOUGHNESS, 0, slot))));
      obj.add("knockback_resistance", number(Math.max(0, attrs.compute(Attributes.KNOCKBACK_RESISTANCE, 0, slot))));
   
      ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(stack);
      List<Pair<ResourceKey<Enchantment>, Integer>> list = new ArrayList<>();
      enchants.entrySet().forEach(entry -> entry.getKey().unwrapKey().ifPresent(key -> list.add(Pair.of(key, entry.getIntValue()))));
      obj.add("enchantments", enchantmentList(list, true));
      return obj;
   }
   
   // ========== Mod Items ==========
   
   private JsonArray buildModItems(){
      JsonArray arr = new JsonArray();
      List<Map.Entry<ResourceKey<Item>, Item>> entries = ArcanaRegistry.ITEMS.entrySet().stream()
            .filter(entry -> ArcanaRegistry.getArcanaItem(entry.getKey().identifier().getPath()) == null)
            .sorted(Comparator.comparing(entry -> entry.getKey().identifier().getPath()))
            .toList();
      for(Map.Entry<ResourceKey<Item>, Item> entry : entries){
         String id = entry.getKey().identifier().getPath();
         Item item = entry.getValue();
         try{
            ItemStack stack = item.getDefaultInstance();
            Item baseItem = item instanceof PolymerItem polymerItem ? polymerItem.getPolymerItem(stack, PacketContext.get()) : item;
   
            JsonObject obj = new JsonObject();
            obj.addProperty("id", id);
            obj.addProperty("display_name", modItemName(item));
            obj.add("base_item", itemRef(baseItem, null, List.of()));
            obj.addProperty("max_stack", Math.clamp(item.getDefaultMaxStackSize(), 1, 99));
            obj.add("tooltip", lines(stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines(), false));
            arr.add(obj);
         }catch(Exception e){
            warnings.add("Failed to export mod item " + id + ": " + e);
         }
      }
      return arr;
   }
   
   // ========== Recipes ==========
   
   private Map<String, ArcanaRecipe> loadRecipes(String folder){
      Map<String, ArcanaRecipe> recipes = new HashMap<>();
      Path dir = FabricLoader.getInstance().getConfigDir().resolve("arcananovum").resolve("recipes").resolve(folder);
      if(!Files.isDirectory(dir)){
         warnings.add("Recipe folder not found: " + dir);
         return recipes;
      }
      List<Path> files;
      try(Stream<Path> paths = Files.walk(dir)){
         files = paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".json")).sorted().toList();
      }catch(IOException e){
         warnings.add("Could not read recipe folder " + dir + ": " + e.getMessage());
         return recipes;
      }
      for(Path file : files){
         try{
            JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            if(!json.has("type") || !json.get("type").getAsString().equals("arcananovum:forging_recipe")) continue;
            ArcanaRecipe recipe = ArcanaRecipe.fromJson(json);
            if(recipe == null || !recipe.getOutputId().getNamespace().equals(MOD_ID)) continue;
            String id = recipe.getOutputId().getPath();
            if(recipes.containsKey(id)){
               warnings.add("Multiple " + folder + " recipes for " + id + ", using the first (" + dir.relativize(file) + " ignored)");
               continue;
            }
            recipes.put(id, recipe);
         }catch(Exception e){
            warnings.add("Failed to read recipe " + file + ": " + e.getMessage());
         }
      }
      return recipes;
   }
   
   private JsonObject buildRecipe(ArcanaRecipe recipe){
      ArcanaIngredient[][] grid = recipe.getIngredients();
      List<String> seen = new ArrayList<>();
      List<JsonObject> legendEntries = new ArrayList<>();
      JsonArray rows = new JsonArray();
      for(ArcanaIngredient[] gridRow : grid){
         StringBuilder row = new StringBuilder();
         for(ArcanaIngredient ingredient : gridRow){
            if(ingredient == null || ingredient == ArcanaIngredient.EMPTY || ingredient.getCount() <= 0 || ingredient.ingredientAsStack().isEmpty()){
               row.append('.');
               continue;
            }
            JsonObject json = ingredientJson(ingredient);
            String serialized = json.toString();
            int index = seen.indexOf(serialized);
            if(index < 0){
               seen.add(serialized);
               legendEntries.add(json);
               index = seen.size() - 1;
            }
            row.append((char) ('a' + index));
         }
         rows.add(row.toString());
      }
   
      JsonObject legend = new JsonObject();
      for(int i = 0; i < legendEntries.size(); i++){
         legend.add(String.valueOf((char) ('a' + i)), legendEntries.get(i));
      }
   
      JsonArray requirements = new JsonArray();
      recipe.getForgeRequirementList().stream().map(ArcanaItem::getId).distinct().sorted().forEach(requirements::add);
   
      JsonObject obj = new JsonObject();
      obj.addProperty("type", "starlight_forge");
      obj.add("grid", rows);
      obj.add("legend", legend);
      obj.add("forge_requirements", requirements);
      return obj;
   }
   
   private JsonObject ingredientJson(ArcanaIngredient ingredient){
      JsonObject requires = requirements(ingredient);
      List<Either<Item, TagKey<Item>>> accepted = ingredient.getAcceptedItems();
      if(accepted.size() == 1 && accepted.getFirst().right().isPresent()){
         JsonObject obj = tagIngredient(accepted.getFirst().right().get(), ingredient.getCount());
         if(requires != null) obj.add("requires", requires);
         return obj;
      }
      if(accepted.size() > 1){
         warnings.add("Ingredient accepting several items/tags " + accepted + " exported as its example item only");
      }
   
      // The name is the item type's name: what the contents must be goes in `requires`
      JsonObject obj = itemIngredient(ingredient.ingredientAsStack().getItem(), ingredient.getCount());
      List<IngredientCondition> conditions = ingredient.getConditions();
      if(!conditions.isEmpty()) obj.add("conditions", conditionsJson(conditions));
      if(requires != null) obj.add("requires", requires);
      return obj;
   }
   
   /**
    * The minimum contents an ingredient checks for, mirroring the predicates built by
    * {@link ArcanaIngredient#withPotion}, {@link ArcanaIngredient#withEffects} and {@link ArcanaIngredient#withEnchantments}:
    * every effect at or above its amplifier and duration, every enchantment at or above its level.
    */
   private JsonObject requirements(ArcanaIngredient ingredient){
      List<MobEffectInstance> effectInstances = new ArrayList<>();
      if(ingredient.getPotion() != null) effectInstances.addAll(ingredient.getPotion().value().getEffects());
      effectInstances.addAll(ingredient.getEffects());
      TreeMap<String, JsonObject> effects = new TreeMap<>();
      for(MobEffectInstance instance : effectInstances){
         Holder<MobEffect> effect = instance.getEffect();
         String id = effect.unwrapKey().orElseThrow().identifier().toString();
         JsonObject obj = new JsonObject();
         obj.addProperty("id", id);
         obj.addProperty("name", effect.value().getDisplayName().getString());
         obj.addProperty("min_amplifier", Math.max(0, instance.getAmplifier()));
         // Instant effects have no meaningful duration, so any duration passes the check
         if(!effect.value().isInstantaneous() && instance.getDuration() > 1){
            obj.addProperty("min_duration_ticks", instance.getDuration());
         }
         if(effects.putIfAbsent(id, obj) != null){
            warnings.add("Ingredient requires effect " + id + " twice, keeping the first requirement");
         }
      }
   
      TreeMap<String, JsonObject> enchantments = new TreeMap<>();
      for(Pair<ResourceKey<Enchantment>, Integer> pair : ingredient.getEnchantments()){
         String id = pair.getFirst().identifier().toString();
         JsonObject obj = new JsonObject();
         obj.addProperty("id", id);
         obj.addProperty("name", registryAccess.lookupOrThrow(Registries.ENCHANTMENT).get(pair.getFirst())
               .map(holder -> holder.value().description().getString())
               .orElse(pair.getFirst().identifier().getPath()));
         obj.addProperty("min_level", Math.max(1, pair.getSecond()));
         if(enchantments.putIfAbsent(id, obj) != null){
            warnings.add("Ingredient requires enchantment " + id + " twice, keeping the first requirement");
         }
      }
   
      if(effects.isEmpty() && enchantments.isEmpty()) return null;
      JsonObject requires = new JsonObject();
      if(!effects.isEmpty()){
         JsonArray arr = new JsonArray();
         effects.values().forEach(arr::add);
         requires.add("effects", arr);
      }
      if(!enchantments.isEmpty()){
         JsonArray arr = new JsonArray();
         enchantments.values().forEach(arr::add);
         requires.add("enchantments", arr);
      }
      return requires;
   }
   
   private JsonObject itemIngredient(Item item, int count){
      JsonObject ref = itemRef(item, null, List.of());
      JsonObject obj = new JsonObject();
      obj.add("id", ref.get("id"));
      obj.add("name", ref.get("name"));
      obj.addProperty("count", count);
      return obj;
   }
   
   private JsonObject tagIngredient(TagKey<Item> tag, int count){
      JsonObject obj = tagRef(tag);
      JsonElement items = obj.remove("items"); // Schema key order: tag, name, count, items
      obj.addProperty("count", count);
      obj.add("items", items);
      return obj;
   }
   
   // An item-or-tag list as used by transmutation recipes; the contract has no "any of" form
   private JsonObject eitherIngredient(List<Either<Item, TagKey<Item>>> options, int count, String where){
      if(options.isEmpty()) throw new IllegalStateException(where + " has no items");
      if(options.size() > 1){
         warnings.add(where + " accepts several items/tags " + options + ", exported as the first only");
      }
      Either<Item, TagKey<Item>> first = options.getFirst();
      return first.left().isPresent() ? itemIngredient(first.left().get(), count) : tagIngredient(first.right().orElseThrow(), count);
   }
   
   // ========== Multiblocks ==========
   
   private JsonObject buildMultiblock(ArcanaItem arcanaItem, MultiblockCore core){
      Multiblock multiblock = core.getMultiblock();
      if(multiblock == null){
         core.loadMultiblock();
         multiblock = core.getMultiblock();
      }
      if(multiblock == null){
         warnings.add("Multiblock for " + arcanaItem.getId() + " could not be loaded");
         return null;
      }
      int[][][] pattern = multiblock.getStatePattern(); // [x][y][z]
      List<BlockState> states = multiblock.getPaletteStates();
      int sizeX = pattern.length;
      int sizeY = pattern[0].length;
      int sizeZ = pattern[0][0].length;
   
      List<String> seen = new ArrayList<>();
      List<JsonObject> paletteEntries = new ArrayList<>();
      JsonArray layers = new JsonArray();
      for(int y = 0; y < sizeY; y++){
         JsonArray layer = new JsonArray();
         for(int z = 0; z < sizeZ; z++){
            StringBuilder row = new StringBuilder();
            for(int x = 0; x < sizeX; x++){
               int index = pattern[x][y][z];
               if(index < 0 || index >= states.size()){
                  row.append('.');
                  continue;
               }
               JsonObject block = blockJson(states.get(index));
               String serialized = block.toString();
               int key = seen.indexOf(serialized);
               if(key < 0){
                  seen.add(serialized);
                  paletteEntries.add(block);
                  key = seen.size() - 1;
               }
               if(key >= KEY_CHARS.length()){
                  warnings.add("Multiblock for " + arcanaItem.getId() + " has more than " + KEY_CHARS.length() + " distinct blocks, skipping");
                  return null;
               }
               row.append(KEY_CHARS.charAt(key));
            }
            layer.add(row.toString());
         }
         layers.add(layer);
      }
   
      JsonObject palette = new JsonObject();
      for(int i = 0; i < paletteEntries.size(); i++){
         palette.add(String.valueOf(KEY_CHARS.charAt(i)), paletteEntries.get(i));
      }
   
      JsonObject size = new JsonObject();
      size.addProperty("x", sizeX);
      size.addProperty("y", sizeY);
      size.addProperty("z", sizeZ);
   
      // The check offset points from the core to the structure's corner
      Vec3i offset = core.getCheckOffset();
      JsonObject corePos = new JsonObject();
      corePos.addProperty("x", -offset.getX());
      corePos.addProperty("y", -offset.getY());
      corePos.addProperty("z", -offset.getZ());
   
      JsonObject obj = new JsonObject();
      obj.add("size", size);
      obj.add("palette", palette);
      obj.add("layers", layers);
      obj.add("core", corePos);
      return obj;
   }
   
   private JsonObject blockJson(BlockState state){
      Block block = state.getBlock();
      JsonObject obj = new JsonObject();
      obj.addProperty("id", BuiltInRegistries.BLOCK.getKey(block).toString());
      obj.addProperty("name", block.getName().getString());
      List<Property<?>> properties = state.getProperties().stream().sorted(Comparator.comparing(Property::getName)).toList();
      if(!properties.isEmpty()){
         JsonObject stateObj = new JsonObject();
         for(Property<?> property : properties){
            stateObj.addProperty(property.getName(), propertyValue(state, property));
         }
         obj.add("state", stateObj);
      }
      return obj;
   }
   
   private static <T extends Comparable<T>> String propertyValue(BlockState state, Property<T> property){
      return property.getName(state.getValue(property));
   }
   
   // ========== Augments ==========
   
   private JsonArray buildAugments(){
      JsonArray arr = new JsonArray();
      List<ArcanaAugment> augments = ArcanaAugments.registry.values().stream().sorted(Comparator.comparing(a -> a.id)).toList();
      for(ArcanaAugment augment : augments){
         try{
            JsonObject obj = new JsonObject();
            obj.addProperty("id", augment.id);
            obj.addProperty("name", augment.getTranslatedName().getString());
            obj.addProperty("item", augment.getArcanaItem().getId());
            obj.addProperty("levels", augment.getTiers().length);
            JsonArray tiers = new JsonArray();
            for(ArcanaRarity tier : augment.getTiers()){
               tiers.add(tier.id);
            }
            obj.add("tiers", tiers);
            obj.add("text", lines(augment.getDescription(), true));
            arr.add(obj);
         }catch(Exception e){
            warnings.add("Failed to export augment " + augment.id + ": " + e);
         }
      }
      return arr;
   }
   
   // ========== Achievements ==========
   
   private JsonArray buildAchievements(){
      JsonArray arr = new JsonArray();
      List<ArcanaAchievement> achievements = ArcanaAchievements.ARCANA_ACHIEVEMENTS.values().stream().sorted(Comparator.comparing(a -> a.id)).toList();
      for(ArcanaAchievement achievement : achievements){
         try{
            String type;
            Integer goal = null;
            Integer timerSeconds = null;
            JsonArray conditions = null;
            switch(achievement){
               case TimedAchievement timed -> {
                  type = "timer";
                  goal = timed.getGoal();
                  timerSeconds = Math.max(1, Math.round(timed.getTimeFrame() / 20.0f));
               }
               case ProgressAchievement progress -> {
                  type = "progress";
                  goal = progress.getGoal();
               }
               case ConditionalsAchievement conditionals -> {
                  // Every condition must be met; the game shows each condition's key as its label
                  type = "conditions";
                  goal = conditionals.getConditions().size();
                  conditions = new JsonArray();
                  for(String key : new TreeSet<>(conditionals.getConditions().keySet())){
                     JsonObject cond = new JsonObject();
                     cond.addProperty("id", key);
                     cond.addProperty("text", sanitizeLine(key));
                     conditions.add(cond);
                  }
               }
               default -> type = "event";
            }
   
            JsonObject obj = new JsonObject();
            obj.addProperty("id", achievement.id);
            obj.addProperty("name", achievement.getTranslatedName().getString());
            if(achievement.getArcanaItem() == null){
               obj.add("item", JsonNull.INSTANCE);
            }else{
               obj.addProperty("item", achievement.getArcanaItem().getId());
            }
            obj.addProperty("type", type);
            obj.add("goal", goal == null ? JsonNull.INSTANCE : new JsonPrimitive(goal));
            obj.add("timer_seconds", timerSeconds == null ? JsonNull.INSTANCE : new JsonPrimitive(timerSeconds));
            obj.addProperty("xp", achievement.xpReward);
            obj.addProperty("skill_points", achievement.pointsReward);
            obj.add("text", lines(achievement.getDescription(), true));
            if(conditions != null) obj.add("conditions", conditions);
            arr.add(obj);
         }catch(Exception e){
            warnings.add("Failed to export achievement " + achievement.id + ": " + e);
         }
      }
      return arr;
   }
   
   // ========== Research ==========
   
   private JsonArray buildResearch(){
      JsonArray arr = new JsonArray();
      List<ResearchTask> tasks = ResearchTasks.RESEARCH_TASKS.stream().sorted(Comparator.comparing(ResearchTask::getId)).toList();
      for(ResearchTask task : tasks){
         try{
            JsonObject params = new JsonObject();
            String type = switch(task){
               case StatisticResearchTask<?> stat -> {
                  statisticParams(stat, params);
                  yield "statistic";
               }
               case ObtainResearchTask obtain -> {
                  if(obtain.getItem().right().isPresent()){
                     JsonObject tag = tagRef(obtain.getItem().right().get());
                     tag.entrySet().forEach(e -> params.add(e.getKey(), e.getValue()));
                  }else{
                     params.add("item", itemRef(obtain.getItem().left().orElseThrow(), null, List.of()));
                  }
                  params.addProperty("count", 1);
                  yield "obtain_item";
               }
               case EffectResearchTask effect -> {
                  // Any level of the effect completes the task, so no amplifier is given
                  params.addProperty("effect", effect.getEffect().unwrapKey().orElseThrow().identifier().toString());
                  params.addProperty("name", effect.getEffect().value().getDisplayName().getString());
                  yield "effect";
               }
               case AdvancementResearchTask advancement -> {
                  Identifier id = Identifier.parse(advancement.getAdvancementId());
                  params.addProperty("advancement", id.toString());
                  params.addProperty("name", advancementTitle(id));
                  yield "vanilla_advancement";
               }
               case ArcanaItemResearchTask unlock -> {
                  params.addProperty("item", unlock.getArcanaItem().getId());
                  yield "arcana_item_unlock";
               }
               default -> "custom";
            };
   
            JsonArray prerequisites = new JsonArray();
            task.getPreReqs().stream().map(ResearchTask::getId).distinct().forEach(prerequisites::add);
   
            JsonObject obj = new JsonObject();
            obj.addProperty("id", task.getId());
            obj.addProperty("name", task.getName().getString());
            obj.addProperty("type", type);
            obj.add("prerequisites", prerequisites);
            obj.add("display_item", itemRef(task.getDisplayItem()));
            obj.add("text", lines(task.getDescription(), true));
            obj.add("params", params);
            arr.add(obj);
         }catch(Exception e){
            warnings.add("Failed to export research task " + task.getId() + ": " + e);
         }
      }
      return arr;
   }
   
   private <T> void statisticParams(StatisticResearchTask<T> task, JsonObject params){
      Either<Identifier, Pair<StatType<T>, T>> data = task.getData();
      if(data.left().isPresent()){
         Identifier id = data.left().get();
         params.addProperty("stat", id.toString());
         params.addProperty("name", Component.translatable("stat." + id.toString().replace(':', '.')).getString());
      }else{
         Pair<StatType<T>, T> pair = data.right().orElseThrow();
         StatType<T> statType = pair.getFirst();
         T value = pair.getSecond();
         params.addProperty("stat", Stat.buildName(statType, value));
         String valueName = switch(value){
            case Block block -> block.getName().getString();
            case Item item -> new ItemStack(item).getItemName().getString();
            case EntityType<?> entityType -> entityType.getDescription().getString();
            default -> String.valueOf(statType.getRegistry().getKey(value));
         };
         // The killed/killed_by stat types translate to templates ("You killed %s %s"), not labels
         String typeName;
         if(statType == Stats.ENTITY_KILLED){
            typeName = "Times Killed";
         }else if(statType == Stats.ENTITY_KILLED_BY){
            typeName = "Times Killed By";
         }else{
            typeName = statType.getDisplayName().getString();
            if(typeName.matches(".*%(\\d+\\$)?[sd].*")){
               warnings.add("Stat type " + BuiltInRegistries.STAT_TYPE.getKey(statType) + " translates to a template: " + typeName);
            }
         }
         params.addProperty("name", typeName + ": " + valueName);
      }
      params.addProperty("amount", Math.max(1, task.getThreshold()));
   }
   
   private String advancementTitle(Identifier id){
      AdvancementHolder holder = server.getAdvancements().get(id);
      if(holder == null){
         warnings.add("Unknown advancement " + id);
         return id.toString();
      }
      return holder.value().display().map(DisplayInfo::title).map(Component::getString).orElse(id.toString());
   }
   
   // ========== Configs ==========
   
   private JsonArray buildConfigs(){
      // Configs are linked to items only where the mod declares it: an item's own related configs,
      // plus the configs its augments' descriptions reference
      Map<String, TreeSet<String>> configItems = new HashMap<>();
      for(ArcanaItem arcanaItem : ArcanaRegistry.ARCANA_ITEMS){
         if(arcanaItem.getRelatedConfigs() == null) continue;
         for(IConfigSetting<?> setting : arcanaItem.getRelatedConfigs()){
            if(setting == null){
               warnings.add("Item " + arcanaItem.getId() + " lists a null related config");
               continue;
            }
            configItems.computeIfAbsent(setting.getName(), k -> new TreeSet<>()).add(arcanaItem.getId());
         }
      }
      for(ArcanaAugment augment : ArcanaAugments.registry.values()){
         if(augment.getRelatedConfigs() == null || augment.getArcanaItem() == null) continue;
         for(Pair<IConfigSetting<?>, ConfigUnits> related : augment.getRelatedConfigs()){
            configItems.computeIfAbsent(related.getFirst().getName(), k -> new TreeSet<>()).add(augment.getArcanaItem().getId());
         }
      }
   
      JsonArray arr = new JsonArray();
      List<IConfigSetting<?>> settings = ArcanaConfig.CONFIG_SETTINGS.stream().sorted(Comparator.comparing(IConfigSetting::getName)).toList();
      for(IConfigSetting<?> setting : settings){
         try{
            ConfigValue<?> value = setting.makeConfigValue();
            Object defaultValue = readField(ConfigValue.class, value, "defaultValue");
            String type;
            JsonElement defaultJson;
            if(value instanceof ListConfigValue<?, ?> listValue){
               ConfigValue<?> elementType = (ConfigValue<?>) readField(ListConfigValue.class, listValue, "configType");
               String elementTypeName = scalarType(elementType);
               if(elementTypeName == null || elementTypeName.equals("bool")){
                  warnings.add("Config " + setting.getName() + " has a list type the contract cannot represent, skipping");
                  continue;
               }
               type = elementTypeName + "[]";
               JsonArray list = new JsonArray();
               for(Object element : (List<?>) defaultValue){
                  list.add(scalarJson(element, elementTypeName));
               }
               defaultJson = list;
            }else{
               type = scalarType(value);
               if(type == null){
                  warnings.add("Config " + setting.getName() + " has an unsupported type " + value.getClass().getSimpleName() + ", skipping");
                  continue;
               }
               defaultJson = scalarJson(defaultValue, type);
            }
   
            JsonObject obj = new JsonObject();
            obj.addProperty("key", setting.getName());
            obj.addProperty("type", type);
            obj.add("default", defaultJson);
            ConfigUnits units = ArcanaConfig.CONFIG_UNITS.get(ArcanaRegistry.arcanaId(setting.getId()));
            String unit = units == null || units == ConfigUnits.NONE ? null : units.name().toLowerCase(Locale.ROOT);
            if(unit != null) obj.addProperty("unit", unit);
            String commentKey = value.getComment(MOD_ID);
            String comment = Component.translatable(commentKey).getString();
            if(!comment.isBlank() && !comment.equals(commentKey)) obj.addProperty("description", comment);
            TreeSet<String> items = configItems.get(setting.getName());
            if(items != null && !items.isEmpty()){
               JsonArray itemArr = new JsonArray();
               items.forEach(itemArr::add);
               obj.add("items", itemArr);
            }
            arr.add(obj);
         }catch(Exception e){
            warnings.add("Failed to export config " + setting.getName() + ": " + e);
         }
      }
      return arr;
   }
   
   private static String scalarType(ConfigValue<?> value){
      return switch(value){
         case BooleanConfigValue ignored -> "bool";
         case IntConfigValue ignored -> "int";
         case DoubleConfigValue ignored -> "double";
         case FloatConfigValue ignored -> "double";
         case StringConfigValue ignored -> "string";
         case EnumConfigValue<?> ignored -> "string";
         default -> null;
      };
   }
   
   private static JsonElement scalarJson(Object value, String type){
      return switch(type){
         case "bool" -> new JsonPrimitive((Boolean) value);
         case "int" -> new JsonPrimitive(((Number) value).intValue());
         // Go through the decimal string so floats don't pick up binary noise (0.1f -> 0.10000000149)
         case "double" -> new JsonPrimitive(Double.parseDouble(value.toString()));
         default -> new JsonPrimitive(value instanceof StringRepresentable rep ? rep.getSerializedName() : String.valueOf(value));
      };
   }
   
   private static Object readField(Class<?> owner, Object instance, String name) throws ReflectiveOperationException{
      Field field = owner.getDeclaredField(name);
      field.setAccessible(true);
      return field.get(instance);
   }
   
   // ========== Skins ==========
   
   private JsonArray buildSkins(){
      JsonArray arr = new JsonArray();
      // Skins come from the skin API, so this exports whatever catalog is installed on the server running the export
      List<ArcanaSkin> skins = SkinCatalog.getInstalled().byId().values().stream().sorted(Comparator.comparing(skin -> skin.getId().getPath())).toList();
      for(ArcanaSkin skin : skins){
         try{
            JsonObject obj = new JsonObject();
            obj.addProperty("id", skin.getId().getPath());
            obj.addProperty("name", skin.getName().getString());
            obj.add("description", lines(skin.getDescription(), true));
            obj.addProperty("item", skin.getArcanaItem().getId());
            obj.addProperty("primary_color", hexColor(skin.getPrimaryColor()));
            obj.addProperty("secondary_color", hexColor(skin.getSecondaryColor()));
            obj.add("attributions", buildAttributions(skin.getAttributions()));
            arr.add(obj);
         }catch(Exception e){
            warnings.add("Failed to export skin " + skin.getSerializedName() + ": " + e);
         }
      }
      return arr;
   }
   
   // ========== Transmutations ==========
   
   private JsonArray buildTransmutations(){
      // Commutative and infusion recipes come from each pack's JSON. The rest are defined in code and
      // are the same under either pack, so they only have a default.
      Map<String, TransmutationRecipe> defaults = loadTransmutations("default");
      for(TransmutationRecipe recipe : RecipeManager.TRANSMUTATION_RECIPES){
         if(recipe instanceof CommutativeTransmutationRecipe || recipe instanceof InfusionTransmutationRecipe) continue;
         defaults.putIfAbsent(recipe.getId(), recipe);
      }
      Map<String, TransmutationRecipe> classics = loadTransmutations("classic");
      for(String id : classics.keySet()){
         if(!defaults.containsKey(id)) warnings.add("Transmutation " + id + " exists only in the classic pack, skipping");
      }
   
      JsonArray arr = new JsonArray();
      for(String id : new TreeSet<>(defaults.keySet())){
         TransmutationRecipe recipe = defaults.get(id);
         try{
            String type = transmutationType(recipe);
            JsonObject recipes = new JsonObject();
            recipes.add("default", transmutationBody(recipe));
            TransmutationRecipe classic = classics.get(id);
            if(classic != null && !transmutationType(classic).equals(type)){
               warnings.add("Classic transmutation " + id + " is a different type than the default, exporting classic as null");
               classic = null;
            }
            recipes.add("classic", classic == null ? JsonNull.INSTANCE : transmutationBody(classic));
   
            JsonObject obj = new JsonObject();
            obj.addProperty("id", id);
            obj.addProperty("type", type);
            obj.addProperty("name", sanitizeLine(recipe.getName().getString()));
            obj.add("recipes", recipes);
            arr.add(obj);
         }catch(Exception e){
            warnings.add("Failed to export transmutation " + id + ": " + e);
         }
      }
      return arr;
   }
   
   private Map<String, TransmutationRecipe> loadTransmutations(String folder){
      Map<String, TransmutationRecipe> recipes = new HashMap<>();
      Path dir = FabricLoader.getInstance().getConfigDir().resolve("arcananovum").resolve("recipes").resolve(folder);
      if(!Files.isDirectory(dir)) return recipes; // Already reported by loadRecipes
      List<Path> files;
      try(Stream<Path> paths = Files.walk(dir)){
         files = paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".json")).sorted().toList();
      }catch(IOException e){
         warnings.add("Could not read recipe folder " + dir + ": " + e.getMessage());
         return recipes;
      }
      for(Path file : files){
         try{
            JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            if(!json.has("type")) continue;
            TransmutationRecipe recipe = switch(json.get("type").getAsString()){
               case "arcananovum:commutative_transmutation" -> CommutativeTransmutationRecipe.fromJson(json);
               case "arcananovum:infusion_transmutation" -> InfusionTransmutationRecipe.fromJson(json);
               default -> null;
            };
            if(recipe == null) continue;
            if(recipes.putIfAbsent(recipe.getId(), recipe) != null){
               warnings.add("Multiple " + folder + " transmutations with id " + recipe.getId() + ", using the first (" + dir.relativize(file) + " ignored)");
            }
         }catch(Exception e){
            warnings.add("Failed to read transmutation " + file + ": " + e.getMessage());
         }
      }
      return recipes;
   }
   
   private static String transmutationType(TransmutationRecipe recipe){
      return switch(recipe){
         case CommutativeTransmutationRecipe ignored -> "commutative";
         case InfusionTransmutationRecipe ignored -> "infusion";
         case PermutationTransmutationRecipe ignored -> "permutation";
         case AequalisCatalystTransmutationRecipe ignored -> "aequalis_catalyst";
         case AequalisSkillTransmutationRecipe ignored -> "aequalis_skill";
         case AequalisUnattuneTransmutationRecipe ignored -> "aequalis_unattune";
         case TransmogrificationTransmutationRecipe ignored -> "transmogrification";
         default -> throw new IllegalStateException("Unknown transmutation class " + recipe.getClass().getSimpleName());
      };
   }
   
   private JsonObject transmutationBody(TransmutationRecipe recipe){
      String where = "Transmutation " + recipe.getId();
      JsonArray reagents = new JsonArray();
      reagents.add(eitherIngredient(recipe.getReagent1(), recipe.getReagent1Count(), where + " reagent 1"));
      reagents.add(eitherIngredient(recipe.getReagent2(), recipe.getReagent2Count(), where + " reagent 2"));
   
      JsonObject body = new JsonObject();
      body.add("reagents", reagents);
      switch(recipe){
         case CommutativeTransmutationRecipe commutative -> {
            JsonArray pool = new JsonArray();
            Set<Item> seenItems = new HashSet<>();
            for(Either<Item, TagKey<Item>> entry : commutative.getCommunalInputs()){
               if(entry.left().isPresent()){
                  // Block tags are expanded to items when the recipe is built, which can repeat a listed item
                  if(!seenItems.add(entry.left().get())) continue;
                  pool.add(itemRef(entry.left().get(), null, List.of()));
               }else{
                  pool.add(tagRef(entry.right().orElseThrow()));
               }
            }
            body.add("pool", pool);
            body.add("view_item", itemRef(commutative.getViewStack().getItem(), null, List.of()));
         }
         case InfusionTransmutationRecipe infusion -> {
            body.add("input", eitherIngredient(infusion.getInputs(), infusion.getInputCount(), where + " input"));
            body.add("output", stackJson(infusion.getOutput(), infusion.getOutputCount()));
         }
         case PermutationTransmutationRecipe permutation -> {
            // The input check matches Arcana items by id alone, so e.g. any Itineranteur counts
            ItemStack input = permutation.getInput();
            body.add("input", itemIngredient(input.getItem(), input.getCount()));
            body.addProperty("result", sanitizeLine(permutation.getOutputDescription().getString()));
         }
         default -> body.add("slots", specialSlots(recipe));
      }
      return body;
   }
   
   /**
    * Pad descriptions for the hard-coded recipes. The text is worded as the Transmutation Altar recipe GUI
    * shows it; the conditions are what each recipe's canTransmute checks beyond that wording.
    */
   private JsonObject specialSlots(TransmutationRecipe recipe){
      IngredientCondition notMatrix = new IngredientCondition("excluded_item", ArcanaRegistry.arcanaId(ArcanaRegistry.CATALYTIC_MATRIX.getId()).toString(), "Can't be a Catalytic Matrix");
      IngredientCondition aequalisResearched = new IngredientCondition("crafter_researched", ArcanaRegistry.AEQUALIS_SCIENTIA.getId(),
            "The Aequalis Scientia's crafter must be online and have researched it");
      JsonObject slots = new JsonObject();
      switch(recipe){
         case AequalisCatalystTransmutationRecipe ignored -> {
            slots.add("input", slot("An Arcana Item", null,
                  notMatrix,
                  new IngredientCondition("excluded_type", "runic_arrow", "Runic Arrows can't be used")));
            slots.add("focus", slot("1-4 Catalytic Matrices", ArcanaRegistry.CATALYTIC_MATRIX.getItem()));
            slots.add("aequalis", slot("Your Aequalis Scientia", ArcanaRegistry.AEQUALIS_SCIENTIA.getItem(),
                  new IngredientCondition("augment", ArcanaAugments.EQUIVALENT_EXCHANGE.id, "The Aequalis Scientia must have Equivalent Exchange"),
                  aequalisResearched));
         }
         case AequalisSkillTransmutationRecipe ignored -> {
            slots.add("input", slot("An Arcana Item", null, notMatrix));
            slots.add("focus", slot("An Arcana Item", null, notMatrix));
            slots.add("aequalis", slot("Your Aequalis Scientia", ArcanaRegistry.AEQUALIS_SCIENTIA.getItem(), aequalisResearched));
         }
         case AequalisUnattuneTransmutationRecipe ignored -> slots.add("input", slot("An Aequalis Scientia", ArcanaRegistry.AEQUALIS_SCIENTIA.getItem(),
               new IngredientCondition("attuned", true, "Must be attuned"),
               new IngredientCondition("augment", ArcanaAugments.IMPERMANENT_PERMUTATION.id, "The Aequalis Scientia must have Impermanent Permutation")));
         case TransmogrificationTransmutationRecipe ignored -> {
            slots.add("input", slot("An Arcana Item", null,
                  new IngredientCondition("excluded_item", ArcanaRegistry.arcanaId(ArcanaRegistry.TRANSMOGRIFICATION_CATALYST.getId()).toString(), "Can't be a Transmogrification Catalyst")));
            slots.add("focus", slot("A Transmogrification Catalyst", ArcanaRegistry.TRANSMOGRIFICATION_CATALYST.getItem(),
                  new IngredientCondition("skin_matches_input", true, "The selected skin must be for the input item and differ from its current skin, or no skin to remove the input's skin")));
         }
         default -> throw new IllegalStateException("No slot description for " + recipe.getId());
      }
      return slots;
   }

   private JsonObject slot(String text, Item item, IngredientCondition... conditions){
      JsonObject obj = new JsonObject();
      obj.addProperty("text", text);
      if(item != null) obj.add("item", itemRef(item, null, List.of()));
      if(conditions.length > 0) obj.add("conditions", conditionsJson(List.of(conditions)));
      return obj;
   }

   private JsonArray conditionsJson(List<IngredientCondition> conditions){
      JsonArray arr = new JsonArray();
      for(IngredientCondition condition : conditions){
         JsonObject cond = new JsonObject();
         cond.addProperty("type", condition.type());
         cond.add("value", switch(condition.value()){
            case Boolean b -> new JsonPrimitive(b);
            case Number n -> new JsonPrimitive(n);
            default -> new JsonPrimitive(String.valueOf(condition.value()));
         });
         cond.addProperty("text", sanitizeLine(condition.text()));
         arr.add(cond);
      }
      return arr;
   }
   
   // An exact stack, e.g. a transmutation output: named as the game names that stack
   private JsonObject stackJson(ItemStack stack, int count){
      JsonObject ref = itemRef(stack);
      JsonObject obj = new JsonObject();
      obj.add("id", ref.get("id"));
      obj.add("name", ref.get("name"));
      obj.addProperty("count", Math.max(1, count));
      if(ref.has("potion")) obj.add("potion", ref.get("potion"));
      if(ref.has("enchantments")) obj.add("enchantments", ref.get("enchantments"));
      return obj;
   }
   
   // ========== Shared Helpers ==========
   
   private JsonObject itemRef(ItemStack stack){
      Holder<Potion> potion = null;
      PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
      if(contents != null && contents.potion().isPresent()) potion = contents.potion().get();
      List<Pair<ResourceKey<Enchantment>, Integer>> enchants = new ArrayList<>();
      ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
      ItemEnchantments applied = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
      for(ItemEnchantments ench : List.of(stored, applied)){
         ench.entrySet().forEach(entry -> entry.getKey().unwrapKey().ifPresent(key -> enchants.add(Pair.of(key, entry.getIntValue()))));
      }
      return itemRef(stack.getItem(), potion, enchants);
   }
   
   private JsonObject itemRef(Item item, Holder<Potion> potion, List<Pair<ResourceKey<Enchantment>, Integer>> enchantments){
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      JsonObject obj = new JsonObject();
      obj.addProperty("id", id.toString());
   
      ArcanaItem arcanaItem = id.getNamespace().equals(MOD_ID) ? ArcanaRegistry.getArcanaItem(id.getPath()) : null;
      String name;
      if(arcanaItem != null){
         name = arcanaDisplayName(arcanaItem);
      }else if(id.getNamespace().equals(MOD_ID)){
         name = modItemName(item);
      }else if(potion != null){
         ItemStack stack = new ItemStack(item);
         stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
         name = stack.getHoverName().getString();
      }else{
         // Not getItemName(): potions derive it from their contents, so an empty one is "Uncraftable Potion"
         name = Component.translatable(item.getDescriptionId()).getString();
      }
      obj.addProperty("name", name);
   
      if(potion != null){
         potion.unwrapKey().ifPresent(key -> obj.addProperty("potion", key.identifier().toString()));
      }
      if(enchantments != null && !enchantments.isEmpty()){
         obj.add("enchantments", enchantmentList(enchantments, false));
      }
      return obj;
   }
   
   private JsonArray enchantmentList(List<Pair<ResourceKey<Enchantment>, Integer>> enchantments, boolean sort){
      List<Pair<ResourceKey<Enchantment>, Integer>> list = new ArrayList<>(enchantments);
      if(sort) list.sort(Comparator.comparing(pair -> pair.getFirst().identifier().toString()));
      JsonArray arr = new JsonArray();
      for(Pair<ResourceKey<Enchantment>, Integer> pair : list){
         JsonObject obj = new JsonObject();
         obj.addProperty("id", pair.getFirst().identifier().toString());
         obj.addProperty("name", registryAccess.lookupOrThrow(Registries.ENCHANTMENT).get(pair.getFirst())
               .map(holder -> holder.value().description().getString())
               .orElse(pair.getFirst().identifier().getPath()));
         obj.addProperty("level", Math.max(1, pair.getSecond()));
         arr.add(obj);
      }
      return arr;
   }
   
   private JsonObject tagRef(TagKey<Item> tag){
      Identifier id = tag.location();
      JsonObject obj = new JsonObject();
      obj.addProperty("tag", id.toString());
      String nameKey = "tag.item." + id.getNamespace() + "." + id.getPath().replace('/', '.');
      String name = Component.translatable(nameKey).getString();
      if(!name.equals(nameKey)) obj.addProperty("name", name);
      TreeMap<String, Item> members = new TreeMap<>();
      for(Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)){
         members.put(BuiltInRegistries.ITEM.getKey(holder.value()).toString(), holder.value());
      }
      if(members.isEmpty()) warnings.add("Tag #" + id + " has no members");
      JsonArray items = new JsonArray();
      members.values().forEach(item -> items.add(itemRef(item, null, List.of())));
      obj.add("items", items);
      return obj;
   }
   
   private static String modItemName(Item item){
      return item.getDefaultInstance().getHoverName().getString();
   }
   
   private static String arcanaDisplayName(ArcanaItem arcanaItem){
      Component displayName = arcanaItem.getDisplayName();
      return displayName != null ? displayName.getString() : arcanaItem.getTranslatedName().getString();
   }
   
   /**
    * Converts components to plain lines. Embedded line breaks become separate lines, so no string
    * in the export contains a newline. A text block must have at least one line.
    */
   private JsonArray lines(List<? extends Component> components, boolean textBlock){
      JsonArray arr = new JsonArray();
      for(Component component : components){
         splitLines(component.getString()).forEach(arr::add);
      }
      if(textBlock && arr.isEmpty()) arr.add("");
      return arr;
   }
   
   private List<String> splitLines(String text){
      return Arrays.stream(text.split("\\r\\n|\\r|\\n", -1)).map(this::sanitizeLine).toList();
   }
   
   private String sanitizeLine(String line){
      if(line.indexOf('\u00A7') < 0) return line;
      warnings.add("Stripped legacy formatting codes from: " + line);
      return line.replaceAll("\u00A7.?", "");
   }
   
   private static JsonPrimitive number(double value){
      if(value == Math.rint(value) && Math.abs(value) < 1e15) return new JsonPrimitive((long) value);
      return new JsonPrimitive(value);
   }
   
   private static String hexColor(int rgb){
      return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
   }
}
