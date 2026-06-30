package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Set;

public class OreSonarSkill extends AbstractSkill {

    private static final Set<Material> ORES = Set.of(
        Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE
    );

    public OreSonarSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "ore_sonar"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.GLOWSTONE_DUST; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(30); }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.GLOWSTONE_DUST); }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "§cRadar de Minério em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        consume(ctx, 1);
        startCooldown(ctx);
        Location ploc = p.getLocation();
        for (int x = -8; x <= 8; x++) {
            for (int y = -8; y <= 8; y++) {
                for (int z = -8; z <= 8; z++) {
                    Block b = ploc.clone().add(x, y, z).getBlock();
                    if (ORES.contains(b.getType())) {
                        b.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, b.getLocation().add(0.5, 0.5, 0.5), 5, 0.1, 0.1, 0.1, 0);
                    }
                }
            }
        }
        feedback(ctx, "§eRadar de Minério ativo!", Sound.ENTITY_ALLAY_DEATH);
    }
}
