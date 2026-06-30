package com.project.rpgplugin.task;

import com.project.rpgplugin.core.progression.DistanceTracker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class DistanceTask {

    private BukkitTask task;

    public void start(JavaPlugin plugin, DistanceTracker tracker) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, tracker::tick, 0L, 10L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
