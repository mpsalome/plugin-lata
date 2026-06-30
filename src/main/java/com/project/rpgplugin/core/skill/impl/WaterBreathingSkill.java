package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
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
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.LAPIS_LAZULI); }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        consume(ctx, 1);
        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 300, 2));
        feedback(ctx, "§9Respiração Aquática Ativada!", null);
    }
}
