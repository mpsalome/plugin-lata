package com.project.rpgplugin.listener;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunPersistenceService;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerLifecycleListener implements Listener {

    private final RunManager runManager;
    private final RunPersistenceService persistence;
    private final RPGPlugin plugin;

    public PlayerLifecycleListener(RunManager runManager, RunPersistenceService persistence) {
        this.runManager = runManager;
        this.persistence = persistence;
        this.plugin = runManager.plugin();
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

        // Desativa modificadores do Mayhem antes de resetar a run
        if (plugin.getMayhemService() != null) {
            plugin.getMayhemService().clear(p, run);
        }

        plugin.getResetService().resetBuild(p, run);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        // Forca respawn no spawn do mundo (ignora cama)
        Location worldSpawn = p.getWorld().getSpawnLocation();
        e.setRespawnLocation(worldSpawn);

        if (!runManager.hasActiveRun(p)) {
            runManager.startRun(p);
            if (plugin.getHudService() != null) {
                plugin.getHudService().startPlayer(p);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        boolean isFirstJoin = !persistence.hasRun(p);

        RunState run = persistence.loadRun(p);
        if (run != null) {
            runManager.restoreRun(p, run);
        } else if (!runManager.hasActiveRun(p)) {
            runManager.startRun(p);
        }

        // EPIC-11: Veteran migration — convert existing vanilla levels to pending drafts
        if (isFirstJoin && p.getLevel() > 0) {
            run = runManager.getRun(p);
            if (run != null) {
                int levels = p.getLevel();
                run.addPendingDrafts(levels);
                p.sendMessage(Text.mm(
                    "<gradient:#ffd700:#ff8c00>\uD83E\uDD56 Bem-vindo ao RogueLata!</gradient> <gray>Seus <white>" + levels
                    + " niveis</white> antigos foram convertidos em <yellow>" + levels
                    + " Cartas de Build</yellow>! Digite <click:run_command:'/lata draft'><b>/lata draft</b></click> para abrir.</gray>"
                ));
            }
        }

        if (plugin.getHudService() != null) {
            plugin.getHudService().startPlayer(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (runManager.hasActiveRun(p)) {
            RunState run = runManager.getRun(p);
            if (run != null) {
                persistence.saveRun(p, run);
            }
        }
        runManager.removeRun(p);
    }
}
