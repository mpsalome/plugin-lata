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

public class GraceSkill extends AbstractSkill {

    public GraceSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "grace"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.FEATHER; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(45); }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.FEATHER); }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "§cGraça da Pena em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        consume(ctx, 1);
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 200, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0));
        startCooldown(ctx);
        feedback(ctx, "§aGraça da Pena: Super Salto + Slow Fall!", Sound.ENTITY_CHICKEN_EGG);
    }
}
