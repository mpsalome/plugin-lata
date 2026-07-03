package com.project.rpgplugin.data;

import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.run.RunOutcome;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlDataStore implements PlayerDataStore {

    private final JavaPlugin plugin;
    private final CardRegistry cardRegistry;
    private final File dataFolder;

    public YamlDataStore(JavaPlugin plugin, CardRegistry cardRegistry) {
        this.plugin = plugin;
        this.cardRegistry = cardRegistry;
        this.dataFolder = new File(plugin.getDataFolder(), "runs");
        dataFolder.mkdirs();
    }

    @Override
    public void save(UUID playerId, RunState run) {
        File file = getFile(playerId);
        YamlConfiguration config = new YamlConfiguration();
        config.set("level", run.level());
        config.set("pendingDrafts", run.pendingDrafts());
        config.set("milestonesReached", run.milestonesReached());
        config.set("phoenixCharge", run.phoenixCharge());
        config.set("blocksWalked", run.blocksWalked());
        config.set("blocksSinceRecall", run.blocksSinceRecall());
        config.set("recallUses", run.recallUses());
        config.set("startedAt", run.startedAt());
        config.set("outcome", run.outcome().name());
        config.set("cards", new ArrayList<>(run.ownedCards()));
        config.set("cardCounts", new HashMap<>(run.cardCounts()));
        config.set("ownedAbilities", new ArrayList<>(run.ownedAbilities()));
        config.set("activeModifiers", new ArrayList<>(run.activeModifiers()));
        Map<String, Double> mults = new HashMap<>();
        for (Map.Entry<String, Double> e : run.multipliers().entrySet()) {
            mults.put(e.getKey(), e.getValue());
        }
        config.set("multipliers", mults);

        SchedulerUtil.runAsync(plugin, () -> {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save run for " + playerId + ": " + e.getMessage());
            }
        });
    }

    @Override
    public Optional<RunState> load(UUID playerId) {
        File file = getFile(playerId);
        if (!file.exists()) return Optional.empty();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        RunState run = new RunState(playerId, cardRegistry);
        run.setLevel(config.getInt("level", 1));
        run.addPendingDrafts(config.getInt("pendingDrafts", 0));
        run.setMilestonesReached(config.getInt("milestonesReached", 0));
        run.setPhoenixCharge(config.getBoolean("phoenixCharge", false));
        run.setBlocksWalked(config.getLong("blocksWalked", 0));
        run.setBlocksSinceRecall(config.getLong("blocksSinceRecall", 0));
        run.setRecallUses(config.getInt("recallUses", 0));
        run.setStartedAt(config.getLong("startedAt", System.currentTimeMillis()));
        try {
            run.setOutcome(RunOutcome.valueOf(config.getString("outcome", "ONGOING")));
        } catch (IllegalArgumentException e) {
            run.setOutcome(RunOutcome.ONGOING);
        }

        List<String> cards = config.getStringList("cards");
        for (String c : cards) {
            run.addCard(c);
        }

        var cardCounts = config.getConfigurationSection("cardCounts");
        if (cardCounts != null) {
            for (String key : cardCounts.getKeys(false)) {
                int count = cardCounts.getInt(key);
                for (int i = 0; i < count; i++) {
                    run.addCard(key);
                }
            }
        }

        List<String> abilities = config.getStringList("ownedAbilities");
        abilities.forEach(a -> run.ownedAbilities().add(a));

        List<String> modifiers = config.getStringList("activeModifiers");
        modifiers.forEach(m -> run.addModifier(m));

        var multipliers = config.getConfigurationSection("multipliers");
        if (multipliers != null) {
            for (String key : multipliers.getKeys(false)) {
                run.addMultiplier(key, multipliers.getDouble(key));
            }
        }

        return Optional.of(run);
    }

    @Override
    public void delete(UUID playerId) {
        File file = getFile(playerId);
        if (file.exists()) file.delete();
    }

    @Override
    public void flushAll() {
        plugin.getLogger().info("YamlDataStore: flush completed (async saves were dispatched).");
    }

    private File getFile(UUID playerId) {
        return new File(dataFolder, playerId.toString() + ".yml");
    }
}
