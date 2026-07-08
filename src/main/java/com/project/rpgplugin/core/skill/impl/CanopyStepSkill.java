package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.MoveTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;

public class CanopyStepSkill extends AbstractSkill {

    public CanopyStepSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "canopy_step"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.LEATHER_BOOTS; }

    @Override
    public boolean passive() { return true; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return MoveTrigger.always(); }

    @Override
    public void activate(SkillContext ctx) {
        Block under = ctx.targetBlock();
        if (under == null) return;
        Material type = under.getType();
        boolean natural = switch (type) {
            case GRASS_BLOCK, DIRT, ROOTED_DIRT, PODZOL, MYCELIUM, SAND, RED_SAND,
                 GRAVEL, CLAY, MUD, PACKED_MUD, SOUL_SAND, SOUL_SOIL -> true;
            default -> Tag.LEAVES.isTagged(type) || Tag.LOGS.isTagged(type)
                || type == Material.COARSE_DIRT || type == Material.FARMLAND
                || type == Material.DIRT_PATH || Tag.SAND.isTagged(type);
        };
        if (natural) {
            Player p = ctx.player();
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false, false));
        }
    }
}
