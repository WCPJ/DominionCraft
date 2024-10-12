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
import java.util.Set;
import java.util.UUID;

public class CityManager {
    public static Map<String, City> cities = new HashMap<>();  // Хранение всех городов (name -> city)
    public static Map<String, City> getCities() {
        return cities;
    }
    public static Map<String, String> playerCityMap = new HashMap<>();  // Игрок -> Город (UUID игрока в String -> название города)
    public static Map<String, String> getPlayerCityMap() {
        return playerCityMap;
    }
    // Получение города по его названию
    public static City getCity(String cityName) {
        City city = cities.get(cityName);
        if (city == null) {
            System.out.println("City " + cityName + " not found.");
        }
        return city;
    }


    // Проверка, является ли чанк частью города
    public static City getCityByChunk(ChunkPos chunkPos) {
        for (City city : cities.values()) {
            if (city.isChunkClaimed(chunkPos)) {
                return city; // Возвращаем объект City вместо имени
            }
        }
        System.out.println("No city found for chunk at " + chunkPos);
        return null; // Возвращаем null, если город не найден
    }


    // Установка мэра города

    public static boolean isPlayerInCity(UUID playerUUID, String cityName) {
        City city = cities.get(cityName.toLowerCase()); // Получаем город по имени
        if (city == null) {
            return false; // Город не существует
        }
        return city.getCitizens().contains(playerUUID); // Проверяем, является ли игрок гражданином
    }

    public static void createCity(EntityPlayer player, String[] args, MinecraftServer server) throws CommandException {
        // Проверка наличия имени города в аргументах
        if (args.length < 2) {
            throw new CommandException("Usage: /city new <name>");
        }

        String cityName = args[1].toLowerCase(); // Приведение имени города к нижнему регистру
        UUID playerUUID = player.getUniqueID(); // Получение UUID игрока

        // Проверка, состоит ли игрок уже в городе
        if (playerCityMap.containsKey(playerUUID.toString())) {
            throw new CommandException("You are already part of a city."); // Игрок не может создать город, если он уже в одном
        }

        // Проверка прав на создание города
        if (!Permissions.canCreateCity(playerUUID, cityName, cities, playerCityMap)) {
            throw new CommandException("You cannot create a city.");
        }

        // Проверка, занят ли чанк
        if (getCityByChunk(new ChunkPos(player.getPosition())) != null) {
            throw new CommandException("Chunk is already claimed.");
        }

        // Создание нового города
        City newCity = new City(cityName, playerUUID);
        cities.put(cityName, newCity); // Сохраняем город в карте
        playerCityMap.put(playerUUID.toString(), cityName); // Связываем игрока с городом
        newCity.claimChunk(new ChunkPos(player.getPosition())); // Приватизация чанка

        // Назначение ранга мэра, с проверкой на существующего мэра
        try {
            newCity.addRank(playerUUID, Rank.MAYOR); // Назначение игрока мэром
        } catch (IllegalStateException e) {
            throw new CommandException(e.getMessage()); // Если уже есть мэр, выбрасываем исключение
        }

        // Уведомление игрока о создании города
        player.sendMessage(new TextComponentString("You have become the mayor of city " + cityName + "!"));
        player.sendMessage(new TextComponentString("City created successfully!"));
    }




    // Вступление в город
    public static void joinCity(EntityPlayer player, String[] args) throws CommandException {
        // Проверка наличия имени города в аргументах
        if (args.length < 2) {
            throw new CommandException("Usage: /city join <name>");
        }

        String cityName = args[1].toLowerCase(); // Приведение имени города к нижнему регистру

        UUID playerUUID = player.getUniqueID(); // Получение UUID игрока
        City city = cities.get(cityName); // Получаем город по имени

        // Проверка, существует ли город и является ли игрок его гражданином
        if (city == null) {
            throw new CommandException("City not found.");
        }
        if (isPlayerInCity(playerUUID, cityName)) {
            throw new CommandException("You are already in a city.");
        }

        // Проверка, может ли игрок присоединиться к городу
        if (!Permissions.canJoinCity(playerUUID, playerCityMap)) {
            throw new CommandException("You cannot join this city.");
        }

        // Добавление игрока в список граждан города
        city.addCitizen(playerUUID);
        playerCityMap.put(playerUUID.toString(), cityName); // Связываем игрока с городом
        player.sendMessage(new TextComponentString("You have joined city " + cityName + "!")); // Уведомление игрока
    }


