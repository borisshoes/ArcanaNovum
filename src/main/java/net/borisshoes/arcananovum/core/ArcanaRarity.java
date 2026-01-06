package net.borisshoes.arcananovum.core;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public enum ArcanaRarity {
   MUNDANE(0,"Mundane","mundane"),
   EMPOWERED(1,"Empowered","empowered"),
   EXOTIC(2,"Exotic","exotic"),
   SOVEREIGN(3,"Sovereign","sovereign"),
   DIVINE(4,"Divine","divine");
   
   public final String label;
   public final int rarity;
   public final String id;
   
   ArcanaRarity(int rarity, String label, String id){
      this.label = label;
      this.rarity = rarity;
      this.id = id;
   }
   
   public String getTranslationKey(){
      return "rarity."+MOD_ID+"."+this.id;
   }
   
   public static ArcanaRarity rarityFromInt(int value){
      for(ArcanaRarity arcanaRarity : ArcanaRarity.values()){
         if(arcanaRarity.rarity == value) return arcanaRarity;
      }
      return null;
   }
   
   public static Component getColoredLabel(ArcanaRarity rarity, boolean bold){
      MutableComponent text;
      if(rarity == null){
         text = Component.literal("None").withStyle(ChatFormatting.WHITE);
      }else{
         text = Component.translatableWithFallback(rarity.getTranslationKey(),rarity.label);
      }
      if(bold) text = text.withStyle(ChatFormatting.BOLD);
      if(rarity == null) return text;
   
      return switch(rarity){
         case MUNDANE -> text.withStyle(ChatFormatting.GRAY);
         case EMPOWERED -> text.withStyle(ChatFormatting.GREEN);
         case EXOTIC -> text.withStyle(ChatFormatting.AQUA);
         case SOVEREIGN -> text.withStyle(ChatFormatting.GOLD);
         case DIVINE -> text.withStyle(ChatFormatting.LIGHT_PURPLE);
      };
   }
   
   public static ChatFormatting getColor(ArcanaRarity rarity){
      if(rarity == null){
         return null;
      }
   
      return switch(rarity){
         case MUNDANE -> ChatFormatting.GRAY;
         case EMPOWERED -> ChatFormatting.GREEN;
         case EXOTIC -> ChatFormatting.AQUA;
         case SOVEREIGN -> ChatFormatting.GOLD;
         case DIVINE -> ChatFormatting.LIGHT_PURPLE;
      };
   }
   
   public static Item getColoredConcrete(ArcanaRarity rarity){
      if(rarity == null){
         return Items.BLACK_CONCRETE;
      }
   
      return switch(rarity){
         case MUNDANE -> Items.LIGHT_GRAY_CONCRETE;
         case EMPOWERED -> Items.LIME_CONCRETE;
         case EXOTIC -> Items.LIGHT_BLUE_CONCRETE;
         case SOVEREIGN -> Items.ORANGE_CONCRETE;
         case DIVINE -> Items.MAGENTA_CONCRETE;
      };
   }
   
   public static ArcanaItem getAugmentCatalyst(ArcanaRarity rarity){
      if(rarity == null){
         return null;
      }
   
      return switch(rarity){
         case MUNDANE -> ArcanaRegistry.MUNDANE_CATALYST;
         case EMPOWERED -> ArcanaRegistry.EMPOWERED_CATALYST;
         case EXOTIC -> ArcanaRegistry.EXOTIC_CATALYST;
         case SOVEREIGN -> ArcanaRegistry.SOVEREIGN_CATALYST;
         case DIVINE -> ArcanaRegistry.DIVINE_CATALYST;
      };
   }
   
   public static int getRarityInt(ArcanaRarity rarity){
      return rarity.rarity;
   }
   
   public static int getConcentration(ArcanaRarity rarity){
      return switch(rarity){
         case MUNDANE -> 0;
         case EMPOWERED -> 1;
         case EXOTIC -> 5;
         case SOVEREIGN -> 20;
         case DIVINE -> 0;
         default -> 0;
      };
   }
   
   public static int getFirstCraftXp(ArcanaRarity rarity){
      switch(rarity){
         case MUNDANE -> {return 5000;}
         case EMPOWERED -> {return 10000;}
         case EXOTIC -> {return 50000;}
         case SOVEREIGN -> {return 100000;}
         case DIVINE -> {return 0;}
      }
      return 0;
   }
   
   public static int getCraftXp(ArcanaRarity rarity){
      switch(rarity){
         case MUNDANE -> {return 1000;}
         case EMPOWERED -> {return 5000;}
         case EXOTIC -> {return 10000;}
         case SOVEREIGN -> {return 50000;}
         case DIVINE -> {return 0;}
      }
      return 0;
   }
   
   public static String getRarityLabel(ArcanaRarity rarity){
      return rarity.label;
   }
   
   public static Item getArcanePaper(ArcanaRarity rarity){
      return switch(rarity){
         case MUNDANE -> ArcanaRegistry.MUNDANE_ARCANE_PAPER;
         case EMPOWERED -> ArcanaRegistry.EMPOWERED_ARCANE_PAPER;
         case EXOTIC -> ArcanaRegistry.EXOTIC_ARCANE_PAPER;
         case SOVEREIGN -> ArcanaRegistry.SOVEREIGN_ARCANE_PAPER;
         case DIVINE -> ArcanaRegistry.DIVINE_ARCANE_PAPER;
         default -> ArcanaRegistry.MUNDANE_ARCANE_PAPER;
      };
   }
   
   public static ArcaneTomeGui.TomeFilter getTomeFilter(ArcanaRarity rarity){
      return switch(rarity){
         case MUNDANE -> ArcaneTomeGui.TomeFilter.MUNDANE;
         case EMPOWERED -> ArcaneTomeGui.TomeFilter.EMPOWERED;
         case EXOTIC -> ArcaneTomeGui.TomeFilter.EXOTIC;
         case SOVEREIGN -> ArcaneTomeGui.TomeFilter.SOVEREIGN;
         case DIVINE -> ArcaneTomeGui.TomeFilter.DIVINE;
         default -> ArcaneTomeGui.TomeFilter.NONE;
      };
   }
}
