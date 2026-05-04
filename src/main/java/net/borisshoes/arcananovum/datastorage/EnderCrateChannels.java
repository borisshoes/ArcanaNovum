package net.borisshoes.arcananovum.datastorage;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItemContainer;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.borisshoes.borislib.datastorage.StorableData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnderCrateChannels implements StorableData {
   
   public static final DataKey<EnderCrateChannels> KEY = DataRegistry.register(DataKey.ofGlobal(ArcanaRegistry.arcanaId("ender_crates"), EnderCrateChannels::new));
   
   private final Map<ChannelKey, EnderCrateChannel> channels = new HashMap<>();
   private Runnable dirtyCallback = () -> {};
   
   public EnderCrateChannels(){
   }
   
   @Override
   public void setDirtyCallback(Runnable callback){
      this.dirtyCallback = callback == null ? () -> {} : callback;
      channels.values().forEach(this::attachChannelDirtyCallback);
   }
   
   @Override
   public void markDirty(){
      dirtyCallback.run();
   }
   
   @Override
   public void read(ValueInput view){
      this.channels.clear();
      view.listOrEmpty("channels", EnderCrateChannel.CODEC).forEach(channel -> {
         if(!channel.getInventory().isEmpty()){
            ChannelKey key = ChannelKey.of(channel.getIdLock(), channel.getColors());
            attachChannelDirtyCallback(channel);
            channels.put(key, channel);
         }
      });
   }
   
   @Override
   public void writeNbt(CompoundTag tag){
      ListTag channelsList = new ListTag();
      for(EnderCrateChannel channel : channels.values()){
         if(!channel.getInventory().isEmpty()){
            EnderCrateChannel.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, BorisLib.SERVER.registryAccess()), channel).result().ifPresent(channelsList::add);
         }
      }
      tag.put("channels", channelsList);
   }
   
   public List<ArcanaItemContainer> arcanaInventoriesForPlayer(UUID playerId){
      List<ArcanaItemContainer> containers = new ArrayList<>();
      for(EnderCrateChannel value : channels.values()){
         if(value.isLocked() && value.getIdLock().equals(playerId)){
            containers.add(value.getArcanaItemContainer(null));
         }
      }
      return containers;
   }
   
   public EnderCrateChannel getCrateChannel(@Nullable UUID lock, DyeColor... colors){
      ChannelKey key = ChannelKey.of(lock, colors);
      return channels.computeIfAbsent(key, k -> {
         EnderCrateChannel channel = new EnderCrateChannel(lock, colors);
         attachChannelDirtyCallback(channel);
         return channel;
      });
   }
   
   private void attachChannelDirtyCallback(EnderCrateChannel channel){
      channel.setDirtyCallback(this::markDirty);
   }
   
   public static EnderCrateChannel getChannel(@Nullable UUID lock, DyeColor... colors){
      return DataAccess.getGlobal(KEY).getCrateChannel(lock, colors);
   }
   
   public static EnderCrateChannel getChannel(DyeColor... colors){
      return DataAccess.getGlobal(KEY).getCrateChannel(null, colors);
   }
   
   private record ChannelKey(@Nullable UUID idLock, DyeColor[] colors) {
      ChannelKey{
         colors = normalizeColors(colors);
      }
      
      static ChannelKey of(@Nullable UUID idLock, DyeColor... colors){
         return new ChannelKey(idLock, colors);
      }
      
      private static DyeColor[] normalizeColors(DyeColor[] colors){
         DyeColor[] normalized = new DyeColor[9];
         for(int i = 0; i < 9; i++){
            normalized[i] = (i < colors.length) ? colors[i] : null;
         }
         return normalized;
      }
      
      @Override
      public boolean equals(Object o){
         if(this == o) return true;
         if(o == null || getClass() != o.getClass()) return false;
         ChannelKey that = (ChannelKey) o;
         return Arrays.equals(colors, that.colors) && Objects.equals(idLock, that.idLock);
      }
      
      @Override
      public int hashCode(){
         int result = Objects.hashCode(idLock);
         result = 31 * result + Arrays.hashCode(colors);
         return result;
      }
   }
}
