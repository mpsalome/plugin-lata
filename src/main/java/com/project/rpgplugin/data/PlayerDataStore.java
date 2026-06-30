package com.project.rpgplugin.data;

import com.project.rpgplugin.core.run.RunState;

import java.util.Optional;
import java.util.UUID;

public interface PlayerDataStore {
    void save(UUID playerId, RunState run);
    Optional<RunState> load(UUID playerId);
    void delete(UUID playerId);
    void flushAll();
}
