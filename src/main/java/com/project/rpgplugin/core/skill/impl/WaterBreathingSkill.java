package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.PassiveTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;

public class WaterBreathingSkill extends AbstractSkill {

    public WaterBreathingSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "water_breathing"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.LAPIS_LAZULI; }

    @Override
    public boolean passive() { return true; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return PassiveTrigger.instance(); }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        if (!p.isInWaterOrBubbleColumn() && !p.getLocation().getBlock().isLiquid()) return;
        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 100, 0, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 100, 0, true, false, false));
    }
}
