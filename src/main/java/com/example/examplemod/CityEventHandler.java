package com.example.examplemod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CityEventHandler {

    // Обработка события разрушения блока
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        ChunkPos chunkPos = new ChunkPos(event.getPos());

        // Проверка, принадлежит ли чанк городу
        City city = CityManager.getCityByChunk(chunkPos);
        if (city != null && city.isChunkClaimed(chunkPos)) {
            // Проверка, состоит ли игрок в городе и имеет ли он достаточный ранг
            if (!city.getCitizens().contains(player.getUniqueID()) ||
                    !(city.getPlayerRanks(player.getUniqueID()).contains(Rank.MAYOR) ||
                            city.getPlayerRanks(player.getUniqueID()).contains(Rank.ADVISOR))) {
                event.setCanceled(true); // Отмена события, если условия не выполнены
                player.sendMessage(new TextComponentString("You don't have permission to destroy blocks here!"));
            }
        }
    }

    // Обработка события установки блока
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        EntityPlayer player = event.getPlayer();
        ChunkPos chunkPos = new ChunkPos(event.getPos());

        // Проверка, принадлежит ли чанк городу
        City city = CityManager.getCityByChunk(chunkPos);
        if (city != null && city.isChunkClaimed(chunkPos)) {
            // Проверка, состоит ли игрок в городе и имеет ли он достаточный ранг
            if (!city.getCitizens().contains(player.getUniqueID()) ||
                    !(city.getPlayerRanks(player.getUniqueID()).contains(Rank.MAYOR) ||
                            city.getPlayerRanks(player.getUniqueID()).contains(Rank.ADVISOR))) {
                event.setCanceled(true); // Отмена события, если условия не выполнены
                player.sendMessage(new TextComponentString("You don't have permission to place blocks here!"));
            }
        }
    }

    // Обработка события взаимодействия с предметами
    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        ChunkPos chunkPos = new ChunkPos(event.getPos());

        // Проверка, принадлежит ли чанк городу
        City city = CityManager.getCityByChunk(chunkPos);
        if (city != null && city.isChunkClaimed(chunkPos)) {
            // Проверка, состоит ли игрок в городе и имеет ли он достаточный ранг
            if (!city.getCitizens().contains(player.getUniqueID()) ||
                    !(city.getPlayerRanks(player.getUniqueID()).contains(Rank.MAYOR) ||
                            city.getPlayerRanks(player.getUniqueID()).contains(Rank.ADVISOR))) {
                event.setCanceled(true); // Отмена события, если условия не выполнены
                player.sendMessage(new TextComponentString("You don't have permission to use it here!"));
            }
        }
    }
}
