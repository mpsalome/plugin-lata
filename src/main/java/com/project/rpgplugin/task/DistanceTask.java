package com.project.rpgplugin.task;

import com.project.rpgplugin.core.progression.DistanceTracker;
import com.project.rpgplugin.listener.AugmentListener;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class DistanceTask {

    private BukkitTask task;

    public void start(JavaPlugin plugin, DistanceTracker tracker, AugmentListener augmentListener) {
        task = SchedulerUtil.runTimer(plugin, scheduledTask -> {
            tracker.tick();
            if (augmentListener != null) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    augmentListener.tickMagnet(p);
                }
            }
        }, 0L, 10L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
