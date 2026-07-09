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
    private static NamespacedKey SHOP_ITEM;
    private static NamespacedKey BOSS_BEACON;

    private ItemKeys() {}

    public static void init(JavaPlugin plugin) {
        PLUGIN = plugin;
        RPG_BOOK = new NamespacedKey(plugin, "rpg_book");
        ELITE_ID = new NamespacedKey(plugin, "elite_id");
        IS_BOSS = new NamespacedKey(plugin, "is_boss");
        SKILL_ITEM = new NamespacedKey(plugin, "skill_item");
        SHOP_ITEM = new NamespacedKey(plugin, "shop_item");
        BOSS_BEACON = new NamespacedKey(plugin, "boss_beacon");
    }

    public static NamespacedKey rpgBook() { return RPG_BOOK; }

    public static NamespacedKey eliteId() { return ELITE_ID; }

    public static NamespacedKey isBoss() { return IS_BOSS; }

    public static NamespacedKey skillItem() { return SKILL_ITEM; }

    public static NamespacedKey shopItem() { return SHOP_ITEM; }

    public static NamespacedKey bossBeacon() { return BOSS_BEACON; }

    public static boolean isRpgBook(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return false;
        return is.getItemMeta().getPersistentDataContainer()
                .has(RPG_BOOK, PersistentDataType.BYTE);
    }

    public static boolean isShopItem(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return false;
        return is.getItemMeta().getPersistentDataContainer()
                .has(SHOP_ITEM, PersistentDataType.BYTE);
    }

    public static boolean isBossBeacon(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return false;
        return is.getItemMeta().getPersistentDataContainer()
                .has(BOSS_BEACON, PersistentDataType.BYTE);
    }

    public static NamespacedKey withKey(String key) {
        if (PLUGIN == null) throw new IllegalStateException("ItemKeys not initialized");
        return new NamespacedKey(PLUGIN, key);
    }
}
