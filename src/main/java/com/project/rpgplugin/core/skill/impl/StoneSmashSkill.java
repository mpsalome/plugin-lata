package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.BlockBreakTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StoneSmashSkill extends AbstractSkill {

    private static final Map<UUID, StoneMomentum> MOMENTUM = new HashMap<>();

    private record StoneMomentum(int level, long lastBreakTime) {}

    public StoneSmashSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "stone_smash"; }

    @Override
    public SkillTier tier() { return SkillTier.BRONZE; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.COBBLESTONE; }

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
                case STONE, COBBLESTONE, DEEPSLATE, ANDESITE, DIORITE, GRANITE, TUFF, DRIPSTONE_BLOCK, CALCITE -> true;
                default -> false;
            };
        }, "Quebre <gray>pedra</gray> ou derivados");
    }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        UUID uid = p.getUniqueId();
        long now = System.currentTimeMillis();
        StoneMomentum current = MOMENTUM.get(uid);
        int level = 0;
        if (current != null && (now - current.lastBreakTime) < 4000) {
            level = Math.min(2, current.level + 1);
        }
        MOMENTUM.put(uid, new StoneMomentum(level, now));
        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 80, level, true, false, false));
        if (level == 2) {
            p.getWorld().spawnParticle(org.bukkit.Particle.CRIT, p.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
        }
        final int capturedLevel = level;
        final long capturedNow = now;
        SchedulerUtil.runLater(services.plugin(), () -> {
            StoneMomentum m = MOMENTUM.get(uid);
            if (m != null && m.equals(new StoneMomentum(capturedLevel, capturedNow))) {
                MOMENTUM.remove(uid, m);
            }
        }, 80L);
    }
}
