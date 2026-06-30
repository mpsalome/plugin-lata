package com.project.rpgplugin.mayhem;

import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.card.CardRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MayhemRunStateTest {

    private RunState run;

    @BeforeEach
    void setUp() {
        run = new RunState(UUID.randomUUID(), new CardRegistry());
    }

    @Test
    void modifiersStartEmpty() {
        assertTrue(run.activeModifiers().isEmpty());
    }

    @Test
    void addAndClearModifiers() {
        run.addModifier("eternal_night");
        run.addModifier("double_xp");
        assertEquals(2, run.activeModifiers().size());
        assertTrue(run.activeModifiers().contains("eternal_night"));

        run.activeModifiers().clear();
        assertTrue(run.activeModifiers().isEmpty());
    }

    @Test
    void milestonesReached() {
        assertEquals(0, run.milestonesReached());
        run.setMilestonesReached(3);
        assertEquals(3, run.milestonesReached());
    }

    @Test
    void resetClearsModifiers() {
        run.addModifier("glass_world");
        run.setMilestonesReached(2);
        run.reset();
        assertTrue(run.activeModifiers().isEmpty());
        assertEquals(0, run.milestonesReached());
    }
}
