package net.borisshoes.arcananovum.blocks.astralgateway;

import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

public enum GatewayState implements StringRepresentable {
   WARMUP,
   LOCKED_OPEN,
   OPEN,
   COOLDOWN,
   CLOSED;
   
   @Override
   public @NonNull String getSerializedName(){
      return name().toLowerCase(Locale.ROOT);
   }
}
