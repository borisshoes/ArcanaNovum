package net.borisshoes.arcananovum.datastorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.borisshoes.borislib.utils.CodecUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class EnderCrateChannels {
   
   public static final Codec<EnderCrateChannels> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         EnderCrateChannel.CODEC.listOf().fieldOf("channels").forGetter(data -> new ArrayList<>(data.channels.values()))
   ).apply(instance, EnderCrateChannels::new));
   
   public static final DataKey<EnderCrateChannels> KEY = DataRegistry.register(DataKey.ofGlobal(Identifier.fromNamespaceAndPath(MOD_ID, "ender_crates"), CODEC,EnderCrateChannels::new));
   
   private final Map<ChannelKey, EnderCrateChannel> channels = new HashMap<>();
   
   public EnderCrateChannels(){}
   
   private EnderCrateChannels(List<EnderCrateChannel> channelList){
      for(EnderCrateChannel channel : channelList){
         ChannelKey key = ChannelKey.of(channel.getIdLock(), channel.getColors());
         channels.put(key, channel);
      }
   }
   
   public EnderCrateChannel getCrateChannel(@Nullable UUID lock, DyeColor... colors){
      ChannelKey key = ChannelKey.of(lock, colors);
      return channels.computeIfAbsent(key, k -> new EnderCrateChannel(lock, colors));
   }
   
   public static EnderCrateChannel getChannel(@Nullable UUID lock, DyeColor... colors){
      return DataAccess.getGlobal(KEY).getCrateChannel(lock,colors);
   }
   
   public static EnderCrateChannel getChannel(DyeColor... colors){
      return DataAccess.getGlobal(KEY).getCrateChannel(null,colors);
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
