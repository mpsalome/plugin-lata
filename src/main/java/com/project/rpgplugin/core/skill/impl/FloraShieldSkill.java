package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.time.Duration;

public class FloraShieldSkill extends AbstractSkill {

    public FloraShieldSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "flora_shield"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public org.bukkit.Material icon() { return org.bukkit.Material.DANDELION; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(15); }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(org.bukkit.Tag.FLOWERS); }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "§cEscudo Floral em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        if (p.getHealth() < p.getAttribute(Attribute.MAX_HEALTH).getValue()) {
            consume(ctx, 1);
            p.setHealth(Math.min(p.getAttribute(Attribute.MAX_HEALTH).getValue(), p.getHealth() + 8.0));
            startCooldown(ctx);
            p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0);
            feedback(ctx, "§aEscudo Floral: +4 Corações!", Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        }
    }
}
