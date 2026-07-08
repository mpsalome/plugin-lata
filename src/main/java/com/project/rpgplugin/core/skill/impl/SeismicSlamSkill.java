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
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Set;

public class SeismicSlamSkill extends AbstractSkill {

    public SeismicSlamSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "seismic_slam"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.DIAMOND_PICKAXE; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(12); }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.INTERACT), ctx -> {
            if (!(ctx.sourceEvent() instanceof PlayerInteractEvent e)) return false;
            if (!ctx.player().isSneaking()) return false;
            if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
            if (ctx.usedItem() == null) return false;
            return Tag.ITEMS_PICKAXES.isTagged(ctx.usedItem().getType());
        }, "<gray>Agache + clique direito com <white>Picareta</white> (30 Mana)");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "<red>Onda Sismica em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        startCooldown(ctx);
        Location origin = p.getLocation().add(0, 0.5, 0);
        Vector direction = p.getLocation().getDirection().normalize().setY(0);
        origin.getWorld().spawnParticle(Particle.SONIC_BOOM, origin, 3, 0, 0, 0, 0);
        origin.getWorld().playSound(origin, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 0.5f);
        for (int i = 1; i <= 8; i++) {
            Location point = origin.clone().add(direction.clone().multiply(i));
            point.getWorld().spawnParticle(Particle.SWEEP_ATTACK, point, 3, 0.5, 0.2, 0.5, 0);
            point.getWorld().spawnParticle(Particle.CRIT, point, 5, 0.5, 0.1, 0.5, 0.05);
            for (Entity e : point.getWorld().getNearbyEntities(point, 1.5, 1, 1.5)) {
                if (e instanceof LivingEntity le && e != p) {
                    le.damage(6.0, p);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, true, false, false));
                    le.setVelocity(le.getVelocity().add(new Vector(0, 0.3, 0)));
                }
            }
        }
        feedback(ctx, "<green><bold>Onda Sismica!</bold></green>", Sound.ENTITY_WARDEN_SONIC_BOOM);
    }
}
