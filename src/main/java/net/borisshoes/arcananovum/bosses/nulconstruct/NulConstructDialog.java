package net.borisshoes.arcananovum.bosses.nulconstruct;

import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class NulConstructDialog {
   
   public static void abilityText(PlayerEntity summoner, WitherEntity wither, String text){
      List<ServerPlayerEntity> playersInRange = wither.getWorld().getNonSpectatingEntities(ServerPlayerEntity.class, wither.getBoundingBox().expand(50.0));
      if(summoner instanceof ServerPlayerEntity player && !playersInRange.contains(player)) playersInRange.add(player);
      for(ServerPlayerEntity inRange : playersInRange){
         inRange.sendMessage(Text.literal(text).formatted(Formatting.DARK_GRAY,Formatting.ITALIC),true);
      }
   }
   
   public static void announce(MinecraftServer server, PlayerEntity summoner, WitherEntity wither, NulConstructDialog.Announcements type){
      announce(server,summoner,wither,type,new boolean[]{});
   }
   
   public static void announce(MinecraftServer server, PlayerEntity summoner, WitherEntity wither, NulConstructDialog.Announcements type, boolean[] args){
      ArrayList<MutableText> message = new ArrayList<>();
   
      int[] weights;
      int choice;
   
      switch(type){
         case SUMMON_TEXT -> {
            message.add(Text.literal("")
                  .append(Text.literal("You Feel ").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC))
                  .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC, Formatting.BOLD))
                  .append(Text.literal(" Flow Into The ").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC))
                  .append(Text.literal("Construct").formatted(Formatting.GRAY, Formatting.ITALIC)));
            message.add(Text.literal("")
                  .append(Text.literal("A ").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC))
                  .append(Text.literal("Dark Presence").formatted(Formatting.DARK_GRAY, Formatting.ITALIC, Formatting.BOLD))
                  .append(Text.literal(" Looms...").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC)));
            message.add(Text.literal(""));
         }
         case SUMMON_DIALOG -> {
            weights = new int[]{3, 3, 3, 3, 3, 1, args[1] ? 10 : 0, args[0] ? 10 : 0, args[0] ? 10 : 0};
            choice = getWeightedResult(weights);
            switch(choice){
               case 0 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Mortal").formatted(Formatting.GOLD))
                        .append(Text.literal(" knocks on the door of the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal("? They know not what they are toying with...").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 1 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("Those unworthy of ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("knowledge").formatted(Formatting.BLUE))
                        .append(Text.literal(" shall be reduced to ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("nothing").formatted(Formatting.GRAY))
                        .append(Text.literal("...").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 2 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Mortals").formatted(Formatting.GOLD))
                        .append(Text.literal(" grow bolder by the minute. Perhaps they need to be put in their place.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 3 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("A ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Mortal").formatted(Formatting.GOLD))
                        .append(Text.literal(" seeks to harness ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal("? Let them try...").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 4 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("Of all the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Gods").formatted(Formatting.AQUA, Formatting.BOLD))
                        .append(Text.literal(" to call upon, you disturb me? You must be ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("ignorant").formatted(Formatting.DARK_RED))
                        .append(Text.literal(" of my domain, or ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("arrogant").formatted(Formatting.DARK_RED))
                        .append(Text.literal(" enough to tempt ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Death").formatted(Formatting.GRAY))
                        .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 5 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("I am the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("God of ").formatted(Formatting.AQUA, Formatting.BOLD))
                        .append(Text.literal("Death").formatted(Formatting.GRAY, Formatting.BOLD))
                        .append(Text.literal(" and ").formatted(Formatting.AQUA, Formatting.BOLD))
                        .append(Text.literal("Knowledge").formatted(Formatting.BLUE, Formatting.BOLD))
                        .append(Text.literal(". If my ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Construct").formatted(Formatting.GRAY))
                        .append(Text.literal(" does not give you the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("former").formatted(Formatting.GRAY))
                        .append(Text.literal(", you shall earn the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("latter").formatted(Formatting.BLUE))
                        .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 6 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("So you have banished ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                        .append(Text.literal(" and now knock on my door? You seek to challenge the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("God of ").formatted(Formatting.AQUA, Formatting.BOLD))
                        .append(Text.literal("Death").formatted(Formatting.GRAY, Formatting.BOLD))
                        .append(Text.literal("?").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 7 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("You have tasted the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal(" and want more? Lets hope your ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("greed").formatted(Formatting.GOLD))
                        .append(Text.literal(" is not your downfall.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 8 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("Just because you already carry the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal(" does not mean you are ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("entitled").formatted(Formatting.GOLD))
                        .append(Text.literal(" to more.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
            }
         }
         case SUCCESS -> {
            weights = new int[]{3, 3, 3, 3, 3, 1, args[1] ? 10 : 0, args[0] ? 10 : 0, args[2] ? -1 : 0};
            choice = getWeightedResult(weights);
            switch(choice){
               case 0 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("You have impressed me ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Mortal").formatted(Formatting.GOLD))
                        .append(Text.literal(", you have earned a taste of my ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal(" power.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 1 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("Impressive, I have imbued your ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Catalyst").formatted(Formatting.GOLD))
                        .append(Text.literal(" with ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal(", I'm curious as to how you'll use it.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 2 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("You have defeated my ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Construct").formatted(Formatting.GRAY))
                        .append(Text.literal(", no easy feat. Gather what ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal(" remains for your ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Catalyst").formatted(Formatting.GOLD)));
                  message.add(Text.literal(""));
               }
               case 3 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("Death").formatted(Formatting.GRAY))
                        .append(Text.literal(" does not come for you today, I shall grant you what you have sought.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 4 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Catalytic Matrix").formatted(Formatting.GOLD))
                        .append(Text.literal(" of yours is a quaint toy, lets see if you can handle a taste of ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("true power").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 5 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("A valiant fight! ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                        .append(Text.literal(" must be getting nervous. Perhaps you have paid her a visit already?").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 6 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("I can see how you defeated ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                        .append(Text.literal(", however I am not so ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("weak").formatted(Formatting.DARK_RED))
                        .append(Text.literal(". Be thankful I only sent a ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Construct").formatted(Formatting.GRAY))
                        .append(Text.literal(" to greet you.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 7 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("It seems you are worthy enough to add another piece of the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal(" to your collection.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 8 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("You are unlike any I have seen before. Perhaps you are worthy of my ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("guidance").formatted(Formatting.BLUE))
                        .append(Text.literal(". This ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Memento").formatted(Formatting.BLACK, Formatting.BOLD))
                        .append(Text.literal(" shall be my gift to you.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
            }
         }
         case FAILURE -> {
            weights = new int[]{5, 3, 3, 3, 3, 1, 1, args[1] ? 20 : 0, args[0] ? 20 : 0};
            choice = getWeightedResult(weights);
            switch(choice){
               case 0 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("Another arrogant ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Mortal").formatted(Formatting.GOLD))
                        .append(Text.literal(", not worthy of my time.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 1 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("Such a simple ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Construct").formatted(Formatting.GRAY))
                        .append(Text.literal(" defeated you? You are not worthy of the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 2 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("Such a small sample of ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine Power").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal(" overwhelmed you? How did you plan on ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("harnessing").formatted(Formatting.BLUE))
                        .append(Text.literal(" it in the first place?").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 3 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("An expected result from calling upon the ").formatted(Formatting.GRAY))
                        .append(Text.literal("God of ").formatted(Formatting.AQUA, Formatting.BOLD))
                        .append(Text.literal("Death").formatted(Formatting.GRAY, Formatting.BOLD))
                        .append(Text.literal(". Do not waste my time again.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 4 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("Arrogant enough to tempt ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Death").formatted(Formatting.GRAY))
                        .append(Text.literal("... I can't fathom how you expected to win.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 5 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("There is ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("knowledge").formatted(Formatting.BLUE))
                        .append(Text.literal(" in failure, but only if you have the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("wisdom").formatted(Formatting.AQUA))
                        .append(Text.literal(" to find it.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 6 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("death").formatted(Formatting.GRAY))
                        .append(Text.literal(" is a mercy. Do not be ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("foolish").formatted(Formatting.DARK_RED))
                        .append(Text.literal(" enough to find out why.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 7 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("A ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("weakling").formatted(Formatting.DARK_RED))
                        .append(Text.literal(" like you defeated ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                        .append(Text.literal("? She must have gotten ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("indolent").formatted(Formatting.DARK_RED))
                        .append(Text.literal(" in her isolation.").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
               case 8 -> {
                  message.add(Text.literal("")
                        .append(Text.literal("Whatever petty tricks got you the ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("Divine").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal(" in the past won't work on me, ").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal("knowledge").formatted(Formatting.BLUE))
                        .append(Text.literal(" must be earned!").formatted(Formatting.DARK_GRAY)));
                  message.add(Text.literal(""));
               }
            }
         }
      }
      List<ServerPlayerEntity> playersInRange = wither.getWorld().getNonSpectatingEntities(ServerPlayerEntity.class, wither.getBoundingBox().expand(50.0));
      if(summoner instanceof ServerPlayerEntity player && !playersInRange.contains(player)) playersInRange.add(player);
      
      for(MutableText msg : message){
         if(type != Announcements.FAILURE){
            for(ServerPlayerEntity playerInRange : playersInRange){
               playerInRange.sendMessage(msg, false);
            }
         }else if(summoner != null){
            summoner.sendMessage(msg,false);
         }
      }
   }
   
   private static int getWeightedResult(int[] weights){
      ArrayList<Integer> pool = new ArrayList<>();
   
      for(int i = 0; i < weights.length; i++){
         if(weights[i] == -1) return i;
         for(int j = 0; j < weights[i]; j++){
            pool.add(i);
         }
      }
      
      return pool.get((int) (Math.random()*pool.size()));
   }
   
   public enum Announcements{
      SUMMON_TEXT,
      SUMMON_DIALOG,
      SUCCESS,
      FAILURE
   }
}
