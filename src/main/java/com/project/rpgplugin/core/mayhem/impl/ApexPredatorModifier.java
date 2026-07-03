package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class ApexPredatorModifier extends BaseModifier {

    private BukkitTask task;
    private Mob apex;
    private final Random random = new Random();

    public ApexPredatorModifier() {
        super("apex_predator", ModifierSeverity.INSANE, ModifierTag.CHAOS, ModifierTag.OFFENSE);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        task = SchedulerUtil.runTimer(ctx.plugin(), () -> {
            if (apex == null || !apex.isValid() || apex.isDead()) {
                spawnApex(ctx);
            }
            if (apex != null && apex.isValid()) {
                Player nearest = findNearestPlayer(apex);
                if (nearest != null) {
                    apex.setTarget(nearest);
                }
            }
        }, 0L, 100L);
    }

    private void spawnApex(MayhemContext ctx) {
        World w = ctx.world();
        Player target = findRandomPlayer(w);
        if (target == null) return;
        Location loc = target.getLocation().clone().add(random.nextInt(20) - 10, 0, random.nextInt(20) - 10);
        apex = (Mob) w.spawnEntity(loc, EntityType.ZOGLIN);
        apex.customName(com.project.rpgplugin.util.Text.mm("<red><bold>Predador Alfa"));
        apex.setCustomNameVisible(true);
        var health = apex.getAttribute(Attribute.MAX_HEALTH);
        if (health != null) health.setBaseValue(80.0);
        apex.setHealth(80.0);
        var damage = apex.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue(12.0);
    }

    private Player findNearestPlayer(Mob mob) {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Player p : Bukkit.getOnlinePlayers()) {
            double dist = p.getLocation().distanceSquared(mob.getLocation());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    private Player findRandomPlayer(World w) {
        var players = w.getPlayers();
        if (players.isEmpty()) return null;
        return players.get(random.nextInt(players.size()));
    }

    @Override
    public void onDeactivate(MayhemContext ctx) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (apex != null && apex.isValid()) {
            apex.remove();
            apex = null;
        }
    }
}
