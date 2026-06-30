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
    public SkillTrigger trigger() { return MoveTrigger.whenOn(Tag.LEAVES); }

    @Override
    public void activate(SkillContext ctx) {
        Block under = ctx.targetBlock();
        if (under != null && (under.getType() == Material.GRASS_BLOCK || Tag.LEAVES.isTagged(under.getType()))) {
            Player p = ctx.player();
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false, false));
        }
    }
}
