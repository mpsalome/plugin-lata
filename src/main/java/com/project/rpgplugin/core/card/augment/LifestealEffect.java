package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

public record LifestealEffect(double healPct) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        run.addMultiplier("lifesteal", healPct);
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        run.removeMultiplier("lifesteal", healPct);
    }
}
