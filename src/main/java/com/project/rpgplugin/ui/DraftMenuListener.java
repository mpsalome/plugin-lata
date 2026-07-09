package com.project.rpgplugin.ui;

import com.project.rpgplugin.core.draft.DraftService;
import com.project.rpgplugin.core.draft.DraftSession;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.listener.PlayerLevelListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class DraftMenuListener implements Listener {

    private final RunManager runManager;
    private final DraftService draftService;
    private final PlayerLevelListener levelListener;

    public DraftMenuListener(RunManager runManager, DraftService draftService, PlayerLevelListener levelListener) {
        this.runManager = runManager;
        this.draftService = draftService;
        this.levelListener = levelListener;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (!(e.getInventory().getHolder() instanceof DraftMenu)) return;

        // Nao reabre automaticamente — o jogador decide quando draftar
        // Os drafts pendentes ficam na fila, acessiveis via /lata draft
    }
}
