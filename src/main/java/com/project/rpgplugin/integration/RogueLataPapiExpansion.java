package com.project.rpgplugin.integration;

import com.project.rpgplugin.core.mana.ManaService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RogueLataPapiExpansion extends PlaceholderExpansion {

    private final RunManager runManager;
    private final ManaService manaService;

    public RogueLataPapiExpansion(RunManager runManager, ManaService manaService) {
        this.runManager = runManager;
        this.manaService = manaService;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "roguelata";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Developer";
    }

    @Override
    public @NotNull String getVersion() {
        return "3.4.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        return switch (params.toLowerCase()) {
            case "mana" -> {
                if (manaService != null && manaService.isEnabled()) {
                    yield String.valueOf((int) manaService.getMana(player));
                }
                yield "0";
            }
            case "max_mana" -> {
                if (manaService != null && manaService.isEnabled()) {
                    RunState run = runManager.getRun(player);
                    yield String.valueOf((int) manaService.getMaxMana(player, run));
                }
                yield "100";
            }
            case "mana_percent" -> {
                if (manaService != null && manaService.isEnabled()) {
                    double mana = manaService.getMana(player);
                    double max = manaService.getMaxMana(player, runManager.getRun(player));
                    yield max > 0 ? String.valueOf((int) ((mana / max) * 100)) : "0";
                }
                yield "0";
            }
            case "level" -> {
                RunState run = runManager.getRun(player);
                yield run != null ? String.valueOf(run.level()) : "0";
            }
            case "health" -> String.valueOf((int) player.getHealth());
            case "max_health" -> String.valueOf((int) player.getMaxHealth());
            case "health_percent" ->
                String.valueOf((int) ((player.getHealth() / player.getMaxHealth()) * 100));
            case "has_run" -> runManager.getRun(player) != null ? "true" : "false";
            default -> null;
        };
    }
}
