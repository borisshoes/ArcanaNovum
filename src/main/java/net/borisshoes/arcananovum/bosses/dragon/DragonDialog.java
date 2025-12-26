package net.borisshoes.arcananovum.bosses.dragon;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DragonDialog {
   
   public static void announce(Announcements type, MinecraftServer server, @Nullable String extra){
      ArrayList<MutableComponent> mainMessage = new ArrayList<>();
      ArrayList<MutableComponent> subMessage1 = new ArrayList<>();
      ArrayList<MutableComponent> subMessage2 = new ArrayList<>();
      ArrayList<MutableComponent> subMessage3 = new ArrayList<>();
      ArrayList<MutableComponent> subMessage4 = new ArrayList<>();
      ArrayList<MutableComponent> subMessage5 = new ArrayList<>();
      ArrayList<MutableComponent> subMessage6 = new ArrayList<>();
      
      switch(type){
         case EVENT_PREP:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("=======================================").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("      The ").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal("Ender Dragon").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" Boss Fight Will Begin Shortly!").withStyle(ChatFormatting.LIGHT_PURPLE)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   * All players will be teleported to The End in ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal(extra).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE))
                  .append(Component.literal(". Gear up!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   * ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("Keep Inventory").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC, ChatFormatting.BOLD))
                  .append(Component.literal(" will be turned ON").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   * You get a free teleport back and brief invulnerability on death").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   * Stay near the highlighted raid leader for gear repairs and buffs!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("=======================================").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            
            subMessage1.add(Component.literal(""));
            subMessage1.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   So the ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("Players").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(" have re-awoken the old gateways? What do you say ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("Brother").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal(", shall we pay ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(" a visit?").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal(""));
            
            subMessage2.add(Component.literal(""));
            subMessage2.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   I have been waiting for this moment for eons! No longer will ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(" be allowed to hide away in her realm!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal(""));
            
            subMessage3.add(Component.literal(""));
            subMessage3.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage3.add(Component.literal("")
                  .append(Component.literal("   Players").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(", I have anchored the fate of your items to you. I encourage you to take advantage of my blessing.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage3.add(Component.literal(""));
            
            BorisLib.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            BorisLib.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage2); }));
            BorisLib.addTickTimerCallback(new GenericTimer(300, () ->{ announceHelper(server,subMessage3); }));
            break;
         case EVENT_START:
            mainMessage.add(Component.literal("")
                  .append(Component.literal("=======================================").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("      The ").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal("Ender Dragon").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" Boss Fight Has Begun!").withStyle(ChatFormatting.LIGHT_PURPLE)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" * You will be teleported to The End in 5 seconds!").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD, ChatFormatting.UNDERLINE)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("=======================================").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal(""));
            
            subMessage1.add(Component.literal(""));
            subMessage1.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   So you pitiful ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal("Players").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(" think you can usurp ME? Behold the Empress of The End, for I will be the last thing you see.").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal(""));
            
            BorisLib.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            
            break;
         case PHASE_ONE_START:
            mainMessage.add(Component.literal("")
                  .append(Component.literal("============== Phase One ==============").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   Take flight my Royal Guardians! Feast on their flesh!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            
            subMessage1.add(Component.literal(""));
            subMessage1.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   It appears as though the ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("Players").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(" have come to slay you, dear ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("Sister").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(". I do hope you haven't forgotten how to fight after all this time.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal(""));
            
            subMessage2.add(Component.literal(""));
            subMessage2.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   It isn't too late to change ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("Sister").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(". Join our cause and return to the path you started on long ago.").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal(""));
            
            subMessage3.add(Component.literal(""));
            subMessage3.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            subMessage3.add(Component.literal("")
                  .append(Component.literal("   Oh, of course ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal("you ").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("two").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal(" would be among them, they reek of your influence. These ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal("Players").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(" are mere ants under my wings!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            subMessage3.add(Component.literal(""));
            
            subMessage4.add(Component.literal(""));
            subMessage4.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage4.add(Component.literal("")
                  .append(Component.literal("   By all means... prove to me you aren't an indolent waste as you've led me to believe.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage4.add(Component.literal(""));
            
            BorisLib.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            BorisLib.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage2); }));
            BorisLib.addTickTimerCallback(new GenericTimer(360, () ->{ announceHelper(server,subMessage3); }));
            BorisLib.addTickTimerCallback(new GenericTimer(480, () ->{ announceHelper(server,subMessage4); }));
            break;
         case PHANTOM_DEATH:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("A ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal("Guardian Phantom").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(" has been slain!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case PHASE_TWO_START:
            mainMessage.add(Component.literal("")
                  .append(Component.literal("============== Phase Two ==============").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   Useless birds! No matter, my crystals still shield me!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case FIRST_CRYSTAL_DESTROYED:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("A ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("Crystal").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(" has been destroyed!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   But one of many... You cannot destroy them all!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            
            subMessage1.add(Component.literal(""));
            subMessage1.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   Look at what you have done to this place! I have read the ancient tomes... This land used to be flourishing with life!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal(""));
            
            subMessage2.add(Component.literal(""));
            subMessage2.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   You know ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal("nothing").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE))
                  .append(Component.literal(" of what it was like then, child!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal(""));
            
            BorisLib.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            BorisLib.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage1); }));
            break;
         case CRYSTAL_DESTROYED:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("A ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("Crystal").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(" has been destroyed!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case HALF_CRYSTALS_DESTROYED:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("A ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("Crystal").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(" has been destroyed!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   You meatbags grow troublesome! KILL THEM YOU INCOMPETENT WIZARDS!!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            
            subMessage1.add(Component.literal(""));
            subMessage1.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   The full power of a Progenitor at the tips of your talons and a whole ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("dimension").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE))
                  .append(Component.literal(" of souls in your heart and this is all you can muster?").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal(""));
            
            subMessage2.add(Component.literal(""));
            subMessage2.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   You CHOSE to be a warrior! I've always had to fight just to SURVIVE! And now you've led these BLOODTHIRSTY MONSTERS TO MY HOME!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal(""));
            
            BorisLib.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            BorisLib.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage1); }));
            break;
         case PHASE_THREE_START:
            mainMessage.add(Component.literal("")
                  .append(Component.literal("============== Phase Three ==============").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   RELENTLESS INSECTS!! I WILL VANQUISH YOU MYSELF!!!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            
            subMessage1.add(Component.literal(""));
            subMessage1.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   It is time for your scarred realm to heal from the wounds left by you and your predecessor.").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("\n    Players").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(", will you help me reclaim her corrupted crystal towers?").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal(""));
            
            subMessage2.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   You have no right to equate our actions! You know what Tenbrous did to me! I just want to live in PEACE!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal(""));
            
            BorisLib.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            BorisLib.addTickTimerCallback(new GenericTimer(300, () ->{ announceHelper(server,subMessage2); }));
            break;
         case DRAGON_HALF_HP:
            
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   AARGH!! THIS DIMENSION BELONGS TO ME! YOU ARE ALL UNWORTHY OF ITS POWER!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            
            subMessage1.add(Component.literal(""));
            subMessage1.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   We do not possess our realms. We are here to help them renew what has been lost.").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal(""));
            
            subMessage2.add(Component.literal(""));
            subMessage2.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   Haha... Behold the First Ascendant; Bane of Tenbrous! Oh, how far you've fallen!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal(""));
            
            subMessage3.add(Component.literal(""));
            subMessage3.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            subMessage3.add(Component.literal("")
                  .append(Component.literal("   Your mockery means nothing! Neither of you had to struggle like I did to gain our power! LEAVE ME BE!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            subMessage3.add(Component.literal(""));
            
            subMessage4.add(Component.literal(""));
            subMessage4.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage4.add(Component.literal("")
                  .append(Component.literal("   NO! This is what you deserve for your actions. Peace does not require subjugation and enslavement!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage3.add(Component.literal(""));
            
            BorisLib.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            BorisLib.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage2); }));
            BorisLib.addTickTimerCallback(new GenericTimer(360, () ->{ announceHelper(server,subMessage3); }));
            BorisLib.addTickTimerCallback(new GenericTimer(480, () ->{ announceHelper(server,subMessage4); }));
            break;
         case DRAGON_QUARTER_HP:
            
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   MY REIGN IS ETERNAL! I WILL DESTROY YOU ALL!!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            
            subMessage1.add(Component.literal(""));
            subMessage1.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   NUL...").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal(" EQUAYUS...").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("PLEASE! We shouldn't turn our backs on each other!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal(""));
            
            subMessage2.add(Component.literal(""));
            subMessage2.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   You turned your back on US! Your Ascension inspired us!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   You were the first to reject the Progenitors' ideology, and proved that things could change for the better!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal(""));
            
            subMessage3.add(Component.literal(""));
            subMessage3.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage3.add(Component.literal("")
                  .append(Component.literal("   When ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("Gaialtus").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal(" disappeared, you rose up and defeated ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("Tenbrous").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC))
                  .append(Component.literal("!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage3.add(Component.literal("")
                  .append(Component.literal("   Ceptyus").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal(" sealed themselves away out of fear!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage3.add(Component.literal("")
                  .append(Component.literal("   And ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("Brimsüth").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC))
                  .append(Component.literal(" took a ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("fuck").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC, ChatFormatting.OBFUSCATED))
                  .append(Component.literal("ing nap!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage3.add(Component.literal("")
                  .append(Component.literal("   So what do you do, as the only God remaining?").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage3.add(Component.literal(""));
            
            subMessage4.add(Component.literal(""));
            subMessage4.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            subMessage4.add(Component.literal("")
                  .append(Component.literal("   I...").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            subMessage4.add(Component.literal(""));
            
            subMessage5.add(Component.literal(""));
            subMessage5.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage5.add(Component.literal("")
                  .append(Component.literal("   You cowered in your realm, and became just like them!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage5.add(Component.literal(""));
            
            BorisLib.addTickTimerCallback(new GenericTimer(150, () ->{ announceHelper(server,subMessage1); }));
            BorisLib.addTickTimerCallback(new GenericTimer(300, () ->{ announceHelper(server,subMessage2); }));
            BorisLib.addTickTimerCallback(new GenericTimer(450, () ->{ announceHelper(server,subMessage3); }));
            BorisLib.addTickTimerCallback(new GenericTimer(700, () ->{ announceHelper(server,subMessage4); }));
            BorisLib.addTickTimerCallback(new GenericTimer(750, () ->{ announceHelper(server,subMessage5); }));
            break;
         case DRAGON_DEATH:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   NO! I AM THE GOD OF THIS REALM! I won't go back! ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal("...Nul...").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal(" please don...").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            
            subMessage1.add(Component.literal(""));
            subMessage1.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   I'm sorry ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("Sister").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(". You had your chance, and now we have ours. The ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("Players").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(" are our best shot at restoring this world. We'll see if they are worthy...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal(""));
            
            subMessage2.add(Component.literal(""));
            subMessage2.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   Once you are ready to heal, and if the ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("Players").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                  .append(Component.literal(" wish to give you a second chance, I will always be here to help you.").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal(""));
            
            BorisLib.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            BorisLib.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage2); }));
            break;
         case EVENT_END:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("==================================").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("      ").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal("The Empress ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.UNDERLINE))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                  .append(Component.literal(" Has Been Defeated!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.UNDERLINE)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("  Her burning body crackles in the sky as a lone egg falls atop her perch!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("  You have 'freed' The End from its tyrant and unlocked new lands to explore!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("==================================").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal(""));
            
            subMessage1.add(Component.literal(""));
            subMessage1.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   You all ARE worthy, right?").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal("")
                  .append(Component.literal("   ...We shall see...").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
            subMessage1.add(Component.literal(""));
            
            ItemStack wings = ArcanaRegistry.WINGS_OF_ENDERIA.getPrefItem();
            
            subMessage2.add(Component.literal(""));
            subMessage2.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   Nevertheless, your efforts will be rewarded. You will receive a portion of our ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal("Sister's").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(" Divine power.").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal("")
                  .append(Component.literal("   Take these ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(wings.getDisplayName())
                  .append(Component.literal(" and let them guide you to new heights!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            subMessage2.add(Component.literal(""));
            
            BorisLib.addTickTimerCallback(new GenericTimer(240, () -> announceHelper(server,subMessage1)));
            BorisLib.addTickTimerCallback(new GenericTimer(360, () -> announceHelper(server,subMessage2)));
            
            break;
         case PHASE_ONE_GOONS:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   Even the pests of this realm work against you!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case PHASE_TWO_GOONS:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   The Shulkers know what you do to their kind. They would rather die than become your backpacks!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case PHASE_THREE_GOONS:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   My subjects, repel these invaders from our land!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_DRACONIC_RESILIENCE:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   ... huff... Perhaps you aren't as feeble as I thought... hmph...").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_CORRUPT_ARCANA:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   Your reliance on your arcane trinkets will be your downfall!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_BOMBARDMENT:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   I RAIN DESTRUCTION FROM THE HEAVENS!!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_CONSCRIPT_ARMY:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   Your Empress needs reinforcements! Defend your homeland!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_DIMENSION_SHIFT:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   Players ").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
                  .append(Component.literal("get disorientated so easily.").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_GRAVITY_AMP:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   The skies are MINE alone!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_GRAVITY_LAPSE:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   Gravity is but one of MY gifts to this world... and it can be taken away!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_OBLITERATE_TOWER:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   You DARE use MY towers against ME?!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_OVERLOAD_CRYSTALS:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   Crystals! I lend you my divine strength!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_QUAKE:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   Don't lose your footing now. Pitiful bipedals.").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_STARFALL:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   Even the sky obeys ME! ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
         case ABILITY_TERRAIN_SHIFT:
            mainMessage.add(Component.literal(""));
            mainMessage.add(Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            mainMessage.add(Component.literal("")
                  .append(Component.literal("   This Island bends to MY will!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
            mainMessage.add(Component.literal(""));
            break;
      }
      
      announceHelper(server,mainMessage);
   }
   
   private static void announceHelper(MinecraftServer server, ArrayList<MutableComponent> message){
      for(MutableComponent msg : message){
         server.getPlayerList().broadcastSystemMessage(msg, false);
      }
   }
   
   public enum Announcements{
      EVENT_PREP,
      EVENT_START,
      PHASE_ONE_START,
      PHASE_TWO_START,
      PHASE_THREE_START,
      PHASE_ONE_GOONS,
      PHASE_TWO_GOONS,
      PHASE_THREE_GOONS,
      PHANTOM_DEATH,
      FIRST_CRYSTAL_DESTROYED,
      CRYSTAL_DESTROYED,
      HALF_CRYSTALS_DESTROYED,
      DRAGON_HALF_HP,
      DRAGON_QUARTER_HP,
      DRAGON_DEATH,
      ABILITY_OVERLOAD_CRYSTALS,
      ABILITY_BOMBARDMENT,
      ABILITY_OBLITERATE_TOWER,
      ABILITY_CONSCRIPT_ARMY,
      ABILITY_TERRAIN_SHIFT,
      ABILITY_GRAVITY_AMP,
      ABILITY_GRAVITY_LAPSE,
      ABILITY_DIMENSION_SHIFT,
      ABILITY_QUAKE,
      ABILITY_STARFALL,
      ABILITY_DRACONIC_RESILIENCE,
      ABILITY_CORRUPT_ARCANA,
      EVENT_END
   }
}
