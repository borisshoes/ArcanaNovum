package net.borisshoes.arcananovum.events.special;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

public class ZeraiyaStartEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("zeraiya_start");
   
   private final ServerPlayer player;
   
   public ZeraiyaStartEvent(ServerPlayer player){
      super(ID, 6000);
      this.player = player;
   }
   
   public ServerPlayer getPlayer(){
      return player;
   }
   
   @Override
   public void onExpiry(){
      Component.literal("")
            .append(Component.literal("The presence fades and ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
            .append(Component.literal("Enderia's Egg").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
            .append(Component.literal(" stirs...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
      SoundUtils.playSongToPlayer(player, SoundEvents.ENDER_DRAGON_GROWL, 0.05f, 1.4f);
   }
   
   public void refresh(){
      this.timeAlive = 0;
   }
}
