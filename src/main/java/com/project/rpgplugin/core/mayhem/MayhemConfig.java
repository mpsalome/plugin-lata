package com.project.rpgplugin.core.mayhem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MayhemConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public MayhemConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "mayhem.yml");
        if (!file.exists()) {
            plugin.saveResource("mayhem.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public String scope() {
        return config.getString("mayhem.scope", "server");
    }

    public String milestoneType() {
        return config.getString("mayhem.milestones.type", "level");
    }

    public List<Integer> thresholds() {
        return config.getIntegerList("mayhem.milestones.thresholds");
    }

    public boolean announce() {
        return config.getBoolean("mayhem.announce", true);
    }

    public int maxActive() {
        return config.getInt("mayhem.max_active", 6);
    }

    public Map<Integer, List<ModifierSeverity>> severityByIndex() {
        Map<Integer, List<ModifierSeverity>> result = new LinkedHashMap<>();
        for (var entry : config.getMapList("mayhem.severity_by_index")) {
            if (entry.containsKey("index") && entry.containsKey("allow")) {
                Object rawIndex = entry.get("index");
                int index;
                if (rawIndex instanceof Number n) {
                    index = n.intValue();
                } else {
                    continue;
                }
                @SuppressWarnings("unchecked")
                List<String> allow = (List<String>) entry.get("allow");
                List<ModifierSeverity> sevs = allow.stream()
                    .map(String::toUpperCase)
                    .map(s -> {
                        try { return ModifierSeverity.valueOf(s); }
                        catch (IllegalArgumentException e) { return null; }
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();
                if (!sevs.isEmpty()) result.put(index, sevs);
            }
        }
        return result;
    }

    public Set<String> incompatibilities() {
        return Set.copyOf(config.getStringList("mayhem.incompatibilities"));
    }

    public List<Integer> getDisabledModifiers() {
        return config.getIntegerList("mayhem.disabled_modifiers");
    }
}
