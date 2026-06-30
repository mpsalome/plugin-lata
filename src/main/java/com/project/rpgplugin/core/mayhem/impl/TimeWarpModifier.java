package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

public class TimeWarpModifier extends BaseModifier {

    private BukkitTask task;
    private boolean accelerated = false;

    public TimeWarpModifier() {
        super("time_warp", ModifierSeverity.INSANE, ModifierTag.CHAOS);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        for (World w : Bukkit.getWorlds()) {
            w.setGameRule(org.bukkit.GameRule.RANDOM_TICK_SPEED, 6);
        }
        task = Bukkit.getScheduler().runTaskTimer(ctx.plugin(), () -> {
            accelerated = !accelerated;
            for (World w : Bukkit.getWorlds()) {
                if (accelerated) {
                    w.setTime(w.getTime() + 100);
                }
            }
        }, 0L, 40L);
    }

    @Override
    public void onDeactivate(MayhemContext ctx) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (World w : Bukkit.getWorlds()) {
            w.setGameRule(org.bukkit.GameRule.RANDOM_TICK_SPEED, 3);
        }
    }
}
