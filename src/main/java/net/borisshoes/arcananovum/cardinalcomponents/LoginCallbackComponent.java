package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.callbacks.LoginCallback;
import net.borisshoes.arcananovum.callbacks.LoginCallbacks;
import net.borisshoes.arcananovum.utils.CodecUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.ArrayList;
import java.util.List;

public class LoginCallbackComponent implements ILoginCallbackComponent{
   public final List<LoginCallback> callbacks = new ArrayList<>();
   
   @Override
   public List<LoginCallback> getCallbacks(){
      return callbacks;
   }
   
   @Override
   public boolean addCallback(LoginCallback callback){
      if(callbacks.contains(callback)) return false;
      for(LoginCallback loginCallback : callbacks){
         if(callback.getId().equals(loginCallback.getId()) && callback.getPlayer().equals(loginCallback.getPlayer())){
            if(loginCallback.combineCallbacks(callback)){
               return true;
            }
         }
      }
      return callbacks.add(callback);
   }
   
   @Override
   public boolean removeCallback(LoginCallback callback){
      if(!callbacks.contains(callback)) return false;
      return callbacks.remove(callback);
   }
   
   @Override
   public void readData(ReadView readView){
      try{
         callbacks.clear();
         List<NbtCompound> callbackTags = readView.read("Callbacks", CodecUtils.COMPOUND_LIST).orElse(new ArrayList<>());
         for (NbtCompound callbackTag : callbackTags){
            String playerUUID = callbackTag.getString("uuid", "");
            String callbackId = callbackTag.getString("id", "");
            LoginCallback callback = LoginCallbacks.createCallback(callbackId);
            if(callback == null) continue;
            callback.setData(callbackTag.getCompoundOrEmpty("data"));
            callback.setPlayer(playerUUID);
            callbacks.add(callback);
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeData(WriteView writeView){
      try{
         ArrayList<NbtCompound> callbackTags = new ArrayList<>();
         for(LoginCallback callback : callbacks){
            NbtCompound callbackTag = new NbtCompound();
            NbtCompound dataTag = callback.getData();
            callbackTag.putString("uuid",callback.getPlayer());
            callbackTag.putString("id",callback.getId());
            callbackTag.put("data",dataTag);
            callbackTags.add(callbackTag);
         }
         writeView.put("Callbacks", CodecUtils.COMPOUND_LIST,callbackTags);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