    public static void claimChunk(EntityPlayer player, ChunkPos chunkPos) throws CommandException {
        // Получаем название города, к которому принадлежит игрок
        String cityName = playerCityMap.get(player.getUniqueID().toString());
        System.out.println("Player UUID: " + player.getUniqueID() + ", City Name: " + cityName);

        if (cityName == null) {
            throw new CommandException("You are not part of any city.");
        }

        City city = cities.get(cityName); // Получаем объект города
        UUID playerUUID = player.getUniqueID(); // Получаем UUID игрока

        // Получаем ранги игрока
        Set<Rank> playerRanks = city.getRanks(playerUUID);
        System.out.println("Player " + player.getName() + " has ranks: " + playerRanks);

        // Проверка прав на присвоение
        if (!Permissions.canClaimChunk(playerUUID, cityName, cities, playerCityMap)) {
            System.out.println("Player " + player.getName() + " does not have permission to claim chunks.");
            throw new CommandException("You do not have permission to claim chunks.");
        }

        // Проверка, не является ли чанк уже заприваченным
        if (isChunkClaimed(player.world, chunkPos)) {
            throw new CommandException("Chunk is already claimed.");
        }

        city.claimChunk(chunkPos); // Приватизация чанка
        player.sendMessage(new TextComponentString("Chunk claimed for city " + cityName + "!")); // Уведомление игрока
    }




    // Расприват чанка
    public static void unclaimChunk(EntityPlayer player, ChunkPos chunkPos) throws CommandException {
        // Получаем название города, к которому принадлежит игрок
        String cityName = playerCityMap.get(player.getUniqueID().toString());
        if (cityName == null) {
            throw new CommandException("You are not part of any city.");
        }

        City city = cities.get(cityName); // Получаем объект города
        UUID playerUUID = player.getUniqueID(); // Получаем UUID игрока

        // Проверка прав на расприватизацию
        if (!Permissions.canUnclaimChunk(playerUUID, cityName, cities, playerCityMap)) {
            throw new CommandException("You do not have permission to unclaim chunks.");
        }

        // Проверка, является ли это единственным чанком города
        if (city.getClaimedChunks().size() <= 1) {
            throw new CommandException("You cannot unclaim the last chunk of the city.");
        }

        // Проверка, является ли чанк заприваченным
        if (!city.isChunkClaimed(chunkPos)) {
            throw new CommandException("Chunk is not claimed.");
        }

        city.unclaimChunk(chunkPos); // Расприватизация чанка
        player.sendMessage(new TextComponentString("Chunk unclaimed.")); // Уведомление игрока
    }



    // Выход из города
    public static void leaveCity(EntityPlayer player, MinecraftServer server) throws CommandException {
        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        if (cityName == null) {
            throw new CommandException("You are not part of any city."); // Игрок не в городе
        }

        City city = cities.get(cityName);

        // Проверяем, является ли игрок мэром
        if (city.isMayor(playerUUID)) {
            throw new CommandException("You are the mayor of the city. You must delete the city to leave."); // Нельзя покинуть как мэр
        }

        // Удаляем игрока из города
        city.removeCitizen(playerUUID);

        // Удаляем все ранги игрока в этом городе
        city.removeAllRanks(playerUUID); // Предполагается, что у вас есть метод removeAllRanks

        // Удаляем запись о городе игрока
        playerCityMap.remove(playerUUID.toString());

        // Уведомление игрока
        player.sendMessage(new TextComponentString("You have left the city " + cityName));
    }



    // Удаление города
    public static void deleteCity(EntityPlayer player, MinecraftServer server) throws CommandException {
        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());

        if (cityName == null) {
            throw new CommandException("You are not part of any city."); // Игрок не в городе
        }

        City city = cities.get(cityName);

        // Проверка прав на удаление города
        if (!Permissions.canDeleteCity(playerUUID, cityName, cities, playerCityMap)) {
            throw new CommandException("Only the mayor can delete the city."); // Нельзя удалить город, если не мэр
        }

