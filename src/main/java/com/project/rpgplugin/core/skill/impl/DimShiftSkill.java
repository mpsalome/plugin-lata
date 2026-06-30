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
import org.bukkit.util.Vector;

import java.time.Duration;

public class DimShiftSkill extends AbstractSkill {

    public DimShiftSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "dim_shift"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.ENDER_PEARL; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.ENDER_PEARL); }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        consume(ctx, 1);
        Vector dir = p.getLocation().getDirection().normalize().multiply(8);
        p.teleport(p.getLocation().add(dir));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 2));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.0f);
        feedback(ctx, "§5Mudança Dimensional! Speed IV + Drawbacks.", null);
    }
}
