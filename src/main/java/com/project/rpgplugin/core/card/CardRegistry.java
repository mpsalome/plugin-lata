package com.project.rpgplugin.core.card;

import com.project.rpgplugin.core.run.RunState;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CardRegistry {

    private final Map<String, Card> byId = new LinkedHashMap<>();

    public void register(Card c) {
        String id = c.id().toLowerCase();
        if (byId.containsKey(id)) {
            throw new IllegalStateException("Duplicate card id: " + id);
        }
        byId.put(id, c);
    }

    public Optional<Card> byId(String id) {
        return Optional.ofNullable(byId.get(id.toLowerCase()));
    }

    public List<Card> byTier(CardTier t) {
        return byId.values().stream()
            .filter(c -> c.tier() == t)
            .toList();
    }

    public List<Card> byTag(CardTag tag) {
        return byId.values().stream()
            .filter(c -> c.tags().contains(tag))
            .toList();
    }

    public List<Card> offerable(RunState run) {
        return byId.values().stream()
            .filter(c -> c.offerable(run))
            .toList();
    }

    public List<Card> offerableByTier(RunState run, CardTier tier) {
        return byId.values().stream()
            .filter(c -> c.tier() == tier && c.offerable(run))
            .toList();
    }

    public Collection<Card> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public int size() {
        return byId.size();
    }

    public boolean contains(String id) {
        return byId.containsKey(id.toLowerCase());
    }
}
