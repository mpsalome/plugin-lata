package com.project.rpgplugin.core.run;

import com.project.rpgplugin.core.card.CardRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunPersistenceService {

    private static final String PREFIX = "run_";

    private final CardRegistry cardRegistry;

    // Simple typed keys
    private final NamespacedKey hasRunKey;
    private final NamespacedKey levelKey;
    private final NamespacedKey pendingDraftsKey;
    private final NamespacedKey milestonesKey;
    private final NamespacedKey phoenixKey;
    private final NamespacedKey startedAtKey;
    private final NamespacedKey blocksWalkedKey;
    private final NamespacedKey blocksSinceRecallKey;
    private final NamespacedKey recallUsesKey;
    private final NamespacedKey extraSlotsKey;
    private final NamespacedKey skipHealthKey;
    private final NamespacedKey extraDraftKey;
    private final NamespacedKey outcomeKey;
    private final NamespacedKey xpMultKey;
    private final NamespacedKey cooldownMultKey;

    private final NamespacedKey freeRerollsKey;

    // String list keys (comma-separated)
    private final NamespacedKey cardsKey;
    private final NamespacedKey cardCountsKey;
    private final NamespacedKey abilitiesKey;
    private final NamespacedKey multipliersKey;
    private final NamespacedKey modifiersKey;
    private final NamespacedKey toggledOffKey;

    @ApiStatus.Internal
    public RunPersistenceService(JavaPlugin plugin, CardRegistry cardRegistry) {
        this.cardRegistry = cardRegistry;

        this.hasRunKey = new NamespacedKey(plugin, PREFIX + "has_run");
        this.levelKey = new NamespacedKey(plugin, PREFIX + "level");
        this.pendingDraftsKey = new NamespacedKey(plugin, PREFIX + "pending_drafts");
        this.milestonesKey = new NamespacedKey(plugin, PREFIX + "milestones");
        this.phoenixKey = new NamespacedKey(plugin, PREFIX + "phoenix");
        this.startedAtKey = new NamespacedKey(plugin, PREFIX + "started_at");
        this.blocksWalkedKey = new NamespacedKey(plugin, PREFIX + "blocks_walked");
        this.blocksSinceRecallKey = new NamespacedKey(plugin, PREFIX + "blocks_since_recall");
        this.recallUsesKey = new NamespacedKey(plugin, PREFIX + "recall_uses");
        this.extraSlotsKey = new NamespacedKey(plugin, PREFIX + "extra_slots");
        this.skipHealthKey = new NamespacedKey(plugin, PREFIX + "skip_health");
        this.extraDraftKey = new NamespacedKey(plugin, PREFIX + "extra_draft");
        this.freeRerollsKey = new NamespacedKey(plugin, PREFIX + "free_rerolls");
        this.outcomeKey = new NamespacedKey(plugin, PREFIX + "outcome");
        this.xpMultKey = new NamespacedKey(plugin, PREFIX + "xp_mult");
        this.cooldownMultKey = new NamespacedKey(plugin, PREFIX + "cooldown_mult");
        this.cardsKey = new NamespacedKey(plugin, PREFIX + "cards");
        this.cardCountsKey = new NamespacedKey(plugin, PREFIX + "card_counts");
        this.abilitiesKey = new NamespacedKey(plugin, PREFIX + "abilities");
        this.multipliersKey = new NamespacedKey(plugin, PREFIX + "multipliers");
        this.modifiersKey = new NamespacedKey(plugin, PREFIX + "modifiers");
        this.toggledOffKey = new NamespacedKey(plugin, PREFIX + "toggled_off");
    }

    public boolean hasRun(Player player) {
        return player.getPersistentDataContainer().has(hasRunKey, PersistentDataType.BYTE);
    }

    public void saveRun(Player player, RunState run) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        pdc.set(hasRunKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(levelKey, PersistentDataType.INTEGER, run.level());
        pdc.set(pendingDraftsKey, PersistentDataType.INTEGER, run.pendingDrafts());
        pdc.set(milestonesKey, PersistentDataType.INTEGER, run.milestonesReached());
        pdc.set(phoenixKey, PersistentDataType.BYTE, run.phoenixCharge() ? (byte) 1 : 0);
        pdc.set(startedAtKey, PersistentDataType.LONG, run.startedAt());
        pdc.set(blocksWalkedKey, PersistentDataType.LONG, run.blocksWalked());
        pdc.set(blocksSinceRecallKey, PersistentDataType.LONG, run.blocksSinceRecall());
        pdc.set(recallUsesKey, PersistentDataType.INTEGER, run.recallUses());
        pdc.set(extraSlotsKey, PersistentDataType.INTEGER, run.extraSkillSlots());
        pdc.set(skipHealthKey, PersistentDataType.DOUBLE, run.skipHealthBonus());
        pdc.set(extraDraftKey, PersistentDataType.INTEGER, run.extraDraftSlots());
        pdc.set(freeRerollsKey, PersistentDataType.INTEGER, run.freeRerolls());
        pdc.set(outcomeKey, PersistentDataType.STRING, run.outcome().name());
        pdc.set(xpMultKey, PersistentDataType.INTEGER, run.xpMultTotal());
        pdc.set(cooldownMultKey, PersistentDataType.DOUBLE, run.cooldownMultTotal());
        pdc.set(cardsKey, PersistentDataType.STRING, String.join(",", run.ownedCards()));
        pdc.set(cardCountsKey, PersistentDataType.STRING, encodeIntMap(run.cardCounts()));
        pdc.set(abilitiesKey, PersistentDataType.STRING, String.join(",", run.ownedAbilities()));
        pdc.set(multipliersKey, PersistentDataType.STRING, encodeDoubleMap(run.multipliers()));
        pdc.set(modifiersKey, PersistentDataType.STRING, String.join(",", run.activeModifiers()));
        pdc.set(toggledOffKey, PersistentDataType.STRING, String.join(",", run.toggledOff()));
    }

    @Nullable
    public RunState loadRun(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!pdc.has(hasRunKey, PersistentDataType.BYTE)) return null;

        RunState run = new RunState(player.getUniqueId(), cardRegistry);

        run.setLevel(pdc.getOrDefault(levelKey, PersistentDataType.INTEGER, 1));
        run.addPendingDrafts(pdc.getOrDefault(pendingDraftsKey, PersistentDataType.INTEGER, 0));
        run.setMilestonesReached(pdc.getOrDefault(milestonesKey, PersistentDataType.INTEGER, 0));
        run.setPhoenixCharge(pdc.getOrDefault(phoenixKey, PersistentDataType.BYTE, (byte) 0) == 1);
        run.setStartedAt(pdc.getOrDefault(startedAtKey, PersistentDataType.LONG, System.currentTimeMillis()));
        run.setBlocksWalked(pdc.getOrDefault(blocksWalkedKey, PersistentDataType.LONG, 0L));
        run.setBlocksSinceRecall(pdc.getOrDefault(blocksSinceRecallKey, PersistentDataType.LONG, 0L));
        run.setRecallUses(pdc.getOrDefault(recallUsesKey, PersistentDataType.INTEGER, 0));
        run.setExtraSkillSlots(pdc.getOrDefault(extraSlotsKey, PersistentDataType.INTEGER, 0));
        run.setSkipHealthBonus(pdc.getOrDefault(skipHealthKey, PersistentDataType.DOUBLE, 0.0));
        run.setExtraDraftSlots(pdc.getOrDefault(extraDraftKey, PersistentDataType.INTEGER, 0));
        run.setFreeRerolls(pdc.getOrDefault(freeRerollsKey, PersistentDataType.INTEGER, 0));

        String outcomeStr = pdc.getOrDefault(outcomeKey, PersistentDataType.STRING, "ONGOING");
        try {
            run.setOutcome(RunOutcome.valueOf(outcomeStr));
        } catch (IllegalArgumentException e) {
            run.setOutcome(RunOutcome.ONGOING);
        }

        run.setXpMultTotal(pdc.getOrDefault(xpMultKey, PersistentDataType.INTEGER, 0));
        run.setCooldownMultTotal(pdc.getOrDefault(cooldownMultKey, PersistentDataType.DOUBLE, 0.0));

        // Cards — use card_counts as source of truth
        String cardCountsStr = pdc.getOrDefault(cardCountsKey, PersistentDataType.STRING, "");
        if (!cardCountsStr.isEmpty()) {
            for (String entry : cardCountsStr.split(",")) {
                String[] parts = entry.split(":", 2);
                if (parts.length == 2) {
                    int count = parseInt(parts[1], 1);
                    String id = parts[0];
                    for (int i = 0; i < count; i++) {
                        run.addCard(id);
                    }
                }
            }
        }

        // Fallback: if no card_counts but old cards key exists
        if (run.ownedCards().isEmpty()) {
            String cardsStr = pdc.getOrDefault(cardsKey, PersistentDataType.STRING, "");
            if (!cardsStr.isEmpty()) {
                for (String id : cardsStr.split(",")) {
                    run.addCard(id);
                }
            }
        }

        // Abilities
        String abilitiesStr = pdc.getOrDefault(abilitiesKey, PersistentDataType.STRING, "");
        if (!abilitiesStr.isEmpty()) {
            run.ownedAbilities().clear();
            Collections.addAll(run.ownedAbilities(), abilitiesStr.split(","));
        }

        // Multipliers
        String multsStr = pdc.getOrDefault(multipliersKey, PersistentDataType.STRING, "");
        if (!multsStr.isEmpty()) {
            for (String entry : multsStr.split(",")) {
                String[] parts = entry.split(":", 2);
                if (parts.length == 2) {
                    run.addMultiplier(parts[0], parseDouble(parts[1], 0.0));
                }
            }
        }

        // Active modifiers
        String modsStr = pdc.getOrDefault(modifiersKey, PersistentDataType.STRING, "");
        if (!modsStr.isEmpty()) {
            run.activeModifiers().clear();
            Collections.addAll(run.activeModifiers(), modsStr.split(","));
        }

        // Toggled off
        String toggledStr = pdc.getOrDefault(toggledOffKey, PersistentDataType.STRING, "");
        if (!toggledStr.isEmpty()) {
            run.toggledOff().clear();
            Collections.addAll(run.toggledOff(), toggledStr.split(","));
        }

        return run;
    }

    public void clearRun(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.remove(hasRunKey);
        pdc.remove(levelKey);
        pdc.remove(pendingDraftsKey);
        pdc.remove(milestonesKey);
        pdc.remove(phoenixKey);
        pdc.remove(startedAtKey);
        pdc.remove(blocksWalkedKey);
        pdc.remove(blocksSinceRecallKey);
        pdc.remove(recallUsesKey);
        pdc.remove(extraSlotsKey);
        pdc.remove(skipHealthKey);
        pdc.remove(extraDraftKey);
        pdc.remove(freeRerollsKey);
        pdc.remove(outcomeKey);
        pdc.remove(xpMultKey);
        pdc.remove(cooldownMultKey);
        pdc.remove(cardsKey);
        pdc.remove(cardCountsKey);
        pdc.remove(abilitiesKey);
        pdc.remove(multipliersKey);
        pdc.remove(modifiersKey);
        pdc.remove(toggledOffKey);
    }

    // --- encoding helpers ---

    private static String encodeIntMap(Map<String, Integer> map) {
        if (map.isEmpty()) return "";
        return map.entrySet().stream()
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining(","));
    }

    private static String encodeDoubleMap(Map<String, Double> map) {
        if (map.isEmpty()) return "";
        return map.entrySet().stream()
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining(","));
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    private static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return def; }
    }
}
