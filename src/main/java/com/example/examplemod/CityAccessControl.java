package com.example.examplemod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.UUID;



@Mod.EventBusSubscriber
public class CityAccessControl {

    // Проверка, может ли игрок модифицировать блоки в данном чанке
    public class CityInteractionHandler {

        public boolean canPlayerModify(EntityPlayer player, BlockPos pos) {
            ChunkPos chunkPos = new ChunkPos(pos);
            String cityName = CityManager.getCityByChunk(chunkPos); // Получаем город по чанку

            if (cityName == null) {
                return false; // Чанк не принадлежит ни одному городу
            }

            City city = CityManager.getCity(cityName);
            UUID playerUUID = player.getUniqueID();

            if (!CityManager.isPlayerInCity(playerUUID, cityName)) {
                return false; // Игрок не состоит в этом городе
            }

            Rank playerRank = CityManager.getRank(playerUUID, cityName);
            Set<String> permissions = city.getPermissions(playerRank); // Получаем права для ранга игрока

            return permissions.contains("modify"); // Проверяем, есть ли право на модификацию
        }


        @SubscribeEvent
        public void onBlockBreak(BlockEvent.BreakEvent event) {
            if (!canPlayerModify(event.getPlayer(), event.getPos())) {
                event.setCanceled(true);  // Отменяем событие, если игрок не имеет прав
            }
        }

        @SubscribeEvent
        public void onBlockPlace(BlockEvent.PlaceEvent event) {
            if (!canPlayerModify(event.getPlayer(), event.getPos())) {
                event.setCanceled(true);  // Отменяем событие, если игрок не имеет прав
            }
        }

        @SubscribeEvent
        public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
            if (!canPlayerModify(event.getEntityPlayer(), event.getPos())) {
                event.setCanceled(true);  // Отменяем событие, если игрок не имеет прав
            }
        }
    }}

