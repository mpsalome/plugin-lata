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
import org.bukkit.entity.Player;

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
    public Material icon() { return Material.GUNPOWDER; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(25); }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.GUNPOWDER); }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "§cExplosão de Vento em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        consume(ctx, 1);
        p.setVelocity(new org.bukkit.util.Vector(0, 1.3, 0));
        startCooldown(ctx);
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, p.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
        feedback(ctx, "§aExplosão de Vento!", Sound.ENTITY_WIND_CHARGE_THROW);
    }
}
