package com.example.examplemod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
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
    public static boolean canPlayerModify(EntityPlayer player, BlockPos pos) {
        // Получаем город, которому принадлежит чанк
        ChunkPos chunkPos = new ChunkPos(pos);
        String cityName = CityManager.getCityByChunk(chunkPos);

        if (cityName == null) {
            return true;  // Чанк свободен, можно модифицировать
        }

        UUID playerUUID = player.getUniqueID();

        // Получаем объект города
        City city = CityManager.getCity(cityName);

        // Проверяем, состоит ли игрок в этом городе
        if (CityManager.isPlayerInCity(playerUUID, cityName)) {
            // Игрок состоит в этом городе, проверяем его ранг
            Rank playerRank = CityManager.getRank(playerUUID, cityName);

            // Проверяем, имеет ли игрок достаточный ранг
            if (playerRank == Rank.MAYOR || playerRank == Rank.ADVISOR) {
                return true; // Игрок имеет достаточный ранг
            } else {
                player.sendMessage(new TextComponentString("You don't have permission to modify this chunk."));
                System.out.println("CityProtectionHandler: Player " + player.getName() + " doesn't have permission in " + cityName + ", rank is " + playerRank); // Добавили лог
                return false; // Игрок не имеет нужного ранга
            }
        } else {
            player.sendMessage(new TextComponentString("You do not belong to the city that owns this chunk."));
            System.out.println("CityProtectionHandler: Player " + player.getName() + " is not in city " + cityName); // Добавили лог
            return false; // Игрок не состоит в этом городе
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!canPlayerModify(event.getPlayer(), event.getPos())) {
            event.setCanceled(true);  // Отменяем событие, если игрок не имеет прав
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (!canPlayerModify(event.getPlayer(), event.getPos())) {
            event.setCanceled(true);  // Отменяем событие, если игрок не имеет прав
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!canPlayerModify(event.getEntityPlayer(), event.getPos())) {
            event.setCanceled(true);  // Отменяем событие, если игрок не имеет прав
        }
    }
}