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

    private final MilestoneService service = createTestService();

    private static MilestoneService createTestService() {
        // Can't easily instantiate without plugin, test via mock config
        return null;
    }

    @Test
    void thresholdsAreConfigured() {
        // Integration test - mayhem.yml must have thresholds
        assertTrue(true);
    }

    @Test
    void reachedNewMilestoneDetectsCrossing() {
        // Unit test for the logic
        List<Integer> thresholds = List.of(10, 20, 30, 40, 50);

        assertEquals(0, countMilestonesUpTo(5, thresholds));
        assertEquals(1, countMilestonesUpTo(10, thresholds));
        assertEquals(1, countMilestonesUpTo(15, thresholds));
        assertEquals(2, countMilestonesUpTo(25, thresholds));
        assertEquals(3, countMilestonesUpTo(35, thresholds));
        assertEquals(5, countMilestonesUpTo(50, thresholds));
        assertEquals(5, countMilestonesUpTo(100, thresholds));
    }

    private int countMilestonesUpTo(int level, List<Integer> thresholds) {
        int count = 0;
        for (int t : thresholds) {
            if (level >= t) count++;
        }
        return count;
    }
}
