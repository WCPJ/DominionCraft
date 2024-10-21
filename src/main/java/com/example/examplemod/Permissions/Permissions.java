package com.example.examplemod;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;



public class Permissions {
    private static final Set<String> MAYOR_PERMISSIONS = new HashSet<>(Arrays.asList(
            "new", "claim", "unclaim", "join", "leave", "info", "list", "delete", "invite", "kick", "rank", "closejoin", "openjoin"
    ));

    private static final Set<String> ADVISOR_PERMISSIONS = new HashSet<>(Arrays.asList(
            "claim", "unclaim", "join", "leave", "info", "list", "invite", "rank", "closejoin", "openjoin"
    ));

    private static final Set<String> CITIZEN_PERMISSIONS = new HashSet<>(Arrays.asList(
            "leave", "info", "list"
    ));









    public static Set<String> getPermissions(Rank rank) {
        switch (rank) {
            case MAYOR:
                return MAYOR_PERMISSIONS;
            case ADVISOR:
                return ADVISOR_PERMISSIONS;
            case CITIZEN:
                return CITIZEN_PERMISSIONS;
            case NONE:
                return new HashSet<>();
            default:
                return new HashSet<>(); // На случай, если передан неизвестный ранг
        }
    }
    public static boolean hasRank(UUID playerUUID, String cityName, Rank requiredRank, Map<String, City> cities, Map<String, String> playerCityMap) {
        // Получаем город по имени
        City city = cities.get(cityName);

        // Проверяем, существует ли город и состоит ли игрок в этом городе
        if (city != null && playerCityMap.get(playerUUID.toString()) != null && playerCityMap.get(playerUUID.toString()).equals(cityName)) {
            // Получаем фактический ранг игрока в городе
            Rank playerRank = city.getRank(playerUUID);

            // Проверяем, соответствует ли ранг игрока требуемому
            return playerRank == requiredRank || playerRank == Rank.MAYOR || playerRank == Rank.ADVISOR;
        }

        return false; // Игрок не состоит в городе или не имеет требуемого ранга
    }



    public static boolean canCreateCity(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        // Проверяем, состоит ли игрок уже в городе
        if (playerCityMap.containsKey(playerUUID.toString())) return false; // Игрок уже в городе

        // Проверка, существует ли уже город с таким именем
        if (cities.containsKey(cityName)) return false; // Город с таким именем уже существует

        return true; // Игрок может создать город
    }
    public static boolean canJoinCity(UUID playerUUID, Map<String, String> playerCityMap) {
        return !playerCityMap.containsKey(playerUUID.toString()); // Проверка, не состоит ли игрок в другом городе
    }
    public static boolean canClaimChunk(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        City city = cities.get(cityName);
        if (city == null) return false;

        Set<Rank> playerRanks = city.getPlayerRanks(playerUUID); // Предполагается, что в City есть метод getPlayerRanks
        return playerRanks.contains(Rank.MAYOR) || playerRanks.contains(Rank.ADVISOR); // Проверка, является ли игрок мэром или советником
    }
    public static boolean canUnclaimChunk(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        City city = cities.get(cityName);
        if (city == null) return false;

        Set<Rank> playerRanks = city.getPlayerRanks(playerUUID);
        return playerRanks.contains(Rank.MAYOR) || playerRanks.contains(Rank.ADVISOR);
    }

    public static boolean canDeleteCity(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        City city = cities.get(cityName);
        return city != null && city.isMayor(playerUUID); // Проверка, является ли игрок мэром города
    }
    public static boolean canInvitePlayer(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        City city = cities.get(cityName);
        if (city == null) return false;

        Set<Rank> playerRanks = city.getPlayerRanks(playerUUID);
        return playerRanks.contains(Rank.MAYOR) || playerRanks.contains(Rank.ADVISOR);
    }

    public static boolean canKickPlayer(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        City city = cities.get(cityName);
        return city != null && city.isMayor(playerUUID); // Проверка, является ли игрок мэром города
    }
    public static boolean canManageRanks(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        City city = cities.get(cityName);
        return city != null && city.isMayor(playerUUID); // Проверка, является ли игрок мэром города
    }
    public static boolean canCloseJoin(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        City city = cities.get(cityName);
        if (city == null) return false;

        Set<Rank> playerRanks = city.getPlayerRanks(playerUUID);
        return playerRanks.contains(Rank.MAYOR) || playerRanks.contains(Rank.ADVISOR);
    }

    public static boolean canOpenJoin(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        City city = cities.get(cityName);
        if (city == null) return false;

        Set<Rank> playerRanks = city.getPlayerRanks(playerUUID);
        return playerRanks.contains(Rank.MAYOR) || playerRanks.contains(Rank.ADVISOR);
    }
    public static boolean canViewCityInfo(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        // В данной функции можно добавить логику для проверки прав на просмотр информации о городе
        // Например, разрешить всем гражданам города просматривать его информацию
        String playerCity = playerCityMap.get(playerUUID.toString());
        return playerCity != null && playerCity.equals(cityName); // Граждане могут видеть информацию о своем городе
    }
    public static boolean canViewCityList(UUID playerUUID) {
        // В данной функции можно добавить логику для проверки прав на просмотр списка городов
        // Например, разрешить всем игрокам видеть список городов
        return true; // В данном случае, разрешаем всем видеть список городов
    }
    public static boolean canManageCity(UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        return hasRank(playerUUID, cityName, Rank.MAYOR, cities, playerCityMap) || hasRank(playerUUID, cityName, Rank.ADVISOR, cities, playerCityMap);
    }



    // Проверка, состоит ли игрок в городе
    private static boolean isPlayerInCity(UUID playerUUID, String cityName, Map<String, String> playerCityMap) {
        return playerCityMap.get(playerUUID.toString()).equals(cityName); // Проверка, что город соответствует имени
    }
    public static boolean CityToggle (UUID playerUUID, String cityName, Map<String, City> cities, Map<String, String> playerCityMap) {
        City city = cities.get(cityName);
        if (city == null) return false;

        // Получаем ранги игрока в городе
        Set<Rank> playerRanks = city.getPlayerRanks(playerUUID); // Предполагается, что в City есть метод getPlayerRanks
        return playerRanks.contains(Rank.MAYOR) || playerRanks.contains(Rank.ADVISOR); // Проверка на наличие ранга
    }




    private static Rank getPlayerRank(UUID playerUUID, String cityName, Map<String, String> playerCityMap) {
        // Получаем город
        City city = CityManager.getCities().get(cityName);
        if (city != null) {
            return city.getRank(playerUUID); // Предполагается, что в City есть метод для получения ранга игрока
        }
        return Rank.NONE; // Если город не найден, возвращаем NONE
    }


}







