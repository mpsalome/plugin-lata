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

import java.util.Random;

public class GravityChaosModifier extends BaseModifier {

    private BukkitTask task;
    private final Random random = new Random();

    public GravityChaosModifier() {
        super("gravity_chaos", ModifierSeverity.INSANE, ModifierTag.CHAOS);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        task = SchedulerUtil.runTimer(ctx.plugin(), () -> {
            int effect = random.nextInt(3);
            for (Player p : Bukkit.getOnlinePlayers()) {
                switch (effect) {
                    case 0 -> p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 3, true, false, false));
                    case 1 -> p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 1, true, false, false));
                    case 2 -> p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 0, true, false, false));
                }
            }
        }, 0L, 120L);
    }

    @Override
    public void onDeactivate(MayhemContext ctx) {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
