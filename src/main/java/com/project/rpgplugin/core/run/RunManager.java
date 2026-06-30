package com.project.rpgplugin.core.run;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.StatService;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RunManager {

    private final RPGPlugin plugin;
    private final CardRegistry cardRegistry;
    private final StatService statService;
    private final Map<UUID, RunState> activeRuns = new HashMap<>();

    public RunManager(RPGPlugin plugin, CardRegistry cardRegistry, StatService statService) {
        this.plugin = plugin;
        this.cardRegistry = cardRegistry;
        this.statService = statService;
    }

    public void startRun(Player p) {
        RunState run = new RunState(p.getUniqueId(), cardRegistry);
        activeRuns.put(p.getUniqueId(), run);
    }

    public RunState getRun(Player p) {
        return activeRuns.get(p.getUniqueId());
    }

    public boolean hasActiveRun(Player p) {
        return activeRuns.containsKey(p.getUniqueId());
    }

    public void endRun(Player p) {
        RunState run = activeRuns.remove(p.getUniqueId());
        if (run == null) return;
        for (String cardId : run.ownedCards()) {
            Card c = cardRegistry.byId(cardId).orElse(null);
            if (c != null) c.onRemove(p, run);
        }
        statService.resetToBaseline(p);
    }

    public RPGPlugin plugin() { return plugin; }
    public CardRegistry cardRegistry() { return cardRegistry; }
    public StatService statService() { return statService; }
}
