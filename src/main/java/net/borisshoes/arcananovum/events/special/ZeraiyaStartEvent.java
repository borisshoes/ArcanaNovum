package net.borisshoes.arcananovum.events.special;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.utils.Dialog;
import net.borisshoes.arcananovum.utils.DialogHelper;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZeraiyaStartEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("zeraiya_start");
   
   private final ServerPlayer player;
   private final String voice;
   
   public ZeraiyaStartEvent(ServerPlayer player){
      super(ID, 6000);
      this.player = player;
      this.voice = player.getRandom().nextBoolean() ? "story" : "love";
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
   
   public void sendInquiryDialog(){
      MutableComponent intro1;
      MutableComponent intro2;
      MutableComponent line;
      Dialog.DialogSound ambient;
      Dialog.DialogSound talking;
      if(this.voice.equals("story")){
         intro1 = Component.literal("\n")
               .append(Component.literal("As ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               .append(Component.literal("Enderia's Egg").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
               .append(Component.literal(" quietly dwells in your pocket, you feel a ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               .append(Component.literal("warming fire").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
               .append(Component.literal(" heat your mind.\n").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
         intro2 = Component.literal("")
               .append(Component.literal("A warm voice inquires...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
         line = Component.literal("")
               .append(player.getDisplayName().copy())
               .append(Component.literal(", would you like to hear a story?").withStyle(ChatFormatting.RED));
         ambient = new Dialog.DialogSound(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS.value(), 2f, 2f);
         talking = new Dialog.DialogSound(SoundEvents.FIRECHARGE_USE, 0.35f, 0.5f);
      }else{
         intro1 = Component.literal("\n")
               .append(Component.literal("As ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               .append(Component.literal("Enderia's Egg").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
               .append(Component.literal(" quietly dwells in your pocket, you feel a ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               .append(Component.literal("wisp of ocean breeze").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
               .append(Component.literal(" blow through your mind.\n").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
         intro2 = Component.literal("")
               .append(Component.literal("A gentle voice inquires...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
         line = Component.literal("")
               .append(Component.literal("That egg you carry, ").withStyle(ChatFormatting.DARK_GREEN))
               .append(player.getDisplayName().copy())
               .append(Component.literal(", would you like to know where she came from?").withStyle(ChatFormatting.DARK_GREEN));
         ambient = new Dialog.DialogSound(SoundEvents.GEYSER_ERUPTION_ACTIVE, 2f, 0.5f);
         talking = new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1);
      }
      
      DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
            intro1,
            intro2,
            line,
            Component.literal("\n")
                  .append(Component.literal("[No]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_RED).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action z_no"))))
                  .append(Component.literal(" "))
                  .append(Component.literal("[Yes]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_GREEN).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action z_yes"))))
                  .append(Component.literal(" "))
                  .append(Component.literal("[Is it a sad story?]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_PURPLE).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action z_ask"))))
      )), new ArrayList<>(Arrays.asList(ambient, talking, talking, talking)), new int[]{0, 80, 60, 80}, 1, 1, 0), true);
   }
   
   public void sendYesDialog(){
      MutableComponent intro;
      MutableComponent conclusion;
      Dialog.DialogSound ambient;
      Dialog.DialogSound talking;
      if(this.voice.equals("story")){
         intro = Component.literal("\n")
               .append(Component.literal("Wonderful! Our story begins with a ").withStyle(ChatFormatting.RED))
               .append(Component.literal("young dragon girl").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.literal(" a long time ago...").withStyle(ChatFormatting.RED));
         conclusion = Component.literal("")
               .append(Component.literal("I hope hers is a story that helps you understand the choice she faced, the choice we all face.").withStyle(ChatFormatting.RED))
               .append(Component.literal("\nAnd now both your stories enter a new chapter... How exciting!").withStyle(ChatFormatting.RED));
         ambient = new Dialog.DialogSound(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS.value(), 2f, 2f);
         talking = new Dialog.DialogSound(SoundEvents.FIRECHARGE_USE, 0.35f, 0.5f);
      }else {
         intro = Component.literal("\n")
               .append(Component.literal("This is how a ").withStyle(ChatFormatting.DARK_GREEN))
               .append(Component.literal("young dragon girl").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.literal(" lost her way a long time ago...").withStyle(ChatFormatting.DARK_GREEN));
         conclusion = Component.literal("")
               .append(Component.literal("No one is born with venom in their veins, darkness is always chosen.").withStyle(ChatFormatting.DARK_GREEN));
         ambient = new Dialog.DialogSound(SoundEvents.GEYSER_ERUPTION_ACTIVE, 2f, 0.5f);
         talking = new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1);
      }
      
      DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
            intro,
            Component.literal("\n")
                  .append(Component.literal("[Listen More]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_AQUA).withClickEvent(new ClickEvent.OpenUrl(URI.create("https://docs.google.com/document/d/1LONwFaFzXycvnQ5u4IliQmAcJfQzC9MUnWX04puoHQw/edit?usp=sharing"))))),
            Component.literal("\n")
                  .append(Component.literal("As the story ends you see at your feet a ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("pitch black spear").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal(".").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
            conclusion,
            Component.literal("")
                  .append(Component.literal("The presence fades and ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("Enderia's Egg").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(" stirs...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
      )), new ArrayList<>(Arrays.asList(ambient, talking, ambient, talking,
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.05f, 1.4f))
      ), new int[]{0, 60, 200, 80, 80}, 1, 1, 0), true);
   }
   
   public void sendMaybeDialog(){
      MutableComponent line;
      Dialog.DialogSound ambient;
      Dialog.DialogSound talking;
      if(this.voice.equals("story")){
         line = Component.literal("\n")
               .append(Component.literal("It is a tale of a fight against ").withStyle(ChatFormatting.RED))
               .append(Component.literal("darkness").withStyle(ChatFormatting.DARK_GRAY))
               .append(Component.literal(", one that is both ").withStyle(ChatFormatting.RED))
               .append(Component.literal("won").withStyle(ChatFormatting.GOLD))
               .append(Component.literal(" and ").withStyle(ChatFormatting.RED))
               .append(Component.literal("lost").withStyle(ChatFormatting.GOLD))
               .append(Component.literal(".").withStyle(ChatFormatting.RED));
         ambient = new Dialog.DialogSound(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS.value(), 2f, 2f);
         talking = new Dialog.DialogSound(SoundEvents.FIRECHARGE_USE, 0.35f, 0.5f);
      }else {
         line = Component.literal("\n")
               .append(Component.literal("The luminance of life often casts a ").withStyle(ChatFormatting.DARK_GREEN))
               .append(Component.literal("dark shadow").withStyle(ChatFormatting.DARK_GRAY))
               .append(Component.literal(". Not everyone has learned to live in the ").withStyle(ChatFormatting.DARK_GREEN))
               .append(Component.literal("light").withStyle(ChatFormatting.WHITE))
               .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN));
         ambient = new Dialog.DialogSound(SoundEvents.GEYSER_ERUPTION_ACTIVE, 2f, 0.5f);
         talking = new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1);
      }
      
      DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
            line,
            Component.literal("\n")
                  .append(Component.literal("[I don't like sad stories]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_RED).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action z_no"))))
                  .append(Component.literal(" "))
                  .append(Component.literal("[I'll listen]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_GREEN).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action z_yes"))))
      )), new ArrayList<>(Arrays.asList(ambient, talking)), new int[]{0, 80}, 1, 1, 0), true);
   }
}
