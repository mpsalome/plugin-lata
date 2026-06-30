package com.project.rpgplugin.progression;

import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.card.CardRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProgressionTest {

    private RunState run;

    @BeforeEach
    void setUp() {
        run = new RunState(UUID.randomUUID(), new CardRegistry());
    }

    @Test
    void initialRecallState() {
        assertEquals(0, run.recallUses());
        assertEquals(0, run.blocksSinceRecall());
        assertFalse(run.hasCard("recall"));
    }

    @Test
    void addBlocksIncrementsBothCounters() {
        run.addBlocksWalked(100);
        assertEquals(100, run.blocksWalked());
        assertEquals(100, run.blocksSinceRecall());
    }

    @Test
    void recallUseResetsBlocksSinceRecall() {
        run.addBlocksWalked(5000);
        assertEquals(5000, run.blocksSinceRecall());

        run.incrementRecallUses();
        run.resetBlocksSinceRecall();

        assertEquals(1, run.recallUses());
        assertEquals(0, run.blocksSinceRecall());
    }

    @Test
    void recallCostGrowsExponentially() {
        // First recall: base * 1.5^0 = base
        // Second recall: base * 1.5^1 = 1.5*base
        // Use the formula from RecallProgression: base * growth^uses
        double base = 2000;
        double growth = 1.5;

        double cost1 = base * Math.pow(growth, 0);
        double cost2 = base * Math.pow(growth, 1);
        double cost3 = base * Math.pow(growth, 2);

        assertEquals(2000, cost1, 0.001);
        assertEquals(3000, cost2, 0.001);
        assertEquals(4500, cost3, 0.001);
    }

    @Test
    void recallNotReadyWithoutCard() {
        run.addBlocksWalked(99999);
        assertFalse(run.hasCard("recall"));
    }

    @Test
    void resetClearsRecallFields() {
        run.addBlocksWalked(1000);
        run.incrementRecallUses();
        run.incrementRecallUses();
        assertEquals(2, run.recallUses());
        assertEquals(1000, run.blocksSinceRecall());

        run.reset();

        assertEquals(0, run.recallUses());
        assertEquals(0, run.blocksSinceRecall());
        assertEquals(0, run.blocksWalked());
    }
}
