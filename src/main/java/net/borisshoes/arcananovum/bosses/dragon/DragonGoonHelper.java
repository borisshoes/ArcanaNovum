package net.borisshoes.arcananovum.bosses.dragon;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.entities.DragonPhantomEntity;
import net.borisshoes.arcananovum.entities.DragonWizardEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

public class DragonGoonHelper {
   
   
   public static DragonPhantomEntity makeGuardianPhantom(ServerLevel endWorld, int numPlayers){
      DragonPhantomEntity guardian = new DragonPhantomEntity(ArcanaRegistry.DRAGON_PHANTOM_ENTITY, endWorld);
      MutableComponent phantomName = Component.literal("")
            .append(Component.literal("-").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("=").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("-").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" "))
            .append(Component.literal("Guardian Phantom").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
            .append(Component.literal(" "))
            .append(Component.literal("-").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("=").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("-").withStyle(ChatFormatting.LIGHT_PURPLE));
      guardian.setNumPlayers(numPlayers);
      guardian.setCustomName(phantomName);
      guardian.setCustomNameVisible(true);
      guardian.setPosRaw(endWorld.getRandom().nextDouble()*50-25,100,endWorld.getRandom().nextDouble()*50-25);
      return guardian;
   }
   
   public static DragonWizardEntity makeWizard(ServerLevel endWorld, int numPlayers){
      DragonWizardEntity wizard = new DragonWizardEntity(ArcanaRegistry.DRAGON_WIZARD_ENTITY,endWorld);
      MutableComponent wizardName = Component.literal("")
            .append(Component.literal("~").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" "))
            .append(Component.literal("Crystal Defender").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
            .append(Component.literal(" "))
            .append(Component.literal("~").withStyle(ChatFormatting.DARK_AQUA));
      wizard.setNumPlayers(numPlayers);
      wizard.setCustomName(wizardName);
      wizard.setCustomNameVisible(true);
      return wizard;
   }
   
   
}
