package com.project.rpgplugin.core.skill;

public enum SkillTier {
    BRONZE(1, "<gold>", 3),
    SILVER(3, "<gray>", 3),
    GOLD(5, "<yellow>", 3);

    private final int xpCost;
    private final String color;
    private final int maxEquipped;

    SkillTier(int xpCost, String color, int maxEquipped) {
        this.xpCost = xpCost;
        this.color = color;
        this.maxEquipped = maxEquipped;
    }

    public int xpCost() { return xpCost; }
    public String color() { return color; }
    public int maxEquipped() { return maxEquipped; }
    public String i18nKey() { return "tier." + name().toLowerCase(); }
}
