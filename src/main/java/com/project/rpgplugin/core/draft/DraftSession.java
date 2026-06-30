package com.project.rpgplugin.core.draft;

import com.project.rpgplugin.core.card.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DraftSession {

    private final UUID playerId;
    private final List<Card> options;
    private Card chosen;
    private int rerollsUsed;
    private boolean skipped;

    public DraftSession(UUID playerId, List<Card> options) {
        this.playerId = playerId;
        this.options = new ArrayList<>(options);
    }

    public UUID playerId() { return playerId; }

    public List<Card> options() { return Collections.unmodifiableList(options); }

    public Card chosen() { return chosen; }

    public void choose(int index) {
        if (index < 0 || index >= options.size()) return;
        this.chosen = options.get(index);
    }

    public void skip() {
        this.skipped = true;
    }

    public boolean isSkipped() { return skipped; }

    public boolean isDecided() { return chosen != null || skipped; }

    public int rerollsUsed() { return rerollsUsed; }

    public void useReroll() { rerollsUsed++; }

    public void replaceOptions(List<Card> newOptions) {
        this.options.clear();
        this.options.addAll(newOptions);
    }
}
