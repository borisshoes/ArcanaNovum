package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.networking.BlockPacketUtil;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGatewayPortalBlock;
import net.borisshoes.borislib.BorisLib;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(BlockPacketUtil.class)
public class BlockPacketUtilMixin {
   
   @Inject(method = "sendFromPacket", at = @At(value = "INVOKE", target = "Ljava/util/Collection;contains(Ljava/lang/Object;)Z"))
   private static void arcananovum$polymerPortalFix(Packet<?> packet, ServerGamePacketListenerImpl handler, CallbackInfo ci, @Local(name = "blockState") BlockState state){
      if(state.getBlock() instanceof AstralGatewayPortalBlock block){
         block.onPolymerBlockSend(state, ((ClientboundBlockUpdatePacket) packet).getPos().mutable(), PacketContext.create(handler));
      }
   }
}
