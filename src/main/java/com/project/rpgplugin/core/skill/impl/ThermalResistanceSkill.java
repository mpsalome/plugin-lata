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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;

public class ThermalResistanceSkill extends AbstractSkill {

    public ThermalResistanceSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "thermal_resistance"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.MAGMA_CREAM; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(30); }

    @Override
    public SkillTrigger trigger() {
        return CompositeTriggerHelper.onDamageTaken(
            DamageCause.FIRE, DamageCause.FIRE_TICK, DamageCause.LAVA, DamageCause.HOT_FLOOR, DamageCause.CAMPFIRE
        );
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            return;
        }
        Player p = ctx.player();
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 160, 0, true, false, true));
        startCooldown(ctx);
        p.getWorld().spawnParticle(org.bukkit.Particle.FLAME, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
        feedback(ctx, "<yellow>Escudo Termal ativado! Fire Resist 8s.", Sound.ITEM_FIRECHARGE_USE);
    }
}
