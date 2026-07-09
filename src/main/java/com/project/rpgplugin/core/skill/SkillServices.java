package com.project.rpgplugin.core.skill;

import com.project.rpgplugin.RPGPlugin;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkillServices {

    private final RPGPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final Set<Location> reinforcedBlocks = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> moltenTouchActiveUntil = new ConcurrentHashMap<>();

    public SkillServices(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    public RPGPlugin plugin() { return plugin; }

    // -- Cooldowns --

    public boolean isOnCooldown(UUID playerId, String skillId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return false;
        Long until = playerCooldowns.get(skillId);
        return until != null && System.currentTimeMillis() < until;
    }

    public long cooldownRemaining(UUID playerId, String skillId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return 0;
        Long until = playerCooldowns.get(skillId);
        if (until == null) return 0;
        long rem = until - System.currentTimeMillis();
        return Math.max(0, rem);
    }

    public void startCooldown(UUID playerId, String skillId, Duration duration) {
        cooldowns.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
            .put(skillId, System.currentTimeMillis() + duration.toMillis());
    }

    public void clearPlayerCooldowns(UUID playerId) {
        cooldowns.remove(playerId);
    }

    public void clearAllCooldowns() {
        cooldowns.clear();
    }

    public void clearMoltenTouchAll() {
        moltenTouchActiveUntil.clear();
    }

    // -- Molten Touch state --

    public boolean isMoltenTouchActive(UUID playerId) {
        Long until = moltenTouchActiveUntil.get(playerId);
        return until != null && System.currentTimeMillis() < until;
    }

    public void activateMoltenTouch(UUID playerId, long durationMillis) {
        moltenTouchActiveUntil.put(playerId, System.currentTimeMillis() + durationMillis);
    }

    // -- Reinforced blocks --

    public boolean isReinforced(Location loc) {
        return reinforcedBlocks.contains(loc);
    }

    public void addReinforcedBlock(Location loc) {
        reinforcedBlocks.add(loc);
    }

    public void removeReinforcedBlock(Location loc) {
        reinforcedBlocks.remove(loc);
    }

    public Set<Location> reinforcedBlocks() {
        return reinforcedBlocks;
    }

    // -- Config --

    public FileConfiguration config() {
        return plugin.getConfig();
    }

    public ConfigurationSection skillConfig(String skillId) {
        for (String cls : new String[]{"explorer", "miner", "builder"}) {
            String path = "classes." + cls + "." + skillId;
            if (plugin.getConfig().contains(path)) {
                return plugin.getConfig().getConfigurationSection(path);
            }
        }
        return null;
    }

    // -- Feedback --

    public void feedback(Player player, String message, Sound sound) {
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    // -- Difficulty (from old PlayerManager) --

    public double getDifficultyDamageMultiplier(int unlockedCount) {
        return 1.0 + (unlockedCount * 0.02);
    }

    public double getDifficultyHungerMultiplier(int unlockedCount) {
        return 1.0 + (unlockedCount * 0.015);
    }
}
