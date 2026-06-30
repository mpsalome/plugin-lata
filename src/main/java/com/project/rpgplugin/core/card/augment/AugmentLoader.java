package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.card.CardRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Set;

public final class AugmentLoader {

    private AugmentLoader() {}

    public static void load(JavaPlugin plugin, CardRegistry cardRegistry) {
        File file = new File(plugin.getDataFolder(), "augments.yml");
        if (!file.exists()) {
            plugin.saveResource("augments.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection augments = config.getConfigurationSection("");
        if (augments == null) return;

        Set<String> keys = augments.getKeys(false);
        for (String id : keys) {
            ConfigurationSection section = augments.getConfigurationSection(id);
            if (section == null) continue;
            try {
                AugmentCard card = AugmentCard.fromConfig(id, section);
                cardRegistry.register(card);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load augment '" + id + "': " + e.getMessage());
            }
        }
    }
}
