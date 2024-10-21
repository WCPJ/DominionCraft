package com.example.examplemod;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        super.preInit();
        // Клиентская инициализация (например, рендеринг)
    }

    @Override
    public void init() {
        super.init();
        // Регистрация клиентских событий
    }
}