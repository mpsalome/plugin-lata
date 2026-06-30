package com.project.rpgplugin.core.card;

public enum CardTier {
    BRONZE("<gradient:#CD7F32:#B8860B>", 1),
    SILVER("<gradient:#C0C0C0:#E8E8E8>", 2),
    GOLD("<gradient:#FFD700:#FFA500>", 3);

    private final String color;
    private final int rank;

    CardTier(String color, int rank) {
        this.color = color;
        this.rank = rank;
    }

    public String color() { return color; }
    public int rank() { return rank; }
    public String nameKey() { return "tier." + name().toLowerCase(); }
}
