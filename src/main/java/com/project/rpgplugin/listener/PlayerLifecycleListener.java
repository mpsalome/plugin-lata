package com.project.rpgplugin.listener;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunOutcome;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.data.PlayerDataStore;
import com.project.rpgplugin.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerLifecycleListener implements Listener {

    private final RunManager runManager;
    private PlayerDataStore dataStore;
    private RPGPlugin plugin;

    public PlayerLifecycleListener(RunManager runManager) {
        this.runManager = runManager;
        this.plugin = runManager.plugin();
        if (plugin instanceof RPGPlugin rpg) {
            this.dataStore = rpg.getDataStore();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (!runManager.hasActiveRun(p)) return;

        RunState run = runManager.getRun(p);
        if (run == null) return;

        if (run.phoenixCharge()) {
            run.setPhoenixCharge(false);
            p.setHealth(1.0);
            p.sendMessage(Text.mm("<light_purple><bold>Fenix! Voce reviveu!"));
            e.setCancelled(true);
            return;
        }

        runManager.endRun(p, RunOutcome.DIED);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!runManager.hasActiveRun(p)) {
            runManager.startRun(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (dataStore != null) {
            var loaded = dataStore.load(p.getUniqueId());
            if (loaded.isPresent()) {
                RunState run = loaded.get();
                runManager.restoreRun(p, run);
                return;
            }
        }
        if (!runManager.hasActiveRun(p)) {
            runManager.startRun(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (dataStore != null && runManager.hasActiveRun(p)) {
            RunState run = runManager.getRun(p);
            if (run != null) {
                dataStore.save(p.getUniqueId(), run);
            }
        }
        runManager.removeRun(p);
    }
}
