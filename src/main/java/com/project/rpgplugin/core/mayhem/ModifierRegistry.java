package com.project.rpgplugin.core.mayhem;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public final class ModifierRegistry {

    private final Map<String, Modifier> byId = new LinkedHashMap<>();
    private final Random random = new Random();

    public void register(Modifier m) {
        String id = m.id().toLowerCase();
        if (byId.containsKey(id)) {
            throw new IllegalStateException("Duplicate modifier id: " + id);
        }
        byId.put(id, m);
    }

    public Optional<Modifier> byId(String id) {
        return Optional.ofNullable(byId.get(id.toLowerCase()));
    }

    public List<Modifier> bySeverity(ModifierSeverity severity) {
        return byId.values().stream()
            .filter(m -> m.severity() == severity)
            .toList();
    }

    public List<Modifier> byTag(ModifierTag tag) {
        return byId.values().stream()
            .filter(m -> m.tags().contains(tag))
            .toList();
    }

    public Modifier rollOne(Set<ModifierSeverity> allowed, Set<String> active) {
        List<Modifier> candidates = byId.values().stream()
            .filter(m -> allowed.contains(m.severity()))
            .filter(m -> !active.contains(m.id()))
            .filter(m -> m.compatibleWith(active))
            .toList();
        if (candidates.isEmpty()) return null;
        return candidates.get(random.nextInt(candidates.size()));
    }

    public Collection<Modifier> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public int size() {
        return byId.size();
    }
}
