package com.project.rpgplugin.core.progression;

import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DistanceTracker {

    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final RunManager runManager;

    public DistanceTracker(RunManager runManager) {
        this.runManager = runManager;
    }

    public void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!runManager.hasActiveRun(p)) continue;
            RunState run = runManager.getRun(p);
            if (run == null) continue;

            Location prev = lastLocations.get(p.getUniqueId());
            Location cur = p.getLocation();

            if (prev != null && prev.getWorld() == cur.getWorld()) {
                double d = prev.distance(cur);
                if (d > 0.05 && d < 64) {
                    run.addBlocksWalked((long) d);
                }
            }

            lastLocations.put(p.getUniqueId(), cur.clone());
        }
    }

    public void clear(UUID playerId) {
        lastLocations.remove(playerId);
    }

    public void clearAll() {
        lastLocations.clear();
    }
}
