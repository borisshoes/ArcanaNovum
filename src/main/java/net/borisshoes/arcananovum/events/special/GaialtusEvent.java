package net.borisshoes.arcananovum.events.special;

import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class GaialtusEvent extends Event {
   public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID,"ceptyus_start");
   
   private final ServerPlayer player;
   private int stage;
   
   public GaialtusEvent(ServerPlayer player){
      super(ID, 12000);
      this.player = player;
      this.stage = 0;
   }
   
   public ServerPlayer getPlayer(){
      return player;
   }
   
   @Override
   public void onExpiry(){
   
   }
   
   public int getStage(){
      if(!player.level().equals(player.level().getServer().overworld())){
         markForRemoval();
         player.sendSystemMessage(Component.literal("\n")
               .append(Component.literal("The breeze fades...").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC)),false);
         SoundUtils.playSongToPlayer(player, SoundEvents.SOUL_ESCAPE.value(), 1f,1);
         return -1;
      }
      return stage;
   }
   
   public void setStage(int stage){
      this.stage = stage;
   }
}
