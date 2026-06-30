package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

public record AttributeEffect(String attribute, double addPerStack) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        // StatService handles attribute recalculation globally
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        // StatService handles attribute recalculation globally
    }

    public double totalForStacks(int stacks) {
        return addPerStack * stacks;
    }
}
