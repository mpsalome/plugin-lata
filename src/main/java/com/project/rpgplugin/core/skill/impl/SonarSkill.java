package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.time.Duration;

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
    public Duration cooldown() { return Duration.ofSeconds(20); }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.AMETHYST_SHARD); }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "§cSonar em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        consume(ctx, 1);
        startCooldown(ctx);
        for (Entity entity : p.getNearbyEntities(15, 15, 15)) {
            if (entity instanceof LivingEntity && entity != p) {
                entity.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, entity.getLocation().add(0, 1, 0), 10, 0.2, 0.5, 0.2, 0);
            }
        }
        feedback(ctx, "§5Sonar ativo! Localizando entidades...", Sound.BLOCK_AMETHYST_BLOCK_CHIME);
    }
}
