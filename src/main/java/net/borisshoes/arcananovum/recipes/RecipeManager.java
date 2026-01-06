package net.borisshoes.arcananovum.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Lifecycle;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datagen.DefaultRecipeGenerator;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.*;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class RecipeManager {
   
   public static final ArrayList<ArcanaRecipe> ARCANA_RECIPES = new ArrayList<>();
   public static final ArrayList<TransmutationRecipe> TRANSMUTATION_RECIPES = new ArrayList<>();
   
   public static ArcanaRecipe getRecipeFor(ArcanaItem arcanaItem){ // TODO: Support multiple recipes
      for(ArcanaRecipe arcanaRecipe : ARCANA_RECIPES){
         Optional<Holder.Reference<ArcanaItem>> opt = ArcanaRegistry.ARCANA_ITEMS.get(arcanaRecipe.getOutputId());
         if(opt.isEmpty()) continue;
         ArcanaItem item = opt.get().value();
         if(item.getId().equals(arcanaItem.getId())) return arcanaRecipe;
      }
      return null;
   }
   
   public static ArcanaRecipe getRecipeFor(Item item){ // TODO: Support multiple recipes
      for(ArcanaRecipe arcanaRecipe : ARCANA_RECIPES){
         Holder<Item> holder = BuiltInRegistries.ITEM.get(arcanaRecipe.getOutputId()).orElse(null);
         if(holder == null) return null;
         if(item == holder.value()) return arcanaRecipe;
      }
      return null;
   }
   
   public static void refreshRecipes(MinecraftServer server){
      ArcanaNovum.log(0, "Initializing Arcana Recipes...");
      ARCANA_RECIPES.clear();
      TRANSMUTATION_RECIPES.clear();
      String activeRecipePath = ArcanaNovum.CONFIG.getValue(ArcanaRegistry.RECIPE_FOLDER).toString();
      Path dirPath = FabricLoader.getInstance().getConfigDir().resolve("arcananovum").resolve("recipes").resolve(activeRecipePath);
      
      // Check if the directory exists
      if(!Files.exists(dirPath) || !Files.isDirectory(dirPath)){
         ArcanaNovum.log(2, "Recipe directory does not exist: " + dirPath);
         return;
      }
      
      // Find all .json files in the directory and subdirectories
      try(Stream<Path> paths = Files.walk(dirPath)){
         paths.filter(Files::isRegularFile)
               .filter(path -> path.toString().endsWith(".json"))
               .forEach(RecipeManager::processRecipeFile);
      }catch(IOException e){
         ArcanaNovum.log(3, "Error reading recipe directory: " + e.getMessage());
         throw new RuntimeException("Error reading recipe directory", e);
      }
      
      addNonSerializedRecipes();
      ArcanaNovum.log(0, "Loaded "+ARCANA_RECIPES.size()+" Arcana Recipes and "+TRANSMUTATION_RECIPES.size()+" Transmutation Recipes");
   }
   
   private static void addNonSerializedRecipes(){
      ExplainIngredient a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z;
      ExplainIngredient[][] ingredients;
      
      // ===================================
      //          STARLIGHT FORGE
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Component.literal("In World Recipe").withStyle(ChatFormatting.BLUE))
            .withLore(List.of(Component.literal("Do this in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      s = new ExplainIngredient(Items.SMITHING_TABLE,1,"Smithing Table")
            .withName(Component.literal("Smithing Table").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY))
            .withLore(List.of(Component.literal("Place a Smithing Table in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      m = new ExplainIngredient(Items.SEA_LANTERN,1,"",false)
            .withName(Component.literal("Night of a New Moon").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY))
            .withLore(List.of(Component.literal("Follow this Recipe under the darkness of a New Moon").withStyle(ChatFormatting.DARK_PURPLE)));
      g = new ExplainIngredient(Items.ENCHANTED_GOLDEN_APPLE,1,"Enchanted Golden Apple")
            .withName(Component.literal("Enchanted Golden Apple").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD))
            .withLore(List.of(Component.literal("Place the apple upon the Smithing Table.").withStyle(ChatFormatting.DARK_PURPLE)));
      t = new ExplainIngredient(ArcanaRegistry.ARCANE_TOME.getItem(),1,"Tome of Arcana Novum")
            .withName(Component.literal("Tome of Arcana Novum").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA))
            .withLore(List.of(Component.literal("Place the Tome upon the Smithing Table.").withStyle(ChatFormatting.DARK_PURPLE)));
      ingredients = new ExplainIngredient[][]{
            {m,a,a,a,a},
            {a,a,t,a,a},
            {a,a,g,a,a},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.STARLIGHT_FORGE, ingredients));
      
      // ===================================
      //          DIVINE CATALYST
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Component.literal("In World Recipe").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Build this in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      s = new ExplainIngredient(Items.SOUL_SAND,1,"Soul Sand or Soil")
            .withName(Component.literal("Soul Sand or Soil").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      k = new ExplainIngredient(Items.WITHER_SKELETON_SKULL,1,"Wither Skeleton Skull")
            .withName(Component.literal("Wither Skeleton Skull").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      n = new ExplainIngredient(Items.NETHERITE_BLOCK,1,"Netherite Block")
            .withName(Component.literal("Block of Netherite").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      c = new ExplainIngredient(ArcanaRegistry.SOVEREIGN_CATALYST.getItem(),1,"Sovereign Augment Catalyst")
            .withName(Component.literal("Sovereign Augmentation Catalyst").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .withLore(List.of(
                  Component.literal("")
                        .append(Component.literal("Right Click").withStyle(ChatFormatting.BLUE))
                        .append(Component.literal(" the ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("Catalyst").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" on the ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("Netherite Heart").withStyle(ChatFormatting.DARK_RED)),
                  Component.literal("")
                        .append(Component.literal("Divine Energy").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(" will flow into the ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("Nul Construct").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(" empowering it").withStyle(ChatFormatting.DARK_PURPLE)),
                  Component.literal("")
                        .append(Component.literal("Defeat the ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("Nul Construct").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(" without dying to receive a ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("Divine Catalyst").withStyle(ChatFormatting.LIGHT_PURPLE)),
                  Component.literal(""),
                  Component.literal("Warning! This fight is difficult, preparation is necessary.").withStyle(ChatFormatting.RED)
            ));
      ingredients = new ExplainIngredient[][]{
            {a,a,a,a,a},
            {a,k,k,k,a},
            {a,s,n,s,c},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.DIVINE_CATALYST, ingredients));
      
      // ===================================
      //          AEQUALIS SCIENTIA
      // ===================================
      b = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      w = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LIGHT_COLOR),1,"",false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      a = new ExplainIngredient(Items.AMETHYST_BLOCK,64,"Amethyst Blocks")
            .withName(Component.literal("Amethyst Blocks").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.LIGHT_PURPLE)));
      t = new ExplainIngredient(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem(),1,"",false)
            .withName(Component.literal("Transmutation Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      d = new ExplainIngredient(Items.DIAMOND_BLOCK,1,"Diamond Block")
            .withName(Component.literal("Diamond Block").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.LIGHT_PURPLE)));
      c = new ExplainIngredient(ArcanaRegistry.DIVINE_CATALYST.getItem(),1,"Divine Augment Catalyst")
            .withName(Component.literal("Divine Augmentation Catalyst").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Infusion Input").withStyle(ChatFormatting.WHITE)));
      ingredients = new ExplainIngredient[][]{
            {b,b,c,b,b},
            {b,b,b,b,w},
            {a,b,t,w,d},
            {b,w,w,w,w},
            {w,w,w,w,w}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.AEQUALIS_SCIENTIA, ingredients));
      
      // ===================================
      //            ARCANE TOME
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Component.literal("In World Recipe").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Do this in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      t = new ExplainIngredient(Items.ENCHANTING_TABLE,1,"Enchanting Table")
            .withName(Component.literal("Enchanting Table").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Place an Enchanting Table in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      p = new ExplainIngredient(ArcanaRegistry.MUNDANE_ARCANE_PAPER,4,"Mundane Arcane Paper")
            .withName(Component.literal("Mundane Arcane Paper").withStyle(ChatFormatting.AQUA))
            .withLore(List.of(Component.literal("Place the Paper onto the Enchanting Table").withStyle(ChatFormatting.DARK_PURPLE)));
      e = new ExplainIngredient(Items.ENDER_EYE,1,"Eye of Ender")
            .withName(Component.literal("Eye of Ender").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Place an Eye of Ender onto the Enchanting Table").withStyle(ChatFormatting.DARK_PURPLE)));
      ingredients = new ExplainIngredient[][]{
            {a,a,a,a,a},
            {a,a,e,a,a},
            {a,a,p,a,a},
            {a,a,t,a,a},
            {a,a,a,a,a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.ARCANE_TOME, ingredients));
      
      // ===================================
      //            NUL MEMENTO
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Component.literal("In World Recipe").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Build this in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      s = new ExplainIngredient(Items.SOUL_SAND,1,"Soul Sand or Soil")
            .withName(Component.literal("Soul Sand or Soil").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      k = new ExplainIngredient(Items.WITHER_SKELETON_SKULL,1,"Wither Skeleton Skull")
            .withName(Component.literal("Wither Skeleton Skull").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      n = new ExplainIngredient(Items.NETHERITE_BLOCK,1,"Netherite Block")
            .withName(Component.literal("Block of Netherite").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      c = new ExplainIngredient(ArcanaRegistry.DIVINE_CATALYST.getItem(),1,"Divine Augment Catalyst")
            .withName(Component.literal("Divine Augmentation Catalyst").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
            .withLore(List.of(
                  Component.literal("")
                        .append(Component.literal("Right Click").withStyle(ChatFormatting.BLUE))
                        .append(Component.literal(" the ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("Catalyst").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(" on the ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("Netherite Heart").withStyle(ChatFormatting.DARK_RED)),
                  Component.literal("")
                        .append(Component.literal("Divine Energy").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(" will flow into the ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("Exalted Construct").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(" empowering it").withStyle(ChatFormatting.DARK_PURPLE)),
                  Component.literal("")
                        .append(Component.literal("Defeat the ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("Exalted Construct").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(" without dying to receive a ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("Nul Memento").withStyle(ChatFormatting.BLACK)),
                  Component.literal(""),
                  Component.literal("WARNING!!! This fight is considerably harder than a Nul Construct. Attempt at your own peril.").withStyle(ChatFormatting.RED)
            ));
      ingredients = new ExplainIngredient[][]{
            {a,a,a,a,a},
            {a,k,k,k,a},
            {a,s,n,s,c},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.NUL_MEMENTO, ingredients));
      
      // ===================================
      //             WAYSTONE
      // ===================================
      b = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      w = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LIGHT_COLOR),1,"",false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      c = new ExplainIngredient(Items.REDSTONE,42,"Redstone Dust")
            .withName(Component.literal("Redstone Dust").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.GOLD)));
      t = new ExplainIngredient(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem(),1,"",false)
            .withName(Component.literal("Transmutation Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      d = new ExplainIngredient(Items.AMETHYST_SHARD,16,"Amethyst Shard")
            .withName(Component.literal("Amethyst Shards").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.DARK_PURPLE)));
      p = new ExplainIngredient(Items.LODESTONE,1,"Lodestone")
            .withName(Component.literal("Lodestone").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Infusion Input").withStyle(ChatFormatting.WHITE)));
      ingredients = new ExplainIngredient[][]{
            {b,b,p,b,b},
            {b,b,b,b,w},
            {c,b,t,w,d},
            {b,w,w,w,w},
            {w,w,w,w,w}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.WAYSTONE, ingredients));
      
      // Permutation Recipes
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("faeries_stew", new ItemStack(Items.MUSHROOM_STEW,1), MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 1)), new ItemStack(Items.NETHER_WART),  (stack, minecraftServer) -> {
         ItemStack stewStack = new ItemStack(Items.SUSPICIOUS_STEW);
         stewStack.set(DataComponents.RARITY, Rarity.RARE);
         stewStack.set(DataComponents.ITEM_NAME, Component.translatable("item."+MOD_ID+".faeries_stew"));
         List<SuspiciousStewEffects.Entry> effects = new ArrayList<>();
         Registry<MobEffect> effectRegistry = minecraftServer.registryAccess().lookupOrThrow(Registries.MOB_EFFECT);
         List<Holder.Reference<MobEffect>> effectEntries = effectRegistry.listElements().toList();
         int count = 0;
         while(count < 10 && (Math.random() < 0.35 || count == 0)){
            effects.add(new SuspiciousStewEffects.Entry(effectEntries.get((int)(Math.random()*effectEntries.size())),(int)(Math.random()*580 + 20)));
            count++;
         }
         SuspiciousStewEffects comp = new SuspiciousStewEffects(effects);
         stewStack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS,comp);
         return stewStack;
      }, Component.literal("A 'Delicious' Stew")));
      
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("book_exchange", new ItemStack(Items.ENCHANTED_BOOK,1), MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 15)), new ItemStack(Items.LAPIS_LAZULI,5),  (stack, minecraftServer) -> {
         ItemStack newBook = new ItemStack(Items.BOOK);
         ArrayList<Holder<Enchantment>> enchants = new ArrayList<>();
         minecraftServer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap().forEach(enchants::add);
         return EnchantmentHelper.enchantItem(RandomSource.create(), newBook, (int)(Math.random()*30+15),enchants.stream());
      }, Component.literal("A Random ").append(Component.translatable(Items.ENCHANTED_BOOK.getDescriptionId()))));
      
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("goat_horns", new ItemStack(Items.GOAT_HORN,1), MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 3)), new ItemStack(Items.AMETHYST_SHARD,7),  (stack, minecraftServer) -> {
         InstrumentComponent curInstrument = stack.get(DataComponents.INSTRUMENT);
         Registry<Instrument> registry = minecraftServer.registryAccess().lookupOrThrow(Registries.INSTRUMENT);
         Identifier curId = Identifier.fromNamespaceAndPath("empty", "empty");
         if(curInstrument != null){
            Instrument inst = curInstrument.instrument().unwrap(registry).orElse(null);
            if(inst != null && registry.getKey(inst) != null){
               curId = registry.getKey(inst);
            }
         }
         ArrayList<Holder<Instrument>> options = new ArrayList<>();
         for(Holder<Instrument> entry : registry.getTagOrEmpty(InstrumentTags.GOAT_HORNS)){
            if(entry.unwrapKey().get().identifier().equals(curId)) continue;
            options.add(entry);
         }
         if(options.isEmpty()) return stack;
         Holder<Instrument> newInst = options.get(minecraftServer.overworld().random.nextInt(options.size()));
         return InstrumentItem.create(Items.GOAT_HORN, newInst);
      }, Component.literal("A Random ").append(Component.translatable(Items.GOAT_HORN.getDescriptionId()))));
      
      // Aequalis Scientia Recipes
      TRANSMUTATION_RECIPES.add(new AequalisUnattuneTransmutationRecipe("aequalis_reconfiguration"));
      
      TRANSMUTATION_RECIPES.add(new AequalisSkillTransmutationRecipe("transfer_skill_points"));
      
      TRANSMUTATION_RECIPES.add(new AequalisCatalystTransmutationRecipe("reclaim_catalysts"));
   }
   
   private static void processRecipeFile(Path filePath){
      try{
         String content = Files.readString(filePath);
         JsonObject json = JsonParser.parseString(content).getAsJsonObject();
         
         if(!json.has("type")){
            ArcanaNovum.log(2, "Recipe file missing 'type' key: " + filePath);
            return;
         }
         
         String type = json.get("type").getAsString();
         
         switch(type){
            case "arcananovum:forging_recipe" -> {
               ArcanaRecipe recipe = ArcanaRecipe.fromJson(json);
               if(recipe != null){
                  ARCANA_RECIPES.add(recipe);
               }else{
                  ArcanaNovum.log(2, "Failed to parse forging recipe: " + filePath);
               }
            }
            case "arcananovum:commutative_transmutation" -> {
               CommutativeTransmutationRecipe recipe = CommutativeTransmutationRecipe.fromJson(json);
               if(recipe != null){
                  TRANSMUTATION_RECIPES.add(recipe);
               }else{
                  ArcanaNovum.log(2, "Failed to parse commutative transmutation recipe: " + filePath);
               }
            }
            case "arcananovum:infusion_transmutation" -> {
               InfusionTransmutationRecipe recipe = InfusionTransmutationRecipe.fromJson(json);
               if(recipe != null){
                  TRANSMUTATION_RECIPES.add(recipe);
               }else{
                  ArcanaNovum.log(2, "Failed to parse infusion transmutation recipe: " + filePath);
               }
            }
            default -> ArcanaNovum.log(2, "Unknown recipe type '" + type + "' in file: " + filePath);
         }
      }catch(IOException e){
         ArcanaNovum.log(2, "Error reading recipe file " + filePath + ": " + e.getMessage());
      }catch(Exception e){
         ArcanaNovum.log(2, "Error parsing recipe file " + filePath + ": " + e.getMessage());
      }
   }
   
   public static TransmutationRecipe findMatchingRecipe(ItemStack positive, ItemStack negative, ItemStack re1, ItemStack re2, ItemStack aequalis, TransmutationAltarBlockEntity altar){
      TransmutationRecipe matching = null;
      for(TransmutationRecipe recipe : TRANSMUTATION_RECIPES){
         if(recipe.canTransmute(positive,negative,re1,re2,aequalis,altar)){
            matching = recipe;
         }
      }
      return matching;
   }
   
   public static TransmutationRecipe findMatchingTransmutationRecipe(String id){
      return TRANSMUTATION_RECIPES.stream().filter(r -> r.getId().equals(id)).findAny().orElse(null);
   }
}
