package com.project.rpgplugin.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public final class SchedulerUtil {

    private static final boolean FOLIA;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) {
        }
        FOLIA = folia;
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static BukkitTask runTimer(JavaPlugin plugin, Runnable task, long delay, long period) {
        return runTimer(plugin, scheduledTask -> task.run(), delay, period);
    }

    public static BukkitTask runTimer(JavaPlugin plugin, Consumer<BukkitTask> task, long delay, long period) {
        if (FOLIA) {
            var future = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
                task.accept(null);
            }, delay, period);
            return new BukkitTask() {
                @Override
                public int getTaskId() { return -1; }
                @Override
                public JavaPlugin getOwner() { return plugin; }
                @Override
                public boolean isSync() { return true; }
                @Override
                public void cancel() { future.cancel(); }
                @Override
                public boolean isCancelled() { return future.isCancelled(); }
            };
        }
        return Bukkit.getScheduler().runTaskTimer(plugin, (Runnable) () -> task.accept(null), delay, period);
    }

    public static void runLater(JavaPlugin plugin, Runnable task, long delay) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    public static void runAsync(JavaPlugin plugin, Runnable task) {
        if (FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    private SchedulerUtil() {}
}
