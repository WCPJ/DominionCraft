package com.example.examplemod;

import java.util.*;

public enum Rank {
    MAYOR("mayor", true, true, true, new HashSet<>(Arrays.asList("new", "claim", "unclaim", "join", "leave", "info", "list", "delete", "invite", "kick", "rank", "closejoin", "openjoin"))),
    ADVISOR("advisor", true, true, true, new HashSet<>(Arrays.asList("claim", "unclaim", "join", "leave", "info", "list", "invite", "rank", "closejoin", "openjoin"))),
    CITIZEN("citizen", false, false, true, new HashSet<>(Arrays.asList("leave", "info", "list"))),
    NONE("none", false, false, false, new HashSet<>(Arrays.asList("new", "join", "info", "list")));

    private final String rankName;
    private final boolean canBuild;
    private final boolean canBreak;
    private final boolean canUse;
    private final Set<String> permissions; // Права для каждого ранга

    Rank(String rankName, boolean canBuild, boolean canBreak, boolean canUse, Set<String> permissions) {
        this.rankName = rankName;
        this.canBuild = canBuild;
        this.canBreak = canBreak;
        this.canUse = canUse;
        this.permissions = permissions;
    }

    public String getRankName() {
        return rankName;
    }

    public boolean canBuild() {
        return canBuild;
    }

    public boolean canBreak() {
        return canBreak;
    }

    public boolean canUse() {
        return canUse;
    }

    public Set<String> getPermissions() {
        return permissions; // Возвращает права для данного ранга
    }
}
