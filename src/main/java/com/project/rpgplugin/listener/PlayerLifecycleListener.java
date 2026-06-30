package com.project.rpgplugin.listener;

import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunOutcome;
import com.project.rpgplugin.core.run.RunState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerLifecycleListener implements Listener {

    private final RunManager runManager;

    public PlayerLifecycleListener(RunManager runManager) {
        this.runManager = runManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (!runManager.hasActiveRun(p)) return;

        RunState run = runManager.getRun(p);
        if (run == null) return;

        // Phoenix check — if charge available, cancel death
        if (run.phoenixCharge()) {
            run.setPhoenixCharge(false);
            p.setHealth(1.0);
            p.sendMessage(Component.text("§d§l🔥 Fênix! Você reviveu!"));
            e.setCancelled(true);
            return;
        }

        runManager.endRun(p, RunOutcome.DIED);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        // Ensure a run exists after respawn
        if (!runManager.hasActiveRun(p)) {
            runManager.startRun(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!runManager.hasActiveRun(p)) {
            runManager.startRun(p);
        }
    }
}
