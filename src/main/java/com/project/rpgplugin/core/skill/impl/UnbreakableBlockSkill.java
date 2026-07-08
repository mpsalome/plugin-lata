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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UnbreakableBlockSkill extends AbstractSkill {

    public UnbreakableBlockSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "unbreakable_block"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.CLAY_BALL; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(30); }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.INTERACT), ctx -> {
            if (!(ctx.sourceEvent() instanceof BlockPlaceEvent e)) return false;
            return ctx.player().isSneaking();
        }, "<gray>Agache e coloque um bloco para criar barreira");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "<red>Barreira em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        if (!(ctx.sourceEvent() instanceof BlockPlaceEvent event)) return;
        Location origin = event.getBlockPlaced().getLocation();
        List<Location> barrier = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location loc = origin.clone().add(x, 0, z);
                Block b = loc.getBlock();
                if (b.getType() == Material.AIR || b.isReplaceable()) {
                    b.setType(Material.OBSIDIAN);
                    barrier.add(loc);
                    services.addReinforcedBlock(loc);
                }
            }
        }
        startCooldown(ctx);
        p.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, origin.add(0, 1, 0), 50, 1.5, 0.5, 1.5, 0.1);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        feedback(ctx, "<bold>Barreira de Obsidiana ativada! 10s de protecao.</bold>", null);
        SchedulerUtil.runLater(services.plugin(), () -> {
            for (Location loc : barrier) {
                loc.getBlock().setType(Material.AIR);
                services.removeReinforcedBlock(loc);
            }
        }, 200L);
    }
}
