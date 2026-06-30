package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;

public class FeastSkill extends AbstractSkill {

    public FeastSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "feast"; }

    @Override
    public SkillTier tier() { return SkillTier.BRONZE; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public org.bukkit.Material icon() { return org.bukkit.Material.OAK_LEAVES; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(org.bukkit.Tag.LEAVES); }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        if (p.getFoodLevel() < 20) {
            consume(ctx, 1);
            p.setFoodLevel(Math.min(20, p.getFoodLevel() + 2));
            p.setSaturation((float) Math.min(20, p.getSaturation() + 0.8));
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.2f);
            feedback(ctx, "§2Banquete de Folhas!", null);
        }
    }
}
