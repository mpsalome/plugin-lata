package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.ConsumeTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;

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
    public org.bukkit.Material icon() { return org.bukkit.Material.POTION; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return ConsumeTrigger.of(PotionType.WATER); }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        p.setFoodLevel(Math.min(20, p.getFoodLevel() + 1));
        p.setSaturation((float) Math.min(20, p.getSaturation() + 0.5));
        p.sendMessage(net.kyori.adventure.text.Component.text("§bHidratado! Fome regenerada."));
    }
}
