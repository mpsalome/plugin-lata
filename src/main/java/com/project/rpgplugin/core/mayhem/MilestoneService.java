package com.project.rpgplugin.core.mayhem;

import com.project.rpgplugin.core.run.RunState;

import java.util.List;

public class MilestoneService {

    private final MayhemConfig config;

    public MilestoneService(MayhemConfig config) {
        this.config = config;
    }

    public List<Integer> thresholds() {
        return config.thresholds();
    }

    public int milestonesReached(RunState run) {
        int level = run.level();
        List<Integer> thresholds = config.thresholds();
        int count = 0;
        for (int t : thresholds) {
            if (level >= t) count++;
        }
        return count;
    }

    public boolean reachedNewMilestone(RunState run, int oldLevel, int newLevel) {
        int oldMilestones = countMilestonesUpTo(oldLevel);
        int newMilestones = countMilestonesUpTo(newLevel);
        return newMilestones > oldMilestones;
    }

    private int countMilestonesUpTo(int level) {
        int count = 0;
        for (int t : config.thresholds()) {
            if (level >= t) count++;
        }
        return count;
    }

    public int latestMilestoneLevel(int milestonesReached) {
        List<Integer> thresholds = config.thresholds();
        if (milestonesReached <= 0 || milestonesReached > thresholds.size()) {
            return -1;
        }
        return thresholds.get(milestonesReached - 1);
    }

    public List<ModifierSeverity> allowedSeverities(int milestoneIndex) {
        return config.severityByIndex().getOrDefault(milestoneIndex, List.of(ModifierSeverity.MILD));
    }
}
