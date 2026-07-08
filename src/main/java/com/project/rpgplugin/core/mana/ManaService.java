package com.project.rpgplugin.core.mana;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ManaService {

    private final JavaPlugin plugin;
    private final ManaProvider provider;
    private final Map<String, Double> manaCosts = new HashMap<>();
    private boolean enabled;

    public ManaService(JavaPlugin plugin, ManaProvider provider) {
        this.plugin = plugin;
        this.provider = provider;
        loadConfig();
    }

    private void loadConfig() {
        File file = new File(plugin.getDataFolder(), "mana_abilities.yml");
        if (!file.exists()) {
            plugin.saveResource("mana_abilities.yml", false);
            file = new File(plugin.getDataFolder(), "mana_abilities.yml");
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            double cost = config.getDouble(key, 0);
            if (cost > 0) {
                manaCosts.put(key, cost);
            }
        }
        enabled = provider.isAvailable() && !manaCosts.isEmpty();
        plugin.getLogger().info("ManaService loaded " + manaCosts.size() + " ability costs. Provider=" + provider.name() + " enabled=" + enabled);
    }

    public ManaProvider provider() {
        return provider;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getManaCost(String abilityId) {
        return manaCosts.getOrDefault(abilityId, 0.0);
    }

    public boolean hasCost(String abilityId) {
        return manaCosts.containsKey(abilityId);
    }

    public boolean hasEnoughMana(Player p, double cost) {
        if (!enabled || cost <= 0) return true;
        return provider.hasEnoughMana(p, cost);
    }

    public boolean tryConsumeMana(Player p, double cost) {
        if (!enabled || cost <= 0) return true;
        return provider.consumeMana(p, cost);
    }

    public double getMana(Player p) {
        if (!enabled) return 0;
        return provider.getMana(p);
    }

    public double getMaxMana(Player p, RunState run) {
        double base = provider.isAvailable() ? provider.getMaxMana(p) : 100;
        double bonus = run != null ? run.getMultiplier("mana_max") : 0;
        return Math.max(100, base + bonus);
    }
}
