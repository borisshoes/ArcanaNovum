package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class PackAwareBlockModel extends BlockModel {
   
   @Override
   public boolean startWatching(ServerGamePacketListenerImpl player){
      if(PolymerResourcePackUtils.hasMainPack(player)){
         return super.startWatching(player);
      }else{
         return false;
      }
   }
}
