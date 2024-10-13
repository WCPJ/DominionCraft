package com.example.examplemod;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

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
    // Обработка события спавна сущностей (пример с использованием события, если оно есть)
    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityLivingBase) { // Проверяем, является ли сущность мобом
            ChunkPos chunkPos = new ChunkPos(entity.getPosition());

            // Проверка, принадлежит ли чанк городу
            City city = CityManager.getCityByChunk(chunkPos);
            if (city != null && city.isChunkClaimed(chunkPos)) {
                // Проверка, разрешен ли спавн мобв в этом городе
                if (!city.isMobSpawningAllowed()) {
                    event.setCanceled(true); // Блокируем спавн мобов
                }
            }
        }

}
    // Обработка события взрыва
    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Detonate event) {
        BlockPos explosionPos = new BlockPos(event.getExplosion().getPosition()); // Преобразуем Vec3d в BlockPos
        ChunkPos chunkPos = new ChunkPos(explosionPos); // Получаем ChunkPos из BlockPos

        // Проверка, принадлежит ли взрыв чанк города
        City city = CityManager.getCityByChunk(chunkPos);
        boolean inCity = (city != null && city.isChunkClaimed(chunkPos));

        // Создаём временный список для хранения затронутых блоков, которые нужно удалить
        List<BlockPos> blocksToRemove = new ArrayList<>();

        // Проверка, затрагивает ли взрыв какие-либо блоки в зачищенных чанках
        for (BlockPos pos : event.getAffectedBlocks()) {
            ChunkPos affectedChunk = new ChunkPos(pos); // Получаем ChunkPos затронутого блока
            City affectedCity = CityManager.getCityByChunk(affectedChunk); // Проверяем, принадлежит ли затронутый чанк городу

            // Если взрыв происходит в городе
            if (inCity) {
                if (affectedCity != null && affectedCity.isChunkClaimed(affectedChunk)) {
                    // Если взрыв затрагивает зачищенный чанк, добавляем затронутый блок в список для удаления
                    if (!city.areExplosionsAllowed()) {
                        blocksToRemove.add(pos); // Блок добавляется в список на удаление
                    }
                }
            } else { // Если взрыв происходит за пределами города
                if (affectedCity != null && affectedCity.isChunkClaimed(affectedChunk)) {
                    // Если взрыв затрагивает зачищенный чанк, добавляем затронутый блок в список для удаления
                    blocksToRemove.add(pos); // Блок добавляется в список на удаление
                }
            }
        }

        // Удаляем затронутые блоки после итерации
        event.getAffectedBlocks().removeAll(blocksToRemove);
    }



    // Обработка события атаки
    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        EntityPlayer attacker = event.getEntityPlayer(); // Получаем атакующего
        EntityLivingBase target = (EntityLivingBase) event.getTarget(); // Получаем цель атаки

        // Проверка, что цель атаки - игрок
        if (target instanceof EntityPlayer) {
            ChunkPos chunkPos = new ChunkPos(target.getPosition()); // Получаем ChunkPos цели атаки
            City city = CityManager.getCityByChunk(chunkPos); // Проверяем, принадлежит ли чанк городу

            if (city != null && city.isChunkClaimed(chunkPos)) {
                // Проверка, разрешено ли PvP в этом городе
                if (!city.isPvPAllowed()) { // Проверяем разрешение на PvP
                    event.setCanceled(true); // Отмена события атаки
                    attacker.sendMessage(new TextComponentString("PvP is not allowed in this city!")); // Сообщение игроку
                }
            }
        }
    }
}

