package net.borisshoes.arcananovum.callbacks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.borisshoes.arcananovum.ArcanaCommands;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.borisshoes.arcananovum.ArcanaNovum.DEV_MODE;
import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.*;
import static net.minecraft.commands.arguments.IdentifierArgument.id;

public class CommandRegisterCallback {
   public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment){
      dispatcher.register(literal("arcana")
            .then(literal("create").requires(Permissions.require(MOD_ID + ".create", PermissionLevel.GAMEMASTERS))
                  .then(argument("id", string()).suggests(ArcanaCommands::getItemSuggestions)
                        .executes(ctx -> ArcanaCommands.createItem(ctx, getString(ctx, "id")))
                        .then(argument("targets", players()).requires(Permissions.require(MOD_ID + ".create.others", PermissionLevel.GAMEMASTERS))
                              .executes(ctx -> ArcanaCommands.createItems(ctx, getString(ctx, "id"), getPlayers(ctx, "targets"))))))
            .then(literal("cache").requires(Permissions.require(MOD_ID + ".cache", PermissionLevel.GAMEMASTERS)).executes(ArcanaCommands::cacheCommand))
            .then(literal("reload").requires(Permissions.require(MOD_ID + ".reload", PermissionLevel.GAMEMASTERS)).executes(ArcanaCommands::reloadCommand))
            .then(literal("version").requires(Permissions.require(MOD_ID + ".version", PermissionLevel.ALL)).executes(ArcanaCommands::versionCommand))
            .then(literal("items").requires(Permissions.require(MOD_ID + ".items", PermissionLevel.ALL)).executes(ArcanaCommands::itemsCommand))
            .then(literal("help").requires(Permissions.require(MOD_ID + ".help", PermissionLevel.ALL)).executes(ArcanaCommands::openGuideBook))
            .then(literal("guide").requires(Permissions.require(MOD_ID + ".help", PermissionLevel.ALL)).executes(ArcanaCommands::openGuideBook))
            .then(literal("show").requires(Permissions.require(MOD_ID + ".show", PermissionLevel.ALL)).executes(ArcanaCommands::showItem))
            .then(literal("blocks").requires(Permissions.require(MOD_ID + ".blocks", PermissionLevel.ALL)).executes(ArcanaCommands::placedBlocks)
                  .then(argument("player", player()).requires(Permissions.require(MOD_ID + ".blocks.others", PermissionLevel.GAMEMASTERS))
                        .executes(context -> ArcanaCommands.placedBlocks(context, getPlayer(context, "player")))))
            .then(literal("uuids").requires(Permissions.require(MOD_ID + ".uuids", PermissionLevel.GAMEMASTERS))
                  .then(argument("player", player()).executes(context -> ArcanaCommands.uuidCommand(context, getPlayer(context, "player")))))
            .then(literal("infuse").requires(Permissions.require(MOD_ID + ".infuse", PermissionLevel.GAMEMASTERS))
                  .then((argument("percentage", doubleArg())
                        .executes(context -> ArcanaCommands.enhanceCommand(context, getDouble(context, "percentage"), null)))
                        .then(argument("target", player()).requires(Permissions.require(MOD_ID + ".infuse.others", PermissionLevel.GAMEMASTERS))
                              .executes(context -> ArcanaCommands.enhanceCommand(context, getDouble(context, "percentage"), getPlayer(context, "target"))))))
            .then(literal("xp").requires(Permissions.require(MOD_ID + ".xp", PermissionLevel.GAMEMASTERS))
                  .then(literal("add").requires(Permissions.require(MOD_ID + ".xp.add", PermissionLevel.GAMEMASTERS))
                        .then(argument("targets", players())
                              .then(((argument("amount", integer())
                                    .executes(context -> ArcanaCommands.xpCommand(context, getPlayers(context, "targets"), getInteger(context, "amount"), false, true)))
                                    .then(literal("points")
                                          .executes(context -> ArcanaCommands.xpCommand(context, getPlayers(context, "targets"), getInteger(context, "amount"), false, true))))
                                    .then(literal("levels")
                                          .executes(context -> ArcanaCommands.xpCommand(context, getPlayers(context, "targets"), getInteger(context, "amount"), false, false))))))
                  .then(literal("set").requires(Permissions.require(MOD_ID + ".xp.set", PermissionLevel.GAMEMASTERS))
                        .then(argument("targets", players()).then(((argument("amount", integer(0))
                              .executes(context -> ArcanaCommands.xpCommand(context, getPlayers(context, "targets"), getInteger(context, "amount"), true, true)))
                              .then(literal("points")
                                    .executes(context -> ArcanaCommands.xpCommand(context, getPlayers(context, "targets"), getInteger(context, "amount"), true, true))))
                              .then(literal("levels")
                                    .executes(context -> ArcanaCommands.xpCommand(context, getPlayers(context, "targets"), getInteger(context, "amount"), true, false))))))
                  .then(literal("query").requires(Permissions.require(MOD_ID + ".xp.query", PermissionLevel.GAMEMASTERS))
                        .then(argument("target", player())
                              .executes(context -> ArcanaCommands.xpCommandQuery(context, getPlayer(context, "target"))))))
            .then(literal("skillpoints").requires(Permissions.require(MOD_ID + ".skillpoints", PermissionLevel.GAMEMASTERS))
                  .then(literal("add").requires(Permissions.require(MOD_ID + ".skillpoints.add", PermissionLevel.GAMEMASTERS))
                        .then(argument("targets", players())
                              .then(argument("amount", integer())
                                    .executes(context -> ArcanaCommands.skillpointsCommand(context, getPlayers(context, "targets"), getInteger(context, "amount"), false)))))
                  .then(literal("set").requires(Permissions.require(MOD_ID + ".skillpoints.set", PermissionLevel.GAMEMASTERS))
                        .then(argument("targets", players()).then(argument("amount", integer())
                              .executes(context -> ArcanaCommands.skillpointsCommand(context, getPlayers(context, "targets"), getInteger(context, "amount"), true)))))
                  .then(literal("query").requires(Permissions.require(MOD_ID + ".skillpoints.query", PermissionLevel.GAMEMASTERS))
                        .then(argument("target", player())
                              .executes(context -> ArcanaCommands.skillpointsCommandQuery(context, getPlayer(context, "target"))))))
            .then(literal("achievement").requires(Permissions.require(MOD_ID + ".achievement", PermissionLevel.GAMEMASTERS))
                  .then(literal("grant").requires(Permissions.require(MOD_ID + ".achievement.grant", PermissionLevel.GAMEMASTERS))
                        .then(argument("id", string()).suggests(ArcanaCommands::getAchievementSuggestions)
                              .then(argument("targets", players())
                                    .executes(context -> ArcanaCommands.setAchievement(context, getString(context, "id"), true, getPlayers(context, "targets"))))))
                  .then(literal("revoke").requires(Permissions.require(MOD_ID + ".achievement.revoke", PermissionLevel.GAMEMASTERS))
                        .then(argument("id", string()).suggests(ArcanaCommands::getAchievementSuggestions)
                              .then(argument("targets", players())
                                    .executes(context -> ArcanaCommands.setAchievement(context, getString(context, "id"), false, getPlayers(context, "targets"))))))
                  .then(literal("query").requires(Permissions.require(MOD_ID + ".achievement.query", PermissionLevel.GAMEMASTERS))
                        .then(argument("id", string()).suggests(ArcanaCommands::getAchievementSuggestions)
                              .then(argument("target", player())
                                    .executes(context -> ArcanaCommands.getAchievement(context, getString(context, "id"), getPlayer(context, "target")))))))
            .then(literal("research").requires(Permissions.require(MOD_ID + ".research", PermissionLevel.GAMEMASTERS))
                  .then(literal("grant").requires(Permissions.require(MOD_ID + ".research.grant", PermissionLevel.GAMEMASTERS))
                        .then(argument("id", string()).suggests(ArcanaCommands::getResearchSuggestions)
                              .then(argument("targets", players())
                                    .executes(context -> ArcanaCommands.setResearch(context, getString(context, "id"), true, getPlayers(context, "targets"))))))
                  .then(literal("revoke").requires(Permissions.require(MOD_ID + ".research.revoke", PermissionLevel.GAMEMASTERS))
                        .then(argument("id", string()).suggests(ArcanaCommands::getResearchSuggestions)
                              .then(argument("targets", players())
                                    .executes(context -> ArcanaCommands.setResearch(context, getString(context, "id"), false, getPlayers(context, "targets"))))))
                  .then(literal("query").requires(Permissions.require(MOD_ID + ".research.query", PermissionLevel.GAMEMASTERS))
                        .then(argument("id", string()).suggests(ArcanaCommands::getResearchSuggestions)
                              .then(argument("target", player())
                                    .executes(context -> ArcanaCommands.getResearch(context, getString(context, "id"), getPlayer(context, "target")))))))
            .then(literal("augment").requires(Permissions.require(MOD_ID + ".augment", PermissionLevel.GAMEMASTERS))
                  .then(literal("apply").requires(Permissions.require(MOD_ID + ".augment.apply", PermissionLevel.GAMEMASTERS))
                        .then(argument("id", string()).suggests(ArcanaCommands::getAugmentSuggestions)
                              .then((argument("level", integer())
                                    .executes(context -> ArcanaCommands.applyAugment(context, getString(context, "id"), getInteger(context, "level"), null)))
                                    .then(argument("target", player()).requires(Permissions.require(MOD_ID + ".augment.apply.others", PermissionLevel.GAMEMASTERS))
                                          .executes(context -> ArcanaCommands.applyAugment(context, getString(context, "id"), getInteger(context, "level"), getPlayer(context, "target")))))))
                  .then(literal("setlevel").requires(Permissions.require(MOD_ID + ".augment.setlevel", PermissionLevel.GAMEMASTERS))
                        .then(argument("id", string()).suggests(ArcanaCommands::getAugmentSuggestions)
                              .then((argument("level", integer())
                                    .executes(context -> ArcanaCommands.setAugment(context, getString(context, "id"), getInteger(context, "level"), null)))
                                    .then(argument("target", player()).requires(Permissions.require(MOD_ID + ".augment.setlevel.others", PermissionLevel.GAMEMASTERS))
                                          .executes(context -> ArcanaCommands.setAugment(context, getString(context, "id"), getInteger(context, "level"), getPlayer(context, "target"))))))))
            .then(literal("changeCrafter").requires(Permissions.require(MOD_ID + ".changecrafter", PermissionLevel.GAMEMASTERS))
                  .then(argument("username", word()).suggests(MinecraftUtils::getPlayerSuggestions)
                        .then(literal("crafted").executes(context -> ArcanaCommands.changeCrafter(context, getString(context, "username"), 0)))
                        .then(literal("synthesized").executes(context -> ArcanaCommands.changeCrafter(context, getString(context, "username"), 1)))
                        .then(literal("earned").executes(context -> ArcanaCommands.changeCrafter(context, getString(context, "username"), 3)))
                        .then(literal("found").executes(context -> ArcanaCommands.changeCrafter(context, getString(context, "username"), 2)))))
            .then(literal("changeSkin").requires(Permissions.require(MOD_ID + ".changeskin", PermissionLevel.GAMEMASTERS))
                  .then(argument("skin", id()).suggests(CommandRegisterCallback::getSkinSuggestions)
                        .executes(context -> ArcanaCommands.changeSkin(context, String.valueOf(IdentifierArgument.getId(context, "skin"))))))
            .then(literal("boss").requires(Permissions.require(MOD_ID + ".boss", PermissionLevel.ALL))
                  .then(literal("start").requires(Permissions.require(MOD_ID + ".boss.start", PermissionLevel.GAMEMASTERS))
                        .then(literal("dragon").executes(ArcanaCommands::startDragonBoss)))
                  .then(literal("resetAbilities").requires(Permissions.require(MOD_ID + ".boss.resetabilities", PermissionLevel.GAMEMASTERS))
                        .then(argument("doAbility", bool()).executes(context -> ArcanaCommands.bossResetAbilities(context, getBool(context, "doAbility")))))
                  .then(literal("forceLairAction").requires(Permissions.require(MOD_ID + ".boss.forcelairaction", PermissionLevel.GAMEMASTERS)).executes(ArcanaCommands::bossForceLairAction))
                  .then(literal("abort").requires(Permissions.require(MOD_ID + ".boss.abort", PermissionLevel.GAMEMASTERS)).executes(ArcanaCommands::abortBoss))
                  .then(literal("clean").requires(Permissions.require(MOD_ID + ".boss.clean", PermissionLevel.GAMEMASTERS)).executes(ArcanaCommands::cleanBoss))
                  .then(literal("status").requires(Permissions.require(MOD_ID + ".boss.status", PermissionLevel.GAMEMASTERS)).executes(ArcanaCommands::bossStatus))
                  .then(literal("forcePlayerCount").requires(Permissions.require(MOD_ID + ".boss.forceplayercount", PermissionLevel.GAMEMASTERS))
                        .then(argument("players", integer()).executes(ctx -> ArcanaCommands.bossForcePlayerCount(ctx, getInteger(ctx, "players")))))
                  .then(literal("announce").requires(Permissions.require(MOD_ID + ".boss.announce", PermissionLevel.GAMEMASTERS))
                        .then(argument("time", MessageArgument.message()).executes(ctx -> ArcanaCommands.announceBoss(ctx, MessageArgument.getMessage(ctx, "time").getString()))))
                  .then(literal("begin").requires(Permissions.require(MOD_ID + ".boss.begin", PermissionLevel.GAMEMASTERS)).executes(ArcanaCommands::beginBoss))
                  .then(literal("teleport").requires(Permissions.require(MOD_ID + ".boss.teleport", PermissionLevel.ALL))
                        .executes(context -> ArcanaCommands.bossTeleport(context, context.getSource().getPlayer(), false))
                        .then(argument("player", player()).requires(Permissions.require(MOD_ID + ".boss.teleport.others", PermissionLevel.GAMEMASTERS))
                              .executes(context -> ArcanaCommands.bossTeleport(context, getPlayer(context, "player"), false)))
                        .then(literal("all").requires(Permissions.require(MOD_ID + ".boss.teleport.all", PermissionLevel.GAMEMASTERS))
                              .executes(context -> ArcanaCommands.bossTeleport(context, context.getSource().getPlayer(), true)))))
            .then(literal("specialEvent").requires(Permissions.require(MOD_ID + ".specialevent", PermissionLevel.ALL))
                  .then(literal("check").requires(Permissions.require(MOD_ID + ".specialevent.check", PermissionLevel.GAMEMASTERS))
                        .then(argument("player", player())
                              .executes(context -> ArcanaCommands.checkSpecialEventConditions(context, getPlayer(context, "player")))))
                  .then(literal("trigger")
                        .then(argument("players", players())
                              .then(literal("gaialtus").requires(Permissions.require(MOD_ID + ".specialevent.trigger.gaialtus", PermissionLevel.GAMEMASTERS))
                                    .executes(context -> ArcanaCommands.forceGaialtusEvent(context, getPlayers(context, "players"))))
                              .then(literal("ceptyus").requires(Permissions.require(MOD_ID + ".specialevent.trigger.ceptyus", PermissionLevel.GAMEMASTERS))
                                    .executes(context -> ArcanaCommands.forceCeptyusEvent(context, getPlayers(context, "players"))))
                              .then(literal("zeraiya").requires(Permissions.require(MOD_ID + ".specialevent.trigger.zeraiya", PermissionLevel.GAMEMASTERS))
                                    .executes(context -> ArcanaCommands.forceZeraiyaEvent(context, getPlayers(context, "players"))))))
                  .then(literal("action").requires(Permissions.require(MOD_ID + ".specialevent.action", PermissionLevel.ALL))
                        .then(argument("choice", greedyString())
                              .executes(context -> ArcanaCommands.specialEventCommand(context, getString(context, "choice"))))))
      );
      
      dispatcher.register(ArcanaNovum.CONFIG.generateCommand("arcana", "config"));
      
      if(DEV_MODE){
         dispatcher.register(literal("arcana")
               .then(literal("test").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                     .executes(ArcanaCommands::testCommand)
                     .then(argument("num", integer())
                           .executes(ctx -> ArcanaCommands.testCommand(ctx, getInteger(ctx, "num")))))
               .then(literal("getbookdata").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ArcanaCommands::getBookData))
               .then(literal("getitemdata").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                     .then(argument("name", string()).executes(ctx -> ArcanaCommands.getItemData(ctx, getString(ctx, "name")))))
               .then(literal("makerecipe").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ArcanaCommands::makeCraftingRecipe))
               .then(literal("loaditemdata").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                     .then(argument("id", string()).suggests(ArcanaCommands::getItemSuggestions)
                           .executes(ctx -> ArcanaCommands.loadItemData(ctx, getString(ctx, "id")))))
               .then(literal("boss")
                     .then(literal("test").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ArcanaCommands::testBoss)))
               .then(literal("item").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                     .then(literal("name")
                           .then(literal("set")
                                 .then(argument("name", greedyString()).executes(ctx -> ArcanaCommands.setItemName(ctx, getString(ctx, "name")))))
                           .then(literal("get").executes(ArcanaCommands::getItemName)))
                     .then(literal("lore")
                           .then(literal("set")
                                 .then(argument("index", integer(0))
                                       .then(argument("lore", greedyString()).executes(ctx -> ArcanaCommands.setItemLore(ctx, getInteger(ctx, "index"), getString(ctx, "lore"))))))
                           .then(literal("get")
                                 .then(argument("index", integer(0)).executes(ctx -> ArcanaCommands.getItemLore(ctx, getInteger(ctx, "index")))))
                           .then(literal("remove")
                                 .then(argument("index", integer(0)).executes(ctx -> ArcanaCommands.removeItemLore(ctx, getInteger(ctx, "index")))))
                           .then(literal("add")
                                 .then(argument("lore", greedyString()).executes(ctx -> ArcanaCommands.setItemLore(ctx, -1, getString(ctx, "lore")))))))
         );
      }
   }
   
   private static CompletableFuture<Suggestions> getSkinSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder){
      ServerPlayer player = context.getSource().getPlayer();
      if(player == null) return builder.buildFuture();
      
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(player.getMainHandItem());
      Set<String> suggestions = new HashSet<>();
      suggestions.add("none");
      if(arcanaItem != null){
         ArcanaPlayerData data = ArcanaNovum.data(player);
         for(ArcanaSkin skin : ArcanaSkin.getAllSkinsForItem(arcanaItem)){
            if(data.hasSkin(skin)){
               suggestions.add(skin.getSerializedName());
            }
         }
      }
      
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      for(String suggestion : suggestions){
         if(suggestion.toLowerCase(Locale.ROOT).contains(remaining)){
            builder.suggest(suggestion);
         }
      }
      return builder.buildFuture();
   }
}
