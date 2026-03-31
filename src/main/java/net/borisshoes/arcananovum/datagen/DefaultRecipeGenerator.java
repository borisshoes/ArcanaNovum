package net.borisshoes.arcananovum.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.recipes.arcana.*;
import net.borisshoes.arcananovum.recipes.transmutation.CommutativeTransmutationRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.InfusionTransmutationRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.TransmutationRecipe;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DefaultRecipeGenerator {
   
   public static void generateBuiltInRecipes(){
      Path dirPathClassic = FabricLoader.getInstance().getConfigDir().resolve("arcananovum").resolve("recipes").resolve("classic");
      Path dirPathDefault = FabricLoader.getInstance().getConfigDir().resolve("arcananovum").resolve("recipes").resolve("default");
      
      // Delete existing directories if they exist
      try{
         deleteDirectoryRecursively(dirPathClassic);
         deleteDirectoryRecursively(dirPathDefault);
      }catch(IOException e){
         ArcanaNovum.log(2, "Error deleting existing recipe directories: " + e.getMessage());
      }
      
      generateClassicRecipes();
      generateDefaultRecipes();
   }
   
   public static void generateDefaultRecipes(){
      List<ArcanaRecipe> arcanaRecipes = new ArrayList<>();
      ArcanaIngredient a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;
      ArcanaIngredient[][] ingredients;
      
      
      // ===================================
      //         CHARM OF NEGOTIATION
      // ===================================
      a = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE, 1);
      b = new ArcanaIngredient(Items.GOLD_NUGGET, 4);
      c = new ArcanaIngredient(Items.GOLD_INGOT, 4);
      d = new ArcanaIngredient(Items.GOLD_BLOCK, 1);
      e = new ArcanaIngredient(Items.EMERALD, 4);
      f = new ArcanaIngredient(Items.EMERALD_BLOCK, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.NEGOTIATION_CHARM, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //           CLOCKWORK MULTITOOL
      // ===================================
      a = new ArcanaIngredient(Items.CRAFTER, 1);
      b = new ArcanaIngredient(Items.GOLD_INGOT, 1);
      c = new ArcanaIngredient(Items.IRON_CHAIN, 2);
      d = new ArcanaIngredient(Items.CLOCK, 2);
      e = new ArcanaIngredient(Items.SMITHING_TABLE, 1);
      f = new ArcanaIngredient(Items.GRINDSTONE, 1);
      g = new ArcanaIngredient(Items.CARTOGRAPHY_TABLE, 1);
      h = new ArcanaIngredient(Items.REDSTONE_BLOCK, 4);
      i = new ArcanaIngredient(Items.CRAFTING_TABLE, 1);
      j = new ArcanaIngredient(Items.STONECUTTER, 1);
      k = new ArcanaIngredient(Items.LOOM, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, f, b},
            {c, g, h, i, c},
            {b, j, k, d, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CLOCKWORK_MULTITOOL, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //           ITINERANTEUR
      // ===================================
      a = new ArcanaIngredient(Items.IRON_NUGGET, 4);
      b = new ArcanaIngredient(Items.IRON_INGOT, 2);
      c = new ArcanaIngredient(Items.IRON_CHAIN, 4);
      d = new ArcanaIngredient(Items.BLAZE_ROD, 2);
      e = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.LONG_SWIFTNESS);
      f = new ArcanaIngredient(Items.GLASS, 4);
      g = new ArcanaIngredient(Items.BLAZE_POWDER, 4);
      h = new ArcanaIngredient(ItemTags.LANTERNS, 4);
      i = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_SWIFTNESS);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {f, g, h, g, f},
            {b, d, i, d, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ITINERANTEUR, ingredients, new ForgeRequirement().withEnchanter()).addCenterpiece(12));
      
      
      // ===================================
      //           GEOMANTIC STELE
      // ===================================
      a = new ArcanaIngredient(Items.OBSIDIAN, 8);
      b = new ArcanaIngredient(Items.CHISELED_TUFF, 12);
      c = new ArcanaIngredient(Items.CHISELED_STONE_BRICKS, 8);
      d = new ArcanaIngredient(Items.DEEPSLATE_TILES, 16);
      e = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      f = new ArcanaIngredient(Items.AMETHYST_BLOCK, 8);
      g = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      h = new ArcanaIngredient(Items.NETHER_STAR, 1);
      i = new ArcanaIngredient(Items.CHISELED_TUFF_BRICKS, 16);
      j = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 8);
      k = new ArcanaIngredient(Items.QUARTZ, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {f, g, h, g, f},
            {a, d, e, d, a},
            {i, j, k, j, i}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.GEOMANTIC_STELE, ingredients, new ForgeRequirement().withAnvil().withCore()));
      
      
      // ===================================
      //           INTERDICTOR
      // ===================================
      a = new ArcanaIngredient(Items.PURPUR_PILLAR, 4);
      b = new ArcanaIngredient(Items.END_STONE_BRICKS, 2);
      c = new ArcanaIngredient(Items.ENDER_EYE, 2);
      d = new ArcanaIngredient(Items.ENDER_PEARL, 8);
      e = new ArcanaIngredient(Items.NETHER_STAR, 1);
      f = new ArcanaIngredient(Items.END_CRYSTAL, 4);
      g = new ArcanaIngredient(Items.BEACON, 1);
      h = new ArcanaIngredient(Items.OBSIDIAN, 4);
      i = new GenericArcanaIngredient(ArcanaRegistry.EXOTIC_MATTER, 1);
      j = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, f, g, f, c},
            {b, h, i, h, b},
            {a, j, j, j, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.INTERDICTOR, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //           ASTRAL GATEWAY
      // ===================================
      a = new ArcanaIngredient(Items.END_STONE_BRICKS, 4);
      b = new ArcanaIngredient(ArcanaRegistry.STARDUST, 2);
      c = new ArcanaIngredient(Items.ENDER_EYE, 4);
      d = new ArcanaIngredient(Items.OBSIDIAN, 4);
      e = new ArcanaIngredient(Items.NETHER_STAR, 1);
      f = new ArcanaIngredient(Items.ENDER_PEARL, 8);
      g = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      h = new GenericArcanaIngredient(ArcanaRegistry.EXOTIC_MATTER, 1);
      i = new ArcanaIngredient(Items.IRON_INGOT, 4);
      j = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {f, g, h, g, f},
            {i, d, j, d, i},
            {a, i, c, i, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ASTRAL_GATEWAY, ingredients, new ForgeRequirement().withAnvil().withCore()));
      
      
      // ===================================
      //           ENDER CRATE
      // ===================================
      a = new ArcanaIngredient(ItemTags.LOGS, 2);
      b = new ArcanaIngredient(Items.OBSIDIAN, 1);
      c = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 2);
      d = new ArcanaIngredient(Items.ENDER_EYE, 2);
      e = new ArcanaIngredient(Items.BARREL, 4);
      f = new ArcanaIngredient(Items.ENDER_CHEST, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ENDER_CRATE, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          ALCHEMICAL ARBALEST
      // ===================================
      a = new ArcanaIngredient(Items.GLOWSTONE_DUST, 8);
      b = new ArcanaIngredient(Items.FERMENTED_SPIDER_EYE, 4);
      c = new ArcanaIngredient(Items.DRAGON_BREATH, 4);
      d = new ArcanaIngredient(Items.BLAZE_POWDER, 3);
      e = new ArcanaIngredient(Items.NETHER_WART, 8);
      f = new ArcanaIngredient(Items.SPECTRAL_ARROW, 8);
      g = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      h = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.MULTISHOT, 1));
      i = new ArcanaIngredient(Items.NETHER_STAR, 1);
      j = new ArcanaIngredient(Items.CROSSBOW, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, e},
            {b, f, g, h, d},
            {c, i, j, i, c},
            {d, h, g, f, b},
            {e, d, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ALCHEMICAL_ARBALEST, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore().withFletchery()).addCenterpiece(12));
      
      
      // ===================================
      //          ANCIENT DOWSING ROD
      // ===================================
      a = new ArcanaIngredient(Items.GOLD_INGOT, 4);
      b = new ArcanaIngredient(Items.RED_NETHER_BRICKS, 6);
      c = new ArcanaIngredient(Items.FIRE_CHARGE, 2);
      d = new ArcanaIngredient(Items.BLAZE_ROD, 2);
      e = new ArcanaIngredient(Items.ANCIENT_DEBRIS, 1);
      f = new ArcanaIngredient(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1);
      g = new ArcanaIngredient(Items.BELL, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, d},
            {b, e, a, e, d},
            {c, f, g, f, c},
            {d, e, a, e, b},
            {d, d, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ANCIENT_DOWSING_ROD, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          AQUATIC EVERSOURCE
      // ===================================
      a = new ArcanaIngredient(Items.BUCKET, 1);
      b = new ArcanaIngredient(Items.GOLD_INGOT, 1);
      c = new ArcanaIngredient(Items.BLUE_ICE, 1);
      d = new ArcanaIngredient(Items.DIAMOND, 1);
      e = new ArcanaIngredient(Items.HEART_OF_THE_SEA, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, d, c, b},
            {c, d, e, d, c},
            {b, c, d, c, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.AQUATIC_EVERSOURCE, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          ARCANE FLAK ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.GLOWSTONE_DUST, 4);
      b = new ArcanaIngredient(Items.FIREWORK_STAR, 4);
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new ArcanaIngredient(Items.DRAGON_BREATH, 4);
      e = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, d, ArcanaIngredient.EMPTY},
            {a, c, e, c, a},
            {ArcanaIngredient.EMPTY, d, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ARCANE_FLAK_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // ===================================
      //          ARCANE SINGULARITY
      // ===================================
      a = new ArcanaIngredient(ArcanaRegistry.NEBULOUS_ESSENCE, 8);
      b = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 8);
      c = new ArcanaIngredient(Items.ENDER_EYE, 6);
      d = new ArcanaIngredient(ArcanaRegistry.STARDUST, 16);
      e = new ArcanaIngredient(Items.NETHERITE_SCRAP, 1);
      f = new ArcanaIngredient(Items.NETHER_STAR, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, a, d, a, b},
            {c, e, f, e, c},
            {b, a, d, a, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ARCANE_SINGULARITY, ingredients, new ForgeRequirement().withEnchanter().withAnvil()));
      
      
      // ===================================
      //          ARCANISTS BELT
      // ===================================
      a = new ArcanaIngredient(Items.GOLD_INGOT, 3);
      b = new ArcanaIngredient(Items.LEATHER, 4);
      c = new ArcanaIngredient(Items.NETHER_STAR, 1);
      d = new ArcanaIngredient(Items.CHEST, 2);
      e = new ArcanaIngredient(Items.ENDER_CHEST, 2);
      f = new ArcanaIngredient(Items.NETHERITE_SCRAP, 2);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, b, b, a},
            {b, a, c, a, b},
            {d, d, e, d, d},
            {b, a, f, a, b},
            {a, b, b, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ARCANISTS_BELT, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //          BINARY BLADES
      // ===================================
      a = new ArcanaIngredient(Items.GLOWSTONE_DUST, 8);
      b = new ArcanaIngredient(ArcanaRegistry.STARDUST, 4);
      c = new ArcanaIngredient(Items.DIAMOND, 4);
      d = new ArcanaIngredient(Items.NETHERITE_SWORD, 1, true);
      e = new ArcanaIngredient(Items.BLAZE_POWDER, 8);
      f = new ArcanaIngredient(Items.NETHER_STAR, 1);
      g = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, f, b},
            {c, e, g, e, c},
            {b, f, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.BINARY_BLADES, ingredients, new ForgeRequirement().withAnvil().withCore()).addCenterpiece(6).addCenterpiece(18));
      
      
      // ===================================
      //          BLINK ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_PEARL, 2);
      b = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      c = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, a, b, a, ArcanaIngredient.EMPTY},
            {a, b, c, b, a},
            {ArcanaIngredient.EMPTY, a, b, a, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.BLINK_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // ===================================
      //          BRAIN JAR
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_CHEST, 2);
      b = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE, 3);
      c = new ArcanaIngredient(Items.SCULK, 8);
      d = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.MENDING, 1));
      e = new ArcanaIngredient(Items.SCULK_CATALYST, 4);
      f = new ArcanaIngredient(Items.ZOMBIE_HEAD, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.BRAIN_JAR, ingredients, new ForgeRequirement().withEnchanter().withAnvil()));
      
      
      // ===================================
      //          CATALYTIC MATRIX
      // ===================================
      a = new ArcanaIngredient(Items.AMETHYST_SHARD, 3);
      b = new ArcanaIngredient(Items.CRAFTER, 1);
      c = new ArcanaIngredient(Items.DIAMOND, 1);
      d = new ArcanaIngredient(Items.NETHER_STAR, 1);
      e = new ArcanaIngredient(Items.END_CRYSTAL, 1);
      f = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, a, b, a},
            {b, c, d, c, b},
            {a, e, f, e, a},
            {b, c, e, c, b},
            {a, b, a, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CATALYTIC_MATRIX, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //          CELESTIAL ALTAR
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 16);
      b = new ArcanaIngredient(Items.GOLD_BLOCK, 1);
      c = new ArcanaIngredient(Items.GLOWSTONE, 1);
      d = new ArcanaIngredient(Items.OBSIDIAN, 16);
      e = new ArcanaIngredient(ArcanaRegistry.STARDUST, 6);
      f = new ArcanaIngredient(Items.NETHER_STAR, 2);
      g = new ArcanaIngredient(Items.PEARLESCENT_FROGLIGHT, 1);
      h = new ArcanaIngredient(Items.SEA_LANTERN, 1);
      i = new ArcanaIngredient(Items.LAPIS_BLOCK, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, b, c, c},
            {b, d, b, c, c},
            {e, f, g, f, e},
            {h, h, i, d, i},
            {h, h, i, i, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CELESTIAL_ALTAR, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          CETACEA CHARM
      // ===================================
      a = new ArcanaIngredient(Items.TURTLE_SCUTE, 1);
      b = new ArcanaIngredient(Items.PRISMARINE_CRYSTALS, 2);
      c = new ArcanaIngredient(Items.PUFFERFISH, 2);
      d = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_SWIFTNESS);
      e = new ArcanaIngredient(Items.TROPICAL_FISH, 2);
      f = new ArcanaIngredient(Items.NAUTILUS_SHELL, 1);
      g = new ArcanaIngredient(Items.CONDUIT, 1);
      h = new ArcanaIngredient(Items.COD, 2);
      i = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.LONG_WATER_BREATHING);
      j = new ArcanaIngredient(Items.SALMON, 2);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, a, b, a},
            {b, c, d, e, b},
            {a, f, g, f, a},
            {b, h, i, j, b},
            {a, b, a, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CETACEA_CHARM, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          CHEST TRANSLOCATOR
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 1);
      b = new ArcanaIngredient(Items.OBSIDIAN, 1);
      c = new ArcanaIngredient(Items.ENDER_EYE, 1);
      d = new ArcanaIngredient(Items.BARREL, 4);
      e = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_STRENGTH);
      f = new ArcanaIngredient(Items.CHEST, 4);
      g = new ArcanaIngredient(Items.ENDER_CHEST, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, b, b, a},
            {b, c, d, c, b},
            {e, f, g, f, e},
            {b, c, d, c, b},
            {a, b, b, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CHEST_TRANSLOCATOR, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          CINDERS CHARM
      // ===================================
      a = new ArcanaIngredient(Items.COAL_BLOCK, 1);
      b = new ArcanaIngredient(Items.BLAZE_ROD, 2);
      c = new ArcanaIngredient(Items.FIRE_CHARGE, 4);
      d = new ArcanaIngredient(Items.MAGMA_CREAM, 4);
      e = new ArcanaIngredient(Items.NETHER_STAR, 1);
      f = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, a, f, a, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CINDERS_CHARM, ingredients, new ForgeRequirement().withAnvil().withCore()));
      
      
      // ===================================
      //          CLEANSING CHARM
      // ===================================
      a = new ArcanaIngredient(Items.MILK_BUCKET, 1);
      b = new ArcanaIngredient(Items.HONEY_BOTTLE, 2);
      c = new ArcanaIngredient(Items.CHARCOAL, 6);
      d = new ArcanaIngredient(Items.DIAMOND, 2);
      e = new ArcanaIngredient(Items.QUARTZ, 8);
      f = new ArcanaIngredient(Items.NETHER_STAR, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CLEANSING_CHARM, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          CONCUSSION ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.INK_SAC, 4);
      b = new ArcanaIngredient(Items.GLOW_INK_SAC, 2);
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new ArcanaIngredient(Items.LINGERING_POTION, 1).withPotion(Potions.LONG_WEAKNESS);
      e = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      f = new ArcanaIngredient(Items.LINGERING_POTION, 1).withPotion(Potions.STRONG_SLOWNESS);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, d, ArcanaIngredient.EMPTY},
            {a, c, e, c, a},
            {ArcanaIngredient.EMPTY, f, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CONCUSSION_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // ===================================
      //          CONTAINMENT CIRCLET
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 1);
      b = new ArcanaIngredient(Items.OBSIDIAN, 1);
      c = new ArcanaIngredient(Items.IRON_BARS, 8);
      d = new ArcanaIngredient(Items.IRON_CHAIN, 6);
      e = new ArcanaIngredient(Items.COBWEB, 2);
      f = new ArcanaIngredient(Items.ENDER_CHEST, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CONTAINMENT_CIRCLET, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          CONTINUUM ANCHOR
      // ===================================
      a = new ArcanaIngredient(Items.OBSIDIAN, 2);
      b = new ArcanaIngredient(Items.RESPAWN_ANCHOR, 2);
      c = new ArcanaIngredient(Items.ENDER_EYE, 4);
      d = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      e = new ArcanaIngredient(Items.NETHER_STAR, 1);
      f = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, a, b, a},
            {b, c, d, c, b},
            {a, e, f, e, a},
            {b, c, d, c, b},
            {a, b, a, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CONTINUUM_ANCHOR, ingredients, new ForgeRequirement().withAnvil().withCore()));
      
      
      // ===================================
      //          DETONATION ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.TNT, 2);
      b = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      c = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, a, b, a, ArcanaIngredient.EMPTY},
            {a, b, c, b, a},
            {ArcanaIngredient.EMPTY, a, b, a, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.DETONATION_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // ===================================
      //          EMPOWERED CATALYST
      // ===================================
      a = new ArcanaIngredient(Items.OBSIDIAN, 2);
      b = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 2);
      c = new ArcanaIngredient(Items.EMERALD, 2);
      d = new GenericArcanaIngredient(ArcanaRegistry.MUNDANE_CATALYST, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {a, c, d, c, a},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.EMPOWERED_CATALYST, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //          ENSNAREMENT ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 12);
      b = new ArcanaIngredient(Items.COBWEB, 8);
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_SLOWNESS);
      e = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {d, c, e, c, d},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ENSNAREMENT_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // ===================================
      //          ESSENCE EGG
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      b = new ArcanaIngredient(Items.OBSIDIAN, 4);
      c = new ArcanaIngredient(Items.IRON_BARS, 8);
      d = new ArcanaIngredient(Items.RED_NETHER_BRICKS, 4);
      e = new ArcanaIngredient(Items.SOUL_SAND, 8);
      f = new SoulstoneIngredient(Soulstone.tiers[0], true, false, false, null);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ESSENCE_EGG, ingredients, new ForgeRequirement()).addCenterpiece(12));
      
      
      // ===================================
      //          EVERLASTING ROCKET
      // ===================================
      a = new ArcanaIngredient(Items.FIREWORK_ROCKET, 4);
      b = new ArcanaIngredient(Items.GUNPOWDER, 2);
      c = new ArcanaIngredient(Items.PAPER, 3);
      d = new ArcanaIngredient(Items.FIREWORK_STAR, 8);
      e = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.MENDING, 1));
      f = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.UNBREAKING, 3));
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, d, c, b},
            {c, e, a, f, c},
            {b, c, d, c, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.EVERLASTING_ROCKET, ingredients, new ForgeRequirement().withEnchanter()).addCenterpiece(12));
      
      
      // ===================================
      //          EXOTIC CATALYST
      // ===================================
      a = new ArcanaIngredient(Items.OBSIDIAN, 3);
      b = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 3);
      c = new ArcanaIngredient(Items.DIAMOND, 3);
      d = new GenericArcanaIngredient(ArcanaRegistry.EMPOWERED_CATALYST, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {a, c, d, c, a},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.EXOTIC_CATALYST, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //          EXOTIC MATTER
      // ===================================
      a = new ArcanaIngredient(Items.OBSIDIAN, 2);
      b = new ArcanaIngredient(Items.END_CRYSTAL, 1);
      c = new ArcanaIngredient(Items.DIAMOND, 1);
      d = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 1);
      e = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {d, a, d, a, b},
            {c, d, e, d, c},
            {b, a, d, a, d},
            {a, d, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.EXOTIC_MATTER, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          EXPULSION ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.AMETHYST_SHARD, 8);
      b = new ArcanaIngredient(Items.SLIME_BLOCK, 1);
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new ArcanaIngredient(Items.ENDER_PEARL, 4);
      e = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {d, c, e, c, d},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.EXPULSION_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // ===================================
      //          FEASTING CHARM
      // ===================================
      a = new ArcanaIngredient(Items.COOKED_SALMON, 2);
      b = new ArcanaIngredient(Items.GLOW_BERRIES, 2);
      c = new ArcanaIngredient(Items.COOKED_BEEF, 2);
      d = new ArcanaIngredient(Items.PUMPKIN_PIE, 2);
      e = new ArcanaIngredient(Items.BREAD, 2);
      f = new ArcanaIngredient(Items.COOKIE, 2);
      g = new ArcanaIngredient(Items.GOLDEN_CARROT, 4);
      h = new ArcanaIngredient(Items.GOLDEN_APPLE, 2);
      i = new ArcanaIngredient(Items.MELON_SLICE, 2);
      j = new ArcanaIngredient(Items.COOKED_CHICKEN, 2);
      k = new ArcanaIngredient(Items.ENCHANTED_GOLDEN_APPLE, 1, true);
      l = new ArcanaIngredient(Items.COOKED_MUTTON, 2);
      m = new ArcanaIngredient(Items.BEETROOT, 2);
      n = new ArcanaIngredient(Items.DRIED_KELP, 2);
      o = new ArcanaIngredient(Items.COOKED_RABBIT, 2);
      p = new ArcanaIngredient(Items.BAKED_POTATO, 2);
      q = new ArcanaIngredient(Items.COOKED_PORKCHOP, 2);
      r = new ArcanaIngredient(Items.SWEET_BERRIES, 2);
      s = new ArcanaIngredient(Items.COOKED_COD, 2);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, e},
            {f, g, h, g, i},
            {j, h, k, h, l},
            {m, g, h, g, n},
            {o, p, q, r, s}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.FEASTING_CHARM, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore()));
      
      
      // ===================================
      //          FELIDAE CHARM
      // ===================================
      a = new ArcanaIngredient(Items.GUNPOWDER, 2);
      b = new ArcanaIngredient(Items.STRING, 4);
      c = new ArcanaIngredient(Items.PUFFERFISH, 2);
      d = new ArcanaIngredient(Items.PHANTOM_MEMBRANE, 4);
      e = new ArcanaIngredient(Items.COD, 2);
      f = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.FEATHER_FALLING, 4));
      g = new ArcanaIngredient(Items.CREEPER_HEAD, 1, true);
      h = new ArcanaIngredient(Items.TROPICAL_FISH, 2);
      i = new ArcanaIngredient(Items.SALMON, 2);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, a, d, a, b},
            {e, f, g, f, h},
            {b, a, d, a, b},
            {a, b, i, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.FELIDAE_CHARM, ingredients, new ForgeRequirement().withEnchanter()));
      
      
      // ===================================
      //          FRACTAL SPONGE
      // ===================================
      a = new ArcanaIngredient(Items.OBSIDIAN, 4);
      b = new ArcanaIngredient(Items.MAGMA_BLOCK, 4);
      c = new ArcanaIngredient(Items.SPONGE, 1);
      d = new ArcanaIngredient(Items.BLUE_ICE, 4);
      e = new ArcanaIngredient(Items.END_CRYSTAL, 1);
      f = new ArcanaIngredient(Items.NETHER_STAR, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {b, e, c, e, d},
            {c, c, f, c, c},
            {d, e, c, e, b},
            {a, d, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.FRACTAL_SPONGE, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          GRAVITON ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.COBWEB, 4);
      b = new ArcanaIngredient(Items.LINGERING_POTION, 1).withPotion(Potions.STRONG_SLOWNESS);
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, a, ArcanaIngredient.EMPTY},
            {a, c, d, c, a},
            {ArcanaIngredient.EMPTY, a, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.GRAVITON_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // ===================================
      //          GRAVITON MAUL
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 8);
      b = new ArcanaIngredient(Items.OBSIDIAN, 4);
      c = new ArcanaIngredient(Items.BREEZE_ROD, 12);
      d = new ArcanaIngredient(Items.COBWEB, 12);
      e = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      f = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.BREACH, 4));
      g = new ArcanaIngredient(Items.NETHER_STAR, 1);
      h = new ArcanaIngredient(Items.MACE, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {f, g, h, g, f},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.GRAVITON_MAUL, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore()).addCenterpiece(12));
      
      
      // ===================================
      //          IGNEOUS COLLIDER
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      b = new ArcanaIngredient(Items.OBSIDIAN, 4);
      c = new ArcanaIngredient(Items.MAGMA_BLOCK, 4);
      d = new ArcanaIngredient(Items.CAULDRON, 1);
      e = new ArcanaIngredient(Items.BLUE_ICE, 4);
      f = new ArcanaIngredient(Items.DIAMOND_PICKAXE, 1, true).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.EFFICIENCY, 5), new ArcanaIngredient.EnchantmentEntry(Enchantments.UNBREAKING, 3));
      g = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, a, b, a},
            {b, c, d, e, b},
            {a, f, g, f, a},
            {b, c, d, e, b},
            {a, b, a, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.IGNEOUS_COLLIDER, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore()));
      
      
      // ===================================
      //          LEVITATION HARNESS
      // ===================================
      a = new ArcanaIngredient(Items.SHULKER_SHELL, 4);
      b = new ArcanaIngredient(Items.GLOWSTONE, 2);
      c = new ArcanaIngredient(Items.LEATHER, 4);
      d = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      e = new ArcanaIngredient(Items.ELYTRA, 1);
      f = new ArcanaIngredient(Items.NETHER_STAR, 1);
      g = new ShulkerCoreIngredient(true, 100);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, d, e, b},
            {c, f, g, f, c},
            {b, e, d, c, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.LEVITATION_HARNESS, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore().withSingularity()).addCenterpiece(12));
      
      
      // ===================================
      //          LIGHT CHARM
      // ===================================
      a = new ArcanaIngredient(Items.GLOWSTONE, 4);
      b = new ArcanaIngredient(Items.LANTERN, 4);
      c = new ArcanaIngredient(Items.SOUL_LANTERN, 4);
      d = new ArcanaIngredient(Items.COPPER_LANTERN.unaffected(), 4);
      e = new ArcanaIngredient(Items.VERDANT_FROGLIGHT, 4);
      f = new ArcanaIngredient(Items.SEA_LANTERN, 2);
      g = new ArcanaIngredient(Items.CANDLE, 4);
      h = new ArcanaIngredient(Items.REDSTONE_LAMP, 4);
      i = new ArcanaIngredient(Items.PEARLESCENT_FROGLIGHT, 4);
      j = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.LONG_NIGHT_VISION);
      k = new ArcanaIngredient(Items.BEACON, 1, true);
      l = new ArcanaIngredient(Items.SHROOMLIGHT, 4);
      m = new ArcanaIngredient(Items.OCHRE_FROGLIGHT, 4);
      n = new ArcanaIngredient(Items.JACK_O_LANTERN, 4);
      o = new ArcanaIngredient(Items.COPPER_BULB, 4);
      p = new ArcanaIngredient(Items.TORCH, 4);
      q = new ArcanaIngredient(Items.SOUL_TORCH, 4);
      r = new ArcanaIngredient(Items.COPPER_TORCH, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {e, f, g, f, h},
            {i, j, k, j, l},
            {m, f, n, f, o},
            {a, p, q, r, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.LIGHT_CHARM, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          MAGMATIC EVERSOURCE
      // ===================================
      a = new ArcanaIngredient(Items.BUCKET, 4);
      b = new ArcanaIngredient(Items.MAGMA_BLOCK, 4);
      c = new ArcanaIngredient(Items.BLAZE_POWDER, 8);
      d = new ArcanaIngredient(Items.BLAZE_ROD, 8);
      e = new ArcanaIngredient(Items.RED_NETHER_BRICKS, 4);
      f = new ArcanaIngredient(Items.NETHERITE_SCRAP, 1);
      g = new ArcanaIngredient(Items.MAGMA_CREAM, 12);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, f, g, f, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.MAGMATIC_EVERSOURCE, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //          MAGNETISM CHARM
      // ===================================
      a = new ArcanaIngredient(Items.LIGHTNING_ROD, 4);
      b = new ArcanaIngredient(Items.IRON_INGOT, 2);
      c = new ArcanaIngredient(Items.IRON_BARS, 4);
      d = new ArcanaIngredient(Items.IRON_BLOCK, 1);
      e = new ArcanaIngredient(Items.HEAVY_CORE, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, d, c, b},
            {c, d, e, d, c},
            {b, c, d, c, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.MAGNETISM_CHARM, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          MIDNIGHT ENCHANTER
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 6);
      b = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.PROTECTION, 4));
      c = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.THORNS, 3));
      d = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.BINDING_CURSE, 1));
      f = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.EFFICIENCY, 5));
      g = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE, 4);
      h = new ArcanaIngredient(Items.LAPIS_BLOCK, 4);
      j = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.SWIFT_SNEAK, 3));
      k = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.UNBREAKING, 3));
      m = new ArcanaIngredient(Items.ENCHANTING_TABLE, 4);
      o = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.SOUL_SPEED, 3));
      p = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.MENDING, 1));
      t = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.FEATHER_FALLING, 4));
      v = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.SHARPNESS, 5));
      w = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.LOOTING, 3));
      x = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.VANISHING_CURSE, 1));
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {f, g, h, g, j},
            {k, h, m, h, o},
            {p, g, h, g, t},
            {a, v, w, x, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.MIDNIGHT_ENCHANTER, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          MUNDANE CATALYST
      // ===================================
      a = new ArcanaIngredient(Items.OBSIDIAN, 1);
      b = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 1);
      c = new ArcanaIngredient(Items.QUARTZ, 1);
      d = new GenericArcanaIngredient(ArcanaRegistry.CATALYTIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {a, c, d, c, a},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.MUNDANE_CATALYST, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //          OVERFLOWING QUIVER
      // ===================================
      a = new ArcanaIngredient(Items.COPPER_INGOT, 4);
      b = new ArcanaIngredient(Items.RABBIT_HIDE, 4);
      c = new ArcanaIngredient(Items.CHEST, 2);
      d = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.INFINITY, 1));
      e = new ArcanaIngredient(Items.SPECTRAL_ARROW, 8);
      f = new ArcanaIngredient(Items.NETHER_STAR, 1);
      g = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, f, b},
            {c, e, g, e, c},
            {b, f, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.OVERFLOWING_QUIVER, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore().withFletchery()));
      
      
      // ===================================
      //          PEARL OF RECALL
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_PEARL, 4);
      b = new ArcanaIngredient(Items.GOLD_INGOT, 2);
      c = new ArcanaIngredient(Items.CLOCK, 2);
      d = new ArcanaIngredient(Items.ENDER_EYE, 2);
      e = new WaystoneIngredient(true);
      f = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      g = new ArcanaIngredient(Items.NETHER_STAR, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {d, c, f, c, d},
            {b, d, g, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.PEARL_OF_RECALL, ingredients, new ForgeRequirement()).addCenterpiece(7));
      
      
      // ===================================
      //          PHOTONIC ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.BEACON, 1);
      b = new ArcanaIngredient(Items.GLOW_INK_SAC, 4);
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new ArcanaIngredient(Items.AMETHYST_CLUSTER, 8);
      e = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {d, c, e, c, d},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, d, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.PHOTONIC_ARROWS, ingredients, new ForgeRequirement().withEnchanter().withFletchery()));
      
      
      // ===================================
      //          PLANESHIFTER
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 8);
      b = new ArcanaIngredient(Items.OBSIDIAN, 4);
      c = new ArcanaIngredient(Items.ENDER_EYE, 4);
      d = new ArcanaIngredient(Items.ENDER_PEARL, 4);
      e = new ArcanaIngredient(Items.NETHER_STAR, 1);
      f = new ArcanaIngredient(Items.END_CRYSTAL, 1);
      g = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, f, g, f, c},
            {b, d, f, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.PLANESHIFTER, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          RADIANT FLETCHERY
      // ===================================
      a = new ArcanaIngredient(Items.BLAZE_POWDER, 6);
      b = new ArcanaIngredient(Items.GLOWSTONE_DUST, 4);
      c = new ArcanaIngredient(Items.END_CRYSTAL, 1);
      d = new ArcanaIngredient(Items.SPECTRAL_ARROW, 8);
      e = new ArcanaIngredient(Items.FLETCHING_TABLE, 1);
      f = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.RADIANT_FLETCHERY, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          RUNIC BOW
      // ===================================
      a = new ArcanaIngredient(Items.END_CRYSTAL, 4);
      b = new ArcanaIngredient(Items.AMETHYST_SHARD, 8);
      c = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.POWER, 5));
      d = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      e = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      f = new ArcanaIngredient(Items.NETHER_STAR, 1);
      g = new ArcanaIngredient(Items.BOW, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, f, b},
            {c, e, g, e, c},
            {b, f, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.RUNIC_BOW, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore().withFletchery()).addCenterpiece(12));
      
      
      // ===================================
      //          RUNIC MATRIX
      // ===================================
      a = new ArcanaIngredient(Items.AMETHYST_SHARD, 2);
      b = new ArcanaIngredient(Items.DIAMOND, 1);
      c = new ArcanaIngredient(Items.END_CRYSTAL, 1);
      d = new ArcanaIngredient(Items.CRAFTER, 1);
      e = new ArcanaIngredient(Items.NETHER_STAR, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, a, d, b},
            {c, a, e, a, c},
            {b, d, a, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.RUNIC_MATRIX, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          RUNIC QUIVER
      // ===================================
      a = new ArcanaIngredient(Items.AMETHYST_SHARD, 4);
      b = new ArcanaIngredient(Items.LEATHER, 4);
      c = new ArcanaIngredient(Items.NETHER_STAR, 1);
      d = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      e = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.INFINITY, 1));
      f = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      g = new GenericArcanaIngredient(ArcanaRegistry.OVERFLOWING_QUIVER, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, a, b, a},
            {b, c, d, e, b},
            {a, f, g, f, a},
            {b, e, d, c, b},
            {a, b, a, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.RUNIC_QUIVER, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore().withFletchery()).addCenterpiece(12));
      
      
      // ===================================
      //          SHADOW STALKERS GLAIVE
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_EYE, 3);
      b = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      c = new ArcanaIngredient(Items.RED_NETHER_BRICKS, 8);
      d = new ArcanaIngredient(Items.OBSIDIAN, 8);
      e = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      f = new ArcanaIngredient(Items.NETHER_STAR, 1);
      g = new ArcanaIngredient(Items.NETHERITE_SWORD, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, f, g, f, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SHADOW_STALKERS_GLAIVE, ingredients, new ForgeRequirement().withAnvil().withCore()).addCenterpiece(12));
      
      
      // ===================================
      //          SHIELD OF FORTITUDE
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      b = new ArcanaIngredient(Items.OBSIDIAN, 8);
      c = new ArcanaIngredient(Items.GOLDEN_APPLE, 4);
      d = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.BLAST_PROTECTION, 4));
      e = new ArcanaIngredient(Items.NETHER_STAR, 1);
      f = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.PROJECTILE_PROTECTION, 4));
      g = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      h = new ArcanaIngredient(Items.SHIELD, 1, true);
      i = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.PROTECTION, 4));
      j = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.FIRE_PROTECTION, 4));
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, f, b},
            {c, g, h, g, c},
            {b, i, e, j, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SHIELD_OF_FORTITUDE, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore()).addCenterpiece(12));
      
      
      // ===================================
      //          SHULKER CORE
      // ===================================
      a = new ArcanaIngredient(Items.PHANTOM_MEMBRANE, 2);
      b = new ArcanaIngredient(Items.SHULKER_SHELL, 2);
      c = new ArcanaIngredient(Items.PURPUR_BLOCK, 4);
      d = new ArcanaIngredient(Items.GLOWSTONE_DUST, 4);
      e = new ArcanaIngredient(Items.NETHER_STAR, 1);
      f = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.LONG_SLOW_FALLING);
      g = new SoulstoneIngredient(100, false, true, false, EntityType.getKey(EntityType.SHULKER).toString());
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, f, g, f, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SHULKER_CORE, ingredients, new ForgeRequirement().withAnvil().withCore()).addCenterpiece(12));
      
      
      // ===================================
      //          SIPHONING ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.GLISTERING_MELON_SLICE, 2);
      b = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_HEALING);
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new ArcanaIngredient(Items.FERMENTED_SPIDER_EYE, 4);
      e = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      f = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_HARMING);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, d, ArcanaIngredient.EMPTY},
            {a, c, e, c, a},
            {ArcanaIngredient.EMPTY, d, c, f, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SIPHONING_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // ===================================
      //          SMOKE ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.CAMPFIRE, 3);
      b = new ArcanaIngredient(Items.GLOW_INK_SAC, 3);
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new ArcanaIngredient(Items.INK_SAC, 6);
      e = new ArcanaIngredient(Items.LINGERING_POTION, 1).withPotion(Potions.LONG_WEAKNESS);
      f = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, d, ArcanaIngredient.EMPTY},
            {e, c, f, c, e},
            {ArcanaIngredient.EMPTY, d, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SMOKE_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // ===================================
      //          SOJOURNER BOOTS
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      b = new ArcanaIngredient(Items.TUFF, 8);
      c = new ArcanaIngredient(Items.OBSIDIAN, 8);
      d = new ArcanaIngredient(Items.RED_SAND, 8);
      e = new ArcanaIngredient(Items.ROOTED_DIRT, 8);
      f = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_LEAPING);
      g = new ArcanaIngredient(Items.NETHER_STAR, 1);
      h = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_SWIFTNESS);
      i = new ArcanaIngredient(Items.PACKED_MUD, 8);
      j = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      k = new ArcanaIngredient(Items.NETHERITE_BOOTS, 1, true);
      l = new ArcanaIngredient(Items.GRASS_BLOCK, 8);
      m = new ArcanaIngredient(Items.GRAVEL, 8);
      n = new ArcanaIngredient(Items.SAND, 8);
      o = new ArcanaIngredient(Items.TERRACOTTA, 8);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {e, f, g, h, i},
            {c, j, k, j, c},
            {l, h, g, f, m},
            {a, n, c, o, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SOJOURNER_BOOTS, ingredients, new ForgeRequirement().withAnvil().withCore()).addCenterpiece(12));
      
      
      // ===================================
      //          SOULSTONE
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      b = new ArcanaIngredient(Items.OBSIDIAN, 4);
      c = new ArcanaIngredient(Items.RED_NETHER_BRICKS, 6);
      d = new ArcanaIngredient(Items.SOUL_SAND, 8);
      e = new ArcanaIngredient(Items.NETHERITE_INGOT, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, a, b, a},
            {b, c, d, c, b},
            {a, d, e, d, a},
            {b, c, d, c, b},
            {a, b, a, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SOULSTONE, ingredients, new ForgeRequirement().withAnvil().withCore()));
      
      
      // ===================================
      //          SOVEREIGN CATALYST
      // ===================================
      a = new ArcanaIngredient(Items.OBSIDIAN, 4);
      b = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      c = new ArcanaIngredient(Items.GOLD_INGOT, 4);
      d = new GenericArcanaIngredient(ArcanaRegistry.EXOTIC_CATALYST, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {a, c, d, c, a},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SOVEREIGN_CATALYST, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //          SPAWNER HARNESS
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 2);
      b = new ArcanaIngredient(Items.OBSIDIAN, 2);
      c = new ArcanaIngredient(Items.DEEPSLATE_BRICKS, 8);
      d = new ArcanaIngredient(Items.ENDER_EYE, 2);
      e = new ArcanaIngredient(Items.IRON_BARS, 8);
      f = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.SILK_TOUCH, 1));
      g = new ArcanaIngredient(Items.NETHERITE_SCRAP, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {e, f, g, f, e},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SPAWNER_HARNESS, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore()));
      
      
      // ===================================
      //          SPAWNER INFUSER
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 6);
      b = new ArcanaIngredient(Items.ECHO_SHARD, 1);
      c = new ArcanaIngredient(Items.SCULK_CATALYST, 2);
      d = new ArcanaIngredient(Items.SCULK_SHRIEKER, 2);
      e = new ArcanaIngredient(Items.NETHER_STAR, 1);
      f = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      g = new GenericArcanaIngredient(ArcanaRegistry.SPAWNER_HARNESS, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, f, g, f, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SPAWNER_INFUSER, ingredients, new ForgeRequirement().withEnchanter().withAnvil().withCore().withSingularity()));
      
      
      // ===================================
      //          STARPATH ALTAR
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_EYE, 6);
      b = new ArcanaIngredient(Items.OBSIDIAN, 4);
      c = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      d = new ArcanaIngredient(ArcanaRegistry.STARDUST, 2);
      e = new GenericArcanaIngredient(ArcanaRegistry.WAYSTONE, 1);
      f = new ArcanaIngredient(Items.NETHER_STAR, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.STARPATH_ALTAR, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          STASIS PEARL
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_PEARL, 4);
      b = new ArcanaIngredient(Items.OBSIDIAN, 2);
      c = new ArcanaIngredient(Items.DIAMOND, 1);
      d = new ArcanaIngredient(Items.ENDER_EYE, 4);
      e = new ArcanaIngredient(Items.NETHER_STAR, 1);
      f = new ArcanaIngredient(Items.NETHERITE_SCRAP, 1);
      g = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      h = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, f, g, f, c},
            {b, h, d, h, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.STASIS_PEARL, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //          STELLAR CORE
      // ===================================
      a = new ArcanaIngredient(Items.POLISHED_BLACKSTONE_BRICKS, 6);
      b = new ArcanaIngredient(Items.OBSIDIAN, 4);
      c = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      d = new ArcanaIngredient(Items.BLAZE_POWDER, 8);
      e = new ArcanaIngredient(Items.BLAST_FURNACE, 2);
      f = new ArcanaIngredient(Items.NETHER_STAR, 1);
      g = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, f, g, f, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.STELLAR_CORE, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //          STORMCALLER ALTAR
      // ===================================
      a = new ArcanaIngredient(Items.LIGHTNING_ROD, 4);
      b = new ArcanaIngredient(Items.OXIDIZED_COPPER, 4);
      c = new ArcanaIngredient(Items.DIAMOND, 3);
      d = new ArcanaIngredient(Items.RAW_COPPER_BLOCK, 1);
      e = new ArcanaIngredient(Items.COPPER_BULB, 2);
      f = new ArcanaIngredient(Items.HEART_OF_THE_SEA, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.STORMCALLER_ALTAR, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          STORM ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.LIGHTNING_ROD, 4);
      b = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.CHANNELING, 1));
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, a, ArcanaIngredient.EMPTY},
            {a, c, d, c, a},
            {ArcanaIngredient.EMPTY, a, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.STORM_ARROWS, ingredients, new ForgeRequirement().withEnchanter().withFletchery()));
      
      
      // ===================================
      //          TELESCOPING BEACON
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 2);
      b = new ArcanaIngredient(Items.OBSIDIAN, 2);
      c = new ArcanaIngredient(Items.PISTON, 1);
      d = new ArcanaIngredient(Items.IRON_BLOCK, 1);
      e = new ArcanaIngredient(Items.BEACON, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, d, c, b},
            {c, d, e, d, c},
            {b, c, d, c, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TELESCOPING_BEACON, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          TEMPORAL MOMENT
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 1);
      b = new ArcanaIngredient(Items.ENDER_PEARL, 1);
      c = new ArcanaIngredient(Items.OBSIDIAN, 2);
      d = new ArcanaIngredient(Items.DIAMOND, 1);
      e = new ArcanaIngredient(Items.LAPIS_LAZULI, 4);
      f = new ArcanaIngredient(Items.CLOCK, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TEMPORAL_MOMENT, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          TETHER ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.STRING, 6);
      b = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_LEAPING);
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      e = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.RIPTIDE, 3));
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, a, ArcanaIngredient.EMPTY},
            {a, c, d, c, a},
            {ArcanaIngredient.EMPTY, a, c, e, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TETHER_ARROWS, ingredients, new ForgeRequirement().withEnchanter().withFletchery()));
      
      
      // ===================================
      //          TOTEM OF VENGEANCE
      // ===================================
      r = new SoulstoneIngredient(100, false, false, true, null);
      p = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_TURTLE_MASTER);
      a = ArcanaIngredient.EMPTY;
      b = new ArcanaIngredient(Items.OBSIDIAN, 4);
      t = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_SWIFTNESS);
      v = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_STRENGTH);
      g = new ArcanaIngredient(Items.NETHER_STAR, 1);
      x = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.LONG_FIRE_RESISTANCE);
      k = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      m = new ArcanaIngredient(Items.TOTEM_OF_UNDYING, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, b, b, a},
            {a, g, b, g, a},
            {k, b, m, b, k},
            {p, k, r, k, t},
            {a, v, k, x, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TOTEM_OF_VENGEANCE, ingredients, new ForgeRequirement().withEnchanter()));
      
      
      // ===================================
      //          TRACKING ARROWS
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_EYE, 8);
      b = new ArcanaIngredient(Items.COMPASS, 8);
      c = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      d = new ArcanaIngredient(Items.NETHER_STAR, 1);
      e = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {d, c, e, c, d},
            {ArcanaIngredient.EMPTY, b, c, b, ArcanaIngredient.EMPTY},
            {ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY, a, ArcanaIngredient.EMPTY, ArcanaIngredient.EMPTY}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TRACKING_ARROWS, ingredients, new ForgeRequirement().withEnchanter().withFletchery()));
      
      
      // ===================================
      //          TRANSMUTATION ALTAR
      // ===================================
      a = new ArcanaIngredient(Items.BLACKSTONE, 6);
      b = new ArcanaIngredient(Items.AMETHYST_BLOCK, 4);
      c = new ArcanaIngredient(Items.QUARTZ_BLOCK, 6);
      d = new ArcanaIngredient(Items.AMETHYST_SHARD, 8);
      e = new ArcanaIngredient(Items.DIAMOND, 2);
      f = new ArcanaIngredient(Items.DIAMOND_BLOCK, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, b, c, c},
            {a, d, e, d, c},
            {b, e, f, e, b},
            {c, d, e, d, a},
            {c, c, b, a, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TRANSMUTATION_ALTAR, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          TWILIGHT ANVIL
      // ===================================
      a = new ArcanaIngredient(Items.IRON_BLOCK, 1);
      b = new ArcanaIngredient(Items.DIAMOND, 1);
      c = new ArcanaIngredient(Items.GLOWSTONE_DUST, 8);
      d = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE, 3);
      e = new ArcanaIngredient(Items.ANVIL, 1);
      f = new ArcanaIngredient(Items.NETHERITE_SCRAP, 1);
      g = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, f, g, f, c},
            {b, d, e, d, b},
            {a, b, c, b, a}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TWILIGHT_ANVIL, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          WILD GROWTH CHARM
      // ===================================
      a = new ArcanaIngredient(Items.SWEET_BERRIES, 2);
      b = new ArcanaIngredient(Items.JUNGLE_SAPLING, 2);
      c = new ArcanaIngredient(Items.SEA_PICKLE, 2);
      d = new ArcanaIngredient(Items.ACACIA_SAPLING, 2);
      e = new ArcanaIngredient(Items.CARROT, 2);
      f = new ArcanaIngredient(Items.BIRCH_SAPLING, 2);
      g = new ArcanaIngredient(Items.TORCHFLOWER, 2);
      h = new ArcanaIngredient(Items.BONE_BLOCK, 4);
      i = new ArcanaIngredient(Items.RED_MUSHROOM, 2);
      j = new ArcanaIngredient(Items.CHERRY_SAPLING, 2);
      k = new ArcanaIngredient(Items.VINE, 2);
      l = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      m = new ArcanaIngredient(Items.BAMBOO, 2);
      n = new ArcanaIngredient(Items.SPRUCE_SAPLING, 2);
      o = new ArcanaIngredient(Items.BROWN_MUSHROOM, 2);
      p = new ArcanaIngredient(Items.PITCHER_PLANT, 2);
      q = new ArcanaIngredient(Items.DARK_OAK_SAPLING, 2);
      r = new ArcanaIngredient(Items.POTATO, 2);
      s = new ArcanaIngredient(Items.OAK_SAPLING, 2);
      t = new ArcanaIngredient(Items.SUGAR_CANE, 2);
      u = new ArcanaIngredient(Items.MANGROVE_PROPAGULE, 2);
      v = new ArcanaIngredient(Items.GLOW_BERRIES, 2);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, e},
            {f, g, h, i, j},
            {k, h, l, h, m},
            {n, o, h, p, q},
            {r, s, t, u, v}
      };
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.WILD_GROWTH_CHARM, ingredients, new ForgeRequirement()));
      
      // ===================================
      //           TRANSMOGRIFICATION CATALYST
      // ===================================
      a = new ArcanaIngredient(Items.WHITE_DYE, 2);
      b = new ArcanaIngredient(Items.AMETHYST_SHARD, 2);
      c = new ArcanaIngredient(Items.LIME_DYE, 2);
      d = new ArcanaIngredient(Items.GRAY_DYE, 2);
      e = new ArcanaIngredient(Items.LIGHT_BLUE_DYE, 2);
      f = new ArcanaIngredient(Items.MAGENTA_DYE, 2);
      g = new ArcanaIngredient(Items.CYAN_DYE, 2);
      h = new ArcanaIngredient(Items.RED_DYE, 2);
      i = new ArcanaIngredient(Items.PINK_DYE, 2);
      j = new GenericArcanaIngredient(ArcanaRegistry.CATALYTIC_MATRIX, 1);
      k = new ArcanaIngredient(Items.BROWN_DYE, 2);
      l = new ArcanaIngredient(Items.ORANGE_DYE, 2);
      m = new ArcanaIngredient(Items.GREEN_DYE, 2);
      n = new ArcanaIngredient(Items.PURPLE_DYE, 2);
      o = new ArcanaIngredient(Items.BLUE_DYE, 2);
      p = new ArcanaIngredient(Items.LIGHT_GRAY_DYE, 2);
      q = new ArcanaIngredient(Items.YELLOW_DYE, 2);
      r = new ArcanaIngredient(Items.BLACK_DYE, 2);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, d},
            {b, e, f, g, b},
            {h, i, j, k, l},
            {b, m, n, o, b},
            {p, b, q, b, r}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TRANSMOGRIFICATION_CATALYST, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //        TRANSMUTATION RECIPES
      // ===================================
      List<TransmutationRecipe> transmutationRecipes = new ArrayList<>();
      
      // Commutative Recipes
      transmutationRecipes.add(new CommutativeTransmutationRecipe("stones", new ItemStack(Items.COAL, 4), new ItemStack(Items.QUARTZ, 4))
            .with(Items.COBBLESTONE, Items.STONE, Items.GRANITE, Items.POLISHED_GRANITE, Items.DIORITE, Items.POLISHED_DIORITE, Items.ANDESITE,
                  Items.POLISHED_ANDESITE, Items.DEEPSLATE, Items.COBBLED_DEEPSLATE, Items.POLISHED_DEEPSLATE, Items.DEEPSLATE_TILES,
                  Items.CRACKED_DEEPSLATE_TILES, Items.CALCITE, Items.TUFF, Items.DRIPSTONE_BLOCK, Items.SMOOTH_STONE, Items.STONE_BRICKS,
                  Items.MOSSY_STONE_BRICKS, Items.CRACKED_STONE_BRICKS, Items.CHISELED_STONE_BRICKS, Items.MOSSY_COBBLESTONE, Items.BASALT,
                  Items.POLISHED_BASALT, Items.SMOOTH_BASALT, Items.BLACKSTONE, Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE_BRICKS,
                  Items.CRACKED_POLISHED_BLACKSTONE_BRICKS)
            .with(Either.left(ConventionalItemTags.STONES))
            .with(Either.left(ConventionalItemTags.COBBLESTONES))
            .withViewStack(new ItemStack(Items.COBBLESTONE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("dirts", new ItemStack(Items.EMERALD, 2), new ItemStack(Items.QUARTZ, 4))
            .with(Items.GRASS_BLOCK, Items.DIRT, Items.COARSE_DIRT, Items.ROOTED_DIRT, Items.DIRT_PATH, Items.PODZOL, Items.MYCELIUM)
            .withViewStack(new ItemStack(Items.GRASS_BLOCK)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("sandstones", new ItemStack(Items.REDSTONE, 6), new ItemStack(Items.QUARTZ, 4))
            .with(Items.SANDSTONE, Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE, Items.SMOOTH_SANDSTONE, Items.RED_SAND, Items.CHISELED_RED_SANDSTONE,
                  Items.CUT_RED_SANDSTONE, Items.SMOOTH_RED_SANDSTONE)
            .with(Either.left(ConventionalItemTags.SANDSTONE_BLOCKS))
            .withViewStack(new ItemStack(Items.SANDSTONE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("copper", new ItemStack(Items.REDSTONE, 6), new ItemStack(Items.AMETHYST_SHARD, 4))
            .with(Items.OXIDIZED_COPPER, Items.WEATHERED_COPPER, Items.EXPOSED_COPPER, Items.COPPER_BLOCK,
                  Items.OXIDIZED_CUT_COPPER, Items.WEATHERED_CUT_COPPER, Items.EXPOSED_CUT_COPPER, Items.CUT_COPPER,
                  Items.OXIDIZED_CUT_COPPER_STAIRS, Items.WEATHERED_CUT_COPPER_STAIRS, Items.EXPOSED_CUT_COPPER_STAIRS, Items.CUT_COPPER_STAIRS,
                  Items.OXIDIZED_CHISELED_COPPER, Items.WEATHERED_CHISELED_COPPER, Items.EXPOSED_CHISELED_COPPER, Items.CHISELED_COPPER,
                  Items.OXIDIZED_COPPER_GRATE, Items.WEATHERED_COPPER_GRATE, Items.EXPOSED_COPPER_GRATE, Items.COPPER_GRATE,
                  Items.COPPER_BARS.oxidized(), Items.COPPER_BARS.weathered(), Items.COPPER_BARS.exposed(), Items.COPPER_BARS.unaffected(),
                  Items.COPPER_CHAIN.oxidized(), Items.COPPER_CHAIN.weathered(), Items.COPPER_CHAIN.exposed(), Items.COPPER_CHAIN.unaffected(),
                  Items.COPPER_LANTERN.oxidized(), Items.COPPER_LANTERN.weathered(), Items.COPPER_LANTERN.exposed(), Items.COPPER_LANTERN.unaffected(),
                  Items.OXIDIZED_CUT_COPPER_SLAB, Items.WEATHERED_CUT_COPPER_SLAB, Items.EXPOSED_CUT_COPPER_SLAB, Items.CUT_COPPER_SLAB,
                  Items.OXIDIZED_COPPER_DOOR, Items.WEATHERED_COPPER_DOOR, Items.EXPOSED_COPPER_DOOR, Items.COPPER_DOOR,
                  Items.OXIDIZED_COPPER_TRAPDOOR, Items.WEATHERED_COPPER_TRAPDOOR, Items.EXPOSED_COPPER_TRAPDOOR, Items.COPPER_TRAPDOOR,
                  Items.OXIDIZED_COPPER_BULB, Items.WEATHERED_COPPER_BULB, Items.EXPOSED_COPPER_BULB, Items.COPPER_BULB,
                  Items.OXIDIZED_COPPER_GOLEM_STATUE, Items.WEATHERED_COPPER_GOLEM_STATUE, Items.EXPOSED_COPPER_GOLEM_STATUE, Items.COPPER_GOLEM_STATUE,
                  Items.OXIDIZED_COPPER_CHEST, Items.WEATHERED_COPPER_CHEST, Items.EXPOSED_COPPER_CHEST, Items.COPPER_CHEST)
            .withViewStack(new ItemStack(Items.OXIDIZED_COPPER)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("logs", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Items.ACACIA_LOG, Items.BAMBOO_BLOCK, Items.BIRCH_LOG, Items.CHERRY_LOG, Items.CRIMSON_STEM,
                  Items.DARK_OAK_LOG, Items.JUNGLE_LOG, Items.MANGROVE_LOG, Items.OAK_LOG, Items.SPRUCE_LOG,
                  Items.WARPED_STEM, Items.PALE_OAK_LOG, Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_BAMBOO_BLOCK,
                  Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_CHERRY_LOG, Items.STRIPPED_CRIMSON_STEM,
                  Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_MANGROVE_LOG,
                  Items.STRIPPED_OAK_LOG, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_WARPED_STEM,
                  Items.STRIPPED_PALE_OAK_LOG)
            .with(Either.left(ItemTags.LOGS))
            .with(Either.left(ConventionalItemTags.STRIPPED_LOGS))
            .withViewStack(new ItemStack(Items.OAK_LOG)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("wood", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Items.ACACIA_WOOD, Items.BAMBOO_BLOCK, Items.BIRCH_WOOD, Items.CHERRY_WOOD, Items.CRIMSON_HYPHAE,
                  Items.DARK_OAK_WOOD, Items.JUNGLE_WOOD, Items.MANGROVE_WOOD, Items.OAK_WOOD, Items.SPRUCE_WOOD,
                  Items.WARPED_HYPHAE, Items.PALE_OAK_WOOD, Items.STRIPPED_ACACIA_WOOD, Items.STRIPPED_BAMBOO_BLOCK,
                  Items.STRIPPED_BIRCH_WOOD, Items.STRIPPED_CHERRY_WOOD, Items.STRIPPED_CRIMSON_HYPHAE,
                  Items.STRIPPED_DARK_OAK_WOOD, Items.STRIPPED_JUNGLE_WOOD, Items.STRIPPED_MANGROVE_WOOD,
                  Items.STRIPPED_OAK_WOOD, Items.STRIPPED_SPRUCE_WOOD, Items.STRIPPED_WARPED_HYPHAE,
                  Items.STRIPPED_PALE_OAK_WOOD)
            .withViewStack(new ItemStack(Items.OAK_WOOD)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("planks", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.PLANKS))
            .withViewStack(new ItemStack(Items.OAK_PLANKS)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("stairs", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.WOODEN_STAIRS))
            .withViewStack(new ItemStack(Items.OAK_STAIRS)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("slabs", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.WOODEN_SLABS))
            .withViewStack(new ItemStack(Items.OAK_SLAB)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("fences", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.WOODEN_FENCES))
            .with(Either.left(ConventionalItemTags.WOODEN_FENCES))
            .withViewStack(new ItemStack(Items.OAK_FENCE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("fence_gates", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.FENCE_GATES))
            .with(Either.left(ConventionalItemTags.WOODEN_FENCE_GATES))
            .withViewStack(new ItemStack(Items.OAK_FENCE_GATE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("signs", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.SIGNS))
            .withViewStack(new ItemStack(Items.OAK_SIGN)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("hanging_signs", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.HANGING_SIGNS)).
            withViewStack(new ItemStack(Items.OAK_HANGING_SIGN)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("doors", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.WOODEN_DOORS))
            .withViewStack(new ItemStack(Items.OAK_DOOR)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("trapdoors", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.WOODEN_TRAPDOORS))
            .withViewStack(new ItemStack(Items.OAK_TRAPDOOR)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("pressure_plates", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.WOODEN_PRESSURE_PLATES))
            .withViewStack(new ItemStack(Items.OAK_PRESSURE_PLATE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("buttons", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.BUTTONS))
            .withViewStack(new ItemStack(Items.OAK_BUTTON)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("boats", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.BOATS))
            .withViewStack(new ItemStack(Items.OAK_BOAT)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("chest_boats", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.EMERALD, 2))
            .with(Either.left(ItemTags.CHEST_BOATS))
            .withViewStack(new ItemStack(Items.OAK_CHEST_BOAT)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("seeds", new ItemStack(Items.LAPIS_LAZULI, 5), new ItemStack(Items.EMERALD, 3))
            .with(Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS,
                  Items.PITCHER_POD, Items.POTATO, Items.CARROT, Items.COCOA_BEANS)
            .with(Either.left(ConventionalItemTags.SEEDS))
            .withViewStack(new ItemStack(Items.WHEAT_SEEDS)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("eggs", new ItemStack(Items.LAPIS_LAZULI, 5), new ItemStack(Items.EMERALD, 3))
            .with(Items.EGG, Items.BLUE_EGG, Items.BROWN_EGG, Items.TURTLE_EGG, Items.SNIFFER_EGG)
            .with(Either.left(ConventionalItemTags.EGGS))
            .withViewStack(new ItemStack(Items.EGG)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("leaves", new ItemStack(Items.LAPIS_LAZULI, 5), new ItemStack(Items.EMERALD, 3))
            .with(Either.right(BlockTags.LEAVES))
            .withViewStack(new ItemStack(Items.OAK_LEAVES)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("flowers_and_plants", new ItemStack(Items.GLOWSTONE_DUST, 3), new ItemStack(Items.LAPIS_LAZULI, 7))
            .with(Items.PEONY, Items.ROSE_BUSH, Items.LILAC, Items.SUNFLOWER, Items.AZURE_BLUET, Items.OXEYE_DAISY,
                  Items.CORNFLOWER, Items.WITHER_ROSE, Items.TORCHFLOWER, Items.PITCHER_PLANT, Items.PINK_PETALS,
                  Items.DANDELION, Items.POPPY, Items.BLUE_ORCHID, Items.ALLIUM, Items.RED_TULIP, Items.ORANGE_TULIP,
                  Items.WHITE_TULIP, Items.PINK_TULIP, Items.SPORE_BLOSSOM, Items.FLOWERING_AZALEA, Items.OPEN_EYEBLOSSOM,
                  Items.CLOSED_EYEBLOSSOM, Items.CHORUS_FLOWER, Items.BUSH, Items.FIREFLY_BUSH, Items.DRY_SHORT_GRASS,
                  Items.DRY_TALL_GRASS, Items.CACTUS_FLOWER, Items.WILDFLOWERS, Items.LEAF_LITTER, Items.BIG_DRIPLEAF,
                  Items.SMALL_DRIPLEAF, Items.VINE, Items.FERN, Items.LARGE_FERN, Items.TALL_GRASS, Items.SHORT_GRASS,
                  Items.LILY_PAD, Items.PALE_HANGING_MOSS, Items.GLOW_LICHEN)
            .with(Either.left(ConventionalItemTags.FLOWERS))
            .withViewStack(new ItemStack(Items.POPPY)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("fungi", new ItemStack(Items.GLOWSTONE_DUST, 3), new ItemStack(Items.LAPIS_LAZULI, 7))
            .with(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS, Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM_BLOCK, Items.MUSHROOM_STEM)
            .withViewStack(new ItemStack(Items.RED_MUSHROOM)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("froglights", new ItemStack(Items.REDSTONE, 9), new ItemStack(Items.EMERALD, 5))
            .with(Items.OCHRE_FROGLIGHT, Items.PEARLESCENT_FROGLIGHT, Items.VERDANT_FROGLIGHT)
            .withViewStack(new ItemStack(Items.OCHRE_FROGLIGHT)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("fish", new ItemStack(Items.REDSTONE, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH)
            .withViewStack(new ItemStack(Items.COD)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("coral", new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.LAPIS_LAZULI, 5))
            .with(Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK,
                  Items.HORN_CORAL_BLOCK, Items.TUBE_CORAL, Items.BRAIN_CORAL, Items.BUBBLE_CORAL, Items.FIRE_CORAL,
                  Items.HORN_CORAL, Items.TUBE_CORAL_FAN, Items.BRAIN_CORAL_FAN, Items.BUBBLE_CORAL_FAN,
                  Items.FIRE_CORAL_FAN, Items.HORN_CORAL_FAN, Items.DEAD_TUBE_CORAL_BLOCK, Items.DEAD_BRAIN_CORAL_BLOCK,
                  Items.DEAD_BUBBLE_CORAL_BLOCK, Items.DEAD_FIRE_CORAL_BLOCK, Items.DEAD_HORN_CORAL_BLOCK,
                  Items.DEAD_TUBE_CORAL, Items.DEAD_BRAIN_CORAL, Items.DEAD_BUBBLE_CORAL, Items.DEAD_FIRE_CORAL,
                  Items.DEAD_HORN_CORAL, Items.DEAD_TUBE_CORAL_FAN, Items.DEAD_BRAIN_CORAL_FAN, Items.DEAD_BUBBLE_CORAL_FAN,
                  Items.DEAD_FIRE_CORAL_FAN, Items.DEAD_HORN_CORAL_FAN)
            .withViewStack(new ItemStack(Items.TUBE_CORAL_BLOCK)));
      
      CommutativeTransmutationRecipe banners = new CommutativeTransmutationRecipe("banner_patterns", new ItemStack(Items.REDSTONE, 7), new ItemStack(Items.QUARTZ, 4));
      for(Holder<Item> itemEntry : BuiltInRegistries.ITEM.asHolderIdMap()){
         if(itemEntry.value().getDefaultInstance().has(DataComponents.PROVIDES_BANNER_PATTERNS)){
            banners = banners.with(itemEntry.value());
         }
      }
      transmutationRecipes.add(banners.withViewStack(new ItemStack(Items.CREEPER_BANNER_PATTERN)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("heads", new ItemStack(Items.NETHERITE_SCRAP, 1), new ItemStack(Items.REDSTONE, 16))
            .with(Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.ZOMBIE_HEAD, Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD).withViewStack(new ItemStack(Items.SKELETON_SKULL)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("smithing_templates", new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.LAPIS_LAZULI, 13))
            .with(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE)
            .withViewStack(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("pottery_sherds", new ItemStack(Items.AMETHYST_SHARD, 7), new ItemStack(Items.LAPIS_LAZULI, 5))
            .with(Either.left(ItemTags.DECORATED_POT_SHERDS))
            .withViewStack(new ItemStack(Items.ANGLER_POTTERY_SHERD)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("music_discs", new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.AMETHYST_SHARD, 7))
            .with(Either.left(ConventionalItemTags.MUSIC_DISCS))
            .withViewStack(new ItemStack(Items.MUSIC_DISC_CAT)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("dyes", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.left(ConventionalItemTags.DYES))
            .withViewStack(new ItemStack(Items.WHITE_DYE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("terracotta", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.right(BlockTags.TERRACOTTA))
            .withViewStack(new ItemStack(Items.TERRACOTTA)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("wool", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.left(ItemTags.WOOL))
            .withViewStack(new ItemStack(Items.WHITE_WOOL)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("carpet", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.left(ItemTags.WOOL_CARPETS))
            .withViewStack(new ItemStack(Items.WHITE_CARPET)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("glass", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.left(ConventionalItemTags.GLASS_BLOCKS_CHEAP))
            .withViewStack(new ItemStack(Items.GLASS)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("glass_panes", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.left(ConventionalItemTags.GLASS_PANES))
            .withViewStack(new ItemStack(Items.GLASS_PANE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("candles", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.left(ItemTags.CANDLES)).withViewStack(new ItemStack(Items.CANDLE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("glazed_terracotta", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.left(ConventionalItemTags.GLAZED_TERRACOTTAS))
            .withViewStack(new ItemStack(Items.WHITE_GLAZED_TERRACOTTA)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("concrete", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.left(ConventionalItemTags.CONCRETES))
            .withViewStack(new ItemStack(Items.WHITE_CONCRETE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("gravity_blocks", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.left(ConventionalItemTags.CONCRETE_POWDERS))
            .with(Either.left(ConventionalItemTags.SANDS))
            .with(Either.left(ConventionalItemTags.GRAVELS))
            .withViewStack(new ItemStack(Items.WHITE_CONCRETE_POWDER)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("banners", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.right(BlockTags.BANNERS))
            .withViewStack(new ItemStack(Items.WHITE_BANNER)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("beds", new ItemStack(Items.LAPIS_LAZULI, 3), new ItemStack(Items.GLOWSTONE_DUST, 3))
            .with(Either.right(BlockTags.BEDS))
            .withViewStack(new ItemStack(Items.WHITE_BED)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("rods", new ItemStack(Items.QUARTZ, 11), new ItemStack(Items.REDSTONE, 13))
            .with(Items.BLAZE_ROD, Items.BREEZE_ROD)
            .withViewStack(new ItemStack(Items.BREEZE_ROD)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("coal_ore", new ItemStack(Items.COAL, 4), new ItemStack(Items.QUARTZ, 3))
            .with(Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE)
            .withViewStack(new ItemStack(Items.COAL_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("emerald_ore", new ItemStack(Items.COAL, 4), new ItemStack(Items.QUARTZ, 3))
            .with(Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE)
            .withViewStack(new ItemStack(Items.EMERALD_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("iron_ore", new ItemStack(Items.COAL, 4), new ItemStack(Items.QUARTZ, 3))
            .with(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE)
            .withViewStack(new ItemStack(Items.IRON_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("lapis_ore", new ItemStack(Items.COAL, 4), new ItemStack(Items.QUARTZ, 3))
            .with(Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE)
            .withViewStack(new ItemStack(Items.LAPIS_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("diamond_ore", new ItemStack(Items.COAL, 4), new ItemStack(Items.QUARTZ, 3))
            .with(Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE)
            .withViewStack(new ItemStack(Items.DIAMOND_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("copper_ore", new ItemStack(Items.COAL, 4), new ItemStack(Items.QUARTZ, 3))
            .with(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE)
            .withViewStack(new ItemStack(Items.COPPER_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("gold_ore", new ItemStack(Items.COAL, 4), new ItemStack(Items.QUARTZ, 3))
            .with(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE, Items.NETHER_GOLD_ORE)
            .withViewStack(new ItemStack(Items.GOLD_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("redstone_ore", new ItemStack(Items.COAL, 4), new ItemStack(Items.QUARTZ, 3))
            .with(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE)
            .withViewStack(new ItemStack(Items.REDSTONE_ORE)));
      
      
      // Infusion Recipes
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("cream_reduction", new ItemStack(Items.MAGMA_CREAM, 1), new ItemStack(Items.SLIME_BALL, 1), new ItemStack(Items.CHARCOAL, 4), new ItemStack(Items.SUGAR, 4)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("sand_decomposition", new ItemStack(Items.SANDSTONE, 1), new ItemStack(Items.SAND, 4), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("fiber_decomposition", new ItemStack(Items.WHITE_WOOL, 1), new ItemStack(Items.STRING, 4), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("honeycomb_decomposition", new ItemStack(Items.HONEYCOMB_BLOCK, 1), new ItemStack(Items.HONEYCOMB, 4), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("amethyst_decomposition", new ItemStack(Items.AMETHYST_BLOCK, 1), new ItemStack(Items.AMETHYST_SHARD, 4), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("prismarine_decomposition", new ItemStack(Items.PRISMARINE, 1), new ItemStack(Items.PRISMARINE_SHARD, 4), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("prismarine_brick_decomposition", new ItemStack(Items.PRISMARINE_BRICKS, 1), new ItemStack(Items.PRISMARINE_SHARD, 9), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("quartz_decomposition", new ItemStack(Items.QUARTZ_BLOCK, 1), new ItemStack(Items.QUARTZ, 4), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("nether_brick_decomposition", new ItemStack(Items.NETHER_BRICKS, 1), new ItemStack(Items.NETHER_BRICK, 4), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("glowstone_decomposition", new ItemStack(Items.GLOWSTONE, 1), new ItemStack(Items.GLOWSTONE_DUST, 4), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("magma_decomposition", new ItemStack(Items.MAGMA_BLOCK, 1), new ItemStack(Items.MAGMA_CREAM, 4), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("nether_wart_decomposition", new ItemStack(Items.NETHER_WART_BLOCK, 1), new ItemStack(Items.NETHER_WART, 9), new ItemStack(Items.GUNPOWDER, 3), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("blaze_rod_decomposition", new ItemStack(Items.BLAZE_ROD, 1), new ItemStack(Items.BLAZE_POWDER, 6), new ItemStack(Items.GUNPOWDER, 15), new ItemStack(ArcanaRegistry.STARDUST, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("breeze_rod_decomposition", new ItemStack(Items.BREEZE_ROD, 1), new ItemStack(Items.WIND_CHARGE, 9), new ItemStack(Items.GUNPOWDER, 15), new ItemStack(ArcanaRegistry.STARDUST, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("blackstone_infusion", new ItemStack(Items.BLACKSTONE, 1), new ItemStack(Items.GILDED_BLACKSTONE, 1), new ItemStack(Items.GLOWSTONE_DUST, 13), new ItemStack(Items.GOLD_INGOT, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("nether_brick_infusion", new ItemStack(Items.NETHER_BRICKS, 1), new ItemStack(Items.RED_NETHER_BRICKS, 1), new ItemStack(Items.NETHER_WART, 32), new ItemStack(Items.GLOWSTONE_DUST, 12)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("obsidian_infusion", new ItemStack(Items.MAGMA_BLOCK, 4), new ItemStack(Items.OBSIDIAN, 1), new ItemStack(Items.ICE, 12), new ItemStack(Items.REDSTONE, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("crying_obsidian_infusion", new ItemStack(Items.OBSIDIAN, 1), new ItemStack(Items.CRYING_OBSIDIAN, 1), new ItemStack(Items.REDSTONE, 16), new ItemStack(Items.GLOWSTONE_DUST, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("glowstone_infusion", new ItemStack(Items.REDSTONE, 4), new ItemStack(Items.GLOWSTONE_DUST, 1), new ItemStack(Items.REDSTONE, 16), new ItemStack(Items.QUARTZ, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("ender_infusion", new ItemStack(Items.ENDER_PEARL, 1), new ItemStack(Items.ENDER_EYE, 2), new ItemStack(Items.GLOWSTONE_DUST, 12), new ItemStack(Items.BLAZE_POWDER, 18)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("chorus_transmogrification", new ItemStack(Items.APPLE, 1), new ItemStack(Items.CHORUS_FLOWER, 1), new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 12), new ItemStack(Items.ENDER_PEARL, 6)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("endstone_transmogrification", new ItemStack(Items.COBBLED_DEEPSLATE, 1), new ItemStack(Items.END_STONE, 1), new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 6), new ItemStack(Items.ENDER_PEARL, 2)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("refined_flesh", new ItemStack(Items.ROTTEN_FLESH, 4), new ItemStack(Items.LEATHER, 1), new ItemStack(Items.REDSTONE, 8), new ItemStack(Items.SUGAR, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("carbon_compression", new ItemStack(Items.COAL, 12), new ItemStack(Items.DIAMOND, 1), new ItemStack(Items.REDSTONE, 16), new ItemStack(Items.GUNPOWDER, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("refined_netherite", new ItemStack(Items.ANCIENT_DEBRIS, 3), new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.GOLD_INGOT, 16), new ItemStack(Items.GLOWSTONE_DUST, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("condensed_string", new ItemStack(Items.STRING, 16), new ItemStack(Items.COBWEB, 1), new ItemStack(Items.SLIME_BLOCK, 4), new ItemStack(Items.HONEY_BLOCK, 4)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("key_ominousification", new ItemStack(Items.TRIAL_KEY, 1), new ItemStack(Items.OMINOUS_TRIAL_KEY, 1), new ItemStack(Items.OMINOUS_BOTTLE, 1), new ItemStack(Items.DIAMOND, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("sand_susification", new ItemStack(Items.SAND, 1), new ItemStack(Items.SUSPICIOUS_SAND, 1), new ItemStack(Items.SUSPICIOUS_STEW, 1), new ItemStack(ArcanaRegistry.STARDUST, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("gravel_susification", new ItemStack(Items.GRAVEL, 1), new ItemStack(Items.SUSPICIOUS_GRAVEL, 1), new ItemStack(Items.SUSPICIOUS_STEW, 1), new ItemStack(ArcanaRegistry.STARDUST, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("liquid_experience", new ItemStack(Items.SCULK, 32), new ItemStack(Items.EXPERIENCE_BOTTLE, 16), new ItemStack(Items.GLASS_BOTTLE, 16), new ItemStack(Items.LAPIS_LAZULI, 32)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("sculk_infusion", new ItemStack(Items.SCULK_VEIN, 4), new ItemStack(Items.SCULK, 1), new ItemStack(Items.GUNPOWDER, 16), new ItemStack(Items.DIAMOND, 4)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("sculk_shard_infusion", new ItemStack(Items.SCULK, 4), new ItemStack(Items.ECHO_SHARD, 1), new ItemStack(Items.AMETHYST_SHARD, 15), new ItemStack(Items.DIAMOND, 12)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("lodestone_refinement", new ItemStack(Items.LODESTONE, 1), ArcanaRegistry.WAYSTONE.getPrefItemNoLore(), new ItemStack(Items.AMETHYST_SHARD, 7), new ItemStack(Items.REDSTONE, 15)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("divine_paper_infusion", new ItemStack(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER, 2), new ItemStack(ArcanaRegistry.DIVINE_ARCANE_PAPER, 1), ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(), new ItemStack(Items.DIAMOND, 12)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("nautilus_synthesis", new ItemStack(Items.TURTLE_SCUTE, 4), new ItemStack(Items.NAUTILUS_SHELL, 1), new ItemStack(Items.PRISMARINE_CRYSTALS, 16), new ItemStack(Items.QUARTZ, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("oceanic_heart_synthesis", new ItemStack(Items.NAUTILUS_SHELL, 8), new ItemStack(Items.HEART_OF_THE_SEA, 1), new ItemStack(Items.PRISMARINE_CRYSTALS, 16), new ItemStack(Items.QUARTZ, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("trident_synthesis", new ItemStack(Items.DIAMOND_SWORD, 1), new ItemStack(Items.TRIDENT, 1), new ItemStack(Items.PRISMARINE_CRYSTALS, 16), new ItemStack(Items.HEART_OF_THE_SEA, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("resonator_synthesis", new ItemStack(Items.GOLD_BLOCK, 1), new ItemStack(Items.BELL, 1), new ItemStack(Items.QUARTZ, 16), new ItemStack(Items.AMETHYST_SHARD, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("gravity_core_synthesis", new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.HEAVY_CORE, 1), new ItemStack(Items.IRON_BLOCK, 16), new ItemStack(Items.POLISHED_TUFF, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("enchanted_golden_apple_synthesis", new ItemStack(Items.TOTEM_OF_UNDYING, 1), new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1), new ItemStack(Items.GOLD_BLOCK, 4), new ItemStack(Items.GOLDEN_APPLE, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("divine_catalyst_synthesis", ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(), ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(), ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(), ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore()));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("aequalis_scientia_synthesis", ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(), ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore(), new ItemStack(Items.AMETHYST_BLOCK, 32), new ItemStack(Items.DIAMOND_BLOCK, 1)));
      
      
      // Save Recipes
      Path dirPath = FabricLoader.getInstance().getConfigDir().resolve("arcananovum").resolve("recipes").resolve("default");
      Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
      
      for(ArcanaRecipe recipe : arcanaRecipes){
         File newFile = dirPath.resolve(recipe.getOutputId().getPath() + "_forging.json").toFile();
         newFile.getParentFile().mkdirs();
         
         try(BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)))){
            JsonObject json = recipe.toJson();
            gson.toJson(json, output);
            //ArcanaNovum.log.info("Saved Forging Recipe for {} to {}",recipe.getOutputId().toString(),newFile.getAbsolutePath());
         }catch(IOException err){
            ArcanaNovum.log(2, "Failed to save " + recipe.getOutputId().toString() + " forging recipe file!");
            ArcanaNovum.log(2, err.toString());
         }
      }
      
      for(TransmutationRecipe recipe : transmutationRecipes){
         JsonObject json = null;
         if(recipe instanceof CommutativeTransmutationRecipe comm) json = comm.toJson();
         if(recipe instanceof InfusionTransmutationRecipe infu) json = infu.toJson();
         if(json == null) continue;
         File newFile = dirPath.resolve(recipe.getId() + "_transmutation.json").toFile();
         newFile.getParentFile().mkdirs();
         
         try(BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)))){
            gson.toJson(json, output);
            //ArcanaNovum.log.info("Saved Recipe for {} to {}",recipe.getId(),newFile.getAbsolutePath());
         }catch(IOException err){
            ArcanaNovum.log(2, "Failed to save " + recipe.getId() + " transmutation recipe file!");
            ArcanaNovum.log(2, err.toString());
         }
      }
   }
   
   public static void generateClassicRecipes(){
      List<ArcanaRecipe> arcanaRecipes = new ArrayList<>();
      ArcanaIngredient a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;
      ArcanaIngredient[][] ingredients;
      
      // ===================================
      //         CHARM OF NEGOTIATION
      // ===================================
      a = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE, 4);
      b = new ArcanaIngredient(Items.GOLD_NUGGET, 64);
      c = new ArcanaIngredient(Items.GOLD_INGOT, 32);
      d = new ArcanaIngredient(Items.GOLD_BLOCK, 4);
      e = new ArcanaIngredient(Items.EMERALD, 16);
      f = new ArcanaIngredient(Items.EMERALD_BLOCK, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.NEGOTIATION_CHARM, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //           CLOCKWORK MULTITOOL
      // ===================================
      a = new ArcanaIngredient(Items.CRAFTER, 4);
      b = new ArcanaIngredient(Items.GOLD_INGOT, 16);
      c = new ArcanaIngredient(Items.IRON_CHAIN, 16);
      d = new ArcanaIngredient(Items.CLOCK, 16);
      e = new ArcanaIngredient(Items.SMITHING_TABLE, 4);
      f = new ArcanaIngredient(Items.GRINDSTONE, 4);
      g = new ArcanaIngredient(Items.CARTOGRAPHY_TABLE, 4);
      h = new ArcanaIngredient(Items.REDSTONE_BLOCK, 8);
      i = new ArcanaIngredient(Items.CRAFTING_TABLE, 4);
      j = new ArcanaIngredient(Items.STONECUTTER, 4);
      k = new ArcanaIngredient(Items.LOOM, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, f, b},
            {c, g, h, i, c},
            {b, j, k, d, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CLOCKWORK_MULTITOOL, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //           ITINERANTEUR
      // ===================================
      a = new ArcanaIngredient(Items.IRON_NUGGET, 64);
      b = new ArcanaIngredient(Items.IRON_INGOT, 8);
      c = new ArcanaIngredient(Items.IRON_CHAIN, 32);
      d = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.LONG_SWIFTNESS);
      e = new ArcanaIngredient(Items.BLAZE_ROD, 16);
      f = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_SWIFTNESS);
      g = new ArcanaIngredient(Items.GLASS, 16);
      h = new ArcanaIngredient(Items.BLAZE_POWDER, 16);
      i = new ArcanaIngredient(ItemTags.LANTERNS, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, f, b},
            {g, h, i, h, g},
            {b, f, e, d, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ITINERANTEUR, ingredients, new ForgeRequirement().withEnchanter()).addCenterpiece(12));
      
      
      // ===================================
      //           GEOMANTIC STELE
      // ===================================
      a = new ArcanaIngredient(Items.OBSIDIAN, 32);
      b = new ArcanaIngredient(Items.CHISELED_TUFF, 64);
      c = new ArcanaIngredient(Items.CHISELED_STONE_BRICKS, 32);
      d = new ArcanaIngredient(Items.DEEPSLATE_TILES, 32);
      e = new ArcanaIngredient(Items.NETHER_STAR, 4);
      f = new ArcanaIngredient(Items.AMETHYST_BLOCK, 32);
      g = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      h = new ArcanaIngredient(Items.NETHERITE_INGOT, 4);
      i = new ArcanaIngredient(Items.CHISELED_TUFF_BRICKS, 64);
      j = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 64);
      k = new ArcanaIngredient(Items.QUARTZ, 48);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {f, g, h, g, f},
            {a, d, e, d, a},
            {i, j, k, j, i}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.GEOMANTIC_STELE, ingredients, new ForgeRequirement().withAnvil().withCore()));
      
      
      // ===================================
      //           INTERDICTOR
      // ===================================
      a = new ArcanaIngredient(Items.PURPUR_PILLAR, 16);
      b = new ArcanaIngredient(Items.END_STONE_BRICKS, 12);
      c = new ArcanaIngredient(Items.ENDER_EYE, 12);
      d = new ArcanaIngredient(Items.ENDER_PEARL, 8);
      e = new ArcanaIngredient(Items.NETHER_STAR, 2);
      f = new ArcanaIngredient(Items.END_CRYSTAL, 12);
      g = new ArcanaIngredient(Items.BEACON, 4);
      h = new ArcanaIngredient(Items.OBSIDIAN, 24);
      i = new GenericArcanaIngredient(ArcanaRegistry.EXOTIC_MATTER, 1);
      j = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 24);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, f, g, f, c},
            {b, h, i, h, b},
            {a, j, j, j, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.INTERDICTOR, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //           ASTRAL GATEWAY
      // ===================================
      a = new ArcanaIngredient(Items.END_STONE_BRICKS, 16);
      b = new ArcanaIngredient(ArcanaRegistry.STARDUST, 16);
      c = new ArcanaIngredient(Items.ENDER_EYE, 16);
      d = new ArcanaIngredient(Items.OBSIDIAN, 8);
      e = new ArcanaIngredient(Items.NETHER_STAR, 2);
      f = new ArcanaIngredient(Items.ENDER_PEARL, 16);
      g = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 16);
      h = new GenericArcanaIngredient(ArcanaRegistry.EXOTIC_MATTER, 1);
      i = new ArcanaIngredient(Items.IRON_INGOT, 16);
      j = new ArcanaIngredient(Items.NETHERITE_INGOT, 2);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {f, g, h, g, f},
            {i, d, j, d, i},
            {a, i, c, i, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ASTRAL_GATEWAY, ingredients, new ForgeRequirement().withAnvil().withCore()));
      
      
      // ===================================
      //           ENDER CRATE
      // ===================================
      a = new ArcanaIngredient(ItemTags.LOGS, 16);
      b = new ArcanaIngredient(Items.OBSIDIAN, 8);
      c = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 8);
      d = new ArcanaIngredient(Items.ENDER_EYE, 8);
      e = new ArcanaIngredient(Items.BARREL, 12);
      f = new ArcanaIngredient(Items.ENDER_CHEST, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, d, e, d, b},
            {c, e, f, e, c},
            {b, d, e, d, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ENDER_CRATE, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          ALCHEMICAL ARBALEST
      // ===================================
      a = new ArcanaIngredient(Items.GLOWSTONE_DUST, 32);
      b = new ArcanaIngredient(Items.NETHER_WART, 16);
      c = new ArcanaIngredient(Items.DRAGON_BREATH, 32);
      d = new ArcanaIngredient(Items.BLAZE_POWDER, 24);
      g = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.MULTISHOT, 1));
      h = new ArcanaIngredient(Items.NETHERITE_INGOT, 2);
      k = new ArcanaIngredient(Items.FERMENTED_SPIDER_EYE, 32);
      l = new ArcanaIngredient(Items.NETHER_STAR, 4);
      m = new ArcanaIngredient(Items.CROSSBOW, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {b, g, h, g, d},
            {k, l, m, l, k},
            {d, g, h, g, b},
            {a, d, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ALCHEMICAL_ARBALEST, ingredients, new ForgeRequirement().withAnvil().withCore().withFletchery().withEnchanter()).addCenterpiece(12));
      
      
      // ===================================
      //       ANCIENT DOWSING ROD
      // ===================================
      a = new ArcanaIngredient(Items.GOLD_INGOT, 16);
      b = new ArcanaIngredient(Items.RED_NETHER_BRICKS, 12);
      c = new ArcanaIngredient(Items.FIRE_CHARGE, 16);
      d = new ArcanaIngredient(Items.BLAZE_ROD, 8);
      g = new ArcanaIngredient(Items.NETHERITE_SCRAP, 1);
      h = new ArcanaIngredient(Items.ANCIENT_DEBRIS, 1);
      l = new ArcanaIngredient(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1);
      m = new ArcanaIngredient(Items.BELL, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, b},
            {b, g, h, g, d},
            {h, l, m, l, h},
            {d, g, h, g, b},
            {b, d, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ANCIENT_DOWSING_ROD, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //        AQUATIC EVERSOURCE
      // ===================================
      a = new ArcanaIngredient(Items.BUCKET, 8);
      b = new ArcanaIngredient(Items.GOLD_INGOT, 6);
      c = new ArcanaIngredient(Items.BLUE_ICE, 4);
      h = new ArcanaIngredient(Items.DIAMOND, 2);
      m = new ArcanaIngredient(Items.HEART_OF_THE_SEA, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, h, c, b},
            {c, h, m, h, c},
            {b, c, h, c, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.AQUATIC_EVERSOURCE, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //         ARCANIST'S BELT
      // ===================================
      a = new ArcanaIngredient(Items.NETHER_STAR, 1);
      b = new ArcanaIngredient(Items.LEATHER, 12);
      c = new ArcanaIngredient(Items.NETHERITE_SCRAP, 2);
      g = new ArcanaIngredient(Items.GOLD_INGOT, 24);
      h = new ArcanaIngredient(Items.CHEST, 16);
      m = new ArcanaIngredient(Items.ENDER_CHEST, 8);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ARCANISTS_BELT, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //          BINARY BLADES
      // ===================================
      a = new ArcanaIngredient(Items.GLOWSTONE_DUST, 32);
      b = new ArcanaIngredient(ArcanaRegistry.STARDUST, 32);
      c = new ArcanaIngredient(Items.DIAMOND, 8);
      e = new ArcanaIngredient(Items.NETHER_STAR, 4);
      g = new ArcanaIngredient(Items.NETHERITE_SWORD, 1, true);
      h = new ArcanaIngredient(Items.BLAZE_POWDER, 48);
      m = new ArcanaIngredient(Items.NETHERITE_INGOT, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, e},
            {b, g, h, a, b},
            {c, h, m, h, c},
            {b, a, h, g, b},
            {e, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.BINARY_BLADES, ingredients, new ForgeRequirement().withAnvil().withCore()).addCenterpiece(6).addCenterpiece(18));
      
      
      // ===================================
      //            BRAIN JAR
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_CHEST, 4);
      b = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE, 8);
      c = new ArcanaIngredient(Items.SCULK, 16);
      g = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.MENDING, 1));
      h = new ArcanaIngredient(Items.SCULK_CATALYST, 8);
      m = new ArcanaIngredient(Items.ZOMBIE_HEAD, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.BRAIN_JAR, ingredients, new ForgeRequirement().withAnvil().withEnchanter()));
      
      
      // ===================================
      //        CHEST TRANSLOCATOR
      // ===================================
      a = new ArcanaIngredient(Items.CHEST, 12);
      b = new ArcanaIngredient(Items.OBSIDIAN, 2);
      c = new ArcanaIngredient(Items.BARREL, 12);
      g = new ArcanaIngredient(Items.ENDER_EYE, 4);
      h = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_STRENGTH);
      m = new ArcanaIngredient(Items.ENDER_CHEST, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CHEST_TRANSLOCATOR, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //       CONTAINMENT CIRCLET
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      b = new ArcanaIngredient(Items.OBSIDIAN, 4);
      c = new ArcanaIngredient(Items.IRON_BARS, 16);
      g = new ArcanaIngredient(Items.IRON_CHAIN, 12);
      h = new ArcanaIngredient(Items.COBWEB, 8);
      m = new ArcanaIngredient(Items.ENDER_CHEST, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CONTAINMENT_CIRCLET, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          ESSENCE EGG
      // ===================================
      t = new SoulstoneIngredient(Soulstone.tiers[0], true, false, false, null);
      a = new ArcanaIngredient(Items.OBSIDIAN, 16);
      b = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 16);
      c = new ArcanaIngredient(Items.IRON_BARS, 16);
      h = new ArcanaIngredient(Items.SOUL_SAND, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, h, c, b},
            {c, h, t, h, c},
            {b, c, h, c, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ESSENCE_EGG, ingredients, new ForgeRequirement()).addCenterpiece(12));
      
      
      // ===================================
      //       EVERLASTING ROCKET
      // ===================================
      a = new ArcanaIngredient(Items.FIREWORK_ROCKET, 16);
      b = new ArcanaIngredient(Items.GUNPOWDER, 8);
      c = new ArcanaIngredient(Items.PAPER, 24);
      g = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.MENDING, 1));
      h = new ArcanaIngredient(Items.FIREWORK_STAR, 8);
      i = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.UNBREAKING, 3));
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, i, b},
            {c, h, a, h, c},
            {b, i, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.EVERLASTING_ROCKET, ingredients, new ForgeRequirement().withEnchanter()).addCenterpiece(12));
      
      
      // ===================================
      //         EXOTIC MATTER
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 8);
      b = new ArcanaIngredient(Items.OBSIDIAN, 8);
      c = new ArcanaIngredient(Items.DIAMOND, 2);
      d = new ArcanaIngredient(Items.END_CRYSTAL, 2);
      m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {d, a, d, a, b},
            {c, d, m, d, c},
            {b, a, d, a, d},
            {a, d, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.EXOTIC_MATTER, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //         GRAVITON MAUL
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 32);
      b = new ArcanaIngredient(Items.OBSIDIAN, 32);
      k = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.BREACH, 4));
      g = new ArcanaIngredient(Items.COBWEB, 32);
      h = new ArcanaIngredient(Items.NETHERITE_INGOT, 3);
      c = new ArcanaIngredient(Items.BREEZE_ROD, 32);
      l = new ArcanaIngredient(Items.NETHER_STAR, 3);
      m = new ArcanaIngredient(Items.MACE, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {k, l, m, l, k},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.GRAVITON_MAUL, ingredients, new ForgeRequirement().withAnvil().withCore().withEnchanter()).addCenterpiece(12));
      
      
      // ===================================
      //       LEVITATION HARNESS
      // ===================================
      m = new ShulkerCoreIngredient(true, 500);
      a = new ArcanaIngredient(Items.GLOWSTONE, 32);
      b = new ArcanaIngredient(Items.SHULKER_SHELL, 24);
      c = new ArcanaIngredient(Items.ELYTRA, 1);
      g = new ArcanaIngredient(Items.NETHER_STAR, 4);
      h = new ArcanaIngredient(Items.NETHERITE_INGOT, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.LEVITATION_HARNESS, ingredients, new ForgeRequirement().withEnchanter().withCore().withAnvil().withSingularity()).addCenterpiece(12));
      
      
      // ===================================
      //       MAGMATIC EVERSOURCE
      // ===================================
      a = new ArcanaIngredient(Items.BUCKET, 16);
      b = new ArcanaIngredient(Items.NETHERITE_SCRAP, 1);
      c = new ArcanaIngredient(Items.BLAZE_POWDER, 32);
      g = new ArcanaIngredient(Items.BLAZE_ROD, 16);
      h = new ArcanaIngredient(Items.MAGMA_BLOCK, 24);
      m = new ArcanaIngredient(Items.MAGMA_CREAM, 48);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.MAGMATIC_EVERSOURCE, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //       OVERFLOWING QUIVER
      // ===================================
      a = new ArcanaIngredient(Items.NETHER_STAR, 1);
      b = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.INFINITY, 1));
      c = new ArcanaIngredient(Items.RABBIT_HIDE, 12);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 32);
      m = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, h, c, b},
            {c, h, m, h, c},
            {b, c, h, c, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.OVERFLOWING_QUIVER, ingredients, new ForgeRequirement().withAnvil().withFletchery().withCore().withEnchanter()));
      
      
      // ===================================
      //        PEARL OF RECALL
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_PEARL, 8);
      b = new ArcanaIngredient(Items.GOLD_INGOT, 8);
      c = new ArcanaIngredient(Items.CLOCK, 8);
      g = new ArcanaIngredient(Items.ENDER_EYE, 4);
      h = new WaystoneIngredient(true);
      l = new ArcanaIngredient(Items.NETHER_STAR, 1);
      m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, l, m, l, c},
            {b, g, l, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.PEARL_OF_RECALL, ingredients, new ForgeRequirement()).addCenterpiece(7));
      
      
      // ===================================
      //          PLANESHIFTER
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 16);
      b = new ArcanaIngredient(Items.OBSIDIAN, 12);
      c = new ArcanaIngredient(Items.ENDER_EYE, 8);
      g = new ArcanaIngredient(Items.NETHER_STAR, 1);
      h = new ArcanaIngredient(Items.END_CRYSTAL, 8);
      m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.PLANESHIFTER, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //           RUNIC BOW
      // ===================================
      a = new ArcanaIngredient(Items.NETHER_STAR, 2);
      b = new ArcanaIngredient(Items.AMETHYST_SHARD, 32);
      c = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.POWER, 5));
      d = new ArcanaIngredient(Items.END_CRYSTAL, 24);
      g = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      h = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      m = new ArcanaIngredient(Items.BOW, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {b, g, h, g, d},
            {c, h, m, h, c},
            {d, g, h, g, b},
            {a, d, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.RUNIC_BOW, ingredients, new ForgeRequirement().withAnvil().withFletchery().withEnchanter().withCore()).addCenterpiece(12));
      
      
      // ===================================
      //          RUNIC MATRIX
      // ===================================
      a = new ArcanaIngredient(Items.AMETHYST_SHARD, 12);
      b = new ArcanaIngredient(Items.DIAMOND, 2);
      c = new ArcanaIngredient(Items.END_CRYSTAL, 2);
      g = new ArcanaIngredient(Items.CRAFTER, 8);
      m = new ArcanaIngredient(Items.NETHER_STAR, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, a, g, b},
            {c, a, m, a, c},
            {b, g, a, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.RUNIC_MATRIX, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //          RUNIC QUIVER
      // ===================================
      a = new ArcanaIngredient(Items.LEATHER, 32);
      b = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.INFINITY, 1));
      c = new ArcanaIngredient(Items.NETHER_STAR, 2);
      g = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      h = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      m = new GenericArcanaIngredient(ArcanaRegistry.OVERFLOWING_QUIVER, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.RUNIC_QUIVER, ingredients, new ForgeRequirement().withCore().withFletchery().withEnchanter().withAnvil()).addCenterpiece(12));
      
      
      // ===================================
      //     SHADOW STALKER'S GLAIVE
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_EYE, 12);
      b = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 16);
      c = new ArcanaIngredient(Items.RED_NETHER_BRICKS, 32);
      g = new ArcanaIngredient(Items.OBSIDIAN, 24);
      h = new ArcanaIngredient(Items.NETHERITE_INGOT, 3);
      l = new ArcanaIngredient(Items.NETHER_STAR, 3);
      m = new ArcanaIngredient(Items.NETHERITE_SWORD, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, l, m, l, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SHADOW_STALKERS_GLAIVE, ingredients, new ForgeRequirement().withAnvil().withCore()).addCenterpiece(12));
      
      
      // ===================================
      //      SHIELD OF FORTITUDE
      // ===================================
      a = new ArcanaIngredient(Items.NETHER_STAR, 2);
      b = new ArcanaIngredient(Items.OBSIDIAN, 32);
      r = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.FIRE_PROTECTION, 4));
      c = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      g = new ArcanaIngredient(Items.GOLDEN_APPLE, 16);
      h = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.BLAST_PROTECTION, 4));
      l = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.PROTECTION, 4));
      m = new ArcanaIngredient(Items.SHIELD, 1, true);
      n = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.PROJECTILE_PROTECTION, 4));
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, l, m, n, c},
            {b, g, r, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SHIELD_OF_FORTITUDE, ingredients, new ForgeRequirement().withAnvil().withCore().withEnchanter()).addCenterpiece(12));
      
      
      // ===================================
      //          SHULKER CORE
      // ===================================
      t = new SoulstoneIngredient(Soulstone.tiers[4], false, true, false, EntityType.getKey(EntityType.SHULKER).toString());
      a = new ArcanaIngredient(Items.PHANTOM_MEMBRANE, 16);
      b = new ArcanaIngredient(Items.SHULKER_SHELL, 8);
      c = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.LONG_SLOW_FALLING);
      g = new ArcanaIngredient(Items.GLOWSTONE_DUST, 32);
      h = new ArcanaIngredient(Items.NETHER_STAR, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, t, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SHULKER_CORE, ingredients, new ForgeRequirement().withCore().withAnvil()).addCenterpiece(12));
      
      
      // ===================================
      //        SOJOURNER BOOTS
      // ===================================
      a = new ArcanaIngredient(Items.OBSIDIAN, 32);
      b = new ArcanaIngredient(Items.TUFF, 16);
      c = new ArcanaIngredient(Items.NETHER_STAR, 2);
      d = new ArcanaIngredient(Items.RED_SAND, 16);
      f = new ArcanaIngredient(Items.ROOTED_DIRT, 16);
      g = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      h = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_SWIFTNESS);
      j = new ArcanaIngredient(Items.PACKED_MUD, 16);
      l = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_LEAPING);
      m = new ArcanaIngredient(Items.NETHERITE_BOOTS, 1, true);
      p = new ArcanaIngredient(Items.GRASS_BLOCK, 16);
      t = new ArcanaIngredient(Items.GRAVEL, 16);
      v = new ArcanaIngredient(Items.SAND, 16);
      x = new ArcanaIngredient(Items.TERRACOTTA, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {f, g, h, g, j},
            {c, l, m, l, c},
            {p, g, h, g, t},
            {a, v, c, x, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SOJOURNER_BOOTS, ingredients, new ForgeRequirement().withCore().withAnvil()).addCenterpiece(12));
      
      
      // ===================================
      //           SOULSTONE
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 16);
      b = new ArcanaIngredient(Items.OBSIDIAN, 16);
      c = new ArcanaIngredient(Items.RED_NETHER_BRICKS, 16);
      h = new ArcanaIngredient(Items.SOUL_SAND, 32);
      m = new ArcanaIngredient(Items.NETHERITE_BLOCK, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, h, c, b},
            {c, h, m, h, c},
            {b, c, h, c, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SOULSTONE, ingredients, new ForgeRequirement().withCore().withAnvil()));
      
      
      // ===================================
      //        SPAWNER HARNESS
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 16);
      b = new ArcanaIngredient(Items.OBSIDIAN, 16);
      c = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.SILK_TOUCH, 1));
      g = new ArcanaIngredient(Items.ENDER_EYE, 4);
      h = new ArcanaIngredient(Items.IRON_BARS, 16);
      m = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SPAWNER_HARNESS, ingredients, new ForgeRequirement().withAnvil().withEnchanter().withCore()));
      
      
      // ===================================
      //         STASIS PEARL
      // ===================================
      a = new ArcanaIngredient(Items.ENDER_EYE, 4);
      b = new ArcanaIngredient(Items.OBSIDIAN, 12);
      c = new ArcanaIngredient(Items.NETHERITE_SCRAP, 1);
      g = new ArcanaIngredient(Items.NETHER_STAR, 1);
      h = new ArcanaIngredient(Items.ENDER_PEARL, 8);
      m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.STASIS_PEARL, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //      TELESCOPING BEACON
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 8);
      b = new ArcanaIngredient(Items.OBSIDIAN, 8);
      c = new ArcanaIngredient(Items.PISTON, 16);
      h = new ArcanaIngredient(Items.IRON_BLOCK, 6);
      m = new ArcanaIngredient(Items.BEACON, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, h, c, b},
            {c, h, m, h, c},
            {b, c, h, c, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TELESCOPING_BEACON, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //        TEMPORAL MOMENT
      // ===================================
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 4);
      b = new ArcanaIngredient(Items.ENDER_PEARL, 2);
      c = new ArcanaIngredient(Items.OBSIDIAN, 4);
      g = new ArcanaIngredient(Items.DIAMOND, 2);
      h = new ArcanaIngredient(Items.LAPIS_LAZULI, 16);
      m = new ArcanaIngredient(Items.CLOCK, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TEMPORAL_MOMENT, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //       TOTEM OF VENGEANCE
      // ===================================
      r = new SoulstoneIngredient(100, false, false, true, null);
      p = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_TURTLE_MASTER);
      a = ArcanaIngredient.EMPTY;
      b = new ArcanaIngredient(Items.OBSIDIAN, 12);
      t = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_SWIFTNESS);
      v = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_STRENGTH);
      g = new ArcanaIngredient(Items.NETHER_STAR, 1);
      x = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.LONG_FIRE_RESISTANCE);
      k = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 8);
      m = new ArcanaIngredient(Items.TOTEM_OF_UNDYING, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, b, b, a},
            {a, g, b, g, a},
            {k, b, m, b, k},
            {p, k, r, k, t},
            {a, v, k, x, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TOTEM_OF_VENGEANCE, ingredients, new ForgeRequirement().withEnchanter()));
      
      
      // ===================================
      //          RUNIC ARROWS
      // ===================================
      
      // --- ARCANE FLAK ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.DRAGON_BREATH, 16);
      g = new ArcanaIngredient(Items.FIREWORK_STAR, 12);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      k = new ArcanaIngredient(Items.GLOWSTONE_DUST, 32);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {k, h, m, h, k},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ARCANE_FLAK_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // --- BLINK ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.ENDER_PEARL, 8);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, c, h, c, a},
            {c, h, m, h, c},
            {a, c, h, c, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.BLINK_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // --- CONCUSSION ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.LINGERING_POTION, 1).withPotion(Potions.STRONG_SLOWNESS);
      g = new ArcanaIngredient(Items.GLOW_INK_SAC, 16);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      i = new ArcanaIngredient(Items.INK_SAC, 16);
      k = new ArcanaIngredient(Items.LINGERING_POTION, 1).withPotion(Potions.LONG_WEAKNESS);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, i, a},
            {k, h, m, h, k},
            {a, i, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CONCUSSION_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // --- DETONATION ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.TNT, 8);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, c, h, c, a},
            {c, h, m, h, c},
            {a, c, h, c, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.DETONATION_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // --- ENSNAREMENT ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 24);
      g = new ArcanaIngredient(Items.COBWEB, 16);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      k = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_SLOWNESS);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {k, h, m, h, k},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ENSNAREMENT_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // --- EXPULSION ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.AMETHYST_SHARD, 32);
      g = new ArcanaIngredient(Items.SLIME_BLOCK, 8);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      k = new ArcanaIngredient(Items.ENDER_PEARL, 16);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {k, h, m, h, k},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.EXPULSION_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // --- GRAVITON ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.LINGERING_POTION, 1).withPotion(Potions.STRONG_SLOWNESS);
      g = new ArcanaIngredient(Items.COBWEB, 16);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {c, h, m, h, c},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.GRAVITON_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // --- PHOTONIC ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.AMETHYST_CLUSTER, 32);
      g = new ArcanaIngredient(Items.BEACON, 1);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      k = new ArcanaIngredient(Items.GLOW_INK_SAC, 32);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {k, h, m, h, k},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.PHOTONIC_ARROWS, ingredients, new ForgeRequirement().withFletchery().withEnchanter()));
      
      
      // --- SIPHONING ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_HARMING);
      g = new ArcanaIngredient(Items.FERMENTED_SPIDER_EYE, 16);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      i = new ArcanaIngredient(Items.GLISTERING_MELON_SLICE, 16);
      k = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_HEALING);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, i, a},
            {k, h, m, h, k},
            {a, i, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SIPHONING_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // --- SMOKE ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.CAMPFIRE, 12);
      g = new ArcanaIngredient(Items.GLOW_INK_SAC, 12);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      i = new ArcanaIngredient(Items.INK_SAC, 12);
      k = new ArcanaIngredient(Items.LINGERING_POTION, 1).withPotion(Potions.LONG_WEAKNESS);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, i, a},
            {k, h, m, h, k},
            {a, i, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SMOKE_ARROWS, ingredients, new ForgeRequirement().withFletchery()));
      
      
      // --- STORM ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.LIGHTNING_ROD, 24);
      g = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.CHANNELING, 1));
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {c, h, m, h, c},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.STORM_ARROWS, ingredients, new ForgeRequirement().withFletchery().withEnchanter()));
      
      
      // --- TETHER ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.STRING, 32);
      g = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.RIPTIDE, 3));
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      i = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_LEAPING);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, i, a},
            {c, h, m, h, c},
            {a, i, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TETHER_ARROWS, ingredients, new ForgeRequirement().withFletchery().withEnchanter()));
      
      
      // --- TRACKING ARROWS ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.ENDER_EYE, 16);
      g = new ArcanaIngredient(Items.COMPASS, 16);
      h = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      k = new ArcanaIngredient(Items.NETHER_STAR, 2);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {k, h, m, h, k},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TRACKING_ARROWS, ingredients, new ForgeRequirement().withFletchery().withEnchanter()));
      
      
      // ===================================
      //           CATALYSTS
      // ===================================
      
      // --- CATALYTIC MATRIX ---
      a = new ArcanaIngredient(Items.AMETHYST_SHARD, 12);
      b = new ArcanaIngredient(Items.CRAFTER, 3);
      c = new ArcanaIngredient(Items.DIAMOND, 2);
      g = new ArcanaIngredient(Items.END_CRYSTAL, 4);
      h = new ArcanaIngredient(Items.NETHER_STAR, 1);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CATALYTIC_MATRIX, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // --- EMPOWERED CATALYST ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.OBSIDIAN, 12);
      g = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 12);
      h = new ArcanaIngredient(Items.EMERALD, 8);
      m = new GenericArcanaIngredient(ArcanaRegistry.MUNDANE_CATALYST, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {c, h, m, h, c},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.EMPOWERED_CATALYST, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // --- EXOTIC CATALYST ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.OBSIDIAN, 16);
      g = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 16);
      h = new ArcanaIngredient(Items.DIAMOND, 6);
      m = new GenericArcanaIngredient(ArcanaRegistry.EMPOWERED_CATALYST, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {c, h, m, h, c},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.EXOTIC_CATALYST, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // --- MUNDANE CATALYST ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.OBSIDIAN, 8);
      g = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 8);
      h = new ArcanaIngredient(Items.QUARTZ, 6);
      m = new GenericArcanaIngredient(ArcanaRegistry.CATALYTIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {c, h, m, h, c},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.MUNDANE_CATALYST, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // --- SOVEREIGN CATALYST ---
      a = ArcanaIngredient.EMPTY;
      c = new ArcanaIngredient(Items.OBSIDIAN, 24);
      g = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 24);
      h = new ArcanaIngredient(Items.GOLD_INGOT, 12);
      m = new GenericArcanaIngredient(ArcanaRegistry.EXOTIC_CATALYST, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, a, a},
            {a, g, h, g, a},
            {c, h, m, h, c},
            {a, g, h, g, a},
            {a, a, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SOVEREIGN_CATALYST, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //            CHARMS
      // ===================================
      
      // --- CETACEA CHARM ---
      a = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.LONG_WATER_BREATHING);
      q = new ArcanaIngredient(Items.COD, 16);
      b = new ArcanaIngredient(Items.PRISMARINE_CRYSTALS, 4);
      c = new ArcanaIngredient(Items.TURTLE_SCUTE, 4);
      s = new ArcanaIngredient(Items.SALMON, 16);
      e = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.STRONG_SWIFTNESS);
      g = new ArcanaIngredient(Items.PUFFERFISH, 16);
      h = new ArcanaIngredient(Items.NAUTILUS_SHELL, 1);
      i = new ArcanaIngredient(Items.TROPICAL_FISH, 16);
      m = new ArcanaIngredient(Items.CONDUIT, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, e},
            {b, g, h, i, b},
            {c, h, m, h, c},
            {b, q, h, s, b},
            {e, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CETACEA_CHARM, ingredients, new ForgeRequirement()));
      
      
      // --- CINDERS CHARM ---
      a = new ArcanaIngredient(Items.NETHER_STAR, 2);
      b = new ArcanaIngredient(Items.BLAZE_ROD, 24);
      c = new ArcanaIngredient(Items.FIRE_CHARGE, 32);
      g = new ArcanaIngredient(Items.MAGMA_CREAM, 32);
      h = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      m = new ArcanaIngredient(Items.COAL_BLOCK, 32);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CINDERS_CHARM, ingredients, new ForgeRequirement().withCore().withAnvil()));
      
      
      // --- CLEANSING CHARM ---
      a = new ArcanaIngredient(Items.MILK_BUCKET, 1);
      b = new ArcanaIngredient(Items.HONEY_BOTTLE, 16);
      c = new ArcanaIngredient(Items.CHARCOAL, 48);
      g = new ArcanaIngredient(Items.DIAMOND, 10);
      h = new ArcanaIngredient(Items.QUARTZ, 32);
      m = new ArcanaIngredient(Items.NETHER_STAR, 2);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CLEANSING_CHARM, ingredients, new ForgeRequirement()));
      
      
      // --- FEASTING CHARM ---
      a = new ArcanaIngredient(Items.COOKED_SALMON, 16);
      b = new ArcanaIngredient(Items.GLOW_BERRIES, 16);
      c = new ArcanaIngredient(Items.COOKED_BEEF, 16);
      d = new ArcanaIngredient(Items.PUMPKIN_PIE, 16);
      e = new ArcanaIngredient(Items.BREAD, 16);
      f = new ArcanaIngredient(Items.COOKIE, 16);
      g = new ArcanaIngredient(Items.GOLDEN_CARROT, 8);
      h = new ArcanaIngredient(Items.GOLDEN_APPLE, 4);
      j = new ArcanaIngredient(Items.MELON_SLICE, 16);
      k = new ArcanaIngredient(Items.COOKED_CHICKEN, 16);
      m = new ArcanaIngredient(Items.ENCHANTED_GOLDEN_APPLE, 1, true);
      o = new ArcanaIngredient(Items.COOKED_MUTTON, 16);
      p = new ArcanaIngredient(Items.BEETROOT, 16);
      t = new ArcanaIngredient(Items.DRIED_KELP, 16);
      u = new ArcanaIngredient(Items.COOKED_RABBIT, 16);
      v = new ArcanaIngredient(Items.BAKED_POTATO, 16);
      w = new ArcanaIngredient(Items.COOKED_PORKCHOP, 16);
      x = new ArcanaIngredient(Items.SWEET_BERRIES, 16);
      y = new ArcanaIngredient(Items.COOKED_COD, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, e},
            {f, g, h, g, j},
            {k, h, m, h, o},
            {p, g, h, g, t},
            {u, v, w, x, y}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.FEASTING_CHARM, ingredients, new ForgeRequirement()));
      
      
      // --- FELIDAE CHARM ---
      a = new ArcanaIngredient(Items.GUNPOWDER, 16);
      b = new ArcanaIngredient(Items.STRING, 12);
      c = new ArcanaIngredient(Items.PUFFERFISH, 16);
      g = new ArcanaIngredient(Items.PHANTOM_MEMBRANE, 4);
      w = new ArcanaIngredient(Items.SALMON, 16);
      h = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.FEATHER_FALLING, 4));
      k = new ArcanaIngredient(Items.COD, 16);
      m = new ArcanaIngredient(Items.CREEPER_HEAD, 1, true);
      o = new ArcanaIngredient(Items.TROPICAL_FISH, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {k, h, m, h, o},
            {b, g, h, g, b},
            {a, b, w, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.FELIDAE_CHARM, ingredients, new ForgeRequirement().withEnchanter()));
      
      
      // --- LIGHT CHARM ---
      a = new ArcanaIngredient(Items.POTION, 1).withPotion(Potions.LONG_NIGHT_VISION);
      b = new ArcanaIngredient(Items.TORCH, 32);
      c = new ArcanaIngredient(Items.VERDANT_FROGLIGHT, 16);
      d = new ArcanaIngredient(Items.SOUL_LANTERN, 32);
      f = new ArcanaIngredient(Items.REDSTONE_LAMP, 16);
      g = new ArcanaIngredient(Items.SEA_LANTERN, 8);
      h = new ArcanaIngredient(Items.GLOWSTONE, 8);
      j = new ArcanaIngredient(Items.CANDLE, 16);
      k = new ArcanaIngredient(Items.PEARLESCENT_FROGLIGHT, 16);
      m = new ArcanaIngredient(Items.BEACON, 1, true);
      o = new ArcanaIngredient(Items.SHROOMLIGHT, 16);
      p = new ArcanaIngredient(Items.JACK_O_LANTERN, 16);
      t = new ArcanaIngredient(Items.COPPER_BULB, 16);
      v = new ArcanaIngredient(Items.LANTERN, 32);
      w = new ArcanaIngredient(Items.OCHRE_FROGLIGHT, 16);
      x = new ArcanaIngredient(Items.SOUL_TORCH, 32);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {f, g, h, g, j},
            {k, h, m, h, o},
            {p, g, h, g, t},
            {a, v, w, x, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.LIGHT_CHARM, ingredients, new ForgeRequirement()));
      
      
      // --- MAGNETISM CHARM ---
      a = new ArcanaIngredient(Items.LIGHTNING_ROD, 16);
      b = new ArcanaIngredient(Items.IRON_INGOT, 16);
      c = new ArcanaIngredient(Items.IRON_BARS, 16);
      h = new ArcanaIngredient(Items.IRON_BLOCK, 8);
      m = new ArcanaIngredient(Items.HEAVY_CORE, 1, true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, c, h, c, b},
            {c, h, m, h, c},
            {b, c, h, c, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.MAGNETISM_CHARM, ingredients, new ForgeRequirement()));
      
      
      // --- WILD GROWTH CHARM ---
      a = new ArcanaIngredient(Items.SWEET_BERRIES, 16);
      b = new ArcanaIngredient(Items.JUNGLE_SAPLING, 16);
      c = new ArcanaIngredient(Items.SEA_PICKLE, 16);
      d = new ArcanaIngredient(Items.ACACIA_SAPLING, 16);
      e = new ArcanaIngredient(Items.CARROT, 16);
      f = new ArcanaIngredient(Items.BIRCH_SAPLING, 16);
      g = new ArcanaIngredient(Items.TORCHFLOWER, 16);
      h = new ArcanaIngredient(Items.BONE_BLOCK, 8);
      i = new ArcanaIngredient(Items.RED_MUSHROOM, 16);
      j = new ArcanaIngredient(Items.CHERRY_SAPLING, 16);
      k = new ArcanaIngredient(Items.VINE, 16);
      m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      o = new ArcanaIngredient(Items.BAMBOO, 16);
      p = new ArcanaIngredient(Items.SPRUCE_SAPLING, 16);
      q = new ArcanaIngredient(Items.BROWN_MUSHROOM, 16);
      s = new ArcanaIngredient(Items.PITCHER_PLANT, 16);
      t = new ArcanaIngredient(Items.DARK_OAK_SAPLING, 16);
      u = new ArcanaIngredient(Items.POTATO, 16);
      v = new ArcanaIngredient(Items.OAK_SAPLING, 16);
      w = new ArcanaIngredient(Items.SUGAR_CANE, 16);
      x = new ArcanaIngredient(Items.MANGROVE_PROPAGULE, 16);
      y = new ArcanaIngredient(Items.GLOW_BERRIES, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, e},
            {f, g, h, i, j},
            {k, h, m, h, o},
            {p, q, h, s, t},
            {u, v, w, x, y}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.WILD_GROWTH_CHARM, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //         FORGE BLOCKS
      // ===================================
      
      // --- CONTINUUM ANCHOR ---
      a = new ArcanaIngredient(Items.NETHERITE_INGOT, 2);
      b = new ArcanaIngredient(Items.OBSIDIAN, 32);
      c = new ArcanaIngredient(Items.RESPAWN_ANCHOR, 16);
      g = new ArcanaIngredient(Items.NETHER_STAR, 2);
      h = new ArcanaIngredient(Items.ENDER_EYE, 16);
      m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CONTINUUM_ANCHOR, ingredients, new ForgeRequirement().withAnvil().withCore()));
      
      
      // --- FRACTAL SPONGE ---
      a = new ArcanaIngredient(Items.OBSIDIAN, 16);
      b = new ArcanaIngredient(Items.MAGMA_BLOCK, 16);
      c = new ArcanaIngredient(Items.SPONGE, 6);
      d = new ArcanaIngredient(Items.BLUE_ICE, 16);
      g = new ArcanaIngredient(Items.END_CRYSTAL, 4);
      m = new ArcanaIngredient(Items.NETHER_STAR, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {b, g, c, g, d},
            {c, c, m, c, c},
            {d, g, c, g, b},
            {a, d, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.FRACTAL_SPONGE, ingredients, new ForgeRequirement()));
      
      
      // --- IGNEOUS COLLIDER ---
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 12);
      b = new ArcanaIngredient(Items.OBSIDIAN, 16);
      g = new ArcanaIngredient(Items.MAGMA_BLOCK, 32);
      h = new ArcanaIngredient(Items.CAULDRON, 24);
      i = new ArcanaIngredient(Items.BLUE_ICE, 32);
      l = new ArcanaIngredient(Items.NETHERITE_PICKAXE, 1, true).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.EFFICIENCY, 5), new ArcanaIngredient.EnchantmentEntry(Enchantments.UNBREAKING, 3));
      m = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, a, b, a},
            {b, g, h, i, b},
            {a, l, m, l, a},
            {b, g, h, i, b},
            {a, b, a, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.IGNEOUS_COLLIDER, ingredients, new ForgeRequirement().withAnvil().withCore().withEnchanter()));
      
      
      // --- SPAWNER INFUSER ---
      a = new ArcanaIngredient(Items.NETHER_STAR, 2);
      b = new ArcanaIngredient(Items.ECHO_SHARD, 8);
      c = new ArcanaIngredient(Items.SCULK_CATALYST, 24);
      g = new ArcanaIngredient(Items.SCULK_SHRIEKER, 24);
      h = new ArcanaIngredient(Items.NETHERITE_INGOT, 2);
      m = new GenericArcanaIngredient(ArcanaRegistry.SPAWNER_HARNESS, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.SPAWNER_INFUSER, ingredients, new ForgeRequirement().withSingularity().withCore().withEnchanter().withAnvil()));
      
      
      // ===================================
      //            ALTARS
      // ===================================
      
      // --- CELESTIAL ALTAR ---
      p = new ArcanaIngredient(Items.SEA_LANTERN, 4);
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 32);
      b = new ArcanaIngredient(Items.GOLD_BLOCK, 4);
      r = new ArcanaIngredient(Items.LAPIS_BLOCK, 4);
      d = new ArcanaIngredient(Items.GLOWSTONE, 4);
      g = new ArcanaIngredient(Items.OBSIDIAN, 32);
      k = new ArcanaIngredient(ArcanaRegistry.STARDUST, 12);
      l = new ArcanaIngredient(Items.NETHER_STAR, 2);
      m = new ArcanaIngredient(Items.PEARLESCENT_FROGLIGHT, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, b, d, d},
            {b, g, b, d, d},
            {k, l, m, l, k},
            {p, p, r, g, r},
            {p, p, r, r, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.CELESTIAL_ALTAR, ingredients, new ForgeRequirement()));
      
      
      // --- STARPATH ALTAR ---
      a = new ArcanaIngredient(Items.ENDER_EYE, 24);
      b = new ArcanaIngredient(Items.OBSIDIAN, 16);
      c = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 16);
      h = new ArcanaIngredient(ArcanaRegistry.STARDUST, 8);
      m = new ArcanaIngredient(Items.NETHER_STAR, 1);
      o = new WaystoneIngredient(true);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, h, o, h, b},
            {c, o, m, o, c},
            {b, h, o, h, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.STARPATH_ALTAR, ingredients, new ForgeRequirement()));
      
      
      // --- STORMCALLER ALTAR ---
      a = new ArcanaIngredient(Items.LIGHTNING_ROD, 16);
      b = new ArcanaIngredient(Items.OXIDIZED_COPPER, 12);
      c = new ArcanaIngredient(Items.DIAMOND, 6);
      g = new ArcanaIngredient(Items.HEART_OF_THE_SEA, 1);
      h = new ArcanaIngredient(Items.COPPER_BULB, 8);
      m = new ArcanaIngredient(Items.RAW_COPPER_BLOCK, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.STORMCALLER_ALTAR, ingredients, new ForgeRequirement()));
      
      
      // --- TRANSMUTATION ALTAR ---
      a = new ArcanaIngredient(Items.BLACKSTONE, 24);
      c = new ArcanaIngredient(Items.AMETHYST_BLOCK, 16);
      d = new ArcanaIngredient(Items.QUARTZ_BLOCK, 24);
      g = new ArcanaIngredient(Items.AMETHYST_SHARD, 24);
      h = new ArcanaIngredient(Items.DIAMOND, 16);
      m = new ArcanaIngredient(Items.DIAMOND_BLOCK, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, a, c, d, d},
            {a, g, h, g, d},
            {c, h, m, h, c},
            {d, g, h, g, a},
            {d, d, c, a, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TRANSMUTATION_ALTAR, ingredients, new ForgeRequirement()));
      
      
      // ===================================
      //        FORGE ADDITIONS
      // ===================================
      
      // --- ARCANE SINGULARITY ---
      a = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      b = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 16);
      c = new ArcanaIngredient(Items.ENDER_EYE, 24);
      g = new ArcanaIngredient(ArcanaRegistry.NEBULOUS_ESSENCE, 32);
      h = new ArcanaIngredient(ArcanaRegistry.STARDUST, 32);
      m = new ArcanaIngredient(Items.NETHER_STAR, 4);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.ARCANE_SINGULARITY, ingredients, new ForgeRequirement().withEnchanter().withCore().withAnvil()));
      
      
      // --- MIDNIGHT ENCHANTER ---
      a = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 24);
      b = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.PROTECTION, 4));
      c = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.THORNS, 3));
      d = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.BINDING_CURSE, 1));
      f = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.EFFICIENCY, 5));
      g = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE, 16);
      h = new ArcanaIngredient(Items.LAPIS_BLOCK, 12);
      j = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.SWIFT_SNEAK, 3));
      k = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.UNBREAKING, 3));
      m = new ArcanaIngredient(Items.ENCHANTING_TABLE, 16);
      o = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.SOUL_SPEED, 3));
      p = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.MENDING, 1));
      t = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.FEATHER_FALLING, 4));
      v = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.SHARPNESS, 5));
      w = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.LOOTING, 3));
      x = new ArcanaIngredient(Items.ENCHANTED_BOOK, 1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.VANISHING_CURSE, 1));
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, d, a},
            {f, g, h, g, j},
            {k, h, m, h, o},
            {p, g, h, g, t},
            {a, v, w, x, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.MIDNIGHT_ENCHANTER, ingredients, new ForgeRequirement()));
      
      
      // --- RADIANT FLETCHERY ---
      a = new ArcanaIngredient(Items.BLAZE_POWDER, 24);
      b = new ArcanaIngredient(Items.GLOWSTONE_DUST, 12);
      c = new ArcanaIngredient(Items.END_CRYSTAL, 8);
      g = new ArcanaIngredient(Items.SPECTRAL_ARROW, 16);
      h = new ArcanaIngredient(Items.FLETCHING_TABLE, 8);
      m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX, 1);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.RADIANT_FLETCHERY, ingredients, new ForgeRequirement()));
      
      
      // --- STELLAR CORE ---
      a = new ArcanaIngredient(Items.NETHER_STAR, 1);
      b = new ArcanaIngredient(Items.CRYING_OBSIDIAN, 12);
      c = new ArcanaIngredient(Items.OBSIDIAN, 32);
      g = new ArcanaIngredient(Items.BLAZE_POWDER, 32);
      h = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      m = new ArcanaIngredient(Items.BLAST_FURNACE, 16);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.STELLAR_CORE, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // --- TWILIGHT ANVIL ---
      a = new ArcanaIngredient(Items.DIAMOND, 4);
      b = new ArcanaIngredient(Items.NETHERITE_SCRAP, 1);
      c = new ArcanaIngredient(Items.GLOWSTONE_DUST, 16);
      g = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE, 8);
      h = new ArcanaIngredient(Items.NETHERITE_INGOT, 1);
      m = new ArcanaIngredient(Items.ANVIL, 8);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, a},
            {b, g, h, g, b},
            {c, h, m, h, c},
            {b, g, h, g, b},
            {a, b, c, b, a}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TWILIGHT_ANVIL, ingredients, new ForgeRequirement()));
      
      // ===================================
      //           TRANSMOGRIFICATION CATALYST
      // ===================================
      a = new ArcanaIngredient(Items.WHITE_DYE, 8);
      b = new ArcanaIngredient(Items.AMETHYST_SHARD, 4);
      c = new ArcanaIngredient(Items.LIME_DYE, 8);
      d = new ArcanaIngredient(Items.GRAY_DYE, 8);
      e = new ArcanaIngredient(Items.LIGHT_BLUE_DYE, 8);
      f = new ArcanaIngredient(Items.MAGENTA_DYE, 8);
      g = new ArcanaIngredient(Items.CYAN_DYE, 8);
      h = new ArcanaIngredient(Items.RED_DYE, 8);
      i = new ArcanaIngredient(Items.PINK_DYE, 8);
      j = new GenericArcanaIngredient(ArcanaRegistry.CATALYTIC_MATRIX, 1);
      k = new ArcanaIngredient(Items.BROWN_DYE, 8);
      l = new ArcanaIngredient(Items.ORANGE_DYE, 8);
      m = new ArcanaIngredient(Items.GREEN_DYE, 8);
      n = new ArcanaIngredient(Items.PURPLE_DYE, 8);
      o = new ArcanaIngredient(Items.BLUE_DYE, 8);
      p = new ArcanaIngredient(Items.LIGHT_GRAY_DYE, 8);
      q = new ArcanaIngredient(Items.YELLOW_DYE, 8);
      r = new ArcanaIngredient(Items.BLACK_DYE, 8);
      
      ingredients = new ArcanaIngredient[][]{
            {a, b, c, b, d},
            {b, e, f, g, b},
            {h, i, j, k, l},
            {b, m, n, o, b},
            {p, b, q, b, r}};
      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry.TRANSMOGRIFICATION_CATALYST, ingredients, new ForgeRequirement().withAnvil()));
      
      
      // ===================================
      //        TRANSMUTATION RECIPES
      // ===================================
      List<TransmutationRecipe> transmutationRecipes = new ArrayList<>();
      
      // Commutative Recipes
      transmutationRecipes.add(new CommutativeTransmutationRecipe("stones", new ItemStack(Items.COAL, 16), new ItemStack(Items.QUARTZ, 12))
            .with(Items.COBBLESTONE, Items.STONE, Items.GRANITE, Items.POLISHED_GRANITE, Items.DIORITE, Items.POLISHED_DIORITE, Items.ANDESITE,
                  Items.POLISHED_ANDESITE, Items.DEEPSLATE, Items.COBBLED_DEEPSLATE, Items.POLISHED_DEEPSLATE, Items.DEEPSLATE_TILES,
                  Items.CRACKED_DEEPSLATE_TILES, Items.CALCITE, Items.TUFF, Items.DRIPSTONE_BLOCK, Items.SMOOTH_STONE, Items.STONE_BRICKS,
                  Items.MOSSY_STONE_BRICKS, Items.CRACKED_STONE_BRICKS, Items.CHISELED_STONE_BRICKS, Items.MOSSY_COBBLESTONE, Items.BASALT,
                  Items.POLISHED_BASALT, Items.SMOOTH_BASALT, Items.BLACKSTONE, Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE_BRICKS,
                  Items.CRACKED_POLISHED_BLACKSTONE_BRICKS)
            .with(Either.left(ConventionalItemTags.STONES))
            .with(Either.left(ConventionalItemTags.COBBLESTONES))
            .withViewStack(new ItemStack(Items.COBBLESTONE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("dirts", new ItemStack(Items.EMERALD, 6), new ItemStack(Items.QUARTZ, 12))
            .with(Items.GRASS_BLOCK, Items.DIRT, Items.COARSE_DIRT, Items.ROOTED_DIRT, Items.DIRT_PATH, Items.PODZOL, Items.MYCELIUM)
            .withViewStack(new ItemStack(Items.GRASS_BLOCK)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("sandstones", new ItemStack(Items.REDSTONE, 16), new ItemStack(Items.QUARTZ, 12))
            .with(Items.SANDSTONE, Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE, Items.SMOOTH_SANDSTONE, Items.RED_SAND, Items.CHISELED_RED_SANDSTONE,
                  Items.CUT_RED_SANDSTONE, Items.SMOOTH_RED_SANDSTONE)
            .with(Either.left(ConventionalItemTags.SANDSTONE_BLOCKS))
            .withViewStack(new ItemStack(Items.SANDSTONE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("copper", new ItemStack(Items.REDSTONE, 36), new ItemStack(Items.AMETHYST_SHARD, 24))
            .with(Items.OXIDIZED_COPPER, Items.WEATHERED_COPPER, Items.EXPOSED_COPPER, Items.COPPER_BLOCK,
                  Items.OXIDIZED_CUT_COPPER, Items.WEATHERED_CUT_COPPER, Items.EXPOSED_CUT_COPPER, Items.CUT_COPPER,
                  Items.OXIDIZED_CUT_COPPER_STAIRS, Items.WEATHERED_CUT_COPPER_STAIRS, Items.EXPOSED_CUT_COPPER_STAIRS, Items.CUT_COPPER_STAIRS,
                  Items.OXIDIZED_CHISELED_COPPER, Items.WEATHERED_CHISELED_COPPER, Items.EXPOSED_CHISELED_COPPER, Items.CHISELED_COPPER,
                  Items.OXIDIZED_COPPER_GRATE, Items.WEATHERED_COPPER_GRATE, Items.EXPOSED_COPPER_GRATE, Items.COPPER_GRATE,
                  Items.COPPER_BARS.oxidized(), Items.COPPER_BARS.weathered(), Items.COPPER_BARS.exposed(), Items.COPPER_BARS.unaffected(),
                  Items.COPPER_CHAIN.oxidized(), Items.COPPER_CHAIN.weathered(), Items.COPPER_CHAIN.exposed(), Items.COPPER_CHAIN.unaffected(),
                  Items.COPPER_LANTERN.oxidized(), Items.COPPER_LANTERN.weathered(), Items.COPPER_LANTERN.exposed(), Items.COPPER_LANTERN.unaffected(),
                  Items.OXIDIZED_CUT_COPPER_SLAB, Items.WEATHERED_CUT_COPPER_SLAB, Items.EXPOSED_CUT_COPPER_SLAB, Items.CUT_COPPER_SLAB,
                  Items.OXIDIZED_COPPER_DOOR, Items.WEATHERED_COPPER_DOOR, Items.EXPOSED_COPPER_DOOR, Items.COPPER_DOOR,
                  Items.OXIDIZED_COPPER_TRAPDOOR, Items.WEATHERED_COPPER_TRAPDOOR, Items.EXPOSED_COPPER_TRAPDOOR, Items.COPPER_TRAPDOOR,
                  Items.OXIDIZED_COPPER_BULB, Items.WEATHERED_COPPER_BULB, Items.EXPOSED_COPPER_BULB, Items.COPPER_BULB,
                  Items.OXIDIZED_COPPER_GOLEM_STATUE, Items.WEATHERED_COPPER_GOLEM_STATUE, Items.EXPOSED_COPPER_GOLEM_STATUE, Items.COPPER_GOLEM_STATUE,
                  Items.OXIDIZED_COPPER_CHEST, Items.WEATHERED_COPPER_CHEST, Items.EXPOSED_COPPER_CHEST, Items.COPPER_CHEST)
            .withViewStack(new ItemStack(Items.OXIDIZED_COPPER)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("logs", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Items.ACACIA_LOG, Items.BAMBOO_BLOCK, Items.BIRCH_LOG, Items.CHERRY_LOG, Items.CRIMSON_STEM,
                  Items.DARK_OAK_LOG, Items.JUNGLE_LOG, Items.MANGROVE_LOG, Items.OAK_LOG, Items.SPRUCE_LOG,
                  Items.WARPED_STEM, Items.PALE_OAK_LOG, Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_BAMBOO_BLOCK,
                  Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_CHERRY_LOG, Items.STRIPPED_CRIMSON_STEM,
                  Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_MANGROVE_LOG,
                  Items.STRIPPED_OAK_LOG, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_WARPED_STEM,
                  Items.STRIPPED_PALE_OAK_LOG)
            .with(Either.left(ItemTags.LOGS))
            .with(Either.left(ConventionalItemTags.STRIPPED_LOGS))
            .withViewStack(new ItemStack(Items.OAK_LOG)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("wood", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Items.ACACIA_WOOD, Items.BAMBOO_BLOCK, Items.BIRCH_WOOD, Items.CHERRY_WOOD, Items.CRIMSON_HYPHAE,
                  Items.DARK_OAK_WOOD, Items.JUNGLE_WOOD, Items.MANGROVE_WOOD, Items.OAK_WOOD, Items.SPRUCE_WOOD,
                  Items.WARPED_HYPHAE, Items.PALE_OAK_WOOD, Items.STRIPPED_ACACIA_WOOD, Items.STRIPPED_BAMBOO_BLOCK,
                  Items.STRIPPED_BIRCH_WOOD, Items.STRIPPED_CHERRY_WOOD, Items.STRIPPED_CRIMSON_HYPHAE,
                  Items.STRIPPED_DARK_OAK_WOOD, Items.STRIPPED_JUNGLE_WOOD, Items.STRIPPED_MANGROVE_WOOD,
                  Items.STRIPPED_OAK_WOOD, Items.STRIPPED_SPRUCE_WOOD, Items.STRIPPED_WARPED_HYPHAE,
                  Items.STRIPPED_PALE_OAK_WOOD)
            .withViewStack(new ItemStack(Items.OAK_WOOD)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("planks", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.PLANKS))
            .withViewStack(new ItemStack(Items.OAK_PLANKS)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("stairs", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.WOODEN_STAIRS))
            .withViewStack(new ItemStack(Items.OAK_STAIRS)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("slabs", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.WOODEN_SLABS))
            .withViewStack(new ItemStack(Items.OAK_SLAB)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("fences", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.WOODEN_FENCES))
            .with(Either.left(ConventionalItemTags.WOODEN_FENCES))
            .withViewStack(new ItemStack(Items.OAK_FENCE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("fence_gates", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.FENCE_GATES))
            .with(Either.left(ConventionalItemTags.WOODEN_FENCE_GATES))
            .withViewStack(new ItemStack(Items.OAK_FENCE_GATE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("signs", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.SIGNS))
            .withViewStack(new ItemStack(Items.OAK_SIGN)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("hanging_signs", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.HANGING_SIGNS)).
            withViewStack(new ItemStack(Items.OAK_HANGING_SIGN)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("doors", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.WOODEN_DOORS))
            .withViewStack(new ItemStack(Items.OAK_DOOR)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("trapdoors", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.WOODEN_TRAPDOORS))
            .withViewStack(new ItemStack(Items.OAK_TRAPDOOR)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("pressure_plates", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.WOODEN_PRESSURE_PLATES))
            .withViewStack(new ItemStack(Items.OAK_PRESSURE_PLATE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("buttons", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.BUTTONS))
            .withViewStack(new ItemStack(Items.OAK_BUTTON)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("boats", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.BOATS))
            .withViewStack(new ItemStack(Items.OAK_BOAT)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("chest_boats", new ItemStack(Items.COPPER_INGOT, 12), new ItemStack(Items.EMERALD, 6))
            .with(Either.left(ItemTags.CHEST_BOATS))
            .withViewStack(new ItemStack(Items.OAK_CHEST_BOAT)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("seeds", new ItemStack(Items.LAPIS_LAZULI, 16), new ItemStack(Items.EMERALD, 24))
            .with(Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS,
                  Items.PITCHER_POD, Items.POTATO, Items.CARROT, Items.COCOA_BEANS)
            .with(Either.left(ConventionalItemTags.SEEDS))
            .withViewStack(new ItemStack(Items.WHEAT_SEEDS)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("eggs", new ItemStack(Items.LAPIS_LAZULI, 16), new ItemStack(Items.EMERALD, 24))
            .with(Items.EGG, Items.BLUE_EGG, Items.BROWN_EGG, Items.TURTLE_EGG, Items.SNIFFER_EGG)
            .with(Either.left(ConventionalItemTags.EGGS))
            .withViewStack(new ItemStack(Items.EGG)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("leaves", new ItemStack(Items.LAPIS_LAZULI, 16), new ItemStack(Items.EMERALD, 12))
            .with(Either.right(BlockTags.LEAVES))
            .withViewStack(new ItemStack(Items.OAK_LEAVES)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("flowers_and_plants", new ItemStack(Items.GLOWSTONE_DUST, 16), new ItemStack(Items.LAPIS_LAZULI, 32))
            .with(Items.PEONY, Items.ROSE_BUSH, Items.LILAC, Items.SUNFLOWER, Items.AZURE_BLUET, Items.OXEYE_DAISY,
                  Items.CORNFLOWER, Items.WITHER_ROSE, Items.TORCHFLOWER, Items.PITCHER_PLANT, Items.PINK_PETALS,
                  Items.DANDELION, Items.POPPY, Items.BLUE_ORCHID, Items.ALLIUM, Items.RED_TULIP, Items.ORANGE_TULIP,
                  Items.WHITE_TULIP, Items.PINK_TULIP, Items.SPORE_BLOSSOM, Items.FLOWERING_AZALEA, Items.OPEN_EYEBLOSSOM,
                  Items.CLOSED_EYEBLOSSOM, Items.CHORUS_FLOWER, Items.BUSH, Items.FIREFLY_BUSH, Items.DRY_SHORT_GRASS,
                  Items.DRY_TALL_GRASS, Items.CACTUS_FLOWER, Items.WILDFLOWERS, Items.LEAF_LITTER, Items.BIG_DRIPLEAF,
                  Items.SMALL_DRIPLEAF, Items.VINE, Items.FERN, Items.LARGE_FERN, Items.TALL_GRASS, Items.SHORT_GRASS,
                  Items.LILY_PAD, Items.PALE_HANGING_MOSS, Items.GLOW_LICHEN)
            .with(Either.left(ConventionalItemTags.FLOWERS))
            .withViewStack(new ItemStack(Items.POPPY)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("fungi", new ItemStack(Items.GLOWSTONE_DUST, 16), new ItemStack(Items.LAPIS_LAZULI, 32))
            .with(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS, Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM_BLOCK, Items.MUSHROOM_STEM)
            .withViewStack(new ItemStack(Items.RED_MUSHROOM)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("froglights", new ItemStack(Items.REDSTONE, 48), new ItemStack(Items.EMERALD, 48))
            .with(Items.OCHRE_FROGLIGHT, Items.PEARLESCENT_FROGLIGHT, Items.VERDANT_FROGLIGHT)
            .withViewStack(new ItemStack(Items.OCHRE_FROGLIGHT)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("fish", new ItemStack(Items.REDSTONE, 16), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH)
            .withViewStack(new ItemStack(Items.COD)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("coral", new ItemStack(Items.COPPER_INGOT, 32), new ItemStack(Items.LAPIS_LAZULI, 32))
            .with(Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK,
                  Items.HORN_CORAL_BLOCK, Items.TUBE_CORAL, Items.BRAIN_CORAL, Items.BUBBLE_CORAL, Items.FIRE_CORAL,
                  Items.HORN_CORAL, Items.TUBE_CORAL_FAN, Items.BRAIN_CORAL_FAN, Items.BUBBLE_CORAL_FAN,
                  Items.FIRE_CORAL_FAN, Items.HORN_CORAL_FAN, Items.DEAD_TUBE_CORAL_BLOCK, Items.DEAD_BRAIN_CORAL_BLOCK,
                  Items.DEAD_BUBBLE_CORAL_BLOCK, Items.DEAD_FIRE_CORAL_BLOCK, Items.DEAD_HORN_CORAL_BLOCK,
                  Items.DEAD_TUBE_CORAL, Items.DEAD_BRAIN_CORAL, Items.DEAD_BUBBLE_CORAL, Items.DEAD_FIRE_CORAL,
                  Items.DEAD_HORN_CORAL, Items.DEAD_TUBE_CORAL_FAN, Items.DEAD_BRAIN_CORAL_FAN, Items.DEAD_BUBBLE_CORAL_FAN,
                  Items.DEAD_FIRE_CORAL_FAN, Items.DEAD_HORN_CORAL_FAN)
            .withViewStack(new ItemStack(Items.TUBE_CORAL_BLOCK)));
      
      CommutativeTransmutationRecipe banners = new CommutativeTransmutationRecipe("banner_patterns", new ItemStack(Items.REDSTONE, 48), new ItemStack(Items.QUARTZ, 24));
      for(Holder<Item> itemEntry : BuiltInRegistries.ITEM.asHolderIdMap()){
         if(itemEntry.value().getDefaultInstance().has(DataComponents.PROVIDES_BANNER_PATTERNS)){
            banners = banners.with(itemEntry.value());
         }
      }
      transmutationRecipes.add(banners.withViewStack(new ItemStack(Items.CREEPER_BANNER_PATTERN)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("heads", new ItemStack(Items.NETHERITE_SCRAP, 4), new ItemStack(Items.REDSTONE, 64))
            .with(Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.ZOMBIE_HEAD, Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD).withViewStack(new ItemStack(Items.SKELETON_SKULL)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("smithing_templates", new ItemStack(Items.DIAMOND, 16), new ItemStack(Items.LAPIS_LAZULI, 64))
            .with(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE)
            .withViewStack(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("pottery_sherds", new ItemStack(Items.AMETHYST_SHARD, 36), new ItemStack(Items.LAPIS_LAZULI, 24))
            .with(Either.left(ItemTags.DECORATED_POT_SHERDS))
            .withViewStack(new ItemStack(Items.ANGLER_POTTERY_SHERD)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("music_discs", new ItemStack(Items.DIAMOND, 12), new ItemStack(Items.AMETHYST_SHARD, 36))
            .with(Either.left(ConventionalItemTags.MUSIC_DISCS))
            .withViewStack(new ItemStack(Items.MUSIC_DISC_CAT)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("dyes", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.left(ConventionalItemTags.DYES))
            .withViewStack(new ItemStack(Items.WHITE_DYE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("terracotta", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.right(BlockTags.TERRACOTTA))
            .withViewStack(new ItemStack(Items.TERRACOTTA)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("wool", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.left(ItemTags.WOOL))
            .withViewStack(new ItemStack(Items.WHITE_WOOL)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("carpet", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.left(ItemTags.WOOL_CARPETS))
            .withViewStack(new ItemStack(Items.WHITE_CARPET)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("glass", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.left(ConventionalItemTags.GLASS_BLOCKS_CHEAP))
            .withViewStack(new ItemStack(Items.GLASS)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("glass_panes", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.left(ConventionalItemTags.GLASS_PANES))
            .withViewStack(new ItemStack(Items.GLASS_PANE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("candles", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.left(ItemTags.CANDLES)).withViewStack(new ItemStack(Items.CANDLE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("glazed_terracotta", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.left(ConventionalItemTags.GLAZED_TERRACOTTAS))
            .withViewStack(new ItemStack(Items.WHITE_GLAZED_TERRACOTTA)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("concrete", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.left(ConventionalItemTags.CONCRETES))
            .withViewStack(new ItemStack(Items.WHITE_CONCRETE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("gravity_blocks", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.left(ConventionalItemTags.CONCRETE_POWDERS))
            .with(Either.left(ConventionalItemTags.SANDS))
            .with(Either.left(ConventionalItemTags.GRAVELS))
            .withViewStack(new ItemStack(Items.WHITE_CONCRETE_POWDER)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("banners", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.right(BlockTags.BANNERS))
            .withViewStack(new ItemStack(Items.WHITE_BANNER)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("beds", new ItemStack(Items.LAPIS_LAZULI, 24), new ItemStack(Items.GLOWSTONE_DUST, 12))
            .with(Either.right(BlockTags.BEDS))
            .withViewStack(new ItemStack(Items.WHITE_BED)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("rods", new ItemStack(Items.QUARTZ, 35), new ItemStack(Items.REDSTONE, 45))
            .with(Items.BLAZE_ROD, Items.BREEZE_ROD)
            .withViewStack(new ItemStack(Items.BREEZE_ROD)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("coal_ore", new ItemStack(Items.COAL, 16), new ItemStack(Items.QUARTZ, 12))
            .with(Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE)
            .withViewStack(new ItemStack(Items.COAL_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("emerald_ore", new ItemStack(Items.COAL, 16), new ItemStack(Items.QUARTZ, 12))
            .with(Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE)
            .withViewStack(new ItemStack(Items.EMERALD_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("iron_ore", new ItemStack(Items.COAL, 16), new ItemStack(Items.QUARTZ, 12))
            .with(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE)
            .withViewStack(new ItemStack(Items.IRON_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("lapis_ore", new ItemStack(Items.COAL, 16), new ItemStack(Items.QUARTZ, 12))
            .with(Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE)
            .withViewStack(new ItemStack(Items.LAPIS_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("diamond_ore", new ItemStack(Items.COAL, 16), new ItemStack(Items.QUARTZ, 12))
            .with(Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE)
            .withViewStack(new ItemStack(Items.DIAMOND_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("copper_ore", new ItemStack(Items.COAL, 16), new ItemStack(Items.QUARTZ, 12))
            .with(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE)
            .withViewStack(new ItemStack(Items.COPPER_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("gold_ore", new ItemStack(Items.COAL, 16), new ItemStack(Items.QUARTZ, 12))
            .with(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE, Items.NETHER_GOLD_ORE)
            .withViewStack(new ItemStack(Items.GOLD_ORE)));
      
      transmutationRecipes.add(new CommutativeTransmutationRecipe("redstone_ore", new ItemStack(Items.COAL, 16), new ItemStack(Items.QUARTZ, 12))
            .with(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE)
            .withViewStack(new ItemStack(Items.REDSTONE_ORE)));
      
      // Infusion Recipes
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("cream_reduction", new ItemStack(Items.MAGMA_CREAM, 1), new ItemStack(Items.SLIME_BALL, 1), new ItemStack(Items.CHARCOAL, 16), new ItemStack(Items.SUGAR, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("sand_decomposition", new ItemStack(Items.SANDSTONE, 1), new ItemStack(Items.SAND, 4), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("fiber_decomposition", new ItemStack(Items.WHITE_WOOL, 1), new ItemStack(Items.STRING, 4), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("honeycomb_decomposition", new ItemStack(Items.HONEYCOMB_BLOCK, 1), new ItemStack(Items.HONEYCOMB, 4), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("amethyst_decomposition", new ItemStack(Items.AMETHYST_BLOCK, 1), new ItemStack(Items.AMETHYST_SHARD, 4), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("prismarine_decomposition", new ItemStack(Items.PRISMARINE, 1), new ItemStack(Items.PRISMARINE_SHARD, 4), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("prismarine_brick_decomposition", new ItemStack(Items.PRISMARINE_BRICKS, 1), new ItemStack(Items.PRISMARINE_SHARD, 9), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("quartz_decomposition", new ItemStack(Items.QUARTZ_BLOCK, 1), new ItemStack(Items.QUARTZ, 4), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("nether_brick_decomposition", new ItemStack(Items.NETHER_BRICKS, 1), new ItemStack(Items.NETHER_BRICK, 4), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("glowstone_decomposition", new ItemStack(Items.GLOWSTONE, 1), new ItemStack(Items.GLOWSTONE_DUST, 4), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("magma_decomposition", new ItemStack(Items.MAGMA_BLOCK, 1), new ItemStack(Items.MAGMA_CREAM, 4), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("nether_wart_decomposition", new ItemStack(Items.NETHER_WART_BLOCK, 1), new ItemStack(Items.NETHER_WART, 9), new ItemStack(Items.GUNPOWDER, 4), new ItemStack(ArcanaRegistry.STARDUST, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("blaze_rod_decomposition", new ItemStack(Items.BLAZE_ROD, 1), new ItemStack(Items.BLAZE_POWDER, 6), new ItemStack(Items.GUNPOWDER, 24), new ItemStack(ArcanaRegistry.STARDUST, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("breeze_rod_decomposition", new ItemStack(Items.BREEZE_ROD, 1), new ItemStack(Items.WIND_CHARGE, 9), new ItemStack(Items.GUNPOWDER, 24), new ItemStack(ArcanaRegistry.STARDUST, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("blackstone_infusion", new ItemStack(Items.BLACKSTONE, 1), new ItemStack(Items.GILDED_BLACKSTONE, 1), new ItemStack(Items.GLOWSTONE_DUST, 24), new ItemStack(Items.GOLD_INGOT, 12)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("nether_brick_infusion", new ItemStack(Items.NETHER_BRICKS, 1), new ItemStack(Items.RED_NETHER_BRICKS, 1), new ItemStack(Items.NETHER_WART, 32), new ItemStack(Items.GLOWSTONE_DUST, 12)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("obsidian_infusion", new ItemStack(Items.MAGMA_BLOCK, 4), new ItemStack(Items.OBSIDIAN, 1), new ItemStack(Items.ICE, 12), new ItemStack(Items.REDSTONE, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("crying_obsidian_infusion", new ItemStack(Items.OBSIDIAN, 1), new ItemStack(Items.CRYING_OBSIDIAN, 1), new ItemStack(Items.REDSTONE, 16), new ItemStack(Items.GLOWSTONE_DUST, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("glowstone_infusion", new ItemStack(Items.REDSTONE, 4), new ItemStack(Items.GLOWSTONE_DUST, 1), new ItemStack(Items.REDSTONE, 16), new ItemStack(Items.QUARTZ, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("ender_infusion", new ItemStack(Items.ENDER_PEARL, 1), new ItemStack(Items.ENDER_EYE, 2), new ItemStack(Items.GLOWSTONE_DUST, 12), new ItemStack(Items.BLAZE_POWDER, 24)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("chorus_transmogrification", new ItemStack(Items.APPLE, 1), new ItemStack(Items.CHORUS_FLOWER, 1), new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 12), new ItemStack(Items.ENDER_PEARL, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("endstone_transmogrification", new ItemStack(Items.COBBLED_DEEPSLATE, 1), new ItemStack(Items.END_STONE, 1), new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 6), new ItemStack(Items.ENDER_PEARL, 4)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("refined_flesh", new ItemStack(Items.ROTTEN_FLESH, 4), new ItemStack(Items.LEATHER, 1), new ItemStack(Items.REDSTONE, 8), new ItemStack(Items.SUGAR, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("carbon_compression", new ItemStack(Items.COAL, 12), new ItemStack(Items.DIAMOND, 1), new ItemStack(Items.REDSTONE, 16), new ItemStack(Items.GUNPOWDER, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("refined_netherite", new ItemStack(Items.ANCIENT_DEBRIS, 3), new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(Items.GOLD_INGOT, 16), new ItemStack(Items.GLOWSTONE_DUST, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("condensed_string", new ItemStack(Items.STRING, 16), new ItemStack(Items.COBWEB, 1), new ItemStack(Items.SLIME_BLOCK, 4), new ItemStack(Items.HONEY_BLOCK, 4)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("key_ominousification", new ItemStack(Items.TRIAL_KEY, 1), new ItemStack(Items.OMINOUS_TRIAL_KEY, 1), new ItemStack(Items.OMINOUS_BOTTLE, 1), new ItemStack(Items.DIAMOND, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("sand_susification", new ItemStack(Items.SAND, 1), new ItemStack(Items.SUSPICIOUS_SAND, 1), new ItemStack(Items.SUSPICIOUS_STEW, 1), new ItemStack(ArcanaRegistry.STARDUST, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("gravel_susification", new ItemStack(Items.GRAVEL, 1), new ItemStack(Items.SUSPICIOUS_GRAVEL, 1), new ItemStack(Items.SUSPICIOUS_STEW, 1), new ItemStack(ArcanaRegistry.STARDUST, 8)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("liquid_experience", new ItemStack(Items.SCULK, 64), new ItemStack(Items.EXPERIENCE_BOTTLE, 16), new ItemStack(Items.GLASS_BOTTLE, 16), new ItemStack(Items.LAPIS_LAZULI, 32)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("sculk_infusion", new ItemStack(Items.SCULK_VEIN, 4), new ItemStack(Items.SCULK, 1), new ItemStack(Items.GUNPOWDER, 16), new ItemStack(Items.DIAMOND, 4)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("sculk_shard_infusion", new ItemStack(Items.SCULK, 4), new ItemStack(Items.ECHO_SHARD, 1), new ItemStack(Items.AMETHYST_SHARD, 36), new ItemStack(Items.DIAMOND, 12)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("lodestone_refinement", new ItemStack(Items.LODESTONE, 1), ArcanaRegistry.WAYSTONE.getPrefItemNoLore(), new ItemStack(Items.AMETHYST_SHARD, 16), new ItemStack(Items.REDSTONE, 42)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("divine_paper_infusion", new ItemStack(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER, 2), new ItemStack(ArcanaRegistry.DIVINE_ARCANE_PAPER, 1), ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(), new ItemStack(Items.DIAMOND, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("nautilus_synthesis", new ItemStack(Items.TURTLE_SCUTE, 4), new ItemStack(Items.NAUTILUS_SHELL, 1), new ItemStack(Items.PRISMARINE_CRYSTALS, 16), new ItemStack(Items.QUARTZ, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("oceanic_heart_synthesis", new ItemStack(Items.NAUTILUS_SHELL, 8), new ItemStack(Items.HEART_OF_THE_SEA, 1), new ItemStack(Items.PRISMARINE_CRYSTALS, 32), new ItemStack(Items.QUARTZ, 32)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("trident_synthesis", new ItemStack(Items.DIAMOND_SWORD, 1), new ItemStack(Items.TRIDENT, 1), new ItemStack(Items.PRISMARINE_CRYSTALS, 16), new ItemStack(Items.HEART_OF_THE_SEA, 1)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("resonator_synthesis", new ItemStack(Items.GOLD_BLOCK, 1), new ItemStack(Items.BELL, 1), new ItemStack(Items.QUARTZ, 16), new ItemStack(Items.AMETHYST_SHARD, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("gravity_core_synthesis", new ItemStack(Items.NETHERITE_BLOCK, 1), new ItemStack(Items.HEAVY_CORE, 1), new ItemStack(Items.IRON_BLOCK, 32), new ItemStack(Items.POLISHED_TUFF, 32)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("enchanted_golden_apple_synthesis", new ItemStack(Items.TOTEM_OF_UNDYING, 1), new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1), new ItemStack(Items.GOLD_BLOCK, 8), new ItemStack(Items.GOLDEN_APPLE, 16)));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("divine_catalyst_synthesis", ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(), ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(), ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(), ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore()));
      
      transmutationRecipes.add(new InfusionTransmutationRecipe("aequalis_scientia_synthesis", ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(), ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore(), new ItemStack(Items.AMETHYST_BLOCK, 64), new ItemStack(Items.DIAMOND_BLOCK, 1)));
      
      
      // Save Recipes
      Path dirPath = FabricLoader.getInstance().getConfigDir().resolve("arcananovum").resolve("recipes").resolve("classic");
      Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
      
      for(ArcanaRecipe recipe : arcanaRecipes){
         File newFile = dirPath.resolve(recipe.getOutputId().getPath() + "_forging.json").toFile();
         newFile.getParentFile().mkdirs();
         
         try(BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)))){
            JsonObject json = recipe.toJson();
            gson.toJson(json, output);
            //ArcanaNovum.log.info("Saved Forging Recipe for {} to {}",recipe.getOutputId().toString(),newFile.getAbsolutePath());
         }catch(IOException err){
            ArcanaNovum.log(2, "Failed to save " + recipe.getOutputId().toString() + " forging recipe file!");
            ArcanaNovum.log(2, err.toString());
         }
      }
      
      for(TransmutationRecipe recipe : transmutationRecipes){
         JsonObject json = null;
         if(recipe instanceof CommutativeTransmutationRecipe comm) json = comm.toJson();
         if(recipe instanceof InfusionTransmutationRecipe infu) json = infu.toJson();
         if(json == null) continue;
         File newFile = dirPath.resolve(recipe.getId() + "_transmutation.json").toFile();
         newFile.getParentFile().mkdirs();
         
         try(BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)))){
            gson.toJson(json, output);
            //ArcanaNovum.log.info("Saved Recipe for {} to {}",recipe.getId(),newFile.getAbsolutePath());
         }catch(IOException err){
            ArcanaNovum.log(2, "Failed to save " + recipe.getId() + " transmutation recipe file!");
            ArcanaNovum.log(2, err.toString());
         }
      }
   }
   
   private static void deleteDirectoryRecursively(Path path) throws IOException{
      if(!path.toFile().exists()) return;
      
      File[] files = path.toFile().listFiles();
      if(files != null){
         for(File file : files){
            if(file.isDirectory()){
               deleteDirectoryRecursively(file.toPath());
            }else{
               if(!file.delete()){
                  ArcanaNovum.log(2, "Failed to delete file: " + file.getAbsolutePath());
               }
            }
         }
      }
      if(!path.toFile().delete()){
         ArcanaNovum.log(2, "Failed to delete directory: " + path.toAbsolutePath());
      }
   }
}
