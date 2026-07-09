package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTriggerHelper;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.Duration;

public class DashSkill extends AbstractSkill {

    public DashSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "dash"; }

    @Override
    public SkillTier tier() { return SkillTier.BRONZE; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public org.bukkit.Material icon() { return org.bukkit.Material.NETHER_WART; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() {
        return Duration.ofSeconds(cfgInt("cooldown", 8));
    }

    @Override
    public SkillTrigger trigger() {
        return CompositeTriggerHelper.sneakJump();
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            ctx.player().playSound(ctx.player().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return;
        }
        Player p = ctx.player();
        Vector dir = p.getLocation().getDirection().normalize();
        p.setVelocity(dir.multiply(1.5).setY(0.4));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, true, false, false));
        startCooldown(ctx);
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.5, 0), 20, 0.3, 0.3, 0.3, 0.05);
        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, p.getLocation(), 5, 0.5, 0.5, 0.5, 0);
        p.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
    }
}
