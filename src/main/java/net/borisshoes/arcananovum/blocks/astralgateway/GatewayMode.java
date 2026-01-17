package net.borisshoes.arcananovum.blocks.astralgateway;

import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

public enum GatewayMode implements StringRepresentable {
   SEND_ONLY,
   RECEIVE_ONLY,
   BOTH;
   
   @Override
   public @NonNull String getSerializedName(){
      return name().toLowerCase(Locale.ROOT);
   }
}