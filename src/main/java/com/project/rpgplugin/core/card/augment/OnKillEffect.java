package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

public record OnKillEffect(String effect, double value) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        run.addOnKillEffect(this);
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        run.removeOnKillEffect(this);
    }
}
