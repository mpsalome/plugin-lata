package com.project.rpgplugin.draft;

import com.project.rpgplugin.core.card.CardTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DraftWeightingTest {

    @Test
    void pickTierBronzeIsMostCommonInEarlyGame() {
        double[] weights = {80, 18, 2};
        int bronze = 0, silver = 0, gold = 0;
        for (int i = 0; i < 10000; i++) {
            CardTier tier = pickTier(weights);
            switch (tier) {
                case BRONZE -> bronze++;
                case SILVER -> silver++;
                case GOLD -> gold++;
            }
        }
        assertTrue(bronze > silver && bronze > gold, "Bronze should be most common (" + bronze + ")");
        assertTrue(gold < silver, "Gold should be rarest (" + gold + " vs " + silver + ")");
    }

    @Test
    void pickTierGoldIsMoreCommonInLateGame() {
        double[] weights = {25, 40, 35};
        int bronze = 0, silver = 0, gold = 0;
        for (int i = 0; i < 10000; i++) {
            CardTier tier = pickTier(weights);
            switch (tier) {
                case BRONZE -> bronze++;
                case SILVER -> silver++;
                case GOLD -> gold++;
            }
        }
        assertTrue(gold >= bronze * 0.5, "Gold should be common in late game (" + gold + " vs " + bronze + ")");
    }

    @Test
    void tierRankIsConsistent() {
        assertTrue(CardTier.BRONZE.rank() < CardTier.SILVER.rank());
        assertTrue(CardTier.SILVER.rank() < CardTier.GOLD.rank());
    }

    private CardTier pickTier(double[] weights) {
        double total = weights[0] + weights[1] + weights[2];
        double roll = Math.random() * total;
        if (roll < weights[0]) return CardTier.BRONZE;
        if (roll < weights[0] + weights[1]) return CardTier.SILVER;
        return CardTier.GOLD;
    }
}
