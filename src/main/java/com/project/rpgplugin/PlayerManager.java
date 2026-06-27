package com.project.rpgplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlayerManager {

    private final RPGPlugin plugin;
    private final NamespacedKey classKey;

    public PlayerManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.classKey = new NamespacedKey(plugin, "rpg_class");
    }

    /**
     * Set a player's RPG class using PersistentDataContainer.
     * @param player The Minecraft Player
     * @param className The class name (EXPLORER, MINER, BUILDER, NONE)
     */
    public void setPlayerClass(Player player, String className) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (className == null || className.equalsIgnoreCase("NONE")) {
            data.remove(classKey);
        } else {
            data.set(classKey, PersistentDataType.STRING, className.toUpperCase());
        }
    }

    /**
     * Get the player's current RPG class from PersistentDataContainer.
     * @param player The Minecraft Player
     * @return The class name (defaulting to "NONE")
     */
    public String getPlayerClass(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (!data.has(classKey, PersistentDataType.STRING)) {
            return "NONE";
        }
        String rpgClass = data.get(classKey, PersistentDataType.STRING);
        return rpgClass != null ? rpgClass : "NONE";
    }

    /**
     * Checks if a player has a specific RPG class.
     */
    public boolean hasClass(Player player, String className) {
        return getPlayerClass(player).equalsIgnoreCase(className);
    }
}
