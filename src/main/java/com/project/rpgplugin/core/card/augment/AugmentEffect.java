package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

public interface AugmentEffect {
    void apply(Player p, RunState run, int stacks);
    void unapply(Player p, RunState run, int stacks);
}
