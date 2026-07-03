package com.project.rpgplugin.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

public class AuraMobsBridge {

    private final boolean enabled;

    public AuraMobsBridge() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("AuraMobs");
        this.enabled = plugin != null && plugin.isEnabled();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void applyScaling(LivingEntity entity) {
        if (!enabled) return;
        try {
            Class<?> apiClass = Class.forName("dev.aurelium.auramobs.api.AuraMobsApi");
            Object api = apiClass.getMethod("get").invoke(null);
            Object levelManager = api.getClass().getMethod("getLevelManager").invoke(api);
            levelManager.getClass().getMethod("applyLevel", LivingEntity.class).invoke(levelManager, entity);
        } catch (Exception ignored) {}
    }
}
