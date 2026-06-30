package com.project.rpgplugin.core.difficulty;

import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class DifficultyService {

    private final RunManager runManager;

    public DifficultyService(RunManager runManager) {
        this.runManager = runManager;
    }

    public double factorAround(Location loc) {
        double depthFactor = depthFactor(loc);
        double playerFactor = nearbyPlayerFactor(loc);
        return Math.min(5.0, 1.0 + depthFactor + playerFactor);
    }

    private double depthFactor(Location loc) {
        Collection<RunState> runs = runManager.getAllRuns();
        if (runs.isEmpty()) return 0.0;
        double avgMilestones = runs.stream().mapToInt(RunState::milestonesReached).average().orElse(0);
        return avgMilestones * 0.15;
    }

    private double nearbyPlayerFactor(Location loc) {
        Collection<Player> nearby = loc.getNearbyPlayers(48);
        if (nearby.isEmpty()) return 0.0;
        double avgLevel = nearby.stream().mapToInt(Player::getLevel).average().orElse(0);
        return avgLevel * 0.02;
    }

    public void applyMobScaling(LivingEntity mob, Location loc) {
        double factor = factorAround(loc);
        if (factor <= 1.0) return;
        var maxHealthAttr = mob.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double baseHp = maxHealthAttr.getBaseValue();
            maxHealthAttr.setBaseValue(baseHp * factor);
            mob.setHealth(Math.min(mob.getMaxHealth(), mob.getHealth() * factor));
        }
        var damageAttr = mob.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(damageAttr.getBaseValue() * factor);
        }
    }

    public void aposentarLegacyMultipliers() {
    }
}
