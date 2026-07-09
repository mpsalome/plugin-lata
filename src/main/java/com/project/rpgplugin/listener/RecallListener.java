package com.project.rpgplugin.listener;

import com.project.rpgplugin.core.progression.RecallProgression;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class RecallListener implements Listener {

    private final RunManager runManager;
    private final RecallProgression recallProgression;

    public RecallListener(RunManager runManager, RecallProgression recallProgression) {
        this.runManager = runManager;
        this.recallProgression = recallProgression;
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (!runManager.hasActiveRun(p)) return;
        RunState run = runManager.getRun(p);
        if (run == null || !run.hasCard("recall")) return;

        e.setCancelled(true);
        recallProgression.use(p, run);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!runManager.hasActiveRun(p)) return;
        RunState run = runManager.getRun(p);
        if (run == null || !run.hasCard("recall")) return;

        ItemStack item = e.getItem();
        if (item != null && item.getType().name().contains("TOTEM")) {
            String progress = recallProgression.progress(run);
            p.sendActionBar(Text.mm(progress));
        }
    }
}
