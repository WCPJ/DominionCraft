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
    private Map<UUID, Rank> ranks = new HashMap<>();  // Ранги игроков


    // Конструктор города
    public City(String name, UUID mayor) {
        this.name = name;
        this.mayor = mayor;
        this.open = true;

        ranks.put(mayor, Rank.MAYOR);  // Мэр получает ранг автоматически
    }

    public boolean isMayor(UUID playerUUID) {
        return mayor.equals(playerUUID);
    }


    // Добавление гражданина в город
    public void addCitizen(UUID playerUUID) {
        citizens.add(playerUUID);
        ranks.putIfAbsent(playerUUID, Rank.CITIZEN);  // По умолчанию гражданин
    }
    // Удаление гражданина из города
    public boolean removeCitizen(UUID playerUUID) {
        citizens.remove(playerUUID);
        ranks.remove(playerUUID);  // Удаляем ранг
        return true;
    }
    // Добавление гражданина в город



    public Set<UUID> getCitizens() {
        return citizens;
    }

    // Приват территории (чанка)
    public void claimChunk(ChunkPos chunkPos) {
        claimedChunks.add(chunkPos);
    }

    public void unclaimChunk(ChunkPos chunkPos) {
        claimedChunks.remove(chunkPos);
    }

    public boolean isChunkClaimed(ChunkPos chunkPos) {
        return claimedChunks.contains(chunkPos);
    }

    public Set<ChunkPos> getClaimedChunks() {
        return claimedChunks;
    }

    public String getName() {
        return name;
    }

    public UUID getMayor() {
        return mayor;
    }

    public boolean isOpen() {
        return open;
    }

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

    public Rank getRank(UUID player) {
        if (isMayor(player)) { // Используем метод isMayor для проверки
            return Rank.MAYOR;
        } else {
            return Rank.CITIZEN;
        }
    }


}
