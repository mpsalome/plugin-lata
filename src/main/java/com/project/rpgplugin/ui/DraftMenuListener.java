package com.project.rpgplugin.ui;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.draft.DraftService;
import com.project.rpgplugin.core.draft.DraftSession;
import com.project.rpgplugin.core.draft.DraftWeighting;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.listener.PlayerLevelListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class DraftMenuListener implements Listener {

    private final RunManager runManager;
    private final DraftService draftService;
    private final DraftWeighting weighting;
    private final PlayerLevelListener levelListener;

    public DraftMenuListener(RunManager runManager, DraftService draftService,
                             DraftWeighting weighting, PlayerLevelListener levelListener) {
        this.runManager = runManager;
        this.draftService = draftService;
        this.weighting = weighting;
        this.levelListener = levelListener;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        Inventory inv = e.getInventory();
        if (inv == null) return;
        String title = e.getView().title().toString();
        if (title == null || !title.contains("Escolha sua carta")) return;

        e.setCancelled(true);

        if (!runManager.hasActiveRun(p)) return;
        RunState run = runManager.getRun(p);
        if (run == null) return;

        DraftSession session = draftService.getActiveSession(p.getUniqueId());
        if (session == null || session.isDecided()) return;

        int slot = e.getRawSlot();
        if (slot == 11 || slot == 14 || slot == 17) {
            int index = (slot - 11) / 3;
            draftService.applyChoice(p, run, session, index);
            p.closeInventory();

            if (run.hasPendingDrafts()) {
                levelListener.openNextDraft(p, run);
            }
        } else if (slot == 22 && DraftWeighting.isRerollEnabled(runManager.plugin())) {
            draftService.reroll(p, run, session);
            new DraftMenu(p, session, draftService, run, runManager, weighting, runManager.plugin()).open();
        } else if (slot == 26 && DraftWeighting.isSkipAllowed(runManager.plugin())) {
            draftService.skipDraft(p, run, session);
            p.closeInventory();

            if (run.hasPendingDrafts()) {
                levelListener.openNextDraft(p, run);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        Inventory inv = e.getInventory();
        if (inv == null) return;
        String title = e.getView().title().toString();
        if (title == null || !title.contains("Escolha sua carta")) return;

        if (!runManager.hasActiveRun(p)) return;
        RunState run = runManager.getRun(p);
        if (run == null) return;

        DraftSession session = draftService.getActiveSession(p.getUniqueId());
        if (session == null || session.isDecided()) return;

        // Session wasn't decided - queue it back
        if (run.hasPendingDrafts()) {
            levelListener.openNextDraft(p, run);
        }
    }
}
