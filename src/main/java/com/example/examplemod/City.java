package com.example.examplemod;

import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class City {
    private String name; // Название города
    private UUID mayor; // UUID мэра
    private Set<UUID> citizens; // Граждане города
    private Set<ChunkPos> claimedChunks; // Приватизированные чанки
    private boolean open; // Открыт ли город для новых граждан
    private Map<Rank, Set<String>> permissions; // Права для каждого ранга
    private Map<UUID, Set<Rank>> playerRanks; // Хранение рангов игроков
    private Map<UUID, Rank> ranks; // Ранги игроков в городе
    private boolean allowPvP;
    private boolean allowMobSpawning;
    private boolean allowExplosions;



    // Конструктор города
    public City(String name, UUID mayor) {
        this.name = name; // Устанавливаем название города
        this.mayor = mayor; // Устанавливаем мэра
        this.open = true; // Город открыт по умолчанию
        this.citizens = new HashSet<>(); // Инициализируем множество граждан
        this.claimedChunks = new HashSet<>(); // Инициализируем множество приватизированных чанков
        this.playerRanks = new HashMap<>(); // Инициализация карты рангов
        this.ranks = new HashMap<>(); // Инициализация карты рангов
        playerRanks.put(mayor, new HashSet<>(Arrays.asList(Rank.MAYOR))); // Назначение мэра
        this.permissions = new HashMap<>(); // Инициализируем карту прав
        this.allowPvP = false; // По умолчанию PvP разрешено
        this.allowMobSpawning = false; // По умолчанию спавн мобов разрешен
        this.allowExplosions = false; // По умолчанию взрывы разрешены


        addCitizen(mayor); // Добавляем мэра как гражданина
        ranks.put(mayor, Rank.MAYOR); // Назначаем мэру ранг автоматически
        initializePermissions(); // Инициализируем права при создании города
    }

    private void initializePermissions() {
        permissions.put(Rank.MAYOR, Permissions.getPermissions(Rank.MAYOR));
        permissions.put(Rank.ADVISOR, Permissions.getPermissions(Rank.ADVISOR));
        permissions.put(Rank.CITIZEN, Permissions.getPermissions(Rank.CITIZEN));
        permissions.put(Rank.NONE, Permissions.getPermissions(Rank.NONE));
    }

    public Set<Rank> getRanks(UUID playerUUID) {
        return playerRanks.getOrDefault(playerUUID, new HashSet<>()); // Возвращаем ранги игрока или пустое множество
    }



    // Метод для передачи мэрства другому игроку
    public boolean transferMayor(UUID newMayor) {
        if (citizens.contains(newMayor)) { // Проверяем, является ли новый мэр гражданином
            ranks.put(mayor, Rank.CITIZEN); // Мэр становится гражданином
            mayor = newMayor; // Передаем мэрство
            ranks.put(mayor, Rank.MAYOR); // Назначаем нового мэра
            return true; // Успешная передача мэрства
        }
        return false; // Новый мэр не является гражданином
    }

    // Проверка, является ли игрок мэром
    public boolean isMayor(UUID playerUUID) {
        return mayor.equals(playerUUID); // Сравниваем с UUID мэра
    }
    // Проверка, может ли игрок выполнять действия


    // Добавление гражданина в город
    public void addCitizen(UUID playerUUID) {
        citizens.add(playerUUID); // Добавляем гражданина
        ranks.putIfAbsent(playerUUID, Rank.CITIZEN); // Назначаем ранг гражданина по умолчанию
    }
    public Set<Rank> getPlayerRanks(UUID playerUUID) {
        return playerRanks.getOrDefault(playerUUID, new HashSet<>()); // Возвращает набор рангов игрока или пустой набор, если игрока нет
    }

    // Удаление гражданина из города
    public boolean removeCitizen(UUID playerUUID) {
        if (isMayor(playerUUID)) {
            return false; // Мэра нельзя удалить
        }
        citizens.remove(playerUUID); // Удаляем гражданина
        ranks.put(playerUUID, Rank.NONE); // Присваиваем игроку ранг NONE вместо полного удаления
        return true;
    }


    // Получение списка приватизированных чанков
    public Set<ChunkPos> getClaimedChunks() {
        return claimedChunks; // Возвращаем приватизированные чанки
    }

    // Получение списка граждан города
    public Set<UUID> getCitizens() {
        return citizens; // Возвращаем граждан города
    }

    // Приватизация чанка
    public void claimChunk(ChunkPos chunkPos) {
        claimedChunks.add(chunkPos); // Добавляем чанк в приватизированные
    }

    // Расприватизация чанка
    public void unclaimChunk(ChunkPos chunkPos) {
        claimedChunks.remove(chunkPos); // Удаляем чанк из приватизированных
    }

    // Проверка, приватен ли чанк
    public boolean isChunkClaimed(ChunkPos chunkPos) {
        return claimedChunks.contains(chunkPos); // Возвращаем true, если чанк приватизирован
    }

    // Назначение ранга игроку
    public void addRank(UUID playerUUID, Rank rank) {
        if (rank == Rank.MAYOR && playerRanks.values().stream().flatMap(Set::stream).anyMatch(r -> r == Rank.MAYOR)) {
            throw new IllegalStateException("City Created!");
        }
        playerRanks.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(rank);
    }


    // Удаление ранга у игрока
    public void removeRank(UUID playerUUID, Rank rank) {
        Set<Rank> ranks = playerRanks.get(playerUUID);
        if (ranks != null) {
            ranks.remove(rank);
            if (ranks.isEmpty()) {
                playerRanks.remove(playerUUID); // Удаляем игрока, если у него больше нет рангов
            }
        }
    }
    public void removeAllRanks(UUID playerUUID) {
        playerRanks.remove(playerUUID); // Предполагается, что у вас есть карта playerRanks, где ключ - UUID игрока
    }


    // Получение ранга игрока
    public Rank getRank(UUID playerUUID) {
        if (!citizens.contains(playerUUID)) {
            return Rank.NONE; // Возвращаем ранг NONE, если игрок не состоит в городе
        }
        return ranks.getOrDefault(playerUUID, Rank.CITIZEN); // Иначе возвращаем ранг игрока или CITIZEN по умолчанию
    }
    // Метод для настройки разрешения PvP
    public void setPvPAllowed(boolean allowed) {
        this.allowPvP = allowed;
    }

    public boolean isPvPAllowed() {
        return allowPvP;
    }

    // Метод для настройки разрешения спавна мобов
    public void setMobSpawningAllowed(boolean allowed) {
        this.allowMobSpawning = allowed;
    }

    public boolean isMobSpawningAllowed() {
        return allowMobSpawning;
    }

    // Метод для настройки разрешения взрывов
    public void setExplosionsAllowed(boolean allowed) {
        this.allowExplosions = allowed;
    }

    public boolean areExplosionsAllowed() {
        return allowExplosions;
    }





    // Получение названия города
    public String getName() {
        return name; // Возвращаем название города
    }

    // Получение UUID мэра
    public UUID getMayor() {
        return mayor; // Возвращаем мэра города
    }

    // Установка статуса города (открыт/закрыт)
    public void setOpen(boolean open) {
        this.open = open; // Устанавливаем статус открытия
    }




}
