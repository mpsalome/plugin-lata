package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;

public class UnbreakableBlockSkill extends AbstractSkill {

    public UnbreakableBlockSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "unbreakable_block"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.CLAY_BALL; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.CLAY_BALL); }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        Block target = p.getTargetBlockExact(5);
        if (target != null && target.getType() != Material.AIR && target.getType() != Material.BEDROCK) {
            consume(ctx, 1);
            Location targetLoc = target.getLocation();
            services.addReinforcedBlock(targetLoc);
            p.playSound(targetLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.5f);
            feedback(ctx, "§aBloco Reforçado: Inquebrável por 15s!", null);
            SchedulerUtil.runLater(services.plugin(), () -> {
                services.removeReinforcedBlock(targetLoc);
            }, 300L);
        }
    }
}
