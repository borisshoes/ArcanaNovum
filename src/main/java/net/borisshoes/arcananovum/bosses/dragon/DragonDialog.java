package net.borisshoes.arcananovum.bosses.dragon;

import net.borisshoes.arcananovum.items.core.MagicItems;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DragonDialog {
   
   public static void announce(Announcements type, MinecraftServer server, @Nullable String extra){
      ServerWorld endWorld = server.getWorld(World.END);
      ArrayList<MutableText> message = new ArrayList<>();
      
      switch(type){
         case EVENT_PREP:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("=======================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("      The ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("Ender Dragon").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" Boss Fight Will Begin Shortly!").formatted(Formatting.LIGHT_PURPLE)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("   * All players will be teleported to The End in ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal(extra).formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC,Formatting.UNDERLINE))
                  .append(Text.literal(". Gear up!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("   * ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal("Keep Inventory").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC,Formatting.BOLD))
                  .append(Text.literal(" will be turned ON").formatted(Formatting.AQUA,Formatting.ITALIC)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("   * You get a free teleport back and brief invulnerability on death").formatted(Formatting.AQUA,Formatting.ITALIC)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("   * Stay near the highlighted raid leader for gear repairs and buffs!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("=======================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            break;
         case EVENT_START:
            message.add(Text.literal("")
                  .append(Text.literal("=======================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("      The ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("Ender Dragon").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" Boss Fight Has Begun!").formatted(Formatting.LIGHT_PURPLE)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" * You will be teleported to The End in 5 seconds!").formatted(Formatting.AQUA,Formatting.BOLD,Formatting.UNDERLINE)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("=======================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal(""));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   So you pitiful mortals think you can usurp ME? Behold the Empress of The End, for I will be the last thing you see.").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case PHASE_ONE_START:
            message.add(Text.literal("")
                  .append(Text.literal("============== Phase One ==============").formatted(Formatting.AQUA,Formatting.BOLD)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   Take flight my Royal Guardians! Feast on their flesh!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case PHANTOM_DEATH:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("A ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))
                  .append(Text.literal("Guardian Phantom").formatted(Formatting.DARK_PURPLE,Formatting.BOLD,Formatting.ITALIC))
                  .append(Text.literal(" has been slain!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case PHASE_TWO_START:
            message.add(Text.literal("")
                  .append(Text.literal("============== Phase Two ==============").formatted(Formatting.AQUA,Formatting.BOLD)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   Useless birds! No matter, my crystals still shield me!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case FIRST_CRYSTAL_DESTROYED:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("A ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal("Crystal").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD,Formatting.ITALIC))
                  .append(Text.literal(" has been destroyed!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   But one of many... You cannot destroy them all!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case CRYSTAL_DESTROYED:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("A ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal("Crystal").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD,Formatting.ITALIC))
                  .append(Text.literal(" has been destroyed!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case HALF_CRYSTALS_DESTROYED:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("A ").formatted(Formatting.AQUA,Formatting.ITALIC))
                  .append(Text.literal("Crystal").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD,Formatting.ITALIC))
                  .append(Text.literal(" has been destroyed!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   You meatbags grow troublesome! KILL THEM YOU INCOMPETENT WIZARDS!!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case PHASE_THREE_START:
            message.add(Text.literal("")
                  .append(Text.literal("============== Phase Three ==============").formatted(Formatting.AQUA,Formatting.BOLD)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   RELENTLESS INSECTS!! I WILL VANQUISH YOU MYSELF!!!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case DRAGON_HALF_HP:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   AARGH!! THIS DIMENSION BELONGS TO ME! YOU ARE ALL UNWORTHY OF ITS POWER!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case DRAGON_QUARTER_HP:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   MY REIGN IS ETERNAL! I WILL DESTROY YOU ALL!!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case DRAGON_DEATH:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   NO! I AM THE GOD OF THIS REALM! I won't go back! ...please don...").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case EVENT_END:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("==================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("      ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("The Empress ").formatted(Formatting.LIGHT_PURPLE,Formatting.UNDERLINE))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD,Formatting.UNDERLINE))
                  .append(Text.literal(" Has Been Slain!").formatted(Formatting.LIGHT_PURPLE,Formatting.UNDERLINE)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("  Her burning body crackles in the sky as a lone egg falls atop her perch!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("  You have freed The End from its dictator and unlocked new lands to explore!").formatted(Formatting.AQUA,Formatting.ITALIC)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("==================================").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal("A reward is due!").formatted(Formatting.GREEN, Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("You brave heroes shall receive the first ").formatted(Formatting.YELLOW))
                  .append(Text.literal("Mythical").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" Magic Item!").formatted(Formatting.YELLOW)));
            message.add(Text.literal(""));
   
            ItemStack wings = MagicItems.WINGS_OF_ZEPHYR.getPrefItem();
            MutableText wingText = Text.literal("[Armored Wings of Zephyr]").styled(s ->
                  s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(wings)))
                        .withColor(Formatting.GRAY).withBold(true).withUnderline(true)
            );
   
            message.add(Text.literal("")
                  .append(Text.literal("Take these ").formatted(Formatting.YELLOW))
                  .append(wingText)
                  .append(Text.literal(" and let them guide you to new heights!").formatted(Formatting.YELLOW)));
            break;
         case PHASE_ONE_GOONS:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   Even the pests of this realm work against you!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case PHASE_TWO_GOONS:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   The Shulkers know what you do to their kind. They would rather die than become your backpacks!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case PHASE_THREE_GOONS:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   My children, repel these invaders from our land!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_DRACONIC_RESILIENCE:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   ... huff... Perhaps you arent as feeble as I thought... hmph...").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_BOMBARDMENT:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   I RAIN DESTRUCTION FROM THE HEAVENS!!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_CONSCRIPT_ARMY:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   Your Empress needs reinforcements! Defend your homeland!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_DIMENSION_SHIFT:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   Humans get disorientated so easily.").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_GRAVITY_AMP:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   The skies are MINE alone!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_GRAVITY_LAPSE:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   Gravity is but one of MY gifts to this world... and it can be taken away!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_OBLITERATE_TOWER:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   You DARE use MY towers against ME?!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_OVERLOAD_CRYSTALS:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   Crystals! I lend you my divine strength!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_QUAKE:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   Dont lose your footing now. Pitiful bipedals.").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_STARFALL:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   Even the stars obey ME! ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
         case ABILITY_TERRAIN_SHIFT:
            message.add(Text.literal(""));
            message.add(Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD)));
            message.add(Text.literal("")
                  .append(Text.literal("   This Island bends to MY will!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)));
            message.add(Text.literal(""));
            break;
      }
   
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
      EVENT_END
   }
}
