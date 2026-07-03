package com.project.rpgplugin.core.draft;

import com.project.rpgplugin.AuraSkillsIntegration;
import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.CardTag;
import com.project.rpgplugin.core.card.CardTier;
import com.project.rpgplugin.core.card.StatService;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.run.RunManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DraftService {

    private final CardRegistry cardRegistry;
    private final DraftWeighting weighting;
    private final StatService statService;
    private final RunManager runManager;
    private final AuraSkillsIntegration auraSkills;
    private final Random random = new Random();
    private final Map<UUID, DraftSession> activeSessions = new HashMap<>();

    public DraftService(CardRegistry cardRegistry, DraftWeighting weighting, StatService statService, RunManager runManager, AuraSkillsIntegration auraSkills) {
        this.cardRegistry = cardRegistry;
        this.weighting = weighting;
        this.statService = statService;
        this.runManager = runManager;
        this.auraSkills = auraSkills;
    }

    public DraftSession getActiveSession(UUID playerId) {
        return activeSessions.get(playerId);
    }

    public void clearSession(UUID playerId) {
        activeSessions.remove(playerId);
    }

    private boolean cardPluginAvailable(Card c) {
        String req = c.requiredPlugin();
        return req == null || Bukkit.getPluginManager().getPlugin(req) != null;
    }

    public DraftSession roll(Player p, RunState run) {
        double[] w = weighting.weightsFor(run.level());
        List<Card> pool = cardRegistry.offerable(run).stream()
            .filter(this::cardPluginAvailable)
            .toList();
        Map<CardTag, Double> classWeights = auraSkills.getClassWeights(p);
        List<Card> picks = new ArrayList<>(3);

        while (picks.size() < 3) {
            CardTier tier = weighting.pickTier(w);
            Card c = pickFromTierWeighted(pool, tier, picks, classWeights);
            if (c != null && !picks.contains(c)) {
                picks.add(c);
            } else {
                boolean found = false;
                for (CardTier fallback : CardTier.values()) {
                    if (fallback.rank() >= tier.rank()) continue;
                    c = pickFromTierWeighted(pool, fallback, picks, classWeights);
                    if (c != null && !picks.contains(c)) {
                        picks.add(c);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    for (Card any : pool) {
                        if (!picks.contains(any)) {
                            picks.add(any);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) break;
            }
        }

        DraftSession session = new DraftSession(p.getUniqueId(), picks);
        activeSessions.put(p.getUniqueId(), session);
        return session;
    }

    public void applyChoice(Player p, RunState run, DraftSession session, int index) {
        activeSessions.remove(p.getUniqueId());
        session.choose(index);
        Card chosen = session.chosen();
        if (chosen == null) return;

        chosen.onAcquire(p, run);
        statService.recompute(p, run);

        playTierSound(p, chosen.tier());
        p.sendActionBar(Component.text("<green>✔ Draftado: " + chosen.id()));
    }

    public void skipDraft(Player p, RunState run, DraftSession session) {
        activeSessions.remove(p.getUniqueId());
        session.skip();
        double heal = 6.0;
        if (p.getHealth() + heal < p.getMaxHealth()) {
            p.setHealth(p.getHealth() + heal);
        } else {
            p.setHealth(p.getMaxHealth());
        }
        p.sendActionBar(Component.text("<gray>Draft pulado. Você recuperou <red>❤ " + (int) heal + " <gray>de vida."));
    }

    public boolean reroll(Player p, RunState run, DraftSession session) {
        int cost = DraftWeighting.getRerollCostLevels(runManager.plugin());
        if (p.getLevel() < cost) return false;
        if (session.rerollsUsed() >= DraftWeighting.getMaxRerollPerDraft(runManager.plugin())) return false;

        p.setLevel(p.getLevel() - cost);
        session.useReroll();

        Map<CardTag, Double> classWeights = auraSkills.getClassWeights(p);
        double[] w = weighting.weightsFor(run.level());
        List<Card> pool = cardRegistry.offerable(run).stream()
            .filter(this::cardPluginAvailable)
            .toList();
        List<Card> newPicks = new ArrayList<>(3);
        while (newPicks.size() < 3) {
            CardTier tier = weighting.pickTier(w);
            Card c = pickFromTierWeighted(pool, tier, newPicks, classWeights);
            if (c != null && !newPicks.contains(c)) {
                newPicks.add(c);
            } else {
                for (CardTier fallback : CardTier.values()) {
                    if (fallback.rank() >= tier.rank()) continue;
                    c = pickFromTierWeighted(pool, fallback, newPicks, classWeights);
                    if (c != null && !newPicks.contains(c)) {
                        newPicks.add(c);
                        break;
                    }
                }
            }
        }
        session.replaceOptions(newPicks);
        return true;
    }

    private Card pickFromTierWeighted(List<Card> pool, CardTier tier, List<Card> exclude, Map<CardTag, Double> classWeights) {
        List<Card> candidates = pool.stream()
            .filter(c -> c.tier() == tier && !exclude.contains(c))
            .toList();
        if (candidates.isEmpty()) return null;

        Map<Card, Double> weighted = new LinkedHashMap<>();
        double totalWeight = 0;
        for (Card c : candidates) {
            double weight = 0;
            for (CardTag tag : c.tags()) {
                Double w = classWeights.get(tag);
                if (w != null) weight += w;
            }
            if (weight <= 0) weight = 0.01;
            weighted.put(c, weight);
            totalWeight += weight;
        }

        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (Map.Entry<Card, Double> entry : weighted.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) return entry.getKey();
        }

        return candidates.getLast();
    }

    private void playTierSound(Player p, CardTier tier) {
        Sound sound = switch (tier) {
            case BRONZE -> Sound.BLOCK_NOTE_BLOCK_HAT;
            case SILVER -> Sound.BLOCK_NOTE_BLOCK_PLING;
            case GOLD -> Sound.UI_TOAST_CHALLENGE_COMPLETE;
        };
        p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
    }
}
