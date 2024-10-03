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
        addCitizen(mayor);
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
        return ranks.getOrDefault(player, Rank.CITIZEN);  // По умолчанию гражданин
    }

    // Сохранение данных города в NBT
    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("name", name);
        tag.setString("mayor", mayor.toString());  // Сохраняем мэра как строку
        tag.setBoolean("open", open);

        // Сохраняем граждан
        NBTTagList citizensTag = new NBTTagList();
        for (UUID citizen : citizens) {
            NBTTagCompound citizenTag = new NBTTagCompound();
            citizenTag.setString("uuid", citizen.toString());
            citizensTag.appendTag(citizenTag);
        }
        tag.setTag("citizens", citizensTag);

        // Сохраняем чанки (X и Z координаты)
        NBTTagList chunksTag = new NBTTagList();
        for (ChunkPos chunkPos : claimedChunks) {
            NBTTagCompound chunkTag = new NBTTagCompound();
            chunkTag.setInteger("x", chunkPos.x);
            chunkTag.setInteger("z", chunkPos.z);
            chunksTag.appendTag(chunkTag);
        }
        tag.setTag("claimedChunks", chunksTag);

        // Сохраняем ранги
        NBTTagList ranksTag = new NBTTagList();
        for (Map.Entry<UUID, Rank> entry : ranks.entrySet()) {
            NBTTagCompound rankTag = new NBTTagCompound();
            rankTag.setString("uuid", entry.getKey().toString());
            rankTag.setString("rank", entry.getValue().getRankName());
            ranksTag.appendTag(rankTag);
        }
        tag.setTag("ranks", ranksTag);

        return tag;
    }

    // Восстановление данных города из NBT
    public static City fromNBT(NBTTagCompound tag) {
        String name = tag.getString("name");
        UUID mayor = UUID.fromString(tag.getString("mayor"));  // Восстанавливаем UUID мэра
        boolean open = tag.getBoolean("open");

        City city = new City(name, mayor);
        city.setOpen(open);

        // Восстанавливаем граждан
        NBTTagList citizensTag = tag.getTagList("citizens", 10);
        for (int i = 0; i < citizensTag.tagCount(); i++) {
            NBTTagCompound citizenTag = citizensTag.getCompoundTagAt(i);
            city.addCitizen(UUID.fromString(citizenTag.getString("uuid")));
        }

        // Восстанавливаем чанки (X и Z координаты)
        NBTTagList chunksTag = tag.getTagList("claimedChunks", 10);
        for (int i = 0; i < chunksTag.tagCount(); i++) {
            NBTTagCompound chunkTag = chunksTag.getCompoundTagAt(i);
            int chunkX = chunkTag.getInteger("x");
            int chunkZ = chunkTag.getInteger("z");
            ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);  // Восстанавливаем координаты чанка
            city.claimChunk(chunkPos);
        }

        // Восстанавливаем ранги
        NBTTagList ranksTag = tag.getTagList("ranks", 10);
        for (int i = 0; i < ranksTag.tagCount(); i++) {
            NBTTagCompound rankTag = ranksTag.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(rankTag.getString("uuid"));
            Rank rank = Rank.valueOf(rankTag.getString("rank").toUpperCase());
            city.addRank(uuid, rank);
        }

        return city;
    }
}
