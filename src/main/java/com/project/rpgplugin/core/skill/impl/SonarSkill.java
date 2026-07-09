package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTriggerHelper;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.util.SchedulerUtil;
import com.project.rpgplugin.util.Text;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SonarSkill extends AbstractSkill {

    private static final Map<UUID, SonarSession> activeSessions = new ConcurrentHashMap<>();

    public SonarSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "sonar"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.AMETHYST_SHARD; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() {
        return CompositeTriggerHelper.sneakRightClick();
    }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        UUID pid = p.getUniqueId();
        SonarSession existing = activeSessions.get(pid);

        if (existing != null) {
            deactivate(p, existing);
            return;
        }

        int range = cfgInt("range", 30);
        long interval = cfgInt("tick_interval", 20);
        int glowDuration = cfgInt("glow_duration", 60);

        services.plugin().getHudService().setActiveEffect(p, "Sonar", -1);

        BukkitTask task = SchedulerUtil.runTimer(services.plugin(), () -> {
            if (!p.isOnline() || activeSessions.get(pid) == null) {
                return;
            }
            for (Entity entity : p.getNearbyEntities(range, range, range)) {
                if (entity instanceof LivingEntity le && entity != p) {
                    le.setGlowing(true);
                    le.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, le.getLocation().add(0, 1, 0), 6, 0.3, 0.5, 0.3, 0);
                    SchedulerUtil.runLater(services.plugin(), () -> {
                        if (le.isValid()) le.setGlowing(false);
                    }, glowDuration);
                }
            }
        }, 0L, interval);

        activeSessions.put(pid, new SonarSession(task));
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
        p.sendActionBar(Text.mm("<aqua>Sonar ativado! Agache + clique direito novamente para desligar.</aqua>"));
    }

    private void deactivate(Player p, SonarSession session) {
        UUID pid = p.getUniqueId();
        activeSessions.remove(pid);
        session.task.cancel();
        services.plugin().getHudService().removeActiveEffect(p, "Sonar");
        for (Entity entity : p.getNearbyEntities(60, 60, 60)) {
            if (entity instanceof LivingEntity le && entity != p) {
                le.setGlowing(false);
            }
        }
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f, 1.0f);
        p.sendActionBar(Text.mm("<gray>Sonar desativado.</gray>"));
    }

    public static boolean isActive(Player p) {
        return activeSessions.containsKey(p.getUniqueId());
    }

    public static void cleanup(Player p) {
        SonarSession session = activeSessions.remove(p.getUniqueId());
        if (session != null) {
            session.task.cancel();
        }
    }

    private record SonarSession(BukkitTask task) {}
}
