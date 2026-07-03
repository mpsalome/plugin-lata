package com.project.rpgplugin.core.mana;

import com.project.rpgplugin.AuraSkillsIntegration;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ManaService {

    private final JavaPlugin plugin;
    private final AuraSkillsIntegration auraSkills;
    private final Map<String, Double> manaCosts = new HashMap<>();
    private boolean enabled;

    public ManaService(JavaPlugin plugin, AuraSkillsIntegration auraSkills) {
        this.plugin = plugin;
        this.auraSkills = auraSkills;
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
        enabled = auraSkills != null && auraSkills.isEnabled() && !manaCosts.isEmpty();
        plugin.getLogger().info("ManaService loaded " + manaCosts.size() + " ability costs. AuraSkills=" + enabled);
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
        if (!enabled) return true;
        try {
            var user = dev.aurelium.auraskills.api.AuraSkillsApi.get().getUser(p.getUniqueId());
            return user.getMana() >= cost;
        } catch (Exception e) {
            return true;
        }
    }

    public boolean tryConsumeMana(Player p, double cost) {
        if (!enabled || cost <= 0) return true;
        try {
            var user = dev.aurelium.auraskills.api.AuraSkillsApi.get().getUser(p.getUniqueId());
            if (user.getMana() < cost) return false;
            user.setMana(user.getMana() - cost);
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public double getMana(Player p) {
        if (!enabled) return 0;
        try {
            var user = dev.aurelium.auraskills.api.AuraSkillsApi.get().getUser(p.getUniqueId());
            return user.getMana();
        } catch (Exception e) {
            return 0;
        }
    }

    public double getMaxMana(Player p, RunState run) {
        double base = 100;
        double bonus = run.getMultiplier("mana_max"); // from mana_pool augment
        double total = base + bonus;
        if (enabled) {
            try {
                var user = dev.aurelium.auraskills.api.AuraSkillsApi.get().getUser(p.getUniqueId());
                double regenMult = 1.0 + run.getMultiplier("mana_regen_mult"); // from mayhem modifiers
                // AuraSkills handles its own regen, we just sync max mana if possible
                total = Math.max(total, user.getMaxMana());
            } catch (Exception ignored) {
            }
        }
        return total;
    }
}
