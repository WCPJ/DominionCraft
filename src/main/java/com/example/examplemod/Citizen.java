package com.example.examplemod;

public class Citizen {
    private String playerName; // Имя игрока
    private Rank rank; // Ранг игрока
    private String city; // Город, к которому принадлежит игрок

    public Citizen(String playerName, Rank rank, String city) {
        this.playerName = playerName;
        this.rank = rank;
        this.city = city;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Rank getRank() {
        return rank;
    }

    public String getCity() {
        return city;
    }

    // Метод для проверки прав на команду
    public boolean canUseCommand(String command) {
        return rank.canUseCommand(command);
    }
}
