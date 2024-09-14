package net.borisshoes.arcananovum.callbacks;

import com.mojang.brigadier.CommandDispatcher;
import net.borisshoes.arcananovum.ArcanaCommands;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.borisshoes.arcananovum.ArcanaNovum.DEV_MODE;
import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandRegisterCallback {
   public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
      dispatcher.register(literal("arcana")
            .then(literal("create").requires(source -> source.hasPermissionLevel(2))
                  .then(argument("id", string()).suggests(ArcanaCommands::getItemSuggestions)
                        .executes(ctx -> ArcanaCommands.createItem(ctx.getSource(), getString(ctx, "id")))
                        .then(argument("targets", players())
                              .executes(ctx -> ArcanaCommands.createItems(ctx.getSource(), getString(ctx, "id"), getPlayers(ctx,"targets"))))))
            .then(literal("cache").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::cacheCommand))
            .then(literal("help").executes(ArcanaCommands::openGuideBook))
            .then(literal("guide").executes(ArcanaCommands::openGuideBook))
            .then(literal("show").executes(ArcanaCommands::showItem))
            .then(literal("uuids").requires(source -> source.hasPermissionLevel(2))
                  .then(argument("player",player()).executes(context -> ArcanaCommands.uuidCommand(context,getPlayer(context,"player")))))
            .then(literal("enhance").requires(source -> source.hasPermissionLevel(2))
                  .then((argument("percentage", doubleArg())
                        .executes(context -> ArcanaCommands.enhanceCommand(context,getDouble(context,"percentage"),null)))
                        .then(argument("target",player())
                              .executes(context -> ArcanaCommands.enhanceCommand(context,getDouble(context,"percentage"),getPlayer(context,"target"))))))
            .then(literal("xp").requires(source -> source.hasPermissionLevel(2))
                  .then(literal("add")
                        .then(argument("targets", players())
                              .then(((argument("amount", integer())
                                    .executes(context -> ArcanaCommands.xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),false,true)))
                                    .then(literal("points")
                                          .executes(context -> ArcanaCommands.xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"), false,true))))
                                    .then(literal("levels")
                                          .executes(context -> ArcanaCommands.xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),false,false))))))
                  .then(literal("set")
                        .then(argument("targets", players()).then(((argument("amount", integer(0))
                              .executes(context -> ArcanaCommands.xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),true,true)))
                              .then(literal("points")
                                    .executes(context -> ArcanaCommands.xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),true,true))))
                              .then(literal("levels")
                                    .executes(context -> ArcanaCommands.xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),true,false))))))
                  .then(literal("query")
                        .then(argument("target",player())
                              .executes(context -> ArcanaCommands.xpCommandQuery(context, getPlayer(context,"target"))))))
            .then(literal("skillpoints").requires(source -> source.hasPermissionLevel(2))
                  .then(literal("add")
                        .then(argument("targets", players())
                              .then(argument("amount", integer())
                                    .executes(context -> ArcanaCommands.skillpointsCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),false)))))
                  .then(literal("set")
                        .then(argument("targets", players()).then(argument("amount", integer())
                              .executes(context -> ArcanaCommands.skillpointsCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),true)))))
                  .then(literal("query")
                        .then(argument("target",player())
                              .executes(context -> ArcanaCommands.skillpointsCommandQuery(context, getPlayer(context,"target"))))))
            .then(literal("achievement").requires(source -> source.hasPermissionLevel(2))
                  .then(literal("grant")
                        .then(argument("id", string()).suggests(ArcanaCommands::getAchievementSuggestions)
                              .then(argument("targets", players())
                                    .executes(context -> ArcanaCommands.setAchievement(context,getString(context, "id"),true,getPlayers(context,"targets"))))))
                  .then(literal("revoke")
                        .then(argument("id", string()).suggests(ArcanaCommands::getAchievementSuggestions)
                              .then(argument("targets", players())
                                    .executes(context -> ArcanaCommands.setAchievement(context,getString(context, "id"),false,getPlayers(context,"targets"))))))
                  .then(literal("query")
                        .then(argument("id", string()).suggests(ArcanaCommands::getAchievementSuggestions)
                              .then(argument("target", player())
                                    .executes(context -> ArcanaCommands.getAchievement(context,getString(context, "id"),getPlayer(context,"target")))))))
            .then(literal("research").requires(source -> source.hasPermissionLevel(2))
                  .then(literal("grant")
                        .then(argument("id", string()).suggests(ArcanaCommands::getResearchSuggestions)
                              .then(argument("targets", players())
                                    .executes(context -> ArcanaCommands.setResearch(context,getString(context, "id"),true,getPlayers(context,"targets"))))))
                  .then(literal("revoke")
                        .then(argument("id", string()).suggests(ArcanaCommands::getResearchSuggestions)
                              .then(argument("targets", players())
                                    .executes(context -> ArcanaCommands.setResearch(context,getString(context, "id"),false,getPlayers(context,"targets"))))))
                  .then(literal("query")
                        .then(argument("id", string()).suggests(ArcanaCommands::getResearchSuggestions)
                              .then(argument("target", player())
                                    .executes(context -> ArcanaCommands.getResearch(context,getString(context, "id"),getPlayer(context,"target")))))))
            .then(literal("augment").requires(source -> source.hasPermissionLevel(2))
                  .then(literal("apply")
                        .then(argument("id", string()).suggests(ArcanaCommands::getAugmentSuggestions)
                              .then((argument("level", integer())
                                    .executes(context -> ArcanaCommands.applyAugment(context,getString(context, "id"),getInteger(context,"level"),null)))
                                    .then(argument("target",player())
                                          .executes(context -> ArcanaCommands.applyAugment(context,getString(context, "id"),getInteger(context,"level"),getPlayer(context,"target")))))))
                  .then(literal("setlevel")
                        .then(argument("id", string()).suggests(ArcanaCommands::getAugmentSuggestions)
                              .then((argument("level", integer())
                                    .executes(context -> ArcanaCommands.setAugment(context,getString(context, "id"),getInteger(context,"level"),null)))
                                    .then(argument("target",player())
                                          .executes(context -> ArcanaCommands.setAugment(context,getString(context, "id"),getInteger(context,"level"),getPlayer(context,"target"))))))))
            .then(literal("boss")
                  .then(literal("start").requires(source -> source.hasPermissionLevel(2))
                        .then(literal("dragon").executes(ArcanaCommands::startDragonBoss)))
                  .then(literal("resetAbilities").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("doAbility",bool()).executes(context -> ArcanaCommands.bossResetAbilities(context,getBool(context,"doAbility")))))
                  .then(literal("forceLairAction").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::bossForceLairAction))
                  .then(literal("abort").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::abortBoss))
                  .then(literal("clean").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::cleanBoss))
                  .then(literal("status").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::bossStatus))
                  .then(literal("forcePlayerCount").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("players", integer()).executes(ctx -> ArcanaCommands.bossForcePlayerCount(ctx, getInteger(ctx, "players")))))
                  .then(literal("announce").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("time", MessageArgumentType.message()).executes(ctx -> ArcanaCommands.announceBoss(ctx.getSource(), MessageArgumentType.getMessage(ctx, "time").getString()))))
                  .then(literal("begin").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::beginBoss))
                  .then(literal("teleport")
                        .executes(context -> ArcanaCommands.bossTeleport(context,context.getSource().getPlayer(),false))
                        .then(argument("player",player()).requires(source -> source.hasPermissionLevel(2))
                              .executes(context -> ArcanaCommands.bossTeleport(context,getPlayer(context,"player"),false)))
                        .then(literal("all").requires(source -> source.hasPermissionLevel(2))
                              .executes(context ->ArcanaCommands.bossTeleport(context,context.getSource().getPlayer(),true)))))
      );
   
      dispatcher.register(ArcanaNovum.config.generateCommand());
   
      if(DEV_MODE){
         dispatcher.register(literal("arcana")
               .then(literal("test").requires(source -> source.hasPermissionLevel(2))
                     .executes(ArcanaCommands::testCommand)
                     .then(argument("num",integer())
                           .executes(ctx -> ArcanaCommands.testCommand(ctx, getInteger(ctx, "num")))))
               .then(literal("getbookdata").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::getBookData))
               .then(literal("getitemdata").requires(source -> source.hasPermissionLevel(2))
                     .then(argument("name", string()).executes(ctx -> ArcanaCommands.getItemData(ctx, getString(ctx, "name")))))
               .then(literal("makerecipe").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::makeCraftingRecipe))
               .then(literal("boss")
                     .then(literal("test").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::testBoss)))
               .then(literal("item").requires(source -> source.hasPermissionLevel(2))
                     .then(literal("name")
                           .then(literal("set")
                                 .then(argument("name",greedyString()).executes(ctx -> ArcanaCommands.setItemName(ctx, getString(ctx,"name")))))
                           .then(literal("get").executes(ArcanaCommands::getItemName)))
                     .then(literal("lore")
                           .then(literal("set")
                                 .then(argument("index",integer(0))
                                       .then(argument("lore",greedyString()).executes(ctx -> ArcanaCommands.setItemLore(ctx, getInteger(ctx,"index"), getString(ctx, "lore"))))))
                           .then(literal("get")
                                 .then(argument("index",integer(0)).executes(ctx -> ArcanaCommands.getItemLore(ctx, getInteger(ctx,"index")))))
                           .then(literal("remove")
                                 .then(argument("index",integer(0)).executes(ctx -> ArcanaCommands.removeItemLore(ctx, getInteger(ctx,"index")))))
                           .then(literal("add")
                                 .then(argument("lore",greedyString()).executes(ctx -> ArcanaCommands.setItemLore(ctx, -1, getString(ctx, "lore")))))))
         );
      }
   }
}
