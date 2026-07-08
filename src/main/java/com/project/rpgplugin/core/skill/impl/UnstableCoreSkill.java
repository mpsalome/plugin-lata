package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.BlockBreakTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class UnstableCoreSkill extends AbstractSkill {

    public UnstableCoreSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "unstable_core"; }

    @Override
    public SkillTier tier() { return SkillTier.BRONZE; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.TNT; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() {
        return BlockBreakTrigger.custom(ctx -> {
            Block block = ctx.targetBlock();
            if (block == null) return false;
            return switch (block.getType()) {
                case IRON_ORE, DEEPSLATE_IRON_ORE, GOLD_ORE, DEEPSLATE_GOLD_ORE,
                     DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE, COPPER_ORE, DEEPSLATE_COPPER_ORE,
                     LAPIS_ORE, DEEPSLATE_LAPIS_ORE, REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE,
                     EMERALD_ORE, DEEPSLATE_EMERALD_ORE, NETHER_GOLD_ORE, NETHER_QUARTZ_ORE,
                     ANCIENT_DEBRIS -> true;
                default -> false;
            };
        }, "Quebre <gold>minerios</gold> (15% chance de explodir)");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (ThreadLocalRandom.current().nextInt(100) >= 15) return;
        Player p = ctx.player();
        Block block = ctx.targetBlock();
        if (block == null) return;
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        block.getWorld().createExplosion(loc, 2.0f, false, false, p);
        block.getWorld().spawnParticle(org.bukkit.Particle.FLAME, loc, 20, 0.5, 0.5, 0.5, 0.1);
        block.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        for (Entity e : loc.getNearbyEntities(3, 3, 3)) {
            if (e instanceof LivingEntity le && e != p) {
                le.damage(8.0, p);
            }
        }
    }
}
