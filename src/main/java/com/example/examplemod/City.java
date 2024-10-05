package com.example.examplemod;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class City {
    private String name;
    private UUID mayor;  // Храним мэра как UUID
    private Set<UUID> citizens = new HashSet<>();
    private Set<ChunkPos> claimedChunks = new HashSet<>();
    private boolean open;
    private Map<String, String> players;
    private Map<UUID, Rank> ranks = new HashMap<>();  // Ранги игроков

    // Конструктор города
    public City(String name, UUID mayor) {
        this.name = name;
        this.mayor = mayor;
        this.open = true;
        addCitizen(mayor);
        ranks.put(mayor, Rank.MAYOR);  // Мэр получает ранг автоматически
    }


    // Проверка, является ли игрок мэром
    public boolean isMayor(UUID playerUUID) {
        return mayor.equals(playerUUID);
    }
    // Метод для добавления игрока и его роли
    public void addPlayer(String playerName, String role) {
        // Добавление игрока в словарь с его ролью
        players.put(playerName, role);
    }

    // Добавление гражданина в город
    public void addCitizen(UUID playerUUID) {
        citizens.add(playerUUID);
        ranks.putIfAbsent(playerUUID, Rank.CITIZEN);  // По умолчанию гражданин
    }

    // Удаление гражданина из города
    public boolean removeCitizen(UUID playerUUID) {
        if (isMayor(playerUUID)) {
            return false;  // Мэра нельзя удалить как гражданина
        }
        citizens.remove(playerUUID);
        ranks.remove(playerUUID);  // Удаляем ранг
        return true;
    }

    // Получение граждан города
    public Set<UUID> getCitizens() {
        return citizens;
    }

    // Приват территории (чанка)
    public void claimChunk(ChunkPos chunkPos) {
        claimedChunks.add(chunkPos);
    }

    // Расприват территории (чанка)
    public void unclaimChunk(ChunkPos chunkPos) {
        claimedChunks.remove(chunkPos);
    }

    // Проверка, приватен ли чанк
    public boolean isChunkClaimed(ChunkPos chunkPos) {
        return claimedChunks.contains(chunkPos);
    }

    // Получение всех приватных чанков города
    public Set<ChunkPos> getClaimedChunks() {
        return claimedChunks;
    }

    // Получение названия города
    public String getName() {
        return name;
    }

    // Получение мэра города
    public UUID getMayor() {
        return mayor;
    }

    // Проверка, открыт ли город для вступления
    public boolean isOpen() {
        return open;
    }

    // Установка статуса города (открыт/закрыт)
    public void setOpen(boolean open) {
        this.open = open;
    }

    // Управление рангами
    public void addRank(UUID player, Rank rank) {
        ranks.put(player, rank);
    }

    public void removeRank(UUID player) {
        ranks.remove(player);
    }

    // Получение ранга игрока
    public Rank getRank(UUID player) {
        if (isMayor(player)) {  // Если игрок мэр, возвращаем ранг мэра
            return Rank.MAYOR;
        }
        return ranks.getOrDefault(player, Rank.CITIZEN);  // Если нет другого ранга, по умолчанию гражданин
    }
}
