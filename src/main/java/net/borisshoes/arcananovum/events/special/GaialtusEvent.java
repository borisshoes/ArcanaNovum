package net.borisshoes.arcananovum.events.special;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

public class GaialtusEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("gaialtus_start");
   
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

   @Override
   public void tick(){
      // Once the credits start there is no time limit, the event just waits for [Look Around]
      if(stage < 3) super.tick();
   }

   public int getStage(){
      return stage;
   }

   // Called once per click. Leaving the overworld ends the conversation, up until the credits start
   public int checkStage(ServerPlayer clicker){
      if(stage < 3 && !clicker.level().equals(clicker.level().getServer().overworld())){
         markForRemoval();
         clicker.sendSystemMessage(Component.literal("\n")
               .append(Component.literal("The breeze fades...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)), false);
         SoundUtils.playSongToPlayer(clicker, SoundEvents.SOUL_ESCAPE.value(), 1f, 1);
         return -1;
      }
      return stage;
   }

   public void setStage(int stage){
      this.stage = stage;
   }
}
