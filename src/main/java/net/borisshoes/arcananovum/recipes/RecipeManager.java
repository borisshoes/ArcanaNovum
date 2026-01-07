package net.borisshoes.arcananovum.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Lifecycle;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datagen.DefaultRecipeGenerator;
import net.borisshoes.arcananovum.gui.arcanetome.ArcanaItemCompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.CompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
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
import net.minecraft.world.Container;
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
import static net.borisshoes.arcananovum.ArcanaRegistry.RECOMMENDED_LIST;

public class RecipeManager {
   
   public static final ArrayList<ArcanaRecipe> ARCANA_RECIPES = new ArrayList<>();
   public static final ArrayList<TransmutationRecipe> TRANSMUTATION_RECIPES = new ArrayList<>();
   
   public static List<ArcanaRecipe> getRecipesFor(Item item){
      List<ArcanaRecipe> recipes = new ArrayList<>();
      for(ArcanaRecipe arcanaRecipe : ARCANA_RECIPES){
         Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(arcanaRecipe.getOutputId());
         if(opt.isEmpty()) continue;
         Item checkedItem = opt.get().value();
         if(checkedItem == item) recipes.add(arcanaRecipe);
      }
      return recipes;
   }
   
   public static List<ArcanaRecipe> getSimilarRecipes(ArcanaRecipe recipe){
      List<ArcanaRecipe> recipes = new ArrayList<>();
      for(ArcanaRecipe arcanaRecipe : ARCANA_RECIPES){
         if(arcanaRecipe.getOutputId().equals(recipe.getOutputId())) recipes.add(arcanaRecipe);
      }
      return recipes;
   }
   
