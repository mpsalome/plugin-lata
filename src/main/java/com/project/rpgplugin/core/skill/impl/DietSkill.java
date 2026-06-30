package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;

public class DietSkill extends AbstractSkill {

    public DietSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "diet"; }

    @Override
    public SkillTier tier() { return SkillTier.BRONZE; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.COAL; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.COAL, Material.CHARCOAL); }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        if (p.getFoodLevel() < 20) {
            consume(ctx, 1);
            p.setFoodLevel(Math.min(20, p.getFoodLevel() + 4));
            p.setSaturation((float) Math.min(20, p.getSaturation() + 2.0));
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
            feedback(ctx, "§6Dieta de Carvão!", null);
        }
    }
}
