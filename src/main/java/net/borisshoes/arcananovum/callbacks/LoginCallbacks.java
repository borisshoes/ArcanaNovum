package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.items.*;

import java.util.HashMap;

public class LoginCallbacks {
   public static HashMap<String, LoginCallback> registry = new HashMap<>();
   
   public static final LoginCallback SHIELD_OF_FORTITUDE = LoginCallbacks.register("shield_of_fortitude",new ShieldLoginCallback());
   
   private static LoginCallback register(String id, LoginCallback callback){
      registry.put(id,callback);
      return callback;
   }
}
