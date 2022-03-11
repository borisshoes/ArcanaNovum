package net.borisshoes.arcananovum.utils;

public enum MagicRarity {
   NONE(0,"None"),
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
   
   public static int getRarityInt(MagicRarity rarity){
      return rarity.rarity;
   }
   
   public static int getConcentration(MagicRarity rarity){
      switch(rarity){
         case NONE -> {return 0;}
         case EMPOWERED -> {return 1;}
         case EXOTIC -> {return 5;}
         case LEGENDARY -> {return 20;}
         case MYTHICAL -> {return 0;}
      }
      return 0;
   }
   
   public static int getFirstCraftXp(MagicRarity rarity){
      switch(rarity){
         case NONE -> {return 1000;}
         case EMPOWERED -> {return 5000;}
         case EXOTIC -> {return 10000;}
         case LEGENDARY -> {return 25000;}
         case MYTHICAL -> {return 0;}
      }
      return 0;
   }
   
   public static int getCraftXp(MagicRarity rarity){
      switch(rarity){
         case NONE -> {return 100;}
         case EMPOWERED -> {return 1000;}
         case EXOTIC -> {return 5000;}
         case LEGENDARY -> {return 15000;}
         case MYTHICAL -> {return 0;}
      }
      return 0;
   }
   
   public static String getRarityLabel(MagicRarity rarity){
      return rarity.label;
   }
}
