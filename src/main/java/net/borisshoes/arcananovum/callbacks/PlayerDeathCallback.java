package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.Timer;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;

public class PlayerDeathCallback {
   
   public static void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive){
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(newPlayer.getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null) return;
      if(bossFight.getLeft() == BossFights.DRAGON){
         DragonBossFight.playerDied(newPlayer);
         Arcananovum.addTickTimerCallback(new GenericTimer(20, new TimerTask() {
            @Override
            public void run(){
               // Give teleport option
   
               MutableText deathMsg1 = Text.literal("")
                     .append(Text.literal("You have ").formatted(Formatting.AQUA))
                     .append(Text.literal("died").formatted(Formatting.RED, Formatting.BOLD, Formatting.ITALIC))
                     .append(Text.literal("!").formatted(Formatting.AQUA));
               MutableText deathMsg2 = Text.literal("")
                     .append(Text.literal("Click ").formatted(Formatting.AQUA))
                     .append(Text.literal("[Here]").styled(s ->
                           s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arcana boss teleport"))
                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to Teleport!")))
                                 .withColor(Formatting.LIGHT_PURPLE).withBold(true)))
                     .append(Text.literal(" to Teleport back to the fight!").formatted(Formatting.AQUA));
   
               newPlayer.sendMessage(deathMsg1, false);
               newPlayer.sendMessage(deathMsg2, false);
            }
         }));
      }
   }
}
