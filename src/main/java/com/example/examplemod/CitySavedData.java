package com.example.examplemod;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CitySavedData extends WorldSavedData {
    public static final String DATA_NAME = "city_data";

    // Карта для хранения городов, где ключ - название города
    private final Map<String, City> cities = new HashMap<>();

    // Конструктор
    public CitySavedData() {
        super(DATA_NAME);
    }

    // Метод для добавления города
    public void addCity(City city) {
        cities.put(city.getName(), city);
        markDirty(); // Отмечаем данные как измененные
    }

    // Метод для получения города по названию
    public City getCity(String name) {
        return cities.get(name);
    }

    // Метод для удаления города
    public void removeCity(String name) {
        cities.remove(name);
        markDirty(); // Отмечаем данные как измененные
    }

    // Метод для сохранения данных в NBT
    @Override
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList cityList = new NBTTagList(); // Список для сохранения всех городов

        for (City city : cities.values()) {
            NBTTagCompound cityTag = new NBTTagCompound(); // Создаем NBTTagCompound для каждого города
            city.writeToNBT(cityTag); // Сериализация города в NBT
            cityList.appendTag(cityTag); // Добавляем его в список городов
        }

        compound.setTag("Cities", cityList); // Добавляем список городов в основной compound
    }

    // Метод для загрузки данных из NBT
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList cityList = compound.getTagList("Cities", 10); // Читаем список городов (10 = NBTTagCompound)

        for (int i = 0; i < cityList.tagCount(); i++) {
            NBTTagCompound cityTag = cityList.getCompoundTagAt(i);
            City city = City.readFromNBT(cityTag); // Десериализация города из NBT
            cities.put(city.getName(), city); // Добавляем город в карту
        }
    }

    // Метод для получения данных из мира
    public static CitySavedData getOrCreate(World world) {
        CitySavedData data = (CitySavedData) world.getMapStorage().getOrLoadData(CitySavedData.class, DATA_NAME);

        if (data == null) {
            data = new CitySavedData();
            world.getMapStorage().setData(DATA_NAME, data);
        }

        return data;
    }
}
