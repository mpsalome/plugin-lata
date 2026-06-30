package com.project.rpgplugin.core.build;

import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.CardTag;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SynergyService {

    private final CardRegistry cardRegistry;

    public SynergyService(CardRegistry cardRegistry) {
        this.cardRegistry = cardRegistry;
    }

    public Map<CardTag, Integer> countByTag(RunState run) {
        Map<CardTag, Integer> counts = new HashMap<>();
        for (String cardId : run.ownedCards()) {
            Card card = cardRegistry.byId(cardId).orElse(null);
            if (card != null) {
                for (CardTag tag : card.tags()) {
                    counts.merge(tag, 1, Integer::sum);
                }
            }
        }
        return counts;
    }

    public void applySynergies(Player p, RunState run) {
        Map<CardTag, Integer> counts = countByTag(run);
        int explorerCount = counts.getOrDefault(CardTag.EXPLORER, 0);
        int minerCount = counts.getOrDefault(CardTag.MINER, 0);
        int builderCount = counts.getOrDefault(CardTag.BUILDER, 0);

        int duration = 120;
        if (explorerCount >= 8) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 2, true, false, false));
        } else if (explorerCount >= 6) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1, true, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, 0, true, false, false));
        } else if (explorerCount >= 4) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 0, true, false, false));
        }

        if (minerCount >= 8) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, 2, true, false, false));
        } else if (minerCount >= 6) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, 1, true, false, false));
        } else if (minerCount >= 4) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, 0, true, false, false));
        }

        if (builderCount >= 8) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 1, true, false, false));
        } else if (builderCount >= 6) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 0, true, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, 0, true, false, false));
        } else if (builderCount >= 4) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 0, true, false, false));
        }
    }

    public Archetype detectArchetype(RunState run) {
        Map<CardTag, Integer> counts = countByTag(run);
        return counts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> Archetype.fromTag(e.getKey()))
            .orElse(Archetype.UNKNOWN);
    }

    public enum Archetype {
        TANK, DPS, MOBILITY, LOOT, SUSTAIN, EXPLORER, MINER, BUILDER, UNKNOWN;

        public static Archetype fromTag(CardTag tag) {
            return switch (tag) {
                case TANK -> TANK;
                case DPS -> DPS;
                case MOBILITY -> MOBILITY;
                case LOOT -> LOOT;
                case SUSTAIN -> SUSTAIN;
                case EXPLORER, MINER, BUILDER -> Archetype.valueOf(tag.name());
                default -> UNKNOWN;
            };
        }
    }
}
