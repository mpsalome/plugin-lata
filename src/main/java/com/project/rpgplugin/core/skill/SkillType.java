package com.project.rpgplugin.core.skill;

public enum SkillType {
    EXPLORER("<aqua>", "explorer"),
    MINER("<gold>", "miner"),
    BUILDER("<green>", "builder");

    private final String color;
    private final String key;

    SkillType(String color, String key) {
        this.color = color;
        this.key = key;
    }

    public String color() { return color; }
    public String key() { return key; }
    public String i18nKey() { return "type." + key; }
}