        cities.remove(cityName); // Удаляем город из списка городов
        for (UUID citizen : city.getCitizens()) {
            playerCityMap.remove(citizen.toString()); // Удаляем игроков из мапы
        }
        player.sendMessage(new TextComponentString("City " + cityName + " has been deleted.")); // Уведомление игрока
    }



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

        // Проверка прав на приглашение игрока
        if (!Permissions.canInvitePlayer(playerUUID, cityName, cities, playerCityMap)) {
            throw new CommandException("Only the mayor can invite players.");
        }

        city.addCitizen(inviteeUUID);
        playerCityMap.put(inviteeUUID.toString(), cityName);
        player.sendMessage(new TextComponentString("Player " + inviteeName + " has been invited to join the city."));
    }


    public static void kickPlayer(EntityPlayer player, String[] args, MinecraftServer server) throws CommandException {
        // Проверка, что введено достаточно аргументов
        if (args.length < 2) {
            throw new CommandException("Usage: /city kick <player>");
        }

        String playerToKickName = args[1]; // Имя игрока, которого нужно кикнуть

        // Получаем целевого игрока по имени
        EntityPlayer targetPlayer = server.getPlayerList().getPlayerByUsername(playerToKickName);
        if (targetPlayer == null) {
            throw new CommandException("Player " + playerToKickName + " not found.");
        }
        UUID playerToKickUUID = targetPlayer.getUniqueID(); // UUID игрока, которого нужно кикнуть

        UUID playerUUID = player.getUniqueID(); // UUID мэра
        String cityName = playerCityMap.get(playerUUID.toString()); // Название города мэра
        City city = cities.get(cityName); // Получаем объект города

        // Проверка, является ли вызывающий игрок мэром города
        if (!city.isMayor(playerUUID)) {
            throw new CommandException("Only the mayor can kick players.");
        }

        // Проверяем, состоит ли игрок, которого кикают, в том же городе
        String targetCityName = playerCityMap.get(playerToKickUUID.toString());
        if (targetCityName == null || !targetCityName.equals(cityName)) {
            throw new CommandException("Player " + playerToKickName + " is not part of your city.");
        }

        // Проверка, является ли игрок мэром
        if (city.isMayor(playerToKickUUID)) {
            throw new CommandException("You cannot kick the mayor. Delete the city to leave.");
        }

        // Удаляем гражданина
        if (city.removeCitizen(playerToKickUUID)) {
            playerCityMap.remove(playerToKickUUID.toString()); // Удаляем запись о городе игрока
            player.sendMessage(new TextComponentString("Player " + playerToKickName + " was kicked from the city."));
            targetPlayer.sendMessage(new TextComponentString("You have been kicked from the city " + cityName + "."));
        } else {
            throw new CommandException("Failed to kick player " + playerToKickName + " from the city.");
        }
    }




    public static void manageRanks(EntityPlayer player, String[] args) throws CommandException {
        if (args.length < 4) throw new CommandException("Usage: /city rank <add|remove> <player> <rank>");
        String action = args[1];
        String targetPlayerName = args[2];
        String rankName = args[3].toUpperCase();

        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);

        // Проверка прав на управление рангами
        if (!Permissions.canManageRanks(playerUUID, cityName, cities, playerCityMap)) {
            throw new CommandException("Only the mayor can manage ranks.");
        }

        // Получаем целевого игрока
        EntityPlayer targetPlayer = player.getServer().getPlayerList().getPlayerByUsername(targetPlayerName);
        if (targetPlayer == null) {
            throw new CommandException("Player " + targetPlayerName + " not found.");
        }
        UUID targetPlayerUUID = targetPlayer.getUniqueID();

        // Проверяем, что целевой игрок в том же городе
        String targetCityName = playerCityMap.get(targetPlayerUUID.toString());
        if (targetCityName == null || !targetCityName.equals(cityName)) {
            throw new CommandException(targetPlayerName + " is not part of your city.");
        }

        // Проверка на существование ранга
        Rank rank;
        try {
            rank = Rank.valueOf(rankName);
        } catch (IllegalArgumentException e) {
            throw new CommandException("Invalid rank name: " + rankName);
        }

        if (action.equals("add")) {
            city.addRank(targetPlayerUUID, rank); // Добавляем ранг
            System.out.println("Added rank: " + rank.getRankName() + " to player: " + targetPlayerName);
            player.sendMessage(new TextComponentString("Rank " + rank.getRankName() + " assigned to " + targetPlayerName));
        } else if (action.equals("remove")) {
            // Удаляем указанный ранг
            city.removeRank(targetPlayerUUID, rank); // Исправлено здесь
            System.out.println("Removed rank: " + rank.getRankName() + " from player: " + targetPlayerName);
            player.sendMessage(new TextComponentString("Rank " + rank.getRankName() + " removed from " + targetPlayerName));
        } else {
            throw new CommandException("Invalid action. Use 'add' or 'remove'.");
        }

        // Проверяем текущие ранги у целевого игрока
        Set<Rank> targetPlayerRanks = city.getRanks(targetPlayerUUID);
        System.out.println("Current ranks for " + targetPlayerName + ": " + targetPlayerRanks);
    }








    // Закрытие города для вступления
    public static void closeJoin(EntityPlayer player, MinecraftServer server) throws CommandException {
        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);

        // Проверка прав на закрытие города
        if (!Permissions.canCloseJoin(playerUUID, cityName, cities, playerCityMap)) {
            throw new CommandException("Only the mayor can close the city.");
        }

        city.setOpen(false);  // Закрываем город для вступления
        player.sendMessage(new TextComponentString("City " + cityName + " is now closed for new players."));
    }


    // Открытие города для вступления
    public static void openJoin(EntityPlayer player, MinecraftServer server) throws CommandException {
        UUID playerUUID = player.getUniqueID();
        String cityName = playerCityMap.get(playerUUID.toString());
        if (cityName == null) throw new CommandException("You are not part of any city.");
        City city = cities.get(cityName);

        // Проверка прав на открытие города
        if (!Permissions.canOpenJoin(playerUUID, cityName, cities, playerCityMap)) {
            throw new CommandException("Only the mayor can open the city.");
        }

        city.setOpen(true);  // Открываем город для вступления
        player.sendMessage(new TextComponentString("City " + cityName + " is now open for new players."));
    }


    // Показ информации о городе
    public static void cityInfo(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new CommandException("Usage: /city info <name>");
        String cityName = args[1];
        City city = cities.get(cityName);
        if (city == null) throw new CommandException("City not found.");

        // Проверяем, имеет ли игрок право на получение информации о городе
        UUID playerUUID = sender.getCommandSenderEntity() != null ? sender.getCommandSenderEntity().getUniqueID() : null;
        if (playerUUID != null && !Permissions.canViewCityInfo(playerUUID, cityName, cities, playerCityMap)) {
            throw new CommandException("You do not have permission to view city information.");
        }

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "City: " + city.getName()));
        sender.sendMessage(new TextComponentString("Mayor: " + city.getMayor()));
        sender.sendMessage(new TextComponentString("Citizens: " + city.getCitizens().size()));
        sender.sendMessage(new TextComponentString("Claimed Chunks: " + city.getClaimedChunks().size()));
    }


    // Показ списка городов
    public static void cityList(ICommandSender sender) {
        UUID playerUUID = sender.getCommandSenderEntity() != null ? sender.getCommandSenderEntity().getUniqueID() : null;

        // Проверяем, имеет ли игрок право на просмотр списка городов
        if (playerUUID != null && !Permissions.canViewCityList(playerUUID)) {
            sender.sendMessage(new TextComponentString("You do not have permission to view the city list."));
            return;
        }

        if (cities.isEmpty()) {
            sender.sendMessage(new TextComponentString("No cities exist."));
        } else {
            sender.sendMessage(new TextComponentString("Cities: " + String.join(", ", cities.keySet())));
        }
    }


    // Проверка, состоит ли игрок в городе

    public static Rank getRank(UUID playerUUID, String cityName) {
        City city = cities.get(cityName.toLowerCase()); // Получаем город по имени
        if (city != null) {
            return city.getRank(playerUUID); // Возвращаем ранг игрока в этом городе
        }
        return null; // Если город не найден, возвращаем null
    }



    // Проверка, является ли чанк заприваченным
    private static boolean isChunkClaimed(World world, ChunkPos chunkPos) {
        for (City city : cities.values()) {
            if (city.isChunkClaimed(chunkPos)) {
                return true; // Если чанк уже приватизирован, возвращаем true
            }
        }
        return false; // Чанк не найден среди приватизированных
    }

}
