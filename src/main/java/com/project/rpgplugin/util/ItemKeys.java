package com.project.rpgplugin.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemKeys {
    private static JavaPlugin PLUGIN;
    private static NamespacedKey RPG_BOOK;
    private static NamespacedKey ELITE_ID;
    private static NamespacedKey IS_BOSS;
    private static NamespacedKey SKILL_ITEM;

    private ItemKeys() {}

    public static void init(JavaPlugin plugin) {
        PLUGIN = plugin;
        RPG_BOOK = new NamespacedKey(plugin, "rpg_book");
        ELITE_ID = new NamespacedKey(plugin, "elite_id");
        IS_BOSS = new NamespacedKey(plugin, "is_boss");
        SKILL_ITEM = new NamespacedKey(plugin, "skill_item");
    }

    public static NamespacedKey rpgBook() { return RPG_BOOK; }

    public static NamespacedKey eliteId() { return ELITE_ID; }

    public static NamespacedKey isBoss() { return IS_BOSS; }

    public static NamespacedKey skillItem() { return SKILL_ITEM; }

    public static boolean isRpgBook(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return false;
        return is.getItemMeta().getPersistentDataContainer()
                .has(RPG_BOOK, PersistentDataType.BYTE);
    }

    public static NamespacedKey withKey(String key) {
        if (PLUGIN == null) throw new IllegalStateException("ItemKeys not initialized");
        return new NamespacedKey(PLUGIN, key);
    }
}
