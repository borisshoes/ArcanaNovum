package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum MagicRarity {
   MUNDANE(0,"Mundane"),
   EMPOWERED(1,"Empowered"),
   EXOTIC(2,"Exotic"),
   LEGENDARY(3,"Legendary"),
   MYTHICAL(4,"Mythical");
   
   public final String label;
   public final int rarity;
   
   MagicRarity(int rarity, String label){
      this.label = label;
      this.rarity = rarity;
   }
   
   public static MagicRarity rarityFromInt(int value){
      for(MagicRarity magicRarity : MagicRarity.values()){
         if(magicRarity.rarity == value) return magicRarity;
      }
      return null;
   }
   
   public static Text getColoredLabel(MagicRarity rarity, boolean bold){
      MutableText text;
      if(rarity == null){
         text = Text.literal("None").formatted(Formatting.WHITE);
      }else{
         text = Text.literal(rarity.label);
      }
      if(bold) text = text.formatted(Formatting.BOLD);
      if (rarity == null) return text;
   
      return switch(rarity){
         case MUNDANE -> text.formatted(Formatting.GRAY);
         case EMPOWERED -> text.formatted(Formatting.GREEN);
         case EXOTIC -> text.formatted(Formatting.AQUA);
         case LEGENDARY -> text.formatted(Formatting.GOLD);
         case MYTHICAL -> text.formatted(Formatting.LIGHT_PURPLE);
      };
   }
   
   public static Formatting getColor(MagicRarity rarity){
      if(rarity == null){
         return null;
      }
   
      return switch(rarity){
         case MUNDANE -> Formatting.GRAY;
         case EMPOWERED -> Formatting.GREEN;
         case EXOTIC -> Formatting.AQUA;
         case LEGENDARY -> Formatting.GOLD;
         case MYTHICAL -> Formatting.LIGHT_PURPLE;
      };
   }
   
   public static Item getColoredConcrete(MagicRarity rarity){
      if(rarity == null){
         return Items.BLACK_CONCRETE;
      }
   
      return switch(rarity){
         case MUNDANE -> Items.LIGHT_GRAY_CONCRETE;
         case EMPOWERED -> Items.LIME_CONCRETE;
         case EXOTIC -> Items.LIGHT_BLUE_CONCRETE;
         case LEGENDARY -> Items.ORANGE_CONCRETE;
         case MYTHICAL -> Items.MAGENTA_CONCRETE;
      };
   }
   
   public static MagicItem getAugmentCatalyst(MagicRarity rarity){
      if(rarity == null){
         return null;
      }
   
      return switch(rarity){
         case MUNDANE -> ArcanaRegistry.MUNDANE_CATALYST;
         case EMPOWERED -> ArcanaRegistry.EMPOWERED_CATALYST;
         case EXOTIC -> ArcanaRegistry.EXOTIC_CATALYST;
         case LEGENDARY -> ArcanaRegistry.LEGENDARY_CATALYST;
         case MYTHICAL -> ArcanaRegistry.MYTHICAL_CATALYST;
      };
   }
   
   public static int getRarityInt(MagicRarity rarity){
      return rarity.rarity;
   }
   
   public static int getConcentration(MagicRarity rarity){
      return switch(rarity){
         case MUNDANE -> 0;
         case EMPOWERED -> 1;
         case EXOTIC -> 5;
         case LEGENDARY -> 20;
         case MYTHICAL -> 0;
         default -> 0;
      };
   }
   
   public static int getFirstCraftXp(MagicRarity rarity){
      switch(rarity){
         case MUNDANE -> {return 5000;}
         case EMPOWERED -> {return 10000;}
         case EXOTIC -> {return 50000;}
         case LEGENDARY -> {return 100000;}
         case MYTHICAL -> {return 0;}
      }
      return 0;
   }
   
   public static int getCraftXp(MagicRarity rarity){
      switch(rarity){
         case MUNDANE -> {return 1000;}
         case EMPOWERED -> {return 5000;}
         case EXOTIC -> {return 10000;}
         case LEGENDARY -> {return 50000;}
         case MYTHICAL -> {return 0;}
      }
      return 0;
   }
   
   public static String getRarityLabel(MagicRarity rarity){
      return rarity.label;
   }
}
