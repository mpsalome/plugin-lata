package com.project.rpgplugin.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTracker {

    private static final Map<UUID, Long> LAST_DAMAGE_TIME = new ConcurrentHashMap<>();
    private static final long COMBAT_TIMEOUT_MS = 10_000;

    private CombatTracker() {}

    public static void recordDamage(UUID playerId) {
        LAST_DAMAGE_TIME.put(playerId, System.currentTimeMillis());
    }

    public static boolean isInCombat(UUID playerId) {
        Long last = LAST_DAMAGE_TIME.get(playerId);
        return last != null && (System.currentTimeMillis() - last) < COMBAT_TIMEOUT_MS;
    }

    public static void clear(UUID playerId) {
        LAST_DAMAGE_TIME.remove(playerId);
    }
}
