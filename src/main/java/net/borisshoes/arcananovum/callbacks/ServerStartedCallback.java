package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.server.MinecraftServer;

public class ServerStartedCallback {
   
   public static void serverStarted(MinecraftServer server){
      ArcanaRegistry.onServerStarted(server);
   }
}
