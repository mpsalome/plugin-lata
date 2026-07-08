package com.project.rpgplugin.task;

import com.project.rpgplugin.AuraSkillsIntegration;
import com.project.rpgplugin.core.build.SynergyService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillRegistry;
import com.project.rpgplugin.core.skill.SkillServices;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class PassiveTask {

    private BukkitTask task;

    public void start(JavaPlugin plugin, RunManager runManager, SynergyService synergyService, AuraSkillsIntegration auraSkills, SkillRegistry skillRegistry, SkillServices skillServices) {
        task = SchedulerUtil.runTimer(plugin, scheduledTask -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                RunState run = runManager.getRun(p);
                if (run == null) continue;

                synergyService.applySynergies(p, run);
                auraSkills.syncSkillSlots(p, 3 + run.extraSkillSlots());

                for (String skillId : run.ownedAbilities()) {
                    if (!run.isToggledOn(skillId)) continue;
                    Skill skill = skillRegistry.byId(skillId).orElse(null);
                    if (skill == null) continue;
                    if (!skill.trigger().kinds().contains(TriggerKind.PASSIVE)) continue;
                    SkillContext ctx = new SkillContext(p, skillServices, null, null, null);
                    skill.activate(ctx);
                }

                for (String potionTypeName : run.activePotionTypes()) {
                    PotionEffectType type = PotionEffectType.getByName(potionTypeName);
                    if (type != null && !p.hasPotionEffect(type)) {
                        int amplifier = 0;
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
