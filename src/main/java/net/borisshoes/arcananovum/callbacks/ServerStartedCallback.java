package net.borisshoes.arcananovum.callbacks;

import com.mojang.authlib.GameProfile;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.cardinalcomponents.DataFixer;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.borisshoes.arcananovum.ArcanaNovum.log;

public class ServerStartedCallback {
   
   public static void serverStarted(MinecraftServer server){
      ArcanaRegistry.onServerStarted(server);
      DataFixer.onServerStarted(server);
   }
}
