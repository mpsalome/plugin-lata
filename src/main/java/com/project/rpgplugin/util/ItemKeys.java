package com.project.rpgplugin.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemKeys {
    private static JavaPlugin PLUGIN;
    private static NamespacedKey RPG_BOOK;

    private ItemKeys() {}

    public static void init(JavaPlugin plugin) {
        PLUGIN = plugin;
        RPG_BOOK = new NamespacedKey(plugin, "rpg_book");
    }

    public static NamespacedKey rpgBook() {
        return RPG_BOOK;
    }

    public static boolean isRpgBook(ItemStack is) {
        if (is == null || is.getType() != Material.BOOK || !is.hasItemMeta()) return false;
        return is.getItemMeta().getPersistentDataContainer()
                .has(RPG_BOOK, PersistentDataType.BYTE);
    }

    public static NamespacedKey withKey(String key) {
        if (PLUGIN == null) throw new IllegalStateException("ItemKeys not initialized");
        return new NamespacedKey(PLUGIN, key);
    }
}
