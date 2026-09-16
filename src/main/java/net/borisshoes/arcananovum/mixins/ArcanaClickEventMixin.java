package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Function;

@Mixin(ClickEvent.class)
public interface ArcanaClickEventMixin {
   
   @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
   private static <A> Codec<ClickEvent> arcananovum$patchClickEventCodec(Codec<ClickEvent> instance, String typeKey, Function<ClickEvent, ? extends A> type, Function<? super A, ? extends MapCodec<ClickEvent>> codec, Operation<Codec<ClickEvent>> original){
      return original.call(instance, typeKey, type, codec).xmap(Function.identity(), clickEvent -> {
         PacketContext packetContext = PacketContext.get();
         if(packetContext != null && packetContext.get(PacketContext.CONNECTION) != null){
            if(clickEvent instanceof ClickEvent.RunCommand(String command) && command.startsWith("/arcana")){
               CompoundTag tag = new CompoundTag();
               tag.putString(ArcanaNovum.ARCANA_CLICK_KEY, command);
               return new ClickEvent.Custom(ArcanaNovum.ARCANA_CLICK_ACTION_ID, Optional.of(tag));
            }
         }
         return clickEvent;
      });
   }
}
