package com.example.examplemod;

public enum Rank {
    MAYOR("mayor"),  // Мэр
    ADVISOR("advisor"),  // Советник
    SOLDIER("soldier"),  // Солдат
    CITIZEN("citizen"),  // Обычный гражданин
    NONE("none");  // Не состоит в городе

    private final String rankName;

    Rank(String rankName) {
        this.rankName = rankName;
    }

    public String getRankName() {
        return rankName;
    }

    // Определяем, может ли игрок использовать команду
    public boolean canUseCommand(String command) {
        switch (this) {
            case MAYOR:
                return true;  // Мэр может использовать все команды
            case ADVISOR:
                return !command.equals("delete") && !command.equals("kick");
            case SOLDIER:
                return command.equals("defend");
            case CITIZEN:
                return command.equals("join") || command.equals("leave") || command.equals("info") || command.equals("list");
            case NONE:
                return command.equals("join") || command.equals("info") || command.equals("list");
            default:
                return false;
        }
    }
}


