package com.project.rpgplugin.core.draft;

import com.project.rpgplugin.core.card.CardTier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DraftWeighting {

    private final List<TierWeightEntry> entries = new ArrayList<>();
    private final Random random = new Random();

    public DraftWeighting(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "draft.yml");
        if (!file.exists()) {
            plugin.saveResource("draft.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<ConfigurationSection> weightList = (List<ConfigurationSection>) config.getList("draft.weights");
        if (weightList != null) {
            for (ConfigurationSection entry : weightList) {
                int from = entry.getInt("from");
                int to = entry.getInt("to");
                double bronze = entry.getDouble("bronze", 80);
                double silver = entry.getDouble("silver", 18);
                double gold = entry.getDouble("gold", 2);
                entries.add(new TierWeightEntry(from, to, bronze, silver, gold));
            }
        }
        if (entries.isEmpty()) {
            entries.add(new TierWeightEntry(1, 9, 80, 18, 2));
            entries.add(new TierWeightEntry(10, 19, 60, 30, 10));
            entries.add(new TierWeightEntry(20, 29, 40, 40, 20));
            entries.add(new TierWeightEntry(30, 999, 25, 40, 35));
        }
    }

    public double[] weightsFor(int level) {
        for (TierWeightEntry e : entries) {
            if (level >= e.from && level <= e.to) {
                return new double[]{e.bronze, e.silver, e.gold};
            }
        }
        TierWeightEntry last = entries.getLast();
        return new double[]{last.bronze, last.silver, last.gold};
    }

    public CardTier pickTier(double[] weights) {
        double total = weights[0] + weights[1] + weights[2];
        double roll = random.nextDouble() * total;
        if (roll < weights[0]) return CardTier.BRONZE;
        if (roll < weights[0] + weights[1]) return CardTier.SILVER;
        return CardTier.GOLD;
    }

    public static int getEveryLevels(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "draft.yml");
        if (!file.exists()) {
            plugin.saveResource("draft.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getInt("draft.every_levels", 3);
    }

    public static boolean isRerollEnabled(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "draft.yml");
        if (!file.exists()) {
            plugin.saveResource("draft.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getBoolean("draft.reroll.enabled", true);
    }

    public static int getRerollCostLevels(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "draft.yml");
        if (!file.exists()) {
            plugin.saveResource("draft.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getInt("draft.reroll.cost_levels", 1);
    }

    public static int getMaxRerollPerDraft(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "draft.yml");
        if (!file.exists()) {
            plugin.saveResource("draft.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getInt("draft.reroll.max_per_draft", 1);
    }

    public static boolean isSkipAllowed(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "draft.yml");
        if (!file.exists()) {
            plugin.saveResource("draft.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getBoolean("draft.allow_skip", true);
    }

    private record TierWeightEntry(int from, int to, double bronze, double silver, double gold) {}
}
