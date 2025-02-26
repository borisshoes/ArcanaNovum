package net.borisshoes.arcananovum.bosses.dragon;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DragonDialog {
   
   public static void announce(Announcements type, MinecraftServer server, @Nullable String extra){
      ArrayList<MutableText> mainMessage = new ArrayList<>();
      ArrayList<MutableText> subMessage1 = new ArrayList<>();
      ArrayList<MutableText> subMessage2 = new ArrayList<>();
      ArrayList<MutableText> subMessage3 = new ArrayList<>();
      ArrayList<MutableText> subMessage4 = new ArrayList<>();
      ArrayList<MutableText> subMessage5 = new ArrayList<>();
      ArrayList<MutableText> subMessage6 = new ArrayList<>();
      
      switch(type){
         case EVENT_PREP:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("=======================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("      The ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("Ender Dragon").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" Boss Fight Will Begin Shortly!").formatted(Formatting.LIGHT_PURPLE)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   * All players will be teleported to The End in ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal(extra).formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC,Formatting.UNDERLINE))
                  .append(Text.literal(". Gear up!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   * ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal("Keep Inventory").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC,Formatting.BOLD))
                  .append(Text.literal(" will be turned ON").formatted(Formatting.AQUA,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   * You get a free teleport back and brief invulnerability on death").formatted(Formatting.AQUA,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   * Stay near the highlighted raid leader for gear repairs and buffs!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("=======================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            
            subMessage1.add(Text.literal(""));
            subMessage1.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   So the Players have re-awoken the old gateways? What do you say Brother, shall we pay Enderia a visit?").formatted(Formatting.AQUA,Formatting.ITALIC)));
            subMessage1.add(Text.literal(""));
            
            subMessage2.add(Text.literal(""));
            subMessage2.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   I have been waiting for this moment for eons! No longer will Enderia be allowed to hide away in her realm!").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage2.add(Text.literal(""));
            
            ArcanaNovum.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage2); }));
            break;
         case EVENT_START:
            mainMessage.add(Text.literal("")
                  .append(Text.literal("=======================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("      The ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("Ender Dragon").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" Boss Fight Has Begun!").formatted(Formatting.LIGHT_PURPLE)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" * You will be teleported to The End in 5 seconds!").formatted(Formatting.AQUA,Formatting.BOLD,Formatting.UNDERLINE)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("=======================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal(""));
            
            subMessage1.add(Text.literal(""));
            subMessage1.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   So you pitiful Players think you can usurp ME? Behold the Empress of The End, for I will be the last thing you see.").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            subMessage1.add(Text.literal(""));
            
            ArcanaNovum.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            
            break;
         case PHASE_ONE_START:
            mainMessage.add(Text.literal("")
                  .append(Text.literal("============== Phase One ==============").formatted(Formatting.AQUA,Formatting.BOLD)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   Take flight my Royal Guardians! Feast on their flesh!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            
            subMessage1.add(Text.literal(""));
            subMessage1.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   It appears as though the Players have come to slay you, dear Sister. I do hope you haven't forgotten how to fight after all this time.").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage1.add(Text.literal(""));
            
            subMessage2.add(Text.literal(""));
            subMessage2.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   It isn't too late to change Sister. Join our cause and return to the path you started on long ago.").formatted(Formatting.AQUA,Formatting.ITALIC)));
            subMessage2.add(Text.literal(""));
            
            subMessage3.add(Text.literal(""));
            subMessage3.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            subMessage3.add(Text.literal("")
                  .append(Text.literal("   Oh, of course ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))
                  .append(Text.literal("you ").formatted(Formatting.DARK_GRAY,Formatting.ITALIC))
                  .append(Text.literal("two").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal(" would be among them, they reek of your influence. These Players are mere ants under my wings!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            subMessage3.add(Text.literal(""));
            
            subMessage4.add(Text.literal(""));
            subMessage4.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage4.add(Text.literal("")
                  .append(Text.literal("   By all means... prove to me you aren't an indolent waste as you've led me to believe.").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage4.add(Text.literal(""));
            
            ArcanaNovum.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage2); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(360, () ->{ announceHelper(server,subMessage3); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(480, () ->{ announceHelper(server,subMessage4); }));
            break;
         case PHANTOM_DEATH:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("A ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))
                  .append(Text.literal("Guardian Phantom").formatted(Formatting.DARK_PURPLE,Formatting.BOLD,Formatting.ITALIC))
                  .append(Text.literal(" has been slain!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case PHASE_TWO_START:
            mainMessage.add(Text.literal("")
                  .append(Text.literal("============== Phase Two ==============").formatted(Formatting.AQUA,Formatting.BOLD)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   Useless birds! No matter, my crystals still shield me!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case FIRST_CRYSTAL_DESTROYED:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("A ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal("Crystal").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD,Formatting.ITALIC))
                  .append(Text.literal(" has been destroyed!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   But one of many... You cannot destroy them all!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            
            subMessage1.add(Text.literal(""));
            subMessage1.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   Look at what you have done to this place! I have read the ancient tomes... This land used to be flourishing with life!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            subMessage1.add(Text.literal(""));
            
            subMessage2.add(Text.literal(""));
            subMessage2.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   You know ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))
                  .append(Text.literal("nothing").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC,Formatting.UNDERLINE))
                  .append(Text.literal(" of what it was like then, child!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            subMessage2.add(Text.literal(""));
            
            ArcanaNovum.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage1); }));
            break;
         case CRYSTAL_DESTROYED:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("A ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal("Crystal").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD,Formatting.ITALIC))
                  .append(Text.literal(" has been destroyed!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case HALF_CRYSTALS_DESTROYED:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("A ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal("Crystal").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD,Formatting.ITALIC))
                  .append(Text.literal(" has been destroyed!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   You meatbags grow troublesome! KILL THEM YOU INCOMPETENT WIZARDS!!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            
            subMessage1.add(Text.literal(""));
            subMessage1.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   The full power of a Progenitor at the tips of your talons and a whole ").formatted(Formatting.GRAY,Formatting.ITALIC))
                  .append(Text.literal("dimension").formatted(Formatting.GRAY,Formatting.ITALIC, Formatting.UNDERLINE))
                  .append(Text.literal(" of souls in your heart and this is all you can muster?").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage1.add(Text.literal(""));
            
            subMessage2.add(Text.literal(""));
            subMessage2.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   You CHOSE to be a warrior! I've always had to fight just to SURVIVE! And now you've led these BLOODTHIRSTY MONSTERS TO MY HOME!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            subMessage2.add(Text.literal(""));
            
            ArcanaNovum.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage1); }));
            break;
         case PHASE_THREE_START:
            mainMessage.add(Text.literal("")
                  .append(Text.literal("============== Phase Three ==============").formatted(Formatting.AQUA,Formatting.BOLD)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   RELENTLESS INSECTS!! I WILL VANQUISH YOU MYSELF!!!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            
            subMessage1.add(Text.literal(""));
            subMessage1.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   It is time for your scarred realm to heal from the wounds left by your and your predecessor.").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal("\n    Players, will you help me reclaim her corrupted crystal towers?").formatted(Formatting.AQUA,Formatting.ITALIC)));
            subMessage1.add(Text.literal(""));
            
            subMessage2.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   You have no right to equate our actions! You know what Tenbrous did to me! I just want to live in PEACE!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            subMessage2.add(Text.literal(""));
            
            ArcanaNovum.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(300, () ->{ announceHelper(server,subMessage2); }));
            break;
         case DRAGON_HALF_HP:
            
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   AARGH!! THIS DIMENSION BELONGS TO ME! YOU ARE ALL UNWORTHY OF ITS POWER!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            
            subMessage1.add(Text.literal(""));
            subMessage1.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   We do not possess our realms. We are here to help them renew what has been lost.").formatted(Formatting.AQUA,Formatting.ITALIC)));
            subMessage1.add(Text.literal(""));
            
            subMessage2.add(Text.literal(""));
            subMessage2.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   Haha... Behold the First Ascendant; Bane of Tenbrous! Oh, how far you've fallen!").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage2.add(Text.literal(""));
            
            subMessage3.add(Text.literal(""));
            subMessage3.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            subMessage3.add(Text.literal("")
                  .append(Text.literal("   Your mockery means nothing! Neither of you had to struggle like I did to gain our power! LEAVE ME BE!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            subMessage3.add(Text.literal(""));
            
            subMessage4.add(Text.literal(""));
            subMessage4.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage4.add(Text.literal("")
                  .append(Text.literal("   NO! This is what you deserve for your actions. Peace does not require subjugation and enslavement!").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage3.add(Text.literal(""));
            
            ArcanaNovum.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage2); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(360, () ->{ announceHelper(server,subMessage3); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(480, () ->{ announceHelper(server,subMessage4); }));
            break;
         case DRAGON_QUARTER_HP:
            
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   MY REIGN IS ETERNAL! I WILL DESTROY YOU ALL!!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            
            subMessage1.add(Text.literal(""));
            subMessage1.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   NUL... EQUAYUS... PLEASE! We shouldn't turn our backs on each other!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            subMessage1.add(Text.literal(""));
            
            subMessage2.add(Text.literal(""));
            subMessage2.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   You turned your back on US! Your Ascension inspired us!").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   You were the first to reject the Progenitors' ideology, and proved that things could change for the better!").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage2.add(Text.literal(""));
            
            subMessage3.add(Text.literal(""));
            subMessage3.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage3.add(Text.literal("")
                  .append(Text.literal("   When ").formatted(Formatting.GRAY,Formatting.ITALIC))
                  .append(Text.literal("Gaialtus").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal(" disappeared, you rose up and defeated ").formatted(Formatting.GRAY,Formatting.ITALIC))
                  .append(Text.literal("Tenbrous").formatted(Formatting.DARK_GREEN,Formatting.ITALIC))
                  .append(Text.literal("!").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage3.add(Text.literal("")
                  .append(Text.literal("   Ceptyus").formatted(Formatting.DARK_AQUA,Formatting.ITALIC))
                  .append(Text.literal(" sealed themselves away out of fear!").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage3.add(Text.literal("")
                  .append(Text.literal("   And ").formatted(Formatting.GRAY,Formatting.ITALIC))
                  .append(Text.literal("Brimsüth").formatted(Formatting.RED,Formatting.ITALIC))
                  .append(Text.literal(" took a ").formatted(Formatting.GRAY,Formatting.ITALIC))
                  .append(Text.literal("fuck").formatted(Formatting.GRAY,Formatting.ITALIC,Formatting.OBFUSCATED))
                  .append(Text.literal("ing nap!").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage3.add(Text.literal("")
                  .append(Text.literal("   So what do you do, as the only God remaining?").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage3.add(Text.literal(""));
            
            subMessage4.add(Text.literal(""));
            subMessage4.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            subMessage4.add(Text.literal("")
                  .append(Text.literal("   I...").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            subMessage4.add(Text.literal(""));
            
            subMessage5.add(Text.literal(""));
            subMessage5.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage5.add(Text.literal("")
                  .append(Text.literal("   You cowered in your realm, and became just like them!").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage5.add(Text.literal(""));
            
            ArcanaNovum.addTickTimerCallback(new GenericTimer(150, () ->{ announceHelper(server,subMessage1); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(300, () ->{ announceHelper(server,subMessage2); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(450, () ->{ announceHelper(server,subMessage3); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(700, () ->{ announceHelper(server,subMessage4); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(750, () ->{ announceHelper(server,subMessage5); }));
            break;
         case DRAGON_DEATH:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   NO! I AM THE GOD OF THIS REALM! I won't go back! ...Nul... please don...").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            
            subMessage1.add(Text.literal(""));
            subMessage1.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   I'm sorry Sister. You had your chance, and now we have ours. The Players are our best shot at restoring this world. We'll see if they are worthy...").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage1.add(Text.literal(""));
            
            subMessage2.add(Text.literal(""));
            subMessage2.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   Once you are ready to heal, and if the Players wish to give you a second chance, I will always be here to help you.").formatted(Formatting.AQUA,Formatting.ITALIC)));
            subMessage2.add(Text.literal(""));
            
            ArcanaNovum.addTickTimerCallback(new GenericTimer(120, () ->{ announceHelper(server,subMessage1); }));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(240, () ->{ announceHelper(server,subMessage2); }));
            break;
         case EVENT_END:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("==================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("      ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("The Empress ").formatted(Formatting.LIGHT_PURPLE,Formatting.UNDERLINE))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD,Formatting.UNDERLINE))
                  .append(Text.literal(" Has Been Defeated!").formatted(Formatting.LIGHT_PURPLE,Formatting.UNDERLINE)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("  Her burning body crackles in the sky as a lone egg falls atop her perch!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("  You have 'freed' The End from its tyrant and unlocked new lands to explore!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("==================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal(""));
            
            subMessage1.add(Text.literal(""));
            subMessage1.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   You all ARE worthy, right?").formatted(Formatting.GRAY,Formatting.ITALIC)));
            subMessage1.add(Text.literal("")
                  .append(Text.literal("   ...We shall see...").formatted(Formatting.DARK_GRAY,Formatting.ITALIC)));
            subMessage1.add(Text.literal(""));
            
            ItemStack wings = ArcanaRegistry.WINGS_OF_ENDERIA.getPrefItem();
            
            subMessage2.add(Text.literal(""));
            subMessage2.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   Nevertheless, your efforts will be rewarded. You will receive a portion of our Sister's Divine power.").formatted(Formatting.AQUA,Formatting.ITALIC)));
            subMessage2.add(Text.literal("")
                  .append(Text.literal("   Take these ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(wings.toHoverableText())
                  .append(Text.literal(" and let them guide you to new heights!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            subMessage2.add(Text.literal(""));
            
            ArcanaNovum.addTickTimerCallback(new GenericTimer(240, () -> announceHelper(server,subMessage1)));
            ArcanaNovum.addTickTimerCallback(new GenericTimer(360, () -> announceHelper(server,subMessage2)));
            
            break;
         case PHASE_ONE_GOONS:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   Even the pests of this realm work against you!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case PHASE_TWO_GOONS:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   The Shulkers know what you do to their kind. They would rather die than become your backpacks!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case PHASE_THREE_GOONS:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   My subjects, repel these invaders from our land!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_DRACONIC_RESILIENCE:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   ... huff... Perhaps you arent as feeble as I thought... hmph...").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_CORRUPT_ARCANA:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   Your reliance on your arcane trinkets will be your downfall!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_BOMBARDMENT:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   I RAIN DESTRUCTION FROM THE HEAVENS!!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_CONSCRIPT_ARMY:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   Your Empress needs reinforcements! Defend your homeland!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_DIMENSION_SHIFT:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   Players get disorientated so easily.").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_GRAVITY_AMP:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   The skies are MINE alone!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_GRAVITY_LAPSE:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   Gravity is but one of MY gifts to this world... and it can be taken away!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_OBLITERATE_TOWER:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   You DARE use MY towers against ME?!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_OVERLOAD_CRYSTALS:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   Crystals! I lend you my divine strength!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_QUAKE:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   Don't lose your footing now. Pitiful bipedals.").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_STARFALL:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   Even the sky obeys ME! ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
         case ABILITY_TERRAIN_SHIFT:
            mainMessage.add(Text.literal(""));
            mainMessage.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            mainMessage.add(Text.literal("")
                  .append(Text.literal("   This Island bends to MY will!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            mainMessage.add(Text.literal(""));
            break;
      }
      
      announceHelper(server,mainMessage);
   }
   
   private static void announceHelper(MinecraftServer server, ArrayList<MutableText> message){
      for(MutableText msg : message){
         server.getPlayerManager().broadcast(msg, false);
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
