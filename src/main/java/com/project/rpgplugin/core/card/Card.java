package com.project.rpgplugin.core.card;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Set;

public interface Card {
    String id();
    CardTier tier();
    Set<CardTag> tags();
    CardKind kind();
    Material icon();
    int maxStacks();
    boolean offerable(RunState run);
    void onAcquire(Player p, RunState run);
    void onRemove(Player p, RunState run);
    default String nameKey() { return "card." + id() + ".name"; }
    default String descKey() { return "card." + id() + ".desc"; }
    default String requiredPlugin() { return null; }
}
