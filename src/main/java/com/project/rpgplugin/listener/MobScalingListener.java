package com.project.rpgplugin.listener;

import com.project.rpgplugin.core.difficulty.DifficultyService;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobScalingListener implements Listener {

    private final DifficultyService difficultyService;

    public MobScalingListener(DifficultyService difficultyService) {
        this.difficultyService = difficultyService;
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (!(e.getEntity() instanceof LivingEntity mob)) return;
        difficultyService.applyMobScaling(mob, e.getLocation());
    }
}
