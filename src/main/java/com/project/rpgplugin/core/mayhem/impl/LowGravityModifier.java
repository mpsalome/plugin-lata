package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class LowGravityModifier extends BaseModifier {

    private BukkitTask task;

    public LowGravityModifier() {
        super("low_gravity", ModifierSeverity.WILD, ModifierTag.ENVIRONMENT);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        task = SchedulerUtil.runTimer(ctx.plugin(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 200, 1, true, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0, true, false, false));
            }
        }, 0L, 100L);
    }

    @Override
    public void onDeactivate(MayhemContext ctx) {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
