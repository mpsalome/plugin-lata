package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.BlockBreakTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.Set;

public class EntanglingRootsSkill extends AbstractSkill {

    public EntanglingRootsSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "entangling_roots"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.MOSS_BLOCK; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(15); }

    @Override
    public SkillTrigger trigger() {
        return BlockBreakTrigger.custom(ctx -> {
            if (!ctx.player().isSneaking()) return false;
            Block block = ctx.targetBlock();
            if (block == null) return false;
            Material type = block.getType();
            return type == Material.GRASS_BLOCK || type == Material.DIRT
                || type == Material.ROOTED_DIRT || type == Material.MUD
                || type == Material.PODZOL || type == Material.MYCELIUM
                || type == Material.COARSE_DIRT || type == Material.FARMLAND
                || type == Material.DIRT_PATH;
        }, "<gray>Agache + quebre <green>terra/grama</green> para invocar raizes (25 Mana)");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "<red>Raizes em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        Block targetBlock = ctx.targetBlock();
        if (targetBlock == null) return;
        Location center = targetBlock.getLocation().add(0.5, 0, 0.5);
        startCooldown(ctx);
        int radius = cfg().getInt("radius", 4);
        int duration = cfg().getInt("duration", 3) * 20;
        center.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, center.add(0, 0.5, 0), 40, radius, 0.2, radius, 1);
        center.getWorld().playSound(center, Sound.BLOCK_GRASS_BREAK, 1.0f, 0.5f);
        for (Entity e : center.getWorld().getNearbyEntities(center, radius, 2, radius)) {
            if (e instanceof LivingEntity le && e != p) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 3, true, false, false));
                le.damage(4.0, p);
                le.setVelocity(le.getVelocity().setX(0).setZ(0));
            }
        }
        feedback(ctx, "<green><bold>Raizes Esmagadoras!</bold></green>", Sound.BLOCK_GRASS_BREAK);
    }
}
