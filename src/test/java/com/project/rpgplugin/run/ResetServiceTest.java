package com.project.rpgplugin.run;

import com.project.rpgplugin.core.run.RunOutcome;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.card.CardRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResetServiceTest {

    private RunState run;

    @BeforeEach
    void setUp() {
        run = new RunState(UUID.randomUUID(), new CardRegistry());
    }

    @Test
    void resetClearsOwnedCards() {
        run.addCard("swift");
        run.addCard("recall");
        run.ownedAbilities().add("dash");
        run.reset();
        assertTrue(run.ownedCards().isEmpty());
        assertTrue(run.ownedAbilities().isEmpty());
    }

    @Test
    void resetClearsCardCounts() {
        run.addCard("swift");
        run.addCard("swift");
        run.addCard("xp_boost");
        run.reset();
        assertTrue(run.cardCounts().isEmpty());
    }

    @Test
    void resetClearsMultipliers() {
        run.addMultiplier("xp_gain", 0.5);
        run.addMultiplier("lifesteal", 0.1);
        run.reset();
        assertEquals(0.0, run.getMultiplier("xp_gain"), 0.001);
        assertEquals(0.0, run.getMultiplier("lifesteal"), 0.001);
    }

    @Test
    void resetClearsOnKillEffects() {
        run.addOnKillEffect(new com.project.rpgplugin.core.card.augment.OnKillEffect("gold_drop", 1));
        run.reset();
        assertTrue(run.onKillEffects().isEmpty());
    }

    @Test
    void resetClearsModifiers() {
        run.addModifier("eternal_night");
        run.addModifier("glass_world");
        run.reset();
        assertTrue(run.activeModifiers().isEmpty());
    }

    @Test
    void resetResetsLevelAndPendingDrafts() {
        run.setLevel(25);
        run.addPendingDrafts(3);
        run.reset();
        assertEquals(1, run.level());
        assertEquals(0, run.pendingDrafts());
    }

    @Test
    void resetResetsMilestones() {
        run.setMilestonesReached(5);
        run.reset();
        assertEquals(0, run.milestonesReached());
    }

    @Test
    void resetResetsOutcomeToOngoing() {
        run.setOutcome(RunOutcome.VICTORY);
        run.reset();
        assertEquals(RunOutcome.ONGOING, run.outcome());
    }

    @Test
    void resetClearsPotionTypes() {
        run.addPotionType("SPEED");
        run.addPotionType("REGENERATION");
        run.reset();
        assertTrue(run.activePotionTypes().isEmpty());
    }

    @Test
    void resetResetsRecallAndDistanceFields() {
        run.addBlocksWalked(5000);
        run.incrementRecallUses();
        run.incrementRecallUses();
        run.setPhoenixCharge(true);
        run.reset();
        assertEquals(0, run.recallUses());
        assertEquals(0, run.blocksSinceRecall());
        assertEquals(0, run.blocksWalked());
        assertFalse(run.phoenixCharge());
    }

    @Test
    void resetResetsExtraSkillSlots() {
        run.setExtraSkillSlots(2);
        run.reset();
        assertEquals(0, run.extraSkillSlots());
    }

    @Test
    void resetClearsToggledOff() {
        run.toggle("dash");
        run.toggle("recall");
        run.reset();
        assertTrue(run.isToggledOn("dash"));
        assertTrue(run.isToggledOn("recall"));
    }
}
