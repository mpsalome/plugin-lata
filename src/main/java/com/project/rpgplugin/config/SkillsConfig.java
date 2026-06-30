package com.project.rpgplugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SkillsConfig {

    private final JavaPlugin plugin;
    private FileConfiguration skillsConfig;

    public SkillsConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) {
            plugin.saveResource("skills.yml", false);
        }
        skillsConfig = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration config() {
        return skillsConfig;
    }
}
