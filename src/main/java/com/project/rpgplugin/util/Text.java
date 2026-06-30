package com.project.rpgplugin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Text {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Text() {}

    public static Component mm(String s) {
        return MM.deserialize(s).decoration(TextDecoration.ITALIC, false);
    }

    public static String legacyToMiniMessage(String legacy) {
        return legacy
                .replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§k", "<obf>")
                .replace("§l", "<bold>")
                .replace("§m", "<strike>")
                .replace("§n", "<u>")
                .replace("§o", "<i>")
                .replace("§r", "<reset>");
    }
}
