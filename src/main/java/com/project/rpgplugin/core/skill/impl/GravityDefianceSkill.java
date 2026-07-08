package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.Set;

public class GravityDefianceSkill extends AbstractSkill {

    public GravityDefianceSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "gravity_defiance"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.SLIME_BLOCK; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(25); }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.DAMAGE), ctx -> {
            if (!(ctx.sourceEvent() instanceof EntityDamageEvent e)) return false;
            EntityDamageEvent.DamageCause cause = e.getCause();
            return cause == EntityDamageEvent.DamageCause.FALL
                || cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                || cause == EntityDamageEvent.DamageCause.PROJECTILE
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
        }, "<gray>Ao sofrer knockback ou dano de queda");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            return;
        }
        Player p = ctx.player();
        if (ctx.sourceEvent() instanceof EntityDamageEvent e) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                e.setDamage(0);
            }
            if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                p.setVelocity(p.getVelocity().setX(0).setZ(0));
            }
        }
        p.setFallDistance(0);
        p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 80, 0, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 0, true, false, false));
        startCooldown(ctx);
        p.getWorld().spawnParticle(org.bukkit.Particle.ITEM_SLIME, p.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        p.playSound(p.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.0f, 1.0f);
        feedback(ctx, "<b>Desafio Gravitacional: Voo temporario 4s!", Sound.ENTITY_PHANTOM_FLAP);
    }
}
