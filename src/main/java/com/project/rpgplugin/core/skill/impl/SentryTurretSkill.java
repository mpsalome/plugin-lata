package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTriggerHelper;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Collection;

public class SentryTurretSkill extends AbstractSkill {

    public SentryTurretSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "sentry_turret"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.DISPENSER; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(40); }

    @Override
    public SkillTrigger trigger() {
        return CompositeTriggerHelper.sneakRightClick(Material.PUMPKIN);
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            ctx.player().playSound(ctx.player().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return;
        }
        Player p = ctx.player();
        startCooldown(ctx);
        ctx.services().plugin().getHudService().setCooldown(p, "Torreta", (int) cooldown().toSeconds());
        Location turretLoc = p.getLocation().add(0, 1, 0).add(p.getLocation().getDirection().normalize().multiply(2));
        turretLoc.getBlock().setType(Material.DISPENSER);
        turretLoc.getWorld().spawnParticle(Particle.PORTAL, turretLoc, 30, 0.5, 0.5, 0.5, 1);
        turretLoc.getWorld().playSound(turretLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        int[] ticks = {0};
        int[] taskId = { -1 };
        taskId[0] = SchedulerUtil.runTimer(services.plugin(), () -> {
            ticks[0]++;
            if (ticks[0] >= 300) {
                turretLoc.getBlock().setType(Material.AIR);
                return;
            }
            turretLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, turretLoc.clone().add(0.5, 0.5, 0.5), 2, 0.3, 0.3, 0.3, 0);
            Collection<LivingEntity> nearby = turretLoc.getNearbyLivingEntities(10, 5, 10);
            LivingEntity target = null;
            double closest = Double.MAX_VALUE;
            for (LivingEntity le : nearby) {
                if (le == p || le instanceof Player) continue;
                double dist = le.getLocation().distanceSquared(turretLoc);
                if (dist < closest) {
                    closest = dist;
                    target = le;
                }
            }
            if (target != null) {
                Arrow arrow = turretLoc.getWorld().spawnArrow(
                    turretLoc.clone().add(0.5, 0.5, 0.5),
                    target.getLocation().toVector().subtract(turretLoc.clone().add(0.5, 0.5, 0.5).toVector()).normalize(),
                    1.5f, 0
                );
                arrow.setShooter(p);
                arrow.setDamage(4.0);
            }
        }, 20L, 20L).getTaskId();
        p.playSound(p.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
    }
}
