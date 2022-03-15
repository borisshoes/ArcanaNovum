package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.borisshoes.arcananovum.callbacks.LoginCallback;

import java.util.List;

public interface ILoginCallbackComponent extends ComponentV3 {
   List<LoginCallback> getCallbacks();
   boolean addCallback(LoginCallback callback);
   boolean removeCallback(LoginCallback callback);
}
