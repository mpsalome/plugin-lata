package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FortressProtocolSkill extends AbstractSkill {

    public FortressProtocolSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "fortress_protocol"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.IRON_BLOCK; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(45); }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.DAMAGE), ctx -> {
            if (!(ctx.sourceEvent() instanceof EntityDamageEvent e)) return false;
            if (!(e.getEntity() instanceof Player p)) return false;
            return p.getHealth() - e.getFinalDamage() < p.getHealth() * 0.3;
        }, "<gray>Quando HP cai abaixo de 30%");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            return;
        }
        if (!(ctx.sourceEvent() instanceof EntityDamageEvent e)) return;
        Player p = ctx.player();
        startCooldown(ctx);
        Location center = p.getLocation();
        List<Location> cage = new ArrayList<>();
        int[][] offsets = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1},{0,0}};
        for (int[] off : offsets) {
            Location loc = center.clone().add(off[0], 0, off[1]);
            if (loc.getBlock().getType().isAir() || loc.getBlock().isReplaceable()) {
                loc.getBlock().setType(Material.IRON_BARS);
                cage.add(loc);
            }
        }
        cage.add(center.clone().add(0, 1, 0));
        if (center.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            center.clone().add(0, 1, 0).getBlock().setType(Material.IRON_BARS);
        }
        for (Entity entity : center.getNearbyEntities(4, 3, 4)) {
            if (entity instanceof LivingEntity le && entity != p) {
                le.damage(8.0, p);
                le.setVelocity(new Vector(
                    le.getLocation().getX() - center.getX(),
                    0.5,
                    le.getLocation().getZ() - center.getZ()
                ).normalize().multiply(2));
            }
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 2, true, false, false));
        center.getWorld().spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, center.add(0, 1, 0), 30, 1, 1, 1, 0.1);
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        feedback(ctx, "<gold><bold>Protocolo Fortaleza ativado! Gaiola de Ferro 5s.</bold></gold>", null);
        SchedulerUtil.runLater(services.plugin(), () -> {
            for (Location loc : cage) {
                if (loc.getBlock().getType() == Material.IRON_BARS) {
                    loc.getBlock().setType(Material.AIR);
                }
            }
        }, 100L);
    }
}
