package com.project.rpgplugin.integration;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BetterHudIntegration {

    private final boolean betterHudEnabled;
    private final boolean placeholderApiEnabled;

    public BetterHudIntegration() {
        Plugin bh = Bukkit.getPluginManager().getPlugin("BetterHud");
        this.betterHudEnabled = bh != null && bh.isEnabled();
        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        this.placeholderApiEnabled = papi != null && papi.isEnabled();
    }

    public boolean isBetterHudEnabled() {
        return betterHudEnabled;
    }

    public boolean isPlaceholderApiEnabled() {
        return placeholderApiEnabled;
    }

    /**
     * Só desabilita a BossBar nativa se BetterHud estiver presente
     * E PlaceholderAPI também estiver instalada (para fornecer placeholders).
     * Sem PlaceholderAPI, o BetterHud não consegue consumir dados do RogueLata,
     * então mantemos a BossBar nativa ativa.
     */
    public boolean shouldDisableBossBar() {
        return betterHudEnabled && placeholderApiEnabled;
    }
}
