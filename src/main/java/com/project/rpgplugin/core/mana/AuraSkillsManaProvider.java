package com.project.rpgplugin.core.mana;

import com.project.rpgplugin.RPGPlugin;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AuraSkillsManaProvider implements ManaProvider {

    private final RPGPlugin plugin;
    private final boolean available;

    public AuraSkillsManaProvider(RPGPlugin plugin) {
        this.plugin = plugin;
        Plugin asPlugin = Bukkit.getPluginManager().getPlugin("AuraSkills");
        this.available = asPlugin != null && asPlugin.isEnabled();
        if (available) {
            plugin.getLogger().info("ManaProvider: AuraSkills disponivel — usando implementacao real.");
        } else {
            plugin.getLogger().warning("ManaProvider: AuraSkills NAO encontrado. Registre o StandaloneDummyManaProvider como fallback.");
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public double getMana(Player player) {
        if (!available) return 0;
        try {
            SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
            return user != null ? user.getMana() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public double getMaxMana(Player player) {
        if (!available) return 100;
        try {
            SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
            return user != null ? user.getMaxMana() : 100;
        } catch (Exception e) {
            return 100;
        }
    }

    @Override
    public boolean hasEnoughMana(Player player, double amount) {
        if (!available || amount <= 0) return true;
        return getMana(player) >= amount;
    }

    @Override
    public boolean consumeMana(Player player, double amount) {
        if (!available || amount <= 0) return true;
        try {
            SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
            if (user == null || user.getMana() < amount) return false;
            user.setMana(user.getMana() - amount);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void addMana(Player player, double amount) {
        if (!available || amount <= 0) return;
        try {
            SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
            if (user != null) {
                user.setMana(Math.min(user.getMaxMana(), user.getMana() + amount));
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void setMana(Player player, double amount) {
        if (!available) return;
        try {
            SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
            if (user != null) {
                user.setMana(Math.max(0, Math.min(user.getMaxMana(), amount)));
            }
        } catch (Exception ignored) {}
    }

    @Override
    public String name() {
        return available ? "AuraSkills" : "AuraSkills (unavailable)";
    }
}
