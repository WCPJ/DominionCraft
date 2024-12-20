    package com.example.examplemod;

    import net.minecraft.client.Minecraft;
    import net.minecraft.nbt.CompressedStreamTools;
    import net.minecraft.nbt.NBTTagCompound;
    import net.minecraft.nbt.NBTTagList;
    import net.minecraftforge.common.MinecraftForge;
    import net.minecraftforge.common.util.Constants;
    import net.minecraftforge.fml.common.FMLCommonHandler;
    import net.minecraftforge.fml.common.Mod;
    import net.minecraftforge.fml.common.Mod.EventHandler;
    import net.minecraftforge.fml.common.SidedProxy;
    import net.minecraftforge.fml.common.event.FMLInitializationEvent;
    import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
    import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
    import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
    import net.minecraftforge.fml.common.network.NetworkRegistry;
    import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
    import net.minecraftforge.fml.relauncher.Side;
    import org.apache.logging.log4j.Logger;

    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.util.concurrent.Executors;
    import java.util.concurrent.ScheduledExecutorService;
    import java.util.concurrent.TimeUnit;


    @Mod(modid = ExampleMod.MODID, name = ExampleMod.NAME, version = ExampleMod.VERSION)
    public class ExampleMod {
        public static final String MODID = "dominioncraft";
        public static final String NAME = "Dominion Craft";
        public static final String VERSION = "1.0";


        private static Logger logger;

        private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Планировщик для выполнения задач


        @Mod.Instance
        public static ExampleMod instance;  // Статический экземпляр мода
        public static final CityManager CITY_MANAGER = new CityManager();


        @EventHandler
        public void preInit(FMLPreInitializationEvent event) {
            logger = event.getModLog();
            NetworkHandler.registerMessages(); // Регистрируем сетевые сообщения
            Banner.printBanner();

        }

        @EventHandler
        public void init(FMLInitializationEvent event) {

            // Регистрируем обработчики событий
            MinecraftForge.EVENT_BUS.register(new CityEventHandler());  // Если нужен для обработки событий
            NetworkHandler.registerMessages(); // Регистрируем сетевые сообщения
            MinecraftForge.EVENT_BUS.register(new ClientHudRenderer()); // Регистрируем HUD на клиенте
            MinecraftForge.EVENT_BUS.register(new ChunkStatusHandler()); // Регистрируем обработчик смены чанка

            // Регистрация рендерера на клиенте
            if (event.getSide() == Side.CLIENT) {
                CityHubRenderer.register();
            }
            // Регистрируем обработчик статусов на сервере
            MinecraftForge.EVENT_BUS.register(new CityStatusHandler());


            //MinecraftForge.EVENT_BUS.register(new CityHubRenderer());
            //MinecraftForge.EVENT_BUS.register(new CityStatusHandler()); // Регистрируем обработчик событий


            // Регистрируем защиту от атак между членами одного города
            CityFriendlyFire.register();  // Здесь регистрируем защиту от friendly fire
            startCitySaveScheduler();

        }

        @Mod.EventHandler
        public void onServerStarting(FMLServerStartingEvent event) {


            // Регистрируем команду через сервер
            event.registerServerCommand(new CityCommandHandler());
            event.registerServerCommand(new SetChatModeCommand());
            event.registerServerCommand(new CityAdminCommandHandler());
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new CityStatusHandler());


            // Загружаем города при старте сервера
            loadCities();
            CityManager.startCleanupTask(); // Очистка приглошений у игроков каждые 5 минут СТАРТ
        }

        @Mod.EventHandler
        public void onServerStopping(FMLServerStoppingEvent event) {
            CityManager.stopCleanupTask();// Остановка задачи очистки приглошений игроков.
            // Сохраняем города при остановке сервера
            saveCities();

        }

        private void startCitySaveScheduler() {
            scheduler.scheduleAtFixedRate(() -> {
                // Логируем, что прошло еще 1 час
                logger.info("AutoSaving, saving cities...");

                // Логируем все города, которые будут сохранены
                logger.info("Saving the following cities:");
                for (City city : CityManager.getCities().values()) {
                    logger.info("City: " + city.getName()); // Логируем имя города
                }

                // Сохраняем города
                saveCities();
            }, 0, 30, TimeUnit.MINUTES); // Сохранение каждые 1 час
        }


        private void saveCities() {
            if (CityManager.getCities().isEmpty()) {
                logger.warn("No cities to save.");
                return; // Прерываем сохранение, если нет городов
            }

            NBTTagList cityList = new NBTTagList();
            for (City city : CityManager.getCities().values()) {
                cityList.appendTag(city.writeToNBT()); // Сохранение каждого города в NBT
            }

            // Сохранение списка городов в файл
            File file = new File("world/dominioncraft_cities.nbt"); // Путь к файлу
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setTag("cities", cityList);
                CompressedStreamTools.writeCompressed(compound, fileOutputStream); // Используем writeCompressed
                logger.info("Cities saved successfully.");
            } catch (IOException e) {
                logger.error("Failed to save cities: " + e.getMessage());
            }
        }

        private void loadCities() {
            File file = new File("world/dominioncraft_cities.nbt"); // Путь к файлу
            if (file.exists()) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    NBTTagCompound compound = CompressedStreamTools.readCompressed(fileInputStream);
                    NBTTagList cityList = compound.getTagList("cities", Constants.NBT.TAG_COMPOUND);

                    for (int i = 0; i < cityList.tagCount(); i++) {
                        NBTTagCompound cityData = cityList.getCompoundTagAt(i);
                        try {
                            City city = City.readFromNBT(cityData); // Загрузка каждого города из NBT
                            if (city != null && city.getName() != null && !city.getName().isEmpty()) {
                                CityManager.getCities().put(city.getName(), city); // Добавление города в менеджер
                                logger.info("Loaded city: " + city.getName());
                            } else {
                                logger.warn("City data is invalid or missing: " + cityData);
                            }
                        } catch (Exception e) {
                            logger.error("Failed to load city from data: " + e.getMessage(), e);
                        }
                    }

                    logger.info("Cities loaded successfully.");
                } catch (IOException e) {
                    logger.error("Failed to load cities: " + e.getMessage(), e);
                }
            } else {
                logger.info("No saved cities found.");
            }
        }
    }



