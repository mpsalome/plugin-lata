package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.BlockBreakTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class WoodcutterSkill extends AbstractSkill {

    public WoodcutterSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "woodcutter"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.IRON_BLOCK; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() {
        return BlockBreakTrigger.custom(ctx -> {
            Block block = ctx.targetBlock();
            return block != null && Tag.LOGS.isTagged(block.getType());
        }, "Quebre <gold>troncos</gold> para derrubar a arvore inteira");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (ctx.sourceEvent() == null) return;
        Block origin = ctx.targetBlock();
        if (origin == null) return;
        Player p = ctx.player();
        Set<Location> visited = new HashSet<>();
        Queue<Block> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin.getLocation());
        List<Block> logs = new ArrayList<>();
        while (!queue.isEmpty()) {
            Block current = queue.poll();
            logs.add(current);
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block neighbor = current.getRelative(x, y, z);
                        Location nl = neighbor.getLocation();
                        if (!visited.contains(nl) && Tag.LOGS.isTagged(neighbor.getType())) {
                            visited.add(nl);
                            queue.add(neighbor);
                        }
                    }
                }
            }
            if (logs.size() >= 32) break;
        }
        for (Block log : logs) {
            if (log.equals(origin)) continue;
            for (ItemStack drop : log.getDrops(p.getInventory().getItemInMainHand())) {
                log.getWorld().dropItemNaturally(log.getLocation(), drop);
            }
            log.setType(Material.AIR);
            log.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, log.getLocation().add(0.5, 0.5, 0.5),
                5, 0.2, 0.2, 0.2, 0.1, log.getType().createBlockData());
        }
    }
}
