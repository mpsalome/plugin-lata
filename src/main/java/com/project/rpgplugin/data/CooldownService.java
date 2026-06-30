package com.project.rpgplugin.data;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownService {

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void start(UUID playerId, String skillId, Duration duration) {
        long expiry = System.currentTimeMillis() + duration.toMillis();
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(skillId, expiry);
    }

    public boolean isOnCooldown(UUID playerId, String skillId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return false;
        Long expiry = playerCooldowns.get(skillId);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            playerCooldowns.remove(skillId);
            return false;
        }
        return true;
    }

    public long remaining(UUID playerId, String skillId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return 0;
        Long expiry = playerCooldowns.get(skillId);
        if (expiry == null) return 0;
        long rem = expiry - System.currentTimeMillis();
        return Math.max(0, rem);
    }

    public void clearAll(UUID playerId) {
        cooldowns.remove(playerId);
    }

    public void clearSkill(UUID playerId, String skillId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns != null) playerCooldowns.remove(skillId);
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        cooldowns.forEach((uuid, map) -> {
            map.entrySet().removeIf(e -> e.getValue() <= now);
            if (map.isEmpty()) cooldowns.remove(uuid);
        });
    }
}
