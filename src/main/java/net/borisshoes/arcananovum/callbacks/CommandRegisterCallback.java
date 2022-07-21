package net.borisshoes.arcananovum.callbacks;

import com.mojang.brigadier.CommandDispatcher;
import net.borisshoes.arcananovum.ArcanaCommands;
import net.borisshoes.arcananovum.Arcananovum;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandRegisterCallback {
   public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
      dispatcher.register(literal("arcana")
            .then(literal("create").requires(source -> source.hasPermissionLevel(2))
                  .then(argument("id", string()).suggests(ArcanaCommands::getItemSuggestions)
                        .executes(ctx -> ArcanaCommands.createItem(ctx.getSource(), getString(ctx, "id")))))
            .then(literal("test").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::testCommand))
            .then(literal("getbookdata").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::getBookData))
            .then(literal("getitemdata").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::getItemData))
            .then(literal("makerecipe").requires(source -> source.hasPermissionLevel(2)).executes(ArcanaCommands::makeCraftingRecipe))
            .then(literal("help").executes(ArcanaCommands::openGuideBook))
            .then(literal("guide").executes(ArcanaCommands::openGuideBook))
            .then(literal("uuids").requires(source -> source.hasPermissionLevel(2))
                  .then(argument("player",player()).executes(context -> ArcanaCommands.uuidCommand(context,getPlayer(context,"player")))))
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
                        .then(argument("targets",player())
                              .executes(context -> ArcanaCommands.xpCommandQuery(context, getPlayer(context,"targets"))))))
      );
   }
}
