package com.example.examplemod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;



@Mod.EventBusSubscriber
public class CityProtectionHandler {

    // Проверка, может ли игрок модифицировать блоки в данном чанке
    public static boolean canPlayerModify(EntityPlayer player, ChunkPos chunkPos) {
        // Получаем город, которому принадлежит чанк
        String cityName = CityManager.getCityByChunk(chunkPos);

        if (cityName == null) {
            return true;  // Чанк свободен, можно модифицировать
        }

        UUID playerUUID = player.getUniqueID();

        // Получаем объект города
        City city = CityManager.getCity(cityName);

        // Проверяем, состоит ли игрок в городе или является ли мэром
        if (!city.getCitizens().isEmpty() && !city.isMayor(playerUUID)) {
            player.sendMessage(new TextComponentString("You do not have permission to modify blocks in this city."));
            return false;  // Игрок не имеет прав
        }

        // Проверяем, состоит ли игрок в этом городе
        boolean isInCity = CityManager.isPlayerInCity(playerUUID, cityName);
        Rank playerRank = CityManager.getRank(playerUUID, cityName); // Получаем ранг игрока в конкретном городе

        if (!isInCity) {
            player.sendMessage(new TextComponentString("You do not belong to the city that owns this chunk."));
            return false; // Игрок не состоит в этом городе
        }

        // Если игрок состоит в городе, но его ранг не позволяет модифицировать
        if (playerRank == null || (playerRank != Rank.MAYOR && playerRank != Rank.ADVISOR)) {
            player.sendMessage(new TextComponentString("You don't have permission to modify this chunk."));
            return false; // Игрок не имеет нужного ранга
        }

        return true; // Игрок может действовать

    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!canPlayerModify(event.getPlayer(), new ChunkPos(event.getPos()))) {
            event.setCanceled(true);  // Отменяем событие, если игрок не имеет прав
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (!canPlayerModify(event.getPlayer(), new ChunkPos(event.getPos()))) {
            event.setCanceled(true);  // Отменяем событие, если игрок не имеет прав
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!canPlayerModify(event.getEntityPlayer(), new ChunkPos(event.getPos()))) {
            event.setCanceled(true);  // Отменяем событие, если игрок не имеет прав
        }
    }
}
