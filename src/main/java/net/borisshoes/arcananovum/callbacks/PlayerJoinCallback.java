package net.borisshoes.arcananovum.callbacks;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;

import static net.borisshoes.arcananovum.cardinalcomponents.LoginCallbackComponentInitializer.LOGIN_CALLBACK_LIST;

public class PlayerJoinCallback {
   public static void onPlayerJoin(ServerPlayNetworkHandler netHandler, PacketSender sender, MinecraftServer server){
      ServerPlayerEntity player = netHandler.player;
      //log(player.getEntityName()+" has joined the game");
      
      ArrayList<LoginCallback> toBeRemoved = new ArrayList<>();
      for(LoginCallback callback : LOGIN_CALLBACK_LIST.get(server.getWorld(ServerWorld.OVERWORLD)).getCallbacks()){
         if(callback.getPlayer().equals(player.getUuidAsString())){
            //log("Running login callback for "+player.getEntityName()+". ID: "+callback.getId());
            callback.onLogin(netHandler,server);
            toBeRemoved.add(callback);
         }
      }
      for(LoginCallback callback :toBeRemoved){
         LOGIN_CALLBACK_LIST.get(server.getWorld(ServerWorld.OVERWORLD)).removeCallback(callback);
      }
   }
}
