package com.example.examplemod;

public enum Rank {
    MAYOR("mayor"),
    ADVISOR("advisor"),
    CITIZEN("citizen"),
    NONE("none");

    private final String rankName;

    Rank(String rankName) {
        this.rankName = rankName;
    }

    public String getRankName() {
        return rankName;
    }
}
