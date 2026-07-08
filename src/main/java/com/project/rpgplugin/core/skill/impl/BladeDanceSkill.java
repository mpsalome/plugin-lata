package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BladeDanceSkill extends AbstractSkill {

    private static final Map<UUID, Integer> MOBILITY_COUNT = new HashMap<>();

    public BladeDanceSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "blade_dance"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.NETHERITE_SWORD; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.DAMAGE), ctx -> {
            if (!(ctx.sourceEvent() instanceof EntityDamageByEntityEvent e)) return false;
            if (!(e.getDamager() instanceof Player p)) return false;
            if (!(e.getEntity() instanceof LivingEntity)) return false;
            UUID uid = p.getUniqueId();
            int count = MOBILITY_COUNT.getOrDefault(uid, 0);
            return count >= 3;
        }, "<gray>Apos usar 3 skills de mobilidade, o proximo hit libera um corte de vento");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (!(ctx.sourceEvent() instanceof EntityDamageByEntityEvent e)) return;
        Player p = ctx.player();
        MOBILITY_COUNT.put(p.getUniqueId(), 0);
        Location origin = e.getEntity().getLocation();
        double radius = cfg().getInt("radius", 4);
        double damage = cfg().getInt("damage", 6);
        double lifesteal = cfg().getDouble("lifesteal", 0.05);
        origin.getWorld().spawnParticle(Particle.SWEEP_ATTACK, origin, 20, radius, 1, radius, 0);
        origin.getWorld().playSound(origin, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
        int hitCount = 0;
        for (Entity entity : origin.getNearbyEntities(radius, 2, radius)) {
            if (entity instanceof LivingEntity le && entity != p) {
                le.damage(damage, p);
                hitCount++;
            }
        }
        if (hitCount > 0) {
            double heal = hitCount * damage * lifesteal;
            p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + heal));
        }
        feedback(ctx, "<light_purple><bold>Danca das Laminas!</bold></light_purple>", Sound.ENTITY_PLAYER_ATTACK_SWEEP);
    }

    public static void recordMobility(UUID playerId) {
        MOBILITY_COUNT.merge(playerId, 1, Integer::sum);
    }
}
