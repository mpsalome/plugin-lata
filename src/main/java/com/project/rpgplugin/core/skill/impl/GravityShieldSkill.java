package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTriggerHelper;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;

public class GravityShieldSkill extends AbstractSkill {

    public GravityShieldSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "gravity_shield"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.OBSIDIAN; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(30); }

    @Override
    public SkillTrigger trigger() {
        return CompositeTriggerHelper.onDamageTaken(
            e -> {
                if (!(e.getEntity() instanceof Player)) return false;
                if (e.getCause() == EntityDamageEvent.DamageCause.FALL) return false;
                if (e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) return false;
                if (e.getCause() == EntityDamageEvent.DamageCause.POISON) return false;
                if (e.getCause() == EntityDamageEvent.DamageCause.WITHER) return false;
                return e.getFinalDamage() >= 8.0;
            },
            "<gray>Ao sofrer dano fisico > 4 coracoes"
        );
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            return;
        }
        Player p = ctx.player();
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 2, true, false, true));
        p.setNoDamageTicks(Math.max(p.getNoDamageTicks(), 20));
        startCooldown(ctx);
        p.getWorld().spawnParticle(org.bukkit.Particle.CRIT, p.getLocation().add(0, 1, 0), 30, 0.8, 0.8, 0.8, 0.1);
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);
        feedback(ctx, "<dark_gray>Escudo Gravitacional: Resistencia III + anti-kb 6s!", null);
    }
}
