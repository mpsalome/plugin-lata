package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTriggerHelper;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.time.Duration;

public class WindBurstSkill extends AbstractSkill {

    public WindBurstSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "wind_burst"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public org.bukkit.Material icon() { return org.bukkit.Material.GUNPOWDER; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(20); }

    @Override
    public SkillTrigger trigger() {
        return CompositeTriggerHelper.sneakLeftClickAir();
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "<red>Explosao de Vento em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        p.setVelocity(new Vector(0, 1.5, 0));
        startCooldown(ctx);
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, p.getLocation(), 15, 0.5, 0.5, 0.5, 0.1);
        SchedulerUtil.runLater(services.plugin(), () -> {
            for (Entity e : p.getNearbyEntities(4, 2, 4)) {
                if (e instanceof LivingEntity && e != p) {
                    Vector knockback = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.8).setY(0.6);
                    e.setVelocity(knockback);
                    e.getWorld().spawnParticle(Particle.CLOUD, e.getLocation(), 10, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }, 5L);
        feedback(ctx, "<green>Explosao de Vento!</green>", Sound.ENTITY_WIND_CHARGE_THROW);
    }
}
