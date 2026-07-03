package com.project.rpgplugin.task;

import com.project.rpgplugin.AuraSkillsIntegration;
import com.project.rpgplugin.core.build.SynergyService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class PassiveTask {

    private BukkitTask task;

    public void start(JavaPlugin plugin, RunManager runManager, SynergyService synergyService, AuraSkillsIntegration auraSkills) {
        task = SchedulerUtil.runTimer(plugin, scheduledTask -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                RunState run = runManager.getRun(p);
                if (run == null) continue;

                synergyService.applySynergies(p, run);
                auraSkills.syncSkillSlots(p, run.extraSkillSlots());

                for (String potionTypeName : run.activePotionTypes()) {
                    PotionEffectType type = PotionEffectType.getByName(potionTypeName);
                    if (type != null && !p.hasPotionEffect(type)) {
                        int amplifier = 0;
                        if (potionTypeName.equals("ABSORPTION")) amplifier = 0;
                        p.addPotionEffect(new PotionEffect(type, PotionEffect.INFINITE_DURATION, amplifier, false, false, true));
                    }
                }
            }
        }, 40L, 40L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
