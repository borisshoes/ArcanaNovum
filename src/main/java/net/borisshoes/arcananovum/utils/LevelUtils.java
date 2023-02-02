package net.borisshoes.arcananovum.utils;

import java.text.DecimalFormat;

public class LevelUtils {
   
   public static int levelFromXp(int xp){
      return (int) Math.min(100,Math.floor((-1000 + Math.sqrt(4000*((double)xp)+9000000))/2000));
   }
   
   public static int levelToTotalXp(int lvl){
      return 1000*(lvl*lvl+lvl-2);
   }
   
   public static int xpToNextLevel(int xp){
      return levelToTotalXp(levelFromXp(xp)+1) - xp;
   }
   
   public static int concFromLevel(int lvl){
      if(lvl < 1){
         return 0;
      }else if(lvl <= 5){
         return (int)Math.floor(8*Math.sqrt(lvl-1)+5);
      }else if(lvl <= 50){
         return (int)Math.floor(2*lvl+11);
      }else{
         return (int)Math.floor(37.95*Math.sqrt(lvl+40)-249);
      }
   }
   
   public static int getLevelSkillPoints(int level){
      return level*3;
   } // 3 Skill points per level
   
   public static String readableInt(int num){
      return String.format("%,d", num);
   }
   
   public static int concFromXp(int xp){
      return concFromLevel(levelFromXp(xp));
   }
   
   public static int getCurLevelXp(int xp){
      return xp-levelToTotalXp(levelFromXp(xp));
   }
   
   public static int nextLevelNewXp(int lvl){
      return levelToTotalXp(lvl+1) - levelToTotalXp(lvl);
   }
   
   public static int vanillaXpToLevel(int xp){
      if(xp<0)
         return 0;
      if(xp <= 352){
         return (int)(Math.sqrt(xp+9)-3);
      }else if(xp <= 1507){
         return (int)((81.0/10.0)+Math.sqrt(0.4*(xp-(7839.0/40.0))));
      }else{
         return (int)((325.0/18.0)+Math.sqrt((2.0/9.0)*(xp-(54215.0/72.0))));
      }
   }
   
   public static int vanillaLevelToTotalXp(int lvl){
      if(lvl < 0){
         return 0;
      }else if(lvl <= 16){
         return (lvl*lvl) + 6*lvl;
      }else if(lvl <= 31){
         return (int) (2.5*(lvl*lvl) - 40.5*lvl + 360);
      }else{
         return (int) (4.5*(lvl*lvl) - 162.5*lvl + 2220);
      }
   }
   
}
