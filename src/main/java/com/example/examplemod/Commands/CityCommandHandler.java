package com.example.examplemod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;

import java.util.Set;

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
        // Получаем название города игрока
        String cityName = CityManager.getPlayerCity(player.getUniqueID());

        // Проверка, является ли игрок частью города
        if (cityName == null) {
            throw new CommandException("You are not a member of any city.");
        }

        // Получаем объект города
        City city = CityManager.getCity(cityName);
        if (city == null) {
            throw new CommandException("The city was not found.");
        }


// Проверка прав игрока (MAYOR или ADVISOR)
        if (!Permissions.CityToggle(player.getUniqueID(), cityName, CityManager.getCities(), CityManager.getPlayerCityMap())) {
            throw new CommandException("You do not have the rights to execute this command.");
        }


        // Устанавливаем состояние функции города
        boolean allowed;
        switch (feature.toLowerCase()) {
            case "mobspawn":
                allowed = parseState(state);
                city.setMobSpawningAllowed(allowed);
                player.sendMessage(new TextComponentString("Spawn mobs in the city " + city.getName() + " " + (allowed ? "enabled." : "disabled.")));
                break;
            case "explosions":
                allowed = parseState(state);
                city.setExplosionsAllowed(allowed);
                player.sendMessage(new TextComponentString("Explosions in the city " + city.getName() + " " + (allowed ? "enabled." : "disabled.")));
                break;
            case "pvp":
                allowed = parseState(state);
                city.setPvPAllowed(allowed);
                player.sendMessage(new TextComponentString("PvP in the city " + city.getName() + " " + (allowed ? "enabled." : "disabled.")));
                break;
            default:
                throw new CommandException("Invalid function parameter. Use: mobspawn, explosions, pvp.");
        }
    }

    private boolean hasPermission(EntityPlayer player, String cityName) {
        return Permissions.hasRank(player.getUniqueID(), cityName, Rank.MAYOR, CityManager.getCities(), CityManager.getPlayerCityMap()) ||
                Permissions.hasRank(player.getUniqueID(), cityName, Rank.ADVISOR, CityManager.getCities(), CityManager.getPlayerCityMap());
    }

    private boolean parseState(String state) throws CommandException {
        if ("on".equalsIgnoreCase(state)) {
            return true;
        } else if ("off".equalsIgnoreCase(state)) {
            return false;
        } else {
            throw new CommandException("The state must be \"on\" or \"off\".");
        }
    }

}
