package com.project.rpgplugin.listener;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.draft.DraftService;
import com.project.rpgplugin.core.draft.DraftSession;
import com.project.rpgplugin.core.draft.DraftWeighting;
import com.project.rpgplugin.core.mayhem.MilestoneService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.ui.DraftMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class PlayerLevelListener implements Listener {

    private final RunManager runManager;
    private final DraftService draftService;
    private final DraftWeighting weighting;
    private final RPGPlugin plugin;
    private final MilestoneService milestoneService;

    public PlayerLevelListener(RunManager runManager, DraftService draftService, DraftWeighting weighting, RPGPlugin plugin, MilestoneService milestoneService) {
        this.runManager = runManager;
        this.draftService = draftService;
        this.weighting = weighting;
        this.plugin = plugin;
        this.milestoneService = milestoneService;
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent e) {
        Player p = e.getPlayer();
        if (!runManager.hasActiveRun(p)) {
            runManager.startRun(p);
        }

        RunState run = runManager.getRun(p);
        if (run == null) return;

        int oldLevel = e.getOldLevel();
        int newLevel = e.getNewLevel();

        if (newLevel <= oldLevel) return;

        run.setLevel(newLevel);

        // EPIC-2: Draft trigger — 1 augment per level
        int draftsEarned = newLevel - oldLevel;
        if (draftsEarned > 0) {
            run.addPendingDrafts(draftsEarned);
            openNextDraft(p, run);
        }

        // EPIC-4: Mayhem milestone check
        if (milestoneService.reachedNewMilestone(run, oldLevel, newLevel)) {
            int newMilestones = milestoneService.milestonesReached(run);
            run.setMilestonesReached(newMilestones);
            plugin.getMayhemService().rollAndApply(run, p.getWorld());
        }
    }

    public void openNextDraft(Player p, RunState run) {
        if (!run.hasPendingDrafts()) return;
        run.consumePendingDraft();

        DraftSession session = draftService.roll(p, run);
        new DraftMenu(p, session, draftService, run, runManager, weighting, plugin, this).open();
    }
}
