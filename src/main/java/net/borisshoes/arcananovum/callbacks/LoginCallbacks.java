package net.borisshoes.arcananovum.callbacks;

import java.util.HashMap;

public class LoginCallbacks {
   private static HashMap<String, LoginCallback> registry = new HashMap<>();
   
   public static final LoginCallback SHIELD_OF_FORTITUDE = LoginCallbacks.register("shield_of_fortitude",new ShieldLoginCallback());
   public static final LoginCallback CONTINUUM_ANCHOR = LoginCallbacks.register("continuum_anchor",new AnchorTimeLoginCallback());
   public static final LoginCallback IGNEOUS_COLLIDER = LoginCallbacks.register("igneous_collider",new ColliderLoginCallback());
   public static final LoginCallback XP_LOGIN_CALLBACK = LoginCallbacks.register("xp_login_callback",new XPLoginCallback());
   public static final LoginCallback ACHIEVEMENT_LOGIN_CALLBACK = LoginCallbacks.register("achievement_login_callback",new AchievementLoginCallback());
   public static final LoginCallback MAX_HEALTH_LOGIN_CALLBACK = LoginCallbacks.register("max_health_login_callback",new MaxHealthLoginCallback());
   public static final LoginCallback TOTEM_OF_VENGEANCE_LOGIN_CALLBACK = LoginCallbacks.register("totem_of_vengeance_login_callback", new VengeanceTotemLoginCallback());
   public static final LoginCallback ITEM_RETURN_LOGIN_CALLBACK = LoginCallbacks.register("item_return_login_callback", new ItemReturnLoginCallback());
   
   private static LoginCallback register(String id, LoginCallback callback){
      registry.put(id,callback);
      return callback;
   }
   
   public static LoginCallback createCallback(String id){
      if(registry.containsKey(id)){
         return registry.get(id).makeNew();
      }else{
         return null;
      }
   }
}
