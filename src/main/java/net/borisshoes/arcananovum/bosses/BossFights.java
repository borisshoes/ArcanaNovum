package net.borisshoes.arcananovum.bosses;

public enum BossFights {
   DRAGON("dragon"),
   WARDEN("warden");
   
   public final String label;
   
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
