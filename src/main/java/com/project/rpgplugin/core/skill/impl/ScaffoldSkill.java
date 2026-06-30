package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.time.Duration;

public class ScaffoldSkill extends AbstractSkill {

    public ScaffoldSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "scaffold"; }

    @Override
    public SkillTier tier() { return SkillTier.BRONZE; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.DIRT; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(12); }

    @Override
    public SkillTrigger trigger() { return InteractTrigger.of(Material.DIRT); }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "§cSalto do Andaime em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        consume(ctx, 1);
        p.setVelocity(new Vector(0, 1.0, 0));
        startCooldown(ctx);
        Location bloc = p.getLocation().clone().subtract(0, 1, 0);
        if (bloc.getBlock().getType() == Material.AIR) {
            bloc.getBlock().setType(Material.HAY_BLOCK);
            org.bukkit.Bukkit.getScheduler().runTaskLater(services.plugin(), () -> {
                if (bloc.getBlock().getType() == Material.HAY_BLOCK) {
                    bloc.getBlock().setType(Material.AIR);
                }
            }, 100L);
        }
        feedback(ctx, "§aSalto do Andaime!", Sound.BLOCK_GRASS_PLACE);
    }
}
