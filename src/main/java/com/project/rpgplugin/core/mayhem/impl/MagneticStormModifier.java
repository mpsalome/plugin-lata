package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class MagneticStormModifier extends BaseModifier {

    private BukkitTask task;

    public MagneticStormModifier() {
        super("magnetic_storm", ModifierSeverity.WILD, ModifierTag.ENVIRONMENT);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        task = Bukkit.getScheduler().runTaskTimer(ctx.plugin(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (Math.random() < 0.3) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true, false, false));
                }
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
