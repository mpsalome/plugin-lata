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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    public Material icon() { return Material.NETHER_WART; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() {
        return Duration.ofSeconds(cfg().getInt("cooldown", 30));
    }

    @Override
    public SkillTrigger trigger() {
        return InteractTrigger.of(org.bukkit.Tag.FLOWERS);
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "§cDash em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        consume(ctx, 1);
        int dur = cfg().getInt("duration", 10) * 20;
        int speed = cfg().getInt("speed", 1);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur, speed));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, dur, 0));
        startCooldown(ctx);
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
        feedback(ctx, "§bDash das Flores Ativado!", Sound.ENTITY_BAT_TAKEOFF);
    }
}
