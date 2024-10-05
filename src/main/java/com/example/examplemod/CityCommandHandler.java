package com.example.examplemod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;

public class CityCommandHandler extends CommandBase {

    @Override
    public String getName() {
        return "city";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/city <new|claim|unclaim|join|leave|info|list|delete|invite|kick|rank|closejoin|openjoin>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException("Invalid usage. Use /city <new|claim|...>");
        }

        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException("Only players can use this command.");
        }

        EntityPlayer player = (EntityPlayer) sender;
        ChunkPos playerChunk = new ChunkPos(player.getPosition());

        switch (args[0]) {
            case "new":
                // Создание города
                CityManager.createCity(player, args, server);
                break;
            case "claim":
                // Приват чанка
                CityManager.claimChunk(player, playerChunk);
                break;
            case "unclaim":
                // Расприват чанка
                CityManager.unclaimChunk(player, playerChunk);
                break;
            case "join":
                // Вступление в город
                CityManager.joinCity(player, args);
                break;
            case "leave":
                // Выход из города
                CityManager.leaveCity(player, server);
                break;
            case "info":
                // Информация о городе
                CityManager.cityInfo(sender, args);
                break;
            case "list":
                // Список городов
                CityManager.cityList(sender);
                break;
            case "delete":
                // Удаление города
                CityManager.deleteCity(player, server);
                break;
            case "invite":
                // Приглашение игрока в город
                CityManager.invitePlayer(player, args, server);
                break;
            case "kick":
                // Кик игрока из города
                CityManager.kickPlayer(player, args, server);
                break;
            case "rank":
                // Управление рангами (добавление/удаление)
                if (args.length < 4) throw new CommandException("Usage: /city rank <add|remove> <player> <rank>");
                CityManager.manageRanks(player, args);
                break;
            case "closejoin":
                // Закрытие города для вступления
                CityManager.closeJoin(player, server);
                break;
            case "openjoin":
                // Открытие города для вступления
                CityManager.openJoin(player, server);
                break;

            default:
                throw new CommandException("Invalid subcommand.");
        }
    }
}