   public static ArcanaRecipe getMatchingRecipe(Container inv, StarlightForgeBlockEntity blockEntity){
      ItemStack[][] curItems = new ItemStack[5][5];
      for(int i = 0; i < 5; i++){
         for(int j = 0; j < 5; j++){
            curItems[i][j] = inv.getItem(i * 5 + j);
         }
      }
      
      for(ArcanaRecipe arcanaRecipe : RecipeManager.ARCANA_RECIPES){
         if(arcanaRecipe.satisfiesRecipe(curItems, blockEntity)){
            if(!BuiltInRegistries.ITEM.containsKey(arcanaRecipe.getOutputId())) continue;
            return arcanaRecipe;
         }
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
      addTransmutationExplainRecipes();
      ArcanaNovum.log(0, "Loaded " + ARCANA_RECIPES.size() + " Arcana Recipes and " + TRANSMUTATION_RECIPES.size() + " Transmutation Recipes");
   }
   
   private static void addNonSerializedRecipes(){
      ExplainIngredient a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;
      ExplainIngredient[][] ingredients;
      
      // ===================================
      //          STARLIGHT FORGE
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR), 1, "", false)
            .withName(Component.literal("In World Recipe").withStyle(ChatFormatting.BLUE))
            .withLore(List.of(Component.literal("Do this in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      s = new ExplainIngredient(Items.SMITHING_TABLE, 1, "Smithing Table")
            .withName(Component.literal("Smithing Table").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY))
            .withLore(List.of(Component.literal("Place a Smithing Table in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      m = new ExplainIngredient(Items.SEA_LANTERN, 1, "", false)
            .withName(Component.literal("Night of a New Moon").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY))
            .withLore(List.of(Component.literal("Follow this Recipe under the darkness of a New Moon").withStyle(ChatFormatting.DARK_PURPLE)));
      g = new ExplainIngredient(Items.ENCHANTED_GOLDEN_APPLE, 1, "Enchanted Golden Apple")
            .withName(Component.literal("Enchanted Golden Apple").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD))
            .withLore(List.of(Component.literal("Place the apple upon the Smithing Table.").withStyle(ChatFormatting.DARK_PURPLE)));
      t = new ExplainIngredient(ArcanaRegistry.ARCANE_TOME.getItem(), 1, "Tome of Arcana Novum")
            .withName(Component.literal("Tome of Arcana Novum").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA))
            .withLore(List.of(Component.literal("Place the Tome upon the Smithing Table.").withStyle(ChatFormatting.DARK_PURPLE)));
      ingredients = new ExplainIngredient[][]{
            {m, a, a, a, a},
            {a, a, t, a, a},
            {a, a, g, a, a},
            {a, a, s, a, a},
            {a, a, a, a, a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.STARLIGHT_FORGE, ingredients));
      
      // ===================================
      //          DIVINE CATALYST
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR), 1, "", false)
            .withName(Component.literal("In World Recipe").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Build this in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      s = new ExplainIngredient(Items.SOUL_SAND, 1, "Soul Sand or Soil")
            .withName(Component.literal("Soul Sand or Soil").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      k = new ExplainIngredient(Items.WITHER_SKELETON_SKULL, 1, "Wither Skeleton Skull")
            .withName(Component.literal("Wither Skeleton Skull").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      n = new ExplainIngredient(Items.ANCIENT_DEBRIS, 1, "Ancient Debris")
            .withName(Component.literal("Ancient Debris").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      c = new ExplainIngredient(ArcanaRegistry.SOVEREIGN_CATALYST.getItem(), 1, "Sovereign Augment Catalyst")
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
            {a, a, a, a, a},
            {a, k, k, k, a},
            {a, s, n, s, c},
            {a, a, s, a, a},
            {a, a, a, a, a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.DIVINE_CATALYST, ingredients));
      
      // ===================================
      //          AEQUALIS SCIENTIA
      // ===================================
      b = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR), 1, "", false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      w = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LIGHT_COLOR), 1, "", false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      a = new ExplainIngredient(Items.AMETHYST_BLOCK, 64, "Amethyst Blocks")
            .withName(Component.literal("Amethyst Blocks").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.LIGHT_PURPLE)));
      t = new ExplainIngredient(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem(), 1, "", false)
            .withName(Component.literal("Transmutation Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      d = new ExplainIngredient(Items.DIAMOND_BLOCK, 1, "Diamond Block")
            .withName(Component.literal("Diamond Block").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.LIGHT_PURPLE)));
      c = new ExplainIngredient(ArcanaRegistry.DIVINE_CATALYST.getItem(), 1, "Divine Augment Catalyst")
            .withName(Component.literal("Divine Augmentation Catalyst").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Infusion Input").withStyle(ChatFormatting.WHITE)));
      ingredients = new ExplainIngredient[][]{
            {b, b, c, b, b},
            {b, b, b, b, w},
            {a, b, t, w, d},
            {b, w, w, w, w},
            {w, w, w, w, w}};
      //ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.AEQUALIS_SCIENTIA, ingredients));
      
      // ===================================
      //            ARCANE TOME
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR), 1, "", false)
            .withName(Component.literal("In World Recipe").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Do this in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      t = new ExplainIngredient(Items.ENCHANTING_TABLE, 1, "Enchanting Table")
            .withName(Component.literal("Enchanting Table").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Place an Enchanting Table in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      p = new ExplainIngredient(ArcanaRegistry.MUNDANE_ARCANE_PAPER, 4, "Mundane Arcane Paper")
            .withName(Component.literal("Mundane Arcane Paper").withStyle(ChatFormatting.AQUA))
            .withLore(List.of(Component.literal("Place the Paper onto the Enchanting Table").withStyle(ChatFormatting.DARK_PURPLE)));
      e = new ExplainIngredient(Items.ENDER_EYE, 1, "Eye of Ender")
            .withName(Component.literal("Eye of Ender").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Place an Eye of Ender onto the Enchanting Table").withStyle(ChatFormatting.DARK_PURPLE)));
      ingredients = new ExplainIngredient[][]{
            {a, a, a, a, a},
            {a, a, e, a, a},
            {a, a, p, a, a},
            {a, a, t, a, a},
            {a, a, a, a, a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.ARCANE_TOME, ingredients));
      
      // ===================================
      //            NUL MEMENTO
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR), 1, "", false)
            .withName(Component.literal("In World Recipe").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Build this in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      s = new ExplainIngredient(Items.SOUL_SAND, 1, "Soul Sand or Soil")
            .withName(Component.literal("Soul Sand or Soil").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      k = new ExplainIngredient(Items.WITHER_SKELETON_SKULL, 1, "Wither Skeleton Skull")
            .withName(Component.literal("Wither Skeleton Skull").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      n = new ExplainIngredient(Items.NETHERITE_BLOCK, 1, "Netherite Block")
            .withName(Component.literal("Block of Netherite").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Construct a Wither Base with a heart of Netherite").withStyle(ChatFormatting.DARK_PURPLE)));
      c = new ExplainIngredient(ArcanaRegistry.DIVINE_CATALYST.getItem(), 1, "Divine Augment Catalyst")
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
                        .append(Component.literal("Nul Memento").withColor(ArcanaColors.NUL_COLOR)),
                  Component.literal(""),
                  Component.literal("WARNING!!! This fight is considerably harder than a Nul Construct. Attempt at your own peril.").withStyle(ChatFormatting.RED)
            ));
      ingredients = new ExplainIngredient[][]{
            {a, a, a, a, a},
            {a, k, k, k, a},
            {a, s, n, s, c},
            {a, a, s, a, a},
            {a, a, a, a, a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.NUL_MEMENTO, ingredients));
      
      // ===================================
      //             WAYSTONE
      // ===================================
      b = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR), 1, "", false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      w = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LIGHT_COLOR), 1, "", false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      c = new ExplainIngredient(Items.REDSTONE, 42, "Redstone Dust")
            .withName(Component.literal("Redstone Dust").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.GOLD)));
      t = new ExplainIngredient(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem(), 1, "", false)
            .withName(Component.literal("Transmutation Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      d = new ExplainIngredient(Items.AMETHYST_SHARD, 16, "Amethyst Shard")
            .withName(Component.literal("Amethyst Shards").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.DARK_PURPLE)));
      p = new ExplainIngredient(Items.LODESTONE, 1, "Lodestone")
            .withName(Component.literal("Lodestone").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Infusion Input").withStyle(ChatFormatting.WHITE)));
      ingredients = new ExplainIngredient[][]{
            {b, b, p, b, b},
            {b, b, b, b, w},
            {c, b, t, w, d},
            {b, w, w, w, w},
            {w, w, w, w, w}};
      //ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.WAYSTONE, ingredients));
      
      
      // ===================================
      //       MUNDANE ARCANE PAPER
      // ===================================
      c = new ExplainIngredient(Items.CRAFTING_TABLE, 1, "", false)
            .withName(Component.literal("Crafting Recipe").withStyle(ChatFormatting.YELLOW))
            .withLore(List.of(Component.literal("Use a normal crafting table").withStyle(ChatFormatting.GRAY)));
      p = new ExplainIngredient(Items.PAPER, 1, "Paper")
            .withName(Component.literal("Paper").withStyle(ChatFormatting.AQUA));
      a = new ExplainIngredient(Items.AIR, 1, "", false);
      b = new ExplainIngredient(Items.ENCHANTED_BOOK, 1, "Enchanted Book")
            .withName(Component.literal("Enchanted Book").withStyle(ChatFormatting.LIGHT_PURPLE))
            .withLore(List.of(Component.literal("Any enchantments work").withStyle(ChatFormatting.DARK_PURPLE)));
      
      ingredients = new ExplainIngredient[][]{
            {c, c, c, c, c},
            {c, a, p, a, c},
            {c, p, b, p, c},
            {c, a, p, a, c},
            {c, c, c, c, c}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.MUNDANE_ARCANE_PAPER, ingredients));
      
      
      // ===================================
      //      EMPOWERED ARCANE PAPER
      // ===================================
      c = new ExplainIngredient(Items.CRAFTING_TABLE, 1, "", false)
            .withName(Component.literal("Crafting Recipe").withStyle(ChatFormatting.YELLOW))
            .withLore(List.of(Component.literal("Use a normal crafting table").withStyle(ChatFormatting.GRAY)));
      p = new ExplainIngredient(ArcanaRegistry.MUNDANE_ARCANE_PAPER, 1, "Mundane Arcane Paper")
            .withName(Component.literal("Mundane Arcane Paper").withStyle(ChatFormatting.AQUA));
      a = new ExplainIngredient(Items.AIR, 1, "", false);
      b = new ExplainIngredient(Items.ENDER_EYE, 1, "Eye of Ender")
            .withName(Component.literal("Eye of Ender").withStyle(ChatFormatting.DARK_AQUA));
      
      ingredients = new ExplainIngredient[][]{
            {c, c, c, c, c},
            {c, a, p, a, c},
            {c, p, b, p, c},
            {c, a, p, a, c},
            {c, c, c, c, c}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.EMPOWERED_ARCANE_PAPER, ingredients));
      
      
      // ===================================
      //        EXOTIC ARCANE PAPER
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LAPIS_COLOR), 1, "", false)
            .withName(Component.literal("In Midnight Enchanter").withStyle(ChatFormatting.DARK_AQUA));
      c = new ExplainIngredient(ArcanaRegistry.NEBULOUS_ESSENCE, 1, "Nebulous Essence", true)
            .withName(ArcanaRegistry.NEBULOUS_ESSENCE.getName().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("1 Nebulous Essence per Paper").withStyle(ChatFormatting.BLUE)));
      d = new ExplainIngredient(Items.EXPERIENCE_BOTTLE, 10, "Levels", true)
            .withName(Component.literal("Experience").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("10 Levels").withStyle(ChatFormatting.YELLOW)));
      m = new ExplainIngredient(ArcanaRegistry.MIDNIGHT_ENCHANTER.getItem(), 1, "", false)
            .withName(Component.literal("Midnight Enchanter").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Enchant Empowered Arcane Paper in the Midnight Enchanter").withStyle(ChatFormatting.BLUE)));
      b = new ExplainIngredient(ArcanaRegistry.EMPOWERED_ARCANE_PAPER, 1, "Empowered Arcane Paper")
            .withName(Component.literal("Empowered Arcane Paper").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN))
            .withLore(List.of(Component.literal("Enchant Empowered Arcane Paper in the Midnight Enchanter").withStyle(ChatFormatting.LIGHT_PURPLE)));
      
      ingredients = new ExplainIngredient[][]{
            {a, a, a, a, a},
            {a, c, a, d, a},
            {a, a, b, a, a},
            {a, a, m, a, a},
            {a, a, a, a, a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.EXOTIC_ARCANE_PAPER, ingredients));
      
      
      // ===================================
      //         NEBULOUS ESSENCE
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LAPIS_COLOR), 1, "", false)
            .withName(Component.literal("In Midnight Enchanter").withStyle(ChatFormatting.DARK_AQUA));
      m = new ExplainIngredient(ArcanaRegistry.MIDNIGHT_ENCHANTER.getItem(), 1, "", false)
            .withName(Component.literal("Midnight Enchanter").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Disenchant an item using the Midnight Enchanter").withStyle(ChatFormatting.BLUE)));
      ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
      chestplate.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
      b = new ExplainIngredient(chestplate, 1, "Enchanted Item", true)
            .withName(Component.literal("Enchanted Item").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE))
            .withLore(List.of(Component.literal("Better enchantments yield more Essence").withStyle(ChatFormatting.DARK_PURPLE)));
      
      ingredients = new ExplainIngredient[][]{
            {a, a, a, a, a},
            {a, a, a, a, a},
            {a, a, b, a, a},
            {a, a, m, a, a},
            {a, a, a, a, a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.NEBULOUS_ESSENCE, ingredients));
      
      
      // ===================================
      //      SOVEREIGN ARCANE PAPER
      // ===================================
      c = new ExplainIngredient(ArcanaRegistry.STARLIGHT_FORGE.getItem(), 1, "", false)
            .withName(Component.literal("Starlight Forge").withStyle(ChatFormatting.LIGHT_PURPLE))
            .withLore(List.of(
                  Component.literal("Stardust Infusion Recipe").withStyle(ChatFormatting.YELLOW),
                  Component.literal(""),
                  Component.literal("A better infusion result yields more paper").withStyle(ChatFormatting.GOLD)));
      p = new ExplainIngredient(ArcanaRegistry.EXOTIC_ARCANE_PAPER, 1, "Exotic Arcane Paper")
            .withName(Component.literal("Exotic Arcane Paper").withStyle(ChatFormatting.AQUA));
      b = new ExplainIngredient(Items.GOLD_INGOT, 1, "Gold Ingot")
            .withName(Component.literal("Gold Ingot").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
      
      ingredients = new ExplainIngredient[][]{
            {c, c, c, c, c},
            {c, p, p, p, c},
            {c, p, b, p, c},
            {c, p, p, p, c},
            {c, c, c, c, c}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER, ingredients));
      
      
      // ===================================
      //             STARDUST
      // ===================================
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, 0xff8800), 1, "", false)
            .withName(Component.literal("In Stellar Core").withStyle(ChatFormatting.GOLD));
      b = new ExplainIngredient(Items.BLAZE_POWDER, 1, "", false)
            .withName(Component.literal("Salvage Enchanted Equipment").withStyle(ChatFormatting.GOLD))
            .withLore(List.of(Component.literal("Use a Stellar Core to salvage enchanted equipment").withStyle(ChatFormatting.YELLOW)));
      m = new ExplainIngredient(Items.MAGMA_BLOCK, 1, "", false)
            .withName(Component.literal("Salvage Enchanted Equipment").withStyle(ChatFormatting.GOLD))
            .withLore(List.of(Component.literal("Use a Stellar Core to salvage enchanted equipment").withStyle(ChatFormatting.YELLOW)));
      ItemStack chestplate2 = new ItemStack(Items.DIAMOND_CHESTPLATE);
      chestplate2.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
      g = new ExplainIngredient(chestplate2, 1, "Piece of Enchanted Equipment", true)
            .withName(Component.literal("Enchanted Equipment").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE))
            .withLore(List.of(Component.literal("Better enchantments yield more Stardust").withStyle(ChatFormatting.DARK_PURPLE)));
      
      ingredients = new ExplainIngredient[][]{
            {a, a, a, a, a},
            {a, m, b, m, a},
            {a, b, g, b, a},
            {a, m, b, m, a},
            {a, a, a, a, a}};
      ARCANA_RECIPES.add(new ExplainRecipe(ArcanaRegistry.STARDUST, ingredients));
      
      
      // ===================================
      //         CRYING OBSIDIAN
      // ===================================
      w = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LAPIS_COLOR), 1, "", false)
            .withName(Component.literal("Water").withStyle(ChatFormatting.BLUE))
            .withLore(List.of(Component.literal("Throw the ingredients into water").withStyle(ChatFormatting.AQUA)));
      a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR), 1, "", false)
            .withName(Component.literal("In World Recipe").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Do this in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      o = new ExplainIngredient(Items.OBSIDIAN, 1, "Obsidian")
            .withName(Component.literal("Obsidian").withStyle(ChatFormatting.DARK_PURPLE));
      r = new ExplainIngredient(Items.REDSTONE, 16, "Redstone Dust")
            .withName(Component.literal("Redstone Dust").withStyle(ChatFormatting.RED))
            .withLore(List.of(Component.literal("Use Redstone OR Glowstone Dust").withStyle(ChatFormatting.GOLD)));
      g = new ExplainIngredient(Items.GLOWSTONE_DUST, 4, "Glowstone Dust")
            .withName(Component.literal("Glowstone Dust").withStyle(ChatFormatting.YELLOW))
            .withLore(List.of(Component.literal("Use Redstone OR Glowstone Dust").withStyle(ChatFormatting.GOLD)));
      
      ingredients = new ExplainIngredient[][]{
            {a, a, a, a, a},
            {a, r, a, g, a},
            {a, w, o, w, a},
            {a, w, w, w, a},
            {a, a, a, a, a}};
      ARCANA_RECIPES.add(new ExplainRecipe(Items.CRYING_OBSIDIAN, ingredients));
      
      
      // Permutation Recipes
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("faeries_stew", new ItemStack(Items.MUSHROOM_STEW, 1), MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 1)), new ItemStack(Items.NETHER_WART), (stack, minecraftServer) -> {
         ItemStack stewStack = new ItemStack(Items.SUSPICIOUS_STEW);
         stewStack.set(DataComponents.RARITY, Rarity.RARE);
         stewStack.set(DataComponents.ITEM_NAME, Component.translatable("item." + MOD_ID + ".faeries_stew"));
         List<SuspiciousStewEffects.Entry> effects = new ArrayList<>();
         Registry<MobEffect> effectRegistry = minecraftServer.registryAccess().lookupOrThrow(Registries.MOB_EFFECT);
         List<Holder.Reference<MobEffect>> effectEntries = effectRegistry.listElements().toList();
         int count = 0;
         while(count < 10 && (Math.random() < 0.35 || count == 0)){
            effects.add(new SuspiciousStewEffects.Entry(effectEntries.get((int) (Math.random() * effectEntries.size())), (int) (Math.random() * 580 + 20)));
            count++;
         }
         SuspiciousStewEffects comp = new SuspiciousStewEffects(effects);
         stewStack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, comp);
         return stewStack;
      }, Component.literal("A 'Delicious' Stew")));
      
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("book_exchange", new ItemStack(Items.ENCHANTED_BOOK, 1), MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 15)), new ItemStack(Items.LAPIS_LAZULI, 5), (stack, minecraftServer) -> {
         ItemStack newBook = new ItemStack(Items.BOOK);
         ArrayList<Holder<Enchantment>> enchants = new ArrayList<>();
         minecraftServer.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap().forEach(enchants::add);
         return EnchantmentHelper.enchantItem(RandomSource.create(), newBook, (int) (Math.random() * 30 + 15), enchants.stream());
      }, Component.literal("A Random ").append(Component.translatable(Items.ENCHANTED_BOOK.getDescriptionId()))));
      
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("goat_horns", new ItemStack(Items.GOAT_HORN, 1), MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 3)), new ItemStack(Items.AMETHYST_SHARD, 7), (stack, minecraftServer) -> {
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
   
   private static void addTransmutationExplainRecipes(){
      for(CompendiumEntry entry : RECOMMENDED_LIST){
         if(entry instanceof ArcanaItemCompendiumEntry arcanaEntry){
            ArcanaItem arcanaItem = arcanaEntry.getArcanaItem();
            for(TransmutationRecipe transRecipe : TRANSMUTATION_RECIPES){
               if(!(transRecipe instanceof InfusionTransmutationRecipe infusion)) continue;
               if(arcanaItem.getItem() != infusion.getOutput().getItem()) continue;
               System.out.println("Adding recipe for "+infusion.getId());
               ARCANA_RECIPES.add(createInfusionExplainRecipe(infusion));
            }
         }else if(entry instanceof IngredientCompendiumEntry ingEntry){
            ItemStack stack = ingEntry.getDisplayStack();
            for(TransmutationRecipe transRecipe : TRANSMUTATION_RECIPES){
               if(!(transRecipe instanceof InfusionTransmutationRecipe infusion)) continue;
               if(!stack.is(infusion.getOutput().getItem())) continue;
               System.out.println("Adding recipe for "+infusion.getId());
               ARCANA_RECIPES.add(createInfusionExplainRecipe(infusion));
            }
         }
      }
   }
   
   private static ExplainRecipe createInfusionExplainRecipe(InfusionTransmutationRecipe recipe){
      ItemStack reagent1 = recipe.getExampleReagent1();
      ItemStack reagent2 = recipe.getExampleReagent2();
      ItemStack input = recipe.getExampleInput();
      
      ExplainIngredient b = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_BLACK_COLOR), 1, "", false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      ExplainIngredient w = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LIGHT_COLOR), 1, "", false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      ExplainIngredient c = new ExplainIngredient(reagent1.getItem(), reagent1.getCount(), reagent1.getItemName().getString())
            .withName(reagent1.getItemName().copy().withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.LIGHT_PURPLE)));
      ExplainIngredient t = new ExplainIngredient(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem(), 1, "", false)
            .withName(Component.literal("Transmutation Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      ExplainIngredient d = new ExplainIngredient(reagent2.getItem(), reagent2.getCount(), reagent2.getItemName().getString())
            .withName(reagent2.getItemName().copy().withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.LIGHT_PURPLE)));
      ExplainIngredient p = new ExplainIngredient(input.getItem(), input.getCount(), recipe.getInputName().getString())
            .withName(recipe.getInputName().copy().withStyle(ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Infusion Input").withStyle(ChatFormatting.WHITE)));
      
      ExplainIngredient[][] ingredients = {
            {b, b, p, b, b},
            {b, b, b, b, w},
            {c, b, t, w, d},
            {b, w, w, w, w},
            {w, w, w, w, w}};
      
      return new ExplainRecipe(recipe.getOutput().getItem(), ingredients);
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
         if(recipe.canTransmute(positive, negative, re1, re2, aequalis, altar)){
            matching = recipe;
         }
      }
      return matching;
   }
   
   public static TransmutationRecipe findMatchingTransmutationRecipe(String id){
      return TRANSMUTATION_RECIPES.stream().filter(r -> r.getId().equals(id)).findAny().orElse(null);
   }
}
