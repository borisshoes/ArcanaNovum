package net.borisshoes.arcananovum.bosses.dragon;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.entities.DragonPhantomEntity;
import net.borisshoes.arcananovum.entities.DragonWizardEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DragonGoonHelper {
   
   
   public static DragonPhantomEntity makeGuardianPhantom(ServerWorld endWorld, int numPlayers){
      DragonPhantomEntity guardian = new DragonPhantomEntity(ArcanaRegistry.DRAGON_PHANTOM_ENTITY, endWorld);
      MutableText phantomName = Text.literal("")
            .append(Text.literal("-").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("=").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("-").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" "))
            .append(Text.literal("Guardian Phantom").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD, Formatting.UNDERLINE))
            .append(Text.literal(" "))
            .append(Text.literal("-").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("=").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("-").formatted(Formatting.LIGHT_PURPLE));
      guardian.setNumPlayers(numPlayers);
      guardian.setCustomName(phantomName);
      guardian.setCustomNameVisible(true);
      guardian.setPos(Math.random()*50-25,100,Math.random()*50-25);
      return guardian;
   }
   
   public static DragonWizardEntity makeWizard(ServerWorld endWorld, int numPlayers){
      DragonWizardEntity wizard = new DragonWizardEntity(ArcanaRegistry.DRAGON_WIZARD_ENTITY,endWorld);
      MutableText wizardName = Text.literal("")
            .append(Text.literal("~").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" "))
            .append(Text.literal("Crystal Defender").formatted(Formatting.AQUA, Formatting.BOLD, Formatting.UNDERLINE))
            .append(Text.literal(" "))
            .append(Text.literal("~").formatted(Formatting.DARK_AQUA));
      wizard.setNumPlayers(numPlayers);
      wizard.setCustomName(wizardName);
      wizard.setCustomNameVisible(true);
      return wizard;
   }
   
   
}
