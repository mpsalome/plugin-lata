package com.project.rpgplugin.listener;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.draft.DraftService;
import com.project.rpgplugin.core.draft.DraftSession;
import com.project.rpgplugin.core.draft.DraftWeighting;
import com.project.rpgplugin.core.mayhem.MilestoneService;
import com.project.rpgplugin.core.mob.EliteFactory;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.ui.DraftMenu;
import com.project.rpgplugin.util.Text;
import org.bukkit.Location;
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

        // EPIC-2: Draft trigger — 1 augment per level (nao bloqueante)
        int draftsEarned = newLevel - oldLevel;
        if (draftsEarned > 0) {
            run.addPendingDrafts(draftsEarned);
            p.sendActionBar(Text.mm("<gold>\uD83C\uDFB4 +" + draftsEarned + " draft(s)! Digite <white>/lata draft</white> para abrir."));
        }

        // EPIC-4: Mayhem milestone check — 50% mayhem / 50% boss aleatorio
        if (milestoneService.reachedNewMilestone(run, oldLevel, newLevel)) {
            int newMilestones = milestoneService.milestonesReached(run);
            run.setMilestonesReached(newMilestones);

            if (Math.random() < 0.5) {
                plugin.getMayhemService().rollAndApply(run, p.getWorld());
            } else {
                spawnMilestoneBoss(p, run);
            }
        }
    }

    private void spawnMilestoneBoss(Player p, RunState run) {
        String[] bossIds = {"frostmaw", "magma_tyrant", "storm_wyvern", "void_lich",
            "sir_creeper_alot", "slime_shady", "the_beheader",
            "ancient_guardian", "piglin_warlord", "phantom_king"};
        String bossId = bossIds[(int) (Math.random() * bossIds.length)];

        EliteFactory.BossDef def = plugin.getMobSpawnService().getBossDef(bossId);
        if (def == null) return;

        int level = run.level();
        EliteFactory.BossDef scaled = def.scaleByLevel(level);

        Location loc = p.getLocation().clone().add(Math.random() * 30 - 15, 0, Math.random() * 30 - 15);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc) + 1);

        plugin.getMobSpawnService().getEliteFactory().spawnBoss(loc, scaled, level);

        String name = scaled.displayName().replaceAll("<[^>]+>", "").trim();
        p.sendMessage(Text.mm(
            "<red><bold>\u2620 " + name + "</bold></red> <gray>|</gray> <yellow>Nivel " + level + "</yellow>\n"
            + "<gray>O caos ao inves do mayhem...</gray>"
        ));
    }

    public void openNextDraft(Player p, RunState run) {
        if (!run.hasPendingDrafts()) return;
        run.consumePendingDraft();

        DraftSession session = draftService.roll(p, run);
        new DraftMenu(p, session, draftService, run, runManager, weighting, plugin, this).open();
    }
}
