package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class CaffeinatedModifier extends BaseModifier {

    private BukkitTask task;

    public CaffeinatedModifier() {
        super("caffeinated", ModifierSeverity.MILD, ModifierTag.OFFENSE);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        task = Bukkit.getScheduler().runTaskTimer(ctx.plugin(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 200, 0, true, false, false));
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
