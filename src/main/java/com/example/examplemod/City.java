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

    private Map<UUID, Rank> ranks; // Ранги игроков в городе

    // Конструктор города
    public City(String name, UUID mayor) {
        this.name = name; // Устанавливаем название города
        this.mayor = mayor; // Устанавливаем мэра
        this.open = true; // Город открыт по умолчанию
        this.citizens = new HashSet<>(); // Инициализируем множество граждан
        this.claimedChunks = new HashSet<>(); // Инициализируем множество приватизированных чанков
        this.ranks = new HashMap<>(); // Инициализируем карту рангов
        this.permissions = new HashMap<>();
        initializePermissions(); // Инициализируем права при создании города

        addCitizen(mayor); // Добавляем мэра как гражданина
        ranks.put(mayor, Rank.MAYOR); // Назначаем мэру ранг автоматически
    }

    private void initializePermissions() {
        permissions.put(Rank.MAYOR, new HashSet<>(Arrays.asList("new", "claim", "unclaim", "join", "leave", "info", "list", "delete", "invite", "kick", "rank", "closejoin", "openjoin")));
        permissions.put(Rank.ADVISOR, new HashSet<>(Arrays.asList("claim", "unclaim", "join", "leave", "info", "list", "invite", "rank", "closejoin", "openjoin")));
        permissions.put(Rank.CITIZEN, new HashSet<>(Arrays.asList("leave", "info", "list")));
        permissions.put(Rank.NONE, new HashSet<>(Arrays.asList("new", "join", "info", "list")));
    }
    public Set<String> getPermissions(Rank rank) {
        return permissions.getOrDefault(rank, Collections.emptySet());
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

    // Добавление гражданина в город
    public void addCitizen(UUID playerUUID) {
        citizens.add(playerUUID); // Добавляем гражданина
        ranks.putIfAbsent(playerUUID, Rank.CITIZEN); // Назначаем ранг гражданина по умолчанию
    }

    // Удаление гражданина из города
    public boolean removeCitizen(UUID playerUUID) {
        if (isMayor(playerUUID)) {
            return false; // Мэра нельзя удалить
        }
        citizens.remove(playerUUID); // Удаляем гражданина
        ranks.remove(playerUUID); // Удаляем ранг игрока
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
        ranks.put(playerUUID, rank); // Устанавливаем ранг для игрока
    }

    // Удаление ранга у игрока
    public void removeRank(UUID playerUUID) {
        ranks.remove(playerUUID); // Удаляем ранг игрока
    }

    // Получение ранга игрока
    public Rank getRank(UUID playerUUID) {
        return ranks.getOrDefault(playerUUID, Rank.CITIZEN); // Возвращаем ранг игрока, если нет - по умолчанию гражданин
    }

    // Получение названия города
    public String getName() {
        return name; // Возвращаем название города
    }

    // Получение UUID мэра
    public UUID getMayor() {
        return mayor; // Возвращаем мэра города
    }

    // Проверка, открыт ли город для вступления
    public boolean isOpen() {
        return open; // Возвращаем статус открытия города
    }

    // Установка статуса города (открыт/закрыт)
    public void setOpen(boolean open) {
        this.open = open; // Устанавливаем статус открытия
    }

}


