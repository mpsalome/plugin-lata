package com.project.rpgplugin.draft;

import com.project.rpgplugin.core.card.CardKind;
import com.project.rpgplugin.core.card.CardTier;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.CardTag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RunStateTest {

    private RunState run;
    private CardRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CardRegistry();
        run = new RunState(UUID.randomUUID(), registry);
    }

    @Test
    void initialStateIsEmpty() {
        assertTrue(run.ownedCards().isEmpty());
        assertEquals(0, run.pendingDrafts());
        assertEquals(1, run.level());
    }

    @Test
    void addAndRemoveCard() {
        run.addCard("test_card");
        assertTrue(run.hasCard("test_card"));
        assertEquals(1, run.cardCount("test_card"));

        run.addCard("test_card"); // stack
        assertEquals(2, run.cardCount("test_card"));

        run.removeCard("test_card");
        assertFalse(run.hasCard("test_card"));
    }

    @Test
    void pendingDraftsQueue() {
        assertEquals(0, run.pendingDrafts());
        run.addPendingDrafts(3);
        assertEquals(3, run.pendingDrafts());
        assertTrue(run.hasPendingDrafts());

        run.consumePendingDraft();
        assertEquals(2, run.pendingDrafts());
    }

    @Test
    void multipliersWork() {
        run.addMultiplier("xp_gain", 0.1);
        assertEquals(0.1, run.getMultiplier("xp_gain"), 0.001);

        run.addMultiplier("xp_gain", 0.2);
        assertEquals(0.3, run.getMultiplier("xp_gain"), 0.001);

        run.removeMultiplier("xp_gain", 0.1);
        assertEquals(0.2, run.getMultiplier("xp_gain"), 0.001);
    }

    @Test
    void levelUpdates() {
        assertEquals(1, run.level());
        run.setLevel(10);
        assertEquals(10, run.level());
    }

    @Test
    void resetClearsEverything() {
        run.addCard("card1");
        run.addCard("card2");
        run.addPendingDrafts(2);
        run.addMultiplier("xp_gain", 0.3);
        run.setLevel(15);

        run.reset();

        assertTrue(run.ownedCards().isEmpty());
        assertEquals(0, run.pendingDrafts());
        assertEquals(1, run.level());
        assertEquals(0.0, run.getMultiplier("xp_gain"), 0.001);
    }
}
