package com.project.rpgplugin.integration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public class MythicMobsBridge {

    private final boolean enabled;

    public MythicMobsBridge() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        this.enabled = plugin != null && plugin.isEnabled();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Optional<LivingEntity> trySpawnMob(String mobName, Location loc) {
        if (!enabled) return Optional.empty();
        try {
            Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            Object inst = mythicBukkitClass.getMethod("inst").invoke(null);
            Object mobManager = inst.getClass().getMethod("getMobManager").invoke(inst);

            Optional<?> mythicMob = (Optional<?>) mobManager.getClass()
                .getMethod("getMythicMob", String.class).invoke(mobManager, mobName);
            if (mythicMob.isEmpty()) return Optional.empty();

            Object activeMob = mobManager.getClass()
                .getMethod("spawnMob", String.class, Location.class).invoke(mobManager, mobName, loc);
            Object bukkitEntity = activeMob.getClass().getMethod("getEntity").invoke(activeMob);
            Object entity = bukkitEntity.getClass().getMethod("getBukkitEntity").invoke(bukkitEntity);
            if (entity instanceof LivingEntity le) return Optional.of(le);
        } catch (Exception ignored) {}
        return Optional.empty();
    }
}
