package com.project.rpgplugin.core.skill;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SkillRegistry {

    private final Map<String, Skill> byId = new LinkedHashMap<>();

    public void register(Skill s) {
        String id = s.id().toLowerCase();
        if (byId.containsKey(id)) {
            throw new IllegalStateException("Duplicate skill id: " + id);
        }
        byId.put(id, s);
    }

    public Optional<Skill> byId(String id) {
        return Optional.ofNullable(byId.get(id.toLowerCase()));
    }

    public List<Skill> byTier(SkillTier t) {
        return byId.values().stream()
            .filter(s -> s.tier() == t)
            .toList();
    }

    public List<Skill> byType(SkillType t) {
        return byId.values().stream()
            .filter(s -> s.type() == t)
            .toList();
    }

    public Collection<Skill> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public int size() {
        return byId.size();
    }

    public boolean contains(String id) {
        return byId.containsKey(id.toLowerCase());
    }
}
