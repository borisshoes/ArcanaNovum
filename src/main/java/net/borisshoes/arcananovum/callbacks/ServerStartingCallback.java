package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.server.MinecraftServer;

public class ServerStartingCallback {
   
   public static void serverStarting(MinecraftServer server){
      ArcanaNovum.SERVER = server;
   }
}
