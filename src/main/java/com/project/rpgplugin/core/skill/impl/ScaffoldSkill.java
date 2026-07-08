package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Set;

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
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.INTERACT), ctx -> {
            if (!(ctx.sourceEvent() instanceof org.bukkit.event.player.PlayerInteractEvent e)) return false;
            if (!ctx.player().isSneaking()) return false;
            if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return false;
            return ctx.usedItem() != null && ctx.usedItem().getType() == Material.DIRT;
        }, "<gray>Agache e clique direito com terra (Catalisador)");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            ctx.player().playSound(ctx.player().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return;
        }
        Player p = ctx.player();
        p.setVelocity(new Vector(0, 1.0, 0));
        startCooldown(ctx);
        Location bloc = p.getLocation().clone().subtract(0, 1, 0);
        if (bloc.getBlock().getType() == Material.AIR) {
            bloc.getBlock().setType(Material.HAY_BLOCK);
            SchedulerUtil.runLater(services.plugin(), () -> {
                if (bloc.getBlock().getType() == Material.HAY_BLOCK) {
                    bloc.getBlock().setType(Material.AIR);
                }
            }, 100L);
        }
        p.playSound(p.getLocation(), Sound.BLOCK_GRASS_PLACE, 1.0f, 1.0f);
    }
}
