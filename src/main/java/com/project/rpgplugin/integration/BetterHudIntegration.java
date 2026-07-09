package com.project.rpgplugin.integration;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BetterHudIntegration {

    private final boolean enabled;

    public BetterHudIntegration() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BetterHud");
        this.enabled = plugin != null && plugin.isEnabled();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean shouldDisableBossBar() {
        return enabled;
    }
}
