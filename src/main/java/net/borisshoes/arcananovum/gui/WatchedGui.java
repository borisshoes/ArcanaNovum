package net.borisshoes.arcananovum.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

import static net.borisshoes.arcananovum.Arcananovum.OPEN_GUIS;

public interface WatchedGui {
   public BlockEntity getBlockEntity();
   
   public void close();
   
   public SimpleGui getGui();
   
   public default boolean tryOpen(ServerPlayerEntity player){
      boolean inUseByOther = false;
      for(Map.Entry<ServerPlayerEntity, WatchedGui> entry : OPEN_GUIS.entrySet()){
         if(entry.getValue().getBlockEntity().getPos().equals(getBlockEntity().getPos()) && !player.equals(entry.getKey())){
            inUseByOther = true;
            break;
         }
      }
      
      if(inUseByOther){
         return false;
      }else{
         OPEN_GUIS.put(player,this);
         getGui().open();
         return true;
      }
   }
   
   public static boolean guiInUse(BlockPos pos){
      for(Map.Entry<ServerPlayerEntity, WatchedGui> entry : OPEN_GUIS.entrySet()){
         if(entry.getValue().getBlockEntity().getPos().equals(pos)) return true;
      }
      return false;
   }
}
