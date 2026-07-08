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

public class HydrationSkill extends AbstractSkill {

    public HydrationSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "hydration"; }

    @Override
    public SkillTier tier() { return SkillTier.BRONZE; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.POTION; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return MoveTrigger.always(); }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        if (!p.isInWaterOrBubbleColumn() && !p.getWorld().hasStorm()) return;
        if (p.getSaturation() >= 20 && p.getFoodLevel() >= 20) return;
        p.setFoodLevel(Math.min(20, p.getFoodLevel() + 1));
        p.setSaturation((float) Math.min(20, p.getSaturation() + 0.5f));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
    }
}
