package net.borisshoes.arcananovum.callbacks;

import java.util.HashMap;

public class LoginCallbacks {
   public static HashMap<String, LoginCallback> registry = new HashMap<>();
   
   public static final LoginCallback SHIELD_OF_FORTITUDE = LoginCallbacks.register("shield_of_fortitude",new ShieldLoginCallback());
   public static final LoginCallback CONTINUUM_ANCHOR = LoginCallbacks.register("continuum_anchor",new AnchorTimeLoginCallback());
   public static final LoginCallback IGNEOUS_COLLIDER = LoginCallbacks.register("igneous_collider",new ColliderLoginCallback());
   public static final LoginCallback XP_LOGIN_CALLBACK = LoginCallbacks.register("xp_login_callback",new XPLoginCallback());
   public static final LoginCallback ACHIEVEMENT_LOGIN_CALLBACK = LoginCallbacks.register("achievement_login_callback",new AchievementLoginCallback());
   public static final LoginCallback MAX_HEALTH_LOGIN_CALLBACK = LoginCallbacks.register("max_health_login_callback",new MaxHealthLoginCallback());
   
   private static LoginCallback register(String id, LoginCallback callback){
      registry.put(id,callback);
      return callback;
   }
}
