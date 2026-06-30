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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;

public class ArchitectFocusSkill extends AbstractSkill {

    public ArchitectFocusSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "architect_focus"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.STONE_BRICKS; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.STONE_BRICKS); }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        consume(ctx, 1);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 3));
        p.playSound(p.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
        feedback(ctx, "<green>Foco do Arquiteto: Resistência IV!", null);
    }
}
