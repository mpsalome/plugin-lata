package com.project.rpgplugin.mayhem;

import com.project.rpgplugin.core.mayhem.MayhemConfig;
import com.project.rpgplugin.core.mayhem.MilestoneService;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.card.CardRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MilestoneServiceTest {

    private final List<Integer> thresholds = List.of(10, 20, 30, 40, 50);

    @Test
    void zeroMilestonesBelowFirstThreshold() {
        assertEquals(0, countMilestonesUpTo(5));
    }

    @Test
    void exactThresholdCountsAsReached() {
        assertEquals(1, countMilestonesUpTo(10));
    }

    @Test
    void betweenThresholds() {
        assertEquals(1, countMilestonesUpTo(15));
    }

    @Test
    void secondThresholdAtLevel20() {
        assertEquals(2, countMilestonesUpTo(25));
    }

    @Test
    void thirdThresholdAtLevel30() {
        assertEquals(3, countMilestonesUpTo(35));
    }

    @Test
    void allFiveThresholdsAtLevel50() {
        assertEquals(5, countMilestonesUpTo(50));
    }

    @Test
    void cappedAtThresholdCount() {
        assertEquals(5, countMilestonesUpTo(100));
    }

    @Test
    void zeroForNegativeLevel() {
        assertEquals(0, countMilestonesUpTo(-1));
    }

    @Test
    void zeroForLevelZero() {
        assertEquals(0, countMilestonesUpTo(0));
    }

    @Test
    void milestonesPersistInRunState() {
        RunState run = new RunState(UUID.randomUUID(), new CardRegistry());
        assertEquals(0, run.milestonesReached());
        run.setMilestonesReached(3);
        assertEquals(3, run.milestonesReached());
    }

    @Test
    void milestonesResetOnRunReset() {
        RunState run = new RunState(UUID.randomUUID(), new CardRegistry());
        run.setMilestonesReached(4);
        run.reset();
        assertEquals(0, run.milestonesReached());
    }

    @Test
    void milestoneTriggersMayhemState() {
        RunState run = new RunState(UUID.randomUUID(), new CardRegistry());
        run.setMilestonesReached(2);
        run.addModifier("test_mayhem");
        assertTrue(run.activeModifiers().contains("test_mayhem"));
    }

    private int countMilestonesUpTo(int level) {
        int count = 0;
        for (int t : thresholds) {
            if (level >= t) count++;
        }
        return count;
    }
}
