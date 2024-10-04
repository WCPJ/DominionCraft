package com.example.examplemod;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CityManager {
    private static Map<String, City> cities = new HashMap<>();  // Хранение всех городов (name -> city)
    private static Map<String, String> playerCityMap = new HashMap<>();  // Игрок -> Город (UUID игрока в String -> название города)

    // Получение города по его названию
    public static City getCity(String cityName) {
        return cities.get(cityName);
    }

    // Проверка, является ли чанк частью города
    public static String getCityByChunk(ChunkPos chunkPos) {
        for (City city : cities.values()) {
            if (city.isChunkClaimed(chunkPos)) {
                return city.getName();
            }
        }
        return null;
    }

    // Установка мэра города
    public static void setMayor(UUID playerUUID, String cityName) {
        if (cities.containsKey(cityName)) {
            City city = cities.get(cityName);
            city.addRank(playerUUID, Rank.MAYOR);
        }
    }

    // Создание города
    public static void createCity(EntityPlayer player, String[] args, MinecraftServer server) throws CommandException {
        if (args.length < 2) throw new CommandException("Usage: /city new <name>");
        String cityName = args[1];

        UUID playerUUID = player.getUniqueID();
        if (isPlayerInCity(playerUUID, cityName)) throw new CommandException("You are already part of a city.");
        if (isChunkClaimed(player.world, new ChunkPos(player.getPosition()))) throw new CommandException("Chunk is already claimed.");

        City newCity = new City(cityName, playerUUID);
        cities.put(cityName, newCity);
        playerCityMap.put(playerUUID.toString(), cityName);
        newCity.claimChunk(new ChunkPos(player.getPosition()));

        player.sendMessage(new TextComponentString("City created successfully!"));
        newCity.addRank(playerUUID, Rank.MAYOR);
        setMayor(playerUUID, cityName);
    }

    // Вступление в город
    public static void joinCity(EntityPlayer player, String[] args) throws CommandException {
        if (args.length < 2) throw new CommandException("Usage: /city join <name>");
        String cityName = args[1];

        UUID playerUUID = player.getUniqueID();
        City city = cities.get(cityName);
        if (city == null) throw new CommandException("City not found.");
        if (isPlayerInCity(playerUUID, cityName)) throw new CommandException("You are already in a city.");

        city.addCitizen(playerUUID);
        playerCityMap.put(playerUUID.toString(), cityName);
        player.sendMessage(new TextComponentString("Joined city " + cityName));
    }

    // Приват чанка
    public static void claimChunk(EntityPlayer player, ChunkPos chunkPos) throws CommandException {
        String cityName = playerCityMap.get(player.getUniqueID().toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);
        if (!city.isMayor(player.getUniqueID())) throw new CommandException("Only the mayor can claim chunks.");
        if (!isChunkClaimed(player.world, chunkPos)) {
            city.claimChunk(chunkPos);
            player.sendMessage(new TextComponentString("Chunk claimed for city " + cityName));
        } else {
            throw new CommandException("Chunk is already claimed.");
        }
    }

    // Расприват чанка
    public static void unclaimChunk(EntityPlayer player, ChunkPos chunkPos) throws CommandException {
        String cityName = playerCityMap.get(player.getUniqueID().toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);
        if (!city.isMayor(player.getUniqueID())) throw new CommandException("Only the mayor can unclaim chunks.");
        if (city.getClaimedChunks().size() <= 1) {
            throw new CommandException("You cannot unclaim the last chunk of the city.");
        }
        if (city.isChunkClaimed(chunkPos)) {
            city.unclaimChunk(chunkPos);
            player.sendMessage(new TextComponentString("Chunk unclaimed."));
        } else {
            throw new CommandException("Chunk is not claimed.");
        }
    }

    // Выход из города
    public static void leaveCity(EntityPlayer player, MinecraftServer server) throws CommandException {
        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);
        if (city.isMayor(playerUUID)) {
            throw new CommandException("You are the mayor of the city. You must delete the city to leave.");
        }
        city.removeCitizen(playerUUID);
        playerCityMap.remove(playerUUID.toString());
        player.sendMessage(new TextComponentString("You have left the city " + cityName));
    }

    // Удаление города
    public static void deleteCity(EntityPlayer player, MinecraftServer server) throws CommandException {
        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);
        if (!city.isMayor(playerUUID)) throw new CommandException("Only the mayor can delete the city.");

        cities.remove(cityName);
        for (UUID citizen : city.getCitizens()) {
            playerCityMap.remove(citizen.toString());
        }
        player.sendMessage(new TextComponentString("City " + cityName + " has been deleted."));
    }

    // Приглашение игрока в город
    public static void invitePlayer(EntityPlayer player, String[] args, MinecraftServer server) throws CommandException {
        if (args.length < 2) throw new CommandException("Usage: /city invite <player>");
        String inviteeName = args[1];

        // Получаем UUID игрока по его имени
        EntityPlayer targetPlayer = player.getServer().getPlayerList().getPlayerByUsername(inviteeName);
        if (targetPlayer == null) throw new CommandException("Player " + inviteeName + " not found.");
        UUID inviteeUUID = targetPlayer.getUniqueID();

        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);
        if (!city.isMayor(playerUUID)) throw new CommandException("Only the mayor can invite players.");

        city.addCitizen(inviteeUUID);
        playerCityMap.put(inviteeUUID.toString(), cityName);
        player.sendMessage(new TextComponentString("Player " + inviteeName + " has been invited to join the city."));
    }

    // Кик игрока
    public static void kickPlayer(EntityPlayer player, String[] args, MinecraftServer server) throws CommandException {
        if (args.length < 2) throw new CommandException("Usage: /city kick <player>");
        String playerToKickName = args[1];

        // Получаем UUID игрока по его имени
        EntityPlayer targetPlayer = player.getServer().getPlayerList().getPlayerByUsername(playerToKickName);
        if (targetPlayer == null) throw new CommandException("Player " + playerToKickName + " not found.");
        UUID playerToKickUUID = targetPlayer.getUniqueID();

        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        City city = cities.get(cityName);
        if (!city.isMayor(playerUUID)) throw new CommandException("Only the mayor can kick players.");
        if (city.isMayor(playerToKickUUID)) {
            throw new CommandException("You cannot kick yourself. Delete the city to leave.");
        }

        city.removeCitizen(playerToKickUUID);
        playerCityMap.remove(playerToKickUUID.toString());
        player.sendMessage(new TextComponentString("Player " + playerToKickName + " was kicked from the city."));
    }

    public static void manageRanks(EntityPlayer player, String[] args) throws CommandException {
        if (args.length < 4) throw new CommandException("Usage: /city rank <add|remove> <player> <rank>");
        String action = args[1];
        String targetPlayerName = args[2];
        String rankName = args[3];

        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);

        if (!city.isMayor(playerUUID)) {
            throw new CommandException("Only the mayor can manage ranks.");
        }

        // Попробуем получить UUID игрока через сервер
        EntityPlayer targetPlayer = player.getServer().getPlayerList().getPlayerByUsername(targetPlayerName);
        if (targetPlayer == null) {
            throw new CommandException("Player " + targetPlayerName + " not found.");
        }
        UUID targetPlayerUUID = targetPlayer.getUniqueID();

        // Получаем город целевого игрока
        String targetCityName = playerCityMap.get(targetPlayerUUID.toString());
        if (targetCityName == null || !targetCityName.equals(cityName)) {
            throw new CommandException(targetPlayerName + " is not part of your city.");
        }

        Rank rank = Rank.valueOf(rankName.toUpperCase());

        if (action.equals("add")) {
            city.addRank(targetPlayerUUID, rank);
            player.sendMessage(new TextComponentString("Rank " + rankName + " assigned to " + targetPlayerName));
        } else if (action.equals("remove")) {
            city.removeRank(targetPlayerUUID);
            player.sendMessage(new TextComponentString("Rank removed from " + targetPlayerName));
        } else {
            throw new CommandException("Invalid action. Use 'add' or 'remove'.");
        }


    }



    // Закрытие города для вступления
    public static void closeJoin(EntityPlayer player, MinecraftServer server) throws CommandException {
        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);

        if (!city.isMayor(playerUUID)) throw new CommandException("Only the mayor can close the city.");
        city.setOpen(false);  // Закрываем город для вступления
        player.sendMessage(new TextComponentString("City " + cityName + " is now closed for new players."));

    }

    // Открытие города для вступления
    public static void openJoin(EntityPlayer player, MinecraftServer server) throws CommandException {
        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);

        if (!city.isMayor(playerUUID)) throw new CommandException("Only the mayor can open the city.");
        city.setOpen(true);  // Открываем город для вступления
        player.sendMessage(new TextComponentString("City " + cityName + " is now open for new players."));

    }

    // Показ информации о городе
    public static void cityInfo(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new CommandException("Usage: /city info <name>");
        String cityName = args[1];
        City city = cities.get(cityName);
        if (city == null) throw new CommandException("City not found.");

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "City: " + city.getName()));
        sender.sendMessage(new TextComponentString("Mayor: " + city.getMayor()));
        sender.sendMessage(new TextComponentString("Citizens: " + city.getCitizens().size()));
        sender.sendMessage(new TextComponentString("Claimed Chunks: " + city.getClaimedChunks().size()));
    }

    // Показ списка городов
    public static void cityList(ICommandSender sender) {
        if (cities.isEmpty()) {
            sender.sendMessage(new TextComponentString("No cities exist."));
        } else {
            sender.sendMessage(new TextComponentString("Cities: " + String.join(", ", cities.keySet())));
        }
    }

    // Проверка, состоит ли игрок в городе
     static boolean isPlayerInCity(UUID playerUUID, String cityName) {
        return playerCityMap.containsKey(playerUUID.toString());
    }
    public static Rank getRank(UUID playerUUID, String cityName) {
        for (City city : cities.values()) {
            Rank rank = city.getRank(playerUUID);
            if (rank != null) {
                return rank; // Возвращаем ранг, если игрок найден в городе
            }
        }
        return null; // Игрок не найден в ни одном городе
    }


    // Проверка, является ли чанк заприваченным
    private static boolean isChunkClaimed(World world, ChunkPos chunkPos) {
        for (City city : cities.values()) {
            if (city.isChunkClaimed(chunkPos)) {
                return true;
            }
        }
        return false;
    }

}
