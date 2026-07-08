package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.time.Duration;

public class RecallSkill extends AbstractSkill {

    public RecallSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "recall"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.ENDER_EYE; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() {
        return Duration.ofSeconds(cfg() != null ? cfg().getInt("cooldown", 60) : 60);
    }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.ENDER_EYE); }

    @Override
    public void activate(SkillContext ctx) {
        consume(ctx, 1);
        ctx.player().teleport(ctx.player().getWorld().getSpawnLocation());
        ctx.player().playSound(ctx.player().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        feedback(ctx, "§5Recall! Teleportado ao Spawn!", null);
    }
}
