package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTriggerHelper;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SonarSkill extends AbstractSkill {

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
    public Duration cooldown() { return Duration.ofSeconds(25); }

    @Override
    public SkillTrigger trigger() {
        return CompositeTriggerHelper.sneakRightClick();
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            ctx.player().playSound(ctx.player().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return;
        }
        Player p = ctx.player();
        startCooldown(ctx);
        int range = cfgInt("range", 20);
        int duration = cfgInt("glow_duration", 10) * 20;
        List<LivingEntity> revealed = new ArrayList<>();
        for (Entity entity : p.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity le && entity != p) {
                le.setGlowing(true);
                revealed.add(le);
                le.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, le.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0);
            }
        }
        SchedulerUtil.runLater(services.plugin(), () -> {
            for (LivingEntity le : revealed) {
                le.setGlowing(false);
            }
        }, duration);
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
    }
}
