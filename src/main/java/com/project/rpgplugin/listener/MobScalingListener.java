package com.project.rpgplugin.listener;

import com.project.rpgplugin.core.difficulty.DifficultyService;
import com.project.rpgplugin.integration.AuraMobsBridge;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobScalingListener implements Listener {

    private final DifficultyService difficultyService;
    private final AuraMobsBridge auraMobs;

    public MobScalingListener(DifficultyService difficultyService, AuraMobsBridge auraMobs) {
        this.difficultyService = difficultyService;
        this.auraMobs = auraMobs;
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (!(e.getEntity() instanceof LivingEntity mob)) return;
        difficultyService.applyMobScaling(mob, e.getLocation());
        auraMobs.applyScaling(mob);
    }
}
