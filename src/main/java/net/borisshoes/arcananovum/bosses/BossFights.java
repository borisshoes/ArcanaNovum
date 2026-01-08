package net.borisshoes.arcananovum.bosses;

import com.mojang.serialization.Codec;

public enum BossFights {
   DRAGON("dragon"),
   WARDEN("warden");
   
   public final String label;
   
   public static final Codec<BossFights> BOSS_FIGHTS_CODEC = Codec.STRING.xmap(
         BossFights::fromLabel,
         BossFights::getLabel
   );
   
   BossFights(String label){
      this.label = label;
   }
   
   public static String getLabel(BossFights boss){
      return boss.label;
   }
   
   public static BossFights fromLabel(String id){
      if(id.equals("dragon")){
         return DRAGON;
      }else if(id.equals("warden")){
         return WARDEN;
      }else{
         return null;
      }
   }
}
