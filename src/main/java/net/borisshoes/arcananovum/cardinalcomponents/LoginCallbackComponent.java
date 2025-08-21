package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.callbacks.LoginCallback;
import net.borisshoes.arcananovum.callbacks.LoginCallbacks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

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
   public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup){
      try{
         callbacks.clear();
         NbtList callbacksTag = tag.getListOrEmpty("Callbacks");
         for (NbtElement e : callbacksTag){
            NbtCompound callbackTag = (NbtCompound) e;
            String playerUUID = callbackTag.getString("uuid", "");
            String callbackId = callbackTag.getString("id", "");
            LoginCallback callback = LoginCallbacks.createCallback(callbackId);
            if(callback == null) continue;
            callback.setData(callbackTag.getCompoundOrEmpty("data"), registryLookup);
            callback.setPlayer(playerUUID);
            callbacks.add(callback);
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup){
      try{
         NbtList callbacksTag = new NbtList();
         for(LoginCallback callback : callbacks){
            NbtCompound callbackTag = new NbtCompound();
            NbtCompound dataTag = callback.getData(registryLookup);
            callbackTag.putString("uuid",callback.getPlayer());
            callbackTag.putString("id",callback.getId());
            callbackTag.put("data",dataTag);
            callbacksTag.add(callbackTag);
         }
         tag.put("Callbacks",callbacksTag);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
