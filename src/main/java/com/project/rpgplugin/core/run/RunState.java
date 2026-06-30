package com.project.rpgplugin.core.run;

import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.augment.OnKillEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RunState {

    private final UUID playerId;
    private final CardRegistry cardRegistry;
    private final Set<String> ownedCards = new HashSet<>();
    private final Map<String, Integer> cardCounts = new HashMap<>();
    private final Set<String> ownedAbilities = new HashSet<>();
    private final Map<String, Double> multipliers = new HashMap<>();
    private final List<OnKillEffect> onKillEffects = new ArrayList<>();
    private int level;
    private int pendingDrafts;
    private int milestonesReached;
    private final Set<String> activeModifiers = new LinkedHashSet<>();
    private Set<String> sharedModifiers = Set.of();
    private int xpMultTotal;
    private double cooldownMultTotal;
    private RunOutcome outcome = RunOutcome.ONGOING;
    private long startedAt;
    private boolean phoenixCharge;
    private long blocksWalked;
    private int recallUses;

    public RunState(UUID playerId, CardRegistry cardRegistry) {
        this.playerId = playerId;
        this.cardRegistry = cardRegistry;
        this.level = 1;
        this.startedAt = System.currentTimeMillis();
    }

    public UUID playerId() { return playerId; }
    public CardRegistry cardRegistry() { return cardRegistry; }
    public int level() { return level; }

    public void setLevel(int level) { this.level = level; }

    public Set<String> ownedCards() { return ownedCards; }
    public Set<String> ownedAbilities() { return ownedAbilities; }

    public boolean hasCard(String id) { return ownedCards.contains(id); }

    public int cardCount(String id) { return cardCounts.getOrDefault(id, 0); }

    public void addCard(String id) {
        if (ownedCards.add(id)) {
            cardCounts.put(id, cardCounts.getOrDefault(id, 0) + 1);
        } else {
            cardCounts.merge(id, 1, Integer::sum);
        }
    }

    public void removeCard(String id) {
        ownedCards.remove(id);
        cardCounts.remove(id);
    }

    public int pendingDrafts() { return pendingDrafts; }

    public void addPendingDrafts(int count) { this.pendingDrafts += count; }

    public void consumePendingDraft() {
        if (pendingDrafts > 0) pendingDrafts--;
    }

    public boolean hasPendingDrafts() { return pendingDrafts > 0; }

    public void addMultiplier(String key, double value) {
        multipliers.merge(key, value, Double::sum);
    }

    public void removeMultiplier(String key, double value) {
        multipliers.merge(key, -value, Double::sum);
        if (multipliers.getOrDefault(key, 0.0) <= 0.001) {
            multipliers.remove(key);
        }
    }

    public double getMultiplier(String key) {
        return multipliers.getOrDefault(key, 0.0);
    }

    public Map<String, Double> multipliers() { return Map.copyOf(multipliers); }

    public void addOnKillEffect(OnKillEffect e) { onKillEffects.add(e); }

    public void removeOnKillEffect(OnKillEffect e) { onKillEffects.remove(e); }

    public List<OnKillEffect> onKillEffects() { return List.copyOf(onKillEffects); }

    public int xpMultTotal() { return xpMultTotal; }
    public void setXpMultTotal(int value) { this.xpMultTotal = value; }

    public double cooldownMultTotal() { return cooldownMultTotal; }
    public void setCooldownMultTotal(double value) { this.cooldownMultTotal = value; }

    public int milestonesReached() { return milestonesReached; }
    public void setMilestonesReached(int v) { this.milestonesReached = v; }

    public Set<String> activeModifiers() { return activeModifiers; }
    public void addModifier(String id) { activeModifiers.add(id); }
    public void removeModifier(String id) { activeModifiers.remove(id); }

    public Set<String> sharedModifiers() { return sharedModifiers; }
    public void setSharedModifiers(Set<String> v) { this.sharedModifiers = v; }

    public RunOutcome outcome() { return outcome; }
    public void setOutcome(RunOutcome v) { this.outcome = v; }

    public long startedAt() { return startedAt; }
    public void setStartedAt(long v) { this.startedAt = v; }

    public boolean phoenixCharge() { return phoenixCharge; }
    public void setPhoenixCharge(boolean v) { this.phoenixCharge = v; }

    public long blocksWalked() { return blocksWalked; }
    public void setBlocksWalked(long v) { this.blocksWalked = v; }
    public void addBlocksWalked(long v) { this.blocksWalked += v; }

    public int recallUses() { return recallUses; }
    public void setRecallUses(int v) { this.recallUses = v; }
    public void incrementRecallUses() { this.recallUses++; }

    public void reset() {
        ownedCards.clear();
        cardCounts.clear();
        ownedAbilities.clear();
        multipliers.clear();
        onKillEffects.clear();
        level = 1;
        pendingDrafts = 0;
        milestonesReached = 0;
        activeModifiers.clear();
        sharedModifiers = Set.of();
        xpMultTotal = 0;
        cooldownMultTotal = 0;
        outcome = RunOutcome.ONGOING;
        startedAt = System.currentTimeMillis();
        phoenixCharge = false;
        blocksWalked = 0;
        recallUses = 0;
    }
}
