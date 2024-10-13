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
        return "/city <new|claim|unclaim|join|leave|info|list|delete|invite|kick|rank|closejoin|openjoin|toggle>";
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
                CityManager.createCity(player, args, server);
                break;
            case "claim":
                CityManager.claimChunk(player, playerChunk);
                break;
            case "unclaim":
                CityManager.unclaimChunk(player, playerChunk);
                break;
            case "join":
                CityManager.joinCity(player, args);
                break;
            case "leave":
                CityManager.leaveCity(player, server);
                break;
            case "info":
                CityManager.cityInfo(sender, args);
                break;
            case "list":
                CityManager.cityList(sender);
                break;
            case "delete":
                CityManager.deleteCity(player, server);
                break;
            case "invite":
                CityManager.invitePlayer(player, args, server);
                break;
            case "kick":
                CityManager.kickPlayer(player, args, server);
                break;
            case "rank":
                if (args.length < 4) throw new CommandException("Usage: /city rank <add|remove> <player> <rank>");
                CityManager.manageRanks(player, args);
                break;
            case "closejoin":
                CityManager.closeJoin(player, server);
                break;
            case "openjoin":
                CityManager.openJoin(player, server);
                break;
            case "toggle":
                if (args.length < 3) throw new CommandException("Usage: /city toggle <mobspawn|explosions|pvp> <on|off>");
                toggleCityFeature(player, args[1], args[2]);
                break;

            default:
                throw new CommandException("Invalid subcommand.");
        }
    }

    private void toggleCityFeature(EntityPlayer player, String feature, String state) throws CommandException {
        String cityName = CityManager.getPlayerCity(player.getUniqueID());
        if (cityName == null) {
            throw new CommandException("You are not part of any city.");
        }


    }
}
